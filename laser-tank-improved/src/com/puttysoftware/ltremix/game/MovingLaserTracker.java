/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.game;

import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.AbstractArena;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractTransientObject;
import com.puttysoftware.ltremix.arena.objects.AntiTankDisguise;
import com.puttysoftware.ltremix.arena.objects.BlueLaser;
import com.puttysoftware.ltremix.arena.objects.Disruptor;
import com.puttysoftware.ltremix.arena.objects.Empty;
import com.puttysoftware.ltremix.arena.objects.GreenLaser;
import com.puttysoftware.ltremix.arena.objects.Ground;
import com.puttysoftware.ltremix.arena.objects.Missile;
import com.puttysoftware.ltremix.arena.objects.PowerLaser;
import com.puttysoftware.ltremix.arena.objects.PowerfulTank;
import com.puttysoftware.ltremix.arena.objects.RedLaser;
import com.puttysoftware.ltremix.arena.objects.Stunner;
import com.puttysoftware.ltremix.arena.objects.Wall;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.ArenaConstants;
import com.puttysoftware.ltremix.utilities.DirectionConstants;
import com.puttysoftware.ltremix.utilities.DirectionResolver;
import com.puttysoftware.ltremix.utilities.LaserTypeConstants;
import com.puttysoftware.ltremix.utilities.TankInventory;
import com.puttysoftware.ltremix.utilities.TypeConstants;

final class MovingLaserTracker {
    // Fields
    private AbstractArenaObject shooter;
    private int ox, oy, lt;
    private boolean res;
    private boolean laser;
    private int cumX, cumY, incX, incY;
    private AbstractTransientObject l;

    // Constructors
    public MovingLaserTracker() {
	this.lt = 0;
    }

    boolean isTracking() {
	return this.laser;
    }

    boolean isChecking() {
	return this.res;
    }

    void activateLasers(final int zx, final int zy, final int zox, final int zoy, final int zlt,
	    final AbstractArenaObject zshooter) {
	final GameManager gm = LTRemix.getApplication().getGameManager();
	this.shooter = zshooter;
	this.ox = zox;
	this.oy = zoy;
	this.lt = zlt;
	this.cumX = zx;
	this.cumY = zy;
	this.incX = zx;
	this.incY = zy;
	if (this.lt == LaserTypeConstants.LASER_TYPE_GREEN) {
	    if (this.shooter instanceof PowerfulTank) {
		this.lt = LaserTypeConstants.LASER_TYPE_POWER;
		SoundManager.playSound(SoundConstants.SOUND_POWER_LASER);
	    } else if (this.shooter instanceof AntiTankDisguise) {
		this.lt = LaserTypeConstants.LASER_TYPE_RED;
		SoundManager.playSound(SoundConstants.SOUND_ANTI_FIRE);
	    } else {
		SoundManager.playSound(SoundConstants.SOUND_FIRE_LASER);
	    }
	    LTRemix.getApplication().getArenaManager().setDirty(true);
	    GameManager.updateUndo(true, false, false, false, false, false, false, false, false, false);
	    gm.updateScore(0, 1, 0);
	    if (!gm.isReplaying()) {
		gm.updateReplay(true, 0, 0);
	    }
	    this.laser = true;
	    this.res = true;
	} else if (this.lt == LaserTypeConstants.LASER_TYPE_RED) {
	    if (!gm.getCheatStatus(GameManager.CHEAT_INVINCIBLE)) {
		SoundManager.playSound(SoundConstants.SOUND_ANTI_FIRE);
		this.laser = true;
		this.res = true;
	    }
	} else if (this.lt == LaserTypeConstants.LASER_TYPE_MISSILE) {
	    LTRemix.getApplication().getArenaManager().setDirty(true);
	    GameManager.updateUndo(false, true, false, false, false, false, false, false, false, false);
	    TankInventory.fireMissile();
	    SoundManager.playSound(SoundConstants.SOUND_MISSILE);
	    gm.updateScore(0, 0, 1);
	    if (!gm.isReplaying()) {
		gm.updateReplay(true, 0, 0);
	    }
	    this.laser = true;
	    this.res = true;
	} else if (this.lt == LaserTypeConstants.LASER_TYPE_STUNNER) {
	    LTRemix.getApplication().getArenaManager().setDirty(true);
	    GameManager.updateUndo(false, false, true, false, false, false, false, false, false, false);
	    TankInventory.fireStunner();
	    SoundManager.playSound(SoundConstants.SOUND_STUNNER);
	    gm.updateScore(0, 0, 1);
	    if (!gm.isReplaying()) {
		gm.updateReplay(true, 0, 0);
	    }
	    this.laser = true;
	    this.res = true;
	} else if (this.lt == LaserTypeConstants.LASER_TYPE_BLUE) {
	    LTRemix.getApplication().getArenaManager().setDirty(true);
	    GameManager.updateUndo(false, false, false, false, false, true, false, false, false, false);
	    TankInventory.fireBlueLaser();
	    SoundManager.playSound(SoundConstants.SOUND_FIRE_LASER);
	    gm.updateScore(0, 0, 1);
	    if (!gm.isReplaying()) {
		gm.updateReplay(true, 0, 0);
	    }
	    this.laser = true;
	    this.res = true;
	} else if (this.lt == LaserTypeConstants.LASER_TYPE_DISRUPTOR) {
	    LTRemix.getApplication().getArenaManager().setDirty(true);
	    GameManager.updateUndo(false, false, false, false, false, false, true, false, false, false);
	    TankInventory.fireDisruptor();
	    SoundManager.playSound(SoundConstants.SOUND_DISRUPTOR);
	    gm.updateScore(0, 0, 1);
	    if (!gm.isReplaying()) {
		gm.updateReplay(true, 0, 0);
	    }
	    this.laser = true;
	    this.res = true;
	}
    }

