package com.puttysoftware.lasertank.improved.datatypes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.puttysoftware.lasertank.improved.fileio.GameIOUtilities;
import com.puttysoftware.storage.ByteStorage;

public class LaserTankLevel {
    // Constants
    private static final int OBJECT_DATA_LEN = 256;
    private static final int NAME_LEN = 31;
    private static final int HINT_LEN = 256;
    private static final int AUTHOR_LEN = 31;
    private static final int DIFFICULTY_LEN = 2;
    // Fields
    private final String name;
    private final String author;
    private final String hint;
    private final int difficulty;
    private final ByteStorage objectData;

    // Constructor - used only internally
    private LaserTankLevel(final String loadName, final String loadAuthor, final String loadHint,
	    final int loadDifficulty, final ByteStorage loadObjectData) {
	this.name = loadName;
	this.author = loadAuthor;
	this.hint = loadHint;
	this.difficulty = loadDifficulty;
	this.objectData = loadObjectData;
    }

    // Methods
    public final String getName() {
	return this.name;
    }

    public final String getAuthor() {
	return this.author;
    }

    public final String getHint() {
	return this.hint;
    }

    public final int getDifficulty() {
	return this.difficulty;
    }

    public final ByteStorage getObjectData() {
	return this.objectData;
    }

    public static LaserTankLevel loadFromFile(final File file) throws IOException {
	try (FileInputStream fs = new FileInputStream(file)) {
	    return LaserTankLevel.loadFromStream(fs);
	}
    }

    public static LaserTankLevel loadFromResource(final String resource) throws IOException {
	try (InputStream fs = LaserTankLevel.class.getResourceAsStream(resource)) {
	    return LaserTankLevel.loadFromStream(fs);
	}
    }

    // Internal stuff
    private static LaserTankLevel loadFromStream(final InputStream fs) throws IOException {
	int bytesRead = 0;
	// Load name
	byte[] nameData = new byte[LaserTankLevel.NAME_LEN];
	bytesRead = fs.read(nameData);
	if (bytesRead < LaserTankLevel.NAME_LEN) {
	    throw new LVLLoadException();
	}
	String loadName = GameIOUtilities.decodeWindowsStringData(nameData);
	// Load author
	byte[] authorData = new byte[LaserTankLevel.AUTHOR_LEN];
	bytesRead = fs.read(authorData);
	if (bytesRead < LaserTankLevel.AUTHOR_LEN) {
	    throw new LVLLoadException();
	}
	String loadAuthor = GameIOUtilities.decodeWindowsStringData(authorData);
	// Load hint
	byte[] hintData = new byte[LaserTankLevel.HINT_LEN];
	bytesRead = fs.read(hintData);
	if (bytesRead < LaserTankLevel.HINT_LEN) {
	    throw new LVLLoadException();
	}
	String loadHint = GameIOUtilities.decodeWindowsStringData(hintData);
	// Load difficulty
	byte[] difficultyData = new byte[LaserTankLevel.DIFFICULTY_LEN];
	bytesRead = fs.read(difficultyData);
	if (bytesRead < LaserTankLevel.DIFFICULTY_LEN) {
	    throw new LVLLoadException();
	}
	int loadDifficulty = GameIOUtilities.unsignedShortByteArrayToInt(difficultyData);
	// Load object data
	byte[] objectData = new byte[LaserTankLevel.OBJECT_DATA_LEN];
	bytesRead = fs.read(objectData);
	if (bytesRead < LaserTankLevel.OBJECT_DATA_LEN) {
	    throw new LVLLoadException();
	}
	// Decode raw recording data
	ByteStorage loadObjectData = LaserTankLevel.decodeObjectData(objectData);
	// Return final result
	return new LaserTankLevel(loadName, loadAuthor, loadHint, loadDifficulty, loadObjectData);
    }

    private static ByteStorage decodeObjectData(final byte[] d) {
	return new LaserTankLevelStorage(d);
    }

    public static class LVLLoadException extends IOException {
	private static final long serialVersionUID = 8993383672852880300L;

	public LVLLoadException() {
	    super();
	}
    }

    private static class LaserTankLevelStorage extends ByteStorage {
	public LaserTankLevelStorage(final byte[] source) {
	    super(source, 16, 16);
	}
    }
}
