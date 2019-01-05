/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.current;

import java.io.IOException;
import java.util.ArrayDeque;

import com.puttysoftware.lasertank.improved.fileio.XMLFileReader;
import com.puttysoftware.lasertank.improved.fileio.XMLFileWriter;
import com.puttysoftware.storage.FlagStorage;
import com.puttysoftware.storage.NumberStorage;
import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.AbstractArenaData;
import com.puttysoftware.ltremix.arena.HistoryStatus;
import com.puttysoftware.ltremix.arena.LowLevelArenaDataStore;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractButton;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractButtonDoor;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractCharacter;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractTunnel;
import com.puttysoftware.ltremix.arena.objects.AntiTank;
import com.puttysoftware.ltremix.arena.objects.AntiTankDisguise;
import com.puttysoftware.ltremix.arena.objects.DeadAntiTank;
import com.puttysoftware.ltremix.arena.objects.Empty;
import com.puttysoftware.ltremix.arena.objects.Ground;
import com.puttysoftware.ltremix.arena.objects.Tank;
import com.puttysoftware.ltremix.arena.objects.Wall;
import com.puttysoftware.ltremix.game.GameManager;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;
import com.puttysoftware.ltremix.utilities.ArenaConstants;
import com.puttysoftware.ltremix.utilities.DirectionConstants;
import com.puttysoftware.ltremix.utilities.DirectionResolver;
import com.puttysoftware.ltremix.utilities.EraConstants;
import com.puttysoftware.ltremix.utilities.FormatConstants;
import com.puttysoftware.ltremix.utilities.MaterialConstants;
import com.puttysoftware.ltremix.utilities.ProgressTracker;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public final class CurrentArenaData extends AbstractArenaData {
    // Properties
    private LowLevelArenaDataStore pastEraData;
    private LowLevelArenaDataStore presentEraData;
    private LowLevelArenaDataStore futureEraData;
    private LowLevelArenaDataStore virtualData;
    private FlagStorage dirtyData;
    private LowLevelArenaDataStore pastSavedTowerState;
    private LowLevelArenaDataStore presentSavedTowerState;
    private LowLevelArenaDataStore futureSavedTowerState;
    private NumberStorage startData;
    private NumberStorage savedStartData;
    private boolean horizontalWraparoundEnabled;
    private boolean verticalWraparoundEnabled;
    private boolean thirdDimensionWraparoundEnabled;
    private String name;
    private String hint;
    private String author;
    private int difficulty;
    private int foundX, foundY;
    private boolean moveShootAllowed;
    private int activeEra;
    private ImageUndoEngine iue;
    private static final int PLAYER_DIMS = 3;
    private static final int NUM_PLAYERS = 9;
    private static final int NUM_ERAS = 3;
    private static final int PLAYER_1 = 0;
    private static final int PROGRESS_STAGES = 6;

    // Constructors
    public CurrentArenaData() {
	this.pastEraData = new LowLevelArenaDataStore(AbstractArenaData.MIN_COLUMNS, AbstractArenaData.MIN_ROWS,
		AbstractArenaData.MIN_FLOORS, ArenaConstants.NUM_LAYERS);
	this.presentEraData = new LowLevelArenaDataStore(AbstractArenaData.MIN_COLUMNS, AbstractArenaData.MIN_ROWS,
		AbstractArenaData.MIN_FLOORS, ArenaConstants.NUM_LAYERS);
	this.futureEraData = new LowLevelArenaDataStore(AbstractArenaData.MIN_COLUMNS, AbstractArenaData.MIN_ROWS,
		AbstractArenaData.MIN_FLOORS, ArenaConstants.NUM_LAYERS);
	this.virtualData = new LowLevelArenaDataStore(AbstractArenaData.MIN_COLUMNS, AbstractArenaData.MIN_ROWS,
		AbstractArenaData.MIN_FLOORS, ArenaConstants.NUM_VIRTUAL_LAYERS);
	this.fillVirtual();
	this.dirtyData = new FlagStorage(AbstractArenaData.MIN_COLUMNS, AbstractArenaData.MIN_ROWS,
		AbstractArenaData.MIN_FLOORS);
	this.pastSavedTowerState = new LowLevelArenaDataStore(AbstractArenaData.MIN_ROWS, AbstractArenaData.MIN_COLUMNS,
		AbstractArenaData.MIN_FLOORS, ArenaConstants.NUM_LAYERS);
	this.presentSavedTowerState = new LowLevelArenaDataStore(AbstractArenaData.MIN_ROWS,
		AbstractArenaData.MIN_COLUMNS, AbstractArenaData.MIN_FLOORS, ArenaConstants.NUM_LAYERS);
	this.futureSavedTowerState = new LowLevelArenaDataStore(AbstractArenaData.MIN_ROWS,
		AbstractArenaData.MIN_COLUMNS, AbstractArenaData.MIN_FLOORS, ArenaConstants.NUM_LAYERS);
	this.startData = new NumberStorage(CurrentArenaData.PLAYER_DIMS, CurrentArenaData.NUM_PLAYERS,
		CurrentArenaData.NUM_ERAS);
	this.startData.fill(-1);
	this.savedStartData = new NumberStorage(CurrentArenaData.PLAYER_DIMS, CurrentArenaData.NUM_PLAYERS,
		CurrentArenaData.NUM_ERAS);
	this.savedStartData.fill(-1);
	this.horizontalWraparoundEnabled = false;
	this.verticalWraparoundEnabled = false;
	this.name = StringLoader.loadString(StringConstants.GENERIC_STRINGS_FILE,
		StringConstants.GENERIC_STRING_UN_NAMED_LEVEL);
	this.author = StringLoader.loadString(StringConstants.GENERIC_STRINGS_FILE,
		StringConstants.GENERIC_STRING_UNKNOWN_AUTHOR);
	this.hint = StringConstants.COMMON_STRING_EMPTY;
	this.difficulty = 1;
	this.foundX = -1;
	this.foundY = -1;
	this.iue = new ImageUndoEngine();
	this.moveShootAllowed = true;
	this.activeEra = EraConstants.ERA_PRESENT;
    }

    // Methods
    static int getProgressStages() {
	return CurrentArenaData.PROGRESS_STAGES;
    }

    @Override
    public CurrentArenaData clone() {
	try {
	    final CurrentArenaData copy = new CurrentArenaData();
	    copy.pastEraData = (LowLevelArenaDataStore) this.pastEraData.clone();
	    copy.presentEraData = (LowLevelArenaDataStore) this.presentEraData.clone();
	    copy.futureEraData = (LowLevelArenaDataStore) this.futureEraData.clone();
	    copy.pastSavedTowerState = (LowLevelArenaDataStore) this.pastSavedTowerState.clone();
	    copy.presentSavedTowerState = (LowLevelArenaDataStore) this.presentSavedTowerState.clone();
	    copy.futureSavedTowerState = (LowLevelArenaDataStore) this.futureSavedTowerState.clone();
	    copy.startData = new NumberStorage(this.startData);
	    copy.savedStartData = new NumberStorage(this.savedStartData);
	    copy.horizontalWraparoundEnabled = this.horizontalWraparoundEnabled;
	    copy.verticalWraparoundEnabled = this.verticalWraparoundEnabled;
	    copy.author = this.author;
	    copy.name = this.name;
	    copy.hint = this.hint;
	    copy.difficulty = this.difficulty;
	    copy.moveShootAllowed = this.moveShootAllowed;
	    return copy;
	} catch (final CloneNotSupportedException cnse) {
	    LTRemix.getErrorLogger().logError(cnse);
	    return null;
	}
    }

    public static int getMaxPlayers() {
	return CurrentArenaData.NUM_PLAYERS;
    }

    @Override
    public boolean isMoveShootAllowed() {
	return this.moveShootAllowed;
    }

    @Override
    public void setMoveShootAllowed(final boolean value) {
	this.moveShootAllowed = value;
    }

    @Override
    public String getName() {
	return this.name;
    }

    @Override
    public void setName(final String newName) {
	this.name = newName;
    }

    @Override
    public String getHint() {
	return this.hint;
    }

    @Override
    public void setHint(final String newHint) {
	this.hint = newHint;
    }

    @Override
    public String getAuthor() {
	return this.author;
    }

    @Override
    public void setAuthor(final String newAuthor) {
	this.author = newAuthor;
    }

    @Override
    public int getDifficulty() {
	return this.difficulty;
    }

    @Override
    public void setDifficulty(final int newDifficulty) {
	this.difficulty = newDifficulty;
    }

    @Override
    public boolean isCellDirty(final int row, final int col, final int floor) {
	int fR = row;
	int fC = col;
	int fF = floor;
	if (this.verticalWraparoundEnabled) {
	    fC = this.normalizeColumn(fC);
	}
	if (this.horizontalWraparoundEnabled) {
	    fR = this.normalizeRow(fR);
	}
	if (this.thirdDimensionWraparoundEnabled) {
	    fF = this.normalizeFloor(fF);
	}
	return this.dirtyData.getCell(fC, fR, fF);
    }

    @Override
    public AbstractArenaObject getCell(final int row, final int col, final int floor, final int layer) {
	int fR = row;
	int fC = col;
	int fF = floor;
	if (this.verticalWraparoundEnabled) {
	    fC = this.normalizeColumn(fC);
	}
	if (this.horizontalWraparoundEnabled) {
	    fR = this.normalizeRow(fR);
	}
	if (this.thirdDimensionWraparoundEnabled) {
	    fF = this.normalizeFloor(fF);
	}
	if (this.activeEra == EraConstants.ERA_PAST) {
	    return this.pastEraData.getArenaDataCell(fC, fR, fF, layer);
	} else if (this.activeEra == EraConstants.ERA_PRESENT) {
	    return this.presentEraData.getArenaDataCell(fC, fR, fF, layer);
	} else if (this.activeEra == EraConstants.ERA_FUTURE) {
	    return this.futureEraData.getArenaDataCell(fC, fR, fF, layer);
	} else {
	    return this.presentEraData.getArenaDataCell(fC, fR, fF, layer);
	}
    }

    @Override
    public AbstractArenaObject getCellEra(final int row, final int col, final int floor, final int layer,
	    final int era) {
	int fR = row;
	int fC = col;
	int fF = floor;
	if (this.verticalWraparoundEnabled) {
	    fC = this.normalizeColumn(fC);
	}
	if (this.horizontalWraparoundEnabled) {
	    fR = this.normalizeRow(fR);
	}
	if (this.thirdDimensionWraparoundEnabled) {
	    fF = this.normalizeFloor(fF);
	}
	if (era == EraConstants.ERA_PAST) {
	    return this.pastEraData.getArenaDataCell(fC, fR, fF, layer);
	} else if (era == EraConstants.ERA_PRESENT) {
	    return this.presentEraData.getArenaDataCell(fC, fR, fF, layer);
	} else if (era == EraConstants.ERA_FUTURE) {
	    return this.futureEraData.getArenaDataCell(fC, fR, fF, layer);
	} else {
	    return this.presentEraData.getArenaDataCell(fC, fR, fF, layer);
	}
    }

    @Override
    public AbstractArenaObject getVirtualCell(final int row, final int col, final int floor, final int layer) {
	int fR = row;
	int fC = col;
	int fF = floor;
	if (this.verticalWraparoundEnabled) {
	    fC = this.normalizeColumn(fC);
	}
	if (this.horizontalWraparoundEnabled) {
	    fR = this.normalizeRow(fR);
	}
	if (this.thirdDimensionWraparoundEnabled) {
	    fF = this.normalizeFloor(fF);
	}
	return this.virtualData.getArenaDataCell(fC, fR, fF, layer);
    }

    @Override
    public int getStartRow() {
	return this.startData.getCell(1, CurrentArenaData.PLAYER_1, this.activeEra);
    }

    @Override
    public int getStartColumn() {
	return this.startData.getCell(0, CurrentArenaData.PLAYER_1, this.activeEra);
    }

    @Override
    public int getStartFloor() {
	return this.startData.getCell(2, CurrentArenaData.PLAYER_1, this.activeEra);
    }

    private int getStartRowEra(final int era) {
	return this.startData.getCell(1, CurrentArenaData.PLAYER_1, era);
    }

    private int getStartColumnEra(final int era) {
	return this.startData.getCell(0, CurrentArenaData.PLAYER_1, era);
    }

    private int getStartFloorEra(final int era) {
	return this.startData.getCell(2, CurrentArenaData.PLAYER_1, era);
    }

    @Override
    public int getPlayerRow(final int playerNum, final int era) {
	return this.startData.getCell(1, playerNum, era);
    }

    @Override
    public int getPlayerColumn(final int playerNum, final int era) {
	return this.startData.getCell(0, playerNum, era);
    }

    @Override
    public int getPlayerFloor(final int playerNum, final int era) {
	return this.startData.getCell(2, playerNum, era);
    }

    @Override
    public int getRows() {
	return this.presentEraData.getShape()[1];
    }

    @Override
    public int getColumns() {
	return this.presentEraData.getShape()[0];
    }

    @Override
    public int getFloors() {
	return this.presentEraData.getShape()[2];
    }

    @Override
    public int getEra() {
	return this.activeEra;
    }

    @Override
    public void setEra(final int newEra) {
	this.activeEra = newEra;
    }

    @Override
    public boolean doesPlayerExist() {
	final int px0 = this.getStartRowEra(EraConstants.ERA_PAST);
	final int py0 = this.getStartColumnEra(EraConstants.ERA_PAST);
	final int pz0 = this.getStartFloorEra(EraConstants.ERA_PAST);
	final int px1 = this.getStartRowEra(EraConstants.ERA_PRESENT);
	final int py1 = this.getStartColumnEra(EraConstants.ERA_PRESENT);
	final int pz1 = this.getStartFloorEra(EraConstants.ERA_PRESENT);
	final int px2 = this.getStartRowEra(EraConstants.ERA_FUTURE);
	final int py2 = this.getStartColumnEra(EraConstants.ERA_FUTURE);
	final int pz2 = this.getStartFloorEra(EraConstants.ERA_FUTURE);
	return px0 != -1 && py0 != -1 && pz0 != -1 && px1 != -1 && py1 != -1 && pz1 != -1 && px2 != -1 && py2 != -1
		&& pz2 != -1;
    }

    @Override
    public boolean findStart() {
	final Tank t = new Tank();
	int w, y, x, z;
	boolean found = false;
	for (w = 0; w < CurrentArenaData.NUM_PLAYERS; w++) {
	    for (x = 0; x < this.getColumns(); x++) {
		for (y = 0; y < this.getRows(); y++) {
		    for (z = 0; z < this.getFloors(); z++) {
			final AbstractArenaObject mo = this.getCell(y, x, z, t.getPrimaryLayer());
			if (mo != null) {
			    if (mo instanceof AbstractCharacter && mo.getInstanceNum() == w) {
				this.startData.setCell(x, 1, w, this.activeEra);
				this.startData.setCell(y, 0, w, this.activeEra);
				this.startData.setCell(z, 2, w, this.activeEra);
				found = true;
			    }
			}
		    }
		}
	    }
	}
	return found;
    }

    @Override
    public void tickTimers(final int floor, final int actionType) {
	int floorFix = floor;
	if (this.thirdDimensionWraparoundEnabled) {
	    floorFix = this.normalizeFloor(floorFix);
	}
	int x, y, z, w;
	// Tick all ArenaObject timers
	AbstractTunnel.checkTunnels();
	for (z = DirectionConstants.NORTH; z < DirectionConstants.COUNT; z += 2) {
	    for (x = 0; x < this.getColumns(); x++) {
		for (y = 0; y < this.getRows(); y++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			final AbstractArenaObject mo = this.getCell(y, x, floorFix, w);
			if (mo != null) {
			    if (z == DirectionConstants.NORTH) {
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
    public void checkForEnemies(final int floorIn, final int enemyLocXIn, final int enemyLocYIn,
	    final AbstractArenaObject enemy) {
	if (enemy instanceof AntiTankDisguise) {
	    // Anti Tanks are fooled by disguises
	    return;
	}
	final AntiTank template = new AntiTank();
	int enemyLocX = enemyLocXIn;
	int enemyLocY = enemyLocYIn;
	int floor = floorIn;
	if (this.verticalWraparoundEnabled) {
	    enemyLocX = this.normalizeColumn(enemyLocX);
	}
	if (this.horizontalWraparoundEnabled) {
	    enemyLocY = this.normalizeRow(enemyLocY);
	}
	if (this.thirdDimensionWraparoundEnabled) {
	    floor = this.normalizeFloor(floor);
	}
	final boolean scanE = this.linearScan(enemyLocX, enemyLocY, floor, DirectionConstants.EAST);
	if (scanE) {
	    try {
		final AntiTank at = (AntiTank) this.getCell(this.foundX, this.foundY, floor,
			template.getPrimaryLayer());
		at.kill(this.foundX, this.foundY);
	    } catch (final ClassCastException cce) {
		// Ignore
	    }
	}
	final boolean scanW = this.linearScan(enemyLocX, enemyLocY, floor, DirectionConstants.WEST);
	if (scanW) {
	    try {
		final AntiTank at = (AntiTank) this.getCell(this.foundX, this.foundY, floor,
			template.getPrimaryLayer());
		at.kill(this.foundX, this.foundY);
	    } catch (final ClassCastException cce) {
		// Ignore
	    }
	}
	final boolean scanS = this.linearScan(enemyLocX, enemyLocY, floor, DirectionConstants.SOUTH);
	if (scanS) {
	    try {
		final AntiTank at = (AntiTank) this.getCell(this.foundX, this.foundY, floor,
			template.getPrimaryLayer());
		at.kill(this.foundX, this.foundY);
	    } catch (final ClassCastException cce) {
		// Ignore
	    }
	}
	final boolean scanN = this.linearScan(enemyLocX, enemyLocY, floor, DirectionConstants.NORTH);
	if (scanN) {
	    try {
		final AntiTank at = (AntiTank) this.getCell(this.foundX, this.foundY, floor,
			template.getPrimaryLayer());
		at.kill(this.foundX, this.foundY);
	    } catch (final ClassCastException cce) {
		// Ignore
	    }
	}
    }

    @Override
    public int checkForMagnetic(final int floor, final int centerX, final int centerY, final int dir) {
	if (dir == DirectionConstants.EAST) {
	    return this.linearScanMagnetic(centerX, centerY, floor, DirectionConstants.EAST);
	} else if (dir == DirectionConstants.WEST) {
	    return this.linearScanMagnetic(centerX, centerY, floor, DirectionConstants.WEST);
	} else if (dir == DirectionConstants.SOUTH) {
	    return this.linearScanMagnetic(centerX, centerY, floor, DirectionConstants.SOUTH);
	} else if (dir == DirectionConstants.NORTH) {
	    return this.linearScanMagnetic(centerX, centerY, floor, DirectionConstants.NORTH);
	}
	return 0;
    }

    @Override
    public boolean linearScan(final int xIn, final int yIn, final int zIn, final int d) {
	// Perform the scan
	int xFix = xIn;
	int yFix = yIn;
	int zFix = zIn;
	if (this.verticalWraparoundEnabled) {
	    xFix = this.normalizeColumn(xFix);
	}
	if (this.horizontalWraparoundEnabled) {
	    yFix = this.normalizeRow(yFix);
	}
	if (this.thirdDimensionWraparoundEnabled) {
	    zFix = this.normalizeFloor(zFix);
	}
	int u, w;
	if (d == DirectionConstants.NORTH) {
	    final AbstractArenaObject tank = LTRemix.getApplication().getGameManager().getTank();
	    if (tank.getSavedObject().isSolid()) {
		return false;
	    } else {
		for (u = yFix - 1; u >= 0; u--) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			try {
			    final AbstractArenaObject obj = this.getCell(xFix, u, zFix, w);
			    if (obj.isOfType(TypeConstants.TYPE_ANTI)) {
				final int[] unres = DirectionResolver.unresolveRelativeDirection(obj.getDirection());
				final int invert = DirectionResolver.resolveRelativeDirectionInvert(unres[0], unres[1]);
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
	} else if (d == DirectionConstants.SOUTH) {
	    final AbstractArenaObject tank = LTRemix.getApplication().getGameManager().getTank();
	    if (tank.getSavedObject().isSolid()) {
		return false;
	    } else {
		for (u = yFix + 1; u < 24; u++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			try {
			    final AbstractArenaObject obj = this.getCell(xFix, u, zFix, w);
			    if (obj.isOfType(TypeConstants.TYPE_ANTI)) {
				final int[] unres = DirectionResolver.unresolveRelativeDirection(obj.getDirection());
				final int invert = DirectionResolver.resolveRelativeDirectionInvert(unres[0], unres[1]);
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
	} else if (d == DirectionConstants.WEST) {
	    final AbstractArenaObject tank = LTRemix.getApplication().getGameManager().getTank();
	    if (tank.getSavedObject().isSolid()) {
		return false;
	    } else {
		for (u = xFix - 1; u >= 0; u--) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			try {
			    final AbstractArenaObject obj = this.getCell(u, yFix, zFix, w);
			    if (obj.isOfType(TypeConstants.TYPE_ANTI)) {
				final int[] unres = DirectionResolver.unresolveRelativeDirection(obj.getDirection());
				final int invert = DirectionResolver.resolveRelativeDirectionInvert(unres[0], unres[1]);
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
	} else if (d == DirectionConstants.EAST) {
	    final AbstractArenaObject tank = LTRemix.getApplication().getGameManager().getTank();
	    if (tank.getSavedObject().isSolid()) {
		return false;
	    } else {
		for (u = xFix + 1; u < 24; u++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			try {
			    final AbstractArenaObject obj = this.getCell(u, yFix, zFix, w);
			    if (obj.isOfType(TypeConstants.TYPE_ANTI)) {
				final int[] unres = DirectionResolver.unresolveRelativeDirection(obj.getDirection());
				final int invert = DirectionResolver.resolveRelativeDirectionInvert(unres[0], unres[1]);
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
    public int linearScanMagnetic(final int xIn, final int yIn, final int zIn, final int d) {
	// Perform the scan
	int xFix = xIn;
	int yFix = yIn;
	int zFix = zIn;
	if (this.verticalWraparoundEnabled) {
	    xFix = this.normalizeColumn(xFix);
	}
	if (this.horizontalWraparoundEnabled) {
	    yFix = this.normalizeRow(yFix);
	}
	if (this.thirdDimensionWraparoundEnabled) {
	    zFix = this.normalizeFloor(zFix);
	}
	int u, w;
	if (d == DirectionConstants.NORTH) {
	    for (u = yFix - 1; u >= 0; u--) {
		for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
		    try {
			final AbstractArenaObject obj = this.getCell(xFix, u, zFix, w);
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
	} else if (d == DirectionConstants.SOUTH) {
	    for (u = yFix + 1; u < 24; u++) {
		for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
		    try {
			final AbstractArenaObject obj = this.getCell(xFix, u, zFix, w);
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
	} else if (d == DirectionConstants.WEST) {
	    for (u = xFix - 1; u >= 0; u--) {
		for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
		    try {
			final AbstractArenaObject obj = this.getCell(u, yFix, zFix, w);
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
	} else if (d == DirectionConstants.EAST) {
	    for (u = xFix + 1; u < 24; u++) {
		for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
		    try {
			final AbstractArenaObject obj = this.getCell(u, yFix, zFix, w);
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
    public int[] findObject(final int z, final String targetName) {
	// Perform the scan
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		for (int w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
		    try {
			final AbstractArenaObject obj = this.getCell(x, y, z, w);
			final String testName = obj.getBaseName();
			if (testName.equals(targetName)) {
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
    public int[] tunnelScan(final int xIn, final int yIn, final int zIn, final String targetName) {
	int xFix = xIn;
	int yFix = yIn;
	int zFix = zIn;
	if (this.verticalWraparoundEnabled) {
	    xFix = this.normalizeColumn(xFix);
	}
	if (this.horizontalWraparoundEnabled) {
	    yFix = this.normalizeRow(yFix);
	}
	if (this.thirdDimensionWraparoundEnabled) {
	    zFix = this.normalizeFloor(zFix);
	}
	int u, v, w, skipIter;
	skipIter = yFix * this.getRows() + xFix;
	for (v = 0; v < this.getColumns(); v++) {
	    for (u = 0; u < this.getRows(); u++) {
		if (v * this.getRows() + u <= skipIter) {
		    continue;
		}
		for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
		    final AbstractArenaObject obj = this.getCell(u, v, zFix, w);
		    if (obj.getImageName().equals(targetName)) {
			return new int[] { u, v, zFix };
		    }
		}
	    }
	}
	for (v = 0; v < this.getColumns(); v++) {
	    for (u = 0; u < this.getRows(); u++) {
		if (v * this.getRows() + u >= skipIter) {
		    continue;
		}
		for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
		    final AbstractArenaObject obj = this.getCell(u, v, zFix, w);
		    if (obj.getImageName().equals(targetName)) {
			return new int[] { u, v, zFix };
		    }
		}
	    }
	}
	return null;
    }

    @Override
    public void circularScanRange(final int xIn, final int yIn, final int zIn, final int r, final int rangeType,
	    final int forceUnits) {
	int xFix = xIn;
	int yFix = yIn;
	int zFix = zIn;
	if (this.verticalWraparoundEnabled) {
	    xFix = this.normalizeColumn(xFix);
	}
	if (this.horizontalWraparoundEnabled) {
	    yFix = this.normalizeRow(yFix);
	}
	if (this.thirdDimensionWraparoundEnabled) {
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
			this.getCell(u, v, zFix, w).rangeAction(xFix, yFix, zFix, u - xFix, v - yFix, rangeType,
				forceUnits);
		    } catch (final ArrayIndexOutOfBoundsException aioob) {
			// Do nothing
		    }
		}
	    }
	}
    }

    @Override
    public boolean circularScanTank(final int x, final int y, final int z, final int r) {
	final int[] tankLoc = LTRemix.getApplication().getGameManager().getTankLocation();
	int fX = x;
	int fY = y;
	int fZ = z;
	if (this.verticalWraparoundEnabled) {
	    fX = this.normalizeColumn(fX);
	}
	if (this.horizontalWraparoundEnabled) {
	    fY = this.normalizeRow(fY);
	}
	if (this.thirdDimensionWraparoundEnabled) {
	    fZ = this.normalizeFloor(fZ);
	}
	final int tx = tankLoc[0];
	final int ty = tankLoc[1];
	final int tz = tankLoc[2];
	return fZ == tz && Math.abs(fX - tx) <= r && Math.abs(fY - ty) <= r;
    }

    @Override
    public void fullScanActivateTanks() {
	// Perform the scan
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		for (int z = 0; z < this.getFloors(); z++) {
		    final AbstractArenaObject obj = this.getCell(y, x, z, ArenaConstants.LAYER_UPPER_OBJECTS);
		    if (obj instanceof AbstractCharacter) {
			obj.setEnabled(true);
		    }
		}
	    }
	}
    }

    @Override
    public void fullScanProcessTanks(final AbstractCharacter activeTank) {
	// Perform the scan
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		for (int z = 0; z < this.getFloors(); z++) {
		    final AbstractArenaObject obj = this.getCell(y, x, z, ArenaConstants.LAYER_UPPER_OBJECTS);
		    if (obj instanceof AbstractCharacter) {
			if (obj.equals(activeTank)) {
			    obj.setEnabled(true);
			} else {
			    obj.setEnabled(false);
			}
		    }
		}
	    }
	}
    }

    @Override
    public void fullScanMoveObjects(final int locZ, final int dirX, final int dirY) {
	// Perform the scan
	final Application app = LTRemix.getApplication();
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		final AbstractArenaObject obj = this.getCell(y, x, locZ, ArenaConstants.LAYER_LOWER_OBJECTS);
		if (obj instanceof AbstractMovableObject) {
		    final AbstractMovableObject mobj = (AbstractMovableObject) obj;
		    if (mobj.didPreCheck() && !mobj.didMove()) {
			app.getGameManager().updatePushedPosition(x, y, x + dirX, y + dirY, mobj);
			mobj.setMoved();
		    }
		}
	    }
	}
    }

    @Override
    public void fullScanKillTanks() {
	// Perform the scan
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		for (int z = 0; z < this.getFloors(); z++) {
		    final AbstractArenaObject obj = this.getCell(y, x, z, ArenaConstants.LAYER_LOWER_OBJECTS);
		    if (obj instanceof AntiTank) {
			// Kill the tank
			final GameManager gm = LTRemix.getApplication().getGameManager();
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
    public void fullScanFreezeGround() {
	// Perform the scan
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		for (int z = 0; z < this.getFloors(); z++) {
		    final AbstractArenaObject obj = this.getCell(y, x, z, ArenaConstants.LAYER_LOWER_GROUND);
		    if (!(obj instanceof Ground)) {
			// Freeze the ground
			LTRemix.getApplication().getGameManager().morph(
				obj.changesToOnExposure(MaterialConstants.MATERIAL_ICE), y, x, z,
				ArenaConstants.LAYER_LOWER_GROUND);
		    }
		}
	    }
	}
    }

    @Override
    public void fullScanAllButtonOpen(final int zIn, final AbstractButton source) {
	// Perform the scan
	int zFix = zIn;
	if (this.thirdDimensionWraparoundEnabled) {
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
		final AbstractArenaObject obj = this.getCell(y, x, zFix, source.getPrimaryLayer());
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
	    if (!(this.getCell(dx, dy, zFix, source.getPrimaryLayer()) instanceof Ground)) {
		this.setCell(new Ground(), dx, dy, zFix, source.getPrimaryLayer());
		SoundManager.playSound(SoundConstants.SOUND_DOOR_OPENS);
	    }
	}
    }

    @Override
    public void fullScanAllButtonClose(final int zIn, final AbstractButton source) {
	// Perform the scan
	int zFix = zIn;
	if (this.thirdDimensionWraparoundEnabled) {
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
		final AbstractArenaObject obj = this.getCell(y, x, zFix, source.getPrimaryLayer());
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
	    if (!this.getCell(dx, dy, zFix, source.getPrimaryLayer()).getClass()
		    .equals(source.getButtonDoor().getClass())) {
		this.setCell(source.getButtonDoor(), dx, dy, zFix, source.getPrimaryLayer());
		SoundManager.playSound(SoundConstants.SOUND_DOOR_CLOSES);
	    }
	}
    }

    @Override
    public void fullScanButtonBind(final int dx, final int dy, final int zIn, final AbstractButtonDoor source) {
	// Perform the scan
	int z = zIn;
	if (this.thirdDimensionWraparoundEnabled) {
	    z = this.normalizeFloor(z);
	}
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		final AbstractArenaObject obj = this.getCell(x, y, z, source.getPrimaryLayer());
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
		final AbstractArenaObject obj = this.getCell(x, y, z, source.getPrimaryLayer());
		if (obj instanceof AbstractButtonDoor) {
		    final AbstractButtonDoor door = (AbstractButtonDoor) obj;
		    if (source.getClass().equals(door.getClass())) {
			this.setCell(new Ground(), x, y, z, source.getPrimaryLayer());
		    }
		}
	    }
	}
    }

    @Override
    public void fullScanButtonCleanup(final int px, final int py, final int zIn, final AbstractButton button) {
	// Perform the scan
	int zFix = zIn;
	if (this.thirdDimensionWraparoundEnabled) {
	    zFix = this.normalizeFloor(zFix);
	}
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		if (x == px && y == py) {
		    continue;
		}
		final AbstractArenaObject obj = this.getCell(x, y, zFix, button.getPrimaryLayer());
		if (obj instanceof AbstractButton) {
		    if (((AbstractButton) obj).boundButtonDoorEquals(button)) {
			this.setCell(new Ground(), x, y, zFix, button.getPrimaryLayer());
		    }
		}
	    }
	}
    }

    @Override
    public void fullScanFindButtonLostDoor(final int zIn, final AbstractButtonDoor door) {
	// Perform the scan
	int zFix = zIn;
	if (this.thirdDimensionWraparoundEnabled) {
	    zFix = this.normalizeFloor(zFix);
	}
	for (int x = 0; x < AbstractArenaData.MIN_COLUMNS; x++) {
	    for (int y = 0; y < AbstractArenaData.MIN_ROWS; y++) {
		final AbstractArenaObject obj = this.getCell(x, y, zFix, door.getPrimaryLayer());
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
    public void setCell(final AbstractArenaObject mo, final int row, final int col, final int floor, final int layer) {
	int fR = row;
	int fC = col;
	int fF = floor;
	if (this.verticalWraparoundEnabled) {
	    fC = this.normalizeColumn(fC);
	}
	if (this.horizontalWraparoundEnabled) {
	    fR = this.normalizeRow(fR);
	}
	if (this.thirdDimensionWraparoundEnabled) {
	    fF = this.normalizeFloor(fF);
	}
	if (this.activeEra == EraConstants.ERA_PAST) {
	    this.pastEraData.setArenaDataCell(mo, fC, fR, fF, layer);
	    if (!(mo instanceof AbstractCharacter)) {
		this.presentEraData.setArenaDataCell(mo, fC, fR, fF, layer);
		this.futureEraData.setArenaDataCell(mo, fC, fR, fF, layer);
	    }
	} else if (this.activeEra == EraConstants.ERA_PRESENT) {
	    this.presentEraData.setArenaDataCell(mo, fC, fR, fF, layer);
	    if (!(mo instanceof AbstractCharacter)) {
		this.futureEraData.setArenaDataCell(mo, fC, fR, fF, layer);
	    }
	} else if (this.activeEra == EraConstants.ERA_FUTURE) {
	    this.futureEraData.setArenaDataCell(mo, fC, fR, fF, layer);
	} else {
	    this.presentEraData.setArenaDataCell(mo, fC, fR, fF, layer);
	    if (!(mo instanceof AbstractCharacter)) {
		this.futureEraData.setArenaDataCell(mo, fC, fR, fF, layer);
	    }
	}
	this.dirtyData.setCell(true, fC, fR, fF);
    }

    @Override
    public void setCellEra(final AbstractArenaObject mo, final int row, final int col, final int floor, final int layer,
	    final int era) {
	int fR = row;
	int fC = col;
	int fF = floor;
	if (this.verticalWraparoundEnabled) {
	    fC = this.normalizeColumn(fC);
	}
	if (this.horizontalWraparoundEnabled) {
	    fR = this.normalizeRow(fR);
	}
	if (this.thirdDimensionWraparoundEnabled) {
	    fF = this.normalizeFloor(fF);
	}
	if (era == EraConstants.ERA_PAST) {
	    this.pastEraData.setArenaDataCell(mo, fC, fR, fF, layer);
	} else if (era == EraConstants.ERA_PRESENT) {
	    this.presentEraData.setArenaDataCell(mo, fC, fR, fF, layer);
	} else if (era == EraConstants.ERA_FUTURE) {
	    this.futureEraData.setArenaDataCell(mo, fC, fR, fF, layer);
	}
    }

    @Override
    public void markAsDirty(final int row, final int col, final int floor) {
	int fR = row;
	int fC = col;
	int fF = floor;
	if (this.verticalWraparoundEnabled) {
	    fC = this.normalizeColumn(fC);
	}
	if (this.horizontalWraparoundEnabled) {
	    fR = this.normalizeRow(fR);
	}
	if (this.thirdDimensionWraparoundEnabled) {
	    fF = this.normalizeFloor(fF);
	}
	this.dirtyData.setCell(true, fC, fR, fF);
    }

    @Override
    public void markAsClean(final int row, final int col, final int floor) {
	int fR = row;
	int fC = col;
	int fF = floor;
	if (this.verticalWraparoundEnabled) {
	    fC = this.normalizeColumn(fC);
	}
	if (this.horizontalWraparoundEnabled) {
	    fR = this.normalizeRow(fR);
	}
	if (this.thirdDimensionWraparoundEnabled) {
	    fF = this.normalizeFloor(fF);
	}
	this.dirtyData.setCell(false, fC, fR, fF);
    }

    @Override
    public void setVirtualCell(final AbstractArenaObject mo, final int row, final int col, final int floor,
	    final int layer) {
	int fR = row;
	int fC = col;
	int fF = floor;
	if (this.verticalWraparoundEnabled) {
	    fC = this.normalizeColumn(fC);
	}
	if (this.horizontalWraparoundEnabled) {
	    fR = this.normalizeRow(fR);
	}
	if (this.thirdDimensionWraparoundEnabled) {
	    fF = this.normalizeFloor(fF);
	}
	this.virtualData.setArenaDataCell(mo, fC, fR, fF, layer);
	this.dirtyData.setCell(true, fC, fR, fF);
    }

    @Override
    public void setAllDirtyFlags() {
	for (int floor = 0; floor < this.getFloors(); floor++) {
	    this.setDirtyFlags(floor);
	}
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
    public void setDirtyFlags(final int floor) {
	for (int row = 0; row < this.getRows(); row++) {
	    for (int col = 0; col < this.getColumns(); col++) {
		this.dirtyData.setCell(true, col, row, floor);
	    }
	}
    }

    @Override
    public void clearVirtualGrid() {
	for (int row = 0; row < this.getRows(); row++) {
	    for (int col = 0; col < this.getColumns(); col++) {
		for (int floor = 0; floor < this.getFloors(); floor++) {
		    for (int layer = 0; layer < ArenaConstants.NUM_VIRTUAL_LAYERS; layer++) {
			this.setVirtualCell(new Empty(), row, col, floor, layer);
		    }
		}
	    }
	}
    }

    @Override
    public void setPlayerRow(final int newPlayerRow, final int playerNum, final int era) {
	int fR = newPlayerRow;
	if (this.horizontalWraparoundEnabled) {
	    fR = this.normalizeRow(fR);
	}
	this.startData.setCell(fR, 1, playerNum, era);
    }

    @Override
    public void setPlayerColumn(final int newPlayerColumn, final int playerNum, final int era) {
	int fC = newPlayerColumn;
	if (this.verticalWraparoundEnabled) {
	    fC = this.normalizeColumn(fC);
	}
	this.startData.setCell(fC, 0, playerNum, era);
    }

    @Override
    public void setPlayerFloor(final int newPlayerFloor, final int playerNum, final int era) {
	int fF = newPlayerFloor;
	if (this.thirdDimensionWraparoundEnabled) {
	    fF = this.normalizeFloor(fF);
	}
	this.startData.setCell(fF, 2, playerNum, era);
    }

    @Override
    public void fill(final AbstractArenaObject fill) {
	int y, x, z, w, e;
	for (e = 0; e < EraConstants.MAX_ERAS; e++) {
	    for (x = 0; x < this.getColumns(); x++) {
		for (y = 0; y < this.getRows(); y++) {
		    for (z = 0; z < this.getFloors(); z++) {
			for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			    if (w == ArenaConstants.LAYER_LOWER_GROUND) {
				this.setCellEra(fill, y, x, z, w, e);
			    } else {
				this.setCellEra(new Empty(), y, x, z, w, e);
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
    public void save() {
	int y, x, z, w;
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			final AbstractArenaObject oldObj = this.getCellEra(y, x, z, w, EraConstants.ERA_PAST);
			final AbstractArenaObject newObj = oldObj.clone();
			this.pastSavedTowerState.setCell(newObj, x, y, z, w);
		    }
		}
	    }
	}
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			final AbstractArenaObject oldObj = this.getCellEra(y, x, z, w, EraConstants.ERA_PRESENT);
			final AbstractArenaObject newObj = oldObj.clone();
			this.presentSavedTowerState.setCell(newObj, x, y, z, w);
		    }
		}
	    }
	}
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			final AbstractArenaObject oldObj = this.getCellEra(y, x, z, w, EraConstants.ERA_FUTURE);
			final AbstractArenaObject newObj = oldObj.clone();
			this.futureSavedTowerState.setCell(newObj, x, y, z, w);
		    }
		}
	    }
	}
    }

    @Override
    public void restore() {
	int y, x, z, w;
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			final AbstractArenaObject oldObj = (AbstractArenaObject) this.pastSavedTowerState.getCell(x, y,
				z, w);
			final AbstractArenaObject newObj = oldObj.clone();
			this.setCellEra(newObj, y, x, z, w, EraConstants.ERA_PAST);
		    }
		}
	    }
	}
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			final AbstractArenaObject oldObj = (AbstractArenaObject) this.presentSavedTowerState.getCell(x,
				y, z, w);
			final AbstractArenaObject newObj = oldObj.clone();
			this.setCellEra(newObj, y, x, z, w, EraConstants.ERA_PRESENT);
		    }
		}
	    }
	}
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			final AbstractArenaObject oldObj = (AbstractArenaObject) this.futureSavedTowerState.getCell(x,
				y, z, w);
			final AbstractArenaObject newObj = oldObj.clone();
			this.setCellEra(newObj, y, x, z, w, EraConstants.ERA_FUTURE);
		    }
		}
	    }
	}
    }

    @Override
    public void saveStart() {
	this.savedStartData = new NumberStorage(this.startData);
    }

    @Override
    public void restoreStart() {
	this.startData = new NumberStorage(this.savedStartData);
    }

    @Override
    public void resize(final int zIn, final AbstractArenaObject nullFill) {
	final int x = AbstractArenaData.MIN_ROWS;
	final int y = AbstractArenaData.MIN_COLUMNS;
	int z = zIn;
	if (this.thirdDimensionWraparoundEnabled) {
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
			    tempStorage.setCell(this.getCell(v, u, w, t), u, v, w, t);
			} catch (final ArrayIndexOutOfBoundsException aioob) {
			    // Do nothing
			}
		    }
		}
	    }
	}
	// Set the current data to the temporary array
	this.presentEraData = tempStorage;
	this.virtualData = new LowLevelArenaDataStore(x, y, z, ArenaConstants.NUM_VIRTUAL_LAYERS);
	this.dirtyData = new FlagStorage(x, y, z);
	// Fill any blanks
	this.fillNulls(nullFill, null, false);
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
			    tempStorage.setCell(this.presentSavedTowerState.getCell(v, u, w, t), u, v, w, t);
			} catch (final ArrayIndexOutOfBoundsException aioob) {
			    // Do nothing
			}
		    }
		}
	    }
	}
	// Set the current data to the temporary array
	this.presentSavedTowerState = tempStorage;
	// Fill any blanks
	this.fillSTSNulls(nullFill);
    }

    @Override
    public void fillNulls(final AbstractArenaObject fill1, final AbstractArenaObject fill2, final boolean was16) {
	int y, x, z, w;
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			if (this.getCell(y, x, z, w) == null) {
			    if (w == ArenaConstants.LAYER_LOWER_GROUND) {
				this.setCellEra(fill1, y, x, z, w, this.activeEra);
			    } else if (w == ArenaConstants.LAYER_LOWER_OBJECTS && was16) {
				if (x >= 16 || y >= 16) {
				    this.setCellEra(fill2, y, x, z, w, this.activeEra);
				} else {
				    this.setCellEra(new Empty(), y, x, z, w, this.activeEra);
				}
			    } else {
				this.setCellEra(new Empty(), y, x, z, w, this.activeEra);
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
			if (this.presentSavedTowerState.getCell(y, x, z, w) == null) {
			    if (w == ArenaConstants.LAYER_LOWER_GROUND) {
				this.presentSavedTowerState.setCell(fill, y, x, z, w);
			    } else {
				this.presentSavedTowerState.setCell(new Empty(), y, x, z, w);
			    }
			}
		    }
		}
	    }
	}
    }

    @Override
    public void enableHorizontalWraparound() {
	this.horizontalWraparoundEnabled = true;
    }

    @Override
    public void disableHorizontalWraparound() {
	this.horizontalWraparoundEnabled = false;
    }

    @Override
    public void enableVerticalWraparound() {
	this.verticalWraparoundEnabled = true;
    }

    @Override
    public void disableVerticalWraparound() {
	this.verticalWraparoundEnabled = false;
    }

    @Override
    public void enableThirdDimensionWraparound() {
	this.thirdDimensionWraparoundEnabled = true;
    }

    @Override
    public void disableThirdDimensionWraparound() {
	this.thirdDimensionWraparoundEnabled = false;
    }

    @Override
    public boolean isHorizontalWraparoundEnabled() {
	return this.horizontalWraparoundEnabled;
    }

    @Override
    public boolean isVerticalWraparoundEnabled() {
	return this.verticalWraparoundEnabled;
    }

    @Override
    public boolean isThirdDimensionWraparoundEnabled() {
	return this.thirdDimensionWraparoundEnabled;
    }

    @Override
    public void writeData(final XMLFileWriter writer, final ProgressTracker pt) throws IOException {
	int y, x, z, w, e;
	writer.writeInt(this.getColumns());
	writer.writeInt(this.getRows());
	writer.writeInt(this.getFloors());
	for (e = 0; e < EraConstants.MAX_ERAS; e++) {
	    for (x = 0; x < this.getColumns(); x++) {
		for (y = 0; y < this.getRows(); y++) {
		    for (z = 0; z < this.getFloors(); z++) {
			for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			    this.getCellEra(y, x, z, w, e).writeArenaObject(writer);
			}
		    }
		}
	    }
	}
	for (x = 0; x < CurrentArenaData.PLAYER_DIMS; x++) {
	    for (y = 0; y < CurrentArenaData.NUM_PLAYERS; y++) {
		for (z = 0; z < CurrentArenaData.NUM_ERAS; z++) {
		    writer.writeInt(this.startData.getCell(x, y, z));
		}
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
	writer.writeInt(this.activeEra);
    }

    @Override
    public AbstractArenaData readData(final XMLFileReader reader, final int formatVersion, final ProgressTracker pt)
	    throws IOException {
	if (FormatConstants.isFormatVersionValidGeneration1(formatVersion)) {
	    final CurrentArenaData tempData = CurrentArenaData.readTowerG1(reader, formatVersion, pt);
	    return tempData;
	} else if (FormatConstants.isFormatVersionValidGeneration2(formatVersion)) {
	    final CurrentArenaData tempData = CurrentArenaData.readTowerG2(reader, formatVersion, pt);
	    return tempData;
	} else if (FormatConstants.isFormatVersionValidGeneration3(formatVersion)) {
	    final CurrentArenaData tempData = CurrentArenaData.readTowerG3(reader, formatVersion, pt);
	    return tempData;
	} else if (FormatConstants.isFormatVersionValidGeneration4(formatVersion)) {
	    final CurrentArenaData tempData = CurrentArenaData.readTowerG4(reader, formatVersion, pt);
	    return tempData;
	} else if (FormatConstants.isFormatVersionValidGeneration5(formatVersion)) {
	    final CurrentArenaData tempData = CurrentArenaData.readTowerG5(reader, formatVersion, pt);
	    return tempData;
	} else if (FormatConstants.isFormatVersionValidGeneration6(formatVersion)) {
	    final CurrentArenaData tempData = CurrentArenaData.readTowerG6(reader, formatVersion, pt);
	    return tempData;
	} else if (FormatConstants.isFormatVersionValidGeneration7(formatVersion)) {
	    final CurrentArenaData tempData = CurrentArenaData.readTowerG7(reader, formatVersion, pt);
	    return tempData;
	} else {
	    throw new IOException(StringLoader.loadString(StringConstants.ERROR_STRINGS_FILE,
		    StringConstants.ERROR_STRING_UNKNOWN_ARENA_FORMAT));
	}
    }

    private static CurrentArenaData readTowerG1(final XMLFileReader reader, final int ver, final ProgressTracker pt)
	    throws IOException {
	int y, x, z, arenaSizeX, arenaSizeY, arenaSizeZ;
	arenaSizeX = reader.readInt();
	arenaSizeY = reader.readInt();
	arenaSizeZ = reader.readInt();
	final CurrentArenaData lt = new CurrentArenaData();
	for (x = 0; x < arenaSizeX; x++) {
	    for (y = 0; y < arenaSizeY; y++) {
		for (z = 0; z < arenaSizeZ; z++) {
		    final AbstractArenaObject obj = LTRemix.getApplication().getObjects().readArenaObjectG2(reader,
			    ver);
		    lt.setCellEra(obj, y, x, z, obj.getPrimaryLayer(), EraConstants.ERA_PRESENT);
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	for (y = 0; y < CurrentArenaData.PLAYER_DIMS; y++) {
	    lt.startData.setCell(reader.readInt(), y, CurrentArenaData.PLAYER_1, EraConstants.ERA_PRESENT);
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	lt.horizontalWraparoundEnabled = reader.readBoolean();
	lt.verticalWraparoundEnabled = reader.readBoolean();
	lt.thirdDimensionWraparoundEnabled = false;
	lt.name = reader.readString();
	lt.hint = reader.readString();
	lt.author = reader.readString();
	lt.difficulty = reader.readInt();
	// Set Era
	lt.activeEra = EraConstants.ERA_PRESENT;
	// Fill nulls
	lt.fillNulls(new Ground(), new Wall(), true);
	lt.fillVirtual();
	if (pt != null) {
	    pt.updateProgress();
	}
	// Backwards compatibility
	try {
	    lt.pastEraData = (LowLevelArenaDataStore) lt.presentEraData.clone();
	    lt.futureEraData = (LowLevelArenaDataStore) lt.presentEraData.clone();
	} catch (final CloneNotSupportedException cnse) {
	    LTRemix.getErrorLogger().logError(cnse);
	}
	for (x = 0; x < CurrentArenaData.PLAYER_DIMS; x++) {
	    for (y = 0; y < CurrentArenaData.NUM_PLAYERS; y++) {
		for (z = 0; z < CurrentArenaData.NUM_ERAS; z++) {
		    final int curr = lt.startData.getCell(x, y, EraConstants.ERA_PRESENT);
		    lt.startData.setCell(curr, x, y, z);
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	return lt;
    }

    private static CurrentArenaData readTowerG2(final XMLFileReader reader, final int ver, final ProgressTracker pt)
	    throws IOException {
	int y, x, z, arenaSizeX, arenaSizeY, arenaSizeZ;
	arenaSizeX = reader.readInt();
	arenaSizeY = reader.readInt();
	arenaSizeZ = reader.readInt();
	final CurrentArenaData lt = new CurrentArenaData();
	lt.resize(arenaSizeZ, new Ground());
	for (x = 0; x < arenaSizeX; x++) {
	    for (y = 0; y < arenaSizeY; y++) {
		for (z = 0; z < arenaSizeZ; z++) {
		    final AbstractArenaObject obj = LTRemix.getApplication().getObjects().readArenaObjectG2(reader,
			    ver);
		    lt.setCellEra(obj, y, x, z, obj.getPrimaryLayer(), EraConstants.ERA_PRESENT);
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	for (y = 0; y < CurrentArenaData.PLAYER_DIMS; y++) {
	    lt.startData.setCell(reader.readInt(), y, CurrentArenaData.PLAYER_1, EraConstants.ERA_PRESENT);
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	lt.horizontalWraparoundEnabled = reader.readBoolean();
	lt.verticalWraparoundEnabled = reader.readBoolean();
	lt.thirdDimensionWraparoundEnabled = reader.readBoolean();
	lt.name = reader.readString();
	lt.hint = reader.readString();
	lt.author = reader.readString();
	lt.difficulty = reader.readInt();
	// Set Era
	lt.activeEra = EraConstants.ERA_PRESENT;
	// Fill nulls
	lt.fillNulls(new Ground(), null, false);
	lt.fillVirtual();
	if (pt != null) {
	    pt.updateProgress();
	}
	// Backwards compatibility
	try {
	    lt.pastEraData = (LowLevelArenaDataStore) lt.presentEraData.clone();
	    lt.futureEraData = (LowLevelArenaDataStore) lt.presentEraData.clone();
	} catch (final CloneNotSupportedException cnse) {
	    LTRemix.getErrorLogger().logError(cnse);
	}
	for (x = 0; x < CurrentArenaData.PLAYER_DIMS; x++) {
	    for (y = 0; y < CurrentArenaData.NUM_PLAYERS; y++) {
		for (z = 0; z < CurrentArenaData.NUM_ERAS; z++) {
		    final int curr = lt.startData.getCell(x, y, EraConstants.ERA_PRESENT);
		    lt.startData.setCell(curr, x, y, z);
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	return lt;
    }

    private static CurrentArenaData readTowerG3(final XMLFileReader reader, final int ver, final ProgressTracker pt)
	    throws IOException {
	int y, x, z, arenaSizeX, arenaSizeY, arenaSizeZ;
	arenaSizeX = reader.readInt();
	arenaSizeY = reader.readInt();
	arenaSizeZ = reader.readInt();
	final CurrentArenaData lt = new CurrentArenaData();
	lt.resize(arenaSizeZ, new Ground());
	for (x = 0; x < arenaSizeX; x++) {
	    for (y = 0; y < arenaSizeY; y++) {
		for (z = 0; z < arenaSizeZ; z++) {
		    final AbstractArenaObject obj = LTRemix.getApplication().getObjects().readArenaObjectG3(reader,
			    ver);
		    lt.setCellEra(obj, y, x, z, obj.getPrimaryLayer(), EraConstants.ERA_PRESENT);
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	for (y = 0; y < CurrentArenaData.PLAYER_DIMS; y++) {
	    lt.startData.setCell(reader.readInt(), y, CurrentArenaData.PLAYER_1, EraConstants.ERA_PRESENT);
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	lt.horizontalWraparoundEnabled = reader.readBoolean();
	lt.verticalWraparoundEnabled = reader.readBoolean();
	lt.thirdDimensionWraparoundEnabled = reader.readBoolean();
	lt.name = reader.readString();
	lt.hint = reader.readString();
	lt.author = reader.readString();
	lt.difficulty = reader.readInt();
	// Set Era
	lt.activeEra = EraConstants.ERA_PRESENT;
	// Fill nulls
	lt.fillNulls(new Ground(), null, false);
	lt.fillVirtual();
	if (pt != null) {
	    pt.updateProgress();
	}
	// Backwards compatibility
	try {
	    lt.pastEraData = (LowLevelArenaDataStore) lt.presentEraData.clone();
	    lt.futureEraData = (LowLevelArenaDataStore) lt.presentEraData.clone();
	} catch (final CloneNotSupportedException cnse) {
	    LTRemix.getErrorLogger().logError(cnse);
	}
	for (x = 0; x < CurrentArenaData.PLAYER_DIMS; x++) {
	    for (y = 0; y < CurrentArenaData.NUM_PLAYERS; y++) {
		for (z = 0; z < CurrentArenaData.NUM_ERAS; z++) {
		    final int curr = lt.startData.getCell(x, y, EraConstants.ERA_PRESENT);
		    lt.startData.setCell(curr, x, y, z);
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	return lt;
    }

    private static CurrentArenaData readTowerG4(final XMLFileReader reader, final int ver, final ProgressTracker pt)
	    throws IOException {
	int y, x, z, arenaSizeX, arenaSizeY, arenaSizeZ;
	arenaSizeX = reader.readInt();
	arenaSizeY = reader.readInt();
	arenaSizeZ = reader.readInt();
	final CurrentArenaData lt = new CurrentArenaData();
	lt.resize(arenaSizeZ, new Ground());
	for (x = 0; x < arenaSizeX; x++) {
	    for (y = 0; y < arenaSizeY; y++) {
		for (z = 0; z < arenaSizeZ; z++) {
		    final AbstractArenaObject obj = LTRemix.getApplication().getObjects().readArenaObjectG4(reader,
			    ver);
		    lt.setCellEra(obj, y, x, z, obj.getPrimaryLayer(), EraConstants.ERA_PRESENT);
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	for (y = 0; y < CurrentArenaData.PLAYER_DIMS; y++) {
	    lt.startData.setCell(reader.readInt(), y, CurrentArenaData.PLAYER_1, EraConstants.ERA_PRESENT);
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	lt.horizontalWraparoundEnabled = reader.readBoolean();
	lt.verticalWraparoundEnabled = reader.readBoolean();
	lt.thirdDimensionWraparoundEnabled = reader.readBoolean();
	lt.name = reader.readString();
	lt.hint = reader.readString();
	lt.author = reader.readString();
	lt.difficulty = reader.readInt();
	// Set Era
	lt.activeEra = EraConstants.ERA_PRESENT;
	// Fill nulls
	lt.fillNulls(new Ground(), null, false);
	lt.fillVirtual();
	if (pt != null) {
	    pt.updateProgress();
	}
	// Backwards compatibility
	try {
	    lt.pastEraData = (LowLevelArenaDataStore) lt.presentEraData.clone();
	    lt.futureEraData = (LowLevelArenaDataStore) lt.presentEraData.clone();
	} catch (final CloneNotSupportedException cnse) {
	    LTRemix.getErrorLogger().logError(cnse);
	}
	for (x = 0; x < CurrentArenaData.PLAYER_DIMS; x++) {
	    for (y = 0; y < CurrentArenaData.NUM_PLAYERS; y++) {
		for (z = 0; z < CurrentArenaData.NUM_ERAS; z++) {
		    final int curr = lt.startData.getCell(x, y, EraConstants.ERA_PRESENT);
		    lt.startData.setCell(curr, x, y, z);
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	return lt;
    }

    private static CurrentArenaData readTowerG5(final XMLFileReader reader, final int ver, final ProgressTracker pt)
	    throws IOException {
	int y, x, z, w, arenaSizeX, arenaSizeY, arenaSizeZ;
	arenaSizeX = reader.readInt();
	arenaSizeY = reader.readInt();
	arenaSizeZ = reader.readInt();
	final CurrentArenaData lt = new CurrentArenaData();
	lt.resize(arenaSizeZ, new Ground());
	for (x = 0; x < arenaSizeX; x++) {
	    for (y = 0; y < arenaSizeY; y++) {
		for (z = 0; z < arenaSizeZ; z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			final AbstractArenaObject obj = LTRemix.getApplication().getObjects().readArenaObjectG5(reader,
				ver);
			lt.setCellEra(obj, y, x, z, w, EraConstants.ERA_PRESENT);
		    }
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	for (y = 0; y < CurrentArenaData.PLAYER_DIMS; y++) {
	    lt.startData.setCell(reader.readInt(), y, CurrentArenaData.PLAYER_1, EraConstants.ERA_PRESENT);
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	lt.horizontalWraparoundEnabled = reader.readBoolean();
	lt.verticalWraparoundEnabled = reader.readBoolean();
	lt.thirdDimensionWraparoundEnabled = reader.readBoolean();
	lt.name = reader.readString();
	lt.hint = reader.readString();
	lt.author = reader.readString();
	lt.difficulty = reader.readInt();
	lt.moveShootAllowed = reader.readBoolean();
	// Set Era
	lt.activeEra = EraConstants.ERA_PRESENT;
	// Fill nulls
	lt.fillNulls(new Ground(), null, false);
	lt.fillVirtual();
	if (pt != null) {
	    pt.updateProgress();
	}
	// Backwards compatibility
	try {
	    lt.pastEraData = (LowLevelArenaDataStore) lt.presentEraData.clone();
	    lt.futureEraData = (LowLevelArenaDataStore) lt.presentEraData.clone();
	} catch (final CloneNotSupportedException cnse) {
	    LTRemix.getErrorLogger().logError(cnse);
	}
	for (x = 0; x < CurrentArenaData.PLAYER_DIMS; x++) {
	    for (y = 0; y < CurrentArenaData.NUM_PLAYERS; y++) {
		for (z = 0; z < CurrentArenaData.NUM_ERAS; z++) {
		    final int curr = lt.startData.getCell(x, y, EraConstants.ERA_PRESENT);
		    lt.startData.setCell(curr, x, y, z);
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	return lt;
    }

    private static CurrentArenaData readTowerG6(final XMLFileReader reader, final int ver, final ProgressTracker pt)
	    throws IOException {
	int y, x, z, w, arenaSizeX, arenaSizeY, arenaSizeZ;
	arenaSizeX = reader.readInt();
	arenaSizeY = reader.readInt();
	arenaSizeZ = reader.readInt();
	final CurrentArenaData lt = new CurrentArenaData();
	lt.resize(arenaSizeZ, new Ground());
	for (x = 0; x < arenaSizeX; x++) {
	    for (y = 0; y < arenaSizeY; y++) {
		for (z = 0; z < arenaSizeZ; z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			final AbstractArenaObject obj = LTRemix.getApplication().getObjects().readArenaObjectG6(reader,
				ver);
			lt.setCellEra(obj, y, x, z, w, EraConstants.ERA_PRESENT);
		    }
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	for (x = 0; x < CurrentArenaData.PLAYER_DIMS; x++) {
	    for (y = 0; y < CurrentArenaData.NUM_PLAYERS; y++) {
		lt.startData.setCell(reader.readInt(), x, y, EraConstants.ERA_PRESENT);
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	lt.horizontalWraparoundEnabled = reader.readBoolean();
	lt.verticalWraparoundEnabled = reader.readBoolean();
	lt.thirdDimensionWraparoundEnabled = reader.readBoolean();
	lt.name = reader.readString();
	lt.hint = reader.readString();
	lt.author = reader.readString();
	lt.difficulty = reader.readInt();
	lt.moveShootAllowed = reader.readBoolean();
	// Set Era
	lt.activeEra = EraConstants.ERA_PRESENT;
	// Fill nulls
	lt.fillNulls(new Ground(), null, false);
	lt.fillVirtual();
	if (pt != null) {
	    pt.updateProgress();
	}
	// Backwards compatibility
	try {
	    lt.pastEraData = (LowLevelArenaDataStore) lt.presentEraData.clone();
	    lt.futureEraData = (LowLevelArenaDataStore) lt.presentEraData.clone();
	} catch (final CloneNotSupportedException cnse) {
	    LTRemix.getErrorLogger().logError(cnse);
	}
	for (x = 0; x < CurrentArenaData.PLAYER_DIMS; x++) {
	    for (y = 0; y < CurrentArenaData.NUM_PLAYERS; y++) {
		for (z = 0; z < CurrentArenaData.NUM_ERAS; z++) {
		    final int curr = lt.startData.getCell(x, y, EraConstants.ERA_PRESENT);
		    lt.startData.setCell(curr, x, y, z);
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	return lt;
    }

    private static CurrentArenaData readTowerG7(final XMLFileReader reader, final int ver, final ProgressTracker pt)
	    throws IOException {
	int y, x, z, w, e, arenaSizeX, arenaSizeY, arenaSizeZ;
	arenaSizeX = reader.readInt();
	arenaSizeY = reader.readInt();
	arenaSizeZ = reader.readInt();
	final CurrentArenaData lt = new CurrentArenaData();
	lt.resize(arenaSizeZ, new Ground());
	for (e = 0; e < EraConstants.MAX_ERAS; e++) {
	    for (x = 0; x < arenaSizeX; x++) {
		for (y = 0; y < arenaSizeY; y++) {
		    for (z = 0; z < arenaSizeZ; z++) {
			for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			    final AbstractArenaObject obj = LTRemix.getApplication().getObjects()
				    .readArenaObjectG7(reader, ver);
			    lt.setCellEra(obj, y, x, z, w, e);
			}
		    }
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	for (x = 0; x < CurrentArenaData.PLAYER_DIMS; x++) {
	    for (y = 0; y < CurrentArenaData.NUM_PLAYERS; y++) {
		for (z = 0; z < CurrentArenaData.NUM_ERAS; z++) {
		    lt.startData.setCell(reader.readInt(), x, y, z);
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	lt.horizontalWraparoundEnabled = reader.readBoolean();
	lt.verticalWraparoundEnabled = reader.readBoolean();
	lt.thirdDimensionWraparoundEnabled = reader.readBoolean();
	lt.name = reader.readString();
	lt.hint = reader.readString();
	lt.author = reader.readString();
	lt.difficulty = reader.readInt();
	lt.moveShootAllowed = reader.readBoolean();
	lt.activeEra = reader.readInt();
	// Fill nulls
	lt.fillNulls(new Ground(), null, false);
	lt.fillVirtual();
	if (pt != null) {
	    pt.updateProgress();
	    pt.updateProgress();
	}
	return lt;
    }

    @Override
    public void writeSavedState(final XMLFileWriter writer, final ProgressTracker pt) throws IOException {
	int y, x, z, w;
	writer.writeInt(this.getColumns());
	writer.writeInt(this.getRows());
	writer.writeInt(this.getFloors());
	for (x = 0; x < this.getColumns(); x++) {
	    for (y = 0; y < this.getRows(); y++) {
		for (z = 0; z < this.getFloors(); z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			((AbstractArenaObject) this.pastSavedTowerState.getCell(y, x, z, w)).writeArenaObject(writer);
			((AbstractArenaObject) this.presentSavedTowerState.getCell(y, x, z, w))
				.writeArenaObject(writer);
			((AbstractArenaObject) this.futureSavedTowerState.getCell(y, x, z, w)).writeArenaObject(writer);
		    }
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	    pt.updateProgress();
	}
    }

    @Override
    public void readSavedState(final XMLFileReader reader, final int formatVersion, final ProgressTracker pt)
	    throws IOException {
	if (FormatConstants.isFormatVersionValidGeneration1(formatVersion)) {
	    this.readSavedStateG2(reader, formatVersion, pt);
	} else if (FormatConstants.isFormatVersionValidGeneration2(formatVersion)) {
	    this.readSavedStateG2(reader, formatVersion, pt);
	} else if (FormatConstants.isFormatVersionValidGeneration3(formatVersion)) {
	    this.readSavedStateG3(reader, formatVersion, pt);
	} else if (FormatConstants.isFormatVersionValidGeneration4(formatVersion)) {
	    this.readSavedStateG4(reader, formatVersion, pt);
	} else if (FormatConstants.isFormatVersionValidGeneration5(formatVersion)) {
	    this.readSavedStateG5(reader, formatVersion, pt);
	} else if (FormatConstants.isFormatVersionValidGeneration6(formatVersion)) {
	    this.readSavedStateG6(reader, formatVersion, pt);
	} else if (FormatConstants.isFormatVersionValidGeneration7(formatVersion)) {
	    this.readSavedStateG7(reader, formatVersion, pt);
	} else {
	    throw new IOException(StringLoader.loadString(StringConstants.ERROR_STRINGS_FILE,
		    StringConstants.ERROR_STRING_UNKNOWN_ARENA_FORMAT));
	}
    }

    private void readSavedStateG2(final XMLFileReader reader, final int formatVersion, final ProgressTracker pt)
	    throws IOException {
	int y, x, z, saveSizeX, saveSizeY, saveSizeZ;
	saveSizeX = reader.readInt();
	saveSizeY = reader.readInt();
	saveSizeZ = reader.readInt();
	this.presentSavedTowerState = new LowLevelArenaDataStore(saveSizeY, saveSizeX, saveSizeZ,
		ArenaConstants.NUM_LAYERS);
	for (x = 0; x < saveSizeX; x++) {
	    for (y = 0; y < saveSizeY; y++) {
		for (z = 0; z < saveSizeZ; z++) {
		    this.presentSavedTowerState.setCell(
			    LTRemix.getApplication().getObjects().readArenaObjectG2(reader, formatVersion), y, x, z,
			    ArenaConstants.LAYER_LOWER_GROUND);
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	if (saveSizeX != AbstractArenaData.MIN_COLUMNS || saveSizeY != AbstractArenaData.MIN_ROWS) {
	    this.resizeSavedState(saveSizeZ, new Ground());
	}
	// Backwards compatibility
	try {
	    this.pastSavedTowerState = (LowLevelArenaDataStore) this.presentSavedTowerState.clone();
	    this.futureSavedTowerState = (LowLevelArenaDataStore) this.presentSavedTowerState.clone();
	} catch (final CloneNotSupportedException cnse) {
	    LTRemix.getErrorLogger().logError(cnse);
	}
	if (pt != null) {
	    pt.updateProgress();
	}
    }

    private void readSavedStateG3(final XMLFileReader reader, final int formatVersion, final ProgressTracker pt)
	    throws IOException {
	int y, x, z, saveSizeX, saveSizeY, saveSizeZ;
	saveSizeX = reader.readInt();
	saveSizeY = reader.readInt();
	saveSizeZ = reader.readInt();
	this.presentSavedTowerState = new LowLevelArenaDataStore(saveSizeY, saveSizeX, saveSizeZ,
		ArenaConstants.NUM_LAYERS);
	for (x = 0; x < saveSizeX; x++) {
	    for (y = 0; y < saveSizeY; y++) {
		for (z = 0; z < saveSizeZ; z++) {
		    this.presentSavedTowerState.setCell(
			    LTRemix.getApplication().getObjects().readArenaObjectG3(reader, formatVersion), y, x, z,
			    ArenaConstants.LAYER_LOWER_GROUND);
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	if (saveSizeX != AbstractArenaData.MIN_COLUMNS || saveSizeY != AbstractArenaData.MIN_ROWS) {
	    this.resizeSavedState(saveSizeZ, new Ground());
	}
	// Backwards compatibility
	try {
	    this.pastSavedTowerState = (LowLevelArenaDataStore) this.presentSavedTowerState.clone();
	    this.futureSavedTowerState = (LowLevelArenaDataStore) this.presentSavedTowerState.clone();
	} catch (final CloneNotSupportedException cnse) {
	    LTRemix.getErrorLogger().logError(cnse);
	}
	if (pt != null) {
	    pt.updateProgress();
	}
    }

    private void readSavedStateG4(final XMLFileReader reader, final int formatVersion, final ProgressTracker pt)
	    throws IOException {
	int y, x, z, saveSizeX, saveSizeY, saveSizeZ;
	saveSizeX = reader.readInt();
	saveSizeY = reader.readInt();
	saveSizeZ = reader.readInt();
	this.presentSavedTowerState = new LowLevelArenaDataStore(saveSizeY, saveSizeX, saveSizeZ,
		ArenaConstants.NUM_LAYERS);
	for (x = 0; x < saveSizeX; x++) {
	    for (y = 0; y < saveSizeY; y++) {
		for (z = 0; z < saveSizeZ; z++) {
		    this.presentSavedTowerState.setCell(
			    LTRemix.getApplication().getObjects().readArenaObjectG4(reader, formatVersion), y, x, z,
			    ArenaConstants.LAYER_LOWER_GROUND);
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	if (saveSizeX != AbstractArenaData.MIN_COLUMNS || saveSizeY != AbstractArenaData.MIN_ROWS) {
	    this.resizeSavedState(saveSizeZ, new Ground());
	}
	// Backwards compatibility
	try {
	    this.pastSavedTowerState = (LowLevelArenaDataStore) this.presentSavedTowerState.clone();
	    this.futureSavedTowerState = (LowLevelArenaDataStore) this.presentSavedTowerState.clone();
	} catch (final CloneNotSupportedException cnse) {
	    LTRemix.getErrorLogger().logError(cnse);
	}
	if (pt != null) {
	    pt.updateProgress();
	}
    }

    private void readSavedStateG5(final XMLFileReader reader, final int formatVersion, final ProgressTracker pt)
	    throws IOException {
	int y, x, z, w, saveSizeX, saveSizeY, saveSizeZ;
	saveSizeX = reader.readInt();
	saveSizeY = reader.readInt();
	saveSizeZ = reader.readInt();
	this.presentSavedTowerState = new LowLevelArenaDataStore(saveSizeY, saveSizeX, saveSizeZ,
		ArenaConstants.NUM_LAYERS);
	for (x = 0; x < saveSizeX; x++) {
	    for (y = 0; y < saveSizeY; y++) {
		for (z = 0; z < saveSizeZ; z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			this.presentSavedTowerState.setCell(
				LTRemix.getApplication().getObjects().readArenaObjectG5(reader, formatVersion), y, x, z,
				w);
		    }
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	if (saveSizeX != AbstractArenaData.MIN_COLUMNS || saveSizeY != AbstractArenaData.MIN_ROWS) {
	    this.resizeSavedState(saveSizeZ, new Ground());
	}
	// Backwards compatibility
	try {
	    this.pastSavedTowerState = (LowLevelArenaDataStore) this.presentSavedTowerState.clone();
	    this.futureSavedTowerState = (LowLevelArenaDataStore) this.presentSavedTowerState.clone();
	} catch (final CloneNotSupportedException cnse) {
	    LTRemix.getErrorLogger().logError(cnse);
	}
	if (pt != null) {
	    pt.updateProgress();
	}
    }

    private void readSavedStateG6(final XMLFileReader reader, final int formatVersion, final ProgressTracker pt)
	    throws IOException {
	int y, x, z, w, saveSizeX, saveSizeY, saveSizeZ;
	saveSizeX = reader.readInt();
	saveSizeY = reader.readInt();
	saveSizeZ = reader.readInt();
	this.presentSavedTowerState = new LowLevelArenaDataStore(saveSizeY, saveSizeX, saveSizeZ,
		ArenaConstants.NUM_LAYERS);
	for (x = 0; x < saveSizeX; x++) {
	    for (y = 0; y < saveSizeY; y++) {
		for (z = 0; z < saveSizeZ; z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			this.presentSavedTowerState.setCell(
				LTRemix.getApplication().getObjects().readArenaObjectG6(reader, formatVersion), y, x, z,
				w);
		    }
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	if (saveSizeX != AbstractArenaData.MIN_COLUMNS || saveSizeY != AbstractArenaData.MIN_ROWS) {
	    this.resizeSavedState(saveSizeZ, new Ground());
	}
	// Backwards compatibility
	try {
	    this.pastSavedTowerState = (LowLevelArenaDataStore) this.presentSavedTowerState.clone();
	    this.futureSavedTowerState = (LowLevelArenaDataStore) this.presentSavedTowerState.clone();
	} catch (final CloneNotSupportedException cnse) {
	    LTRemix.getErrorLogger().logError(cnse);
	}
	if (pt != null) {
	    pt.updateProgress();
	}
    }

    private void readSavedStateG7(final XMLFileReader reader, final int formatVersion, final ProgressTracker pt)
	    throws IOException {
	int y, x, z, w, saveSizeX, saveSizeY, saveSizeZ;
	saveSizeX = reader.readInt();
	saveSizeY = reader.readInt();
	saveSizeZ = reader.readInt();
	this.presentSavedTowerState = new LowLevelArenaDataStore(saveSizeY, saveSizeX, saveSizeZ,
		ArenaConstants.NUM_LAYERS);
	for (x = 0; x < saveSizeX; x++) {
	    for (y = 0; y < saveSizeY; y++) {
		for (z = 0; z < saveSizeZ; z++) {
		    for (w = 0; w < ArenaConstants.NUM_LAYERS; w++) {
			this.pastSavedTowerState.setCell(
				LTRemix.getApplication().getObjects().readArenaObjectG7(reader, formatVersion), y, x, z,
				w);
			this.presentSavedTowerState.setCell(
				LTRemix.getApplication().getObjects().readArenaObjectG7(reader, formatVersion), y, x, z,
				w);
			this.futureSavedTowerState.setCell(
				LTRemix.getApplication().getObjects().readArenaObjectG7(reader, formatVersion), y, x, z,
				w);
		    }
		}
	    }
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	if (saveSizeX != AbstractArenaData.MIN_COLUMNS || saveSizeY != AbstractArenaData.MIN_ROWS) {
	    this.resizeSavedState(saveSizeZ, new Ground());
	}
	if (pt != null) {
	    pt.updateProgress();
	}
    }

    @Override
    public void undo() {
	this.iue.undo();
	this.pastEraData = this.iue.getPastImage();
	this.presentEraData = this.iue.getPresentImage();
	this.futureEraData = this.iue.getFutureImage();
	this.setAllDirtyFlags();
	this.clearVirtualGrid();
    }

    @Override
    public void redo() {
	this.iue.redo();
	this.pastEraData = this.iue.getPastImage();
	this.presentEraData = this.iue.getPresentImage();
	this.futureEraData = this.iue.getFutureImage();
	this.setAllDirtyFlags();
	this.clearVirtualGrid();
    }

    @Override
    public boolean tryUndo() {
	return this.iue.tryUndo();
    }

    @Override
    public boolean tryRedo() {
	return this.iue.tryRedo();
    }

    @Override
    public void clearUndoHistory() {
	this.iue.clearUndoHistory();
    }

    @Override
    public void clearRedoHistory() {
	this.iue.clearRedoHistory();
    }

    @Override
    public void updateUndoHistory(final HistoryStatus whatWas) {
	try {
	    this.iue.updateUndoHistory((LowLevelArenaDataStore) this.pastEraData.clone(),
		    (LowLevelArenaDataStore) this.presentEraData.clone(),
		    (LowLevelArenaDataStore) this.futureEraData.clone(), whatWas);
	} catch (final CloneNotSupportedException cnse) {
	    LTRemix.getErrorLogger().logError(cnse);
	}
    }

    @Override
    public void updateRedoHistory(final HistoryStatus whatWas) {
	try {
	    this.iue.updateRedoHistory((LowLevelArenaDataStore) this.pastEraData.clone(),
		    (LowLevelArenaDataStore) this.presentEraData.clone(),
		    (LowLevelArenaDataStore) this.futureEraData.clone(), whatWas);
	} catch (final CloneNotSupportedException cnse) {
	    LTRemix.getErrorLogger().logError(cnse);
	}
    }

    @Override
    public HistoryStatus getWhatWas() {
	return this.iue.getWhatWas();
    }

    @Override
    public void resetHistoryEngine() {
	this.iue = new ImageUndoEngine();
    }

    private class ImageUndoEngine {
	// Fields
	private HistoryStack undoHistory, redoHistory;
	private HistoryStatus whatWas;
	private LowLevelArenaDataStore pastImage, presentImage, futureImage;

	// Constructors
	public ImageUndoEngine() {
	    this.undoHistory = new HistoryStack();
	    this.redoHistory = new HistoryStack();
	    this.pastImage = null;
	    this.presentImage = null;
	    this.futureImage = null;
	    this.whatWas = null;
	}

	// Public methods
	public void undo() {
	    if (!this.undoHistory.isEmpty()) {
		final HistoryEntry entry = this.undoHistory.pop();
		this.pastImage = entry.getPastImage();
		this.presentImage = entry.getPresentImage();
		this.futureImage = entry.getFutureImage();
		this.whatWas = entry.getWhatWas();
	    } else {
		this.pastImage = null;
		this.presentImage = null;
		this.futureImage = null;
		this.whatWas = null;
	    }
	}

	public void redo() {
	    if (!this.redoHistory.isEmpty()) {
		final HistoryEntry entry = this.redoHistory.pop();
		this.pastImage = entry.getPastImage();
		this.presentImage = entry.getPresentImage();
		this.futureImage = entry.getFutureImage();
		this.whatWas = entry.getWhatWas();
	    } else {
		this.pastImage = null;
		this.presentImage = null;
		this.futureImage = null;
		this.whatWas = null;
	    }
	}

	public boolean tryUndo() {
	    return !this.undoHistory.isEmpty();
	}

	public boolean tryRedo() {
	    return !this.redoHistory.isEmpty();
	}

	public void clearUndoHistory() {
	    this.undoHistory = new HistoryStack();
	}

	public void clearRedoHistory() {
	    this.redoHistory = new HistoryStack();
	}

	public void updateUndoHistory(final LowLevelArenaDataStore newPastImage,
		final LowLevelArenaDataStore newPresentImage, final LowLevelArenaDataStore newFutureImage,
		final HistoryStatus newWhatWas) {
	    this.undoHistory.push(newPastImage, newPresentImage, newFutureImage, newWhatWas);
	}

	public void updateRedoHistory(final LowLevelArenaDataStore newPastImage,
		final LowLevelArenaDataStore newPresentImage, final LowLevelArenaDataStore newFutureImage,
		final HistoryStatus newWhatWas) {
	    this.redoHistory.push(newPastImage, newPresentImage, newFutureImage, newWhatWas);
	}

	public HistoryStatus getWhatWas() {
	    return this.whatWas;
	}

	public LowLevelArenaDataStore getPastImage() {
	    return this.pastImage;
	}

	public LowLevelArenaDataStore getPresentImage() {
	    return this.presentImage;
	}

	public LowLevelArenaDataStore getFutureImage() {
	    return this.futureImage;
	}

	// Inner classes
	private class HistoryEntry {
	    // Fields
	    private final LowLevelArenaDataStore histPastImage;
	    private final LowLevelArenaDataStore histPresentImage;
	    private final LowLevelArenaDataStore histFutureImage;
	    private final HistoryStatus histWhatWas;

	    HistoryEntry(final LowLevelArenaDataStore pastI, final LowLevelArenaDataStore presentI,
		    final LowLevelArenaDataStore futureI, final HistoryStatus hww) {
		this.histPastImage = pastI;
		this.histPresentImage = presentI;
		this.histFutureImage = futureI;
		this.histWhatWas = hww;
	    }

	    public LowLevelArenaDataStore getPastImage() {
		return this.histPastImage;
	    }

	    public LowLevelArenaDataStore getPresentImage() {
		return this.histPresentImage;
	    }

	    public LowLevelArenaDataStore getFutureImage() {
		return this.histFutureImage;
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

	    public void push(final LowLevelArenaDataStore pastI, final LowLevelArenaDataStore presentI,
		    final LowLevelArenaDataStore futureI, final HistoryStatus hww) {
		final HistoryEntry newEntry = new HistoryEntry(pastI, presentI, futureI, hww);
		this.stack.addFirst(newEntry);
	    }

	    public HistoryEntry pop() {
		return this.stack.removeFirst();
	    }
	}
    }
}
