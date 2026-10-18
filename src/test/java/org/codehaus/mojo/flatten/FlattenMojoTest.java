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
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoParameter;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    private static Model flatten(FlattenMojo flattenMojo) throws Exception {
        flattenMojo.execute();

        return readPom(flattenMojo.getFlattenedPomFile());
    }
}
