/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractDisruptedObject;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.MaterialConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class DisruptedMagneticWall extends AbstractDisruptedObject {
    // Fields
    private int disruptionLeft;
    private static final int DISRUPTION_START = 20;

    // Constructors
    public DisruptedMagneticWall() {
	super();
	this.type.set(TypeConstants.TYPE_PLAIN_WALL);
	this.disruptionLeft = DisruptedMagneticWall.DISRUPTION_START;
	this.activateTimer(1);
	this.setMaterial(MaterialConstants.MATERIAL_MAGNETIC);
    }

    @Override
    public void timerExpiredAction(final int locX, final int locY) {
	this.disruptionLeft--;
	if (this.disruptionLeft == 0) {
	    SoundManager.playSound(SoundConstants.SOUND_DISRUPT_END);
	    final int z = LTRemix.getApplication().getGameManager().getPlayerManager().getPlayerLocationZ();
	    LTRemix.getApplication().getGameManager().morph(new MagneticWall(), locX, locY, z, this.getPrimaryLayer());
	} else {
	    this.activateTimer(1);
	}
    }

    @Override
    public final int getStringBaseID() {
	return 50;
    }
}