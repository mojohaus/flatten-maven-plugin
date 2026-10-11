package org.codehaus.mojo.flatten;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Runs the test with {@link ProjectMojoExtension}: like {@code @MojoTest}, but the mojo works on the project built
 * from a copy of its POM's directory.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(ProjectMojoExtension.class)
@interface ProjectMojoTest {}
