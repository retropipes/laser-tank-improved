/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.ltremix.game.GameManager;
import com.puttysoftware.ltremix.game.MovingObjectTracker;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.ActionConstants;
import com.puttysoftware.ltremix.utilities.DirectionConstants;
import com.puttysoftware.ltremix.utilities.DirectionResolver;
import com.puttysoftware.ltremix.utilities.LaserTypeConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class AntiTank extends AbstractMovableObject {
    // Fields
    private boolean autoMove;
    private boolean canShoot;
    private MovingObjectTracker subscribed;

    // Constructors
    public AntiTank() {
	super(true);
	this.setDirection(DirectionConstants.NORTH);
	this.setFrameNumber(1);
	this.activateTimer(1);
	this.canShoot = true;
	this.autoMove = false;
	this.subscribed = null;
	this.type.set(TypeConstants.TYPE_ANTI);
    }

    public void subscribe(final MovingObjectTracker toWhat) {
	this.subscribed = toWhat;
    }

    public void unsubscribe() {
	this.subscribed = null;
    }

    public void kill(final int locX, final int locY) {
	if (this.canShoot) {
	    LTRemix.getApplication().getGameManager().setLaserType(LaserTypeConstants.LASER_TYPE_RED);
	    LTRemix.getApplication().getGameManager().fireLaser(locX, locY, this);
	    this.canShoot = false;
	}
    }

    @Override
    public boolean isDirectional() {
	return true;
    }

    @Override
    public boolean canShoot() {
	return true;
    }

    @Override
    public void laserDoneAction() {
	this.canShoot = true;
    }

    @Override
    public boolean acceptTick(final int actionType) {
	return actionType == ActionConstants.ACTION_MOVE;
    }

    @Override
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	final int baseDir = this.getDirection();
	if (laserType == LaserTypeConstants.LASER_TYPE_MISSILE || laserType == LaserTypeConstants.LASER_TYPE_POWER) {
	    // Kill
	    final GameManager gm = LTRemix.getApplication().getGameManager();
	    final DeadAntiTank dat = new DeadAntiTank();
	    dat.setSavedObject(this.getSavedObject());
	    dat.setDirection(baseDir);
	    gm.morph(dat, locX, locY, locZ, this.getPrimaryLayer());
	    if (this.subscribed != null) {
		this.subscribed.updateObject(dat, this);
	    }
	    SoundManager.playSound(SoundConstants.SOUND_ANTI_DIE);
	    return DirectionConstants.NONE;
	} else if (laserType == LaserTypeConstants.LASER_TYPE_STUNNER) {
	    // Stun
	    final GameManager gm = LTRemix.getApplication().getGameManager();
	    final StunnedAntiTank sat = new StunnedAntiTank();
	    sat.setSavedObject(this.getSavedObject());
	    sat.setDirection(baseDir);
	    gm.morph(sat, locX, locY, locZ, this.getPrimaryLayer());
	    SoundManager.playSound(SoundConstants.SOUND_STUN);
	    return DirectionConstants.NONE;
	} else {
	    final int sourceDir = DirectionResolver.resolveRelativeDirectionInvert(dirX, dirY);
	    if (sourceDir == baseDir) {
		// Kill
		final GameManager gm = LTRemix.getApplication().getGameManager();
		final DeadAntiTank dat = new DeadAntiTank();
		dat.setSavedObject(this.getSavedObject());
		dat.setDirection(baseDir);
		gm.morph(dat, locX, locY, locZ, this.getPrimaryLayer());
		if (this.subscribed != null) {
		    this.subscribed.updateObject(dat, this);
		}
		SoundManager.playSound(SoundConstants.SOUND_ANTI_DIE);
		return DirectionConstants.NONE;
	    } else {
		return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	    }
	}
    }

    @Override
    public void timerExpiredAction(final int locX, final int locY) {
	if (this.getSavedObject().isOfType(TypeConstants.TYPE_ANTI_MOVER)) {
	    final int moveDir = this.getSavedObject().getDirection();
	    final int[] unres = DirectionResolver.unresolveRelativeDirection(moveDir);
	    if (GameManager.canObjectMove(locX, locY, unres[0], unres[1])) {
		if (this.autoMove) {
		    this.autoMove = false;
		    LTRemix.getApplication().getGameManager().updatePushedPosition(locX, locY, locX + unres[0],
			    locY + unres[1], this);
		}
	    } else {
		this.autoMove = true;
	    }
	}
	this.activateTimer(1);
    }

    @Override
    public final int getStringBaseID() {
	return 0;
    }

    @Override
    public void playSoundHook() {
	SoundManager.playSound(SoundConstants.SOUND_PUSH_ANTI_TANK);
    }
}
