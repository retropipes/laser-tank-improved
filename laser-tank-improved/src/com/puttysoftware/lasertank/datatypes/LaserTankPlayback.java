package com.puttysoftware.lasertank.datatypes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.puttysoftware.fileio.GameIOUtilities;

public class LaserTankPlayback {
    public enum Entry {
	SHOOT, MOVE_LEFT, MOVE_DOWN, MOVE_RIGHT, MOVE_UP;
    }

    public static class LPBLoadException extends IOException {
	private static final long serialVersionUID = 8993383672852880300L;

	public LPBLoadException() {
	    super();
	}
    }

    // Constants
    private static final int LEVEL_NAME_LEN = 31;
    private static final int AUTHOR_LEN = 31;

    private static Entry[] decodeRawData(final byte[] d) throws LPBLoadException {
	final Entry[] decoded = new Entry[d.length];
	for (int x = 0; x < d.length; x++) {
	    decoded[x] = LaserTankPlayback.decodeRawDataPoint(d[x]);
	}
	return decoded;
    }

    private static Entry decodeRawDataPoint(final byte d) throws LPBLoadException {
	switch (d) {
	case 0x20:
	    return Entry.SHOOT;
	case 0x25:
	    return Entry.MOVE_LEFT;
	case 0x26:
	    return Entry.MOVE_DOWN;
	case 0x27:
	    return Entry.MOVE_RIGHT;
	case 0x28:
	    return Entry.MOVE_UP;
	default:
	    throw new LPBLoadException();
	}
    }

    public static LaserTankPlayback loadFromFile(final File file) throws IOException {
	try (FileInputStream fs = new FileInputStream(file)) {
	    return LaserTankPlayback.loadFromStream(fs);
	}
    }

    public static LaserTankPlayback loadFromResource(final String resource) throws IOException {
	try (InputStream fs = LaserTankPlayback.class.getResourceAsStream(resource)) {
	    return LaserTankPlayback.loadFromStream(fs);
	}
    }

    // Internal stuff
    private static LaserTankPlayback loadFromStream(final InputStream fs) throws IOException {
	int bytesRead = 0;
	// Load level name
	final byte[] levelNameData = new byte[LaserTankPlayback.LEVEL_NAME_LEN];
	bytesRead = fs.read(levelNameData);
	if (bytesRead < LaserTankPlayback.LEVEL_NAME_LEN) {
	    throw new LPBLoadException();
	}
	final String loadLevelName = GameIOUtilities.decodeWindowsStringData(levelNameData);
	// Load author
	final byte[] authorData = new byte[LaserTankPlayback.AUTHOR_LEN];
	bytesRead = fs.read(authorData);
	if (bytesRead < LaserTankPlayback.AUTHOR_LEN) {
	    throw new LPBLoadException();
	}
	final String loadAuthor = GameIOUtilities.decodeWindowsStringData(authorData);
	// Load info
	final byte[] levelNumberData = new byte[Short.BYTES];
	bytesRead = fs.read(levelNumberData);
	if (bytesRead < Short.BYTES) {
	    throw new LPBLoadException();
	}
	final int loadLevelNumber = GameIOUtilities.unsignedShortByteArrayToInt(levelNumberData);
	// Load recording size
	final byte[] recordingSizeData = new byte[Short.BYTES];
	bytesRead = fs.read(recordingSizeData);
	if (bytesRead < Short.BYTES) {
	    throw new LPBLoadException();
	}
	final int recordingSize = GameIOUtilities.unsignedShortByteArrayToInt(levelNumberData);
	// Load raw recording data
	final byte[] rawRecordingData = new byte[recordingSize];
	bytesRead = fs.read(rawRecordingData);
	if (bytesRead < recordingSize) {
	    throw new LPBLoadException();
	}
	// Decode raw recording data
	final Entry[] loadRecordingData = LaserTankPlayback.decodeRawData(rawRecordingData);
	// Return final result
	return new LaserTankPlayback(loadLevelName, loadAuthor, loadLevelNumber, loadRecordingData);
    }

    // Fields
    private final String levelName;
    private final String author;
    private final int levelNumber;
    private final Entry[] recordingData;

    // Constructor - used only internally
    private LaserTankPlayback(final String loadLevelName, final String loadAuthor, final int loadLevelNumber,
	    final Entry[] loadRecordingData) {
	this.levelName = loadLevelName;
	this.author = loadAuthor;
	this.levelNumber = loadLevelNumber;
	this.recordingData = loadRecordingData;
    }

    public final String getAuthor() {
	return this.author;
    }

    // Methods
    public final String getLevelName() {
	return this.levelName;
    }

    public final int getLevelNumber() {
	return this.levelNumber;
    }

    public final Entry[] getRecordingData() {
	return this.recordingData;
    }
}
