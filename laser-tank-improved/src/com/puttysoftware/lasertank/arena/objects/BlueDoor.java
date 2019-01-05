/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractDoor;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.utilities.TankInventory;

public class BlueDoor extends AbstractDoor {
    // Constructors
    public BlueDoor() {
	super(new BlueKey());
    }

    @Override
    public final int getStringBaseID() {
	return 4;
    }

    // Scriptability
    @Override
    public boolean isConditionallySolid() {
	return TankInventory.getBlueKeysLeft() < 1;
    }

    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	SoundManager.playSound(SoundConstants.SOUND_UNLOCK);
	TankInventory.useBlueKey();
	LaserTank.getApplication().getGameManager().morph(new Empty(), dirX, dirY, dirZ, this.getLayer());
    }
}