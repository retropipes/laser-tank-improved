/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractMover;
import com.puttysoftware.ltremix.utilities.DirectionConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class MirrorMover extends AbstractMover {
    // Constructors
    public MirrorMover() {
	super(true);
	this.setDirection(DirectionConstants.NORTH);
	this.setFrameNumber(1);
	this.type.set(TypeConstants.TYPE_MIRROR_MOVER);
    }

    @Override
    public final int getStringBaseID() {
	return 125;
    }

    @Override
    public boolean isDirectional() {
	return true;
    }
}