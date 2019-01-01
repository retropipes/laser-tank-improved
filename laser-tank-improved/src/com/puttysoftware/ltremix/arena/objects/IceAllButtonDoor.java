/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractAllButtonDoor;
import com.puttysoftware.ltremix.utilities.MaterialConstants;

public class IceAllButtonDoor extends AbstractAllButtonDoor {
    // Constructors
    public IceAllButtonDoor() {
	super();
	this.setMaterial(MaterialConstants.MATERIAL_ICE);
    }

    @Override
    public final int getStringBaseID() {
	return 81;
    }
}