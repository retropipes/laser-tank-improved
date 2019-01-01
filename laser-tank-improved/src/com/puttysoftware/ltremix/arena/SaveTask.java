/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena;

import java.io.File;
import java.io.FileNotFoundException;

import com.puttysoftware.lasertank.arena.ProtectionCancelException;
import com.puttysoftware.lasertank.improved.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.improved.fileio.ZipUtilities;
import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;
import com.puttysoftware.ltremix.utilities.Extension;
import com.puttysoftware.ltremix.utilities.ProgressTracker;

public class SaveTask extends Thread {
    // Fields
    private String filename;
    private final boolean saveProtected;
    private final boolean isSavedGame;
    private final ProgressTracker pt;
    private static final int MAX_PROGRESS = 2;

    // Constructors
    public SaveTask(final String file, final boolean saved, final boolean protect) {
	this.filename = file;
	this.isSavedGame = saved;
	this.saveProtected = protect;
	this.setName(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_NEW_AG_SAVER_NAME));
	this.pt = new ProgressTracker(
		StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE, StringConstants.DIALOG_STRING_SAVING));
	this.pt.setMaximum(SaveTask.MAX_PROGRESS + AbstractArena.getProgressStages());
    }

    @Override
    public void run() {
	final Application app = LTRemix.getApplication();
	// Lock state
	if (app.getMode() == Application.STATUS_GAME) {
	    app.getGameManager().getOutputFrame().setEnabled(false);
	} else if (app.getMode() == Application.STATUS_EDITOR) {
	    app.getEditor().getOutputFrame().setEnabled(false);
	} else if (app.getMode() == Application.STATUS_GUI) {
	    app.getGUIManager().getGUIFrame().setEnabled(false);
	}
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
	    // Show progress
	    this.pt.resetProgress();
	    this.pt.setMaximum(SaveTask.MAX_PROGRESS + AbstractArena.getProgressStages());
	    this.pt.show();
	    // Start
	    // Set prefix handler
	    app.getArenaManager().getArena().setPrefixHandler(new PrefixHandler());
	    // Set suffix handler
	    if (this.isSavedGame) {
		app.getArenaManager().getArena().setSuffixHandler(new SuffixHandler());
	    } else {
		app.getArenaManager().getArena().setSuffixHandler(null);
	    }
	    app.getArenaManager().getArena().writeArena(this.pt);
	    this.pt.updateProgress();
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
	    this.pt.updateProgress();
	} catch (final FileNotFoundException fnfe) {
	    if (this.isSavedGame) {
		CommonDialogs.showDialog(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
			StringConstants.DIALOG_STRING_GAME_SAVING_FAILED));
	    } else {
		CommonDialogs.showDialog(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
			StringConstants.DIALOG_STRING_ARENA_SAVING_FAILED));
	    }
	    success = false;
	} catch (final ProtectionCancelException pce) {
	    success = false;
	} catch (final Exception ex) {
	    LTRemix.getErrorLogger().logError(ex);
	} finally {
	    // Hide progress
	    this.pt.hide();
	    // Unlock state
	    if (app.getMode() == Application.STATUS_GAME) {
		app.getGameManager().getOutputFrame().setEnabled(true);
	    } else if (app.getMode() == Application.STATUS_EDITOR) {
		app.getEditor().getOutputFrame().setEnabled(true);
	    } else if (app.getMode() == Application.STATUS_GUI) {
		app.getGUIManager().getGUIFrame().setEnabled(true);
	    }
	}
	if (this.isSavedGame) {
	    LTRemix.getApplication().showMessage(StringLoader.loadString(StringConstants.MESSAGE_STRINGS_FILE,
		    StringConstants.MESSAGE_STRING_GAME_SAVED));
	} else {
	    LTRemix.getApplication().showMessage(StringLoader.loadString(StringConstants.MESSAGE_STRINGS_FILE,
		    StringConstants.MESSAGE_STRING_ARENA_SAVED));
	}
	app.getArenaManager().handleDeferredSuccess(success);
    }

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
}
