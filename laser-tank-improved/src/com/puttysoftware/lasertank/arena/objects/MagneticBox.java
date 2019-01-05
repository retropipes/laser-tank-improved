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

public class MagneticBox extends AbstractMovableObject {
    // Constructors
    public MagneticBox() {
	super(true);
	this.type.set(TypeConstants.TYPE_BOX);
	this.type.set(TypeConstants.TYPE_MAGNETIC_BOX);
	this.setMaterial(MaterialConstants.MATERIAL_MAGNETIC);
    }

    @Override
    public final int getStringBaseID() {
	return 22;
    }

    @Override
    public Direction laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	final Application app = LaserTank.getApplication();
	final AbstractArenaObject mo = app.getArenaManager().getArena().getCell(locX - dirX, locY - dirY, locZ,
		this.getLayer());
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
	return Direction.NONE;
    }

    @Override
    public void playSoundHook() {
	SoundManager.playSound(SoundConstants.SOUND_PUSH_BOX);
    }
}