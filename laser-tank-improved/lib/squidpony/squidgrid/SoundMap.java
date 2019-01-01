package squidpony.squidgrid;

import java.util.Map;
import java.util.Set;

import squidpony.squidmath.Coord;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.RNG;

/**
 * This class is used to determine when a sound is audible on a map and at what
 * positions. Created by Tommy Ettinger on 4/4/2015.
 */
public class SoundMap {
    /**
     * The type of heuristic to use. Note that EUCLIDEAN is not an option here
     * because it would only affect paths, and there is no path-finding
     * functionality in this class.
     */
    public enum Measurement {
	/**
	 * The distance it takes when only the four primary directions can be moved in.
	 * The default.
	 */
	MANHATTAN,
	/**
	 * The distance it takes when diagonal movement costs the same as cardinal
	 * movement.
	 */
	CHEBYSHEV
    }

    /**
     * This affects how sound travels on diagonal directions vs. orthogonal
     * directions. MANHATTAN should form a diamond shape on a featureless map, while
     * CHEBYSHEV will form a square.
     */
    public Measurement measurement = Measurement.MANHATTAN;
    /**
     * Stores which parts of the map are accessible and which are not. Should not be
     * changed unless the actual physical terrain has changed. You should call
     * initialize() with a new map instead of changing this directly.
     */
    public double[][] physicalMap;
    /**
     * The frequently-changing values that are often the point of using this class;
     * cells producing sound will have a value greater than 0, cells that cannot
     * possibly be reached by a sound will have a value of exactly 0, and walls will
     * have a value equal to the WALL constant (a negative number).
     */
    public double[][] gradientMap;
    /**
     * Height of the map. Exciting stuff. Don't change this, instead call
     * initialize().
     */
    public int height;
    /**
     * Width of the map. Exciting stuff. Don't change this, instead call
     * initialize().
     */
    public int width;
    /**
     * The latest results of findAlerted(), with Coord keys representing the
     * positions of creatures that were alerted and Double values representing how
     * loud the sound was when it reached them.
     */
    public OrderedMap<Coord, Double> alerted = new OrderedMap<>();
    /**
     * Cells with no sound are always marked with 0.
     */
    public static final double SILENT = 0.0;
    /**
     * Walls, which are solid no-entry cells, are marked with a significant negative
     * number equal to -999500.0 .
     */
    public static final double WALL = -999500.0;
    /**
     * Sources of sound on the map; keys are positions, values are how loud the
     * noise is (10.0 should spread 10 cells away, with diminishing values assigned
     * to further positions).
     */
    public OrderedMap<Coord, Double> sounds;
    private final OrderedMap<Coord, Double> fresh;
    /**
     * The RNG used to decide which one of multiple equally-short paths to take.
     */
    public RNG rng;
    private boolean initialized = false;

    /**
     * Construct a SoundMap without a level to actually scan. If you use this
     * constructor, you must call an initialize() method before using this class.
     */
    public SoundMap() {
	this.rng = new RNG(new LightRNG());
	this.alerted = new OrderedMap<>();
	this.fresh = new OrderedMap<>();
	this.sounds = new OrderedMap<>();
    }

    /**
     * Construct a SoundMap without a level to actually scan. This constructor
     * allows you to specify an RNG before it is used. If you use this constructor,
     * you must call an initialize() method before using this class.
     */
    public SoundMap(final RNG random) {
	this.rng = random;
	this.alerted = new OrderedMap<>();
	this.fresh = new OrderedMap<>();
	this.sounds = new OrderedMap<>();
    }

    /**
     * Used to construct a SoundMap from the output of another. Any sounds will need
     * to be assigned again.
     *
     * @param level
     */
    public SoundMap(final double[][] level) {
	this.rng = new RNG(new LightRNG());
	this.alerted = new OrderedMap<>();
	this.fresh = new OrderedMap<>();
	this.sounds = new OrderedMap<>();
	this.initialize(level);
    }

