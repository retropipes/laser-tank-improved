/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2010 Eric Ahnell

 Any questions should be directed to the author via email at: lasertank@worldwizard.net
 */
package com.puttysoftware.lasertank.improved.scoring;

import java.util.ArrayList;

public class ScoreTable {
    // Fields and Constants
    protected int scoreCount;
    protected ArrayList<Score> table;
    protected String[] unit;
    protected static final String DEFAULT_UNIT = "";

    // Constructors
    public ScoreTable() {
	this.scoreCount = 1;
	this.table = new ArrayList<>(10);
	int x;
	for (x = 0; x < 10; x++) {
	    this.table.set(x, new Score());
	}
	this.unit = new String[1];
	this.unit[0] = ScoreTable.DEFAULT_UNIT;
    }

    public ScoreTable(final int mv, final int length, final String[] customUnit) {
	this.scoreCount = mv;
	this.table = new ArrayList<>(length);
	int x;
	for (x = 0; x < length; x++) {
	    this.table.add(new Score(mv));
	}
	this.unit = new String[mv];
	for (x = 0; x < mv; x++) {
	    if (customUnit[x] == null || customUnit[x].isEmpty()) {
		this.unit[x] = ScoreTable.DEFAULT_UNIT;
	    } else {
		this.unit[x] = " " + customUnit[x];
	    }
	}
    }

    // Methods
    public long getEntryScore(final int which, final int pos) {
	return this.table.get(pos).getScore(which);
    }

    public String getEntryName(final int pos) {
	return this.table.get(pos).getName();
    }

    public int getLength() {
	return this.table.size();
    }

    public String getUnit() {
	return this.unit[0];
    }

    public String[] getUnits() {
	return this.unit;
    }

    public int getScoreCount() {
	return this.scoreCount;
    }

    public void setEntryScore(final int pos, final long newScore) {
	this.table.get(pos).setScore(newScore);
    }
    
    public void setEntryScore(final int pos, final int which, final long newScore) {
	this.table.get(pos).setScore(which, newScore);
    }

    public void setEntryName(final int pos, final String newName) {
	this.table.get(pos).setName(newName);
    }
}
