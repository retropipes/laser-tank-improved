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

public class DisruptedWall extends AbstractDisruptedObject {
    // Fields
    private int disruptionLeft;
    private static final int DISRUPTION_START = 20;

    // Constructors
    public DisruptedWall() {
	super();
	this.type.set(TypeConstants.TYPE_PLAIN_WALL);
	this.disruptionLeft = DisruptedWall.DISRUPTION_START;
	this.activateTimer(1);
	this.setMaterial(MaterialConstants.MATERIAL_METALLIC);
    }

    DisruptedWall(final int disruption) {
	super();
	this.type.set(TypeConstants.TYPE_PLAIN_WALL);
	this.disruptionLeft = disruption;
	this.activateTimer(1);
	this.setMaterial(MaterialConstants.MATERIAL_METALLIC);
    }

    @Override
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
	    // Heat up wall
	    SoundManager.playSound(SoundConstants.SOUND_MELT);
	    LTRemix.getApplication().getGameManager().morph(new DisruptedHotWall(this.disruptionLeft), locX, locY, locZ,
		    this.getPrimaryLayer());
	    return DirectionConstants.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_STUNNER) {
	    // Freeze wall
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
	    LTRemix.getApplication().getGameManager().morph(new Wall(), locX, locY, z, this.getPrimaryLayer());
	} else {
	    this.activateTimer(1);
	}
    }

    @Override
    public final int getStringBaseID() {
	return 52;
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_ICE:
	    final DisruptedIcyWall diw = new DisruptedIcyWall(this.disruptionLeft);
	    diw.setPreviousState(this);
	    return diw;
	case MaterialConstants.MATERIAL_FIRE:
	    return new DisruptedHotWall(this.disruptionLeft);
	default:
	    return this;
	}
    }
}