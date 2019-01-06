/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.arena.ArenaManager;
import com.puttysoftware.lasertank.editor.ArenaEditor;
import com.puttysoftware.lasertank.game.GameManager;
import com.puttysoftware.lasertank.prefs.PreferencesManager;
import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;
import com.puttysoftware.lasertank.utilities.ArenaObjectList;

public final class Application implements MenuSection {
    private static final int VERSION_MAJOR = 1;
    private static final int VERSION_MINOR = 0;
    private static final int VERSION_BUGFIX = 0;
    private static final int VERSION_BETA = 1;
    public static final int STATUS_GUI = 0;
    public static final int STATUS_GAME = 1;
    public static final int STATUS_EDITOR = 2;
    public static final int STATUS_PREFS = 3;
    public static final int STATUS_HELP = 4;
    private static final int STATUS_NULL = 5;

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

    public static String getLogoVersionString() {
	if (Application.isBetaModeEnabled()) {
	    return StringConstants.COMMON_STRING_LOGO_VERSION_PREFIX + Application.VERSION_MAJOR
		    + StringConstants.COMMON_STRING_NOTL_PERIOD + Application.VERSION_MINOR
		    + StringConstants.COMMON_STRING_NOTL_PERIOD + Application.VERSION_BUGFIX
		    + StringConstants.COMMON_STRING_BETA_SHORT + Application.VERSION_BETA;
	} else {
	    return StringConstants.COMMON_STRING_LOGO_VERSION_PREFIX + Application.VERSION_MAJOR
		    + StringConstants.COMMON_STRING_NOTL_PERIOD + Application.VERSION_MINOR
		    + StringConstants.COMMON_STRING_NOTL_PERIOD + Application.VERSION_BUGFIX;
	}
    }

    private static String getVersionString() {
	if (Application.isBetaModeEnabled()) {
	    return StringConstants.COMMON_STRING_EMPTY + Application.VERSION_MAJOR
		    + StringConstants.COMMON_STRING_NOTL_PERIOD + Application.VERSION_MINOR
		    + StringConstants.COMMON_STRING_NOTL_PERIOD + Application.VERSION_BUGFIX
		    + StringLoader.loadString(StringConstants.MESSAGE_STRINGS_FILE, StringConstants.MESSAGE_STRING_BETA)
		    + Application.VERSION_BETA;
	} else {
	    return StringConstants.COMMON_STRING_EMPTY + Application.VERSION_MAJOR
		    + StringConstants.COMMON_STRING_NOTL_PERIOD + Application.VERSION_MINOR
		    + StringConstants.COMMON_STRING_NOTL_PERIOD + Application.VERSION_BUGFIX;
	}
    }

    private static boolean isBetaModeEnabled() {
	return Application.VERSION_BETA > 0;
    }

    // Fields
    private AboutDialog about;
    private GameManager gameMgr;
    private ArenaManager arenaMgr;
    private MenuManager menuMgr;
    private HelpManager helpMgr;
    private ArenaEditor editor;
    private GUIManager guiMgr;
    private int mode, formerMode;
    private final ArenaObjectList objects;
    private JMenuItem playPlay, playEdit;
    private JCheckBoxMenuItem playToggleAccelerators;

    // Constructors
    public Application() {
	this.objects = new ArenaObjectList();
	this.mode = Application.STATUS_NULL;
	this.formerMode = Application.STATUS_NULL;
    }

    // Methods
    public void activeLanguageChanged() {
	// Rebuild menus
	this.menuMgr.unregisterAllModeManagers();
	this.menuMgr.registerModeManager(this.guiMgr);
	this.menuMgr.registerModeManager(this);
	this.menuMgr.registerModeManager(this.gameMgr);
	this.menuMgr.registerModeManager(this.editor);
	this.menuMgr.registerModeManager(this.about);
	this.menuMgr.populateMenuBar();
	// Fire hooks
	this.getHelpManager().activeLanguageChanged();
	this.getGameManager().activeLanguageChanged();
	this.getEditor().activeLanguageChanged();
    }

