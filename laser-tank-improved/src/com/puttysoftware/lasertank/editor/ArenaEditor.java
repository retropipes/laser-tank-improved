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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JToggleButton;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.images.BufferedImageIcon;
import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.arena.ArenaManager;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractJumpObject;
import com.puttysoftware.lasertank.arena.objects.Ground;
import com.puttysoftware.lasertank.arena.objects.Tank;
import com.puttysoftware.lasertank.game.GameManager;
import com.puttysoftware.lasertank.prefs.PreferencesManager;
import com.puttysoftware.lasertank.resourcemanagers.ImageManager;
import com.puttysoftware.lasertank.strings.CommonString;
import com.puttysoftware.lasertank.strings.EditorString;
import com.puttysoftware.lasertank.strings.MenuString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.ArenaObjectList;
import com.puttysoftware.lasertank.utilities.DrawGrid;
import com.puttysoftware.lasertank.utilities.EditorLayoutConstants;
import com.puttysoftware.lasertank.utilities.InvalidArenaException;
import com.puttysoftware.lasertank.utilities.RCLGenerator;
import com.puttysoftware.pickers.PicturePicker;

public class ArenaEditor {
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
		} else {
		    me.editObject(x, y);
		}
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
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
		LaserTank.logError(ex);
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
	    LaserTank.getApplication().getMenuManager().updateMenuItemState();
	}

	@Override
	public void windowLostFocus(final WindowEvent e) {
	    // Do nothing
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
		LaserTank.logError(ex);
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
		if (cmd.equals(StringLoader.loadEditor(EditorString.LOWER_GROUND_LAYER))) {
		    ae.changeLayerImpl(ArenaConstants.LAYER_LOWER_GROUND);
		} else if (cmd.equals(StringLoader.loadEditor(EditorString.UPPER_GROUND_LAYER))) {
		    ae.changeLayerImpl(ArenaConstants.LAYER_UPPER_GROUND);
		} else if (cmd.equals(StringLoader.loadEditor(EditorString.LOWER_OBJECTS_LAYER))) {
		    ae.changeLayerImpl(ArenaConstants.LAYER_LOWER_OBJECTS);
		} else if (cmd.equals(StringLoader.loadEditor(EditorString.UPPER_OBJECTS_LAYER))) {
		    ae.changeLayerImpl(ArenaConstants.LAYER_UPPER_OBJECTS);
		}
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}
    }

    private static final String[] JUMP_LIST = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
    // Declarations
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
    private final FocusHandler fHandler = new FocusHandler();

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
	    CommonDialogs.showDialog(StringLoader.loadEditor(EditorString.LEVEL_ADDED));
	} else {
	    CommonDialogs.showDialog(StringLoader.loadEditor(EditorString.LEVEL_ADDING_FAILED));
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
	    app.getMenuManager().updateMenuItemState();
	}
	return success;
    }

    public void arenaChanged() {
	this.arenaChanged = true;
    }

    public void changeLayer() {
	final String[] list = ArenaConstants.getLayerList();
	final String choice = CommonDialogs.showInputDialog(StringLoader.loadEditor(EditorString.CHANGE_LAYER_PROMPT),
		StringLoader.loadEditor(EditorString.EDITOR), list, list[this.elMgr.getEditorLocationW()]);
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

    public void clearHistory() {
	Application app = LaserTank.getApplication();
	this.engine = new EditorUndoRedoEngine();
	app.getMenuManager().updateMenuItemState();
    }

    private boolean confirmNonUndoable() {
	final int confirm = CommonDialogs.showConfirmDialog(
		StringLoader.loadEditor(EditorString.CONFIRM_CANNOT_BE_UNDONE),
		StringLoader.loadEditor(EditorString.EDITOR));
	if (confirm == JOptionPane.YES_OPTION) {
	    this.clearHistory();
	    return true;
	}
	return false;
    }

    void disableOutput() {
	this.borderPane.setEnabled(false);
    }

    public void editArena() {
	final Application app = LaserTank.getApplication();
	if (app.getArenaManager().getLoaded()) {
	    app.setInEditor(this.borderPane);
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
	    app.getMenuManager().updateMenuItemState();
	} else {
	    CommonDialogs.showDialog(StringLoader.loadMenu(MenuString.ERROR_NO_ARENA_OPENED));
	}
    }

    public void editJumpBox(final AbstractJumpObject jumper) {
	final int currentX = jumper.getJumpCols();
	final int currentY = jumper.getJumpRows();
	final String newXStr = CommonDialogs.showInputDialog(StringLoader.loadEditor(EditorString.HORZ_JUMP),
		StringLoader.loadEditor(EditorString.EDITOR), ArenaEditor.JUMP_LIST, ArenaEditor.JUMP_LIST[currentX]);
	if (newXStr != null) {
	    final String newYStr = CommonDialogs.showInputDialog(StringLoader.loadEditor(EditorString.VERT_JUMP),
		    StringLoader.loadEditor(EditorString.EDITOR), ArenaEditor.JUMP_LIST,
		    ArenaEditor.JUMP_LIST[currentY]);
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
	    app.getMenuManager().updateMenuItemState();
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
		    LaserTank.getApplication().showMessage(StringLoader.loadEditor(EditorString.NO_PROPERTIES));
		} else {
		    this.updateUndoHistory(this.savedArenaObject, gridX, gridY, this.elMgr.getEditorLocationZ(),
			    this.elMgr.getEditorLocationW(), this.elMgr.getEditorLocationU());
		    app.getArenaManager().getArena().setCell(mo2, gridX, gridY, this.elMgr.getEditorLocationZ(),
			    this.elMgr.getEditorLocationW());
		    app.getMenuManager().updateMenuItemState();
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
	LaserTank.getApplication().showMessage(StringLoader.loadEditor(EditorString.SET_START_POINT));
    }

    void enableOutput() {
	Application app = LaserTank.getApplication();
	this.borderPane.setEnabled(true);
	app.getMenuManager().updateMenuItemState();
    }

    public void exitEditor() {
	final Application app = LaserTank.getApplication();
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
	    LaserTank.getApplication().showMessage(StringLoader.loadEditor(EditorString.LEVEL_FILLED));
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

    public void goToLevelHandler() {
	int locW;
	final String msg = StringLoader.loadEditor(EditorString.GO_TO_LEVEL);
	String input;
	final String[] choices = LaserTank.getApplication().getLevelInfoList();
	input = CommonDialogs.showInputDialog(StringLoader.loadEditor(EditorString.GO_TO_WHICH_LEVEL), msg, choices,
		choices[0]);
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
	    LaserTank.logError(ex);
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
		throw new InvalidArenaException(ioe);
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
	    CommonDialogs.showDialog(StringLoader.loadEditor(EditorString.ARENA_CREATED));
	} else {
	    CommonDialogs.showDialog(StringLoader.loadEditor(EditorString.ARENA_CREATION_FAILED));
	}
	return success;
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
	    app.getMenuManager().updateMenuItemState();
	    this.redrawEditor();
	} else {
	    LaserTank.getApplication().showMessage(StringLoader.loadEditor(EditorString.NOTHING_TO_REDO));
	}
    }

    public void redrawEditor() {
	final Application app = LaserTank.getApplication();
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
	app.pack();
	app.setTitle(StringLoader.loadEditor(EditorString.EDITOR_TITLE_1) + (z + 1)
		+ StringLoader.loadEditor(EditorString.EDITOR_TITLE_2) + (u + 1)
		+ StringLoader.loadCommon(CommonString.SPACE_DASH_SPACE) + StringLoader.loadTime(e));
	this.outputPane.repaint();
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
	input = CommonDialogs.showInputDialog(StringLoader.loadEditor(EditorString.WHICH_LEVEL_TO_REMOVE),
		StringLoader.loadEditor(EditorString.REMOVE_LEVEL), choices, choices[0]);
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
			app.getMenuManager().updateMenuItemState();
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
	final Application app = LaserTank.getApplication();
	if (this.borderPane != null) {
	    this.updatePicker();
	    this.borderPane.removeAll();
	    this.borderPane.add(this.outerOutputPane, BorderLayout.CENTER);
	    this.borderPane.add(this.messageLabel, BorderLayout.NORTH);
	    this.borderPane.add(this.picker.getPicker(), BorderLayout.EAST);
	    this.borderPane.add(this.switcherPane, BorderLayout.SOUTH);
	    app.pack();
	}
    }

    public boolean resizeLevel() {
	final Application app = LaserTank.getApplication();
	int levelSizeZ;
	final int maxF = AbstractArena.getMaxFloors();
	final int minF = AbstractArena.getMinFloors();
	final String msg = StringLoader.loadEditor(EditorString.RESIZE_LEVEL);
	boolean success = true;
	String input3;
	input3 = CommonDialogs.showTextInputDialogWithDefault(StringLoader.loadEditor(EditorString.NUMBER_OF_FLOORS),
		msg, Integer.toString(app.getArenaManager().getArena().getFloors()));
	if (input3 != null) {
	    try {
		levelSizeZ = Integer.parseInt(input3);
		if (levelSizeZ < minF) {
		    throw new NumberFormatException(StringLoader.loadEditor(EditorString.FLOORS_TOO_LOW));
		}
		if (levelSizeZ > maxF) {
		    throw new NumberFormatException(StringLoader.loadEditor(EditorString.FLOORS_TOO_HIGH));
		}
		app.getArenaManager().getArena().resize(levelSizeZ, new Ground());
		this.fixLimits();
		// Save the entire level
		app.getArenaManager().getArena().save();
		app.getMenuManager().updateMenuItemState();
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
	    LaserTank.getApplication().showMessage(StringLoader.loadEditor(EditorString.START_POINT_SET));
	} catch (final ArrayIndexOutOfBoundsException aioob) {
	    try {
		app.getArenaManager().getArena().setStartRow(this.activePlayer, oldY);
		app.getArenaManager().getArena().setStartColumn(this.activePlayer, oldX);
		app.getArenaManager().getArena().setCell(template, oldX, oldY, oldZ, template.getLayer());
	    } catch (final ArrayIndexOutOfBoundsException aioob2) {
		// Ignore
	    }
	    LaserTank.getApplication().showMessage(StringLoader.loadEditor(EditorString.AIM_WITHIN_THE_ARENA));
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
	this.messageLabel = new JLabel(StringLoader.loadCommon(CommonString.SPACE));
	this.outputPane = new EditorDraw();
	this.secondaryPane = new Container();
	this.borderPane = new Container();
	this.borderPane.setLayout(new BorderLayout());
	this.messageLabel.setLabelFor(this.outputPane);
	this.outerOutputPane = RCLGenerator.generateRowColumnLabels();
	this.outerOutputPane.add(this.outputPane, BorderLayout.CENTER);
	this.outputPane.setLayout(new GridLayout(1, 1));
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
	this.switcherPane = new Container();
	final SwitcherHandler switcherHandler = new SwitcherHandler();
	final ButtonGroup switcherGroup = new ButtonGroup();
	this.lowerGround = new JToggleButton(StringLoader.loadEditor(EditorString.LOWER_GROUND_LAYER));
	this.upperGround = new JToggleButton(StringLoader.loadEditor(EditorString.UPPER_GROUND_LAYER));
	this.lowerObjects = new JToggleButton(StringLoader.loadEditor(EditorString.LOWER_OBJECTS_LAYER));
	this.upperObjects = new JToggleButton(StringLoader.loadEditor(EditorString.UPPER_OBJECTS_LAYER));
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

    public boolean tryUndo() {
	return this.engine.tryUndo();
    }

    public boolean tryRedo() {
	return this.engine.tryRedo();
    }

    public boolean tryBoth() {
	return this.engine.tryBoth();
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
	    app.getMenuManager().updateMenuItemState();
	    this.redrawEditor();
	} else {
	    LaserTank.getApplication().showMessage(StringLoader.loadEditor(EditorString.NOTHING_TO_UNDO));
	}
    }

    public void updateEditorLevelAbsolute(final int w) {
	Application app = LaserTank.getApplication();
	this.elMgr.setEditorLocationU(w);
	// Level Change
	app.getArenaManager().getArena().switchLevel(w);
	this.fixLimits();
	this.setUpGUI();
	app.getMenuManager().updateMenuItemState();
	this.redrawEditor();
    }

    public void updateEditorPosition(final int z, final int w) {
	Application app = LaserTank.getApplication();
	this.elMgr.offsetEditorLocationU(w);
	this.elMgr.offsetEditorLocationZ(z);
	if (w != 0) {
	    // Level Change
	    app.getArenaManager().getArena().switchLevelOffset(w);
	    this.fixLimits();
	    this.setUpGUI();
	}
	app.getMenuManager().updateMenuItemState();
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

    public void setUp() {
	final Application app = LaserTank.getApplication();
	app.setTitle(StringLoader.loadEditor(EditorString.EDITOR));
	app.addWindowListener(this.mhandler);
	app.addWindowFocusListener(this.fHandler);
    }

    public void tearDown() {
	final Application app = LaserTank.getApplication();
	app.removeWindowListener(this.mhandler);
	app.removeWindowFocusListener(this.fHandler);
    }
}
