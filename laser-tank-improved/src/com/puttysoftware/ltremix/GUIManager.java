/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.puttysoftware.lasertank.improved.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.improved.images.BufferedImageIcon;
import com.puttysoftware.ltremix.arena.ArenaManager;
import com.puttysoftware.ltremix.prefs.PreferencesManager;
import com.puttysoftware.ltremix.resourcemanagers.LogoManager;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;
import com.puttysoftware.ltremix.utilities.BoardPrinter;
import com.puttysoftware.ltremix.utilities.CleanupTask;

public class GUIManager implements MenuSection {
    // Fields
    private final JFrame guiFrame;
    private final JLabel logoLabel;
    private JMenuItem fileNew, fileOpen, fileClose, fileSave, fileSaveAs, fileSaveAsProtected, filePrint,
	    filePreferences, fileExit;

    // Constructors
    public GUIManager() {
	final CloseHandler cHandler = new CloseHandler();
	final FocusHandler fHandler = new FocusHandler();
	this.guiFrame = new JFrame(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_PROGRAM_NAME));
	final Container guiPane = this.guiFrame.getContentPane();
	this.guiFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	this.guiFrame.setLayout(new GridLayout(1, 1));
	this.logoLabel = new JLabel(StringConstants.COMMON_STRING_EMPTY, null, SwingConstants.CENTER);
	this.logoLabel.setLabelFor(null);
	this.logoLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
	guiPane.add(this.logoLabel);
	this.guiFrame.setResizable(false);
	this.guiFrame.addWindowListener(cHandler);
	this.guiFrame.addWindowFocusListener(fHandler);
    }

    // Methods
    public JFrame getGUIFrame() {
	if (this.guiFrame.isVisible()) {
	    return this.guiFrame;
	} else {
	    return null;
	}
    }

    public void showGUI() {
	final Application app = LTRemix.getApplication();
	app.setInGUI();
	this.guiFrame.setJMenuBar(app.getMenuManager().getMainMenuBar());
	this.guiFrame.setVisible(true);
	this.guiFrame.pack();
	app.getMenuManager().checkFlags();
    }

    public void attachMenus() {
	final Application app = LTRemix.getApplication();
	this.guiFrame.setJMenuBar(app.getMenuManager().getMainMenuBar());
	app.getMenuManager().checkFlags();
    }

    public void hideGUI() {
	this.guiFrame.setVisible(false);
    }

    void updateLogo() {
	final BufferedImageIcon logo = LogoManager.getLogo();
	this.logoLabel.setIcon(logo);
	final Image iconlogo = LogoManager.getIconLogo();
	this.guiFrame.setIconImage(iconlogo);
	this.guiFrame.pack();
    }

    public boolean quitHandler() {
	final ArenaManager mm = LTRemix.getApplication().getArenaManager();
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
	    GUIManager.this.attachMenus();
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
		final Application app = LTRemix.getApplication();
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
			StringConstants.MENU_STRING_ITEM_CLOSE))) {
		    // Close the window
		    if (app.getMode() == Application.STATUS_EDITOR) {
			app.getEditor().handleCloseWindow();
		    } else if (app.getMode() == Application.STATUS_GAME) {
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
		    BoardPrinter.printBoard(app.getOutputFrame());
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
		app.getMenuManager().checkFlags();
	    } catch (final Exception ex) {
		LTRemix.getErrorLogger().logError(ex);
	    }
	}
    }

    @Override
    public void enableModeCommands() {
	this.fileNew.setEnabled(true);
	this.fileOpen.setEnabled(true);
	LTRemix.getApplication().getMenuManager().enableModeCommands();
    }

    @Override
    public void disableModeCommands() {
	this.fileNew.setEnabled(false);
	this.fileOpen.setEnabled(false);
	LTRemix.getApplication().getMenuManager().disableModeCommands();
    }

    @Override
    public void setInitialState() {
	this.fileNew.setEnabled(true);
	this.fileOpen.setEnabled(true);
	this.fileClose.setEnabled(false);
	this.fileSave.setEnabled(false);
	this.fileSaveAs.setEnabled(false);
	this.fileSaveAsProtected.setEnabled(false);
	this.filePreferences.setEnabled(true);
	this.filePrint.setEnabled(true);
	this.fileExit.setEnabled(true);
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
	this.fileClose.addActionListener(mhandler);
	this.fileSave.addActionListener(mhandler);
	this.fileSaveAs.addActionListener(mhandler);
	this.fileSaveAsProtected.addActionListener(mhandler);
	this.filePreferences.addActionListener(mhandler);
	this.filePrint.addActionListener(mhandler);
	this.fileExit.addActionListener(mhandler);
	fileMenu.add(this.fileNew);
	fileMenu.add(this.fileOpen);
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
    public void attachAccelerators(final Accelerators accel) {
	this.fileNew.setAccelerator(accel.fileNewAccel);
	this.fileOpen.setAccelerator(accel.fileOpenAccel);
	this.fileClose.setAccelerator(accel.fileCloseAccel);
	this.fileSave.setAccelerator(accel.fileSaveAccel);
	this.fileSaveAs.setAccelerator(accel.fileSaveAsAccel);
	this.filePreferences.setAccelerator(accel.filePreferencesAccel);
	this.filePrint.setAccelerator(accel.filePrintAccel);
	this.fileExit.setAccelerator(accel.fileExitAccel);
    }

    @Override
    public void enableLoadedCommands() {
	final Application app = LTRemix.getApplication();
	if (app.getMode() == Application.STATUS_GUI) {
	    this.fileClose.setEnabled(false);
	    this.fileSaveAs.setEnabled(false);
	    this.fileSaveAsProtected.setEnabled(false);
	} else {
	    this.fileClose.setEnabled(true);
	    this.fileSaveAs.setEnabled(true);
	    this.fileSaveAsProtected.setEnabled(true);
	}
	LTRemix.getApplication().getMenuManager().enableLoadedCommands();
    }

    @Override
    public void disableLoadedCommands() {
	this.fileClose.setEnabled(false);
	this.fileSaveAs.setEnabled(false);
	this.fileSaveAsProtected.setEnabled(false);
	LTRemix.getApplication().getMenuManager().disableLoadedCommands();
    }

    @Override
    public void enableDirtyCommands() {
	this.fileSave.setEnabled(true);
	LTRemix.getApplication().getMenuManager().enableDirtyCommands();
    }

    @Override
    public void disableDirtyCommands() {
	this.fileSave.setEnabled(false);
	LTRemix.getApplication().getMenuManager().disableDirtyCommands();
    }
}
