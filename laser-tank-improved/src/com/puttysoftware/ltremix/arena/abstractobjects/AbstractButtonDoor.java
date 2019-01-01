/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.utilities.ArenaConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public abstract class AbstractButtonDoor extends AbstractArenaObject {
    // Constructors
    protected AbstractButtonDoor() {
	super(true);
	this.type.set(TypeConstants.TYPE_BUTTON_DOOR);
    }

    @Override
    public int getPrimaryLayer() {
	return ArenaConstants.LAYER_LOWER_OBJECTS;
    }

    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	// Do nothing
    }

    @Override
    public boolean editorPlaceHook(final int x, final int y, final int z) {
	final Application app = LTRemix.getApplication();
	app.getArenaManager().getArena().fullScanButtonBind(x, y, z, this);
	app.getEditor().redrawEditor(false);
	return true;
    }

    @Override
    public void editorRemoveHook(final int x, final int y, final int z) {
	final Application app = LTRemix.getApplication();
	app.getArenaManager().getArena().fullScanFindButtonLostDoor(z, this);
	app.getEditor().redrawEditor(false);
    }

    @Override
    public int getCustomProperty(final int propID) {
	return AbstractArenaObject.DEFAULT_CUSTOM_VALUE;
    }

    @Override
    public void setCustomProperty(final int propID, final int value) {
	// Do nothing
    }
}