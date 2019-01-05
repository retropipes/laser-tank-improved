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

public class Lava extends AbstractGround {
    // Constructors
    public Lava() {
	super();
	this.setMaterial(MaterialConstants.MATERIAL_FIRE);
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_ICE:
	    return new Ground();
	default:
	    return this;
	}
    }

    @Override
    public int getBlockHeight() {
	return -1;
    }

    @Override
    public final int getStringBaseID() {
	return 62;
    }

    @Override
    public boolean killsOnMove() {
	return true;
    }

    // Scriptability
    @Override
    public boolean pushIntoAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	final Application app = LaserTank.getApplication();
	if (pushed instanceof IcyBox) {
	    app.getGameManager().morph(new Ground(), x, y, z, this.getLayer());
	    SoundManager.playSound(SoundConstants.SOUND_COOL_OFF);
	    return true;
	} else {
	    app.getGameManager().morph(new Empty(), x, y, z, pushed.getLayer());
	    SoundManager.playSound(SoundConstants.SOUND_MELT);
	    return false;
	}
    }
}
