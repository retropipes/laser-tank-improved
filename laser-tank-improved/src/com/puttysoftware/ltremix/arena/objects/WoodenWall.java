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

public class WoodenWall extends AbstractWall {
    // Constructors
    public WoodenWall() {
	super();
	this.type.set(TypeConstants.TYPE_PLAIN_WALL);
	this.setMaterial(MaterialConstants.MATERIAL_WOODEN);
    }

    @Override
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	if (laserType == LaserTypeConstants.LASER_TYPE_DISRUPTOR) {
	    // Disrupt wooden wall
	    SoundManager.playSound(SoundConstants.SOUND_DISRUPTED);
	    LTRemix.getApplication().getGameManager().morph(new DisruptedWoodenWall(), locX, locY, locZ,
		    this.getPrimaryLayer());
	    return DirectionConstants.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
	    // Destroy wooden wall
	    SoundManager.playSound(SoundConstants.SOUND_BOOM);
	    LTRemix.getApplication().getGameManager().morph(new Empty(), locX, locY, locZ, this.getPrimaryLayer());
	    return DirectionConstants.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_STUNNER) {
	    // Freeze wooden wall
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
	return 56;
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_FIRE:
	    return new Ground();
	case MaterialConstants.MATERIAL_ICE:
	    final IcyWall iw = new IcyWall();
	    iw.setPreviousState(this);
	    return iw;
	default:
	    return this;
	}
    }
}