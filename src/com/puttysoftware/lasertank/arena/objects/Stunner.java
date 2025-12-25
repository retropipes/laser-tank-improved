/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.arena.abstractobjects.AbstractTransientObject;

public class Stunner extends AbstractTransientObject {
    // Constructors
    public Stunner() {
	super();
    }

    @Override
    public int getForceUnitsImbued() {
	return 0;
    }

    @Override
    public final int getStringBaseID() {
	return 35;
    }
}
