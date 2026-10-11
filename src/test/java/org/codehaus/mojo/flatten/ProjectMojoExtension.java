package org.codehaus.mojo.flatten;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.apache.maven.api.plugin.testing.MojoExtension;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.internal.aether.DefaultRepositorySystemSessionFactory;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.RepositorySystemSession;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.mockito.Mockito.lenient;

/**
 * {@code MojoExtension} gives the mojo an empty project and no repository session, so the POM's dependencies and
 * properties are missing. This builds the project from a copy of the POM's directory, with its dependencies resolved
 * offline, and loads it into the project the harness gives the mojo; the flattened POM is written to the copy.
 * <p>
 * The local repository is the {@code repository} directory next to the POM if there is one, and
 * {@code target/local-repo} in the copy otherwise.
 */
class ProjectMojoExtension extends MojoExtension {

    private static final ExtensionContext.Namespace OWN = ExtensionContext.Namespace.create(ProjectMojoExtension.class);

    @Override
    protected Mojo lookupMojo(
            ExtensionContext extensionContext,
            String[] coord,
            Xpp3Dom pluginConfiguration,
            PluginDescriptor descriptor,
            Path pomPath)
            throws Exception {
        if (pomPath == null) {
            return super.lookupMojo(extensionContext, coord, pluginConfiguration, descriptor, null);
        }
        Path workDir = Files.createTempDirectory("project-mojo-test");
        extensionContext.getStore(OWN).put(workDir, (AutoCloseable) () -> FileUtils.deleteDirectory(workDir.toFile()));
        FileUtils.copyDirectoryStructure(pomPath.getParent().toFile(), workDir.toFile());
        Path pom = workDir.resolve(pomPath.getFileName().toString());

        PlexusContainer container = getContainer(extensionContext);
        MavenSession session = container.lookup(MavenSession.class);
        RepositorySystemSession repositorySession = newRepositorySession(container, session.getRequest(), workDir);
        lenient().doReturn(repositorySession).when(session).getRepositorySession();
        if (session.getSettings() == null) {
            lenient().doReturn(new Settings()).when(session).getSettings();
        }

        ProjectBuildingRequest request = session.getRequest().getProjectBuildingRequest();
        request.setRepositorySession(repositorySession);
        // test POMs are inputs for the goal, such as an unresolved ${revision}
        request.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        // as a build would, so that project.getArtifacts() is filled
        request.setResolveDependencies(true);
        MavenProject built = container
                .lookup(ProjectBuilder.class)
                .build(pom.toFile(), request)
                .getProject();
        load(container, session, built);

        return super.lookupMojo(extensionContext, coord, pluginConfiguration, descriptor, pom);
    }

    private static RepositorySystemSession newRepositorySession(
            PlexusContainer container, MavenExecutionRequest request, Path workDir) throws Exception {
        Path repository = workDir.resolve("repository");
        if (!Files.isDirectory(repository)) {
            repository = workDir.resolve("target/local-repo");
        }
        request.setLocalRepositoryPath(repository.toFile());
        request.setBaseDirectory(workDir.toFile());
        // tests never reach a remote repository
        request.setOffline(true);
        container.lookup(MavenExecutionRequestPopulator.class).populateDefaults(request);
        return container.lookup(DefaultRepositorySystemSessionFactory.class).newRepositorySession(request);
    }

    /** Loads {@code built} into the project the harness gives the mojo. */
    private static void load(PlexusContainer container, MavenSession session, MavenProject built) throws Exception {
        MavenProject project = container.lookup(MavenProject.class);
        project.setModel(built.getModel());
        project.setOriginalModel(built.getOriginalModel());
        project.setFile(built.getFile());
        project.setArtifact(built.getArtifact());
        project.setArtifacts(built.getArtifacts());
        project.setRemoteArtifactRepositories(built.getRemoteArtifactRepositories());
        project.setPluginArtifactRepositories(built.getPluginArtifactRepositories());
        // a build of this project alone
        List<MavenProject> projects = Collections.singletonList(project);
        lenient().doReturn(projects).when(session).getProjects();
        lenient().doReturn(projects).when(session).getAllProjects();
    }
}
