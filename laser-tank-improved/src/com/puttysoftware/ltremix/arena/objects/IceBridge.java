/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractGround;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.MaterialConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class IceBridge extends AbstractGround {
    // Constructors
    public IceBridge() {
	super(false);
	this.setMaterial(MaterialConstants.MATERIAL_ICE);
	this.type.set(TypeConstants.TYPE_ICY);
    }

    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	SoundManager.playSound(SoundConstants.SOUND_PUSH_MIRROR);
    }

    @Override
    public final int getStringBaseID() {
	return 71;
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_FIRE:
	    if (this.hasPreviousState()) {
		return this.getPreviousState();
	    } else {
		return new Bridge();
	    }
	default:
	    return this;
	}
    }

    @Override
    public boolean pushIntoAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	if (pushed instanceof HotBox) {
	    pushed.setSavedObject(new Bridge());
	}
	return true;
    }
}