/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.utilities;

import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;

public final class DifficultyConstants {
    // Constants
    public static final int DIFFICULTY_KIDS = 1;
    public static final int DIFFICULTY_EASY = 2;
    public static final int DIFFICULTY_MEDIUM = 3;
    public static final int DIFFICULTY_HARD = 4;
    public static final int DIFFICULTY_DEADLY = 5;
    private static String[] DIFFICULTY_NAMES = null;

    // Private Constructor
    private DifficultyConstants() {
	// Do nothing
    }

    public static String[] getDifficultyNames() {
	if (DifficultyConstants.DIFFICULTY_NAMES == null) {
	    DifficultyConstants.reloadDifficultyNames();
	}
	return DifficultyConstants.DIFFICULTY_NAMES;
    }

    public static void reloadDifficultyNames() {
	DifficultyConstants.DIFFICULTY_NAMES = new String[] {
		StringLoader.loadString(StringConstants.DIFFICULTY_STRINGS_FILE,
			StringConstants.DIFFICULTY_STRING_KIDS),
		StringLoader.loadString(StringConstants.DIFFICULTY_STRINGS_FILE,
			StringConstants.DIFFICULTY_STRING_EASY),
		StringLoader.loadString(StringConstants.DIFFICULTY_STRINGS_FILE,
			StringConstants.DIFFICULTY_STRING_MEDIUM),
		StringLoader.loadString(StringConstants.DIFFICULTY_STRINGS_FILE,
			StringConstants.DIFFICULTY_STRING_HARD),
		StringLoader.loadString(StringConstants.DIFFICULTY_STRINGS_FILE,
			StringConstants.DIFFICULTY_STRING_DEADLY) };
    }
}
