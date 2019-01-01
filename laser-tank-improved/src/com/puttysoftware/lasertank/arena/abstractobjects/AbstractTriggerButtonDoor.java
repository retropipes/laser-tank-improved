/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.abstractobjects;

import com.puttysoftware.lasertank.utilities.TypeConstants;

public abstract class AbstractTriggerButtonDoor extends AbstractButtonDoor {
    // Constructors
    protected AbstractTriggerButtonDoor() {
	super();
	this.type.set(TypeConstants.TYPE_TRIGGER_BUTTON_DOOR);
    }
}