/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import com.puttysoftware.lasertank.strings.global.GlobalLoader;

public class IndexResolver {
    public static String resolveIndexNumberToImageSuffix(final int in) {
	return GlobalLoader.loadIndex(in);
    }

    private IndexResolver() {
	// Do nothing
    }
}