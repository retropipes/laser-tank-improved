/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;

public class DirectionResolver {
    private DirectionResolver() {
	// Do nothing
    }

    public static Direction resolveRelativeDirection(final int dX, final int dY) {
	final int dirX = (int) Math.signum(dX);
	final int dirY = (int) Math.signum(dY);
	if (dirX == 0 && dirY == 0) {
	    return Direction.NONE;
	} else if (dirX == 0 && dirY == -1) {
	    return Direction.NORTH;
	} else if (dirX == 0 && dirY == 1) {
	    return Direction.SOUTH;
	} else if (dirX == -1 && dirY == 0) {
	    return Direction.WEST;
	} else if (dirX == 1 && dirY == 0) {
	    return Direction.EAST;
	} else if (dirX == 1 && dirY == 1) {
	    return Direction.SOUTHEAST;
	} else if (dirX == -1 && dirY == 1) {
	    return Direction.SOUTHWEST;
	} else if (dirX == -1 && dirY == -1) {
	    return Direction.NORTHWEST;
	} else if (dirX == 1 && dirY == -1) {
	    return Direction.NORTHEAST;
	} else {
	    return Direction.INVALID;
	}
    }

    public static Direction resolveRelativeDirectionInvert(final int dX, final int dY) {
	final int dirX = (int) Math.signum(dX);
	final int dirY = (int) Math.signum(dY);
	if (dirX == 0 && dirY == 0) {
	    return Direction.NONE;
	} else if (dirX == 0 && dirY == -1) {
	    return Direction.SOUTH;
	} else if (dirX == 0 && dirY == 1) {
	    return Direction.NORTH;
	} else if (dirX == -1 && dirY == 0) {
	    return Direction.EAST;
	} else if (dirX == 1 && dirY == 0) {
	    return Direction.WEST;
	} else if (dirX == 1 && dirY == 1) {
	    return Direction.NORTHWEST;
	} else if (dirX == -1 && dirY == 1) {
	    return Direction.NORTHEAST;
	} else if (dirX == -1 && dirY == -1) {
	    return Direction.SOUTHEAST;
	} else if (dirX == 1 && dirY == -1) {
	    return Direction.SOUTHWEST;
	} else {
	    return Direction.INVALID;
	}
    }

    public static Direction resolveRelativeDirectionHV(final int dX, final int dY) {
	final int dirX = (int) Math.signum(dX);
	final int dirY = (int) Math.signum(dY);
	if (dirX == 0 && dirY == 0) {
	    return Direction.NONE;
	} else if (dirX == 0 && dirY == -1) {
	    return Direction.VERTICAL;
	} else if (dirX == 0 && dirY == 1) {
	    return Direction.VERTICAL;
	} else if (dirX == -1 && dirY == 0) {
	    return Direction.HORIZONTAL;
	} else if (dirX == 1 && dirY == 0) {
	    return Direction.HORIZONTAL;
	} else if (dirX == 1 && dirY == 1) {
	    return Direction.SOUTHEAST;
	} else if (dirX == -1 && dirY == 1) {
	    return Direction.SOUTHWEST;
	} else if (dirX == -1 && dirY == -1) {
	    return Direction.NORTHWEST;
	} else if (dirX == 1 && dirY == -1) {
	    return Direction.NORTHEAST;
	} else {
	    return Direction.INVALID;
	}
    }

    public static int[] unresolveRelativeDirection(final Direction dir) {
	int[] res = new int[2];
	if (dir == Direction.NONE) {
	    res[0] = 0;
	    res[1] = 0;
	} else if (dir == Direction.NORTH) {
	    res[0] = 0;
	    res[1] = -1;
	} else if (dir == Direction.SOUTH) {
	    res[0] = 0;
	    res[1] = 1;
	} else if (dir == Direction.WEST) {
	    res[0] = -1;
	    res[1] = 0;
	} else if (dir == Direction.EAST) {
	    res[0] = 1;
	    res[1] = 0;
	} else if (dir == Direction.SOUTHEAST) {
	    res[0] = 1;
	    res[1] = 1;
	} else if (dir == Direction.SOUTHWEST) {
	    res[0] = -1;
	    res[1] = 1;
	} else if (dir == Direction.NORTHWEST) {
	    res[0] = -1;
	    res[1] = -1;
	} else if (dir == Direction.NORTHEAST) {
	    res[0] = 1;
	    res[1] = -1;
	} else {
	    res = null;
	}
	return res;
    }

    public static String resolveDirectionConstantToImageName(final Direction dir) {
	return StringLoader.loadString(StringConstants.STRINGS_FILE, dir.getInternalValue());
    }
}
