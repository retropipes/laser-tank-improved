/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.puttysoftware.images.BufferedImageIcon;
import com.puttysoftware.lasertank.arena.ArenaManager;
import com.puttysoftware.lasertank.prefs.PreferencesManager;
import com.puttysoftware.lasertank.resourcemanagers.LogoManager;
import com.puttysoftware.lasertank.strings.CommonString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.strings.global.UntranslatedString;
import com.puttysoftware.lasertank.utilities.CleanupTask;

public class GUIManager implements QuitHandler {
    private class CloseHandler implements WindowListener {
	public CloseHandler() {
	    // Do nothing
	}

	@Override
	public void windowActivated(final WindowEvent arg0) {
	    // Do nothing
	}

	@Override
	public void windowClosed(final WindowEvent arg0) {
	    // Do nothing
	}

	@Override
	public void windowClosing(final WindowEvent arg0) {
	    if (GUIManager.this.quitHandler()) {
		System.exit(0);
	    }
	}

	@Override
	public void windowDeactivated(final WindowEvent arg0) {
	    // Do nothing
	}

	@Override
	public void windowDeiconified(final WindowEvent arg0) {
	    // Do nothing
	}

	@Override
	public void windowIconified(final WindowEvent arg0) {
	    // Do nothing
	}

	@Override
	public void windowOpened(final WindowEvent arg0) {
	    // Do nothing
	}
    }

    private class FocusHandler implements WindowFocusListener {
	public FocusHandler() {
	    // Do nothing
	}

	@Override
	public void windowGainedFocus(final WindowEvent e) {
	    LaserTank.getApplication().getMenuManager().updateMenuItemState();
	}

	@Override
	public void windowLostFocus(final WindowEvent e) {
	    // Do nothing
	}
    }

    // Fields
    private JLabel logoLabel;
    private final CloseHandler cHandler = new CloseHandler();
    private final FocusHandler fHandler = new FocusHandler();
    private final Container guiPane = new Container();

    // Constructors
    public GUIManager() {
	this.setUpGUI();
    }

    // Methods
    public boolean quitHandler() {
	final ArenaManager mm = LaserTank.getApplication().getArenaManager();
	boolean saved = true;
	int status = JOptionPane.DEFAULT_OPTION;
	if (mm.getDirty()) {
	    status = ArenaManager.showSaveDialog();
	    if (status == JOptionPane.YES_OPTION) {
		saved = mm.saveArena(mm.isArenaProtected());
	    } else if (status == JOptionPane.CANCEL_OPTION) {
		saved = false;
	    } else {
		mm.setDirty(false);
	    }
	}
	if (saved) {
	    PreferencesManager.writePrefs();
	    // Run cleanup task
	    CleanupTask.cleanUp();
	}
	return saved;
    }

    private void setUpGUI() {
	this.guiPane.setLayout(new GridLayout(1, 1));
	this.logoLabel = new JLabel(StringLoader.loadCommon(CommonString.EMPTY), null, SwingConstants.CENTER);
	this.logoLabel.setLabelFor(null);
	this.logoLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
	final BufferedImageIcon logo = LogoManager.getOpening();
	this.logoLabel.setIcon(logo);
	this.guiPane.add(this.logoLabel);
    }

    public void showGUI() {
	final Application app = LaserTank.getApplication();
	app.setInGUI(this.guiPane);
    }

    void updateLogo() {
	final Application app = LaserTank.getApplication();
	final BufferedImageIcon logo = LogoManager.getOpening();
	this.logoLabel.setIcon(logo);
	app.pack();
    }

    @Override
    public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
	boolean okToQuit = this.quitHandler();
	if (okToQuit) {
	    response.performQuit();
	} else {
	    response.cancelQuit();
	}
    }

    public void setUp() {
	final Application app = LaserTank.getApplication();
	app.setTitle(GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME));
	app.addWindowListener(this.cHandler);
	app.addWindowFocusListener(this.fHandler);
    }

    public void tearDown() {
	final Application app = LaserTank.getApplication();
	app.removeWindowListener(this.cHandler);
	app.removeWindowFocusListener(this.fHandler);
    }
}
