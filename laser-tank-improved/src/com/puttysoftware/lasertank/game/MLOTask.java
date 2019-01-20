/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.game;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractCharacter;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.lasertank.arena.current.CurrentArenaData;
import com.puttysoftware.lasertank.arena.objects.FrozenTank;
import com.puttysoftware.lasertank.arena.objects.Ground;
import com.puttysoftware.lasertank.arena.objects.Tank;
import com.puttysoftware.lasertank.arena.objects.Wall;
import com.puttysoftware.lasertank.prefs.PreferencesManager;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.strings.global.UntranslatedString;
import com.puttysoftware.lasertank.utilities.ActionConstants;
import com.puttysoftware.lasertank.utilities.AlreadyDeadException;
import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.DirectionResolver;
import com.puttysoftware.lasertank.utilities.TypeConstants;

final class MLOTask extends Thread {
    static void activateAutomaticMovement() {
	LaserTank.getApplication().getGameManager().scheduleAutoMove();
    }

    private static boolean checkSolid(final AbstractArenaObject next) {
	final GameManager gm = LaserTank.getApplication().getGameManager();
	// Check cheats
	if (gm.getCheatStatus(GameManager.CHEAT_GHOSTLY)) {
	    return true;
	} else {
	    return !next.isConditionallySolid();
	}
    }

    static boolean checkSolid(final int zx, final int zy) {
	final GameManager gm = LaserTank.getApplication().getGameManager();
	final AbstractArenaObject next = LaserTank.getApplication().getArenaManager().getArena().getCell(zx, zy,
		gm.getPlayerManager().getPlayerLocationZ(), ArenaConstants.LAYER_LOWER_OBJECTS);
	// Check cheats
	if (gm.getCheatStatus(GameManager.CHEAT_GHOSTLY)) {
	    return true;
	} else {
	    return !next.isConditionallySolid();
	}
    }

