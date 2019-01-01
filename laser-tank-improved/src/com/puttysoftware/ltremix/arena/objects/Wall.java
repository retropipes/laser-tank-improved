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

public class Wall extends AbstractWall {
    // Constructors
    public Wall() {
	super();
	this.type.set(TypeConstants.TYPE_PLAIN_WALL);
	this.setMaterial(MaterialConstants.MATERIAL_METALLIC);
    }

    @Override
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	if (laserType == LaserTypeConstants.LASER_TYPE_DISRUPTOR) {
	    // Disrupt wall
	    SoundManager.playSound(SoundConstants.SOUND_DISRUPTED);
	    LTRemix.getApplication().getGameManager().morph(new DisruptedWall(), locX, locY, locZ,
		    this.getPrimaryLayer());
	    return DirectionConstants.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
	    // Heat up wall
	    SoundManager.playSound(SoundConstants.SOUND_MELT);
	    LTRemix.getApplication().getGameManager().morph(new HotWall(), locX, locY, locZ, this.getPrimaryLayer());
	    return DirectionConstants.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_STUNNER) {
	    // Freeze wall
	    SoundManager.playSound(SoundConstants.SOUND_FROZEN);
	    final IcyWall iw = new IcyWall();
	    iw.setPreviousState(this);
	    LTRemix.getApplication().getGameManager().morph(iw, locX, locY, locZ, this.getPrimaryLayer());
	    return DirectionConstants.NONE;
	} else {
	    // Stop laser
	    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	}
    }

    @Override
    public final int getStringBaseID() {
	return 45;
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
}