/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractReactionWall;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.DirectionConstants;
import com.puttysoftware.ltremix.utilities.DirectionResolver;
import com.puttysoftware.ltremix.utilities.LaserTypeConstants;

public class RotaryMirror extends AbstractReactionWall {
    // Constructors
    public RotaryMirror() {
	super();
	this.setDirection(DirectionConstants.NORTHEAST);
	this.setDiagonalOnly(true);
    }

    @Override
    public int laserEnteredActionHook(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
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
		// Rotate mirror
		this.toggleDirection();
		SoundManager.playSound(SoundConstants.SOUND_ROTATE);
		LTRemix.getApplication().getGameManager().redrawArena();
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
    public boolean rangeActionHook(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int rangeType, final int forceUnits) {
	// Rotate mirror
	this.toggleDirection();
	SoundManager.playSound(SoundConstants.SOUND_ROTATE);
	LTRemix.getApplication().getArenaManager().getArena().markAsDirty(locX + dirX, locY + dirY, locZ);
	LTRemix.getApplication().getGameManager().redrawArena();
	return true;
    }

    @Override
    public boolean doLasersPassThrough() {
	return true;
    }

    @Override
    public final int getStringBaseID() {
	return 31;
    }

    @Override
    public boolean isDirectional() {
	return true;
    }
}
