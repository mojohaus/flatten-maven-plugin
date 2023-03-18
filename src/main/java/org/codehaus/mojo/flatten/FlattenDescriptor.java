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

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * The descriptor that defines the additional POM elements that should be kept and copied to flattened POM.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0-beta-2
 */
public class FlattenDescriptor {

    private final Map<String, ElementHandling> name2handlingMap;

    /**
     * The constructor.
     */
    public FlattenDescriptor() {
        super();
        this.name2handlingMap = new HashMap<>();
    }

    /**
     * The constructor.
     *
     * @param descriptor is the raw {@link Xpp3Dom} from the MOJOs configuration.
     */
    public FlattenDescriptor(Xpp3Dom descriptor) {
        this();
        for (PomProperty<?> property : PomProperty.getPomProperties()) {
            String element = property.getName();
            if (descriptor.getChild(element) != null) {
                this.name2handlingMap.put(element, ElementHandling.expand);
            }
        }
    }

    /**
     * Generic method to get a {@link ElementHandling}.
     *
     * @param property is the {@link PomProperty} such as {@link PomProperty#NAME}.
     * @return the {@link ElementHandling}. Will be {@link ElementHandling#flatten flattened} as fallback if undefined.
     */
    public ElementHandling getHandling(PomProperty<?> property) {
        ElementHandling handling = this.name2handlingMap.get(property.getName());
        if (handling == null) {
            handling = ElementHandling.flatten;
        }
        return handling;
    }

    /**
     * Generic method to set an {@link ElementHandling}.
     *
     * @param property is the {@link PomProperty} such as {@link PomProperty#NAME}.
     * @param handling the new {@link ElementHandling}.
     */
    public void setHandling(PomProperty<?> property, ElementHandling handling) {

        this.name2handlingMap.put(property.getName(), handling);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getName() name}.
     */
    public ElementHandling getName() {
        return getHandling(PomProperty.NAME);
    }

    /**
     * @param name the {@link #getName() name} to set.
     */
    public void setName(ElementHandling name) {
        setHandling(PomProperty.NAME, name);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getDescription() description}.
     */
    public ElementHandling getDescription() {
        return getHandling(PomProperty.DESCRIPTION);
    }

    /**
     * @param description the {@link #getDescription() description} to set.
     */
    public void setDescription(ElementHandling description) {
        setHandling(PomProperty.DESCRIPTION, description);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getUrl() URL}.
     */
    public ElementHandling getUrl() {
        return getHandling(PomProperty.URL);
    }

    /**
     * @param url the {@link #getUrl() URL} to set.
     */
    public void setUrl(ElementHandling url) {
        setHandling(PomProperty.URL, url);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getInceptionYear() inceptionYear}.
     */
    public ElementHandling getInceptionYear() {
        return getHandling(PomProperty.INCEPTION_YEAR);
    }

    /**
     * @param inceptionYear the inceptionYear to set
     */
    public void setInceptionYear(ElementHandling inceptionYear) {
        setHandling(PomProperty.INCEPTION_YEAR, inceptionYear);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getOrganization() organization}.
     */
    public ElementHandling getOrganization() {
        return getHandling(PomProperty.ORGANIZATION);
    }

    /**
     * @param organization the {@link #getOrganization() organization} to set.
     */
    public void setOrganization(ElementHandling organization) {
        setHandling(PomProperty.ORGANIZATION, organization);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getScm() SCM}.
     */
    public ElementHandling getScm() {
        return getHandling(PomProperty.SCM);
    }

    /**
     * @param scm the {@link #getScm() scm} to set.
     */
    public void setScm(ElementHandling scm) {
        setHandling(PomProperty.SCM, scm);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getPrerequisites() prerequisites}.
     */
    public ElementHandling getPrerequisites() {
        return getHandling(PomProperty.PREREQUISITES);
    }

