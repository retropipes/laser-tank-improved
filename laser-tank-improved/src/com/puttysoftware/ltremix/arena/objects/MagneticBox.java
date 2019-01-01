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

public class MagneticBox extends AbstractMovableObject {
    // Constructors
    public MagneticBox() {
	super(true);
	this.type.set(TypeConstants.TYPE_BOX);
	this.type.set(TypeConstants.TYPE_MAGNETIC_BOX);
	this.setMaterial(MaterialConstants.MATERIAL_MAGNETIC);
    }

    @Override
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	final Application app = LTRemix.getApplication();
	final AbstractArenaObject mo = app.getArenaManager().getArena().getCell(locX - dirX, locY - dirY, locZ,
		this.getPrimaryLayer());
	if (laserType == LaserTypeConstants.LASER_TYPE_BLUE && mo != null
		&& (mo.isOfType(TypeConstants.TYPE_CHARACTER) || !mo.isSolid())) {
	    app.getGameManager().updatePushedPosition(locX, locY, locX + dirX, locY + dirY, this);
	    this.playSoundHook();
	} else if (mo != null && (mo.isOfType(TypeConstants.TYPE_CHARACTER) || !mo.isSolid())) {
	    app.getGameManager().updatePushedPosition(locX, locY, locX - dirX, locY - dirY, this);
	    this.playSoundHook();
	} else {
	    if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
		SoundManager.playSound(SoundConstants.SOUND_BOOM);
	    } else {
		return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	    }
	}
	app.getGameManager().redrawArena();
	return DirectionConstants.NONE;
    }

    @Override
    public void playSoundHook() {
	SoundManager.playSound(SoundConstants.SOUND_PUSH_BOX);
    }

    @Override
    public final int getStringBaseID() {
	return 22;
    }
}