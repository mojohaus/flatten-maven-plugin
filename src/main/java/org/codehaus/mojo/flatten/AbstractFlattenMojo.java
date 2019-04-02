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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * This is the abstract base class for {@link AbstractMojo MOJOs} that realize the different goals of this plugin.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 */
public abstract class AbstractFlattenMojo
    extends AbstractMojo
{

    /**
     * The directory where the generated flattened POM file will be written to.
     */
    @Parameter( defaultValue = "${project.basedir}" )
    private File outputDirectory;

    /**
     * The filename of the generated flattened POM file.
     */
    @Parameter( property = "flattenedPomFilename", defaultValue = ".flattened-pom.xml" )
    private String flattenedPomFilename;

    /**
     * The constructor.
     */
    public AbstractFlattenMojo()
    {

        super();
    }

    /**
     * @return the filename of the generated flattened POM file.
     */
    public String getFlattenedPomFilename()
    {
        return this.flattenedPomFilename;
    }

    /**
     * @return the directory where the generated flattened POM file will be written to.
     */
    public File getOutputDirectory()
    {
        return this.outputDirectory;
    }

    /**
     * @return a {@link File} instance pointing to the flattened POM.
     */
    protected File getFlattenedPomFile()
    {
        return new File( getOutputDirectory(), getFlattenedPomFilename() );
    }

}
