/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

public enum Direction {
    INVALID(-1), NONE(0), NORTHWEST(1), NORTH(2), NORTHEAST(3), EAST(4), SOUTHEAST(5), SOUTH(6), SOUTHWEST(7), WEST(8),
    HORIZONTAL(9), VERTICAL(10);
    int internalValue;

    Direction(final int v) {
	this.internalValue = v;
    }

    int getInternalValue() {
	return this.internalValue;
    }
}
