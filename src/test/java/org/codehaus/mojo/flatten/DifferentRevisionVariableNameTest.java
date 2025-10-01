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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.DefaultPlexusConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

public class DifferentRevisionVariableNameTest {
    private static final String PATH = "src/test/resources/differentrevisionvariablename/";
    private static final String TEST_TARGET_PATH = "target/test/resources/differentrevisionvariablename/";
    private static final String FLATTENED_POM = TEST_TARGET_PATH + ".flattened-pom.xml";
    private static final String EXPECTED_FLATTENED_POM = PATH + "expected-flattened-pom.xml";

    @Rule
    public MojoRule rule = new MojoRule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setup() {
        new File(TEST_TARGET_PATH).mkdirs();
    }

    @Test
    public void differentRevisionVariableNameTest() throws Exception {
        MavenProject project = rule.readMavenProject(new File(PATH));
        FlattenMojo flattenMojo = (FlattenMojo) rule.lookupConfiguredMojo(project, "flatten");

        DefaultPlexusConfiguration tempPluginConfiguration = new DefaultPlexusConfiguration("test");
        tempPluginConfiguration.addChild("outputDirectory", TEST_TARGET_PATH);
        tempPluginConfiguration.addChild("keepCommentsInPom", "true");
        rule.configureMojo(flattenMojo, tempPluginConfiguration);

        // execute writes new FLATTENED_POM
        flattenMojo.execute();

        Path expectedContentFile = Paths.get(EXPECTED_FLATTENED_POM);
        Path actualContentFile = Paths.get(FLATTENED_POM);
        assertThat(actualContentFile).hasSameTextualContentAs(expectedContentFile, StandardCharsets.UTF_8);
    }
}
