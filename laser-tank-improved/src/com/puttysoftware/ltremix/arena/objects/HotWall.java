/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractWall;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.DirectionConstants;
import com.puttysoftware.ltremix.utilities.LaserTypeConstants;
import com.puttysoftware.ltremix.utilities.MaterialConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class HotWall extends AbstractWall {
    // Constructors
    public HotWall() {
	super();
	this.type.set(TypeConstants.TYPE_PLAIN_WALL);
	this.setMaterial(MaterialConstants.MATERIAL_FIRE);
    }

    @Override
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	if (laserType == LaserTypeConstants.LASER_TYPE_DISRUPTOR) {
	    // Disrupt hot wall
	    SoundManager.playSound(SoundConstants.SOUND_DISRUPTED);
	    LTRemix.getApplication().getGameManager().morph(new DisruptedHotWall(), locX, locY, locZ,
		    this.getPrimaryLayer());
	    return DirectionConstants.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_STUNNER) {
	    // Cool off hot wall
	    SoundManager.playSound(SoundConstants.SOUND_COOL_OFF);
	    LTRemix.getApplication().getGameManager().morph(new Wall(), locX, locY, locZ, this.getPrimaryLayer());
	    return DirectionConstants.NONE;
	} else {
	    // Stop laser
	    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	}
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_ICE:
	    return new Wall();
	default:
	    return this;
	}
    }

    @Override
    public final int getStringBaseID() {
	return 60;
    }
}