/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractCharacter;

public class FrozenTank extends AbstractCharacter {
    // Constructors
    public FrozenTank() {
	super(true, 0);
    }

    public FrozenTank(final int dir, final int instance) {
	super(true, instance);
	this.setDirection(dir);
    }

    @Override
    public final int getStringBaseID() {
	return 15;
    }

    @Override
    public boolean isDirectional() {
	return true;
    }
}