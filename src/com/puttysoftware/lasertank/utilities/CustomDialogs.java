/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import javax.swing.JOptionPane;

import com.puttysoftware.lasertank.strings.DialogString;
import com.puttysoftware.lasertank.strings.StringLoader;

public class CustomDialogs {
    public static int showDeadDialog() {
	return JOptionPane.showOptionDialog(null, StringLoader.loadDialog(DialogString.DEAD_MESSAGE),
		StringLoader.loadDialog(DialogString.DEAD_TITLE), JOptionPane.YES_NO_CANCEL_OPTION,
		JOptionPane.INFORMATION_MESSAGE, null,
		new String[] { StringLoader.loadDialog(DialogString.UNDO_BUTTON),
			StringLoader.loadDialog(DialogString.RESTART_BUTTON),
			StringLoader.loadDialog(DialogString.END_BUTTON) },
		StringLoader.loadDialog(DialogString.UNDO_BUTTON));
    }

    private CustomDialogs() {
	// Do nothing
    }
}