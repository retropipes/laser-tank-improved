/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.arena.ArenaManager;
import com.puttysoftware.lasertank.editor.ArenaEditor;
import com.puttysoftware.lasertank.editor.EditorLocationManager;
import com.puttysoftware.lasertank.game.GameManager;
import com.puttysoftware.lasertank.game.lpb.LPBManager;
import com.puttysoftware.lasertank.prefs.PreferencesManager;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.strings.EditorString;
import com.puttysoftware.lasertank.strings.MenuString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.strings.global.UntranslatedString;
import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.BoardPrinter;

public class MenuManager {
    private class MenuHandler implements ActionListener {
	public MenuHandler() {
	    // Do nothing
	}

	// Handle menus
	@Override
	public void actionPerformed(final ActionEvent e) {
	    try {
		final Application app = LaserTank.getApplication();
		final GameManager game = app.getGameManager();
		final ArenaEditor editor = app.getEditor();
		final MenuManager menu = MenuManager.this;
		boolean loaded = false;
		final String cmd = e.getActionCommand();
		if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_NEW))) {
		    loaded = app.getEditor().newArena();
		    app.getArenaManager().setLoaded(loaded);
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_OPEN))) {
		    loaded = app.getArenaManager().loadArena();
		    app.getArenaManager().setLoaded(loaded);
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_OPEN_DEFAULT))) {
		    loaded = app.getArenaManager().loadArenaDefault();
		    app.getArenaManager().setLoaded(loaded);
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_CLOSE))) {
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
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_SAVE))) {
		    if (app.getArenaManager().getLoaded()) {
			app.getArenaManager().saveArena(app.getArenaManager().isArenaProtected());
		    } else {
			CommonDialogs.showDialog(StringLoader.loadMenu(MenuString.ERROR_NO_ARENA_OPENED));
		    }
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_SAVE_AS))) {
		    if (app.getArenaManager().getLoaded()) {
			app.getArenaManager().saveArenaAs(false);
		    } else {
			CommonDialogs.showDialog(StringLoader.loadMenu(MenuString.ERROR_NO_ARENA_OPENED));
		    }
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_SAVE_AS_PROTECTED))) {
		    if (app.getArenaManager().getLoaded()) {
			app.getArenaManager().saveArenaAs(true);
		    } else {
			CommonDialogs.showDialog(StringLoader.loadMenu(MenuString.ERROR_NO_ARENA_OPENED));
		    }
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_PREFERENCES))) {
		    // Show preferences dialog
		    PreferencesManager.showPrefs();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_PRINT_GAMEBOARD))) {
		    BoardPrinter.printBoard(app.getMasterContent());
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_EXIT))) {
		    // Exit program
		    if (app.getGUIManager().quitHandler()) {
			System.exit(0);
		    }
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_QUIT))) {
		    // Quit program
		    if (app.getGUIManager().quitHandler()) {
			System.exit(0);
		    }
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_PLAY))) {
		    // Play the current arena
		    final boolean proceed = app.getGameManager().newGame();
		    if (proceed) {
			app.exitCurrentMode();
			app.getGameManager().playArena();
		    }
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_EDIT))) {
		    // Edit the current arena
		    app.exitCurrentMode();
		    app.getEditor().editArena();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_USE_CLASSIC_ACCELERATORS))) {
		    // Toggle accelerators
		    app.getMenuManager().toggleAccelerators();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_RESET_CURRENT_LEVEL))) {
		    final int result = CommonDialogs.showConfirmDialog(
			    StringLoader.loadMenu(MenuString.CONFIRM_RESET_CURRENT_LEVEL),
			    GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME));
		    if (result == JOptionPane.YES_OPTION) {
			game.abortAndWaitForMLOLoop();
			game.resetCurrentLevel();
		    }
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_SHOW_SCORE_TABLE))) {
		    game.showScoreTable();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_REPLAY_SOLUTION))) {
		    game.abortAndWaitForMLOLoop();
		    game.replaySolution();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_RECORD_SOLUTION))) {
		    game.toggleRecording();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_LOAD_PLAYBACK_FILE))) {
		    game.abortAndWaitForMLOLoop();
		    LPBManager.loadLPB();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_PREVIOUS_LEVEL))) {
		    game.abortAndWaitForMLOLoop();
		    game.previousLevel();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_SKIP_LEVEL))) {
		    game.abortAndWaitForMLOLoop();
		    game.solvedLevel(false);
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_LOAD_LEVEL))) {
		    game.abortAndWaitForMLOLoop();
		    game.loadLevel();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_SHOW_HINT))) {
		    CommonDialogs.showDialog(app.getArenaManager().getArena().getHint().trim());
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_CHEATS))) {
		    game.enterCheatCode();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_CHANGE_OTHER_AMMO))) {
		    game.changeOtherAmmoMode();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_CHANGE_OTHER_TOOL))) {
		    game.changeOtherToolMode();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_CHANGE_OTHER_RANGE))) {
		    game.changeOtherRangeMode();
		} else if (cmd.equals(StringLoader.loadTime(ArenaConstants.ERA_DISTANT_PAST))) {
		    // Time Travel: Distant Past
		    SoundManager.playSound(SoundConstants.SOUND_ERA_CHANGE);
		    app.getArenaManager().getArena().switchEra(ArenaConstants.ERA_DISTANT_PAST);
		    menu.gameEraDistantPast.setSelected(true);
		    menu.gameEraPast.setSelected(false);
		    menu.gameEraPresent.setSelected(false);
		    menu.gameEraFuture.setSelected(false);
		    menu.gameEraDistantFuture.setSelected(false);
		} else if (cmd.equals(StringLoader.loadTime(ArenaConstants.ERA_PAST))) {
		    // Time Travel: Past
		    SoundManager.playSound(SoundConstants.SOUND_ERA_CHANGE);
		    app.getArenaManager().getArena().switchEra(ArenaConstants.ERA_PAST);
		    menu.gameEraDistantPast.setSelected(false);
		    menu.gameEraPast.setSelected(true);
		    menu.gameEraPresent.setSelected(false);
		    menu.gameEraFuture.setSelected(false);
		    menu.gameEraDistantFuture.setSelected(false);
		} else if (cmd.equals(StringLoader.loadTime(ArenaConstants.ERA_PRESENT))) {
		    // Time Travel: Present
		    SoundManager.playSound(SoundConstants.SOUND_ERA_CHANGE);
		    app.getArenaManager().getArena().switchEra(ArenaConstants.ERA_PRESENT);
		    menu.gameEraDistantPast.setSelected(false);
		    menu.gameEraPast.setSelected(false);
		    menu.gameEraPresent.setSelected(true);
		    menu.gameEraFuture.setSelected(false);
		    menu.gameEraDistantFuture.setSelected(false);
		} else if (cmd.equals(StringLoader.loadTime(ArenaConstants.ERA_FUTURE))) {
		    // Time Travel: Future
		    SoundManager.playSound(SoundConstants.SOUND_ERA_CHANGE);
		    app.getArenaManager().getArena().switchEra(ArenaConstants.ERA_FUTURE);
		    menu.gameEraDistantPast.setSelected(false);
		    menu.gameEraPast.setSelected(false);
		    menu.gameEraPresent.setSelected(false);
		    menu.gameEraFuture.setSelected(true);
		    menu.gameEraDistantFuture.setSelected(false);
		} else if (cmd.equals(StringLoader.loadTime(ArenaConstants.ERA_DISTANT_FUTURE))) {
		    // Time Travel: Distant Future
		    SoundManager.playSound(SoundConstants.SOUND_ERA_CHANGE);
		    app.getArenaManager().getArena().switchEra(ArenaConstants.ERA_DISTANT_FUTURE);
		    menu.gameEraDistantPast.setSelected(false);
		    menu.gameEraPast.setSelected(false);
		    menu.gameEraPresent.setSelected(false);
		    menu.gameEraFuture.setSelected(false);
		    menu.gameEraDistantFuture.setSelected(true);
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_UNDO))) {
		    // Undo most recent action
		    if (app.isInEditorMode()) {
			editor.undo();
		    } else if (app.isInGameMode()) {
			app.getGameManager().abortAndWaitForMLOLoop();
			app.getGameManager().undoLastMove();
		    }
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_REDO))) {
		    // Redo most recent undone action
		    if (app.isInEditorMode()) {
			editor.redo();
		    } else if (app.isInGameMode()) {
			app.getGameManager().abortAndWaitForMLOLoop();
			app.getGameManager().redoLastMove();
		    }
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_CUT_LEVEL))) {
		    // Cut Level
		    final int level = editor.getLocationManager().getEditorLocationU();
		    app.getArenaManager().getArena().cutLevel();
		    editor.fixLimits();
		    editor.updateEditorLevelAbsolute(level);
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_COPY_LEVEL))) {
		    // Copy Level
		    app.getArenaManager().getArena().copyLevel();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_PASTE_LEVEL))) {
		    // Paste Level
		    app.getArenaManager().getArena().pasteLevel();
		    editor.fixLimits();
		    editor.redrawEditor();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_INSERT_LEVEL_FROM_CLIPBOARD))) {
		    // Insert Level From Clipboard
		    app.getArenaManager().getArena().insertLevelFromClipboard();
		    editor.fixLimits();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_CLEAR_HISTORY))) {
		    // Clear undo/redo history, confirm first
		    final int res = CommonDialogs.showConfirmDialog(
			    StringLoader.loadMenu(MenuString.CONFIRM_CLEAR_HISTORY),
			    StringLoader.loadEditor(EditorString.EDITOR));
		    if (res == JOptionPane.YES_OPTION) {
			editor.clearHistory();
		    }
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_GO_TO_LEVEL))) {
		    // Go To Level
		    editor.goToLevelHandler();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_UP_ONE_FLOOR))) {
		    // Go up one floor
		    editor.updateEditorPosition(1, 0);
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_DOWN_ONE_FLOOR))) {
		    // Go down one floor
		    editor.updateEditorPosition(-1, 0);
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_UP_ONE_LEVEL))) {
		    // Go up one level
		    editor.updateEditorPosition(0, 1);
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_DOWN_ONE_LEVEL))) {
		    // Go down one level
		    editor.updateEditorPosition(0, -1);
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_ADD_A_LEVEL))) {
		    // Add a level
		    editor.addLevel();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_REMOVE_A_LEVEL))) {
		    // Remove a level
		    editor.removeLevel();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_FILL_CURRENT_LEVEL))) {
		    // Fill level
		    editor.fillLevel();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_RESIZE_CURRENT_LEVEL))) {
		    // Resize level
		    editor.resizeLevel();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_LEVEL_PREFERENCES))) {
		    // Set Level Preferences
		    editor.setLevelPrefs();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_SET_START_POINT))) {
		    // Set Start Point
		    editor.editPlayerLocation();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_CHANGE_LAYER))) {
		    // Change Layer
		    editor.changeLayer();
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_ENABLE_GLOBAL_MOVE_SHOOT))) {
		    // Enable Global Move-Shoot
		    LaserTank.getApplication().getArenaManager().getArena().setMoveShootAllowedGlobally(true);
		    menu.editorGlobalMoveShoot
			    .setText(StringLoader.loadMenu(MenuString.ITEM_DISABLE_GLOBAL_MOVE_SHOOT));
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_DISABLE_GLOBAL_MOVE_SHOOT))) {
		    // Disable Global Move-Shoot
		    menu.editorGlobalMoveShoot.setText(StringLoader.loadMenu(MenuString.ITEM_ENABLE_GLOBAL_MOVE_SHOOT));
		    app.getArenaManager().getArena().setMoveShootAllowedGlobally(false);
		} else if (cmd.equals(StringLoader.loadTime(ArenaConstants.ERA_DISTANT_PAST))) {
		    // Time Travel: Distant Past
		    app.getArenaManager().getArena().switchEra(ArenaConstants.ERA_DISTANT_PAST);
		    menu.editorEraDistantPast.setSelected(true);
		    menu.editorEraPast.setSelected(false);
		    menu.editorEraPresent.setSelected(false);
		    menu.editorEraFuture.setSelected(false);
		    menu.editorEraDistantFuture.setSelected(false);
		} else if (cmd.equals(StringLoader.loadTime(ArenaConstants.ERA_PAST))) {
		    // Time Travel: Past
		    app.getArenaManager().getArena().switchEra(ArenaConstants.ERA_PAST);
		    menu.editorEraDistantPast.setSelected(false);
		    menu.editorEraPast.setSelected(true);
		    menu.editorEraPresent.setSelected(false);
		    menu.editorEraFuture.setSelected(false);
		    menu.editorEraDistantFuture.setSelected(false);
		} else if (cmd.equals(StringLoader.loadTime(ArenaConstants.ERA_PRESENT))) {
		    // Time Travel: Present
		    app.getArenaManager().getArena().switchEra(ArenaConstants.ERA_PRESENT);
		    menu.editorEraDistantPast.setSelected(false);
		    menu.editorEraPast.setSelected(false);
		    menu.editorEraPresent.setSelected(true);
		    menu.editorEraFuture.setSelected(false);
		    menu.editorEraDistantFuture.setSelected(false);
		} else if (cmd.equals(StringLoader.loadTime(ArenaConstants.ERA_FUTURE))) {
		    // Time Travel: Future
		    app.getArenaManager().getArena().switchEra(ArenaConstants.ERA_FUTURE);
		    menu.editorEraDistantPast.setSelected(false);
		    menu.editorEraPast.setSelected(false);
		    menu.editorEraPresent.setSelected(false);
		    menu.editorEraFuture.setSelected(true);
		    menu.editorEraDistantFuture.setSelected(false);
		} else if (cmd.equals(StringLoader.loadTime(ArenaConstants.ERA_DISTANT_FUTURE))) {
		    // Time Travel: Distant Future
		    app.getArenaManager().getArena().switchEra(ArenaConstants.ERA_DISTANT_FUTURE);
		    menu.editorEraDistantPast.setSelected(false);
		    menu.editorEraPast.setSelected(false);
		    menu.editorEraPresent.setSelected(false);
		    menu.editorEraFuture.setSelected(false);
		    menu.editorEraDistantFuture.setSelected(true);
		} else if (cmd.equals(StringLoader.loadMenu(MenuString.ITEM_ABOUT_LASERTANK))) {
		    app.getAboutDialog().showAboutDialog();
		}
		app.getMenuManager().updateMenuItemState();
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}
    }

    // Fields
    private final JMenuBar menuBar;
    private JMenuItem fileNew, fileOpen, fileOpenDefault, fileClose, fileSave, fileSaveAs, fileSaveAsProtected,
	    filePrint, filePreferences, fileExit;
    private JMenuItem playPlay, playEdit;
    private JCheckBoxMenuItem playToggleAccelerators;
    private JMenu gameTimeTravelSubMenu;
    JCheckBoxMenuItem gameEraDistantPast, gameEraPast, gameEraPresent, gameEraFuture, gameEraDistantFuture;
    private JMenuItem gameReset, gameShowTable, gameReplaySolution, gameLoadLPB, gamePreviousLevel, gameSkipLevel,
	    gameLoadLevel, gameShowHint, gameCheats, gameChangeOtherAmmoMode, gameChangeOtherToolMode,
	    gameChangeOtherRangeMode;
    private JCheckBoxMenuItem gameRecordSolution;
    private JMenu editorTimeTravelSubMenu;
    JCheckBoxMenuItem editorEraDistantPast, editorEraPast, editorEraPresent, editorEraFuture, editorEraDistantFuture;
    private JMenuItem editorUndo, editorRedo, editorCutLevel, editorCopyLevel, editorPasteLevel,
	    editorInsertLevelFromClipboard, editorClearHistory, editorGoToLevel, editorUpOneFloor, editorDownOneFloor,
	    editorUpOneLevel, editorDownOneLevel, editorAddLevel, editorRemoveLevel, editorLevelPreferences,
	    editorSetStartPoint, editorFillLevel, editorResizeLevel, editorChangeLayer, editorGlobalMoveShoot;
    private JMenuItem helpAbout, helpHelp;
    private Accelerators accel;

    // Constructors
    public MenuManager() {
	this.menuBar = new JMenuBar();
	if (PreferencesManager.useClassicAccelerators()) {
	    this.accel = new ClassicAccelerators();
	} else {
	    this.accel = new ModernAccelerators();
	}
    }

    // Methods
    public final void updateMenuItemState() {
	try {
	    final Application app = LaserTank.getApplication();
	    final ArenaEditor editor = app.getEditor();
	    final EditorLocationManager elMgr = editor.getLocationManager();
	    if (app.getArenaManager().getLoaded()) {
		this.enableLoadedCommands();
	    } else {
		this.disableLoadedCommands();
	    }
	    if (app.getArenaManager().getDirty()) {
		this.enableDirtyCommands();
	    } else {
		this.disableDirtyCommands();
	    }
	    if (app.isInEditorMode()) {
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
		    if (elMgr.getEditorLocationZ() == elMgr.getMinEditorLocationZ()) {
			this.disableDownOneFloor();
		    } else {
			this.enableDownOneFloor();
		    }
		    if (elMgr.getEditorLocationZ() == elMgr.getMaxEditorLocationZ()) {
			this.disableUpOneFloor();
		    } else {
			this.enableUpOneFloor();
		    }
		} catch (final NullPointerException npe) {
		    this.disableDownOneFloor();
		    this.disableUpOneFloor();
		}
		try {
		    if (elMgr.getEditorLocationU() == elMgr.getMinEditorLocationU()) {
			this.disableDownOneLevel();
		    } else {
			this.enableDownOneLevel();
		    }
		    if (elMgr.getEditorLocationU() == elMgr.getMaxEditorLocationU()) {
			this.disableUpOneLevel();
		    } else {
			this.enableUpOneLevel();
		    }
		} catch (final NullPointerException npe) {
		    this.disableDownOneLevel();
		    this.disableUpOneLevel();
		}
		if (elMgr != null) {
		    this.enableSetStartPoint();
		} else {
		    this.disableSetStartPoint();
		}
		if (!editor.tryUndo()) {
		    this.disableUndo();
		} else {
		    this.enableUndo();
		}
		if (!editor.tryRedo()) {
		    this.disableRedo();
		} else {
		    this.enableRedo();
		}
		if (editor.tryBoth()) {
		    this.disableClearHistory();
		} else {
		    this.enableClearHistory();
		}
	    }
	    if (app.isInGameMode()) {
		final AbstractArena a = app.getArenaManager().getArena();
		if (a.tryUndo()) {
		    this.enableUndo();
		} else {
		    this.disableUndo();
		}
		if (a.tryRedo()) {
		    this.enableRedo();
		} else {
		    this.disableRedo();
		}
	    }
	    AbstractArena a = app.getArenaManager().getArena();
	    if (a != null && a.isPasteBlocked()) {
		this.disablePasteLevel();
		this.disableInsertLevelFromClipboard();
	    } else {
		this.enablePasteLevel();
		this.enableInsertLevelFromClipboard();
	    }
	    if (a != null && a.isCutBlocked()) {
		this.disableCutLevel();
	    } else {
		this.enableCutLevel();
	    }
	} catch (final Exception ex) {
	    LaserTank.logError(ex);
	}
    }

    public final JMenuBar getMenuBar() {
	return this.menuBar;
    }

    public final void disableRecording() {
	this.gameRecordSolution.setSelected(false);
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

    private void disableDownOneFloor() {
	this.editorDownOneFloor.setEnabled(false);
    }

    private void disableDownOneLevel() {
	this.editorDownOneLevel.setEnabled(false);
    }

    private void disableInsertLevelFromClipboard() {
	this.editorInsertLevelFromClipboard.setEnabled(false);
    }

    private void disablePasteLevel() {
	this.editorPasteLevel.setEnabled(false);
    }

    private void disableRedo() {
	this.editorRedo.setEnabled(false);
    }

    private void disableRemoveLevel() {
	this.editorRemoveLevel.setEnabled(false);
    }

    private void disableSetStartPoint() {
	this.editorSetStartPoint.setEnabled(false);
    }

    private void disableUndo() {
	this.editorUndo.setEnabled(false);
    }

    private void disableUpOneFloor() {
	this.editorUpOneFloor.setEnabled(false);
    }

    private void disableUpOneLevel() {
	this.editorUpOneLevel.setEnabled(false);
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

    private void enableDownOneFloor() {
	this.editorDownOneFloor.setEnabled(true);
    }

    private void enableDownOneLevel() {
	this.editorDownOneLevel.setEnabled(true);
    }

    private void enableInsertLevelFromClipboard() {
	this.editorInsertLevelFromClipboard.setEnabled(true);
    }

    private void enablePasteLevel() {
	this.editorPasteLevel.setEnabled(true);
    }

    private void enableRedo() {
	this.editorRedo.setEnabled(true);
    }

    private void enableRemoveLevel() {
	this.editorRemoveLevel.setEnabled(true);
    }

    private void enableSetStartPoint() {
	this.editorSetStartPoint.setEnabled(true);
    }

    private void enableUndo() {
	this.editorUndo.setEnabled(true);
    }

    private void enableUpOneFloor() {
	this.editorUpOneFloor.setEnabled(true);
    }

    private void enableUpOneLevel() {
	this.editorUpOneLevel.setEnabled(true);
    }

    void populateMenuBar() {
	final MenuHandler mhandler = new MenuHandler();
	JMenu fileMenu = this.buildFileMenu(mhandler);
	JMenu playMenu = this.buildPlayMenu(mhandler);
	JMenu gameMenu = this.buildGameMenu(mhandler);
	JMenu editorMenu = this.buildEditorMenu(mhandler);
	JMenu helpMenu = this.buildHelpMenu(mhandler);
	attachAccelerators();
	this.menuBar.add(fileMenu);
	this.menuBar.add(playMenu);
	this.menuBar.add(gameMenu);
	this.menuBar.add(editorMenu);
	this.menuBar.add(helpMenu);
    }

    private JMenu buildFileMenu(final MenuHandler mhandler) {
	final JMenu fileMenu = new JMenu(StringLoader.loadMenu(MenuString.MENU_FILE));
	this.fileNew = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_NEW));
	this.fileOpen = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_OPEN));
	this.fileOpenDefault = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_OPEN_DEFAULT));
	this.fileClose = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_CLOSE));
	this.fileSave = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_SAVE));
	this.fileSaveAs = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_SAVE_AS));
	this.fileSaveAsProtected = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_SAVE_AS_PROTECTED));
	this.filePreferences = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_PREFERENCES));
	this.filePrint = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_PRINT_GAMEBOARD));
	if (System.getProperty(GlobalLoader.loadUntranslated(UntranslatedString.OS_NAME))
		.contains(GlobalLoader.loadUntranslated(UntranslatedString.WINDOWS))) {
	    this.fileExit = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_EXIT));
	} else {
	    this.fileExit = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_QUIT));
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
	if (!System.getProperty(GlobalLoader.loadUntranslated(UntranslatedString.OS_NAME))
		.equalsIgnoreCase(GlobalLoader.loadUntranslated(UntranslatedString.MAC_OS_X))) {
	    fileMenu.add(this.filePreferences);
	}
	fileMenu.add(this.filePrint);
	if (!System.getProperty(GlobalLoader.loadUntranslated(UntranslatedString.OS_NAME))
		.equalsIgnoreCase(GlobalLoader.loadUntranslated(UntranslatedString.MAC_OS_X))) {
	    fileMenu.add(this.fileExit);
	}
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
	return fileMenu;
    }

    private JMenu buildPlayMenu(final MenuHandler mhandler) {
	final JMenu playMenu = new JMenu(StringLoader.loadMenu(MenuString.MENU_PLAY));
	this.playPlay = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_PLAY));
	this.playEdit = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_EDIT));
	this.playToggleAccelerators = new JCheckBoxMenuItem(
		StringLoader.loadMenu(MenuString.ITEM_USE_CLASSIC_ACCELERATORS));
	this.playPlay.addActionListener(mhandler);
	this.playEdit.addActionListener(mhandler);
	this.playToggleAccelerators.addActionListener(mhandler);
	playMenu.add(this.playPlay);
	playMenu.add(this.playEdit);
	playMenu.add(this.playToggleAccelerators);
	this.playPlay.setEnabled(false);
	this.playEdit.setEnabled(false);
	this.playToggleAccelerators.setEnabled(true);
	return playMenu;
    }

    private JMenu buildGameMenu(final MenuHandler mhandler) {
	final JMenu gameMenu = new JMenu(StringLoader.loadMenu(MenuString.MENU_GAME));
	this.gameReset = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_RESET_CURRENT_LEVEL));
	this.gameShowTable = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_SHOW_SCORE_TABLE));
	this.gameReplaySolution = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_REPLAY_SOLUTION));
	this.gameRecordSolution = new JCheckBoxMenuItem(StringLoader.loadMenu(MenuString.ITEM_RECORD_SOLUTION));
	this.gameLoadLPB = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_LOAD_PLAYBACK_FILE));
	this.gamePreviousLevel = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_PREVIOUS_LEVEL));
	this.gameSkipLevel = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_SKIP_LEVEL));
	this.gameLoadLevel = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_LOAD_LEVEL));
	this.gameShowHint = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_SHOW_HINT));
	this.gameCheats = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_CHEATS));
	this.gameChangeOtherAmmoMode = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_CHANGE_OTHER_AMMO));
	this.gameChangeOtherToolMode = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_CHANGE_OTHER_TOOL));
	this.gameChangeOtherRangeMode = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_CHANGE_OTHER_RANGE));
	this.gameEraDistantPast = new JCheckBoxMenuItem(StringLoader.loadTime(ArenaConstants.ERA_DISTANT_PAST), false);
	this.gameEraPast = new JCheckBoxMenuItem(StringLoader.loadTime(ArenaConstants.ERA_PAST), false);
	this.gameEraPresent = new JCheckBoxMenuItem(StringLoader.loadTime(ArenaConstants.ERA_PRESENT), true);
	this.gameEraFuture = new JCheckBoxMenuItem(StringLoader.loadTime(ArenaConstants.ERA_FUTURE), false);
	this.gameEraDistantFuture = new JCheckBoxMenuItem(StringLoader.loadTime(ArenaConstants.ERA_DISTANT_FUTURE),
		false);
	this.gameReset.addActionListener(mhandler);
	this.gameShowTable.addActionListener(mhandler);
	this.gameReplaySolution.addActionListener(mhandler);
	this.gameRecordSolution.addActionListener(mhandler);
	this.gameLoadLPB.addActionListener(mhandler);
	this.gamePreviousLevel.addActionListener(mhandler);
	this.gameSkipLevel.addActionListener(mhandler);
	this.gameLoadLevel.addActionListener(mhandler);
	this.gameShowHint.addActionListener(mhandler);
	this.gameCheats.addActionListener(mhandler);
	this.gameChangeOtherAmmoMode.addActionListener(mhandler);
	this.gameChangeOtherToolMode.addActionListener(mhandler);
	this.gameChangeOtherRangeMode.addActionListener(mhandler);
	this.gameEraDistantPast.addActionListener(mhandler);
	this.gameEraPast.addActionListener(mhandler);
	this.gameEraPresent.addActionListener(mhandler);
	this.gameEraFuture.addActionListener(mhandler);
	this.gameEraDistantFuture.addActionListener(mhandler);
	this.gameTimeTravelSubMenu = new JMenu(StringLoader.loadMenu(MenuString.SUB_TIME_TRAVEL));
	this.gameTimeTravelSubMenu.add(this.gameEraDistantPast);
	this.gameTimeTravelSubMenu.add(this.gameEraPast);
	this.gameTimeTravelSubMenu.add(this.gameEraPresent);
	this.gameTimeTravelSubMenu.add(this.gameEraFuture);
	this.gameTimeTravelSubMenu.add(this.gameEraDistantFuture);
	gameMenu.add(this.gameReset);
	gameMenu.add(this.gameShowTable);
	gameMenu.add(this.gameReplaySolution);
	gameMenu.add(this.gameRecordSolution);
	gameMenu.add(this.gameLoadLPB);
	gameMenu.add(this.gamePreviousLevel);
	gameMenu.add(this.gameSkipLevel);
	gameMenu.add(this.gameLoadLevel);
	gameMenu.add(this.gameShowHint);
	gameMenu.add(this.gameCheats);
	gameMenu.add(this.gameChangeOtherAmmoMode);
	gameMenu.add(this.gameChangeOtherToolMode);
	gameMenu.add(this.gameChangeOtherRangeMode);
	gameMenu.add(this.gameTimeTravelSubMenu);
	this.gameReset.setEnabled(false);
	this.gameShowTable.setEnabled(false);
	this.gameReplaySolution.setEnabled(false);
	this.gameRecordSolution.setEnabled(false);
	this.gameLoadLPB.setEnabled(false);
	this.gamePreviousLevel.setEnabled(false);
	this.gameSkipLevel.setEnabled(false);
	this.gameLoadLevel.setEnabled(false);
	this.gameShowHint.setEnabled(false);
	this.gameCheats.setEnabled(false);
	this.gameChangeOtherAmmoMode.setEnabled(false);
	this.gameChangeOtherToolMode.setEnabled(false);
	this.gameChangeOtherRangeMode.setEnabled(false);
	this.gameEraDistantPast.setEnabled(false);
	this.gameEraPast.setEnabled(false);
	this.gameEraPresent.setEnabled(false);
	this.gameEraFuture.setEnabled(false);
	this.gameEraDistantFuture.setEnabled(false);
	return gameMenu;
    }

    private JMenu buildEditorMenu(final MenuHandler mhandler) {
	final JMenu editorMenu = new JMenu(StringLoader.loadMenu(MenuString.MENU_EDITOR));
	this.editorUndo = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_UNDO));
	this.editorRedo = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_REDO));
	this.editorCutLevel = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_CUT_LEVEL));
	this.editorCopyLevel = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_COPY_LEVEL));
	this.editorPasteLevel = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_PASTE_LEVEL));
	this.editorInsertLevelFromClipboard = new JMenuItem(
		StringLoader.loadMenu(MenuString.ITEM_INSERT_LEVEL_FROM_CLIPBOARD));
	this.editorClearHistory = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_CLEAR_HISTORY));
	this.editorGoToLevel = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_GO_TO_LEVEL));
	this.editorUpOneFloor = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_UP_ONE_FLOOR));
	this.editorDownOneFloor = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_DOWN_ONE_FLOOR));
	this.editorUpOneLevel = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_UP_ONE_LEVEL));
	this.editorDownOneLevel = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_DOWN_ONE_LEVEL));
	this.editorAddLevel = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_ADD_A_LEVEL));
	this.editorRemoveLevel = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_REMOVE_A_LEVEL));
	this.editorFillLevel = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_FILL_CURRENT_LEVEL));
	this.editorResizeLevel = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_RESIZE_CURRENT_LEVEL));
	this.editorLevelPreferences = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_LEVEL_PREFERENCES));
	this.editorSetStartPoint = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_SET_START_POINT));
	this.editorChangeLayer = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_CHANGE_LAYER));
	this.editorGlobalMoveShoot = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_ENABLE_GLOBAL_MOVE_SHOOT));
	this.editorTimeTravelSubMenu = new JMenu(StringLoader.loadMenu(MenuString.SUB_TIME_TRAVEL));
	this.editorEraDistantPast = new JCheckBoxMenuItem(StringLoader.loadTime(ArenaConstants.ERA_DISTANT_PAST),
		false);
	this.editorEraPast = new JCheckBoxMenuItem(StringLoader.loadTime(ArenaConstants.ERA_PAST), false);
	this.editorEraPresent = new JCheckBoxMenuItem(StringLoader.loadTime(ArenaConstants.ERA_PRESENT), true);
	this.editorEraFuture = new JCheckBoxMenuItem(StringLoader.loadTime(ArenaConstants.ERA_FUTURE), false);
	this.editorEraDistantFuture = new JCheckBoxMenuItem(StringLoader.loadTime(ArenaConstants.ERA_DISTANT_FUTURE),
		false);
	this.editorUndo.addActionListener(mhandler);
	this.editorRedo.addActionListener(mhandler);
	this.editorCutLevel.addActionListener(mhandler);
	this.editorCopyLevel.addActionListener(mhandler);
	this.editorPasteLevel.addActionListener(mhandler);
	this.editorInsertLevelFromClipboard.addActionListener(mhandler);
	this.editorClearHistory.addActionListener(mhandler);
	this.editorGoToLevel.addActionListener(mhandler);
	this.editorUpOneFloor.addActionListener(mhandler);
	this.editorDownOneFloor.addActionListener(mhandler);
	this.editorUpOneLevel.addActionListener(mhandler);
	this.editorDownOneLevel.addActionListener(mhandler);
	this.editorAddLevel.addActionListener(mhandler);
	this.editorRemoveLevel.addActionListener(mhandler);
	this.editorFillLevel.addActionListener(mhandler);
	this.editorResizeLevel.addActionListener(mhandler);
	this.editorLevelPreferences.addActionListener(mhandler);
	this.editorSetStartPoint.addActionListener(mhandler);
	this.editorChangeLayer.addActionListener(mhandler);
	this.editorGlobalMoveShoot.addActionListener(mhandler);
	this.editorEraDistantPast.addActionListener(mhandler);
	this.editorEraPast.addActionListener(mhandler);
	this.editorEraPresent.addActionListener(mhandler);
	this.editorEraFuture.addActionListener(mhandler);
	this.editorEraDistantFuture.addActionListener(mhandler);
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
	return editorMenu;
    }

    private JMenu buildHelpMenu(final MenuHandler mhandler) {
	final JMenu helpMenu = new JMenu(StringLoader.loadMenu(MenuString.MENU_HELP));
	this.helpAbout = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_ABOUT_LASERTANK));
	this.helpHelp = new JMenuItem(StringLoader.loadMenu(MenuString.ITEM_LASERTANK_HELP));
	this.helpAbout.addActionListener(mhandler);
	this.helpHelp.addActionListener(mhandler);
	if (!System.getProperty(GlobalLoader.loadUntranslated(UntranslatedString.OS_NAME))
		.equalsIgnoreCase(GlobalLoader.loadUntranslated(UntranslatedString.MAC_OS_X))) {
	    helpMenu.add(this.helpAbout);
	}
	helpMenu.add(this.helpHelp);
	this.helpAbout.setEnabled(true);
	this.helpHelp.setEnabled(true);
	return helpMenu;
    }

    private void attachAccelerators() {
	this.fileNew.setAccelerator(this.accel.fileNewAccel);
	this.fileOpen.setAccelerator(this.accel.fileOpenAccel);
	this.fileClose.setAccelerator(this.accel.fileCloseAccel);
	this.fileSave.setAccelerator(this.accel.fileSaveAccel);
	this.fileSaveAs.setAccelerator(this.accel.fileSaveAsAccel);
	this.filePreferences.setAccelerator(this.accel.filePreferencesAccel);
	this.filePrint.setAccelerator(this.accel.filePrintAccel);
	if (System.getProperty(GlobalLoader.loadUntranslated(UntranslatedString.OS_NAME))
		.contains(GlobalLoader.loadUntranslated(UntranslatedString.WINDOWS))) {
	    this.fileExit.setAccelerator(null);
	} else {
	    this.fileExit.setAccelerator(this.accel.fileExitAccel);
	}
	this.playPlay.setAccelerator(this.accel.playPlayArenaAccel);
	this.playEdit.setAccelerator(this.accel.playEditArenaAccel);
	this.gameReset.setAccelerator(this.accel.gameResetAccel);
	this.gameShowTable.setAccelerator(this.accel.gameShowTableAccel);
	this.editorUndo.setAccelerator(this.accel.editorUndoAccel);
	this.editorRedo.setAccelerator(this.accel.editorRedoAccel);
	this.editorCutLevel.setAccelerator(this.accel.editorCutLevelAccel);
	this.editorCopyLevel.setAccelerator(this.accel.editorCopyLevelAccel);
	this.editorPasteLevel.setAccelerator(this.accel.editorPasteLevelAccel);
	this.editorInsertLevelFromClipboard.setAccelerator(this.accel.editorInsertLevelFromClipboardAccel);
	this.editorClearHistory.setAccelerator(this.accel.editorClearHistoryAccel);
	this.editorGoToLevel.setAccelerator(this.accel.editorGoToLocationAccel);
	this.editorUpOneLevel.setAccelerator(this.accel.editorUpOneLevelAccel);
	this.editorDownOneLevel.setAccelerator(this.accel.editorDownOneLevelAccel);
    }

    private void disableDirtyCommands() {
	this.fileSave.setEnabled(false);
    }

    private void enableDirtyCommands() {
	this.fileSave.setEnabled(true);
    }

    private void disableLoadedCommands() {
	this.fileClose.setEnabled(false);
	this.fileSaveAs.setEnabled(false);
	this.fileSaveAsProtected.setEnabled(false);
	this.playPlay.setEnabled(false);
	this.playEdit.setEnabled(false);
    }

    private void enableLoadedCommands() {
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
	if (app.getArenaManager().getArena().doesPlayerExist(0)) {
	    this.playPlay.setEnabled(true);
	} else {
	    this.playPlay.setEnabled(false);
	}
	this.playEdit.setEnabled(true);
    }

    void activateEditorCommands() {
	this.fileNew.setEnabled(false);
	this.fileOpen.setEnabled(false);
	this.fileOpenDefault.setEnabled(false);
	this.gameReset.setEnabled(false);
	this.gameShowTable.setEnabled(false);
	this.gameReplaySolution.setEnabled(false);
	this.gameRecordSolution.setEnabled(false);
	this.gameLoadLPB.setEnabled(false);
	this.gamePreviousLevel.setEnabled(false);
	this.gameSkipLevel.setEnabled(false);
	this.gameLoadLevel.setEnabled(false);
	this.gameShowHint.setEnabled(false);
	this.gameCheats.setEnabled(false);
	this.gameChangeOtherAmmoMode.setEnabled(false);
	this.gameChangeOtherToolMode.setEnabled(false);
	this.gameChangeOtherRangeMode.setEnabled(false);
	this.gameEraDistantPast.setEnabled(false);
	this.gameEraPast.setEnabled(false);
	this.gameEraPresent.setEnabled(false);
	this.gameEraFuture.setEnabled(false);
	this.gameEraDistantFuture.setEnabled(false);
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

    void activateGameCommands() {
	this.fileNew.setEnabled(false);
	this.fileOpen.setEnabled(false);
	this.fileOpenDefault.setEnabled(false);
	this.gameReset.setEnabled(true);
	this.gameShowTable.setEnabled(true);
	this.gameReplaySolution.setEnabled(true);
	this.gameRecordSolution.setEnabled(true);
	this.gameLoadLPB.setEnabled(true);
	this.gamePreviousLevel.setEnabled(true);
	this.gameSkipLevel.setEnabled(true);
	this.gameLoadLevel.setEnabled(true);
	this.gameShowHint.setEnabled(true);
	this.gameCheats.setEnabled(true);
	this.gameChangeOtherAmmoMode.setEnabled(true);
	this.gameChangeOtherToolMode.setEnabled(true);
	this.gameChangeOtherRangeMode.setEnabled(true);
	this.gameEraDistantPast.setEnabled(true);
	this.gameEraPast.setEnabled(true);
	this.gameEraPresent.setEnabled(true);
	this.gameEraFuture.setEnabled(true);
	this.gameEraDistantFuture.setEnabled(true);
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

    void activateGUICommands() {
	this.fileNew.setEnabled(true);
	this.fileOpen.setEnabled(true);
	this.fileOpenDefault.setEnabled(true);
	this.gameReset.setEnabled(false);
	this.gameShowTable.setEnabled(false);
	this.gameReplaySolution.setEnabled(false);
	this.gameRecordSolution.setEnabled(false);
	this.gameLoadLPB.setEnabled(false);
	this.gamePreviousLevel.setEnabled(false);
	this.gameSkipLevel.setEnabled(false);
	this.gameLoadLevel.setEnabled(false);
	this.gameShowHint.setEnabled(false);
	this.gameCheats.setEnabled(false);
	this.gameChangeOtherAmmoMode.setEnabled(false);
	this.gameChangeOtherToolMode.setEnabled(false);
	this.gameChangeOtherRangeMode.setEnabled(false);
	this.gameEraDistantPast.setEnabled(false);
	this.gameEraPast.setEnabled(false);
	this.gameEraPresent.setEnabled(false);
	this.gameEraFuture.setEnabled(false);
	this.gameEraDistantFuture.setEnabled(false);
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

    void toggleAccelerators() {
	if (this.accel instanceof ClassicAccelerators) {
	    this.accel = new ModernAccelerators();
	    PreferencesManager.setClassicAccelerators(false);
	} else {
	    this.accel = new ClassicAccelerators();
	    PreferencesManager.setClassicAccelerators(true);
	}
    }
}
