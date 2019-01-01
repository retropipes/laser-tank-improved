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
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class Acid extends AbstractGround {
    // Constructors
    public Acid() {
	super();
	this.setFrameNumber(1);
	this.setFrameCount(4);
	this.setMaterial(MaterialConstants.MATERIAL_WOODEN);
    }

    // Scriptability
    @Override
    public boolean pushIntoAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	final Application app = LTRemix.getApplication();
	// Get rid of pushed object
	app.getGameManager().morph(new Empty(), x, y, z, pushed.getPrimaryLayer());
	if (pushed.isOfType(TypeConstants.TYPE_BOX) && pushed.getMaterial() == MaterialConstants.MATERIAL_METALLIC) {
	    app.getGameManager().morph(new Ground(), x, y, z, this.getPrimaryLayer());
	}
	SoundManager.playSound(SoundConstants.SOUND_COOL_OFF);
	return false;
    }

    @Override
    public boolean killsOnMove() {
	return true;
    }

    @Override
    public final int getStringBaseID() {
	return 146;
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_ICE:
	    final Ice i = new Ice();
	    i.setPreviousState(this);
	    return i;
	case MaterialConstants.MATERIAL_FIRE:
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
