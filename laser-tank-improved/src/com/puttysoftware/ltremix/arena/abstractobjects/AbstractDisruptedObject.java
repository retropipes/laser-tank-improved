/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

import com.puttysoftware.ltremix.utilities.ActionConstants;

public abstract class AbstractDisruptedObject extends AbstractPassThroughObject {
    // Constructors
    protected AbstractDisruptedObject() {
	super();
    }

    @Override
    public boolean acceptTick(final int actionType) {
	return actionType == ActionConstants.ACTION_MOVE;
    }
}