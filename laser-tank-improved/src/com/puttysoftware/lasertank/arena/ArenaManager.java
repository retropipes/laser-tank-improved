/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena;

import java.awt.FileDialog;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.current.CurrentArena;
import com.puttysoftware.lasertank.arena.v4.V4LevelLoadTask;
import com.puttysoftware.lasertank.improved.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.improved.fileio.FilenameChecker;
import com.puttysoftware.lasertank.prefs.PreferencesManager;
import com.puttysoftware.lasertank.resourcemanagers.MusicManager;
import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;
import com.puttysoftware.lasertank.utilities.CleanupTask;
import com.puttysoftware.lasertank.utilities.Extension;

public class ArenaManager {
    // Fields
    private AbstractArena gameArena;
    private boolean loaded, isDirty;
    private String scoresFileName;
    private String lastUsedArenaFile;
    private String lastUsedGameFile;
    private boolean arenaProtected;

    // Constructors
    public ArenaManager() {
	this.loaded = false;
	this.isDirty = false;
	this.lastUsedArenaFile = StringConstants.COMMON_STRING_EMPTY;
	this.lastUsedGameFile = StringConstants.COMMON_STRING_EMPTY;
	this.scoresFileName = StringConstants.COMMON_STRING_EMPTY;
    }

    // Methods
    public static AbstractArena createArena() throws IOException {
	return new CurrentArena();
    }

    public AbstractArena getArena() {
	return this.gameArena;
    }

    public void setArena(final AbstractArena newArena) {
	this.gameArena = newArena;
    }

    public boolean isArenaProtected() {
	return this.arenaProtected;
    }

    public void setArenaProtected(final boolean value) {
	this.arenaProtected = value;
    }

    public void handleDeferredSuccess(final boolean value) {
	if (value) {
	    this.setLoaded(true);
	}
	this.setDirty(false);
	LaserTank.getApplication().getEditor().arenaChanged();
	MusicManager.arenaChanged();
	LaserTank.getApplication().getMenuManager().checkFlags();
    }

