/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena;

import com.puttysoftware.lasertank.improved.CloneableObject;
import com.puttysoftware.lasertank.improved.storage.ObjectStorage;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;

public class LowLevelArenaDataStore extends ObjectStorage {
    // Constructor
    public LowLevelArenaDataStore(final int... shape) {
	super(shape);
    }

    // Methods
    @Override
    public Object clone() throws CloneNotSupportedException {
	final LowLevelArenaDataStore copy = new LowLevelArenaDataStore(this.getShape());
	for (int x = 0; x < copy.getRawLength(); x++) {
	    if (this.getRawCell(x) != null) {
		copy.setRawCell(((CloneableObject) this.getRawCell(x)).clone(), x);
	    }
	}
	return copy;
    }

    public AbstractArenaObject getArenaDataCell(final int... loc) {
	return (AbstractArenaObject) this.getCell(loc);
    }

    public void setArenaDataCell(final AbstractArenaObject obj, final int... loc) {
	this.setCell(obj, loc);
    }
}
