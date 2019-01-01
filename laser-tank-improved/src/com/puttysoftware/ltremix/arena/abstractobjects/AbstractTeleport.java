/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

import com.puttysoftware.ltremix.utilities.ArenaConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public abstract class AbstractTeleport extends AbstractArenaObject {
    // Constructors
    protected AbstractTeleport() {
	super(false);
	this.type.set(TypeConstants.TYPE_TELEPORT);
    }

    public abstract int getDestinationFloor();

    @Override
    public abstract void postMoveAction(final int dirX, final int dirY, int dirZ);

    @Override
    public int getPrimaryLayer() {
	return ArenaConstants.LAYER_LOWER_OBJECTS;
    }

    @Override
    public int getCustomProperty(final int propID) {
	return AbstractArenaObject.DEFAULT_CUSTOM_VALUE;
    }

    @Override
    public void setCustomProperty(final int propID, final int value) {
	// Do nothing
    }
}
