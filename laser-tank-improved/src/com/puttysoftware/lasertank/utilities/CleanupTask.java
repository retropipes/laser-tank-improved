/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import java.io.File;

import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.improved.fileio.DirectoryUtilities;

public class CleanupTask {
    private CleanupTask() {
	// Do nothing
    }

    public static void cleanUp() {
	try {
	    final File dirToDelete = new File(AbstractArena.getArenaTempFolder());
	    DirectoryUtilities.removeDirectory(dirToDelete);
	} catch (final Throwable t) {
	    // Ignore
	}
    }
}
