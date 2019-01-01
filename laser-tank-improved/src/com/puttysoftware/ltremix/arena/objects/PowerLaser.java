/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractTransientObject;

public class PowerLaser extends AbstractTransientObject {
    // Constructors
    public PowerLaser() {
	super();
    }

    @Override
    public final int getStringBaseID() {
	return 137;
    }

    @Override
    public int getForceUnitsImbued() {
	return 5;
    }

    @Override
    public boolean isDirectional() {
	return true;
    }
}
