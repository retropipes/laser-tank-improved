/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractCharacter;
import com.puttysoftware.ltremix.editor.ArenaEditor;
import com.puttysoftware.ltremix.utilities.DirectionConstants;

public class Tank extends AbstractCharacter {
    // Constructors
    public Tank() {
	super(true, 0);
	this.setDirection(DirectionConstants.NORTH);
    }

    public Tank(final int dir, final int instance) {
	super(true, instance);
	this.setDirection(dir);
    }

    @Override
    public boolean editorPlaceHook(final int x, final int y, final int z) {
	final ArenaEditor me = LTRemix.getApplication().getEditor();
	me.setPlayerLocation();
	return false;
    }

    @Override
    public void editorRemoveHook(final int x, final int y, final int z) {
	final ArenaEditor me = LTRemix.getApplication().getEditor();
	me.clearPlayerLocation();
    }

    @Override
    public final int getStringBaseID() {
	return 36;
    }

    @Override
    public boolean isDirectional() {
	return true;
    }
}