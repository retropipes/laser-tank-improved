package com.puttysoftware.lasertank.datatypes;

import java.io.IOException;

import com.puttysoftware.fileio.GameIODataReader;
import com.puttysoftware.fileio.GameIOReader;
import com.puttysoftware.storage.StringStorage;

public class LaserTankLevel {
    // Constants for Improved Level Files
    // Internal object data layout: rows, cols, floors, metas, levels
    // Object meta layout: object, attribute, direction, index, frame
    // Internal meta layout: level_number, metadata
    // Level metadata layout: name, author, hint
    private static final int ILVL_METADATA_INDEX_NAME = 0;
    private static final int ILVL_METADATA_INDEX_AUTHOR = 1;
    private static final int ILVL_METADATA_INDEX_HINT = 2;
    private static final int ILVL_METADATA_INDEXES = 3;
    private static final int ILVL_OBJECT_DATA_ROWS = 24;
    private static final int ILVL_OBJECT_DATA_COLS = 24;
    private static final int ILVL_OBJECT_DATA_FLOORS = 9;
    private static final int ILVL_OBJECT_DATA_METAS = 5;
    private static final int ILVL_OBJECT_META_INDEX_OBJECTS = 0;
    private static final int ILVL_OBJECT_META_INDEX_ATTRIBUTES = 1;
    private static final int ILVL_OBJECT_META_INDEX_DIRECTIONS = 2;
    private static final int ILVL_OBJECT_META_INDEX_INDEXES = 3;
    private static final int ILVL_OBJECT_META_INDEX_FRAMES = 4;
    // Constants for LVL files
    private static final int LVL_OBJECT_DATA_LEN = 256;
    private static final int LVL_NAME_LEN = 31;
    private static final int LVL_HINT_LEN = 256;
    private static final int LVL_AUTHOR_LEN = 31;

