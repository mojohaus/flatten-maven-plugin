/* Copyright (c) The m-m-m Team, Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0 */
package org.codehaus.mojo.flatten;

/**
 * This enum contains the available options to handle a POM element.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0-beta-2
 */
public enum ElementHandling
{
    /** Remove the according element from the POM. */
    Remove,

    /** Keep the according element in the POM if it exists. */
    KeepIfExists,

    /** Keep the according element in the POM if it exists or add it from default or parent inheritance. */
    KeepOrAdd
}
