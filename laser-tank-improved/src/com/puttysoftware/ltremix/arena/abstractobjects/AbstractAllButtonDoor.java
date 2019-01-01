/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

import com.puttysoftware.ltremix.utilities.TypeConstants;

public abstract class AbstractAllButtonDoor extends AbstractButtonDoor {
    // Constructors
    protected AbstractAllButtonDoor() {
	super();
	this.type.set(TypeConstants.TYPE_ALL_BUTTON_DOOR);
    }
}