/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.abstractobjects;

import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;
import com.puttysoftware.ltremix.utilities.ArenaConstants;
import com.puttysoftware.ltremix.utilities.ColorConstants;
import com.puttysoftware.ltremix.utilities.ColorResolver;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public abstract class AbstractTunnel extends AbstractArenaObject {
    // Fields
    private final static boolean[] tunnelsFull = new boolean[ColorConstants.COLOR_COUNT];

    // Constructors
    protected AbstractTunnel() {
	super(false, false, true);
	this.type.set(TypeConstants.TYPE_TUNNEL);
    }

    // Static methods
    public static void checkTunnels() {
	for (int x = 0; x < ColorConstants.COLOR_COUNT; x++) {
	    AbstractTunnel.checkTunnelsOfColor(x);
	}
    }

    public static boolean tunnelsFull(final int color) {
	return AbstractTunnel.tunnelsFull[color];
    }

    private static void checkTunnelsOfColor(final int color) {
	final Application app = LTRemix.getApplication();
	final String targetName = ColorResolver.resolveColorConstantToName(color) + StringConstants.COMMON_STRING_SPACE
		+ StringLoader.loadString(StringConstants.GENERIC_STRINGS_FILE, StringConstants.GENERIC_STRING_TUNNEL);
	final int[] pgrmdest = app.getArenaManager().getArena().tunnelScan(0, 0, 0, targetName);
	if (pgrmdest != null) {
	    AbstractTunnel.tunnelsFull[color] = false;
	} else {
	    AbstractTunnel.tunnelsFull[color] = true;
	}
    }

    // Scriptability
    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	final Application app = LTRemix.getApplication();
	final String targetName = this.getImageName();
	final int[] pgrmdest = app.getArenaManager().getArena().tunnelScan(dirX, dirY, dirZ, targetName);
	if (pgrmdest != null) {
	    app.getGameManager().updatePositionAbsoluteNoEvents(pgrmdest[0], pgrmdest[1], pgrmdest[2]);
	}
    }

    @Override
    public boolean pushIntoAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	final Application app = LTRemix.getApplication();
	final String targetName = ColorResolver.resolveColorConstantToName(this.getColor())
		+ StringConstants.COMMON_STRING_SPACE + this.getBaseName();
	final int color = this.getColor();
	final int[] pgrmdest = app.getArenaManager().getArena().tunnelScan(x, y, z, targetName);
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
    public int getPrimaryLayer() {
	return ArenaConstants.LAYER_LOWER_OBJECTS;
    }

    @Override
    public int getCustomProperty(final int propID) {
	return AbstractArenaObject.DEFAULT_CUSTOM_VALUE;
    }

    @Override
    public void setCustomProperty(final int propID, final int value) {
	// Do nothing
    }
}
