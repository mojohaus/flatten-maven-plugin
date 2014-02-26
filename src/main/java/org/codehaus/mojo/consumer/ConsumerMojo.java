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
package org.codehaus.mojo.consumer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Activation;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * This {@link AbstractMojo MOJO} realizes the goal <code>consumer</code> that generates the consumer POM and
 * {@link #isUpdatePomFile() potentially updates the POM file} so that the current {@link MavenProject}'s
 * {@link MavenProject#getFile() file} points to the consumer POM instead of the original <code>pom.xml</code>
 * file.<br/>
 * The consumer POM is a reduced version of the original POM with the focus to contain only the important
 * information for consuming it. Therefore information that is only required for maintenance by developers and
 * to build the project artifact(s) are stripped. <br/>
 * Starting from here we specify how the consumer POM is created from the original POM and its project:<br/>
 * <table border="1">
 * <tr>
 * <th>Element</th>
 * <th>Transformation</th>
 * <th>Note</th>
 * </tr>
 * <tr>
 * <td>{@link Model#getModelVersion() modelVersion}</td>
 * <td>Fixed to "4.0.0"</td>
 * <td>New maven versions will once be able to evolve the model version without incompatibility to older
 * versions if consumer POMs get deployed. The code of this plugin might need to be changed then as it
 * currently uses the same {@link Model} to access the original POM as well as to create and write the
 * consumer POM.</td>
 * </tr>
 * <tr>
 * <td>
 * {@link Model#getGroupId() groupId}<br/>
 * {@link Model#getArtifactId() artifactId}<br/>
 * {@link Model#getGroupId() groupId}<br/>
 * {@link Model#getArtifactId() artifactId}<br/>
 * {@link Model#getVersion() version}<br/>
 * {@link Model#getPackaging() packaging}<br/>
 * </td>
 * <td>resolved</td>
 * <td>copied to the consumer POM but with inheritance from {@link Model#getParent() parent} as well as with
 * all variables and defaults resolved. These elements are technically required for consumption.</td>
 * </tr>
 * <tr>
 * <td>
 * {@link Model#getName() name}<br/>
 * {@link Model#getDescription() description}<br/>
 * {@link Model#getUrl() url}<br/>
 * {@link Model#getInceptionYear() inceptionYear}<br/>
 * {@link Model#getLicenses() licenses}<br/>
 * {@link Model#getScm() scm}<br/>
 * </td>
 * <td>resolved</td>
 * <td>copied to the consumer POM but with inheritance from {@link Model#getParent() parent} as well as with
 * all variables and defaults resolved. Unlike the above elements these elements would not be required in
 * consumer POM. However, they make sense for publication and deployment. Either because they are just plain
 * descriptive {@link String}s or because they contain important information about the artifact (
 * {@link Model#getLicenses() licenses}) or they link to the actual source of the project where further
 * information can be found ({@link Model#getScm() scm} and {@link Model#getUrl() url}).</td>
 * </tr>
 * <tr>
 * <td>{@link Model#getDependencies() dependencies}</td>
 * <td>resolved specially</td>
 * <td>Consumer POM contains the actual dependencies of the project. Test dependencies are removed. Variables
 * and {@link Model#getDependencyManagement() dependencyManagement} is resolved to get fixed dependency
 * attributes (especially {@link Dependency#getVersion() version}). However specific impact triggered by
 * consumer relevant profiles (see below) is ignored to create the consumer POM. Therefore dynamic
 * dependencies triggered by profiles will still work for consumers except they are realized via variable
 * properties (TODO discuss and clarify the latter).</td>
 * </tr>
 * <tr>
 * <td>{@link Model#getProfiles() profiles}</td>
 * <td>resolved specially</td>
 * <td>Consumer POM contains {@link Profile profiles} reduced to the ones that are relevant for consumers.
 * These are the ones {@link Activation activated} by e.g. {@link Activation#getJdk() JDK} or
 * {@link Activation#getOs() OS}. Only dependencies added to the project are visible in the consumer POM.</td>
 * </tr>
 * <tr>
 * <td>
 * {@link Model#getParent() parent}<br/>
 * {@link Model#getBuild() build}<br/>
 * {@link Model#getDependencyManagement() dependencyManagement}<br/>
 * {@link Model#getDistributionManagement() distributionManagement}<br/>
 * {@link Model#getCiManagement() ciManagement}<br/>
 * {@link Model#getProperties() properties}<br/>
 * {@link Model#getModules() modules}<br/>
 * {@link Model#getPluginRepositories() pluginRepositories}<br/>
 * {@link Model#getPrerequisites() prerequisites}<br/>
 * {@link Model#getRepositories() repositories}<br/>
 * {@link Model#getIssueManagement() issueManagement}<br/>
 * {@link Model#getReporting() reporting}</td>
 * <td>removed</td>
 * <td>Will be completely stripped and never occur in a consumer POM.</td>
 * </tr>
 * </table>
 * 
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @goal consumer
 * @requiresProject true
 * @requiresDirectInvocation false
 * @executionStrategy once-per-session
 */
public class ConsumerMojo extends AbstractMojo {

  /**
   * The Maven Project.
   * 
   * @parameter property="project"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * The flag to indicate if the generated consumer POM shall be set as POM file to the current project. By
   * default this is only done for projects with packaging other than <code>pom</code>. You may want to also
   * do this for <code>pom</code> packages projects by setting this parameter to <code>true</code> or you can
   * use <code>false</code> in order to only generate the consumer POM but never set it as POM file.
   * 
   * @parameter property="updatePomFile"
   */
  private Boolean updatePomFile;

  /**
   * The filename of the generated consumer POM file.
   * 
   * @parameter expression="${project.build.directory}/consumer-pom.xml"
   */
  private File consumerPomFile;

  /**
   * The constructor.
   */
  public ConsumerMojo() {

    super();
  }

  /**
   * {@inheritDoc}
   */
  public void execute() throws MojoExecutionException, MojoFailureException {

    getLog().info("Generating consumer POM of project " + this.project.getId() + "...");

    Model consumerPom = createConsumerPom();
    writePom(consumerPom, this.consumerPomFile);

    if (isUpdatePomFile()) {
      this.project.setFile(this.consumerPomFile);
    }
  }

  /**
   * Writes the given POM {@link Model} to the given {@link File}.
   * 
   * @param pom the {@link Model} of the POM to write.
   * @param pomFile the {@link File} where to write the given POM will be written to.
   *        {@link File#getParentFile() Parent directories} are {@link File#mkdirs() created} automatically.
   * @throws MojoExecutionException if the operation failed (e.g. due to an {@link IOException}).
   */
  protected void writePom(Model pom, File pomFile) throws MojoExecutionException {

    File parentFile = pomFile.getParentFile();
    if (!parentFile.exists()) {
      boolean success = parentFile.mkdirs();
      if (!success) {
        throw new MojoExecutionException("Failed to create directory " + pomFile.getParent());
      }
    }
    MavenXpp3Writer pomWriter = new MavenXpp3Writer();
    Writer writer = null;
    try {
      writer = new FileWriter(this.consumerPomFile);
      pomWriter.write(writer, pom);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to write consumer POM to " + this.consumerPomFile, e);
    } finally {
      // resource-handling not perfectly solved but we do not want to require java 1.7
      // and this is not a server application.
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          getLog().error("Error while closing writer", e);
        }
      }
    }
  }

  /**
   * This method creates the consumer POM what is the main task of this plugin.
   * 
   * @return the {@link Model} of the consumer POM.
   */
  protected Model createConsumerPom() {

    // actually we would need a copy of the 4.0.0 model in a separate package (version_4_0_0 subpackage).
    Model model = new Model();

    // fixed to 4.0.0 forever :)
    model.setModelVersion("4.0.0");

    // GAV values have to be fixed without variables, etc.
    model.setGroupId(this.project.getGroupId());
    model.setArtifactId(this.project.getArtifactId());
    model.setVersion(this.project.getVersion());

    // general attributes also need no dynamics/variables
    model.setPackaging(this.project.getPackaging());
    model.setName(this.project.getName());
    model.setDescription(this.project.getDescription());
    model.setUrl(this.project.getUrl());
    model.setInceptionYear(this.project.getInceptionYear());

    // copy by reference - if model changes this code has to explicitly create the new elements
    model.setLicenses(this.project.getLicenses());
    model.setScm(this.project.getScm());

    // transform dependencies...
    List<Dependency> dependencies = createConsumerDependencies();
    model.setDependencies(dependencies);

    // transform profiles...
    List<Profile> profiles = createConsumerProfiles();
    model.setProfiles(profiles);

    return model;
  }

  /**
   * TODO: javadoc
   * 
   * @return
   */
  protected List<Profile> createConsumerProfiles() {

    List<Profile> projectProfiles = this.project.getModel().getProfiles();
    List<Profile> consumerProfiles = new ArrayList<Profile>(projectProfiles.size());
    for (Profile projectProfile : projectProfiles) {
      Profile consumerProfile = createConsumerProfile(projectProfile);
      if (consumerProfile != null) {
        consumerProfiles.add(consumerProfile);
      }
    }
    return consumerProfiles;
  }

  /**
   * TODO: javadoc
   * 
   * @param projectProfile
   * @return
   */
  protected Profile createConsumerProfile(Profile projectProfile) {

    Activation activation = projectProfile.getActivation();
    if (!isConsumerRelevant(activation)) {
      return null;
    }
    List<Dependency> projectProfileDependencies = projectProfile.getDependencies();
    List<Dependency> consumerProfileDependencies = new ArrayList<Dependency>(projectProfileDependencies.size());
    for (Dependency projectProfileDependency : projectProfileDependencies) {
      Dependency consumerProfileDependency = createConsumerProfileDependency(projectProfileDependency);
      if (consumerProfileDependency != null) {
        consumerProfileDependencies.add(consumerProfileDependency);
      }
    }
    if (consumerProfileDependencies.size() > 0) {
      Profile consumerProfile = new Profile();
      consumerProfile.setActivation(activation);
      consumerProfile.setDependencies(consumerProfileDependencies);
      consumerProfile.setId(projectProfile.getId());
      return consumerProfile;
    }
    return null;
  }

  /**
   * TODO: javadoc
   * 
   * @param projectProfileDependency
   * @return
   */
  protected Dependency createConsumerProfileDependency(Dependency projectProfileDependency) {

    return createConsumerDependency(projectProfileDependency);
  }

  /**
   * TODO: javadoc
   * 
   * @param activation
   * @return
   */
  protected boolean isConsumerRelevant(Activation activation) {

    if (activation == null) {
      return false;
    }
    if (StringUtils.isEmpty(activation.getJdk()) && (activation.getOs() == null)) {
      return false;
    }
    return true;
  }

  /**
   * Creates the {@link List} of {@link Dependency dependencies} for the consumer POM. These are all resolved
   * {@link Dependency dependencies} except for those added from {@link Profile profiles}.
   * 
   * @return the {@link List} of {@link Dependency dependencies}.
   */
  protected List<Dependency> createConsumerDependencies() {

    List<Dependency> consumerDependencies = new ArrayList<Dependency>(this.project.getDependencies().size());
    createConsumerDependenciesRecursive(this.project, consumerDependencies);
    getLog().info("Resolved " + consumerDependencies.size() + " dependency/-ies for consumer POM.");
    return consumerDependencies;
  }

  /**
   * Collects the resolved {@link Dependency dependencies} from the given <code>currentProject</code> and all
   * its {@link MavenProject#getParent() parents} recursively.
   * 
   * @param currentProject is the current {@link MavenProject} to process.
   * @param consumerDependencies is the {@link List} where to add the collected {@link Dependency
   *        dependencies}.
   */
  protected void createConsumerDependenciesRecursive(MavenProject currentProject, List<Dependency> consumerDependencies) {

    getLog().info("Resolving dependencies of " + currentProject.getId());
    // this.project.getDependencies() already contains the inherited dependencies but also those from profiles
    List<Dependency> projectDependencies = currentProject.getOriginalModel().getDependencies();
    for (Dependency projectDependency : projectDependencies) {
      Dependency consumerDependency = createConsumerDependency(projectDependency);
      if (consumerDependency != null) {
        consumerDependencies.add(consumerDependency);
      }
    }
    MavenProject parentProject = currentProject.getParent();
    if (parentProject != null) {
      createConsumerDependenciesRecursive(parentProject, consumerDependencies);
    }
  }

  /**
   * TODO: javadoc
   * 
   * @param projectDependency
   * @return
   */
  protected Dependency createConsumerDependency(Dependency projectDependency) {

    if ("test".equals(projectDependency.getScope())) {
      // remove test dependencies from consumer POM
      return null;
    }
    Dependency consumerDependency = new Dependency();
    consumerDependency.setGroupId(projectDependency.getGroupId());
    consumerDependency.setArtifactId(projectDependency.getArtifactId());
    consumerDependency.setVersion(projectDependency.getVersion());
    consumerDependency.setScope(projectDependency.getScope());
    consumerDependency.setType(projectDependency.getType());
    consumerDependency.setClassifier(projectDependency.getClassifier());
    consumerDependency.setOptional(projectDependency.isOptional());
    // for completeness, actually system scope is sick for consumers
    consumerDependency.setSystemPath(projectDependency.getSystemPath());
    consumerDependency.setExclusions(projectDependency.getExclusions());
    return projectDependency;
  }

  /**
   * @return <code>true</code> if the generated consumer POM shall be
   *         {@link MavenProject#setFile(java.io.File) set} as POM artifact of the {@link MavenProject},
   *         <code>false</code> otherwise.
   */
  protected boolean isUpdatePomFile() {

    if (this.updatePomFile == null) {
      return !this.project.getPackaging().equals("pom");
    } else {
      return this.updatePomFile.booleanValue();
    }
  }

}
