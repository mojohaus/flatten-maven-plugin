package org.codehaus.mojo.consumer.model.resolution;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;


public class ConsumerModelResolver implements ModelResolver
{
    private ArtifactRepository localRepository;
    
    private ArtifactFactory artifactFactory;
    
    public ConsumerModelResolver( ArtifactRepository localRepository, ArtifactFactory artifactFactory )
    {
        this.localRepository = localRepository;
        this.artifactFactory = artifactFactory;
    }

    public ModelSource resolveModel( String groupId, String artifactId, String version )
        throws UnresolvableModelException
    {
        Artifact pomArtifact = artifactFactory.createProjectArtifact( groupId, artifactId, version );
        pomArtifact = localRepository.find( pomArtifact );

        File pomFile = pomArtifact.getFile();

        return new FileModelSource( pomFile );
    }

    public void addRepository( Repository repository )
        throws InvalidRepositoryException
    {
    }

    public ModelResolver newCopy()
    {
        return null;
    }

}
