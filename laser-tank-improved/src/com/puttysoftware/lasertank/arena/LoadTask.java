/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipException;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.improved.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.improved.fileio.ZipUtilities;
import com.puttysoftware.lasertank.resourcemanagers.LogoManager;
import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;
import com.puttysoftware.lasertank.utilities.InvalidArenaException;

public class LoadTask extends Thread {
    // Fields
    private final String filename;
    private final boolean isSavedGame;
    private final JFrame loadFrame;
    private final boolean arenaProtected;

    // Constructors
    public LoadTask(final String file, final boolean saved, final boolean protect) {
	JProgressBar loadBar;
	this.filename = file;
	this.isSavedGame = saved;
	this.arenaProtected = protect;
	this.setName(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_NEW_AG_LOADER_NAME));
	this.loadFrame = new JFrame(
		StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE, StringConstants.DIALOG_STRING_LOADING));
	this.loadFrame.setIconImage(LogoManager.getIconLogo());
	loadBar = new JProgressBar();
	loadBar.setIndeterminate(true);
	this.loadFrame.getContentPane().add(loadBar);
	this.loadFrame.setResizable(false);
	this.loadFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	this.loadFrame.pack();
    }

    // Methods
    @Override
    public void run() {
	this.loadFrame.setVisible(true);
	final Application app = LaserTank.getApplication();
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
	    // Set prefix handler
	    gameArena.setPrefixHandler(new PrefixHandler());
	    // Set suffix handler
	    if (this.isSavedGame) {
		gameArena.setSuffixHandler(new SuffixHandler());
	    } else {
		gameArena.setSuffixHandler(null);
	    }
	    gameArena = gameArena.readArena();
	    if (gameArena == null) {
		throw new InvalidArenaException(StringLoader.loadString(StringConstants.ERROR_STRINGS_FILE,
			StringConstants.ERROR_STRING_UNKNOWN_OBJECT));
	    }
	    app.getArenaManager().setArena(gameArena);
	    final boolean playerExists = gameArena.doesPlayerExist(0);
	    if (playerExists) {
		app.getGameManager().getPlayerManager().resetPlayerLocation();
	    }
	    if (!this.isSavedGame) {
		gameArena.save();
	    }
	    // Final cleanup
	    final String lum = app.getArenaManager().getLastUsedArena();
	    final String lug = app.getArenaManager().getLastUsedGame();
	    app.getArenaManager().clearLastUsedFilenames();
	    if (this.isSavedGame) {
		app.getArenaManager().setLastUsedGame(lug);
	    } else {
		app.getArenaManager().setLastUsedArena(lum);
	    }
	    app.getEditor().arenaChanged();
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
	    LaserTank.getErrorLoggerDirectly().logNonFatalError(ie);
	    app.getArenaManager().handleDeferredSuccess(false);
	} catch (final Exception ex) {
	    LaserTank.getErrorLogger().logError(ex);
	} finally {
	    this.loadFrame.setVisible(false);
	}
    }
}
