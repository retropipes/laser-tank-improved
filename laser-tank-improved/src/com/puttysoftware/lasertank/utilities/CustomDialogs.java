/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import javax.swing.JOptionPane;

import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.resourcemanagers.LogoManager;
import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;

public class CustomDialogs {
    private CustomDialogs() {
	// Do nothing
    }

    public static int showDeadDialog() {
	final Application app = LaserTank.getApplication();
	return JOptionPane.showOptionDialog(app.getOutputFrame(),
		StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
			StringConstants.DIALOG_STRING_DEAD_MESSAGE),
		StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE, StringConstants.DIALOG_STRING_DEAD_TITLE),
		JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, LogoManager.getMicroLogo(),
		new String[] {
			StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
				StringConstants.DIALOG_STRING_UNDO_BUTTON),
			StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
				StringConstants.DIALOG_STRING_RESTART_BUTTON),
			StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
				StringConstants.DIALOG_STRING_END_BUTTON) },
		StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
			StringConstants.DIALOG_STRING_UNDO_BUTTON));
    }
}