/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.current;

import java.io.IOException;
import java.util.ArrayDeque;

import com.puttysoftware.fileio.XMLFileReader;
import com.puttysoftware.fileio.XMLFileWriter;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.arena.AbstractArenaData;
import com.puttysoftware.lasertank.arena.HistoryStatus;
import com.puttysoftware.lasertank.arena.LowLevelArenaDataStore;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractButton;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractButtonDoor;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractCharacter;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractTunnel;
import com.puttysoftware.lasertank.arena.objects.AntiTank;
import com.puttysoftware.lasertank.arena.objects.AntiTankDisguise;
import com.puttysoftware.lasertank.arena.objects.DeadAntiTank;
import com.puttysoftware.lasertank.arena.objects.Empty;
import com.puttysoftware.lasertank.arena.objects.Ground;
import com.puttysoftware.lasertank.arena.objects.Tank;
import com.puttysoftware.lasertank.arena.objects.Wall;
import com.puttysoftware.lasertank.game.GameManager;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.strings.ErrorString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.DirectionResolver;
import com.puttysoftware.lasertank.utilities.FormatConstants;
import com.puttysoftware.lasertank.utilities.MaterialConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;
import com.puttysoftware.storage.FlagStorage;

public final class CurrentArenaData extends AbstractArenaData {
    private class ImageUndoEngine {
	// Inner classes
	private class HistoryEntry {
	    // Fields
	    private final LowLevelArenaDataStore histImage;
	    private final HistoryStatus histWhatWas;

	    HistoryEntry(final LowLevelArenaDataStore i, final HistoryStatus hww) {
		this.histImage = i;
		this.histWhatWas = hww;
	    }

	    public LowLevelArenaDataStore getImage() {
		return this.histImage;
	    }

	    public HistoryStatus getWhatWas() {
		return this.histWhatWas;
	    }
	}

	private class HistoryStack {
	    // Fields
	    private final ArrayDeque<HistoryEntry> stack;

	    HistoryStack() {
		this.stack = new ArrayDeque<>();
	    }

	    public boolean isEmpty() {
		return this.stack.isEmpty();
	    }

	    public HistoryEntry pop() {
		return this.stack.removeFirst();
	    }

	    public void push(final LowLevelArenaDataStore i, final HistoryStatus hww) {
		final HistoryEntry newEntry = new HistoryEntry(i, hww);
		this.stack.addFirst(newEntry);
	    }
	}

	// Fields
	private HistoryStack undoHistory, redoHistory;
	private HistoryStatus whatWas;
	private LowLevelArenaDataStore image;

	// Constructors
	public ImageUndoEngine() {
	    this.undoHistory = new HistoryStack();
	    this.redoHistory = new HistoryStack();
	    this.image = null;
	    this.whatWas = null;
	}

	public void clearRedoHistory() {
	    this.redoHistory = new HistoryStack();
	}

	public void clearUndoHistory() {
	    this.undoHistory = new HistoryStack();
	}

	public LowLevelArenaDataStore getImage() {
	    return this.image;
	}

	public HistoryStatus getWhatWas() {
	    return this.whatWas;
	}

	public void redo() {
	    if (!this.redoHistory.isEmpty()) {
		final HistoryEntry entry = this.redoHistory.pop();
		this.image = entry.getImage();
		this.whatWas = entry.getWhatWas();
	    } else {
		this.image = null;
		this.whatWas = null;
	    }
	}

	public boolean tryRedo() {
	    return !this.redoHistory.isEmpty();
	}

	public boolean tryUndo() {
	    return !this.undoHistory.isEmpty();
	}

	// Public methods
	public void undo() {
	    if (!this.undoHistory.isEmpty()) {
		final HistoryEntry entry = this.undoHistory.pop();
		this.image = entry.getImage();
		this.whatWas = entry.getWhatWas();
	    } else {
		this.image = null;
		this.whatWas = null;
	    }
	}

	public void updateRedoHistory(final LowLevelArenaDataStore newImage, final HistoryStatus newWhatWas) {
	    this.redoHistory.push(newImage, newWhatWas);
	}

	public void updateUndoHistory(final LowLevelArenaDataStore newImage, final HistoryStatus newWhatWas) {
	    this.undoHistory.push(newImage, newWhatWas);
	}
    }

    public static final CurrentArenaLock LOCK_OBJECT = new CurrentArenaLock();

    private static CurrentArenaData readDataG1(final AbstractArena arena, final XMLFileReader reader, final int ver)
	    throws IOException {
	int y, x, z, arenaSizeX, arenaSizeY, arenaSizeZ;
	arenaSizeX = reader.readInt();
	arenaSizeY = reader.readInt();
	arenaSizeZ = reader.readInt();
	final CurrentArenaData lt = new CurrentArenaData();
	for (x = 0; x < arenaSizeX; x++) {
	    for (y = 0; y < arenaSizeY; y++) {
		for (z = 0; z < arenaSizeZ; z++) {
		    final AbstractArenaObject obj = LaserTank.getApplication().getObjects().readArenaObjectG2(reader,
			    ver);
		    lt.setCell(arena, obj, y, x, z, obj.getLayer());
		}
	    }
	}
	arena.setStartColumn(0, reader.readInt());
	arena.setStartRow(0, reader.readInt());
	arena.setStartFloor(0, reader.readInt());
	final boolean horzWrap = reader.readBoolean();
	if (horzWrap) {
	    arena.enableHorizontalWraparound();
	} else {
	    arena.disableHorizontalWraparound();
	}
	final boolean vertWrap = reader.readBoolean();
	if (vertWrap) {
	    arena.enableVerticalWraparound();
	} else {
	    arena.disableVerticalWraparound();
	}
	arena.disableThirdDimensionWraparound();
	arena.setName(reader.readString());
	arena.setHint(reader.readString());
	arena.setAuthor(reader.readString());
	arena.setDifficulty(reader.readInt());
	arena.setMoveShootAllowedThisLevel(false);
	// Fill nulls
	lt.fillNulls(arena, new Ground(), new Wall(), true);
	lt.fillVirtual();
	return lt;
    }

    private static CurrentArenaData readDataG2(final AbstractArena arena, final XMLFileReader reader, final int ver)
	    throws IOException {
	int y, x, z, arenaSizeX, arenaSizeY, arenaSizeZ;
	arenaSizeX = reader.readInt();
	arenaSizeY = reader.readInt();
	arenaSizeZ = reader.readInt();
	final CurrentArenaData lt = new CurrentArenaData();
	lt.resize(arena, arenaSizeZ, new Ground());
	for (x = 0; x < arenaSizeX; x++) {
	    for (y = 0; y < arenaSizeY; y++) {
		for (z = 0; z < arenaSizeZ; z++) {
		    final AbstractArenaObject obj = LaserTank.getApplication().getObjects().readArenaObjectG2(reader,
			    ver);
		    lt.setCell(arena, obj, y, x, z, obj.getLayer());
		}
	    }
	}
	arena.setStartColumn(0, reader.readInt());
	arena.setStartRow(0, reader.readInt());
	arena.setStartFloor(0, reader.readInt());
	final boolean horzWrap = reader.readBoolean();
	if (horzWrap) {
	    arena.enableHorizontalWraparound();
	} else {
	    arena.disableHorizontalWraparound();
	}
	final boolean vertWrap = reader.readBoolean();
	if (vertWrap) {
	    arena.enableVerticalWraparound();
	} else {
	    arena.disableVerticalWraparound();
	}
	final boolean thirdWrap = reader.readBoolean();
	if (thirdWrap) {
	    arena.enableThirdDimensionWraparound();
	} else {
	    arena.disableThirdDimensionWraparound();
	}
	arena.setName(reader.readString());
	arena.setHint(reader.readString());
	arena.setAuthor(reader.readString());
	arena.setDifficulty(reader.readInt());
	arena.setMoveShootAllowedThisLevel(false);
	// Fill nulls
	lt.fillNulls(arena, new Ground(), null, false);
	lt.fillVirtual();
	return lt;
    }

