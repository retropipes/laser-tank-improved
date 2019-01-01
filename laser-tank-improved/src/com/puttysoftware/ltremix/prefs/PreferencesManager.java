/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.prefs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFrame;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.objects.Ground;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;
import com.puttysoftware.ltremix.utilities.Extension;

public class PreferencesManager {
    // Fields
    private final static PreferencesStoreManager storeMgr = new PreferencesStoreManager();
    private final static PreferencesGUIManager guiMgr = new PreferencesGUIManager();
    private final static int FALLBACK_LANGUAGE_ID = 0;

    // Private constructor
    private PreferencesManager() {
	// Do nothing
    }

    // Methods
    public static void activeLanguageChanged() {
	PreferencesManager.guiMgr.activeLanguageChanged();
    }

    public static int getLanguageID() {
	return PreferencesManager.storeMgr.getInteger(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_LANGUAGE_ID), PreferencesManager.FALLBACK_LANGUAGE_ID);
    }

    public static void setLanguageID(final int value) {
	final int oldValue = PreferencesManager.getLanguageID();
	PreferencesManager.storeMgr.setInteger(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_LANGUAGE_ID), value);
	if (oldValue != value) {
	    StringLoader.activeLanguageChanged(value);
	}
    }

    public static String getLastDirOpen() {
	return PreferencesManager.storeMgr.getString(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_LAST_DIR_OPEN), StringConstants.COMMON_STRING_EMPTY);
    }

    public static void setLastDirOpen(final String value) {
	PreferencesManager.storeMgr.setString(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_LAST_DIR_OPEN), value);
    }

    public static String getLastDirSave() {
	return PreferencesManager.storeMgr.getString(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_LAST_DIR_SAVE), StringConstants.COMMON_STRING_EMPTY);
    }

    public static void setLastDirSave(final String value) {
	PreferencesManager.storeMgr.setString(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_LAST_DIR_SAVE), value);
    }

    public static boolean shouldCheckUpdatesAtStartup() {
	return PreferencesManager.storeMgr.getBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_UPDATES_STARTUP), true);
    }

    static void setCheckUpdatesAtStartup(final boolean value) {
	PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_UPDATES_STARTUP), value);
    }

    static int getActionDelay() {
	return PreferencesManager.storeMgr.getInteger(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ACTION_DELAY), 2);
    }

    static void setActionDelay(final int value) {
	PreferencesManager.storeMgr.setInteger(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ACTION_DELAY), value);
    }

    public static long getActionSpeed() {
	return (PreferencesManager.getActionDelay() + 1) * 10;
    }

    public static long getReplaySpeed() {
	return (PreferencesManager.getActionDelay() + 1) * 20;
    }

    public static boolean oneMove() {
	return PreferencesManager.storeMgr.getBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ONE_MOVE), true);
    }

    static void setOneMove(final boolean value) {
	PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ONE_MOVE), value);
    }

    public static boolean enableAnimation() {
	return PreferencesManager.storeMgr.getBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_ANIMATION), true);
    }

    static void setEnableAnimation(final boolean value) {
	PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_ANIMATION), value);
    }

    public static boolean isKidsDifficultyEnabled() {
	return PreferencesManager.storeMgr.getBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_DIFFICULTY_KIDS), true);
    }

    public static void setKidsDifficultyEnabled(final boolean value) {
	PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_DIFFICULTY_KIDS), value);
    }

    public static boolean isEasyDifficultyEnabled() {
	return PreferencesManager.storeMgr.getBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_DIFFICULTY_EASY), true);
    }

    public static void setEasyDifficultyEnabled(final boolean value) {
	PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_DIFFICULTY_EASY), value);
    }

    public static boolean isMediumDifficultyEnabled() {
	return PreferencesManager.storeMgr.getBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_DIFFICULTY_MEDIUM), true);
    }

    public static void setMediumDifficultyEnabled(final boolean value) {
	PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_DIFFICULTY_MEDIUM), value);
    }

    public static boolean isHardDifficultyEnabled() {
	return PreferencesManager.storeMgr.getBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_DIFFICULTY_HARD), true);
    }

    public static void setHardDifficultyEnabled(final boolean value) {
	PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_DIFFICULTY_HARD), value);
    }

    public static boolean isDeadlyDifficultyEnabled() {
	return PreferencesManager.storeMgr.getBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_DIFFICULTY_DEADLY), true);
    }

    public static void setDeadlyDifficultyEnabled(final boolean value) {
	PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_DIFFICULTY_DEADLY), value);
    }

    public static AbstractArenaObject getEditorDefaultFill() {
	return new Ground();
    }

    public static boolean getSoundsEnabled() {
	return PreferencesManager.storeMgr.getBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_SOUNDS), true);
    }

    static void setSoundsEnabled(final boolean status) {
	PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_SOUNDS), status);
    }

    public static JFrame getPrefFrame() {
	return PreferencesManager.guiMgr.getPrefFrame();
    }

    public static void showPrefs() {
	PreferencesManager.guiMgr.showPrefs();
    }

    private static String getPrefsDirPrefix() {
	final String osName = System.getProperty(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_OS_NAME));
	if (osName.indexOf(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_MAC_OS_X)) != -1) {
	    // Mac OS X
	    return System.getenv(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_DIRECTORY_UNIX_HOME));
	} else if (osName.indexOf(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_WINDOWS)) != -1) {
	    // Windows
	    return System.getenv(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_DIRECTORY_WINDOWS_APPDATA));
	} else {
	    // Other - assume UNIX-like
	    return System.getenv(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_DIRECTORY_UNIX_HOME));
	}
    }

    private static String getPrefsDirectory() {
	final String osName = System.getProperty(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_OS_NAME));
	if (osName.indexOf(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_MAC_OS_X)) != -1) {
	    // Mac OS X
	    return StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_DIRECTORY_PREFS_MAC);
	} else if (osName.indexOf(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_WINDOWS)) != -1) {
	    // Windows
	    return StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_DIRECTORY_PREFS_WINDOWS);
	} else {
	    // Other - assume UNIX-like
	    return StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_DIRECTORY_PREFS_UNIX);
	}
    }

    private static String getPrefsFileExtension() {
	return StringConstants.COMMON_STRING_NOTL_PERIOD + Extension.getPreferencesExtension();
    }

    private static String getPrefsFileName() {
	final String osName = System.getProperty(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_OS_NAME));
	if (osName.indexOf(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_MAC_OS_X)) != -1) {
	    // Mac OS X
	    return StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_FILE_PREFS_MAC);
	} else if (osName.indexOf(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_WINDOWS)) != -1) {
	    // Windows
	    return StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_FILE_PREFS_WINDOWS);
	} else {
	    // Other - assume UNIX-like
	    return StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_FILE_PREFS_UNIX);
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

    public static void writePrefs() {
	try (BufferedOutputStream buf = new BufferedOutputStream(
		new FileOutputStream(PreferencesManager.getPrefsFile()))) {
	    PreferencesManager.storeMgr.saveStore(buf);
	} catch (final IOException io) {
	    // Ignore
	}
    }

    public static void readPrefs() {
	try (BufferedInputStream buf = new BufferedInputStream(
		new FileInputStream(PreferencesManager.getPrefsFile()))) {
	    // Read new preferences
	    PreferencesManager.storeMgr.loadStore(buf);
	} catch (final IOException io) {
	    // Populate store with defaults
	    PreferencesManager.storeMgr.setString(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_PREFS_KEY_LAST_DIR_OPEN), StringConstants.COMMON_STRING_EMPTY);
	    PreferencesManager.storeMgr.setString(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_PREFS_KEY_LAST_DIR_SAVE), StringConstants.COMMON_STRING_EMPTY);
	    PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_PREFS_KEY_UPDATES_STARTUP), true);
	    PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_PREFS_KEY_ONE_MOVE), true);
	    PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_SOUNDS), true);
	    PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_ANIMATION), true);
	    PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_DIFFICULTY_KIDS), true);
	    PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_DIFFICULTY_EASY), true);
	    PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_DIFFICULTY_MEDIUM), true);
	    PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_DIFFICULTY_HARD), true);
	    PreferencesManager.storeMgr.setBoolean(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_PREFS_KEY_ENABLE_DIFFICULTY_DEADLY), true);
	    PreferencesManager.storeMgr.setInteger(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_PREFS_KEY_ACTION_DELAY), 2);
	    PreferencesManager.storeMgr.setInteger(
		    StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			    StringConstants.NOTL_STRING_PREFS_KEY_LANGUAGE_ID),
		    PreferencesManager.FALLBACK_LANGUAGE_ID);
	}
    }
}
