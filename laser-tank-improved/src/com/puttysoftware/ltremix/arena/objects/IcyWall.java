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

public class IcyWall extends AbstractWall {
    // Constructors
    public IcyWall() {
	super();
	this.type.set(TypeConstants.TYPE_PLAIN_WALL);
	this.setMaterial(MaterialConstants.MATERIAL_ICE);
    }

    @Override
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	if (laserType == LaserTypeConstants.LASER_TYPE_DISRUPTOR) {
	    // Disrupt icy wall
	    SoundManager.playSound(SoundConstants.SOUND_DISRUPTED);
	    final DisruptedIcyWall diw = new DisruptedIcyWall();
	    if (this.hasPreviousState()) {
		diw.setPreviousState(this.getPreviousState());
	    }
	    LTRemix.getApplication().getGameManager().morph(diw, locX, locY, locZ, this.getPrimaryLayer());
	    return DirectionConstants.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
	    // Defrost icy wall
	    SoundManager.playSound(SoundConstants.SOUND_DEFROST);
	    AbstractArenaObject ao;
	    if (this.hasPreviousState()) {
		ao = this.getPreviousState();
	    } else {
		ao = new Wall();
	    }
	    LTRemix.getApplication().getGameManager().morph(ao, locX, locY, locZ, this.getPrimaryLayer());
	    return DirectionConstants.NONE;
	} else {
	    // Stop laser
	    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	}
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_FIRE:
	    if (this.hasPreviousState()) {
		return this.getPreviousState();
	    } else {
		return new Wall();
	    }
	default:
	    return this;
	}
    }

    @Override
    public final int getStringBaseID() {
	return 58;
    }
}