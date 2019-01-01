/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractAllButton;
import com.puttysoftware.ltremix.utilities.MaterialConstants;

public class PlasticAllButton extends AbstractAllButton {
    // Constructors
    public PlasticAllButton() {
	super(new PlasticAllButtonDoor(), false);
	this.setMaterial(MaterialConstants.MATERIAL_PLASTIC);
    }

    @Override
    public final int getStringBaseID() {
	return 98;
    }
}