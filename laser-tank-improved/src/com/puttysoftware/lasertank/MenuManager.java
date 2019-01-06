/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank;

import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.puttysoftware.lasertank.prefs.PreferencesManager;

public class MenuManager {
    // Fields
    private final JMenuBar mainMenuBar;
    private final ArrayList<MenuSection> modeMgrs;
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

    public void updateMenuItemState() {
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

    // Methods
    public JMenuBar getMainMenuBar() {
	return this.mainMenuBar;
    }

    public void populateMenuBar() {
	for (final MenuSection mgr : this.modeMgrs) {
	    final JMenu menu = mgr.createCommandsMenu();
	    mgr.attachAccelerators(this.accel);
	    mgr.setInitialState();
	    this.mainMenuBar.add(menu);
	}
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

    public void disableDirtyCommands() {
	for (final MenuSection mgr : this.modeMgrs) {
	    mgr.disableDirtyCommands();
	}
    }

    public void enableDirtyCommands() {
	for (final MenuSection mgr : this.modeMgrs) {
	    mgr.enableDirtyCommands();
	}
    }

    public void disableLoadedCommands() {
	for (final MenuSection mgr : this.modeMgrs) {
	    mgr.disableLoadedCommands();
	}
    }

    public void enableLoadedCommands() {
	for (final MenuSection mgr : this.modeMgrs) {
	    mgr.enableLoadedCommands();
	}
    }

    public void registerModeManager(final MenuSection mgr) {
	this.modeMgrs.add(mgr);
	final JMenu menu = mgr.createCommandsMenu();
	mgr.attachAccelerators(this.accel);
	mgr.setInitialState();
	this.mainMenuBar.add(menu);
    }

    public void toggleAccelerators() {
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
