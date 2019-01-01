/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.AbstractArena;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.DirectionConstants;
import com.puttysoftware.ltremix.utilities.DirectionResolver;
import com.puttysoftware.ltremix.utilities.LaserTypeConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class RollingBarrelVertical extends AbstractMovableObject {
    // Constructors
    public RollingBarrelVertical() {
	super(true);
	this.type.set(TypeConstants.TYPE_BARREL);
	this.type.set(TypeConstants.TYPE_ICY);
    }

    @Override
    public void playSoundHook() {
	SoundManager.playSound(SoundConstants.SOUND_BALL_ROLL);
    }

    @Override
    public final int getStringBaseID() {
	return 141;
    }

    @Override
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	final int dir = DirectionResolver.resolveRelativeDirection(dirX, dirY);
	if (dir == DirectionConstants.NORTH || dir == DirectionConstants.SOUTH) {
	    // Roll
	    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	} else {
	    // Break up
	    final AbstractArena a = LTRemix.getApplication().getArenaManager().getArena();
	    // Boom!
	    SoundManager.playSound(SoundConstants.SOUND_BARREL);
	    // Destroy barrel
	    LTRemix.getApplication().getGameManager().morph(new Empty(), locX, locY, locZ, this.getPrimaryLayer());
	    // Check for tank in range of explosion
	    final boolean target = a.circularScanTank(locX, locY, locZ, 1);
	    if (target) {
		// Kill tank
		LTRemix.getApplication().getGameManager().gameOver();
	    }
	    if (laserType == LaserTypeConstants.LASER_TYPE_POWER) {
		// Laser keeps going
		return DirectionResolver.resolveRelativeDirection(dirX, dirY);
	    } else {
		// Laser stops
		return DirectionConstants.NONE;
	    }
	}
    }

    @Override
    public void pushCollideAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	// Break up
	final AbstractArena a = LTRemix.getApplication().getArenaManager().getArena();
	// Boom!
	SoundManager.playSound(SoundConstants.SOUND_BARREL);
	// Destroy barrel
	LTRemix.getApplication().getGameManager().morph(new Empty(), x, y, z, this.getPrimaryLayer());
	// Check for tank in range of explosion
	final boolean target = a.circularScanTank(x, y, z, 1);
	if (target) {
	    // Kill tank
	    LTRemix.getApplication().getGameManager().gameOver();
	}
    }
}