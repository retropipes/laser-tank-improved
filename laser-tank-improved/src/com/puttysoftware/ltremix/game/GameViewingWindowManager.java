/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.game;

import com.puttysoftware.ltremix.arena.AbstractArena;

final class GameViewingWindowManager {
    // Fields
    private static final int VIEWING_WINDOW_SIZE_X = AbstractArena.getMinColumns();
    private static final int VIEWING_WINDOW_SIZE_Y = AbstractArena.getMinRows();

    // Constructors
    private GameViewingWindowManager() {
	// Do nothing
    }

    // Methods
    static int getViewingWindowLocationX() {
	return 0;
    }

    static int getViewingWindowLocationY() {
	return 0;
    }

    static int getLowerRightViewingWindowLocationX() {
	return GameViewingWindowManager.VIEWING_WINDOW_SIZE_X - 1;
    }

    static int getLowerRightViewingWindowLocationY() {
	return GameViewingWindowManager.VIEWING_WINDOW_SIZE_Y - 1;
    }

    static int getViewingWindowSize() {
	return GameViewingWindowManager.VIEWING_WINDOW_SIZE_X;
    }

    static int getViewingWindowSizeX() {
	return GameViewingWindowManager.VIEWING_WINDOW_SIZE_X;
    }

    static int getViewingWindowSizeY() {
	return GameViewingWindowManager.VIEWING_WINDOW_SIZE_Y;
    }
}
