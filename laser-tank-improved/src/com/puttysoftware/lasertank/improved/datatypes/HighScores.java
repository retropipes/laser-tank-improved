package com.puttysoftware.lasertank.improved.datatypes;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.puttysoftware.lasertank.improved.fileio.GameIOUtilities;
import com.puttysoftware.lasertank.improved.scoring.ScoreTable;

public class HighScores {
    // Constants
    private static final int NAME_LEN = 6;
    // Fields
    private ScoreTable scoreData;

    // Constructor - used only internally
    private HighScores(final ScoreTable data) {
	this.scoreData = data;
    }

    // Methods
    public final ScoreTable getScores() {
	return this.scoreData;
    }

    public static HighScores loadFromFile(final File file) throws IOException {
	try (FileInputStream fs = new FileInputStream(file)) {
	    return HighScores.loadFromStream(fs);
	}
    }

    public static HighScores loadFromResource(final String resource) throws IOException {
	try (InputStream fs = HighScores.class.getResourceAsStream(resource)) {
	    return HighScores.loadFromStream(fs);
	}
    }

    // Internal stuff
    private static HighScores loadFromStream(final InputStream fs) throws IOException {
	// Create temporary storage
	ArrayList<String> nameTemp = new ArrayList<>();
	ArrayList<Integer> moveTemp = new ArrayList<>();
	ArrayList<Integer> shotTemp = new ArrayList<>();
	boolean success = true;
	while (success) {
	    try {
		// Load name
		byte[] nameData = new byte[HighScores.NAME_LEN];
		fs.read(nameData);
		String loadName = GameIOUtilities.decodeWindowsStringData(nameData);
		// Load moves
		byte[] moveData = new byte[Short.BYTES];
		fs.read(moveData);
		int moves = ByteBuffer.wrap(moveData).asShortBuffer().get();
		// Load shots
		byte[] shotData = new byte[Short.BYTES];
		fs.read(shotData);
		int shots = ByteBuffer.wrap(shotData).asShortBuffer().get();
		// Add values to temporary storage
		nameTemp.add(loadName);
		moveTemp.add(moves);
		shotTemp.add(shots);
	    } catch (EOFException e) {
		success = false;
	    }
	}
	// Convert temporary storage to the correct format
	String[] nameLoad = nameTemp.toArray(new String[nameTemp.size()]);
	int len = nameLoad.length;
	Integer[] moveLoadTemp = moveTemp.toArray(new Integer[moveTemp.size()]);
	Integer[] shotLoadTemp = shotTemp.toArray(new Integer[shotTemp.size()]);
	String[] units = new String[] { "moves", "shots" };
	ScoreTable table = new ScoreTable(2, len, units);
	for (int x = 0; x < len; x++) {
	    table.setEntryName(x, nameLoad[x]);
	    table.setEntryScore(x, 0, moveLoadTemp[x]);
	    table.setEntryScore(x, 1, shotLoadTemp[x]);
	}
	// Return final result
	return new HighScores(table);
    }
}
