/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractSpell;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;

public class KillSpell extends AbstractSpell {
    // Constructors
    public KillSpell() {
	super();
    }

    // Scriptability
    @Override
    public void postMoveAction(final int dirX, final int dirY, final int dirZ) {
	SoundManager.playSound(SoundConstants.SOUND_KILL_SPELL);
	LTRemix.getApplication().getArenaManager().getArena().fullScanKillTanks();
	LTRemix.getApplication().getGameManager().morph(new Empty(), dirX, dirY, dirZ, this.getPrimaryLayer());
    }

    @Override
    public final int getStringBaseID() {
	return 143;
    }
}