/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.current;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.puttysoftware.fileio.FileUtilities;
import com.puttysoftware.fileio.XMLFileReader;
import com.puttysoftware.fileio.XMLFileWriter;
import com.puttysoftware.lasertank.IDGenerator;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.arena.AbstractArenaData;
import com.puttysoftware.lasertank.arena.AbstractPrefixIO;
import com.puttysoftware.lasertank.arena.AbstractSuffixIO;
import com.puttysoftware.lasertank.arena.HistoryStatus;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractButton;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractButtonDoor;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractCharacter;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractTunnel;
import com.puttysoftware.lasertank.prefs.PreferencesManager;
import com.puttysoftware.lasertank.strings.CommonString;
import com.puttysoftware.lasertank.strings.DialogString;
import com.puttysoftware.lasertank.strings.ErrorString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.strings.global.UntranslatedString;
import com.puttysoftware.lasertank.utilities.DifficultyConstants;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.Extension;
import com.puttysoftware.lasertank.utilities.FormatConstants;
import com.puttysoftware.lasertank.utilities.InvalidArenaException;

public class CurrentArena extends AbstractArena {
    private static String convertDifficultyNumberToName(final int number) {
	return DifficultyConstants.getDifficultyNames()[number - 1];
    }

    public static int getStartLevel() {
	return 0;
    }

    // Properties
    private CurrentArenaData arenaData;
    private CurrentArenaData clipboard;
    private LevelInfo infoClipboard;
    private int levelCount;
    private int activeLevel;
    private int activeEra;
    private String basePath;
    private AbstractPrefixIO prefixHandler;
    private AbstractSuffixIO suffixHandler;
    private String musicFilename;
    private boolean moveShootAllowed;
    private final ArrayList<LevelInfo> levelInfoData;
    private ArrayList<String> levelInfoList;

