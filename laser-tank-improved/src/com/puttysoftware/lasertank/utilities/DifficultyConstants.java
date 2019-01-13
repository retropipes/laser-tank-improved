/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import com.puttysoftware.lasertank.strings.DifficultyString;
import com.puttysoftware.lasertank.strings.StringLoader;

public final class DifficultyConstants {
    // Constants
    public static final int DIFFICULTY_KIDS = 1;
    public static final int DIFFICULTY_EASY = 2;
    public static final int DIFFICULTY_MEDIUM = 3;
    public static final int DIFFICULTY_HARD = 4;
    public static final int DIFFICULTY_DEADLY = 5;
    private static String[] DIFFICULTY_NAMES = null;

    public static String[] getDifficultyNames() {
	if (DifficultyConstants.DIFFICULTY_NAMES == null) {
	    DifficultyConstants.reloadDifficultyNames();
	}
	return DifficultyConstants.DIFFICULTY_NAMES;
    }

    public static void reloadDifficultyNames() {
	DifficultyConstants.DIFFICULTY_NAMES = new String[] { StringLoader.loadDifficulty(DifficultyString.KIDS),
		StringLoader.loadDifficulty(DifficultyString.EASY),
		StringLoader.loadDifficulty(DifficultyString.MEDIUM),
		StringLoader.loadDifficulty(DifficultyString.HARD),
		StringLoader.loadDifficulty(DifficultyString.DEADLY) };
    }

    // Private Constructor
    private DifficultyConstants() {
	// Do nothing
    }
}
