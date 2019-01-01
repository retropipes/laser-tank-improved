/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.resourcemanagers;

import java.io.File;

import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.editor.ExternalMusic;

public class ExternalMusicLoadTask extends Thread {
    // Fields
    private ExternalMusic gameExternalMusic;
    private final String filename;

    // Constructors
    public ExternalMusicLoadTask(final String file) {
	this.filename = file;
	this.setName("External Music Loader");
    }

    // Methods
    @Override
    public void run() {
	final Application app = LaserTank.getApplication();
	final AbstractArena a = app.getArenaManager().getArena();
	try {
	    this.gameExternalMusic = new ExternalMusic();
	    this.gameExternalMusic.setName(ExternalMusicLoadTask.getFileNameOnly(this.filename));
	    this.gameExternalMusic.setPath(a.getArenaTempMusicFolder());
	    MusicManager.setExternalMusic(this.gameExternalMusic);
	} catch (final Exception ex) {
	    LaserTank.getErrorLogger().logError(ex);
	}
    }

    private static String getFileNameOnly(final String s) {
	String fno = null;
	final int i = s.lastIndexOf(File.separatorChar);
	if (i > 0 && i < s.length() - 1) {
	    fno = s.substring(i + 1);
	} else {
	    fno = s;
	}
	return fno;
    }
}
