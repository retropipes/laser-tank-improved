/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2010 Eric Ahnell

 Any questions should be directed to the author via email at: lasertank@worldwizard.net
 */
package com.puttysoftware.lasertank.scoring;

import java.io.IOException;

import com.puttysoftware.fileio.GameIODataReader;
import com.puttysoftware.fileio.GameIODataWriter;
import com.puttysoftware.fileio.GameIOReader;
import com.puttysoftware.fileio.GameIOWriter;
import com.puttysoftware.lasertank.LaserTank;

public class SavedScoreManager extends ScoreManager {
    // Fields
    private final String scoresFilename;

    // Constructors
    public SavedScoreManager(final int length, final SortedScoreTable.SortOrder sortOrder, final String customTitle,
	    final String scoresFile) {
	super(length, sortOrder, customTitle);
	this.scoresFilename = scoresFile;
	try {
	    this.readScoresFile();
	} catch (final IOException io) {
	    // Do nothing
	}
    }

    // Methods
    @Override
    public boolean add(final long newMoves, final long newShots) {
	final boolean success = super.add(newMoves, newShots);
	try {
	    this.writeScoresFile();
	} catch (final IOException io) {
	    LaserTank.logNonFatalErrorDirectly(io);
	}
	return success;
    }

    @Override
    public boolean add(final long newMoves, final long newShots, final String newName) {
	final boolean success = super.add(newMoves, newShots, newName);
	try {
	    this.writeScoresFile();
	} catch (final IOException io) {
	    LaserTank.logNonFatalErrorDirectly(io);
	}
	return success;
    }

    private void readScoresFile() throws IOException {
	try (GameIOReader gio = new GameIODataReader(this.scoresFilename)) {
	    this.table = SortedScoreTable.load(gio);
	}
    }

    private void writeScoresFile() throws IOException {
	try (GameIOWriter gio = new GameIODataWriter(this.scoresFilename)) {
	    this.table.save(gio);
	}
    }
}
