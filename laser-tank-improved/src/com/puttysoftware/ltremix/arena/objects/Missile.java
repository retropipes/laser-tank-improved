/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractTransientObject;

public class Missile extends AbstractTransientObject {
    // Constructors
    public Missile() {
	super();
    }

    @Override
    public final int getStringBaseID() {
	return 27;
    }

    @Override
    public int getForceUnitsImbued() {
	return 2;
    }

    @Override
    public boolean isDirectional() {
	return true;
    }
}
