package com.puttysoftware.lasertank.arena;

import java.io.IOException;

import com.puttysoftware.fileio.XMLFileReader;
import com.puttysoftware.fileio.XMLFileWriter;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractButton;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractButtonDoor;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractCharacter;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractTunnel;
import com.puttysoftware.lasertank.utilities.Direction;

public abstract class AbstractArenaData implements Cloneable {
    // Constants
    protected final static int MIN_FLOORS = 1;
    protected final static int MAX_FLOORS = 9;
    protected final static int MIN_COLUMNS = 24;
    protected final static int MIN_ROWS = 24;

    public static int getMaxFloors() {
	return AbstractArenaData.MAX_FLOORS;
    }

    public static int getMinColumns() {
	return AbstractArenaData.MIN_COLUMNS;
    }

    public static int getMinFloors() {
	return AbstractArenaData.MIN_FLOORS;
    }

    // Static methods
    public static int getMinRows() {
	return AbstractArenaData.MIN_ROWS;
    }

    public abstract void checkForEnemies(final AbstractArena arena, final int floorIn, final int enemyLocXIn,
	    final int enemyLocYIn, final AbstractCharacter enemy);

    public abstract int checkForMagnetic(final AbstractArena arena, final int floor, final int centerX,
	    final int centerY, final Direction dir);

    public abstract int[] circularScan(final AbstractArena arena, final int xIn, final int yIn, final int zIn,
	    final int r, final String targetName, boolean moved);

    public abstract void circularScanRange(final AbstractArena arena, final int xIn, final int yIn, final int zIn,
	    final int r, final int rangeType, final int forceUnits);

    public abstract boolean circularScanTank(final AbstractArena arena, final int x, final int y, final int z,
	    final int r);

    public abstract int[] circularScanTunnel(final AbstractArena arena, final int x, final int y, final int z,
	    final int maxR, final int tx, final int ty, final AbstractTunnel target, final boolean moved);

    public abstract void clearDirtyFlags(final int floor);

    public abstract void clearRedoHistory();

    public abstract void clearUndoHistory();

    public abstract void clearVirtualGrid(final AbstractArena arena);

    @Override
    public abstract AbstractArenaData clone();

    public abstract void fill(final AbstractArena arena, final AbstractArenaObject fillWith);

    public abstract void fillNulls(final AbstractArena arena, final AbstractArenaObject fill1,
	    final AbstractArenaObject fill2, final boolean was16);

    public abstract void fillSTSNulls(final AbstractArenaObject fillWith);

    public abstract void fillVirtual();

    public abstract int[] findObject(final AbstractArena arena, final int z, final AbstractArenaObject target);

    public abstract int[] findPlayer(final AbstractArena arena, final int number);

    public abstract void fullScanAllButtonClose(final AbstractArena arena, final int zIn, final AbstractButton source);

    public abstract void fullScanAllButtonOpen(final AbstractArena arena, final int zIn, final AbstractButton source);

    public abstract void fullScanButtonBind(final AbstractArena arena, final int dx, final int dy, final int zIn,
	    final AbstractButtonDoor source);

    public abstract void fullScanButtonCleanup(final AbstractArena arena, final int px, final int py, final int zIn,
	    final AbstractButton button);

    public abstract void fullScanFindButtonLostDoor(final AbstractArena arena, final int zIn,
	    final AbstractButtonDoor door);

    public abstract void fullScanFreezeGround(final AbstractArena arena);

    public abstract void fullScanKillTanks(final AbstractArena arena);

    public abstract AbstractArenaObject getCell(final AbstractArena arena, final int row, final int col,
	    final int floor, final int layer);

    public abstract int getColumns();

    public abstract int getFloors();

    public abstract int getRows();

    public abstract AbstractArenaObject getVirtualCell(final AbstractArena arena, final int row, final int col,
	    final int floor, final int layer);

    public abstract HistoryStatus getWhatWas();

    public abstract boolean isCellDirty(final AbstractArena arena, final int row, final int col, final int floor);

    public abstract boolean linearScan(final AbstractArena arena, final int xIn, final int yIn, final int zIn,
	    final Direction d);

    public abstract int linearScanMagnetic(final AbstractArena arena, final int xIn, final int yIn, final int zIn,
	    final Direction d);

    public abstract void markAsDirty(final AbstractArena arena, final int row, final int col, final int floor);

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

    public abstract AbstractArenaData readData(final AbstractArena arena, final XMLFileReader reader, final int ver)
	    throws IOException;

    public abstract void readSavedState(final XMLFileReader reader, final int formatVersion) throws IOException;

    public abstract void redo(final AbstractArena arena);

    public abstract void resetHistoryEngine();

    public abstract void resize(final AbstractArena arena, final int zIn, final AbstractArenaObject nullFill);

    public abstract void resizeSavedState(final int z, final AbstractArenaObject nullFill);

    public abstract void restore(final AbstractArena arena);

    public abstract void save(final AbstractArena arena);

    public abstract void setAllDirtyFlags();

    public abstract void setCell(final AbstractArena arena, final AbstractArenaObject mo, final int row, final int col,
	    final int floor, final int layer);

    public abstract void setDirtyFlags(final int floor);

    public abstract void setVirtualCell(final AbstractArena arena, final AbstractArenaObject mo, final int row,
	    final int col, final int floor, final int layer);

    public abstract void tickTimers(final AbstractArena arena, final int floor, final int actionType);

    public abstract boolean tryRedo();

    public abstract boolean tryUndo();

    public abstract void undo(final AbstractArena arena);

    public abstract void updateRedoHistory(final HistoryStatus whatIs);

    public abstract void updateUndoHistory(final HistoryStatus whatIs);

    public abstract void writeData(final AbstractArena arena, final XMLFileWriter writer) throws IOException;

    public abstract void writeSavedState(final XMLFileWriter writer) throws IOException;
}
