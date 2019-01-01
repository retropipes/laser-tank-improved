/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.current;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.puttysoftware.lasertank.improved.fileio.FileUtilities;
import com.puttysoftware.lasertank.improved.fileio.XMLFileReader;
import com.puttysoftware.lasertank.improved.fileio.XMLFileWriter;
import com.puttysoftware.lasertank.improved.random.RandomLongRange;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.AbstractArena;
import com.puttysoftware.ltremix.arena.AbstractArenaData;
import com.puttysoftware.ltremix.arena.AbstractPrefixIO;
import com.puttysoftware.ltremix.arena.AbstractSuffixIO;
import com.puttysoftware.ltremix.arena.HistoryStatus;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractButton;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractButtonDoor;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractCharacter;
import com.puttysoftware.ltremix.prefs.PreferencesManager;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;
import com.puttysoftware.ltremix.utilities.DifficultyConstants;
import com.puttysoftware.ltremix.utilities.Extension;
import com.puttysoftware.ltremix.utilities.FormatConstants;
import com.puttysoftware.ltremix.utilities.ProgressTracker;

public class CurrentArena extends AbstractArena {
    // Properties
    private CurrentArenaData arenaData;
    private CurrentArenaData clipboard;
    private int levelCount;
    private int activeLevel;
    private String basePath;
    private AbstractPrefixIO prefixHandler;
    private AbstractSuffixIO suffixHandler;
    private boolean moveShootAllowed;
    private boolean eraChangeAllowed;
    private static final int PROGRESS_STAGES = 2;

