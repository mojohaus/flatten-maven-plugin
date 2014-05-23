/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package org.codehaus.mojo.flatten;

import org.apache.maven.model.Model;

/**
 * The descriptor that defines how to {@link ElementHandling handle} additional POM elements.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0-beta-2
 */
public class FlattenDescriptor
{

    /** @see #getName() */
    private ElementHandling name;

    /** @see #getDescription() */
    private ElementHandling description;

    /** @see #getScm() */
    private ElementHandling scm;

    /** @see #getPrerequisites() */
    private ElementHandling prerequisites;

    /** @see #getDevelopers() */
    private ElementHandling developers;

    /** @see #getContributors() */
    private ElementHandling contributors;

    /** @see #getMailingLists() */
    private ElementHandling mailingLists;

    /** @see #getRepositories() */
    private ElementHandling repositories;

    /** @see #getPluginRepositories() */
    private ElementHandling pluginRepositories;

    /** @see #getUrl() */
    private ElementHandling url;

    /** @see #getInceptionYear() */
    private ElementHandling inceptionYear;

    /** @see #getIssueManagement() */
    private ElementHandling issueManagement;

    /** @see #getCiManagement() */
    private ElementHandling ciManagement;

    /** @see #getDistributionManagement() */
    private ElementHandling distributionManagement;

    /**
     * The constructor.
     */
    public FlattenDescriptor()
    {

        super();
        this.name = ElementHandling.Remove;
        this.description = ElementHandling.Remove;
        this.scm = ElementHandling.Remove;
        this.prerequisites = null; // ATTENTION: Default is NOT Remove as kept by default if packaging is maven-plugin
        this.developers = ElementHandling.Remove;
        this.contributors = ElementHandling.Remove;
        this.mailingLists = ElementHandling.Remove;
        this.url = ElementHandling.Remove;
        this.inceptionYear = ElementHandling.Remove;
        this.issueManagement = ElementHandling.Remove;
        this.ciManagement = ElementHandling.Remove;
    }

    /**
     * @return the {@link ElementHandling} for the {@link Model#getName() &lt;name>} element of the flattened POM.
     */
    public ElementHandling getName()
    {
        return this.name;
    }

    /**
     * @param name is the new value of {@link #getName()}.
     */
    public void setName( ElementHandling name )
    {
        this.name = name;
    }

    /**
     * @return the {@link ElementHandling} for the {@link Model#getDescription() &lt;description>} element of the
     *         flattened POM.
     */
    public ElementHandling getDescription()
    {
        return this.description;
    }

    /**
     * @param description is the new value of {@link #getDescription()}.
     */
    public void setDescription( ElementHandling description )
    {
        this.description = description;
    }

    /**
     * @return the {@link ElementHandling} for the {@link Model#getScm() &lt;scm>} element of the flattened POM.
     */
    public ElementHandling getScm()
    {
        return this.scm;
    }

    /**
     * @param scm is the new value of {@link #getScm()}.
     */
    public void setScm( ElementHandling scm )
    {
        this.scm = scm;
    }

    /**
     * @return the {@link ElementHandling} for the {@link Model#getPrerequisites() &lt;prerequisites>} element of the
     *         flattened POM.
     */
    public ElementHandling getPrerequisites()
    {
        return this.prerequisites;
    }

    /**
     * @param prerequisites is the new value of {@link #getPrerequisites()}.
     */
    public void setPrerequisites( ElementHandling prerequisites )
    {
        this.prerequisites = prerequisites;
    }

    /**
     * @return the {@link ElementHandling} for the {@link Model#getDevelopers() &lt;developers>} element of the
     *         flattened POM.
     */
    public ElementHandling getDevelopers()
    {
        return this.developers;
    }

    /**
     * @param developers is the new value of {@link #getDevelopers()}.
     */
    public void setDevelopers( ElementHandling developers )
    {
        this.developers = developers;
    }