    // Constructors
    public CurrentArena() throws IOException {
	super();
	this.arenaData = null;
	this.clipboard = null;
	this.levelCount = 0;
	this.activeLevel = 0;
	this.activeEra = 0;
	this.prefixHandler = null;
	this.suffixHandler = null;
	this.musicFilename = "null";
	this.moveShootAllowed = false;
	this.levelInfoData = new ArrayList<>();
	this.levelInfoList = new ArrayList<>();
	final String randomID = IDGenerator.getRandomIDString(16);
	this.basePath = System.getProperty(GlobalLoader.loadUntranslated(UntranslatedString.TEMP_DIR)) + File.separator
		+ GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME) + File.separator + randomID
		+ GlobalLoader.loadUntranslated(UntranslatedString.ARENA_FORMAT_FOLDER);
	final File base = new File(this.basePath);
	final boolean res = base.mkdirs();
	if (!res) {
	    throw new IOException(StringLoader.loadError(ErrorString.TEMP_DIR));
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
		} catch (final IOException ioe) {
		    throw new InvalidArenaException(ioe);
		}
	    }
	    // Add all eras for the new level
	    final int saveEra = this.activeEra;
	    this.arenaData = new CurrentArenaData();
	    for (int e = 0; e < AbstractArena.ERA_COUNT; e++) {
		this.switchEra(e);
		this.arenaData = new CurrentArenaData();
	    }
	    this.switchEra(saveEra);
	    // Clean up
	    this.levelCount++;
	    this.activeLevel = this.levelCount - 1;
	    this.levelInfoData.add(new LevelInfo());
	    this.levelInfoList.add(this.generateCurrentLevelInfo());
	    return true;
	} else {
	    return false;
	}
    }

    @Override
    public void checkForEnemies(final int floor, final int ex, final int ey, final AbstractCharacter e) {
	this.arenaData.checkForEnemies(this, floor, ex, ey, e);
    }

    @Override
    public int checkForMagnetic(final int floor, final int centerX, final int centerY, final Direction dir) {
	return this.arenaData.checkForMagnetic(this, floor, centerX, centerY, dir);
    }

    @Override
    public int[] circularScan(final int x, final int y, final int z, final int maxR, final String targetName,
	    final boolean moved) {
	return this.arenaData.circularScan(this, x, y, z, maxR, targetName, moved);
    }

    @Override
    public void circularScanRange(final int x, final int y, final int z, final int maxR, final int rangeType,
	    final int forceUnits) {
	this.arenaData.circularScanRange(this, x, y, z, maxR, rangeType, forceUnits);
    }

    @Override
    public boolean circularScanTank(final int x, final int y, final int z, final int maxR) {
	return this.arenaData.circularScanTank(this, x, y, z, maxR);
    }

    @Override
    public int[] circularScanTunnel(final int x, final int y, final int z, final int maxR, final int tx, final int ty,
	    final AbstractTunnel target, final boolean moved) {
	return this.arenaData.circularScanTunnel(this, x, y, z, maxR, tx, ty, target, moved);
    }

    @Override
    public void clearDirtyFlags(final int floor) {
	this.arenaData.clearDirtyFlags(floor);
    }

    @Override
    public void clearVirtualGrid() {
	this.arenaData.clearVirtualGrid(this);
    }

    @Override
    public void copyLevel() {
	this.clipboard = this.arenaData.clone();
	this.infoClipboard = this.levelInfoData.get(this.activeLevel).clone();
    }

    @Override
    public void cutLevel() {
	if (this.levelCount > 1) {
	    this.clipboard = this.arenaData;
	    this.infoClipboard = this.levelInfoData.get(this.activeLevel);
	    this.removeActiveLevel();
	}
    }

    @Override
    public void disableHorizontalWraparound() {
	this.levelInfoData.get(this.activeLevel).disableHorizontalWraparound();
    }

    @Override
    public void disableThirdDimensionWraparound() {
	this.levelInfoData.get(this.activeLevel).disableThirdDimensionWraparound();
    }

    @Override
    public void disableVerticalWraparound() {
	this.levelInfoData.get(this.activeLevel).disableVerticalWraparound();
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
    public boolean doesPlayerExist(final int pi) {
	return this.levelInfoData.get(this.activeLevel).doesPlayerExist(pi);
    }

    @Override
    public void enableHorizontalWraparound() {
	this.levelInfoData.get(this.activeLevel).enableHorizontalWraparound();
    }

    @Override
    public void enableThirdDimensionWraparound() {
	this.levelInfoData.get(this.activeLevel).enableThirdDimensionWraparound();
    }

    @Override
    public void enableVerticalWraparound() {
	this.levelInfoData.get(this.activeLevel).enableVerticalWraparound();
    }

    @Override
    public void fillDefault() {
	final AbstractArenaObject fill = PreferencesManager.getEditorDefaultFill();
	this.arenaData.fill(this, fill);
    }

    @Override
    public int[] findObject(final int z, final AbstractArenaObject target) {
	return this.arenaData.findObject(this, z, target);
    }

    @Override
    public int[] findPlayer(final int number) {
	return this.arenaData.findPlayer(this, number);
    }

    @Override
    public void fullScanAllButtonClose(final int z, final AbstractButton source) {
	this.arenaData.fullScanAllButtonClose(this, z, source);
    }

    @Override
    public void fullScanAllButtonOpen(final int z, final AbstractButton source) {
	this.arenaData.fullScanAllButtonOpen(this, z, source);
    }

    @Override
    public void fullScanButtonBind(final int dx, final int dy, final int z, final AbstractButtonDoor source) {
	this.arenaData.fullScanButtonBind(this, dx, dy, z, source);
    }

    @Override
    public void fullScanButtonCleanup(final int px, final int py, final int z, final AbstractButton button) {
	this.arenaData.fullScanButtonCleanup(this, px, py, z, button);
    }

    @Override
    public void fullScanFindButtonLostDoor(final int z, final AbstractButtonDoor door) {
	this.arenaData.fullScanFindButtonLostDoor(this, z, door);
    }

    @Override
    public void fullScanFreezeGround() {
	this.arenaData.fullScanFreezeGround(this);
    }

    @Override
    public void fullScanKillTanks() {
	this.arenaData.fullScanKillTanks(this);
    }

    private String generateCurrentLevelInfo() {
	final StringBuilder sb = new StringBuilder();
	sb.append(StringLoader.loadDialog(DialogString.ARENA_LEVEL));
	sb.append(StringLoader.loadCommon(CommonString.SPACE));
	sb.append(this.getActiveLevelNumber() + 1);
	sb.append(StringLoader.loadCommon(CommonString.COLON) + StringLoader.loadCommon(CommonString.SPACE));
	sb.append(this.getName().trim());
	sb.append(StringLoader.loadCommon(CommonString.SPACE));
	sb.append(StringLoader.loadDialog(DialogString.ARENA_LEVEL_BY));
	sb.append(StringLoader.loadCommon(CommonString.SPACE));
	sb.append(this.getAuthor().trim());
	sb.append(StringLoader.loadCommon(CommonString.SPACE));
	sb.append(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES));
	sb.append(CurrentArena.convertDifficultyNumberToName(this.getDifficulty()));
	sb.append(StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	return sb.toString();
    }

    @Override
    public void generateLevelInfoList() {
	final int saveLevel = this.getActiveLevelNumber();
	final ArrayList<String> tempStorage = new ArrayList<>();
	for (int x = 0; x < this.levelCount; x++) {
	    this.switchLevel(x);
	    tempStorage.add(this.generateCurrentLevelInfo());
	}
	this.switchLevel(saveLevel);
	this.levelInfoList = tempStorage;
    }

    @Override
    public int getActiveEraNumber() {
	return this.activeEra;
    }

    @Override
    public int getActiveLevelNumber() {
	return this.activeLevel;
    }

    // Methods
    @Override
    public String getArenaTempMusicFolder() {
	return this.basePath + File.separator + GlobalLoader.loadUntranslated(UntranslatedString.MUSIC_FOLDER)
		+ File.separator;
    }

    @Override
    public String getAuthor() {
	return this.levelInfoData.get(this.activeLevel).getAuthor();
    }

    @Override
    public String getBasePath() {
	return this.basePath;
    }

    @Override
    public AbstractArenaObject getCell(final int row, final int col, final int floor, final int layer) {
	return this.arenaData.getCell(this, row, col, floor, layer);
    }

    @Override
    public int getColumns() {
	return this.arenaData.getColumns();
    }

    @Override
    public int getDifficulty() {
	return this.levelInfoData.get(this.activeLevel).getDifficulty();
    }

    @Override
    public int getFloors() {
	return this.arenaData.getFloors();
    }

    @Override
    public String getHint() {
	return this.levelInfoData.get(this.activeLevel).getHint();
    }

    private File getLevelFile(final int level, final int era) {
	return new File(
		this.basePath + File.separator + GlobalLoader.loadUntranslated(UntranslatedString.ARENA_FORMAT_LEVEL)
			+ level + GlobalLoader.loadUntranslated(UntranslatedString.ARENA_FORMAT_ERA) + era
			+ Extension.getArenaLevelExtensionWithPeriod());
    }

    @Override
    public String[] getLevelInfoList() {
	return this.levelInfoList.toArray(new String[this.levelInfoList.size()]);
    }

    private XMLFileReader getLevelReaderG5() throws IOException {
	return new XMLFileReader(
		this.basePath + File.separator + GlobalLoader.loadUntranslated(UntranslatedString.ARENA_FORMAT_LEVEL)
			+ this.activeLevel + Extension.getArenaLevelExtensionWithPeriod(),
		GlobalLoader.loadUntranslated(UntranslatedString.ARENA_FORMAT_LEVEL));
    }

    private XMLFileReader getLevelReaderG6() throws IOException {
	return new XMLFileReader(
		this.basePath + File.separator + GlobalLoader.loadUntranslated(UntranslatedString.ARENA_FORMAT_LEVEL)
			+ this.activeLevel + GlobalLoader.loadUntranslated(UntranslatedString.ARENA_FORMAT_ERA)
			+ this.activeEra + Extension.getArenaLevelExtensionWithPeriod(),
		GlobalLoader.loadUntranslated(UntranslatedString.ARENA_FORMAT_LEVEL));
    }

    @Override
    public int getLevels() {
	return this.levelCount;
    }

    private XMLFileWriter getLevelWriter() throws IOException {
	return new XMLFileWriter(
		this.basePath + File.separator + GlobalLoader.loadUntranslated(UntranslatedString.ARENA_FORMAT_LEVEL)
			+ this.activeLevel + GlobalLoader.loadUntranslated(UntranslatedString.ARENA_FORMAT_ERA)
			+ this.activeEra + Extension.getArenaLevelExtensionWithPeriod(),
		GlobalLoader.loadUntranslated(UntranslatedString.ARENA_FORMAT_LEVEL));
    }

    @Override
    public String getMusicFilename() {
	return this.musicFilename;
    }

    @Override
    public String getName() {
	return this.levelInfoData.get(this.activeLevel).getName();
    }

    @Override
    public int getRows() {
	return this.arenaData.getRows();
    }

    @Override
    public int getStartColumn(final int pi) {
	return this.levelInfoData.get(this.activeLevel).getStartColumn(pi);
    }

    @Override
    public int getStartFloor(final int pi) {
	return this.levelInfoData.get(this.activeLevel).getStartFloor(pi);
    }

    @Override
    public int getStartRow(final int pi) {
	return this.levelInfoData.get(this.activeLevel).getStartRow(pi);
    }

    @Override
    public AbstractArenaObject getVirtualCell(final int row, final int col, final int floor, final int layer) {
	return this.arenaData.getVirtualCell(this, row, col, floor, layer);
    }

    @Override
    public HistoryStatus getWhatWas() {
	return this.arenaData.getWhatWas();
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
    public boolean isCellDirty(final int row, final int col, final int floor) {
	return this.arenaData.isCellDirty(this, row, col, floor);
    }

    @Override
    public boolean isCutBlocked() {
	return this.levelCount <= 1;
    }

    @Override
    public boolean isHorizontalWraparoundEnabled() {
	return this.levelInfoData.get(this.activeLevel).isHorizontalWraparoundEnabled();
    }

    @Override
    public boolean isMoveShootAllowed() {
	return this.isMoveShootAllowedGlobally() && this.isMoveShootAllowedThisLevel();
    }

    @Override
    public boolean isMoveShootAllowedGlobally() {
	return this.moveShootAllowed;
    }

    @Override
    public boolean isMoveShootAllowedThisLevel() {
	return this.levelInfoData.get(this.activeLevel).isMoveShootAllowed();
    }

    @Override
    public boolean isPasteBlocked() {
	return this.clipboard == null;
    }

    @Override
    public boolean isThirdDimensionWraparoundEnabled() {
	return this.levelInfoData.get(this.activeLevel).isThirdDimensionWraparoundEnabled();
    }

    @Override
    public boolean isVerticalWraparoundEnabled() {
	return this.levelInfoData.get(this.activeLevel).isVerticalWraparoundEnabled();
    }

    @Override
    public void markAsDirty(final int row, final int col, final int floor) {
	this.arenaData.markAsDirty(this, row, col, floor);
    }

    @Override
    public void pasteLevel() {
	if (this.clipboard != null) {
	    this.arenaData = this.clipboard.clone();
	    this.levelInfoData.set(this.activeLevel, this.infoClipboard.clone());
	    this.levelInfoList.set(this.activeLevel, this.generateCurrentLevelInfo());
	    LaserTank.getApplication().getArenaManager().setDirty(true);
	}
    }

    @Override
    public CurrentArena readArena() throws IOException {
	final CurrentArena m = new CurrentArena();
	// Attach handlers
	m.setPrefixHandler(this.prefixHandler);
	m.setSuffixHandler(this.suffixHandler);
	// Make base paths the same
	m.basePath = this.basePath;
	int version = -1;
	// Create metafile reader
	try (XMLFileReader metaReader = new XMLFileReader(
		m.basePath + File.separator + GlobalLoader.loadUntranslated(UntranslatedString.ARENA_FORMAT_METAFILE)
			+ Extension.getArenaLevelExtensionWithPeriod(),
		GlobalLoader.loadUntranslated(UntranslatedString.ARENA_FORMAT_ARENA))) {
	    // Read metafile
	    version = m.readArenaMetafileVersion(metaReader);
	    if (FormatConstants.isFormatVersionValidGeneration6(version)) {
		m.readArenaMetafileG6(metaReader, version);
	    } else if (FormatConstants.isFormatVersionValidGeneration4(version)
		    || FormatConstants.isFormatVersionValidGeneration5(version)) {
		m.readArenaMetafileG4(metaReader, version);
	    } else {
		m.readArenaMetafileG3(metaReader, version);
	    }
	} catch (final IOException ioe) {
	    throw new InvalidArenaException(ioe);
	}
	if (!FormatConstants.isLevelListStored(version)) {
	    // Create data reader
	    try (XMLFileReader dataReader = m.getLevelReaderG5()) {
		// Read data
		m.readArenaLevel(dataReader, version);
	    } catch (final IOException ioe) {
		throw new InvalidArenaException(ioe);
	    }
	    // Update level info
	    m.generateLevelInfoList();
	} else {
	    // Create data reader
	    try (XMLFileReader dataReader = m.getLevelReaderG6()) {
		// Read data
		m.readArenaLevel(dataReader, version);
	    } catch (final IOException ioe) {
		throw new InvalidArenaException(ioe);
	    }
	}
	return m;
    }

    private void readArenaLevel(final XMLFileReader reader) throws IOException {
	this.readArenaLevel(reader, FormatConstants.ARENA_FORMAT_LATEST);
    }

    private void readArenaLevel(final XMLFileReader reader, final int formatVersion) throws IOException {
	this.arenaData = (CurrentArenaData) new CurrentArenaData().readData(this, reader, formatVersion);
	this.arenaData.readSavedState(reader, formatVersion);
    }

    private void readArenaMetafileG3(final XMLFileReader reader, final int ver) throws IOException {
	this.levelCount = reader.readInt();
	this.musicFilename = "null";
	if (this.suffixHandler != null) {
	    this.suffixHandler.readSuffix(reader, ver);
	}
    }

    private void readArenaMetafileG4(final XMLFileReader reader, final int ver) throws IOException {
	this.levelCount = reader.readInt();
	this.musicFilename = reader.readString();
	if (this.suffixHandler != null) {
	    this.suffixHandler.readSuffix(reader, ver);
	}
    }

    private void readArenaMetafileG6(final XMLFileReader reader, final int ver) throws IOException {
	this.levelCount = reader.readInt();
	this.musicFilename = reader.readString();
	this.moveShootAllowed = reader.readBoolean();
	for (int l = 0; l < this.levelCount; l++) {
	    this.levelInfoData.add(LevelInfo.readLevelInfo(reader));
	    this.levelInfoList.add(reader.readString());
	}
	if (this.suffixHandler != null) {
	    this.suffixHandler.readSuffix(reader, ver);
	}
    }

    private int readArenaMetafileVersion(final XMLFileReader reader) throws IOException {
	int ver = FormatConstants.ARENA_FORMAT_LATEST;
	if (this.prefixHandler != null) {
	    ver = this.prefixHandler.readPrefix(reader);
	}
	this.moveShootAllowed = FormatConstants.isMoveShootAllowed(ver);
	return ver;
    }

    @Override
    public void redo() {
	this.arenaData.redo(this);
    }

    @Override
    protected boolean removeActiveLevel() {
	if (this.levelCount > 1) {
	    if (this.activeLevel >= 0 && this.activeLevel <= this.levelCount) {
		this.arenaData = null;
		// Delete all files corresponding to current level
		for (int e = 0; e < AbstractArena.ERA_COUNT; e++) {
		    final boolean res = this.getLevelFile(this.activeLevel, e).delete();
		    if (!res) {
			return false;
		    }
		}
		// Shift all higher-numbered levels down
		for (int x = this.activeLevel; x < this.levelCount - 1; x++) {
		    for (int e = 0; e < AbstractArena.ERA_COUNT; e++) {
			final File sourceLocation = this.getLevelFile(x + 1, e);
			final File targetLocation = this.getLevelFile(x, e);
			try {
			    FileUtilities.moveFile(sourceLocation, targetLocation);
			} catch (final IOException ioe) {
			    throw new InvalidArenaException(ioe);
			}
		    }
		}
		this.levelCount--;
		this.levelInfoData.remove(this.activeLevel);
		this.levelInfoList.remove(this.activeLevel);
		return true;
	    } else {
		return false;
	    }
	} else {
	    return false;
	}
    }

    @Override
    public void resetHistoryEngine() {
	this.arenaData.resetHistoryEngine();
    }

    @Override
    public void resize(final int z, final AbstractArenaObject nullFill) {
	this.arenaData.resize(this, z, nullFill);
    }

    @Override
    public void restore() {
	this.arenaData.restore(this);
    }

    @Override
    public void save() {
	this.arenaData.save(this);
    }

    @Override
    public void setAuthor(final String newAuthor) {
	this.levelInfoData.get(this.activeLevel).setAuthor(newAuthor);
	this.levelInfoList.set(this.activeLevel, this.generateCurrentLevelInfo());
    }

    @Override
    public void setCell(final AbstractArenaObject mo, final int row, final int col, final int floor, final int layer) {
	this.arenaData.setCell(this, mo, row, col, floor, layer);
    }

    @Override
    public void setData(final AbstractArenaData newData, final int count) {
	if (newData instanceof CurrentArenaData) {
	    this.arenaData = (CurrentArenaData) newData;
	    this.levelCount = count;
	}
    }

    @Override
    public void setDifficulty(final int newDifficulty) {
	this.levelInfoData.get(this.activeLevel).setDifficulty(newDifficulty);
	this.levelInfoList.set(this.activeLevel, this.generateCurrentLevelInfo());
    }

    @Override
    public void setDirtyFlags(final int floor) {
	this.arenaData.setDirtyFlags(floor);
    }

    @Override
    public void setHint(final String newHint) {
	this.levelInfoData.get(this.activeLevel).setHint(newHint);
    }

    @Override
    public void setMoveShootAllowedGlobally(final boolean value) {
	this.moveShootAllowed = value;
    }

    @Override
    public void setMoveShootAllowedThisLevel(final boolean value) {
	this.levelInfoData.get(this.activeLevel).setMoveShootAllowed(value);
    }

    @Override
    public void setMusicFilename(final String newMusicFilename) {
	this.musicFilename = newMusicFilename;
    }

    @Override
    public void setName(final String newName) {
	this.levelInfoData.get(this.activeLevel).setName(newName);
	this.levelInfoList.set(this.activeLevel, this.generateCurrentLevelInfo());
    }

    @Override
    public void setPrefixHandler(final AbstractPrefixIO xph) {
	this.prefixHandler = xph;
    }

    @Override
    public void setStartColumn(final int pi, final int newStartColumn) {
	this.levelInfoData.get(this.activeLevel).setStartColumn(pi, newStartColumn);
    }

    @Override
    public void setStartFloor(final int pi, final int newStartFloor) {
	this.levelInfoData.get(this.activeLevel).setStartFloor(pi, newStartFloor);
    }

    @Override
    public void setStartRow(final int pi, final int newStartRow) {
	this.levelInfoData.get(this.activeLevel).setStartRow(pi, newStartRow);
    }

    @Override
    public void setSuffixHandler(final AbstractSuffixIO xsh) {
	this.suffixHandler = xsh;
    }

    @Override
    public void setVirtualCell(final AbstractArenaObject mo, final int row, final int col, final int floor,
	    final int layer) {
	this.arenaData.setVirtualCell(this, mo, row, col, floor, layer);
    }

    @Override
    public void switchEra(final int era) {
	this.switchInternal(this.activeLevel, era);
    }

    @Override
    public void switchEraOffset(final int era) {
	this.switchInternal(this.activeLevel, this.activeEra + era);
    }

    @Override
    protected void switchInternal(final int level, final int era) {
	if (this.activeLevel != level || this.activeEra != era || this.arenaData == null) {
	    if (this.arenaData != null) {
		try (XMLFileWriter writer = this.getLevelWriter()) {
		    // Save old level
		    this.writeArenaLevel(writer);
		    writer.close();
		} catch (final IOException ioe) {
		    throw new InvalidArenaException(ioe);
		}
	    }
	    this.activeLevel = level;
	    this.activeEra = era;
	    try (XMLFileReader reader = this.getLevelReaderG6()) {
		// Load new level
		this.readArenaLevel(reader);
		reader.close();
	    } catch (final IOException ioe) {
		throw new InvalidArenaException(ioe);
	    }
	}
    }

    @Override
    public void switchLevel(final int level) {
	this.switchInternal(level, this.activeEra);
    }

    @Override
    public void switchLevelOffset(final int level) {
	this.switchInternal(this.activeLevel + level, this.activeEra);
    }

    @Override
    public void tickTimers(final int floor, final int actionType) {
	this.arenaData.tickTimers(this, floor, actionType);
    }

    @Override
    public boolean tryRedo() {
	return this.arenaData.tryRedo();
    }

    @Override
    public boolean tryUndo() {
	return this.arenaData.tryUndo();
    }

    @Override
    public void undo() {
	this.arenaData.undo(this);
    }

    @Override
    public void updateRedoHistory(final HistoryStatus whatIs) {
	this.arenaData.updateRedoHistory(whatIs);
    }

    @Override
    public void updateUndoHistory(final HistoryStatus whatIs) {
	this.arenaData.updateUndoHistory(whatIs);
    }

    @Override
    public void writeArena() throws IOException {
	// Create metafile writer
	try (XMLFileWriter metaWriter = new XMLFileWriter(
		this.basePath + File.separator + GlobalLoader.loadUntranslated(UntranslatedString.ARENA_FORMAT_METAFILE)
			+ Extension.getArenaLevelExtensionWithPeriod(),
		GlobalLoader.loadUntranslated(UntranslatedString.ARENA_FORMAT_ARENA))) {
	    // Write metafile
	    this.writeArenaMetafile(metaWriter);
	} catch (final IOException ioe) {
	    throw new InvalidArenaException(ioe);
	}
	// Create data writer
	try (XMLFileWriter dataWriter = this.getLevelWriter()) {
	    // Write data
	    this.writeArenaLevel(dataWriter);
	} catch (final IOException ioe) {
	    throw new InvalidArenaException(ioe);
	}
    }

    private void writeArenaLevel(final XMLFileWriter writer) throws IOException {
	// Write the level
	this.arenaData.writeData(this, writer);
	this.arenaData.writeSavedState(writer);
    }

    private void writeArenaMetafile(final XMLFileWriter writer) throws IOException {
	if (this.prefixHandler != null) {
	    this.prefixHandler.writePrefix(writer);
	}
	writer.writeInt(this.levelCount);
	writer.writeString(this.musicFilename);
	writer.writeBoolean(this.moveShootAllowed);
	for (int l = 0; l < this.levelCount; l++) {
	    this.levelInfoData.get(l).writeLevelInfo(writer);
	    writer.writeString(this.levelInfoList.get(l));
	}
	if (this.suffixHandler != null) {
	    this.suffixHandler.writeSuffix(writer);
	}
    }
}