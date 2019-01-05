/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.current;

import java.io.IOException;

import com.puttysoftware.lasertank.improved.fileio.XMLFileReader;
import com.puttysoftware.lasertank.improved.fileio.XMLFileWriter;
import com.puttysoftware.storage.NumberStorage;
import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;
import com.puttysoftware.lasertank.utilities.ArenaConstants;

public final class LevelInfo {
    // Properties
    private NumberStorage playerData;
    private boolean horizontalWraparoundEnabled;
    private boolean verticalWraparoundEnabled;
    private boolean thirdDimensionWraparoundEnabled;
    private String name;
    private String hint;
    private String author;
    private int difficulty;
    private boolean moveShootAllowed;

    // Constructors
    public LevelInfo() {
	this.playerData = new NumberStorage(ArenaConstants.PLAYER_DIMS, ArenaConstants.NUM_PLAYERS);
	this.playerData.fill(-1);
	this.horizontalWraparoundEnabled = false;
	this.verticalWraparoundEnabled = false;
	this.name = StringLoader.loadString(StringConstants.GENERIC_STRINGS_FILE,
		StringConstants.GENERIC_STRING_UN_NAMED_LEVEL);
	this.author = StringLoader.loadString(StringConstants.GENERIC_STRINGS_FILE,
		StringConstants.GENERIC_STRING_UNKNOWN_AUTHOR);
	this.hint = StringConstants.COMMON_STRING_EMPTY;
	this.difficulty = 1;
	this.moveShootAllowed = false;
    }

    // Methods
    @Override
    public LevelInfo clone() {
	final LevelInfo copy = new LevelInfo();
	copy.playerData = new NumberStorage(this.playerData);
	copy.horizontalWraparoundEnabled = this.horizontalWraparoundEnabled;
	copy.verticalWraparoundEnabled = this.verticalWraparoundEnabled;
	copy.author = this.author;
	copy.name = this.name;
	copy.hint = this.hint;
	copy.difficulty = this.difficulty;
	copy.moveShootAllowed = this.moveShootAllowed;
	return copy;
    }

    public boolean isMoveShootAllowed() {
	return this.moveShootAllowed;
    }

    public void setMoveShootAllowed(final boolean value) {
	this.moveShootAllowed = value;
    }

    public String getName() {
	return this.name;
    }

    public void setName(final String newName) {
	this.name = newName;
    }

    public String getHint() {
	return this.hint;
    }

    public void setHint(final String newHint) {
	this.hint = newHint;
    }

    public String getAuthor() {
	return this.author;
    }

    public void setAuthor(final String newAuthor) {
	this.author = newAuthor;
    }

    public int getDifficulty() {
	return this.difficulty;
    }

    public void setDifficulty(final int newDifficulty) {
	this.difficulty = newDifficulty;
    }

    public int getStartRow(final int pi) {
	return this.playerData.getCell(1, pi);
    }

    public int getStartColumn(final int pi) {
	return this.playerData.getCell(0, pi);
    }

    public int getStartFloor(final int pi) {
	return this.playerData.getCell(2, pi);
    }

    public void setStartRow(final int pi, final int value) {
	this.playerData.setCell(value, 1, pi);
    }

    public void setStartColumn(final int pi, final int value) {
	this.playerData.setCell(value, 0, pi);
    }

    public void setStartFloor(final int pi, final int value) {
	this.playerData.setCell(value, 2, pi);
    }

    public boolean doesPlayerExist(final int pi) {
	for (int y = 0; y < ArenaConstants.PLAYER_DIMS; y++) {
	    if (this.playerData.getCell(y, pi) == -1) {
		return false;
	    }
	}
	return true;
    }

    public void enableHorizontalWraparound() {
	this.horizontalWraparoundEnabled = true;
    }

    public void disableHorizontalWraparound() {
	this.horizontalWraparoundEnabled = false;
    }

    public void enableVerticalWraparound() {
	this.verticalWraparoundEnabled = true;
    }

    public void disableVerticalWraparound() {
	this.verticalWraparoundEnabled = false;
    }

    public void enableThirdDimensionWraparound() {
	this.thirdDimensionWraparoundEnabled = true;
    }

    public void disableThirdDimensionWraparound() {
	this.thirdDimensionWraparoundEnabled = false;
    }

    public boolean isHorizontalWraparoundEnabled() {
	return this.horizontalWraparoundEnabled;
    }

    public boolean isVerticalWraparoundEnabled() {
	return this.verticalWraparoundEnabled;
    }

    public boolean isThirdDimensionWraparoundEnabled() {
	return this.thirdDimensionWraparoundEnabled;
    }

    public void writeLevelInfo(final XMLFileWriter writer) throws IOException {
	int x, y;
	for (y = 0; y < ArenaConstants.PLAYER_DIMS; y++) {
	    for (x = 0; x < ArenaConstants.NUM_PLAYERS; x++) {
		writer.writeInt(this.playerData.getCell(y, x));
	    }
	}
	writer.writeBoolean(this.horizontalWraparoundEnabled);
	writer.writeBoolean(this.verticalWraparoundEnabled);
	writer.writeBoolean(this.thirdDimensionWraparoundEnabled);
	writer.writeString(this.name);
	writer.writeString(this.hint);
	writer.writeString(this.author);
	writer.writeInt(this.difficulty);
	writer.writeBoolean(this.moveShootAllowed);
    }

    public static LevelInfo readLevelInfo(final XMLFileReader reader) throws IOException {
	LevelInfo li = new LevelInfo();
	int x, y;
	for (y = 0; y < 3; y++) {
	    for (x = 0; x < ArenaConstants.NUM_PLAYERS; x++) {
		li.playerData.setCell(reader.readInt(), y, x);
	    }
	}
	li.horizontalWraparoundEnabled = reader.readBoolean();
	li.verticalWraparoundEnabled = reader.readBoolean();
	li.thirdDimensionWraparoundEnabled = reader.readBoolean();
	li.name = reader.readString();
	li.hint = reader.readString();
	li.author = reader.readString();
	li.difficulty = reader.readInt();
	li.moveShootAllowed = reader.readBoolean();
	return li;
    }
}
