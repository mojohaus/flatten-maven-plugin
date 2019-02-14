package org.codehaus.mojo.flatten;

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

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.handler.manager.DefaultArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.shared.dependencies.resolve.internal.DefaultDependencyResolver;
import org.assertj.core.api.Assertions;
import org.codehaus.mojo.flatten.model.resolution.FlattenModelResolver;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.model.building.DefaultModelBuilderFactory;

/**
 * Test-Case for {@link FlattenMojo}.
 *
 * @author hohwille
 */
public class CreateEffectivePomTest
    extends Assertions
{

    FlattenMojo tested = new FlattenMojo();
    
    /**
     * Tests method to create effective POM.
     *
     * @throws Exception if something goes wrong.
     */
    @Test
    public void testCreateEffectivePom()
        throws Exception
    {

        String magicValue = "magic-value";
        Properties userProperties = new Properties();
        userProperties.setProperty( "cmd.test.property", magicValue );

        File pomFile = new File( "src/test/resources/cmdpropertysubstituion/pom.xml" );
        ArtifactRepository localRepository = new MavenArtifactRepository();
        localRepository.setLayout( new DefaultRepositoryLayout() );
        ArtifactFactory artifactFactory = new DefaultArtifactFactory();
        ArtifactHandlerManager artifactHandlerManager = new DefaultArtifactHandlerManager();
        setDeclaredField( artifactFactory, "artifactHandlerManager", artifactHandlerManager );
        Map<String, ArtifactHandler> artifactHandlers = new HashMap<String, ArtifactHandler>();
        setDeclaredField( artifactHandlerManager, "artifactHandlers", artifactHandlers );
        DefaultDependencyResolver depencencyResolver = new DefaultDependencyResolver();
        DefaultProjectBuildingRequest projectBuildingRequest = new DefaultProjectBuildingRequest();
        FlattenModelResolver resolver = new FlattenModelResolver( localRepository, artifactFactory,
                depencencyResolver, projectBuildingRequest, Collections.<MavenProject>emptyList() );
        ModelBuildingRequest buildingRequest =
            new DefaultModelBuildingRequest().setPomFile( pomFile ).setModelResolver( resolver ).setUserProperties( userProperties );
        setDeclaredField(tested, "defaultModelBuilder", new DefaultModelBuilderFactory().newInstance());
        Model effectivePom = tested.createEffectivePom( buildingRequest, false, FlattenMode.defaults );
        assertThat( effectivePom.getName() ).isEqualTo( magicValue );
    }

    private void setDeclaredField( Object pojo, String fieldName, Object propertyValue )
        throws NoSuchFieldException, IllegalAccessException
    {
        Field field = pojo.getClass().getDeclaredField( fieldName );
        field.setAccessible( true );
        field.set( pojo, propertyValue );
    }
}
