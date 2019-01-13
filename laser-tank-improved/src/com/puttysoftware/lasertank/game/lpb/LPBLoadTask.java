/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.game.lpb;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.strings.DialogString;
import com.puttysoftware.lasertank.strings.GameString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.strings.global.UntranslatedString;
import com.puttysoftware.lasertank.utilities.InvalidArenaException;

class LPBLoadTask extends Thread {
    // Fields
    private final String filename;
    private final JFrame loadFrame;

    // Constructors
    LPBLoadTask(final String file) {
	JProgressBar loadBar;
	this.filename = file;
	this.setName(GlobalLoader.loadUntranslated(UntranslatedString.PLAYBACK_LOADER_NAME));
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
	    LPBFile.loadLPB(arenaFile);
	    arenaFile.close();
	} catch (final FileNotFoundException fnfe) {
	    CommonDialogs.showDialog(StringLoader.loadGame(GameString.PLAYBACK_LOAD_FAILED));
	} catch (final IOException ioe) {
	    throw new InvalidArenaException(ioe);
	} catch (final Exception ex) {
	    LaserTank.logError(ex);
	} finally {
	    this.loadFrame.setVisible(false);
	}
    }
}
