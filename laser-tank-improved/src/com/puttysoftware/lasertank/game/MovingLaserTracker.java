/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.game;

import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractTransientObject;
import com.puttysoftware.lasertank.arena.objects.AntiTankDisguise;
import com.puttysoftware.lasertank.arena.objects.BlueLaser;
import com.puttysoftware.lasertank.arena.objects.Disruptor;
import com.puttysoftware.lasertank.arena.objects.Empty;
import com.puttysoftware.lasertank.arena.objects.GreenLaser;
import com.puttysoftware.lasertank.arena.objects.Ground;
import com.puttysoftware.lasertank.arena.objects.Missile;
import com.puttysoftware.lasertank.arena.objects.PowerLaser;
import com.puttysoftware.lasertank.arena.objects.PowerfulTank;
import com.puttysoftware.lasertank.arena.objects.RedLaser;
import com.puttysoftware.lasertank.arena.objects.Stunner;
import com.puttysoftware.lasertank.arena.objects.Wall;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.DirectionResolver;
import com.puttysoftware.lasertank.utilities.LaserTypeConstants;
import com.puttysoftware.lasertank.utilities.TankInventory;
import com.puttysoftware.lasertank.utilities.TypeConstants;

final class MovingLaserTracker {
    private static boolean canMoveThere(final int sx, final int sy) {
	final GameManager gm = LaserTank.getApplication().getGameManager();
	final PlayerLocationManager plMgr = gm.getPlayerManager();
	final int px = plMgr.getPlayerLocationX();
	final int py = plMgr.getPlayerLocationY();
	final int pz = plMgr.getPlayerLocationZ();
	final Application app = LaserTank.getApplication();
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
	final GameManager gm = LaserTank.getApplication().getGameManager();
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

    private static int normalizeColumn(final int column, final int columns) {
	int fC = column;
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
	return fC;
    }

    private static int normalizeRow(final int row, final int rows) {
	int fR = row;
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
	return fR;
    }

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

    void activateLasers(final int zx, final int zy, final int zox, final int zoy, final int zlt,
	    final AbstractArenaObject zshooter) {
	final GameManager gm = LaserTank.getApplication().getGameManager();
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
	    LaserTank.getApplication().getArenaManager().setDirty(true);
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
	    LaserTank.getApplication().getArenaManager().setDirty(true);
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
	    LaserTank.getApplication().getArenaManager().setDirty(true);
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
	    LaserTank.getApplication().getArenaManager().setDirty(true);
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
	    LaserTank.getApplication().getArenaManager().setDirty(true);
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

    void clearLastLaser() {
	final GameManager gm = LaserTank.getApplication().getGameManager();
	final PlayerLocationManager plMgr = gm.getPlayerManager();
	final int pz = plMgr.getPlayerLocationZ();
	if (this.laser) {
	    // Clear last laser
	    try {
		LaserTank.getApplication().getArenaManager().getArena().setVirtualCell(new Empty(),
			this.ox + this.cumX - this.incX, this.oy + this.cumY - this.incY, pz, this.l.getLayer());
		gm.redrawArena();
	    } catch (final ArrayIndexOutOfBoundsException aioobe) {
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
	final Application app = LaserTank.getApplication();
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
	    int[] resolved;
	    Direction laserDir;
	    this.l = MovingLaserTracker.createLaserForType(this.lt);
	    if (this.lt == LaserTypeConstants.LASER_TYPE_MISSILE) {
		final Direction suffix = DirectionResolver.resolveRelative(this.incX, this.incY);
		this.l.setDirection(suffix);
	    } else if (this.lt == LaserTypeConstants.LASER_TYPE_STUNNER
		    || this.lt == LaserTypeConstants.LASER_TYPE_DISRUPTOR) {
		// Do nothing
	    } else {
		final Direction suffix = DirectionResolver.resolveRelativeHV(this.incX, this.incY);
		this.l.setDirection(suffix);
	    }
	    final int oldincX = this.incX;
	    final int oldincY = this.incY;
	    try {
		if (lol.doLasersPassThrough() && lou.doLasersPassThrough()) {
		    m.setVirtualCell(this.l, this.ox + this.cumX, this.oy + this.cumY, pz, this.l.getLayer());
		}
	    } catch (final ArrayIndexOutOfBoundsException aioobe) {
		// Ignore
	    }
	    try {
		m.setVirtualCell(new Empty(), this.ox + this.cumX - this.incX, this.oy + this.cumY - this.incY, pz,
			this.l.getLayer());
	    } catch (final ArrayIndexOutOfBoundsException aioobe) {
		// Ignore
	    }
	    final Direction oldLaserDir = this.l.getDirection();
	    laserDir = oldLaserDir;
	    final boolean laserKill = this.ox + this.cumX == px && this.oy + this.cumY == py;
	    if (laserKill) {
		gm.gameOver();
		return;
	    }
	    Direction dir = lou.laserEnteredAction(this.ox + this.cumX, this.oy + this.cumY, pz, this.incX, this.incY,
		    this.lt, this.l.getForceUnitsImbued());
	    if (dir != Direction.NONE) {
		dir = lol.laserEnteredAction(this.ox + this.cumX, this.oy + this.cumY, pz, this.incX, this.incY,
			this.lt, this.l.getForceUnitsImbued());
	    }
	    if (dir == Direction.NONE) {
		this.res = false;
		// Clear laser, because it died
		try {
		    m.setVirtualCell(new Empty(), this.ox + this.cumX, this.oy + this.cumY, pz, this.l.getLayer());
		} catch (final ArrayIndexOutOfBoundsException aioobe) {
		    // Ignore
		}
		return;
	    }
	    resolved = DirectionResolver.unresolveRelative(dir);
	    int resX = resolved[0];
	    int resY = resolved[1];
	    laserDir = DirectionResolver.resolveRelativeHV(resX, resY);
	    this.l.setDirection(laserDir);
	    this.incX = resX;
	    this.incY = resY;
	    dir = lou.laserExitedAction(oldincX, oldincY, pz, this.incX, this.incY, this.lt);
	    if (dir != Direction.NONE) {
		dir = lol.laserExitedAction(oldincX, oldincY, pz, this.incX, this.incY, this.lt);
	    }
	    if (dir == Direction.NONE) {
		this.res = false;
		// Clear laser, because it died
		try {
		    m.setVirtualCell(new Empty(), this.ox + this.cumX, this.oy + this.cumY, pz, this.l.getLayer());
		} catch (final ArrayIndexOutOfBoundsException aioobe) {
		    // Ignore
		}
		return;
	    }
	    resolved = DirectionResolver.unresolveRelative(dir);
	    resX = resolved[0];
	    resY = resolved[1];
	    laserDir = DirectionResolver.resolveRelativeHV(resX, resY);
	    this.l.setDirection(laserDir);
	    this.incX = resX;
	    this.incY = resY;
	    if (m.isVerticalWraparoundEnabled()) {
		this.cumX = MovingLaserTracker.normalizeColumn(this.cumX + this.incX, AbstractArena.getMinColumns());
	    } else {
		this.cumX += this.incX;
	    }
	    if (m.isHorizontalWraparoundEnabled()) {
		this.cumY = MovingLaserTracker.normalizeRow(this.cumY + this.incY, AbstractArena.getMinRows());
	    } else {
		this.cumY += this.incY;
	    }
	    if (oldLaserDir != laserDir && tracking) {
		try {
		    m.setVirtualCell(new Empty(), this.ox + this.cumX - this.incX, this.oy + this.cumY - this.incY, pz,
			    this.l.getLayer());
		} catch (final ArrayIndexOutOfBoundsException aioobe) {
		    // Ignore
		}
		if (m.isVerticalWraparoundEnabled()) {
		    this.cumX = MovingLaserTracker.normalizeColumn(this.cumX + this.incX,
			    AbstractArena.getMinColumns());
		} else {
		    this.cumX += this.incX;
		}
		if (m.isHorizontalWraparoundEnabled()) {
		    this.cumY = MovingLaserTracker.normalizeColumn(this.cumY + this.incY,
			    AbstractArena.getMinColumns());
		} else {
		    this.cumY += this.incY;
		}
	    }
	}
	gm.redrawArena();
    }

    boolean isChecking() {
	return this.res;
    }

    boolean isTracking() {
	return this.laser;
    }

    void trackPart1(final boolean tracking) {
	if (this.laser && this.res) {
	    this.doLasersOnce(tracking);
	}
    }

    boolean trackPart2(final int nsx, final int nsy, final boolean nMover) {
	final GameManager gm = LaserTank.getApplication().getGameManager();
	int sx = nsx;
	int sy = nsy;
	boolean mover = nMover;
	if (!this.res && this.laser) {
	    if (gm.getTank().getSavedObject().isOfType(TypeConstants.TYPE_MOVER)) {
		final Direction dir = gm.getTank().getSavedObject().getDirection();
		final int[] unres = DirectionResolver.unresolveRelative(dir);
		sx = unres[0];
		sy = unres[1];
		mover = true;
	    }
	    if (mover && !MovingLaserTracker.canMoveThere(sx, sy)) {
		MLOTask.activateAutomaticMovement();
	    }
	    this.clearLastLaser();
	}
	return mover;
    }
}
