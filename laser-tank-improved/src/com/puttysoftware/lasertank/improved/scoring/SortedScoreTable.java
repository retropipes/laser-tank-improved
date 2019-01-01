/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2010 Eric Ahnell

 Any questions should be directed to the author via email at: lasertank@worldwizard.net
 */
package com.puttysoftware.lasertank.improved.scoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.puttysoftware.lasertank.improved.fileio.XMLFileReader;
import com.puttysoftware.lasertank.improved.fileio.XMLFileWriter;

public class SortedScoreTable extends ScoreTable {
    // Fields
    protected boolean sortOrder;

    // Constructors
    public SortedScoreTable() {
	super();
	this.sortOrder = true;
    }

    private SortedScoreTable(final int mv, final int length, final boolean ascending, final String[] customUnit) {
	super(mv, length, customUnit);
	this.sortOrder = ascending;
    }

    public SortedScoreTable(final int mv, final int length, final boolean ascending, final long startingScore,
	    final String[] customUnit) {
	super(mv, length, customUnit);
	this.sortOrder = ascending;
	int x, y;
	for (x = 0; x < this.table.size(); x++) {
	    for (y = 0; y < this.scoreCount; y++) {
		this.table.get(x).setScore(y, startingScore);
	    }
	}
    }

    @Override
    public void setEntryScore(final int pos, final long newScore) {
	// Do nothing
    }

    @Override
    public void setEntryName(final int pos, final String newName) {
	// Do nothing
    }

    public void addScore(final long newScore, final String newName) {
	final Score newEntry = new Score(newScore, newName);
	if (this.sortOrder) {
	    // Append the new score to the end
	    this.table.add(newEntry);
	    // Sort the score table
	    Collections.sort(this.table, new Score.ScoreComparatorDesc());
	    // Remove the lowest score
	    this.table.remove(0);
	} else {
	    // Append the new score to the end
	    this.table.add(newEntry);
	    // Sort the score table
	    Collections.sort(this.table, new Score.ScoreComparatorAsc());
	    // Remove the highest score
	    this.table.remove(this.table.size() - 1);
	}
    }

    public void addScore(final long[] newScore, final String newName) {
	final Score newEntry = new Score(this.scoreCount, newScore, newName);
	if (this.sortOrder) {
	    // Append the new score to the end
	    this.table.add(newEntry);
	    // Sort the score table
	    Collections.sort(this.table, new Score.ScoreComparatorDesc());
	    // Remove the lowest score
	    this.table.remove(this.table.size() - 1);
	} else {
	    // Append the new score to the end
	    this.table.add(newEntry);
	    // Sort the score table
	    Collections.sort(this.table, new Score.ScoreComparatorAsc());
	    // Remove the highest score
	    this.table.remove(this.table.size() - 1);
	}
    }

    public boolean checkScore(final long[] newScore) {
	final Score newEntry = new Score(this.scoreCount, newScore, "");
	final ArrayList<Score> temp = new ArrayList<>(this.table);
	if (this.sortOrder) {
	    // Copy the current table to the temporary table
	    Collections.copy(temp, this.table);
	    // Append the new score to the end
	    temp.add(newEntry);
	    // Sort the score table
	    Collections.sort(temp, new Score.ScoreComparatorDesc());
	    // Determine if lowest score would be removed
	    return !Collections.min(temp, new Score.ScoreComparatorDesc()).equals(newEntry);
	} else {
	    // Copy the current table to the temporary table
	    Collections.copy(temp, this.table);
	    // Append the new score to the end
	    temp.add(newEntry);
	    // Sort the score table
	    Collections.sort(temp, new Score.ScoreComparatorAsc());
	    // Determine if highest score would be removed
	    return !Collections.max(temp, new Score.ScoreComparatorAsc()).equals(newEntry);
	}
    }

    public static SortedScoreTable readSortedScoreTable(final XMLFileReader xdr) throws IOException {
	final boolean order = xdr.readBoolean();
	final int len = xdr.readInt();
	final int unitLen = xdr.readInt();
	final String[] unitArr = new String[unitLen];
	for (int z = 0; z < unitLen; z++) {
	    unitArr[z] = xdr.readString();
	}
	final SortedScoreTable sst = new SortedScoreTable(unitLen, len, order, unitArr);
	for (int x = 0; x < len; x++) {
	    sst.table.set(x, Score.readScore(xdr));
	}
	return sst;
    }

    public void writeSortedScoreTable(final XMLFileWriter xdw) throws IOException {
	xdw.writeBoolean(this.sortOrder);
	xdw.writeInt(this.table.size());
	xdw.writeInt(this.unit.length);
	for (final String element : this.unit) {
	    if (element.length() > 1) {
		xdw.writeString(element.substring(1));
	    } else {
		xdw.writeString(element);
	    }
	}
	for (int x = 0; x < this.table.size(); x++) {
	    this.table.get(x).writeScore(xdw);
	}
    }
}
