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

public class Wall extends AbstractWall {
    // Constructors
    public Wall() {
	super();
	this.type.set(TypeConstants.TYPE_PLAIN_WALL);
	this.setMaterial(MaterialConstants.MATERIAL_METALLIC);
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_ICE:
	    final IcyWall iw = new IcyWall();
	    iw.setPreviousState(this);
	    return iw;
	case MaterialConstants.MATERIAL_FIRE:
	    return new HotWall();
	default:
	    return this;
	}
    }

    @Override
    public final int getStringBaseID() {
	return 45;
    }

    @Override
    public Direction laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	if (laserType == LaserTypeConstants.LASER_TYPE_DISRUPTOR) {
	    // Disrupt wall
	    SoundManager.playSound(SoundConstants.SOUND_DISRUPTED);
	    LaserTank.getApplication().getGameManager().morph(new DisruptedWall(), locX, locY, locZ, this.getLayer());
	    return Direction.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
	    // Heat up wall
	    SoundManager.playSound(SoundConstants.SOUND_MELT);
	    LaserTank.getApplication().getGameManager().morph(new HotWall(), locX, locY, locZ, this.getLayer());
	    return Direction.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_STUNNER) {
	    // Freeze wall
	    SoundManager.playSound(SoundConstants.SOUND_FROZEN);
	    final IcyWall iw = new IcyWall();
	    iw.setPreviousState(this);
	    LaserTank.getApplication().getGameManager().morph(iw, locX, locY, locZ, this.getLayer());
	    return Direction.NONE;
	} else {
	    // Stop laser
	    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	}
    }
}