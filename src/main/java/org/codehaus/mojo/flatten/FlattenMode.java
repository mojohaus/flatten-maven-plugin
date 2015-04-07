/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package org.codehaus.mojo.flatten;

import org.apache.maven.model.Model;

/**
 * This enum contains the predefined modes how to deal with {@link FlattenDescriptor additional POM elements} when
 * {@link FlattenMojo generating the flattened POM}.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0-beta-2
 */
public enum FlattenMode
{
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
     * Keeps all {@link FlattenDescriptor optional POM elements} that are required for <a
     * href="https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide">OSS
     * Repository-Hosting</a>.
     */
    ossrh,

    /**
     * Like {@link #ossrh} but additionally keeps {@link Model#getDependencyManagement() dependencyManagement} and
     * {@link Model#getProperties() properties}. Especially it will keep the {@link Model#getDependencyManagement()
     * dependencyManagement} <em>as-is</em> without resolving parent influences and import-scoped dependencies. This is
     * useful if your POM represents a <a href=
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
    clean;

    /**
     * @return the {@link FlattenDescriptor} defined by this {@link FlattenMode}.
     */
    public FlattenDescriptor getDescriptor()
    {

        FlattenDescriptor descriptor = new FlattenDescriptor();
        switch ( this )
        {
            case minimum:
                descriptor.setPluginRepositories( ElementHandling.effective );
                //$FALL-THROUGH$
            case bom:
                // MOJO-2041
                descriptor.setDependencyManagement( ElementHandling.keep );
                descriptor.setProperties( ElementHandling.effective );
                //$FALL-THROUGH$
            case oss:
                descriptor.setCiManagement( ElementHandling.effective );
                descriptor.setContributors( ElementHandling.effective );
                descriptor.setDistributionManagement( ElementHandling.effective );
                descriptor.setInceptionYear( ElementHandling.effective );
                descriptor.setIssueManagement( ElementHandling.effective );
                descriptor.setMailingLists( ElementHandling.effective );
                descriptor.setOrganization( ElementHandling.effective );
                descriptor.setPrerequisites( ElementHandling.effective );
                //$FALL-THROUGH$
            case ossrh:
                descriptor.setName( ElementHandling.effective );
                descriptor.setDescription( ElementHandling.effective );
                descriptor.setUrl( ElementHandling.effective );
                descriptor.setScm( ElementHandling.effective );
                descriptor.setDevelopers( ElementHandling.effective );
                //$FALL-THROUGH$
            case defaults:
                descriptor.setRepositories( ElementHandling.effective );
                break;
            case clean:
                // nothing to do...
                break;
        }
        return descriptor;
    }

}
