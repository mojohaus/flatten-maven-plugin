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

/**
 * This enum contains the predefined modes on how to flatten the dependencies.
 *
 * @author Ray Tsang
 * @since 1.2.0
 */
public enum FlattenDependencyMode
{
    /**
     * Flatten only the direct dependency versions. This is the default mode and compatible with
     * Flatten Plugin prior to 1.2.0.
     */
    direct,

    /**
     * Flatten both direct and transitive dependencies. This will examine the full dependency tree, and pull up
     * all transitive dependencies as a direct dependency, and setting their versions appropriately.
     *
     * This is recommended if you are releasing a library that uses dependency management to manage dependency
     * versions.
     */
    all
}
