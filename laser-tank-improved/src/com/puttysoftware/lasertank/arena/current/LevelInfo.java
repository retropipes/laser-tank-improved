/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.current;

import java.io.IOException;

import com.puttysoftware.fileio.XMLFileReader;
import com.puttysoftware.fileio.XMLFileWriter;
import com.puttysoftware.lasertank.strings.CommonString;
import com.puttysoftware.lasertank.strings.GenericString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.storage.NumberStorage;

public final class LevelInfo {
    public static LevelInfo readLevelInfo(final XMLFileReader reader) throws IOException {
	final LevelInfo li = new LevelInfo();
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
	this.name = StringLoader.loadGeneric(GenericString.UN_NAMED_LEVEL);
	this.author = StringLoader.loadGeneric(GenericString.UNKNOWN_AUTHOR);
	this.hint = StringLoader.loadCommon(CommonString.EMPTY);
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

    public void disableHorizontalWraparound() {
	this.horizontalWraparoundEnabled = false;
    }

    public void disableThirdDimensionWraparound() {
	this.thirdDimensionWraparoundEnabled = false;
    }

    public void disableVerticalWraparound() {
	this.verticalWraparoundEnabled = false;
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

    public void enableThirdDimensionWraparound() {
	this.thirdDimensionWraparoundEnabled = true;
    }

    public void enableVerticalWraparound() {
	this.verticalWraparoundEnabled = true;
    }

    public String getAuthor() {
	return this.author;
    }

    public int getDifficulty() {
	return this.difficulty;
    }

    public String getHint() {
	return this.hint;
    }

    public String getName() {
	return this.name;
    }

    public int getStartColumn(final int pi) {
	return this.playerData.getCell(0, pi);
    }

    public int getStartFloor(final int pi) {
	return this.playerData.getCell(2, pi);
    }

    public int getStartRow(final int pi) {
	return this.playerData.getCell(1, pi);
    }

    public boolean isHorizontalWraparoundEnabled() {
	return this.horizontalWraparoundEnabled;
    }

    public boolean isMoveShootAllowed() {
	return this.moveShootAllowed;
    }

    public boolean isThirdDimensionWraparoundEnabled() {
	return this.thirdDimensionWraparoundEnabled;
    }

    public boolean isVerticalWraparoundEnabled() {
	return this.verticalWraparoundEnabled;
    }

    public void setAuthor(final String newAuthor) {
	this.author = newAuthor;
    }

    public void setDifficulty(final int newDifficulty) {
	this.difficulty = newDifficulty;
    }

    public void setHint(final String newHint) {
	this.hint = newHint;
    }

    public void setMoveShootAllowed(final boolean value) {
	this.moveShootAllowed = value;
    }

    public void setName(final String newName) {
	this.name = newName;
    }

    public void setStartColumn(final int pi, final int value) {
	this.playerData.setCell(value, 0, pi);
    }

    public void setStartFloor(final int pi, final int value) {
	this.playerData.setCell(value, 2, pi);
    }

    public void setStartRow(final int pi, final int value) {
	this.playerData.setCell(value, 1, pi);
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
}
