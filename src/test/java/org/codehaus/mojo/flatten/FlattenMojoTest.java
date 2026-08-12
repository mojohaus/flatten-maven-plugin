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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test-Case for {@link FlattenMojo}.
 *
 * @author dehasi
 */
public class FlattenMojoTest {

    private static final String PATH =
            "src/test/resources/resolve-properties-ci-do-not-interpolate-profile-activation-file/";
    private static final String POM = PATH + "pom.xml";
    private static final String FLATTENED_POM = PATH + ".flattened-pom.xml";

    @Rule
    public MojoRule rule = new MojoRule();

    /**
     * Test method to check that profile activation file is not interpolated.
     *
     * @throws Exception if something goes wrong.
     */
    @Test
    public void keepsProfileActivationFile() throws Exception {
        MavenProject project = rule.readMavenProject(new File(PATH));
        FlattenMojo flattenMojo = (FlattenMojo) rule.lookupConfiguredMojo(project, "flatten");

        flattenMojo.execute();

        assertThat(profileActivationFile(FLATTENED_POM)).isEqualTo(profileActivationFile(POM));
    }

    private static String profileActivationFile(String pom) throws Exception {
        return readPom(pom).getProfiles().get(0).getActivation().getFile().getExists();
    }

    private static Model readPom(String pomFilePath) throws IOException, XmlPullParserException {
        try (FileInputStream input = new FileInputStream(new File(pomFilePath))) {
            return new MavenXpp3Reader().read(input);
        }
    }

    /**
     * After test method. Removes flattened-pom.xml file which is created during test.
     *
     * @throws IOException if can't remove file.
     */
    @After
    public void removeFlattenedPom() throws IOException {
        File flattenedPom = new File(FLATTENED_POM);
        if (flattenedPom.exists()) {
            if (!flattenedPom.delete()) {
                throw new IOException("Can't delete " + flattenedPom);
            }
        }
    }

    private static final String CI_WITH_POM_ELEMENTS_PATH =
            "src/test/resources/resolve-ci-friendlies-only-with-pom-elements/";
    private static final String CI_WITH_POM_ELEMENTS_FLATTENED = CI_WITH_POM_ELEMENTS_PATH + ".flattened-pom.xml";

    /**
     * flattenMode=resolveCiFriendliesOnly combined with a non-null pomElements skips the
     * resolveCiFriendliesOnly fast path (see the {@code pomElements == null} guard in
     * {@link FlattenMojo#execute()}) and falls through to {@link FlattenMojo#createInterpolatedPom}, which calls
     * the same {@code modelCiFriendlyInterpolator} without ever calling
     * {@code setRevisionVariablePattern} on it first. That leaves the pattern null, and
     * {@code CiModelInterpolator#interpolateInternal} NPEs on {@code src.contains(null)} for any POM text.
     *
     * @throws Exception if something goes wrong.
     */
    @Test
    public void resolveCiFriendliesOnlyWithPomElementsDoesNotThrow() throws Exception {
        MavenProject project = rule.readMavenProject(new File(CI_WITH_POM_ELEMENTS_PATH));
        FlattenMojo flattenMojo = (FlattenMojo) rule.lookupConfiguredMojo(project, "flatten");

        flattenMojo.execute();

        assertThat(readPom(CI_WITH_POM_ELEMENTS_FLATTENED).getVersion()).isEqualTo("1.2.3.4");
    }

    @After
    public void removeCiWithPomElementsFlattenedPom() throws IOException {
        File flattenedPom = new File(CI_WITH_POM_ELEMENTS_FLATTENED);
        if (flattenedPom.exists()) {
            if (!flattenedPom.delete()) {
                throw new IOException("Can't delete " + flattenedPom);
            }
        }
    }

    private static final String FLATTEN_LOCAL_PARENT_PATH = "src/test/resources/flatten-local-parent/";
    private static final String FLATTEN_LOCAL_PARENT_FLATTENED = FLATTEN_LOCAL_PARENT_PATH + ".flattened-pom.xml";

    @Test
    public void flattensLocalParentChain() throws Exception {
        MavenProject project = rule.readMavenProject(new File(FLATTEN_LOCAL_PARENT_PATH));
        FlattenMojo flattenMojo = (FlattenMojo) rule.lookupConfiguredMojo(project, "flatten");

        flattenMojo.execute();

        Model flattenedPom = readPom(FLATTEN_LOCAL_PARENT_FLATTENED);
        assertThat(flattenedPom.getVersion()).isEqualTo("1.2.3.4");
        assertThat(flattenedPom.getParent()).isNull();
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

    @After
    public void removeFlattenRelativePathParentFlattenedPom() throws IOException {
        File flattenedPom = new File(FLATTEN_LOCAL_PARENT_FLATTENED);
        if (flattenedPom.exists() && !flattenedPom.delete()) {
            throw new IOException("Can't delete " + flattenedPom);
        }
    }
}
