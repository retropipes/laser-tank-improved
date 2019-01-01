/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

public class InvalidArenaException extends Exception {
    // Serialization
    private static final long serialVersionUID = 999L;

    // Constructors
    public InvalidArenaException() {
	super();
    }

    public InvalidArenaException(final String msg) {
	super(msg);
    }
}
