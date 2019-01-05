/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.arena.abstractobjects.AbstractCharacter;
import com.puttysoftware.lasertank.utilities.Direction;

public class FrozenTank extends AbstractCharacter {
    public FrozenTank(final Direction dir, final int number) {
	super(number);
	this.setDirection(dir);
    }

    // Constructors
    public FrozenTank(final int number) {
	super(number);
    }

    @Override
    public final int getStringBaseID() {
	return 15;
    }
}