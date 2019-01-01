/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipException;

import com.puttysoftware.lasertank.arena.ProtectionCancelException;
import com.puttysoftware.lasertank.improved.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.improved.fileio.ZipUtilities;
import com.puttysoftware.lasertank.utilities.InvalidArenaException;
import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;
import com.puttysoftware.ltremix.utilities.ProgressTracker;

public class LoadTask extends Thread {
    // Fields
    private final String filename;
    private final boolean isSavedGame;
    private final boolean arenaProtected;
    private final ProgressTracker pt;
    private static final int MAX_PROGRESS = 5;

    // Constructors
    public LoadTask(final String file, final boolean saved, final boolean protect) {
	this.filename = file;
	this.isSavedGame = saved;
	this.arenaProtected = protect;
	this.setName(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_NEW_AG_LOADER_NAME));
	this.pt = new ProgressTracker(
		StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE, StringConstants.DIALOG_STRING_LOADING));
	this.pt.setMaximum(LoadTask.MAX_PROGRESS + AbstractArena.getProgressStages());
    }

    // Methods
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
	// Show progress
	this.pt.resetProgress();
	this.pt.setMaximum(LoadTask.MAX_PROGRESS + AbstractArena.getProgressStages());
	this.pt.show();
	int startW;
	// Start
	if (this.isSavedGame) {
	    app.getGameManager().setSavedGameFlag(true);
	} else {
	    app.getGameManager().setSavedGameFlag(false);
	}
	try {
	    final File arenaFile = new File(this.filename);
	    final File tempLock = new File(AbstractArena.getArenaTempFolder() + "lock.tmp");
	    AbstractArena gameArena = ArenaManager.createArena();
	    if (this.arenaProtected) {
		// Attempt to unprotect the file
		ProtectionWrapper.unprotect(arenaFile, tempLock);
		try {
		    ZipUtilities.unzipDirectory(tempLock, new File(gameArena.getBasePath()));
		    app.getArenaManager().setArenaProtected(true);
		} catch (final ZipException ze) {
		    CommonDialogs.showErrorDialog(
			    StringLoader.loadString(StringConstants.ERROR_STRINGS_FILE,
				    StringConstants.ERROR_STRING_BAD_PROTECTION_KEY),
			    StringLoader.loadString(StringConstants.ERROR_STRINGS_FILE,
				    StringConstants.ERROR_STRING_PROTECTION));
		    app.getArenaManager().handleDeferredSuccess(false);
		    return;
		} finally {
		    tempLock.delete();
		}
	    } else {
		ZipUtilities.unzipDirectory(arenaFile, new File(gameArena.getBasePath()));
		app.getArenaManager().setArenaProtected(false);
	    }
	    this.pt.updateProgress();
	    // Set prefix handler
	    gameArena.setPrefixHandler(new PrefixHandler());
	    // Set suffix handler
	    if (this.isSavedGame) {
		gameArena.setSuffixHandler(new SuffixHandler());
	    } else {
		gameArena.setSuffixHandler(null);
	    }
	    gameArena = gameArena.readArena(this.pt);
	    if (gameArena == null) {
		throw new InvalidArenaException(StringLoader.loadString(StringConstants.ERROR_STRINGS_FILE,
			StringConstants.ERROR_STRING_UNKNOWN_OBJECT));
	    }
	    this.pt.updateProgress();
	    app.getArenaManager().setArena(gameArena);
	    startW = AbstractArena.getStartLevel();
	    gameArena.switchLevel(startW);
	    final boolean playerExists = gameArena.doesPlayerExist();
	    if (playerExists) {
		app.getGameManager().getPlayerManager().setPlayerLocation(gameArena.getStartColumn(),
			gameArena.getStartRow(), gameArena.getStartFloor());
	    }
	    if (!this.isSavedGame) {
		gameArena.save();
	    }
	    this.pt.updateProgress();
	    // Final cleanup
	    final String lum = app.getArenaManager().getLastUsedArena();
	    final String lug = app.getArenaManager().getLastUsedGame();
	    app.getArenaManager().clearLastUsedFilenames();
	    if (this.isSavedGame) {
		app.getArenaManager().setLastUsedGame(lug);
	    } else {
		app.getArenaManager().setLastUsedArena(lum);
	    }
	    this.pt.updateProgress();
	    gameArena.generateLevelInfoList(this.pt);
	    app.getEditor().arenaChanged();
	    this.pt.updateProgress();
	    if (this.isSavedGame) {
		CommonDialogs.showDialog(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
			StringConstants.DIALOG_STRING_GAME_LOADING_SUCCESS));
	    } else {
		CommonDialogs.showDialog(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
			StringConstants.DIALOG_STRING_ARENA_LOADING_SUCCESS));
	    }
	    app.getArenaManager().handleDeferredSuccess(true);
	} catch (final FileNotFoundException fnfe) {
	    if (this.isSavedGame) {
		CommonDialogs.showDialog(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
			StringConstants.DIALOG_STRING_GAME_LOADING_FAILED));
	    } else {
		CommonDialogs.showDialog(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
			StringConstants.DIALOG_STRING_ARENA_LOADING_FAILED));
	    }
	    app.getArenaManager().handleDeferredSuccess(false);
	} catch (final ProtectionCancelException pce) {
	    app.getArenaManager().handleDeferredSuccess(false);
	} catch (final IOException ie) {
	    if (this.isSavedGame) {
		CommonDialogs.showDialog(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
			StringConstants.DIALOG_STRING_GAME_LOADING_FAILED));
	    } else {
		CommonDialogs.showDialog(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
			StringConstants.DIALOG_STRING_ARENA_LOADING_FAILED));
	    }
	    LTRemix.getErrorLoggerDirectly().logNonFatalError(ie);
	    app.getArenaManager().handleDeferredSuccess(false);
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
    }
}
