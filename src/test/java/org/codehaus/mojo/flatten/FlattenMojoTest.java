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
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
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
}
