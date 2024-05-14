package org.codehaus.mojo.flatten;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import javax.inject.Inject;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.interpolation.ModelInterpolator;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.model.profile.DefaultProfileSelector;
import org.apache.maven.model.profile.ProfileInjector;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.mojo.flatten.cifriendly.CiInterpolator;
import org.codehaus.mojo.flatten.extendedinterpolation.ExtendedModelInterpolator;
import org.codehaus.mojo.flatten.model.resolution.FlattenModelResolver;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.xml.sax.Attributes;
import org.xml.sax.ext.DefaultHandler2;

/**
 * This MOJO realizes the goal <code>flatten</code> that generates the flattened POM and {@link #isUpdatePomFile()
 * potentially updates the POM file} so that the current {@link MavenProject}'s {@link MavenProject#getFile() file}
 * points to the flattened POM instead of the original <code>pom.xml</code> file. The flattened POM is a reduced version
 * of the original POM with the focus to contain only the important information for consuming it. Therefore information
 * that is only required for maintenance by developers and to build the project artifact(s) are stripped. Starting from
 * here we specify how the flattened POM is created from the original POM and its project:<br>
 * <table>
 * <caption></caption>
 * <tr>
 * <th>Element</th>
 * <th>Transformation</th>
 * <th>Note</th>
 * </tr>
 * <tr>
 * <td>{@link Model#getModelVersion() modelVersion}</td>
 * <td>Fixed to "4.0.0"</td>
 * <td>New maven versions will once be able to evolve the model version without incompatibility to older versions if
 * flattened POMs get deployed.</td>
 * </tr>
 * <tr>
 * <td>{@link Model#getGroupId() groupId}<br>
 * {@link Model#getArtifactId() artifactId}<br>
 * {@link Model#getVersion() version}<br>
 * {@link Model#getPackaging() packaging}</td>
 * <td>resolved</td>
 * <td>copied to the flattened POM but with inheritance from {@link Model#getParent() parent} as well as with all
 * variables and defaults resolved. These elements are technically required for consumption.</td>
 * </tr>
 * <tr>
 * <td>{@link Model#getLicenses() licenses}</td>
 * <td>resolved</td>
 * <td>copied to the flattened POM but with inheritance from {@link Model#getParent() parent} as well as with all
 * variables and defaults resolved. The licenses would not be required in flattened POM. However, they make sense for
 * publication and deployment and are important for consumers of your artifact.</td>
 * </tr>
 * <tr>
 * <td>{@link Model#getDependencies() dependencies}</td>
 * <td>resolved specially</td>
 * <td>flattened POM contains the actual dependencies of the project. Test dependencies are removed. Variables and
 * {@link Model#getDependencyManagement() dependencyManagement} is resolved to get fixed dependency attributes
 * (especially {@link Dependency#getVersion() version}). If {@link #isEmbedBuildProfileDependencies()
 * embedBuildProfileDependencies} is set to <code>true</code>, then also build-time driven {@link Profile}s will be
 * evaluated and may add {@link Dependency dependencies}. For further details see {@link Profile}s below.</td>
 * </tr>
 * <tr>
 * <td>{@link Model#getProfiles() profiles}</td>
 * <td>resolved specially</td>
 * <td>only the {@link Activation} and the {@link Dependency dependencies} of a {@link Profile} are copied to the
 * flattened POM. If you set the parameter {@link #isEmbedBuildProfileDependencies() embedBuildProfileDependencies} to
 * <code>true</code> then only profiles {@link Activation activated} by {@link Activation#getJdk() JDK} or
 * {@link Activation#getOs() OS} will be added to the flattened POM while the other profiles are triggered by the
 * current build setup and if activated their impact on dependencies is embedded into the resulting flattened POM.</td>
 * </tr>
 * <tr>
 * <td>{@link Model#getName() name}<br>
 * {@link Model#getDescription() description}<br>
 * {@link Model#getUrl() url}<br>
 * {@link Model#getInceptionYear() inceptionYear}<br>
 * {@link Model#getOrganization() organization}<br>
 * {@link Model#getScm() scm}<br>
 * {@link Model#getDevelopers() developers}<br>
 * {@link Model#getContributors() contributors}<br>
 * {@link Model#getMailingLists() mailingLists}<br>
 * {@link Model#getPluginRepositories() pluginRepositories}<br>
 * {@link Model#getIssueManagement() issueManagement}<br>
 * {@link Model#getCiManagement() ciManagement}<br>
 * {@link Model#getDistributionManagement() distributionManagement}</td>
 * <td>configurable</td>
 * <td>Will be stripped from the flattened POM by default. You can configure all of the listed elements inside
 * <code>pomElements</code> that should be kept in the flattened POM (e.g. {@literal
 * <pomElements><name/><description/><developers/><contributors/></pomElements>}). For common use-cases there are
 * predefined modes available via the parameter <code>flattenMode</code> that should be used in preference.</td>
 * </tr>
 * <tr>
 * <td>{@link Model#getPrerequisites() prerequisites}</td>
 * <td>configurable</td>
 * <td>Like above but by default NOT removed if packaging is "maven-plugin".</td>
 * </tr>
 * <tr>
 * <td>{@link Model#getRepositories() repositories}</td>
 * <td>configurable</td>
 * <td>Like two above but by default NOT removed. If you want have it removed, you need to use the parameter
 * <code>pomElements</code> and configure the child element <code>repositories</code> with value <code>flatten</code>.
 * </td>
 * </tr>
 * <tr>
 * <td>{@link Model#getParent() parent}<br>
 * {@link Model#getBuild() build}<br>
 * {@link Model#getDependencyManagement() dependencyManagement}<br>
 * {@link Model#getProperties() properties}<br>
 * {@link Model#getModules() modules}<br>
 * {@link Model#getReporting() reporting}</td>
 * <td>configurable</td>
 * <td>These elements should typically be completely stripped from the flattened POM. However for ultimate flexibility
 * (e.g. if you only want to resolve variables in a POM with packaging pom) you can also configure to keep these
 * elements. We strictly recommend to use this feature with extreme care and only if packaging is pom (for "Bill of
 * Materials"). In the latter case you configure the parameter <code>flattenMode</code> to the value
 * <code>bom</code>.<br>
 * If the <code>build</code> element contains plugins in the <code>build/plugins</code> section which are configured to
 * load <a href="http://maven.apache.org/pom.html#Extensions">extensions</a>, a reduced <code>build</code> element
 * containing these plugins will be kept in the flattened pom.</td>
 * </tr>
 * </table>
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 */
@Mojo(name = "flatten", requiresDependencyCollection = ResolutionScope.RUNTIME, threadSafe = true)
public class FlattenMojo extends AbstractFlattenMojo {

