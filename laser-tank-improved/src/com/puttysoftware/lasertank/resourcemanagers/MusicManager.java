/*  SharedX: An RPG
 Copyright (C) 2011-2012 Eric Ahnell

 Any questions should be directed to the author via email at: realmzxfamily@worldwizard.net
 */
package com.puttysoftware.lasertank.resourcemanagers;

import java.io.File;
import java.io.IOException;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.editor.ExternalMusic;
import com.puttysoftware.lasertank.improved.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.improved.sound.mod.MicroMod;

public class MusicManager {
    // Fields
    private static String EXTERNAL_LOAD_PATH = null;
    private static MicroMod CURRENT_EXTERNAL_MUSIC;
    private static ExternalMusic gameExternalMusic;

    // Constructors
    private MusicManager() {
	// Do nothing
    }

    private static MicroMod getMusic(final String filename) {
	try {
	    if (MusicManager.EXTERNAL_LOAD_PATH == null) {
		MusicManager.EXTERNAL_LOAD_PATH = LaserTank.getApplication().getArenaManager().getArena()
			.getArenaTempMusicFolder();
	    }
	    final File mfile = new File(MusicManager.EXTERNAL_LOAD_PATH + filename);
	    final MicroMod mmod = new MicroMod();
	    mmod.loadModule(mfile);
	    MusicManager.CURRENT_EXTERNAL_MUSIC = mmod;
	    return mmod;
	} catch (final NullPointerException np) {
	    return null;
	} catch (final IOException io) {
	    return null;
	}
    }

    public static boolean isMusicPlaying() {
	if (MusicManager.CURRENT_EXTERNAL_MUSIC != null) {
	    if (MusicManager.CURRENT_EXTERNAL_MUSIC.isPlayThreadAlive()) {
		return true;
	    }
	}
	return false;
    }

    public static void playMusic() {
	final AbstractArena a = LaserTank.getApplication().getArenaManager().getArena();
	final MicroMod mmod = MusicManager.getMusic(a.getMusicFilename());
	if (mmod != null) {
	    mmod.playModule();
	}
    }

    public static void loadPlayMusic(final String filename) {
	final MicroMod mmod = MusicManager.getMusic(filename);
	if (mmod != null) {
	    MusicManager.CURRENT_EXTERNAL_MUSIC = mmod;
	    mmod.playModule();
	}
    }

    public static void stopMusic() {
	if (MusicManager.isMusicPlaying()) {
	    MusicManager.CURRENT_EXTERNAL_MUSIC.stopModule();
	}
    }

    public static void arenaChanged() {
	MusicManager.EXTERNAL_LOAD_PATH = null;
    }

    // Methods
    public static ExternalMusic getExternalMusic() {
	if (MusicManager.gameExternalMusic == null) {
	    MusicManager.loadExternalMusic();
	}
	return MusicManager.gameExternalMusic;
    }

    public static void setExternalMusic(final ExternalMusic newExternalMusic) {
	MusicManager.gameExternalMusic = newExternalMusic;
    }

    public static void loadExternalMusic() {
	final AbstractArena a = LaserTank.getApplication().getArenaManager().getArena();
	final ExternalMusicLoadTask ellt = new ExternalMusicLoadTask(
		a.getArenaTempMusicFolder() + a.getMusicFilename());
	ellt.start();
	// Wait
	if (ellt.isAlive()) {
	    boolean waiting = true;
	    while (waiting) {
		try {
		    ellt.join();
		    waiting = false;
		} catch (final InterruptedException ie) {
		    // Ignore
		}
	    }
	}
    }

    public static void deleteExternalMusicFile() {
	final AbstractArena a = LaserTank.getApplication().getArenaManager().getArena();
	final File file = new File(a.getArenaTempMusicFolder() + a.getMusicFilename());
	file.delete();
    }

    public static void saveExternalMusic() {
	// Write external music
	final File extMusicDir = new File(
		LaserTank.getApplication().getArenaManager().getArena().getArenaTempMusicFolder());
	if (!extMusicDir.exists()) {
	    final boolean res = extMusicDir.mkdirs();
	    if (!res) {
		CommonDialogs.showErrorDialog("Save External Music Failed!", "External Music Editor");
		return;
	    }
	}
	final String filename = MusicManager.gameExternalMusic.getName();
	final String filepath = MusicManager.gameExternalMusic.getPath();
	final ExternalMusicSaveTask esst = new ExternalMusicSaveTask(filepath, filename);
	esst.start();
    }

    public static String getExtension(final File f) {
	String ext = null;
	final String s = f.getName();
	final int i = s.lastIndexOf('.');
	if (i > 0 && i < s.length() - 1) {
	    ext = s.substring(i + 1).toLowerCase();
	}
	return ext;
    }
}