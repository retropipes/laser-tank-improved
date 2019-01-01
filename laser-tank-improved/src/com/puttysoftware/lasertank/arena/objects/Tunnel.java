/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.objects;

import com.puttysoftware.lasertank.arena.abstractobjects.AbstractTunnel;
import com.puttysoftware.lasertank.utilities.ColorConstants;

public class Tunnel extends AbstractTunnel {
    // Constructors
    public Tunnel() {
	super();
	this.setColor(ColorConstants.COLOR_GRAY);
    }

    public Tunnel(final int color) {
	super();
	this.setColor(color);
    }

    @Override
    public final int getStringBaseID() {
	return 44;
    }
}