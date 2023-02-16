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

import java.io.File;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.profile.ProfileInjector;
import org.apache.maven.model.profile.ProfileSelector;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.flatten.model.resolution.FlattenModelResolver;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test-Case for {@link FlattenMojo}.
 *
 * @author hohwille
 */
public class CreateEffectivePomTest
{
    @Rule
    public MojoRule rule = new MojoRule();

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
        MavenProject mavenProject = rule.readMavenProject( pomFile.getParentFile() );

        MavenSession session = rule.newMavenSession( mavenProject );
        FlattenModelResolver resolver = new FlattenModelResolver( session.getRepositorySession(),
                null, null, null, Collections.emptyList(), Collections.singletonList( mavenProject ) );
        ModelBuildingRequest buildingRequest =
            new DefaultModelBuildingRequest().setPomFile( pomFile ).setModelResolver( resolver )
                .setUserProperties( userProperties );

        FlattenMojo tested = (FlattenMojo) rule.lookupConfiguredMojo( mavenProject, "flatten" );
        rule.setVariableValueToObject( tested, "modelBuilderThreadSafetyWorkaround",
                          buildModelBuilderThreadSafetyWorkaroundForTest() );
        Model effectivePom = tested.createEffectivePom( buildingRequest, false, FlattenMode.defaults );
        assertThat( effectivePom.getName() ).isEqualTo( magicValue );
    }

    /**
     * @return ModelBuilderThreadSafetyWorkaround with a reduced scope for this simple test
     */
    private ModelBuilderThreadSafetyWorkaround buildModelBuilderThreadSafetyWorkaroundForTest()
    {
        return new ModelBuilderThreadSafetyWorkaround()
        {
            @Override
            public ModelBuildingResult build( ModelBuildingRequest buildingRequest, ProfileInjector customInjector,
                                              ProfileSelector customSelector )
                throws ModelBuildingException
            {
                return new DefaultModelBuilderFactory().newInstance()
                    .setProfileInjector( customInjector )
                    .setProfileSelector( customSelector )
                    .build( buildingRequest );
            }
        };
    }
}
