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
package org.codehaus.mojo.flatten;

import javax.inject.Named;
import javax.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.api.model.Model;
import org.apache.maven.api.model.ModelBase;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.inheritance.DefaultInheritanceAssembler;
import org.apache.maven.model.merge.MavenModelMerger;

/**
 * The DefaultInheritanceAssembler is encapsulating the InheritanceModelMerger.
 * The only way to override functionality needed is to define an own InheritanceAssembler
 * to provide the needed ModelMerger.
 * The container is providing the InheritanceAssembler.
 * This class will be configured in the META-INF/sisu/javax.inject.Named by the sisu-maven-plugin.
 * <p>
 * see issue https://github.com/mojohaus/flatten-maven-plugin/issues/220
 *
 * @author kemalsoysal
 * @author ralfluebeck
 */
@Singleton
@Named
public class DirectDependenciesInheritanceAssembler extends DefaultInheritanceAssembler {

    protected InheritanceModelMerger merger = new DirectDependenciesInheritanceModelMerger();

    /**
     * copied from super implementation because it is private
     */
    private static final String CHILD_DIRECTORY = "child-directory";

    /**
     * copied from super implementation because it is private
     */
    private static final String CHILD_DIRECTORY_PROPERTY = "project.directory";

    protected FlattenDependencyMode flattenDependencyMode;

    /**
     *
     */
    public DirectDependenciesInheritanceAssembler() {}

    @Override
    public Model assembleModelInheritance(
            Model child, Model parent, ModelBuildingRequest request, ModelProblemCollector problems) {
        Map<Object, Object> hints = new HashMap<>();
        String childPath = child.getProperties().get(CHILD_DIRECTORY_PROPERTY);
        if (childPath == null) {
            childPath = child.getArtifactId();
        }
        hints.put(CHILD_DIRECTORY, childPath);
        hints.put(MavenModelMerger.CHILD_PATH_ADJUSTMENT, getChildPathAdjustment(child, parent, childPath));
        return merger.merge(child, parent, false, hints);
    }

    /**
     * copied from super implementation because it is private though the adjustment
     * is only for compatibility due to the comment with Maven 2.0
     *
     * @param child
     * @param parent
     * @param childDirectory
     * @return
     */
    private String getChildPathAdjustment(Model child, Model parent, String childDirectory) {
        String adjustment = "";

        if (parent != null) {
            String childName = child.getArtifactId();

            /*
             * This logic (using filesystem, against wanted independence from the user
             * environment) exists only for the sake of backward-compat with 2.x (MNG-5000).
             * In general, it is wrong to base URL inheritance on the module directory names
             * as this information is unavailable for POMs in the repository. In other
             * words, modules where artifactId != moduleDirName will see different effective
             * URLs depending on how the model was constructed (from filesystem or from
             * repository).
             */
            if (child.getProjectDirectory() != null) {
                childName = child.getProjectDirectory().getFileName().toString();
            }

            for (String module : parent.getModules()) {
                module = module.replace('\\', '/');

                if (module.regionMatches(true, module.length() - 4, ".xml", 0, 4)) {
                    module = module.substring(0, module.lastIndexOf('/') + 1);
                }

                String moduleName = module;
                if (moduleName.endsWith("/")) {
                    moduleName = moduleName.substring(0, moduleName.length() - 1);
                }

                int lastSlash = moduleName.lastIndexOf('/');

                moduleName = moduleName.substring(lastSlash + 1);

                if ((moduleName.equals(childName) || (moduleName.equals(childDirectory))) && lastSlash >= 0) {
                    adjustment = module.substring(0, lastSlash);
                    break;
                }
            }
        }

        return adjustment;
    }

    /**
     * InheritanceModelMerger
     */
    protected class DirectDependenciesInheritanceModelMerger
            extends DefaultInheritanceAssembler.InheritanceModelMerger {

        @Override
        public Model merge(Model target, Model source, boolean sourceDominant, Map<?, ?> hints) {
            return super.merge(target, source, sourceDominant, hints);
        }

        @Override
        protected void mergeModelBase_Dependencies(
                ModelBase.Builder builder,
                ModelBase target,
                ModelBase source,
                boolean sourceDominant,
                Map<Object, Object> context) {
            if (flattenDependencyMode == FlattenDependencyMode.direct) {
                return;
            }
            super.mergeModelBase_Dependencies(builder, target, source, sourceDominant, context);
        }
    }
}
