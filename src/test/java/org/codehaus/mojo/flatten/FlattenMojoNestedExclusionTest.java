package org.codehaus.mojo.flatten;

import javax.inject.Inject;

import java.io.File;

import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test-Case for {@link FlattenMojo}. The local repository is the {@code repository} directory of the fixture.
 */
@ProjectMojoTest
class FlattenMojoNestedExclusionTest {

    @Inject
    private MavenSession session;

    @Inject
    private ProjectBuilder projectBuilder;

    /**
     * Test method to check that nested exclusion are correctly honored.
     * Three levels of dependencies are defined for this test:
     * <ul>
     * <li>A depends on B with A excluding C through B</li>
     * <li>B depends on C</li>
     * </ul>
     *
     * The tested pom depends on A only. It is then expected that C is not present in the flattened pom.
     * Only A and B should be present.
     *
     * 1.3.0 of the plugin was handling this case correctly but since 1.4.0 it is not the case anymore.
     * @see <a href="https://github.com/mojohaus/flatten-maven-plugin/issues/408">Issue #408</a>
     *
     * @since 1.7.2+
     * @throws Exception if something goes wrong.
     */
    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/nested-exclusion/pom.xml")
    void testNestedExclusionAreEnforced(FlattenMojo flattenMojo) throws Exception {

        flattenMojo.execute();

        MavenProject flattenedProject =
                loadResolvedProject(flattenMojo.getFlattenedPomFile().toFile());

        flattenedProject.getDependencies().stream()
                .filter(dep -> dep.getArtifactId().equals("c"))
                .findAny()
                .ifPresent(dep -> fail(
                        "As B dependency in A is excluding C, no C dependency should be present in flattened POM."));
    }

    /**
     * Load a maven project with resolved dependencies (same as standard maven execution).
     * @param pomFile the POM file to load and resolve dependencies for
     * @return the resolved MavenProject instance with dependencies
     * @throws ProjectBuildingException if an error occurs while building the project
     */
    private MavenProject loadResolvedProject(File pomFile) throws ProjectBuildingException {
        ProjectBuildingRequest buildingRequest = session.getRequest().getProjectBuildingRequest();
        buildingRequest.setRepositorySession(session.getRepositorySession());
        buildingRequest.setResolveDependencies(true);

        ProjectBuildingResult result = projectBuilder.build(pomFile, buildingRequest);
        return result.getProject();
    }
}
