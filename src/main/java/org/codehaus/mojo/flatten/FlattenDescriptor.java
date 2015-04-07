/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package org.codehaus.mojo.flatten;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * The descriptor that defines the additional POM elements that should be kept and copied to flattened POM.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0-beta-2
 */
public class FlattenDescriptor
{
    /** @see #getBuild() */
    public static final String BUILD = "build";

    /** @see #getCiManagement() */
    public static final String CI_MANAGEMENT = "ciManagement";

    /** @see #getContributors() */
    public static final String CONTRIBUTORS = "contributors";

    /** @see #getDependencies() */
    public static final String DEPENDENCIES = "dependencies";

    /** @see #getDependencyManagement() */
    public static final String DEPENDENCY_MANAGEMENT = "dependencyManagement";

    /** @see #getDescription() */
    public static final String DESCRIPTION = "description";

    /** @see #getDevelopers() */
    public static final String DEVELOPERS = "developers";

    /** @see #getDistributionManagement() */
    public static final String DISTRIBUTION_MANAGEMENT = "distributionManagement";

    /** @see #getInceptionYear() */
    public static final String INCEPTION_YEAR = "inceptionYear";

    /** @see #getIssueManagement() */
    public static final String ISSUE_MANAGEMENT = "issueManagement";

    /** @see #getMailingLists() */
    public static final String MAILING_LISTS = "mailingLists";

    /** @see #getModules() */
    public static final String MODULES = "modules";

    /** @see #getName() */
    public static final String NAME = "name";

    /** @see #getOrganization() */
    public static final String ORGANIZATION = "organization";

    /** @see #getParent() */
    public static final String PARENT = "parent";

    /** @see #getPluginRepositories() */
    public static final String PLUGIN_REPOSITORIES = "pluginRepositories";

    /** @see #getPrerequisites() */
    public static final String PREREQUISITES = "prerequisites";

    /** @see #getProfiles() */
    public static final String PROFILES = "profiles";

    /** @see #getProperties() */
    public static final String PROPERTIES = "properties";

    /** @see #getReporting() */
    public static final String REPORTING = "reporting";

    /** @see #getRepositories() */
    public static final String REPOSITORIES = "repositories";

    /** @see #getScm() */
    public static final String SCM = "scm";

    /** @see #getUrl() */
    public static final String URL = "url";

    private static final String[] ELEMENET_NAMES = new String[] { BUILD, CI_MANAGEMENT, CONTRIBUTORS, DEPENDENCIES,
        DEPENDENCY_MANAGEMENT, DESCRIPTION, DEVELOPERS, DISTRIBUTION_MANAGEMENT, INCEPTION_YEAR, ISSUE_MANAGEMENT,
        MAILING_LISTS, MODULES, NAME, ORGANIZATION, PARENT, PLUGIN_REPOSITORIES, PREREQUISITES, PROFILES, PROPERTIES,
        REPORTING, REPOSITORIES, SCM, URL };

    private final Map<String, ElementHandling> name2handlingMap;

    /**
     * The constructor.
     */
    public FlattenDescriptor()
    {
        super();
        this.name2handlingMap = new HashMap<String, ElementHandling>();
    }

    /**
     * The constructor.
     *
     * @param descriptor is the raw {@link Xpp3Dom} from the MOJOs configuration.
     */
    public FlattenDescriptor( Xpp3Dom descriptor )
    {
        this();
        for ( String element : ELEMENET_NAMES )
        {
            if ( descriptor.getChild( element ) != null )
            {
                this.name2handlingMap.put( element, ElementHandling.effective );
            }
        }
    }

    /**
     * Generic method to get a {@link ElementHandling}.
     *
     * @param element is the name of a POM element. Please use constants defined in this class.
     * @return the {@link ElementHandling}. Will be {@link ElementHandling#remove} as fallback if undefined.
     */
    public ElementHandling getHandling( String element )
    {
        ElementHandling handling = this.name2handlingMap.get( element );
        if ( handling == null )
        {
            handling = ElementHandling.remove;
        }
        return handling;
    }

    /**
     * Generic method to set an {@link ElementHandling}.
     *
     * @param element is the name of a POM element. Please use constants defined in this class.
     * @param handling the new {@link ElementHandling}.
     */
    public void setHandling( String element, ElementHandling handling )
    {

        this.name2handlingMap.put( element, handling );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getName() name}.
     */
    public ElementHandling getName()
    {
        return getHandling( NAME );
    }

