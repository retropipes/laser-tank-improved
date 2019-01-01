/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;

public final class EditorLayoutConstants {
    // Constants
    public static final int EDITOR_LAYOUT_CLASSIC = 0;
    public static final int EDITOR_LAYOUT_MODERN_V11 = 1;
    public static final int EDITOR_LAYOUT_MODERN_V12 = 2;
    private static String[] EDITOR_LAYOUT_LIST = new String[] {
	    StringLoader.loadString(StringConstants.PREFS_STRINGS_FILE,
		    StringConstants.PREFS_STRING_EDITOR_LAYOUT_CLASSIC),
	    StringLoader.loadString(StringConstants.PREFS_STRINGS_FILE,
		    StringConstants.PREFS_STRING_EDITOR_LAYOUT_MODERN_V11),
	    StringLoader.loadString(StringConstants.PREFS_STRINGS_FILE,
		    StringConstants.PREFS_STRING_EDITOR_LAYOUT_MODERN_V12) };

    // Private Constructor
    private EditorLayoutConstants() {
	// Do nothing
    }

    public static void activeLanguageChanged() {
	EditorLayoutConstants.EDITOR_LAYOUT_LIST = new String[] {
		StringLoader.loadString(StringConstants.PREFS_STRINGS_FILE,
			StringConstants.PREFS_STRING_EDITOR_LAYOUT_CLASSIC),
		StringLoader.loadString(StringConstants.PREFS_STRINGS_FILE,
			StringConstants.PREFS_STRING_EDITOR_LAYOUT_MODERN_V11),
		StringLoader.loadString(StringConstants.PREFS_STRINGS_FILE,
			StringConstants.PREFS_STRING_EDITOR_LAYOUT_MODERN_V12) };
    }

    public static String[] getEditorLayoutList() {
	return EditorLayoutConstants.EDITOR_LAYOUT_LIST;
    }
}
