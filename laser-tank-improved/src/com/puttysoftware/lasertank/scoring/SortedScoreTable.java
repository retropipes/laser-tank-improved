/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2010 Eric Ahnell

 Any questions should be directed to the author via email at: lasertank@worldwizard.net
 */
package com.puttysoftware.lasertank.scoring;

import java.util.ArrayList;
import java.util.Collections;

public class SortedScoreTable extends ScoreTable {
    public static enum SortOrder {
	ASCENDING, DESCENDING;
    }

    // Fields
    protected SortOrder sortOrder;

    // Constructors
    public SortedScoreTable() {
	super();
	this.sortOrder = SortOrder.ASCENDING;
    }

    public SortedScoreTable(final int length, final SortOrder order) {
	super(length);
	this.sortOrder = order;
    }

    @Override
    public void add(final Score newEntry) {
	if (this.sortOrder == SortOrder.DESCENDING) {
	    // Append the new score to the end
	    this.table.add(newEntry);
	    // Sort the score table
	    Collections.sort(this.table, new Score.DescendingSorter());
	    // Remove the lowest score
	    this.table.remove(0);
	} else {
	    // Append the new score to the end
	    this.table.add(newEntry);
	    // Sort the score table
	    Collections.sort(this.table, new Score.AscendingSorter());
	    // Remove the highest score
	    this.table.remove(this.table.size() - 1);
	}
    }

    public boolean check(final Score newEntry) {
	final ArrayList<Score> temp = new ArrayList<>(this.table);
	if (this.sortOrder == SortOrder.DESCENDING) {
	    // Copy the current table to the temporary table
	    Collections.copy(temp, this.table);
	    // Append the new score to the end
	    temp.add(newEntry);
	    // Sort the score table
	    Collections.sort(temp, new Score.DescendingSorter());
	    // Determine if lowest score would be removed
	    return !Collections.min(temp, new Score.DescendingSorter()).equals(newEntry);
	} else {
	    // Copy the current table to the temporary table
	    Collections.copy(temp, this.table);
	    // Append the new score to the end
	    temp.add(newEntry);
	    // Sort the score table
	    Collections.sort(temp, new Score.AscendingSorter());
	    // Determine if highest score would be removed
	    return !Collections.max(temp, new Score.AscendingSorter()).equals(newEntry);
	}
    }
}
