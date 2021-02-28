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
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test-Case for {@link FlattenMojo}.
 *
 * @author dehasi
 */
public class FlattenMojoTest {

    private static final String PATH = "src/test/resources/resolve-properties-ci-do-not-interpolate-profile-activation-file/";
    private static final String POM = PATH + "pom.xml";
    private static final String FLATTENED_POM = PATH + ".flattened-pom.xml";

    @Rule
    public MojoRule rule = new MojoRule();

    /**
     * Test method to check that profile activation file is not interpolated.
     *
     * @throws Exception if something goes wrong.
     */
    @Test
    public void keepsProfileActivationFile() throws Exception {
        MavenProject project = rule.readMavenProject( new File( PATH ) );
        FlattenMojo flattenMojo = (FlattenMojo) rule.lookupConfiguredMojo( project, "flatten" );

        flattenMojo.execute();

        assertThat( profileActivationFile( FLATTENED_POM ) )
            .isEqualTo( profileActivationFile( POM ) );
    }

    private static String profileActivationFile( String pom ) throws Exception  {
        return readPom( pom ).getProfiles().get( 0 ).getActivation().getFile().getExists();
    }

    private static Model readPom(String pomFilePath) throws IOException, XmlPullParserException {
        try ( FileInputStream input = new FileInputStream( new File( pomFilePath ) ) ) {
            return new MavenXpp3Reader().read( input );
        }
    }

    /**
     * After test method. Removes flattened-pom.xml file which is created during test.
     *
     * @throws IOException if can't remove file.
     */
    @After
    public void removeFlattenedPom() throws IOException {
        File flattenedPom = new File( FLATTENED_POM );
        if ( flattenedPom.exists() ) {
            if ( !flattenedPom.delete() ) {
                throw new IOException( "Can't delete " +  flattenedPom );
            }
        }
    }
}
