/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import com.puttysoftware.lasertank.strings.PrefString;
import com.puttysoftware.lasertank.strings.StringLoader;

public final class EditorLayoutConstants {
    // Constants
    public static final int EDITOR_LAYOUT_CLASSIC = 0;
    public static final int EDITOR_LAYOUT_MODERN_V11 = 1;
    public static final int EDITOR_LAYOUT_MODERN_V12 = 2;
    private static String[] EDITOR_LAYOUT_LIST = new String[] { StringLoader.loadPref(PrefString.EDITOR_LAYOUT_CLASSIC),
	    StringLoader.loadPref(PrefString.EDITOR_LAYOUT_MODERN_V11),
	    StringLoader.loadPref(PrefString.EDITOR_LAYOUT_MODERN_V12) };

    public static void activeLanguageChanged() {
	EditorLayoutConstants.EDITOR_LAYOUT_LIST = new String[] {
		StringLoader.loadPref(PrefString.EDITOR_LAYOUT_CLASSIC),
		StringLoader.loadPref(PrefString.EDITOR_LAYOUT_MODERN_V11),
		StringLoader.loadPref(PrefString.EDITOR_LAYOUT_MODERN_V12) };
    }

    public static String[] getEditorLayoutList() {
	return EditorLayoutConstants.EDITOR_LAYOUT_LIST;
    }

    // Private Constructor
    private EditorLayoutConstants() {
	// Do nothing
    }
}
