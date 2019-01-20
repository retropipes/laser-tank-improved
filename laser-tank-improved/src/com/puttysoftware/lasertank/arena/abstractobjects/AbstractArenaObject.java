/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.abstractobjects;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.BitSet;

import com.puttysoftware.fileio.XMLFileReader;
import com.puttysoftware.fileio.XMLFileWriter;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.objects.Empty;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.strings.CommonString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.DirectionResolver;
import com.puttysoftware.lasertank.utilities.FrameResolver;
import com.puttysoftware.lasertank.utilities.LaserTypeConstants;
import com.puttysoftware.lasertank.utilities.MaterialConstants;
import com.puttysoftware.lasertank.utilities.RangeTypeConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;
import com.puttysoftware.storage.CloneableObject;

public abstract class AbstractArenaObject extends CloneableObject {
    static final int DEFAULT_CUSTOM_VALUE = 0;
    protected static final int CUSTOM_FORMAT_MANUAL_OVERRIDE = -1;
    private static final int PLASTIC_MINIMUM_REACTION_FORCE = 0;
    private static final int DEFAULT_MINIMUM_REACTION_FORCE = 1;
    private static final int METAL_MINIMUM_REACTION_FORCE = 2;

    public static final int getImbuedRangeForce(final int material) {
	if (material == MaterialConstants.MATERIAL_PLASTIC) {
	    return AbstractArenaObject.PLASTIC_MINIMUM_REACTION_FORCE;
	} else if (material == MaterialConstants.MATERIAL_METALLIC) {
	    return AbstractArenaObject.METAL_MINIMUM_REACTION_FORCE;
	} else {
	    return AbstractArenaObject.DEFAULT_MINIMUM_REACTION_FORCE;
	}
    }

    public static boolean hitReflectiveSide(final Direction dir) {
	Direction trigger1, trigger2;
	trigger1 = DirectionResolver.previous(dir);
	trigger2 = DirectionResolver.next(dir);
	return dir == trigger1 || dir == trigger2;
    }

    // Properties
    private boolean solid;
    private boolean pushable;
    private boolean friction;
    protected BitSet type;
    private int timerValue;
    private int initialTimerValue;
    private boolean timerActive;
    private int frameNumber;
    private Direction direction;
    private boolean diagonalOnly;
    private int color;
    private int material;
    private boolean imageEnabled;
    private AbstractArenaObject savedObject;
    private AbstractArenaObject previousState;

    public AbstractArenaObject() {
	this.solid = false;
	this.pushable = false;
	this.friction = true;
	this.type = new BitSet(TypeConstants.TYPES_COUNT);
	this.timerValue = 0;
	this.timerActive = false;
	this.frameNumber = 0;
	this.direction = Direction.NONE;
	this.diagonalOnly = false;
	this.color = -1;
	this.material = MaterialConstants.MATERIAL_DEFAULT;
	this.imageEnabled = true;
    }

    // Constructors
    AbstractArenaObject(final boolean isSolid) {
	this.solid = isSolid;
	this.pushable = false;
	this.friction = true;
	this.type = new BitSet(TypeConstants.TYPES_COUNT);
	this.timerValue = 0;
	this.initialTimerValue = 0;
	this.timerActive = false;
	this.frameNumber = 0;
	this.direction = Direction.NONE;
	this.diagonalOnly = false;
	this.color = -1;
	this.material = MaterialConstants.MATERIAL_DEFAULT;
	this.imageEnabled = true;
    }

    AbstractArenaObject(final boolean isSolid, final boolean isPushable, final boolean hasFriction) {
	this.solid = isSolid;
	this.pushable = isPushable;
	this.friction = hasFriction;
	this.type = new BitSet(TypeConstants.TYPES_COUNT);
	this.timerValue = 0;
	this.timerActive = false;
	this.frameNumber = 0;
	this.direction = Direction.NONE;
	this.diagonalOnly = false;
	this.color = -1;
	this.material = MaterialConstants.MATERIAL_DEFAULT;
	this.imageEnabled = true;
    }

    /**
     *
     * @param actionType
     * @return
     */
    public boolean acceptTick(final int actionType) {
	return true;
    }

    public final void activateTimer(final int ticks) {
	this.timerActive = true;
	this.timerValue = ticks;
	this.initialTimerValue = ticks;
    }

