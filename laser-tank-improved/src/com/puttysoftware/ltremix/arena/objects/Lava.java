/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractGround;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.MaterialConstants;

public class Lava extends AbstractGround {
    // Constructors
    public Lava() {
	super();
	this.setMaterial(MaterialConstants.MATERIAL_FIRE);
    }

    // Scriptability
    @Override
    public boolean pushIntoAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	final Application app = LTRemix.getApplication();
	if (pushed instanceof IcyBox) {
	    app.getGameManager().morph(new Ground(), x, y, z, this.getPrimaryLayer());
	    SoundManager.playSound(SoundConstants.SOUND_COOL_OFF);
	    return true;
	} else {
	    app.getGameManager().morph(new Empty(), x, y, z, pushed.getPrimaryLayer());
	    SoundManager.playSound(SoundConstants.SOUND_MELT);
	    return false;
	}
    }

    @Override
    public boolean killsOnMove() {
	return true;
    }

    @Override
    public final int getStringBaseID() {
	return 62;
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
}
