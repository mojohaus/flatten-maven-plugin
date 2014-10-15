/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package org.codehaus.mojo.flatten;

import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * The descriptor that defines the additional POM elements that should be kept and copied to flattened POM.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0-beta-2
 */
public class FlattenDescriptor
{

    /** @see #isKeepName() */
    private String name;

    /** @see #isKeepDescription() */
    private String description;

    /** @see #isKeepUrl() */
    private String url;

    /** @see #isKeepInceptionYear() */
    private String inceptionYear;

    /** @see #isKeepOrganization() */
    private String organization;

    /** @see #isKeepScm() */
    private String scm;

    /** @see #isKeepPrerequisites() */
    private String prerequisites;

    /** @see #isKeepDevelopers() */
    private String developers;

    /** @see #isKeepContributors() */
    private String contributors;

    /** @see #isKeepMailingLists() */
    private String mailingLists;

    /** @see #isKeepRepositories() */
    private String repositories;

    /** @see #isKeepPluginRepositories() */
    private String pluginRepositories;

    /** @see #isKeepIssueManagement() */
    private String issueManagement;

    /** @see #isKeepCiManagement() */
    private String ciManagement;

    /** @see #isKeepDistributionManagement() */
    private String distributionManagement;

    /** @see #isKeepDependencyManagement() */
    private String dependencyManagement;

    /** @see #isKeepBuild() */
    private String build;

    /** @see #isKeepParent() */
    private String parent;

    /** @see #isKeepModules() */
    private String modules;

    /** @see #isKeepProperties() */
    private String properties;

    /** @see #isKeepReporting() */
    private String reporting;

    /**
     * The constructor.
     */
    public FlattenDescriptor()
    {

        super();
        // keep repositories by default as suggested by Robert...
        setKeepRepositories();
    }

    /**
     * The constructor.
     *
     * @param descriptor is the raw {@link Xpp3Dom} from the MOJOs configuration.
     */
    public FlattenDescriptor( Xpp3Dom descriptor )
    {
        super();
        if ( descriptor.getChild( "ciManagement" ) != null )
        {
            this.ciManagement = "";
        }
        if ( descriptor.getChild( "contributors" ) != null )
        {
            this.contributors = "";
        }
        if ( descriptor.getChild( "description" ) != null )
        {
            this.description = "";
        }
        if ( descriptor.getChild( "developers" ) != null )
        {
            this.developers = "";
        }
        if ( descriptor.getChild( "distributionManagement" ) != null )
        {
            this.distributionManagement = "";
        }
        if ( descriptor.getChild( "inceptionYear" ) != null )
        {
            this.inceptionYear = "";
        }
        if ( descriptor.getChild( "issueManagement" ) != null )
        {
            this.issueManagement = "";
        }
        if ( descriptor.getChild( "mailingLists" ) != null )
        {
            this.mailingLists = "";
        }
        if ( descriptor.getChild( "name" ) != null )
        {
            this.name = "";
        }
        if ( descriptor.getChild( "organization" ) != null )
        {
            this.organization = "";
        }
        if ( descriptor.getChild( "pluginRepositories" ) != null )
        {
            this.pluginRepositories = "";
        }
        if ( descriptor.getChild( "prerequisites" ) != null )
        {
            this.prerequisites = "";
        }
        if ( descriptor.getChild( "repositories" ) != null )
        {
            this.repositories = "";
        }
        if ( descriptor.getChild( "scm" ) != null )
        {
            this.scm = "";
        }
        if ( descriptor.getChild( "url" ) != null )
        {
            this.url = "";
        }
        if ( descriptor.getChild( "dependencyManagement" ) != null )
        {
            this.dependencyManagement = "";
        }
        if ( descriptor.getChild( "build" ) != null )
        {
            this.build = "";
        }
        if ( descriptor.getChild( "parent" ) != null )
        {
            this.parent = "";
        }
        if ( descriptor.getChild( "modules" ) != null )
        {
            this.modules = "";
        }
        if ( descriptor.getChild( "properties" ) != null )
        {
            this.properties = "";
        }
        if ( descriptor.getChild( "reporting" ) != null )
        {
            this.reporting = "";
        }
    }

    /**
     * @return <code>true</code> if we should keep name
     */
    public boolean isKeepName()
    {
        return this.name != null;
    }

    /**
     * @return <code>true</code> if we should keep description
     */
    public boolean isKeepDescription()
    {
        return this.description != null;
    }

    /**
     * @return <code>true</code> if we should keep url
     */
    public boolean isKeepUrl()
    {
        return this.url != null;
    }

    /**
     * @return <code>true</code> if we should keep inceptionYear
     */
    public boolean isKeepInceptionYear()
    {
        return this.inceptionYear != null;
    }

    /**
     * @return <code>true</code> if we should keep organization
     */
    public boolean isKeepOrganization()
    {
        return this.organization != null;
    }

    /**
     * @return <code>true</code> if we should keep scm
     */
    public boolean isKeepScm()
    {
        return this.scm != null;
    }

    /**
     * @return <code>true</code> if we should keep prerequisites
     */
    public boolean isKeepPrerequisites()
    {
        return this.prerequisites != null;
    }

