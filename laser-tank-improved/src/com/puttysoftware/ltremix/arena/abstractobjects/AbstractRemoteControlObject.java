/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.utilities.ArenaConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public abstract class AbstractRemoteControlObject extends AbstractArenaObject {
    // Fields
    private int remoteX, remoteY;

    // Constructors
    protected AbstractRemoteControlObject() {
	super(false);
	this.type.set(TypeConstants.TYPE_REMOTE_CONTROL);
	this.remoteX = 0;
	this.remoteY = 0;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + this.remoteX;
	result = prime * result + this.remoteY;
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!super.equals(obj)) {
	    return false;
	}
	if (!(obj instanceof AbstractRemoteControlObject)) {
	    return false;
	}
	final AbstractRemoteControlObject other = (AbstractRemoteControlObject) obj;
	if (this.remoteX != other.remoteX) {
	    return false;
	}
	if (this.remoteY != other.remoteY) {
	    return false;
	}
	return true;
    }

    @Override
    public AbstractRemoteControlObject clone() {
	final AbstractRemoteControlObject copy = (AbstractRemoteControlObject) super.clone();
	copy.remoteX = this.remoteX;
	copy.remoteY = this.remoteY;
	return copy;
    }

    public int getRemoteX() {
	return this.remoteX;
    }

    public void setRemoteX(final int newRemoteX) {
	this.remoteX = newRemoteX;
    }

    public int getRemoteY() {
	return this.remoteY;
    }

    public void setRemoteY(final int newRemoteY) {
	this.remoteY = newRemoteY;
    }

    @Override
    public AbstractArenaObject editorPropertiesHook() {
	LTRemix.getApplication().getEditor().editRemoteController(this);
	return this;
    }

    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	// Do nothing
    }

    @Override
    public int getPrimaryLayer() {
	return ArenaConstants.LAYER_LOWER_OBJECTS;
    }

    @Override
    public int getCustomFormat() {
	return 2;
    }

    @Override
    public int getCustomProperty(final int propID) {
	switch (propID) {
	case 1:
	    return this.remoteX;
	case 2:
	    return this.remoteY;
	default:
	    return AbstractArenaObject.DEFAULT_CUSTOM_VALUE;
	}
    }

    @Override
    public void setCustomProperty(final int propID, final int value) {
	switch (propID) {
	case 1:
	    this.remoteX = value;
	    break;
	case 2:
	    this.remoteY = value;
	    break;
	default:
	    break;
	}
    }
}