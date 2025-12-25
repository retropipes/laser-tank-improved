/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.abstractobjects;

import java.awt.Color;
import java.io.IOException;

import com.puttysoftware.fileio.XMLFileReader;
import com.puttysoftware.fileio.XMLFileWriter;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.objects.Empty;
import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public abstract class AbstractCharacter extends AbstractArenaObject {
    // Fields
    private final int characterNumber;

    // Constructors
    protected AbstractCharacter(final int number) {
	super(true);
	this.setSavedObject(new Empty());
	this.activateTimer(1);
	this.type.set(TypeConstants.TYPE_CHARACTER);
	this.characterNumber = number;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!super.equals(obj)) {
	    return false;
	}
	if (!(obj instanceof AbstractCharacter)) {
	    return false;
	}
	final AbstractCharacter other = (AbstractCharacter) obj;
	if (this.characterNumber != other.characterNumber) {
	    return false;
	}
	return true;
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
    public String getCustomText() {
	return Integer.toString(this.characterNumber);
    }

    @Override
    public Color getCustomTextColor() {
	return Color.white;
    }

    @Override
    public int getLayer() {
	return ArenaConstants.LAYER_UPPER_OBJECTS;
    }

    // Methods
    public int getNumber() {
	return this.characterNumber;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + this.characterNumber;
	return result;
    }

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

    @Override
    public void timerExpiredAction(final int x, final int y) {
	if (this.getSavedObject() instanceof AbstractMovableObject) {
	    this.getSavedObject().timerExpiredAction(x, y);
	}
	this.activateTimer(1);
    }

    @Override
    protected void writeArenaObjectHook(final XMLFileWriter writer) throws IOException {
	this.getSavedObject().writeArenaObject(writer);
    }
}