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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoParameter;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test-Case for {@link FlattenMojo}.
 *
 * @author dehasi
 */
@ProjectMojoTest
class FlattenMojoTest {

    @Inject
    private MavenProject project;

    /**
     * Test method to check that profile activation file is not interpolated.
     *
     * @throws Exception if something goes wrong.
     */
    @Test
    @InjectMojo(
            goal = "flatten",
            pom = "src/test/resources/resolve-properties-ci-do-not-interpolate-profile-activation-file/pom.xml")
    void keepsProfileActivationFile(FlattenMojo flattenMojo) throws Exception {
        Path pom = project.getFile().toPath();

        flattenMojo.execute();

        assertThat(profileActivationFile(flattenMojo.getFlattenedPomFile())).isEqualTo(profileActivationFile(pom));
    }

    private static String profileActivationFile(Path pom) throws Exception {
        return readPom(pom).getProfiles().get(0).getActivation().getFile().getExists();
    }

    private static Model readPom(Path pomFile) throws IOException, XmlPullParserException {
        try (InputStream input = Files.newInputStream(pomFile)) {
            return new MavenXpp3Reader().read(input);
        }
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/resolve-ci-friendlies-only-with-pom-elements/pom.xml")
    void resolveCiFriendliesOnlyWithPomElementsDoesNotThrow(FlattenMojo flattenMojo) throws Exception {
        flattenMojo.execute();

        assertThat(readPom(flattenMojo.getFlattenedPomFile()).getVersion()).isEqualTo("1.2.3.4");
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/processed-original-model/pom.xml")
    @MojoParameter(name = "flattenMode", value = "resolveCiFriendliesOnly")
    void preservesModelProcessorContributions(FlattenMojo flattenMojo) throws Exception {
        contributeOriginalModel();

        Model flattened = flatten(flattenMojo);
        assertContributions(flattened);
        assertThat(flattened.getParent().getArtifactId()).isEqualTo("published-parent");
        assertThat(flattened.getParent().getVersion()).isEqualTo("1");
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/resolve-ci-friendlies-only-with-pom-elements/pom.xml")
    void preservesModelProcessorContributionsWithPomElements(FlattenMojo flattenMojo) throws Exception {
        contributeOriginalModel();

        Model flattened = flatten(flattenMojo);
        assertContributions(flattened);
        assertThat(flattened.getParent()).isNull();
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/processed-original-model/pom.xml")
    @MojoParameter(name = "flattenMode", value = "resolveCiFriendliesOnly")
    void preservesSourceFormattingWithoutModelProcessorContributions(FlattenMojo flattenMojo) throws Exception {
        String source = new String(Files.readAllBytes(project.getFile().toPath()), StandardCharsets.UTF_8);

        flattenMojo.execute();

        assertThat(new String(Files.readAllBytes(flattenMojo.getFlattenedPomFile()), StandardCharsets.UTF_8))
                .isEqualTo(source.replace("${revision}", "1.2.3.4"));
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/flatten-local-parent/pom.xml")
    @MojoParameter(name = "flattenMode", value = "resolveCiFriendliesOnly")
    @MojoParameter(name = "flattenRelativePathParent", value = "true")
    void preservesModelProcessorContributionsWhenCollapsingLocalParents(FlattenMojo flattenMojo) throws Exception {
        project.getOriginalModel().getProperties().setProperty("processor.property", "shared");

        Model flattened = flatten(flattenMojo);

        assertThat(flattened.getProperties())
                .containsEntry("processor.property", "shared")
                .containsEntry("grandparent.property", "grandparent");
    }

    private void contributeOriginalModel() {
        Model original = project.getOriginalModel();
        original.getProperties().setProperty("shared.version", "2");
        Dependency dependency = new Dependency();
        dependency.setGroupId("localhost");
        dependency.setArtifactId("shared-api");
        dependency.setVersion("${shared.version}");
        original.addDependency(dependency);
        DependencyManagement management = new DependencyManagement();
        management.addDependency(dependency.clone());
        original.setDependencyManagement(management);
        Plugin plugin = new Plugin();
        plugin.setGroupId("localhost");
        plugin.setArtifactId("shared-plugin");
        plugin.setVersion("${shared.version}");
        Xpp3Dom configuration = new Xpp3Dom("configuration");
        Xpp3Dom output = new Xpp3Dom("output");
        output.setValue("${project.build.directory}/shared");
        configuration.addChild(output);
        plugin.setConfiguration(configuration);
        original.getBuild().addPlugin(plugin);
        Parent parent = new Parent();
        parent.setGroupId("localhost");
        parent.setArtifactId("published-parent");
        parent.setVersion("1");
        parent.setRelativePath("");
        original.setParent(parent);
        project.getModel().setParent(parent.clone());
    }

    private static void assertContributions(Model flattened) {
        assertThat(flattened.getVersion()).isEqualTo("1.2.3.4");
        assertThat(flattened.getProperties()).containsEntry("shared.version", "2");
        assertThat(flattened.getDependencies()).anySatisfy(dependency -> {
            assertThat(dependency.getArtifactId()).isEqualTo("shared-api");
            assertThat(dependency.getVersion()).isEqualTo("${shared.version}");
        });
        assertThat(flattened.getDependencyManagement().getDependencies())
                .anySatisfy(dependency -> assertThat(dependency.getArtifactId()).isEqualTo("shared-api"));
        assertThat(flattened.getBuild().getPlugins()).anySatisfy(plugin -> {
            assertThat(plugin.getArtifactId()).isEqualTo("shared-plugin");
            assertThat(((Xpp3Dom) plugin.getConfiguration()).getChild("output").getValue())
                    .isEqualTo("${project.build.directory}/shared");
        });
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/flatten-local-parent/pom.xml")
    @MojoParameter(name = "flattenMode", value = "resolveCiFriendliesOnly")
    @MojoParameter(name = "flattenRelativePathParent", value = "true")
    void flattensLocalParentChain(FlattenMojo flattenMojo) throws Exception {
        Model flattenedPom = flatten(flattenMojo);
        assertThat(flattenedPom.getVersion()).isEqualTo("1.2.3.4");
        assertThat(flattenedPom.getProperties())
                .containsEntry("grandparent.property", "grandparent")
                .containsEntry("shared.property", "shared")
                .containsEntry("line.property", "line");
        assertThat(flattenedPom.getDependencyManagement().getDependencies()).anySatisfy(dependency -> {
            assertThat(dependency.getArtifactId()).isEqualTo("managed-dependency");
            assertThat(dependency.getVersion()).isEqualTo("${line.property}");
        });
        assertThat(flattenedPom.getBuild().getPlugins())
                .anySatisfy(plugin -> assertThat(plugin.getArtifactId()).isEqualTo("maven-compiler-plugin"));
        Plugin flattenPlugin = flattenedPom.getBuild().getPlugins().stream()
                .filter(plugin -> "flatten-maven-plugin".equals(plugin.getArtifactId()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        assertThat(((Xpp3Dom) flattenPlugin.getConfiguration()).getChild("flattenRelativePathParent"))
                .isNull();
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/flatten-local-parent/pom.xml")
    @MojoParameter(name = "flattenMode", value = "resolveCiFriendliesOnly")
    @MojoParameter(name = "flattenRelativePathParent", value = "true")
    void inheritsActiveLocalParentProfiles(FlattenMojo flattenMojo) throws Exception {
        Model flattenedPom = flatten(flattenMojo);

        assertThat(flattenedPom.getProperties()).containsEntry("active.parent.property", "profile-value");
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/flatten-local-parent/pom.xml")
    @MojoParameter(name = "flattenMode", value = "resolveCiFriendliesOnly")
    @MojoParameter(name = "flattenRelativePathParent", value = "true")
    void removesFlattenRelativePathParentFromProfiles(FlattenMojo flattenMojo) throws Exception {
        Model flattenedPom = flatten(flattenMojo);

        Profile profile = flattenedPom.getProfiles().stream()
                .filter(candidate -> "inactive-control-profile".equals(candidate.getId()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        Plugin flattenPlugin = profile.getBuild().getPlugins().stream()
                .filter(plugin -> "flatten-maven-plugin".equals(plugin.getArtifactId()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        assertThat(flattenPlugin.getConfiguration()).isNull();
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/flatten-inherited-local-parent/pom.xml")
    @MojoParameter(name = "flattenMode", value = "resolveCiFriendliesOnly")
    @MojoParameter(name = "flattenRelativePathParent", value = "true")
    void flattensLocalParentWithInheritedCoordinates(FlattenMojo flattenMojo) throws Exception {
        Model flattenedPom = flatten(flattenMojo);

        assertThat(flattenedPom.getProperties()).containsEntry("repository.property", "repository-value");
        assertThat(flattenedPom.getParent()).isNull();
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/flatten-local-parent/pom.xml")
    void rejectsPreviouslyVisitedLocalParent(FlattenMojo flattenMojo) throws Exception {
        Path pom = project.getFile().toPath().toAbsolutePath().normalize();
        Set<Path> visited = new HashSet<>();
        visited.add(pom);
        Model model = readPom(pom);

        assertThatThrownBy(() -> flattenMojo.collapseRelativePathParent(model, pom, visited))
                .isInstanceOf(MojoExecutionException.class)
                .hasMessage("Cyclic local parent chain detected at " + pom
                        + "; local parents must not reference each other in a loop through relativePath.");
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/legacy-property-support/pom.xml")
    void resolvesTheLegacyArtifactIdProperty(FlattenMojo flattenMojo) throws Exception {
        assertThat(project.getOriginalModel().getName()).isEqualTo("${artifactId}");

        assertThat(flatten(flattenMojo).getName()).isEqualTo("legacy-property-support");
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/xml-header-comment-present/pom.xml")
    void keepsTheLicenseHeaderComment(FlattenMojo flattenMojo) throws Exception {
        String sourceHeader = commentBeforeProject(project.getFile().toPath());

        flattenMojo.execute();

        assertThat(sourceHeader).contains("Licensed to the Apache Software Foundation (ASF)");
        assertThat(commentBeforeProject(flattenMojo.getFlattenedPomFile())).isEqualTo(sourceHeader);
    }

    private static String commentBeforeProject(Path pom) throws IOException {
        String content = new String(Files.readAllBytes(pom), StandardCharsets.UTF_8).replace("\r", "");
        String beforeProject = content.substring(0, content.indexOf("<project"));
        return beforeProject.substring(beforeProject.indexOf("<!--"), beforeProject.indexOf("-->"));
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/removing-pluginManagement-if-build-is-empty/pom.xml")
    void removesPluginManagementWhenBuildIsEmpty(FlattenMojo flattenMojo) throws Exception {
        assertThat(project.getOriginalModel().getBuild().getPluginManagement()).isNotNull();

        assertThat(flatten(flattenMojo).getBuild()).isNull();
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/removing-pluginManagement-if-build-is-flatten/pom.xml")
    void removesPluginManagementWhenBuildIsFlatten(FlattenMojo flattenMojo) throws Exception {
        assertThat(project.getOriginalModel().getBuild().getPluginManagement()).isNotNull();

        Model flattened = flatten(flattenMojo);

        assertThat(flattened.getBuild() == null || flattened.getBuild().getPluginManagement() == null)
                .isTrue();
    }

    @Test
    @InjectMojo(
            goal = "flatten",
            pom = "src/test/resources/issue-383-project-basedir-and-baseUri-for-are-interpolated/pom.xml")
    void interpolatesBaseUriButNotBasedirOrBuildPaths(FlattenMojo flattenMojo) throws Exception {
        Properties properties = flatten(flattenMojo).getProperties();

        assertCoordinatesAreInterpolated(properties);
        assertThat(properties.getProperty("projectBasedir")).isEqualTo("x" + project.getBasedir() + "y");
        assertThat(properties.getProperty("projectBaseUri"))
                .startsWith("xfile:")
                .endsWith("y");
        assertBuildPathsAreKept(properties);
    }

    @Test
    @InjectMojo(
            goal = "flatten",
            pom =
                    "src/test/resources/issue-383-with-extended_interpolate-project-basedir-and-baseUri-for-are-not-interpolated/pom.xml")
    void extendedInterpolationKeepsBasedirAndBaseUri(FlattenMojo flattenMojo) throws Exception {
        Properties properties = flatten(flattenMojo).getProperties();

        assertCoordinatesAreInterpolated(properties);
        assertThat(properties.getProperty("projectBasedir")).isEqualTo("x${project.basedir}y");
        assertThat(properties.getProperty("projectBaseUri")).isEqualTo("x${project.baseUri}y");
        assertBuildPathsAreKept(properties);
    }

    private static void assertCoordinatesAreInterpolated(Properties properties) {
        assertThat(properties.getProperty("projectArtifactId"))
                .isEqualTo("xissue-383-project-basedir-and-baseUri-for-are-not-interpolatedy");
        assertThat(properties.getProperty("projectGroupId")).isEqualTo("xorg.codehaus.mojo.flatten.itsy");
        assertThat(properties.getProperty("projectVersion")).isEqualTo("x0.0.1-SNAPSHOTy");
    }

    private static void assertBuildPathsAreKept(Properties properties) {
        assertThat(properties.getProperty("projectBuildDirectory")).isEqualTo("x${project.build.directory}y");
        assertThat(properties.getProperty("projectBuildOutputDirectory"))
                .isEqualTo("x${project.build.outputDirectory}y");
        assertThat(properties.getProperty("projectBuildSourceDirectory"))
                .isEqualTo("x${project.build.sourceDirectory}y");
        assertThat(properties.getProperty("projectBuildScriptSourceDirectory"))
                .isEqualTo("x${project.build.scriptSourceDirectory}y");
        assertThat(properties.getProperty("projectBuildTestSourceDirectory"))
                .isEqualTo("x${project.build.testSourceDirectory}y");
        assertThat(properties.getProperty("projectReportingOutputDirectory"))
                .isEqualTo("x${project.reporting.outputDirectory}y");
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/skip/flatten-skip-pom.xml")
    void skippedFlattenWritesNothing(FlattenMojo flattenMojo) throws Exception {
        flattenMojo.execute();

        assertThat(flattenMojo.getFlattenedPomFile()).doesNotExist();
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/skip/flatten-skip-pom.xml")
    void skippedFlattenLeavesAnExistingFlattenedPomAlone(FlattenMojo flattenMojo) throws Exception {
        Path flattenedPom = existingFlattenedPom(flattenMojo.getFlattenedPomFile());

        flattenMojo.execute();

        assertFlattenedPomUntouched(flattenedPom);
    }

    @Test
    @InjectMojo(goal = "clean", pom = "src/test/resources/skip/clean-skip-pom.xml")
    void skippedCleanKeepsTheFlattenedPom(CleanMojo cleanMojo) throws Exception {
        Path flattenedPom = existingFlattenedPom(cleanMojo.getFlattenedPomFile());

        cleanMojo.execute();

        assertFlattenedPomUntouched(flattenedPom);
    }

    @Test
    @InjectMojo(goal = "clean", pom = "src/test/resources/skip/clean-skip-pom.xml")
    @MojoParameter(name = "skipClean", value = "false")
    void cleanDeletesTheFlattenedPom(CleanMojo cleanMojo) throws Exception {
        Path flattenedPom = existingFlattenedPom(cleanMojo.getFlattenedPomFile());

        cleanMojo.execute();

        assertThat(flattenedPom).doesNotExist();
    }

    private static final long OLD_TIMESTAMP = 1_000_000_000_000L;

    /** Writes a flattened POM dated long before the test, so that a rewrite shows in its timestamp. */
    private static Path existingFlattenedPom(Path flattenedPom) throws IOException {
        Files.createDirectories(flattenedPom.getParent());
        Files.write(flattenedPom, "<project/>".getBytes(StandardCharsets.UTF_8));
        assertThat(flattenedPom.toFile().setLastModified(OLD_TIMESTAMP)).isTrue();
        return flattenedPom;
    }

    private static void assertFlattenedPomUntouched(Path flattenedPom) throws IOException {
        assertThat(flattenedPom).hasContent("<project/>");
        assertThat(Files.getLastModifiedTime(flattenedPom).toMillis()).isEqualTo(OLD_TIMESTAMP);
    }

    private static Model flatten(FlattenMojo flattenMojo) throws Exception {
        flattenMojo.execute();

        return readPom(flattenMojo.getFlattenedPomFile());
    }
}
