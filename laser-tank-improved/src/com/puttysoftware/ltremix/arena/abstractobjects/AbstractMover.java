/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

import com.puttysoftware.ltremix.utilities.ArenaConstants;

public abstract class AbstractMover extends AbstractGround {
    // Constructors
    protected AbstractMover() {
	super();
    }

    protected AbstractMover(final boolean hasFriction) {
	super(hasFriction);
    }

    @Override
    public int getPrimaryLayer() {
	return ArenaConstants.LAYER_UPPER_GROUND;
    }
}
