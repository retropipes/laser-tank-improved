/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.arena.abstractobjects.AbstractPressureButton;
import com.puttysoftware.lasertank.utilities.MaterialConstants;

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