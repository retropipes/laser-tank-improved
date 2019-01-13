/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena;

import java.io.File;
import java.io.FileNotFoundException;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.fileio.ZipUtilities;
import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;
import com.puttysoftware.lasertank.utilities.Extension;

public class SaveTask extends Thread {
    private static boolean hasExtension(final String s) {
	String ext = null;
	final int i = s.lastIndexOf('.');
	if (i > 0 && i < s.length() - 1) {
	    ext = s.substring(i + 1).toLowerCase();
	}
	if (ext == null) {
	    return false;
	} else {
	    return true;
	}
    }

    // Fields
    private String filename;
    private final boolean saveProtected;
    private final boolean isSavedGame;

    // Constructors
    public SaveTask(final String file, final boolean saved, final boolean protect) {
	this.filename = file;
	this.isSavedGame = saved;
	this.saveProtected = protect;
	this.setName(StringLoader.loadString(StringConstants.STRINGS_FILE_GLOBAL,
		StringConstants.NOTL_STRING_NEW_AG_SAVER_NAME));
    }

    @Override
    public void run() {
	final Application app = LaserTank.getApplication();
	boolean success = true;
	// filename check
	final boolean hasExtension = SaveTask.hasExtension(this.filename);
	if (!hasExtension) {
	    if (this.isSavedGame) {
		this.filename += Extension.getGameExtensionWithPeriod();
	    } else {
		this.filename += Extension.getArenaExtensionWithPeriod();
	    }
	}
	final File arenaFile = new File(this.filename);
	final File tempLock = new File(AbstractArena.getArenaTempFolder() + "lock.tmp");
	try {
	    // Set prefix handler
	    app.getArenaManager().getArena().setPrefixHandler(new PrefixHandler());
	    // Set suffix handler
	    if (this.isSavedGame) {
		app.getArenaManager().getArena().setSuffixHandler(new SuffixHandler());
	    } else {
		app.getArenaManager().getArena().setSuffixHandler(null);
	    }
	    app.getArenaManager().getArena().writeArena();
	    if (this.saveProtected) {
		ZipUtilities.zipDirectory(new File(app.getArenaManager().getArena().getBasePath()), tempLock);
		// Protect the arena
		ProtectionWrapper.protect(tempLock, arenaFile);
		tempLock.delete();
		app.getArenaManager().setArenaProtected(true);
	    } else {
		ZipUtilities.zipDirectory(new File(app.getArenaManager().getArena().getBasePath()), arenaFile);
		app.getArenaManager().setArenaProtected(false);
	    }
	} catch (final FileNotFoundException fnfe) {
	    if (this.isSavedGame) {
		CommonDialogs.showDialog(StringLoader.loadString(StringConstants.STRINGS_FILE_DIALOG,
			StringConstants.DIALOG_STRING_GAME_SAVING_FAILED));
	    } else {
		CommonDialogs.showDialog(StringLoader.loadString(StringConstants.STRINGS_FILE_DIALOG,
			StringConstants.DIALOG_STRING_ARENA_SAVING_FAILED));
	    }
	    success = false;
	} catch (final ProtectionCancelException pce) {
	    success = false;
	} catch (final Exception ex) {
	    LaserTank.logError(ex);
	}
	if (this.isSavedGame) {
	    LaserTank.getApplication().showMessage(StringLoader.loadString(StringConstants.STRINGS_FILE_MESSAGE,
		    StringConstants.MESSAGE_STRING_GAME_SAVED));
	} else {
	    LaserTank.getApplication().showMessage(StringLoader.loadString(StringConstants.STRINGS_FILE_MESSAGE,
		    StringConstants.MESSAGE_STRING_ARENA_SAVED));
	}
	app.getArenaManager().handleDeferredSuccess(success);
    }
}
