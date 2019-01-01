/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;

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
	ArenaConstants.ERA_LIST = new String[] {
		StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, ArenaConstants.ERA_DISTANT_PAST),
		StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, ArenaConstants.ERA_PAST),
		StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, ArenaConstants.ERA_PRESENT),
		StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, ArenaConstants.ERA_FUTURE),
		StringLoader.loadString(StringConstants.ERA_STRINGS_FILE, ArenaConstants.ERA_DISTANT_FUTURE) };
    }

    public static String[] getLayerList() {
	return ArenaConstants.LAYER_LIST;
    }

    public static String[] getEraList() {
	return ArenaConstants.ERA_LIST;
    }

    public static Direction nextDirOrtho(final Direction input) {
	switch (input) {
	case INVALID:
	    return Direction.INVALID;
	case NONE:
	    return Direction.NONE;
	case NORTH:
	    return Direction.EAST;
	case NORTHEAST:
	    return Direction.SOUTHEAST;
	case EAST:
	    return Direction.SOUTH;
	case SOUTHEAST:
	    return Direction.SOUTHWEST;
	case SOUTH:
	    return Direction.WEST;
	case SOUTHWEST:
	    return Direction.NORTHWEST;
	case WEST:
	    return Direction.NORTH;
	case NORTHWEST:
	    return Direction.NORTHEAST;
	case HORIZONTAL:
	    return Direction.VERTICAL;
	case VERTICAL:
	    return Direction.HORIZONTAL;
	default:
	    return Direction.INVALID;
	}
    }

    public static Direction previousDirOrtho(final Direction input) {
	switch (input) {
	case INVALID:
	    return Direction.INVALID;
	case NONE:
	    return Direction.NONE;
	case NORTH:
	    return Direction.WEST;
	case NORTHEAST:
	    return Direction.NORTHWEST;
	case EAST:
	    return Direction.NORTH;
	case SOUTHEAST:
	    return Direction.NORTHEAST;
	case SOUTH:
	    return Direction.EAST;
	case SOUTHWEST:
	    return Direction.SOUTHEAST;
	case WEST:
	    return Direction.SOUTH;
	case NORTHWEST:
	    return Direction.SOUTHWEST;
	case HORIZONTAL:
	    return Direction.VERTICAL;
	case VERTICAL:
	    return Direction.HORIZONTAL;
	default:
	    return Direction.INVALID;
	}
    }

    public static Direction nextDir(final Direction input) {
	switch (input) {
	case INVALID:
	    return Direction.INVALID;
	case NONE:
	    return Direction.NONE;
	case NORTH:
	    return Direction.NORTHEAST;
	case NORTHEAST:
	    return Direction.EAST;
	case EAST:
	    return Direction.SOUTHEAST;
	case SOUTHEAST:
	    return Direction.SOUTH;
	case SOUTH:
	    return Direction.SOUTHWEST;
	case SOUTHWEST:
	    return Direction.WEST;
	case WEST:
	    return Direction.NORTHWEST;
	case NORTHWEST:
	    return Direction.NORTH;
	case HORIZONTAL:
	    return Direction.VERTICAL;
	case VERTICAL:
	    return Direction.HORIZONTAL;
	default:
	    return Direction.INVALID;
	}
    }

    public static Direction previousDir(final Direction input) {
	switch (input) {
	case INVALID:
	    return Direction.INVALID;
	case NONE:
	    return Direction.NONE;
	case NORTH:
	    return Direction.NORTHWEST;
	case NORTHEAST:
	    return Direction.NORTH;
	case EAST:
	    return Direction.NORTHEAST;
	case SOUTHEAST:
	    return Direction.EAST;
	case SOUTH:
	    return Direction.SOUTHEAST;
	case SOUTHWEST:
	    return Direction.SOUTH;
	case WEST:
	    return Direction.SOUTHWEST;
	case NORTHWEST:
	    return Direction.WEST;
	case HORIZONTAL:
	    return Direction.VERTICAL;
	case VERTICAL:
	    return Direction.HORIZONTAL;
	default:
	    return Direction.INVALID;
	}
    }
}