    void trackPart1(final boolean tracking) {
	if (this.laser && this.res) {
	    this.doLasersOnce(tracking);
	}
    }

    boolean trackPart2(final int nsx, final int nsy, final boolean nMover) {
	final GameManager gm = LTRemix.getApplication().getGameManager();
	int sx = nsx;
	int sy = nsy;
	boolean mover = nMover;
	if (!this.res && this.laser) {
	    if (gm.getTank().getSavedObject().isOfType(TypeConstants.TYPE_MOVER)) {
		final int dir = gm.getTank().getSavedObject().getDirection();
		final int[] unres = DirectionResolver.unresolveRelativeDirection(dir);
		sx = unres[0];
		sy = unres[1];
		mover = true;
	    }
	    if (mover && !MovingLaserTracker.canMoveThere(sx, sy)) {
		MLOTask.activateAutomaticMovement(new int[] { sx, sy });
	    }
	    this.clearLastLaser();
	}
	return mover;
    }

    void clearLastLaser() {
	final GameManager gm = LTRemix.getApplication().getGameManager();
	final PlayerLocationManager plMgr = gm.getPlayerManager();
	final int pz = plMgr.getPlayerLocationZ();
	if (this.laser) {
	    // Clear last laser
	    try {
		LTRemix.getApplication().getArenaManager().getArena().setVirtualCell(new Empty(),
			this.ox + this.cumX - this.incX, this.oy + this.cumY - this.incY, pz, this.l.getPrimaryLayer());
		gm.redrawArena();
	    } catch (final ArrayIndexOutOfBoundsException | NullPointerException e) {
		// Ignore
	    }
	    gm.laserDone();
	    if (this.shooter.canShoot()) {
		this.shooter.laserDoneAction();
	    }
	    this.laser = false;
	}
    }

