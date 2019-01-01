/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.game;

import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.AbstractArena;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractJumpObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.ltremix.arena.objects.AntiTank;
import com.puttysoftware.ltremix.arena.objects.AnyMover;
import com.puttysoftware.ltremix.utilities.ArenaConstants;
import com.puttysoftware.ltremix.utilities.DirectionResolver;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public final class MovingObjectTracker {
    // Fields
    private boolean objectMoving;
    private int objCumX, objCumY, objIncX, objIncY;
    private int objMultX, objMultY;
    private AbstractArenaObject belowUpper;
    private AbstractArenaObject belowLower;
    private AbstractMovableObject movingObj;
    private boolean objectCheck;
    private boolean objectNewlyActivated;
    private boolean jumpOnMover;

    // Constructors
    public MovingObjectTracker() {
	this.resetTracker();
    }

    boolean isTracking() {
	return this.objectMoving;
    }

    boolean isChecking() {
	return this.objectCheck;
    }

    void resetTracker() {
	this.objectCheck = true;
	this.objectNewlyActivated = false;
	this.jumpOnMover = false;
	this.objMultX = 1;
	this.objMultY = 1;
    }

    public void updateObject(final AbstractMovableObject newObj, final AntiTank subTo) {
	this.movingObj = newObj;
	subTo.unsubscribe();
    }

    void trackPart1() {
	if (this.objectMoving && this.objectCheck) {
	    this.doObjectOnce();
	}
    }

    void trackPart2() {
	try {
	    final GameManager gm = LTRemix.getApplication().getGameManager();
	    final PlayerLocationManager plMgr = gm.getPlayerManager();
	    final int pz = plMgr.getPlayerLocationZ();
	    if (this.objectMoving) {
		// Make objects pushed into ice move 2 squares first time
		if (this.objectCheck && this.objectNewlyActivated) {
		    if (!this.belowLower.hasFriction() || !this.belowUpper.hasFriction()) {
			this.doObjectOnce();
			this.objectCheck = !this.belowLower.hasFriction() || !this.belowUpper.hasFriction();
		    }
		}
	    } else {
		this.objectCheck = false;
		// Check for moving object stopped on thin ice
		if (this.movingObj != null) {
		    if (gm.isDelayedDecayActive() && gm.isRemoteDecayActive()) {
			gm.doRemoteDelayedDecay(this.movingObj);
			this.belowUpper.pushIntoAction(this.movingObj, this.objCumX, this.objCumY, pz);
			this.belowLower.pushIntoAction(this.movingObj, this.objCumX, this.objCumY, pz);
		    }
		}
	    }
	} catch (final ArrayIndexOutOfBoundsException aioobe) {
	    // Stop object
	    this.objectMoving = false;
	    this.objectCheck = false;
	}
    }

    void trackPart3() {
	if (!this.objectCheck) {
	    this.objectMoving = false;
	}
    }

    void trackPart4() {
	this.objectNewlyActivated = false;
    }

    void activateObject(final int zx, final int zy, final int pushX, final int pushY, final AbstractMovableObject gmo) {
	final GameManager gm = LTRemix.getApplication().getGameManager();
	final PlayerLocationManager plMgr = gm.getPlayerManager();
	final int pz = plMgr.getPlayerLocationZ();
	this.objIncX = pushX - zx;
	this.objIncY = pushY - zy;
	if (gmo instanceof AbstractJumpObject) {
	    if (pushX - zx == 0) {
		if (pushY > zy) {
		    this.objIncX = -1;
		} else {
		    this.objIncX = 1;
		}
	    }
	    if (pushY - zy == 0) {
		if (pushX > zx) {
		    this.objIncY = 1;
		} else {
		    this.objIncY = -1;
		}
	    }
	}
	this.objCumX = zx;
	this.objCumY = zy;
	this.movingObj = gmo;
	if (this.movingObj instanceof AntiTank) {
	    final AntiTank at = (AntiTank) this.movingObj;
	    at.subscribe(this);
	}
	this.belowUpper = LTRemix.getApplication().getArenaManager().getArena().getCell(
		this.objCumX + this.objIncX * this.objMultX, this.objCumY + this.objIncY * this.objMultY, pz,
		ArenaConstants.LAYER_UPPER_GROUND);
	this.belowLower = LTRemix.getApplication().getArenaManager().getArena().getCell(
		this.objCumX + this.objIncX * this.objMultX, this.objCumY + this.objIncY * this.objMultY, pz,
		ArenaConstants.LAYER_LOWER_GROUND);
	this.objectMoving = true;
	this.objectCheck = true;
	this.objectNewlyActivated = true;
    }

    void haltMovingObject() {
	this.objectMoving = false;
	this.objectCheck = false;
    }

    private void doObjectOnce() {
	if (this.movingObj instanceof AbstractJumpObject) {
	    this.doJumpObjectOnce((AbstractJumpObject) this.movingObj);
	} else {
	    this.doNormalObjectOnce();
	}
    }

    private void doNormalObjectOnce() {
	final Application app = LTRemix.getApplication();
	final AbstractArena m = app.getArenaManager().getArena();
	final GameManager gm = app.getGameManager();
	final int pz = gm.getPlayerManager().getPlayerLocationZ();
	try {
	    if (gm.isDelayedDecayActive() && gm.isRemoteDecayActive()) {
		gm.doRemoteDelayedDecay(this.movingObj);
	    }
	    final AbstractArenaObject oldSave = this.movingObj.getSavedObject();
	    final AbstractArenaObject saved = m.getCell(this.objCumX + this.objIncX * this.objMultX,
		    this.objCumY + this.objIncY * this.objMultY, pz, this.movingObj.getPrimaryLayer());
	    this.belowUpper = LTRemix.getApplication().getArenaManager().getArena().getCell(
		    this.objCumX + this.objIncX * this.objMultX, this.objCumY + this.objIncY * this.objMultY, pz,
		    ArenaConstants.LAYER_UPPER_GROUND);
	    this.belowLower = LTRemix.getApplication().getArenaManager().getArena().getCell(
		    this.objCumX + this.objIncX * this.objMultX, this.objCumY + this.objIncY * this.objMultY, pz,
		    ArenaConstants.LAYER_LOWER_GROUND);
	    if (!saved.isConditionallySolid()) {
		this.belowLower.pushOutAction(this.movingObj, this.objCumX, this.objCumY, pz);
		this.belowUpper.pushOutAction(this.movingObj, this.objCumX, this.objCumY, pz);
		oldSave.pushOutAction(this.movingObj, this.objCumX, this.objCumY, pz);
		m.setCell(oldSave, this.objCumX, this.objCumY, pz, this.movingObj.getPrimaryLayer());
		this.movingObj.setSavedObject(saved);
		m.setCell(this.movingObj, this.objCumX + this.objIncX * this.objMultX,
			this.objCumY + this.objIncY * this.objMultY, pz, this.movingObj.getPrimaryLayer());
		boolean stopObj = this.belowLower.pushIntoAction(this.movingObj,
			this.objCumX + this.objIncX * this.objMultX, this.objCumY + this.objIncY * this.objMultY, pz);
		final boolean temp1 = this.belowUpper.pushIntoAction(this.movingObj,
			this.objCumX + this.objIncX * this.objMultX, this.objCumY + this.objIncY * this.objMultY, pz);
		if (!temp1) {
		    stopObj = false;
		}
		final boolean temp2 = saved.pushIntoAction(this.movingObj, this.objCumX + this.objIncX * this.objMultX,
			this.objCumY + this.objIncY * this.objMultY, pz);
		if (!temp2) {
		    stopObj = false;
		}
		this.objectMoving = stopObj;
		this.objectCheck = stopObj;
		final int oldObjIncX = this.objIncX;
		final int oldObjIncY = this.objIncY;
		if (this.movingObj.isOfType(TypeConstants.TYPE_ICY)) {
		    // Handle icy objects
		    this.objectCheck = true;
		} else if (this.belowUpper.isOfType(TypeConstants.TYPE_ANTI_MOVER)
			&& this.movingObj.isOfType(TypeConstants.TYPE_ANTI)) {
		    // Handle anti-tank on anti-tank mover
		    final int dir = this.belowUpper.getDirection();
		    final int[] unres = DirectionResolver.unresolveRelativeDirection(dir);
		    this.objIncX = unres[0];
		    this.objIncY = unres[1];
		    this.objectCheck = true;
		} else if (this.belowUpper.isOfType(TypeConstants.TYPE_BOX_MOVER)
			&& this.movingObj.isOfType(TypeConstants.TYPE_BOX)) {
		    // Handle box on box mover
		    final int dir = this.belowUpper.getDirection();
		    final int[] unres = DirectionResolver.unresolveRelativeDirection(dir);
		    this.objIncX = unres[0];
		    this.objIncY = unres[1];
		    this.objectCheck = true;
		} else if (this.belowUpper.isOfType(TypeConstants.TYPE_MIRROR_MOVER)
			&& this.movingObj.isOfType(TypeConstants.TYPE_MOVABLE_MIRROR)) {
		    // Handle mirror on mirror mover
		    final int dir = this.belowUpper.getDirection();
		    final int[] unres = DirectionResolver.unresolveRelativeDirection(dir);
		    this.objIncX = unres[0];
		    this.objIncY = unres[1];
		    this.objectCheck = true;
		} else if (this.belowUpper instanceof AnyMover) {
		    // Handle anything on any mover
		    final int dir = this.belowUpper.getDirection();
		    final int[] unres = DirectionResolver.unresolveRelativeDirection(dir);
		    this.objIncX = unres[0];
		    this.objIncY = unres[1];
		    this.objectCheck = true;
		} else {
		    this.objectCheck = !this.belowLower.hasFriction() || !this.belowUpper.hasFriction();
		}
		if (this.objIncX != oldObjIncX || this.objIncY != oldObjIncY) {
		    this.objCumX += oldObjIncX;
		    this.objCumY += oldObjIncY;
		} else {
		    this.objCumX += this.objIncX;
		    this.objCumY += this.objIncY;
		}
		app.getArenaManager().setDirty(true);
	    } else {
		// Movement failed
		this.belowLower.pushIntoAction(this.movingObj, this.objCumX, this.objCumY, pz);
		this.belowUpper.pushIntoAction(this.movingObj, this.objCumX, this.objCumY, pz);
		oldSave.pushIntoAction(this.movingObj, this.objCumX, this.objCumY, pz);
		this.movingObj.pushCollideAction(this.movingObj, this.objCumX, this.objCumY, pz);
		saved.pushCollideAction(this.movingObj, this.objCumX + this.objIncX * this.objMultX,
			this.objCumY + this.objIncY * this.objMultY, pz);
		this.objectMoving = false;
		this.objectCheck = false;
	    }
	} catch (final ArrayIndexOutOfBoundsException ae) {
	    this.objectMoving = false;
	    this.objectCheck = false;
	}
	gm.redrawArena();
    }

    private void doJumpObjectOnce(final AbstractJumpObject jumper) {
	final Application app = LTRemix.getApplication();
	final AbstractArena m = app.getArenaManager().getArena();
	final GameManager gm = app.getGameManager();
	final int pz = gm.getPlayerManager().getPlayerLocationZ();
	try {
	    this.jumpOnMover = false;
	    this.objMultX = jumper.getActualJumpCols();
	    this.objMultY = jumper.getActualJumpRows();
	    if (gm.isDelayedDecayActive() && gm.isRemoteDecayActive()) {
		gm.doRemoteDelayedDecay(jumper);
	    }
	    final AbstractArenaObject oldSave = jumper.getSavedObject();
	    final AbstractArenaObject saved = m.getCell(this.objCumX + this.objIncX * this.objMultX,
		    this.objCumY + this.objIncY * this.objMultY, pz, jumper.getPrimaryLayer());
	    this.belowUpper = LTRemix.getApplication().getArenaManager().getArena().getCell(
		    this.objCumX + this.objIncX * this.objMultX, this.objCumY + this.objIncY * this.objMultY, pz,
		    ArenaConstants.LAYER_UPPER_GROUND);
	    this.belowLower = LTRemix.getApplication().getArenaManager().getArena().getCell(
		    this.objCumX + this.objIncX * this.objMultX, this.objCumY + this.objIncY * this.objMultY, pz,
		    ArenaConstants.LAYER_LOWER_GROUND);
	    if (!saved.isConditionallySolid() && this.objMultX != 0 && this.objMultY != 0) {
		jumper.jumpSound(true);
		oldSave.pushOutAction(jumper, this.objCumX, this.objCumY, pz);
		m.setCell(oldSave, this.objCumX, this.objCumY, pz, jumper.getPrimaryLayer());
		jumper.setSavedObject(saved);
		m.setCell(jumper, this.objCumX + this.objIncX * this.objMultX,
			this.objCumY + this.objIncY * this.objMultY, pz, jumper.getPrimaryLayer());
		boolean stopObj = this.belowLower.pushIntoAction(this.movingObj,
			this.objCumX + this.objIncX * this.objMultX, this.objCumY + this.objIncY * this.objMultY, pz);
		final boolean temp1 = this.belowUpper.pushIntoAction(this.movingObj,
			this.objCumX + this.objIncX * this.objMultX, this.objCumY + this.objIncY * this.objMultY, pz);
		if (!temp1) {
		    stopObj = false;
		}
		final boolean temp2 = saved.pushIntoAction(this.movingObj, this.objCumX + this.objIncX * this.objMultX,
			this.objCumY + this.objIncY * this.objMultY, pz);
		if (!temp2) {
		    stopObj = false;
		}
		this.objectMoving = stopObj;
		this.objectCheck = stopObj;
		final int oldObjIncX = this.objIncX;
		final int oldObjIncY = this.objIncY;
		if (jumper.isOfType(TypeConstants.TYPE_ICY)) {
		    // Handle icy objects
		    this.objectCheck = true;
		} else if (this.belowUpper.isOfType(TypeConstants.TYPE_ANTI_MOVER)
			&& jumper.isOfType(TypeConstants.TYPE_ANTI)) {
		    // Handle anti-tank on anti-tank mover
		    final int dir = this.belowUpper.getDirection();
		    final int[] unres = DirectionResolver.unresolveRelativeDirection(dir);
		    this.objIncX = unres[0];
		    this.objIncY = unres[1];
		    this.objectCheck = true;
		} else if (this.belowUpper.isOfType(TypeConstants.TYPE_BOX_MOVER)
			&& jumper.isOfType(TypeConstants.TYPE_BOX)) {
		    // Handle box on box mover
		    this.jumpOnMover = true;
		    final int dir = this.belowUpper.getDirection();
		    final int[] unres = DirectionResolver.unresolveRelativeDirection(dir);
		    this.objIncX = unres[0];
		    this.objIncY = unres[1];
		    this.objectCheck = true;
		} else if (this.belowUpper.isOfType(TypeConstants.TYPE_MIRROR_MOVER)
			&& this.movingObj.isOfType(TypeConstants.TYPE_MOVABLE_MIRROR)) {
		    // Handle mirror on mirror mover
		    final int dir = this.belowUpper.getDirection();
		    final int[] unres = DirectionResolver.unresolveRelativeDirection(dir);
		    this.objIncX = unres[0];
		    this.objIncY = unres[1];
		    this.objectCheck = true;
		} else {
		    this.objectCheck = !this.belowLower.hasFriction() || !this.belowUpper.hasFriction();
		}
		if (this.objIncX != oldObjIncX || this.objIncY != oldObjIncY) {
		    if (this.jumpOnMover) {
			this.objCumX += oldObjIncX;
			this.objCumY += oldObjIncY;
		    } else {
			this.objCumX += oldObjIncX * this.objMultX;
			this.objCumY += oldObjIncY * this.objMultY;
		    }
		} else {
		    if (this.jumpOnMover) {
			this.objCumX += this.objIncX;
			this.objCumY += this.objIncY;
		    } else {
			this.objCumX += this.objIncX * this.objMultX;
			this.objCumY += this.objIncY * this.objMultY;
		    }
		}
		app.getArenaManager().setDirty(true);
	    } else {
		// Movement failed
		jumper.jumpSound(false);
		oldSave.pushIntoAction(jumper, this.objCumX, this.objCumY, pz);
		jumper.pushCollideAction(this.movingObj, this.objCumX, this.objCumY, pz);
		saved.pushCollideAction(jumper, this.objCumX + this.objIncX * this.objMultX,
			this.objCumY + this.objIncY * this.objMultY, pz);
		this.objectMoving = false;
		this.objectCheck = false;
	    }
	} catch (final ArrayIndexOutOfBoundsException ae) {
	    jumper.jumpSound(false);
	    this.objectMoving = false;
	    this.objectCheck = false;
	}
	gm.redrawArena();
    }
}
