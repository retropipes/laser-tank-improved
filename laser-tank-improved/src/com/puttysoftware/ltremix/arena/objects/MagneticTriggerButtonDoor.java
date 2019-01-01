/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractTriggerButtonDoor;
import com.puttysoftware.ltremix.utilities.MaterialConstants;

public class MagneticTriggerButtonDoor extends AbstractTriggerButtonDoor {
    // Constructors
    public MagneticTriggerButtonDoor() {
	super();
	this.setMaterial(MaterialConstants.MATERIAL_MAGNETIC);
    }

    @Override
    public final int getStringBaseID() {
	return 91;
    }
}