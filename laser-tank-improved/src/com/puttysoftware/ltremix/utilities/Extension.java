/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.utilities;

import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;

public class Extension {
    private Extension() {
	// Do nothing
    }

    // Constants
    private static final String PREFERENCES_EXTENSION = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_EXTENSION_PREFS);
    private static final String ARENA_EXTENSION = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_EXTENSION_ARENA);
    private static final String PROTECTED_ARENA_EXTENSION = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_EXTENSION_PROTECTED_ARENA);
    private static final String ARENA_LEVEL_EXTENSION = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_EXTENSION_ARENA_DATA);
    private static final String SAVED_GAME_EXTENSION = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_EXTENSION_SAVED_GAME);
    private static final String SCORES_EXTENSION = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_EXTENSION_SCORES);
    private static final String SOLUTION_EXTENSION = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_EXTENSION_SOLUTION);

    // Methods
    public static String getPreferencesExtension() {
	return Extension.PREFERENCES_EXTENSION;
    }

    public static String getArenaExtension() {
	return Extension.ARENA_EXTENSION;
    }

    public static String getArenaExtensionWithPeriod() {
	return StringConstants.COMMON_STRING_NOTL_PERIOD + Extension.ARENA_EXTENSION;
    }

    public static String getProtectedArenaExtension() {
	return Extension.PROTECTED_ARENA_EXTENSION;
    }

    public static String getProtectedArenaExtensionWithPeriod() {
	return StringConstants.COMMON_STRING_NOTL_PERIOD + Extension.PROTECTED_ARENA_EXTENSION;
    }

    public static String getArenaLevelExtensionWithPeriod() {
	return StringConstants.COMMON_STRING_NOTL_PERIOD + Extension.ARENA_LEVEL_EXTENSION;
    }

    public static String getGameExtension() {
	return Extension.SAVED_GAME_EXTENSION;
    }

    public static String getGameExtensionWithPeriod() {
	return StringConstants.COMMON_STRING_NOTL_PERIOD + Extension.SAVED_GAME_EXTENSION;
    }

    public static String getScoresExtensionWithPeriod() {
	return StringConstants.COMMON_STRING_NOTL_PERIOD + Extension.SCORES_EXTENSION;
    }

    public static String getSolutionExtensionWithPeriod() {
	return StringConstants.COMMON_STRING_NOTL_PERIOD + Extension.SOLUTION_EXTENSION;
    }
}