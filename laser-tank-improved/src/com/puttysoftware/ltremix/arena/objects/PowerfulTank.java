/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractCharacter;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.ActionConstants;

public class PowerfulTank extends AbstractCharacter {
    // Constructors
    public PowerfulTank() {
	super(true, 0);
	this.activateTimer(50);
    }

    public PowerfulTank(final int dir, final boolean useTimer, final int instance) {
	super(useTimer, instance);
	this.setDirection(dir);
	if (useTimer) {
	    this.activateTimer(50);
	}
    }

    @Override
    public boolean acceptTick(final int actionType) {
	return actionType == ActionConstants.ACTION_MOVE;
    }

    @Override
    public void timerExpiredAction(final int x, final int y) {
	SoundManager.playSound(SoundConstants.SOUND_DISRUPT_END);
	LTRemix.getApplication().getGameManager().setNormalTank();
    }

    @Override
    public final int getStringBaseID() {
	return 138;
    }

    @Override
    public boolean isDirectional() {
	return true;
    }
}