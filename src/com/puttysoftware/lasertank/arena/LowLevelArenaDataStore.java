/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena;

import org.retropipes.diane.storage.ObjectStorage;

import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;

public class LowLevelArenaDataStore extends ObjectStorage<AbstractArenaObject> {
    // Constructor
    public LowLevelArenaDataStore(final int... shape) {
	super(shape);
    }

    // Methods
    public AbstractArenaObject getArenaDataCell(final int... loc) {
	return this.getCell(loc);
    }

    public void setArenaDataCell(final AbstractArenaObject obj, final int... loc) {
	this.setCell(obj, loc);
    }
}
