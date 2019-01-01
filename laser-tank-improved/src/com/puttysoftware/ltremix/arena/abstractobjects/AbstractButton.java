/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.utilities.ArenaConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public abstract class AbstractButton extends AbstractArenaObject {
    // Fields
    private boolean triggered;
    private int doorX, doorY;
    private final AbstractButtonDoor buttonDoor;
    private final boolean universal;

    // Constructors
    protected AbstractButton(final AbstractButtonDoor bd, final boolean isUniversal) {
	super(false);
	this.triggered = false;
	this.doorX = -1;
	this.doorY = -1;
	this.buttonDoor = bd;
	this.universal = isUniversal;
	this.type.set(TypeConstants.TYPE_BUTTON);
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + (this.buttonDoor == null ? 0 : this.buttonDoor.hashCode());
	result = prime * result + this.doorX;
	result = prime * result + this.doorY;
	result = prime * result + (this.triggered ? 1231 : 1237);
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
	if (!(obj instanceof AbstractButton)) {
	    return false;
	}
	final AbstractButton other = (AbstractButton) obj;
	if (this.buttonDoor == null) {
	    if (other.buttonDoor != null) {
		return false;
	    }
	} else if (!this.buttonDoor.equals(other.buttonDoor)) {
	    return false;
	}
	if (this.doorX != other.doorX) {
	    return false;
	}
	if (this.doorY != other.doorY) {
	    return false;
	}
	if (this.triggered != other.triggered) {
	    return false;
	}
	return true;
    }

    @Override
    public AbstractButton clone() {
	final AbstractButton copy = (AbstractButton) super.clone();
	copy.triggered = this.triggered;
	copy.doorX = this.doorX;
	copy.doorY = this.doorY;
	return copy;
    }

    public boolean boundButtonDoorEquals(final AbstractButton other) {
	if (this == other) {
	    return true;
	}
	if (this.buttonDoor == null) {
	    if (other.buttonDoor != null) {
		return false;
	    }
	} else if (!this.buttonDoor.getClass().equals(other.buttonDoor.getClass())) {
	    return false;
	}
	return true;
    }

    public boolean boundToSameButtonDoor(final AbstractButtonDoor other) {
	if (this.buttonDoor == null) {
	    if (other != null) {
		return false;
	    }
	} else if (!this.buttonDoor.getClass().equals(other.getClass())) {
	    return false;
	}
	return true;
    }

    public boolean isTriggered() {
	return this.triggered;
    }

    public void setTriggered(final boolean isTriggered) {
	this.triggered = isTriggered;
    }

    public int getDoorX() {
	return this.doorX;
    }

    public void setDoorX(final int newDoorX) {
	this.doorX = newDoorX;
    }

    public int getDoorY() {
	return this.doorY;
    }

    public void setDoorY(final int newDoorY) {
	this.doorY = newDoorY;
    }

    public final boolean isUniversal() {
	return this.universal;
    }

    public final AbstractButtonDoor getButtonDoor() {
	return this.buttonDoor;
    }

    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	// Do nothing
    }

    @Override
    public boolean editorPlaceHook(final int x, final int y, final int z) {
	final Application app = LTRemix.getApplication();
	final int[] loc = app.getArenaManager().getArena().findObject(z, this.getButtonDoor().getBaseName());
	if (loc != null) {
	    this.setDoorX(loc[0]);
	    this.setDoorY(loc[1]);
	    this.setTriggered(false);
	}
	if (this instanceof AbstractTriggerButton || this instanceof AbstractPressureButton) {
	    app.getArenaManager().getArena().fullScanButtonCleanup(x, y, z, this);
	}
	app.getEditor().redrawEditor(false);
	return true;
    }

    @Override
    public int getPrimaryLayer() {
	return ArenaConstants.LAYER_LOWER_OBJECTS;
    }

    @Override
    public abstract boolean pushIntoAction(final AbstractMovableObject pushed, final int x, final int y, final int z);

    @Override
    public abstract void pushOutAction(final AbstractMovableObject pushed, final int x, final int y, final int z);

    @Override
    public int getCustomFormat() {
	return 3;
    }

    @Override
    public int getCustomProperty(final int propID) {
	switch (propID) {
	case 1:
	    return this.doorX;
	case 2:
	    return this.doorY;
	case 3:
	    return this.triggered ? 1 : 0;
	default:
	    return AbstractArenaObject.DEFAULT_CUSTOM_VALUE;
	}
    }

    @Override
    public void setCustomProperty(final int propID, final int value) {
	switch (propID) {
	case 1:
	    this.doorX = value;
	    break;
	case 2:
	    this.doorY = value;
	    break;
	case 3:
	    this.triggered = value == 1 ? true : false;
	    break;
	default:
	    break;
	}
    }
}
