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

public class Barrel extends AbstractReactionWall {
    // Constructors
    public Barrel() {
	super();
	this.type.set(TypeConstants.TYPE_BARREL);
	this.setMaterial(MaterialConstants.MATERIAL_WOODEN);
    }

    @Override
    public final int getStringBaseID() {
	return 3;
    }

    @Override
    public Direction laserEnteredActionHook(final int locX, final int locY, final int locZ, final int dirX,
	    final int dirY, final int laserType, final int forceUnits) {
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

    @Override
    public void pushCollideAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	// React to balls hitting barrels
	if (pushed.isOfType(TypeConstants.TYPE_BALL)) {
	    this.laserEnteredAction(x, y, z, 0, 0, LaserTypeConstants.LASER_TYPE_GREEN, 1);
	}
    }

    @Override
    public boolean rangeActionHook(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int rangeType, final int forceUnits) {
	final AbstractArena a = LaserTank.getApplication().getArenaManager().getArena();
	// Boom!
	SoundManager.playSound(SoundConstants.SOUND_BARREL);
	// Check for tank in range of explosion
	final boolean target = a.circularScanTank(locX + dirX, locY + dirY, locZ, 1);
	if (target) {
	    // Kill tank
	    LaserTank.getApplication().getGameManager().gameOver();
	    return true;
	}
	// Destroy barrel
	LaserTank.getApplication().getGameManager().morph(new Empty(), locX + dirX, locY + dirY, locZ, this.getLayer());
	return true;
    }
}