    void exitCurrentMode() {
	if (this.mode == Application.STATUS_GUI) {
	    this.guiMgr.hideGUI();
	} else if (this.mode == Application.STATUS_GAME) {
	    this.gameMgr.exitGame();
	} else if (this.mode == Application.STATUS_EDITOR) {
	    this.editor.exitEditor();
	}
    }

    AboutDialog getAboutDialog() {
	return this.about;
    }

    public ArenaManager getArenaManager() {
	if (this.arenaMgr == null) {
	    this.arenaMgr = new ArenaManager();
	}
	return this.arenaMgr;
    }

    public ArenaEditor getEditor() {
	return this.editor;
    }

    public int getFormerMode() {
	return this.formerMode;
    }

    public GameManager getGameManager() {
	return this.gameMgr;
    }

    public GUIManager getGUIManager() {
	return this.guiMgr;
    }

    HelpManager getHelpManager() {
	return this.helpMgr;
    }

    public String[] getLevelInfoList() {
	return this.arenaMgr.getArena().getLevelInfoList();
    }

    public MenuManager getMenuManager() {
	return this.menuMgr;
    }

    public int getMode() {
	return this.mode;
    }

    public ArenaObjectList getObjects() {
	return this.objects;
    }

    public JFrame getOutputFrame() {
	try {
	    if (this.getMode() == Application.STATUS_PREFS) {
		return PreferencesManager.getPrefFrame();
	    } else if (this.getMode() == Application.STATUS_GUI) {
		return this.getGUIManager().getGUIFrame();
	    } else if (this.getMode() == Application.STATUS_GAME) {
		return this.getGameManager().getOutputFrame();
	    } else if (this.getMode() == Application.STATUS_EDITOR) {
		return this.getEditor().getOutputFrame();
	    } else {
		return null;
	    }
	} catch (final NullPointerException npe) {
	    return null;
	}
    }

    void postConstruct() {
	// Create Managers
	this.menuMgr = new MenuManager();
	this.about = new AboutDialog(Application.getVersionString());
	this.guiMgr = new GUIManager();
	this.helpMgr = new HelpManager();
	this.gameMgr = new GameManager();
	this.editor = new ArenaEditor();
	// Cache Logo
	this.guiMgr.updateLogo();
    }

    public void setInEditor() {
	this.mode = Application.STATUS_EDITOR;
	this.menuMgr.modeChanged(this.editor);
    }

    public void setInGame() {
	this.mode = Application.STATUS_GAME;
	this.menuMgr.modeChanged(this.gameMgr);
    }

    void setInGUI() {
	this.mode = Application.STATUS_GUI;
	this.menuMgr.modeChanged(this.guiMgr);
    }

    public void setInHelp() {
	this.formerMode = this.mode;
	this.mode = Application.STATUS_HELP;
	this.menuMgr.modeChanged(null);
    }

    public void setInPrefs() {
	this.formerMode = this.mode;
	this.mode = Application.STATUS_PREFS;
	this.menuMgr.modeChanged(null);
    }

    public void showMessage(final String msg) {
	if (this.mode == Application.STATUS_EDITOR) {
	    this.getEditor().setStatusMessage(msg);
	} else {
	    CommonDialogs.showDialog(msg);
	}
    }

    public void updateLevelInfoList() {
	JFrame loadFrame;
	JProgressBar loadBar;
	loadFrame = new JFrame(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
		StringConstants.DIALOG_STRING_UPDATING_LEVEL_INFO));
	loadBar = new JProgressBar();
	loadBar.setIndeterminate(true);
	loadBar.setPreferredSize(new Dimension(600, 20));
	loadFrame.getContentPane().add(loadBar);
	loadFrame.setResizable(false);
	loadFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	loadFrame.pack();
	loadFrame.setVisible(true);
	this.arenaMgr.getArena().generateLevelInfoList();
	loadFrame.setVisible(false);
    }
}
