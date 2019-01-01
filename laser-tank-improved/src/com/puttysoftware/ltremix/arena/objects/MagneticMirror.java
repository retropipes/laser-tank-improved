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
import com.puttysoftware.ltremix.utilities.DirectionResolver;
import com.puttysoftware.ltremix.utilities.LaserTypeConstants;
import com.puttysoftware.ltremix.utilities.MaterialConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class MagneticMirror extends AbstractMovableObject {
    // Constructors
    public MagneticMirror() {
	super(true);
	this.setDirection(DirectionConstants.NORTHEAST);
	this.setDiagonalOnly(true);
	this.type.set(TypeConstants.TYPE_MOVABLE_MIRROR);
	this.setMaterial(MaterialConstants.MATERIAL_MAGNETIC);
    }

    @Override
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
	    // Destroy mirror
	    SoundManager.playSound(SoundConstants.SOUND_BOOM);
	    LTRemix.getApplication().getGameManager().morph(new Empty(), locX, locY, locZ, this.getPrimaryLayer());
	    return DirectionConstants.NONE;
	} else {
	    final int dir = DirectionResolver.resolveRelativeDirectionInvert(dirX, dirY);
	    if (this.hitReflectiveSide(dir)) {
		// Reflect laser
		return this.getDirection();
	    } else {
		// Move mirror
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
	}
    }

    @Override
    public int laserExitedAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType) {
	// Finish reflecting laser
	SoundManager.playSound(SoundConstants.SOUND_REFLECT);
	final int oldlaser = DirectionResolver.resolveRelativeDirectionInvert(locX, locY);
	final int currdir = this.getDirection();
	if (oldlaser == DirectionConstants.NORTH) {
	    if (currdir == DirectionConstants.NORTHWEST) {
		return DirectionConstants.WEST;
	    } else if (currdir == DirectionConstants.NORTHEAST) {
		return DirectionConstants.EAST;
	    }
	} else if (oldlaser == DirectionConstants.SOUTH) {
	    if (currdir == DirectionConstants.SOUTHWEST) {
		return DirectionConstants.WEST;
	    } else if (currdir == DirectionConstants.SOUTHEAST) {
		return DirectionConstants.EAST;
	    }
	} else if (oldlaser == DirectionConstants.WEST) {
	    if (currdir == DirectionConstants.SOUTHWEST) {
		return DirectionConstants.SOUTH;
	    } else if (currdir == DirectionConstants.NORTHWEST) {
		return DirectionConstants.NORTH;
	    }
	} else if (oldlaser == DirectionConstants.EAST) {
	    if (currdir == DirectionConstants.SOUTHEAST) {
		return DirectionConstants.SOUTH;
	    } else if (currdir == DirectionConstants.NORTHEAST) {
		return DirectionConstants.NORTH;
	    }
	}
	return DirectionConstants.NONE;
    }

    @Override
    public void playSoundHook() {
	SoundManager.playSound(SoundConstants.SOUND_PUSH_MIRROR);
    }

    @Override
    public boolean doLasersPassThrough() {
	return true;
    }

    @Override
    public final int getStringBaseID() {
	return 23;
    }

    @Override
    public boolean isDirectional() {
	return true;
    }
}
