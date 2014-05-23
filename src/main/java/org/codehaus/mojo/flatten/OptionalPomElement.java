/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package org.codehaus.mojo.flatten;

import org.apache.maven.model.Model;
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
            if ( issueManagement.getUrl() != null )
            {
                return true;
            }
            if ( issueManagement.getSystem() != null )
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
        if ( ( handling == ElementHandling.Remove ) || ( handling == null ) )
        {
            return;
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
