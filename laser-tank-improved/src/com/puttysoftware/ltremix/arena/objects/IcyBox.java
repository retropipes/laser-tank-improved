/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.MaterialConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class IcyBox extends AbstractMovableObject {
    // Constructors
    public IcyBox() {
	super(true);
	this.type.set(TypeConstants.TYPE_BOX);
	this.type.set(TypeConstants.TYPE_ICY);
	this.setMaterial(MaterialConstants.MATERIAL_ICE);
    }

    @Override
    public void playSoundHook() {
	SoundManager.playSound(SoundConstants.SOUND_PUSH_BOX);
    }

    @Override
    public final int getStringBaseID() {
	return 21;
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_FIRE:
	    if (this.hasPreviousState()) {
		return this.getPreviousState();
	    } else {
		return new Box();
	    }
	default:
	    return this;
	}
    }
}