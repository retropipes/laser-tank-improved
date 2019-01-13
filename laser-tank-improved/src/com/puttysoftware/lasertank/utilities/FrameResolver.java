/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;

public class FrameResolver {
    public static String resolveFrameNumberToImageSuffix(final int fn) {
	return StringLoader.loadString(StringConstants.STRINGS_FILE_FRAME, fn);
    }

    private FrameResolver() {
	// Do nothing
    }
}