    /**
     * Used to construct a DijkstraMap from the output of another, specifying a
     * distance calculation.
     *
     * @param level
     * @param measurement
     */
    public SoundMap(final double[][] level, final Measurement measurement) {
	this.rng = new RNG(new LightRNG());
	this.measurement = measurement;
	this.alerted = new OrderedMap<>();
	this.fresh = new OrderedMap<>();
	this.sounds = new OrderedMap<>();
	this.initialize(level);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonBoneGen.generate(),
     * or any other char[][] where '#' means a wall and anything else is a walkable
     * tile. If you only have a map that uses box-drawing characters, use
     * DungeonUtility.linesToHashes() to get a map that can be used here.
     *
     * @param level
     */
    public SoundMap(final char[][] level) {
	this.rng = new RNG(new LightRNG());
	this.alerted = new OrderedMap<>();
	this.fresh = new OrderedMap<>();
	this.sounds = new OrderedMap<>();
	this.initialize(level);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonBoneGen.generate(),
     * or any other char[][] where one char means a wall and anything else is a
     * walkable tile. If you only have a map that uses box-drawing characters, use
     * DungeonUtility.linesToHashes() to get a map that can be used here. You can
     * specify the character used for walls.
     *
     * @param level
     */
    public SoundMap(final char[][] level, final char alternateWall) {
	this.rng = new RNG(new LightRNG());
	this.alerted = new OrderedMap<>();
	this.fresh = new OrderedMap<>();
	this.sounds = new OrderedMap<>();
	this.initialize(level, alternateWall);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonBoneGen.generate(),
     * or any other char[][] where '#' means a wall and anything else is a walkable
     * tile. If you only have a map that uses box-drawing characters, use
     * DungeonUtility.linesToHashes() to get a map that can be used here. This
     * constructor specifies a distance measurement.
     *
     * @param level
     * @param measurement
     */
    public SoundMap(final char[][] level, final Measurement measurement) {
	this.rng = new RNG(new LightRNG());
	this.measurement = measurement;
	this.alerted = new OrderedMap<>();
	this.fresh = new OrderedMap<>();
	this.sounds = new OrderedMap<>();
	this.initialize(level);
    }

    /**
     * Used to initialize or re-initialize a SoundMap that needs a new PhysicalMap
     * because it either wasn't given one when it was constructed, or because the
     * contents of the terrain have changed permanently.
     *
     * @param level
     * @return
     */
    public SoundMap initialize(final double[][] level) {
	this.width = level.length;
	this.height = level[0].length;
	this.gradientMap = new double[this.width][this.height];
	this.physicalMap = new double[this.width][this.height];
	for (int y = 0; y < this.height; y++) {
	    for (int x = 0; x < this.width; x++) {
		this.gradientMap[x][y] = level[x][y];
		this.physicalMap[x][y] = level[x][y];
	    }
	}
	this.initialized = true;
	return this;
    }

    /**
     * Used to initialize or re-initialize a SoundMap that needs a new PhysicalMap
     * because it either wasn't given one when it was constructed, or because the
     * contents of the terrain have changed permanently.
     *
     * @param level
     * @return
     */
    public SoundMap initialize(final char[][] level) {
	this.width = level.length;
	this.height = level[0].length;
	this.gradientMap = new double[this.width][this.height];
	this.physicalMap = new double[this.width][this.height];
	for (int y = 0; y < this.height; y++) {
	    for (int x = 0; x < this.width; x++) {
		final double t = level[x][y] == '#' ? SoundMap.WALL : SoundMap.SILENT;
		this.gradientMap[x][y] = t;
		this.physicalMap[x][y] = t;
	    }
	}
	this.initialized = true;
	return this;
    }

    /**
     * Used to initialize or re-initialize a SoundMap that needs a new PhysicalMap
     * because it either wasn't given one when it was constructed, or because the
     * contents of the terrain have changed permanently. This initialize() method
     * allows you to specify an alternate wall char other than the default
     * character, '#' .
     *
     * @param level
     * @param alternateWall
     * @return
     */
    public SoundMap initialize(final char[][] level, final char alternateWall) {
	this.width = level.length;
	this.height = level[0].length;
	this.gradientMap = new double[this.width][this.height];
	this.physicalMap = new double[this.width][this.height];
	for (int y = 0; y < this.height; y++) {
	    for (int x = 0; x < this.width; x++) {
		final double t = level[x][y] == alternateWall ? SoundMap.WALL : SoundMap.SILENT;
		this.gradientMap[x][y] = t;
		this.physicalMap[x][y] = t;
	    }
	}
	this.initialized = true;
	return this;
    }

    /**
     * Resets the gradientMap to its original value from physicalMap. Does not
     * remove sounds (they will still affect scan() normally).
     */
    public void resetMap() {
	if (!this.initialized) {
	    return;
	}
	for (int y = 0; y < this.height; y++) {
	    for (int x = 0; x < this.width; x++) {
		this.gradientMap[x][y] = this.physicalMap[x][y];
	    }
	}
    }

    /**
     * Resets this SoundMap to a state with no sounds, no alerted creatures, and no
     * changes made to gradientMap relative to physicalMap.
     */
    public void reset() {
	this.resetMap();
	this.alerted.clear();
	this.fresh.clear();
	this.sounds.clear();
    }

    /**
     * Marks a cell as producing a sound with the given loudness; this can be placed
     * on a wall or unreachable area, but that may cause the sound to be
     * un-hear-able. A sound emanating from a cell on one side of a 2-cell-thick
     * wall will only radiate sound on one side, which can be used for certain
     * effects. A sound emanating from a cell in a 1-cell-thick wall will radiate on
     * both sides.
     *
     * @param x
     * @param y
     * @param loudness The number of cells the sound should spread away using the
     *                 current measurement.
     */
    public void setSound(final int x, final int y, final double loudness) {
	if (!this.initialized) {
	    return;
	}
	final Coord pt = Coord.get(x, y);
	if (this.sounds.containsKey(pt) && this.sounds.get(pt) >= loudness) {
	    return;
	}
	this.sounds.put(pt, loudness);
    }

    /**
     * Marks a cell as producing a sound with the given loudness; this can be placed
     * on a wall or unreachable area, but that may cause the sound to be
     * un-hear-able. A sound emanating from a cell on one side of a 2-cell-thick
     * wall will only radiate sound on one side, which can be used for certain
     * effects. A sound emanating from a cell in a 1-cell-thick wall will radiate on
     * both sides.
     *
     * @param pt
     * @param loudness The number of cells the sound should spread away using the
     *                 current measurement.
     */
    public void setSound(final Coord pt, final double loudness) {
	if (!this.initialized) {
	    return;
	}
	if (this.sounds.containsKey(pt) && this.sounds.get(pt) >= loudness) {
	    return;
	}
	this.sounds.put(pt, loudness);
    }

    /**
     * If a sound is being produced at a given (x, y) location, this removes it.
     *
     * @param x
     * @param y
     */
    public void removeSound(final int x, final int y) {
	if (!this.initialized) {
	    return;
	}
	final Coord pt = Coord.get(x, y);
	if (this.sounds.containsKey(pt)) {
	    this.sounds.remove(pt);
	}
    }

    /**
     * If a sound is being produced at a given location (a Coord), this removes it.
     *
     * @param pt
     */
    public void removeSound(final Coord pt) {
	if (!this.initialized) {
	    return;
	}
	if (this.sounds.containsKey(pt)) {
	    this.sounds.remove(pt);
	}
    }

    /**
     * Marks a specific cell in gradientMap as a wall, which makes sounds
     * potentially unable to pass through it.
     *
     * @param x
     * @param y
     */
    public void setOccupied(final int x, final int y) {
	if (!this.initialized) {
	    return;
	}
	this.gradientMap[x][y] = SoundMap.WALL;
    }

    /**
     * Reverts a cell to the value stored in the original state of the level as
     * known by physicalMap.
     *
     * @param x
     * @param y
     */
    public void resetCell(final int x, final int y) {
	if (!this.initialized) {
	    return;
	}
	this.gradientMap[x][y] = this.physicalMap[x][y];
    }

    /**
     * Reverts a cell to the value stored in the original state of the level as
     * known by physicalMap.
     *
     * @param pt
     */
    public void resetCell(final Coord pt) {
	if (!this.initialized) {
	    return;
	}
	this.gradientMap[pt.x][pt.y] = this.physicalMap[pt.x][pt.y];
    }

    /**
     * Used to remove all sounds.
     */
    public void clearSounds() {
	if (!this.initialized) {
	    return;
	}
	this.sounds.clear();
    }

    protected void setFresh(final int x, final int y, final double counter) {
	if (!this.initialized) {
	    return;
	}
	this.gradientMap[x][y] = counter;
	this.fresh.put(Coord.get(x, y), counter);
    }

    protected void setFresh(final Coord pt, final double counter) {
	if (!this.initialized) {
	    return;
	}
	this.gradientMap[pt.x][pt.y] = counter;
	this.fresh.put(Coord.get(pt.x, pt.y), counter);
    }

    /**
     * Recalculate the sound map and return it. Cells that were marked as goals with
     * setSound will have a value greater than 0 (higher numbers are louder sounds),
     * the cells adjacent to sounds will have a value 1 less than the loudest
     * adjacent cell, and cells progressively further from sounds will have a value
     * equal to the loudness of the nearest sound minus the distance from it, to a
     * minimum of 0. The exceptions are walls, which will have a value defined by
     * the WALL constant in this class. Like sound itself, the sound map allows some
     * passage through walls; specifically, 1 cell thick of wall can be passed
     * through, with reduced loudness, before the fill cannot go further. This uses
     * the current measurement.
     *
     * @return A 2D double[width][height] using the width and height of what this
     *         knows about the physical map.
     */
    public double[][] scan() {
	if (!this.initialized) {
	    return null;
	}
	for (final Map.Entry<Coord, Double> entry : this.sounds.entrySet()) {
	    this.gradientMap[entry.getKey().x][entry.getKey().y] = entry.getValue();
	    if (this.fresh.containsKey(entry.getKey()) && this.fresh.get(entry.getKey()) > entry.getValue()) {
	    } else {
		this.fresh.put(entry.getKey(), entry.getValue());
	    }
	}
	int numAssigned = this.fresh.size();
	final Direction[] dirs = this.measurement == Measurement.MANHATTAN ? Direction.CARDINALS : Direction.OUTWARDS;
	while (numAssigned > 0) {
	    numAssigned = 0;
	    final OrderedMap<Coord, Double> fresh2 = new OrderedMap<>(this.fresh.size());
	    fresh2.putAll(this.fresh);
	    this.fresh.clear();
	    for (final Map.Entry<Coord, Double> cell : fresh2.entrySet()) {
		if (cell.getValue() <= 1) {
		    continue;
		}
		for (final Direction dir : dirs) {
		    final Coord adj = cell.getKey().translate(dir.deltaX, dir.deltaY);
		    if (adj.x < 0 || adj.x >= this.width || adj.y < 0 || adj.y >= this.height) {
			continue;
		    }
		    if (this.physicalMap[cell.getKey().x][cell.getKey().y] == SoundMap.WALL
			    && this.physicalMap[adj.x][adj.y] == SoundMap.WALL) {
			continue;
		    }
		    if (this.gradientMap[cell.getKey().x][cell.getKey().y] > this.gradientMap[adj.x][adj.y] + 1) {
			final double v = cell.getValue() - 1
				- (this.physicalMap[adj.x][adj.y] == SoundMap.WALL ? 1 : 0);
			if (v > 0) {
			    this.gradientMap[adj.x][adj.y] = v;
			    this.fresh.put(Coord.get(adj.x, adj.y), v);
			    ++numAssigned;
			}
		    }
		}
	    }
	}
	for (int y = 0; y < this.height; y++) {
	    for (int x = 0; x < this.width; x++) {
		if (this.physicalMap[x][y] == SoundMap.WALL) {
		    this.gradientMap[x][y] = SoundMap.WALL;
		}
	    }
	}
	return this.gradientMap;
    }

    /**
     * Scans the dungeon using SoundMap.scan, adding any positions in extraSounds to
     * the group of known sounds before scanning. The creatures passed to this
     * function as a Set of Points will have the loudness of all sounds at their
     * position put as the value in alerted corresponding to their Coord position.
     *
     * @param creatures
     * @param extraSounds
     * @return
     */
    public OrderedMap<Coord, Double> findAlerted(final Set<Coord> creatures, final Map<Coord, Double> extraSounds) {
	if (!this.initialized) {
	    return null;
	}
	this.alerted = new OrderedMap<>(creatures.size());
	this.resetMap();
	for (final Map.Entry<Coord, Double> sound : extraSounds.entrySet()) {
	    this.setSound(sound.getKey(), sound.getValue());
	}
	this.scan();
	for (final Coord critter : creatures) {
	    if (critter.x < 0 || critter.x >= this.width || critter.y < 0 || critter.y >= this.height) {
		continue;
	    }
	    this.alerted.put(Coord.get(critter.x, critter.y), this.gradientMap[critter.x][critter.y]);
	}
	return this.alerted;
    }
}
