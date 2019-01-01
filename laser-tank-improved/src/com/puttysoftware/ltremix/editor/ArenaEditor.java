/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.editor;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
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

import com.puttysoftware.lasertank.improved.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.improved.images.BufferedImageIcon;
import com.puttysoftware.lasertank.improved.pickers.SXSPicturePicker;
import com.puttysoftware.lasertank.utilities.InvalidArenaException;
import com.puttysoftware.ltremix.Accelerators;
import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.MenuSection;
import com.puttysoftware.ltremix.arena.AbstractArena;
import com.puttysoftware.ltremix.arena.ArenaManager;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractJumpObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractRemoteControlObject;
import com.puttysoftware.ltremix.arena.objects.Empty;
import com.puttysoftware.ltremix.arena.objects.Ground;
import com.puttysoftware.ltremix.arena.objects.Tank;
import com.puttysoftware.ltremix.game.GameManager;
import com.puttysoftware.ltremix.resourcemanagers.ImageManager;
import com.puttysoftware.ltremix.resourcemanagers.LogoManager;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;
import com.puttysoftware.ltremix.utilities.ArenaConstants;
import com.puttysoftware.ltremix.utilities.ArenaObjectList;
import com.puttysoftware.ltremix.utilities.DrawGrid;
import com.puttysoftware.ltremix.utilities.EraConstants;
import com.puttysoftware.ltremix.utilities.RCLGenerator;

public class ArenaEditor implements MenuSection {
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
    private SXSPicturePicker picker;
    private AbstractArenaObject[] objects;
    private BufferedImageIcon[] editorAppearances;
    private boolean[] objectsEnabled;
    private EditorUndoRedoEngine engine;
    private EditorLocationManager elMgr;
    private boolean arenaChanged;
    private int playerInstance;
    private int activeEra;
    private JMenuItem editorUndo, editorRedo, editorCutLevel, editorCopyLevel, editorPasteLevel,
	    editorInsertLevelFromClipboard, editorClearHistory, editorGoToLevel, editorUpOneFloor, editorDownOneFloor,
	    editorUpOneLevel, editorDownOneLevel, editorAddLevel, editorRemoveLevel, editorLevelPreferences,
	    editorSetStartPoint, editorFillLevel, editorResizeLevel, editorChangeLayer, editorChangePlayer,
	    editorChangeEra;
    private JCheckBoxMenuItem editorEnableEraCloneMode;
    private static final int STACK_COUNT = 6;
    private static final String[] JUMP_LIST = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
    private static final String[] PLAYER_LIST = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" };

    public ArenaEditor() {
	this.savedArenaObject = new Ground();
	this.lPrefs = new LevelPreferencesManager();
	this.mhandler = new EventHandler();
	this.shandler = new StartEventHandler();
	this.engine = new EditorUndoRedoEngine();
	final ArenaObjectList objectList = LTRemix.getApplication().getObjects();
	this.objects = objectList.getAllObjectsOnLayer(ArenaConstants.LAYER_LOWER_GROUND);
	this.editorAppearances = objectList.getAllEditorAppearancesOnLayer(ArenaConstants.LAYER_LOWER_GROUND);
	this.objectsEnabled = objectList.getObjectEnabledStatuses(ArenaConstants.LAYER_LOWER_GROUND);
	this.arenaChanged = true;
	this.playerInstance = 0;
    }

    public void activeLanguageChanged() {
	this.updatePicker();
    }

    public void arenaChanged() {
	this.arenaChanged = true;
    }

    public EditorLocationManager getLocationManager() {
	return this.elMgr;
    }

