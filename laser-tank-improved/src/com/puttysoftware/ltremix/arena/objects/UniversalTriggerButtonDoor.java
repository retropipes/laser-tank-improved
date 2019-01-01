/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractTriggerButtonDoor;
import com.puttysoftware.ltremix.utilities.MaterialConstants;

public class UniversalTriggerButtonDoor extends AbstractTriggerButtonDoor {
    // Constructors
    public UniversalTriggerButtonDoor() {
	super();
	this.setMaterial(MaterialConstants.MATERIAL_DEFAULT);
    }

    @Override
    public final int getStringBaseID() {
	return 115;
    }
}