    private static final int INITIAL_POM_WRITER_SIZE = 4096;

    /**
     * The {@link Settings} used to get active profile properties.
     */
    @Parameter(defaultValue = "${settings}", readonly = true, required = true)
    private Settings settings;

    /**
     * The {@link MavenSession} used to get user properties.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    /**
     * The Maven Project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The flag to indicate if the generated flattened POM shall be set as POM file to the current project. By default
     * this is only done for projects with packaging other than <code>pom</code>. You may want to also do this for
     * <code>pom</code> packages projects by setting this parameter to <code>true</code> or you can use
     * <code>false</code> in order to only generate the flattened POM but never set it as POM file. If
     * <code>flattenMode</code> is set to bom the default value will be <code>true</code>.
     */
    @Parameter(property = "updatePomFile")
    private Boolean updatePomFile;

    /**
     * Profiles activated by OS or JDK are valid ways to have different dependencies per environment. However, profiles
     * activated by property of file are less clear. When setting this parameter to <code>true</code>, the latter
     * dependencies will be written as direct dependencies of the project. <strong>This is not how Maven2 and Maven3
     * handles dependencies</strong>. When keeping this property <code>false</code>, all profiles will stay in the
     * flattened-pom.
     */
    @Parameter(defaultValue = "false")
    private Boolean embedBuildProfileDependencies;

    /**
     * The {@link MojoExecution} used to get access to the raw configuration of {@link #pomElements} as empty tags are
     * mapped to null.
     */
    @Parameter(defaultValue = "${mojo}", readonly = true, required = true)
    private MojoExecution mojoExecution;

    /**
     * The {@link Model} that defines how to handle additional POM elements. Please use <code>flattenMode</code> in
     * preference if possible. This parameter is only for ultimate flexibility.
     */
    @Parameter(required = false)
    private FlattenDescriptor pomElements;

    /**
     * Dictates whether dependency exclusions stanzas should be included in the flattened POM. By default exclusions
     * will be included in the flattened POM but if you wish to omit exclusions stanzas from being present then set
     * this configuration property to <code>true</code>.
     *
     * @since 1.3.0
     */
    @Parameter(defaultValue = "false", required = false)
    private boolean omitExclusions;

    /**
     * The different possible values for flattenMode:
     * <table border="1" summary="">
     * <thead>
     * <tr>
     * <td>Mode</td>
     * <td>Description</td>
     * </tr>
     * </thead> <tbody>
     * <tr>
     * <td>oss</td>
     * <td>For Open-Source-Software projects that want to keep all {@link FlattenDescriptor optional POM elements}
     * except for {@link Model#getRepositories() repositories} and {@link Model#getPluginRepositories()
     * pluginRepositories}.</td>
     * <tr>
     * <td>ossrh</td>
     * <td>Keeps all {@link FlattenDescriptor optional POM elements} that are required for
     * <a href="https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide">OSS
     * Repository-Hosting</a>.</td>
     * </tr>
     * <tr>
     * <td>bom</td>
     * <td>Like <code>ossrh</code> but additionally keeps {@link Model#getDependencyManagement() dependencyManagement}
     * and {@link Model#getProperties() properties}. Especially it will keep the {@link Model#getDependencyManagement()
     * dependencyManagement} <em>as-is</em> without resolving parent influences and import-scoped dependencies. This is
     * useful if your POM represents a <a href=
     * "http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Importing_Dependencies"
     * >BOM (Bill Of Material)</a> and you do not want to deploy it as is (to remove parent and resolve version
     * variables, etc.).</td>
     * </tr>
     * <tr>
     * <td>defaults</td>
     * <td>The default mode that removes all {@link FlattenDescriptor optional POM elements} except
     * {@link Model#getRepositories() repositories}.</td>
     * </tr>
     * <tr>
     * <td>clean</td>
     * <td>Removes all {@link FlattenDescriptor optional POM elements}.</td>
     * </tr>
     * <tr>
     * <td>fatjar</td>
     * <td>Removes all {@link FlattenDescriptor optional POM elements} and all {@link Model#getDependencies()
     * dependencies}.</td>
     * </tr>
     * <tr>
     * <td>resolveCiFriendliesOnly</td>
     * <td>Only resolves variables revision, sha1 and changelist. Keeps everything else.
     * See <a href="https://maven.apache.org/maven-ci-friendly.html">Maven CI Friendly</a> for further details.</td>
     * </tr>
     * </tbody>
     * </table>
     */
    @Parameter(property = "flatten.mode", required = false)
    private FlattenMode flattenMode;

    /**
     * The different possible values for flattenDependencyMode:
     * <table border="1" summary="">
     * <thead>
     * <tr>
     * <td>Mode</td>
     * <td>Description</td>
     * </tr>
     * </thead><tbody>
     * <tr>
     * <td>direct</td>
     * <td>Flatten only the direct dependency versions.
     * This is the default mode and compatible with Flatten Plugin prior to 1.2.0.</td>
     * <tr>
     * <td>all</td>
     * <td><p>Flatten both direct and transitive dependencies. This will examine the full dependency tree, and pull up
     * all transitive dependencies as a direct dependency, and setting their versions appropriately.</p>
     * <p>This is recommended if you are releasing a library that uses dependency management to manage dependency
     * versions.</p></td>
     * </tr>
     * </tbody>
     * </table>
     */
    @Parameter(property = "flatten.dependency.mode", required = false)
    private FlattenDependencyMode flattenDependencyMode;

    /**
     * The core maven model readers/writers are discarding the comments of the pom.xml.
     * By setting keepCommentsInPom to true the current comments are moved to the flattened pom.xml.
     * Default value is false (= not re-adding comments).
     *
     * @since 1.3.0
     */
    @Parameter(property = "flatten.dependency.keepComments", required = false, defaultValue = "false")
    private boolean keepCommentsInPom;

    /**
     * If {@code true} the flatten goal will be skipped.
     *
     * @since 1.6.0
     */
    @Parameter(property = "flatten.flatten.skip", defaultValue = "false")
    private boolean skipFlatten;

