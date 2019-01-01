/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.abstractobjects;

import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.MaterialConstants;

public abstract class AbstractInventoryModifier extends AbstractGround {
    // Constructors
    protected AbstractInventoryModifier() {
	super();
	this.setMaterial(MaterialConstants.MATERIAL_NOT_APPLICABLE);
    }

    @Override
    public int getLayer() {
	return ArenaConstants.LAYER_LOWER_OBJECTS;
    }
}
