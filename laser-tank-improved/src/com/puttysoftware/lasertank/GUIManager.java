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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.images.BufferedImageIcon;
import com.puttysoftware.lasertank.arena.ArenaManager;
import com.puttysoftware.lasertank.prefs.PreferencesManager;
import com.puttysoftware.lasertank.resourcemanagers.LogoManager;
import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;
import com.puttysoftware.lasertank.utilities.BoardPrinter;
import com.puttysoftware.lasertank.utilities.CleanupTask;

public class GUIManager implements MenuSection, QuitHandler {
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

    private class MenuHandler implements ActionListener {
	public MenuHandler() {
	    // Do nothing
	}

	// Handle menus
	@Override
	public void actionPerformed(final ActionEvent e) {
	    try {
		final Application app = LaserTank.getApplication();
		boolean loaded = false;
		final String cmd = e.getActionCommand();
		if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_NEW))) {
		    loaded = app.getEditor().newArena();
		    app.getArenaManager().setLoaded(loaded);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_OPEN))) {
		    loaded = app.getArenaManager().loadArena();
		    app.getArenaManager().setLoaded(loaded);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_OPEN_DEFAULT))) {
		    loaded = app.getArenaManager().loadArenaDefault();
		    app.getArenaManager().setLoaded(loaded);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_CLOSE))) {
		    // Close the window
		    if (app.isInEditorMode()) {
			app.getEditor().handleCloseWindow();
		    } else if (app.isInGameMode()) {
			boolean saved = true;
			int status = 0;
			if (app.getArenaManager().getDirty()) {
			    status = ArenaManager.showSaveDialog();
			    if (status == JOptionPane.YES_OPTION) {
				saved = app.getArenaManager().saveArena(app.getArenaManager().isArenaProtected());
			    } else if (status == JOptionPane.CANCEL_OPTION) {
				saved = false;
			    } else {
				app.getArenaManager().setDirty(false);
			    }
			}
			if (saved) {
			    app.getGameManager().exitGame();
			}
		    }
		    app.getGUIManager().showGUI();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_SAVE))) {
		    if (app.getArenaManager().getLoaded()) {
			app.getArenaManager().saveArena(app.getArenaManager().isArenaProtected());
		    } else {
			CommonDialogs.showDialog(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
				StringConstants.MENU_STRING_ERROR_NO_ARENA_OPENED));
		    }
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_SAVE_AS))) {
		    if (app.getArenaManager().getLoaded()) {
			app.getArenaManager().saveArenaAs(false);
		    } else {
			CommonDialogs.showDialog(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
				StringConstants.MENU_STRING_ERROR_NO_ARENA_OPENED));
		    }
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_SAVE_AS_PROTECTED))) {
		    if (app.getArenaManager().getLoaded()) {
			app.getArenaManager().saveArenaAs(true);
		    } else {
			CommonDialogs.showDialog(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
				StringConstants.MENU_STRING_ERROR_NO_ARENA_OPENED));
		    }
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_PREFERENCES))) {
		    // Show preferences dialog
		    PreferencesManager.showPrefs();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_PRINT_GAMEBOARD))) {
		    BoardPrinter.printBoard(app.getMasterContent());
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_EXIT))) {
		    // Exit program
		    if (app.getGUIManager().quitHandler()) {
			System.exit(0);
		    }
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_QUIT))) {
		    // Quit program
		    if (app.getGUIManager().quitHandler()) {
			System.exit(0);
		    }
		}
		app.getMenuManager().updateMenuItemState();
	    } catch (final Exception ex) {
		LaserTank.getErrorLogger().logError(ex);
	    }
	}
    }

    // Fields
    private JLabel logoLabel;
    private JMenuItem fileNew, fileOpen, fileOpenDefault, fileClose, fileSave, fileSaveAs, fileSaveAsProtected,
	    filePrint, filePreferences, fileExit;
    private final CloseHandler cHandler = new CloseHandler();
    private final FocusHandler fHandler = new FocusHandler();
    private final Container guiPane = new Container();

    // Constructors
    public GUIManager() {
	this.setUpGUI();
    }

    @Override
    public void attachAccelerators(final Accelerators accel) {
	this.fileNew.setAccelerator(accel.fileNewAccel);
	this.fileOpen.setAccelerator(accel.fileOpenAccel);
	this.fileClose.setAccelerator(accel.fileCloseAccel);
	this.fileSave.setAccelerator(accel.fileSaveAccel);
	this.fileSaveAs.setAccelerator(accel.fileSaveAsAccel);
	this.filePreferences.setAccelerator(accel.filePreferencesAccel);
	this.filePrint.setAccelerator(accel.filePrintAccel);
	if (System
		.getProperty(
			StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_OS_NAME))
		.contains(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			StringConstants.NOTL_STRING_WINDOWS))) {
	    this.fileExit.setAccelerator(null);
	} else {
	    this.fileExit.setAccelerator(accel.fileExitAccel);
	}
    }

    @Override
    public JMenu createCommandsMenu() {
	final MenuHandler mhandler = new MenuHandler();
	final JMenu fileMenu = new JMenu(
		StringLoader.loadString(StringConstants.MENU_STRINGS_FILE, StringConstants.MENU_STRING_MENU_FILE));
	this.fileNew = new JMenuItem(
		StringLoader.loadString(StringConstants.MENU_STRINGS_FILE, StringConstants.MENU_STRING_ITEM_NEW));
	this.fileOpen = new JMenuItem(
		StringLoader.loadString(StringConstants.MENU_STRINGS_FILE, StringConstants.MENU_STRING_ITEM_OPEN));
	this.fileOpenDefault = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_OPEN_DEFAULT));
	this.fileClose = new JMenuItem(
		StringLoader.loadString(StringConstants.MENU_STRINGS_FILE, StringConstants.MENU_STRING_ITEM_CLOSE));
	this.fileSave = new JMenuItem(
		StringLoader.loadString(StringConstants.MENU_STRINGS_FILE, StringConstants.MENU_STRING_ITEM_SAVE));
	this.fileSaveAs = new JMenuItem(
		StringLoader.loadString(StringConstants.MENU_STRINGS_FILE, StringConstants.MENU_STRING_ITEM_SAVE_AS));
	this.fileSaveAsProtected = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_SAVE_AS_PROTECTED));
	this.filePreferences = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_PREFERENCES));
	this.filePrint = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_PRINT_GAMEBOARD));
	if (System
		.getProperty(
			StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_OS_NAME))
		.contains(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			StringConstants.NOTL_STRING_WINDOWS))) {
	    this.fileExit = new JMenuItem(
		    StringLoader.loadString(StringConstants.MENU_STRINGS_FILE, StringConstants.MENU_STRING_ITEM_EXIT));
	} else {
	    this.fileExit = new JMenuItem(
		    StringLoader.loadString(StringConstants.MENU_STRINGS_FILE, StringConstants.MENU_STRING_ITEM_QUIT));
	}
	this.fileNew.addActionListener(mhandler);
	this.fileOpen.addActionListener(mhandler);
	this.fileOpenDefault.addActionListener(mhandler);
	this.fileClose.addActionListener(mhandler);
	this.fileSave.addActionListener(mhandler);
	this.fileSaveAs.addActionListener(mhandler);
	this.fileSaveAsProtected.addActionListener(mhandler);
	this.filePreferences.addActionListener(mhandler);
	this.filePrint.addActionListener(mhandler);
	this.fileExit.addActionListener(mhandler);
	fileMenu.add(this.fileNew);
	fileMenu.add(this.fileOpen);
	fileMenu.add(this.fileOpenDefault);
	fileMenu.add(this.fileClose);
	fileMenu.add(this.fileSave);
	fileMenu.add(this.fileSaveAs);
	fileMenu.add(this.fileSaveAsProtected);
	if (!System
		.getProperty(
			StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_OS_NAME))
		.equalsIgnoreCase(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			StringConstants.NOTL_STRING_MAC_OS_X))) {
	    fileMenu.add(this.filePreferences);
	}
	fileMenu.add(this.filePrint);
	if (!System
		.getProperty(
			StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_OS_NAME))
		.equalsIgnoreCase(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			StringConstants.NOTL_STRING_MAC_OS_X))) {
	    fileMenu.add(this.fileExit);
	}
	return fileMenu;
    }

    @Override
    public void disableDirtyCommands() {
	this.fileSave.setEnabled(false);
    }

    @Override
    public void disableLoadedCommands() {
	this.fileClose.setEnabled(false);
	this.fileSaveAs.setEnabled(false);
	this.fileSaveAsProtected.setEnabled(false);
    }

    @Override
    public void disableModeCommands() {
	this.fileNew.setEnabled(false);
	this.fileOpen.setEnabled(false);
	this.fileOpenDefault.setEnabled(false);
    }

    @Override
    public void enableDirtyCommands() {
	this.fileSave.setEnabled(true);
    }

    @Override
    public void enableLoadedCommands() {
	final Application app = LaserTank.getApplication();
	if (app.isInGUIMode()) {
	    this.fileClose.setEnabled(false);
	    this.fileSaveAs.setEnabled(false);
	    this.fileSaveAsProtected.setEnabled(false);
	} else {
	    this.fileClose.setEnabled(true);
	    this.fileSaveAs.setEnabled(true);
	    this.fileSaveAsProtected.setEnabled(true);
	}
    }

    @Override
    public void enableModeCommands() {
	this.fileNew.setEnabled(true);
	this.fileOpen.setEnabled(true);
	this.fileOpenDefault.setEnabled(true);
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

    @Override
    public void setInitialState() {
	this.fileNew.setEnabled(true);
	this.fileOpen.setEnabled(true);
	this.fileOpenDefault.setEnabled(true);
	this.fileClose.setEnabled(false);
	this.fileSave.setEnabled(false);
	this.fileSaveAs.setEnabled(false);
	this.fileSaveAsProtected.setEnabled(false);
	this.filePreferences.setEnabled(true);
	this.filePrint.setEnabled(true);
	this.fileExit.setEnabled(true);
    }

    private void setUpGUI() {
	this.guiPane.setLayout(new GridLayout(1, 1));
	this.logoLabel = new JLabel(StringConstants.COMMON_STRING_EMPTY, null, SwingConstants.CENTER);
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

    @Override
    public void setUp() {
	final Application app = LaserTank.getApplication();
	app.setTitle(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_PROGRAM_NAME));
	app.addWindowListener(this.cHandler);
	app.addWindowFocusListener(this.fHandler);
    }

    @Override
    public void tearDown() {
	final Application app = LaserTank.getApplication();
	app.removeWindowListener(this.cHandler);
	app.removeWindowFocusListener(this.fHandler);
    }
}
