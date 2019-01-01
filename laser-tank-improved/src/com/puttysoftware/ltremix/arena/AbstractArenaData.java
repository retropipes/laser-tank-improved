package com.puttysoftware.ltremix.arena;

import java.io.IOException;

import com.puttysoftware.lasertank.improved.fileio.XMLFileReader;
import com.puttysoftware.lasertank.improved.fileio.XMLFileWriter;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractButton;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractButtonDoor;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractCharacter;
import com.puttysoftware.ltremix.utilities.ProgressTracker;

public abstract class AbstractArenaData implements Cloneable {
    // Constants
    protected final static int MIN_FLOORS = 1;
    protected final static int MAX_FLOORS = 9;
    protected final static int MIN_COLUMNS = 24;
    protected final static int MIN_ROWS = 24;

    // Static methods
    public static int getMinRows() {
	return AbstractArenaData.MIN_ROWS;
    }

    public static int getMinColumns() {
	return AbstractArenaData.MIN_COLUMNS;
    }

    public static int getMaxFloors() {
	return AbstractArenaData.MAX_FLOORS;
    }

    public static int getMinFloors() {
	return AbstractArenaData.MIN_FLOORS;
    }

    @Override
    public abstract AbstractArenaData clone();

    public abstract boolean isMoveShootAllowed();

    public abstract void setMoveShootAllowed(final boolean value);

    public abstract String getName();

    public abstract void setName(final String newName);

    public abstract String getHint();

    public abstract void setHint(final String newHint);

    public abstract String getAuthor();

    public abstract void setAuthor(final String newAuthor);

    public abstract int getDifficulty();

    public abstract void setDifficulty(final int newDifficulty);

    public abstract boolean isCellDirty(final int row, final int col, final int floor);

    public abstract AbstractArenaObject getCell(final int row, final int col, final int floor, final int layer);

    public abstract AbstractArenaObject getCellEra(final int row, final int col, final int floor, final int layer,
	    final int era);

    public abstract AbstractArenaObject getVirtualCell(final int row, final int col, final int floor, final int layer);

    public abstract int getStartRow();

    public abstract int getStartColumn();

    public abstract int getStartFloor();

    public abstract int getPlayerRow(final int playerNum, final int era);

    public abstract int getPlayerColumn(final int playerNum, final int era);

    public abstract int getPlayerFloor(final int playerNum, final int era);

    public abstract int getRows();

    public abstract int getColumns();

    public abstract int getFloors();

    public abstract int getEra();

    public abstract void setEra(final int newEra);

    public abstract boolean doesPlayerExist();

    public abstract boolean findStart();

    public abstract void tickTimers(final int floor, final int actionType);

    public abstract void checkForEnemies(final int floorIn, final int enemyLocXIn, final int enemyLocYIn,
	    final AbstractArenaObject enemy);

    public abstract int checkForMagnetic(final int floor, final int centerX, final int centerY, final int dir);

    public abstract boolean linearScan(final int xIn, final int yIn, final int zIn, final int d);

    public abstract int linearScanMagnetic(final int xIn, final int yIn, final int zIn, final int d);

    public abstract int[] findObject(final int z, final String targetName);

    public abstract int[] tunnelScan(final int xIn, final int yIn, final int zIn, final String targetName);

    public abstract void circularScanRange(final int xIn, final int yIn, final int zIn, final int r,
	    final int rangeType, final int forceUnits);

    public abstract boolean circularScanTank(final int x, final int y, final int z, final int r);

    public abstract void fullScanActivateTanks();

    public abstract void fullScanProcessTanks(final AbstractCharacter activeTank);

    public abstract void fullScanMoveObjects(final int locZ, final int dirX, final int dirY);

    public abstract void fullScanKillTanks();

    public abstract void fullScanFreezeGround();

    public abstract void fullScanAllButtonOpen(final int zIn, final AbstractButton source);

    public abstract void fullScanAllButtonClose(final int zIn, final AbstractButton source);

    public abstract void fullScanButtonBind(final int dx, final int dy, final int zIn, final AbstractButtonDoor source);

    public abstract void fullScanButtonCleanup(final int px, final int py, final int zIn, final AbstractButton button);

