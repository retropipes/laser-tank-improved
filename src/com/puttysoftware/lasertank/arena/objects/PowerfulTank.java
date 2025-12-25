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

public class PowerfulTank extends AbstractCharacter {
    public PowerfulTank(final Direction dir, final int number) {
	super(number);
	this.setDirection(dir);
	this.activateTimer(50);
    }

    // Constructors
    public PowerfulTank(final int number) {
	super(number);
	this.activateTimer(50);
    }

    @Override
    public boolean acceptTick(final int actionType) {
	return actionType == ActionConstants.ACTION_MOVE;
    }

    @Override
    public final int getStringBaseID() {
	return 138;
    }

    @Override
    public void timerExpiredAction(final int x, final int y) {
	SoundManager.playSound(SoundConstants.SOUND_DISRUPT_END);
	LaserTank.getApplication().getGameManager().setNormalTank();
    }
}