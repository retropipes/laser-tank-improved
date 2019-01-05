/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.puttysoftware.lasertank.prefs.PreferencesManager;
import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;

public class MenuManager implements MenuSection {
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
		    MenuManager.this.toggleAccelerators();
		}
		app.getMenuManager().checkFlags();
	    } catch (final Exception ex) {
		LaserTank.getErrorLogger().logError(ex);
	    }
	}
    }

    // Fields
    private final JMenuBar mainMenuBar;
    private final ArrayList<MenuSection> modeMgrs;
    private JMenuItem playPlay, playEdit;
    private JCheckBoxMenuItem playToggleAccelerators;
    private Accelerators accel;

    // Constructors
    public MenuManager() {
	this.mainMenuBar = new JMenuBar();
	this.modeMgrs = new ArrayList<>();
	if (PreferencesManager.useClassicAccelerators()) {
	    this.accel = new ClassicAccelerators();
	} else {
	    this.accel = new ModernAccelerators();
	}
    }

    @Override
    public void attachAccelerators(final Accelerators newAccel) {
	this.playPlay.setAccelerator(this.accel.playPlayArenaAccel);
	this.playEdit.setAccelerator(this.accel.playEditArenaAccel);
    }

    public void checkFlags() {
	final Application app = LaserTank.getApplication();
	if (app.getArenaManager().getLoaded()) {
	    for (final MenuSection mgr : this.modeMgrs) {
		mgr.enableLoadedCommands();
	    }
	} else {
	    for (final MenuSection mgr : this.modeMgrs) {
		mgr.disableLoadedCommands();
	    }
	}
	if (app.getArenaManager().getDirty()) {
	    for (final MenuSection mgr : this.modeMgrs) {
		mgr.enableDirtyCommands();
	    }
	} else {
	    for (final MenuSection mgr : this.modeMgrs) {
		mgr.disableDirtyCommands();
	    }
	}
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

    // Methods
    public JMenuBar getMainMenuBar() {
	return this.mainMenuBar;
    }

    public void initMenus() {
	final JMenu menu = this.createCommandsMenu();
	this.attachAccelerators(this.accel);
	this.setInitialState();
	this.mainMenuBar.add(menu);
    }

    public void modeChanged(final MenuSection currentMgr) {
	for (final MenuSection mgr : this.modeMgrs) {
	    if (currentMgr == null || !currentMgr.getClass().equals(mgr.getClass())) {
		mgr.disableModeCommands();
	    } else {
		mgr.enableModeCommands();
	    }
	}
    }

    public void registerModeManager(final MenuSection mgr) {
	this.modeMgrs.add(mgr);
	final JMenu menu = mgr.createCommandsMenu();
	mgr.attachAccelerators(this.accel);
	mgr.setInitialState();
	this.mainMenuBar.add(menu);
    }

    @Override
    public void setInitialState() {
	this.playPlay.setEnabled(false);
	this.playEdit.setEnabled(false);
	this.playToggleAccelerators.setEnabled(true);
    }

    void toggleAccelerators() {
	if (this.accel instanceof ClassicAccelerators) {
	    this.accel = new ModernAccelerators();
	    PreferencesManager.setClassicAccelerators(false);
	} else {
	    this.accel = new ClassicAccelerators();
	    PreferencesManager.setClassicAccelerators(true);
	}
    }

    public void unregisterAllModeManagers() {
	this.modeMgrs.clear();
	this.mainMenuBar.removeAll();
    }
}