    public AbstractArenaObject attributeGameRenderHook() {
	return null;
    }

    public boolean canMove() {
	return false;
    }

    public boolean canShoot() {
	return false;
    }

    /**
     *
     * @param materialID
     * @return
     */
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	return this;
    }

    // Methods
    @Override
    public AbstractArenaObject clone() {
	try {
	    final AbstractArenaObject copy = this.getClass().getConstructor().newInstance();
	    copy.solid = this.solid;
	    copy.pushable = this.pushable;
	    copy.friction = this.friction;
	    copy.type = (BitSet) this.type.clone();
	    copy.timerValue = this.timerValue;
	    copy.initialTimerValue = this.initialTimerValue;
	    copy.timerActive = this.timerActive;
	    copy.frameNumber = this.frameNumber;
	    copy.direction = this.direction;
	    copy.diagonalOnly = this.diagonalOnly;
	    copy.color = this.color;
	    copy.material = this.material;
	    return copy;
	} catch (final InstantiationException e) {
	    LaserTank.logError(e);
	    return null;
	} catch (final IllegalAccessException e) {
	    LaserTank.logError(e);
	    return null;
	} catch (final IllegalArgumentException e) {
	    LaserTank.logError(e);
	    return null;
	} catch (final InvocationTargetException e) {
	    LaserTank.logError(e);
	    return null;
	} catch (final NoSuchMethodException e) {
	    LaserTank.logError(e);
	    return null;
	} catch (final SecurityException e) {
	    LaserTank.logError(e);
	    return null;
	}
    }

    public boolean defersSetProperties() {
	return false;
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     */
    public final void determineCurrentAppearance(final int x, final int y, final int z) {
	// Do nothing
    }

    public boolean doLasersPassThrough() {
	return true;
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     */
    public void editorPlaceHook(final int x, final int y, final int z) {
	// Do nothing
    }

    public AbstractArenaObject editorPropertiesHook() {
	if (this.hasDirection()) {
	    this.toggleDirection();
	    return this;
	} else {
	    return null;
	}
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     */
    public void editorRemoveHook(final int x, final int y, final int z) {
	// Do nothing
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof AbstractArenaObject)) {
	    return false;
	}
	final AbstractArenaObject other = (AbstractArenaObject) obj;
	if (this.friction != other.friction) {
	    return false;
	}
	if (this.initialTimerValue != other.initialTimerValue) {
	    return false;
	}
	if (this.pushable != other.pushable) {
	    return false;
	}
	if (this.solid != other.solid) {
	    return false;
	}
	if (this.type == null) {
	    if (other.type != null) {
		return false;
	    }
	} else if (!this.type.equals(other.type)) {
	    return false;
	}
	if (this.direction != other.direction) {
	    return false;
	}
	if (this.color != other.color) {
	    return false;
	}
	if (this.material != other.material) {
	    return false;
	}
	return true;
    }

    public final String getBaseImageName() {
	return GlobalLoader.loadImage(this.getStringBaseID());
    }

    public int getBlockHeight() {
	return 1;
    }

    public final int getColor() {
	return this.color;
    }

    public int getCustomFormat() {
	return 0;
    }

    abstract public int getCustomProperty(int propID);

    public String getCustomText() {
	return null;
    }

    public Color getCustomTextColor() {
	return null;
    }

    public final Direction getDirection() {
	return this.direction;
    }

    private final String getDirectionSuffix() {
	if (this.hasDirection()) {
	    return DirectionResolver.resolveConstantToImageSuffix(this.direction);
	} else {
	    return StringLoader.loadCommon(CommonString.EMPTY);
	}
    }

    public final int getFrameNumber() {
	return this.frameNumber;
    }

    private final String getFrameSuffix() {
	if (this.isAnimated()) {
	    return FrameResolver.resolveFrameNumberToImageSuffix(this.frameNumber);
	} else {
	    return StringLoader.loadCommon(CommonString.EMPTY);
	}
    }

    private final String getIdentifier() {
	return this.getBaseImageName();
    }

    public final String getImageName() {
	return this.getBaseImageName() + this.getDirectionSuffix() + this.getFrameSuffix();
    }

    abstract public int getLayer();

    public final int getMaterial() {
	return this.material;
    }

    public final int getMinimumReactionForce() {
	if (this.material == MaterialConstants.MATERIAL_PLASTIC) {
	    return AbstractArenaObject.PLASTIC_MINIMUM_REACTION_FORCE;
	} else if (this.material == MaterialConstants.MATERIAL_METALLIC) {
	    return AbstractArenaObject.METAL_MINIMUM_REACTION_FORCE;
	} else {
	    return AbstractArenaObject.DEFAULT_MINIMUM_REACTION_FORCE;
	}
    }

    public final AbstractArenaObject getPreviousState() {
	return this.previousState;
    }

    public final AbstractArenaObject getSavedObject() {
	return this.savedObject;
    }

    abstract public int getStringBaseID();

    private final boolean hasDirection() {
	return this.direction != Direction.INVALID && this.direction != Direction.NONE;
    }

    public final boolean hasFriction() {
	return this.friction;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (this.friction ? 1231 : 1237);
	result = prime * result + this.initialTimerValue;
	result = prime * result + (this.pushable ? 1231 : 1237);
	result = prime * result + (this.solid ? 1231 : 1237);
	result = prime * result + (this.timerActive ? 1231 : 1237);
	result = prime * result + this.timerValue;
	result = prime * result + (this.type == null ? 0 : this.type.hashCode());
	result = prime * result + this.direction.hashCode();
	result = prime * result + this.color;
	return prime * result + this.material;
    }

    public final boolean hasPreviousState() {
	return this.previousState != null;
    }

    private final boolean isAnimated() {
	return this.frameNumber > 0;
    }

    public boolean isConditionallySolid() {
	return this.solid;
    }

    public boolean isEnabled() {
	return this.imageEnabled;
    }

    public final boolean isOfType(final int testType) {
	return this.type.get(testType);
    }

    public final boolean isPushable() {
	return this.pushable;
    }

    public final boolean isSolid() {
	return this.solid;
    }

    public boolean killsOnMove() {
	return false;
    }

    public void laserDoneAction() {
	// Do nothing
    }

    /**
     *
     * @param locX
     * @param locY
     * @param locZ
     * @param dirX
     * @param dirY
     * @param laserType
     * @param forceUnits
     * @return
     */
    public Direction laserEnteredAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	if (this.isSolid()) {
	    if (forceUnits > this.getMinimumReactionForce() && this.canMove()) {
		try {
		    final AbstractArenaObject nextObj = LaserTank.getApplication().getArenaManager().getArena()
			    .getCell(locX + dirX, locY + dirY, locZ, this.getLayer());
		    final AbstractArenaObject nextObj2 = LaserTank.getApplication().getArenaManager().getArena()
			    .getCell(locX + dirX * 2, locY + dirY * 2, locZ, this.getLayer());
		    if (this instanceof AbstractMovableObject && nextObj != null
			    && nextObj instanceof AbstractMovableObject && nextObj.canMove()
			    && (nextObj2 != null && !nextObj2.isConditionallySolid() || forceUnits > 2)) {
			// Move BOTH this object and the one in front of it
			final AbstractMovableObject gmo = (AbstractMovableObject) this;
			final AbstractMovableObject gmo2 = (AbstractMovableObject) nextObj;
			LaserTank.getApplication().getGameManager().updatePushedPositionLater(locX, locY, dirX, dirY,
				gmo, locX + dirX, locY + dirY, gmo2, laserType,
				forceUnits - Math.max(1, this.getMinimumReactionForce()));
		    } else {
			// Object crushed by impact
			this.pushCrushAction(locX, locY, locZ);
		    }
		} catch (final ArrayIndexOutOfBoundsException aioob) {
		    // Object crushed by impact
		    this.pushCrushAction(locX, locY, locZ);
		}
	    } else {
		final AbstractArenaObject adj = LaserTank.getApplication().getArenaManager().getArena()
			.getCell(locX - dirX, locY - dirY, locZ, this.getLayer());
		if (adj != null && !adj.rangeAction(locX - 2 * dirX, locY - 2 * dirY, locZ, dirX, dirY,
			LaserTypeConstants.getRangeTypeForLaserType(laserType), 1)) {
		    SoundManager.playSound(SoundConstants.SOUND_LASER_DIE);
		}
	    }
	    return Direction.NONE;
	} else {
	    return DirectionResolver.resolveRelative(dirX, dirY);
	}
    }

    /**
     *
     * @param locX
     * @param locY
     * @param locZ
     * @param dirX
     * @param dirY
     * @param laserType
     * @return
     */
    public Direction laserExitedAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType) {
	return DirectionResolver.resolveRelative(dirX, dirY);
    }

    /**
     *
     * @param locX
     * @param locY
     * @param locZ
     */
    public void moveFailedAction(final int locX, final int locY, final int locZ) {
	SoundManager.playSound(SoundConstants.SOUND_BUMP_HEAD);
    }

    // Scripting
    public abstract void postMoveAction(final int dirX, final int dirY, int dirZ);

    /**
     *
     * @param pushed
     * @param x
     * @param y
     * @param z
     * @return
     */
    public void pushCollideAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	// Do nothing
    }

    protected void pushCrushAction(final int x, final int y, final int z) {
	// Object crushed
	SoundManager.playSound(SoundConstants.SOUND_CRUSH);
	LaserTank.getApplication().getGameManager().morph(new Empty(), x, y, z, this.getLayer());
    }

    /**
     *
     * @param pushed
     * @param x
     * @param y
     * @param z
     * @return
     */
    public boolean pushIntoAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	// Do nothing
	return true;
    }

    /**
     *
     * @param pushed
     * @param x
     * @param y
     * @param z
     */
    public void pushOutAction(final AbstractMovableObject pushed, final int x, final int y, final int z) {
	// Do nothing
    }

    /**
     *
     * @param locX
     * @param locY
     * @param locZ
     * @param dirX
     * @param dirY
     * @param rangeType
     * @param forceUnits
     * @return
     */
    public boolean rangeAction(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int rangeType, final int forceUnits) {
	if (RangeTypeConstants.getMaterialForRangeType(rangeType) == MaterialConstants.MATERIAL_FIRE
		&& this.getMaterial() == MaterialConstants.MATERIAL_WOODEN
		&& this.changesToOnExposure(MaterialConstants.MATERIAL_FIRE) != null) {
	    // Burn wooden object
	    SoundManager.playSound(SoundConstants.SOUND_WOOD_BURN);
	    LaserTank.getApplication().getGameManager().morph(this.changesToOnExposure(MaterialConstants.MATERIAL_FIRE),
		    locX + dirX, locY + dirY, locZ, this.getLayer());
	    return true;
	} else if (RangeTypeConstants.getMaterialForRangeType(rangeType) == MaterialConstants.MATERIAL_ICE
		&& (this.getMaterial() == MaterialConstants.MATERIAL_METALLIC
			|| this.getMaterial() == MaterialConstants.MATERIAL_WOODEN
			|| this.getMaterial() == MaterialConstants.MATERIAL_PLASTIC)
		&& this.changesToOnExposure(MaterialConstants.MATERIAL_ICE) != null) {
	    // Freeze metal, wooden, or plastic object
	    SoundManager.playSound(SoundConstants.SOUND_FROZEN);
	    LaserTank.getApplication().getGameManager().morph(this.changesToOnExposure(MaterialConstants.MATERIAL_ICE),
		    locX + dirX, locY + dirY, locZ, this.getLayer());
	    return true;
	} else if (RangeTypeConstants.getMaterialForRangeType(rangeType) == MaterialConstants.MATERIAL_FIRE
		&& this.getMaterial() == MaterialConstants.MATERIAL_ICE
		&& this.changesToOnExposure(MaterialConstants.MATERIAL_FIRE) != null) {
	    // Melt icy object
	    SoundManager.playSound(SoundConstants.SOUND_DEFROST);
	    LaserTank.getApplication().getGameManager().morph(this.changesToOnExposure(MaterialConstants.MATERIAL_FIRE),
		    locX + dirX, locY + dirY, locZ, this.getLayer());
	    return true;
	} else if (RangeTypeConstants.getMaterialForRangeType(rangeType) == MaterialConstants.MATERIAL_ICE
		&& this.getMaterial() == MaterialConstants.MATERIAL_FIRE
		&& this.changesToOnExposure(MaterialConstants.MATERIAL_ICE) != null) {
	    // Cool hot object
	    SoundManager.playSound(SoundConstants.SOUND_COOL_OFF);
	    LaserTank.getApplication().getGameManager().morph(this.changesToOnExposure(MaterialConstants.MATERIAL_ICE),
		    locX + dirX, locY + dirY, locZ, this.getLayer());
	    return true;
	} else if (RangeTypeConstants.getMaterialForRangeType(rangeType) == MaterialConstants.MATERIAL_FIRE
		&& this.getMaterial() == MaterialConstants.MATERIAL_METALLIC
		&& this.changesToOnExposure(MaterialConstants.MATERIAL_FIRE) != null) {
	    // Melt metal object
	    SoundManager.playSound(SoundConstants.SOUND_MELT);
	    LaserTank.getApplication().getGameManager().morph(this.changesToOnExposure(MaterialConstants.MATERIAL_FIRE),
		    locX + dirX, locY + dirY, locZ, this.getLayer());
	    return true;
	}
	return false;
    }

    public final AbstractArenaObject readArenaObjectG2(final XMLFileReader reader, final String ident, final int ver)
	    throws IOException {
	if (ident.equals(this.getIdentifier())) {
	    final int cc = this.getCustomFormat();
	    if (cc == AbstractArenaObject.CUSTOM_FORMAT_MANUAL_OVERRIDE) {
		this.direction = Direction.values()[reader.readInt()];
		reader.readInt();
		this.color = reader.readInt();
		return this.readArenaObjectHookG2(reader, ver);
	    } else {
		this.direction = Direction.values()[reader.readInt()];
		this.color = reader.readInt();
		for (int x = 0; x < cc; x++) {
		    final int cx = reader.readInt();
		    this.setCustomProperty(x + 1, cx);
		}
	    }
	    return this;
	} else {
	    return null;
	}
    }

    public final AbstractArenaObject readArenaObjectG3(final XMLFileReader reader, final String ident, final int ver)
	    throws IOException {
	if (ident.equals(this.getIdentifier())) {
	    final int cc = this.getCustomFormat();
	    if (cc == AbstractArenaObject.CUSTOM_FORMAT_MANUAL_OVERRIDE) {
		this.direction = Direction.values()[reader.readInt()];
		this.color = reader.readInt();
		// Discard material
		reader.readInt();
		return this.readArenaObjectHookG3(reader, ver);
	    } else {
		this.direction = Direction.values()[reader.readInt()];
		this.color = reader.readInt();
		// Discard material
		reader.readInt();
		for (int x = 0; x < cc; x++) {
		    final int cx = reader.readInt();
		    this.setCustomProperty(x + 1, cx);
		}
	    }
	    return this;
	} else {
	    return null;
	}
    }

    public final AbstractArenaObject readArenaObjectG4(final XMLFileReader reader, final String ident, final int ver)
	    throws IOException {
	if (ident.equals(this.getIdentifier())) {
	    final int cc = this.getCustomFormat();
	    if (cc == AbstractArenaObject.CUSTOM_FORMAT_MANUAL_OVERRIDE) {
		this.direction = Direction.values()[reader.readInt()];
		this.color = reader.readInt();
		return this.readArenaObjectHookG4(reader, ver);
	    } else {
		this.direction = Direction.values()[reader.readInt()];
		this.color = reader.readInt();
		for (int x = 0; x < cc; x++) {
		    final int cx = reader.readInt();
		    this.setCustomProperty(x + 1, cx);
		}
	    }
	    return this;
	} else {
	    return null;
	}
    }

    public final AbstractArenaObject readArenaObjectG5(final XMLFileReader reader, final String ident, final int ver)
	    throws IOException {
	if (ident.equals(this.getIdentifier())) {
	    final int cc = this.getCustomFormat();
	    if (cc == AbstractArenaObject.CUSTOM_FORMAT_MANUAL_OVERRIDE) {
		this.direction = Direction.values()[reader.readInt()];
		this.color = reader.readInt();
		return this.readArenaObjectHookG5(reader, ver);
	    } else {
		this.direction = Direction.values()[reader.readInt()];
		this.color = reader.readInt();
		for (int x = 0; x < cc; x++) {
		    final int cx = reader.readInt();
		    this.setCustomProperty(x + 1, cx);
		}
	    }
	    return this;
	} else {
	    return null;
	}
    }

    public final AbstractArenaObject readArenaObjectG6(final XMLFileReader reader, final String ident, final int ver)
	    throws IOException {
	if (ident.equals(this.getIdentifier())) {
	    final int cc = this.getCustomFormat();
	    if (cc == AbstractArenaObject.CUSTOM_FORMAT_MANUAL_OVERRIDE) {
		this.direction = Direction.values()[reader.readInt()];
		this.color = reader.readInt();
		return this.readArenaObjectHookG6(reader, ver);
	    } else {
		this.direction = Direction.values()[reader.readInt()];
		this.color = reader.readInt();
		for (int x = 0; x < cc; x++) {
		    final int cx = reader.readInt();
		    this.setCustomProperty(x + 1, cx);
		}
	    }
	    return this;
	} else {
	    return null;
	}
    }

    /**
     *
     * @param reader
     * @param formatVersion
     * @return
     * @throws IOException
     */
    protected AbstractArenaObject readArenaObjectHookG2(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	// Dummy implementation, subclasses can override
	return this;
    }

    /**
     *
     * @param reader
     * @param formatVersion
     * @return
     * @throws IOException
     */
    protected AbstractArenaObject readArenaObjectHookG3(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	// Dummy implementation, subclasses can override
	return this;
    }

    /**
     *
     * @param reader
     * @param formatVersion
     * @return
     * @throws IOException
     */
    protected AbstractArenaObject readArenaObjectHookG4(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	// Dummy implementation, subclasses can override
	return this;
    }

    /**
     *
     * @param reader
     * @param formatVersion
     * @return
     * @throws IOException
     */
    protected AbstractArenaObject readArenaObjectHookG5(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	// Dummy implementation, subclasses can override
	return this;
    }

    /**
     *
     * @param reader
     * @param formatVersion
     * @return
     * @throws IOException
     */
    protected AbstractArenaObject readArenaObjectHookG6(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	// Dummy implementation, subclasses can override
	return this;
    }

    public final void setColor(final int col) {
	this.color = col;
    }

    abstract public void setCustomProperty(int propID, int value);

    public final void setDiagonalOnly(final boolean value) {
	this.diagonalOnly = value;
    }

    public final void setDirection(final Direction dir) {
	this.direction = dir;
    }

    public void setEnabled(final boolean value) {
	this.imageEnabled = value;
    }

    public final void setFrameNumber(final int frame) {
	this.frameNumber = frame;
    }

    protected final void setMaterial(final int mat) {
	this.material = mat;
    }

    public final void setPreviousState(final AbstractArenaObject obj) {
	this.previousState = obj;
    }

    public final void setSavedObject(final AbstractArenaObject obj) {
	this.savedObject = obj;
    }

    public boolean solvesOnMove() {
	return false;
    }

    public final void tickTimer(final int dirX, final int dirY, final int actionType) {
	if (this.timerActive) {
	    if (this.acceptTick(actionType)) {
		this.timerValue--;
		if (this.timerValue == 0) {
		    this.timerActive = false;
		    this.initialTimerValue = 0;
		    this.timerExpiredAction(dirX, dirY);
		}
	    }
	}
    }

    /**
     *
     * @param dirX
     * @param dirY
     */
    public void timerExpiredAction(final int dirX, final int dirY) {
	// Do nothing
    }

    public final void toggleDirection() {
	this.direction = DirectionResolver.nextOrthogonal(this.direction);
    }

    public final void toggleFrameNumber() {
	if (this.isAnimated()) {
	    this.frameNumber++;
	    if (this.frameNumber > 3) {
		this.frameNumber = 1;
	    }
	}
    }

    public final void writeArenaObject(final XMLFileWriter writer) throws IOException {
	writer.writeString(this.getIdentifier());
	final int cc = this.getCustomFormat();
	if (cc == AbstractArenaObject.CUSTOM_FORMAT_MANUAL_OVERRIDE) {
	    writer.writeInt(this.direction.ordinal());
	    writer.writeInt(this.color);
	    this.writeArenaObjectHook(writer);
	} else {
	    writer.writeInt(this.direction.ordinal());
	    writer.writeInt(this.color);
	    for (int x = 0; x < cc; x++) {
		final int cx = this.getCustomProperty(x + 1);
		writer.writeInt(cx);
	    }
	}
    }

    /**
     *
     * @param writer
     * @throws IOException
     */
    protected void writeArenaObjectHook(final XMLFileWriter writer) throws IOException {
	// Do nothing - but let subclasses override
    }
}
