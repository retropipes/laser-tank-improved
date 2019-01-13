/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import com.puttysoftware.lasertank.strings.global.GlobalLoader;

public class FrameResolver {
    public static String resolveFrameNumberToImageSuffix(final int fn) {
	return GlobalLoader.loadFrame(fn);
    }

    private FrameResolver() {
	// Do nothing
    }
}