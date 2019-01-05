/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractPassThroughObject;

public class Flag extends AbstractPassThroughObject {
    // Constructors
    public Flag() {
	super();
	this.setFrameNumber(1);
    }

    // Scriptability
    @Override
    public boolean defersSetProperties() {
	return false;
    }

    @Override
    public AbstractArenaObject editorPropertiesHook() {
	return null;
    }

    @Override
    public int getCustomFormat() {
	return 0;
    }

    @Override
    public final int getStringBaseID() {
	return 13;
    }

    @Override
    public boolean solvesOnMove() {
	return true;
    }
}