    /**
     * The default operation to use when no element handling is given. Defaults to <code>flatten</code>.
     *
     * @since 1.6.0
     */
    @Parameter(property = "flatten.dependency.defaultOperation", required = false, defaultValue = "flatten")
    private ElementHandling defaultOperation;

    @Inject
    private DirectDependenciesInheritanceAssembler inheritanceAssembler;

    /**
     * The {@link ModelInterpolator} used to resolve variables.
     */
    @Inject
    private ExtendedModelInterpolator extendedModelInterpolator;

    /**
     * The {@link ModelInterpolator} used to resolve variables.
     */
    @Inject
    private CiInterpolator modelCiFriendlyInterpolator;

    @Inject
    private ModelBuilderThreadSafetyWorkaround modelBuilderThreadSafetyWorkaround;

    @Inject
    private ArtifactHandlerManager artifactHandlerManager;

    @Inject
    private RepositorySystem repositorySystem;

    /**
     * The constructor.
     */
    public FlattenMojo() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (shouldSkip()) {
            getLog().info("Flatten skipped.");
            return;
        }

        getLog().info("Generating flattened POM of project " + this.project.getId() + "...");

        inheritanceAssembler.flattenDependencyMode = this.flattenDependencyMode;

        File originalPomFile = this.project.getFile();
        KeepCommentsInPom commentsOfOriginalPomFile = null;
        if (keepCommentsInPom) {
            commentsOfOriginalPomFile = KeepCommentsInPom.create(getLog(), originalPomFile);
        }
        Model flattenedPom = createFlattenedPom(originalPomFile);
        String headerComment = extractHeaderComment(originalPomFile);

        File flattenedPomFile = getFlattenedPomFile();
        writePom(flattenedPom, flattenedPomFile, headerComment, commentsOfOriginalPomFile);

