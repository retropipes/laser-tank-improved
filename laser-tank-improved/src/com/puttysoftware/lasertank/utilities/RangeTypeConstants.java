/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

public class RangeTypeConstants {
    public static final int RANGE_TYPE_BOMB = 0;
    public static final int RANGE_TYPE_HEAT_BOMB = 1;
    public static final int RANGE_TYPE_ICE_BOMB = 2;

    public static final int getMaterialForRangeType(final int rt) {
	switch (rt) {
	case RANGE_TYPE_BOMB:
	    return MaterialConstants.MATERIAL_METALLIC;
	case RANGE_TYPE_HEAT_BOMB:
	    return MaterialConstants.MATERIAL_FIRE;
	case RANGE_TYPE_ICE_BOMB:
	    return MaterialConstants.MATERIAL_ICE;
	default:
	    return MaterialConstants.MATERIAL_DEFAULT;
	}
    }

    private RangeTypeConstants() {
	// Do nothing
    }
}
