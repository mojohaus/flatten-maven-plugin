/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package org.codehaus.mojo.flatten;

import java.util.List;

import org.apache.maven.model.Contributor;
import org.apache.maven.model.Developer;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * This enum contains the optional POM elements. By default {@link FlattenMojo} will {@link ElementHandling#Remove
 * remove} them. However, via {@link FlattenDescriptor} other {@link ElementHandling}s can be configured.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0-beta-2
 */
public enum OptionalPomElement
{

    /** @see Model#getName() */
    Name
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementHandling getHandling( FlattenDescriptor descriptor )
        {
            return descriptor.getName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isAvailable( Model model )
        {
            return !StringUtils.isEmpty( model.getName() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copy( Model source, Model destination )
        {
            String name = source.getName();
            if ( StringUtils.isEmpty( name ) )
            {
                // name defaults to artifactId if not set...
                name = source.getArtifactId();
            }
            destination.setName( name );
        }

    },

    /** @see Model#getDescription() */
    Description
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementHandling getHandling( FlattenDescriptor descriptor )
        {
            return descriptor.getDescription();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isAvailable( Model model )
        {
            return !StringUtils.isEmpty( model.getDescription() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copy( Model source, Model destination )
        {
            destination.setDescription( source.getDescription() );
        }
    },

    /** @see Model#getUrl() */
    Url
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementHandling getHandling( FlattenDescriptor descriptor )
        {
            return descriptor.getUrl();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isAvailable( Model model )
        {
            return !StringUtils.isEmpty( model.getUrl() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copy( Model source, Model destination )
        {
            destination.setUrl( source.getUrl() );
        }
    },

    /** @see Model#getScm() */
    Scm
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementHandling getHandling( FlattenDescriptor descriptor )
        {
            return descriptor.getScm();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isAvailable( Model model )
        {
            org.apache.maven.model.Scm scm = model.getScm();
            if ( scm == null )
            {
                return false;
            }
            if ( !StringUtils.isEmpty( scm.getUrl() ) )
            {
                return true;
            }
            if ( !StringUtils.isEmpty( scm.getConnection() ) )
            {
                return true;
            }
            if ( !StringUtils.isEmpty( scm.getDeveloperConnection() ) )
            {
                return true;
            }
            if ( !StringUtils.isEmpty( scm.getTag() ) )
            {
                return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copy( Model source, Model destination )
        {
            destination.setScm( source.getScm() );
        }
    },

    /** @see Model#getPrerequisites() */
    Prerequisites
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementHandling getHandling( FlattenDescriptor descriptor )
        {
            return descriptor.getPrerequisites();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isAvailable( Model model )
        {
            org.apache.maven.model.Prerequisites prerequisites = model.getPrerequisites();
            if ( prerequisites == null )
            {
                return false;
            }
            if ( !StringUtils.isEmpty( prerequisites.getMaven() ) )
            {
                return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copy( Model source, Model destination )
        {
            destination.setPrerequisites( source.getPrerequisites() );
        }
    },

    /** @see Model#getDevelopers() */
    Developers
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementHandling getHandling( FlattenDescriptor descriptor )
        {
            return descriptor.getDevelopers();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isAvailable( Model model )
        {
            List<Developer> developers = model.getDevelopers();
            if ( ( developers == null ) || ( developers.isEmpty() ) )
            {
                return false;
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copy( Model source, Model destination )
        {
            destination.setDevelopers( source.getDevelopers() );
        }
    },

    /** @see Model#getContributors() */
    Contributors
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementHandling getHandling( FlattenDescriptor descriptor )
        {
            return descriptor.getContributors();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isAvailable( Model model )
        {
            List<Contributor> contributors = model.getContributors();
            if ( ( contributors == null ) || ( contributors.isEmpty() ) )
            {
                return false;
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copy( Model source, Model destination )
        {
            destination.setContributors( source.getContributors() );
        }
    },

    /** @see Model#getMailingLists() */
    MailingLists
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementHandling getHandling( FlattenDescriptor descriptor )
        {
            return descriptor.getMailingLists();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isAvailable( Model model )
        {
            List<MailingList> mailingLists = model.getMailingLists();
            if ( ( mailingLists == null ) || ( mailingLists.isEmpty() ) )
            {
                return false;
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copy( Model source, Model destination )
        {
            destination.setMailingLists( source.getMailingLists() );
        }
    },

    /** @see Model#getRepositories() */
    Repositories
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementHandling getHandling( FlattenDescriptor descriptor )
        {
            return descriptor.getRepositories();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isAvailable( Model model )
        {
            List<Repository> repositories = model.getRepositories();
            if ( ( repositories == null ) || ( repositories.isEmpty() ) )
            {
                return false;
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copy( Model source, Model destination )
        {
            destination.setRepositories( source.getRepositories() );
        }
    },

    /** @see Model#getPluginRepositories() */
    PluginRepositories
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementHandling getHandling( FlattenDescriptor descriptor )
        {
            return descriptor.getPluginRepositories();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isAvailable( Model model )
        {
            List<Repository> repositories = model.getPluginRepositories();
            if ( ( repositories == null ) || ( repositories.isEmpty() ) )
            {
                return false;
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copy( Model source, Model destination )
        {
            destination.setPluginRepositories( source.getPluginRepositories() );
        }
    },

    /** @see Model#getIssueManagement() */
    IssueManagement
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementHandling getHandling( FlattenDescriptor descriptor )
        {
            return descriptor.getIssueManagement();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isAvailable( Model model )
        {
            org.apache.maven.model.IssueManagement issueManagement = model.getIssueManagement();
            if ( issueManagement == null )
            {
                return false;
            }
            if ( !StringUtils.isEmpty( issueManagement.getUrl() ) )
            {
                return true;
            }
            if ( !StringUtils.isEmpty( issueManagement.getSystem() ) )
            {
                return true;
            }
            // ...
            // return false;
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copy( Model source, Model destination )
        {
            destination.setIssueManagement( source.getIssueManagement() );
        }
    },

    /** @see Model#getCiManagement() */
    CiManagement
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementHandling getHandling( FlattenDescriptor descriptor )
        {
            return descriptor.getCiManagement();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isAvailable( Model model )
        {
            org.apache.maven.model.CiManagement ciManagement = model.getCiManagement();
            if ( ciManagement == null )
            {
                return false;
            }
            if ( !StringUtils.isEmpty( ciManagement.getUrl() ) )
            {
                return true;
            }
            if ( !StringUtils.isEmpty( ciManagement.getSystem() ) )
            {
                return true;
            }
            // ...
            // return false;
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copy( Model source, Model destination )
        {
            destination.setCiManagement( source.getCiManagement() );
        }
    },

    /** @see Model#getDistributionManagement() */
    DistributionManagement
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementHandling getHandling( FlattenDescriptor descriptor )
        {
            return descriptor.getDistributionManagement();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isAvailable( Model model )
        {
            org.apache.maven.model.DistributionManagement distributionManagement = model.getDistributionManagement();
            if ( distributionManagement == null )
            {
                return false;
            }
            if ( !StringUtils.isEmpty( distributionManagement.getDownloadUrl() ) )
            {
                return true;
            }
            if ( !StringUtils.isEmpty( distributionManagement.getStatus() ) )
            {
                return true;
            }
            if ( distributionManagement.getRepository() != null )
            {
                return true;
            }
            if ( distributionManagement.getSite() != null )
            {
                return true;
            }
            if ( distributionManagement.getSnapshotRepository() != null )
            {
                return true;
            }
            if ( distributionManagement.getRelocation() != null )
            {
                return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copy( Model source, Model destination )
        {
            destination.setCiManagement( source.getCiManagement() );
        }
    },

    /** @see Model#getInceptionYear() */
    InceptionYear
    {

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementHandling getHandling( FlattenDescriptor descriptor )
        {
            return descriptor.getInceptionYear();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isAvailable( Model model )
        {
            return !StringUtils.isEmpty( model.getInceptionYear() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void copy( Model source, Model destination )
        {
            destination.setInceptionYear( source.getInceptionYear() );
        }
    };

    /**
     * @param descriptor is the {@link FlattenDescriptor} where to retrieve the {@link ElementHandling} from.
     * @return the {@link ElementHandling} for this {@link OptionalPomElement}.
     */
    public abstract ElementHandling getHandling( FlattenDescriptor descriptor );

    /**
     * @param model is the {@link Model} to check.
     * @return <code>true</code> if this {@link OptionalPomElement} is available in the given <code>model</code>,
     *         <code>false</code> otherwise (if element is null or empty).
     */
    public abstract boolean isAvailable( Model model );

    /**
     * Copies the element identified by this {@link OptionalPomElement} from <code>source</code> to
     * <code>destination</code>.
     *
     * @param source is the {@link Model} to copy from.
     * @param destination is the {@link Model} to copy to.
     */
    public abstract void copy( Model source, Model destination );

    /**
     * Applies the {@link ElementHandling} configured by this {@link OptionalPomElement} in the given
     * {@link FlattenDescriptor}.
     *
     * @param effectivePom is the calculated effective POM.
     * @param project is the according {@link MavenProject}.
     * @param flattenedPom is the flattened POM to build.
     * @param descriptor is the {@link FlattenDescriptor} configuring the {@link ElementHandling}.
     * @throws MojoFailureException in case an element is missing that should be added but has no default.
     */
    public void apply( Model effectivePom, MavenProject project, Model flattenedPom, FlattenDescriptor descriptor )
        throws MojoFailureException
    {
        ElementHandling handling = getHandling( descriptor );
        if ( handling == ElementHandling.Remove )
        {
            return;
        }
        if ( handling == null )
        {
            if ( ( this != Prerequisites ) || ( !"maven-plugin".equals( effectivePom.getPackaging() ) ) )
            {
                // by default we do not remove prerequisites for maven-plugins
                return;
            }
        }
        if ( isAvailable( project.getOriginalModel() ) )
        {
            // if defined in projects POM without inheritance we will copy in any case (KeepOrAdd/KeepIfExists)
            copy( effectivePom, flattenedPom );
        }
        else if ( handling == ElementHandling.KeepOrAdd )
        {
            if ( ( this != Name ) && ( !isAvailable( effectivePom ) ) )
            {
                throw new MojoFailureException( "Projects pom.xml is missing the " + name()
                    + " element that can not be added or genereated automatically." );

            }
            copy( effectivePom, flattenedPom );
        }
    }
}
