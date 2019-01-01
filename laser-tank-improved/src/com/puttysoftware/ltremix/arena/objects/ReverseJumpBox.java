/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import java.awt.Color;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractJumpObject;
import com.puttysoftware.ltremix.utilities.MaterialConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class ReverseJumpBox extends AbstractJumpObject {
    // Constructors
    public ReverseJumpBox() {
	super();
	this.type.set(TypeConstants.TYPE_BOX);
	this.setMaterial(MaterialConstants.MATERIAL_STONE);
    }

    @Override
    public int getActualJumpRows() {
	return -super.getActualJumpRows();
    }

    @Override
    public int getActualJumpCols() {
	return -super.getActualJumpCols();
    }

    @Override
    public final int getStringBaseID() {
	return 124;
    }

    @Override
    public final Color getCustomTextColor() {
	return Color.black;
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_ICE:
	    final IcyBox ib = new IcyBox();
	    ib.setPreviousState(this);
	    return ib;
	case MaterialConstants.MATERIAL_FIRE:
	    return new HotBox();
	default:
	    return this;
	}
    }
}