    private static void decodeLVLObjectData(final byte[] rawData, final int levelIndex,
	    final LaserTankLevelStorage storage) {
	final int floorIndex = 0;
	for (int x = 0; x < 16; x++) {
	    for (int y = 0; y < 16; y++) {
		final int z = x * 16 + y;
		final byte oo = rawData[z];
		int objID, dirID, indexID, frameID;
		final int attrID = 0; // No attribute
		dirID = 0; // No direction
		indexID = 0; // No index
		frameID = 0; // Not animated
		switch (oo) {
		case 0:
		    objID = 58; // Ground
		    break;
		case 1:
		    objID = 210; // Tank
		    dirID = 2; // North
		    break;
		case 2:
		    objID = 53; // Flag
		    frameID = 1; // Animated
		    break;
		case 3:
		    objID = 231; // Water
		    frameID = 1; // Animated
		    break;
		case 4:
		    objID = 230; // Wall
		    break;
		case 5:
		    objID = 8; // Box
		    break;
		case 6:
		    objID = 10; // Bricks
		    break;
		case 7:
		    objID = 4; // Anti-Tank
		    dirID = 2; // North
		    frameID = 1; // Animated
		    break;
		case 8:
		    objID = 4; // Anti-Tank
		    dirID = 4; // East
		    frameID = 1; // Animated
		    break;
		case 9:
		    objID = 4; // Anti-Tank
		    dirID = 6; // South
		    frameID = 1; // Animated
		    break;
		case 10:
		    objID = 4; // Anti-Tank
		    dirID = 8; // West
		    frameID = 1; // Animated
		    break;
		case 11:
		    objID = 144; // Mirror
		    dirID = 1; // Northwest
		    break;
		case 12:
		    objID = 144; // Mirror
		    dirID = 3; // Northeast
		    break;
		case 13:
		    objID = 144; // Mirror
		    dirID = 5; // Southeast
		    break;
		case 14:
		    objID = 144; // Mirror
		    dirID = 7; // Southwest
		    break;
		case 15:
		    objID = 211; // Tank Mover
		    dirID = 2; // North
		    frameID = 1; // Animated
		    break;
		case 16:
		    objID = 211; // Tank Mover
		    dirID = 4; // East
		    frameID = 1; // Animated
		    break;
		case 17:
		    objID = 211; // Tank Mover
		    dirID = 6; // South
		    frameID = 1; // Animated
		    break;
		case 18:
		    objID = 211; // Tank Mover
		    dirID = 8; // West
		    frameID = 1; // Animated
		    break;
		case 19:
		    objID = 15; // Crystal Block
		    break;
		case 20:
		    objID = 172; // Rotary Mirror
		    dirID = 1; // Northwest
		    break;
		case 21:
		    objID = 172; // Rotary Mirror
		    dirID = 3; // Northeast
		    break;
		case 22:
		    objID = 172; // Rotary Mirror
		    dirID = 5; // Southeast
		    break;
		case 23:
		    objID = 172; // Rotary Mirror
		    dirID = 7; // Southwest
		    break;
		case 24:
		    objID = 68; // Ice
		    break;
		case 25:
		    objID = 222; // Thin Ice
		    break;
		case 64:
		case 65:
		    objID = 228; // Tunnel
		    indexID = 1; // Red
		    break;
		case 66:
		case 67:
		    objID = 228; // Tunnel
		    indexID = 2; // Green
		    break;
		case 68:
		case 69:
		    objID = 228; // Tunnel
		    indexID = 3; // Blue
		    break;
		case 70:
		case 71:
		    objID = 228; // Tunnel
		    indexID = 4; // Cyan
		    break;
		case 72:
		case 73:
		    objID = 228; // Tunnel
		    indexID = 5; // Yellow
		    break;
		case 74:
		case 75:
		    objID = 228; // Tunnel
		    indexID = 6; // Magenta
		    break;
		case 76:
		case 77:
		    objID = 228; // Tunnel
		    indexID = 7; // White
		    break;
		case 78:
		case 79:
		    objID = 228; // Tunnel
		    indexID = 8; // Gray
		    break;
		default:
		    objID = 105; // Placeholder
		}
		// Populate Object ID
		storage.setCell(objID, x, y, floorIndex, LaserTankLevel.ILVL_OBJECT_META_INDEX_OBJECTS, levelIndex);
		// Populate Attribute ID
		storage.setCell(attrID, x, y, floorIndex, LaserTankLevel.ILVL_OBJECT_META_INDEX_ATTRIBUTES, levelIndex);
		// Populate Direction ID
		storage.setCell(dirID, x, y, floorIndex, LaserTankLevel.ILVL_OBJECT_META_INDEX_DIRECTIONS, levelIndex);
		// Populate Index ID
		storage.setCell(indexID, x, y, floorIndex, LaserTankLevel.ILVL_OBJECT_META_INDEX_INDEXES, levelIndex);
		// Populate Frame ID
		storage.setCell(frameID, x, y, floorIndex, LaserTankLevel.ILVL_OBJECT_META_INDEX_FRAMES, levelIndex);
	    }
	}
    }

    public static LaserTankLevel loadLVL(final String filename) throws IOException {
	try (GameIOReader gio = new GameIODataReader(filename)) {
	    return LaserTankLevel.loadLVLFromGameIO(gio);
	}
    }

    // Internal stuff
    private static LaserTankLevel loadLVLFromGameIO(final GameIOReader gio) throws IOException {
	// Load name
	final byte[] nameData = new byte[LaserTankLevel.LVL_NAME_LEN];
	final String loadName = gio.readWindowsString(nameData);
	// Load author
	final byte[] authorData = new byte[LaserTankLevel.LVL_AUTHOR_LEN];
	final String loadAuthor = gio.readWindowsString(authorData);
	// Load hint
	final byte[] hintData = new byte[LaserTankLevel.LVL_HINT_LEN];
	final String loadHint = gio.readWindowsString(hintData);
	// Load difficulty
	final int loadDifficulty = gio.readUnsignedShortByteArrayAsInt();
	// Load and decode object data
	final LaserTankLevelStorage loadObjectData = new LaserTankLevelStorage(LaserTankLevel.ILVL_OBJECT_DATA_ROWS,
		LaserTankLevel.ILVL_OBJECT_DATA_COLS, LaserTankLevel.ILVL_OBJECT_DATA_FLOORS,
		LaserTankLevel.ILVL_OBJECT_DATA_METAS, 1);
	LaserTankLevel.decodeLVLObjectData(gio.readBytes(LaserTankLevel.LVL_OBJECT_DATA_LEN), 0, loadObjectData);
	// Build and return final result
	LaserTankLevel level = new LaserTankLevel(1, loadObjectData);
	level.setName(loadName, 0);
	level.setAuthor(loadAuthor, 0);
	level.setDifficulty(loadDifficulty, 0);
	level.setHint(loadHint, 0);
	return level;
    }

