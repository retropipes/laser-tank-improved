/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractGround;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.MaterialConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public class Ice extends AbstractGround {
    public Ice() {
	super(false);
	this.setMaterial(MaterialConstants.MATERIAL_ICE);
	this.type.set(TypeConstants.TYPE_ICY);
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_FIRE:
	    if (this.hasPreviousState()) {
		return this.getPreviousState();
	    } else {
		return new Ground();
	    }
	default:
	    return this;
	}
    }

    @Override
    public final int getStringBaseID() {
	return 20;
    }

    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	SoundManager.playSound(SoundConstants.SOUND_PUSH_MIRROR);
    }

    @Override
    public boolean pushIntoAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	if (pushed instanceof HotBox) {
	    final Ground g = new Ground();
	    LaserTank.getApplication().getGameManager().morph(g, x, y, z, g.getLayer());
	    SoundManager.playSound(SoundConstants.SOUND_DEFROST);
	}
	return true;
    }
}