    /**
     * @param name the {@link #getName() name} to set.
     */
    public void setName( ElementHandling name )
    {
        setHandling( NAME, name );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getDescription() description}.
     */
    public ElementHandling getDescription()
    {
        return getHandling( DESCRIPTION );
    }

    /**
     * @param description the {@link #getDescription() description} to set.
     */
    public void setDescription( ElementHandling description )
    {
        setHandling( DESCRIPTION, description );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getUrl() URL}.
     */
    public ElementHandling getUrl()
    {
        return getHandling( URL );
    }

    /**
     * @param url the {@link #getUrl() URL} to set.
     */
    public void setUrl( ElementHandling url )
    {
        setHandling( URL, url );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getInceptionYear() inceptionYear}.
     */
    public ElementHandling getInceptionYear()
    {
        return getHandling( INCEPTION_YEAR );
    }

    /**
     * @param inceptionYear the inceptionYear to set
     */
    public void setInceptionYear( ElementHandling inceptionYear )
    {
        setHandling( INCEPTION_YEAR, inceptionYear );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getOrganization() organization}.
     */
    public ElementHandling getOrganization()
    {
        return getHandling( ORGANIZATION );
    }

    /**
     * @param organization the {@link #getOrganization() organization} to set.
     */
    public void setOrganization( ElementHandling organization )
    {
        setHandling( ORGANIZATION, organization );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getScm() SCM}.
     */
    public ElementHandling getScm()
    {
        return getHandling( SCM );
    }

    /**
     * @param scm the {@link #getScm() scm} to set.
     */
    public void setScm( ElementHandling scm )
    {
        setHandling( SCM, scm );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getPrerequisites() prerequisites}.
     */
    public ElementHandling getPrerequisites()
    {
        return getHandling( PREREQUISITES );
    }

    /**
     * @param prerequisites the {@link #getPrerequisites() prerequisites} to set.
     */
    public void setPrerequisites( ElementHandling prerequisites )
    {
        setHandling( PREREQUISITES, prerequisites );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getDevelopers() developers}.
     */
    public ElementHandling getDevelopers()
    {
        return getHandling( DEVELOPERS );
    }

    /**
     * @param developers the {@link #getDevelopers() developers} to set.
     */
    public void setDevelopers( ElementHandling developers )
    {
        setHandling( DEVELOPERS, developers );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getContributors() contributors}.
     */
    public ElementHandling getContributors()
    {
        return getHandling( CONTRIBUTORS );
    }

    /**
     * @param contributors the {@link #getContributors() contributors} to set.
     */
    public void setContributors( ElementHandling contributors )
    {
        setHandling( CONTRIBUTORS, contributors );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getMailingLists() mailingLists}.
     */
    public ElementHandling getMailingLists()
    {
        return getHandling( MAILING_LISTS );
    }

    /**
     * @param mailingLists the {@link #getMailingLists() mailingLists} to set.
     */
    public void setMailingLists( ElementHandling mailingLists )
    {
        setHandling( MAILING_LISTS, mailingLists );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getRepositories() repositories}.
     */
    public ElementHandling getRepositories()
    {
        return getHandling( REPOSITORIES );
    }

    /**
     * @param repositories the {@link #getRepositories() repositories} to set.
     */
    public void setRepositories( ElementHandling repositories )
    {
        setHandling( REPOSITORIES, repositories );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getPluginRepositories() pluginRepositories}.
     */
    public ElementHandling getPluginRepositories()
    {
        return getHandling( PLUGIN_REPOSITORIES );
    }

    /**
     * @param pluginRepositories the {@link #getPluginRepositories() pluginRepositories} to set.
     */
    public void setPluginRepositories( ElementHandling pluginRepositories )
    {
        setHandling( PLUGIN_REPOSITORIES, pluginRepositories );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getIssueManagement() issueManagement}.
     */
    public ElementHandling getIssueManagement()
    {
        return getHandling( ISSUE_MANAGEMENT );
    }

    /**
     * @param issueManagement the {@link #getIssueManagement() issueManagement} to set.
     */
    public void setIssueManagement( ElementHandling issueManagement )
    {
        setHandling( ISSUE_MANAGEMENT, issueManagement );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getCiManagement() ciManagement}.
     */
    public ElementHandling getCiManagement()
    {
        return getHandling( CI_MANAGEMENT );
    }

