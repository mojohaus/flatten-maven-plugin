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
    /** For projects that want to keep all {@link FlattenDescriptor optional POM elements}. */
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
    ossrh;

    /**
     * @return the {@link FlattenDescriptor} defined by this {@link FlattenMode}.
     */
    public FlattenDescriptor getDescriptor()
    {

        FlattenDescriptor descriptor = new FlattenDescriptor();
        switch ( this )
        {
            case minimum:
                descriptor.setKeepRepositories();
                descriptor.setKeepPluginRepositories();
                //$FALL-THROUGH$
            case oss:
                descriptor.setKeepCiManagement();
                descriptor.setKeepContributors();
                descriptor.setKeepDistributionManagement();
                descriptor.setKeepInceptionYear();
                descriptor.setKeepIssueManagement();
                descriptor.setKeepMailingLists();
                descriptor.setKeepOrganization();
                descriptor.setKeepPrerequisites();
                //$FALL-THROUGH$
            case ossrh:
                descriptor.setKeepName();
                descriptor.setKeepDescription();
                descriptor.setKeepUrl();
                descriptor.setKeepScm();
                descriptor.setKeepDevelopers();
                break;
        }
        return descriptor;
    }

}
