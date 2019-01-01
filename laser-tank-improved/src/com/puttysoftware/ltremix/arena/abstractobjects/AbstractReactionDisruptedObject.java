/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

import com.puttysoftware.ltremix.utilities.ActionConstants;

public abstract class AbstractReactionDisruptedObject extends AbstractReactionPassThroughObject {
    // Constructors
    protected AbstractReactionDisruptedObject() {
	super();
    }

    @Override
    public boolean acceptTick(final int actionType) {
	return actionType == ActionConstants.ACTION_MOVE;
    }
}