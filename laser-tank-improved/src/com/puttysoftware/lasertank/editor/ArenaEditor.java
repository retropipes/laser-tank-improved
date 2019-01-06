/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.editor;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.images.BufferedImageIcon;
import com.puttysoftware.lasertank.Accelerators;
import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.MenuSection;
import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.arena.ArenaManager;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractJumpObject;
import com.puttysoftware.lasertank.arena.objects.Ground;
import com.puttysoftware.lasertank.arena.objects.Tank;
import com.puttysoftware.lasertank.game.GameManager;
import com.puttysoftware.lasertank.prefs.PreferencesManager;
import com.puttysoftware.lasertank.resourcemanagers.ImageManager;
import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;
import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.ArenaObjectList;
import com.puttysoftware.lasertank.utilities.DrawGrid;
import com.puttysoftware.lasertank.utilities.EditorLayoutConstants;
import com.puttysoftware.lasertank.utilities.InvalidArenaException;
import com.puttysoftware.lasertank.utilities.RCLGenerator;
import com.puttysoftware.pickers.PicturePicker;

public class ArenaEditor implements MenuSection {
    private class EventHandler implements MouseListener, MouseMotionListener, WindowListener {
	// handle scroll bars
	public EventHandler() {
	    // Do nothing
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	    try {
		final ArenaEditor me = ArenaEditor.this;
		final int x = e.getX();
		final int y = e.getY();
		if (e.isAltDown() || e.isAltGraphDown() || e.isControlDown()) {
		    me.editObjectProperties(x, y);
		} else if (e.isShiftDown()) {
		    me.probeObjectProperties(x, y);
		} else {
		    me.editObject(x, y);
		}
	    } catch (final Exception ex) {
		LaserTank.getErrorLogger().logError(ex);
	    }
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
	    try {
		final ArenaEditor me = ArenaEditor.this;
		final int x = e.getX();
		final int y = e.getY();
		me.editObject(x, y);
	    } catch (final Exception ex) {
		LaserTank.getErrorLogger().logError(ex);
	    }
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	    // Do nothing
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	    // Do nothing
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
	    // Do nothing
	}

	// handle mouse
	@Override
	public void mousePressed(final MouseEvent e) {
	    // Do nothing
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	    // Do nothing
	}

	// Handle windows
	@Override
	public void windowActivated(final WindowEvent we) {
	    // Do nothing
	}

	@Override
	public void windowClosed(final WindowEvent we) {
	    // Do nothing
	}

	@Override
	public void windowClosing(final WindowEvent we) {
	    ArenaEditor.this.handleCloseWindow();
	    LaserTank.getApplication().getGUIManager().showGUI();
	}

	@Override
	public void windowDeactivated(final WindowEvent we) {
	    // Do nothing
	}

	@Override
	public void windowDeiconified(final WindowEvent we) {
	    // Do nothing
	}

	@Override
	public void windowIconified(final WindowEvent we) {
	    // Do nothing
	}

	@Override
	public void windowOpened(final WindowEvent we) {
	    // Do nothing
	}
    }

    private class FocusHandler implements WindowFocusListener {
	public FocusHandler() {
	    // Do nothing
	}

