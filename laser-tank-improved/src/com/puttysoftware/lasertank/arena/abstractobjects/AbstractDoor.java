/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.abstractobjects;

import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public abstract class AbstractDoor extends AbstractArenaObject {
    // Fields
    private AbstractKey key;

    // Constructors
    protected AbstractDoor(final AbstractKey mgk) {
	super(true);
	this.key = mgk;
	this.type.set(TypeConstants.TYPE_DOOR);
    }

    @Override
    public AbstractDoor clone() {
	final AbstractDoor copy = (AbstractDoor) super.clone();
	copy.key = (AbstractKey) this.key.clone();
	return copy;
    }

    @Override
    public boolean equals(final Object obj) {
	if (obj == null) {
	    return false;
	}
	if (this.getClass() != obj.getClass()) {
	    return false;
	}
	final AbstractDoor other = (AbstractDoor) obj;
	if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
	    return false;
	}
	return true;
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
    public int hashCode() {
	final int hash = 7;
	return 71 * hash + (this.key != null ? this.key.hashCode() : 0);
    }

    @Override
    public void setCustomProperty(final int propID, final int value) {
	// Do nothing
    }
}