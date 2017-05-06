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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;

/**
 * This is a simple container for {@link Dependency} objects. Rather than using a {@link List} this object allows
 * operations like {@link #contains(Dependency)} that work reliably even though {@link Dependency} class does not
 * properly implement {@link Object#equals(Object) equals}.
 * 
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 */
public class Dependencies
{

    /** @see #add(Dependency) */
    private final Map<String, Dependency> key2DependencyMap;

    /**
     * The constructor.
     */
    public Dependencies()
    {

        super();
        this.key2DependencyMap = new HashMap<String, Dependency>();
    }

    /**
     * @param dependency is the {@link Dependency} to {@link Map#put(Object, Object) put} or {@link Map#get(Object) get}
     *            .
     * @return the {@link java.util.Map.Entry#getKey() key} for the {@link Dependency}.
     */
    protected String getKey( Dependency dependency )
    {

        return dependency.getManagementKey() + ":" + dependency.getClassifier();
    }

    /**
     * @param dependencies the {@link List} of {@link Dependency} objects to {@link #add(Dependency) add}.
     */
    public void addAll( List<Dependency> dependencies )
    {

        for ( Dependency dependency : dependencies )
        {
            add( dependency );
        }
    }

    /**
     * @param dependency the {@link Dependency} to add.
     */
    public void add( Dependency dependency )
    {

        String key = getKey( dependency );
        Dependency replaced = this.key2DependencyMap.put( key, dependency );
        if ( replaced != null )
        {
            throw new IllegalStateException( "Duplicate dependency! Original: " + getKey( replaced ) + " duplicate: "
                + getKey( dependency ) );
        }
    }

    /**
     * @param dependency the {@link Dependency} to test.
     * @return <code>true</code> if the given {@link Dependency} is contained in these {@link Dependencies},
     *         <code>false</code> otherwise.
     */
    public boolean contains( Dependency dependency )
    {

        // ATTENTION: Dependency does not have a proper equals implementation, we only check that the key is
        // contained. However, this should be sufficient for all reasonable scenarios...
        return this.key2DependencyMap.containsKey( getKey( dependency ) );
    }

    /**
     * @return a {@link List} with the {@link Dependency} objects contained in these {@link Dependencies}.
     */
    public List<Dependency> toList()
    {

        List<Dependency> result = new ArrayList<Dependency>( this.key2DependencyMap.values() );
        return result;
    }

}
