/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.DirectionResolver;
import com.puttysoftware.lasertank.utilities.LaserTypeConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public class RollingBarrelHorizontal extends AbstractMovableObject {
    // Constructors
    public RollingBarrelHorizontal() {
	super(true);
	this.type.set(TypeConstants.TYPE_BARREL);
	this.type.set(TypeConstants.TYPE_ICY);
    }

    @Override
    public final int getStringBaseID() {
	return 140;
    }

    @Override
    public Direction laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	final Direction dir = DirectionResolver.resolveRelative(dirX, dirY);
	if (dir == Direction.EAST || dir == Direction.WEST) {
	    // Roll
	    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	} else {
	    // Break up
	    final AbstractArena a = LaserTank.getApplication().getArenaManager().getArena();
	    // Boom!
	    SoundManager.playSound(SoundConstants.SOUND_BARREL);
	    // Destroy barrel
	    LaserTank.getApplication().getGameManager().morph(new Empty(), locX, locY, locZ, this.getLayer());
	    // Check for tank in range of explosion
	    final boolean target = a.circularScanTank(locX, locY, locZ, 1);
	    if (target) {
		// Kill tank
		LaserTank.getApplication().getGameManager().gameOver();
	    }
	    if (laserType == LaserTypeConstants.LASER_TYPE_POWER) {
		// Laser keeps going
		return DirectionResolver.resolveRelative(dirX, dirY);
	    } else {
		// Laser stops
		return Direction.NONE;
	    }
	}
    }

    @Override
    public void playSoundHook() {
	SoundManager.playSound(SoundConstants.SOUND_BALL_ROLL);
    }

    @Override
    public void pushCollideAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	// Break up
	final AbstractArena a = LaserTank.getApplication().getArenaManager().getArena();
	// Boom!
	SoundManager.playSound(SoundConstants.SOUND_BARREL);
	// Destroy barrel
	LaserTank.getApplication().getGameManager().morph(new Empty(), x, y, z, this.getLayer());
	// Check for tank in range of explosion
	final boolean target = a.circularScanTank(x, y, z, 1);
	if (target) {
	    // Kill tank
	    LaserTank.getApplication().getGameManager().gameOver();
	}
    }
}