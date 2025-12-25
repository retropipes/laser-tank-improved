/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2010 Eric Ahnell

 Any questions should be directed to the author via email at: lasertank@worldwizard.net
 */
package com.puttysoftware.lasertank.scoring;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.strings.DialogString;
import com.puttysoftware.lasertank.strings.StringLoader;

public final class ScoreTableViewer {
    // Private constants
    private static final int ENTRIES_PER_PAGE = 10;

    // Methods
    public static void view(final ScoreTable table, final String customTitle) {
	final StringBuilder msgBuilder = new StringBuilder();
	String msg = null;
	String title = null;
	if (customTitle == null || customTitle.isEmpty()) {
	    title = StringLoader.loadDialog(DialogString.SCORES_HEADER);
	} else {
	    title = customTitle;
	}
	int x;
	int y;
	for (x = 0; x < table.getLength(); x += ScoreTableViewer.ENTRIES_PER_PAGE) {
	    msg = "";
	    for (y = 1; y <= ScoreTableViewer.ENTRIES_PER_PAGE; y++) {
		try {
		    msgBuilder.append(StringLoader.loadDialog(DialogString.SCORE_ENTRY, table.getEntryName(x + y - 1),
			    table.getEntryMoves(x + y - 1), table.getEntryShots(x + y - 1)));
		    msgBuilder.append("\n");
		} catch (final ArrayIndexOutOfBoundsException ae) {
		    // Do nothing
		}
	    }
	    msg = msgBuilder.toString();
	    // Strip final newline character
	    msg = msg.substring(0, msg.length() - 1);
	    CommonDialogs.showTitledDialog(msg, title);
	}
    }

    // Private constructor
    private ScoreTableViewer() {
	// Do nothing
    }
}
