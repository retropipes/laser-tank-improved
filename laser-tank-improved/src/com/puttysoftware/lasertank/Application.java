/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.integration.NativeIntegration;
import com.puttysoftware.lasertank.arena.ArenaManager;
import com.puttysoftware.lasertank.editor.ArenaEditor;
import com.puttysoftware.lasertank.game.GameManager;
import com.puttysoftware.lasertank.strings.CommonString;
import com.puttysoftware.lasertank.strings.DialogString;
import com.puttysoftware.lasertank.strings.MessageString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.strings.global.UntranslatedString;
import com.puttysoftware.lasertank.utilities.ArenaObjectList;

public final class Application {
    private static final int VERSION_MAJOR = 0;
    private static final int VERSION_MINOR = 0;
    private static final int VERSION_BUGFIX = 0;
    private static final int VERSION_BETA = 2;
    private static final int STATUS_GUI = 0;
    private static final int STATUS_GAME = 1;
    private static final int STATUS_EDITOR = 2;
    private static final int STATUS_NULL = 3;

    public static String getLogoVersionString() {
	if (Application.isBetaModeEnabled()) {
	    return StringLoader.loadCommon(CommonString.LOGO_VERSION_PREFIX) + Application.VERSION_MAJOR
		    + StringLoader.loadCommon(CommonString.NOTL_PERIOD) + Application.VERSION_MINOR
		    + StringLoader.loadCommon(CommonString.NOTL_PERIOD) + Application.VERSION_BUGFIX
		    + StringLoader.loadCommon(CommonString.BETA_SHORT) + Application.VERSION_BETA;
	} else {
	    return StringLoader.loadCommon(CommonString.LOGO_VERSION_PREFIX) + Application.VERSION_MAJOR
		    + StringLoader.loadCommon(CommonString.NOTL_PERIOD) + Application.VERSION_MINOR
		    + StringLoader.loadCommon(CommonString.NOTL_PERIOD) + Application.VERSION_BUGFIX;
	}
    }

    private static String getVersionString() {
	if (Application.isBetaModeEnabled()) {
	    return StringLoader.loadCommon(CommonString.EMPTY) + Application.VERSION_MAJOR
		    + StringLoader.loadCommon(CommonString.NOTL_PERIOD) + Application.VERSION_MINOR
		    + StringLoader.loadCommon(CommonString.NOTL_PERIOD) + Application.VERSION_BUGFIX
		    + StringLoader.loadMessage(MessageString.BETA) + Application.VERSION_BETA;
	} else {
	    return StringLoader.loadCommon(CommonString.EMPTY) + Application.VERSION_MAJOR
		    + StringLoader.loadCommon(CommonString.NOTL_PERIOD) + Application.VERSION_MINOR
		    + StringLoader.loadCommon(CommonString.NOTL_PERIOD) + Application.VERSION_BUGFIX;
	}
    }

    private static boolean isBetaModeEnabled() {
	return Application.VERSION_BETA > 0;
    }

    // Fields
    private final JFrame masterFrame;
    private AboutDialog about;
    private GameManager gameMgr;
    private ArenaManager arenaMgr;
    private MenuManager menuMgr;
    private ArenaEditor editor;
    private GUIManager guiMgr;
    private int mode, formerMode;
    private final ArenaObjectList objects;
    private NativeIntegration ni;

    // Constructors
    public Application(final NativeIntegration newNI) {
	this.ni = newNI;
	this.masterFrame = new JFrame();
	this.masterFrame.setResizable(false);
	this.objects = new ArenaObjectList();
	this.mode = Application.STATUS_NULL;
	this.formerMode = Application.STATUS_NULL;
    }

    void init() {
	// Create Managers
	this.menuMgr = new MenuManager();
	this.about = new AboutDialog(Application.getVersionString());
	this.guiMgr = new GUIManager();
	this.gameMgr = new GameManager();
	this.editor = new ArenaEditor();
	// Cache Logo
	this.guiMgr.updateLogo();
    }

    void bootGUI() {
	// Set up default error handling
	UncaughtExceptionHandler eh = new UncaughtExceptionHandler() {
	    @Override
	    public void uncaughtException(Thread t, Throwable e) {
		LaserTank.logNonFatalError(e);
	    }
	};
	Runnable doRun = new Runnable() {
	    @Override
	    public void run() {
		Thread.currentThread().setUncaughtExceptionHandler(eh);
	    }
	};
	try {
	    SwingUtilities.invokeAndWait(doRun);
	} catch (InvocationTargetException | InterruptedException e) {
	    LaserTank.logError(e);
	}
	// Boot GUI
	this.guiMgr.showGUI();
    }

