/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractCharacter;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.ActionConstants;
import com.puttysoftware.lasertank.utilities.Direction;

public class AntiTankDisguise extends AbstractCharacter {
    private static final int DISGUISE_LENGTH = 30;
    // Fields
    private int disguiseLeft;

    public AntiTankDisguise(final Direction dir, final int number) {
	super(number);
	this.disguiseLeft = AntiTankDisguise.DISGUISE_LENGTH;
	this.activateTimer(1);
	this.setDirection(dir);
	this.setFrameNumber(1);
    }

    // Constructors
    public AntiTankDisguise(final int number) {
	super(number);
	this.disguiseLeft = AntiTankDisguise.DISGUISE_LENGTH;
	this.activateTimer(1);
	this.setDirection(Direction.NORTH);
	this.setFrameNumber(1);
    }

    @Override
    public boolean acceptTick(final int actionType) {
	return actionType == ActionConstants.ACTION_MOVE;
    }

    @Override
    public final int getStringBaseID() {
	return 0;
    }

    @Override
    public void timerExpiredAction(final int locX, final int locY) {
	this.disguiseLeft--;
	if (this.disguiseLeft == 0) {
	    SoundManager.playSound(SoundConstants.SOUND_DISRUPT_END);
	    LaserTank.getApplication().getGameManager().setNormalTank();
	} else {
	    this.activateTimer(1);
	}
    }
}