/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.v4;

import java.io.FileInputStream;

import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.arena.current.CurrentArenaData;
import com.puttysoftware.lasertank.strings.ErrorString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.utilities.InvalidArenaException;

class V4File {
    static void loadOldFile(final AbstractArena a, final FileInputStream file) throws InvalidArenaException {
	CurrentArenaData t = null;
	int levelCount = 0;
	do {
	    a.switchLevel(levelCount);
	    t = V4FileLevel.loadAndConvert(file, a);
	    if (t != null) {
		levelCount++;
		a.setData(t, levelCount);
		final int[] found = a.findPlayer(1);
		if (found == null) {
		    throw new InvalidArenaException(StringLoader.loadError(ErrorString.TANK_LOCATION));
		} else {
		    a.setStartColumn(0, found[0]);
		    a.setStartRow(0, found[1]);
		    a.setStartFloor(0, found[2]);
		}
		a.save();
		a.switchLevel(levelCount);
	    }
	} while (t != null);
    }

    private V4File() {
	// Do nothing
    }
}
