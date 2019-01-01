/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.game;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.prefs.PreferencesManager;

class ReplayTask extends Thread {
    // Constructors
    public ReplayTask() {
	// Do nothing
    }

    @Override
    public void run() {
	final GameManager gm = LTRemix.getApplication().getGameManager();
	boolean result = true;
	while (result) {
	    result = gm.replayLastMove();
	    // Delay, for animation purposes
	    try {
		Thread.sleep(PreferencesManager.getReplaySpeed());
	    } catch (final InterruptedException ie) {
		// Ignore
	    }
	}
	gm.replayDone();
    }
}
