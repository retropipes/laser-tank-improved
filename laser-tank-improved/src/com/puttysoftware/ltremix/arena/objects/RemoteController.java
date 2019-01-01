/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractRemoteControlObject;

public class RemoteController extends AbstractRemoteControlObject {
    // Constructors
    public RemoteController() {
	super();
    }

    @Override
    public final int getStringBaseID() {
	return 150;
    }
}