/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractWall;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.LaserTypeConstants;
import com.puttysoftware.lasertank.utilities.MaterialConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public class IcyWall extends AbstractWall {
    // Constructors
    public IcyWall() {
	super();
	this.type.set(TypeConstants.TYPE_PLAIN_WALL);
	this.setMaterial(MaterialConstants.MATERIAL_ICE);
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

    @Override
    public Direction laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	if (laserType == LaserTypeConstants.LASER_TYPE_DISRUPTOR) {
	    // Disrupt icy wall
	    SoundManager.playSound(SoundConstants.SOUND_DISRUPTED);
	    final DisruptedIcyWall diw = new DisruptedIcyWall();
	    if (this.hasPreviousState()) {
		diw.setPreviousState(this.getPreviousState());
	    }
	    LaserTank.getApplication().getGameManager().morph(diw, locX, locY, locZ, this.getLayer());
	    return Direction.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
	    // Defrost icy wall
	    SoundManager.playSound(SoundConstants.SOUND_DEFROST);
	    AbstractArenaObject ao;
	    if (this.hasPreviousState()) {
		ao = this.getPreviousState();
	    } else {
		ao = new Wall();
	    }
	    LaserTank.getApplication().getGameManager().morph(ao, locX, locY, locZ, this.getLayer());
	    return Direction.NONE;
	} else {
	    // Stop laser
	    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	}
    }
}