    /**
     * @param prerequisites the {@link #getPrerequisites() prerequisites} to set.
     */
    public void setPrerequisites(ElementHandling prerequisites) {
        setHandling(PomProperty.PREREQUISITES, prerequisites);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getDevelopers() developers}.
     */
    public ElementHandling getDevelopers() {
        return getHandling(PomProperty.DEVELOPERS);
    }

    /**
     * @param developers the {@link #getDevelopers() developers} to set.
     */
    public void setDevelopers(ElementHandling developers) {
        setHandling(PomProperty.DEVELOPERS, developers);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getContributors() contributors}.
     */
    public ElementHandling getContributors() {
        return getHandling(PomProperty.CONTRIBUTORS);
    }

    /**
     * @param contributors the {@link #getContributors() contributors} to set.
     */
    public void setContributors(ElementHandling contributors) {
        setHandling(PomProperty.CONTRIBUTORS, contributors);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getMailingLists() mailingLists}.
     */
    public ElementHandling getMailingLists() {
        return getHandling(PomProperty.MAILING_LISTS);
    }

    /**
     * @param mailingLists the {@link #getMailingLists() mailingLists} to set.
     */
    public void setMailingLists(ElementHandling mailingLists) {
        setHandling(PomProperty.MAILING_LISTS, mailingLists);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getRepositories() repositories}.
     */
    public ElementHandling getRepositories() {
        return getHandling(PomProperty.REPOSITORIES);
    }

    /**
     * @param repositories the {@link #getRepositories() repositories} to set.
     */
    public void setRepositories(ElementHandling repositories) {
        setHandling(PomProperty.REPOSITORIES, repositories);
    }

    /**
     * @return {@link ElementHandling} for {@link Build#getPluginManagement()} pluginManagement}.
     */
    public ElementHandling getPluginManagement() {
        return getHandling(PomProperty.PLUGIN_MANAGEMENT);
    }

    /**
     * @param pluginManagement the {@link #getPluginManagement() pluginManagement} to set.
     */
    public void setPluginManagement(ElementHandling pluginManagement) {
        setHandling(PomProperty.PLUGIN_MANAGEMENT, pluginManagement);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getPluginRepositories() pluginRepositories}.
     */
    public ElementHandling getPluginRepositories() {
        return getHandling(PomProperty.PLUGIN_REPOSITORIES);
    }

    /**
     * @param pluginRepositories the {@link #getPluginRepositories() pluginRepositories} to set.
     */
    public void setPluginRepositories(ElementHandling pluginRepositories) {
        setHandling(PomProperty.PLUGIN_REPOSITORIES, pluginRepositories);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getIssueManagement() issueManagement}.
     */
    public ElementHandling getIssueManagement() {
        return getHandling(PomProperty.ISSUE_MANAGEMENT);
    }

    /**
     * @param issueManagement the {@link #getIssueManagement() issueManagement} to set.
     */
    public void setIssueManagement(ElementHandling issueManagement) {
        setHandling(PomProperty.ISSUE_MANAGEMENT, issueManagement);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getCiManagement() ciManagement}.
     */
    public ElementHandling getCiManagement() {
        return getHandling(PomProperty.CI_MANAGEMENT);
    }

    /**
     * @param ciManagement the {@link #getCiManagement() ciManagement} to set.
     */
    public void setCiManagement(ElementHandling ciManagement) {
        setHandling(PomProperty.CI_MANAGEMENT, ciManagement);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getDistributionManagement() distributionManagement}.
     */
    public ElementHandling getDistributionManagement() {
        return getHandling(PomProperty.DISTRIBUTION_MANAGEMENT);
    }

    /**
     * @param distributionManagement the {@link #getDistributionManagement() distributionManagement} to set.
     */
    public void setDistributionManagement(ElementHandling distributionManagement) {
        setHandling(PomProperty.DISTRIBUTION_MANAGEMENT, distributionManagement);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getDependencyManagement() dependencyManagement}.
     */
    public ElementHandling getDependencyManagement() {
        return getHandling(PomProperty.DEPENDENCY_MANAGEMENT);
    }

