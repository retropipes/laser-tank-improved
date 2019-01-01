/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

import com.puttysoftware.ltremix.utilities.ArenaConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public abstract class AbstractAttribute extends AbstractPassThroughObject {
    // Constructors
    protected AbstractAttribute() {
	super();
	this.type.set(TypeConstants.TYPE_ATTRIBUTE);
    }

    @Override
    public int getPrimaryLayer() {
	return ArenaConstants.LAYER_UPPER_OBJECTS;
    }
}