    // Fields
    private final StringStorage metaData;
    private final int[] difficulty;
    private final LaserTankLevelStorage objectData;

    // Constructor - used only internally
    private LaserTankLevel(final int levelCount, final LaserTankLevelStorage loadObjectData) {
	this.metaData = new StringStorage(levelCount, LaserTankLevel.ILVL_METADATA_INDEXES);
	this.difficulty = new int[levelCount];
	this.objectData = loadObjectData;
    }

    // Methods
    public final String getAuthor(final int level) {
	return this.metaData.getCell(level, LaserTankLevel.ILVL_METADATA_INDEX_AUTHOR);
    }

    public final int getDifficulty(final int level) {
	return this.difficulty[level];
    }

    public final String getHint(final int level) {
	return this.metaData.getCell(level, LaserTankLevel.ILVL_METADATA_INDEX_HINT);
    }

    public final String getName(final int level) {
	return this.metaData.getCell(level, LaserTankLevel.ILVL_METADATA_INDEX_NAME);
    }

    public final int getObjectID(final int row, final int col, final int floor, final int level) {
	return this.objectData.getCell(row, col, floor, LaserTankLevel.ILVL_OBJECT_META_INDEX_OBJECTS, level);
    }

    public final int getAttributeID(final int row, final int col, final int floor, final int level) {
	return this.objectData.getCell(row, col, floor, LaserTankLevel.ILVL_OBJECT_META_INDEX_ATTRIBUTES, level);
    }

    public final int getDirectionID(final int row, final int col, final int floor, final int level) {
	return this.objectData.getCell(row, col, floor, LaserTankLevel.ILVL_OBJECT_META_INDEX_DIRECTIONS, level);
    }

    public final int getIndexID(final int row, final int col, final int floor, final int level) {
	return this.objectData.getCell(row, col, floor, LaserTankLevel.ILVL_OBJECT_META_INDEX_INDEXES, level);
    }

    public final int getFrameID(final int row, final int col, final int floor, final int level) {
	return this.objectData.getCell(row, col, floor, LaserTankLevel.ILVL_OBJECT_META_INDEX_FRAMES, level);
    }

    public final void setAuthor(final String newValue, final int level) {
	this.metaData.setCell(newValue, level, LaserTankLevel.ILVL_METADATA_INDEX_AUTHOR);
    }

    public final void setDifficulty(final int newValue, final int level) {
	this.difficulty[level] = newValue;
    }

    public final void setHint(final String newValue, final int level) {
	this.metaData.setCell(newValue, level, LaserTankLevel.ILVL_METADATA_INDEX_HINT);
    }

    public final void setName(final String newValue, final int level) {
	this.metaData.setCell(newValue, level, LaserTankLevel.ILVL_METADATA_INDEX_NAME);
    }

    public final void setObjectID(final int newID, final int row, final int col, final int floor, final int level) {
	this.objectData.setCell(newID, row, col, floor, LaserTankLevel.ILVL_OBJECT_META_INDEX_OBJECTS, level);
    }

    public final void setAttributeID(final int newID, final int row, final int col, final int floor, final int level) {
	this.objectData.setCell(newID, row, col, floor, LaserTankLevel.ILVL_OBJECT_META_INDEX_ATTRIBUTES, level);
    }

    public final void setDirectionID(final int newID, final int row, final int col, final int floor, final int level) {
	this.objectData.setCell(newID, row, col, floor, LaserTankLevel.ILVL_OBJECT_META_INDEX_DIRECTIONS, level);
    }

    public final void setIndexID(final int newID, final int row, final int col, final int floor, final int level) {
	this.objectData.setCell(newID, row, col, floor, LaserTankLevel.ILVL_OBJECT_META_INDEX_INDEXES, level);
    }

    public final void setFrameID(final int newID, final int row, final int col, final int floor, final int level) {
	this.objectData.setCell(newID, row, col, floor, LaserTankLevel.ILVL_OBJECT_META_INDEX_FRAMES, level);
    }
}