    private static CurrentArenaData readDataG3(final AbstractArena arena, final XMLFileReader reader, final int ver)
	    throws IOException {
	int y, x, z, arenaSizeX, arenaSizeY, arenaSizeZ;
	arenaSizeX = reader.readInt();
	arenaSizeY = reader.readInt();
	arenaSizeZ = reader.readInt();
	final CurrentArenaData lt = new CurrentArenaData();
	lt.resize(arena, arenaSizeZ, new Ground());
	for (x = 0; x < arenaSizeX; x++) {
	    for (y = 0; y < arenaSizeY; y++) {
		for (z = 0; z < arenaSizeZ; z++) {
		    final AbstractArenaObject obj = LaserTank.getApplication().getObjects().readArenaObjectG3(reader,
			    ver);
		    lt.setCell(arena, obj, y, x, z, obj.getLayer());
		}
	    }
	}
	arena.setStartColumn(0, reader.readInt());
	arena.setStartRow(0, reader.readInt());
	arena.setStartFloor(0, reader.readInt());
	final boolean horzWrap = reader.readBoolean();
	if (horzWrap) {
	    arena.enableHorizontalWraparound();
	} else {
	    arena.disableHorizontalWraparound();
	}
	final boolean vertWrap = reader.readBoolean();
	if (vertWrap) {
	    arena.enableVerticalWraparound();
	} else {
	    arena.disableVerticalWraparound();
	}
	final boolean thirdWrap = reader.readBoolean();
	if (thirdWrap) {
	    arena.enableThirdDimensionWraparound();
	} else {
	    arena.disableThirdDimensionWraparound();
	}
	arena.setName(reader.readString());
	arena.setHint(reader.readString());
	arena.setAuthor(reader.readString());
	arena.setDifficulty(reader.readInt());
	arena.setMoveShootAllowedThisLevel(false);
	// Fill nulls
	lt.fillNulls(arena, new Ground(), null, false);
	lt.fillVirtual();
	return lt;
    }

    private static CurrentArenaData readDataG4(final AbstractArena arena, final XMLFileReader reader, final int ver)
	    throws IOException {
	int y, x, z, arenaSizeX, arenaSizeY, arenaSizeZ;
	arenaSizeX = reader.readInt();
	arenaSizeY = reader.readInt();
	arenaSizeZ = reader.readInt();
	final CurrentArenaData lt = new CurrentArenaData();
	lt.resize(arena, arenaSizeZ, new Ground());
	for (x = 0; x < arenaSizeX; x++) {
	    for (y = 0; y < arenaSizeY; y++) {
		for (z = 0; z < arenaSizeZ; z++) {
		    final AbstractArenaObject obj = LaserTank.getApplication().getObjects().readArenaObjectG4(reader,
			    ver);
		    lt.setCell(arena, obj, y, x, z, obj.getLayer());
		}
	    }
	}
	arena.setStartColumn(0, reader.readInt());
	arena.setStartRow(0, reader.readInt());
	arena.setStartFloor(0, reader.readInt());
	final boolean horzWrap = reader.readBoolean();
	if (horzWrap) {
	    arena.enableHorizontalWraparound();
	} else {
	    arena.disableHorizontalWraparound();
	}
	final boolean vertWrap = reader.readBoolean();
	if (vertWrap) {
	    arena.enableVerticalWraparound();
	} else {
	    arena.disableVerticalWraparound();
	}
	final boolean thirdWrap = reader.readBoolean();
	if (thirdWrap) {
	    arena.enableThirdDimensionWraparound();
	} else {
	    arena.disableThirdDimensionWraparound();
	}
	arena.setName(reader.readString());
	arena.setHint(reader.readString());
	arena.setAuthor(reader.readString());
	arena.setDifficulty(reader.readInt());
	arena.setMoveShootAllowedThisLevel(false);
	// Fill nulls
	lt.fillNulls(arena, new Ground(), null, false);
	lt.fillVirtual();
	return lt;
    }

