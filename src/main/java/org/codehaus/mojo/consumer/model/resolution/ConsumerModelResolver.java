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

/**
 * This is a custom implementation of {@link ModelResolver} to emulate the maven POM resolution in order to build the
 * consumer POM.
 * 
 * @see org.codehaus.mojo.consumer.ConsumerMojo
 * @author Robert Scholte
 */
@SuppressWarnings( "deprecation" )
public class ConsumerModelResolver
    implements ModelResolver
{
    /** The local repository for artifact resolution. */
    private ArtifactRepository localRepository;

    /** The factory used to create project artifact instances. */
    private ArtifactFactory artifactFactory;

    /**
     * The constructor.
     * 
     * @param localRepository is the local repository.
     * @param artifactFactory is the factory used to create project artifact instances.
     */
    public ConsumerModelResolver( ArtifactRepository localRepository, ArtifactFactory artifactFactory )
    {
        this.localRepository = localRepository;
        this.artifactFactory = artifactFactory;
    }

    /**
     * {@inheritDoc}
     */
    public ModelSource resolveModel( String groupId, String artifactId, String version )
        throws UnresolvableModelException
    {
        Artifact pomArtifact = this.artifactFactory.createProjectArtifact( groupId, artifactId, version );
        pomArtifact = this.localRepository.find( pomArtifact );

        File pomFile = pomArtifact.getFile();

        return new FileModelSource( pomFile );
    }

    /**
     * {@inheritDoc}
     */
    public void addRepository( Repository repository )
        throws InvalidRepositoryException
    {
        // ignoring... artifact resolution via repository should already have happened before by maven core.
    }

    /**
     * {@inheritDoc}
     */
    public ModelResolver newCopy()
    {
        return null;
    }

}
