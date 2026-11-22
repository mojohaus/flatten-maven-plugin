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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Which of the optional POM elements the flattened POM keeps, per {@code flattenMode} and {@code pomElements}. Every
 * POM starts with all optional elements set, so that an element missing from the result was removed by the mojo.
 */
@ProjectMojoTest
class FlattenOptionalElementsTest {

    @Inject
    private MavenProject project;

    @Inject
    private MojoExecution mojoExecution;

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/optional-elements/minimum-pom.xml")
    void modeMinimumKeepsEveryOptionalElementButBuildAndParent(FlattenMojo flattenMojo) throws Exception {
        assertEveryOptionalElementIsSet(project.getOriginalModel());

        Model flattened = flatten(flattenMojo);

        assertRequiredElements(flattened, "optional-elements-modeMinimum");
        assertThat(flattened.getBuild()).isNull();
        assertThat(flattened.getReporting()).isNull();
        assertThat(flattened.getReports()).isNull();
        assertThat(flattened.getParent()).isNull();
        assertThat(flattened.getName()).isNull();
        assertThat(flattened.getCiManagement()).isNotNull();
        assertThat(flattened.getContributors()).isNotEmpty();
        assertThat(flattened.getDependencyManagement()).isNotNull();
        assertThat(flattened.getDescription()).isNotNull();
        assertThat(flattened.getDevelopers()).isNotEmpty();
        assertThat(flattened.getDistributionManagement()).isNotNull();
        assertThat(flattened.getIssueManagement()).isNotNull();
        assertThat(flattened.getMailingLists()).isNotEmpty();
        assertThat(flattened.getOrganization()).isNotNull();
        assertThat(flattened.getPluginRepositories()).isNotEmpty();
        assertThat(flattened.getRepositories()).isNotEmpty();
        assertThat(flattened.getPrerequisites()).isNotNull();
        assertThat(flattened.getProperties()).isNotEmpty();
        assertThat(flattened.getScm()).isNotNull();
        assertThat(flattened.getUrl()).isNotNull();
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/optional-elements/oss-pom.xml")
    void modeOssDropsDependencyManagementPluginRepositoriesAndProperties(FlattenMojo flattenMojo) throws Exception {
        assertEveryOptionalElementIsSet(project.getOriginalModel());

        Model flattened = flatten(flattenMojo);

        assertRequiredElements(flattened, "optional-elements-modeOss");
        assertThat(flattened.getBuild()).isNull();
        assertThat(flattened.getReporting()).isNull();
        assertThat(flattened.getReports()).isNull();
        assertThat(flattened.getParent()).isNull();
        assertThat(flattened.getName()).isNull();
        assertThat(flattened.getDependencyManagement()).isNull();
        assertThat(flattened.getPluginRepositories()).isEmpty();
        assertThat(flattened.getProperties()).isEmpty();
        assertThat(flattened.getCiManagement()).isNotNull();
        assertThat(flattened.getContributors()).isNotEmpty();
        assertThat(flattened.getDescription()).isNotNull();
        assertThat(flattened.getDevelopers()).isNotEmpty();
        assertThat(flattened.getDistributionManagement()).isNotNull();
        assertThat(flattened.getIssueManagement()).isNotNull();
        assertThat(flattened.getMailingLists()).isNotEmpty();
        assertThat(flattened.getOrganization()).isNotNull();
        assertThat(flattened.getRepositories()).isNotEmpty();
        assertThat(flattened.getPrerequisites()).isNotNull();
        assertThat(flattened.getScm()).isNotNull();
        assertThat(flattened.getUrl()).isNotNull();
        assertProfileDependency(flattened);
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/optional-elements/ossrh-pom.xml")
    void modeOssrhKeepsOnlyWhatSonatypeRequires(FlattenMojo flattenMojo) throws Exception {
        assertEveryOptionalElementIsSet(project.getOriginalModel());

        Model flattened = flatten(flattenMojo);

        assertRequiredElements(flattened, "optional-elements-modeOssrh");
        assertThat(flattened.getBuild()).isNull();
        assertThat(flattened.getReporting()).isNull();
        assertThat(flattened.getReports()).isNull();
        assertThat(flattened.getParent()).isNull();
        assertThat(flattened.getName()).isNull();
        assertThat(flattened.getCiManagement()).isNull();
        assertThat(flattened.getContributors()).isEmpty();
        assertThat(flattened.getDependencyManagement()).isNull();
        assertThat(flattened.getDistributionManagement()).isNull();
        assertThat(flattened.getIssueManagement()).isNull();
        assertThat(flattened.getMailingLists()).isEmpty();
        assertThat(flattened.getOrganization()).isNull();
        assertThat(flattened.getPluginRepositories()).isEmpty();
        assertThat(flattened.getPrerequisites()).isNull();
        assertThat(flattened.getProperties()).isEmpty();
        assertThat(flattened.getDescription()).isNotNull();
        assertThat(flattened.getDevelopers()).isNotEmpty();
        assertThat(flattened.getRepositories()).isNotEmpty();
        assertThat(flattened.getScm()).isNotNull();
        assertThat(flattened.getUrl()).isNotNull();
        assertProfileDependency(flattened);
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/optional-elements/pom-elements-legacy-pom.xml")
    void emptyPomElementsKeepTheLegacyDefaults(FlattenMojo flattenMojo) throws Exception {
        assertEveryOptionalElementIsSet(project.getOriginalModel());
        // an empty pomElements is told from the other modes by the raw configuration, which the harness leaves out
        mojoExecution.setConfiguration((Xpp3Dom) project.getBuild()
                .getPluginsAsMap()
                .get("org.codehaus.mojo:flatten-maven-plugin")
                .getConfiguration());

        Model flattened = flatten(flattenMojo);

        assertRequiredElements(flattened, "optional-elements-pomElements");
        assertThat(flattened.getBuild()).isNull();
        assertThat(flattened.getReporting()).isNull();
        assertThat(flattened.getReports()).isNull();
        assertThat(flattened.getParent()).isNull();
        assertThat(flattened.getName()).isNull();
        assertThat(flattened.getCiManagement()).isNull();
        assertThat(flattened.getDependencyManagement()).isNull();
        assertThat(flattened.getProperties()).isEmpty();
        assertThat(flattened.getContributors()).isNotEmpty();
        assertThat(flattened.getDescription()).isNotNull();
        assertThat(flattened.getDevelopers()).isNotEmpty();
        assertThat(flattened.getDistributionManagement()).isNotNull();
        assertThat(flattened.getIssueManagement()).isNotNull();
        assertThat(flattened.getMailingLists()).isNotEmpty();
        assertThat(flattened.getOrganization()).isNotNull();
        assertThat(flattened.getPluginRepositories()).isNotEmpty();
        assertThat(flattened.getRepositories()).isNotEmpty();
        assertThat(flattened.getPrerequisites()).isNotNull();
        assertThat(flattened.getScm()).isNotNull();
        assertThat(flattened.getUrl()).isNotNull();
        assertProfileDependency(flattened);
    }

    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/optional-elements/pom-elements-suggested-pom.xml")
    void suggestedPomElementsKeepResolveInterpolateOrDrop(FlattenMojo flattenMojo) throws Exception {
        Model flattened = flatten(flattenMojo);

        assertRequiredElements(flattened, "optional-elements-pomElements-suggested");
        assertThat(flattened.getParent()).isNull();
        // keep
        assertThat(flattened.getName()).isEqualTo("${project.artifactId}");
        assertThat(flattened.getUrl()).isEqualTo("${key}");
        // interpolate
        assertThat(flattened.getDescription()).isEqualTo("Description of optional-elements-pomElements-suggested");
        assertThat(flattened.getScm().getUrl()).isEqualTo("magic-value0.0.1-SNAPSHOT");
        assertThat(flattened.getDevelopers()).isNotEmpty();
        assertThat(flattened.getDevelopers().get(0).getId()).isEqualTo("magic-value0.0.1-SNAPSHOT");
        assertThat(flattened.getDevelopers().get(0).getName()).isEqualTo("magic-value0.0.1-SNAPSHOT");
        assertThat(flattened.getDependencyManagement().getDependencies()).isNotEmpty();
        assertThat(flattened.getDependencyManagement().getDependencies().get(0).getArtifactId())
                .isEqualTo("test");
        // resolve
        assertThat(flattened.getCiManagement().getUrl()).isEqualTo("42magic-value0.0.1-SNAPSHOT42");
        // not listed: dropped
        assertThat(flattened.getContributors()).isEmpty();
        assertThat(flattened.getDistributionManagement()).isNull();
        assertThat(flattened.getIssueManagement()).isNull();
        assertThat(flattened.getMailingLists()).isEmpty();
        assertThat(flattened.getOrganization()).isNull();
        assertThat(flattened.getPluginRepositories()).isEmpty();
        assertThat(flattened.getRepositories()).isEmpty();
        assertThat(flattened.getPrerequisites()).isNull();
        assertThat(flattened.getProperties()).isEmpty();
        assertThat(flattened.getBuild()).isNull();
        assertThat(flattened.getReporting()).isNull();
        assertThat(flattened.getReports()).isNull();
        assertProfileDependency(flattened);
    }

    private static void assertEveryOptionalElementIsSet(Model original) {
        assertThat(original.getBuild()).isNotNull();
        assertThat(original.getCiManagement()).isNotNull();
        assertThat(original.getContributors()).isNotEmpty();
        assertThat(original.getDependencyManagement()).isNotNull();
        assertThat(original.getDescription()).isNotNull();
        assertThat(original.getDevelopers()).isNotEmpty();
        assertThat(original.getDistributionManagement()).isNotNull();
        assertThat(original.getIssueManagement()).isNotNull();
        assertThat(original.getMailingLists()).isNotEmpty();
        assertThat(original.getOrganization()).isNotNull();
        assertThat(original.getParent()).isNotNull();
        assertThat(original.getPluginRepositories()).isNotEmpty();
        assertThat(original.getRepositories()).isNotEmpty();
        assertThat(original.getPrerequisites()).isNotNull();
        assertThat(original.getProperties()).isNotEmpty();
        assertThat(original.getReporting()).isNotNull();
        assertThat(original.getScm()).isNotNull();
        assertThat(original.getUrl()).isNotNull();
    }

    private static void assertRequiredElements(Model flattened, String artifactId) {
        assertThat(flattened.getModelVersion()).isEqualTo("4.0.0");
        assertThat(flattened.getGroupId()).isEqualTo("org.codehaus.mojo.flatten.its");
        assertThat(flattened.getArtifactId()).isEqualTo(artifactId);
        assertThat(flattened.getVersion()).isEqualTo("0.0.1-SNAPSHOT");
    }

    /** The profile's dependency has no version in the source POM; the parent manages it. */
    private static void assertProfileDependency(Model flattened) {
        assertThat(flattened.getProfiles()).isNotEmpty();
        assertThat(flattened.getProfiles().get(0).getId()).isEqualTo("test");
        assertThat(flattened.getProfiles().get(0).getDependencies()).isNotEmpty();
        assertThat(flattened.getProfiles().get(0).getDependencies().get(0).getArtifactId())
                .isEqualTo("dep");
        assertThat(flattened.getProfiles().get(0).getDependencies().get(0).getVersion())
                .isEqualTo("1.1");
    }

    private static Model flatten(FlattenMojo flattenMojo) throws Exception {
        flattenMojo.execute();

        Path flattenedPom = flattenMojo.getFlattenedPomFile();
        try (InputStream input = Files.newInputStream(flattenedPom)) {
            return new MavenXpp3Reader().read(input);
        }
    }
}