    private void doLasersOnce(final boolean tracking) {
	final Ground g = new Ground();
	final Application app = LTRemix.getApplication();
	final GameManager gm = app.getGameManager();
	final PlayerLocationManager plMgr = app.getGameManager().getPlayerManager();
	final int px = plMgr.getPlayerLocationX();
	final int py = plMgr.getPlayerLocationY();
	final int pz = plMgr.getPlayerLocationZ();
	final AbstractArena m = app.getArenaManager().getArena();
	AbstractArenaObject lol = null;
	AbstractArenaObject lou = null;
	try {
	    lol = m.getCell(this.ox + this.cumX, this.oy + this.cumY, pz, ArenaConstants.LAYER_LOWER_OBJECTS);
	    lou = m.getCell(this.ox + this.cumX, this.oy + this.cumY, pz, ArenaConstants.LAYER_UPPER_OBJECTS);
	} catch (final ArrayIndexOutOfBoundsException ae) {
	    this.res = false;
	    lol = g;
	    lou = g;
	}
	if (this.res) {
	    int dirX, dirY;
	    int[] resolved;
	    int laserDir;
	    this.l = MovingLaserTracker.createLaserForType(this.lt);
	    if (this.lt == LaserTypeConstants.LASER_TYPE_MISSILE) {
		final int suffix = DirectionResolver.resolveRelativeDirection(this.incX, this.incY);
		this.l.setDirection(suffix);
	    } else if (this.lt == LaserTypeConstants.LASER_TYPE_STUNNER
		    || this.lt == LaserTypeConstants.LASER_TYPE_DISRUPTOR) {
		// Do nothing
	    } else {
		final int suffix = DirectionResolver.resolveRelativeDirectionHV(this.incX, this.incY);
		this.l.setDirection(suffix);
	    }
	    final int oldincX = this.incX;
	    final int oldincY = this.incY;
	    try {
		if (lol.doLasersPassThrough() && lou.doLasersPassThrough()) {
		    m.setVirtualCell(this.l, this.ox + this.cumX, this.oy + this.cumY, pz, this.l.getPrimaryLayer());
		    gm.redrawArena();
		}
	    } catch (final ArrayIndexOutOfBoundsException aioobe) {
		// Ignore
	    }
	    try {
		m.setVirtualCell(new Empty(), this.ox + this.cumX - this.incX, this.oy + this.cumY - this.incY, pz,
			this.l.getPrimaryLayer());
		gm.redrawArena();
	    } catch (final ArrayIndexOutOfBoundsException aioobe) {
		// Ignore
	    }
	    final int oldLaserDir = this.l.getDirection();
	    laserDir = oldLaserDir;
	    final boolean laserKill = this.ox + this.cumX == px && this.oy + this.cumY == py;
	    if (laserKill) {
		gm.gameOver();
		return;
	    }
	    int dir = lou.laserEnteredAction(this.ox + this.cumX, this.oy + this.cumY, pz, this.incX, this.incY,
		    this.lt, this.l.getForceUnitsImbued());
	    if (dir != DirectionConstants.NONE) {
		dir = lol.laserEnteredAction(this.ox + this.cumX, this.oy + this.cumY, pz, this.incX, this.incY,
			this.lt, this.l.getForceUnitsImbued());
	    }
	    if (dir == DirectionConstants.NONE) {
		this.res = false;
		// Clear laser, because it died
		try {
		    m.setVirtualCell(new Empty(), this.ox + this.cumX, this.oy + this.cumY, pz,
			    this.l.getPrimaryLayer());
		    gm.redrawArena();
		} catch (final ArrayIndexOutOfBoundsException aioobe) {
		    // Ignore
		}
		return;
	    }
	    resolved = DirectionResolver.unresolveRelativeDirection(dir);
	    dirX = resolved[0];
	    dirY = resolved[1];
	    laserDir = DirectionResolver.resolveRelativeDirectionHV(dirX, dirY);
	    this.l.setDirection(laserDir);
	    this.incX = dirX;
	    this.incY = dirY;
	    dir = lou.laserExitedAction(oldincX, oldincY, pz, this.incX, this.incY, this.lt);
	    if (dir != DirectionConstants.NONE) {
		dir = lol.laserExitedAction(oldincX, oldincY, pz, this.incX, this.incY, this.lt);
	    }
	    if (dir == DirectionConstants.NONE) {
		this.res = false;
		// Clear laser, because it died
		try {
		    m.setVirtualCell(new Empty(), this.ox + this.cumX, this.oy + this.cumY, pz,
			    this.l.getPrimaryLayer());
		    gm.redrawArena();
		} catch (final ArrayIndexOutOfBoundsException aioobe) {
		    // Ignore
		}
		return;
	    }
	    resolved = DirectionResolver.unresolveRelativeDirection(dir);
	    dirX = resolved[0];
	    dirY = resolved[1];
	    laserDir = DirectionResolver.resolveRelativeDirectionHV(dirX, dirY);
	    this.l.setDirection(laserDir);
	    this.incX = dirX;
	    this.incY = dirY;
	    if (m.isHorizontalWraparoundEnabled()) {
		this.cumX = MovingLaserTracker.normalizeColumn(this.cumX + this.incX, AbstractArena.getMinColumns());
	    } else {
		this.cumX += this.incX;
	    }
	    if (m.isVerticalWraparoundEnabled()) {
		this.cumY = MovingLaserTracker.normalizeRow(this.cumY + this.incY, AbstractArena.getMinRows());
	    } else {
		this.cumY += this.incY;
	    }
	    if (oldLaserDir != laserDir && tracking) {
		try {
		    m.setVirtualCell(new Empty(), this.ox + this.cumX - this.incX, this.oy + this.cumY - this.incY, pz,
			    this.l.getPrimaryLayer());
		    gm.redrawArena();
		} catch (final ArrayIndexOutOfBoundsException aioobe) {
		    // Ignore
		}
		if (m.isHorizontalWraparoundEnabled()) {
		    this.cumX = MovingLaserTracker.normalizeColumn(this.cumX + this.incX,
			    AbstractArena.getMinColumns());
		} else {
		    this.cumX += this.incX;
		}
		if (m.isVerticalWraparoundEnabled()) {
		    this.cumY = MovingLaserTracker.normalizeRow(this.cumY + this.incY, AbstractArena.getMinRows());
		} else {
		    this.cumY += this.incY;
		}
	    }
	}
    }

