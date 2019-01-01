/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena;

import java.io.File;
import java.io.IOException;

import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractButton;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractButtonDoor;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractCharacter;
import com.puttysoftware.ltremix.arena.current.CurrentArena;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;
import com.puttysoftware.ltremix.utilities.ProgressTracker;

public abstract class AbstractArena {
    // Constants
    private static final int MIN_LEVELS = 1;
    protected static final int MAX_LEVELS = Integer.MAX_VALUE;

    // Constructors
    public AbstractArena() throws IOException {
	// Do nothing
    }

    // Static methods
    public static String getArenaTempFolder() {
	return System
		.getProperty(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			StringConstants.NOTL_STRING_TEMP_DIR))
		+ File.separator
		+ StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_PROGRAM_NAME);
    }

    public static int getMinLevels() {
	return AbstractArena.MIN_LEVELS;
    }

    public static int getMaxLevels() {
	return AbstractArena.MAX_LEVELS;
    }

    public static int getMaxFloors() {
	return AbstractArenaData.getMaxFloors();
    }

    public static int getMinFloors() {
	return AbstractArenaData.getMinFloors();
    }

    public static int getMinRows() {
	return AbstractArenaData.getMinRows();
    }

    public static int getMinColumns() {
	return AbstractArenaData.getMinColumns();
    }

    public static int getProgressStages() {
	return CurrentArena.getProgressStages();
    }

    // Methods
    public abstract boolean isEraChangeAllowed();

    public abstract void setEraChangeAllowed(final boolean value);

    public abstract boolean isMoveShootAllowed();

    public abstract boolean isMoveShootAllowedInternal();

    public abstract void setMoveShootAllowed(boolean value);

    public abstract String getName();

    public abstract void setName(String newName);

    public abstract String getHint();

    public abstract void setHint(String newHint);

    public abstract String getAuthor();

    public abstract void setAuthor(String newAuthor);

    public abstract int getDifficulty();

    public abstract void setDifficulty(int newDifficulty);

    public abstract String getBasePath();

    public abstract void setPrefixHandler(AbstractPrefixIO xph);

    public abstract void setSuffixHandler(AbstractSuffixIO xsh);

    public abstract int getActiveLevelNumber();

    public final boolean switchToNextLevelWithDifficulty(final int[] difficulty) {
	boolean keepGoing = true;
	while (keepGoing) {
	    final int diff = this.getDifficulty();
	    for (final int element : difficulty) {
		if (diff - 1 == element) {
		    keepGoing = false;
		    return true;
		}
	    }
	    if (!this.doesLevelExistOffset(1)) {
		keepGoing = false;
		return false;
	    }
	    if (keepGoing) {
		this.switchLevelOffset(1);
	    }
	}
	return false;
    }

    public final boolean switchToPreviousLevelWithDifficulty(final int[] difficulty) {
	boolean keepGoing = true;
	while (keepGoing) {
	    final int diff = this.getDifficulty();
	    for (final int element : difficulty) {
		if (diff - 1 == element) {
		    keepGoing = false;
		    return true;
		}
	    }
	    if (!this.doesLevelExistOffset(-1)) {
		keepGoing = false;
		return false;
	    }
	    if (keepGoing) {
		this.switchLevelOffset(-1);
	    }
	}
	return false;
    }

    public abstract String[] generateLevelInfoList(final ProgressTracker pt);

    public abstract void switchLevel(int level);

    public abstract void switchLevelOffset(int level);

    protected abstract void switchLevelInternal(int level);

    public abstract boolean doesLevelExist(int level);

    public abstract boolean doesLevelExistOffset(int level);

    public abstract void cutLevel();

    public abstract void copyLevel();

    public abstract void pasteLevel();

    public abstract boolean isPasteBlocked();

    public abstract boolean isCutBlocked();

    public abstract boolean insertLevelFromClipboard();

    public abstract boolean addLevel();

    public final boolean removeLevel(final int num) {
	final int saveLevel = this.getActiveLevelNumber();
	this.switchLevel(num);
	final boolean success = this.removeActiveLevel();
	if (success) {
	    if (saveLevel == 0) {
		// Was at first level
		this.switchLevel(0);
	    } else {
		// Was at level other than first
		if (saveLevel > num) {
		    // Saved level was shifted down
		    this.switchLevel(saveLevel - 1);
		} else if (saveLevel < num) {
		    // Saved level was NOT shifted down
		    this.switchLevel(saveLevel);
		} else {
		    // Saved level was deleted
		    this.switchLevel(0);
		}
	    }
	} else {
	    this.switchLevel(saveLevel);
	}
	return success;
    }

    protected abstract boolean removeActiveLevel();

    public abstract boolean isCellDirty(final int row, final int col, final int floor);

    public abstract AbstractArenaObject getCell(final int row, final int col, final int floor, final int layer);

    public abstract AbstractArenaObject getCellEra(final int row, final int col, final int floor, final int layer,
	    final int era);

    public abstract AbstractArenaObject getVirtualCell(final int row, final int col, final int floor, final int layer);

    public abstract int getStartRow();

    public abstract int getStartColumn();

    public abstract int getStartFloor();

    public static int getStartLevel() {
	return 0;
    }

    public abstract int getPlayerRow(final int playerNum, final int era);

    public abstract int getPlayerColumn(final int playerNum, final int era);

    public abstract int getPlayerFloor(final int playerNum, final int era);

    public abstract int getRows();

    public abstract int getColumns();

    public abstract int getFloors();

    public abstract int getLevels();

    public abstract int getEra();

    public abstract void setEra(final int newEra);

    public abstract boolean doesPlayerExist();

    public abstract boolean findStart();

    public abstract void tickTimers(final int floor, final int actionType);

    public abstract void checkForEnemies(final int floor, final int ex, final int ey, final AbstractArenaObject e);

    public abstract int checkForMagnetic(int floor, int centerX, int centerY, int dir);

    public abstract int[] tunnelScan(final int x, final int y, final int z, final String targetName);

    public abstract void circularScanRange(final int x, final int y, final int z, final int maxR, final int rangeType,
	    final int forceUnits);

    public abstract int[] findObject(int z, String targetName);

    public abstract boolean circularScanTank(final int x, final int y, final int z, final int maxR);

    public abstract void fullScanActivateTanks();

    public abstract void fullScanProcessTanks(final AbstractCharacter activeTank);

    public abstract void fullScanMoveObjects(final int locZ, final int dirX, final int dirY);

    public abstract void fullScanKillTanks();

    public abstract void fullScanFreezeGround();

    public abstract void fullScanAllButtonOpen(int z, AbstractButton source);

    public abstract void fullScanAllButtonClose(int z, AbstractButton source);

    public abstract void fullScanButtonBind(int dx, int dy, int z, AbstractButtonDoor source);

    public abstract void fullScanButtonCleanup(int px, int py, int z, AbstractButton button);

    public abstract void fullScanFindButtonLostDoor(int z, AbstractButtonDoor door);

    public abstract void setCell(final AbstractArenaObject mo, final int row, final int col, final int floor,
	    final int layer);

    public abstract void setCellEra(final AbstractArenaObject mo, final int row, final int col, final int floor,
	    final int layer, final int era);

    public abstract void setVirtualCell(final AbstractArenaObject mo, final int row, final int col, final int floor,
	    final int layer);

    public abstract void markAsDirty(final int row, final int col, final int floor);

    public abstract void markAsClean(final int row, final int col, final int floor);

    public abstract void clearDirtyFlags(int floor);

    public abstract void setDirtyFlags(int floor);

    public abstract void clearVirtualGrid();

    public abstract void setPlayerRow(final int newPlayerRow, final int playerNum, final int era);

    public abstract void setPlayerColumn(final int newPlayerColumn, final int playerNum, final int era);

    public abstract void setPlayerFloor(final int newPlayerFloor, final int playerNum, final int era);

    public abstract void fillDefault();

    public abstract void save();

    public abstract void restore();

    public abstract void resize(int z, AbstractArenaObject nullFill);

    public abstract void setData(AbstractArenaData newData, int count);

    public abstract void saveStart();

    public abstract void restoreStart();

    public abstract void enableHorizontalWraparound();

    public abstract void disableHorizontalWraparound();

    public abstract void enableVerticalWraparound();

    public abstract void disableVerticalWraparound();

    public abstract void enableThirdDimensionWraparound();

    public abstract void disableThirdDimensionWraparound();

    public abstract boolean isHorizontalWraparoundEnabled();

    public abstract boolean isVerticalWraparoundEnabled();

    public abstract boolean isThirdDimensionWraparoundEnabled();

    public abstract AbstractArena readArena(final ProgressTracker pt) throws IOException;

    public abstract void writeArena(final ProgressTracker pt) throws IOException;

    public abstract void undo();

    public abstract void redo();

    public abstract boolean tryUndo();

    public abstract boolean tryRedo();

    public abstract void updateUndoHistory(final HistoryStatus whatIs);

    public abstract void updateRedoHistory(final HistoryStatus whatIs);

    public abstract HistoryStatus getWhatWas();

    public abstract void resetHistoryEngine();
}