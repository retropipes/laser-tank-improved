/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractTransientObject;

public class Disruptor extends AbstractTransientObject {
    // Constructors
    public Disruptor() {
	super();
    }

    @Override
    public final int getStringBaseID() {
	return 47;
    }

    @Override
    public int getForceUnitsImbued() {
	return 0;
    }
}
