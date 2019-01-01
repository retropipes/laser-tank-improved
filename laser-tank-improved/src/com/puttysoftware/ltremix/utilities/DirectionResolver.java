/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.utilities;

import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;

public class DirectionResolver {
    private DirectionResolver() {
	// Do nothing
    }

    public static int resolveRelativeDirection(final int dX, final int dY) {
	final int dirX = (int) Math.signum(dX);
	final int dirY = (int) Math.signum(dY);
	if (dirX == 0 && dirY == 0) {
	    return DirectionConstants.NONE;
	} else if (dirX == 0 && dirY == -1) {
	    return DirectionConstants.NORTH;
	} else if (dirX == 0 && dirY == 1) {
	    return DirectionConstants.SOUTH;
	} else if (dirX == -1 && dirY == 0) {
	    return DirectionConstants.WEST;
	} else if (dirX == 1 && dirY == 0) {
	    return DirectionConstants.EAST;
	} else if (dirX == 1 && dirY == 1) {
	    return DirectionConstants.SOUTHEAST;
	} else if (dirX == -1 && dirY == 1) {
	    return DirectionConstants.SOUTHWEST;
	} else if (dirX == -1 && dirY == -1) {
	    return DirectionConstants.NORTHWEST;
	} else if (dirX == 1 && dirY == -1) {
	    return DirectionConstants.NORTHEAST;
	} else {
	    return DirectionConstants.INVALID;
	}
    }

    public static int resolveRelativeDirectionInvert(final int dX, final int dY) {
	final int dirX = (int) Math.signum(dX);
	final int dirY = (int) Math.signum(dY);
	if (dirX == 0 && dirY == 0) {
	    return DirectionConstants.NONE;
	} else if (dirX == 0 && dirY == -1) {
	    return DirectionConstants.SOUTH;
	} else if (dirX == 0 && dirY == 1) {
	    return DirectionConstants.NORTH;
	} else if (dirX == -1 && dirY == 0) {
	    return DirectionConstants.EAST;
	} else if (dirX == 1 && dirY == 0) {
	    return DirectionConstants.WEST;
	} else if (dirX == 1 && dirY == 1) {
	    return DirectionConstants.NORTHWEST;
	} else if (dirX == -1 && dirY == 1) {
	    return DirectionConstants.NORTHEAST;
	} else if (dirX == -1 && dirY == -1) {
	    return DirectionConstants.SOUTHEAST;
	} else if (dirX == 1 && dirY == -1) {
	    return DirectionConstants.SOUTHWEST;
	} else {
	    return DirectionConstants.INVALID;
	}
    }

    public static int resolveRelativeDirectionHV(final int dX, final int dY) {
	final int dirX = (int) Math.signum(dX);
	final int dirY = (int) Math.signum(dY);
	if (dirX == 0 && dirY == 0) {
	    return DirectionConstants.NONE;
	} else if (dirX == 0 && dirY == -1) {
	    return DirectionConstants.VERTICAL;
	} else if (dirX == 0 && dirY == 1) {
	    return DirectionConstants.VERTICAL;
	} else if (dirX == -1 && dirY == 0) {
	    return DirectionConstants.HORIZONTAL;
	} else if (dirX == 1 && dirY == 0) {
	    return DirectionConstants.HORIZONTAL;
	} else if (dirX == 1 && dirY == 1) {
	    return DirectionConstants.SOUTHEAST;
	} else if (dirX == -1 && dirY == 1) {
	    return DirectionConstants.SOUTHWEST;
	} else if (dirX == -1 && dirY == -1) {
	    return DirectionConstants.NORTHWEST;
	} else if (dirX == 1 && dirY == -1) {
	    return DirectionConstants.NORTHEAST;
	} else {
	    return DirectionConstants.INVALID;
	}
    }

    public static int[] unresolveRelativeDirection(final int dir) {
	int[] res = new int[2];
	if (dir == DirectionConstants.NONE) {
	    res[0] = 0;
	    res[1] = 0;
	} else if (dir == DirectionConstants.NORTH) {
	    res[0] = 0;
	    res[1] = -1;
	} else if (dir == DirectionConstants.SOUTH) {
	    res[0] = 0;
	    res[1] = 1;
	} else if (dir == DirectionConstants.WEST) {
	    res[0] = -1;
	    res[1] = 0;
	} else if (dir == DirectionConstants.EAST) {
	    res[0] = 1;
	    res[1] = 0;
	} else if (dir == DirectionConstants.SOUTHEAST) {
	    res[0] = 1;
	    res[1] = 1;
	} else if (dir == DirectionConstants.SOUTHWEST) {
	    res[0] = -1;
	    res[1] = 1;
	} else if (dir == DirectionConstants.NORTHWEST) {
	    res[0] = -1;
	    res[1] = -1;
	} else if (dir == DirectionConstants.NORTHEAST) {
	    res[0] = 1;
	    res[1] = -1;
	} else {
	    res = null;
	}
	return res;
    }

    public static String resolveDirectionConstantToImageName(final int dir) {
	String res = null;
	if (dir == DirectionConstants.NORTH) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_NORTH);
	} else if (dir == DirectionConstants.SOUTH) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_SOUTH);
	} else if (dir == DirectionConstants.WEST) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_WEST);
	} else if (dir == DirectionConstants.EAST) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_EAST);
	} else if (dir == DirectionConstants.SOUTHEAST) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_SOUTHEAST);
	} else if (dir == DirectionConstants.SOUTHWEST) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_SOUTHWEST);
	} else if (dir == DirectionConstants.NORTHWEST) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_NORTHWEST);
	} else if (dir == DirectionConstants.NORTHEAST) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_NORTHEAST);
	} else if (dir == DirectionConstants.HORIZONTAL) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_HORIZONTAL);
	} else if (dir == DirectionConstants.VERTICAL) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_VERTICAL);
	}
	return res;
    }
}
