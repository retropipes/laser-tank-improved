package com.puttysoftware.lasertank.datatypes;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.puttysoftware.fileio.GameIOUtilities;
import com.puttysoftware.lasertank.scoring.Score;
import com.puttysoftware.lasertank.scoring.ScoreTable;

public class LaserTankHighScores {
    // Constants
    private static final int NAME_LEN = 6;

    public static LaserTankHighScores loadFromFile(final File file) throws IOException {
	try (FileInputStream fs = new FileInputStream(file)) {
	    return LaserTankHighScores.loadFromStream(fs);
	}
    }

    public static LaserTankHighScores loadFromResource(final String resource) throws IOException {
	try (InputStream fs = LaserTankHighScores.class.getResourceAsStream(resource)) {
	    return LaserTankHighScores.loadFromStream(fs);
	}
    }

    // Internal stuff
    private static LaserTankHighScores loadFromStream(final InputStream fs) throws IOException {
	// Create temporary storage
	final ArrayList<String> nameTemp = new ArrayList<>();
	final ArrayList<Integer> moveTemp = new ArrayList<>();
	final ArrayList<Integer> shotTemp = new ArrayList<>();
	boolean success = true;
	int bytesRead = 0;
	while (success) {
	    try {
		// Load name
		final byte[] nameData = new byte[LaserTankHighScores.NAME_LEN];
		bytesRead = fs.read(nameData);
		if (bytesRead < LaserTankHighScores.NAME_LEN) {
		    success = false;
		    break;
		}
		final String loadName = GameIOUtilities.decodeWindowsStringData(nameData);
		// Load moves
		final byte[] moveData = new byte[Short.BYTES];
		bytesRead = fs.read(moveData);
		if (bytesRead < Short.BYTES) {
		    success = false;
		    break;
		}
		final int moves = ByteBuffer.wrap(moveData).asShortBuffer().get();
		// Load shots
		final byte[] shotData = new byte[Short.BYTES];
		bytesRead = fs.read(shotData);
		if (bytesRead < Short.BYTES) {
		    success = false;
		    break;
		}
		final int shots = ByteBuffer.wrap(shotData).asShortBuffer().get();
		// Add values to temporary storage
		nameTemp.add(loadName);
		moveTemp.add(moves);
		shotTemp.add(shots);
	    } catch (final EOFException e) {
		success = false;
		break;
	    }
	}
	// Convert temporary storage to the correct format
	final String[] nameLoad = nameTemp.toArray(new String[nameTemp.size()]);
	final int len = nameLoad.length;
	final Integer[] moveLoadTemp = moveTemp.toArray(new Integer[moveTemp.size()]);
	final Integer[] shotLoadTemp = shotTemp.toArray(new Integer[shotTemp.size()]);
	final ScoreTable table = new ScoreTable(len);
	for (int x = 0; x < len; x++) {
	    table.add(new Score(moveLoadTemp[x], shotLoadTemp[x], nameLoad[x]));
	}
	// Return final result
	return new LaserTankHighScores(table);
    }

    // Fields
    private final ScoreTable scoreData;

    // Constructor - used only internally
    private LaserTankHighScores(final ScoreTable data) {
	this.scoreData = data;
    }

    // Methods
    public final ScoreTable getScores() {
	return this.scoreData;
    }
}
