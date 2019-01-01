/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractPressureButton;
import com.puttysoftware.ltremix.utilities.MaterialConstants;

public class FirePressureButton extends AbstractPressureButton {
    // Constructors
    public FirePressureButton() {
	super(new FirePressureButtonDoor(), false);
	this.setMaterial(MaterialConstants.MATERIAL_FIRE);
    }

    @Override
    public final int getStringBaseID() {
	return 76;
    }
}