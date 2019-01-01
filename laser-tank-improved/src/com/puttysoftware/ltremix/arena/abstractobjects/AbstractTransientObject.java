/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

import com.puttysoftware.ltremix.utilities.ArenaConstants;

public abstract class AbstractTransientObject extends AbstractArenaObject {
    // Constructors
    protected AbstractTransientObject() {
	super(true);
    }

    // Methods
    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	// Do nothing
    }

    @Override
    public int getPrimaryLayer() {
	return ArenaConstants.LAYER_VIRTUAL;
    }

    @Override
    public int getCustomProperty(final int propID) {
	return AbstractArenaObject.DEFAULT_CUSTOM_VALUE;
    }

    @Override
    public void setCustomProperty(final int propID, final int value) {
	// Do nothing
    }

    public abstract int getForceUnitsImbued();

    @Override
    public int getBlockHeight() {
	return 0;
    }
}
