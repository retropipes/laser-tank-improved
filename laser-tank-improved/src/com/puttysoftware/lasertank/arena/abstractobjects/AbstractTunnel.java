/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.abstractobjects;

import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.objects.Tunnel;
import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.ColorConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public abstract class AbstractTunnel extends AbstractArenaObject {
    // Fields
    private final static boolean[] tunnelsFull = new boolean[ColorConstants.COLOR_COUNT];
    private final static int SCAN_RADIUS = 24;

    // Static methods
    public static void checkTunnels() {
	for (int x = 0; x < ColorConstants.COLOR_COUNT; x++) {
	    AbstractTunnel.checkTunnelsOfColor(x);
	}
    }

    private static void checkTunnelsOfColor(final int color) {
	final Application app = LaserTank.getApplication();
	final int tx = app.getGameManager().getPlayerManager().getPlayerLocationX();
	final int ty = app.getGameManager().getPlayerManager().getPlayerLocationY();
	final int[] pgrmdest = app.getArenaManager().getArena().circularScanTunnel(0, 0, 0, AbstractTunnel.SCAN_RADIUS,
		tx, ty, AbstractTunnel.getTunnelOfColor(color), false);
	if (pgrmdest != null) {
	    AbstractTunnel.tunnelsFull[color] = false;
	} else {
	    AbstractTunnel.tunnelsFull[color] = true;
	}
    }

    private static AbstractTunnel getTunnelOfColor(final int color) {
	return new Tunnel(color);
    }

    public static boolean tunnelsFull(final int color) {
	return AbstractTunnel.tunnelsFull[color];
    }

    // Constructors
    protected AbstractTunnel() {
	super(false, false, true);
	this.type.set(TypeConstants.TYPE_TUNNEL);
    }

    @Override
    public int getCustomProperty(final int propID) {
	return AbstractArenaObject.DEFAULT_CUSTOM_VALUE;
    }

    @Override
    public int getLayer() {
	return ArenaConstants.LAYER_LOWER_OBJECTS;
    }

    // Scriptability
    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	final Application app = LaserTank.getApplication();
	final int tx = app.getGameManager().getPlayerManager().getPlayerLocationX();
	final int ty = app.getGameManager().getPlayerManager().getPlayerLocationY();
	final int[] pgrmdest = app.getArenaManager().getArena().circularScanTunnel(dirX, dirY, dirZ,
		AbstractTunnel.SCAN_RADIUS, tx, ty, AbstractTunnel.getTunnelOfColor(this.getColor()), true);
	if (pgrmdest != null) {
	    app.getGameManager().updatePositionAbsoluteNoEvents(pgrmdest[0], pgrmdest[1], pgrmdest[2]);
	}
    }

    @Override
    public boolean pushIntoAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	final Application app = LaserTank.getApplication();
	final int tx = app.getGameManager().getPlayerManager().getPlayerLocationX();
	final int ty = app.getGameManager().getPlayerManager().getPlayerLocationY();
	final int color = this.getColor();
	final int[] pgrmdest = app.getArenaManager().getArena().circularScanTunnel(x, y, z, AbstractTunnel.SCAN_RADIUS,
		tx, ty, AbstractTunnel.getTunnelOfColor(this.getColor()), false);
	if (pgrmdest != null) {
	    AbstractTunnel.tunnelsFull[color] = false;
	    app.getGameManager().updatePushedIntoPositionAbsolute(pgrmdest[0], pgrmdest[1], pgrmdest[2], x, y, z,
		    pushed, this);
	} else {
	    AbstractTunnel.tunnelsFull[color] = true;
	    pushed.setWaitingOnTunnel(true);
	}
	return false;
    }

    @Override
    public void setCustomProperty(final int propID, final int value) {
	// Do nothing
    }
}
