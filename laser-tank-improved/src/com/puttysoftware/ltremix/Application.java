/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix;

import javax.swing.JFrame;

import com.puttysoftware.lasertank.improved.ProductData;
import com.puttysoftware.lasertank.improved.dialogs.CommonDialogs;
import com.puttysoftware.ltremix.arena.ArenaManager;
import com.puttysoftware.ltremix.editor.ArenaEditor;
import com.puttysoftware.ltremix.game.GameManager;
import com.puttysoftware.ltremix.prefs.PreferencesManager;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;
import com.puttysoftware.ltremix.utilities.ArenaObjectList;

public final class Application {
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
    private String[] levelInfoList;
    private static final int VERSION_MAJOR = 7;
    private static final int VERSION_MINOR = 0;
    private static final int VERSION_BUGFIX = 0;
    private static final int VERSION_CODE = ProductData.CODE_ALPHA;
    private static final int VERSION_BETA = 1;
    public static final int STATUS_GUI = 0;
    public static final int STATUS_GAME = 1;
    public static final int STATUS_EDITOR = 2;
    public static final int STATUS_PREFS = 3;
    private static final int STATUS_NULL = 4;

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
	this.menuMgr.initMenus();
	this.menuMgr.registerModeManager(this.gameMgr);
	this.menuMgr.registerModeManager(this.editor);
	this.menuMgr.registerModeManager(this.about);
	// Fire hooks
	this.getHelpManager().activeLanguageChanged();
	this.getGameManager().activeLanguageChanged();
	this.getEditor().activeLanguageChanged();
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

    void setInGUI() {
	this.mode = Application.STATUS_GUI;
	this.menuMgr.modeChanged(this.guiMgr);
    }

    public void setInPrefs() {
	this.formerMode = this.mode;
	this.mode = Application.STATUS_PREFS;
	this.menuMgr.modeChanged(null);
    }

    public void setInGame() {
	this.mode = Application.STATUS_GAME;
	this.menuMgr.modeChanged(this.gameMgr);
    }

    public void setInEditor() {
	this.mode = Application.STATUS_EDITOR;
	this.menuMgr.modeChanged(this.editor);
    }

    public int getMode() {
	return this.mode;
    }

    public int getFormerMode() {
	return this.formerMode;
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

    public void showMessage(final String msg) {
	if (this.mode == Application.STATUS_EDITOR) {
	    this.getEditor().setStatusMessage(msg);
	} else if (this.mode == Application.STATUS_GAME) {
	    this.getGameManager().setStatusMessage(msg);
	} else {
	    CommonDialogs.showDialog(msg);
	}
    }

    public MenuManager getMenuManager() {
	return this.menuMgr;
    }

    public GUIManager getGUIManager() {
	return this.guiMgr;
    }

    public GameManager getGameManager() {
	return this.gameMgr;
    }

    public ArenaManager getArenaManager() {
	if (this.arenaMgr == null) {
	    this.arenaMgr = new ArenaManager();
	}
	return this.arenaMgr;
    }

    HelpManager getHelpManager() {
	if (this.helpMgr == null) {
	    this.helpMgr = new HelpManager();
	}
	return this.helpMgr;
    }

    public ArenaEditor getEditor() {
	return this.editor;
    }

    AboutDialog getAboutDialog() {
	return this.about;
    }

    private static String getVersionString() {
	if (Application.isAlphaModeEnabled(Application.VERSION_CODE)) {
	    return StringConstants.COMMON_STRING_EMPTY + Application.VERSION_MAJOR
		    + StringConstants.COMMON_STRING_NOTL_PERIOD + Application.VERSION_MINOR
		    + StringConstants.COMMON_STRING_NOTL_PERIOD + Application.VERSION_BUGFIX + StringLoader
			    .loadString(StringConstants.MESSAGE_STRINGS_FILE, StringConstants.MESSAGE_STRING_ALPHA)
		    + Application.VERSION_BETA;
	} else if (Application.isBetaModeEnabled(Application.VERSION_CODE)) {
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

    public ArenaObjectList getObjects() {
	return this.objects;
    }

    public String[] getLevelInfoList() {
	return this.levelInfoList;
    }

    public void updateLevelInfoList() {
	this.levelInfoList = this.arenaMgr.getArena().generateLevelInfoList(null);
    }

    private static boolean isAlphaModeEnabled(final int code) {
	return code == ProductData.CODE_ALPHA;
    }

    private static boolean isBetaModeEnabled(final int code) {
	return code == ProductData.CODE_BETA;
    }
}
