/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractInventoryModifier;
import com.puttysoftware.lasertank.game.GameManager;
import com.puttysoftware.lasertank.utilities.TankInventory;

public class TenMagnets extends AbstractInventoryModifier {
    // Constructors
    public TenMagnets() {
	super();
    }

    @Override
    public boolean doLasersPassThrough() {
	return true;
    }

    @Override
    public final int getStringBaseID() {
	return 40;
    }

    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	final GameManager gm = LaserTank.getApplication().getGameManager();
	TankInventory.addTenMagnets();
	gm.morph(new Empty(), dirX, dirY, dirZ, this.getLayer());
    }
}
