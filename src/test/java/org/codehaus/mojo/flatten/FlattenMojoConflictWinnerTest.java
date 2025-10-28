package org.codehaus.mojo.flatten;

import java.io.File;
import java.io.IOException;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Test-Case for {@link FlattenMojo}.
 */
public class FlattenMojoConflictWinnerTest {

    private static final String PATH = "src/test/resources/conflict-winner/";
    private static final String POM = PATH + "pom.xml";
    private static final String FLATTENED_POM = PATH + ".flattened-pom.xml";

    private static final String REPO_PATH = "src/test/resources/conflict-winner/repository/";

    @Rule
    public MojoRule rule = new MojoRule();

    /**
     * Test method to check that version conflict are correctly honored. Two levels
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
    public void testNestedExclusionAreEnforced() throws Exception {

        MavenSession session = newMavenSession(REPO_PATH);
        MavenProject project = loadResolvedProject(session, new File(POM));
        FlattenMojo flattenMojo = (FlattenMojo) rule.lookupConfiguredMojo(project, "flatten");
        rule.setVariableValueToObject(flattenMojo, "session", session);

        flattenMojo.execute();

        MavenProject flattenedProject = loadResolvedProject(session, new File(FLATTENED_POM));

        flattenedProject.getDependencies().stream()
                .filter(dep -> dep.getArtifactId().equals("b"))
                .filter(dep -> dep.getVersion().equals("0.0.1"))
                .findAny()
                .ifPresent(dep -> fail(
                        "B dependency version 0.0.2 must win the conflicting version match in flattened POM."));
    }

    /**
     * Load a maven project with resolved dependencies (same as standard maven execution).
     * By default dependencies are not resolved by MojoRule and project.getArtifacts is empty
     * @param session the Maven session to use for building the project
     * @param pomFile the POM file to load and resolve dependencies for
     * @return the resolved MavenProject instance with dependencies
     * @throws ComponentLookupException if the ProjectBuilder component cannot be found
     * @throws ProjectBuildingException if an error occurs while building the project
     */
    private MavenProject loadResolvedProject(MavenSession session, File pomFile)
            throws ComponentLookupException, ProjectBuildingException {
        ProjectBuilder projectBuilder = rule.lookup(ProjectBuilder.class);

        ProjectBuildingRequest buildingRequest = session.getProjectBuildingRequest();
        buildingRequest.setResolveDependencies(true);

        ProjectBuildingResult result = projectBuilder.build(pomFile, buildingRequest);
        return result.getProject();
    }

    /**
     * Create a new maven session with a local repository at the given path.
     * @param repoPath the path to the local repository to use for the session
     * @return a new MavenSession instance configured with the specified local repository
     * @throws NoLocalRepositoryManagerException if the local repository manager cannot be created
     */
    protected MavenSession newMavenSession(String repoPath) throws NoLocalRepositoryManagerException {
        MavenExecutionRequest request = new DefaultMavenExecutionRequest();
        MavenExecutionResult result = new DefaultMavenExecutionResult();

        MavenSession session =
                new MavenSession(rule.getContainer(), MavenRepositorySystemUtils.newSession(), request, result);
        LocalRepository testRepo = new LocalRepository(repoPath);
        LocalRepositoryManager lrm =
                new SimpleLocalRepositoryManagerFactory().newInstance(session.getRepositorySession(), testRepo);

        ((DefaultRepositorySystemSession) session.getRepositorySession()).setLocalRepositoryManager(lrm);
        return session;
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
}
