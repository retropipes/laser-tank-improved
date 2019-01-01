/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractDisruptedObject;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.DirectionConstants;
import com.puttysoftware.ltremix.utilities.LaserTypeConstants;
import com.puttysoftware.ltremix.utilities.MaterialConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class DisruptedWoodenWall extends AbstractDisruptedObject {
    // Fields
    private int disruptionLeft;
    private static final int DISRUPTION_START = 20;

    // Constructors
    public DisruptedWoodenWall() {
	super();
	this.type.set(TypeConstants.TYPE_PLAIN_WALL);
	this.disruptionLeft = DisruptedWoodenWall.DISRUPTION_START;
	this.activateTimer(1);
	this.setMaterial(MaterialConstants.MATERIAL_WOODEN);
    }

    @Override
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
	    // Destroy disrupted wooden wall
	    SoundManager.playSound(SoundConstants.SOUND_BOOM);
	    LTRemix.getApplication().getGameManager().morph(new Empty(), locX, locY, locZ, this.getPrimaryLayer());
	    return DirectionConstants.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_STUNNER) {
	    // Freeze disrupted wooden wall
	    SoundManager.playSound(SoundConstants.SOUND_FROZEN);
	    LTRemix.getApplication().getGameManager().morph(new DisruptedIcyWall(this.disruptionLeft), locX, locY, locZ,
		    this.getPrimaryLayer());
	    return DirectionConstants.NONE;
	} else {
	    // Stop laser
	    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	}
    }

    @Override
    public void timerExpiredAction(final int locX, final int locY) {
	this.disruptionLeft--;
	if (this.disruptionLeft == 0) {
	    SoundManager.playSound(SoundConstants.SOUND_DISRUPT_END);
	    final int z = LTRemix.getApplication().getGameManager().getPlayerManager().getPlayerLocationZ();
	    LTRemix.getApplication().getGameManager().morph(new WoodenWall(), locX, locY, z, this.getPrimaryLayer());
	} else {
	    this.activateTimer(1);
	}
    }

    @Override
    public final int getStringBaseID() {
	return 57;
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_FIRE:
	    return new Ground();
	case MaterialConstants.MATERIAL_ICE:
	    return new DisruptedIcyWall();
	default:
	    return this;
	}
    }
}