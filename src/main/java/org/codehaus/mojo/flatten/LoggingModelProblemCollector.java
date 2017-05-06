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

import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblemCollectorRequest;
import org.apache.maven.plugin.logging.Log;

/**
 * This is an implementation of {@link ModelProblemCollector} that is logging all problems.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0-beta-4
 */
public class LoggingModelProblemCollector
    implements ModelProblemCollector
{

    private final Log logger;

    /**
     * The constructor.
     *
     * @param logger is the {@link Log}.
     */
    public LoggingModelProblemCollector( Log logger )
    {
        super();
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    public void add( ModelProblemCollectorRequest req )
    {
        this.logger.warn( req.getMessage(), req.getException() );
    }

}
