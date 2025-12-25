/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.game.lpb;

import java.io.FileInputStream;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.game.GameManager;
import com.puttysoftware.lasertank.strings.ErrorString;
import com.puttysoftware.lasertank.strings.GameString;
import com.puttysoftware.lasertank.strings.StringLoader;

class LPBFile {
    private static void decodeData(final byte d) {
	final GameManager gm = LaserTank.getApplication().getGameManager();
	switch (d) {
	case 0x20:
	    gm.loadReplay(true, 0, 0);
	    break;
	case 0x25:
	    gm.loadReplay(false, -1, 0);
	    break;
	case 0x26:
	    gm.loadReplay(false, 0, -1);
	    break;
	case 0x27:
	    gm.loadReplay(false, 1, 0);
	    break;
	case 0x28:
	    gm.loadReplay(false, 0, 1);
	    break;
	default:
	    break;
	}
    }

    static void loadLPB(final FileInputStream file) {
	// Load LPB
	final boolean success = LPBFileLoader.loadLPB(file);
	if (!success) {
	    CommonDialogs.showErrorDialog(StringLoader.loadError(ErrorString.LPB_LOAD_FAILURE),
		    StringLoader.loadGame(GameString.LOAD_PLAYBACK));
	} else {
	    final GameManager gm = LaserTank.getApplication().getGameManager();
	    gm.clearReplay();
	    final byte[] data = LPBFileLoader.getData();
	    for (int x = data.length - 1; x >= 0; x--) {
		LPBFile.decodeData(data[x]);
	    }
	    CommonDialogs.showTitledDialog(StringLoader.loadGame(GameString.PLAYBACK_LOADED),
		    StringLoader.loadGame(GameString.LOAD_PLAYBACK));
	}
    }

    private LPBFile() {
	// Do nothing
    }
}