    void changePlayer() {
	final String[] list = ArenaEditor.PLAYER_LIST;
	final String choice = CommonDialogs.showInputDialog(
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_CHANGE_PLAYER_PROMPT),
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE, StringConstants.EDITOR_STRING_EDITOR),
		list, list[this.playerInstance]);
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
		this.playerInstance = index;
	    }
	}
    }

    void changeEra() {
	final String[] list = EraConstants.getEraNames();
	final String choice = CommonDialogs.showInputDialog(
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_CHANGE_ERA_PROMPT),
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE, StringConstants.EDITOR_STRING_EDITOR),
		list, list[this.activeEra]);
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
		this.activeEra = index;
		ImageManager.changeEra(this.activeEra);
		LTRemix.getApplication().getArenaManager().getArena().setDirtyFlags(this.elMgr.getEditorLocationZ());
		this.updatePicker();
		this.redrawEditor(false);
	    }
	}
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
	this.redrawEditor(false);
    }

    public void updateEditorPosition(final int z, final int w) {
	this.elMgr.offsetEditorLocationU(w);
	this.elMgr.offsetEditorLocationZ(z);
	if (w != 0) {
	    // Level Change
	    LTRemix.getApplication().getArenaManager().getArena().switchLevelOffset(w);
	    this.fixLimits();
	    this.setUpGUI();
	}
	this.checkMenus();
	this.redrawEditor(false);
    }

    public void updateEditorLevelAbsolute(final int w) {
	this.elMgr.setEditorLocationU(w);
	// Level Change
	LTRemix.getApplication().getArenaManager().getArena().switchLevel(w);
	this.fixLimits();
	this.setUpGUI();
	this.checkMenus();
	this.redrawEditor(false);
    }

    private void checkMenus() {
	final Application app = LTRemix.getApplication();
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

    public void setLevelPrefs() {
	this.lPrefs.showPrefs();
    }

    public void redrawEditor(final boolean suspendTitleUpdates) {
	final int z = this.elMgr.getEditorLocationZ();
	final int w = this.elMgr.getEditorLocationW();
	final int u = this.elMgr.getEditorLocationU();
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
	if (!suspendTitleUpdates) {
	    this.outputFrame.setTitle(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		    StringConstants.EDITOR_STRING_EDITOR_TITLE_1)
		    + (z + 1)
		    + StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			    StringConstants.EDITOR_STRING_EDITOR_TITLE_2)
		    + (u + 1) + StringConstants.COMMON_STRING_COLON + StringConstants.COMMON_STRING_SPACE
		    + EraConstants.getEraNames()[this.activeEra]);
	}
	this.outputPane.repaint();
	this.showOutput();
    }

    private void redrawEditorBottomGround() {
	// Draw the arena in edit mode
	final Application app = LTRemix.getApplication();
	int x, y;
	int xFix, yFix;
	final DrawGrid drawGrid = this.outputPane.getGrid();
	for (x = EditorViewingWindowManager.getViewingWindowLocationX(); x <= EditorViewingWindowManager
		.getLowerRightViewingWindowLocationX(); x++) {
	    for (y = EditorViewingWindowManager.getViewingWindowLocationY(); y <= EditorViewingWindowManager
		    .getLowerRightViewingWindowLocationY(); y++) {
		xFix = x - EditorViewingWindowManager.getViewingWindowLocationX();
		yFix = y - EditorViewingWindowManager.getViewingWindowLocationY();
		final AbstractArenaObject lgobj = app.getArenaManager().getArena().getCellEra(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_LOWER_GROUND, this.activeEra);
		drawGrid.setImageCell(ImageManager.getImage(lgobj, true), xFix, yFix);
	    }
	}
    }

    private void redrawEditorGround() {
	// Draw the arena in edit mode
	final Application app = LTRemix.getApplication();
	int x, y;
	int xFix, yFix;
	final DrawGrid drawGrid = this.outputPane.getGrid();
	for (x = EditorViewingWindowManager.getViewingWindowLocationX(); x <= EditorViewingWindowManager
		.getLowerRightViewingWindowLocationX(); x++) {
	    for (y = EditorViewingWindowManager.getViewingWindowLocationY(); y <= EditorViewingWindowManager
		    .getLowerRightViewingWindowLocationY(); y++) {
		xFix = x - EditorViewingWindowManager.getViewingWindowLocationX();
		yFix = y - EditorViewingWindowManager.getViewingWindowLocationY();
		final AbstractArenaObject lgobj = app.getArenaManager().getArena().getCellEra(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_LOWER_GROUND, this.activeEra);
		final AbstractArenaObject ugobj = app.getArenaManager().getArena().getCellEra(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_UPPER_GROUND, this.activeEra);
		drawGrid.setImageCell(ImageManager.getCompositeImage(lgobj, ugobj, true), xFix, yFix);
	    }
	}
    }

    private void redrawEditorGroundBottomObjects() {
	// Draw the arena in edit mode
	final Application app = LTRemix.getApplication();
	int x, y;
	int xFix, yFix;
	final DrawGrid drawGrid = this.outputPane.getGrid();
	for (x = EditorViewingWindowManager.getViewingWindowLocationX(); x <= EditorViewingWindowManager
		.getLowerRightViewingWindowLocationX(); x++) {
	    for (y = EditorViewingWindowManager.getViewingWindowLocationY(); y <= EditorViewingWindowManager
		    .getLowerRightViewingWindowLocationY(); y++) {
		xFix = x - EditorViewingWindowManager.getViewingWindowLocationX();
		yFix = y - EditorViewingWindowManager.getViewingWindowLocationY();
		final AbstractArenaObject lgobj = app.getArenaManager().getArena().getCellEra(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_LOWER_GROUND, this.activeEra);
		final AbstractArenaObject ugobj = app.getArenaManager().getArena().getCellEra(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_UPPER_GROUND, this.activeEra);
		final AbstractArenaObject loobj = app.getArenaManager().getArena().getCellEra(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_LOWER_OBJECTS, this.activeEra);
		drawGrid.setImageCell(ImageManager.getVirtualCompositeImage(lgobj, ugobj, loobj), xFix, yFix);
	    }
	}
    }

    private void redrawEditorGroundObjects() {
	// Draw the arena in edit mode
	final Application app = LTRemix.getApplication();
	int x, y;
	int xFix, yFix;
	final DrawGrid drawGrid = this.outputPane.getGrid();
	for (x = EditorViewingWindowManager.getViewingWindowLocationX(); x <= EditorViewingWindowManager
		.getLowerRightViewingWindowLocationX(); x++) {
	    for (y = EditorViewingWindowManager.getViewingWindowLocationY(); y <= EditorViewingWindowManager
		    .getLowerRightViewingWindowLocationY(); y++) {
		xFix = x - EditorViewingWindowManager.getViewingWindowLocationX();
		yFix = y - EditorViewingWindowManager.getViewingWindowLocationY();
		final AbstractArenaObject lgobj = app.getArenaManager().getArena().getCellEra(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_LOWER_GROUND, this.activeEra);
		final AbstractArenaObject ugobj = app.getArenaManager().getArena().getCellEra(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_UPPER_GROUND, this.activeEra);
		final AbstractArenaObject loobj = app.getArenaManager().getArena().getCellEra(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_LOWER_OBJECTS, this.activeEra);
		final AbstractArenaObject uoobj = app.getArenaManager().getArena().getCellEra(y, x,
			this.elMgr.getEditorLocationZ(), ArenaConstants.LAYER_UPPER_OBJECTS, this.activeEra);
		drawGrid.setImageCell(ImageManager.getVirtualCompositeImage(lgobj, ugobj, loobj, uoobj), xFix, yFix);
	    }
	}
    }

    void editObject(final int x, final int y) {
	if (this.editorEnableEraCloneMode.isSelected()) {
	    final int save = this.activeEra;
	    for (int e = 0; e < EraConstants.MAX_ERAS; e++) {
		this.activeEra = e;
		this.editObjectImpl(x, y, true);
	    }
	    this.activeEra = save;
	} else {
	    this.editObjectImpl(x, y, false);
	}
    }

    private void editObjectImpl(final int x, final int y, final boolean suspendTitleUpdates) {
	final Application app = LTRemix.getApplication();
	final int currentObjectIndex = this.picker.getPicked();
	final int xOffset = this.vertScroll.getValue() - this.vertScroll.getMinimum();
	final int yOffset = this.horzScroll.getValue() - this.horzScroll.getMinimum();
	final int gridX = x / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationX()
		- xOffset + yOffset;
	final int gridY = y / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationY()
		+ xOffset - yOffset;
	try {
	    this.savedArenaObject = app.getArenaManager().getArena().getCellEra(gridX, gridY,
		    this.elMgr.getEditorLocationZ(), this.elMgr.getEditorLocationW(), this.activeEra);
	} catch (final ArrayIndexOutOfBoundsException ae) {
	    return;
	}
	final AbstractArenaObject[] choices = this.objects;
	final AbstractArenaObject mo = choices[currentObjectIndex];
	final AbstractArenaObject instance = mo.clone();
	this.elMgr.setEditorLocationX(gridX);
	this.elMgr.setEditorLocationY(gridY);
	this.savedArenaObject.editorRemoveHook(gridX, gridY, this.elMgr.getEditorLocationZ());
	final boolean setObj = mo.editorPlaceHook(gridX, gridY, this.elMgr.getEditorLocationZ());
	try {
	    this.updateUndoHistory(this.savedArenaObject, gridX, gridY, this.elMgr.getEditorLocationZ(),
		    this.elMgr.getEditorLocationW(), this.elMgr.getEditorLocationU());
	    if (setObj) {
		app.getArenaManager().getArena().setCellEra(instance, gridX, gridY, this.elMgr.getEditorLocationZ(),
			this.elMgr.getEditorLocationW(), this.activeEra);
	    }
	    app.getArenaManager().setDirty(true);
	    this.checkMenus();
	    this.redrawEditor(suspendTitleUpdates);
	} catch (final ArrayIndexOutOfBoundsException aioob) {
	    app.getArenaManager().getArena().setCellEra(this.savedArenaObject, gridX, gridY,
		    this.elMgr.getEditorLocationZ(), this.elMgr.getEditorLocationW(), this.activeEra);
	    this.redrawEditor(suspendTitleUpdates);
	}
    }

    void probeObjectProperties(final int x, final int y) {
	final Application app = LTRemix.getApplication();
	final int xOffset = this.vertScroll.getValue() - this.vertScroll.getMinimum();
	final int yOffset = this.horzScroll.getValue() - this.horzScroll.getMinimum();
	final int gridX = x / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationX()
		- xOffset + yOffset;
	final int gridY = y / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationY()
		+ xOffset - yOffset;
	final AbstractArenaObject mo = app.getArenaManager().getArena().getCellEra(gridX, gridY,
		this.elMgr.getEditorLocationZ(), this.elMgr.getEditorLocationW(), this.activeEra);
	this.elMgr.setEditorLocationX(gridX);
	this.elMgr.setEditorLocationY(gridY);
	final String gameName = mo.getIdentityName();
	final String desc = mo.getDescription();
	CommonDialogs.showTitledDialog(desc, gameName);
    }

    void editObjectProperties(final int x, final int y) {
	final Application app = LTRemix.getApplication();
	final int xOffset = this.vertScroll.getValue() - this.vertScroll.getMinimum();
	final int yOffset = this.horzScroll.getValue() - this.horzScroll.getMinimum();
	final int gridX = x / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationX()
		- xOffset + yOffset;
	final int gridY = y / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationY()
		+ xOffset - yOffset;
	try {
	    final AbstractArenaObject mo = app.getArenaManager().getArena().getCellEra(gridX, gridY,
		    this.elMgr.getEditorLocationZ(), this.elMgr.getEditorLocationW(), this.activeEra);
	    this.elMgr.setEditorLocationX(gridX);
	    this.elMgr.setEditorLocationY(gridY);
	    if (!mo.defersSetProperties()) {
		final AbstractArenaObject mo2 = mo.editorPropertiesHook();
		if (mo2 == null) {
		    LTRemix.getApplication().showMessage(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			    StringConstants.EDITOR_STRING_NO_PROPERTIES));
		} else {
		    this.updateUndoHistory(this.savedArenaObject, gridX, gridY, this.elMgr.getEditorLocationZ(),
			    this.elMgr.getEditorLocationW(), this.elMgr.getEditorLocationU());
		    app.getArenaManager().getArena().setCellEra(mo2, gridX, gridY, this.elMgr.getEditorLocationZ(),
			    this.elMgr.getEditorLocationW(), this.activeEra);
		    this.checkMenus();
		    app.getArenaManager().setDirty(true);
		}
	    } else {
		mo.editorPropertiesHook();
	    }
	    this.redrawEditor(false);
	} catch (final ArrayIndexOutOfBoundsException aioob) {
	    // Do nothing
	}
    }

    public void setStatusMessage(final String msg) {
	this.messageLabel.setText(msg);
    }

    public void editPlayerLocation() {
	// Swap event handlers
	this.secondaryPane.removeMouseListener(this.mhandler);
	this.secondaryPane.addMouseListener(this.shandler);
	LTRemix.getApplication().showMessage(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		StringConstants.EDITOR_STRING_SET_START_POINT));
    }

    public void clearPlayerLocation() {
	final Application app = LTRemix.getApplication();
	app.getArenaManager().getArena().setPlayerRow(-1, this.playerInstance, this.activeEra);
	app.getArenaManager().getArena().setPlayerColumn(-1, this.playerInstance, this.activeEra);
	app.getArenaManager().getArena().setPlayerFloor(-1, this.playerInstance, this.activeEra);
    }

    public void setPlayerLocation() {
	final Tank template = new Tank();
	final Application app = LTRemix.getApplication();
	final int oldX = app.getArenaManager().getArena().getPlayerColumn(this.playerInstance, this.activeEra);
	final int oldY = app.getArenaManager().getArena().getPlayerRow(this.playerInstance, this.activeEra);
	final int oldZ = app.getArenaManager().getArena().getPlayerFloor(this.playerInstance, this.activeEra);
	// Erase old player
	try {
	    app.getArenaManager().getArena().setCellEra(new Empty(), oldX, oldY, oldZ, template.getPrimaryLayer(),
		    this.activeEra);
	} catch (final ArrayIndexOutOfBoundsException aioob) {
	    // Ignore
	}
	// Set new player
	template.setInstanceNum(this.playerInstance);
	app.getArenaManager().getArena().setPlayerRow(this.elMgr.getEditorLocationY(), this.playerInstance,
		this.activeEra);
	app.getArenaManager().getArena().setPlayerColumn(this.elMgr.getEditorLocationX(), this.playerInstance,
		this.activeEra);
	app.getArenaManager().getArena().setPlayerFloor(this.elMgr.getEditorLocationZ(), this.playerInstance,
		this.activeEra);
	app.getArenaManager().getArena().setCellEra(template, this.elMgr.getEditorLocationX(),
		this.elMgr.getEditorLocationY(), this.elMgr.getEditorLocationZ(), template.getPrimaryLayer(),
		this.activeEra);
    }

    void setPlayerLocation(final int x, final int y) {
	final Tank template = new Tank();
	final Application app = LTRemix.getApplication();
	final int xOffset = this.vertScroll.getValue() - this.vertScroll.getMinimum();
	final int yOffset = this.horzScroll.getValue() - this.horzScroll.getMinimum();
	final int destX = x / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationX()
		- xOffset + yOffset;
	final int destY = y / ImageManager.getGraphicSize() + EditorViewingWindowManager.getViewingWindowLocationY()
		+ xOffset - yOffset;
	final int oldX = app.getArenaManager().getArena().getPlayerColumn(this.playerInstance, this.activeEra);
	final int oldY = app.getArenaManager().getArena().getPlayerRow(this.playerInstance, this.activeEra);
	final int oldZ = app.getArenaManager().getArena().getPlayerFloor(this.playerInstance, this.activeEra);
	// Erase old player
	try {
	    app.getArenaManager().getArena().setCellEra(new Empty(), oldX, oldY, oldZ, template.getPrimaryLayer(),
		    this.activeEra);
	} catch (final ArrayIndexOutOfBoundsException aioob) {
	    // Ignore
	}
	// Set new player
	template.setInstanceNum(this.playerInstance);
	try {
	    app.getArenaManager().getArena().saveStart();
	    app.getArenaManager().getArena().setCellEra(template, destX, destY, this.elMgr.getEditorLocationZ(),
		    template.getPrimaryLayer(), this.activeEra);
	    app.getArenaManager().getArena().setPlayerRow(destY, this.playerInstance, this.activeEra);
	    app.getArenaManager().getArena().setPlayerColumn(destX, this.playerInstance, this.activeEra);
	    app.getArenaManager().getArena().setPlayerFloor(this.elMgr.getEditorLocationZ(), this.playerInstance,
		    this.activeEra);
	    LTRemix.getApplication().showMessage(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		    StringConstants.EDITOR_STRING_START_POINT_SET));
	} catch (final ArrayIndexOutOfBoundsException aioob) {
	    app.getArenaManager().getArena().restoreStart();
	    try {
		app.getArenaManager().getArena().setCellEra(template, oldX, oldY, oldZ, template.getPrimaryLayer(),
			this.activeEra);
	    } catch (final ArrayIndexOutOfBoundsException aioob2) {
		// Ignore
	    }
	    LTRemix.getApplication().showMessage(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		    StringConstants.EDITOR_STRING_AIM_WITHIN_THE_ARENA));
	}
	// Swap event handlers
	this.secondaryPane.removeMouseListener(this.shandler);
	this.secondaryPane.addMouseListener(this.mhandler);
	// Set dirty flag
	app.getArenaManager().setDirty(true);
	this.redrawEditor(false);
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

    public void editRemoteController(final AbstractRemoteControlObject rc) {
	final int currentX = rc.getRemoteX();
	final int currentY = rc.getRemoteY();
	final String newXStr = CommonDialogs.showInputDialog(
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE, StringConstants.EDITOR_STRING_HORZ_JUMP),
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE, StringConstants.EDITOR_STRING_EDITOR),
		ArenaConstants.COORDS_LIST_X, ArenaConstants.COORDS_LIST_X[currentX]);
	if (newXStr != null) {
	    final String newYStr = CommonDialogs.showInputDialog(
		    StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			    StringConstants.EDITOR_STRING_VERT_JUMP),
		    StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE, StringConstants.EDITOR_STRING_EDITOR),
		    ArenaConstants.COORDS_LIST_Y, ArenaConstants.COORDS_LIST_Y[currentY]);
	    if (newYStr != null) {
		final int newX = newXStr.toCharArray()[0] - 65;
		final int newY = Integer.parseInt(newYStr) - 1;
		rc.setRemoteX(newX);
		rc.setRemoteY(newY);
	    }
	}
    }

    public void editArena() {
	final Application app = LTRemix.getApplication();
	if (app.getArenaManager().getLoaded()) {
	    app.getGUIManager().hideGUI();
	    app.setInEditor();
	    // Reset game state
	    app.getGameManager().resetGameState();
	    // Create the managers
	    if (this.arenaChanged) {
		this.elMgr = new EditorLocationManager();
		this.elMgr.setLimitsFromArena(app.getArenaManager().getArena());
		app.getArenaManager().getArena().fullScanActivateTanks();
		this.arenaChanged = false;
	    }
	    this.activeEra = EraConstants.ERA_PRESENT;
	    ImageManager.changeEra(EraConstants.ERA_PRESENT);
	    this.setUpGUI();
	    this.updatePicker();
	    this.clearHistory();
	    this.redrawEditor(false);
	    this.updatePickerLayout();
	    this.resetBorderPane();
	    this.checkMenus();
	} else {
	    CommonDialogs.showDialog(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		    StringConstants.MENU_STRING_ERROR_NO_ARENA_OPENED));
	}
    }

    public boolean newArena() {
	final Application app = LTRemix.getApplication();
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

    public void fixLimits() {
	// Fix limits
	final Application app = LTRemix.getApplication();
	if (app.getArenaManager().getArena() != null && this.elMgr != null) {
	    this.elMgr.setLimitsFromArena(app.getArenaManager().getArena());
	}
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

    public void fillLevel() {
	if (this.confirmNonUndoable()) {
	    LTRemix.getApplication().getArenaManager().getArena().fillDefault();
	    LTRemix.getApplication().showMessage(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		    StringConstants.EDITOR_STRING_LEVEL_FILLED));
	    LTRemix.getApplication().getArenaManager().setDirty(true);
	    this.redrawEditor(false);
	}
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
	final Application app = LTRemix.getApplication();
	boolean success = true;
	final int saveLevel = app.getArenaManager().getArena().getActiveLevelNumber();
	success = app.getArenaManager().getArena().addLevel();
	if (success) {
	    this.fixLimits();
	    app.getArenaManager().getArena().fillDefault();
	    // Save the entire level
	    app.getArenaManager().getArena().save();
	    app.getArenaManager().getArena().switchLevel(saveLevel);
	    app.updateLevelInfoList();
	    this.checkMenus();
	}
	return success;
    }

    public boolean removeLevel() {
	final Application app = LTRemix.getApplication();
	int level;
	boolean success = true;
	final String[] choices = app.getLevelInfoList();
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
			app.updateLevelInfoList();
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

    public boolean resizeLevel() {
	final Application app = LTRemix.getApplication();
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
		this.redrawEditor(false);
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

    public void goToLevelHandler() {
	int locW;
	final String msg = StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		StringConstants.EDITOR_STRING_GO_TO_LEVEL);
	String input;
	final String[] choices = LTRemix.getApplication().getLevelInfoList();
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

    public void showOutput() {
	final Application app = LTRemix.getApplication();
	this.outputFrame.setJMenuBar(app.getMenuManager().getMainMenuBar());
	app.getMenuManager().checkFlags();
	this.outputFrame.setVisible(true);
	this.outputFrame.pack();
    }

    public void attachMenus() {
	final Application app = LTRemix.getApplication();
	this.outputFrame.setJMenuBar(app.getMenuManager().getMainMenuBar());
	app.getMenuManager().checkFlags();
    }

    public void hideOutput() {
	if (this.outputFrame != null) {
	    this.outputFrame.setVisible(false);
	}
    }

    void disableOutput() {
	this.outputFrame.setEnabled(false);
    }

    void enableOutput() {
	this.outputFrame.setEnabled(true);
	this.checkMenus();
    }

    public JFrame getOutputFrame() {
	if (this.outputFrame != null && this.outputFrame.isVisible()) {
	    return this.outputFrame;
	} else {
	    return null;
	}
    }

    public void exitEditor() {
	final Application app = LTRemix.getApplication();
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

    private void setUpGUI() {
	// Destroy the old GUI, if one exists
	if (this.outputFrame != null) {
	    this.outputFrame.dispose();
	}
	final FocusHandler fHandler = new FocusHandler();
	this.messageLabel = new JLabel(StringConstants.COMMON_STRING_SPACE);
	this.outputFrame = new JFrame(
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE, StringConstants.EDITOR_STRING_EDITOR));
	final Image iconlogo = LogoManager.getIconLogo();
	this.outputFrame.setIconImage(iconlogo);
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

    public void undo() {
	final Application app = LTRemix.getApplication();
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
	    final AbstractArenaObject oldObj = app.getArenaManager().getArena().getCellEra(x, y, z, w, this.activeEra);
	    app.getArenaManager().getArena().setCellEra(obj, x, y, z, w, this.activeEra);
	    this.updateRedoHistory(oldObj, x, y, z, w, u);
	    this.checkMenus();
	    this.redrawEditor(false);
	} else {
	    LTRemix.getApplication().showMessage(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		    StringConstants.EDITOR_STRING_NOTHING_TO_UNDO));
	}
    }

    public void redo() {
	final Application app = LTRemix.getApplication();
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
	    final AbstractArenaObject oldObj = app.getArenaManager().getArena().getCellEra(x, y, z, w, this.activeEra);
	    app.getArenaManager().getArena().setCellEra(obj, x, y, z, w, this.activeEra);
	    this.updateUndoHistory(oldObj, x, y, z, w, u);
	    this.checkMenus();
	    this.redrawEditor(false);
	} else {
	    LTRemix.getApplication().showMessage(StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
		    StringConstants.EDITOR_STRING_NOTHING_TO_REDO));
	}
    }

    public void clearHistory() {
	this.engine = new EditorUndoRedoEngine();
	this.checkMenus();
    }

    private void updateUndoHistory(final AbstractArenaObject obj, final int x, final int y, final int z, final int w,
	    final int u) {
	this.engine.updateUndoHistory(obj, x, y, z, w, u);
    }

    private void updateRedoHistory(final AbstractArenaObject obj, final int x, final int y, final int z, final int w,
	    final int u) {
	this.engine.updateRedoHistory(obj, x, y, z, w, u);
    }

    private void updatePicker() {
	if (this.elMgr != null) {
	    final ArenaObjectList objectList = LTRemix.getApplication().getObjects();
	    this.objects = objectList.getAllObjectsOnLayer(this.elMgr.getEditorLocationW());
	    this.editorAppearances = objectList.getAllEditorAppearancesOnLayer(this.elMgr.getEditorLocationW());
	    this.objectsEnabled = objectList.getObjectEnabledStatuses(this.elMgr.getEditorLocationW());
	    final BufferedImageIcon[] newImages = this.editorAppearances;
	    final boolean[] enabled = this.objectsEnabled;
	    if (this.picker != null) {
		this.picker.updatePicker(newImages, enabled);
	    } else {
		this.picker = new SXSPicturePicker(newImages, enabled, new Color(223, 223, 223),
			ArenaEditor.STACK_COUNT);
		this.picker.changePickerColor(new Color(223, 223, 223));
	    }
	    this.updatePickerLayout();
	}
    }

    private void updatePickerLayout() {
	if (this.picker != null) {
	    this.picker.updatePickerLayout(this.outputPane.getLayout().preferredLayoutSize(this.outputPane).height);
	}
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

    private void enableUpOneFloor() {
	this.editorUpOneFloor.setEnabled(true);
    }

    private void disableUpOneFloor() {
	this.editorUpOneFloor.setEnabled(false);
    }

    private void enableDownOneFloor() {
	this.editorDownOneFloor.setEnabled(true);
    }

    private void disableDownOneFloor() {
	this.editorDownOneFloor.setEnabled(false);
    }

    private void enableUpOneLevel() {
	this.editorUpOneLevel.setEnabled(true);
    }

    private void disableUpOneLevel() {
	this.editorUpOneLevel.setEnabled(false);
    }

    private void enableDownOneLevel() {
	this.editorDownOneLevel.setEnabled(true);
    }

    private void disableDownOneLevel() {
	this.editorDownOneLevel.setEnabled(false);
    }

    private void enableAddLevel() {
	this.editorAddLevel.setEnabled(true);
    }

    private void disableAddLevel() {
	this.editorAddLevel.setEnabled(false);
    }

    private void enableRemoveLevel() {
	this.editorRemoveLevel.setEnabled(true);
    }

    private void disableRemoveLevel() {
	this.editorRemoveLevel.setEnabled(false);
    }

    public void enableUndo() {
	this.editorUndo.setEnabled(true);
    }

    public void disableUndo() {
	this.editorUndo.setEnabled(false);
    }

    public void enableRedo() {
	this.editorRedo.setEnabled(true);
    }

    public void disableRedo() {
	this.editorRedo.setEnabled(false);
    }

    private void enableClearHistory() {
	this.editorClearHistory.setEnabled(true);
    }

    private void disableClearHistory() {
	this.editorClearHistory.setEnabled(false);
    }

    private void enableCutLevel() {
	this.editorCutLevel.setEnabled(true);
    }

    private void disableCutLevel() {
	this.editorCutLevel.setEnabled(false);
    }

    private void enablePasteLevel() {
	this.editorPasteLevel.setEnabled(true);
    }

    private void disablePasteLevel() {
	this.editorPasteLevel.setEnabled(false);
    }

    private void enableInsertLevelFromClipboard() {
	this.editorInsertLevelFromClipboard.setEnabled(true);
    }

    private void disableInsertLevelFromClipboard() {
	this.editorInsertLevelFromClipboard.setEnabled(false);
    }

    private void enableSetStartPoint() {
	this.editorSetStartPoint.setEnabled(true);
    }

    private void disableSetStartPoint() {
	this.editorSetStartPoint.setEnabled(false);
    }

    public void handleCloseWindow() {
	try {
	    final Application app = LTRemix.getApplication();
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
	    LTRemix.getErrorLogger().logError(ex);
	}
    }

    private class EventHandler implements MouseListener, MouseMotionListener, WindowListener {
	// handle scroll bars
	public EventHandler() {
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
		LTRemix.getErrorLogger().logError(ex);
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
	    LTRemix.getApplication().getGUIManager().showGUI();
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

	@Override
	public void mouseDragged(final MouseEvent e) {
	    try {
		final ArenaEditor me = ArenaEditor.this;
		final int x = e.getX();
		final int y = e.getY();
		me.editObject(x, y);
	    } catch (final Exception ex) {
		LTRemix.getErrorLogger().logError(ex);
	    }
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
	    // Do nothing
	}
    }

    private class StartEventHandler implements MouseListener {
	// handle scroll bars
	public StartEventHandler() {
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

	@Override
	public void mouseClicked(final MouseEvent e) {
	    try {
		final int x = e.getX();
		final int y = e.getY();
		ArenaEditor.this.setPlayerLocation(x, y);
	    } catch (final Exception ex) {
		LTRemix.getErrorLogger().logError(ex);
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
		final Application app = LTRemix.getApplication();
		final String cmd = e.getActionCommand();
		if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_UNDO))) {
		    // Undo most recent action
		    if (app.getMode() == Application.STATUS_EDITOR) {
			app.getEditor().undo();
		    } else if (app.getMode() == Application.STATUS_GAME) {
			app.getGameManager().abortAndWaitForMLOLoop();
			app.getGameManager().undoLastMove();
		    }
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_REDO))) {
		    // Redo most recent undone action
		    if (app.getMode() == Application.STATUS_EDITOR) {
			app.getEditor().redo();
		    } else if (app.getMode() == Application.STATUS_GAME) {
			app.getGameManager().abortAndWaitForMLOLoop();
			app.getGameManager().redoLastMove();
		    }
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_CUT_LEVEL))) {
		    // Cut Level
		    final int level = app.getEditor().getLocationManager().getEditorLocationU();
		    app.getArenaManager().getArena().cutLevel();
		    app.getEditor().fixLimits();
		    app.getEditor().updateEditorLevelAbsolute(level);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_COPY_LEVEL))) {
		    // Copy Level
		    app.getArenaManager().getArena().copyLevel();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_PASTE_LEVEL))) {
		    // Paste Level
		    app.getArenaManager().getArena().pasteLevel();
		    app.getEditor().fixLimits();
		    app.getEditor().redrawEditor(false);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_INSERT_LEVEL_FROM_CLIPBOARD))) {
		    // Insert Level From Clipboard
		    app.getArenaManager().getArena().insertLevelFromClipboard();
		    app.getEditor().fixLimits();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_CLEAR_HISTORY))) {
		    // Clear undo/redo history, confirm first
		    final int res = CommonDialogs.showConfirmDialog(
			    StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
				    StringConstants.MENU_STRING_CONFIRM_CLEAR_HISTORY),
			    StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
				    StringConstants.EDITOR_STRING_EDITOR));
		    if (res == JOptionPane.YES_OPTION) {
			app.getEditor().clearHistory();
		    }
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_GO_TO_LEVEL))) {
		    // Go To Level
		    app.getEditor().goToLevelHandler();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_UP_ONE_FLOOR))) {
		    // Go up one floor
		    app.getEditor().updateEditorPosition(1, 0);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_DOWN_ONE_FLOOR))) {
		    // Go down one floor
		    app.getEditor().updateEditorPosition(-1, 0);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_UP_ONE_LEVEL))) {
		    // Go up one level
		    app.getEditor().updateEditorPosition(0, 1);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_DOWN_ONE_LEVEL))) {
		    // Go down one level
		    app.getEditor().updateEditorPosition(0, -1);
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_ADD_A_LEVEL))) {
		    // Add a level
		    app.getEditor().addLevel();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_REMOVE_A_LEVEL))) {
		    // Remove a level
		    app.getEditor().removeLevel();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_FILL_CURRENT_LEVEL))) {
		    // Fill level
		    app.getEditor().fillLevel();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_RESIZE_CURRENT_LEVEL))) {
		    // Resize level
		    app.getEditor().resizeLevel();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_LEVEL_PREFERENCES))) {
		    // Set Level Preferences
		    app.getEditor().setLevelPrefs();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_SET_START_POINT))) {
		    // Set Start Point
		    app.getEditor().editPlayerLocation();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_CHANGE_LAYER))) {
		    // Change Layer
		    app.getEditor().changeLayer();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_CHANGE_PLAYER))) {
		    // Change Player
		    app.getEditor().changePlayer();
		} else if (cmd.equals(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
			StringConstants.MENU_STRING_ITEM_CHANGE_ERA))) {
		    // Change Era
		    app.getEditor().changeEra();
		}
		app.getMenuManager().checkFlags();
	    } catch (final Exception ex) {
		LTRemix.getErrorLogger().logError(ex);
	    }
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
		LTRemix.getErrorLogger().logError(ex);
	    }
	}
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
	this.editorChangePlayer.setEnabled(true);
	this.editorChangeEra.setEnabled(true);
	this.editorEnableEraCloneMode.setEnabled(true);
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
	this.editorChangePlayer.setEnabled(false);
	this.editorChangeEra.setEnabled(false);
	this.editorEnableEraCloneMode.setEnabled(false);
    }

    @Override
    public void setInitialState() {
	this.disableModeCommands();
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
	this.editorChangePlayer = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_CHANGE_PLAYER));
	this.editorChangeEra = new JMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_CHANGE_ERA));
	this.editorEnableEraCloneMode = new JCheckBoxMenuItem(StringLoader.loadString(StringConstants.MENU_STRINGS_FILE,
		StringConstants.MENU_STRING_ITEM_ENABLE_ERA_CLONE_MODE));
	this.editorEnableEraCloneMode.setSelected(true);
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
	this.editorChangePlayer.addActionListener(menuHandler);
	this.editorChangeEra.addActionListener(menuHandler);
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
	editorMenu.add(this.editorChangePlayer);
	editorMenu.add(this.editorChangeEra);
	editorMenu.add(this.editorEnableEraCloneMode);
	return editorMenu;
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
	this.editorUpOneFloor.setAccelerator(accel.editorUpOneFloorAccel);
	this.editorDownOneFloor.setAccelerator(accel.editorDownOneFloorAccel);
    }

    @Override
    public void enableLoadedCommands() {
	// Do nothing
    }

    @Override
    public void disableLoadedCommands() {
	// Do nothing
    }

    @Override
    public void enableDirtyCommands() {
	// Do nothing
    }

    @Override
    public void disableDirtyCommands() {
	// Do nothing
    }
}
