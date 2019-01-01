/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractPassThroughObject;
import com.puttysoftware.ltremix.utilities.ArenaConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class Empty extends AbstractPassThroughObject {
    // Constructors
    public Empty() {
	super();
	this.type.set(TypeConstants.TYPE_EMPTY_SPACE);
    }

    @Override
    public int[] getSecondaryLayers() {
	return new int[] { ArenaConstants.LAYER_UPPER_GROUND, ArenaConstants.LAYER_UPPER_OBJECTS };
    }

    @Override
    public final int getStringBaseID() {
	return 130;
    }
}