    /**
     * @return <code>true</code> if we should keep developers
     */
    public boolean isKeepDevelopers()
    {
        return this.developers != null;
    }

    /**
     * @return <code>true</code> if we should keep contributors
     */
    public boolean isKeepContributors()
    {
        return this.contributors != null;
    }

    /**
     * @return <code>true</code> if we should keep mailingLists
     */
    public boolean isKeepMailingLists()
    {
        return this.mailingLists != null;
    }

    /**
     * @return <code>true</code> if we should keep repositories
     */
    public boolean isKeepRepositories()
    {
        return this.repositories != null;
    }

    /**
     * @return <code>true</code> if we should keep pluginRepositories
     */
    public boolean isKeepPluginRepositories()
    {
        return this.pluginRepositories != null;
    }

    /**
     * @return <code>true</code> if we should keep issueManagement
     */
    public boolean isKeepIssueManagement()
    {
        return this.issueManagement != null;
    }

    /**
     * @return <code>true</code> if we should keep ciManagement
     */
    public boolean isKeepCiManagement()
    {
        return this.ciManagement != null;
    }

    /**
     * @return <code>true</code> if we should keep distributionManagement
     */
    public boolean isKeepDistributionManagement()
    {
        return this.distributionManagement != null;
    }

    /**
     * @return <code>true</code> if we should keep dependencyManagement
     */
    public boolean isKeepDependencyManagement()
    {
        return this.dependencyManagement != null;
    }

    /**
     * @return <code>true</code> if we should keep build
     */
    public boolean isKeepBuild()
    {
        return this.build != null;
    }

    /**
     * @return <code>true</code> if we should keep parent
     */
    public boolean isKeepParent()
    {
        return this.parent != null;
    }

    /**
     * @return <code>true</code> if we should keep modules
     */
    public boolean isKeepModules()
    {
        return this.modules != null;
    }

    /**
     * @return <code>true</code> if we should keep properties
     */
    public boolean isKeepProperties()
    {
        return this.properties != null;
    }

    /**
     * @return <code>true</code> if we should keep reporting
     */
    public boolean isKeepReporting()
    {
        return this.reporting != null;
    }

    /**
     * Sets {@link #isKeepName()}.
     */
    public void setKeepName()
    {
        this.name = "";
    }

    /**
     * Sets {@link #isKeepDescription()}.
     */
    public void setKeepDescription()
    {
        this.description = "";
    }

    /**
     * Sets {@link #isKeepUrl()}.
     */
    public void setKeepUrl()
    {
        this.url = "";
    }

    /**
     * Sets {@link #isKeepInceptionYear()}.
     */
    public void setKeepInceptionYear()
    {
        this.inceptionYear = "";
    }

    /**
     * Sets {@link #isKeepOrganization()}.
     */
    public void setKeepOrganization()
    {
        this.organization = "";
    }

    /**
     * Sets {@link #isKeepScm()}.
     */
    public void setKeepScm()
    {
        this.scm = "";
    }

    /**
     * Sets {@link #isKeepPrerequisites()}.
     */
    public void setKeepPrerequisites()
    {
        this.prerequisites = "";
    }

    /**
     * Sets {@link #isKeepDevelopers()}.
     */
    public void setKeepDevelopers()
    {
        this.developers = "";
    }

    /**
     * Sets {@link #isKeepContributors()}.
     */
    public void setKeepContributors()
    {
        this.contributors = "";
    }

    /**
     * Sets {@link #isKeepMailingLists()}.
     */
    public void setKeepMailingLists()
    {
        this.mailingLists = "";
    }

    /**
     * Sets {@link #isKeepRepositories()}.
     */
    public void setKeepRepositories()
    {
        this.repositories = "";
    }

    /**
     * Sets {@link #isKeepPluginRepositories()}.
     */
    public void setKeepPluginRepositories()
    {
        this.pluginRepositories = "";
    }

    /**
     * Sets {@link #isKeepIssueManagement()}.
     */
    public void setKeepIssueManagement()
    {
        this.issueManagement = "";
    }

    /**
     * Sets {@link #isKeepCiManagement()}.
     */
    public void setKeepCiManagement()
    {
        this.ciManagement = "";
    }

    /**
     * Sets {@link #isKeepDistributionManagement()}.
     */
    public void setKeepDistributionManagement()
    {
        this.distributionManagement = "";
    }

    /**
     * Sets {@link #isKeepDependencyManagement()}.
     */
    public void setKeepDependencyManagement()
    {
        this.dependencyManagement = "";
    }

    /**
     * Sets {@link #isKeepBuild()}.
     */
    public void setKeepBuild()
    {
        this.build = "";
    }

    /**
     * Sets {@link #isKeepParent()}.
     */
    public void setKeepParent()
    {
        this.parent = "";
    }

    /**
     * Sets {@link #isKeepModules()}.
     */
    public void setKeepModules()
    {
        this.modules = "";
    }

    /**
     * Sets {@link #isKeepProperties()}.
     */
    public void setKeepProperties()
    {
        this.properties = "";
    }

    /**
     * Sets {@link #isKeepReporting()}.
     */
    public void setKeepReporting()
    {
        this.reporting = "";
    }

}
