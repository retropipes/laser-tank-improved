/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public class StunnedAntiTank extends AbstractMovableObject {
    private static final int STUNNED_START = 10;
    // Fields
    private int stunnedLeft;

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
    public final int getStringBaseID() {
	return 34;
    }

    @Override
    public void playSoundHook() {
	SoundManager.playSound(SoundConstants.SOUND_PUSH_ANTI_TANK);
    }

    @Override
    public void timerExpiredAction(final int locX, final int locY) {
	this.stunnedLeft--;
	if (this.stunnedLeft == 1) {
	    SoundManager.playSound(SoundConstants.SOUND_STUN_OFF);
	    this.activateTimer(1);
	} else if (this.stunnedLeft == 0) {
	    final int z = LaserTank.getApplication().getGameManager().getPlayerManager().getPlayerLocationZ();
	    final AntiTank at = new AntiTank();
	    at.setSavedObject(this.getSavedObject());
	    at.setDirection(this.getDirection());
	    LaserTank.getApplication().getGameManager().morph(at, locX, locY, z, this.getLayer());
	} else {
	    SoundManager.playSound(SoundConstants.SOUND_STUNNED);
	    this.activateTimer(1);
	}
    }
}
