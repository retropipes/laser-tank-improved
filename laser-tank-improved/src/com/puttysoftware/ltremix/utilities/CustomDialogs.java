/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.utilities;

import javax.swing.JOptionPane;

import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.resourcemanagers.LogoManager;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;

public class CustomDialogs {
    private CustomDialogs() {
	// Do nothing
    }

    public static int showDeadDialog() {
	final Application app = LTRemix.getApplication();
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