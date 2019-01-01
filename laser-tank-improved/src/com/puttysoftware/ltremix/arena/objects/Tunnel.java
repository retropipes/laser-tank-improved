/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractTunnel;
import com.puttysoftware.ltremix.utilities.ColorConstants;

public class Tunnel extends AbstractTunnel {
    // Constructors
    public Tunnel() {
	super();
	this.setColor(ColorConstants.COLOR_GRAY);
    }

    @Override
    public final int getStringBaseID() {
	return 44;
    }
}