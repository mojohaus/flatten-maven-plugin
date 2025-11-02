package org.codehaus.mojo.flatten;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test case for the omitexclusions configuration option.
 */
public class FlattenMojoOmitExclusionsTest {

    private static final String PATH = "src/test/resources/omit-exclusions/";
    private static final String FLATTENED_POM = PATH + ".flattened-pom.xml";

    @Rule
    public MojoRule rule = new MojoRule();

    /**
     * Verify that when the omit exclusions configuration option is set then the
     * exclusions stanza of any dependencies is not copied into the flattened
     * POM.
     */
    @Test
    void omitExclusions() throws Exception {
        // -- Given...
        //
        MavenProject project = rule.readMavenProject(new File(PATH));
        FlattenMojo flattenMojo = (FlattenMojo) rule.lookupConfiguredMojo(project, "flatten");

        // -- When...
        //
        flattenMojo.execute();

        // -- Then...
        //
        readPom(FLATTENED_POM).getDependencies().stream()
                .filter(dep -> !dep.getExclusions().isEmpty())
                .findAny()
                .ifPresent(dep -> fail("No exclusions should be present in flattened POM."));
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
    @AfterEach
    void removeFlattenedPom() throws IOException {
        File flattenedPom = new File(FLATTENED_POM);
        if (flattenedPom.exists()) {
            if (!flattenedPom.delete()) {
                throw new IOException("Can't delete " + flattenedPom);
            }
        }
    }
}
