/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class StunnedAntiTank extends AbstractMovableObject {
    // Fields
    private int stunnedLeft;
    private static final int STUNNED_START = 10;

    // Constructors
    public StunnedAntiTank() {
	super(true);
	this.activateTimer(1);
	this.stunnedLeft = StunnedAntiTank.STUNNED_START;
	this.type.set(TypeConstants.TYPE_ANTI);
    }

    @Override
    public AbstractArenaObject clone() {
	final StunnedAntiTank copy = (StunnedAntiTank) super.clone();
	copy.stunnedLeft = this.stunnedLeft;
	return copy;
    }

    @Override
    public void timerExpiredAction(final int locX, final int locY) {
	this.stunnedLeft--;
	if (this.stunnedLeft == 1) {
	    SoundManager.playSound(SoundConstants.SOUND_STUN_OFF);
	    this.activateTimer(1);
	} else if (this.stunnedLeft == 0) {
	    final int z = LTRemix.getApplication().getGameManager().getPlayerManager().getPlayerLocationZ();
	    final AntiTank at = new AntiTank();
	    at.setSavedObject(this.getSavedObject());
	    at.setDirection(this.getDirection());
	    LTRemix.getApplication().getGameManager().morph(at, locX, locY, z, this.getPrimaryLayer());
	} else {
	    SoundManager.playSound(SoundConstants.SOUND_STUNNED);
	    this.activateTimer(1);
	}
    }

    @Override
    public void playSoundHook() {
	SoundManager.playSound(SoundConstants.SOUND_PUSH_ANTI_TANK);
    }

    @Override
    public final int getStringBaseID() {
	return 34;
    }

    @Override
    public boolean isDirectional() {
	return true;
    }
}
