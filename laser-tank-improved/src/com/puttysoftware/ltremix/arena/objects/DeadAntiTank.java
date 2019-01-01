/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.DirectionConstants;
import com.puttysoftware.ltremix.utilities.LaserTypeConstants;

public class DeadAntiTank extends AbstractMovableObject {
    // Constructors
    public DeadAntiTank() {
	super(false);
	this.setDirection(DirectionConstants.NORTH);
    }

    @Override
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	LTRemix.getApplication().getGameManager().haltMovingObjects();
	if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
	    // Destroy
	    SoundManager.playSound(SoundConstants.SOUND_BOOM);
	    LTRemix.getApplication().getGameManager().morph(this.getSavedObject(), locX, locY, locZ,
		    this.getPrimaryLayer());
	} else {
	    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	}
	return DirectionConstants.NONE;
    }

    @Override
    public void playSoundHook() {
	// Do nothing
    }

    @Override
    public final int getStringBaseID() {
	return 11;
    }

    @Override
    public boolean isDirectional() {
	return true;
    }
}
