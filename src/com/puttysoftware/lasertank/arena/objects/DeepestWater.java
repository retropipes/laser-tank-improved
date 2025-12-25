/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractGround;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.MaterialConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public class DeepestWater extends AbstractGround {
    // Constructors
    public DeepestWater() {
	super();
	this.setFrameNumber(1);
	this.setMaterial(MaterialConstants.MATERIAL_WOODEN);
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_ICE:
	    final Ice i = new Ice();
	    i.setPreviousState(this);
	    return i;
	case MaterialConstants.MATERIAL_FIRE:
	    return new DeeperWater();
	default:
	    return this;
	}
    }

    @Override
    public int getBlockHeight() {
	return -4;
    }

    @Override
    public final int getStringBaseID() {
	return 69;
    }

    @Override
    public boolean killsOnMove() {
	return true;
    }

    // Scriptability
    @Override
    public boolean pushIntoAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	final Application app = LaserTank.getApplication();
	// Get rid of pushed object
	app.getGameManager().morph(new Empty(), x, y, z, pushed.getLayer());
	if (pushed.isOfType(TypeConstants.TYPE_BOX)) {
	    if (pushed.getMaterial() == MaterialConstants.MATERIAL_WOODEN) {
		app.getGameManager().morph(new Bridge(), x, y, z, this.getLayer());
	    } else {
		app.getGameManager().morph(new DeeperWater(), x, y, z, this.getLayer());
	    }
	}
	SoundManager.playSound(SoundConstants.SOUND_SINK);
	return false;
    }
}