    // Methods
    public void activeLanguageChanged() {
	// Rebuild menus
	this.menuMgr.populateMenuBar();
	this.ni.setDefaultMenuBar(this.menuMgr.getMenuBar(), this.masterFrame);
	// Fire hooks
	this.getGameManager().activeLanguageChanged();
	this.getEditor().activeLanguageChanged();
    }

    void exitCurrentMode() {
	if (this.mode == Application.STATUS_GUI) {
	    this.guiMgr.tearDown();
	} else if (this.mode == Application.STATUS_GAME) {
	    this.gameMgr.tearDown();
	} else if (this.mode == Application.STATUS_EDITOR) {
	    this.editor.tearDown();
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

    public String[] getLevelInfoList() {
	return this.arenaMgr.getArena().getLevelInfoList();
    }

    public MenuManager getMenuManager() {
	return this.menuMgr;
    }

    public boolean isInGameMode() {
	return this.mode == Application.STATUS_GAME;
    }

    public boolean isInEditorMode() {
	return this.mode == Application.STATUS_EDITOR;
    }

    public boolean isInGUIMode() {
	return this.mode == Application.STATUS_GUI;
    }

    public ArenaObjectList getObjects() {
	return this.objects;
    }

    public void setInEditor(final Container masterContent) {
	this.formerMode = this.mode;
	this.mode = Application.STATUS_EDITOR;
	this.tearDownFormerMode();
	this.editor.setUp();
	this.menuMgr.activateEditorCommands();
	this.masterFrame.setContentPane(masterContent);
    }

    public void setInGame(final Container masterContent) {
	this.formerMode = this.mode;
	this.mode = Application.STATUS_GAME;
	this.tearDownFormerMode();
	this.gameMgr.setUp();
	this.menuMgr.activateGameCommands();
	this.masterFrame.setContentPane(masterContent);
    }

    void setInGUI(final Container masterContent) {
	this.formerMode = this.mode;
	this.mode = Application.STATUS_GUI;
	this.tearDownFormerMode();
	this.guiMgr.setUp();
	this.menuMgr.activateGUICommands();
	this.masterFrame.setContentPane(masterContent);
	if (!this.masterFrame.isVisible()) {
	    this.masterFrame.setVisible(true);
	    this.masterFrame.pack();
	}
    }

    public Container getMasterContent() {
	return this.masterFrame.getContentPane();
    }

    public void setTitle(final String title) {
	this.masterFrame.setTitle(title);
    }

    public void updateDirtyWindow(final boolean appDirty) {
	this.masterFrame.getRootPane().putClientProperty(
		GlobalLoader.loadUntranslated(UntranslatedString.WINDOW_MODIFIED), Boolean.valueOf(appDirty));
    }

    public void pack() {
	this.masterFrame.pack();
    }

    public void addWindowListener(final WindowListener l) {
	this.masterFrame.addWindowListener(l);
    }

    public void addWindowFocusListener(final WindowFocusListener l) {
	this.masterFrame.addWindowFocusListener(l);
    }

    public void addKeyListener(final KeyListener l) {
	this.masterFrame.addKeyListener(l);
    }

    public void removeWindowListener(final WindowListener l) {
	this.masterFrame.removeWindowListener(l);
    }

    public void removeWindowFocusListener(final WindowFocusListener l) {
	this.masterFrame.removeWindowFocusListener(l);
    }

    public void removeKeyListener(final KeyListener l) {
	this.masterFrame.removeKeyListener(l);
    }

    public void showMessage(final String msg) {
	if (this.mode == Application.STATUS_EDITOR) {
	    this.getEditor().setStatusMessage(msg);
	} else {
	    CommonDialogs.showDialog(msg);
	}
    }

    private void tearDownFormerMode() {
	if (this.formerMode == Application.STATUS_GUI) {
	    this.getGUIManager().tearDown();
	} else if (this.formerMode == Application.STATUS_GAME) {
	    this.getGameManager().tearDown();
	} else if (this.formerMode == Application.STATUS_EDITOR) {
	    this.getEditor().tearDown();
	}
    }

    public void updateLevelInfoList() {
	JFrame loadFrame;
	JProgressBar loadBar;
	loadFrame = new JFrame(StringLoader.loadDialog(DialogString.UPDATING_LEVEL_INFO));
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
