/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.arena.abstractobjects.AbstractPressureButtonDoor;
import com.puttysoftware.lasertank.utilities.MaterialConstants;

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