/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import com.puttysoftware.lasertank.strings.CommonString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.strings.global.UntranslatedString;

public class Extension {
    // Constants
    private static final String STRINGS_EXTENSION = ".strings";
    private static final String PREFERENCES_EXTENSION = GlobalLoader
	    .loadUntranslated(UntranslatedString.EXTENSION_PREFS);
    private static final String OLD_LEVEL_EXTENSION = GlobalLoader
	    .loadUntranslated(UntranslatedString.EXTENSION_OLD_LEVEL);
    private static final String OLD_PLAYBACK_EXTENSION = GlobalLoader
	    .loadUntranslated(UntranslatedString.EXTENSION_OLD_PLAYBACK);
    private static final String ARENA_EXTENSION = GlobalLoader.loadUntranslated(UntranslatedString.EXTENSION_ARENA);
    private static final String PROTECTED_ARENA_EXTENSION = GlobalLoader
	    .loadUntranslated(UntranslatedString.EXTENSION_PROTECTED_ARENA);
    private static final String ARENA_LEVEL_EXTENSION = GlobalLoader
	    .loadUntranslated(UntranslatedString.EXTENSION_ARENA_DATA);
    private static final String SAVED_GAME_EXTENSION = GlobalLoader
	    .loadUntranslated(UntranslatedString.EXTENSION_SAVED_GAME);
    private static final String SCORES_EXTENSION = GlobalLoader.loadUntranslated(UntranslatedString.EXTENSION_SCORES);
    private static final String SOLUTION_EXTENSION = GlobalLoader
	    .loadUntranslated(UntranslatedString.EXTENSION_SOLUTION);

    public static String getStringsExtensionWithPeriod() {
	return Extension.STRINGS_EXTENSION;
    }

    public static String getArenaExtension() {
	return Extension.ARENA_EXTENSION;
    }

    public static String getArenaExtensionWithPeriod() {
	return StringLoader.loadCommon(CommonString.NOTL_PERIOD) + Extension.ARENA_EXTENSION;
    }

    public static String getArenaLevelExtensionWithPeriod() {
	return StringLoader.loadCommon(CommonString.NOTL_PERIOD) + Extension.ARENA_LEVEL_EXTENSION;
    }

    public static String getGameExtension() {
	return Extension.SAVED_GAME_EXTENSION;
    }

    public static String getGameExtensionWithPeriod() {
	return StringLoader.loadCommon(CommonString.NOTL_PERIOD) + Extension.SAVED_GAME_EXTENSION;
    }

    public static String getOldLevelExtension() {
	return Extension.OLD_LEVEL_EXTENSION;
    }

    public static String getOldPlaybackExtension() {
	return Extension.OLD_PLAYBACK_EXTENSION;
    }

    // Methods
    public static String getPreferencesExtension() {
	return Extension.PREFERENCES_EXTENSION;
    }

    public static String getProtectedArenaExtension() {
	return Extension.PROTECTED_ARENA_EXTENSION;
    }

    public static String getProtectedArenaExtensionWithPeriod() {
	return StringLoader.loadCommon(CommonString.NOTL_PERIOD) + Extension.PROTECTED_ARENA_EXTENSION;
    }

    public static String getScoresExtensionWithPeriod() {
	return StringLoader.loadCommon(CommonString.NOTL_PERIOD) + Extension.SCORES_EXTENSION;
    }

    public static String getSolutionExtensionWithPeriod() {
	return StringLoader.loadCommon(CommonString.NOTL_PERIOD) + Extension.SOLUTION_EXTENSION;
    }

    private Extension() {
	// Do nothing
    }
}