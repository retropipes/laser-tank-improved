/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.abstractobjects;

import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public abstract class AbstractButtonDoor extends AbstractArenaObject {
    // Constructors
    protected AbstractButtonDoor() {
	super(true);
	this.type.set(TypeConstants.TYPE_BUTTON_DOOR);
    }

    @Override
    public void editorPlaceHook(final int x, final int y, final int z) {
	final Application app = LaserTank.getApplication();
	app.getArenaManager().getArena().fullScanButtonBind(x, y, z, this);
	app.getEditor().redrawEditor();
    }

    @Override
    public void editorRemoveHook(final int x, final int y, final int z) {
	final Application app = LaserTank.getApplication();
	app.getArenaManager().getArena().fullScanFindButtonLostDoor(z, this);
	app.getEditor().redrawEditor();
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
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	// Do nothing
    }

    @Override
    public void setCustomProperty(final int propID, final int value) {
	// Do nothing
    }
}