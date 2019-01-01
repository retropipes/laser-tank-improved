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
import com.puttysoftware.ltremix.utilities.DirectionConstants;

public class AntiTankDisguise extends AbstractCharacter {
    // Fields
    private int disguiseLeft;
    private static final int DISGUISE_LENGTH = 30;

    // Constructors
    public AntiTankDisguise() {
	super(true, 0);
	this.disguiseLeft = AntiTankDisguise.DISGUISE_LENGTH;
	this.activateTimer(1);
	this.setDirection(DirectionConstants.NORTH);
	this.setFrameNumber(1);
    }

    public AntiTankDisguise(final int dir, final boolean useTimer, final int instance) {
	super(useTimer, instance);
	if (useTimer) {
	    this.disguiseLeft = AntiTankDisguise.DISGUISE_LENGTH;
	    this.activateTimer(1);
	}
	this.setDirection(dir);
	this.setFrameNumber(1);
    }

    @Override
    public boolean isDirectional() {
	return true;
    }

    @Override
    public final int getStringBaseID() {
	return 0;
    }

    @Override
    public boolean acceptTick(final int actionType) {
	return actionType == ActionConstants.ACTION_MOVE;
    }

    @Override
    public void timerExpiredAction(final int locX, final int locY) {
	this.disguiseLeft--;
	if (this.disguiseLeft == 0) {
	    SoundManager.playSound(SoundConstants.SOUND_DISRUPT_END);
	    LTRemix.getApplication().getGameManager().setNormalTank();
	} else {
	    this.activateTimer(1);
	}
    }
}