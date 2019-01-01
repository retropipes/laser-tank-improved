/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractTriggerButtonDoor;
import com.puttysoftware.ltremix.utilities.MaterialConstants;

public class WoodenTriggerButtonDoor extends AbstractTriggerButtonDoor {
    // Constructors
    public WoodenTriggerButtonDoor() {
	super();
	this.setMaterial(MaterialConstants.MATERIAL_WOODEN);
    }

    @Override
    public final int getStringBaseID() {
	return 121;
    }
}