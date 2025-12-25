/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractAttribute;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.Direction;

public class Damaged extends AbstractAttribute {
    // Constructors
    public Damaged() {
	super();
    }

    @Override
    public final int getStringBaseID() {
	return 133;
    }

    @Override
    public Direction laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	final Application app = LaserTank.getApplication();
	app.getGameManager().morph(new Crumbling(), locX, locY, locZ, this.getLayer());
	SoundManager.playSound(SoundConstants.SOUND_CRACK);
	return Direction.NONE;
    }

    @Override
    public void moveFailedAction(final int locX, final int locY, final int locZ) {
	final Application app = LaserTank.getApplication();
	app.getGameManager().morph(new Crumbling(), locX, locY, locZ, this.getLayer());
	SoundManager.playSound(SoundConstants.SOUND_CRACK);
    }
}