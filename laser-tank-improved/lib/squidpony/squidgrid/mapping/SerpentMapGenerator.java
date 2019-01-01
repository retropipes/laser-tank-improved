package squidpony.squidgrid.mapping;

import java.util.ArrayList;
import java.util.List;

import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrthoLine;
import squidpony.squidmath.RNG;

/**
 * Generate dungeons based on a random, winding, looping path through 2D space.
 * Uses techniques from MixedGenerator. Uses a Moore Curve, which is related to
 * Hilbert Curves but loops back to its starting point, and stretches and
 * distorts the grid to make sure a visual correlation isn't obvious. This
 * supports the getEnvironment() method, which can be used in conjunction with
 * RoomFinder to find where separate room, corridor, and cave areas have been
 * placed. <br>
 * To get a sense of what kinds of map this generates, you can look at a sample
 * map on https://gist.github.com/tommyettinger/93b47048fc8a209a9712 , which
 * also includes a snippet of Java code that can generate that map. <br>
 * The name comes from a vivid dream I had about gigantic, multi-colored snakes
 * that completely occupied a roguelike dungeon. Shortly after, I made the
 * connection to the Australian mythology I'd heard about the Rainbow Serpent,
 * which in some stories dug water-holes and was similarly gigantic. Created by
 * Tommy Ettinger on 10/24/2015.
 */
public class SerpentMapGenerator implements IDungeonGenerator {
    private MixedGenerator mix;
    private final int[] columns, rows;
    private final RNG random;

    /**
     * This prepares a map generator that will generate a map with the given width
     * and height, using the given RNG. The intended purpose is to carve a long path
     * that loops through the whole dungeon, while hopefully maximizing the amount
     * of rooms the player encounters. You call the different carver-adding methods
     * to affect what the dungeon will look like, putCaveCarvers(),
     * putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only caves if
     * none are called. You call generate() after adding carvers, which returns a
     * char[][] for a map.
     *
     * @param width  the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng    an RNG object to use for random choices; this make a lot of
     *               random choices.
     * @see MixedGenerator
     */
    public SerpentMapGenerator(final int width, final int height, final RNG rng) {
	this(width, height, rng, false);
    }

    /**
     * This prepares a map generator that will generate a map with the given width
     * and height, using the given RNG. The intended purpose is to carve a long path
     * that loops through the whole dungeon, while hopefully maximizing the amount
     * of rooms the player encounters. You call the different carver-adding methods
     * to affect what the dungeon will look like, putCaveCarvers(),
     * putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only caves if
     * none are called. You call generate() after adding carvers, which returns a
     * char[][] for a map.
     *
     * @param width       the width of the final map in cells
     * @param height      the height of the final map in cells
     * @param rng         an RNG object to use for random choices; this make a lot
     *                    of random choices.
     * @param symmetrical true if this should generate a bi-radially symmetric map,
     *                    false for a typical map
     * @see MixedGenerator
     */
    public SerpentMapGenerator(final int width, final int height, final RNG rng, final boolean symmetrical) {
	if (width <= 2 || height <= 2) {
	    throw new IllegalArgumentException("width and height must be greater than 2");
	}
	this.random = rng;
	final long columnAlterations = this.random.nextLong(0x1000000000000L);
	final float columnBase = width / (Long.bitCount(columnAlterations) + 48.0f);
	final long rowAlterations = this.random.nextLong(0x1000000000000L);
	final float rowBase = height / (Long.bitCount(rowAlterations) + 48.0f);
	this.columns = new int[16];
	this.rows = new int[16];
	int csum = 0, rsum = 0;
	long b = 7;
	for (int i = 0; i < 16; i++, b <<= 3) {
	    this.columns[i] = csum + (int) (columnBase * 0.5f * (3 + Long.bitCount(columnAlterations & b)));
	    csum += (int) (columnBase * (3 + Long.bitCount(columnAlterations & b)));
	    this.rows[i] = rsum + (int) (rowBase * 0.5f * (3 + Long.bitCount(rowAlterations & b)));
	    rsum += (int) (rowBase * (3 + Long.bitCount(rowAlterations & b)));
	}
	final int cs = width - csum;
	final int rs = height - rsum;
	int cs2 = cs, rs2 = rs, cs3 = cs, rs3 = rs;
	for (int i = 0; i <= 7; i++) {
	    cs2 = cs2 * i / 7;
	    rs2 = rs2 * i / 7;
	    this.columns[i] -= cs2;
	    this.rows[i] -= rs2;
	}
	for (int i = 15; i >= 8; i--) {
	    cs3 = cs3 * (i - 8) / 8;
	    rs3 = rs3 * (i - 8) / 8;
	    this.columns[i] += cs3;
	    this.rows[i] += rs3;
	}
	final List<Coord> points = new ArrayList<>(80);
	Coord temp;
	for (int i = 0, m = this.random.nextInt(64), r; i < 256; r = this.random.between(4, 12), i += r, m += r) {
	    temp = CoordPacker.mooreToCoord(m);
	    points.add(Coord.get(this.columns[temp.x], this.rows[temp.y]));
	}
	points.add(points.get(0));
	if (symmetrical) {
	    this.mix = new SymmetryDungeonGenerator(width, height, this.random,
		    SymmetryDungeonGenerator.removeSomeOverlap(width, height, points));
	} else {
	    this.mix = new MixedGenerator(width, height, this.random, points);
	}
    }

