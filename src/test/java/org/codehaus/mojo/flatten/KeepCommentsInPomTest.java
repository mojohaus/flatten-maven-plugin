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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test-Case for {@link FlattenMojo}.
 */
@ProjectMojoTest
class KeepCommentsInPomTest {

    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("\\n|\\r\\n?");

    @TempDir
    Path temporaryFolder;

    /**
     * Test method to check that profile activation file is not interpolated.
     *
     * @throws Exception if something goes wrong.
     */
    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/keep-comments-in-pom/pom.xml")
    @MojoParameter(name = "keepCommentsInPom", value = "true")
    void keepsProfileActivationFile(FlattenMojo flattenMojo) throws Exception {
        // execute writes new FLATTENED_POM
        flattenMojo.execute();

        Path actualContentFile = flattenMojo.getFlattenedPomFile();
        Path expectedContentFile = actualContentFile.resolveSibling("expected-flattened-pom.xml");
        assertThat(actualContentFile).hasSameTextualContentAs(expectedContentFile, StandardCharsets.UTF_8);
        assertHasLineSeparator(actualContentFile, System.lineSeparator());
    }

    @Test
    void allNewLineSeparatorShouldBeNormalized() throws Exception {

        FlattenMojo flattenMojo = new FlattenMojo();

        File tempFile = Files.createTempFile(temporaryFolder, null, null).toFile();
        flattenMojo.writeStringToFile("line 1\n" + "line 2\r\n" + "line 3\r", tempFile.toPath(), "UTF-8");

        String lf = System.lineSeparator();

        assertHasLineSeparator(tempFile.toPath(), lf);
        assertThat(tempFile)
                .hasBinaryContent(("line 1" + lf + "line 2" + lf + "line 3" + lf).getBytes(StandardCharsets.UTF_8));
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
