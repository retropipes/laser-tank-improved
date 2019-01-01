/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

public abstract class AbstractReactionPassThroughObject extends AbstractPassThroughObject {
    // Constructors
    protected AbstractReactionPassThroughObject() {
	super();
    }

    @Override
    public final int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	if (forceUnits >= this.getMinimumReactionForce()) {
	    return this.laserEnteredActionHook(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	} else {
	    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	}
    }

    @Override
    public final boolean rangeAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int rangeType, final int forceUnits) {
	if (forceUnits >= this.getMinimumReactionForce()) {
	    return this.rangeActionHook(locX, locY, locZ, dirX, dirY, rangeType, forceUnits);
	} else {
	    return super.rangeAction(locX, locY, locZ, dirX, dirY, rangeType, forceUnits);
	}
    }

    public abstract int laserEnteredActionHook(int locX, int locY, int locZ, int dirX, int dirY, int laserType,
	    int forceUnits);

    public abstract boolean rangeActionHook(int locX, int locY, int locZ, int dirX, int dirY, int laserType,
	    int forceUnits);
}