    /**
     * @param dependencyManagement the {@link #getDependencyManagement() dependencyManagement} to set.
     */
    public void setDependencyManagement(ElementHandling dependencyManagement) {
        setHandling(PomProperty.DEPENDENCY_MANAGEMENT, dependencyManagement);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getBuild() build}.
     */
    public ElementHandling getBuild() {
        return getHandling(PomProperty.BUILD);
    }

    /**
     * @param build the {@link #getBuild() build} to set.
     */
    public void setBuild(ElementHandling build) {
        setHandling(PomProperty.BUILD, build);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getParent() parent}.
     */
    public ElementHandling getParent() {
        return getHandling(PomProperty.PARENT);
    }

    /**
     * @param parent the {@link #getParent() parent} to set.
     */
    public void setParent(ElementHandling parent) {
        setHandling(PomProperty.PARENT, parent);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getModules() modules}.
     */
    public ElementHandling getModules() {
        return getHandling(PomProperty.MODULES);
    }

    /**
     * @param modules the {@link #getModules() modules} to set.
     */
    public void setModules(ElementHandling modules) {
        setHandling(PomProperty.MODULES, modules);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getProperties() properties}.
     */
    public ElementHandling getProperties() {
        return getHandling(PomProperty.PROPERTIES);
    }

    /**
     * @param properties the {@link #getProperties() properties} to set.
     */
    public void setProperties(ElementHandling properties) {
        setHandling(PomProperty.PROPERTIES, properties);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getReporting() reporting}.
     */
    public ElementHandling getReporting() {
        return getHandling(PomProperty.REPORTING);
    }

    /**
     * @param reporting the {@link #getReporting() reporting} to set.
     */
    public void setReporting(ElementHandling reporting) {
        setHandling(PomProperty.REPORTING, reporting);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getDependencies() dependencies}.
     */
    public ElementHandling getDependencies() {
        return getHandling(PomProperty.DEPENDENCIES);
    }

    /**
     * @param dependencies the {@link #getDependencies() dependencies} to set.
     */
    public void setDependencies(ElementHandling dependencies) {
        setHandling(PomProperty.DEPENDENCIES, dependencies);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getProfiles() profiles}.
     */
    public ElementHandling getProfiles() {
        return getHandling(PomProperty.PROFILES);
    }

    /**
     * @param profiles the {@link #getProfiles() profiles} to set.
     */
    public void setProfiles(ElementHandling profiles) {
        setHandling(PomProperty.PROFILES, profiles);
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getVersion() version}.
     */
    public ElementHandling getVersion() {
        return getHandling(PomProperty.VERSION);
    }

    /**
     * @param version the {@link #getVersion() version} to set.
     */
    public void setVersion(ElementHandling version) {
        setHandling(PomProperty.VERSION, version);
    }

    /**
     * Creates and returns a new {@link FlattenDescriptor} with the {@link ElementHandling}s merged from this and the
     * given {@link FlattenDescriptor}.
     *
     * @param descriptor is the {@link FlattenDescriptor} to merge with this one.
     * @return the merged {@link FlattenDescriptor}.
     */
    public FlattenDescriptor merge(FlattenDescriptor descriptor) {
        FlattenDescriptor result = new FlattenDescriptor();
        for (PomProperty<?> property : PomProperty.getPomProperties()) {
            String name = property.getName();
            ElementHandling handling = this.name2handlingMap.get(name);
            if (handling == null) {
                handling = descriptor.name2handlingMap.get(name);
            }
            if (handling != null) {
                result.name2handlingMap.put(name, handling);
            }
        }
        return result;
    }

    /**
     * @return <code>true</code> if none of the properties has been set explicitly, <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        for (ElementHandling handling : this.name2handlingMap.values()) {
            if (handling != null) {
                return false;
            }
        }
        return true;
    }
}
