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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * This MOJO realizes the goal <code>flatten:clean</code> that deletes any files created by
 * <code>{@link FlattenMojo flatten:flatten}</code> (more specific the flattened POM file which is by default
 * <code>.flattened-pom.xml</code>). See also <a href="http://jira.codehaus.org/browse/MOJO-2030">MOJO-2030</a> for
 * further details.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0-beta-2
 */
@Mojo(
        name = "clean",
        requiresProject = true,
        requiresDirectInvocation = false,
        executionStrategy = "once-per-session",
        threadSafe = true)
public class CleanMojo extends AbstractFlattenMojo {

    /**
     * The constructor.
     */
    public CleanMojo() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        File flattenedPomFile = getFlattenedPomFile();
        if (flattenedPomFile.isFile()) {
            getLog().info("Deleting " + flattenedPomFile.getPath());
            boolean deleted = flattenedPomFile.delete();
            if (!deleted) {
                throw new MojoFailureException("Could not delete " + flattenedPomFile.getAbsolutePath());
            }
        }
    }
}