    public abstract void fullScanFindButtonLostDoor(final int zIn, final AbstractButtonDoor door);

    public abstract void setCell(final AbstractArenaObject mo, final int row, final int col, final int floor,
	    final int layer);

    public abstract void setCellEra(final AbstractArenaObject mo, final int row, final int col, final int floor,
	    final int layer, final int era);

    public abstract void markAsDirty(final int row, final int col, final int floor);

    public abstract void markAsClean(final int row, final int col, final int floor);

    public abstract void setVirtualCell(final AbstractArenaObject mo, final int row, final int col, final int floor,
	    final int layer);

    public abstract void setAllDirtyFlags();

    public abstract void clearDirtyFlags(final int floor);

    public abstract void setDirtyFlags(final int floor);

    public abstract void clearVirtualGrid();

    public abstract void setPlayerRow(final int newPlayerRow, final int playerNum, final int era);

    public abstract void setPlayerColumn(final int newPlayerColumn, final int playerNum, final int era);

    public abstract void setPlayerFloor(final int newPlayerFloor, final int playerNum, final int era);

    public abstract void fill(final AbstractArenaObject fillWith);

    public abstract void fillVirtual();

    public abstract void save();

    public abstract void restore();

    public abstract void saveStart();

    public abstract void restoreStart();

    public abstract void resize(final int zIn, final AbstractArenaObject nullFill);

    public abstract void resizeSavedState(final int z, final AbstractArenaObject nullFill);

    public abstract void fillNulls(final AbstractArenaObject fill1, final AbstractArenaObject fill2,
	    final boolean was16);

    public abstract void fillSTSNulls(final AbstractArenaObject fillWith);

    protected final int normalizeRow(final int row) {
	int fR = row;
	if (fR < 0) {
	    fR += this.getRows();
	    while (fR < 0) {
		fR += this.getRows();
	    }
	} else if (fR > this.getRows() - 1) {
	    fR -= this.getRows();
	    while (fR > this.getRows() - 1) {
		fR -= this.getRows();
	    }
	}
	return fR;
    }

    protected final int normalizeColumn(final int column) {
	int fC = column;
	if (fC < 0) {
	    fC += this.getColumns();
	    while (fC < 0) {
		fC += this.getColumns();
	    }
	} else if (fC > this.getColumns() - 1) {
	    fC -= this.getColumns();
	    while (fC > this.getColumns() - 1) {
		fC -= this.getColumns();
	    }
	}
	return fC;
    }

    protected final int normalizeFloor(final int floor) {
	int fF = floor;
	if (fF < 0) {
	    fF += this.getFloors();
	    while (fF < 0) {
		fF += this.getFloors();
	    }
	} else if (fF > this.getFloors() - 1) {
	    fF -= this.getFloors();
	    while (fF > this.getFloors() - 1) {
		fF -= this.getFloors();
	    }
	}
	return fF;
    }

    public abstract void enableHorizontalWraparound();

    public abstract void disableHorizontalWraparound();

    public abstract void enableVerticalWraparound();

    public abstract void disableVerticalWraparound();

    public abstract void enableThirdDimensionWraparound();

    public abstract void disableThirdDimensionWraparound();

    public abstract boolean isHorizontalWraparoundEnabled();

    public abstract boolean isVerticalWraparoundEnabled();

    public abstract boolean isThirdDimensionWraparoundEnabled();

    public abstract void writeData(final XMLFileWriter writer, final ProgressTracker pt) throws IOException;

    public abstract AbstractArenaData readData(final XMLFileReader reader, final int ver, final ProgressTracker pt)
	    throws IOException;

    public abstract void writeSavedState(final XMLFileWriter writer, final ProgressTracker pt) throws IOException;

    public abstract void readSavedState(final XMLFileReader reader, final int formatVersion, final ProgressTracker pt)
	    throws IOException;

    public abstract void undo();

    public abstract void redo();

    public abstract boolean tryUndo();

    public abstract boolean tryRedo();

    public abstract void clearUndoHistory();

    public abstract void clearRedoHistory();

    public abstract void updateUndoHistory(final HistoryStatus whatIs);

    public abstract void updateRedoHistory(final HistoryStatus whatIs);

    public abstract HistoryStatus getWhatWas();

    public abstract void resetHistoryEngine();
}
