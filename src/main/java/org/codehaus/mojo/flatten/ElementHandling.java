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
 * This enum contains each available handling for a POM element when flattening.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0-beta-4
 */
public enum ElementHandling
{

    /**
     * Flatten the element. For most elements this means that they will be removed and are not present in the flattened
     * POM.
     */
    flatten,

    /** Take the element from the effective POM. */
    expand,

    /** Take the element from the resolved POM. */
    resolve,

    /** Take the element from the interpolated POM (original POM with variables interpolated). */
    interpolate,

    /** Take the element untouched from the original POM. */
    keep,

    /** Remove the element entirely so it will not be present in flattened POM. */
    remove,

    /** Take the element untouched from the original POM. Fix for {@link #keep} */
    keepRaw

}
