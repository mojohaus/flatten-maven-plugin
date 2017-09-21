/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.codehaus.mojo.flatten.model.resolution;

import com.google.common.base.Objects;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds a list of models and allows to retrieve them by their coordinates.
 *
 * @author Christoph Böhme
 */
class ReactorModelPool
{

    private Map<Coordinates, File> models = new HashMap<Coordinates, File>();

    public File find( String groupId, String artifactId, String version )
    {
        return models.get( new Coordinates( groupId, artifactId, version ) );
    }

    public void addProjects( List<MavenProject> projects )
    {
        for ( MavenProject project : projects )
        {
            addProject( project );
        }
    }

    public void addProject( MavenProject project )
    {
        Coordinates coordinates = new Coordinates( project.getGroupId(), project.getArtifactId(),
                project.getVersion() );
        models.put( coordinates, project.getFile() );
    }

    private static final class Coordinates
    {
        final String groupId;
        final String artifactId;
        final String version;

        Coordinates( String groupId, String artifactId, String version ) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Coordinates) {
                Coordinates other = (Coordinates) obj;
                return artifactId.equals(other.artifactId) && groupId.equals(other.groupId)
                        && version.equals(other.version);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(artifactId, groupId, version);
        }

    }

}
