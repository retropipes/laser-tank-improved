/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

public class FormatConstants {
    private static final int ARENA_FORMAT_5 = 5;
    private static final int ARENA_FORMAT_6 = 6;
    private static final int ARENA_FORMAT_7 = 7;
    private static final int ARENA_FORMAT_8 = 8;
    private static final int ARENA_FORMAT_9 = 9;
    private static final int ARENA_FORMAT_10 = 10;
    private static final int ARENA_FORMAT_11 = 11;
    private static final int ARENA_FORMAT_12 = 12;
    private static final int ARENA_FORMAT_15 = 15;
    private static final int ARENA_FORMAT_16 = 16;
    private static final int ARENA_FORMAT_17 = 17;
    public static final int ARENA_FORMAT_LATEST = 17;

    public static final boolean isFormatVersionValidGeneration1(final int ver) {
	return ver == FormatConstants.ARENA_FORMAT_5 || ver == FormatConstants.ARENA_FORMAT_6;
    }

    public static final boolean isFormatVersionValidGeneration2(final int ver) {
	return ver == FormatConstants.ARENA_FORMAT_7 || ver == FormatConstants.ARENA_FORMAT_8;
    }

    public static final boolean isFormatVersionValidGeneration3(final int ver) {
	return ver == FormatConstants.ARENA_FORMAT_9;
    }

    public static final boolean isFormatVersionValidGeneration4(final int ver) {
	return ver == FormatConstants.ARENA_FORMAT_10 || ver == FormatConstants.ARENA_FORMAT_11;
    }

    public static final boolean isFormatVersionValidGeneration5(final int ver) {
	return ver == FormatConstants.ARENA_FORMAT_12 || ver == FormatConstants.ARENA_FORMAT_15
		|| ver == FormatConstants.ARENA_FORMAT_16;
    }

    public static final boolean isFormatVersionValidGeneration6(final int ver) {
	return ver == FormatConstants.ARENA_FORMAT_17;
    }

    public static final boolean isLevelListStored(final int ver) {
	return ver >= FormatConstants.ARENA_FORMAT_17;
    }

    public static final boolean isMoveShootAllowed(final int ver) {
	return ver >= FormatConstants.ARENA_FORMAT_11;
    }

    private FormatConstants() {
	// Do nothing
    }
}
