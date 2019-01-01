/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.utilities;

public class LaserTypeConstants {
    public static final int LASER_TYPE_GREEN = 3;
    public static final int LASER_TYPE_RED = 5;
    public static final int LASER_TYPE_MISSILE = 9;
    public static final int LASER_TYPE_STUNNER = 17;
    public static final int LASER_TYPE_BLUE = 33;
    public static final int LASER_TYPE_DISRUPTOR = 65;
    public static final int LASER_TYPE_POWER = 129;

    private LaserTypeConstants() {
	// Do nothing
    }

    public static final int getRangeTypeForLaserType(final int lt) {
	switch (lt) {
	case LASER_TYPE_STUNNER:
	    return RangeTypeConstants.RANGE_TYPE_ICE_BOMB;
	case LASER_TYPE_MISSILE:
	    return RangeTypeConstants.RANGE_TYPE_HEAT_BOMB;
	default:
	    return RangeTypeConstants.RANGE_TYPE_BOMB;
	}
    }
}
