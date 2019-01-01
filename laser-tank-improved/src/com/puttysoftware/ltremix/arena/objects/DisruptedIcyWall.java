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

public class DisruptedIcyWall extends AbstractDisruptedObject {
    // Fields
    private int disruptionLeft;
    private static final int DISRUPTION_START = 20;

    // Constructors
    public DisruptedIcyWall() {
	super();
	this.type.set(TypeConstants.TYPE_PLAIN_WALL);
	this.disruptionLeft = DisruptedIcyWall.DISRUPTION_START;
	this.activateTimer(1);
	this.setMaterial(MaterialConstants.MATERIAL_ICE);
    }

    DisruptedIcyWall(final int disruption) {
	super();
	this.type.set(TypeConstants.TYPE_PLAIN_WALL);
	this.disruptionLeft = disruption;
	this.activateTimer(1);
	this.setMaterial(MaterialConstants.MATERIAL_ICE);
    }

    @Override
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
	    // Defrost icy wall
	    SoundManager.playSound(SoundConstants.SOUND_DEFROST);
	    final DisruptedWall dw = new DisruptedWall();
	    if (this.hasPreviousState()) {
		dw.setPreviousState(this.getPreviousState());
	    }
	    LTRemix.getApplication().getGameManager().morph(dw, locX, locY, locZ, this.getPrimaryLayer());
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
	    final IcyWall iw = new IcyWall();
	    if (this.hasPreviousState()) {
		iw.setPreviousState(this.getPreviousState());
	    }
	    LTRemix.getApplication().getGameManager().morph(iw, locX, locY, z, this.getPrimaryLayer());
	} else {
	    this.activateTimer(1);
	}
    }

    @Override
    public final int getStringBaseID() {
	return 59;
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_FIRE:
	    return new DisruptedWall(this.disruptionLeft);
	default:
	    return this;
	}
    }
}