    private static CurrentArenaData readDataG5(final AbstractArena arena, final XMLFileReader reader, final int ver)
	    throws IOException {
	int y, x, z, w, arenaSizeX, arenaSizeY, arenaSizeZ;
	arenaSizeX = reader.readInt();
	arenaSizeY = reader.readInt();
	arenaSizeZ = reader.readInt();
	final CurrentArenaData lt = new CurrentArenaData();
	lt.resize(arena, arenaSizeZ, new Ground());
	for (x = 0; x < arenaSizeX; x++) {
	    for (y = 0; y < arenaSizeY; y++) {
		for (z = 0; z < arenaSizeZ; z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			lt.setCell(arena, LaserTank.getApplication().getObjects().readArenaObjectG5(reader, ver), y, x,
				z, w);
		    }
		}
	    }
	}
	arena.setStartColumn(0, reader.readInt());
	arena.setStartRow(0, reader.readInt());
	arena.setStartFloor(0, reader.readInt());
	final boolean horzWrap = reader.readBoolean();
	if (horzWrap) {
	    arena.enableHorizontalWraparound();
	} else {
	    arena.disableHorizontalWraparound();
	}
	final boolean vertWrap = reader.readBoolean();
	if (vertWrap) {
	    arena.enableVerticalWraparound();
	} else {
	    arena.disableVerticalWraparound();
	}
	final boolean thirdWrap = reader.readBoolean();
	if (thirdWrap) {
	    arena.enableThirdDimensionWraparound();
	} else {
	    arena.disableThirdDimensionWraparound();
	}
	arena.setName(reader.readString());
	arena.setHint(reader.readString());
	arena.setAuthor(reader.readString());
	arena.setDifficulty(reader.readInt());
	arena.setMoveShootAllowedThisLevel(reader.readBoolean());
	// Fill nulls
	lt.fillNulls(arena, new Ground(), null, false);
	lt.fillVirtual();
	return lt;
    }

    private static CurrentArenaData readDataG6(final AbstractArena arena, final XMLFileReader reader, final int ver)
	    throws IOException {
	int y, x, z, w, arenaSizeX, arenaSizeY, arenaSizeZ;
	arenaSizeX = reader.readInt();
	arenaSizeY = reader.readInt();
	arenaSizeZ = reader.readInt();
	final CurrentArenaData lt = new CurrentArenaData();
	lt.resize(arena, arenaSizeZ, new Ground());
	for (x = 0; x < arenaSizeX; x++) {
	    for (y = 0; y < arenaSizeY; y++) {
		for (z = 0; z < arenaSizeZ; z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			lt.setCell(arena, LaserTank.getApplication().getObjects().readArenaObjectG6(reader, ver), y, x,
				z, w);
		    }
		}
	    }
	}
	// Fill nulls
	lt.fillNulls(arena, new Ground(), null, false);
	lt.fillVirtual();
	return lt;
    }

    // Properties
    private LowLevelArenaDataStore data;
    private LowLevelArenaDataStore virtualData;
    private FlagStorage dirtyData;
    private LowLevelArenaDataStore savedState;
    private int foundX, foundY;
    private ImageUndoEngine iue;

    // Constructors
    public CurrentArenaData() {
	this.data = new LowLevelArenaDataStore(AbstractArenaData.MIN_COLUMNS, AbstractArenaData.MIN_ROWS,
		AbstractArenaData.MIN_FLOORS, ArenaConstants.NUM_LAYERS);
	this.virtualData = new LowLevelArenaDataStore(AbstractArenaData.MIN_COLUMNS, AbstractArenaData.MIN_ROWS,
		AbstractArenaData.MIN_FLOORS, ArenaConstants.NUM_VIRTUAL_LAYERS);
	this.fillVirtual();
	this.dirtyData = new FlagStorage(AbstractArenaData.MIN_COLUMNS, AbstractArenaData.MIN_ROWS,
		AbstractArenaData.MIN_FLOORS);
	this.savedState = new LowLevelArenaDataStore(AbstractArenaData.MIN_ROWS, AbstractArenaData.MIN_COLUMNS,
		AbstractArenaData.MIN_FLOORS, ArenaConstants.NUM_LAYERS);
	this.foundX = -1;
	this.foundY = -1;
	this.iue = new ImageUndoEngine();
    }

    @Override
    public void checkForEnemies(final AbstractArena arena, final int floorIn, final int enemyLocXIn,
	    final int enemyLocYIn, final AbstractCharacter enemy) {
	if (enemy instanceof AntiTankDisguise) {
	    // Anti Tanks are fooled by disguises
	    return;
	}
	final AntiTank template = new AntiTank();
	int enemyLocX = enemyLocXIn;
	int enemyLocY = enemyLocYIn;
	int floor = floorIn;
	if (arena.isVerticalWraparoundEnabled()) {
	    enemyLocX = this.normalizeColumn(enemyLocX);
	}
	if (arena.isHorizontalWraparoundEnabled()) {
	    enemyLocY = this.normalizeRow(enemyLocY);
	}
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    floor = this.normalizeFloor(floor);
	}
	final boolean scanE = this.linearScan(arena, enemyLocX, enemyLocY, floor, Direction.EAST);
	if (scanE) {
	    try {
		final AntiTank at = (AntiTank) this.getCell(arena, this.foundX, this.foundY, floor,
			template.getLayer());
		at.kill(this.foundX, this.foundY);
	    } catch (final ClassCastException cce) {
		// Ignore
	    }
	}
	final boolean scanW = this.linearScan(arena, enemyLocX, enemyLocY, floor, Direction.WEST);
	if (scanW) {
	    try {
		final AntiTank at = (AntiTank) this.getCell(arena, this.foundX, this.foundY, floor,
			template.getLayer());
		at.kill(this.foundX, this.foundY);
	    } catch (final ClassCastException cce) {
		// Ignore
	    }
	}
	final boolean scanS = this.linearScan(arena, enemyLocX, enemyLocY, floor, Direction.SOUTH);
	if (scanS) {
	    try {
		final AntiTank at = (AntiTank) this.getCell(arena, this.foundX, this.foundY, floor,
			template.getLayer());
		at.kill(this.foundX, this.foundY);
	    } catch (final ClassCastException cce) {
		// Ignore
	    }
	}
	final boolean scanN = this.linearScan(arena, enemyLocX, enemyLocY, floor, Direction.NORTH);
	if (scanN) {
	    try {
		final AntiTank at = (AntiTank) this.getCell(arena, this.foundX, this.foundY, floor,
			template.getLayer());
		at.kill(this.foundX, this.foundY);
	    } catch (final ClassCastException cce) {
		// Ignore
	    }
	}
    }

    @Override
    public int checkForMagnetic(final AbstractArena arena, final int floor, final int centerX, final int centerY,
	    final Direction dir) {
	if (dir == Direction.EAST) {
	    return this.linearScanMagnetic(arena, centerX, centerY, floor, Direction.EAST);
	} else if (dir == Direction.WEST) {
	    return this.linearScanMagnetic(arena, centerX, centerY, floor, Direction.WEST);
	} else if (dir == Direction.SOUTH) {
	    return this.linearScanMagnetic(arena, centerX, centerY, floor, Direction.SOUTH);
	} else if (dir == Direction.NORTH) {
	    return this.linearScanMagnetic(arena, centerX, centerY, floor, Direction.NORTH);
	}
	return 0;
    }

    @Override
    public int[] circularScan(final AbstractArena arena, final int xIn, final int yIn, final int zIn, final int r,
	    final String targetName, final boolean moved) {
	int xFix = xIn;
	int yFix = yIn;
	int zFix = zIn;
	if (arena.isVerticalWraparoundEnabled()) {
	    xFix = this.normalizeColumn(xFix);
	}
	if (arena.isHorizontalWraparoundEnabled()) {
	    yFix = this.normalizeRow(yFix);
	}
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    zFix = this.normalizeFloor(zFix);
	}
	int u, v, w;
	u = v = 0;
	// Perform the scan
	for (u = xFix - r; u <= xFix + r; u++) {
	    for (v = yFix - r; v <= yFix + r; v++) {
		for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
		    try {
			final AbstractArenaObject obj = this.getCell(arena, v, u, zFix, w);
			final AbstractArenaObject savedObj = obj.getSavedObject();
			String testName;
			if (obj.isOfType(TypeConstants.TYPE_CHARACTER)) {
			    if (moved) {
				testName = obj.getImageName();
			    } else {
				testName = savedObj.getImageName();
			    }
			} else {
			    testName = obj.getImageName();
			}
			if (testName.equals(targetName)) {
			    return new int[] { v, u, zFix };
			}
		    } catch (final ArrayIndexOutOfBoundsException aioob) {
			// Do nothing
		    }
		}
	    }
	}
	return null;
    }

    @Override
    public void circularScanRange(final AbstractArena arena, final int xIn, final int yIn, final int zIn, final int r,
	    final int rangeType, final int forceUnits) {
	int xFix = xIn;
	int yFix = yIn;
	int zFix = zIn;
	if (arena.isVerticalWraparoundEnabled()) {
	    xFix = this.normalizeColumn(xFix);
	}
	if (arena.isHorizontalWraparoundEnabled()) {
	    yFix = this.normalizeRow(yFix);
	}
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    zFix = this.normalizeFloor(zFix);
	}
	int u, v, w;
	u = v = 0;
	// Perform the scan
	for (u = xFix - r; u <= xFix + r; u++) {
	    for (v = yFix - r; v <= yFix + r; v++) {
		if (u == xFix && v == yFix) {
		    continue;
		}
		for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
		    try {
			this.getCell(arena, u, v, zFix, w).rangeAction(xFix, yFix, zFix, u - xFix, v - yFix, rangeType,
				forceUnits);
		    } catch (final ArrayIndexOutOfBoundsException aioob) {
			// Do nothing
		    }
		}
	    }
	}
    }

    @Override
    public boolean circularScanTank(final AbstractArena arena, final int x, final int y, final int z, final int r) {
	final int[] tankLoc = LaserTank.getApplication().getGameManager().getTankLocation();
	int fX = x;
	int fY = y;
	int fZ = z;
	if (arena.isVerticalWraparoundEnabled()) {
	    fX = this.normalizeColumn(fX);
	}
	if (arena.isHorizontalWraparoundEnabled()) {
	    fY = this.normalizeRow(fY);
	}
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    fZ = this.normalizeFloor(fZ);
	}
	final int tx = tankLoc[0];
	final int ty = tankLoc[1];
	final int tz = tankLoc[2];
	return fZ == tz && Math.abs(fX - tx) <= r && Math.abs(fY - ty) <= r;
    }

    @Override
    public int[] circularScanTunnel(final AbstractArena arena, final int xIn, final int yIn, final int zIn, final int r,
	    final int tx, final int ty, final AbstractTunnel target, final boolean moved) {
	int xFix = xIn;
	int yFix = yIn;
	int zFix = zIn;
	if (arena.isVerticalWraparoundEnabled()) {
	    xFix = this.normalizeColumn(xFix);
	}
	if (arena.isHorizontalWraparoundEnabled()) {
	    yFix = this.normalizeRow(yFix);
	}
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    zFix = this.normalizeFloor(zFix);
	}
	int u, v, w;
	w = ArenaConstants.LAYER_LOWER_OBJECTS;
	// Perform the scan
	for (u = xFix - r; u <= xFix + r; u++) {
	    for (v = yFix - r; v <= yFix + r; v++) {
		if (v == tx && u == ty && moved) {
		    continue;
		}
		if (v >= 0 && v < AbstractArenaData.MIN_ROWS && u >= 0 && u < AbstractArenaData.MIN_COLUMNS) {
		    final AbstractArenaObject obj = this.getCell(arena, v, u, zFix, w);
		    final AbstractArenaObject savedObj = obj.getSavedObject();
		    AbstractArenaObject test;
		    if (obj.isOfType(TypeConstants.TYPE_CHARACTER)) {
			test = savedObj;
		    } else {
			test = obj;
		    }
		    if (target.equals(test)) {
			return new int[] { v, u, zFix };
		    }
		}
	    }
	}
	return null;
    }

    @Override
    public void clearDirtyFlags(final int floor) {
	for (int row = 0; row < this.getRows(); row++) {
	    for (int col = 0; col < this.getColumns(); col++) {
		this.dirtyData.setCell(false, col, row, floor);
	    }
	}
    }

    @Override
    public void clearRedoHistory() {
	this.iue.clearRedoHistory();
    }

    @Override
    public void clearUndoHistory() {
	this.iue.clearUndoHistory();
    }

    @Override
    public void clearVirtualGrid(final AbstractArena arena) {
	for (int row = 0; row < this.getRows(); row++) {
	    for (int col = 0; col < this.getColumns(); col++) {
		for (int floor = 0; floor < this.getFloors(); floor++) {
		    for (int layer = 0; layer < ArenaConstants.NUM_VIRTUAL_LAYERS; layer++) {
			this.setVirtualCell(arena, new Empty(), row, col, floor, layer);
		    }
		}
	    }
	}
    }

    // Methods
    @Override
    public CurrentArenaData clone() {
	try {
	    final CurrentArenaData copy = new CurrentArenaData();
	    copy.data = (LowLevelArenaDataStore) this.data.clone();
	    copy.savedState = (LowLevelArenaDataStore) this.savedState.clone();
	    return copy;
	} catch (final CloneNotSupportedException cnse) {
	    LaserTank.logError(cnse);
	    return null;
	}
    }

    @Override
    public void fill(final AbstractArena arena, final AbstractArenaObject fill) {
	int y, x, z, w;
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			if (w == ArenaConstants.LAYER_LOWER_GROUND) {
			    this.setCell(arena, fill, y, x, z, w);
			} else {
			    this.setCell(arena, new Empty(), y, x, z, w);
			}
		    }
		}
	    }
	}
    }

    @Override
    public void fillNulls(final AbstractArena arena, final AbstractArenaObject fill1, final AbstractArenaObject fill2,
	    final boolean was16) {
	int y, x, z, w;
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			if (this.getCell(arena, y, x, z, w) == null) {
			    if (w == ArenaConstants.LAYER_LOWER_GROUND) {
				this.setCell(arena, fill1, y, x, z, w);
			    } else if (w == ArenaConstants.LAYER_LOWER_OBJECTS && was16) {
				if (x >= 16 || y >= 16) {
				    this.setCell(arena, fill2, y, x, z, w);
				} else {
				    this.setCell(arena, new Empty(), y, x, z, w);
				}
			    } else {
				this.setCell(arena, new Empty(), y, x, z, w);
			    }
			}
		    }
		}
	    }
	}
    }

    @Override
    public void fillSTSNulls(final AbstractArenaObject fill) {
	int y, x, z, w;
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			if (this.savedState.getCell(y, x, z, w) == null) {
			    if (w == ArenaConstants.LAYER_LOWER_GROUND) {
				this.savedState.setCell(fill, y, x, z, w);
			    } else {
				this.savedState.setCell(new Empty(), y, x, z, w);
			    }
			}
		    }
		}
	    }
	}
    }

    @Override
    public void fillVirtual() {
	int y, x, z, w;
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_VIRTUAL_LAYERS; w++) {
			this.virtualData.setCell(new Empty(), y, x, z, w);
		    }
		}
	    }
	}
    }

    @Override
    public int[] findObject(final AbstractArena arena, final int z, final AbstractArenaObject target) {
	// Perform the scan
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		for (int w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
		    try {
			final AbstractArenaObject obj = this.getCell(arena, x, y, z, w);
			if (target.equals(obj)) {
			    return new int[] { x, y };
			}
		    } catch (final ArrayIndexOutOfBoundsException aioob) {
			// Do nothing
		    }
		}
	    }
	}
	return null;
    }

    @Override
    public int[] findPlayer(final AbstractArena arena, final int number) {
	final Tank t = new Tank(number);
	int y, x, z;
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    final AbstractArenaObject mo = this.getCell(arena, y, x, z, t.getLayer());
		    if (mo != null) {
			if (t.equals(mo)) {
			    return new int[] { y, x, z };
			}
		    }
		}
	    }
	}
	return null;
    }

    @Override
    public void fullScanAllButtonClose(final AbstractArena arena, final int zIn, final AbstractButton source) {
	// Perform the scan
	int zFix = zIn;
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    zFix = this.normalizeFloor(zFix);
	}
	boolean flag = !source.isTriggered();
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    if (flag) {
		break;
	    }
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		if (flag) {
		    break;
		}
		final AbstractArenaObject obj = this.getCell(arena, y, x, zFix, source.getLayer());
		if (obj instanceof AbstractButton) {
		    final AbstractButton button = (AbstractButton) obj;
		    if (source.boundButtonDoorEquals(button)) {
			if (!button.isTriggered()) {
			    flag = true;
			}
		    }
		}
	    }
	}
	if (flag) {
	    // Scan said OK to proceed
	    final int dx = source.getDoorX();
	    final int dy = source.getDoorY();
	    if (!this.getCell(arena, dx, dy, zFix, source.getLayer()).getClass()
		    .equals(source.getButtonDoor().getClass())) {
		this.setCell(arena, source.getButtonDoor(), dx, dy, zFix, source.getLayer());
		SoundManager.playSound(SoundConstants.SOUND_DOOR_CLOSES);
	    }
	}
    }

    @Override
    public void fullScanAllButtonOpen(final AbstractArena arena, final int zIn, final AbstractButton source) {
	// Perform the scan
	int zFix = zIn;
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    zFix = this.normalizeFloor(zFix);
	}
	boolean flag = true;
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    if (!flag) {
		break;
	    }
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		if (!flag) {
		    break;
		}
		final AbstractArenaObject obj = this.getCell(arena, y, x, zFix, source.getLayer());
		if (obj instanceof AbstractButton) {
		    final AbstractButton button = (AbstractButton) obj;
		    if (source.boundButtonDoorEquals(button)) {
			if (!button.isTriggered()) {
			    flag = false;
			}
		    }
		}
	    }
	}
	if (flag) {
	    // Scan said OK to proceed
	    final int dx = source.getDoorX();
	    final int dy = source.getDoorY();
	    if (!(this.getCell(arena, dx, dy, zFix, source.getLayer()) instanceof Ground)) {
		this.setCell(arena, new Ground(), dx, dy, zFix, source.getLayer());
		SoundManager.playSound(SoundConstants.SOUND_DOOR_OPENS);
	    }
	}
    }

    @Override
    public void fullScanButtonBind(final AbstractArena arena, final int dx, final int dy, final int zIn,
	    final AbstractButtonDoor source) {
	// Perform the scan
	int z = zIn;
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    z = this.normalizeFloor(z);
	}
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		final AbstractArenaObject obj = this.getCell(arena, x, y, z, source.getLayer());
		if (obj instanceof AbstractButton) {
		    final AbstractButton button = (AbstractButton) obj;
		    if (source.getClass().equals(button.getButtonDoor().getClass())) {
			button.setDoorX(dx);
			button.setDoorY(dy);
			button.setTriggered(false);
		    }
		}
	    }
	}
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		final AbstractArenaObject obj = this.getCell(arena, x, y, z, source.getLayer());
		if (obj instanceof AbstractButtonDoor) {
		    final AbstractButtonDoor door = (AbstractButtonDoor) obj;
		    if (source.getClass().equals(door.getClass())) {
			this.setCell(arena, new Ground(), x, y, z, source.getLayer());
		    }
		}
	    }
	}
    }

    @Override
    public void fullScanButtonCleanup(final AbstractArena arena, final int px, final int py, final int zIn,
	    final AbstractButton button) {
	// Perform the scan
	int zFix = zIn;
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    zFix = this.normalizeFloor(zFix);
	}
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		if (x == px && y == py) {
		    continue;
		}
		final AbstractArenaObject obj = this.getCell(arena, x, y, zFix, button.getLayer());
		if (obj instanceof AbstractButton) {
		    if (((AbstractButton) obj).boundButtonDoorEquals(button)) {
			this.setCell(arena, new Ground(), x, y, zFix, button.getLayer());
		    }
		}
	    }
	}
    }

    @Override
    public void fullScanFindButtonLostDoor(final AbstractArena arena, final int zIn, final AbstractButtonDoor door) {
	// Perform the scan
	int zFix = zIn;
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    zFix = this.normalizeFloor(zFix);
	}
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		final AbstractArenaObject obj = this.getCell(arena, x, y, zFix, door.getLayer());
		if (obj instanceof AbstractButton) {
		    final AbstractButton button = (AbstractButton) obj;
		    if (button.boundToSameButtonDoor(door)) {
			button.setTriggered(true);
			return;
		    }
		}
	    }
	}
    }

    @Override
    public void fullScanFreezeGround(final AbstractArena arena) {
	// Perform the scan
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		for (int z = 0; z < this.getFloors(); z++) {
		    final AbstractArenaObject obj = this.getCell(arena, y, x, z, ArenaConstants.LAYER_LOWER_GROUND);
		    if (!(obj instanceof Ground)) {
			// Freeze the ground
			LaserTank.getApplication().getGameManager().morph(
				obj.changesToOnExposure(MaterialConstants.MATERIAL_ICE), y, x, z,
				ArenaConstants.LAYER_LOWER_GROUND);
		    }
		}
	    }
	}
    }

    @Override
    public void fullScanKillTanks(final AbstractArena arena) {
	// Perform the scan
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		for (int z = 0; z < this.getFloors(); z++) {
		    final AbstractArenaObject obj = this.getCell(arena, y, x, z, ArenaConstants.LAYER_LOWER_OBJECTS);
		    if (obj instanceof AntiTank) {
			// Kill the tank
			final GameManager gm = LaserTank.getApplication().getGameManager();
			final DeadAntiTank dat = new DeadAntiTank();
			dat.setSavedObject(obj.getSavedObject());
			dat.setDirection(obj.getDirection());
			gm.morph(dat, y, x, z, ArenaConstants.LAYER_LOWER_OBJECTS);
		    }
		}
	    }
	}
    }

    @Override
    public AbstractArenaObject getCell(final AbstractArena arena, final int row, final int col, final int floor,
	    final int layer) {
	int fR = row;
	int fC = col;
	int fF = floor;
	if (arena.isVerticalWraparoundEnabled()) {
	    fC = this.normalizeColumn(fC);
	}
	if (arena.isHorizontalWraparoundEnabled()) {
	    fR = this.normalizeRow(fR);
	}
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    fF = this.normalizeFloor(fF);
	}
	return this.data.getArenaDataCell(fC, fR, fF, layer);
    }

    @Override
    public int getColumns() {
	return this.data.getShape()[0];
    }

    @Override
    public int getFloors() {
	return this.data.getShape()[2];
    }

    @Override
    public int getRows() {
	return this.data.getShape()[1];
    }

    @Override
    public AbstractArenaObject getVirtualCell(final AbstractArena arena, final int row, final int col, final int floor,
	    final int layer) {
	int fR = row;
	int fC = col;
	int fF = floor;
	if (arena.isVerticalWraparoundEnabled()) {
	    fC = this.normalizeColumn(fC);
	}
	if (arena.isHorizontalWraparoundEnabled()) {
	    fR = this.normalizeRow(fR);
	}
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    fF = this.normalizeFloor(fF);
	}
	return this.virtualData.getArenaDataCell(fC, fR, fF, layer);
    }

    @Override
    public HistoryStatus getWhatWas() {
	return this.iue.getWhatWas();
    }

    @Override
    public boolean isCellDirty(final AbstractArena arena, final int row, final int col, final int floor) {
	int fR = row;
	int fC = col;
	int fF = floor;
	if (arena.isVerticalWraparoundEnabled()) {
	    fC = this.normalizeColumn(fC);
	}
	if (arena.isHorizontalWraparoundEnabled()) {
	    fR = this.normalizeRow(fR);
	}
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    fF = this.normalizeFloor(fF);
	}
	return this.dirtyData.getCell(fC, fR, fF);
    }

    @Override
    public boolean linearScan(final AbstractArena arena, final int xIn, final int yIn, final int zIn,
	    final Direction d) {
	// Perform the scan
	int xFix = xIn;
	int yFix = yIn;
	int zFix = zIn;
	if (arena.isVerticalWraparoundEnabled()) {
	    xFix = this.normalizeColumn(xFix);
	}
	if (arena.isHorizontalWraparoundEnabled()) {
	    yFix = this.normalizeRow(yFix);
	}
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    zFix = this.normalizeFloor(zFix);
	}
	int u, w;
	if (d == Direction.NORTH) {
	    final AbstractArenaObject tank = LaserTank.getApplication().getGameManager().getTank();
	    if (tank.getSavedObject().isSolid()) {
		return false;
	    } else {
		for (u = yFix - 1; u >= 0; u--) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			try {
			    final AbstractArenaObject obj = this.getCell(arena, xFix, u, zFix, w);
			    if (obj.isOfType(TypeConstants.TYPE_ANTI)) {
				final int[] unres = DirectionResolver.unresolveRelative(obj.getDirection());
				final Direction invert = DirectionResolver.resolveRelativeInvert(unres[0],
					unres[1]);
				if (d == invert) {
				    this.foundX = xFix;
				    this.foundY = u;
				    return true;
				}
			    }
			    if (obj.isSolid()) {
				return false;
			    }
			} catch (final ArrayIndexOutOfBoundsException aioobe) {
			    return false;
			}
		    }
		}
	    }
	    return false;
	} else if (d == Direction.SOUTH) {
	    final AbstractArenaObject tank = LaserTank.getApplication().getGameManager().getTank();
	    if (tank.getSavedObject().isSolid()) {
		return false;
	    } else {
		for (u = yFix + 1; u < 24; u++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			try {
			    final AbstractArenaObject obj = this.getCell(arena, xFix, u, zFix, w);
			    if (obj.isOfType(TypeConstants.TYPE_ANTI)) {
				final int[] unres = DirectionResolver.unresolveRelative(obj.getDirection());
				final Direction invert = DirectionResolver.resolveRelativeInvert(unres[0],
					unres[1]);
				if (d == invert) {
				    this.foundX = xFix;
				    this.foundY = u;
				    return true;
				}
			    }
			    if (obj.isSolid()) {
				return false;
			    }
			} catch (final ArrayIndexOutOfBoundsException aioobe) {
			    return false;
			}
		    }
		}
	    }
	    return false;
	} else if (d == Direction.WEST) {
	    final AbstractArenaObject tank = LaserTank.getApplication().getGameManager().getTank();
	    if (tank.getSavedObject().isSolid()) {
		return false;
	    } else {
		for (u = xFix - 1; u >= 0; u--) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			try {
			    final AbstractArenaObject obj = this.getCell(arena, u, yFix, zFix, w);
			    if (obj.isOfType(TypeConstants.TYPE_ANTI)) {
				final int[] unres = DirectionResolver.unresolveRelative(obj.getDirection());
				final Direction invert = DirectionResolver.resolveRelativeInvert(unres[0],
					unres[1]);
				if (d == invert) {
				    this.foundX = u;
				    this.foundY = yFix;
				    return true;
				}
			    }
			    if (obj.isSolid()) {
				return false;
			    }
			} catch (final ArrayIndexOutOfBoundsException aioobe) {
			    return false;
			}
		    }
		}
	    }
	    return false;
	} else if (d == Direction.EAST) {
	    final AbstractArenaObject tank = LaserTank.getApplication().getGameManager().getTank();
	    if (tank.getSavedObject().isSolid()) {
		return false;
	    } else {
		for (u = xFix + 1; u < 24; u++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			try {
			    final AbstractArenaObject obj = this.getCell(arena, u, yFix, zFix, w);
			    if (obj.isOfType(TypeConstants.TYPE_ANTI)) {
				final int[] unres = DirectionResolver.unresolveRelative(obj.getDirection());
				final Direction invert = DirectionResolver.resolveRelativeInvert(unres[0],
					unres[1]);
				if (d == invert) {
				    this.foundX = u;
				    this.foundY = yFix;
				    return true;
				}
			    }
			    if (obj.isSolid()) {
				return false;
			    }
			} catch (final ArrayIndexOutOfBoundsException aioobe) {
			    return false;
			}
		    }
		}
	    }
	    return false;
	}
	return false;
    }

    @Override
    public int linearScanMagnetic(final AbstractArena arena, final int xIn, final int yIn, final int zIn,
	    final Direction d) {
	// Perform the scan
	int xFix = xIn;
	int yFix = yIn;
	int zFix = zIn;
	if (arena.isVerticalWraparoundEnabled()) {
	    xFix = this.normalizeColumn(xFix);
	}
	if (arena.isHorizontalWraparoundEnabled()) {
	    yFix = this.normalizeRow(yFix);
	}
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    zFix = this.normalizeFloor(zFix);
	}
	int u, w;
	if (d == Direction.NORTH) {
	    for (u = yFix - 1; u >= 0; u--) {
		for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
		    try {
			final AbstractArenaObject obj = this.getCell(arena, xFix, u, zFix, w);
			if (obj.getMaterial() == MaterialConstants.MATERIAL_MAGNETIC) {
			    return yFix - u - 1;
			}
			if (obj.isSolid()) {
			    return 0;
			}
		    } catch (final ArrayIndexOutOfBoundsException aioobe) {
			return 0;
		    }
		}
	    }
	    return 0;
	} else if (d == Direction.SOUTH) {
	    for (u = yFix + 1; u < 24; u++) {
		for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
		    try {
			final AbstractArenaObject obj = this.getCell(arena, xFix, u, zFix, w);
			if (obj.getMaterial() == MaterialConstants.MATERIAL_MAGNETIC) {
			    return u - yFix - 1;
			}
			if (obj.isSolid()) {
			    return 0;
			}
		    } catch (final ArrayIndexOutOfBoundsException aioobe) {
			return 0;
		    }
		}
	    }
	    return 0;
	} else if (d == Direction.WEST) {
	    for (u = xFix - 1; u >= 0; u--) {
		for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
		    try {
			final AbstractArenaObject obj = this.getCell(arena, u, yFix, zFix, w);
			if (obj.getMaterial() == MaterialConstants.MATERIAL_MAGNETIC) {
			    return xFix - u - 1;
			}
			if (obj.isSolid()) {
			    return 0;
			}
		    } catch (final ArrayIndexOutOfBoundsException aioobe) {
			return 0;
		    }
		}
	    }
	    return 0;
	} else if (d == Direction.EAST) {
	    for (u = xFix + 1; u < 24; u++) {
		for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
		    try {
			final AbstractArenaObject obj = this.getCell(arena, u, yFix, zFix, w);
			if (obj.getMaterial() == MaterialConstants.MATERIAL_MAGNETIC) {
			    return u - xFix - 1;
			}
			if (obj.isSolid()) {
			    return 0;
			}
		    } catch (final ArrayIndexOutOfBoundsException aioobe) {
			return 0;
		    }
		}
	    }
	    return 0;
	}
	return 0;
    }

    @Override
    public void markAsDirty(final AbstractArena arena, final int row, final int col, final int floor) {
	int fR = row;
	int fC = col;
	int fF = floor;
	if (arena.isVerticalWraparoundEnabled()) {
	    fC = this.normalizeColumn(fC);
	}
	if (arena.isHorizontalWraparoundEnabled()) {
	    fR = this.normalizeRow(fR);
	}
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    fF = this.normalizeFloor(fF);
	}
	this.dirtyData.setCell(true, fC, fR, fF);
    }

    @Override
    public AbstractArenaData readData(final AbstractArena arena, final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	if (FormatConstants.isFormatVersionValidGeneration1(formatVersion)) {
	    final CurrentArenaData tempData = CurrentArenaData.readDataG1(arena, reader, formatVersion);
	    return tempData;
	} else if (FormatConstants.isFormatVersionValidGeneration2(formatVersion)) {
	    final CurrentArenaData tempData = CurrentArenaData.readDataG2(arena, reader, formatVersion);
	    return tempData;
	} else if (FormatConstants.isFormatVersionValidGeneration3(formatVersion)) {
	    final CurrentArenaData tempData = CurrentArenaData.readDataG3(arena, reader, formatVersion);
	    return tempData;
	} else if (FormatConstants.isFormatVersionValidGeneration4(formatVersion)) {
	    final CurrentArenaData tempData = CurrentArenaData.readDataG4(arena, reader, formatVersion);
	    return tempData;
	} else if (FormatConstants.isFormatVersionValidGeneration5(formatVersion)) {
	    final CurrentArenaData tempData = CurrentArenaData.readDataG5(arena, reader, formatVersion);
	    return tempData;
	} else if (FormatConstants.isFormatVersionValidGeneration6(formatVersion)) {
	    final CurrentArenaData tempData = CurrentArenaData.readDataG6(arena, reader, formatVersion);
	    return tempData;
	} else {
	    throw new IOException(StringLoader.loadError(ErrorString.UNKNOWN_ARENA_FORMAT));
	}
    }

    @Override
    public void readSavedState(final XMLFileReader reader, final int formatVersion) throws IOException {
	if (FormatConstants.isFormatVersionValidGeneration1(formatVersion)) {
	    this.readSavedStateG2(reader, formatVersion);
	} else if (FormatConstants.isFormatVersionValidGeneration2(formatVersion)) {
	    this.readSavedStateG2(reader, formatVersion);
	} else if (FormatConstants.isFormatVersionValidGeneration3(formatVersion)) {
	    this.readSavedStateG3(reader, formatVersion);
	} else if (FormatConstants.isFormatVersionValidGeneration4(formatVersion)) {
	    this.readSavedStateG4(reader, formatVersion);
	} else if (FormatConstants.isFormatVersionValidGeneration5(formatVersion)) {
	    this.readSavedStateG5(reader, formatVersion);
	} else if (FormatConstants.isFormatVersionValidGeneration6(formatVersion)) {
	    this.readSavedStateG6(reader, formatVersion);
	} else {
	    throw new IOException(StringLoader.loadError(ErrorString.UNKNOWN_ARENA_FORMAT));
	}
    }

    private void readSavedStateG2(final XMLFileReader reader, final int formatVersion) throws IOException {
	int y, x, z, saveSizeX, saveSizeY, saveSizeZ;
	saveSizeX = reader.readInt();
	saveSizeY = reader.readInt();
	saveSizeZ = reader.readInt();
	this.savedState = new LowLevelArenaDataStore(saveSizeY, saveSizeX, saveSizeZ, ArenaConstants.NUM_LAYERS);
	for (x = 0; x < saveSizeX; x++) {
	    for (y = 0; y < saveSizeY; y++) {
		for (z = 0; z < saveSizeZ; z++) {
		    this.savedState.setCell(
			    LaserTank.getApplication().getObjects().readArenaObjectG2(reader, formatVersion), y, x, z,
			    ArenaConstants.LAYER_LOWER_GROUND);
		}
	    }
	}
	if (saveSizeX != AbstractArenaData.MIN_COLUMNS || saveSizeY != AbstractArenaData.MIN_ROWS) {
	    this.resizeSavedState(saveSizeZ, new Ground());
	}
    }

    private void readSavedStateG3(final XMLFileReader reader, final int formatVersion) throws IOException {
	int y, x, z, saveSizeX, saveSizeY, saveSizeZ;
	saveSizeX = reader.readInt();
	saveSizeY = reader.readInt();
	saveSizeZ = reader.readInt();
	this.savedState = new LowLevelArenaDataStore(saveSizeY, saveSizeX, saveSizeZ, ArenaConstants.NUM_LAYERS);
	for (x = 0; x < saveSizeX; x++) {
	    for (y = 0; y < saveSizeY; y++) {
		for (z = 0; z < saveSizeZ; z++) {
		    this.savedState.setCell(
			    LaserTank.getApplication().getObjects().readArenaObjectG3(reader, formatVersion), y, x, z,
			    ArenaConstants.LAYER_LOWER_GROUND);
		}
	    }
	}
	if (saveSizeX != AbstractArenaData.MIN_COLUMNS || saveSizeY != AbstractArenaData.MIN_ROWS) {
	    this.resizeSavedState(saveSizeZ, new Ground());
	}
    }

    private void readSavedStateG4(final XMLFileReader reader, final int formatVersion) throws IOException {
	int y, x, z, saveSizeX, saveSizeY, saveSizeZ;
	saveSizeX = reader.readInt();
	saveSizeY = reader.readInt();
	saveSizeZ = reader.readInt();
	this.savedState = new LowLevelArenaDataStore(saveSizeY, saveSizeX, saveSizeZ, ArenaConstants.NUM_LAYERS);
	for (x = 0; x < saveSizeX; x++) {
	    for (y = 0; y < saveSizeY; y++) {
		for (z = 0; z < saveSizeZ; z++) {
		    this.savedState.setCell(
			    LaserTank.getApplication().getObjects().readArenaObjectG4(reader, formatVersion), y, x, z,
			    ArenaConstants.LAYER_LOWER_GROUND);
		}
	    }
	}
	if (saveSizeX != AbstractArenaData.MIN_COLUMNS || saveSizeY != AbstractArenaData.MIN_ROWS) {
	    this.resizeSavedState(saveSizeZ, new Ground());
	}
    }

    private void readSavedStateG5(final XMLFileReader reader, final int formatVersion) throws IOException {
	int y, x, z, w, saveSizeX, saveSizeY, saveSizeZ;
	saveSizeX = reader.readInt();
	saveSizeY = reader.readInt();
	saveSizeZ = reader.readInt();
	this.savedState = new LowLevelArenaDataStore(saveSizeY, saveSizeX, saveSizeZ, ArenaConstants.NUM_LAYERS);
	for (x = 0; x < saveSizeX; x++) {
	    for (y = 0; y < saveSizeY; y++) {
		for (z = 0; z < saveSizeZ; z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			this.savedState.setCell(
				LaserTank.getApplication().getObjects().readArenaObjectG5(reader, formatVersion), y, x,
				z, w);
		    }
		}
	    }
	}
	if (saveSizeX != AbstractArenaData.MIN_COLUMNS || saveSizeY != AbstractArenaData.MIN_ROWS) {
	    this.resizeSavedState(saveSizeZ, new Ground());
	}
    }

    private void readSavedStateG6(final XMLFileReader reader, final int formatVersion) throws IOException {
	int y, x, z, w, saveSizeX, saveSizeY, saveSizeZ;
	saveSizeX = reader.readInt();
	saveSizeY = reader.readInt();
	saveSizeZ = reader.readInt();
	this.savedState = new LowLevelArenaDataStore(saveSizeY, saveSizeX, saveSizeZ, ArenaConstants.NUM_LAYERS);
	for (x = 0; x < saveSizeX; x++) {
	    for (y = 0; y < saveSizeY; y++) {
		for (z = 0; z < saveSizeZ; z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			this.savedState.setCell(
				LaserTank.getApplication().getObjects().readArenaObjectG6(reader, formatVersion), y, x,
				z, w);
		    }
		}
	    }
	}
	if (saveSizeX != AbstractArenaData.MIN_COLUMNS || saveSizeY != AbstractArenaData.MIN_ROWS) {
	    this.resizeSavedState(saveSizeZ, new Ground());
	}
    }

    @Override
    public void redo(final AbstractArena arena) {
	this.iue.redo();
	this.data = this.iue.getImage();
	this.setAllDirtyFlags();
	this.clearVirtualGrid(arena);
    }

    @Override
    public void resetHistoryEngine() {
	this.iue = new ImageUndoEngine();
    }

    @Override
    public void resize(final AbstractArena arena, final int zIn, final AbstractArenaObject nullFill) {
	final int x = AbstractArenaData.MIN_ROWS;
	final int y = AbstractArenaData.MIN_COLUMNS;
	int z = zIn;
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    z = this.normalizeFloor(z);
	}
	// Allocate temporary storage array
	final LowLevelArenaDataStore tempStorage = new LowLevelArenaDataStore(y, x, z, ArenaConstants.NUM_LAYERS);
	// Copy existing maze into temporary array
	int u, v, w, t;
	for (u = 0; u < y; u++) {
	    for (v = 0; v < x; v++) {
		for (w = 0; w < z; w++) {
		    for (t = 0; t < ArenaConstants.NUM_LAYERS; t++) {
			try {
			    tempStorage.setCell(this.getCell(arena, v, u, w, t), u, v, w, t);
			} catch (final ArrayIndexOutOfBoundsException aioob) {
			    // Do nothing
			}
		    }
		}
	    }
	}
	// Set the current data to the temporary array
	this.data = tempStorage;
	this.virtualData = new LowLevelArenaDataStore(x, y, z, ArenaConstants.NUM_VIRTUAL_LAYERS);
	this.dirtyData = new FlagStorage(x, y, z);
	// Fill any blanks
	this.fillNulls(arena, nullFill, null, false);
	// Fix saved tower state
	this.resizeSavedState(z, nullFill);
    }

    @Override
    public void resizeSavedState(final int z, final AbstractArenaObject nullFill) {
	final int x = AbstractArenaData.MIN_ROWS;
	final int y = AbstractArenaData.MIN_COLUMNS;
	// Allocate temporary storage array
	final LowLevelArenaDataStore tempStorage = new LowLevelArenaDataStore(y, x, z, ArenaConstants.NUM_LAYERS);
	// Copy existing maze into temporary array
	int u, v, w, t;
	for (u = 0; u < y; u++) {
	    for (v = 0; v < x; v++) {
		for (w = 0; w < z; w++) {
		    for (t = 0; t < ArenaConstants.NUM_LAYERS; t++) {
			try {
			    tempStorage.setCell(this.savedState.getCell(v, u, w, t), u, v, w, t);
			} catch (final ArrayIndexOutOfBoundsException aioob) {
			    // Do nothing
			}
		    }
		}
	    }
	}
	// Set the current data to the temporary array
	this.savedState = tempStorage;
	// Fill any blanks
	this.fillSTSNulls(nullFill);
    }

    @Override
    public void restore(final AbstractArena arena) {
	int y, x, z, w;
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			this.setCell(arena, ((AbstractArenaObject) this.savedState.getCell(x, y, z, w)).clone(), y, x,
				z, w);
		    }
		}
	    }
	}
    }

    @Override
    public void save(final AbstractArena arena) {
	int y, x, z, w;
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			this.savedState.setCell(this.getCell(arena, y, x, z, w).clone(), x, y, z, w);
		    }
		}
	    }
	}
    }

    @Override
    public void setAllDirtyFlags() {
	for (int floor = 0; floor < this.getFloors(); floor++) {
	    this.setDirtyFlags(floor);
	}
    }

    @Override
    public void setCell(final AbstractArena arena, final AbstractArenaObject mo, final int row, final int col,
	    final int floor, final int layer) {
	int fR = row;
	int fC = col;
	int fF = floor;
	if (arena.isVerticalWraparoundEnabled()) {
	    fC = this.normalizeColumn(fC);
	}
	if (arena.isHorizontalWraparoundEnabled()) {
	    fR = this.normalizeRow(fR);
	}
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    fF = this.normalizeFloor(fF);
	}
	this.data.setArenaDataCell(mo, fC, fR, fF, layer);
	this.dirtyData.setCell(true, fC, fR, fF);
    }

    @Override
    public void setDirtyFlags(final int floor) {
	for (int row = 0; row < this.getRows(); row++) {
	    for (int col = 0; col < this.getColumns(); col++) {
		this.dirtyData.setCell(true, col, row, floor);
	    }
	}
    }

    @Override
    public void setVirtualCell(final AbstractArena arena, final AbstractArenaObject mo, final int row, final int col,
	    final int floor, final int layer) {
	int fR = row;
	int fC = col;
	int fF = floor;
	if (arena.isVerticalWraparoundEnabled()) {
	    fC = this.normalizeColumn(fC);
	}
	if (arena.isHorizontalWraparoundEnabled()) {
	    fR = this.normalizeRow(fR);
	}
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    fF = this.normalizeFloor(fF);
	}
	this.virtualData.setArenaDataCell(mo, fC, fR, fF, layer);
	this.dirtyData.setCell(true, fC, fR, fF);
    }

    @Override
    public void tickTimers(final AbstractArena arena, final int floor, final int actionType) {
	int floorFix = floor;
	if (arena.isThirdDimensionWraparoundEnabled()) {
	    floorFix = this.normalizeFloor(floorFix);
	}
	int x, y, z, w;
	// Tick all ArenaObject timers
	AbstractTunnel.checkTunnels();
	for (z = Direction.NORTH.ordinal(); z <= Direction.NORTHWEST.ordinal(); z += 2) {
	    for (x = 0; x < this.getColumns(); x++) {
		for (y = 0; y < this.getRows(); y++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			final AbstractArenaObject mo = this.getCell(arena, y, x, floorFix, w);
			if (mo != null) {
			    if (z == Direction.NORTH.ordinal()) {
				// Handle objects waiting for a tunnel to open
				if (mo instanceof AbstractMovableObject) {
				    final AbstractMovableObject gmo = (AbstractMovableObject) mo;
				    final AbstractArenaObject saved = gmo.getSavedObject();
				    if (saved instanceof AbstractTunnel) {
					final int color = saved.getColor();
					if (gmo.waitingOnTunnel() && !AbstractTunnel.tunnelsFull(color)) {
					    gmo.setWaitingOnTunnel(false);
					    saved.pushIntoAction(gmo, y, x, floorFix);
					}
					if (AbstractTunnel.tunnelsFull(color)) {
					    gmo.setWaitingOnTunnel(true);
					}
				    }
				}
				mo.tickTimer(y, x, actionType);
			    }
			}
		    }
		}
	    }
	}
    }

    @Override
    public boolean tryRedo() {
	return this.iue.tryRedo();
    }

    @Override
    public boolean tryUndo() {
	return this.iue.tryUndo();
    }

    @Override
    public void undo(final AbstractArena arena) {
	this.iue.undo();
	this.data = this.iue.getImage();
	this.setAllDirtyFlags();
	this.clearVirtualGrid(arena);
    }

    @Override
    public void updateRedoHistory(final HistoryStatus whatWas) {
	try {
	    this.iue.updateRedoHistory((LowLevelArenaDataStore) this.data.clone(), whatWas);
	} catch (final CloneNotSupportedException cnse) {
	    LaserTank.logError(cnse);
	}
    }

    @Override
    public void updateUndoHistory(final HistoryStatus whatWas) {
	try {
	    this.iue.updateUndoHistory((LowLevelArenaDataStore) this.data.clone(), whatWas);
	} catch (final CloneNotSupportedException cnse) {
	    LaserTank.logError(cnse);
	}
    }

    @Override
    public void writeData(final AbstractArena arena, final XMLFileWriter writer) throws IOException {
	int y, x, z, w;
	writer.writeInt(this.getColumns());
	writer.writeInt(this.getRows());
	writer.writeInt(this.getFloors());
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			this.getCell(arena, y, x, z, w).writeArenaObject(writer);
		    }
		}
	    }
	}
    }

    @Override
    public void writeSavedState(final XMLFileWriter writer) throws IOException {
	int y, x, z, w;
	writer.writeInt(this.getColumns());
	writer.writeInt(this.getRows());
	writer.writeInt(this.getFloors());
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			((AbstractArenaObject) this.savedState.getCell(y, x, z, w)).writeArenaObject(writer);
		    }
		}
	    }
	}
    }
}
