/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractTransientObject;

public class BlueLaser extends AbstractTransientObject {
    // Constructors
    public BlueLaser() {
	super();
    }

    @Override
    public final int getStringBaseID() {
	return 6;
    }

    @Override
    public int getForceUnitsImbued() {
	return 1;
    }

    @Override
    public boolean isDirectional() {
	return true;
    }
}
