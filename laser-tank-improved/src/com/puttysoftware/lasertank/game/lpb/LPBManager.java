/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.game.lpb;

import java.awt.FileDialog;
import java.io.File;

import javax.swing.JFrame;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.fileio.FilenameChecker;
import com.puttysoftware.lasertank.prefs.PreferencesManager;
import com.puttysoftware.lasertank.strings.DialogString;
import com.puttysoftware.lasertank.strings.GameString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.utilities.Extension;

public class LPBManager {
    private static String getExtension(final String s) {
	String ext = null;
	final int i = s.lastIndexOf('.');
	if (i > 0 && i < s.length() - 1) {
	    ext = s.substring(i + 1).toLowerCase();
	}
	return ext;
    }

    private static String getFileNameOnly(final String s) {
	String fno = null;
	final int i = s.lastIndexOf(File.separatorChar);
	if (i > 0 && i < s.length() - 1) {
	    fno = s.substring(i + 1);
	} else {
	    fno = s;
	}
	return fno;
    }

    private static String getNameWithoutExtension(final String s) {
	String ext = null;
	final int i = s.lastIndexOf('.');
	if (i > 0 && i < s.length() - 1) {
	    ext = s.substring(0, i);
	} else {
	    ext = s;
	}
	return ext;
    }

    public static void loadFile(final String filename) {
	if (!FilenameChecker.isFilenameOK(LPBManager.getNameWithoutExtension(LPBManager.getFileNameOnly(filename)))) {
	    CommonDialogs.showErrorDialog(StringLoader.loadDialog(DialogString.ILLEGAL_CHARACTERS),
		    StringLoader.loadDialog(DialogString.LOAD));
	} else {
	    final LPBLoadTask lpblt = new LPBLoadTask(filename);
	    lpblt.start();
	}
    }

    // Methods
    public static void loadLPB() {
	String filename, extension, file, dir;
	final String lastOpen = PreferencesManager.getLastDirOpen();
	final FileDialog fd = new FileDialog((JFrame) null, StringLoader.loadGame(GameString.LOAD_PLAYBACK),
		FileDialog.LOAD);
	fd.setDirectory(lastOpen);
	fd.setVisible(true);
	file = fd.getFile();
	dir = fd.getDirectory();
	if (file != null && dir != null) {
	    filename = dir + file;
	    extension = LPBManager.getExtension(filename);
	    if (extension.equals(Extension.getOldPlaybackExtension())) {
		LPBManager.loadFile(filename);
	    } else {
		CommonDialogs.showDialog(StringLoader.loadDialog(DialogString.NON_PLAYBACK_FILE));
	    }
	}
    }

    // Constructors
    private LPBManager() {
	// Do nothing
    }
}