    /**
     * This prepares a map generator that will generate a map with the given width
     * and height, using the given RNG. The intended purpose is to carve a long path
     * that loops through the whole dungeon, while hopefully maximizing the amount
     * of rooms the player encounters. You call the different carver-adding methods
     * to affect what the dungeon will look like, putCaveCarvers(),
     * putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only caves if
     * none are called. You call generate() after adding carvers, which returns a
     * char[][] for a map.
     *
     * @param width           the width of the final map in cells
     * @param height          the height of the final map in cells
     * @param rng             an RNG object to use for random choices; this make a
     *                        lot of random choices.
     * @param branchingChance the chance from 0.0 to 1.0 that each room will branch
     *                        at least once
     * @see MixedGenerator
     */
    public SerpentMapGenerator(final int width, final int height, final RNG rng, final double branchingChance) {
	this(width, height, rng, branchingChance, false);
    }

    /**
     * This prepares a map generator that will generate a map with the given width
     * and height, using the given RNG. The intended purpose is to carve a long path
     * that loops through the whole dungeon, while hopefully maximizing the amount
     * of rooms the player encounters. You call the different carver-adding methods
     * to affect what the dungeon will look like, putCaveCarvers(),
     * putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only caves if
     * none are called. You call generate() after adding carvers, which returns a
     * char[][] for a map.
     *
     * @param width           the width of the final map in cells
     * @param height          the height of the final map in cells
     * @param rng             an RNG object to use for random choices; this make a
     *                        lot of random choices.
     * @param branchingChance the chance from 0.0 to 1.0 that each room will branch
     *                        at least once
     * @param symmetrical     true if this should generate a bi-radially symmetric
     *                        map, false for a typical map
     * @see MixedGenerator
     */
    public SerpentMapGenerator(final int width, final int height, final RNG rng, final double branchingChance,
	    final boolean symmetrical) {
	if (width <= 2 || height <= 2) {
	    throw new IllegalArgumentException("width and height must be greater than 2");
	}
	this.random = rng;
	final long columnAlterations = this.random.nextLong(0x1000000000000L);
	final float columnBase = width / (Long.bitCount(columnAlterations) + 48.0f);
	final long rowAlterations = this.random.nextLong(0x1000000000000L);
	final float rowBase = height / (Long.bitCount(rowAlterations) + 48.0f);
	this.columns = new int[16];
	this.rows = new int[16];
	int csum = 0, rsum = 0;
	long b = 7;
	for (int i = 0; i < 16; i++, b <<= 3) {
	    this.columns[i] = csum + (int) (columnBase * 0.5f * (3 + Long.bitCount(columnAlterations & b)));
	    csum += (int) (columnBase * (3 + Long.bitCount(columnAlterations & b)));
	    this.rows[i] = rsum + (int) (rowBase * 0.5f * (3 + Long.bitCount(rowAlterations & b)));
	    rsum += (int) (rowBase * (3 + Long.bitCount(rowAlterations & b)));
	}
	final int cs = width - csum;
	final int rs = height - rsum;
	int cs2 = cs, rs2 = rs, cs3 = cs, rs3 = rs;
	for (int i = 0; i <= 7; i++) {
	    cs2 = cs2 * i / 7;
	    rs2 = rs2 * i / 7;
	    this.columns[i] -= cs2;
	    this.rows[i] -= rs2;
	}
	for (int i = 15; i >= 8; i--) {
	    cs3 = cs3 * (i - 8) / 8;
	    rs3 = rs3 * (i - 8) / 8;
	    this.columns[i] += cs3;
	    this.rows[i] += rs3;
	}
	final OrderedMap<Coord, List<Coord>> connections = new OrderedMap<>(80);
	Coord temp, t;
	int m = this.random.nextInt(64), r = this.random.between(4, 12);
	temp = CoordPacker.mooreToCoord(m);
	final Coord starter = CoordPacker.mooreToCoord(m);
	m += r;
	for (int i = r; i < 256; i += r, m += r) {
	    final List<Coord> cl = new ArrayList<>(4);
	    cl.add(Coord.get(this.columns[temp.x], this.rows[temp.y]));
	    temp = CoordPacker.mooreToCoord(m);
	    r = this.random.between(4, 12);
	    for (int j = 0, p = r - 1; j < 3 && p > 2 && Math.min(this.random.nextDouble(),
		    this.random.nextDouble()) < branchingChance; j++, p -= this.random.between(1, p)) {
		t = CoordPacker.mooreToCoord(m + p);
		cl.add(Coord.get(this.columns[t.x], this.rows[t.y]));
	    }
	    connections.put(Coord.get(this.columns[temp.x], this.rows[temp.y]), cl);
	}
	connections.get(Coord.get(this.columns[temp.x], this.rows[temp.y]))
		.add(Coord.get(this.columns[starter.x], this.rows[starter.y]));
	if (symmetrical) {
	    this.mix = new SymmetryDungeonGenerator(width, height, this.random,
		    SymmetryDungeonGenerator.removeSomeOverlap(width, height, connections));
	} else {
	    this.mix = new MixedGenerator(width, height, this.random, connections);
	}
    }

