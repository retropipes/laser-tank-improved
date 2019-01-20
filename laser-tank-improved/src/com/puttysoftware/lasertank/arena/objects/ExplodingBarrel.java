/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractReactionWall;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.DirectionResolver;
import com.puttysoftware.lasertank.utilities.LaserTypeConstants;
import com.puttysoftware.lasertank.utilities.MaterialConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public class ExplodingBarrel extends AbstractReactionWall {
    private static void laserEnteredActionInnerP2(final int locX, final int locY, final int locZ, final int locW) {
	// Destroy barrel
	LaserTank.getApplication().getGameManager().morph(new Empty(), locX, locY, locZ, locW);
	// Check for nearby exploding barrels and blow them up too
	try {
	    final boolean boom2 = LaserTank.getApplication().getArenaManager().getArena()
		    .getCell(locX, locY - 1, locZ, locW).getClass().equals(ExplodingBarrel.class);
	    if (boom2) {
		ExplodingBarrel.laserEnteredActionInnerP2(locX, locY - 1, locZ, locW);
	    }
	} catch (final ArrayIndexOutOfBoundsException aioobe) {
	    // Ignore
	}
	try {
	    final boolean boom4 = LaserTank.getApplication().getArenaManager().getArena()
		    .getCell(locX - 1, locY, locZ, locW).getClass().equals(ExplodingBarrel.class);
	    if (boom4) {
		ExplodingBarrel.laserEnteredActionInnerP2(locX - 1, locY, locZ, locW);
	    }
	} catch (final ArrayIndexOutOfBoundsException aioobe) {
	    // Ignore
	}
	try {
	    final boolean boom6 = LaserTank.getApplication().getArenaManager().getArena()
		    .getCell(locX + 1, locY, locZ, locW).getClass().equals(ExplodingBarrel.class);
	    if (boom6) {
		ExplodingBarrel.laserEnteredActionInnerP2(locX + 1, locY, locZ, locW);
	    }
	} catch (final ArrayIndexOutOfBoundsException aioobe) {
	    // Ignore
	}
	try {
	    final boolean boom8 = LaserTank.getApplication().getArenaManager().getArena()
		    .getCell(locX, locY + 1, locZ, locW).getClass().equals(ExplodingBarrel.class);
	    if (boom8) {
		ExplodingBarrel.laserEnteredActionInnerP2(locX, locY + 1, locZ, locW);
	    }
	} catch (final ArrayIndexOutOfBoundsException aioobe) {
	    // Ignore
	}
    }

    // Fields
    private boolean destroyed;

    // Constructors
    public ExplodingBarrel() {
	super();
	this.type.set(TypeConstants.TYPE_BARREL);
	this.setMaterial(MaterialConstants.MATERIAL_WOODEN);
	this.destroyed = false;
    }

    @Override
    public final int getStringBaseID() {
	return 12;
    }

    @Override
    public Direction laserEnteredActionHook(final int locX, final int locY, final int locZ, final int dirX,
	    final int dirY, final int laserType, final int forceUnits) {
	// Boom!
	SoundManager.playSound(SoundConstants.SOUND_BOOM);
	// Did tank die?
	final boolean dead = this.laserEnteredActionInnerP1(locX, locY, locZ, false);
	if (dead) {
	    // Kill tank
	    LaserTank.getApplication().getGameManager().gameOver();
	    return Direction.NONE;
	}
	ExplodingBarrel.laserEnteredActionInnerP2(locX, locY, locZ, this.getLayer());
	if (laserType == LaserTypeConstants.LASER_TYPE_POWER) {
	    // Laser keeps going
	    return DirectionResolver.resolveRelative(dirX, dirY);
	} else {
	    // Laser stops
	    return Direction.NONE;
	}
    }

    private boolean laserEnteredActionInnerP1(final int locX, final int locY, final int locZ, final boolean oldDead) {
	final AbstractArena a = LaserTank.getApplication().getArenaManager().getArena();
	boolean dead = oldDead;
	// Check if this barrel's been destroyed already
	if (this.destroyed) {
	    return dead;
	}
	// Check for tank in range of explosion
	if (!dead) {
	    dead = a.circularScanTank(locX, locY, locZ, 1);
	}
	// Set destroyed status
	this.destroyed = true;
	// Check for nearby exploding barrels and blow them up too
	final boolean boom2 = LaserTank.getApplication().getArenaManager().getArena()
		.getCell(locX, locY - 1, locZ, this.getLayer()).getClass().equals(ExplodingBarrel.class);
	if (boom2) {
	    return this.laserEnteredActionInnerP1(locX, locY - 1, locZ, dead);
	}
	final boolean boom4 = LaserTank.getApplication().getArenaManager().getArena()
		.getCell(locX - 1, locY, locZ, this.getLayer()).getClass().equals(ExplodingBarrel.class);
	if (boom4) {
	    return this.laserEnteredActionInnerP1(locX - 1, locY, locZ, dead);
	}
	final boolean boom6 = LaserTank.getApplication().getArenaManager().getArena()
		.getCell(locX + 1, locY, locZ, this.getLayer()).getClass().equals(ExplodingBarrel.class);
	if (boom6) {
	    return this.laserEnteredActionInnerP1(locX + 1, locY, locZ, dead);
	}
	final boolean boom8 = LaserTank.getApplication().getArenaManager().getArena()
		.getCell(locX, locY + 1, locZ, this.getLayer()).getClass().equals(ExplodingBarrel.class);
	if (boom8) {
	    return this.laserEnteredActionInnerP1(locX, locY + 1, locZ, dead);
	}
	// Communicate tank dead status back to caller
	return dead;
    }

    @Override
    public void pushCollideAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	// React to balls hitting exploding barrels
	if (pushed.isOfType(TypeConstants.TYPE_BALL)) {
	    this.laserEnteredAction(x, y, z, 0, 0, LaserTypeConstants.LASER_TYPE_GREEN, 1);
	}
    }

    @Override
    public boolean rangeActionHook(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int rangeType, final int forceUnits) {
	// Boom!
	SoundManager.playSound(SoundConstants.SOUND_BOOM);
	// Did tank die?
	final boolean dead = this.laserEnteredActionInnerP1(locX + dirX, locY + dirY, locZ, false);
	if (dead) {
	    // Kill tank
	    LaserTank.getApplication().getGameManager().gameOver();
	    return true;
	}
	// Destroy barrel
	ExplodingBarrel.laserEnteredActionInnerP2(locX, locY, locZ, this.getLayer());
	return true;
    }
}