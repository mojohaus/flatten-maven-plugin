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
public class FlattenMojoNestedExclusionTest {

    private static final String PATH = "src/test/resources/nested-exclusion/";
    private static final String POM = PATH + "pom.xml";
    private static final String FLATTENED_POM = PATH + ".flattened-pom.xml";

    private static final String REPO_PATH = "src/test/resources/nested-exclusion/repository/";

    @Rule
    public MojoRule rule = new MojoRule();

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
    public void testNestedExclusionAreEnforced() throws Exception {

        MavenSession session = newMavenSession(REPO_PATH);
        MavenProject project = loadResolvedProject(session, new File(POM));
        FlattenMojo flattenMojo = (FlattenMojo) rule.lookupConfiguredMojo(project, "flatten");
        rule.setVariableValueToObject(flattenMojo, "session", session);

        flattenMojo.execute();

        MavenProject flattenedProject = loadResolvedProject(session, new File(FLATTENED_POM));

        flattenedProject.getDependencies().stream()
                .filter(dep -> dep.getArtifactId().equals("c"))
                .findAny()
                .ifPresent(dep -> fail(
                        "As B dependency in A is excluding C, no C dependency should be present in flattened POM."));
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
