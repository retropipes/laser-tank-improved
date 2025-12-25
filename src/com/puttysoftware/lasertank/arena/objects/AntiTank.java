/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.lasertank.game.GameManager;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.ActionConstants;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.DirectionResolver;
import com.puttysoftware.lasertank.utilities.LaserTypeConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public class AntiTank extends AbstractMovableObject {
    // Fields
    private boolean autoMove;
    private boolean canShoot;

    // Constructors
    public AntiTank() {
	super(true);
	this.setDirection(Direction.NORTH);
	this.setFrameNumber(1);
	this.activateTimer(1);
	this.canShoot = true;
	this.autoMove = false;
	this.type.set(TypeConstants.TYPE_ANTI);
    }

    @Override
    public boolean acceptTick(final int actionType) {
	return actionType == ActionConstants.ACTION_MOVE;
    }

    @Override
    public boolean canShoot() {
	return true;
    }

    @Override
    public final int getStringBaseID() {
	return 0;
    }

    public void kill(final int locX, final int locY) {
	if (this.canShoot) {
	    LaserTank.getApplication().getGameManager().setLaserType(LaserTypeConstants.LASER_TYPE_RED);
	    LaserTank.getApplication().getGameManager().fireLaser(locX, locY, this);
	    this.canShoot = false;
	}
    }

    @Override
    public void laserDoneAction() {
	this.canShoot = true;
    }

    @Override
    public Direction laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	final Direction baseDir = this.getDirection();
	if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE || laserType == LaserTypeConstants.LASER_TYPE_POWER) {
	    // Kill
	    final GameManager gm = LaserTank.getApplication().getGameManager();
	    final DeadAntiTank dat = new DeadAntiTank();
	    dat.setSavedObject(this.getSavedObject());
	    dat.setDirection(baseDir);
	    gm.morph(dat, locX, locY, locZ, this.getLayer());
	    SoundManager.playSound(SoundConstants.SOUND_ANTI_DIE);
	    return Direction.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_STUNNER) {
	    // Stun
	    final GameManager gm = LaserTank.getApplication().getGameManager();
	    final StunnedAntiTank sat = new StunnedAntiTank();
	    sat.setSavedObject(this.getSavedObject());
	    sat.setDirection(baseDir);
	    gm.morph(sat, locX, locY, locZ, this.getLayer());
	    SoundManager.playSound(SoundConstants.SOUND_STUN);
	    return Direction.NONE;
	} else {
	    final Direction sourceDir = DirectionResolver.resolveRelativeInvert(dirX, dirY);
	    if (sourceDir == baseDir) {
		// Kill
		final GameManager gm = LaserTank.getApplication().getGameManager();
		final DeadAntiTank dat = new DeadAntiTank();
		dat.setSavedObject(this.getSavedObject());
		dat.setDirection(baseDir);
		gm.morph(dat, locX, locY, locZ, this.getLayer());
		SoundManager.playSound(SoundConstants.SOUND_ANTI_DIE);
		return Direction.NONE;
	    } else {
		return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	    }
	}
    }

    @Override
    public void playSoundHook() {
	SoundManager.playSound(SoundConstants.SOUND_PUSH_ANTI_TANK);
    }

    @Override
    public void timerExpiredAction(final int locX, final int locY) {
	if (this.getSavedObject().isOfType(TypeConstants.TYPE_ANTI_MOVER)) {
	    final Direction moveDir = this.getSavedObject().getDirection();
	    final int[] unres = DirectionResolver.unresolveRelative(moveDir);
	    if (GameManager.canObjectMove(locX, locY, unres[0], unres[1])) {
		if (this.autoMove) {
		    this.autoMove = false;
		    LaserTank.getApplication().getGameManager().updatePushedPosition(locX, locY, locX + unres[0],
			    locY + unres[1], this);
		}
	    } else {
		this.autoMove = true;
	    }
	}
	this.activateTimer(1);
    }
}
