/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractPressureButton;
import com.puttysoftware.ltremix.utilities.MaterialConstants;

public class PlasticPressureButton extends AbstractPressureButton {
    // Constructors
    public PlasticPressureButton() {
	super(new PlasticPressureButtonDoor(), false);
	this.setMaterial(MaterialConstants.MATERIAL_PLASTIC);
    }

    @Override
    public final int getStringBaseID() {
	return 100;
    }
}