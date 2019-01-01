/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

import com.puttysoftware.ltremix.utilities.ArenaConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public abstract class AbstractGround extends AbstractArenaObject {
    // Constructors
    protected AbstractGround() {
	super(false, false, true);
	this.type.set(TypeConstants.TYPE_GROUND);
    }

    protected AbstractGround(final boolean hasFriction) {
	super(false, false, hasFriction);
	this.type.set(TypeConstants.TYPE_GROUND);
    }

    @Override
    public int getPrimaryLayer() {
	return ArenaConstants.LAYER_LOWER_GROUND;
    }

    @Override
    public int getCustomProperty(final int propID) {
	return AbstractArenaObject.DEFAULT_CUSTOM_VALUE;
    }

    @Override
    public void setCustomProperty(final int propID, final int value) {
	// Do nothing
    }

    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	// Do nothing
    }

    @Override
    public int getBlockHeight() {
	return 0;
    }
}
