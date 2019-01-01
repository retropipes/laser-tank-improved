package squidpony.squidgrid.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

import squidpony.ArrayTools;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.mapping.styled.DungeonBoneGen;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.PoissonDisk;
import squidpony.squidmath.RNG;
import squidpony.squidmath.StatefulRNG;

/**
 * A good way to create a more-complete dungeon, layering different effects and
 * modifications on top of a dungeon produced by DungeonBoneGen or another
 * dungeon without such effects. Unlike DungeonGenerator, this class uses
 * environment information for the dungeons it is given (or quickly generates
 * such information if using DungeonBoneGen), and uses that information to only
 * place effects like grass or water where you specify, like "only in caves", or
 * "doors should never be in caves". Ensures only connected regions of the map
 * are used by filling unreachable areas with walls, and can find far-apart
 * staircase positions if generate() is used or can keep existing staircases in
 * a map if generateRespectingStairs() is used. <br>
 * The main technique for using this is simple: Construct a DungeonGenerator,
 * usually with the desired width and height, then call any feature adding
 * methods that you want in the dungeon, like addWater(), addTraps, addGrass(),
 * or addDoors(). All of these methods except addDoors() take an int argument
 * that corresponds to a constant in this class, CAVE, CORRIDOR, or ROOM, or
 * ALL, and they will only cause the requested feature to show up in that
 * environment. Some of these take different parameters, like addDoors() which
 * needs to know if it should check openings that are two cells wide to add a
 * door and a wall to, or whether it should only add doors to single-cell
 * openings. In the case of addDoors(), it doesn't take an environment argument
 * since doors almost always are between environments (rooms and corridors), so
 * placing them only within one or the other doesn't make sense. This class,
 * unlike the normal DungeonGenerator, also has an addLake() method, which, like
 * addDoors(), doesn't take an environment parameter. It can be used to turn a
 * large section of what would otherwise be walls into a lake (of some character
 * for deep lake cells and some character for shallow lake cells), and corridors
 * that cross the lake become bridges, shown as ':'. It should be noted that
 * because the lake fills walls, it doesn't change the connectivity of the map
 * unless you can cross the lake. There's also addMaze(), which does change the
 * connectivity by replacing sections of impassable walls with twisty, maze-like
 * passages. <br>
 * Once you've added any features to the generator's effects list, call
 * generate() to get a char[][] with the desired dungeon map, using a fixed
 * repertoire of chars to represent the different features, with the exception
 * of the customization that can be requested from addLake(). If you use the
 * libGDX text-based display module, you can change what chars are shown by
 * using addSwap() in TextCellFactory. After calling generate(), you can safely
 * get the values from the stairsUp and stairsDown fields, which are Coords that
 * should be a long distance from each other but connected in the dungeon. You
 * may want to change those to staircase characters, but there's no requirement
 * to do anything with them. It's recommended that you keep the resulting
 * char[][] maps in some collection that can be saved, since
 * SectionDungeonGenerator only stores a temporary copy of the most
 * recently-generated map. The DungeonUtility field of this class, utility, is a
 * convenient way of accessing the non-static methods in that class, such as
 * randomFloor(), without needing to create another DungeonUtility (this class
 * creates one, so you don't have to). Similarly, the Placement field of this
 * class, placement, can be used to find parts of a dungeon that fit certain
 * qualities for the placement of items, terrain features, or NPCs. <br>
 * Example map with a custom-representation lake:
 * https://gist.github.com/tommyettinger/0055075f9de59c452d25
 *
 * @see DungeonUtility this class exposes a DungeonUtility member;
 *      DungeonUtility also has many useful static methods
 * @see DungeonGenerator for a slightly simpler alternative that does not
 *      recognize different sections of dungeon
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
public class SectionDungeonGenerator implements IDungeonGenerator {
    /**
     * The effects that can be applied to this dungeon. More may be added in future
     * releases.
     */
    public enum FillEffect {
	/**
	 * Water, represented by '~'
	 */
	WATER,
	/**
	 * Traps, represented by '^'
	 */
	TRAPS,
	/**
	 * Grass, represented by '"'
	 */
	GRASS,
	/**
	 * Boulders strewn about open areas, represented by '#' and treated as walls
	 */
	BOULDERS,
	/**
	 * Islands of ground, '.', surrounded by shallow water, ',', to place in water
	 * at evenly spaced points
	 */
	ISLANDS
    }

    /**
     * Constant for features being added to all environment types.
     */
    public static final int ALL = 0,
	    /**
	     * Constant for features being added only to rooms.
	     */
	    ROOM = 1,
	    /**
	     * Constant for features being added only to corridors.
	     */
	    CORRIDOR = 2,
	    /**
	     * Constant for features being added only to caves.
	     */
	    CAVE = 3;
    /**
     * The effects that will be applied when generate is called. Strongly prefer
     * using addWater, addDoors, addTraps, and addGrass.
     */
    public EnumMap<FillEffect, Integer> roomFX, corridorFX, caveFX;
    /**
     * Percentage of viable positions to fill with doors, represented by '+' for
     * east-to-west connections or '/' for north-to-south ones; this number will be
     * negative if filling two-cell wide positions but will be made positive when
     * needed.
     */
    public int doorFX = 0;
    /**
     * The char to use for deep lake cells.
     */
    public char deepLakeGlyph = '~';
    /**
     * The char to use for shallow lake cells.
     */
    public char shallowLakeGlyph = ',';
    /**
     * The approximate percentage of non-room, non-cave, non-edge-of-map wall cells
     * to try to fill with lake. Corridors that are covered by a lake will become
     * bridges, the glyph ':'.
     */
    public int lakeFX = 0;
    /**
     * The approximate percentage of non-room, non-cave, non-edge-of-map wall cells
     * to try to fill with maze. Corridors that are covered by a maze will become
     * part of its layout.
     */
    public int mazeFX = 0;
    public DungeonUtility utility;
    protected int height, width;
    public Coord stairsUp = null, stairsDown = null;
    public StatefulRNG rng;
    protected long rebuildSeed;
    protected boolean seedFixed = false;
    protected int environmentType = 1;
    protected char[][] dungeon = null;
    /**
     * Potentially important if you need to identify specific rooms, corridors, or
     * cave areas in a map.
     */
    public RoomFinder finder;
    /**
     * Configured by this class after you call generate(), this Placement can be
     * used to locate areas of the dungeon that fit certain properties, like "out of
     * sight from a door" or "a large flat section of wall that could be used to
     * place a straight-line object." You can use this as-needed; it does only a
     * small amount of work at the start, and does the calculations for what areas
     * have certain properties on request.
     */
    public Placement placement;

    /**
     * Get the most recently generated char[][] dungeon out of this class. The
     * dungeon may be null if generate() or setDungeon() have not been called.
     *
     * @return a char[][] dungeon, or null.
     */
    @Override
    public char[][] getDungeon() {
	return this.dungeon;
    }

    /**
     * Get the most recently generated char[][] dungeon out of this class without
     * any chars other than '#' or '.', for walls and floors respectively. The
     * dungeon may be null if generate() or setDungeon() have not been called.
     *
     * @return a char[][] dungeon with only '#' for walls and '.' for floors, or
     *         null.
     */
    public char[][] getBareDungeon() {
	return DungeonUtility.simplifyDungeon(this.dungeon);
    }

    /**
     * Change the underlying char[][]; only affects the toString method, and of
     * course getDungeon.
     *
     * @param dungeon a char[][], probably produced by an earlier call to this class
     *                and then modified.
     */
    public void setDungeon(final char[][] dungeon) {
	this.dungeon = dungeon;
	if (dungeon == null) {
	    this.width = 0;
	    this.height = 0;
	    return;
	}
	this.width = dungeon.length;
	if (this.width > 0) {
	    this.height = dungeon[0].length;
	}
    }

    /**
     * Height of the dungeon in cells.
     *
     * @return Height of the dungeon in cells.
     */
    public int getHeight() {
	return this.height;
    }

    /**
     * Width of the dungeon in cells.
     *
     * @return Width of the dungeon in cells.
     */
    public int getWidth() {
	return this.width;
    }

    /**
     * Make a SectionDungeonGenerator with a LightRNG using a random seed, height
     * 40, and width 40.
     */
    public SectionDungeonGenerator() {
	this.rng = new StatefulRNG();
	this.utility = new DungeonUtility(this.rng);
	this.rebuildSeed = this.rng.getState();
	this.height = 40;
	this.width = 40;
	this.roomFX = new EnumMap<>(FillEffect.class);
	this.corridorFX = new EnumMap<>(FillEffect.class);
	this.caveFX = new EnumMap<>(FillEffect.class);
    }

    /**
     * Make a SectionDungeonGenerator with the given height and width; the RNG used
     * for generating a dungeon and adding features will be a LightRNG using a
     * random seed. If width or height is greater than 256, then this will expand
     * the Coord pool from its 256x256 default so it stores a reference to each
     * Coord that might be used in the creation of the dungeon (if width and height
     * are 300 and 300, the Coord pool will be 300x300; if width and height are 500
     * and 100, the Coord pool will be 500x256 because it won't shrink below the
     * default size of 256x256).
     *
     * @param width  The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     */
    public SectionDungeonGenerator(final int width, final int height) {
	this(width, height, new RNG(new LightRNG()));
    }

    /**
     * Make a SectionDungeonGenerator with the given height, width, and RNG. Use
     * this if you want to seed the RNG. If width or height is greater than 256,
     * then this will expand the Coord pool from its 256x256 default so it stores a
     * reference to each Coord that might be used in the creation of the dungeon (if
     * width and height are 300 and 300, the Coord pool will be 300x300; if width
     * and height are 500 and 100, the Coord pool will be 500x256 because it won't
     * shrink below the default size of 256x256).
     *
     * @param width  The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     * @param rng    The RNG to use for all purposes in this class; if it is a
     *               StatefulRNG, then it will be used as-is, but if it is not a
     *               StatefulRNG, a new StatefulRNG will be used, randomly seeded by
     *               this parameter
     */
    public SectionDungeonGenerator(final int width, final int height, final RNG rng) {
	Coord.expandPoolTo(width, height);
	this.rng = rng instanceof StatefulRNG ? (StatefulRNG) rng : new StatefulRNG(rng.nextLong());
	this.utility = new DungeonUtility(this.rng);
	this.rebuildSeed = this.rng.getState();
	this.height = height;
	this.width = width;
	this.roomFX = new EnumMap<>(FillEffect.class);
	this.corridorFX = new EnumMap<>(FillEffect.class);
	this.caveFX = new EnumMap<>(FillEffect.class);
    }

    /**
     * Copies all fields from copying and makes a new DungeonGenerator.
     *
     * @param copying the DungeonGenerator to copy
     */
    public SectionDungeonGenerator(final SectionDungeonGenerator copying) {
	this.rng = new StatefulRNG(copying.rng.getState());
	this.utility = new DungeonUtility(this.rng);
	this.rebuildSeed = this.rng.getState();
	this.height = copying.height;
	this.width = copying.width;
	Coord.expandPoolTo(this.width, this.height);
	this.roomFX = new EnumMap<>(copying.roomFX);
	this.corridorFX = new EnumMap<>(copying.corridorFX);
	this.caveFX = new EnumMap<>(copying.caveFX);
	this.doorFX = copying.doorFX;
	this.lakeFX = copying.lakeFX;
	this.deepLakeGlyph = copying.deepLakeGlyph;
	this.shallowLakeGlyph = copying.shallowLakeGlyph;
	this.dungeon = copying.dungeon;
    }

    /**
     * Turns the majority of the given percentage of floor cells into water cells,
     * represented by '~'. Water will be clustered into a random number of pools,
     * with more appearing if needed to fill the percentage. Each pool will have
     * randomized volume that should fill or get very close to filling the requested
     * percentage, unless the pools encounter too much tight space. If this
     * DungeonGenerator previously had addWater called, the latest call will take
     * precedence. No islands will be placed with this variant, but the edge of the
     * water will be shallow, represented by ','.
     *
     * @param env        the environment to apply this to; uses MixedGenerator's
     *                   constants, or 0 for "all environments"
     * @param percentage the percentage of floor cells to fill with water
     * @return this DungeonGenerator; can be chained
     */
    public SectionDungeonGenerator addWater(final int env, int percentage) {
	if (percentage < 0) {
	    percentage = 0;
	}
	if (percentage > 100) {
	    percentage = 100;
	}
	switch (env) {
	case ROOM:
	    if (this.roomFX.containsKey(FillEffect.WATER)) {
		this.roomFX.remove(FillEffect.WATER);
	    }
	    this.roomFX.put(FillEffect.WATER, percentage);
	    break;
	case CORRIDOR:
	    if (this.corridorFX.containsKey(FillEffect.WATER)) {
		this.corridorFX.remove(FillEffect.WATER);
	    }
	    this.corridorFX.put(FillEffect.WATER, percentage);
	    break;
	case CAVE:
	    if (this.caveFX.containsKey(FillEffect.WATER)) {
		this.caveFX.remove(FillEffect.WATER);
	    }
	    this.caveFX.put(FillEffect.WATER, percentage);
	    break;
	default:
	    if (this.roomFX.containsKey(FillEffect.WATER)) {
		this.roomFX.put(FillEffect.WATER, Math.min(100, this.roomFX.get(FillEffect.WATER) + percentage));
	    } else {
		this.roomFX.put(FillEffect.WATER, percentage);
	    }
	    if (this.corridorFX.containsKey(FillEffect.WATER)) {
		this.corridorFX.put(FillEffect.WATER,
			Math.min(100, this.corridorFX.get(FillEffect.WATER) + percentage));
	    } else {
		this.corridorFX.put(FillEffect.WATER, percentage);
	    }
	    if (this.caveFX.containsKey(FillEffect.WATER)) {
		this.caveFX.put(FillEffect.WATER, Math.min(100, this.caveFX.get(FillEffect.WATER) + percentage));
	    } else {
		this.caveFX.put(FillEffect.WATER, percentage);
	    }
	}
	return this;
    }

    /**
     * Turns the majority of the given percentage of floor cells into water cells,
     * represented by '~'. Water will be clustered into a random number of pools,
     * with more appearing if needed to fill the percentage. Each pool will have
     * randomized volume that should fill or get very close to filling the requested
     * percentage, unless the pools encounter too much tight space. If this
     * DungeonGenerator previously had addWater called, the latest call will take
     * precedence. If islandSpacing is greater than 1, then this will place islands
     * of floor, '.', surrounded by shallow water, ',', at about the specified
     * distance with Euclidean measurement.
     *
     * @param env           the environment to apply this to; uses MixedGenerator's
     *                      constants, or 0 for "all environments"
     * @param percentage    the percentage of floor cells to fill with water
     * @param islandSpacing if greater than 1, islands will be placed randomly this
     *                      many cells apart.
     * @return this DungeonGenerator; can be chained
     */
    public SectionDungeonGenerator addWater(final int env, int percentage, final int islandSpacing) {
	this.addWater(env, percentage);
	if (percentage < 0) {
	    percentage = 0;
	}
	if (percentage > 100) {
	    percentage = 100;
	}
	switch (env) {
	case ROOM:
	    if (this.roomFX.containsKey(FillEffect.ISLANDS)) {
		this.roomFX.remove(FillEffect.ISLANDS);
	    }
	    if (islandSpacing > 1) {
		this.roomFX.put(FillEffect.ISLANDS, percentage);
	    }
	    break;
	case CORRIDOR:
	    if (this.corridorFX.containsKey(FillEffect.ISLANDS)) {
		this.corridorFX.remove(FillEffect.ISLANDS);
	    }
	    if (islandSpacing > 1) {
		this.corridorFX.put(FillEffect.ISLANDS, percentage);
	    }
	    break;
	case CAVE:
	    if (this.caveFX.containsKey(FillEffect.ISLANDS)) {
		this.caveFX.remove(FillEffect.ISLANDS);
	    }
	    if (islandSpacing > 1) {
		this.caveFX.put(FillEffect.ISLANDS, percentage);
	    }
	    break;
	default:
	    if (this.roomFX.containsKey(FillEffect.ISLANDS)) {
		this.roomFX.remove(FillEffect.ISLANDS);
	    }
	    if (islandSpacing > 1) {
		this.roomFX.put(FillEffect.ISLANDS, percentage);
	    }
	    if (this.corridorFX.containsKey(FillEffect.ISLANDS)) {
		this.corridorFX.remove(FillEffect.ISLANDS);
	    }
	    if (islandSpacing > 1) {
		this.corridorFX.put(FillEffect.ISLANDS, percentage);
	    }
	    if (this.caveFX.containsKey(FillEffect.ISLANDS)) {
		this.caveFX.remove(FillEffect.ISLANDS);
	    }
	    if (islandSpacing > 1) {
		this.caveFX.put(FillEffect.ISLANDS, percentage);
	    }
	}
	return this;
    }

    /**
     * Turns the majority of the given percentage of floor cells into grass cells,
     * represented by '"'. Grass will be clustered into a random number of patches,
     * with more appearing if needed to fill the percentage. Each area will have
     * randomized volume that should fill or get very close to filling (two thirds
     * of) the requested percentage, unless the patches encounter too much tight
     * space. If this DungeonGenerator previously had addGrass called, the latest
     * call will take precedence.
     *
     * @param env        the environment to apply this to; uses MixedGenerator's
     *                   constants, or 0 for "all environments"
     * @param percentage the percentage of floor cells to fill with grass; this can
     *                   vary quite a lot. It may be difficult to fill very high
     *                   (over 66%) percentages of map with grass, though you can do
     *                   this by giving a percentage of between 100 and 150.
     * @return this DungeonGenerator; can be chained
     */
    public SectionDungeonGenerator addGrass(final int env, int percentage) {
	if (percentage < 0) {
	    percentage = 0;
	}
	if (percentage > 100) {
	    percentage = 100;
	}
	switch (env) {
	case ROOM:
	    if (this.roomFX.containsKey(FillEffect.GRASS)) {
		this.roomFX.remove(FillEffect.GRASS);
	    }
	    this.roomFX.put(FillEffect.GRASS, percentage);
	    break;
	case CORRIDOR:
	    if (this.corridorFX.containsKey(FillEffect.GRASS)) {
		this.corridorFX.remove(FillEffect.GRASS);
	    }
	    this.corridorFX.put(FillEffect.GRASS, percentage);
	    break;
	case CAVE:
	    if (this.caveFX.containsKey(FillEffect.GRASS)) {
		this.caveFX.remove(FillEffect.GRASS);
	    }
	    this.caveFX.put(FillEffect.GRASS, percentage);
	    break;
	default:
	    if (this.roomFX.containsKey(FillEffect.GRASS)) {
		this.roomFX.put(FillEffect.GRASS, Math.min(100, this.roomFX.get(FillEffect.GRASS) + percentage));
	    } else {
		this.roomFX.put(FillEffect.GRASS, percentage);
	    }
	    if (this.corridorFX.containsKey(FillEffect.GRASS)) {
		this.corridorFX.put(FillEffect.GRASS,
			Math.min(100, this.corridorFX.get(FillEffect.GRASS) + percentage));
	    } else {
		this.corridorFX.put(FillEffect.GRASS, percentage);
	    }
	    if (this.caveFX.containsKey(FillEffect.GRASS)) {
		this.caveFX.put(FillEffect.GRASS, Math.min(100, this.caveFX.get(FillEffect.GRASS) + percentage));
	    } else {
		this.caveFX.put(FillEffect.GRASS, percentage);
	    }
	}
	return this;
    }

    /**
     * Turns the given percentage of floor cells not already adjacent to walls into
     * wall cells, represented by '#'. If this DungeonGenerator previously had
     * addBoulders called, the latest call will take precedence.
     *
     * @param env        the environment to apply this to; uses MixedGenerator's
     *                   constants, or 0 for "all environments"
     * @param percentage the percentage of floor cells not adjacent to walls to fill
     *                   with boulders.
     * @return this DungeonGenerator; can be chained
     */
    public SectionDungeonGenerator addBoulders(final int env, int percentage) {
	if (percentage < 0) {
	    percentage = 0;
	}
	if (percentage > 100) {
	    percentage = 100;
	}
	switch (env) {
	case ROOM:
	    if (this.roomFX.containsKey(FillEffect.BOULDERS)) {
		this.roomFX.remove(FillEffect.BOULDERS);
	    }
	    this.roomFX.put(FillEffect.BOULDERS, percentage);
	    break;
	case CORRIDOR:
	    if (this.corridorFX.containsKey(FillEffect.BOULDERS)) {
		this.corridorFX.remove(FillEffect.BOULDERS);
	    }
	    this.corridorFX.put(FillEffect.BOULDERS, percentage);
	    break;
	case CAVE:
	    if (this.caveFX.containsKey(FillEffect.BOULDERS)) {
		this.caveFX.remove(FillEffect.BOULDERS);
	    }
	    this.caveFX.put(FillEffect.BOULDERS, percentage);
	    break;
	default:
	    if (this.roomFX.containsKey(FillEffect.BOULDERS)) {
		this.roomFX.put(FillEffect.BOULDERS, Math.min(100, this.roomFX.get(FillEffect.BOULDERS) + percentage));
	    } else {
		this.roomFX.put(FillEffect.BOULDERS, percentage);
	    }
	    if (this.corridorFX.containsKey(FillEffect.BOULDERS)) {
		this.corridorFX.put(FillEffect.BOULDERS,
			Math.min(100, this.corridorFX.get(FillEffect.BOULDERS) + percentage));
	    } else {
		this.corridorFX.put(FillEffect.BOULDERS, percentage);
	    }
	    if (this.caveFX.containsKey(FillEffect.BOULDERS)) {
		this.caveFX.put(FillEffect.BOULDERS, Math.min(100, this.caveFX.get(FillEffect.BOULDERS) + percentage));
	    } else {
		this.caveFX.put(FillEffect.BOULDERS, percentage);
	    }
	}
	return this;
    }

    /**
     * Turns the given percentage of viable doorways into doors, represented by '+'
     * for doors that allow travel along the x-axis and '/' for doors that allow
     * travel along the y-axis. If doubleDoors is true, 2-cell-wide openings will be
     * considered viable doorways and will fill one cell with a wall, the other a
     * door. If this DungeonGenerator previously had addDoors called, the latest
     * call will take precedence.
     *
     * @param percentage  the percentage of valid openings to corridors to fill with
     *                    doors; should be between 10 and 20 if you want doors to
     *                    appear more than a few times, but not fill every possible
     *                    opening.
     * @param doubleDoors true if you want two-cell-wide openings to receive a door
     *                    and a wall; false if only one-cell-wide openings should
     *                    receive doors. Usually, this should be true.
     * @return this DungeonGenerator; can be chained
     */
    public SectionDungeonGenerator addDoors(int percentage, final boolean doubleDoors) {
	if (percentage < 0) {
	    percentage = 0;
	}
	if (percentage > 100) {
	    percentage = 100;
	}
	if (doubleDoors) {
	    percentage *= -1;
	}
	this.doorFX = percentage;
	return this;
    }

    /**
     * Instructs the generator to add a winding section of corridors into a large
     * area that can be filled without overwriting rooms, caves, or the edge of the
     * map; wall cells will become either '#' or '.' and corridors will be
     * overwritten. If the percentage is too high (40% is probably too high to
     * adequately fill), this will fill less than the requested percentage rather
     * than fill multiple mazes.
     *
     * @param percentage The percentage of non-room, non-cave, non-edge-of-map wall
     *                   cells to try to fill with maze.
     * @return this for chaining
     */
    public SectionDungeonGenerator addMaze(int percentage) {
	if (percentage < 0) {
	    percentage = 0;
	}
	if (percentage > 100) {
	    percentage = 100;
	}
	this.mazeFX = percentage;
	return this;
    }

    /**
     * Instructs the generator to add a lake (here, of water) into a large area that
     * can be filled without overwriting rooms, caves, or the edge of the map; wall
     * cells will become the deep lake glyph (here, '~'), unless they are close to
     * an existing room or cave, in which case they become the shallow lake glyph
     * (here, ','), and corridors that are "covered" by a lake will become bridges,
     * the glyph ':'. If the percentage is too high (40% is probably too high to
     * adequately fill), this will fill less than the requested percentage rather
     * than fill multiple lakes.
     *
     * @param percentage The percentage of non-room, non-cave, non-edge-of-map wall
     *                   cells to try to fill with lake.
     * @return this for chaining
     */
    public SectionDungeonGenerator addLake(final int percentage) {
	return this.addLake(percentage, '~', ',');
    }

    /**
     * Instructs the generator to add a lake into a large area that can be filled
     * without overwriting rooms, caves, or the edge of the map; wall cells will
     * become the char deepLake, unless they are close to an existing room or cave,
     * in which case they become the char shallowLake, and corridors that are
     * "covered" by a lake will become bridges, the glyph ':'. If the percentage is
     * too high (40% is probably too high to adequately fill), this will fill less
     * than the requested percentage rather than fill multiple lakes.
     *
     * @param percentage  The percentage of non-room, non-cave, non-edge-of-map wall
     *                    cells to try to fill with lake.
     * @param deepLake    the char to use for deep lake cells, such as '~'
     * @param shallowLake the char to use for shallow lake cells, such as ','
     * @return this for chaining
     */
    public SectionDungeonGenerator addLake(int percentage, final char deepLake, final char shallowLake) {
	if (percentage < 0) {
	    percentage = 0;
	}
	if (percentage > 100) {
	    percentage = 100;
	}
	this.lakeFX = percentage;
	this.deepLakeGlyph = deepLake;
	this.shallowLakeGlyph = shallowLake;
	return this;
    }

    /**
     * Turns the given percentage of open area floor cells into trap cells,
     * represented by '^'. Corridors that have no possible way to move around a trap
     * will not receive traps, ever. If this DungeonGenerator previously had
     * addTraps called, the latest call will take precedence.
     *
     * @param env        the environment to apply this to; uses MixedGenerator's
     *                   constants, or 0 for "all environments"
     * @param percentage the percentage of valid cells to fill with traps; should be
     *                   no higher than 5 unless the dungeon floor is meant to be a
     *                   kill screen or minefield.
     * @return this DungeonGenerator; can be chained
     */
    public SectionDungeonGenerator addTraps(final int env, int percentage) {
	if (percentage < 0) {
	    percentage = 0;
	}
	if (percentage > 100) {
	    percentage = 100;
	}
	switch (env) {
	case ROOM:
	    if (this.roomFX.containsKey(FillEffect.TRAPS)) {
		this.roomFX.remove(FillEffect.TRAPS);
	    }
	    this.roomFX.put(FillEffect.TRAPS, percentage);
	    break;
	case CORRIDOR:
	    if (this.corridorFX.containsKey(FillEffect.TRAPS)) {
		this.corridorFX.remove(FillEffect.TRAPS);
	    }
	    this.corridorFX.put(FillEffect.TRAPS, percentage);
	    break;
	case CAVE:
	    if (this.caveFX.containsKey(FillEffect.TRAPS)) {
		this.caveFX.remove(FillEffect.TRAPS);
	    }
	    this.caveFX.put(FillEffect.TRAPS, percentage);
	    break;
	default:
	    if (this.roomFX.containsKey(FillEffect.TRAPS)) {
		this.roomFX.put(FillEffect.TRAPS, Math.min(100, this.roomFX.get(FillEffect.TRAPS) + percentage));
	    } else {
		this.roomFX.put(FillEffect.TRAPS, percentage);
	    }
	    if (this.corridorFX.containsKey(FillEffect.TRAPS)) {
		this.corridorFX.put(FillEffect.TRAPS,
			Math.min(100, this.corridorFX.get(FillEffect.TRAPS) + percentage));
	    } else {
		this.corridorFX.put(FillEffect.TRAPS, percentage);
	    }
	    if (this.caveFX.containsKey(FillEffect.TRAPS)) {
		this.caveFX.put(FillEffect.TRAPS, Math.min(100, this.caveFX.get(FillEffect.TRAPS) + percentage));
	    } else {
		this.caveFX.put(FillEffect.TRAPS, percentage);
	    }
	}
	return this;
    }

    /**
     * Removes any door, water, or trap insertion effects that this DungeonGenerator
     * would put in future dungeons.
     *
     * @return this DungeonGenerator, with all effects removed. Can be chained.
     */
    public SectionDungeonGenerator clearEffects() {
	this.roomFX.clear();
	this.corridorFX.clear();
	this.caveFX.clear();
	this.lakeFX = 0;
	this.mazeFX = 0;
	this.doorFX = 0;
	return this;
    }

    protected OrderedSet<Coord> removeAdjacent(final OrderedSet<Coord> coll, final Coord pt) {
	for (final Coord temp : new Coord[] { Coord.get(pt.x + 1, pt.y), Coord.get(pt.x - 1, pt.y),
		Coord.get(pt.x, pt.y + 1), Coord.get(pt.x, pt.y - 1) }) {
	    coll.remove(temp);
	}
	return coll;
    }

    protected OrderedSet<Coord> removeAdjacent(final OrderedSet<Coord> coll, final Coord pt1, final Coord pt2) {
	for (final Coord temp : new Coord[] { Coord.get(pt1.x + 1, pt1.y), Coord.get(pt1.x - 1, pt1.y),
		Coord.get(pt1.x, pt1.y + 1), Coord.get(pt1.x, pt1.y - 1), Coord.get(pt2.x + 1, pt2.y),
		Coord.get(pt2.x - 1, pt2.y), Coord.get(pt2.x, pt2.y + 1), Coord.get(pt2.x, pt2.y - 1), }) {
	    if (!(temp.x == pt1.x && temp.y == pt1.y) && !(temp.x == pt2.x && temp.y == pt2.y)) {
		coll.remove(temp);
	    }
	}
	return coll;
    }

    protected OrderedSet<Coord> removeNearby(final OrderedSet<Coord> coll, final char[][] disallowed) {
	if (coll == null || disallowed == null || disallowed.length == 0 || disallowed[0].length == 0) {
	    return new OrderedSet<>();
	}
	final OrderedSet<Coord> next = new OrderedSet<>(coll.size());
	final int width = disallowed.length, height = disallowed[0].length;
	COORD_WISE: for (final Coord c : coll) {
	    for (int x = Math.max(0, c.x - 1); x <= Math.min(width - 1, c.x + 1); x++) {
		for (int y = Math.max(0, c.y - 1); y <= Math.min(height - 1, c.y + 1); y++) {
		    if (disallowed[x][y] != '#') {
			continue COORD_WISE;
		    }
		}
	    }
	    next.add(c);
	}
	return next;
    }

    protected OrderedSet<Coord> viableDoorways(final boolean doubleDoors, final char[][] map, final char[][] allCaves,
	    final char[][] allCorridors) {
	OrderedSet<Coord> doors = new OrderedSet<>();
	final OrderedSet<Coord> blocked = new OrderedSet<>(4);
	final DijkstraMap dm = new DijkstraMap(map, DijkstraMap.Measurement.EUCLIDEAN);
	for (int x = 1; x < map.length - 1; x++) {
	    for (int y = 1; y < map[x].length - 1; y++) {
		if (map[x][y] == '#' || allCorridors[x][y] != '#') {
		    continue;
		}
		if (doubleDoors) {
		    if (x >= map.length - 2 || y >= map[x].length - 2) {
			continue;
		    } else {
			if (map[x + 1][y] != '#' && map[x + 2][y] == '#' && map[x - 1][y] == '#' && map[x][y + 1] != '#'
				&& map[x][y - 1] != '#' && map[x + 1][y + 1] != '#' && map[x + 1][y - 1] != '#') {
			    if (map[x + 2][y + 1] != '#' || map[x - 1][y + 1] != '#' || map[x + 2][y - 1] != '#'
				    || map[x - 1][y - 1] != '#') {
				dm.resetMap();
				dm.clearGoals();
				dm.setGoal(x, y + 1);
				blocked.clear();
				blocked.add(Coord.get(x, y));
				blocked.add(Coord.get(x + 1, y));
				if (dm.partialScan(16, blocked)[x][y - 1] < DijkstraMap.FLOOR) {
				    continue;
				}
				doors.add(Coord.get(x, y));
				doors.add(Coord.get(x + 1, y));
				doors = this.removeAdjacent(doors, Coord.get(x, y), Coord.get(x + 1, y));
				continue;
			    }
			} else if (map[x][y + 1] != '#' && map[x][y + 2] == '#' && map[x][y - 1] == '#'
				&& map[x + 1][y] != '#' && map[x - 1][y] != '#' && map[x + 1][y + 1] != '#'
				&& map[x - 1][y + 1] != '#') {
			    if (map[x + 1][y + 2] != '#' || map[x + 1][y - 1] != '#' || map[x - 1][y + 2] != '#'
				    || map[x - 1][y - 1] != '#') {
				dm.resetMap();
				dm.clearGoals();
				dm.setGoal(x + 1, y);
				blocked.clear();
				blocked.add(Coord.get(x, y));
				blocked.add(Coord.get(x, y + 1));
				if (dm.partialScan(16, blocked)[x - 1][y] < DijkstraMap.FLOOR) {
				    continue;
				}
				doors.add(Coord.get(x, y));
				doors.add(Coord.get(x, y + 1));
				doors = this.removeAdjacent(doors, Coord.get(x, y), Coord.get(x, y + 1));
				continue;
			    }
			}
		    }
		}
		if (map[x + 1][y] == '#' && map[x - 1][y] == '#' && map[x][y + 1] != '#' && map[x][y - 1] != '#') {
		    if (map[x + 1][y + 1] != '#' || map[x - 1][y + 1] != '#' || map[x + 1][y - 1] != '#'
			    || map[x - 1][y - 1] != '#') {
			dm.resetMap();
			dm.clearGoals();
			dm.setGoal(x, y + 1);
			blocked.clear();
			blocked.add(Coord.get(x, y));
			if (dm.partialScan(16, blocked)[x][y - 1] < DijkstraMap.FLOOR) {
			    continue;
			}
			doors.add(Coord.get(x, y));
			doors = this.removeAdjacent(doors, Coord.get(x, y));
		    }
		} else if (map[x][y + 1] == '#' && map[x][y - 1] == '#' && map[x + 1][y] != '#'
			&& map[x - 1][y] != '#') {
		    if (map[x + 1][y + 1] != '#' || map[x + 1][y - 1] != '#' || map[x - 1][y + 1] != '#'
			    || map[x - 1][y - 1] != '#') {
			dm.resetMap();
			dm.clearGoals();
			dm.setGoal(x + 1, y);
			blocked.clear();
			blocked.add(Coord.get(x, y));
			if (dm.partialScan(16, blocked)[x - 1][y] < DijkstraMap.FLOOR) {
			    continue;
			}
			doors.add(Coord.get(x, y));
			doors = this.removeAdjacent(doors, Coord.get(x, y));
		    }
		}
	    }
	}
	return this.removeNearby(doors, allCaves);
    }

    /**
     * Generate a char[][] dungeon using TilesetType.DEFAULT_DUNGEON; this produces
     * a dungeon appropriate for a level of ruins or a partially constructed
     * dungeon. This uses '#' for walls, '.' for floors, '~' for deep water, ',' for
     * shallow water, '^' for traps, '+' for doors that provide horizontal passage,
     * and '/' for doors that provide vertical passage. Use the addDoors, addWater,
     * addGrass, and addTraps methods of this class to request these in the
     * generated map. Also sets the fields stairsUp and stairsDown to two randomly
     * chosen, distant, connected, walkable cells.
     *
     * @return a char[][] dungeon
     */
    @Override
    public char[][] generate() {
	return this.generate(TilesetType.DEFAULT_DUNGEON);
    }

    /**
     * Generate a char[][] dungeon given a TilesetType; the comments in that class
     * provide some opinions on what each TilesetType value could be used for in a
     * game. This uses '#' for walls, '.' for floors, '~' for deep water, ',' for
     * shallow water, '^' for traps, '+' for doors that provide horizontal passage,
     * and '/' for doors that provide vertical passage. Use the addDoors, addWater,
     * addGrass, and addTraps methods of this class to request these in the
     * generated map. Also sets the fields stairsUp and stairsDown to two randomly
     * chosen, distant, connected, walkable cells.
     *
     * @see TilesetType
     * @param kind a TilesetType enum value, such as TilesetType.DEFAULT_DUNGEON
     * @return a char[][] dungeon
     */
    public char[][] generate(final TilesetType kind) {
	this.rebuildSeed = this.rng.getState();
	this.environmentType = kind.environment();
	final DungeonBoneGen gen = new DungeonBoneGen(this.rng);
	final char[][] map = DungeonUtility.wallWrap(gen.generate(kind, this.width, this.height));
	this.seedFixed = false;
	final DijkstraMap dijkstra = new DijkstraMap(map);
	int frustrated = 0;
	do {
	    dijkstra.clearGoals();
	    this.stairsUp = this.utility.randomFloor(map);
	    dijkstra.setGoal(this.stairsUp);
	    dijkstra.scan(null);
	    frustrated++;
	} while (dijkstra.getMappedCount() < this.width + this.height && frustrated < 15);
	double maxDijkstra = 0.0;
	for (int i = 0; i < this.width; i++) {
	    for (int j = 0; j < this.height; j++) {
		if (dijkstra.gradientMap[i][j] >= DijkstraMap.FLOOR) {
		    map[i][j] = '#';
		} else if (dijkstra.gradientMap[i][j] > maxDijkstra) {
		    maxDijkstra = dijkstra.gradientMap[i][j];
		}
	    }
	}
	this.stairsDown = new GreasedRegion(dijkstra.gradientMap, maxDijkstra * 0.7, DijkstraMap.FLOOR)
		.singleRandom(this.rng);
	this.finder = new RoomFinder(map, this.environmentType);
	return this.innerGenerate();
    }

    /**
     * Generate a char[][] dungeon with extra features given a baseDungeon that has
     * already been generated and an environment as an int[][], which can often be
     * obtained from MixedGenerator or classes that use it, like
     * SerpentMapGenerator, with their getEnvironment method. Typically, you want to
     * call generate with a TilesetType or no argument for the easiest generation;
     * this method is meant for adding features like water and doors to existing
     * maps while avoiding placing incongruous features in areas where they don't
     * fit, like a door in a cave or moss in a room. This uses '#' for walls, '.'
     * for floors, '~' for deep water, ',' for shallow water, '^' for traps, '+' for
     * doors that provide horizontal passage, and '/' for doors that provide
     * vertical passage. Use the addDoors, addWater, addGrass, and addTraps methods
     * of this class to request these in the generated map. Also sets the fields
     * stairsUp and stairsDown to two randomly chosen, distant, connected, walkable
     * cells. <br>
     * Special behavior here: If tab characters are present in the 2D char array,
     * they will be replaced with '.' in the final dungeon, but will also be tried
     * first as valid staircase locations (with a high distance possible to travel
     * away from the starting staircase). If no tab characters are present this will
     * search for '.' floors to place stairs on, as normal. This tab-first behavior
     * is useful in conjunction with some methods that establish a good path in an
     * existing dungeon; an example is
     * {@code DungeonUtility.ensurePath(dungeon, rng, '\t', '#');} then passing
     * dungeon (which that code modifies) in as baseDungeon to this method. Because
     * tabs will always be replaced by floors ('.'), this considers any tabs that
     * overlap with what the environment considers a wall (cave wall, room wall,
     * corridor wall, or untouched) to really refer to a corridor floor, but doesn't
     * reconsider tabs that overlap with floors already (it keeps the state of
     * actual room, cave, and corridor floors). This is useful so you only have to
     * call ensurePath or a similar method on the 2D char array and can leave the 2D
     * int array alone.
     *
     * @param baseDungeon a pre-made dungeon consisting of '#' for walls and '.' for
     *                    floors; may be modified in-place
     * @param environment stores whether a cell is room, corridor, or cave;
     *                    getEnvironment() typically gives this
     * @return a char[][] dungeon
     */
    public char[][] generate(final char[][] baseDungeon, final int[][] environment) {
	if (!this.seedFixed) {
	    this.rebuildSeed = this.rng.getState();
	}
	this.seedFixed = false;
	final char[][] map = DungeonUtility.wallWrap(baseDungeon);
	this.width = map.length;
	this.height = map[0].length;
	final int[][] env2 = new int[this.width][this.height];
	for (int x = 0; x < this.width; x++) {
	    System.arraycopy(environment[x], 0, env2[x], 0, this.height);
	}
	final DijkstraMap dijkstra = new DijkstraMap(map);
	int frustrated = 0;
	do {
	    dijkstra.clearGoals();
	    this.stairsUp = this.utility.randomMatchingTile(map, '\t');
	    if (this.stairsUp == null) {
		this.stairsUp = this.utility.randomFloor(map);
		if (this.stairsUp == null) {
		    frustrated++;
		    continue;
		}
	    }
	    dijkstra.setGoal(this.stairsUp);
	    dijkstra.scan(null);
	    frustrated++;
	} while (dijkstra.getMappedCount() < this.width + this.height && frustrated < 8);
	if (frustrated >= 8) {
	    return this.generate();
	}
	double maxDijkstra = 0.0;
	for (int i = 0; i < this.width; i++) {
	    for (int j = 0; j < this.height; j++) {
		if (dijkstra.gradientMap[i][j] >= DijkstraMap.FLOOR) {
		    map[i][j] = '#';
		    env2[i][j] = MixedGenerator.UNTOUCHED;
		} else if (dijkstra.gradientMap[i][j] > maxDijkstra) {
		    maxDijkstra = dijkstra.gradientMap[i][j];
		}
		if (map[i][j] == '\t') {
		    map[i][j] = '.';
		    if ((env2[i][j] & 1) == 0) {
			env2[i][j] = MixedGenerator.CORRIDOR_FLOOR;
		    }
		}
	    }
	}
	if (maxDijkstra < 16) {
	    return this.generate(baseDungeon, environment);
	}
	this.stairsDown = new GreasedRegion(dijkstra.gradientMap, maxDijkstra * 0.7, DijkstraMap.FLOOR)
		.singleRandom(this.rng);
	this.finder = new RoomFinder(map, env2);
	return this.innerGenerate();
    }

    /**
     * Generate a char[][] dungeon with extra features given a baseDungeon that has
     * already been generated, with staircases represented by greater than and less
     * than signs, and an environment as an int[][], which can often be obtained
     * from MixedGenerator or classes that use it, like SerpentMapGenerator, with
     * their getEnvironment method. Typically, you want to call generate with a
     * TilesetType or no argument for the easiest generation; this method is meant
     * for adding features like water and doors to existing maps while avoiding
     * placing incongruous features in areas where they don't fit, like a door in a
     * cave or moss in a room. This uses '#' for walls, '.' for floors, '~' for deep
     * water, ',' for shallow water, '^' for traps, '+' for doors that provide
     * horizontal passage, and '/' for doors that provide vertical passage. Use the
     * addDoors, addWater, addGrass, and addTraps methods of this class to request
     * these in the generated map. Also sets the fields stairsUp and stairsDown to
     * null, and expects stairs to be already handled.
     *
     * @param baseDungeon a pre-made dungeon consisting of '#' for walls and '.' for
     *                    floors, with stairs already in; may be modified in-place
     * @param environment stores whether a cell is room, corridor, or cave;
     *                    getEnvironment() typically gives this
     * @return a char[][] dungeon
     */
    public char[][] generateRespectingStairs(final char[][] baseDungeon, final int[][] environment) {
	if (!this.seedFixed) {
	    this.rebuildSeed = this.rng.getState();
	}
	this.seedFixed = false;
	final char[][] map = DungeonUtility.wallWrap(baseDungeon);
	final int[][] env2 = new int[this.width][this.height];
	for (int x = 0; x < this.width; x++) {
	    System.arraycopy(environment[x], 0, env2[x], 0, this.height);
	}
	final DijkstraMap dijkstra = new DijkstraMap(map);
	this.stairsUp = null;
	this.stairsDown = null;
	dijkstra.clearGoals();
	final ArrayList<Coord> stairs = DungeonUtility.allMatching(map, '<', '>');
	for (int j = 0; j < stairs.size(); j++) {
	    dijkstra.setGoal(stairs.get(j));
	}
	dijkstra.scan(null);
	for (int i = 0; i < this.width; i++) {
	    for (int j = 0; j < this.height; j++) {
		if (dijkstra.gradientMap[i][j] >= DijkstraMap.FLOOR) {
		    map[i][j] = '#';
		    env2[i][j] = MixedGenerator.UNTOUCHED;
		}
	    }
	}
	this.finder = new RoomFinder(map, env2);
	return this.innerGenerate();
    }

    protected char[][] innerGenerate() {
	this.dungeon = ArrayTools.fill('#', this.width, this.height);
	ArrayList<char[][]> rm = this.finder.findRooms(), cr = this.finder.findCorridors(),
		cv = this.finder.findCaves();
	final char[][] roomMap = this.innerGenerate(RoomFinder.merge(rm, this.width, this.height), this.roomFX),
		allCorridors = RoomFinder.merge(cr, this.width, this.height),
		corridorMap = this.innerGenerate(allCorridors, this.corridorFX);
	char[][] allCaves = RoomFinder.merge(cv, this.width, this.height);
	final char[][] caveMap = this.innerGenerate(allCaves, this.caveFX);
	char[][] doorMap;
	final char[][][] lakesAndMazes = this.makeLake(rm, cv);
	for (int y = 0; y < this.height; y++) {
	    for (int x = 0; x < this.width; x++) {
		if (corridorMap[x][y] != '#' && lakesAndMazes[0][x][y] != '#') {
		    this.dungeon[x][y] = ':';
		} else if (roomMap[x][y] != '#') {
		    this.dungeon[x][y] = roomMap[x][y];
		} else if (lakesAndMazes[1][x][y] != '#') {
		    this.dungeon[x][y] = lakesAndMazes[1][x][y];
		    this.finder.environment[x][y] = MixedGenerator.CORRIDOR_FLOOR;
		} else if (corridorMap[x][y] != '#') {
		    this.dungeon[x][y] = corridorMap[x][y];
		} else if (caveMap[x][y] != '#') {
		    this.dungeon[x][y] = caveMap[x][y];
		} else if (lakesAndMazes[0][x][y] != '#') {
		    this.dungeon[x][y] = lakesAndMazes[0][x][y];
		    this.finder.environment[x][y] = MixedGenerator.CAVE_FLOOR;
		}
	    }
	}
	this.finder = new RoomFinder(this.dungeon, this.finder.environment);
	rm = this.finder.findRooms();
	cr = this.finder.findCorridors();
	cv = this.finder.findCaves();
	cv.add(lakesAndMazes[0]);
	allCaves = RoomFinder.merge(cv, this.width, this.height);
	doorMap = this.makeDoors(rm, cr, allCaves, allCorridors);
	for (int y = 0; y < this.height; y++) {
	    for (int x = 0; x < this.width; x++) {
		if (doorMap[x][y] == '+' || doorMap[x][y] == '/') {
		    this.dungeon[x][y] = doorMap[x][y];
		} else if (doorMap[x][y] == '*') {
		    this.dungeon[x][y] = '#';
		}
	    }
	}
	this.placement = new Placement(this.finder);
	return this.dungeon;
    }

    protected char[][] makeDoors(final ArrayList<char[][]> rooms, final ArrayList<char[][]> corridors,
	    final char[][] allCaves, final char[][] allCorridors) {
	char[][] map = new char[this.width][this.height];
	for (int x = 0; x < this.width; x++) {
	    Arrays.fill(map[x], '#');
	}
	if (this.doorFX == 0 || rooms.isEmpty() && corridors.isEmpty()) {
	    return map;
	}
	boolean doubleDoors = false;
	int doorFill = this.doorFX;
	if (doorFill < 0) {
	    doubleDoors = true;
	    doorFill *= -1;
	}
	final ArrayList<char[][]> fused = new ArrayList<>(rooms.size() + corridors.size());
	fused.addAll(rooms);
	fused.addAll(corridors);
	map = RoomFinder.merge(fused, this.width, this.height);
	final OrderedSet<Coord> doorways = this.viableDoorways(doubleDoors, map, allCaves, allCorridors);
	final int total = doorways.size() * doorFill / 100;
	BigLoop: for (int i = 0; i < total; i++) {
	    final Coord entry = this.rng.getRandomElement(doorways);
	    if (map[entry.x][entry.y] == '<' || map[entry.x][entry.y] == '>') {
		continue;
	    }
	    if (map[entry.x - 1][entry.y] != '#' && map[entry.x + 1][entry.y] != '#' && map[entry.x - 1][entry.y] != '*'
		    && map[entry.x + 1][entry.y] != '*') {
		map[entry.x][entry.y] = '+';
	    } else {
		map[entry.x][entry.y] = '/';
	    }
	    final Coord[] adj = new Coord[] { Coord.get(entry.x + 1, entry.y), Coord.get(entry.x - 1, entry.y),
		    Coord.get(entry.x, entry.y + 1), Coord.get(entry.x, entry.y - 1) };
	    for (final Coord near : adj) {
		if (doorways.contains(near)) {
		    map[near.x][near.y] = '*';
		    doorways.remove(near);
		    doorways.remove(entry);
		    i++;
		    continue BigLoop;
		}
	    }
	    doorways.remove(entry);
	}
	return map;
    }

    protected char[][][] makeLake(final ArrayList<char[][]> rooms, final ArrayList<char[][]> caves) {
	final char[][][] maps = new char[2][this.width][this.height];
	char[][] fusedMap;
	for (int x = 0; x < this.width; x++) {
	    Arrays.fill(maps[0][x], '#');
	    Arrays.fill(maps[1][x], '#');
	}
	if (this.lakeFX == 0 && this.mazeFX == 0 || rooms.isEmpty() && caves.isEmpty()) {
	    return maps;
	}
	int lakeFill = this.lakeFX, mazeFill = this.mazeFX;
	if (this.lakeFX + this.mazeFX > 100) {
	    lakeFill -= (this.lakeFX + this.mazeFX - 100) / 2;
	    mazeFill -= (this.lakeFX + this.mazeFX - 99) / 2;
	}
	final ArrayList<char[][]> fused = new ArrayList<>(rooms.size() + caves.size());
	fused.addAll(rooms);
	fused.addAll(caves);
	fusedMap = RoomFinder.merge(fused, this.width, this.height);
	final GreasedRegion limit = new GreasedRegion(this.width, this.height).insertRectangle(1, 1, this.width - 2,
		this.height - 2), potential = new GreasedRegion(fusedMap, '#').and(limit);
	GreasedRegion flooded, chosen;
	final GreasedRegion tmp = new GreasedRegion(this.width, this.height);
	final int ctr = potential.size(), potentialMazeSize = ctr * mazeFill / 100,
		potentialLakeSize = ctr * lakeFill / 100;
	ArrayList<GreasedRegion> viable;
	int minSize;
	Coord center;
	boolean[][] deep;
	if (potentialMazeSize > 0) {
	    viable = potential.split();
	    if (viable.isEmpty()) {
		return maps;
	    }
	    chosen = viable.get(0);
	    minSize = chosen.size();
	    for (final GreasedRegion sa : viable) {
		final int sz = sa.size();
		if (sz > minSize) {
		    chosen = sa;
		    minSize = sz;
		}
	    }
	    final PacMazeGenerator pac = new PacMazeGenerator(this.width - this.width % 3,
		    this.height - this.height % 3, this.rng);
	    final char[][] pacMap = ArrayTools.insert(pac.generate(), ArrayTools.fill('#', this.width, this.height), 1,
		    1);
	    center = chosen.singleRandom(this.rng);
	    flooded = new GreasedRegion(center, this.width, this.height).spill(chosen, potentialMazeSize, this.rng)
		    .and(limit);
	    final GreasedRegion pacEnv = new GreasedRegion(pacMap, '.').and(flooded).removeIsolated();
	    deep = pacEnv.decode();
	    for (int x = 1; x < this.width - 1; x++) {
		for (int y = 1; y < this.height - 1; y++) {
		    if (deep[x][y]) {
			maps[1][x][y] = pacMap[x][y];
		    }
		}
	    }
	    this.finder.corridors.put(pacEnv, new ArrayList<GreasedRegion>());
	    this.finder.allCorridors.or(pacEnv);
	    this.finder.allFloors.or(pacEnv);
	    potential.andNot(flooded);
	}
	if (potentialLakeSize > 0) {
	    viable = potential.split();
	    if (viable.isEmpty()) {
		return maps;
	    }
	    chosen = viable.get(0);
	    minSize = chosen.size();
	    for (final GreasedRegion sa : viable) {
		final int sz = sa.size();
		if (sz > minSize) {
		    chosen = sa;
		    minSize = sz;
		}
	    }
	    center = chosen.singleRandom(this.rng);
	    flooded = new GreasedRegion(center, this.width, this.height).spill(chosen, potentialLakeSize, this.rng)
		    .and(limit);
	    deep = flooded.decode();
	    flooded.flood(new GreasedRegion(fusedMap, '.').fringe8way(3), 3).and(limit);
	    final boolean[][] shallow = flooded.decode();
	    for (int x = 0; x < this.width; x++) {
		for (int y = 0; y < this.height; y++) {
		    if (deep[x][y]) {
			maps[0][x][y] = this.deepLakeGlyph;
		    } else if (shallow[x][y]) {
			maps[0][x][y] = this.shallowLakeGlyph;
		    }
		}
	    }
	    final ArrayList<GreasedRegion> change = new ArrayList<>();
	    for (final GreasedRegion room : this.finder.rooms.keySet()) {
		if (flooded.intersects(tmp.remake(room).expand8way())) {
		    change.add(room);
		}
	    }
	    for (final GreasedRegion region : change) {
		this.finder.caves.put(region, this.finder.rooms.remove(region));
		this.finder.allRooms.andNot(region);
		this.finder.allCaves.or(region);
	    }
	}
	return maps;
    }

    protected char[][] innerGenerate(char[][] map, final EnumMap<FillEffect, Integer> fx) {
	final OrderedSet<Coord> hazards = new OrderedSet<>();
	int floorCount = DungeonUtility.countCells(map, '.'), waterFill = 0, grassFill = 0, trapFill = 0,
		boulderFill = 0, islandSpacing = 0;
	if (fx.containsKey(FillEffect.GRASS)) {
	    grassFill = fx.get(FillEffect.GRASS);
	}
	if (fx.containsKey(FillEffect.WATER)) {
	    waterFill = fx.get(FillEffect.WATER);
	}
	if (fx.containsKey(FillEffect.BOULDERS)) {
	    boulderFill = fx.get(FillEffect.BOULDERS) * floorCount / 100;
	}
	if (fx.containsKey(FillEffect.ISLANDS)) {
	    islandSpacing = fx.get(FillEffect.ISLANDS);
	}
	if (fx.containsKey(FillEffect.TRAPS)) {
	    trapFill = fx.get(FillEffect.TRAPS);
	}
	if (boulderFill > 0.0) {
	    /*
	     * short[] floor = pack(map, '.'); short[] viable = retract(floor, 1, width,
	     * height, true); ArrayList<Coord> boulders = randomPortion(viable, boulderFill,
	     * rng); for (Coord boulder : boulders) { map[boulder.x][boulder.y] = '#'; }
	     */
	    final Coord[] boulders = new GreasedRegion(map, '.').retract8way(1).randomPortion(this.rng, boulderFill);
	    Coord t;
	    for (final Coord boulder : boulders) {
		t = boulder;
		map[t.x][t.y] = '#';
	    }
	}
	if (trapFill > 0) {
	    for (int x = 1; x < map.length - 1; x++) {
		for (int y = 1; y < map[x].length - 1; y++) {
		    if (map[x][y] == '.') {
			int ctr = 0;
			if (map[x + 1][y] != '#') {
			    ++ctr;
			}
			if (map[x - 1][y] != '#') {
			    ++ctr;
			}
			if (map[x][y + 1] != '#') {
			    ++ctr;
			}
			if (map[x][y - 1] != '#') {
			    ++ctr;
			}
			if (map[x + 1][y + 1] != '#') {
			    ++ctr;
			}
			if (map[x - 1][y + 1] != '#') {
			    ++ctr;
			}
			if (map[x + 1][y - 1] != '#') {
			    ++ctr;
			}
			if (map[x - 1][y - 1] != '#') {
			    ++ctr;
			}
			if (ctr >= 5) {
			    hazards.add(Coord.get(x, y));
			}
		    }
		}
	    }
	}
	final GreasedRegion floors = new GreasedRegion(map, '.'), working = new GreasedRegion(this.width, this.height);
	floorCount = floors.size();
	float waterRate = waterFill / 100.0f, grassRate = grassFill / 100.0f;
	if (waterRate + grassRate > 1.0f) {
	    waterRate /= (waterFill + grassFill) / 100.0f;
	    grassRate /= (waterFill + grassFill) / 100.0f;
	}
	final int targetWater = Math.round(floorCount * waterRate), targetGrass = Math.round(floorCount * grassRate),
		sizeWaterPools = targetWater / this.rng.between(3, 6),
		sizeGrassPools = targetGrass / this.rng.between(2, 5);
	Coord[] scatter;
	int remainingWater = targetWater, remainingGrass = targetGrass;
	if (targetWater > 0) {
	    scatter = floors.quasiRandomSeparated(1.0 / 7.0);
	    this.rng.shuffleInPlace(scatter);
	    final GreasedRegion allWater = new GreasedRegion(this.width, this.height);
	    for (final Coord element : scatter) {
		if (remainingWater > 5) {
		    if (!floors.contains(element)) {
			continue;
		    }
		    working.empty().insert(element).spill(floors,
			    this.rng.between(4, Math.min(remainingWater, sizeWaterPools)), this.rng);
		    floors.andNot(working);
		    remainingWater -= working.size();
		    allWater.addAll(working);
		} else {
		    break;
		}
	    }
	    for (final Coord pt : allWater) {
		hazards.remove(pt);
		// obstacles.add(pt);
		if (map[pt.x][pt.y] != '<' && map[pt.x][pt.y] != '>') {
		    map[pt.x][pt.y] = '~';
		}
	    }
	    for (final Coord pt : allWater) {
		if (map[pt.x][pt.y] != '<' && map[pt.x][pt.y] != '>' && (map[pt.x - 1][pt.y] == '.'
			|| map[pt.x + 1][pt.y] == '.' || map[pt.x][pt.y - 1] == '.' || map[pt.x][pt.y + 1] == '.')) {
		    map[pt.x][pt.y] = ',';
		}
	    }
	}
	if (targetGrass > 0) {
	    scatter = floors.quasiRandomSeparated(1.03 / 6.7);
	    this.rng.shuffleInPlace(scatter);
	    for (final Coord element : scatter) {
		if (remainingGrass > 5) // remainingGrass >= targetGrass * 0.02 &&
		{
		    working.empty().insert(element).spill(floors,
			    this.rng.between(4, Math.min(remainingGrass, sizeGrassPools)), this.rng);
		    if (working.isEmpty()) {
			continue;
		    }
		    floors.andNot(working);
		    remainingGrass -= working.size();
		    map = working.inverseMask(map, '"');
		} else {
		    break;
		}
	    }
	}
	if (islandSpacing > 1 && targetWater > 0) {
	    final ArrayList<Coord> islands = PoissonDisk.sampleMap(map, 1f * islandSpacing, this.rng, '#', '.', '"',
		    '+', '/', '^', '<', '>');
	    for (final Coord c : islands) {
		map[c.x][c.y] = '.';
		if (map[c.x - 1][c.y] != '#' && map[c.x - 1][c.y] != '<' && map[c.x - 1][c.y] != '>') {
		    map[c.x - 1][c.y] = ',';
		}
		if (map[c.x + 1][c.y] != '#' && map[c.x + 1][c.y] != '<' && map[c.x + 1][c.y] != '>') {
		    map[c.x + 1][c.y] = ',';
		}
		if (map[c.x][c.y - 1] != '#' && map[c.x][c.y - 1] != '<' && map[c.x][c.y - 1] != '>') {
		    map[c.x][c.y - 1] = ',';
		}
		if (map[c.x][c.y + 1] != '#' && map[c.x][c.y + 1] != '<' && map[c.x][c.y + 1] != '>') {
		    map[c.x][c.y + 1] = ',';
		}
	    }
	}
	if (trapFill > 0) {
	    final int total = hazards.size() * trapFill / 100;
	    for (int i = 0; i < total; i++) {
		final Coord entry = this.rng.getRandomElement(hazards);
		if (map[entry.x][entry.y] == '<' || map[entry.x][entry.y] == '<') {
		    continue;
		}
		map[entry.x][entry.y] = '^';
		hazards.remove(entry);
	    }
	}
	return map;
    }

    /**
     * Gets the seed that can be used to rebuild an identical dungeon to the latest
     * one generated (or the seed that will be used to generate the first dungeon if
     * none has been made yet). You can pass the long this returns to the setState()
     * method on this class' rng field, which assuming all other calls to generate a
     * dungeon are identical, will ensure generate() or generateRespectingStairs()
     * will produce the same dungeon output as the dungeon originally generated with
     * the seed this returned. <br>
     * You can also call getState() on the rng field yourself immediately before
     * generating a dungeon, but this method handles some complexities of when the
     * state is actually used to generate a dungeon; since StatefulRNG objects can
     * be shared between different classes that use random numbers, the state could
     * change between when you call getState() and when this class generates a
     * dungeon. Using getRebuildSeed() eliminates that confusion.
     *
     * @return a seed as a long that can be passed to setState() on this class' rng
     *         field to recreate a dungeon
     */
    public long getRebuildSeed() {
	return this.rebuildSeed;
    }

    /**
     * Provides a string representation of the latest generated dungeon.
     *
     * @return a printable string version of the latest generated dungeon.
     */
    @Override
    public String toString() {
	final char[][] trans = new char[this.height][this.width];
	for (int x = 0; x < this.width; x++) {
	    for (int y = 0; y < this.height; y++) {
		trans[y][x] = this.dungeon[x][y];
	    }
	}
	final StringBuilder sb = new StringBuilder();
	for (int row = 0; row < this.height; row++) {
	    sb.append(trans[row]);
	    sb.append('\n');
	}
	return sb.toString();
    }
}
