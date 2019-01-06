package com.puttysoftware.lasertank;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;

public class PlayManager implements MenuSection {
    private JMenuItem playPlay, playEdit;
    private JCheckBoxMenuItem playToggleAccelerators;

    public PlayManager() {
	// Do nothing
    }

    private class EventHandler implements ActionListener {
	public EventHandler() {
	    // Do nothing
	}

	// Handle menus
	@Override
	public void actionPerformed(final ActionEvent e) {
	    try {
		final Application app = LaserTank.getApplication();
		final String cmd = e.getActionCommand();
		if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_PLAY))) {
		    // Play the current arena
		    final boolean proceed = app.getGameManager().newGame();
		    if (proceed) {
			app.exitCurrentMode();
			app.getGameManager().playArena();
		    }
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_EDIT))) {
		    // Edit the current arena
		    app.exitCurrentMode();
		    app.getEditor().editArena();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_USE_CLASSIC_ACCELERATORS))) {
		    // Toggle accelerators
		    app.getMenuManager().toggleAccelerators();
		}
		app.getMenuManager().updateMenuItemState();
	    } catch (final Exception ex) {
		LaserTank.getErrorLogger().logError(ex);
	    }
	}
    }

    @Override
    public void attachAccelerators(final Accelerators accel) {
	this.playPlay.setAccelerator(accel.playPlayArenaAccel);
	this.playEdit.setAccelerator(accel.playEditArenaAccel);
    }

    @Override
    public JMenu createCommandsMenu() {
	final EventHandler mhandler = new EventHandler();
	final JMenu playMenu = new JMenu(
		StringLoader.loadString(StringConstants.MENU_STRINGS_FILE, StringConstants.MENU_STRING_MENU_PLAY));
	this.playPlay = new JMenuItem(
		StringLoader.loadString(StringConstants.MENU_STRINGS_FILE, StringConstants.MENU_STRING_ITEM_PLAY));
	this.playEdit = new JMenuItem(
		StringLoader.loadString(StringConstants.MENU_STRINGS_FILE, StringConstants.MENU_STRING_ITEM_EDIT));
	this.playToggleAccelerators = new JCheckBoxMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_USE_CLASSIC_ACCELERATORS));
	this.playPlay.addActionListener(mhandler);
	this.playEdit.addActionListener(mhandler);
	this.playToggleAccelerators.addActionListener(mhandler);
	playMenu.add(this.playPlay);
	playMenu.add(this.playEdit);
	playMenu.add(this.playToggleAccelerators);
	return playMenu;
    }

    @Override
    public void disableDirtyCommands() {
	// Do nothing
    }

    @Override
    public void disableLoadedCommands() {
	this.playPlay.setEnabled(false);
	this.playEdit.setEnabled(false);
    }

    @Override
    public void disableModeCommands() {
	// Do nothing
    }

    @Override
    public void enableDirtyCommands() {
	// Do nothing
    }

    @Override
    public void enableLoadedCommands() {
	final Application app = LaserTank.getApplication();
	if (app.getArenaManager().getArena().doesPlayerExist(0)) {
	    this.playPlay.setEnabled(true);
	} else {
	    this.playPlay.setEnabled(false);
	}
	this.playEdit.setEnabled(true);
    }

    @Override
    public void enableModeCommands() {
	// Do nothing
    }

    @Override
    public void setInitialState() {
	this.playPlay.setEnabled(false);
	this.playEdit.setEnabled(false);
	this.playToggleAccelerators.setEnabled(true);
    }

    @Override
    public void setUp() {
	// Do nothing
    }

    @Override
    public void tearDown() {
	// Do nothing
    }
}
