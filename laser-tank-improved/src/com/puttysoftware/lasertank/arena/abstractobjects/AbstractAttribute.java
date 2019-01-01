/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.abstractobjects;

import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public abstract class AbstractAttribute extends AbstractPassThroughObject {
    // Constructors
    protected AbstractAttribute() {
	super();
	this.type.set(TypeConstants.TYPE_ATTRIBUTE);
    }

    @Override
    public int getLayer() {
	return ArenaConstants.LAYER_UPPER_OBJECTS;
    }
}