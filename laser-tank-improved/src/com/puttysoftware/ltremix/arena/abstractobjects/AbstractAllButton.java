/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public abstract class AbstractAllButton extends AbstractButton {
    // Constructors
    protected AbstractAllButton(final AbstractAllButtonDoor abd, final boolean isUniversal) {
	super(abd, isUniversal);
	this.type.set(TypeConstants.TYPE_ALL_BUTTON);
    }

    @Override
    public boolean pushIntoAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	if (this.isUniversal() || pushed.getMaterial() == this.getMaterial()) {
	    SoundManager.playSound(SoundConstants.SOUND_BUTTON);
	    if (!this.isTriggered()) {
		// Check to open door at location
		this.setTriggered(true);
		LTRemix.getApplication().getArenaManager().getArena().fullScanAllButtonOpen(z, this);
	    }
	}
	return true;
    }

    @Override
    public void pushOutAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	if (this.isUniversal() || pushed.getMaterial() == this.getMaterial()) {
	    if (this.isTriggered()) {
		// Check to close door at location
		this.setTriggered(false);
		LTRemix.getApplication().getArenaManager().getArena().fullScanAllButtonClose(z, this);
	    }
	}
    }
}
