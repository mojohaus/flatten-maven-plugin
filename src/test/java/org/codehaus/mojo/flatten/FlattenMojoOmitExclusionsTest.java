package org.codehaus.mojo.flatten;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test case for the omitexclusions configuration option.
 */
@ProjectMojoTest
class FlattenMojoOmitExclusionsTest {

    /**
     * Verify that when the omit exclusions configuration option is set then the
     * exclusions stanza of any dependencies is not copied into the flattened
     * POM.
     */
    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/omit-exclusions/pom.xml")
    void testOmitExclusions(FlattenMojo flattenMojo) throws Exception {
        // -- When...
        //
        flattenMojo.execute();

        // -- Then...
        //
        readPom(flattenMojo.getFlattenedPomFile()).getDependencies().stream()
                .filter(dep -> !dep.getExclusions().isEmpty())
                .findAny()
                .ifPresent(dep -> fail("No exclusions should be present in flattened POM."));
    }

    private static Model readPom(Path pomFile) throws IOException, XmlPullParserException {
        try (InputStream input = Files.newInputStream(pomFile)) {
            return new MavenXpp3Reader().read(input);
        }
    }
}
