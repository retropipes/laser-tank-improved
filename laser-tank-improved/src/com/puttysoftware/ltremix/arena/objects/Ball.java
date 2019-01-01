/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class Ball extends AbstractMovableObject {
    // Constructors
    public Ball() {
	super(true);
	this.type.set(TypeConstants.TYPE_BALL);
	this.type.set(TypeConstants.TYPE_ICY);
    }

    @Override
    public void playSoundHook() {
	SoundManager.playSound(SoundConstants.SOUND_BALL_ROLL);
    }

    @Override
    public final int getStringBaseID() {
	return 2;
    }
}