	@Override
	public void windowGainedFocus(final WindowEvent e) {
	    ArenaEditor.this.attachMenus();
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
		final String cmd = e.getActionCommand();
		final ArenaEditor editor = ArenaEditor.this;
		if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_UNDO))) {
		    // Undo most recent action
		    if (app.getMode() == Application.STATUS_EDITOR) {
			editor.undo();
		    } else if (app.getMode() == Application.STATUS_GAME) {
			app.getGameManager().abortAndWaitForMLOLoop();
			app.getGameManager().undoLastMove();
		    }
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_REDO))) {
		    // Redo most recent undone action
		    if (app.getMode() == Application.STATUS_EDITOR) {
			editor.redo();
		    } else if (app.getMode() == Application.STATUS_GAME) {
			app.getGameManager().abortAndWaitForMLOLoop();
			app.getGameManager().redoLastMove();
		    }
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_CUT_LEVEL))) {
		    // Cut Level
		    final int level = editor.getLocationManager().getEditorLocationU();
		    app.getArenaManager().getArena().cutLevel();
		    editor.fixLimits();
		    editor.updateEditorLevelAbsolute(level);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_COPY_LEVEL))) {
		    // Copy Level
		    app.getArenaManager().getArena().copyLevel();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_PASTE_LEVEL))) {
		    // Paste Level
		    app.getArenaManager().getArena().pasteLevel();
		    editor.fixLimits();
		    editor.redrawEditor();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_INSERT_LEVEL_FROM_CLIPBOARD))) {
		    // Insert Level From Clipboard
		    app.getArenaManager().getArena().insertLevelFromClipboard();
		    editor.fixLimits();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_CLEAR_HISTORY))) {
		    // Clear undo/redo history, confirm first
		    final int res = CommonDialogs.showConfirmDialog(
			    StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
				    StringConstants.MENU_STRING_CONFIRM_CLEAR_HISTORY),
			    StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
				    StringConstants.EDITOR_STRING_EDITOR));
		    if (res == JOptionPane.YES_OPTION) {
			editor.clearHistory();
		    }
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_GO_TO_LEVEL))) {
		    // Go To Level
		    editor.goToLevelHandler();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_UP_ONE_FLOOR))) {
		    // Go up one floor
		    editor.updateEditorPosition(1, 0);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_DOWN_ONE_FLOOR))) {
		    // Go down one floor
		    editor.updateEditorPosition(-1, 0);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_UP_ONE_LEVEL))) {
		    // Go up one level
		    editor.updateEditorPosition(0, 1);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_DOWN_ONE_LEVEL))) {
		    // Go down one level
		    editor.updateEditorPosition(0, -1);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_ADD_A_LEVEL))) {
		    // Add a level
		    editor.addLevel();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_REMOVE_A_LEVEL))) {
		    // Remove a level
		    editor.removeLevel();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_FILL_CURRENT_LEVEL))) {
		    // Fill level
		    editor.fillLevel();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_RESIZE_CURRENT_LEVEL))) {
		    // Resize level
		    editor.resizeLevel();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_LEVEL_PREFERENCES))) {
		    // Set Level Preferences
		    editor.setLevelPrefs();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_SET_START_POINT))) {
		    // Set Start Point
		    editor.editPlayerLocation();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_CHANGE_LAYER))) {
		    // Change Layer
		    editor.changeLayer();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_ENABLE_GLOBAL_MOVE_SHOOT))) {
		    // Enable Global Move-Shoot
		    editor.enableGlobalMoveShoot();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_DISABLE_GLOBAL_MOVE_SHOOT))) {
		    // Disable Global Move-Shoot
		    editor.disableGlobalMoveShoot();
		} else if (cmd.equals(
			StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, ArenaConstants.ERA_DISTANT_PAST))) {
		    // Time Travel: Distant Past
		    app.getArenaManager().getArena().switchEra(ArenaConstants.ERA_DISTANT_PAST);
		    editor.editorEraDistantPast.setSelected(true);
		    editor.editorEraPast.setSelected(false);
		    editor.editorEraPresent.setSelected(false);
		    editor.editorEraFuture.setSelected(false);
		    editor.editorEraDistantFuture.setSelected(false);
		} else if (cmd
			.equals(StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, ArenaConstants.ERA_PAST))) {
		    // Time Travel: Past
		    app.getArenaManager().getArena().switchEra(ArenaConstants.ERA_PAST);
		    editor.editorEraDistantPast.setSelected(false);
		    editor.editorEraPast.setSelected(true);
		    editor.editorEraPresent.setSelected(false);
		    editor.editorEraFuture.setSelected(false);
		    editor.editorEraDistantFuture.setSelected(false);
		} else if (cmd.equals(
			StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, ArenaConstants.ERA_PRESENT))) {
		    // Time Travel: Present
		    app.getArenaManager().getArena().switchEra(ArenaConstants.ERA_PRESENT);
		    editor.editorEraDistantPast.setSelected(false);
		    editor.editorEraPast.setSelected(false);
		    editor.editorEraPresent.setSelected(true);
		    editor.editorEraFuture.setSelected(false);
		    editor.editorEraDistantFuture.setSelected(false);
		} else if (cmd
			.equals(StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, ArenaConstants.ERA_FUTURE))) {
		    // Time Travel: Future
		    app.getArenaManager().getArena().switchEra(ArenaConstants.ERA_FUTURE);
		    editor.editorEraDistantPast.setSelected(false);
		    editor.editorEraPast.setSelected(false);
		    editor.editorEraPresent.setSelected(false);
		    editor.editorEraFuture.setSelected(true);
		    editor.editorEraDistantFuture.setSelected(false);
		} else if (cmd.equals(
			StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, ArenaConstants.ERA_DISTANT_FUTURE))) {
		    // Time Travel: Distant Future
		    app.getArenaManager().getArena().switchEra(ArenaConstants.ERA_DISTANT_FUTURE);
		    editor.editorEraDistantPast.setSelected(false);
		    editor.editorEraPast.setSelected(false);
		    editor.editorEraPresent.setSelected(false);
		    editor.editorEraFuture.setSelected(false);
		    editor.editorEraDistantFuture.setSelected(true);
		}
		app.getMenuManager().checkFlags();
	    } catch (final Exception ex) {
		LaserTank.getErrorLogger().logError(ex);
	    }
	}
    }

    private class StartEventHandler implements MouseListener {
	// handle scroll bars
	public StartEventHandler() {
	    // Do nothing
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	    try {
		final int x = e.getX();
		final int y = e.getY();
		ArenaEditor.this.setPlayerLocation(x, y);
	    } catch (final Exception ex) {
		LaserTank.getErrorLogger().logError(ex);
	    }
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	    // Do nothing
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	    // Do nothing
	}

	// handle mouse
	@Override
	public void mousePressed(final MouseEvent e) {
	    // Do nothing
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	    // Do nothing
	}
    }

    private class SwitcherHandler implements ActionListener {
	SwitcherHandler() {
	    // Do nothing
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
	    try {
		final String cmd = e.getActionCommand();
		final ArenaEditor ae = ArenaEditor.this;
		if (cmd.equals(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_LOWER_GROUND_LAYER))) {
		    ae.changeLayerImpl(ArenaConstants.LAYER_LOWER_GROUND);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_UPPER_GROUND_LAYER))) {
		    ae.changeLayerImpl(ArenaConstants.LAYER_UPPER_GROUND);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_LOWER_OBJECTS_LAYER))) {
		    ae.changeLayerImpl(ArenaConstants.LAYER_LOWER_OBJECTS);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_UPPER_OBJECTS_LAYER))) {
		    ae.changeLayerImpl(ArenaConstants.LAYER_UPPER_OBJECTS);
		}
	    } catch (final Exception ex) {
		LaserTank.getErrorLogger().logError(ex);
	    }
	}
    }

    private static final String[] JUMP_LIST = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
    // Declarations
    private JFrame outputFrame;
    private Container secondaryPane, borderPane, outerOutputPane, switcherPane;
    private EditorDraw outputPane;
    private JToggleButton lowerGround, upperGround, lowerObjects, upperObjects;
    private JLabel messageLabel;
    private AbstractArenaObject savedArenaObject;
    private JScrollBar vertScroll, horzScroll;
    private final EventHandler mhandler;
    private final StartEventHandler shandler;
    private final LevelPreferencesManager lPrefs;
    private PicturePicker picker;
    private AbstractArenaObject[] objects;
    private BufferedImageIcon[] editorAppearances;
    private boolean[] objectsEnabled;
    private EditorUndoRedoEngine engine;
    private EditorLocationManager elMgr;
    private boolean arenaChanged;
    private final int activePlayer;
    private JMenu editorTimeTravelSubMenu;
    JCheckBoxMenuItem editorEraDistantPast, editorEraPast, editorEraPresent, editorEraFuture, editorEraDistantFuture;
    private JMenuItem editorUndo, editorRedo, editorCutLevel, editorCopyLevel, editorPasteLevel,
	    editorInsertLevelFromClipboard, editorClearHistory, editorGoToLevel, editorUpOneFloor, editorDownOneFloor,
	    editorUpOneLevel, editorDownOneLevel, editorAddLevel, editorRemoveLevel, editorLevelPreferences,
	    editorSetStartPoint, editorFillLevel, editorResizeLevel, editorChangeLayer, editorGlobalMoveShoot;

    public ArenaEditor() {
	this.savedArenaObject = new Ground();
	this.lPrefs = new LevelPreferencesManager();
	this.mhandler = new EventHandler();
	this.shandler = new StartEventHandler();
	this.engine = new EditorUndoRedoEngine();
	final ArenaObjectList objectList = LaserTank.getApplication().getObjects();
	this.objects = objectList.getAllObjectsOnLayer(ArenaConstants.LAYER_LOWER_GROUND,
		PreferencesManager.getEditorShowAllObjects());
	this.editorAppearances = objectList.getAllEditorAppearancesOnLayer(ArenaConstants.LAYER_LOWER_GROUND,
		PreferencesManager.getEditorShowAllObjects());
	this.objectsEnabled = objectList.getObjectEnabledStatuses(ArenaConstants.LAYER_LOWER_GROUND);
	this.arenaChanged = true;
	this.activePlayer = 0;
    }

    public void activeLanguageChanged() {
	EditorLayoutConstants.activeLanguageChanged();
	this.updatePicker();
    }

    public boolean addLevel() {
	final boolean success = this.addLevelInternal();
	if (success) {
	    CommonDialogs.showDialog(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		    StringConstants.EDITOR_STRING_LEVEL_ADDED));
	} else {
	    CommonDialogs.showDialog(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		    StringConstants.EDITOR_STRING_LEVEL_ADDING_FAILED));
	}
	return success;
    }

    private boolean addLevelInternal() {
	final Application app = LaserTank.getApplication();
	boolean success = true;
	final int saveLevel = app.getArenaManager().getArena().getActiveLevelNumber();
	success = app.getArenaManager().getArena().addLevel();
	if (success) {
	    this.fixLimits();
	    app.getArenaManager().getArena().fillDefault();
	    // Save the entire level
	    app.getArenaManager().getArena().save();
	    app.getArenaManager().getArena().switchLevel(saveLevel);
	    this.checkMenus();
	}
	return success;
    }

    public void arenaChanged() {
	this.arenaChanged = true;
    }

    @Override
    public void attachAccelerators(final Accelerators accel) {
	this.editorUndo.setAccelerator(accel.editorUndoAccel);
	this.editorRedo.setAccelerator(accel.editorRedoAccel);
	this.editorCutLevel.setAccelerator(accel.editorCutLevelAccel);
	this.editorCopyLevel.setAccelerator(accel.editorCopyLevelAccel);
	this.editorPasteLevel.setAccelerator(accel.editorPasteLevelAccel);
	this.editorInsertLevelFromClipboard.setAccelerator(accel.editorInsertLevelFromClipboardAccel);
	this.editorClearHistory.setAccelerator(accel.editorClearHistoryAccel);
	this.editorGoToLevel.setAccelerator(accel.editorGoToLocationAccel);
	this.editorUpOneLevel.setAccelerator(accel.editorUpOneLevelAccel);
	this.editorDownOneLevel.setAccelerator(accel.editorDownOneLevelAccel);
    }

    public void attachMenus() {
	final Application app = LaserTank.getApplication();
	this.outputFrame.setJMenuBar(app.getMenuManager().getMainMenuBar());
	app.getMenuManager().checkFlags();
    }

    public void changeLayer() {
	final String[] list = ArenaConstants.getLayerList();
	final String choice = CommonDialogs.showInputDialog(
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_CHANGE_LAYER_PROMPT),
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE, StringConstants.EDITOR_STRING_EDITOR),
		list, list[this.elMgr.getEditorLocationW()]);
	if (choice != null) {
	    final int len = list.length;
	    int index = -1;
	    for (int z = 0; z < len; z++) {
		if (choice.equals(list[z])) {
		    index = z;
		    break;
		}
	    }
	    if (index != -1) {
		// Update selected button
		if (index == ArenaConstants.LAYER_LOWER_GROUND) {
		    this.lowerGround.setSelected(true);
		} else if (index == ArenaConstants.LAYER_UPPER_GROUND) {
		    this.upperGround.setSelected(true);
		} else if (index == ArenaConstants.LAYER_LOWER_OBJECTS) {
		    this.lowerObjects.setSelected(true);
		} else if (index == ArenaConstants.LAYER_UPPER_OBJECTS) {
		    this.upperObjects.setSelected(true);
		}
		this.changeLayerImpl(index);
	    }
	}
    }

    void changeLayerImpl(final int layer) {
	this.elMgr.setEditorLocationW(layer);
	this.updatePicker();
	this.redrawEditor();
    }

    private void checkMenus() {
	final Application app = LaserTank.getApplication();
	if (app.getMode() == Application.STATUS_EDITOR) {
	    final AbstractArena m = app.getArenaManager().getArena();
	    if (m.getLevels() == AbstractArena.getMinLevels()) {
		this.disableRemoveLevel();
	    } else {
		this.enableRemoveLevel();
	    }
	    if (m.getLevels() == AbstractArena.getMaxLevels()) {
		this.disableAddLevel();
	    } else {
		this.enableAddLevel();
	    }
	    try {
		if (this.elMgr.getEditorLocationZ() == this.elMgr.getMinEditorLocationZ()) {
		    this.disableDownOneFloor();
		} else {
		    this.enableDownOneFloor();
		}
		if (this.elMgr.getEditorLocationZ() == this.elMgr.getMaxEditorLocationZ()) {
		    this.disableUpOneFloor();
		} else {
		    this.enableUpOneFloor();
		}
	    } catch (final NullPointerException npe) {
		this.disableDownOneFloor();
		this.disableUpOneFloor();
	    }
	    try {
		if (this.elMgr.getEditorLocationU() == this.elMgr.getMinEditorLocationU()) {
		    this.disableDownOneLevel();
		} else {
		    this.enableDownOneLevel();
		}
		if (this.elMgr.getEditorLocationU() == this.elMgr.getMaxEditorLocationU()) {
		    this.disableUpOneLevel();
		} else {
		    this.enableUpOneLevel();
		}
	    } catch (final NullPointerException npe) {
		this.disableDownOneLevel();
		this.disableUpOneLevel();
	    }
	    if (this.elMgr != null) {
		this.enableSetStartPoint();
	    } else {
		this.disableSetStartPoint();
	    }
	    if (!this.engine.tryUndo()) {
		this.disableUndo();
	    } else {
		this.enableUndo();
	    }
	    if (!this.engine.tryRedo()) {
		this.disableRedo();
	    } else {
		this.enableRedo();
	    }
	    if (this.engine.tryBoth()) {
		this.disableClearHistory();
	    } else {
		this.enableClearHistory();
	    }
	}
	if (app.getArenaManager().getArena().isPasteBlocked()) {
	    this.disablePasteLevel();
	    this.disableInsertLevelFromClipboard();
	} else {
	    this.enablePasteLevel();
	    this.enableInsertLevelFromClipboard();
	}
	if (app.getArenaManager().getArena().isCutBlocked()) {
	    this.disableCutLevel();
	} else {
	    this.enableCutLevel();
	}
    }

    public void clearHistory() {
	this.engine = new EditorUndoRedoEngine();
	this.checkMenus();
    }

    private boolean confirmNonUndoable() {
	final int confirm = CommonDialogs.showConfirmDialog(
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_CONFIRM_CANNOT_BE_UNDONE),
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE, StringConstants.EDITOR_STRING_EDITOR));
	if (confirm == JOptionPane.YES_OPTION) {
	    this.clearHistory();
	    return true;
	}
	return false;
    }

    @Override
    public JMenu createCommandsMenu() {
	final MenuHandler menuHandler = new MenuHandler();
	final JMenu editorMenu = new JMenu(
		StringLoader.loadString(StringConstants.MENU_STRINGS_FILE, StringConstants.MENU_STRING_MENU_EDITOR));
	this.editorUndo = new JMenuItem(
		StringLoader.loadString(StringConstants.MENU_STRINGS_FILE, StringConstants.MENU_STRING_ITEM_UNDO));
	this.editorRedo = new JMenuItem(
		StringLoader.loadString(StringConstants.MENU_STRINGS_FILE, StringConstants.MENU_STRING_ITEM_REDO));
	this.editorCutLevel = new JMenuItem(
		StringLoader.loadString(StringConstants.MENU_STRINGS_FILE, StringConstants.MENU_STRING_ITEM_CUT_LEVEL));
	this.editorCopyLevel = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_COPY_LEVEL));
	this.editorPasteLevel = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_PASTE_LEVEL));
	this.editorInsertLevelFromClipboard = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_INSERT_LEVEL_FROM_CLIPBOARD));
	this.editorClearHistory = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_CLEAR_HISTORY));
	this.editorGoToLevel = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_GO_TO_LEVEL));
	this.editorUpOneFloor = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_UP_ONE_FLOOR));
	this.editorDownOneFloor = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_DOWN_ONE_FLOOR));
	this.editorUpOneLevel = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_UP_ONE_LEVEL));
	this.editorDownOneLevel = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_DOWN_ONE_LEVEL));
	this.editorAddLevel = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_ADD_A_LEVEL));
	this.editorRemoveLevel = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_REMOVE_A_LEVEL));
	this.editorFillLevel = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_FILL_CURRENT_LEVEL));
	this.editorResizeLevel = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_RESIZE_CURRENT_LEVEL));
	this.editorLevelPreferences = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_LEVEL_PREFERENCES));
	this.editorSetStartPoint = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_SET_START_POINT));
	this.editorChangeLayer = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_CHANGE_LAYER));
	this.editorGlobalMoveShoot = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_ENABLE_GLOBAL_MOVE_SHOOT));
	this.editorTimeTravelSubMenu = new JMenu(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_SUB_TIME_TRAVEL));
	this.editorEraDistantPast = new JCheckBoxMenuItem(
		StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, ArenaConstants.ERA_DISTANT_PAST), false);
	this.editorEraPast = new JCheckBoxMenuItem(
		StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, ArenaConstants.ERA_PAST), false);
	this.editorEraPresent = new JCheckBoxMenuItem(
		StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, ArenaConstants.ERA_PRESENT), true);
	this.editorEraFuture = new JCheckBoxMenuItem(
		StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, ArenaConstants.ERA_FUTURE), false);
	this.editorEraDistantFuture = new JCheckBoxMenuItem(
		StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, ArenaConstants.ERA_DISTANT_FUTURE), false);
	this.editorUndo.addActionListener(menuHandler);
	this.editorRedo.addActionListener(menuHandler);
	this.editorCutLevel.addActionListener(menuHandler);
	this.editorCopyLevel.addActionListener(menuHandler);
	this.editorPasteLevel.addActionListener(menuHandler);
	this.editorInsertLevelFromClipboard.addActionListener(menuHandler);
	this.editorClearHistory.addActionListener(menuHandler);
	this.editorGoToLevel.addActionListener(menuHandler);
	this.editorUpOneFloor.addActionListener(menuHandler);
	this.editorDownOneFloor.addActionListener(menuHandler);
	this.editorUpOneLevel.addActionListener(menuHandler);
	this.editorDownOneLevel.addActionListener(menuHandler);
	this.editorAddLevel.addActionListener(menuHandler);
	this.editorRemoveLevel.addActionListener(menuHandler);
	this.editorFillLevel.addActionListener(menuHandler);
	this.editorResizeLevel.addActionListener(menuHandler);
	this.editorLevelPreferences.addActionListener(menuHandler);
	this.editorSetStartPoint.addActionListener(menuHandler);
	this.editorChangeLayer.addActionListener(menuHandler);
	this.editorGlobalMoveShoot.addActionListener(menuHandler);
	this.editorEraDistantPast.addActionListener(menuHandler);
	this.editorEraPast.addActionListener(menuHandler);
	this.editorEraPresent.addActionListener(menuHandler);
	this.editorEraFuture.addActionListener(menuHandler);
	this.editorEraDistantFuture.addActionListener(menuHandler);
	this.editorTimeTravelSubMenu.add(this.editorEraDistantPast);
	this.editorTimeTravelSubMenu.add(this.editorEraPast);
	this.editorTimeTravelSubMenu.add(this.editorEraPresent);
	this.editorTimeTravelSubMenu.add(this.editorEraFuture);
	this.editorTimeTravelSubMenu.add(this.editorEraDistantFuture);
	editorMenu.add(this.editorUndo);
	editorMenu.add(this.editorRedo);
	editorMenu.add(this.editorCutLevel);
	editorMenu.add(this.editorCopyLevel);
	editorMenu.add(this.editorPasteLevel);
	editorMenu.add(this.editorInsertLevelFromClipboard);
	editorMenu.add(this.editorClearHistory);
	editorMenu.add(this.editorGoToLevel);
	editorMenu.add(this.editorUpOneFloor);
	editorMenu.add(this.editorDownOneFloor);
	editorMenu.add(this.editorUpOneLevel);
	editorMenu.add(this.editorDownOneLevel);
	editorMenu.add(this.editorAddLevel);
	editorMenu.add(this.editorRemoveLevel);
	editorMenu.add(this.editorFillLevel);
	editorMenu.add(this.editorResizeLevel);
	editorMenu.add(this.editorLevelPreferences);
	editorMenu.add(this.editorSetStartPoint);
	editorMenu.add(this.editorChangeLayer);
	editorMenu.add(this.editorGlobalMoveShoot);
	editorMenu.add(this.editorTimeTravelSubMenu);
	return editorMenu;
    }

    private void disableAddLevel() {
	this.editorAddLevel.setEnabled(false);
    }

    private void disableClearHistory() {
	this.editorClearHistory.setEnabled(false);
    }

    private void disableCutLevel() {
	this.editorCutLevel.setEnabled(false);
    }

    @Override
    public void disableDirtyCommands() {
	// Do nothing
    }

    private void disableDownOneFloor() {
	this.editorDownOneFloor.setEnabled(false);
    }

    private void disableDownOneLevel() {
	this.editorDownOneLevel.setEnabled(false);
    }

    void disableGlobalMoveShoot() {
	LaserTank.getApplication().getArenaManager().getArena().setMoveShootAllowedGlobally(false);
	this.editorGlobalMoveShoot.setText(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_ENABLE_GLOBAL_MOVE_SHOOT));
    }

    private void disableInsertLevelFromClipboard() {
	this.editorInsertLevelFromClipboard.setEnabled(false);
    }

    @Override
    public void disableLoadedCommands() {
	// Do nothing
    }

    @Override
    public void disableModeCommands() {
	this.editorUndo.setEnabled(false);
	this.editorRedo.setEnabled(false);
	this.editorCutLevel.setEnabled(false);
	this.editorCopyLevel.setEnabled(false);
	this.editorPasteLevel.setEnabled(false);
	this.editorInsertLevelFromClipboard.setEnabled(false);
	this.editorClearHistory.setEnabled(false);
	this.editorGoToLevel.setEnabled(false);
	this.editorUpOneFloor.setEnabled(false);
	this.editorDownOneFloor.setEnabled(false);
	this.editorUpOneLevel.setEnabled(false);
	this.editorDownOneLevel.setEnabled(false);
	this.editorAddLevel.setEnabled(false);
	this.editorRemoveLevel.setEnabled(false);
	this.editorFillLevel.setEnabled(false);
	this.editorResizeLevel.setEnabled(false);
	this.editorLevelPreferences.setEnabled(false);
	this.editorSetStartPoint.setEnabled(false);
	this.editorChangeLayer.setEnabled(false);
	this.editorGlobalMoveShoot.setEnabled(false);
	this.editorEraDistantPast.setEnabled(false);
	this.editorEraPast.setEnabled(false);
	this.editorEraPresent.setEnabled(false);
	this.editorEraFuture.setEnabled(false);
	this.editorEraDistantFuture.setEnabled(false);
    }

    void disableOutput() {
	this.outputFrame.setEnabled(false);
    }

    private void disablePasteLevel() {
	this.editorPasteLevel.setEnabled(false);
    }

    public void disableRedo() {
	this.editorRedo.setEnabled(false);
    }

    private void disableRemoveLevel() {
	this.editorRemoveLevel.setEnabled(false);
    }

    private void disableSetStartPoint() {
	this.editorSetStartPoint.setEnabled(false);
    }

    public void disableUndo() {
	this.editorUndo.setEnabled(false);
    }

    private void disableUpOneFloor() {
	this.editorUpOneFloor.setEnabled(false);
    }

    private void disableUpOneLevel() {
	this.editorUpOneLevel.setEnabled(false);
    }

    public void editArena() {
	final Application app = LaserTank.getApplication();
	if (app.getArenaManager().getLoaded()) {
	    app.getGUIManager().hideGUI();
	    app.setInEditor();
	    // Reset game state
	    app.getGameManager().resetGameState();
	    // Create the managers
	    if (this.arenaChanged) {
		this.elMgr = new EditorLocationManager();
		this.elMgr.setLimitsFromArena(app.getArenaManager().getArena());
		this.arenaChanged = false;
	    }
	    this.setUpGUI();
	    this.updatePicker();
	    this.clearHistory();
	    this.redrawEditor();
	    this.updatePickerLayout();
	    this.resetBorderPane();
	    this.checkMenus();
	} else {
	    CommonDialogs.showDialog(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		    StringConstants.MENU_STRING_ERROR_NO_ARENA_OPENED));
	}
    }

    public void editJumpBox(final AbstractJumpObject jumper) {
	final int currentX = jumper.getJumpCols();
	final int currentY = jumper.getJumpRows();
	final String newXStr = CommonDialogs.showInputDialog(
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE, StringConstants.EDITOR_STRING_HORZ_JUMP),
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE, StringConstants.EDITOR_STRING_EDITOR),
		ArenaEditor.JUMP_LIST, ArenaEditor.JUMP_LIST[currentX]);
	if (newXStr != null) {
	    final String newYStr = CommonDialogs.showInputDialog(
		    StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			    StringConstants.EDITOR_STRING_VERT_JUMP),
		    StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE, StringConstants.EDITOR_STRING_EDITOR),
		    ArenaEditor.JUMP_LIST, ArenaEditor.JUMP_LIST[currentY]);
	    if (newYStr != null) {
		final int newX = Integer.parseInt(newXStr);
		final int newY = Integer.parseInt(newYStr);
		jumper.setJumpCols(newX);
		jumper.setJumpRows(newY);
	    }
	}
    }

    void editObject(final int x, final int y) {
	final Application app = LaserTank.getApplication();
	int currentObjectIndex = this.picker.getPicked();
	final int xOffset = this.vertScroll.getValue() - this.vertScroll.getMinimum();
	final int yOffset = this.horzScroll.getValue() - this.horzScroll.getMinimum();
	final int gridX = x / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationX()
		- xOffset + yOffset;
	final int gridY = y / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationY()
		+ xOffset - yOffset;
	try {
	    this.savedArenaObject = app.getArenaManager().getArena().getCell(gridX, gridY,
		    this.elMgr.getEditorLocationZ(), this.elMgr.getEditorLocationW());
	} catch (final ArrayIndexOutOfBoundsException ae) {
	    return;
	}
	final AbstractArenaObject[] choices = this.objects;
	final AbstractArenaObject mo = choices[currentObjectIndex];
	final AbstractArenaObject instance = mo.clone();
	this.elMgr.setEditorLocationX(gridX);
	this.elMgr.setEditorLocationY(gridY);
	this.savedArenaObject.editorRemoveHook(gridX, gridY, this.elMgr.getEditorLocationZ());
	mo.editorPlaceHook(gridX, gridY, this.elMgr.getEditorLocationZ());
	try {
	    this.updateUndoHistory(this.savedArenaObject, gridX, gridY, this.elMgr.getEditorLocationZ(),
		    this.elMgr.getEditorLocationW(), this.elMgr.getEditorLocationU());
	    app.getArenaManager().getArena().setCell(instance, gridX, gridY, this.elMgr.getEditorLocationZ(),
		    this.elMgr.getEditorLocationW());
	    app.getArenaManager().setDirty(true);
	    this.checkMenus();
	    this.redrawEditor();
	} catch (final ArrayIndexOutOfBoundsException aioob) {
	    app.getArenaManager().getArena().setCell(this.savedArenaObject, gridX, gridY,
		    this.elMgr.getEditorLocationZ(), this.elMgr.getEditorLocationW());
	    this.redrawEditor();
	}
    }

    void editObjectProperties(final int x, final int y) {
	final Application app = LaserTank.getApplication();
	final int xOffset = this.vertScroll.getValue() - this.vertScroll.getMinimum();
	final int yOffset = this.horzScroll.getValue() - this.horzScroll.getMinimum();
	final int gridX = x / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationX()
		- xOffset + yOffset;
	final int gridY = y / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationY()
		+ xOffset - yOffset;
	try {
	    final AbstractArenaObject mo = app.getArenaManager().getArena().getCell(gridX, gridY,
		    this.elMgr.getEditorLocationZ(), this.elMgr.getEditorLocationW());
	    this.elMgr.setEditorLocationX(gridX);
	    this.elMgr.setEditorLocationY(gridY);
	    if (!mo.defersSetProperties()) {
		final AbstractArenaObject mo2 = mo.editorPropertiesHook();
		if (mo2 == null) {
		    LaserTank.getApplication().showMessage(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			    StringConstants.EDITOR_STRING_NO_PROPERTIES));
		} else {
		    this.updateUndoHistory(this.savedArenaObject, gridX, gridY, this.elMgr.getEditorLocationZ(),
			    this.elMgr.getEditorLocationW(), this.elMgr.getEditorLocationU());
		    app.getArenaManager().getArena().setCell(mo2, gridX, gridY, this.elMgr.getEditorLocationZ(),
			    this.elMgr.getEditorLocationW());
		    this.checkMenus();
		    app.getArenaManager().setDirty(true);
		}
	    } else {
		mo.editorPropertiesHook();
	    }
	    this.redrawEditor();
	} catch (final ArrayIndexOutOfBoundsException aioob) {
	    // Do nothing
	}
    }

    public void editPlayerLocation() {
	// Swap event handlers
	this.secondaryPane.removeMouseListener(this.mhandler);
	this.secondaryPane.addMouseListener(this.shandler);
	LaserTank.getApplication().showMessage(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		StringConstants.EDITOR_STRING_SET_START_POINT));
    }

    private void enableAddLevel() {
	this.editorAddLevel.setEnabled(true);
    }

    private void enableClearHistory() {
	this.editorClearHistory.setEnabled(true);
    }

    private void enableCutLevel() {
	this.editorCutLevel.setEnabled(true);
    }

    @Override
    public void enableDirtyCommands() {
	// Do nothing
    }

    private void enableDownOneFloor() {
	this.editorDownOneFloor.setEnabled(true);
    }

    private void enableDownOneLevel() {
	this.editorDownOneLevel.setEnabled(true);
    }

    void enableGlobalMoveShoot() {
	LaserTank.getApplication().getArenaManager().getArena().setMoveShootAllowedGlobally(true);
	this.editorGlobalMoveShoot.setText(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_DISABLE_GLOBAL_MOVE_SHOOT));
    }

    private void enableInsertLevelFromClipboard() {
	this.editorInsertLevelFromClipboard.setEnabled(true);
    }

    @Override
    public void enableLoadedCommands() {
	// Do nothing
    }

    @Override
    public void enableModeCommands() {
	this.editorUndo.setEnabled(false);
	this.editorRedo.setEnabled(false);
	this.editorCutLevel.setEnabled(true);
	this.editorCopyLevel.setEnabled(true);
	this.editorPasteLevel.setEnabled(true);
	this.editorInsertLevelFromClipboard.setEnabled(true);
	this.editorGoToLevel.setEnabled(true);
	this.editorFillLevel.setEnabled(true);
	this.editorResizeLevel.setEnabled(true);
	this.editorLevelPreferences.setEnabled(true);
	this.editorSetStartPoint.setEnabled(true);
	this.editorChangeLayer.setEnabled(true);
	this.editorGlobalMoveShoot.setEnabled(true);
	this.editorEraDistantPast.setEnabled(true);
	this.editorEraPast.setEnabled(true);
	this.editorEraPresent.setEnabled(true);
	this.editorEraFuture.setEnabled(true);
	this.editorEraDistantFuture.setEnabled(true);
    }

    void enableOutput() {
	this.outputFrame.setEnabled(true);
	this.checkMenus();
    }

    private void enablePasteLevel() {
	this.editorPasteLevel.setEnabled(true);
    }

    public void enableRedo() {
	this.editorRedo.setEnabled(true);
    }

    private void enableRemoveLevel() {
	this.editorRemoveLevel.setEnabled(true);
    }

    private void enableSetStartPoint() {
	this.editorSetStartPoint.setEnabled(true);
    }

    public void enableUndo() {
	this.editorUndo.setEnabled(true);
    }

    private void enableUpOneFloor() {
	this.editorUpOneFloor.setEnabled(true);
    }

    private void enableUpOneLevel() {
	this.editorUpOneLevel.setEnabled(true);
    }

    public void exitEditor() {
	final Application app = LaserTank.getApplication();
	// Hide the editor
	this.hideOutput();
	final ArenaManager mm = app.getArenaManager();
	final GameManager gm = app.getGameManager();
	// Save the entire level
	mm.getArena().save();
	// Reset the player location
	try {
	    gm.resetPlayerLocation();
	} catch (final InvalidArenaException iae) {
	    // Harmless error, ignore it
	}
    }

    public void fillLevel() {
	if (this.confirmNonUndoable()) {
	    LaserTank.getApplication().getArenaManager().getArena().fillDefault();
	    LaserTank.getApplication().showMessage(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		    StringConstants.EDITOR_STRING_LEVEL_FILLED));
	    LaserTank.getApplication().getArenaManager().setDirty(true);
	    this.redrawEditor();
	}
    }

    public void fixLimits() {
	// Fix limits
	final Application app = LaserTank.getApplication();
	if (app.getArenaManager().getArena() != null && this.elMgr != null) {
	    this.elMgr.setLimitsFromArena(app.getArenaManager().getArena());
	}
    }

    public EditorLocationManager getLocationManager() {
	return this.elMgr;
    }

    public JFrame getOutputFrame() {
	if (this.outputFrame != null && this.outputFrame.isVisible()) {
	    return this.outputFrame;
	} else {
	    return null;
	}
    }

    public void goToLevelHandler() {
	int locW;
	final String msg = StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		StringConstants.EDITOR_STRING_GO_TO_LEVEL);
	String input;
	final String[] choices = LaserTank.getApplication().getLevelInfoList();
	input = CommonDialogs.showInputDialog(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		StringConstants.EDITOR_STRING_GO_TO_WHICH_LEVEL), msg, choices, choices[0]);
	if (input != null) {
	    for (locW = 0; locW < choices.length; locW++) {
		if (input.equals(choices[locW])) {
		    this.updateEditorLevelAbsolute(locW);
		    break;
		}
	    }
	}
    }

    public void handleCloseWindow() {
	try {
	    final Application app = LaserTank.getApplication();
	    boolean success = false;
	    int status = JOptionPane.DEFAULT_OPTION;
	    if (app.getArenaManager().getDirty()) {
		status = ArenaManager.showSaveDialog();
		if (status == JOptionPane.YES_OPTION) {
		    success = app.getArenaManager().saveArena(app.getArenaManager().isArenaProtected());
		    if (success) {
			this.exitEditor();
		    }
		} else if (status == JOptionPane.NO_OPTION) {
		    app.getArenaManager().setDirty(false);
		    this.exitEditor();
		}
	    } else {
		this.exitEditor();
	    }
	} catch (final Exception ex) {
	    LaserTank.getErrorLogger().logError(ex);
	}
    }

    public void hideOutput() {
	if (this.outputFrame != null) {
	    this.outputFrame.setVisible(false);
	}
    }

    public boolean newArena() {
	final Application app = LaserTank.getApplication();
	boolean success = true;
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
	    app.getGameManager().getPlayerManager().resetPlayerLocation();
	    AbstractArena a = null;
	    try {
		a = ArenaManager.createArena();
	    } catch (final IOException ioe) {
		success = false;
	    }
	    if (success) {
		app.getArenaManager().setArena(a);
		success = this.addLevelInternal();
		if (success) {
		    app.getArenaManager().clearLastUsedFilenames();
		    this.clearHistory();
		}
	    }
	} else {
	    success = false;
	}
	if (success) {
	    this.arenaChanged = true;
	    CommonDialogs.showDialog(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		    StringConstants.EDITOR_STRING_ARENA_CREATED));
	} else {
	    CommonDialogs.showDialog(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		    StringConstants.EDITOR_STRING_ARENA_CREATION_FAILED));
	}
	return success;
    }

    void probeObjectProperties(final int x, final int y) {
	final Application app = LaserTank.getApplication();
	final int xOffset = this.vertScroll.getValue() - this.vertScroll.getMinimum();
	final int yOffset = this.horzScroll.getValue() - this.horzScroll.getMinimum();
	final int gridX = x / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationX()
		- xOffset + yOffset;
	final int gridY = y / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationY()
		+ xOffset - yOffset;
	final AbstractArenaObject mo = app.getArenaManager().getArena().getCell(gridX, gridY,
		this.elMgr.getEditorLocationZ(), this.elMgr.getEditorLocationW());
	this.elMgr.setEditorLocationX(gridX);
	this.elMgr.setEditorLocationY(gridY);
	final String gameName = mo.getIdentityName();
	final String desc = mo.getDescription();
	CommonDialogs.showTitledDialog(desc, gameName);
    }

    public void redo() {
	final Application app = LaserTank.getApplication();
	this.engine.redo();
	final AbstractArenaObject obj = this.engine.getObject();
	final int x = this.engine.getX();
	final int y = this.engine.getY();
	final int z = this.engine.getZ();
	final int w = this.engine.getW();
	final int u = this.engine.getU();
	this.elMgr.setEditorLocationX(x);
	this.elMgr.setEditorLocationY(y);
	if (x != -1 && y != -1 && z != -1 && u != -1) {
	    final AbstractArenaObject oldObj = app.getArenaManager().getArena().getCell(x, y, z, w);
	    app.getArenaManager().getArena().setCell(obj, x, y, z, w);
	    this.updateUndoHistory(oldObj, x, y, z, w, u);
	    this.checkMenus();
	    this.redrawEditor();
	} else {
	    LaserTank.getApplication().showMessage(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		    StringConstants.EDITOR_STRING_NOTHING_TO_REDO));
	}
    }

    public void redrawEditor() {
	final int z = this.elMgr.getEditorLocationZ();
	final int w = this.elMgr.getEditorLocationW();
	final int u = this.elMgr.getEditorLocationU();
	final int e = LaserTank.getApplication().getArenaManager().getArena().getActiveEraNumber();
	if (w == ArenaConstants.LAYER_LOWER_GROUND) {
	    this.redrawEditorBottomGround();
	} else if (w == ArenaConstants.LAYER_UPPER_GROUND) {
	    this.redrawEditorGround();
	} else if (w == ArenaConstants.LAYER_LOWER_OBJECTS) {
	    this.redrawEditorGroundBottomObjects();
	} else if (w == ArenaConstants.LAYER_UPPER_OBJECTS) {
	    this.redrawEditorGroundObjects();
	}
	this.outputFrame.pack();
	this.outputFrame.setTitle(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		StringConstants.EDITOR_STRING_EDITOR_TITLE_1)
		+ (z + 1)
		+ StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_EDITOR_TITLE_2)
		+ (u + 1) + StringConstants.COMMON_STRING_SPACE_DASH_SPACE
		+ StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, e));
	this.outputPane.repaint();
	this.showOutput();
    }

    private void redrawEditorBottomGround() {
	// Draw the arena in edit mode
	final Application app = LaserTank.getApplication();
	int x, y;
	int xFix, yFix;
	final DrawGrid drawGrid = this.outputPane.getGrid();
	for (x = EditorViewingWindowManager.getViewingWindowLocationX(); x <= EditorViewingWindowManager
		.getLowerRightViewingWindowLocationX(); x++) {
	    for (y = EditorViewingWindowManager.getViewingWindowLocationY(); y <= EditorViewingWindowManager
		    .getLowerRightViewingWindowLocationY(); y++) {
		xFix = x - EditorViewingWindowManager.getViewingWindowLocationX();
		yFix = y - EditorViewingWindowManager.getViewingWindowLocationY();
		final AbstractArenaObject lgobj = app.getArenaManager().getArena().getCell(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_LOWER_GROUND);
		drawGrid.setImageCell(ImageManager.getImage(lgobj, true), xFix, yFix);
	    }
	}
    }

    private void redrawEditorGround() {
	// Draw the arena in edit mode
	final Application app = LaserTank.getApplication();
	int x, y;
	int xFix, yFix;
	final DrawGrid drawGrid = this.outputPane.getGrid();
	for (x = EditorViewingWindowManager.getViewingWindowLocationX(); x <= EditorViewingWindowManager
		.getLowerRightViewingWindowLocationX(); x++) {
	    for (y = EditorViewingWindowManager.getViewingWindowLocationY(); y <= EditorViewingWindowManager
		    .getLowerRightViewingWindowLocationY(); y++) {
		xFix = x - EditorViewingWindowManager.getViewingWindowLocationX();
		yFix = y - EditorViewingWindowManager.getViewingWindowLocationY();
		final AbstractArenaObject lgobj = app.getArenaManager().getArena().getCell(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_LOWER_GROUND);
		final AbstractArenaObject ugobj = app.getArenaManager().getArena().getCell(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_UPPER_GROUND);
		drawGrid.setImageCell(ImageManager.getCompositeImage(lgobj, ugobj, true), xFix, yFix);
	    }
	}
    }

    private void redrawEditorGroundBottomObjects() {
	// Draw the arena in edit mode
	final Application app = LaserTank.getApplication();
	int x, y;
	int xFix, yFix;
	final DrawGrid drawGrid = this.outputPane.getGrid();
	for (x = EditorViewingWindowManager.getViewingWindowLocationX(); x <= EditorViewingWindowManager
		.getLowerRightViewingWindowLocationX(); x++) {
	    for (y = EditorViewingWindowManager.getViewingWindowLocationY(); y <= EditorViewingWindowManager
		    .getLowerRightViewingWindowLocationY(); y++) {
		xFix = x - EditorViewingWindowManager.getViewingWindowLocationX();
		yFix = y - EditorViewingWindowManager.getViewingWindowLocationY();
		final AbstractArenaObject lgobj = app.getArenaManager().getArena().getCell(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_LOWER_GROUND);
		final AbstractArenaObject ugobj = app.getArenaManager().getArena().getCell(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_UPPER_GROUND);
		final AbstractArenaObject loobj = app.getArenaManager().getArena().getCell(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_LOWER_OBJECTS);
		drawGrid.setImageCell(ImageManager.getVirtualCompositeImage(lgobj, ugobj, loobj), xFix, yFix);
	    }
	}
    }

    private void redrawEditorGroundObjects() {
	// Draw the arena in edit mode
	final Application app = LaserTank.getApplication();
	int x, y;
	int xFix, yFix;
	final DrawGrid drawGrid = this.outputPane.getGrid();
	for (x = EditorViewingWindowManager.getViewingWindowLocationX(); x <= EditorViewingWindowManager
		.getLowerRightViewingWindowLocationX(); x++) {
	    for (y = EditorViewingWindowManager.getViewingWindowLocationY(); y <= EditorViewingWindowManager
		    .getLowerRightViewingWindowLocationY(); y++) {
		xFix = x - EditorViewingWindowManager.getViewingWindowLocationX();
		yFix = y - EditorViewingWindowManager.getViewingWindowLocationY();
		final AbstractArenaObject lgobj = app.getArenaManager().getArena().getCell(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_LOWER_GROUND);
		final AbstractArenaObject ugobj = app.getArenaManager().getArena().getCell(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_UPPER_GROUND);
		final AbstractArenaObject loobj = app.getArenaManager().getArena().getCell(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_LOWER_OBJECTS);
		final AbstractArenaObject uoobj = app.getArenaManager().getArena().getCell(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_UPPER_OBJECTS);
		final AbstractArenaObject lvobj = app.getArenaManager().getArena().getVirtualCell(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_VIRTUAL);
		drawGrid.setImageCell(ImageManager.getVirtualCompositeImage(lgobj, ugobj, loobj, uoobj, lvobj), xFix,
			yFix);
	    }
	}
    }

    public boolean removeLevel() {
	final Application app = LaserTank.getApplication();
	int level;
	boolean success = true;
	String[] choices = app.getLevelInfoList();
	if (choices == null) {
	    choices = app.getLevelInfoList();
	}
	String input;
	input = CommonDialogs.showInputDialog(
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_WHICH_LEVEL_TO_REMOVE),
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_REMOVE_LEVEL),
		choices, choices[0]);
	if (input != null) {
	    for (level = 0; level < choices.length; level++) {
		if (input.equals(choices[level])) {
		    success = app.getArenaManager().getArena().removeLevel(level);
		    if (success) {
			this.fixLimits();
			if (level == this.elMgr.getEditorLocationU()) {
			    // Deleted current level - go to level 1
			    this.updateEditorLevelAbsolute(0);
			}
			this.checkMenus();
			app.getArenaManager().setDirty(true);
		    }
		    break;
		}
	    }
	} else {
	    // User canceled
	    success = false;
	}
	return success;
    }

    public void resetBorderPane() {
	if (this.borderPane != null) {
	    this.updatePicker();
	    this.borderPane.removeAll();
	    this.borderPane.add(this.outerOutputPane, BorderLayout.CENTER);
	    this.borderPane.add(this.messageLabel, BorderLayout.NORTH);
	    this.borderPane.add(this.picker.getPicker(), BorderLayout.EAST);
	    this.borderPane.add(this.switcherPane, BorderLayout.SOUTH);
	    this.outputFrame.pack();
	}
    }

    public boolean resizeLevel() {
	final Application app = LaserTank.getApplication();
	int levelSizeZ;
	final int maxF = AbstractArena.getMaxFloors();
	final int minF = AbstractArena.getMinFloors();
	final String msg = StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		StringConstants.EDITOR_STRING_RESIZE_LEVEL);
	boolean success = true;
	String input3;
	input3 = CommonDialogs.showTextInputDialogWithDefault(
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_NUMBER_OF_FLOORS),
		msg, Integer.toString(app.getArenaManager().getArena().getFloors()));
	if (input3 != null) {
	    try {
		levelSizeZ = Integer.parseInt(input3);
		if (levelSizeZ < minF) {
		    throw new NumberFormatException(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			    StringConstants.EDITOR_STRING_FLOORS_TOO_LOW));
		}
		if (levelSizeZ > maxF) {
		    throw new NumberFormatException(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			    StringConstants.EDITOR_STRING_FLOORS_TOO_HIGH));
		}
		app.getArenaManager().getArena().resize(levelSizeZ, new Ground());
		this.fixLimits();
		// Save the entire level
		app.getArenaManager().getArena().save();
		this.checkMenus();
		// Redraw
		this.redrawEditor();
	    } catch (final NumberFormatException nf) {
		CommonDialogs.showDialog(nf.getMessage());
		success = false;
	    }
	} else {
	    // User canceled
	    success = false;
	}
	return success;
    }

    @Override
    public void setInitialState() {
	this.disableModeCommands();
    }

    public void setLevelPrefs() {
	this.lPrefs.showPrefs();
    }

    public void setPlayerLocation() {
	final Tank template = new Tank(this.activePlayer + 1);
	final Application app = LaserTank.getApplication();
	final int oldX = app.getArenaManager().getArena().getStartColumn(this.activePlayer);
	final int oldY = app.getArenaManager().getArena().getStartRow(this.activePlayer);
	final int oldZ = app.getArenaManager().getArena().getStartFloor(this.activePlayer);
	// Erase old player
	try {
	    app.getArenaManager().getArena().setCell(new Ground(), oldX, oldY, oldZ, template.getLayer());
	} catch (final ArrayIndexOutOfBoundsException aioob) {
	    // Ignore
	}
	// Set new player
	app.getArenaManager().getArena().setStartRow(this.activePlayer, this.elMgr.getEditorLocationY());
	app.getArenaManager().getArena().setStartColumn(this.activePlayer, this.elMgr.getEditorLocationX());
	app.getArenaManager().getArena().setStartFloor(this.activePlayer, this.elMgr.getEditorLocationZ());
	app.getArenaManager().getArena().setCell(template, this.elMgr.getEditorLocationX(),
		this.elMgr.getEditorLocationY(), this.elMgr.getEditorLocationZ(), template.getLayer());
    }

    void setPlayerLocation(final int x, final int y) {
	final Tank template = new Tank(this.activePlayer + 1);
	final Application app = LaserTank.getApplication();
	final int xOffset = this.vertScroll.getValue() - this.vertScroll.getMinimum();
	final int yOffset = this.horzScroll.getValue() - this.horzScroll.getMinimum();
	final int destX = x / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationX()
		- xOffset + yOffset;
	final int destY = y / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationY()
		+ xOffset - yOffset;
	final int oldX = app.getArenaManager().getArena().getStartColumn(this.activePlayer);
	final int oldY = app.getArenaManager().getArena().getStartRow(this.activePlayer);
	final int oldZ = app.getArenaManager().getArena().getStartFloor(this.activePlayer);
	// Erase old player
	try {
	    app.getArenaManager().getArena().setCell(new Ground(), oldX, oldY, oldZ, template.getLayer());
	} catch (final ArrayIndexOutOfBoundsException aioob) {
	    // Ignore
	}
	// Set new player
	try {
	    app.getArenaManager().getArena().setStartRow(this.activePlayer, destY);
	    app.getArenaManager().getArena().setStartColumn(this.activePlayer, destX);
	    app.getArenaManager().getArena().setStartFloor(this.activePlayer, this.elMgr.getEditorLocationZ());
	    app.getArenaManager().getArena().setCell(template, destX, destY, this.elMgr.getEditorLocationZ(),
		    template.getLayer());
	    LaserTank.getApplication().showMessage(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		    StringConstants.EDITOR_STRING_START_POINT_SET));
	} catch (final ArrayIndexOutOfBoundsException aioob) {
	    try {
		app.getArenaManager().getArena().setStartRow(this.activePlayer, oldY);
		app.getArenaManager().getArena().setStartColumn(this.activePlayer, oldX);
		app.getArenaManager().getArena().setCell(template, oldX, oldY, oldZ, template.getLayer());
	    } catch (final ArrayIndexOutOfBoundsException aioob2) {
		// Ignore
	    }
	    LaserTank.getApplication().showMessage(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		    StringConstants.EDITOR_STRING_AIM_WITHIN_THE_ARENA));
	}
	// Swap event handlers
	this.secondaryPane.removeMouseListener(this.shandler);
	this.secondaryPane.addMouseListener(this.mhandler);
	// Set dirty flag
	app.getArenaManager().setDirty(true);
	this.redrawEditor();
    }

    public void setStatusMessage(final String msg) {
	this.messageLabel.setText(msg);
    }

    private void setUpGUI() {
	// Destroy the old GUI, if one exists
	if (this.outputFrame != null) {
	    this.outputFrame.dispose();
	}
	final FocusHandler fHandler = new FocusHandler();
	this.messageLabel = new JLabel(StringConstants.COMMON_STRING_SPACE);
	this.outputFrame = new JFrame(
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE, StringConstants.EDITOR_STRING_EDITOR));
	this.outputPane = new EditorDraw();
	this.secondaryPane = new Container();
	this.borderPane = new Container();
	this.borderPane.setLayout(new BorderLayout());
	this.outputFrame.setContentPane(this.borderPane);
	this.outputFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	this.messageLabel.setLabelFor(this.outputPane);
	this.outerOutputPane = RCLGenerator.generateRowColumnLabels();
	this.outerOutputPane.add(this.outputPane, BorderLayout.CENTER);
	this.outputPane.setLayout(new GridLayout(1, 1));
	this.outputFrame.setResizable(false);
	this.secondaryPane.setLayout(new GridLayout(EditorViewingWindowManager.getViewingWindowSizeX(),
		EditorViewingWindowManager.getViewingWindowSizeY()));
	this.horzScroll = new JScrollBar(Adjustable.HORIZONTAL,
		EditorViewingWindowManager.getMinimumViewingWindowLocationY(),
		EditorViewingWindowManager.getViewingWindowSizeY(),
		EditorViewingWindowManager.getMinimumViewingWindowLocationY(),
		EditorViewingWindowManager.getViewingWindowSizeY());
	this.vertScroll = new JScrollBar(Adjustable.VERTICAL,
		EditorViewingWindowManager.getMinimumViewingWindowLocationX(),
		EditorViewingWindowManager.getViewingWindowSizeX(),
		EditorViewingWindowManager.getMinimumViewingWindowLocationX(),
		EditorViewingWindowManager.getViewingWindowSizeX());
	this.outputPane.add(this.secondaryPane);
	this.secondaryPane.addMouseListener(this.mhandler);
	this.secondaryPane.addMouseMotionListener(this.mhandler);
	this.outputFrame.addWindowListener(this.mhandler);
	this.outputFrame.addWindowFocusListener(fHandler);
	this.switcherPane = new Container();
	final SwitcherHandler switcherHandler = new SwitcherHandler();
	final ButtonGroup switcherGroup = new ButtonGroup();
	this.lowerGround = new JToggleButton(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		StringConstants.EDITOR_STRING_LOWER_GROUND_LAYER));
	this.upperGround = new JToggleButton(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		StringConstants.EDITOR_STRING_UPPER_GROUND_LAYER));
	this.lowerObjects = new JToggleButton(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		StringConstants.EDITOR_STRING_LOWER_OBJECTS_LAYER));
	this.upperObjects = new JToggleButton(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		StringConstants.EDITOR_STRING_UPPER_OBJECTS_LAYER));
	this.lowerGround.setSelected(true);
	this.lowerGround.addActionListener(switcherHandler);
	this.upperGround.addActionListener(switcherHandler);
	this.lowerObjects.addActionListener(switcherHandler);
	this.upperObjects.addActionListener(switcherHandler);
	switcherGroup.add(this.lowerGround);
	switcherGroup.add(this.upperGround);
	switcherGroup.add(this.lowerObjects);
	switcherGroup.add(this.upperObjects);
	this.switcherPane.setLayout(new FlowLayout());
	this.switcherPane.add(this.lowerGround);
	this.switcherPane.add(this.upperGround);
	this.switcherPane.add(this.lowerObjects);
	this.switcherPane.add(this.upperObjects);
    }

    public void showOutput() {
	final Application app = LaserTank.getApplication();
	this.outputFrame.setJMenuBar(app.getMenuManager().getMainMenuBar());
	app.getMenuManager().checkFlags();
	this.outputFrame.setVisible(true);
	this.outputFrame.pack();
    }

    public void undo() {
	final Application app = LaserTank.getApplication();
	this.engine.undo();
	final AbstractArenaObject obj = this.engine.getObject();
	final int x = this.engine.getX();
	final int y = this.engine.getY();
	final int z = this.engine.getZ();
	final int w = this.engine.getW();
	final int u = this.engine.getU();
	this.elMgr.setEditorLocationX(x);
	this.elMgr.setEditorLocationY(y);
	if (x != -1 && y != -1 && z != -1 && u != -1) {
	    final AbstractArenaObject oldObj = app.getArenaManager().getArena().getCell(x, y, z, w);
	    app.getArenaManager().getArena().setCell(obj, x, y, z, w);
	    this.updateRedoHistory(oldObj, x, y, z, w, u);
	    this.checkMenus();
	    this.redrawEditor();
	} else {
	    LaserTank.getApplication().showMessage(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		    StringConstants.EDITOR_STRING_NOTHING_TO_UNDO));
	}
    }

    public void updateEditorLevelAbsolute(final int w) {
	this.elMgr.setEditorLocationU(w);
	// Level Change
	LaserTank.getApplication().getArenaManager().getArena().switchLevel(w);
	this.fixLimits();
	this.setUpGUI();
	this.checkMenus();
	this.redrawEditor();
    }

    public void updateEditorPosition(final int z, final int w) {
	this.elMgr.offsetEditorLocationU(w);
	this.elMgr.offsetEditorLocationZ(z);
	if (w != 0) {
	    // Level Change
	    LaserTank.getApplication().getArenaManager().getArena().switchLevelOffset(w);
	    this.fixLimits();
	    this.setUpGUI();
	}
	this.checkMenus();
	this.redrawEditor();
    }

    private void updatePicker() {
	if (this.elMgr != null) {
	    final ArenaObjectList objectList = LaserTank.getApplication().getObjects();
	    this.objects = objectList.getAllObjectsOnLayer(this.elMgr.getEditorLocationW(),
		    PreferencesManager.getEditorShowAllObjects());
	    this.editorAppearances = objectList.getAllEditorAppearancesOnLayer(this.elMgr.getEditorLocationW(),
		    PreferencesManager.getEditorShowAllObjects());
	    this.objectsEnabled = objectList.getObjectEnabledStatuses(this.elMgr.getEditorLocationW());
	    final BufferedImageIcon[] newImages = this.editorAppearances;
	    final boolean[] enabled = this.objectsEnabled;
	    if (this.picker != null) {
		this.picker.updatePicker(newImages, enabled);
	    } else {
		this.picker = new PicturePicker(newImages, enabled);
	    }
	    this.updatePickerLayout();
	}
    }

    private void updatePickerLayout() {
	if (this.picker != null) {
	    this.picker.updatePickerLayout(this.outputPane.getLayout().preferredLayoutSize(this.outputPane).height);
	}
    }

    private void updateRedoHistory(final AbstractArenaObject obj, final int x, final int y, final int z, final int w,
	    final int u) {
	this.engine.updateRedoHistory(obj, x, y, z, w, u);
    }

    private void updateUndoHistory(final AbstractArenaObject obj, final int x, final int y, final int z, final int w,
	    final int u) {
	this.engine.updateUndoHistory(obj, x, y, z, w, u);
    }
}
