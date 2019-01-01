/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

import com.puttysoftware.ltremix.utilities.ArenaConstants;
import com.puttysoftware.ltremix.utilities.MaterialConstants;

public abstract class AbstractInventoryModifier extends AbstractGround {
    // Constructors
    protected AbstractInventoryModifier() {
	super();
	this.setMaterial(MaterialConstants.MATERIAL_NOT_APPLICABLE);
    }

    @Override
    public int getPrimaryLayer() {
	return ArenaConstants.LAYER_LOWER_OBJECTS;
    }
}