    private static AbstractTransientObject createLaserForType(final int type) {
	switch (type) {
	case LaserTypeConstants.LASER_TYPE_GREEN:
	    return new GreenLaser();
	case LaserTypeConstants.LASER_TYPE_BLUE:
	    return new BlueLaser();
	case LaserTypeConstants.LASER_TYPE_RED:
	    return new RedLaser();
	case LaserTypeConstants.LASER_TYPE_MISSILE:
	    return new Missile();
	case LaserTypeConstants.LASER_TYPE_STUNNER:
	    return new Stunner();
	case LaserTypeConstants.LASER_TYPE_DISRUPTOR:
	    return new Disruptor();
	case LaserTypeConstants.LASER_TYPE_POWER:
	    return new PowerLaser();
	default:
	    return null;
	}
    }

    private static int normalizeRow(final int row, final int rows) {
	int fR = row;
	if (LTRemix.getApplication().getArenaManager().getArena().isVerticalWraparoundEnabled()) {
	    if (fR < 0) {
		fR += rows;
		while (fR < 0) {
		    fR += rows;
		}
	    } else if (fR > rows - 1) {
		fR -= rows;
		while (fR > rows - 1) {
		    fR -= rows;
		}
	    }
	}
	return fR;
    }

    private static int normalizeColumn(final int column, final int columns) {
	int fC = column;
	if (LTRemix.getApplication().getArenaManager().getArena().isHorizontalWraparoundEnabled()) {
	    if (fC < 0) {
		fC += columns;
		while (fC < 0) {
		    fC += columns;
		}
	    } else if (fC > columns - 1) {
		fC -= columns;
		while (fC > columns - 1) {
		    fC -= columns;
		}
	    }
	}
	return fC;
    }

    private static boolean canMoveThere(final int sx, final int sy) {
	final GameManager gm = LTRemix.getApplication().getGameManager();
	final PlayerLocationManager plMgr = gm.getPlayerManager();
	final int px = plMgr.getPlayerLocationX();
	final int py = plMgr.getPlayerLocationY();
	final int pz = plMgr.getPlayerLocationZ();
	final Application app = LTRemix.getApplication();
	final AbstractArena m = app.getArenaManager().getArena();
	boolean zproceed = true;
	AbstractArenaObject zo = null;
	try {
	    try {
		zo = m.getCell(px + sx, py + sy, pz, ArenaConstants.LAYER_LOWER_OBJECTS);
	    } catch (final ArrayIndexOutOfBoundsException ae) {
		zo = new Wall();
	    }
	} catch (final NullPointerException np) {
	    zproceed = false;
	    zo = new Wall();
	}
	if (zproceed) {
	    try {
		if (MovingLaserTracker.checkSolid(zo)) {
		    return true;
		}
	    } catch (final ArrayIndexOutOfBoundsException ae) {
		// Ignore
	    }
	}
	return false;
    }

    private static boolean checkSolid(final AbstractArenaObject next) {
	final GameManager gm = LTRemix.getApplication().getGameManager();
	// Check cheats
	if (gm.getCheatStatus(GameManager.CHEAT_GHOSTLY)) {
	    return true;
	} else {
	    final boolean nextSolid = next.isConditionallySolid();
	    if (nextSolid) {
		if (next.isOfType(TypeConstants.TYPE_CHARACTER)) {
		    return true;
		} else {
		    return false;
		}
	    } else {
		return true;
	    }
	}
    }
}