    /**
     * Changes the number of "carvers" that will create caves from one room to the
     * next. If count is 0 or less, no caves will be made. If count is at least 1,
     * caves are possible, and higher numbers relative to the other carvers make
     * caves more likely. Carvers are shuffled when used, then repeat if exhausted
     * during generation. Since typically about 30-40 rooms are carved, large totals
     * for carver count aren't really needed; aiming for a total of 10 between the
     * count of putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers() is
     * reasonable.
     *
     * @param count the number of carvers making caves between rooms; only matters
     *              in relation to other carvers
     * @see MixedGenerator
     */
    public void putCaveCarvers(final int count) {
	this.mix.putCaveCarvers(count);
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from
     * one room to the next, create rooms with a random size in a box shape at the
     * start and end, and a small room at the corner if there is one. If count is 0
     * or less, no box-shaped rooms will be made. If count is at least 1, box-shaped
     * rooms are possible, and higher numbers relative to the other carvers make
     * box-shaped rooms more likely. Carvers are shuffled when used, then repeat if
     * exhausted during generation. Since typically about 30-40 rooms are carved,
     * large totals for carver count aren't really needed; aiming for a total of 10
     * between the count of putCaveCarvers(), putBoxRoomCarvers(), and
     * putRoundRoomCarvers() is reasonable.
     *
     * @param count the number of carvers making box-shaped rooms and corridors
     *              between them; only matters in relation to other carvers
     * @see MixedGenerator
     */
    public void putBoxRoomCarvers(final int count) {
	this.mix.putBoxRoomCarvers(count);
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from
     * one room to the next, create rooms with a random size in a box shape at the
     * start and end, and a small room at the corner if there is one. This also
     * ensures walls will be placed around the room, only allowing corridors and
     * small cave openings to pass. If count is 0 or less, no box-shaped rooms will
     * be made. If count is at least 1, box-shaped rooms are possible, and higher
     * numbers relative to the other carvers make box-shaped rooms more likely.
     * Carvers are shuffled when used, then repeat if exhausted during generation.
     * Since typically about 30-40 rooms are carved, large totals for carver count
     * aren't really needed; aiming for a total of 10 between the count of
     * putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers() is
     * reasonable.
     *
     * @param count the number of carvers making box-shaped rooms and corridors
     *              between them; only matters in relation to other carvers
     * @see MixedGenerator
     */
    public void putWalledBoxRoomCarvers(final int count) {
	this.mix.putWalledBoxRoomCarvers(count);
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from
     * one room to the next, create rooms with a random size in a circle shape at
     * the start and end, and a small circular room at the corner if there is one.
     * If count is 0 or less, no circular rooms will be made. If count is at least
     * 1, circular rooms are possible, and higher numbers relative to the other
     * carvers make circular rooms more likely. Carvers are shuffled when used, then
     * repeat if exhausted during generation. Since typically about 30-40 rooms are
     * carved, large totals for carver count aren't really needed; aiming for a
     * total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(), and
     * putRoundRoomCarvers() is reasonable.
     *
     * @param count the number of carvers making circular rooms and corridors
     *              between them; only matters in relation to other carvers
     * @see MixedGenerator
     */
    public void putRoundRoomCarvers(final int count) {
	this.mix.putRoundRoomCarvers(count);
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from
     * one room to the next, create rooms with a random size in a circle shape at
     * the start and end, and a small circular room at the corner if there is one.
     * This also ensures walls will be placed around the room, only allowing
     * corridors and small cave openings to pass. If count is 0 or less, no circular
     * rooms will be made. If count is at least 1, circular rooms are possible, and
     * higher numbers relative to the other carvers make circular rooms more likely.
     * Carvers are shuffled when used, then repeat if exhausted during generation.
     * Since typically about 30-40 rooms are carved, large totals for carver count
     * aren't really needed; aiming for a total of 10 between the count of
     * putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers() is
     * reasonable.
     *
     * @param count the number of carvers making circular rooms and corridors
     *              between them; only matters in relation to other carvers
     * @see MixedGenerator
     */
    public void putWalledRoundRoomCarvers(final int count) {
	this.mix.putWalledRoundRoomCarvers(count);
    }

    /**
     * This generates a new map by stretching a 16x16 grid of potential rooms to fit
     * the width and height passed to the constructor, randomly expanding columns
     * and rows before contracting the whole to fit perfectly. This uses the Moore
     * Curve, a space-filling curve that loops around on itself, to guarantee that
     * the rooms will always have a long path through the dungeon that, if followed
     * completely, will take you back to your starting room. Some small branches are
     * possible, and large rooms may merge with other rooms nearby. This uses
     * MixedGenerator.
     *
     * @return a char[][] where '#' is a wall and '.' is a floor or corridor; x
     *         first y second
     * @see MixedGenerator
     */
    @Override
    public char[][] generate() {
	return this.mix.generate();
    }

    /**
     * Gets a 2D array of int constants, each representing a type of environment
     * corresponding to a static field of MixedGenerator. This array will have the
     * same size as the last char 2D array produced by generate(); the value of this
     * method if called before generate() is undefined, but probably will be a 2D
     * array of all 0 (UNTOUCHED).
     * <ul>
     * <li>MixedGenerator.UNTOUCHED, equal to 0, is used for any cells that aren't
     * near a floor.</li>
     * <li>MixedGenerator.ROOM_FLOOR, equal to 1, is used for floor cells inside
     * wide room areas.</li>
     * <li>MixedGenerator.ROOM_WALL, equal to 2, is used for wall cells around wide
     * room areas.</li>
     * <li>MixedGenerator.CAVE_FLOOR, equal to 3, is used for floor cells inside
     * rough cave areas.</li>
     * <li>MixedGenerator.CAVE_WALL, equal to 4, is used for wall cells around rough
     * cave areas.</li>
     * <li>MixedGenerator.CORRIDOR_FLOOR, equal to 5, is used for floor cells inside
     * narrow corridor areas.</li>
     * <li>MixedGenerator.CORRIDOR_WALL, equal to 6, is used for wall cells around
     * narrow corridor areas.</li>
     * </ul>
     *
     * @return a 2D int array where each element is an environment type constant in
     *         MixedGenerator
     */
    public int[][] getEnvironment() {
	return this.mix.getEnvironment();
    }

    @Override
    public char[][] getDungeon() {
	return this.mix.getDungeon();
    }

    public static ArrayList<Coord> pointPath(final int width, final int height, final RNG rng) {
	if (width <= 2 || height <= 2) {
	    throw new IllegalArgumentException("width and height must be greater than 2");
	}
	final long columnAlterations = rng.nextLong(0x1000000000000L);
	final float columnBase = width / (Long.bitCount(columnAlterations) + 48.0f);
	final long rowAlterations = rng.nextLong(0x1000000000000L);
	final float rowBase = height / (Long.bitCount(rowAlterations) + 48.0f);
	final int[] columns = new int[16], rows = new int[16];
	int csum = 0, rsum = 0;
	long b = 7;
	for (int i = 0; i < 16; i++, b <<= 3) {
	    columns[i] = csum + (int) (columnBase * 0.5f * (3 + Long.bitCount(columnAlterations & b)));
	    csum += (int) (columnBase * (3 + Long.bitCount(columnAlterations & b)));
	    rows[i] = rsum + (int) (rowBase * 0.5f * (3 + Long.bitCount(rowAlterations & b)));
	    rsum += (int) (rowBase * (3 + Long.bitCount(rowAlterations & b)));
	}
	final int cs = width - csum;
	final int rs = height - rsum;
	int cs2 = cs, rs2 = rs, cs3 = cs, rs3 = rs;
	for (int i = 0; i <= 7; i++) {
	    cs2 = cs2 * i / 7;
	    rs2 = rs2 * i / 7;
	    columns[i] -= cs2;
	    rows[i] -= rs2;
	}
	for (int i = 15; i >= 8; i--) {
	    cs3 = cs3 * (i - 8) / 8;
	    rs3 = rs3 * (i - 8) / 8;
	    columns[i] += cs3;
	    rows[i] += rs3;
	}
	final ArrayList<Coord> points = new ArrayList<>(80);
	int m = rng.nextInt(64);
	Coord temp = CoordPacker.mooreToCoord(m), next;
	temp = Coord.get(columns[temp.x], rows[temp.y]);
	for (int i = 0, r; i < 256; r = rng.between(4, 12), i += r, m += r) {
	    next = CoordPacker.mooreToCoord(m);
	    next = Coord.get(columns[next.x], rows[next.y]);
	    points.addAll(OrthoLine.line(temp, next));
	    temp = next;
	}
	points.add(points.get(0));
	return points;
    }
}
