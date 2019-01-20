/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractReactionDisruptedObject;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.DirectionResolver;
import com.puttysoftware.lasertank.utilities.LaserTypeConstants;
import com.puttysoftware.lasertank.utilities.MaterialConstants;
import com.puttysoftware.lasertank.utilities.RangeTypeConstants;

public class DisruptedHotCrystalBlock extends AbstractReactionDisruptedObject {
    private static final int DISRUPTION_START = 20;
    // Fields
    private int disruptionLeft;

    // Constructors
    public DisruptedHotCrystalBlock() {
	super();
	this.disruptionLeft = DisruptedHotCrystalBlock.DISRUPTION_START;
	this.activateTimer(1);
	this.setMaterial(MaterialConstants.MATERIAL_FIRE);
    }

    @Override
    public boolean doLasersPassThrough() {
	return true;
    }

    @Override
    public final int getStringBaseID() {
	return 128;
    }

    @Override
    public Direction laserEnteredActionHook(final int locX, final int locY, final int locZ, final int dirX,
	    final int dirY, final int laserType, final int forceUnits) {
	if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE) {
	    // Destroy disrupted hot crystal block
	    SoundManager.playSound(SoundConstants.SOUND_BOOM);
	    LaserTank.getApplication().getGameManager().morph(new Empty(), locX, locY, locZ, this.getLayer());
	    return Direction.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_BLUE) {
	    // Reflect laser
	    return DirectionResolver.resolveRelativeInvert(dirX, dirY);
	} else {
	    // Pass laser through
	    return DirectionResolver.resolveRelative(dirX, dirY);
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
	if (RangeTypeConstants.getMaterialForRangeType(rangeType) == MaterialConstants.MATERIAL_METALLIC) {
	    // Destroy disrupted hot crystal block
	    LaserTank.getApplication().getGameManager().morph(new Empty(), locX + dirX, locY + dirY, locZ,
		    this.getLayer());
	    return true;
	} else {
	    // Do nothing
	    return true;
	}
    }

    @Override
    public void timerExpiredAction(final int locX, final int locY) {
	this.disruptionLeft--;
	if (this.disruptionLeft == 0) {
	    SoundManager.playSound(SoundConstants.SOUND_DISRUPT_END);
	    final int z = LaserTank.getApplication().getGameManager().getPlayerManager().getPlayerLocationZ();
	    LaserTank.getApplication().getGameManager().morph(new HotCrystalBlock(), locX, locY, z, this.getLayer());
	} else {
	    this.activateTimer(1);
	}
    }
}