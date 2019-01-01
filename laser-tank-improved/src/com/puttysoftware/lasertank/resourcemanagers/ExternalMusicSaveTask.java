/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.resourcemanagers;

import java.io.File;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.improved.fileio.FileUtilities;

public class ExternalMusicSaveTask extends Thread {
    // Fields
    private final String filename;
    private final String pathname;

    // Constructors
    public ExternalMusicSaveTask(final String path, final String file) {
	this.filename = file;
	this.pathname = path;
	this.setName("External Music Writer");
    }

    @Override
    public void run() {
	try {
	    final String basePath = LaserTank.getApplication().getArenaManager().getArena().getArenaTempMusicFolder();
	    FileUtilities.copyFile(new File(this.pathname + this.filename),
		    new File(basePath + File.separator + this.filename.toLowerCase()));
	} catch (final Exception ex) {
	    LaserTank.getErrorLogger().logError(ex);
	}
    }
}
