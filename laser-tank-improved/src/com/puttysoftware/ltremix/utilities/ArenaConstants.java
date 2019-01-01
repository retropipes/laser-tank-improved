/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.utilities;

import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;

public class ArenaConstants {
    public static final int LAYER_LOWER_GROUND = 0;
    public static final int LAYER_UPPER_GROUND = 1;
    public static final int LAYER_LOWER_OBJECTS = 2;
    public static final int LAYER_UPPER_OBJECTS = 3;
    public static final int NUM_LAYERS = 4;
    public static final int LAYER_VIRTUAL = 0;
    public static final int NUM_VIRTUAL_LAYERS = 1;
    private static String[] LAYER_LIST = null;
    public static final String[] COORDS_LIST_X = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
	    "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X" };
    public static final String[] COORDS_LIST_Y = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
	    "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24" };

    private ArenaConstants() {
	// Do nothing
    }

    public static void activeLanguageChanged() {
	ArenaConstants.LAYER_LIST = new String[] {
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_LOWER_GROUND_LAYER),
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_UPPER_GROUND_LAYER),
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_LOWER_OBJECTS_LAYER),
		StringLoader.loadString(StringConstants.EDITOR_STRINGS_FILE,
			StringConstants.EDITOR_STRING_UPPER_OBJECTS_LAYER) };
    }

    public static String[] getLayerList() {
	return ArenaConstants.LAYER_LIST;
    }
}