    /**
     * @return the {@link ElementHandling} for the {@link Model#getContributors() &lt;contributors>} element of the
     *         flattened POM.
     */
    public ElementHandling getContributors()
    {
        return this.contributors;
    }

    /**
     * @param contributors is the new value of {@link #getContributors()}.
     */
    public void setContributors( ElementHandling contributors )
    {
        this.contributors = contributors;
    }

    /**
     * @return the {@link ElementHandling} for the {@link Model#getMailingLists() &lt;mailingLists>} element of the
     *         flattened POM.
     */
    public ElementHandling getMailingLists()
    {
        return this.mailingLists;
    }

    /**
     * @param mailingLists is the new value of {@link #getMailingLists()}.
     */
    public void setMailingLists( ElementHandling mailingLists )
    {
        this.mailingLists = mailingLists;
    }

    /**
     * @return the {@link ElementHandling} for the {@link Model#getRepositories() &lt;repositories>} element of the
     *         flattened POM.
     */
    public ElementHandling getRepositories()
    {
        return this.repositories;
    }

    /**
     * @param repositories is the new value of {@link #getRepositories()}.
     */
    public void setRepositories( ElementHandling repositories )
    {
        this.repositories = repositories;
    }

    /**
     * @return the {@link ElementHandling} for the {@link Model#getPluginRepositories() &lt;pluginRepositories>} element
     *         of the flattened POM.
     */
    public ElementHandling getPluginRepositories()
    {
        return this.pluginRepositories;
    }

    /**
     * @param pluginRepositories is the new value of {@link #getPluginRepositories()}.
     */
    public void setPluginRepositories( ElementHandling pluginRepositories )
    {
        this.pluginRepositories = pluginRepositories;
    }

    /**
     * @return the {@link ElementHandling} for the {@link Model#getUrl() &lt;url>} element of the flattened POM.
     */
    public ElementHandling getUrl()
    {
        return this.url;
    }

    /**
     * @param url is the new value of {@link #getUrl()}.
     */
    public void setUrl( ElementHandling url )
    {
        this.url = url;
    }

    /**
     * @return the {@link ElementHandling} for the {@link Model#getInceptionYear() &lt;inceptionYear>} element of the
     *         flattened POM.
     */
    public ElementHandling getInceptionYear()
    {
        return this.inceptionYear;
    }

    /**
     * @param inceptionYear is the new value of {@link #getInceptionYear()}.
     */
    public void setInceptionYear( ElementHandling inceptionYear )
    {
        this.inceptionYear = inceptionYear;
    }

    /**
     * @return the {@link ElementHandling} for the {@link Model#getIssueManagement() &lt;issueManagement>} element of
     *         the flattened POM.
     */
    public ElementHandling getIssueManagement()
    {
        return this.issueManagement;
    }

    /**
     * @param issueManagement is the new value of {@link #getIssueManagement()}.
     */
    public void setIssueManagement( ElementHandling issueManagement )
    {
        this.issueManagement = issueManagement;
    }

    /**
     * @return the {@link ElementHandling} for the {@link Model#getCiManagement() &lt;ciManagement>} element of the
     *         flattened POM.
     */
    public ElementHandling getCiManagement()
    {
        return this.ciManagement;
    }

    /**
     * @param ciManagement is the new value of {@link #getIssueManagement()}.
     */
    public void setCiManagement( ElementHandling ciManagement )
    {
        this.ciManagement = ciManagement;
    }

    /**
     * @return the {@link ElementHandling} for the {@link Model#getDistributionManagement() &lt;distributionManagement>}
     *         element of the flattened POM.
     */
    public ElementHandling getDistributionManagement()
    {
        return this.distributionManagement;
    }

    /**
     * @param distributionManagement is the new value of {@link #getDistributionManagement()}.
     */
    public void setDistributionManagement( ElementHandling distributionManagement )
    {
        this.distributionManagement = distributionManagement;
    }

}
