/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.utilities;

import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;

public final class EraConstants {
    // Constants
    public static final int ERA_PAST = 0;
    public static final int ERA_PRESENT = 1;
    public static final int ERA_FUTURE = 2;
    public static final int MAX_ERAS = 3;
    private static String[] ERA_NAMES = null;

    // Private Constructor
    private EraConstants() {
	// Do nothing
    }

    public static String[] getEraNames() {
	if (EraConstants.ERA_NAMES == null) {
	    EraConstants.reloadEraNames();
	}
	return EraConstants.ERA_NAMES;
    }

    public static void reloadEraNames() {
	EraConstants.ERA_NAMES = new String[] {
		StringLoader.loadString(StringConstants.TIME_STRINGS_FILE, StringConstants.TIME_STRING_PAST),
		StringLoader.loadString(StringConstants.TIME_STRINGS_FILE, StringConstants.TIME_STRING_PRESENT),
		StringLoader.loadString(StringConstants.TIME_STRINGS_FILE, StringConstants.TIME_STRING_FUTURE) };
    }
}