    // Constructors
    public CurrentArena() throws IOException {
	super();
	this.arenaData = null;
	this.clipboard = null;
	this.levelCount = 0;
	this.activeLevel = 0;
	this.prefixHandler = null;
	this.suffixHandler = null;
	this.moveShootAllowed = false;
	this.eraChangeAllowed = false;
	final long random = new RandomLongRange(0, Long.MAX_VALUE).generate();
	final String randomID = Long.toHexString(random);
	this.basePath = System
		.getProperty(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			StringConstants.NOTL_STRING_TEMP_DIR))
		+ File.separator
		+ StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_PROGRAM_NAME)
		+ File.separator + randomID + StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			StringConstants.NOTL_STRING_ARENA_FORMAT_FOLDER);
	final File base = new File(this.basePath);
	final boolean res = base.mkdirs();
	if (!res) {
	    throw new IOException(
		    StringLoader.loadString(StringConstants.ERROR_STRINGS_FILE, StringConstants.ERROR_STRING_TEMP_DIR));
	}
    }

    // Methods
    public static int getProgressStages() {
	return CurrentArena.PROGRESS_STAGES + CurrentArenaData.getProgressStages();
    }

    @Override
    public boolean isEraChangeAllowed() {
	return this.eraChangeAllowed;
    }

    @Override
    public void setEraChangeAllowed(final boolean value) {
	this.eraChangeAllowed = value;
    }

    @Override
    public boolean isMoveShootAllowed() {
	return this.moveShootAllowed && this.arenaData.isMoveShootAllowed();
    }

    @Override
    public boolean isMoveShootAllowedInternal() {
	return this.arenaData.isMoveShootAllowed();
    }

    @Override
    public void setMoveShootAllowed(final boolean value) {
	this.arenaData.setMoveShootAllowed(value);
    }

    @Override
    public String getName() {
	return this.arenaData.getName();
    }

    @Override
    public void setName(final String newName) {
	this.arenaData.setName(newName);
    }

    @Override
    public String getHint() {
	return this.arenaData.getHint();
    }

    @Override
    public void setHint(final String newHint) {
	this.arenaData.setHint(newHint);
    }

    @Override
    public String getAuthor() {
	return this.arenaData.getAuthor();
    }

    @Override
    public void setAuthor(final String newAuthor) {
	this.arenaData.setAuthor(newAuthor);
    }

    @Override
    public int getDifficulty() {
	return this.arenaData.getDifficulty();
    }

    @Override
    public void setDifficulty(final int newDifficulty) {
	this.arenaData.setDifficulty(newDifficulty);
    }

    @Override
    public String getBasePath() {
	return this.basePath;
    }

    @Override
    public void setPrefixHandler(final AbstractPrefixIO xph) {
	this.prefixHandler = xph;
    }

    @Override
    public void setSuffixHandler(final AbstractSuffixIO xsh) {
	this.suffixHandler = xsh;
    }

    @Override
    public int getActiveLevelNumber() {
	return this.activeLevel;
    }

    @Override
    public String[] generateLevelInfoList(final ProgressTracker pt) {
	if (pt != null) {
	    pt.setMaximumDynamic(this.levelCount);
	}
	final int saveLevel = this.getActiveLevelNumber();
	final ArrayList<String> tempStorage = new ArrayList<>();
	for (int x = 0; x < this.levelCount; x++) {
	    this.switchLevel(x);
	    final StringBuilder sb = new StringBuilder();
	    sb.append(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
		    StringConstants.DIALOG_STRING_ARENA_LEVEL));
	    sb.append(StringConstants.COMMON_STRING_SPACE);
	    sb.append(this.getActiveLevelNumber() + 1);
	    sb.append(StringConstants.COMMON_STRING_COLON + StringConstants.COMMON_STRING_SPACE);
	    sb.append(this.getName().trim());
	    sb.append(StringConstants.COMMON_STRING_SPACE);
	    sb.append(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
		    StringConstants.DIALOG_STRING_ARENA_LEVEL_BY));
	    sb.append(StringConstants.COMMON_STRING_SPACE);
	    sb.append(this.getAuthor().trim());
	    sb.append(StringConstants.COMMON_STRING_SPACE);
	    sb.append(StringConstants.COMMON_STRING_OPEN_PARENTHESES);
	    sb.append(CurrentArena.convertDifficultyNumberToName(this.getDifficulty()));
	    sb.append(StringConstants.COMMON_STRING_CLOSE_PARENTHESES);
	    tempStorage.add(sb.toString());
	    if (pt != null) {
		pt.updateProgress();
	    }
	}
	this.switchLevel(saveLevel);
	return tempStorage.toArray(new String[tempStorage.size()]);
    }

    private static String convertDifficultyNumberToName(final int number) {
	return DifficultyConstants.getDifficultyNames()[number - 1];
    }

    @Override
    public void switchLevel(final int level) {
	this.switchLevelInternal(level);
    }

    @Override
    public void switchLevelOffset(final int level) {
	this.switchLevelInternal(this.activeLevel + level);
    }

    @Override
    protected void switchLevelInternal(final int level) {
	if (this.activeLevel != level || this.arenaData == null) {
	    if (this.arenaData != null) {
		try (XMLFileWriter writer = this.getLevelWriter()) {
		    // Save old level
		    this.writeArenaLevel(writer);
		    writer.close();
		} catch (final IOException io) {
		    // Ignore
		}
	    }
	    this.activeLevel = level;
	    try (XMLFileReader reader = this.getLevelReader()) {
		// Load new level
		this.readArenaLevel(reader);
		reader.close();
	    } catch (final IOException io) {
		// Ignore
	    }
	}
    }

    @Override
    public boolean doesLevelExist(final int level) {
	return level < this.levelCount && level >= 0;
    }

    @Override
    public boolean doesLevelExistOffset(final int level) {
	return this.activeLevel + level < this.levelCount && this.activeLevel + level >= 0;
    }

    @Override
    public void cutLevel() {
	if (this.levelCount > 1) {
	    this.clipboard = this.arenaData;
	    this.removeActiveLevel();
	}
    }

    @Override
    public void copyLevel() {
	this.clipboard = this.arenaData.clone();
    }

    @Override
    public void pasteLevel() {
	if (this.clipboard != null) {
	    this.arenaData = this.clipboard.clone();
	    LTRemix.getApplication().getArenaManager().setDirty(true);
	}
    }

    @Override
    public boolean isPasteBlocked() {
	return this.clipboard == null;
    }

    @Override
    public boolean isCutBlocked() {
	return this.levelCount <= 1;
    }

    @Override
    public boolean insertLevelFromClipboard() {
	if (this.levelCount < AbstractArena.MAX_LEVELS) {
	    this.arenaData = this.clipboard.clone();
	    this.levelCount++;
	    return true;
	} else {
	    return false;
	}
    }

    @Override
    public boolean addLevel() {
	if (this.levelCount < AbstractArena.MAX_LEVELS) {
	    if (this.arenaData != null) {
		try (XMLFileWriter writer = this.getLevelWriter()) {
		    // Save old level
		    this.writeArenaLevel(writer);
		    writer.close();
		} catch (final IOException io) {
		    // Ignore
		}
	    }
	    this.arenaData = new CurrentArenaData();
	    this.levelCount++;
	    this.activeLevel = this.levelCount - 1;
	    return true;
	} else {
	    return false;
	}
    }

    @Override
    protected boolean removeActiveLevel() {
	if (this.levelCount > 1) {
	    if (this.activeLevel >= 0 && this.activeLevel <= this.levelCount) {
		this.arenaData = null;
		// Delete file corresponding to current level
		final boolean res = this.getLevelFile(this.activeLevel).delete();
		if (!res) {
		    return false;
		}
		// Shift all higher-numbered levels down
		for (int x = this.activeLevel; x < this.levelCount - 1; x++) {
		    final File sourceLocation = this.getLevelFile(x + 1);
		    final File targetLocation = this.getLevelFile(x);
		    try {
			FileUtilities.moveFile(sourceLocation, targetLocation);
		    } catch (final IOException io) {
			// Ignore
		    }
		}
		this.levelCount--;
		return true;
	    } else {
		return false;
	    }
	} else {
	    return false;
	}
    }

    @Override
    public boolean isCellDirty(final int row, final int col, final int floor) {
	return this.arenaData.isCellDirty(row, col, floor);
    }

    @Override
    public AbstractArenaObject getCell(final int row, final int col, final int floor, final int layer) {
	return this.arenaData.getCell(row, col, floor, layer);
    }

    @Override
    public AbstractArenaObject getCellEra(final int row, final int col, final int floor, final int layer,
	    final int era) {
	return this.arenaData.getCellEra(row, col, floor, layer, era);
    }

    @Override
    public AbstractArenaObject getVirtualCell(final int row, final int col, final int floor, final int layer) {
	return this.arenaData.getVirtualCell(row, col, floor, layer);
    }

    @Override
    public int getStartRow() {
	return this.arenaData.getStartRow();
    }

    @Override
    public int getStartColumn() {
	return this.arenaData.getStartColumn();
    }

    @Override
    public int getStartFloor() {
	return this.arenaData.getStartFloor();
    }

    @Override
    public int getPlayerRow(final int playerNum, final int era) {
	return this.arenaData.getPlayerRow(playerNum, era);
    }

    @Override
    public int getPlayerColumn(final int playerNum, final int era) {
	return this.arenaData.getPlayerColumn(playerNum, era);
    }

    @Override
    public int getPlayerFloor(final int playerNum, final int era) {
	return this.arenaData.getPlayerFloor(playerNum, era);
    }

    @Override
    public int getRows() {
	return this.arenaData.getRows();
    }

    @Override
    public int getColumns() {
	return this.arenaData.getColumns();
    }

    @Override
    public int getFloors() {
	return this.arenaData.getFloors();
    }

    @Override
    public int getLevels() {
	return this.levelCount;
    }

    @Override
    public int getEra() {
	return this.arenaData.getEra();
    }

    @Override
    public void setEra(final int newEra) {
	this.arenaData.setEra(newEra);
    }

    @Override
    public boolean doesPlayerExist() {
	return this.arenaData.doesPlayerExist();
    }

    @Override
    public boolean findStart() {
	return this.arenaData.findStart();
    }

    @Override
    public void tickTimers(final int floor, final int actionType) {
	this.arenaData.tickTimers(floor, actionType);
    }

    @Override
    public void checkForEnemies(final int floor, final int ex, final int ey, final AbstractArenaObject e) {
	this.arenaData.checkForEnemies(floor, ex, ey, e);
    }

    @Override
    public int checkForMagnetic(final int floor, final int centerX, final int centerY, final int dir) {
	return this.arenaData.checkForMagnetic(floor, centerX, centerY, dir);
    }

    @Override
    public int[] tunnelScan(final int x, final int y, final int z, final String targetName) {
	return this.arenaData.tunnelScan(x, y, z, targetName);
    }

    @Override
    public void circularScanRange(final int x, final int y, final int z, final int maxR, final int rangeType,
	    final int forceUnits) {
	this.arenaData.circularScanRange(x, y, z, maxR, rangeType, forceUnits);
    }

    @Override
    public int[] findObject(final int z, final String targetName) {
	return this.arenaData.findObject(z, targetName);
    }

    @Override
    public boolean circularScanTank(final int x, final int y, final int z, final int maxR) {
	return this.arenaData.circularScanTank(x, y, z, maxR);
    }

    @Override
    public void fullScanActivateTanks() {
	this.arenaData.fullScanActivateTanks();
    }

    @Override
    public void fullScanProcessTanks(final AbstractCharacter activeTank) {
	this.arenaData.fullScanProcessTanks(activeTank);
    }

    @Override
    public void fullScanMoveObjects(final int locZ, final int dirX, final int dirY) {
	this.arenaData.fullScanMoveObjects(locZ, dirX, dirY);
    }

    @Override
    public void fullScanKillTanks() {
	this.arenaData.fullScanKillTanks();
    }

    @Override
    public void fullScanFreezeGround() {
	this.arenaData.fullScanFreezeGround();
    }

    @Override
    public void fullScanAllButtonOpen(final int z, final AbstractButton source) {
	this.arenaData.fullScanAllButtonOpen(z, source);
    }

    @Override
    public void fullScanAllButtonClose(final int z, final AbstractButton source) {
	this.arenaData.fullScanAllButtonClose(z, source);
    }

    @Override
    public void fullScanButtonBind(final int dx, final int dy, final int z, final AbstractButtonDoor source) {
	this.arenaData.fullScanButtonBind(dx, dy, z, source);
    }

    @Override
    public void fullScanButtonCleanup(final int px, final int py, final int z, final AbstractButton button) {
	this.arenaData.fullScanButtonCleanup(px, py, z, button);
    }

    @Override
    public void fullScanFindButtonLostDoor(final int z, final AbstractButtonDoor door) {
	this.arenaData.fullScanFindButtonLostDoor(z, door);
    }

    @Override
    public void setCell(final AbstractArenaObject mo, final int row, final int col, final int floor, final int layer) {
	this.arenaData.setCell(mo, row, col, floor, layer);
    }

    @Override
    public void setCellEra(final AbstractArenaObject mo, final int row, final int col, final int floor, final int layer,
	    final int era) {
	this.arenaData.setCellEra(mo, row, col, floor, layer, era);
    }

    @Override
    public void setVirtualCell(final AbstractArenaObject mo, final int row, final int col, final int floor,
	    final int layer) {
	this.arenaData.setVirtualCell(mo, row, col, floor, layer);
    }

    @Override
    public void markAsDirty(final int row, final int col, final int floor) {
	this.arenaData.markAsDirty(row, col, floor);
    }

    @Override
    public void markAsClean(final int row, final int col, final int floor) {
	this.arenaData.markAsClean(row, col, floor);
    }

    @Override
    public void clearDirtyFlags(final int floor) {
	this.arenaData.clearDirtyFlags(floor);
    }

    @Override
    public void setDirtyFlags(final int floor) {
	this.arenaData.setDirtyFlags(floor);
    }

    @Override
    public void clearVirtualGrid() {
	this.arenaData.clearVirtualGrid();
    }

    @Override
    public void setPlayerRow(final int newStartRow, final int playerNum, final int era) {
	this.arenaData.setPlayerRow(newStartRow, playerNum, era);
    }

    @Override
    public void setPlayerColumn(final int newStartColumn, final int playerNum, final int era) {
	this.arenaData.setPlayerColumn(newStartColumn, playerNum, era);
    }

    @Override
    public void setPlayerFloor(final int newStartFloor, final int playerNum, final int era) {
	this.arenaData.setPlayerFloor(newStartFloor, playerNum, era);
    }

    @Override
    public void fillDefault() {
	final AbstractArenaObject fill = PreferencesManager.getEditorDefaultFill();
	this.arenaData.fill(fill);
    }

    @Override
    public void save() {
	this.arenaData.save();
    }

    @Override
    public void restore() {
	this.arenaData.restore();
    }

    @Override
    public void resize(final int z, final AbstractArenaObject nullFill) {
	this.arenaData.resize(z, nullFill);
    }

    @Override
    public void setData(final AbstractArenaData newData, final int count) {
	if (newData instanceof CurrentArenaData) {
	    this.arenaData = (CurrentArenaData) newData;
	    this.levelCount = count;
	}
    }

    @Override
    public void saveStart() {
	this.arenaData.saveStart();
    }

    @Override
    public void restoreStart() {
	this.arenaData.restoreStart();
    }

    @Override
    public void enableHorizontalWraparound() {
	this.arenaData.enableHorizontalWraparound();
    }

    @Override
    public void disableHorizontalWraparound() {
	this.arenaData.disableHorizontalWraparound();
    }

    @Override
    public void enableVerticalWraparound() {
	this.arenaData.enableVerticalWraparound();
    }

    @Override
    public void disableVerticalWraparound() {
	this.arenaData.disableVerticalWraparound();
    }

    @Override
    public void enableThirdDimensionWraparound() {
	this.arenaData.enableThirdDimensionWraparound();
    }

    @Override
    public void disableThirdDimensionWraparound() {
	this.arenaData.disableThirdDimensionWraparound();
    }

    @Override
    public boolean isHorizontalWraparoundEnabled() {
	return this.arenaData.isHorizontalWraparoundEnabled();
    }

    @Override
    public boolean isVerticalWraparoundEnabled() {
	return this.arenaData.isVerticalWraparoundEnabled();
    }

    @Override
    public boolean isThirdDimensionWraparoundEnabled() {
	return this.arenaData.isThirdDimensionWraparoundEnabled();
    }

    @Override
    public CurrentArena readArena(final ProgressTracker pt) throws IOException {
	final CurrentArena m = new CurrentArena();
	// Attach handlers
	m.setPrefixHandler(this.prefixHandler);
	m.setSuffixHandler(this.suffixHandler);
	// Make base paths the same
	m.basePath = this.basePath;
	int version = -1;
	// Create metafile reader
	try (XMLFileReader metaReader = new XMLFileReader(
		m.basePath + File.separator
			+ StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
				StringConstants.NOTL_STRING_ARENA_FORMAT_METAFILE)
			+ Extension.getArenaLevelExtensionWithPeriod(),
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			StringConstants.NOTL_STRING_ARENA_FORMAT_ARENA))) {
	    // Read metafile
	    version = m.readArenaMetafileVersion(metaReader);
	    // Read metafile
	    if (FormatConstants.isFormatVersionValidGeneration4(version)
		    || FormatConstants.isFormatVersionValidGeneration5(version)
		    || FormatConstants.isFormatVersionValidGeneration6(version)
		    || FormatConstants.isFormatVersionValidGeneration7(version)) {
		m.readArenaMetafileG4(metaReader, version);
	    } else {
		m.readArenaMetafileG3(metaReader, version);
	    }
	} catch (final IOException ioe) {
	    throw ioe;
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	// Create data reader
	try (XMLFileReader dataReader = m.getLevelReader()) {
	    // Read data
	    m.readArenaLevelProgress(dataReader, version, pt);
	} catch (final IOException ioe) {
	    throw ioe;
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	return m;
    }

    private XMLFileReader getLevelReader() throws IOException {
	return new XMLFileReader(
		this.basePath + File.separator
			+ StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
				StringConstants.NOTL_STRING_ARENA_FORMAT_LEVEL)
			+ this.activeLevel + Extension.getArenaLevelExtensionWithPeriod(),
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			StringConstants.NOTL_STRING_ARENA_FORMAT_LEVEL));
    }

    private int readArenaMetafileVersion(final XMLFileReader reader) throws IOException {
	int ver = FormatConstants.ARENA_FORMAT_LATEST;
	if (this.prefixHandler != null) {
	    ver = this.prefixHandler.readPrefix(reader);
	}
	this.moveShootAllowed = FormatConstants.isMoveShootAllowed(ver);
	return ver;
    }

    private void readArenaMetafileG3(final XMLFileReader reader, final int ver) throws IOException {
	this.levelCount = reader.readInt();
	if (this.suffixHandler != null) {
	    this.suffixHandler.readSuffix(reader, ver);
	}
    }

    private void readArenaMetafileG4(final XMLFileReader reader, final int ver) throws IOException {
	this.levelCount = reader.readInt();
	reader.readString();
	if (this.suffixHandler != null) {
	    this.suffixHandler.readSuffix(reader, ver);
	}
    }

    private void readArenaLevel(final XMLFileReader reader) throws IOException {
	this.readArenaLevel(reader, FormatConstants.ARENA_FORMAT_LATEST);
    }

    private void readArenaLevel(final XMLFileReader reader, final int formatVersion) throws IOException {
	this.arenaData = (CurrentArenaData) new CurrentArenaData().readData(reader, formatVersion, null);
	this.arenaData.readSavedState(reader, formatVersion, null);
    }

    private void readArenaLevelProgress(final XMLFileReader reader, final int formatVersion, final ProgressTracker pt)
	    throws IOException {
	this.arenaData = (CurrentArenaData) new CurrentArenaData().readData(reader, formatVersion, pt);
	this.arenaData.readSavedState(reader, formatVersion, pt);
    }

    private File getLevelFile(final int level) {
	return new File(
		this.basePath + File.separator + level + StringConstants.COMMON_STRING_NOTL_PERIOD + StringLoader
			.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_ARENA_FORMAT_LEVEL));
    }

    @Override
    public void writeArena(final ProgressTracker pt) throws IOException {
	// Create metafile writer
	try (XMLFileWriter metaWriter = new XMLFileWriter(
		this.basePath + File.separator
			+ StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
				StringConstants.NOTL_STRING_ARENA_FORMAT_METAFILE)
			+ Extension.getArenaLevelExtensionWithPeriod(),
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			StringConstants.NOTL_STRING_ARENA_FORMAT_ARENA))) {
	    // Write metafile
	    this.writeArenaMetafile(metaWriter);
	} catch (final IOException ioe) {
	    throw ioe;
	}
	if (pt != null) {
	    pt.updateProgress();
	}
	// Create data writer
	try (XMLFileWriter dataWriter = this.getLevelWriter()) {
	    // Write data
	    this.writeArenaLevelProgress(dataWriter, pt);
	} catch (final IOException ioe) {
	    throw ioe;
	}
	if (pt != null) {
	    pt.updateProgress();
	}
    }

    private XMLFileWriter getLevelWriter() throws IOException {
	return new XMLFileWriter(
		this.basePath + File.separator
			+ StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
				StringConstants.NOTL_STRING_ARENA_FORMAT_LEVEL)
			+ this.activeLevel + Extension.getArenaLevelExtensionWithPeriod(),
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			StringConstants.NOTL_STRING_ARENA_FORMAT_LEVEL));
    }

    private void writeArenaMetafile(final XMLFileWriter writer) throws IOException {
	if (this.prefixHandler != null) {
	    this.prefixHandler.writePrefix(writer);
	}
	writer.writeInt(this.levelCount);
	writer.writeString(StringConstants.COMMON_STRING_EMPTY);
	if (this.suffixHandler != null) {
	    this.suffixHandler.writeSuffix(writer);
	}
    }

    private void writeArenaLevel(final XMLFileWriter writer) throws IOException {
	// Write the level
	this.arenaData.writeData(writer, null);
	this.arenaData.writeSavedState(writer, null);
    }

    private void writeArenaLevelProgress(final XMLFileWriter writer, final ProgressTracker pt) throws IOException {
	// Write the level
	this.arenaData.writeData(writer, pt);
	this.arenaData.writeSavedState(writer, pt);
    }

    @Override
    public void undo() {
	this.arenaData.undo();
    }

    @Override
    public void redo() {
	this.arenaData.redo();
    }

    @Override
    public boolean tryUndo() {
	return this.arenaData.tryUndo();
    }

    @Override
    public boolean tryRedo() {
	return this.arenaData.tryRedo();
    }

    @Override
    public void updateUndoHistory(final HistoryStatus whatIs) {
	this.arenaData.updateUndoHistory(whatIs);
    }

    @Override
    public void updateRedoHistory(final HistoryStatus whatIs) {
	this.arenaData.updateRedoHistory(whatIs);
    }

    @Override
    public HistoryStatus getWhatWas() {
	return this.arenaData.getWhatWas();
    }

    @Override
    public void resetHistoryEngine() {
	this.arenaData.resetHistoryEngine();
    }
}