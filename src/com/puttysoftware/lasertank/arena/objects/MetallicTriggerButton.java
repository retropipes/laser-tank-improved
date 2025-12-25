/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.arena.abstractobjects.AbstractTriggerButton;
import com.puttysoftware.lasertank.utilities.MaterialConstants;

public class MetallicTriggerButton extends AbstractTriggerButton {
    // Constructors
    public MetallicTriggerButton() {
	super(new MetallicTriggerButtonDoor(), false);
	this.setMaterial(MaterialConstants.MATERIAL_METALLIC);
    }

    @Override
    public final int getStringBaseID() {
	return 96;
    }
}