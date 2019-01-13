/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2010 Eric Ahnell

 Any questions should be directed to the author via email at: lasertank@worldwizard.net
 */
package com.puttysoftware.lasertank.scoring;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.strings.DialogString;
import com.puttysoftware.lasertank.strings.StringLoader;

public class ScoreManager {
    // Fields and Constants
    private static final String NAME_PROMPT = "Enter a name for the score list:";
    public static final boolean SORT_ORDER_DESCENDING = false;
    protected SortedScoreTable table;
    private String name;
    private String title;
    private final String viewerTitle;

    // Constructors
    public ScoreManager() {
	this.table = new SortedScoreTable();
	this.name = "";
	String dialogTitle = StringLoader.loadDialog(DialogString.SCORES_HEADER);
	this.title = dialogTitle;
	this.viewerTitle = dialogTitle;
    }

    public ScoreManager(final int length, final SortedScoreTable.SortOrder sortOrder, final String customTitle) {
	this.table = new SortedScoreTable(length, sortOrder);
	this.name = "";
	if (customTitle == null || customTitle.equals("")) {
	    this.title = StringLoader.loadDialog(DialogString.SCORES_HEADER);
	} else {
	    this.title = customTitle;
	}
	this.viewerTitle = customTitle;
    }

    // Methods
    public boolean add(final long newMoves, final long newShots) {
	boolean success = true;
	this.name = CommonDialogs.showTextInputDialog(ScoreManager.NAME_PROMPT, this.title);
	if (this.name != null) {
	    this.table.add(new Score(newMoves, newShots, this.name));
	} else {
	    success = false;
	}
	return success;
    }

    public boolean add(final long newMoves, final long newShots, final String newName) {
	this.table.add(new Score(newMoves, newShots, newName));
	return true;
    }

    public boolean check(final long newMoves, final long newShots) {
	return this.table.check(new Score(newMoves, newShots, this.name));
    }

    public void view() {
	ScoreTableViewer.view(this.table, this.viewerTitle);
    }
}
