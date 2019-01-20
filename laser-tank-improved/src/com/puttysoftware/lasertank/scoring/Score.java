/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2010 Eric Ahnell

 Any questions should be directed to the author via email at: lasertank@worldwizard.net
 */
package com.puttysoftware.lasertank.scoring;

import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;

import com.puttysoftware.fileio.GameIOReader;
import com.puttysoftware.fileio.GameIOWriter;

public final class Score {
    public static class AscendingSorter implements Comparator<Score>, Serializable {
	private static final long serialVersionUID = 30523263423564L;

	@Override
	public int compare(final Score o1, final Score o2) {
	    if (o2.shots > o1.shots) {
		return 1;
	    } else if (o2.shots < o1.shots) {
		return -1;
	    } else {
		if (o2.moves > o1.moves) {
		    return 1;
		} else if (o2.moves < o1.moves) {
		    return -1;
		} else {
		    return 0;
		}
	    }
	}
    }

    public static class DescendingSorter implements Comparator<Score>, Serializable {
	private static final long serialVersionUID = 30523263423565L;

	@Override
	public int compare(final Score o1, final Score o2) {
	    if (o2.shots > o1.shots) {
		return -1;
	    } else if (o2.shots < o1.shots) {
		return 1;
	    } else {
		if (o2.moves > o1.moves) {
		    return -1;
		} else if (o2.moves < o1.moves) {
		    return 1;
		} else {
		    return 0;
		}
	    }
	}
    }

    public static Score load(final GameIOReader gio) throws IOException {
	String loadName = gio.readString();
	long loadMoves = gio.readLong();
	long loadShots = gio.readLong();
	return new Score(loadMoves, loadShots, loadName);
    }

    // Fields
    private final long moves;
    private final long shots;
    private final String name;

    // Constructors
    public Score() {
	this.moves = 0L;
	this.shots = 0L;
	this.name = "Nobody";
    }

    public Score(final long newMoves, final long newShots, final String newName) {
	this.moves = newMoves;
	this.shots = newShots;
	this.name = newName;
    }

    // Methods
    public String getName() {
	return this.name;
    }

    public long getMoves() {
	return this.moves;
    }

    public long getShots() {
	return this.shots;
    }

    public void save(final GameIOWriter gio) throws IOException {
	gio.writeString(this.name);
	gio.writeLong(this.moves);
	gio.writeLong(this.shots);
    }
}
