/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.DirectionConstants;
import com.puttysoftware.ltremix.utilities.LaserTypeConstants;
import com.puttysoftware.ltremix.utilities.MaterialConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class WoodenBox extends AbstractMovableObject {
    // Constructors
    public WoodenBox() {
	super(true);
	this.type.set(TypeConstants.TYPE_BOX);
	this.setMaterial(MaterialConstants.MATERIAL_WOODEN);
    }

    @Override
    public void playSoundHook() {
	SoundManager.playSound(SoundConstants.SOUND_PUSH_BOX);
    }

    @Override
    public final int getStringBaseID() {
	return 70;
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
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	final Application app = LTRemix.getApplication();
	if (forceUnits >= this.getMinimumReactionForce()) {
	    final AbstractArenaObject mof = app.getArenaManager().getArena().getCell(locX + dirX, locY + dirY, locZ,
		    this.getPrimaryLayer());
	    final AbstractArenaObject mor = app.getArenaManager().getArena().getCell(locX - dirX, locY - dirY, locZ,
		    this.getPrimaryLayer());
	    if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
		// Destroy wooden box
		SoundManager.playSound(SoundConstants.SOUND_BARREL);
		app.getGameManager().morph(new Empty(), locX, locY, locZ, this.getPrimaryLayer());
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
	app.getGameManager().redrawArena();
	return DirectionConstants.NONE;
    }
}