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
import com.puttysoftware.ltremix.utilities.ArenaConstants;
import com.puttysoftware.ltremix.utilities.DirectionConstants;

public class Crumbling extends AbstractAttribute {
    // Constructors
    public Crumbling() {
	super();
    }

    @Override
    public final int getStringBaseID() {
	return 132;
    }

    @Override
    public int laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	final Application app = LTRemix.getApplication();
	app.getGameManager().morph(new Empty(), locX, locY, locZ, this.getPrimaryLayer());
	// Destroy whatever we were attached to
	app.getGameManager().morph(new Empty(), locX, locY, locZ, ArenaConstants.LAYER_LOWER_OBJECTS);
	SoundManager.playSound(SoundConstants.SOUND_CRACK);
	return DirectionConstants.NONE;
    }

    @Override
    public void moveFailedAction(final int locX, final int locY, final int locZ) {
	final Application app = LTRemix.getApplication();
	app.getGameManager().morph(new Empty(), locX, locY, locZ, this.getPrimaryLayer());
	// Destroy whatever we were attached to
	app.getGameManager().morph(new Empty(), locX, locY, locZ, ArenaConstants.LAYER_LOWER_OBJECTS);
	SoundManager.playSound(SoundConstants.SOUND_CRACK);
    }
}