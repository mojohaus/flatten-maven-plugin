package org.codehaus.mojo.flatten;

import org.apache.maven.model.Dependency;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DependencyComparatorTest
{
    private final DependencyComparator comparator = new DependencyComparator();

    @Test
    public void shouldOrder()
    {
        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add( createDependency( "g", "b", "1" ) );
        dependencies.add( createDependency( "g", "a", "1" ) );
        dependencies.add( createDependency( "g", "c", "1" ) );
        Collections.sort( dependencies, comparator );
        assertThat( dependencies ).usingComparatorForType( comparator, Dependency.class ).containsExactly(
                createDependency( "g", "a", "1" ),
                createDependency( "g", "b", "1" ),
                createDependency( "g", "c", "1" ) );
    }

    private Dependency createDependency( String groupId, String artifactId, String version )
    {
        Dependency dependency = new Dependency();
        dependency.setGroupId( groupId );
        dependency.setArtifactId( artifactId );
        dependency.setVersion( version );
        return dependency;
    }
}
