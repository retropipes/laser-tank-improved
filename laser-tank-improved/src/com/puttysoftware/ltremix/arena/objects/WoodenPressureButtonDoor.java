/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractPressureButtonDoor;
import com.puttysoftware.ltremix.utilities.MaterialConstants;

public class WoodenPressureButtonDoor extends AbstractPressureButtonDoor {
    // Constructors
    public WoodenPressureButtonDoor() {
	super();
	this.setMaterial(MaterialConstants.MATERIAL_WOODEN);
    }

    @Override
    public final int getStringBaseID() {
	return 119;
    }
}