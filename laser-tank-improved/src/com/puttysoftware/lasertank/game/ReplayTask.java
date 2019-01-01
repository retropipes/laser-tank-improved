/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.game;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.prefs.PreferencesManager;

class ReplayTask extends Thread {
    // Constructors
    public ReplayTask() {
	// Do nothing
    }

    @Override
    public void run() {
	final GameManager gm = LaserTank.getApplication().getGameManager();
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
