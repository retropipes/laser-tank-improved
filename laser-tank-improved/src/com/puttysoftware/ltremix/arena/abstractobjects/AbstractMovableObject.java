/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

import java.io.IOException;

import com.puttysoftware.lasertank.improved.fileio.XMLFileReader;
import com.puttysoftware.lasertank.improved.fileio.XMLFileWriter;
import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.AbstractArena;
import com.puttysoftware.ltremix.arena.objects.Empty;
import com.puttysoftware.ltremix.arena.objects.Wall;
import com.puttysoftware.ltremix.utilities.ArenaConstants;
import com.puttysoftware.ltremix.utilities.DirectionConstants;
import com.puttysoftware.ltremix.utilities.LaserTypeConstants;
import com.puttysoftware.ltremix.utilities.MaterialConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public abstract class AbstractMovableObject extends AbstractArenaObject {
    // Fields
    private boolean waitingOnTunnel;
    protected boolean didPreCheck;
    protected boolean didMove;

    // Constructors
    protected AbstractMovableObject(final boolean pushable) {
	super(true, pushable, true);
	this.setSavedObject(new Empty());
	this.waitingOnTunnel = false;
	this.type.set(TypeConstants.TYPE_MOVABLE);
	this.didPreCheck = false;
	this.didMove = false;
	this.activateTimer(1);
    }

    public final boolean waitingOnTunnel() {
	return this.waitingOnTunnel;
    }

    public final void setWaitingOnTunnel(final boolean value) {
	this.waitingOnTunnel = value;
    }

    @Override
    public AbstractArenaObject clone() {
	final AbstractMovableObject copy = (AbstractMovableObject) super.clone();
	if (this.getSavedObject() != null) {
	    copy.setSavedObject(this.getSavedObject().clone());
	}
	return copy;
    }

    @Override
    public boolean canMove() {
	return this.isPushable();
    }

    public boolean didPreCheck() {
	return this.didPreCheck;
    }

    public boolean didMove() {
	return this.didMove;
    }

    public void setMoved() {
	this.didMove = true;
    }

    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	// Do nothing
    }

    public abstract void playSoundHook();

    @Override
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	final Application app = LTRemix.getApplication();
	if (this.canMove()) {
	    if (forceUnits >= this.getMinimumReactionForce()) {
		try {
		    final AbstractArenaObject mof = app.getArenaManager().getArena().getCell(locX + dirX, locY + dirY,
			    locZ, this.getPrimaryLayer());
		    final AbstractArenaObject mor = app.getArenaManager().getArena().getCell(locX - dirX, locY - dirY,
			    locZ, this.getPrimaryLayer());
		    if (this.getMaterial() == MaterialConstants.MATERIAL_MAGNETIC) {
			if (laserType == LaserTypeConstants.LASER_TYPE_BLUE && this.moveCheck(mof)) {
			    final boolean moved = this.preMoveObject(locX, locY, locZ, -dirX, -dirY, laserType,
				    forceUnits);
			    if (moved) {
				app.getGameManager().updatePushedPosition(locX, locY, locX - dirX, locY - dirY, this);
				this.didMove = true;
				app.getArenaManager().getArena().fullScanMoveObjects(locZ, dirX, dirY);
				this.playSoundHook();
			    } else {
				// Object can't go that way
				return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
			    }
			} else if (this.moveCheck(mor)) {
			    final boolean moved = this.preMoveObject(locX, locY, locZ, dirX, dirY, laserType,
				    forceUnits);
			    if (moved) {
				app.getGameManager().updatePushedPosition(locX, locY, locX + dirX, locY + dirY, this);
				this.didMove = true;
				app.getArenaManager().getArena().fullScanMoveObjects(locZ, dirX, dirY);
				this.playSoundHook();
			    } else {
				// Object can't go that way
				return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
			    }
			} else {
			    // Object doesn't react to this type of laser
			    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
			}
		    } else {
			if (laserType == LaserTypeConstants.LASER_TYPE_BLUE && this.moveCheck(mor)) {
			    final boolean moved = this.preMoveObject(locX, locY, locZ, -dirX, -dirY, laserType,
				    forceUnits);
			    if (moved) {
				app.getGameManager().updatePushedPosition(locX, locY, locX - dirX, locY - dirY, this);
				this.didMove = true;
				app.getArenaManager().getArena().fullScanMoveObjects(locZ, dirX, dirY);
				this.playSoundHook();
			    } else {
				// Object can't go that way
				return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
			    }
			} else if (this.moveCheck(mof)) {
			    final boolean moved = this.preMoveObject(locX, locY, locZ, dirX, dirY, laserType,
				    forceUnits);
			    if (moved) {
				app.getGameManager().updatePushedPosition(locX, locY, locX + dirX, locY + dirY, this);
				this.didMove = true;
				app.getArenaManager().getArena().fullScanMoveObjects(locZ, dirX, dirY);
				this.playSoundHook();
			    } else {
				// Object can't go that way
				return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
			    }
			} else {
			    // Object doesn't react to this type of laser
			    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
			}
		    }
		} catch (final ArrayIndexOutOfBoundsException aioobe) {
		    // Object can't go that way
		    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
		}
	    } else {
		// Not enough force
		return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	    }
	} else {
	    // Object is not movable
	    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	}
	app.getGameManager().redrawArena();
	return DirectionConstants.NONE;
    }

    protected boolean moveCheck(final AbstractArenaObject mo) {
	return mo != null;
    }

    /**
     *
     * @param locX
     * @param locY
     * @param locZ
     * @param dirX
     * @param dirY
     * @param laserType
     * @param forceUnits
     */
    protected boolean preMoveObject(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	if (forceUnits >= this.getMinimumReactionForce()) {
	    this.didPreCheck = true;
	    final Application app = LTRemix.getApplication();
	    final AbstractArena a = app.getArenaManager().getArena();
	    AbstractArenaObject obj2;
	    try {
		obj2 = a.getCell(locX, locY - 1, locZ, this.getPrimaryLayer());
	    } catch (final ArrayIndexOutOfBoundsException aioobe) {
		obj2 = new Wall();
	    }
	    AbstractArenaObject obj4;
	    try {
		obj4 = a.getCell(locX - 1, locY, locZ, this.getPrimaryLayer());
	    } catch (final ArrayIndexOutOfBoundsException aioobe) {
		obj4 = new Wall();
	    }
	    AbstractArenaObject obj6;
	    try {
		obj6 = a.getCell(locX + 1, locY, locZ, this.getPrimaryLayer());
	    } catch (final ArrayIndexOutOfBoundsException aioobe) {
		obj6 = new Wall();
	    }
	    AbstractArenaObject obj8;
	    try {
		obj8 = a.getCell(locX, locY + 1, locZ, this.getPrimaryLayer());
	    } catch (final ArrayIndexOutOfBoundsException aioobe) {
		obj8 = new Wall();
	    }
	    boolean proceed = true;
	    if (dirX == 0 && dirY == -1) {
		if (obj2 instanceof AbstractMovableObject && !((AbstractMovableObject) obj2).didPreCheck) {
		    ((AbstractMovableObject) obj2).didPreCheck = true;
		    proceed = proceed && ((AbstractMovableObject) obj2).preMoveObject(locX, locY - 1, locZ, dirX, dirY,
			    laserType, forceUnits - obj2.getMinimumReactionForce());
		    if (proceed) {
			app.getGameManager().updatePushedPosition(locX, locY - 1, locX + dirX, locY - 1 + dirY,
				(AbstractMovableObject) obj2);
			((AbstractMovableObject) obj2).didMove = true;
		    } else {
			return false;
		    }
		} else {
		    proceed = proceed && !obj2.isSolid();
		}
	    } else if (dirX == -1 && dirY == 0) {
		if (obj4 instanceof AbstractMovableObject && !((AbstractMovableObject) obj4).didPreCheck) {
		    ((AbstractMovableObject) obj4).didPreCheck = true;
		    proceed = proceed && ((AbstractMovableObject) obj4).preMoveObject(locX - 1, locY, locZ, dirX, dirY,
			    laserType, forceUnits - obj4.getMinimumReactionForce());
		    if (proceed) {
			app.getGameManager().updatePushedPosition(locX - 1, locY, locX - 1 + dirX, locY + dirY,
				(AbstractMovableObject) obj4);
			((AbstractMovableObject) obj4).didMove = true;
		    } else {
			return false;
		    }
		} else {
		    proceed = proceed && !obj4.isSolid();
		}
	    } else if (dirX == 1 && dirY == 0) {
		if (obj6 instanceof AbstractMovableObject && !((AbstractMovableObject) obj6).didPreCheck) {
		    ((AbstractMovableObject) obj6).didPreCheck = true;
		    proceed = proceed && ((AbstractMovableObject) obj6).preMoveObject(locX + 1, locY, locZ, dirX, dirY,
			    laserType, forceUnits - obj6.getMinimumReactionForce());
		    if (proceed) {
			app.getGameManager().updatePushedPosition(locX + 1, locY, locX + 1 + dirX, locY + dirY,
				(AbstractMovableObject) obj6);
			((AbstractMovableObject) obj6).didMove = true;
		    } else {
			return false;
		    }
		} else {
		    proceed = proceed && !obj6.isSolid();
		}
	    } else if (dirX == 0 && dirY == 1) {
		if (obj8 instanceof AbstractMovableObject && !((AbstractMovableObject) obj8).didPreCheck) {
		    ((AbstractMovableObject) obj8).didPreCheck = true;
		    proceed = proceed && ((AbstractMovableObject) obj8).preMoveObject(locX, locY + 1, locZ, dirX, dirY,
			    laserType, forceUnits - obj8.getMinimumReactionForce());
		    if (proceed) {
			app.getGameManager().updatePushedPosition(locX, locY + 1, locX + dirX, locY + 1 + dirY,
				(AbstractMovableObject) obj8);
			((AbstractMovableObject) obj8).didMove = true;
		    } else {
			return false;
		    }
		} else {
		    proceed = proceed && !obj8.isSolid();
		}
	    }
	    return proceed;
	} else {
	    return false;
	}
    }

    @Override
    public int getPrimaryLayer() {
	return ArenaConstants.LAYER_LOWER_OBJECTS;
    }

    @Override
    public int getCustomProperty(final int propID) {
	return AbstractArenaObject.DEFAULT_CUSTOM_VALUE;
    }

    @Override
    public void setCustomProperty(final int propID, final int value) {
	// Do nothing
    }

    @Override
    protected AbstractArenaObject readArenaObjectHookG2(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	this.setSavedObject(LTRemix.getApplication().getObjects().readArenaObjectG2(reader, formatVersion));
	return this;
    }

    @Override
    protected AbstractArenaObject readArenaObjectHookG3(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	this.setSavedObject(LTRemix.getApplication().getObjects().readArenaObjectG3(reader, formatVersion));
	return this;
    }

    @Override
    protected AbstractArenaObject readArenaObjectHookG4(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	this.setSavedObject(LTRemix.getApplication().getObjects().readArenaObjectG4(reader, formatVersion));
	return this;
    }

    @Override
    protected AbstractArenaObject readArenaObjectHookG5(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	this.setSavedObject(LTRemix.getApplication().getObjects().readArenaObjectG5(reader, formatVersion));
	return this;
    }

    @Override
    protected AbstractArenaObject readArenaObjectHookG6(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	this.setSavedObject(LTRemix.getApplication().getObjects().readArenaObjectG6(reader, formatVersion));
	return this;
    }

    @Override
    protected AbstractArenaObject readArenaObjectHookG7(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	this.setSavedObject(LTRemix.getApplication().getObjects().readArenaObjectG7(reader, formatVersion));
	return this;
    }

    @Override
    protected void writeArenaObjectHook(final XMLFileWriter writer) throws IOException {
	this.getSavedObject().writeArenaObject(writer);
    }

    @Override
    public int getCustomFormat() {
	return AbstractArenaObject.CUSTOM_FORMAT_MANUAL_OVERRIDE;
    }

    @Override
    public boolean doLasersPassThrough() {
	return false;
    }

    @Override
    public void timerExpiredAction(final int dirX, final int dirY) {
	if (this.didPreCheck) {
	    this.didPreCheck = false;
	}
	if (this.didMove) {
	    this.didMove = false;
	}
	this.activateTimer(1);
    }
}