    /**
     * @param ciManagement the {@link #getCiManagement() ciManagement} to set.
     */
    public void setCiManagement( ElementHandling ciManagement )
    {
        setHandling( CI_MANAGEMENT, ciManagement );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getDistributionManagement() distributionManagement}.
     */
    public ElementHandling getDistributionManagement()
    {
        return getHandling( DISTRIBUTION_MANAGEMENT );
    }

    /**
     * @param distributionManagement the {@link #getDistributionManagement() distributionManagement} to set.
     */
    public void setDistributionManagement( ElementHandling distributionManagement )
    {
        setHandling( DISTRIBUTION_MANAGEMENT, distributionManagement );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getDependencyManagement() dependencyManagement}.
     */
    public ElementHandling getDependencyManagement()
    {
        return getHandling( DEPENDENCY_MANAGEMENT );
    }

    /**
     * @param dependencyManagement the {@link #getDependencyManagement() dependencyManagement} to set.
     */
    public void setDependencyManagement( ElementHandling dependencyManagement )
    {
        setHandling( DEPENDENCY_MANAGEMENT, dependencyManagement );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getBuild() build}.
     */
    public ElementHandling getBuild()
    {
        return getHandling( BUILD );
    }

    /**
     * @param build the {@link #getBuild() build} to set.
     */
    public void setBuild( ElementHandling build )
    {
        setHandling( BUILD, build );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getParent() parent}.
     */
    public ElementHandling getParent()
    {
        return getHandling( PARENT );
    }

    /**
     * @param parent the {@link #getParent() parent} to set.
     */
    public void setParent( ElementHandling parent )
    {
        setHandling( PARENT, parent );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getModules() modules}.
     */
    public ElementHandling getModules()
    {
        return getHandling( MODULES );
    }

    /**
     * @param modules the {@link #getModules() modules} to set.
     */
    public void setModules( ElementHandling modules )
    {
        setHandling( MODULES, modules );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getProperties() properties}.
     */
    public ElementHandling getProperties()
    {
        return getHandling( PROPERTIES );
    }

    /**
     * @param properties the {@link #getProperties() properties} to set.
     */
    public void setProperties( ElementHandling properties )
    {
        setHandling( PROPERTIES, properties );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getReporting() reporting}.
     */
    public ElementHandling getReporting()
    {
        return getHandling( REPORTING );
    }

    /**
     * @param reporting the {@link #getReporting() reporting} to set.
     */
    public void setReporting( ElementHandling reporting )
    {
        setHandling( REPORTING, reporting );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getDependencies() dependencies}.
     */
    public ElementHandling getDependencies()
    {
        return getHandling( DEPENDENCIES );
    }

    /**
     * @param dependencies the {@link #getDependencies() dependencies} to set.
     */
    public void setDependencies( ElementHandling dependencies )
    {
        setHandling( DEPENDENCIES, dependencies );
    }

    /**
     * @return {@link ElementHandling} for {@link Model#getProfiles() profiles}.
     */
    public ElementHandling getProfiles()
    {
        return getHandling( PROFILES );
    }

    /**
     * @param profiles the {@link #getProfiles() profiles} to set.
     */
    public void setProfiles( ElementHandling profiles )
    {
        setHandling( PROFILES, profiles );
    }

    /**
     * Creates and returns a new {@link FlattenDescriptor} with the {@link ElementHandling}s merged from this and the
     * given {@link FlattenDescriptor}.
     *
     * @param descriptor is the {@link FlattenDescriptor} to merge with this one.
     * @return the merged {@link FlattenDescriptor}.
     */
    public FlattenDescriptor merge( FlattenDescriptor descriptor )
    {
        FlattenDescriptor result = new FlattenDescriptor();
        for ( String name : ELEMENET_NAMES )
        {
            ElementHandling handling = this.name2handlingMap.get( name );
            if ( handling == null )
            {
                handling = descriptor.name2handlingMap.get( name );
            }
            result.name2handlingMap.put( name, handling );
        }
        return result;
    }

    /**
     * @return <code>true</code> if none of the properties has been set explicitly, <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        for ( ElementHandling handling : this.name2handlingMap.values() )
        {
            if ( handling != null )
            {
                return false;
            }
        }
        return true;
    }

}