    private static void freezeTank() {
	final GameManager gm = LaserTank.getApplication().getGameManager();
	final AbstractCharacter tank = gm.getTank();
	final Direction dir = tank.getDirection();
	final int px = gm.getPlayerManager().getPlayerLocationX();
	final int py = gm.getPlayerManager().getPlayerLocationY();
	final int pz = gm.getPlayerManager().getPlayerLocationZ();
	final FrozenTank ft = new FrozenTank(dir, tank.getNumber());
	ft.setSavedObject(tank.getSavedObject());
	gm.morph(ft, px, py, pz, ft.getLayer());
	gm.updateTank();
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
    private int sx, sy;
    private boolean mover;
    private boolean move;
    private boolean proceed;
    private boolean abort;
    private boolean frozen;
    private boolean magnet;
    private boolean loopCheck;
    private final ArrayList<MovingLaserTracker> laserTrackers;
    private final ArrayList<MovingObjectTracker> objectTrackers;

    // Constructors
    public MLOTask() {
	this.setName(GlobalLoader.loadUntranslated(UntranslatedString.MLOH_NAME));
	this.setPriority(Thread.MIN_PRIORITY);
	this.abort = false;
	this.laserTrackers = new ArrayList<>();
	this.objectTrackers = new ArrayList<>();
	this.frozen = false;
	this.magnet = false;
    }

    void abortLoop() {
	this.abort = true;
    }

    void activateFrozenMovement(final int zx, final int zy) {
	final GameManager gm = LaserTank.getApplication().getGameManager();
	// Moving under the influence of a Frost Field
	this.frozen = true;
	MLOTask.freezeTank();
	gm.updateScore(1, 0, 0);
	this.sx = zx;
	this.sy = zy;
	GameManager.updateUndo(false, false, false, false, false, false, false, false, false, false);
	this.move = true;
	if (!gm.isReplaying()) {
	    gm.updateReplay(false, zx, zy);
	}
    }

    void activateLasers(final int zx, final int zy, final int zox, final int zoy, final int zlt,
	    final AbstractArenaObject zshooter) {
	final MovingLaserTracker tracker = new MovingLaserTracker();
	tracker.activateLasers(zx, zy, zox, zoy, zlt, zshooter);
	this.laserTrackers.add(tracker);
    }

    void activateMovement(final int zx, final int zy) {
	final GameManager gm = LaserTank.getApplication().getGameManager();
	if (zx == 2 || zy == 2 || zx == -2 || zy == -2) {
	    // Boosting
	    SoundManager.playSound(SoundConstants.SOUND_BOOST);
	    gm.updateScore(0, 0, 1);
	    this.sx = zx;
	    this.sy = zy;
	    this.magnet = false;
	    GameManager.updateUndo(false, false, false, true, false, false, false, false, false, false);
	} else if (zx == 3 || zy == 3 || zx == -3 || zy == -3) {
	    // Using a Magnet
	    gm.updateScore(0, 0, 1);
	    final AbstractArena a = LaserTank.getApplication().getArenaManager().getArena();
	    final int px = gm.getPlayerManager().getPlayerLocationX();
	    final int py = gm.getPlayerManager().getPlayerLocationY();
	    final int pz = gm.getPlayerManager().getPlayerLocationZ();
	    if (zx == 3) {
		this.sx = a.checkForMagnetic(pz, px, py, Direction.EAST);
		this.sy = 0;
	    } else if (zx == -3) {
		this.sx = -a.checkForMagnetic(pz, px, py, Direction.WEST);
		this.sy = 0;
	    }
	    if (zy == 3) {
		this.sx = 0;
		this.sy = a.checkForMagnetic(pz, px, py, Direction.SOUTH);
	    } else if (zy == -3) {
		this.sx = 0;
		this.sy = -a.checkForMagnetic(pz, px, py, Direction.NORTH);
	    }
	    this.magnet = true;
	    if (this.sx == 0 && this.sy == 0) {
		// Failure
		SoundManager.playSound(SoundConstants.SOUND_BUMP_HEAD);
	    } else {
		// Success
		SoundManager.playSound(SoundConstants.SOUND_MAGNET);
	    }
	    GameManager.updateUndo(false, false, false, false, true, false, false, false, false, false);
	} else {
	    // Moving normally
	    SoundManager.playSound(SoundConstants.SOUND_MOVE);
	    gm.updateScore(1, 0, 0);
	    this.sx = zx;
	    this.sy = zy;
	    this.magnet = false;
	    GameManager.updateUndo(false, false, false, false, false, false, false, false, false, false);
	}
	this.move = true;
	this.loopCheck = true;
	if (!gm.isReplaying()) {
	    gm.updateReplay(false, zx, zy);
	}
    }

    void activateObjects(final int zx, final int zy, final int pushX, final int pushY,
	    final AbstractMovableObject gmo) {
	final MovingObjectTracker tracker = new MovingObjectTracker();
	tracker.activateObject(zx, zy, pushX, pushY, gmo);
	this.objectTrackers.add(tracker);
    }

    private boolean areLaserTrackersChecking() {
	boolean result = false;
	for (final MovingLaserTracker tracker : this.laserTrackers) {
	    if (tracker.isChecking()) {
		result = true;
	    }
	}
	return result;
    }

    private boolean areObjectTrackersChecking() {
	boolean result = false;
	for (final MovingObjectTracker tracker : this.objectTrackers) {
	    if (tracker.isChecking()) {
		result = true;
	    }
	}
	return result;
    }

    private boolean areObjectTrackersTracking() {
	boolean result = false;
	for (final MovingObjectTracker tracker : this.objectTrackers) {
	    if (tracker.isTracking()) {
		result = true;
	    }
	}
	return result;
    }

    private boolean canMoveThere() {
	final Application app = LaserTank.getApplication();
	final GameManager gm = app.getGameManager();
	final PlayerLocationManager plMgr = gm.getPlayerManager();
	final int px = plMgr.getPlayerLocationX();
	final int py = plMgr.getPlayerLocationY();
	final int pz = plMgr.getPlayerLocationZ();
	final int pw = ArenaConstants.LAYER_UPPER_OBJECTS;
	final AbstractArena m = app.getArenaManager().getArena();
	AbstractArenaObject lgo = null;
	AbstractArenaObject ugo = null;
	AbstractArenaObject loo = null;
	AbstractArenaObject uoo = null;
	try {
	    try {
		lgo = m.getCell(px + this.sx, py + this.sy, pz, ArenaConstants.LAYER_LOWER_GROUND);
		ugo = m.getCell(px + this.sx, py + this.sy, pz, ArenaConstants.LAYER_UPPER_GROUND);
		loo = m.getCell(px + this.sx, py + this.sy, pz, ArenaConstants.LAYER_LOWER_OBJECTS);
		uoo = m.getCell(px + this.sx, py + this.sy, pz, pw);
	    } catch (final ArrayIndexOutOfBoundsException ae) {
		lgo = new Wall();
		ugo = new Wall();
		loo = new Wall();
		uoo = new Wall();
	    }
	} catch (final NullPointerException np) {
	    this.proceed = false;
	    lgo = new Wall();
	    ugo = new Wall();
	    loo = new Wall();
	    uoo = new Wall();
	}
	return MLOTask.checkSolid(lgo) && MLOTask.checkSolid(ugo) && MLOTask.checkSolid(loo) && MLOTask.checkSolid(uoo);
    }

    private boolean checkLoopCondition(final boolean zproceed) {
	final Application app = LaserTank.getApplication();
	final GameManager gm = app.getGameManager();
	final PlayerLocationManager plMgr = gm.getPlayerManager();
	final int px = plMgr.getPlayerLocationX();
	final int py = plMgr.getPlayerLocationY();
	final int pz = plMgr.getPlayerLocationZ();
	final AbstractArena m = app.getArenaManager().getArena();
	AbstractArenaObject lgo = null;
	AbstractArenaObject ugo = null;
	try {
	    try {
		lgo = m.getCell(px, py, pz, ArenaConstants.LAYER_LOWER_GROUND);
		ugo = m.getCell(px, py, pz, ArenaConstants.LAYER_UPPER_GROUND);
	    } catch (final ArrayIndexOutOfBoundsException ae) {
		lgo = new Wall();
		ugo = new Wall();
	    }
	} catch (final NullPointerException np) {
	    this.proceed = false;
	    lgo = new Wall();
	    ugo = new Wall();
	}
	return zproceed && (!lgo.hasFriction() || !ugo.hasFriction() || this.mover || this.frozen)
		&& this.canMoveThere();
    }

    private void cullTrackers() {
	final MovingObjectTracker[] tempArray1 = this.objectTrackers
		.toArray(new MovingObjectTracker[this.objectTrackers.size()]);
	this.objectTrackers.clear();
	for (final MovingObjectTracker tracker : tempArray1) {
	    if (tracker != null) {
		if (tracker.isTracking()) {
		    this.objectTrackers.add(tracker);
		}
	    }
	}
	final MovingLaserTracker[] tempArray2 = this.laserTrackers
		.toArray(new MovingLaserTracker[this.laserTrackers.size()]);
	this.laserTrackers.clear();
	for (final MovingLaserTracker tracker : tempArray2) {
	    if (tracker != null) {
		if (tracker.isTracking()) {
		    this.laserTrackers.add(tracker);
		}
	    }
	}
    }

    private void defrostTank() {
	if (this.frozen) {
	    this.frozen = false;
	    final GameManager gm = LaserTank.getApplication().getGameManager();
	    final AbstractCharacter tank = gm.getTank();
	    final Direction dir = tank.getDirection();
	    final int px = gm.getPlayerManager().getPlayerLocationX();
	    final int py = gm.getPlayerManager().getPlayerLocationY();
	    final int pz = gm.getPlayerManager().getPlayerLocationZ();
	    final Tank t = new Tank(dir, tank.getNumber());
	    t.setSavedObject(tank.getSavedObject());
	    gm.morph(t, px, py, pz, t.getLayer());
	    gm.updateTank();
	    SoundManager.playSound(SoundConstants.SOUND_DEFROST);
	}
    }

    private void doMovementLasersObjects() {
	synchronized (CurrentArenaData.LOCK_OBJECT) {
	    final GameManager gm = LaserTank.getApplication().getGameManager();
	    final PlayerLocationManager plMgr = gm.getPlayerManager();
	    final int pz = plMgr.getPlayerLocationZ();
	    this.loopCheck = true;
	    AbstractArenaObject[] objs = new AbstractArenaObject[4];
	    objs[ArenaConstants.LAYER_LOWER_GROUND] = new Wall();
	    objs[ArenaConstants.LAYER_UPPER_GROUND] = new Wall();
	    objs[ArenaConstants.LAYER_LOWER_OBJECTS] = new Wall();
	    objs[ArenaConstants.LAYER_UPPER_OBJECTS] = new Wall();
	    do {
		try {
		    if (this.move && this.loopCheck) {
			objs = this.doMovementOnce();
		    }
		    // Abort check 1
		    if (this.abort) {
			break;
		    }
		    for (final MovingLaserTracker tracker : this.laserTrackers) {
			if (tracker.isTracking()) {
			    tracker.trackPart1(this.areObjectTrackersTracking());
			}
		    }
		    // Abort check 2
		    if (this.abort) {
			break;
		    }
		    for (final MovingObjectTracker tracker : this.objectTrackers) {
			if (tracker.isTracking()) {
			    tracker.trackPart1();
			}
		    }
		    // Abort check 3
		    if (this.abort) {
			break;
		    }
		    int actionType = 0;
		    if (this.move) {
			if (!this.magnet && Math.abs(this.sx) <= 1 && Math.abs(this.sy) <= 1) {
			    actionType = ActionConstants.ACTION_MOVE;
			} else {
			    actionType = ActionConstants.ACTION_NON_MOVE;
			}
		    } else {
			actionType = ActionConstants.ACTION_NON_MOVE;
		    }
		    for (final MovingLaserTracker tracker : this.laserTrackers) {
			if (tracker.isTracking()) {
			    this.mover = tracker.trackPart2(this.sx, this.sy, this.mover);
			}
		    }
		    for (final MovingObjectTracker tracker : this.objectTrackers) {
			if (tracker.isTracking()) {
			    tracker.trackPart2();
			}
		    }
		    if (this.move) {
			this.loopCheck = this.checkLoopCondition(this.proceed);
			if (this.mover && !this.canMoveThere()) {
			    MLOTask.activateAutomaticMovement();
			}
			if (objs[ArenaConstants.LAYER_LOWER_OBJECTS].solvesOnMove()) {
			    this.abort = true;
			    if (this.move) {
				LaserTank.getApplication().getArenaManager().setDirty(true);
				this.defrostTank();
				gm.moveLoopDone();
				this.move = false;
			    }
			    for (final MovingLaserTracker tracker : this.laserTrackers) {
				if (tracker.isTracking()) {
				    tracker.clearLastLaser();
				}
			    }
			    gm.solvedLevel(true);
			    return;
			}
		    } else {
			this.loopCheck = false;
		    }
		    if (this.move && !this.loopCheck) {
			LaserTank.getApplication().getArenaManager().setDirty(true);
			this.defrostTank();
			gm.moveLoopDone();
			this.move = false;
		    }
		    for (final MovingObjectTracker tracker : this.objectTrackers) {
			if (tracker.isTracking()) {
			    tracker.trackPart3();
			}
		    }
		    // Check auto-move flag
		    if (gm.isAutoMoveScheduled() && this.canMoveThere()) {
			gm.unscheduleAutoMove();
			this.move = true;
			this.loopCheck = true;
		    }
		    LaserTank.getApplication().getArenaManager().getArena().tickTimers(pz, actionType);
		    final int px = plMgr.getPlayerLocationX();
		    final int py = plMgr.getPlayerLocationY();
		    LaserTank.getApplication().getArenaManager().getArena().checkForEnemies(pz, px, py,
			    LaserTank.getApplication().getGameManager().getTank());
		    // Delay
		    try {
			Thread.sleep(PreferencesManager.getActionSpeed());
		    } catch (final InterruptedException ie) {
			// Ignore
		    }
		    for (final MovingObjectTracker tracker : this.objectTrackers) {
			if (tracker.isTracking()) {
			    tracker.trackPart4();
			}
		    }
		    this.cullTrackers();
		} catch (final ConcurrentModificationException cme) {
		    // Ignore
		}
	    } while (!this.abort
		    && (this.loopCheck || this.areLaserTrackersChecking() || this.areObjectTrackersChecking()));
	    if (objs[ArenaConstants.LAYER_LOWER_GROUND].killsOnMove()) {
		// Check cheats
		if (!gm.getCheatStatus(GameManager.CHEAT_SWIMMING)) {
		    gm.gameOver();
		}
	    }
	}
    }

    private AbstractArenaObject[] doMovementOnce() {
	final GameManager gm = LaserTank.getApplication().getGameManager();
	final PlayerLocationManager plMgr = gm.getPlayerManager();
	int px = plMgr.getPlayerLocationX();
	int py = plMgr.getPlayerLocationY();
	final int pz = plMgr.getPlayerLocationZ();
	final int pw = ArenaConstants.LAYER_UPPER_OBJECTS;
	final Application app = LaserTank.getApplication();
	final AbstractArena m = app.getArenaManager().getArena();
	this.proceed = true;
	this.mover = false;
	AbstractArenaObject lgo = null;
	AbstractArenaObject ugo = null;
	AbstractArenaObject loo = null;
	AbstractArenaObject uoo = null;
	try {
	    try {
		lgo = m.getCell(px + this.sx, py + this.sy, pz, ArenaConstants.LAYER_LOWER_GROUND);
		ugo = m.getCell(px + this.sx, py + this.sy, pz, ArenaConstants.LAYER_UPPER_GROUND);
		loo = m.getCell(px + this.sx, py + this.sy, pz, ArenaConstants.LAYER_LOWER_OBJECTS);
		uoo = m.getCell(px + this.sx, py + this.sy, pz, pw);
	    } catch (final ArrayIndexOutOfBoundsException ae) {
		lgo = new Wall();
		ugo = new Wall();
		loo = new Wall();
		uoo = new Wall();
	    }
	} catch (final NullPointerException np) {
	    this.proceed = false;
	    lgo = new Wall();
	    ugo = new Wall();
	    loo = new Wall();
	    uoo = new Wall();
	}
	if (this.proceed) {
	    plMgr.savePlayerLocation();
	    try {
		if (this.canMoveThere()) {
		    if (gm.isDelayedDecayActive()) {
			gm.doDelayedDecay();
		    }
		    // Preserve other objects
		    if (m.getCell(px, py, pz, pw) instanceof AbstractMovableObject) {
			gm.getTank().setSavedObject(m.getCell(px, py, pz, pw));
		    }
		    m.setCell(gm.getTank().getSavedObject(), px, py, pz, pw);
		    plMgr.offsetPlayerLocationX(this.sx);
		    plMgr.offsetPlayerLocationY(this.sy);
		    px = MLOTask.normalizeColumn(px + this.sx, AbstractArena.getMinColumns());
		    py = MLOTask.normalizeRow(py + this.sy, AbstractArena.getMinRows());
		    gm.getTank().setSavedObject(m.getCell(px, py, pz, pw));
		    m.setCell(gm.getTank(), px, py, pz, pw);
		    lgo.postMoveAction(px, py, pz);
		    ugo.postMoveAction(px, py, pz);
		    loo.postMoveAction(px, py, pz);
		    uoo.postMoveAction(px, py, pz);
		    if (ugo.isOfType(TypeConstants.TYPE_MOVER)) {
			final Direction dir = ugo.getDirection();
			final int[] unres = DirectionResolver.unresolveRelative(dir);
			this.sx = unres[0];
			this.sy = unres[1];
			this.mover = true;
		    } else {
			this.mover = false;
		    }
		} else {
		    // Move failed - object is solid in that direction
		    if (gm.isDelayedDecayActive()) {
			gm.doDelayedDecay();
		    }
		    if (lgo == null) {
			lgo = new Ground();
		    }
		    lgo.moveFailedAction(plMgr.getPlayerLocationX() + this.sx, plMgr.getPlayerLocationY() + this.sy,
			    plMgr.getPlayerLocationZ());
		    if (ugo == null) {
			ugo = new Ground();
		    }
		    ugo.moveFailedAction(plMgr.getPlayerLocationX() + this.sx, plMgr.getPlayerLocationY() + this.sy,
			    plMgr.getPlayerLocationZ());
		    if (loo == null) {
			loo = new Ground();
		    }
		    loo.moveFailedAction(plMgr.getPlayerLocationX() + this.sx, plMgr.getPlayerLocationY() + this.sy,
			    plMgr.getPlayerLocationZ());
		    if (uoo == null) {
			uoo = new Ground();
		    }
		    uoo.moveFailedAction(plMgr.getPlayerLocationX() + this.sx, plMgr.getPlayerLocationY() + this.sy,
			    plMgr.getPlayerLocationZ());
		    if (gm.getTank().getSavedObject().isOfType(TypeConstants.TYPE_MOVER)) {
			final Direction dir = gm.getTank().getSavedObject().getDirection();
			final int[] unres = DirectionResolver.unresolveRelative(dir);
			this.sx = unres[0];
			this.sy = unres[1];
			this.mover = true;
		    } else {
			this.mover = false;
		    }
		    this.proceed = false;
		}
	    } catch (final ArrayIndexOutOfBoundsException ae) {
		plMgr.restorePlayerLocation();
		m.setCell(gm.getTank(), plMgr.getPlayerLocationX(), plMgr.getPlayerLocationY(),
			plMgr.getPlayerLocationZ(), pw);
		// Move failed - attempted to go outside the arena
		if (lgo == null) {
		    lgo = new Ground();
		}
		lgo.moveFailedAction(plMgr.getPlayerLocationX() + this.sx, plMgr.getPlayerLocationY() + this.sy,
			plMgr.getPlayerLocationZ());
		if (ugo == null) {
		    ugo = new Ground();
		}
		ugo.moveFailedAction(plMgr.getPlayerLocationX() + this.sx, plMgr.getPlayerLocationY() + this.sy,
			plMgr.getPlayerLocationZ());
		if (loo == null) {
		    loo = new Ground();
		}
		loo.moveFailedAction(plMgr.getPlayerLocationX() + this.sx, plMgr.getPlayerLocationY() + this.sy,
			plMgr.getPlayerLocationZ());
		if (uoo == null) {
		    uoo = new Ground();
		}
		uoo.moveFailedAction(plMgr.getPlayerLocationX() + this.sx, plMgr.getPlayerLocationY() + this.sy,
			plMgr.getPlayerLocationZ());
		this.proceed = false;
	    }
	} else {
	    // Move failed - pre-move check failed
	    lgo.moveFailedAction(px + this.sx, py + this.sy, pz);
	    ugo.moveFailedAction(px + this.sx, py + this.sy, pz);
	    loo.moveFailedAction(px + this.sx, py + this.sy, pz);
	    uoo.moveFailedAction(px + this.sx, py + this.sy, pz);
	    this.proceed = false;
	}
	gm.redrawArena();
	return new AbstractArenaObject[] { lgo, ugo, loo, uoo };
    }

    void haltMovingObjects() {
	for (final MovingObjectTracker tracker : this.objectTrackers) {
	    if (tracker.isTracking()) {
		tracker.haltMovingObject();
	    }
	}
    }

    @Override
    public void run() {
	try {
	    final GameManager gm = LaserTank.getApplication().getGameManager();
	    gm.clearDead();
	    this.doMovementLasersObjects();
	    // Check auto-move flag
	    if (gm.isAutoMoveScheduled() && this.canMoveThere()) {
		gm.unscheduleAutoMove();
		this.doMovementLasersObjects();
	    }
	} catch (final AlreadyDeadException ade) {
	    // Ignore
	} catch (final Throwable t) {
	    LaserTank.logError(t);
	}
    }
}
