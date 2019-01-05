/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractCharacter;
import com.puttysoftware.lasertank.editor.ArenaEditor;
import com.puttysoftware.lasertank.utilities.Direction;

public class Tank extends AbstractCharacter {
    public Tank(final Direction dir, final int number) {
	super(number);
	this.setDirection(dir);
    }

    // Constructors
    public Tank(final int number) {
	super(number);
	this.setDirection(Direction.NORTH);
    }

    @Override
    public void editorPlaceHook(final int x, final int y, final int z) {
	final ArenaEditor me = LaserTank.getApplication().getEditor();
	me.setPlayerLocation();
    }

    @Override
    public final int getStringBaseID() {
	return 36;
    }
}