/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public class Ball extends AbstractMovableObject {
    // Constructors
    public Ball() {
	super(true);
	this.type.set(TypeConstants.TYPE_BALL);
	this.type.set(TypeConstants.TYPE_ICY);
    }

    @Override
    public final int getStringBaseID() {
	return 2;
    }

    @Override
    public void playSoundHook() {
	SoundManager.playSound(SoundConstants.SOUND_BALL_ROLL);
    }
}