        if (isUpdatePomFile()) {
            this.project.setPomFile(flattenedPomFile);
            this.project.setOriginalModel(flattenedPom);
        }
    }

    @Override
    protected boolean shouldSkipGoal() {
        return skipFlatten;
    }

    /**
     * This method extracts the XML header comment if available.
     *
     * @param xmlFile is the XML {@link File} to parse.
     * @return the XML comment between the XML header declaration and the root tag or <code>null</code> if NOT
     * available.
     * @throws MojoExecutionException if anything goes wrong.
     */
    protected String extractHeaderComment(File xmlFile) throws MojoExecutionException {

        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            SaxHeaderCommentHandler handler = new SaxHeaderCommentHandler();
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
            parser.parse(xmlFile, handler);
            return handler.getHeaderComment();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to parse XML from " + xmlFile, e);
        }
    }

    /**
     * Writes the given POM {@link Model} to the given {@link File}.
     *
     * @param pom           the {@link Model} of the POM to write.
     * @param pomFile       the {@link File} where to write the given POM will be written to.
     *                      {@link File#getParentFile()
     *                      Parent directories} are {@link File#mkdirs() created} automatically.
     * @param headerComment is the content of a potential XML comment at the top of the XML (after XML declaration and
     *                      before root tag). May be <code>null</code> if not present and to be omitted in target POM.
     * @throws MojoExecutionException if the operation failed (e.g. due to an {@link IOException}).
     */
    protected void writePom(Model pom, File pomFile, String headerComment, KeepCommentsInPom anOriginalCommentsPath)
            throws MojoExecutionException {

        File parentFile = pomFile.getParentFile();
        if (!parentFile.exists()) {
            boolean success = parentFile.mkdirs();
            if (!success) {
                throw new MojoExecutionException("Failed to create directory " + pomFile.getParent());
            }
        }
        // MavenXpp3Writer could internally add the comment but does not expose such feature to API!
        // Instead we have to write POM XML to String and do post processing on that :(
        MavenXpp3Writer pomWriter = new MavenXpp3Writer();
        StringWriter stringWriter = new StringWriter(INITIAL_POM_WRITER_SIZE);
        try {
            pomWriter.write(stringWriter, pom);
        } catch (IOException e) {
            throw new MojoExecutionException("Internal I/O error!", e);
        }
        StringBuffer buffer = stringWriter.getBuffer();
        if (!StringUtils.isEmpty(headerComment)) {
            int projectStartIndex = buffer.indexOf("<project");
            if (projectStartIndex >= 0) {
                buffer.insert(projectStartIndex, "<!--" + headerComment + "-->\n");
            } else {
                getLog().warn("POM XML post-processing failed: no project tag found!");
            }
        }
        String xmlString;
        if (anOriginalCommentsPath == null) {
            xmlString = buffer.toString();
        } else {
            xmlString = anOriginalCommentsPath.restoreOriginalComments(buffer.toString(), pom.getModelEncoding());
        }
        writeStringToFile(xmlString, pomFile, pom.getModelEncoding());
    }

    /**
     * Writes the given <code>data</code> to the given <code>file</code> using the specified <code>encoding</code>.
     *
     * @param data     is the {@link String} to write.
     * @param file     is the {@link File} to write to.
     * @param encoding is the encoding to use for writing the file.
     * @throws MojoExecutionException if anything goes wrong.
     */
    protected void writeStringToFile(String data, File file, String encoding) throws MojoExecutionException {
        if (!"\n".equals(System.lineSeparator())) {
            data = data.replace("\n", System.lineSeparator());
        }
        byte[] binaryData;

        try {
            binaryData = data.getBytes(encoding);
            if (file.isFile() && file.canRead() && file.length() == binaryData.length) {
                try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                    byte[] buffer = new byte[binaryData.length];
                    inputStream.read(buffer);
                    if (Arrays.equals(buffer, binaryData)) {
                        getLog().debug("Arrays.equals( buffer, binaryData ) ");
                        return;
                    }
                    getLog().debug("Not Arrays.equals( buffer, binaryData ) ");
                } catch (IOException e) {
                    // ignore those exceptions, we will overwrite the file
                    getLog().debug("Issue reading file: " + file.getPath(), e);
                }
            } else {
                getLog().debug("file: " + file + ",file.length(): " + file.length() + ", binaryData.length: "
                        + binaryData.length);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("cannot read String as bytes", e);
        }
        try (OutputStream outStream = Files.newOutputStream(file.toPath())) {
            outStream.write(binaryData);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write to " + file, e);
        }
    }

    /**
     * This method creates the flattened POM what is the main task of this plugin.
     *
     * @param pomFile is the name of the original POM file to read and transform.
     * @return the {@link Model} of the flattened POM.
     * @throws MojoExecutionException if anything goes wrong (e.g. POM can not be processed).
     * @throws MojoFailureException   if anything goes wrong (logical error).
     */
    protected Model createFlattenedPom(File pomFile) throws MojoExecutionException, MojoFailureException {

        ModelsFactory modelsFactory = new ModelsFactory(pomFile);

        Model flattenedPom = new Model();

        // keep original encoding (we could also normalize to UTF-8 here)
        String modelEncoding = modelsFactory.getEffectivePom().getModelEncoding();
        if (StringUtils.isEmpty(modelEncoding)) {
            modelEncoding = "UTF-8";
        }
        flattenedPom.setModelEncoding(modelEncoding);

        FlattenDescriptor descriptor = getFlattenDescriptor();

        for (PomProperty<?> property : PomProperty.getPomProperties()) {
            if (property.isElement()) {
                Model sourceModel = getSourceModel(descriptor, property, modelsFactory);
                if (sourceModel == null) {
                    if (property.isRequired()) {
                        throw new MojoFailureException(
                                "Property " + property.getName() + " is required and can not be removed!");
                    }
                } else {
                    property.copy(sourceModel, flattenedPom);
                }
            }
        }

        return flattenedPom;
    }

    Model createEffectivePom(ModelBuildingRequest buildingRequest) throws MojoExecutionException {
        try {
            return createEffectivePomImpl(buildingRequest);
        } catch (Exception e) {
            throw new MojoExecutionException("failed to create the effective pom", e);
        }
    }

    private Model createCleanPom(Model effectivePom) throws MojoExecutionException {
        try {
            return createCleanPomImpl(effectivePom);
        } catch (Exception e) {
            throw new MojoExecutionException("failed to create a clean pom", e);
        }
    }

    private Model createInterpolatedPom(
            ModelBuildingRequest buildingRequest, Model originalPom, File projectDirectory) {
        LoggingModelProblemCollector problems = new LoggingModelProblemCollector(getLog());
        if (this.flattenMode == FlattenMode.resolveCiFriendliesOnly) {
            return this.modelCiFriendlyInterpolator.interpolateModel(
                    originalPom, projectDirectory, buildingRequest, problems);
        }
        return extendedModelInterpolator.interpolateModel(originalPom, projectDirectory, buildingRequest, problems);
    }

    private Model createExtendedInterpolatedPom(
            ModelBuildingRequest buildingRequest, Model originalPom, Model effectivePom, File projectDirectory) {
        LoggingModelProblemCollector problems = new LoggingModelProblemCollector(getLog());
        if (this.flattenMode == FlattenMode.resolveCiFriendliesOnly) {
            return this.modelCiFriendlyInterpolator.interpolateModel(
                    originalPom, projectDirectory, buildingRequest, problems);
        }
        final Model extendedInterpolatedPom = extendedModelInterpolator
                .interpolateModel(effectivePom, originalPom, projectDirectory, buildingRequest, problems)
                .clone();

        // interpolate parent explicitly because parent is excluded from interpolation
        if (effectivePom.getParent() != null) {
            extendedInterpolatedPom.setParent(effectivePom.getParent().clone());
        }

        return extendedInterpolatedPom;
    }

    private Model createOriginalPom(File pomFile) throws MojoExecutionException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            return reader.read(Files.newInputStream(pomFile.toPath()));
        } catch (IOException | XmlPullParserException e) {
            throw new MojoExecutionException("Error reading raw model.", e);
        }
    }

    /**
     * This method creates the clean POM as a {@link Model} where to copy elements from that shall be
     * {@link ElementHandling#flatten flattened}. Will be mainly empty but contains some the minimum elements that have
     * to be kept in flattened POM.
     *
     * @param effectivePom is the effective POM.
     * @return the clean POM.
     * @throws MojoExecutionException if anything goes wrong.
     */
    protected Model createCleanPomImpl(Model effectivePom) throws MojoExecutionException {
        Model cleanPom = new Model();

        cleanPom.setGroupId(effectivePom.getGroupId());
        cleanPom.setArtifactId(effectivePom.getArtifactId());
        cleanPom.setVersion(effectivePom.getVersion());
        cleanPom.setPackaging(effectivePom.getPackaging());
        cleanPom.setLicenses(effectivePom.getLicenses());
        // fixed to 4.0.0 forever :)
        cleanPom.setModelVersion("4.0.0");

        // plugins with extensions must stay
        Build build = effectivePom.getBuild();
        if (build != null) {
            for (Plugin plugin : build.getPlugins()) {
                if (plugin.isExtensions()) {
                    Build cleanBuild = cleanPom.getBuild();
                    if (cleanBuild == null) {
                        cleanBuild = new Build();
                        cleanPom.setBuild(cleanBuild);
                    }
                    Plugin cleanPlugin = new Plugin();
                    cleanPlugin.setGroupId(plugin.getGroupId());
                    cleanPlugin.setArtifactId(plugin.getArtifactId());
                    cleanPlugin.setVersion(plugin.getVersion());
                    cleanPlugin.setExtensions(true);
                    cleanBuild.addPlugin(cleanPlugin);
                }
            }
        }

        // transform profiles...
        Dependencies managedDependencies = new Dependencies();
        if (effectivePom.getDependencyManagement() != null
                && effectivePom.getDependencyManagement().getDependencies() != null) {
            managedDependencies.addAll(effectivePom.getDependencyManagement().getDependencies());
        }

        for (Profile profile : effectivePom.getProfiles()) {
            if (!isEmbedBuildProfileDependencies() || !isBuildTimeDriven(profile.getActivation())) {
                if (!isEmpty(profile.getDependencies()) || !isEmpty(profile.getRepositories())) {
                    List<Dependency> strippedDependencies = new ArrayList<>();
                    for (Dependency dep : profile.getDependencies()) {
                        Dependency parsedDep = dep.clone();
                        if (managedDependencies.contains(parsedDep)) {
                            parsedDep.setVersion(
                                    managedDependencies.resolve(parsedDep).getVersion());
                            String managedDepScope =
                                    managedDependencies.resolve(parsedDep).getScope();
                            if (managedDepScope != null) {
                                parsedDep.setScope(managedDepScope);
                            }
                            if (parsedDep.getScope() == null) {
                                parsedDep.setScope("compile");
                            }
                            String managedDepOptional =
                                    managedDependencies.resolve(parsedDep).getOptional();
                            if (managedDepOptional != null) {
                                parsedDep.setOptional(managedDepOptional);
                            }
                            if (parsedDep.getOptional() == null) {
                                parsedDep.setOptional("false");
                            }
                        }
                        Dependency flattenedDep = createFlattenedDependency(parsedDep);
                        if (flattenedDep != null) {
                            strippedDependencies.add(flattenedDep);
                        }
                    }
                    if (!strippedDependencies.isEmpty() || !isEmpty(profile.getRepositories())) {
                        Profile strippedProfile = new Profile();
                        strippedProfile.setId(profile.getId());
                        strippedProfile.setActivation(profile.getActivation());
                        strippedProfile.setDependencies(strippedDependencies.isEmpty() ? null : strippedDependencies);
                        strippedProfile.setRepositories(profile.getRepositories());
                        cleanPom.addProfile(strippedProfile);
                    }
                }
            }
        }

        // transform dependencies...
        List<Dependency> dependencies = createFlattenedDependencies(effectivePom);
        cleanPom.setDependencies(dependencies);
        return cleanPom;
    }

    private Model getSourceModel(FlattenDescriptor descriptor, PomProperty<?> property, ModelsFactory modelsFactory)
            throws MojoExecutionException {

        ElementHandling handling = descriptor.getHandling(property);
        getLog().debug("Property " + property.getName() + " will be handled using " + handling + " in flattened POM.");
        return modelsFactory.getModel(handling);
    }

    /**
     * Creates a flattened {@link List} of {@link Repository} elements where those from super-POM are omitted.
     *
     * @param repositories is the {@link List} of {@link Repository} elements. May be <code>null</code>.
     * @return the flattened {@link List} of {@link Repository} elements or <code>null</code> if <code>null</code> was
     * given.
     */
    protected static List<Repository> createFlattenedRepositories(List<Repository> repositories) {
        if (repositories != null) {
            List<Repository> flattenedRepositories = new ArrayList<>(repositories.size());
            for (Repository repo : repositories) {
                // filter inherited repository section from super POM (see MOJO-2042)...
                if (!isCentralRepositoryFromSuperPom(repo)) {
                    flattenedRepositories.add(repo);
                }
            }
            return flattenedRepositories;
        }
        return null;
    }

    private FlattenDescriptor getFlattenDescriptor() {
        FlattenDescriptor descriptor = this.pomElements;
        if (descriptor == null) {
            FlattenMode mode = this.flattenMode;
            if (mode == null) {
                mode = FlattenMode.defaults;
            } else if (this.flattenMode == FlattenMode.minimum) {
                getLog().warn("FlattenMode " + FlattenMode.minimum + " is deprecated!");
            }
            descriptor = mode.getDescriptor();
            if ("maven-plugin".equals(this.project.getPackaging())) {
                descriptor.setPrerequisites(ElementHandling.expand);
            }
        } else {
            if (descriptor.isEmpty()) {
                // legacy approach...
                // Can't use Model itself as empty elements are never null, so you can't recognize if it was set or not
                Xpp3Dom rawDescriptor = this.mojoExecution.getConfiguration().getChild("pomElements");
                descriptor = new FlattenDescriptor(rawDescriptor);
            }

            if (this.flattenMode != null) {
                descriptor = descriptor.merge(this.flattenMode.getDescriptor());
            }
        }
        descriptor.setDefaultOperation(defaultOperation);
        return descriptor;
    }

    /**
     * This method determines if the given {@link Repository} section is identical to what is defined from the super
     * POM.
     *
     * @param repo is the {@link Repository} section to check.
     * @return <code>true</code> if maven central default configuration, <code>false</code> otherwise.
     */
    private static boolean isCentralRepositoryFromSuperPom(Repository repo) {
        if (repo != null) {
            if ("central".equals(repo.getId())) {
                RepositoryPolicy snapshots = repo.getSnapshots();
                return snapshots != null && !snapshots.isEnabled();
            }
        }
        return false;
    }

    private ModelBuildingRequest createModelBuildingRequest(File pomFile) {

        RequestTrace trace = new RequestTrace(pomFile);
        String context = mojoExecution.getExecutionId();
        FlattenModelResolver resolver = new FlattenModelResolver(
                session.getRepositorySession(),
                repositorySystem,
                trace,
                context,
                project.getRemoteProjectRepositories(),
                getReactorModelsFromSession());
        Properties userAndActiveExternalProfilesProperties = new Properties();
        userAndActiveExternalProfilesProperties.putAll(this.session.getUserProperties());
        this.settings.getProfiles().stream()
                .filter(p -> this.settings.getActiveProfiles().contains(p.getId())
                        && !this.session.getRequest().getInactiveProfiles().contains(p.getId()))
                .forEach(
                        activeProfile -> userAndActiveExternalProfilesProperties.putAll(activeProfile.getProperties()));

        List<String> activeProfiles = Stream.concat(
                        this.session.getRequest().getActiveProfiles().stream(),
                        this.settings.getActiveProfiles().stream())
                .collect(Collectors.toList());

        return new DefaultModelBuildingRequest()
                .setUserProperties(userAndActiveExternalProfilesProperties)
                .setSystemProperties(System.getProperties())
                .setPomFile(pomFile)
                .setModelResolver(resolver)
                .setActiveProfileIds(activeProfiles)
                .setInactiveProfileIds(this.session.getRequest().getInactiveProfiles());
    }

    private List<MavenProject> getReactorModelsFromSession() {
        // robust approach for 'special' environments like m2e (Eclipse plugin) which don't provide allProjects
        List<MavenProject> models = this.session.getAllProjects();
        if (models == null) {
            models = this.session.getProjects();
        }
        if (models == null) {
            models = Collections.emptyList();
        }
        return models;
    }

    /**
     * Creates the effective POM for the given <code>pomFile</code> trying its best to match the core maven behaviour.
     *
     * @param buildingRequest {@link ModelBuildingRequest}
     * @return the parsed and calculated effective POM.
     * @throws MojoExecutionException if anything goes wrong.
     */
    protected Model createEffectivePomImpl(ModelBuildingRequest buildingRequest) throws MojoExecutionException {
        ModelBuildingResult buildingResult;
        try {
            ProfileInjector customInjector = (model, profile, request, problems) -> {
                Properties merged = new Properties();
                merged.putAll(model.getProperties());
                merged.putAll(profile.getProperties());
                model.setProperties(merged);

                // Copied from org.apache.maven.model.profile.DefaultProfileInjector
                DependencyManagement profileDependencyManagement = profile.getDependencyManagement();
                if (profileDependencyManagement != null) {
                    DependencyManagement modelDependencyManagement = model.getDependencyManagement();
                    if (modelDependencyManagement == null) {
                        modelDependencyManagement = new DependencyManagement();
                        model.setDependencyManagement(modelDependencyManagement);
                    }

                    List<Dependency> src = profileDependencyManagement.getDependencies();
                    if (!src.isEmpty()) {
                        List<Dependency> tgt = modelDependencyManagement.getDependencies();
                        Map<Object, Dependency> mergedDependencies = new LinkedHashMap<>((src.size() + tgt.size()) * 2);

                        for (Dependency element : tgt) {
                            mergedDependencies.put(element.getManagementKey(), element);
                        }

                        for (Dependency element : src) {
                            String key = element.getManagementKey();
                            if (!mergedDependencies.containsKey(key)) {
                                mergedDependencies.put(key, element);
                            }
                        }

                        modelDependencyManagement.setDependencies(new ArrayList<>(mergedDependencies.values()));
                    }
                }
            };

            buildingResult = modelBuilderThreadSafetyWorkaround.build(
                    buildingRequest, customInjector, new DefaultProfileSelector());
        } catch (ModelBuildingException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        Model effectivePom = buildingResult.getEffectiveModel();

        // LoggingModelProblemCollector problems = new LoggingModelProblemCollector( getLog() );
        // Model interpolatedModel =
        // this.modelInterpolator.interpolateModel( this.project.getOriginalModel(),
        // effectivePom.getProjectDirectory(), buildingRequest, problems );

        // remove Repositories from super POM (central)
        effectivePom.setRepositories(createFlattenedRepositories(effectivePom.getRepositories()));
        return effectivePom;
    }

    /**
     * Null-safe check for {@link Collection#isEmpty()}.
     *
     * @param collection is the {@link Collection} to test. May be <code>null</code>.
     * @return <code>true</code> if <code>null</code> or {@link Collection#isEmpty() empty}, <code>false</code>
     * otherwise.
     */
    private boolean isEmpty(Collection<?> collection) {
        if (collection == null) {
            return true;
        }
        return collection.isEmpty();
    }

    /**
     * @return <code>true</code> if build-dependent profiles (triggered by OS or JDK) should be evaluated and their
     * effect (variables and dependencies) are resolved and embedded into the flattened POM while the profile
     * itself is stripped. Otherwise if <code>false</code> the profiles will remain untouched.
     */
    public boolean isEmbedBuildProfileDependencies() {
        return this.embedBuildProfileDependencies;
    }

    /**
     * @param activation is the {@link Activation} of a {@link Profile}.
     * @return <code>true</code> if the given {@link Activation} is build-time driven, <code>false</code> otherwise (if
     * it is triggered by OS or JDK).
     */
    protected static boolean isBuildTimeDriven(Activation activation) {

        if (activation == null) {
            return true;
        }
        return StringUtils.isEmpty(activation.getJdk()) && activation.getOs() == null;
    }

    /**
     * Creates the {@link List} of {@link Dependency dependencies} for the flattened POM. These are all resolved
     * {@link Dependency dependencies} except for those added from {@link Profile profiles}.
     *
     * @param effectiveModel is the effective POM {@link Model} to process.
     * @return the {@link List} of {@link Dependency dependencies}.
     * @throws MojoExecutionException if anything goes wrong.
     */
    protected List<Dependency> createFlattenedDependencies(Model effectiveModel) throws MojoExecutionException {
        List<Dependency> flattenedDependencies = new ArrayList<>();
        // resolve all direct and inherited dependencies...
        try {
            createFlattenedDependencies(effectiveModel, flattenedDependencies);
        } catch (Exception e) {
            throw new MojoExecutionException("unable to create flattened dependencies", e);
        }
        if (isEmbedBuildProfileDependencies()) {
            Model projectModel = this.project.getModel();
            Dependencies modelDependencies = new Dependencies();
            modelDependencies.addAll(projectModel.getDependencies());
            for (Profile profile : projectModel.getProfiles()) {
                // build-time driven activation (by property or file)?
                if (isBuildTimeDriven(profile.getActivation())) {
                    List<Dependency> profileDependencies = profile.getDependencies();
                    for (Dependency profileDependency : profileDependencies) {
                        if (modelDependencies.contains(profileDependency)) {
                            // our assumption here is that the profileDependency has been added to model because of
                            // this build-time driven profile. Therefore we need to add it to the flattened POM.
                            // Non build-time driven profiles will remain in the flattened POM with their dependencies
                            // and
                            // allow dynamic dependencies due to OS or JDK.
                            Dependency resolvedProfileDependency = modelDependencies.resolve(profileDependency);
                            if (omitExclusions) {
                                resolvedProfileDependency.setExclusions(Collections.emptyList());
                            }
                            flattenedDependencies.add(resolvedProfileDependency);
                        }
                    }
                }
            }
            getLog().debug("Resolved " + flattenedDependencies.size() + " dependency/-ies for flattened POM.");
        }
        return flattenedDependencies;
    }

    /**
     * Collects the resolved {@link Dependency dependencies} from the given <code>effectiveModel</code>.
     *
     * @param projectDependencies   is the effective POM {@link Model}'s current dependencies
     * @param flattenedDependencies is the {@link List} where to add the collected {@link Dependency dependencies}.
     */
    private void createFlattenedDependenciesDirect(
            List<Dependency> projectDependencies, List<Dependency> flattenedDependencies) {
        for (Dependency projectDependency : projectDependencies) {
            Dependency flattenedDependency = createFlattenedDependency(projectDependency);
            if (flattenedDependency != null) {
                flattenedDependencies.add(flattenedDependency);
            }
        }
    }

    /**
     * Collects the resolved direct and transitive {@link Dependency dependencies} from the given
     * <code>effectiveModel</code>.
     * The collected dependencies are stored in order, so that the leaf dependencies are prioritized in front of direct
     * dependencies.
     * In addition, every non-leaf dependencies will exclude its own direct dependency, since all transitive
     * dependencies
     * will be collected.
     * <p>
     * Transitive dependencies are all going to be collected and become a direct dependency. Maven should already
     * resolve
     * versions properly because now the transitive dependencies are closer to the artifact. However, when this artifact
     * is
     * being consumed, Maven Enforcer Convergence rule will fail because there may be multiple versions for the same
     * transitive dependency.
     * <p>
     * Typically, exclusion can be done by using the wildcard. However, a known Maven issue prevents convergence
     * enforcer from
     * working properly w/ wildcard exclusions. Thus, this will exclude each dependencies explicitly rather than using
     * the wildcard.
     *
     * @param projectDependencies   is the effective POM {@link Model}'s current dependencies
     * @param flattenedDependencies is the {@link List} where to add the collected {@link Dependency dependencies}.
     * @throws DependencyCollectionException
     * @throws ArtifactDescriptorException
     */
    private void createFlattenedDependenciesAll(
            List<Dependency> projectDependencies,
            List<Dependency> managedDependencies,
            List<Dependency> flattenedDependencies)
            throws ArtifactDescriptorException, DependencyCollectionException {
        final Queue<DependencyNode> dependencyNodeLinkedList = new LinkedList<>();
        final Set<String> processedDependencies = new HashSet<>();
        final Artifact projectArtifact = this.project.getArtifact();

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRepositories(project.getRemoteProjectRepositories());
        collectRequest.setRootArtifact(RepositoryUtils.toArtifact(projectArtifact));
        for (Dependency dependency : projectDependencies) {
            collectRequest.addDependency(RepositoryUtils.toDependency(
                    dependency, session.getRepositorySession().getArtifactTypeRegistry()));
        }

        for (Artifact artifact : project.getArtifacts()) {
            collectRequest.addDependency(RepositoryUtils.toDependency(artifact, null));
        }

        for (Dependency dependency : managedDependencies) {
            collectRequest.addManagedDependency(RepositoryUtils.toDependency(
                    dependency, session.getRepositorySession().getArtifactTypeRegistry()));
        }

        DefaultRepositorySystemSession derived = new DefaultRepositorySystemSession(session.getRepositorySession());
        derived.setConfigProperty(ConflictResolver.CONFIG_PROP_VERBOSE, true);
        derived.setConfigProperty(DependencyManagerUtils.CONFIG_PROP_VERBOSE, true);

        CollectResult collectResult = repositorySystem.collectDependencies(derived, collectRequest);

        final DependencyNode root = collectResult.getRoot();
        final Set<String> directDependencyKeys = Stream.concat(
                        projectDependencies.stream().map(this::getKey),
                        project.getArtifacts().stream().map(this::getKey))
                .collect(Collectors.toSet());

        root.accept(new DependencyVisitor() {
            @Override
            public boolean visitEnter(DependencyNode node) {
                if (root == node) {
                    return true;
                }
                if (JavaScopes.PROVIDED.equals(node.getDependency().getScope())) {
                    String dependencyKey = getKey(node.getDependency());
                    if (!directDependencyKeys.contains(dependencyKey)) {
                        return false; // skip non-direct provided ones
                    }
                }
                if (node.getDependency().isOptional()) {
                    return false; // skip optional ones
                }
                dependencyNodeLinkedList.add(node);
                return true;
            }

            @Override
            public boolean visitLeave(DependencyNode node) {
                return true;
            }
        });

        while (!dependencyNodeLinkedList.isEmpty()) {
            DependencyNode node = dependencyNodeLinkedList.poll();

            org.eclipse.aether.graph.Dependency d = node.getDependency();
            Artifact artifact = RepositoryUtils.toArtifact(node.getArtifact());

            Dependency dependency = new Dependency();
            dependency.setGroupId(artifact.getGroupId());
            dependency.setArtifactId(artifact.getArtifactId());
            dependency.setVersion(artifact.getBaseVersion());
            dependency.setClassifier(artifact.getClassifier());
            dependency.setOptional(d.isOptional());
            dependency.setScope(d.getScope());
            dependency.setType(artifact.getType());

            if (!omitExclusions) {
                List<Exclusion> exclusions = new LinkedList<>();

                org.eclipse.aether.artifact.Artifact aetherArtifact = new DefaultArtifact(
                        artifact.getGroupId(), artifact.getArtifactId(), null, artifact.getVersion());
                ArtifactDescriptorRequest request = new ArtifactDescriptorRequest(aetherArtifact, null, null);
                ArtifactDescriptorResult artifactDescriptorResult =
                        repositorySystem.readArtifactDescriptor(this.session.getRepositorySession(), request);

                for (org.eclipse.aether.graph.Dependency artifactDependency :
                        artifactDescriptorResult.getDependencies()) {
                    if (JavaScopes.TEST.equals(artifactDependency.getScope())) {
                        continue;
                    }
                    Exclusion exclusion = new Exclusion();
                    exclusion.setGroupId(artifactDependency.getArtifact().getGroupId());
                    exclusion.setArtifactId(artifactDependency.getArtifact().getArtifactId());
                    exclusions.add(exclusion);
                }

                dependency.setExclusions(exclusions);
            }

            // convert dependency to string for the set, since Dependency doesn't implement equals, etc.
            String dependencyString = dependency.getManagementKey();

            if (!processedDependencies.add(dependencyString)) {
                continue;
            }

            Dependency flattenedDependency = createFlattenedDependency(dependency);
            if (flattenedDependency != null) {
                flattenedDependencies.add(flattenedDependency);
            }
        }
    }

    /**
     * Keep in sync with {@link #getKey(org.eclipse.aether.graph.Dependency)}
     * and {@link #getKey(Dependency)}.
     */
    private String getKey(Artifact a) {
        final String ext =
                artifactHandlerManager.getArtifactHandler(a.getType()).getExtension();
        return a.getGroupId() + ":" + a.getArtifactId() + ":" + ext
                + (a.getClassifier() != null ? ":" + a.getClassifier() : "");
    }

    /**
     * Keep in sync with {@link #getKey(org.eclipse.aether.graph.Dependency)}
     * and {@link #getKey(Artifact)}
     */
    private String getKey(Dependency d) {
        final String ext =
                artifactHandlerManager.getArtifactHandler(d.getType()).getExtension();
        return d.getGroupId() + ":" + d.getArtifactId() + ":" + ext
                + (d.getClassifier() != null ? ":" + d.getClassifier() : "");
    }

    /**
     * Keep in sync with {@link #getKey(Dependency)}
     * and {@link #getKey(Artifact)}.
     */
    private String getKey(org.eclipse.aether.graph.Dependency dependency) {
        final org.eclipse.aether.artifact.Artifact a = dependency.getArtifact();
        return a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getExtension()
                + (!a.getClassifier().isEmpty() ? ":" + a.getClassifier() : "");
    }

    /**
     * Collects the resolved {@link Dependency dependencies} from the given <code>effectiveModel</code>.
     *
     * @param effectiveModel        is the effective POM {@link Model} to process.
     * @param flattenedDependencies is the {@link List} where to add the collected {@link Dependency dependencies}.
     * @throws MojoExecutionException if anything goes wrong.
     */
    protected void createFlattenedDependencies(Model effectiveModel, List<Dependency> flattenedDependencies)
            throws MojoExecutionException {
        getLog().debug("Resolving dependencies of " + effectiveModel.getId());
        // this.project.getDependencies() already contains the inherited dependencies but also those from profiles
        // List<Dependency> projectDependencies = currentProject.getOriginalModel().getDependencies();
        List<Dependency> projectDependencies = effectiveModel.getDependencies();

        if (flattenDependencyMode == null | flattenDependencyMode == FlattenDependencyMode.direct) {
            createFlattenedDependenciesDirect(projectDependencies, flattenedDependencies);
        } else if (flattenDependencyMode == FlattenDependencyMode.all) {
            try {
                createFlattenedDependenciesAll(
                        projectDependencies,
                        effectiveModel.getDependencyManagement() != null
                                ? effectiveModel.getDependencyManagement().getDependencies()
                                : Collections.emptyList(),
                        flattenedDependencies);
            } catch (Exception e) {
                throw new MojoExecutionException("caught exception when flattening dependencies", e);
            }
        }
    }

    /**
     * @param projectDependency is the project {@link Dependency}.
     * @return the flattened {@link Dependency} or <code>null</code> if the given {@link Dependency} is NOT relevant for
     * flattened POM.
     */
    protected Dependency createFlattenedDependency(Dependency projectDependency) {
        if (JavaScopes.TEST.equals(projectDependency.getScope())) {
            return null;
        }

        if (omitExclusions) {
            projectDependency.setExclusions(Collections.emptyList());
        }

        return projectDependency;
    }

    /**
     * @return <code>true</code> if the generated flattened POM shall be {@link MavenProject#setFile(java.io.File) set}
     * as POM artifact of the {@link MavenProject}, <code>false</code> otherwise.
     */
    public boolean isUpdatePomFile() {

        if (this.updatePomFile == null) {
            if (this.flattenMode == FlattenMode.bom) {
                return true;
            }
            return !this.project.getPackaging().equals("pom");
        } else {
            return this.updatePomFile;
        }
    }

    private class ModelsFactory {
        private final File pomFile;
        private Model effectivePom;
        private Model originalPom;
        private Model resolvedPom;
        private Model interpolatedPom;
        private Model extendedInterpolatedPom;
        private Model cleanPom;

        private ModelsFactory(File pomFile) {
            this.pomFile = pomFile;
        }

        public Model getEffectivePom() throws MojoExecutionException {
            if (effectivePom == null) {
                this.effectivePom = FlattenMojo.this
                        .createEffectivePom(createModelBuildingRequest(pomFile))
                        .clone();
            }
            return this.effectivePom;
        }

        public Model getOriginalPom() throws MojoExecutionException {
            if (this.originalPom == null) {
                this.originalPom = createOriginalPom(this.pomFile);
            }
            return this.originalPom;
        }

        public Model getResolvedPom() {
            if (this.resolvedPom == null) {
                this.resolvedPom = FlattenMojo.this.project.getModel();
            }
            return this.resolvedPom;
        }

        public Model getInterpolatedPom() throws MojoExecutionException {
            if (this.interpolatedPom == null) {
                this.interpolatedPom = createInterpolatedPom(
                                createModelBuildingRequest(pomFile),
                                getOriginalPom().clone(),
                                getResolvedPom().getProjectDirectory())
                        .clone();
            }
            return this.interpolatedPom;
        }

        public Model getExtendedInterpolatedPom() throws MojoExecutionException {
            if (this.extendedInterpolatedPom == null) {
                this.extendedInterpolatedPom = createExtendedInterpolatedPom(
                                createModelBuildingRequest(pomFile),
                                getOriginalPom().clone(),
                                getEffectivePom().clone(),
                                getResolvedPom().getProjectDirectory())
                        .clone();
            }
            return this.extendedInterpolatedPom;
        }

        public Model getCleanPom() throws MojoExecutionException {
            if (this.cleanPom == null) {
                this.cleanPom = createCleanPom(getEffectivePom().clone()).clone();
            }
            return this.cleanPom;
        }

        public Model getModel(ElementHandling handling) throws MojoExecutionException {
            switch (handling) {
                case expand:
                    return this.getEffectivePom();
                case keep:
                    return this.getOriginalPom();
                case resolve:
                    return this.getResolvedPom();
                case interpolate:
                    return this.getInterpolatedPom();
                case extended_interpolate:
                    return this.getExtendedInterpolatedPom();
                case flatten:
                    return this.getCleanPom();
                case remove:
                    return null;
                default:
                    throw new IllegalStateException(handling.toString());
            }
        }
    }

    /**
     * This class is a simple SAX handler that extracts the first comment located before the root tag in an XML
     * document.
     */
    private class SaxHeaderCommentHandler extends DefaultHandler2 {

        /**
         * <code>true</code> if root tag has already been visited, <code>false</code> otherwise.
         */
        private boolean rootTagSeen;

        /**
         * @see #getHeaderComment()
         */
        private String headerComment;

        /**
         * The constructor.
         */
        SaxHeaderCommentHandler() {

            super();
            this.rootTagSeen = false;
        }

        /**
         * @return the XML comment from the header of the document or <code>null</code> if not present.
         */
        public String getHeaderComment() {

            return this.headerComment;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void comment(char[] ch, int start, int length) {

            if (!this.rootTagSeen) {
                if (this.headerComment == null) {
                    this.headerComment = new String(ch, start, length);
                } else {
                    getLog().warn("Ignoring multiple XML header comment!");
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {

            this.rootTagSeen = true;
        }
    }
}
