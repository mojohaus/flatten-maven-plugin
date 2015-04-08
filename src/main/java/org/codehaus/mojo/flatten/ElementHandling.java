package org.codehaus.mojo.flatten;

/**
 * This enum contains each available handling for a POM element when flattening.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0-beta-4
 */
public enum ElementHandling
{

    /**
     * Flatten the element. For most elements this means that they will be removed and are not present in the flattened
     * POM.
     */
    flatten,

    /** Take the element from the effective POM. */
    expand,

    /** Take the element from the resolved POM. */
    resolve,

    /** Take the element from the interpolated POM (original POM with variables interpolated). */
    interpolate,

    /** Take the element untouched from the original POM. */
    keep

}
