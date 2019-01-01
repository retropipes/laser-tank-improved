/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractAttribute;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.DirectionConstants;

public class Cracked extends AbstractAttribute {
    // Constructors
    public Cracked() {
	super();
    }

    @Override
    public final int getStringBaseID() {
	return 131;
    }

    @Override
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	final Application app = LTRemix.getApplication();
	app.getGameManager().morph(new Damaged(), locX, locY, locZ, this.getPrimaryLayer());
	SoundManager.playSound(SoundConstants.SOUND_CRACK);
	return DirectionConstants.NONE;
    }

    @Override
    public void moveFailedAction(final int locX, final int locY, final int locZ) {
	final Application app = LTRemix.getApplication();
	app.getGameManager().morph(new Damaged(), locX, locY, locZ, this.getPrimaryLayer());
	SoundManager.playSound(SoundConstants.SOUND_CRACK);
    }
}