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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.DefaultPlexusConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test-Case for {@link FlattenMojo}.
 */
public class KeepCommentsInPomTest {

    private static final String PATH = "src/test/resources/keep-comments-in-pom/";
    private static final String TEST_TARGET_PATH = "target/test/resources/keep-comments-in-pom/";
    private static final String FLATTENED_POM = TEST_TARGET_PATH + ".flattened-pom.xml";
    private static final String EXPECTED_FLATTENED_POM = PATH + "expected-flattened-pom.xml";

    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("\\n|\\r\\n?");

    @Rule
    public MojoRule rule = new MojoRule();

    @Before
    public void setup() {
        new File(TEST_TARGET_PATH).mkdirs();
    }

    /**
     * Test method to check that profile activation file is not interpolated.
     *
     * @throws Exception if something goes wrong.
     */
    @Test
    public void keepsProfileActivationFile() throws Exception {
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
        assertThat(actualContentFile).hasSameTextualContentAs(expectedContentFile);
        assertHasLineSeparator(actualContentFile, System.lineSeparator());
    }

    private static void assertHasLineSeparator(final Path file, final String expectedSeparator) throws IOException {
        try (Scanner scanner = new Scanner(file)) {
            int lineNr = 0;
            String actualSeparator;
            while ((actualSeparator = scanner.findWithinHorizon(NEW_LINE_PATTERN, 0)) != null) {
                lineNr++;
                if (!expectedSeparator.equals(actualSeparator)) {
                    final String actualDesc =
                            actualSeparator.replace("\r", "CR").replace("\n", "LF");
                    final String expectedDesc =
                            expectedSeparator.replace("\r", "CR").replace("\n", "LF");
                    throw new AssertionError(String.format(
                            "\nLine %d of path %s has %s as line separator.\nExpected line separator: %s.",
                            lineNr, file, actualDesc, expectedDesc));
                }
            }
        }
    }
}
