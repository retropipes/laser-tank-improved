/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractReactionWall;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.DirectionResolver;
import com.puttysoftware.lasertank.utilities.LaserTypeConstants;
import com.puttysoftware.lasertank.utilities.MaterialConstants;

public class MetallicBricks extends AbstractReactionWall {
    // Constructors
    public MetallicBricks() {
	super();
	this.setMaterial(MaterialConstants.MATERIAL_METALLIC);
    }

    @Override
    public final int getStringBaseID() {
	return 64;
    }

    @Override
    public Direction laserEnteredActionHook(final int locX, final int locY, final int locZ, final int dirX,
	    final int dirY, final int laserType, final int forceUnits) {
	SoundManager.playSound(SoundConstants.SOUND_BREAK_BRICKS);
	LaserTank.getApplication().getGameManager().morph(new Empty(), locX, locY, locZ, this.getLayer());
	if (laserType == LaserTypeConstants.LASER_TYPE_POWER) {
	    // Laser keeps going
	    return DirectionResolver.resolveRelative(dirX, dirY);
	} else {
	    // Laser stops
	    return Direction.NONE;
	}
    }

    @Override
    public boolean rangeActionHook(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int rangeType, final int forceUnits) {
	SoundManager.playSound(SoundConstants.SOUND_BREAK_BRICKS);
	LaserTank.getApplication().getGameManager().morph(new Empty(), locX + dirX, locY + dirY, locZ, this.getLayer());
	return true;
    }
}