package org.codehaus.mojo.flatten;

import javax.inject.Inject;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test-Case for {@link FlattenMojo}. The local repository is the {@code repository} directory of the fixture.
 */
@ProjectMojoTest
class FlattenMojoConflictWinnerTest {

    @Inject
    private MavenSession session;

    @Inject
    private ProjectBuilder projectBuilder;

    /**
     * Test method to check that version conflicts are correctly honored. Two levels
     * of dependencies are defined for this test:
     * <ul>
     * <li>A depends on B 0.0.1</li>
     * <li>B 0.0.2</li>
     * </ul>
     *
     * The tested pom depends on A and B. It is then expected that B use version
     * 0.0.2 in the flattened pom because B 0.0.2 is closer to the root.
     *
     * 1.7.3 of the plugin was handling this case correctly but since solving #408
     * it is not the case anymore. By removing resolved transitive dependencies from
     * the collect request parameters, the conflict resolution is not applied anymore
     * as resolved transitive dependencies does not appear first anymore. This test ensure
     * that the conflicting dependencies are correctly filtered from collect
     * results and the winner version is kept.
     *
     * @see <a href=
     *      "https://github.com/mojohaus/flatten-maven-plugin/issues/408">Issue
     *      #408</a>
     *
     * @since 1.7.3+
     * @throws Exception if something goes wrong.
     */
    @Test
    @InjectMojo(goal = "flatten", pom = "src/test/resources/conflict-winner/pom.xml")
    void conflictResolutionIsEnforced(FlattenMojo flattenMojo) throws Exception {

        flattenMojo.execute();

        MavenProject flattenedProject =
                loadResolvedProject(flattenMojo.getFlattenedPomFile().toFile());

        List<Dependency> bDependencies = flattenedProject.getDependencies().stream()
                .filter(dep -> dep.getArtifactId().equals("b"))
                .collect(Collectors.toList());

        assertThat(bDependencies)
                .hasSize(1)
                .withFailMessage("There must be only one B dependency in flattened POM.")
                .allMatch(
                        dep -> dep.getVersion().equals("0.0.2"),
                        "B dependency version 0.0.2 must win the conflicting version match in flattened POM.");
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
