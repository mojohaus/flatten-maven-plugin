package org.codehaus.mojo.flatten.model.resolution;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

/**
 * This is a custom implementation of {@link ModelResolver} to emulate the maven POM resolution in order to build the
 * flattened POM.
 *
 * @see org.codehaus.mojo.flatten.FlattenMojo
 * @author Robert Scholte
 */
@SuppressWarnings( "deprecation" )
public class FlattenModelResolver
    implements ModelResolver
{

    /** The local repository for artifact resolution. */
    private ArtifactRepository localRepository;
    /** The factory used to create project artifact instances. */
    private ArtifactFactory artifactFactory;

    /** The modules of the project being built. */
    private ReactorModelPool reactorModelPool;

    /**
     * The constructor.
     *  @param localRepository is the local repository.
     * @param artifactFactory is the factory used to create project artifact instances.
     * @param reactorModels is the list of modules of the project being built.
     */
    public FlattenModelResolver( ArtifactRepository localRepository, ArtifactFactory artifactFactory,
        List<MavenProject> reactorModels )
    {

        this.localRepository = localRepository;
        this.artifactFactory = artifactFactory;
        this.reactorModelPool = new ReactorModelPool();
        reactorModelPool.addProjects( reactorModels );
    }

    private FlattenModelResolver( FlattenModelResolver other )
    {
        this.localRepository = other.localRepository;
        this.artifactFactory = other.artifactFactory;
        this.reactorModelPool = other.reactorModelPool;
    }

    /**
     * {@inheritDoc}
     */
    public ModelSource resolveModel( String groupId, String artifactId, String version )
    {

        File pomFile = reactorModelPool.find(groupId, artifactId, version);
        if ( pomFile == null )
        {
            Artifact pomArtifact = this.artifactFactory.createProjectArtifact( groupId, artifactId, version );
            pomArtifact = this.localRepository.find( pomArtifact );
            pomFile = pomArtifact.getFile();
        }

        return new FileModelSource( pomFile );
    }

    /**
     * {@inheritDoc}
     */
    public void addRepository( Repository repository )
    {

        // ignoring... artifact resolution via repository should already have happened before by maven core.
    }

    /**
     * {@inheritDoc}
     */
    public ModelResolver newCopy()
    {
        return new FlattenModelResolver( this );
    }

    /**
     * Resolves the POM for the specified parent.
     *
     * @param parent the parent coordinates to resolve, must not be {@code null}
     * @return The source of the requested POM, never {@code null}
     * @since Apache-Maven-3.2.2 (MNG-5639)
     */
    public ModelSource resolveModel( Parent parent )
    {
        return resolveModel( parent.getGroupId(), parent.getArtifactId(), parent.getVersion() );
    }

    public ModelSource resolveModel(Dependency dependency) throws UnresolvableModelException {
        return resolveModel( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion() );
    }

    /**
     * @since Apache-Maven-3.2.2 (MNG-5639)
     */
    public void resetRepositories()
    {
        // ignoring... artifact resolution via repository should already have happened before by maven core.
    }

    /**
     * @param repository The repository to add to the internal search chain, must not be {@code null}.
     * @param replace {true} when repository with same id should be replaced, otherwise {@code false}.
     * @since Apache-Maven-3.2.3 (MNG-5663)
     */
    public void addRepository( Repository repository, boolean replace )
    {
        // ignoring... artifact resolution via repository should already have happened before by maven core.
    }
}
