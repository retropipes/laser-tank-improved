/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.LaserTypeConstants;
import com.puttysoftware.lasertank.utilities.MaterialConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public class WoodenBox extends AbstractMovableObject {
    // Constructors
    public WoodenBox() {
	super(true);
	this.type.set(TypeConstants.TYPE_BOX);
	this.setMaterial(MaterialConstants.MATERIAL_WOODEN);
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_ICE:
	    final IcyBox ib = new IcyBox();
	    ib.setPreviousState(this);
	    return ib;
	case MaterialConstants.MATERIAL_FIRE:
	    return new Ground();
	default:
	    return this;
	}
    }

    @Override
    public final int getStringBaseID() {
	return 70;
    }

    @Override
    public Direction laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	final Application app = LaserTank.getApplication();
	if (forceUnits >= this.getMinimumReactionForce()) {
	    final AbstractArenaObject mof = app.getArenaManager().getArena().getCell(locX + dirX, locY + dirY, locZ,
		    this.getLayer());
	    final AbstractArenaObject mor = app.getArenaManager().getArena().getCell(locX - dirX, locY - dirY, locZ,
		    this.getLayer());
	    if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
		// Destroy wooden box
		SoundManager.playSound(SoundConstants.SOUND_BARREL);
		app.getGameManager().morph(new Empty(), locX, locY, locZ, this.getLayer());
	    } else if (laserType == LaserTypeConstants.LASER_TYPE_BLUE && mor != null
		    && (mor.isOfType(TypeConstants.TYPE_CHARACTER) || !mor.isSolid())) {
		app.getGameManager().updatePushedPosition(locX, locY, locX - dirX, locY - dirY, this);
		this.playSoundHook();
	    } else if (mof != null && (mof.isOfType(TypeConstants.TYPE_CHARACTER) || !mof.isSolid())) {
		app.getGameManager().updatePushedPosition(locX, locY, locX + dirX, locY + dirY, this);
		this.playSoundHook();
	    } else {
		// Object doesn't react to this type of laser
		return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	    }
	} else {
	    // Not enough force
	    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	}
	return Direction.NONE;
    }

    @Override
    public void playSoundHook() {
	SoundManager.playSound(SoundConstants.SOUND_PUSH_BOX);
    }
}