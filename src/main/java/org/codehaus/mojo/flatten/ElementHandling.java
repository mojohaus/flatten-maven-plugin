package org.codehaus.mojo.flatten;

/**
 * This enum contains each available handling for a POM element when flattening.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0-beta-4
 */
public enum ElementHandling
{

    /** Remove the entire element so it is not present in the flattened POM. */
    remove,

    /** Take the element from the effective POM. */
    effective,

    /** Take the element from the original POM but resolve variables. */
    resolve,

    /** Take the element untouched from the original POM. */
    keep

}
