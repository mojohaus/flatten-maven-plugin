package org.codehaus.mojo.flatten;

import org.apache.maven.model.Dependency;

import java.util.Comparator;

/**
 * Orders a {@link Dependency} through its groupId and artifactId.
 */
public class DependencyComparator implements Comparator<Dependency>
{
    @Override
    public int compare( Dependency o1, Dependency o2 )
    {
        int c = o1.getGroupId().compareTo( o2.getGroupId() );
        if ( c == 0 )
        {
            // Same groupId
            c = o1.getArtifactId().compareTo( o2.getArtifactId() );
        }
        return c;
    }
}
