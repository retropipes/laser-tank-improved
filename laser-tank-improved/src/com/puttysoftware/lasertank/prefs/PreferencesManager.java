/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.prefs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.objects.Ground;
import com.puttysoftware.lasertank.strings.CommonString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.strings.global.UntranslatedString;
import com.puttysoftware.lasertank.utilities.EditorLayoutConstants;
import com.puttysoftware.lasertank.utilities.Extension;
import com.puttysoftware.lasertank.utilities.InvalidArenaException;

public class PreferencesManager {
    // Fields
    private final static PreferencesStoreManager storeMgr = new PreferencesStoreManager();
    private final static PreferencesGUIManager guiMgr = new PreferencesGUIManager();
    private final static int FALLBACK_LANGUAGE_ID = 0;
    private final static int DEFAULT_EDITOR_LAYOUT_ID = EditorLayoutConstants.EDITOR_LAYOUT_MODERN_V12;

    // Methods
    public static void activeLanguageChanged() {
	PreferencesManager.guiMgr.activeLanguageChanged();
    }

    public static boolean enableAnimation() {
	return PreferencesManager.storeMgr
		.getBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_ANIMATION), true);
    }

    static int getActionDelay() {
	return PreferencesManager.storeMgr
		.getInteger(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ACTION_DELAY), 2);
    }

    public static long getActionSpeed() {
	return (PreferencesManager.getActionDelay() + 1) * 5;
    }

    public static AbstractArenaObject getEditorDefaultFill() {
	return new Ground();
    }

    public static int getEditorLayoutID() {
	return PreferencesManager.storeMgr.getInteger(
		GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_EDITOR_LAYOUT_ID),
		PreferencesManager.DEFAULT_EDITOR_LAYOUT_ID);
    }

    public static boolean getEditorShowAllObjects() {
	return PreferencesManager.storeMgr
		.getBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_EDITOR_SHOW_ALL), true);
    }

    public static int getLanguageID() {
	return PreferencesManager.storeMgr.getInteger(
		GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_LANGUAGE_ID),
		PreferencesManager.FALLBACK_LANGUAGE_ID);
    }

    public static String getLastDirOpen() {
	return PreferencesManager.storeMgr.getString(
		GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_LAST_DIR_OPEN),
		StringLoader.loadCommon(CommonString.EMPTY));
    }

    public static String getLastDirSave() {
	return PreferencesManager.storeMgr.getString(
		GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_LAST_DIR_SAVE),
		StringLoader.loadCommon(CommonString.EMPTY));
    }

    public static boolean getMusicEnabled() {
	return PreferencesManager.storeMgr
		.getBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_MUSIC), true);
    }

    private static String getPrefsDirectory() {
	final String osName = System.getProperty(GlobalLoader.loadUntranslated(UntranslatedString.OS_NAME));
	if (osName.indexOf(GlobalLoader.loadUntranslated(UntranslatedString.MAC_OS_X)) != -1) {
	    // Mac OS X
	    return GlobalLoader.loadUntranslated(UntranslatedString.DIRECTORY_PREFS_MAC);
	} else if (osName.indexOf(GlobalLoader.loadUntranslated(UntranslatedString.WINDOWS)) != -1) {
	    // Windows
	    return GlobalLoader.loadUntranslated(UntranslatedString.DIRECTORY_PREFS_WINDOWS);
	} else {
	    // Other - assume UNIX-like
	    return GlobalLoader.loadUntranslated(UntranslatedString.DIRECTORY_PREFS_UNIX);
	}
    }

    private static String getPrefsDirPrefix() {
	final String osName = System.getProperty(GlobalLoader.loadUntranslated(UntranslatedString.OS_NAME));
	if (osName.indexOf(GlobalLoader.loadUntranslated(UntranslatedString.MAC_OS_X)) != -1) {
	    // Mac OS X
	    return System.getenv(GlobalLoader.loadUntranslated(UntranslatedString.DIRECTORY_UNIX_HOME));
	} else if (osName.indexOf(GlobalLoader.loadUntranslated(UntranslatedString.WINDOWS)) != -1) {
	    // Windows
	    return System.getenv(GlobalLoader.loadUntranslated(UntranslatedString.DIRECTORY_WINDOWS_APPDATA));
	} else {
	    // Other - assume UNIX-like
	    return System.getenv(GlobalLoader.loadUntranslated(UntranslatedString.DIRECTORY_UNIX_HOME));
	}
    }

    private static String getPrefsFile() {
	final StringBuilder b = new StringBuilder();
	b.append(PreferencesManager.getPrefsDirPrefix());
	b.append(PreferencesManager.getPrefsDirectory());
	b.append(PreferencesManager.getPrefsFileName());
	b.append(PreferencesManager.getPrefsFileExtension());
	return b.toString();
    }

    private static String getPrefsFileExtension() {
	return StringLoader.loadCommon(CommonString.NOTL_PERIOD) + Extension.getPreferencesExtension();
    }

    private static String getPrefsFileName() {
	final String osName = System.getProperty(GlobalLoader.loadUntranslated(UntranslatedString.OS_NAME));
	if (osName.indexOf(GlobalLoader.loadUntranslated(UntranslatedString.MAC_OS_X)) != -1) {
	    // Mac OS X
	    return GlobalLoader.loadUntranslated(UntranslatedString.FILE_PREFS_MAC);
	} else if (osName.indexOf(GlobalLoader.loadUntranslated(UntranslatedString.WINDOWS)) != -1) {
	    // Windows
	    return GlobalLoader.loadUntranslated(UntranslatedString.FILE_PREFS_WINDOWS);
	} else {
	    // Other - assume UNIX-like
	    return GlobalLoader.loadUntranslated(UntranslatedString.FILE_PREFS_UNIX);
	}
    }

    public static long getReplaySpeed() {
	return (PreferencesManager.getActionDelay() + 1) * 10;
    }

    public static boolean getSoundsEnabled() {
	return PreferencesManager.storeMgr
		.getBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_SOUNDS), true);
    }

    public static boolean isDeadlyDifficultyEnabled() {
	return PreferencesManager.storeMgr
		.getBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_DIFFICULTY_DEADLY), true);
    }

    public static boolean isEasyDifficultyEnabled() {
	return PreferencesManager.storeMgr
		.getBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_DIFFICULTY_EASY), true);
    }

    public static boolean isHardDifficultyEnabled() {
	return PreferencesManager.storeMgr
		.getBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_DIFFICULTY_HARD), true);
    }

    public static boolean isKidsDifficultyEnabled() {
	return PreferencesManager.storeMgr
		.getBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_DIFFICULTY_KIDS), true);
    }

    public static boolean isMediumDifficultyEnabled() {
	return PreferencesManager.storeMgr
		.getBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_DIFFICULTY_MEDIUM), true);
    }

    public static boolean oneMove() {
	return PreferencesManager.storeMgr
		.getBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ONE_MOVE), true);
    }

    public static void readPrefs() {
	try (BufferedInputStream buf = new BufferedInputStream(
		new FileInputStream(PreferencesManager.getPrefsFile()))) {
	    // Read new preferences
	    PreferencesManager.storeMgr.loadStore(buf);
	} catch (final IOException io) {
	    // Populate store with defaults
	    PreferencesManager.storeMgr.setString(
		    GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_LAST_DIR_OPEN),
		    StringLoader.loadCommon(CommonString.EMPTY));
	    PreferencesManager.storeMgr.setString(
		    GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_LAST_DIR_SAVE),
		    StringLoader.loadCommon(CommonString.EMPTY));
	    PreferencesManager.storeMgr
		    .setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_UPDATES_STARTUP), true);
	    PreferencesManager.storeMgr.setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ONE_MOVE),
		    true);
	    PreferencesManager.storeMgr
		    .setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_SOUNDS), true);
	    PreferencesManager.storeMgr
		    .setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_MUSIC), true);
	    PreferencesManager.storeMgr
		    .setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_ANIMATION), true);
	    PreferencesManager.storeMgr.setBoolean(
		    GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_DIFFICULTY_KIDS), true);
	    PreferencesManager.storeMgr.setBoolean(
		    GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_DIFFICULTY_EASY), true);
	    PreferencesManager.storeMgr.setBoolean(
		    GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_DIFFICULTY_MEDIUM), true);
	    PreferencesManager.storeMgr.setBoolean(
		    GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_DIFFICULTY_HARD), true);
	    PreferencesManager.storeMgr.setBoolean(
		    GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_DIFFICULTY_DEADLY), true);
	    PreferencesManager.storeMgr
		    .setInteger(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ACTION_DELAY), 2);
	    PreferencesManager.storeMgr
		    .setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_CLASSIC_ACCEL), false);
	    PreferencesManager.storeMgr.setInteger(
		    GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_LANGUAGE_ID),
		    PreferencesManager.FALLBACK_LANGUAGE_ID);
	    PreferencesManager.storeMgr.setInteger(
		    GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_EDITOR_LAYOUT_ID),
		    PreferencesManager.DEFAULT_EDITOR_LAYOUT_ID);
	    PreferencesManager.storeMgr
		    .setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_EDITOR_SHOW_ALL), true);
	}
    }

    static void setActionDelay(final int value) {
	PreferencesManager.storeMgr.setInteger(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ACTION_DELAY),
		value);
    }

    static void setCheckUpdatesAtStartup(final boolean value) {
	PreferencesManager.storeMgr
		.setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_UPDATES_STARTUP), value);
    }

    public static void setClassicAccelerators(final boolean value) {
	PreferencesManager.storeMgr
		.setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_CLASSIC_ACCEL), value);
    }

    public static void setDeadlyDifficultyEnabled(final boolean value) {
	PreferencesManager.storeMgr.setBoolean(
		GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_DIFFICULTY_DEADLY), value);
    }

    public static void setEasyDifficultyEnabled(final boolean value) {
	PreferencesManager.storeMgr
		.setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_DIFFICULTY_EASY), value);
    }

    public static void setEditorLayoutID(final int value) {
	PreferencesManager.storeMgr
		.setInteger(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_EDITOR_LAYOUT_ID), value);
	LaserTank.getApplication().getEditor().resetBorderPane();
    }

    public static void setEditorShowAllObjects(final boolean value) {
	PreferencesManager.storeMgr
		.setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_EDITOR_SHOW_ALL), value);
	LaserTank.getApplication().getEditor().resetBorderPane();
    }

    static void setEnableAnimation(final boolean value) {
	PreferencesManager.storeMgr
		.setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_ANIMATION), value);
    }

    public static void setHardDifficultyEnabled(final boolean value) {
	PreferencesManager.storeMgr
		.setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_DIFFICULTY_HARD), value);
    }

    public static void setKidsDifficultyEnabled(final boolean value) {
	PreferencesManager.storeMgr
		.setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_DIFFICULTY_KIDS), value);
    }

    public static void setLanguageID(final int value) {
	final int oldValue = PreferencesManager.getLanguageID();
	PreferencesManager.storeMgr.setInteger(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_LANGUAGE_ID),
		value);
	if (oldValue != value) {
	    StringLoader.activeLanguageChanged(value);
	}
    }

    public static void setLastDirOpen(final String value) {
	PreferencesManager.storeMgr.setString(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_LAST_DIR_OPEN),
		value);
    }

    public static void setLastDirSave(final String value) {
	PreferencesManager.storeMgr.setString(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_LAST_DIR_SAVE),
		value);
    }

    public static void setMediumDifficultyEnabled(final boolean value) {
	PreferencesManager.storeMgr.setBoolean(
		GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_DIFFICULTY_MEDIUM), value);
    }

    static void setMusicEnabled(final boolean status) {
	PreferencesManager.storeMgr.setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_MUSIC),
		status);
    }

    static void setOneMove(final boolean value) {
	PreferencesManager.storeMgr.setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ONE_MOVE),
		value);
    }

    static void setSoundsEnabled(final boolean status) {
	PreferencesManager.storeMgr
		.setBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_ENABLE_SOUNDS), status);
    }

    public static boolean shouldCheckUpdatesAtStartup() {
	return PreferencesManager.storeMgr
		.getBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_UPDATES_STARTUP), true);
    }

    public static void showPrefs() {
	PreferencesManager.guiMgr.showPrefs();
    }

    public static boolean useClassicAccelerators() {
	return PreferencesManager.storeMgr
		.getBoolean(GlobalLoader.loadUntranslated(UntranslatedString.PREFS_KEY_CLASSIC_ACCEL), false);
    }

    public static void writePrefs() {
	try (BufferedOutputStream buf = new BufferedOutputStream(
		new FileOutputStream(PreferencesManager.getPrefsFile()))) {
	    PreferencesManager.storeMgr.saveStore(buf);
	} catch (final IOException ioe) {
	    throw new InvalidArenaException(ioe);
	}
    }

    // Private constructor
    private PreferencesManager() {
	// Do nothing
    }
}
