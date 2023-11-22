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

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;

/**
 * This enum contains the predefined modes how to deal with {@link FlattenDescriptor additional POM elements} when
 * {@link FlattenMojo generating the flattened POM}.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0-beta-2
 */
public enum FlattenMode {
    /**
     * For projects that want to keep all {@link FlattenDescriptor optional POM elements}.
     *
     * @deprecated confusing name, unstable contract.
     */
    @Deprecated
    minimum,

    /**
     * For Open-Source-Software projects that want to keep all {@link FlattenDescriptor optional POM elements} except
     * for {@link Model#getRepositories() repositories} and {@link Model#getPluginRepositories() pluginRepositories}.
     */
    oss,

    /**
     * Keeps all {@link FlattenDescriptor optional POM elements} that are required for
     * <a href="https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide">OSS
     * Repository-Hosting</a>.
     */
    ossrh,

    /**
     * Like {@link #oss} but additionally keeps {@link Model#getDependencyManagement() dependencyManagement}, {@link
     * Model#getProperties() properties}, and {@link Build#getPluginManagement() pluginManagement}. Especially it will
     * keep {@link Model#getDependencyManagement() dependencyManagement} and {@link Build#getPluginManagement()
     * pluginManagement} <em>as-is</em> without resolving parent influences and import-scoped dependencies. This is
     * useful if your POM represents a <a href=*
     * "http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Importing_Dependencies"
     * >BOM (Bill Of Material)</a> and you do not want to deploy it as is (to remove parent and resolve version
     * variables, etc.).
     */
    bom,

    /**
     * The default mode that removes all {@link FlattenDescriptor optional POM elements} except
     * {@link Model#getRepositories() repositories}.
     */
    defaults,

    /** Removes all {@link FlattenDescriptor optional POM elements}. */
    clean,

    /** Removes all {@link FlattenDescriptor optional POM elements} and dependencies. */
    fatjar,

    /** Only resolves variables revision, sha1 and changelist. Keeps everything else.
     * See <a href="https://maven.apache.org/maven-ci-friendly.html">Maven CI Friendly</a> for further details.
     */
    resolveCiFriendliesOnly;

    /**
     * @return the {@link FlattenDescriptor} defined by this {@link FlattenMode}.
     */
    public FlattenDescriptor getDescriptor() {

        FlattenDescriptor descriptor = new FlattenDescriptor();
        switch (this) {
            case minimum:
                descriptor.setPluginRepositories(ElementHandling.expand);
                // $FALL-THROUGH$
            case bom:
                // MOJO-2041
                descriptor.setDependencyManagement(ElementHandling.keep);
                descriptor.setPluginManagement(ElementHandling.keep);
                descriptor.setProperties(ElementHandling.expand);
                // $FALL-THROUGH$
            case oss:
                descriptor.setCiManagement(ElementHandling.expand);
                descriptor.setContributors(ElementHandling.expand);
                descriptor.setDistributionManagement(ElementHandling.expand);
                descriptor.setInceptionYear(ElementHandling.expand);
                descriptor.setIssueManagement(ElementHandling.expand);
                descriptor.setMailingLists(ElementHandling.expand);
                descriptor.setOrganization(ElementHandling.expand);
                descriptor.setPrerequisites(ElementHandling.expand);
                // $FALL-THROUGH$
            case ossrh:
                descriptor.setName(ElementHandling.expand);
                descriptor.setDescription(ElementHandling.expand);
                descriptor.setUrl(ElementHandling.expand);
                descriptor.setScm(ElementHandling.expand);
                descriptor.setDevelopers(ElementHandling.expand);
                // $FALL-THROUGH$
            case defaults:
                descriptor.setRepositories(ElementHandling.expand);
                break;
            case fatjar:
                descriptor.setDependencies(ElementHandling.remove);
                break;
            case resolveCiFriendliesOnly:
                descriptor.setBuild(ElementHandling.interpolate);
                descriptor.setCiManagement(ElementHandling.interpolate);
                descriptor.setContributors(ElementHandling.interpolate);
                descriptor.setDependencies(ElementHandling.interpolate);
                descriptor.setDependencyManagement(ElementHandling.interpolate);
                descriptor.setDescription(ElementHandling.interpolate);
                descriptor.setDevelopers(ElementHandling.interpolate);
                descriptor.setDistributionManagement(ElementHandling.interpolate);
                descriptor.setInceptionYear(ElementHandling.interpolate);
                descriptor.setIssueManagement(ElementHandling.interpolate);
                descriptor.setMailingLists(ElementHandling.interpolate);
                descriptor.setModules(ElementHandling.interpolate);
                descriptor.setName(ElementHandling.interpolate);
                descriptor.setArtifactId(ElementHandling.interpolate);
                descriptor.setGroupId(ElementHandling.interpolate);
                descriptor.setPackaging(ElementHandling.interpolate);
                descriptor.setOrganization(ElementHandling.interpolate);
                descriptor.setParent(ElementHandling.resolve);
                descriptor.setPluginManagement(ElementHandling.interpolate);
                descriptor.setPluginRepositories(ElementHandling.interpolate);
                descriptor.setPrerequisites(ElementHandling.interpolate);
                descriptor.setProfiles(ElementHandling.interpolate);
                descriptor.setProperties(ElementHandling.interpolate);
                descriptor.setReporting(ElementHandling.interpolate);
                descriptor.setRepositories(ElementHandling.interpolate);
                descriptor.setScm(ElementHandling.interpolate);
                descriptor.setUrl(ElementHandling.interpolate);
                descriptor.setVersion(ElementHandling.resolve);
                break;
            case clean:
                // nothing to do...
                break;

            default:
                // nothing to do...Could not happen.
                break;
        }
        return descriptor;
    }
}
