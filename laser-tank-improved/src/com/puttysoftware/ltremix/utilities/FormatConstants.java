/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.utilities;

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
    private static final int ARENA_FORMAT_18 = 18;
    private static final int ARENA_FORMAT_19 = 19;
    private static final int ARENA_FORMAT_20 = 20;
    private static final int ARENA_FORMAT_21 = 21;
    public static final int ARENA_FORMAT_LATEST = 21;

    private FormatConstants() {
	// Do nothing
    }

    public static final boolean isMoveShootAllowed(final int ver) {
	return ver >= FormatConstants.ARENA_FORMAT_11;
    }

    public static final boolean isFormatVersionValidGeneration7(final int ver) {
	return ver == FormatConstants.ARENA_FORMAT_21;
    }

    public static final boolean isFormatVersionValidGeneration6(final int ver) {
	return ver == FormatConstants.ARENA_FORMAT_19 || ver == FormatConstants.ARENA_FORMAT_20;
    }

    public static final boolean isFormatVersionValidGeneration5(final int ver) {
	return ver == FormatConstants.ARENA_FORMAT_12 || ver == FormatConstants.ARENA_FORMAT_15
		|| ver == FormatConstants.ARENA_FORMAT_16 || ver == FormatConstants.ARENA_FORMAT_17
		|| ver == FormatConstants.ARENA_FORMAT_18;
    }

    public static final boolean isFormatVersionValidGeneration4(final int ver) {
	return ver == FormatConstants.ARENA_FORMAT_10 || ver == FormatConstants.ARENA_FORMAT_11;
    }

    public static final boolean isFormatVersionValidGeneration3(final int ver) {
	return ver == FormatConstants.ARENA_FORMAT_9;
    }

    public static final boolean isFormatVersionValidGeneration2(final int ver) {
	return ver == FormatConstants.ARENA_FORMAT_7 || ver == FormatConstants.ARENA_FORMAT_8;
    }

    public static final boolean isFormatVersionValidGeneration1(final int ver) {
	return ver == FormatConstants.ARENA_FORMAT_5 || ver == FormatConstants.ARENA_FORMAT_6;
    }
}
