/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.arena.abstractobjects.AbstractPassThroughObject;
import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public class UpperGroundEmpty extends AbstractPassThroughObject {
    // Constructors
    public UpperGroundEmpty() {
	super();
	this.type.set(TypeConstants.TYPE_EMPTY_SPACE);
    }

    @Override
    public int getLayer() {
	return ArenaConstants.LAYER_UPPER_GROUND;
    }

    @Override
    public final int getStringBaseID() {
	return 130;
    }
}