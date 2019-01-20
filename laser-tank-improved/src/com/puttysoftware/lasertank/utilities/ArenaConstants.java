/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import com.puttysoftware.lasertank.strings.EditorString;
import com.puttysoftware.lasertank.strings.StringLoader;

public class ArenaConstants {
    public static final int LAYER_LOWER_GROUND = 0;
    public static final int LAYER_UPPER_GROUND = 1;
    public static final int LAYER_LOWER_OBJECTS = 2;
    public static final int LAYER_UPPER_OBJECTS = 3;
    public static final int NUM_LAYERS = 4;
    public static final int LAYER_VIRTUAL = 0;
    public static final int NUM_VIRTUAL_LAYERS = 1;
    private static String[] LAYER_LIST = null;
    public static final int ERA_DISTANT_PAST = 0;
    public static final int ERA_PAST = 1;
    public static final int ERA_PRESENT = 2;
    public static final int ERA_FUTURE = 3;
    public static final int ERA_DISTANT_FUTURE = 4;
    private static String[] ERA_LIST = null;
    public static final int PLAYER_DIMS = 3;
    public static final int NUM_PLAYERS = 9;

    public static void activeLanguageChanged() {
	ArenaConstants.LAYER_LIST = new String[] { StringLoader.loadEditor(EditorString.LOWER_GROUND_LAYER),
		StringLoader.loadEditor(EditorString.UPPER_GROUND_LAYER),
		StringLoader.loadEditor(EditorString.LOWER_OBJECTS_LAYER),
		StringLoader.loadEditor(EditorString.UPPER_OBJECTS_LAYER) };
	ArenaConstants.ERA_LIST = new String[] { StringLoader.loadTime(ArenaConstants.ERA_DISTANT_PAST),
		StringLoader.loadTime(ArenaConstants.ERA_PAST), StringLoader.loadTime(ArenaConstants.ERA_PRESENT),
		StringLoader.loadTime(ArenaConstants.ERA_FUTURE),
		StringLoader.loadTime(ArenaConstants.ERA_DISTANT_FUTURE) };
    }

    public static String[] getEraList() {
	return ArenaConstants.ERA_LIST;
    }

    public static String[] getLayerList() {
	return ArenaConstants.LAYER_LIST;
    }

    private ArenaConstants() {
	// Do nothing
    }
}
