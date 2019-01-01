/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.lasertank.utilities.AlreadyDeadException;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.AbstractArena;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractReactionWall;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.DirectionConstants;
import com.puttysoftware.ltremix.utilities.DirectionResolver;
import com.puttysoftware.ltremix.utilities.LaserTypeConstants;
import com.puttysoftware.ltremix.utilities.MaterialConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class Barrel extends AbstractReactionWall {
    // Constructors
    public Barrel() {
	super();
	this.type.set(TypeConstants.TYPE_BARREL);
	this.setMaterial(MaterialConstants.MATERIAL_WOODEN);
    }

    @Override
    public int laserEnteredActionHook(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	try {
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
	} catch (final AlreadyDeadException ade) {
	    // Ignore
	}
	if (laserType == LaserTypeConstants.LASER_TYPE_POWER) {
	    // Laser keeps going
	    return DirectionResolver.resolveRelativeDirection(dirX, dirY);
	} else {
	    // Laser stops
	    return DirectionConstants.NONE;
	}
    }

    @Override
    public boolean rangeActionHook(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int rangeType, final int forceUnits) {
	try {
	    final AbstractArena a = LTRemix.getApplication().getArenaManager().getArena();
	    // Boom!
	    SoundManager.playSound(SoundConstants.SOUND_BARREL);
	    // Check for tank in range of explosion
	    final boolean target = a.circularScanTank(locX + dirX, locY + dirY, locZ, 1);
	    if (target) {
		// Kill tank
		LTRemix.getApplication().getGameManager().gameOver();
		return true;
	    }
	    // Destroy barrel
	    LTRemix.getApplication().getGameManager().morph(new Empty(), locX + dirX, locY + dirY, locZ,
		    this.getPrimaryLayer());
	} catch (final AlreadyDeadException ade) {
	    // Ignore
	}
	return true;
    }

    @Override
    public void pushCollideAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	// React to balls hitting barrels
	if (pushed.isOfType(TypeConstants.TYPE_BALL)) {
	    this.laserEnteredAction(x, y, z, 0, 0, LaserTypeConstants.LASER_TYPE_GREEN, 1);
	}
    }

    @Override
    public final int getStringBaseID() {
	return 3;
    }
}