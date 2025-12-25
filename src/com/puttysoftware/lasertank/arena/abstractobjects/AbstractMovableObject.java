/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.abstractobjects;

import java.io.IOException;

import com.puttysoftware.fileio.XMLFileReader;
import com.puttysoftware.fileio.XMLFileWriter;
import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.objects.Empty;
import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.LaserTypeConstants;
import com.puttysoftware.lasertank.utilities.MaterialConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public abstract class AbstractMovableObject extends AbstractArenaObject {
    // Fields
    private boolean waitingOnTunnel;

    // Constructors
    protected AbstractMovableObject(final boolean pushable) {
	super(true, pushable, true);
	this.setSavedObject(new Empty());
	this.waitingOnTunnel = false;
	this.type.set(TypeConstants.TYPE_MOVABLE);
    }

    @Override
    public boolean canMove() {
	return this.isPushable();
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
    public boolean doLasersPassThrough() {
	return false;
    }

    @Override
    public int getCustomFormat() {
	return AbstractArenaObject.CUSTOM_FORMAT_MANUAL_OVERRIDE;
    }

    @Override
    public int getCustomProperty(final int propID) {
	return AbstractArenaObject.DEFAULT_CUSTOM_VALUE;
    }

    @Override
    public int getLayer() {
	return ArenaConstants.LAYER_LOWER_OBJECTS;
    }

    @Override
    public Direction laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	final Application app = LaserTank.getApplication();
	if (this.canMove()) {
	    if (forceUnits >= this.getMinimumReactionForce()) {
		try {
		    final AbstractArenaObject mof = app.getArenaManager().getArena().getCell(locX + dirX, locY + dirY,
			    locZ, this.getLayer());
		    final AbstractArenaObject mor = app.getArenaManager().getArena().getCell(locX - dirX, locY - dirY,
			    locZ, this.getLayer());
		    if (this.getMaterial() == MaterialConstants.MATERIAL_MAGNETIC) {
			if (laserType == LaserTypeConstants.LASER_TYPE_BLUE && mof != null
				&& (mof.isOfType(TypeConstants.TYPE_CHARACTER) || !mof.isSolid())) {
			    app.getGameManager().updatePushedPosition(locX, locY, locX - dirX, locY - dirY, this);
			    this.playSoundHook();
			} else if (mor != null && (mor.isOfType(TypeConstants.TYPE_CHARACTER) || !mor.isSolid())) {
			    app.getGameManager().updatePushedPosition(locX, locY, locX + dirX, locY + dirY, this);
			    this.playSoundHook();
			} else {
			    // Object doesn't react to this type of laser
			    return super.laserEnteredAction(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
			}
		    } else {
			if (laserType == LaserTypeConstants.LASER_TYPE_BLUE && mor != null
				&& (mor.isOfType(TypeConstants.TYPE_CHARACTER) || !mor.isSolid())) {
			    app.getGameManager().updatePushedPosition(locX, locY, locX - dirX, locY - dirY, this);
			    this.playSoundHook();
			} else if (mof != null && (mof.isOfType(TypeConstants.TYPE_CHARACTER) || !mof.isSolid())) {
			    app.getGameManager().updatePushedPosition(locX, locY, locX + dirX, locY + dirY, this);
			    this.playSoundHook();
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
	return Direction.NONE;
    }

    public abstract void playSoundHook();

    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	// Do nothing
    }

    @Override
    protected AbstractArenaObject readArenaObjectHookG2(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	this.setSavedObject(LaserTank.getApplication().getObjects().readArenaObjectG2(reader, formatVersion));
	return this;
    }

    @Override
    protected AbstractArenaObject readArenaObjectHookG3(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	this.setSavedObject(LaserTank.getApplication().getObjects().readArenaObjectG3(reader, formatVersion));
	return this;
    }

    @Override
    protected AbstractArenaObject readArenaObjectHookG4(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	this.setSavedObject(LaserTank.getApplication().getObjects().readArenaObjectG4(reader, formatVersion));
	return this;
    }

    @Override
    protected AbstractArenaObject readArenaObjectHookG5(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	this.setSavedObject(LaserTank.getApplication().getObjects().readArenaObjectG5(reader, formatVersion));
	return this;
    }

    @Override
    protected AbstractArenaObject readArenaObjectHookG6(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	this.setSavedObject(LaserTank.getApplication().getObjects().readArenaObjectG6(reader, formatVersion));
	return this;
    }

    @Override
    public void setCustomProperty(final int propID, final int value) {
	// Do nothing
    }

    public final void setWaitingOnTunnel(final boolean value) {
	this.waitingOnTunnel = value;
    }

    public final boolean waitingOnTunnel() {
	return this.waitingOnTunnel;
    }

    @Override
    protected void writeArenaObjectHook(final XMLFileWriter writer) throws IOException {
	this.getSavedObject().writeArenaObject(writer);
    }
}