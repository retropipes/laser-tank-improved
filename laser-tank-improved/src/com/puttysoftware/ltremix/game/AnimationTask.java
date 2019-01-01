/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.game;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.AbstractArena;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;
import com.puttysoftware.ltremix.utilities.ArenaConstants;

class AnimationTask extends Thread {
    // Fields
    private boolean stop = false;

    // Constructors
    public AnimationTask() {
	this.setName(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_ANIMATOR_NAME));
	this.setPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public void run() {
	try {
	    final AbstractArena a = LTRemix.getApplication().getArenaManager().getArena();
	    while (!this.stop) {
		final int pz = LTRemix.getApplication().getGameManager().getPlayerManager().getPlayerLocationZ();
		final int maxX = a.getRows();
		final int maxY = a.getColumns();
		final int maxW = ArenaConstants.NUM_LAYERS;
		for (int x = 0; x < maxX; x++) {
		    for (int y = 0; y < maxY; y++) {
			for (int w = 0; w < maxW; w++) {
			    final AbstractArenaObject obj = a.getCell(x, y, pz, w);
			    if (obj != null) {
				final int oldFN = obj.getFrameNumber();
				obj.toggleFrameNumber();
				final int newFN = obj.getFrameNumber();
				if (oldFN != newFN) {
				    a.markAsDirty(x, y, pz);
				}
			    }
			}
		    }
		}
		LTRemix.getApplication().getGameManager().redrawArena();
		try {
		    Thread.sleep(100);
		} catch (final InterruptedException ie) {
		    // Ignore
		}
	    }
	} catch (final Throwable t) {
	    LTRemix.getErrorLogger().logError(t);
	}
    }

    void stopAnimator() {
	this.stop = true;
    }
}
