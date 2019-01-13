/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2010 Eric Ahnell

 Any questions should be directed to the author via email at: lasertank@worldwizard.net
 */
package com.puttysoftware.scoring;

import java.util.ArrayList;

public class ScoreTable {
    // Fields
    protected ArrayList<Score> table;

    // Constructors
    public ScoreTable() {
	this(10);
    }

    public ScoreTable(final int length) {
	this.table = new ArrayList<>(length);
	int x;
	for (x = 0; x < length; x++) {
	    this.table.set(x, new Score());
	}
    }

    // Methods
    public String getEntryName(final int pos) {
	return this.table.get(pos).getName();
    }

    public long getEntryMoves(final int pos) {
	return this.table.get(pos).getMoves();
    }

    public long getEntryShots(final int pos) {
	return this.table.get(pos).getShots();
    }

    public int getLength() {
	return this.table.size();
    }
    
    public void add(final Score score) {
	this.table.add(score);
    }
}
