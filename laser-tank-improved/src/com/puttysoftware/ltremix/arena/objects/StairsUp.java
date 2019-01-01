/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractTeleport;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;

public class StairsUp extends AbstractTeleport {
    // Constructors
    public StairsUp() {
	super();
    }

    @Override
    public int getDestinationFloor() {
	final Application app = LTRemix.getApplication();
	return app.getGameManager().getPlayerManager().getPlayerLocationZ() + 1;
    }

    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	final Application app = LTRemix.getApplication();
	app.getGameManager().updatePositionAbsoluteNoEvents(this.getDestinationFloor());
	SoundManager.playSound(SoundConstants.SOUND_UP);
    }

    @Override
    public final int getStringBaseID() {
	return 33;
    }
}
