/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractReactionWall;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.DirectionResolver;
import com.puttysoftware.lasertank.utilities.LaserTypeConstants;
import com.puttysoftware.lasertank.utilities.MaterialConstants;
import com.puttysoftware.lasertank.utilities.RangeTypeConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public class MirrorCrystalBlock extends AbstractReactionWall {
    // Constructors
    public MirrorCrystalBlock() {
	super();
	this.type.set(TypeConstants.TYPE_PLAIN_WALL);
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_ICE:
	    final IcyCrystalBlock icb = new IcyCrystalBlock();
	    icb.setPreviousState(this);
	    return icb;
	case MaterialConstants.MATERIAL_FIRE:
	    return new HotCrystalBlock();
	default:
	    return this;
	}
    }

    @Override
    public boolean doLasersPassThrough() {
	return true;
    }

    @Override
    public final int getStringBaseID() {
	return 26;
    }

    @Override
    public Direction laserEnteredActionHook(final int locX, final int locY, final int locZ, final int dirX,
	    final int dirY, final int laserType, final int forceUnits) {
	if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
	    // Destroy mirror crystal block
	    SoundManager.playSound(SoundConstants.SOUND_BOOM);
	    LaserTank.getApplication().getGameManager().morph(new Empty(), locX, locY, locZ, this.getLayer());
	    return Direction.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_BLUE) {
	    // Pass laser through
	    return DirectionResolver.resolveRelative(dirX, dirY);
	} else if (laserType == LaserTypeConstants.LASER_TYPE_DISRUPTOR) {
	    // Disrupt mirror crystal block
	    SoundManager.playSound(SoundConstants.SOUND_DISRUPTED);
	    LaserTank.getApplication().getGameManager().morph(new DisruptedMirrorCrystalBlock(), locX, locY, locZ,
		    this.getLayer());
	    return Direction.NONE;
	} else {
	    // Reflect laser
	    return DirectionResolver.resolveRelativeInvert(dirX, dirY);
	}
    }

    @Override
    public Direction laserExitedAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType) {
	return DirectionResolver.resolveRelative(dirX, dirY);
    }

    @Override
    public boolean rangeActionHook(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int rangeType, final int forceUnits) {
	if (rangeType == RangeTypeConstants.RANGE_TYPE_BOMB
		|| RangeTypeConstants.getMaterialForRangeType(rangeType) == MaterialConstants.MATERIAL_METALLIC) {
	    // Destroy mirror crystal block
	    LaserTank.getApplication().getGameManager().morph(new Empty(), locX + dirX, locY + dirY, locZ,
		    this.getLayer());
	    return true;
	} else if (RangeTypeConstants.getMaterialForRangeType(rangeType) == MaterialConstants.MATERIAL_FIRE) {
	    // Heat up mirror crystal block
	    SoundManager.playSound(SoundConstants.SOUND_MELT);
	    LaserTank.getApplication().getGameManager().morph(this.changesToOnExposure(MaterialConstants.MATERIAL_FIRE),
		    locX + dirX, locY + dirY, locZ, this.getLayer());
	    return true;
	} else if (RangeTypeConstants.getMaterialForRangeType(rangeType) == MaterialConstants.MATERIAL_ICE) {
	    // Freeze mirror crystal block
	    SoundManager.playSound(SoundConstants.SOUND_FROZEN);
	    LaserTank.getApplication().getGameManager().morph(this.changesToOnExposure(MaterialConstants.MATERIAL_ICE),
		    locX + dirX, locY + dirY, locZ, this.getLayer());
	    return true;
	} else {
	    // Do nothing
	    return true;
	}
    }
}