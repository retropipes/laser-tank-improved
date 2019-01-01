/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractAllButton;
import com.puttysoftware.ltremix.utilities.MaterialConstants;

public class IceAllButton extends AbstractAllButton {
    // Constructors
    public IceAllButton() {
	super(new IceAllButtonDoor(), false);
	this.setMaterial(MaterialConstants.MATERIAL_ICE);
    }

    @Override
    public final int getStringBaseID() {
	return 80;
    }
}