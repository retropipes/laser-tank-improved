/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.utilities;

import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;

public class ColorResolver {
    private ColorResolver() {
	// Do nothing
    }

    public static String resolveColorConstantToName(final int dir) {
	String res = null;
	if (dir == ColorConstants.COLOR_BLUE) {
	    res = StringLoader.loadString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_BLUE);
	} else if (dir == ColorConstants.COLOR_GREEN) {
	    res = StringLoader.loadString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_GREEN);
	} else if (dir == ColorConstants.COLOR_GRAY) {
	    res = StringLoader.loadString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_GRAY);
	} else if (dir == ColorConstants.COLOR_MAGENTA) {
	    res = StringLoader.loadString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_MAGENTA);
	} else if (dir == ColorConstants.COLOR_RED) {
	    res = StringLoader.loadString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_RED);
	} else if (dir == ColorConstants.COLOR_CYAN) {
	    res = StringLoader.loadString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_CYAN);
	} else if (dir == ColorConstants.COLOR_WHITE) {
	    res = StringLoader.loadString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_WHITE);
	} else if (dir == ColorConstants.COLOR_YELLOW) {
	    res = StringLoader.loadString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_YELLOW);
	}
	return res;
    }

    public static String resolveColorConstantToImageName(final int dir) {
	String res = null;
	if (dir == ColorConstants.COLOR_BLUE) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_BLUE);
	} else if (dir == ColorConstants.COLOR_GREEN) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_GREEN);
	} else if (dir == ColorConstants.COLOR_GRAY) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_GRAY);
	} else if (dir == ColorConstants.COLOR_MAGENTA) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_MAGENTA);
	} else if (dir == ColorConstants.COLOR_RED) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_RED);
	} else if (dir == ColorConstants.COLOR_CYAN) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_CYAN);
	} else if (dir == ColorConstants.COLOR_WHITE) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_WHITE);
	} else if (dir == ColorConstants.COLOR_YELLOW) {
	    res = StringLoader.loadImageString(StringConstants.GENERIC_STRINGS_FILE,
		    StringConstants.GENERIC_STRING_COLOR_YELLOW);
	}
	return res;
    }
}