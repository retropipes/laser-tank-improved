/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractInventoryModifier;
import com.puttysoftware.ltremix.game.GameManager;
import com.puttysoftware.ltremix.utilities.TankInventory;

public class TenHeatBombs extends AbstractInventoryModifier {
    // Constructors
    public TenHeatBombs() {
	super();
    }

    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	final GameManager gm = LTRemix.getApplication().getGameManager();
	TankInventory.addTenHeatBombs();
	gm.morph(new Empty(), dirX, dirY, dirZ, this.getPrimaryLayer());
    }

    @Override
    public boolean doLasersPassThrough() {
	return true;
    }

    @Override
    public final int getStringBaseID() {
	return 54;
    }
}
