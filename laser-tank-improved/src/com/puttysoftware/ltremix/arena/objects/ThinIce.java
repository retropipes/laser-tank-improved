/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractGround;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.MaterialConstants;

public class ThinIce extends AbstractGround {
    // Constructors
    public ThinIce() {
	super(false);
	this.setMaterial(MaterialConstants.MATERIAL_ICE);
    }

    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	SoundManager.playSound(SoundConstants.SOUND_PUSH_MIRROR);
	LTRemix.getApplication().getGameManager().remoteDelayedDecayTo(new Water());
    }

    @Override
    public boolean pushIntoAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	LTRemix.getApplication().getGameManager().remoteDelayedDecayTo(new Water());
	return true;
    }

    @Override
    public final int getStringBaseID() {
	return 43;
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_ICE:
	    final Ice i = new Ice();
	    i.setPreviousState(this);
	    return i;
	case MaterialConstants.MATERIAL_FIRE:
	    return new Water();
	default:
	    return this;
	}
    }
}