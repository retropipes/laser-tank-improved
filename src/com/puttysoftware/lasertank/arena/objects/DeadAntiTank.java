/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.LaserTypeConstants;

public class DeadAntiTank extends AbstractMovableObject {
    // Constructors
    public DeadAntiTank() {
	super(false);
	this.setDirection(Direction.NORTH);
    }

    @Override
    public final int getStringBaseID() {
	return 11;
    }

    @Override
    public Direction laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	LaserTank.getApplication().getGameManager().haltMovingObjects();
	if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
	    // Destroy
	    SoundManager.playSound(SoundConstants.SOUND_BOOM);
	    LaserTank.getApplication().getGameManager().morph(this.getSavedObject(), locX, locY, locZ, this.getLayer());
	} else {
	    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	}
	return Direction.NONE;
    }

    @Override
    public void playSoundHook() {
	// Do nothing
    }
}
