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
import com.puttysoftware.ltremix.utilities.MaterialConstants;

public class MetallicBricks extends AbstractReactionWall {
    // Constructors
    public MetallicBricks() {
	super();
	this.setMaterial(MaterialConstants.MATERIAL_METALLIC);
    }

    @Override
    public int laserEnteredActionHook(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	SoundManager.playSound(SoundConstants.SOUND_BREAK_BRICKS);
	LTRemix.getApplication().getGameManager().morph(new Empty(), locX, locY, locZ, this.getPrimaryLayer());
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
	SoundManager.playSound(SoundConstants.SOUND_BREAK_BRICKS);
	LTRemix.getApplication().getGameManager().morph(new Empty(), locX + dirX, locY + dirY, locZ,
		this.getPrimaryLayer());
	return true;
    }

    @Override
    public final int getStringBaseID() {
	return 64;
    }
}