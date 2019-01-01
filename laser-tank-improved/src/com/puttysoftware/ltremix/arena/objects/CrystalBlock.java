/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractReactionWall;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.DirectionConstants;
import com.puttysoftware.ltremix.utilities.DirectionResolver;
import com.puttysoftware.ltremix.utilities.LaserTypeConstants;
import com.puttysoftware.ltremix.utilities.MaterialConstants;
import com.puttysoftware.ltremix.utilities.RangeTypeConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class CrystalBlock extends AbstractReactionWall {
    // Constructors
    public CrystalBlock() {
	super();
	this.type.set(TypeConstants.TYPE_PLAIN_WALL);
    }

    @Override
    public int laserEnteredActionHook(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
	    // Destroy crystal block
	    SoundManager.playSound(SoundConstants.SOUND_BOOM);
	    LTRemix.getApplication().getGameManager().morph(new Empty(), locX, locY, locZ, this.getPrimaryLayer());
	    return DirectionConstants.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_BLUE) {
	    // Reflect laser
	    return DirectionResolver.resolveRelativeDirectionInvert(dirX, dirY);
	} else if (laserType == LaserTypeConstants.LASER_TYPE_DISRUPTOR) {
	    // Disrupt crystal block
	    SoundManager.playSound(SoundConstants.SOUND_DISRUPTED);
	    LTRemix.getApplication().getGameManager().morph(new DisruptedCrystalBlock(), locX, locY, locZ,
		    this.getPrimaryLayer());
	    return DirectionConstants.NONE;
	} else {
	    // Pass laser through
	    return DirectionResolver.resolveRelativeDirection(dirX, dirY);
	}
    }

    @Override
    public int laserExitedAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType) {
	return DirectionResolver.resolveRelativeDirection(dirX, dirY);
    }

    @Override
    public boolean rangeActionHook(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int rangeType, final int forceUnits) {
	if (RangeTypeConstants.getMaterialForRangeType(rangeType) == MaterialConstants.MATERIAL_METALLIC) {
	    // Destroy crystal block
	    LTRemix.getApplication().getGameManager().morph(new Empty(), locX + dirX, locY + dirY, locZ,
		    this.getPrimaryLayer());
	    return true;
	} else if (RangeTypeConstants.getMaterialForRangeType(rangeType) == MaterialConstants.MATERIAL_FIRE) {
	    // Heat up crystal block
	    SoundManager.playSound(SoundConstants.SOUND_MELT);
	    LTRemix.getApplication().getGameManager().morph(this.changesToOnExposure(MaterialConstants.MATERIAL_FIRE),
		    locX + dirX, locY + dirY, locZ, this.getPrimaryLayer());
	    return true;
	} else if (RangeTypeConstants.getMaterialForRangeType(rangeType) == MaterialConstants.MATERIAL_ICE) {
	    // Freeze crystal block
	    SoundManager.playSound(SoundConstants.SOUND_FROZEN);
	    LTRemix.getApplication().getGameManager().morph(this.changesToOnExposure(MaterialConstants.MATERIAL_ICE),
		    locX + dirX, locY + dirY, locZ, this.getPrimaryLayer());
	    return true;
	} else {
	    // Do nothing
	    return true;
	}
    }

    @Override
    public boolean doLasersPassThrough() {
	return true;
    }

    @Override
    public final int getStringBaseID() {
	return 10;
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
}