/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractTriggerButton;
import com.puttysoftware.ltremix.utilities.MaterialConstants;

public class UniversalTriggerButton extends AbstractTriggerButton {
    // Constructors
    public UniversalTriggerButton() {
	super(new UniversalTriggerButtonDoor(), true);
	this.setMaterial(MaterialConstants.MATERIAL_DEFAULT);
    }

    @Override
    public final int getStringBaseID() {
	return 114;
    }
}