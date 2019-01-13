/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.v4;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.arena.ArenaManager;
import com.puttysoftware.lasertank.strings.DialogString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.strings.global.UntranslatedString;

public class V4LevelLoadTask extends Thread {
    // Fields
    private final String filename;
    private final JFrame loadFrame;

    // Constructors
    public V4LevelLoadTask(final String file) {
	JProgressBar loadBar;
	this.filename = file;
	this.setName(GlobalLoader.loadUntranslated(UntranslatedString.OLD_AG_LOADER_NAME));
	this.loadFrame = new JFrame(StringLoader.loadDialog(DialogString.LOADING));
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
	app.getGameManager().setSavedGameFlag(false);
	try (FileInputStream arenaFile = new FileInputStream(this.filename)) {
	    final AbstractArena gameArena = ArenaManager.createArena();
	    V4File.loadOldFile(gameArena, arenaFile);
	    arenaFile.close();
	    app.getArenaManager().setArena(gameArena);
	    final boolean playerExists = gameArena.doesPlayerExist(0);
	    if (playerExists) {
		app.getGameManager().getPlayerManager().resetPlayerLocation();
	    }
	    gameArena.save();
	    // Final cleanup
	    final String lum = app.getArenaManager().getLastUsedArena();
	    app.getArenaManager().clearLastUsedFilenames();
	    app.getArenaManager().setLastUsedArena(lum);
	    app.updateLevelInfoList();
	    app.getEditor().arenaChanged();
	    CommonDialogs.showDialog(StringLoader.loadDialog(DialogString.ARENA_LOADING_SUCCESS));
	    app.getArenaManager().handleDeferredSuccess(true);
	} catch (final FileNotFoundException fnfe) {
	    CommonDialogs.showDialog(StringLoader.loadDialog(DialogString.ARENA_LOADING_FAILED));
	    app.getArenaManager().handleDeferredSuccess(false);
	} catch (final IOException ie) {
	    LaserTank.logNonFatalError(ie);
	    app.getArenaManager().handleDeferredSuccess(false);
	} catch (final Exception ex) {
	    LaserTank.logError(ex);
	} finally {
	    this.loadFrame.setVisible(false);
	}
    }
}