    public static int showSaveDialog() {
	String type, source;
	final Application app = LaserTank.getApplication();
	final int mode = app.getMode();
	if (mode == Application.STATUS_EDITOR) {
	    type = StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
		    StringConstants.DIALOG_STRING_PROMPT_SAVE_ARENA);
	    source = StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE, StringConstants.EDITOR_STRING_EDITOR);
	} else if (mode == Application.STATUS_GAME) {
	    type = StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
		    StringConstants.DIALOG_STRING_PROMPT_SAVE_GAME);
	    source = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_PROGRAM_NAME);
	} else {
	    // Not in the game or editor, so abort
	    return JOptionPane.NO_OPTION;
	}
	return CommonDialogs.showYNCConfirmDialog(type, source);
    }

    public boolean getLoaded() {
	return this.loaded;
    }

    public void setLoaded(final boolean status) {
	final Application app = LaserTank.getApplication();
	this.loaded = status;
	app.getMenuManager().checkFlags();
    }

    public boolean getDirty() {
	return this.isDirty;
    }

    public void setDirty(final boolean newDirty) {
	final Application app = LaserTank.getApplication();
	this.isDirty = newDirty;
	final JFrame frame = app.getOutputFrame();
	if (frame != null) {
	    frame.getRootPane().putClientProperty(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_WINDOW_MODIFIED), Boolean.valueOf(newDirty));
	}
	app.getMenuManager().checkFlags();
    }

    public void clearLastUsedFilenames() {
	this.lastUsedArenaFile = StringConstants.COMMON_STRING_EMPTY;
	this.lastUsedGameFile = StringConstants.COMMON_STRING_EMPTY;
    }

    public String getLastUsedArena() {
	return this.lastUsedArenaFile;
    }

    public String getLastUsedGame() {
	return this.lastUsedGameFile;
    }

    public void setLastUsedArena(final String newFile) {
	this.lastUsedArenaFile = newFile;
    }

    public void setLastUsedGame(final String newFile) {
	this.lastUsedGameFile = newFile;
    }

    public String getScoresFileName() {
	return this.scoresFileName;
    }

    public void setScoresFileName(final String filename) {
	this.scoresFileName = filename;
    }

    public boolean loadArena() {
	return this.loadArenaImpl(PreferencesManager.getLastDirOpen());
    }

    public boolean loadArenaDefault() {
	try {
	    return this.loadArenaImpl(
		    ArenaManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()
			    + File.separator + "Common" + File.separator + "Levels");
	} catch (final URISyntaxException e) {
	    return this.loadArena();
	}
    }

    private boolean loadArenaImpl(final String initialDirectory) {
	final Application app = LaserTank.getApplication();
	int status = 0;
	boolean saved = true;
	String filename, extension, file, dir;
	final FileDialog fd = new FileDialog(app.getOutputFrame(),
		StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE, StringConstants.DIALOG_STRING_LOAD),
		FileDialog.LOAD);
	fd.setDirectory(initialDirectory);
	if (this.getDirty()) {
	    status = ArenaManager.showSaveDialog();
	    if (status == JOptionPane.YES_OPTION) {
		saved = this.saveArena(this.isArenaProtected());
	    } else if (status == JOptionPane.CANCEL_OPTION) {
		saved = false;
	    } else {
		this.setDirty(false);
	    }
	}
	if (saved) {
	    fd.setVisible(true);
	    file = fd.getFile();
	    dir = fd.getDirectory();
	    if (file != null && dir != null) {
		PreferencesManager.setLastDirOpen(dir);
		filename = dir + file;
		extension = ArenaManager.getExtension(filename);
		if (extension.equals(Extension.getArenaExtension())) {
		    this.lastUsedArenaFile = filename;
		    this.scoresFileName = ArenaManager.getNameWithoutExtension(file);
		    ArenaManager.loadFile(filename, false, false);
		} else if (extension.equals(Extension.getProtectedArenaExtension())) {
		    this.lastUsedArenaFile = filename;
		    this.scoresFileName = ArenaManager.getNameWithoutExtension(file);
		    ArenaManager.loadFile(filename, false, true);
		} else if (extension.equals(Extension.getGameExtension())) {
		    this.lastUsedGameFile = filename;
		    ArenaManager.loadFile(filename, true, false);
		} else if (extension.equals(Extension.getOldLevelExtension())) {
		    this.lastUsedArenaFile = filename;
		    this.scoresFileName = ArenaManager.getNameWithoutExtension(file);
		    final V4LevelLoadTask ollt = new V4LevelLoadTask(filename);
		    ollt.start();
		} else {
		    CommonDialogs.showDialog(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
			    StringConstants.DIALOG_STRING_NON_ARENA_FILE));
		}
	    } else {
		// User cancelled
		if (this.loaded) {
		    return true;
		}
	    }
	}
	return false;
    }

    private static void loadFile(final String filename, final boolean isSavedGame, final boolean protect) {
	if (!FilenameChecker
		.isFilenameOK(ArenaManager.getNameWithoutExtension(ArenaManager.getFileNameOnly(filename)))) {
	    CommonDialogs.showErrorDialog(
		    StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
			    StringConstants.DIALOG_STRING_ILLEGAL_CHARACTERS),
		    StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE, StringConstants.DIALOG_STRING_LOAD));
	} else {
	    // Run cleanup task
	    CleanupTask.cleanUp();
	    // Load file
	    final LoadTask xlt = new LoadTask(filename, isSavedGame, protect);
	    xlt.start();
	}
    }

    public boolean saveArena(final boolean protect) {
	final Application app = LaserTank.getApplication();
	if (app.getMode() == Application.STATUS_GAME) {
	    if (this.lastUsedGameFile != null && !this.lastUsedGameFile.equals(StringConstants.COMMON_STRING_EMPTY)) {
		final String extension = ArenaManager.getExtension(this.lastUsedGameFile);
		if (extension != null) {
		    if (!extension.equals(Extension.getGameExtension())) {
			this.lastUsedGameFile = ArenaManager.getNameWithoutExtension(this.lastUsedGameFile)
				+ Extension.getGameExtensionWithPeriod();
		    }
		} else {
		    this.lastUsedGameFile += Extension.getGameExtensionWithPeriod();
		}
		ArenaManager.saveFile(this.lastUsedGameFile, true, false);
	    } else {
		return this.saveArenaAs(protect);
	    }
	} else {
	    if (protect) {
		if (this.lastUsedArenaFile != null
			&& !this.lastUsedArenaFile.equals(StringConstants.COMMON_STRING_EMPTY)) {
		    final String extension = ArenaManager.getExtension(this.lastUsedArenaFile);
		    if (extension != null) {
			if (!extension.equals(Extension.getProtectedArenaExtension())) {
			    this.lastUsedArenaFile = ArenaManager.getNameWithoutExtension(this.lastUsedArenaFile)
				    + Extension.getProtectedArenaExtensionWithPeriod();
			}
		    } else {
			this.lastUsedArenaFile += Extension.getProtectedArenaExtensionWithPeriod();
		    }
		    ArenaManager.saveFile(this.lastUsedArenaFile, false, protect);
		} else {
		    return this.saveArenaAs(protect);
		}
	    } else {
		if (this.lastUsedArenaFile != null
			&& !this.lastUsedArenaFile.equals(StringConstants.COMMON_STRING_EMPTY)) {
		    final String extension = ArenaManager.getExtension(this.lastUsedArenaFile);
		    if (extension != null) {
			if (!extension.equals(Extension.getArenaExtension())) {
			    this.lastUsedArenaFile = ArenaManager.getNameWithoutExtension(this.lastUsedArenaFile)
				    + Extension.getArenaExtensionWithPeriod();
			}
		    } else {
			this.lastUsedArenaFile += Extension.getArenaExtensionWithPeriod();
		    }
		    ArenaManager.saveFile(this.lastUsedArenaFile, false, protect);
		} else {
		    return this.saveArenaAs(protect);
		}
	    }
	}
	return false;
    }

    public boolean saveArenaAs(final boolean protect) {
	final Application app = LaserTank.getApplication();
	String filename = StringConstants.COMMON_STRING_EMPTY;
	String fileOnly = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_DOUBLE_BACKSLASH);
	String extension, file, dir;
	final String lastSave = PreferencesManager.getLastDirSave();
	final FileDialog fd = new FileDialog(app.getOutputFrame(),
		StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE, StringConstants.DIALOG_STRING_SAVE),
		FileDialog.SAVE);
	fd.setDirectory(lastSave);
	while (!FilenameChecker.isFilenameOK(fileOnly)) {
	    fd.setVisible(true);
	    file = fd.getFile();
	    dir = fd.getDirectory();
	    if (file != null && dir != null) {
		extension = ArenaManager.getExtension(file);
		filename = dir + file;
		fileOnly = filename.substring(dir.length() + 1);
		if (!FilenameChecker.isFilenameOK(fileOnly)) {
		    CommonDialogs.showErrorDialog(
			    StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
				    StringConstants.DIALOG_STRING_ILLEGAL_CHARACTERS),
			    StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
				    StringConstants.DIALOG_STRING_SAVE));
		} else {
		    PreferencesManager.setLastDirSave(dir);
		    if (app.getMode() == Application.STATUS_GAME) {
			if (extension != null) {
			    if (!extension.equals(Extension.getGameExtension())) {
				filename = ArenaManager.getNameWithoutExtension(file)
					+ Extension.getGameExtensionWithPeriod();
			    }
			} else {
			    filename += Extension.getGameExtensionWithPeriod();
			}
			this.lastUsedGameFile = filename;
			ArenaManager.saveFile(filename, true, false);
		    } else {
			if (protect) {
			    if (extension != null) {
				if (!extension.equals(Extension.getProtectedArenaExtension())) {
				    filename = ArenaManager.getNameWithoutExtension(file)
					    + Extension.getProtectedArenaExtensionWithPeriod();
				}
			    } else {
				filename += Extension.getProtectedArenaExtensionWithPeriod();
			    }
			    this.lastUsedArenaFile = filename;
			    this.scoresFileName = ArenaManager.getNameWithoutExtension(file);
			    ArenaManager.saveFile(filename, false, protect);
			} else {
			    if (extension != null) {
				if (!extension.equals(Extension.getArenaExtension())) {
				    filename = ArenaManager.getNameWithoutExtension(file)
					    + Extension.getArenaExtensionWithPeriod();
				}
			    } else {
				filename += Extension.getArenaExtensionWithPeriod();
			    }
			    this.lastUsedArenaFile = filename;
			    this.scoresFileName = ArenaManager.getNameWithoutExtension(file);
			    ArenaManager.saveFile(filename, false, protect);
			}
		    }
		}
	    } else {
		break;
	    }
	}
	return false;
    }

    private static void saveFile(final String filename, final boolean isSavedGame, final boolean protect) {
	if (isSavedGame) {
	    LaserTank.getApplication().showMessage(StringLoader.loadString(StringConstants.MESSAGE_STRINGS_FILE,
		    StringConstants.MESSAGE_STRING_SAVING_GAME));
	} else {
	    LaserTank.getApplication().showMessage(StringLoader.loadString(StringConstants.MESSAGE_STRINGS_FILE,
		    StringConstants.MESSAGE_STRING_SAVING_ARENA));
	}
	final SaveTask xst = new SaveTask(filename, isSavedGame, protect);
	xst.start();
    }

    private static String getExtension(final String s) {
	String ext = null;
	final int i = s.lastIndexOf('.');
	if (i > 0 && i < s.length() - 1) {
	    ext = s.substring(i + 1).toLowerCase();
	}
	return ext;
    }

    private static String getNameWithoutExtension(final String s) {
	String ext = null;
	final int i = s.lastIndexOf('.');
	if (i > 0 && i < s.length() - 1) {
	    ext = s.substring(0, i);
	} else {
	    ext = s;
	}
	return ext;
    }

    private static String getFileNameOnly(final String s) {
	String fno = null;
	final int i = s.lastIndexOf(File.separatorChar);
	if (i > 0 && i < s.length() - 1) {
	    fno = s.substring(i + 1);
	} else {
	    fno = s;
	}
	return fno;
    }
}
