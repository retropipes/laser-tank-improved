package squidpony.squidgrid.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import squidpony.ArrayTools;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.mapping.locks.Edge;
import squidpony.squidgrid.mapping.locks.IRoomLayout;
import squidpony.squidgrid.mapping.locks.Room;
import squidpony.squidgrid.mapping.locks.util.Rect2I;
import squidpony.squidmath.Coord;
import squidpony.squidmath.IntVLA;
import squidpony.squidmath.PoissonDisk;
import squidpony.squidmath.RNG;
import squidpony.squidmath.VanDerCorputQRNG;

/**
 * A dungeon generator that can use a mix of techniques to have part-cave,
 * part-room dungeons. Not entirely intended for normal use outside of this
 * library, though it can be very useful when you want to make a dungeon match a
 * specific path and existing generators that use MixedGenerator aren't
 * sufficient. You may want to use a simpler generator based on this, like
 * SerpentMapGenerator, which generates a long, winding path that loops around
 * on itself. This supports the getEnvironment() method, which can be used in
 * conjunction with RoomFinder to find where separate room, corridor, and cave
 * areas have been placed. <br>
 * Based on Michael Patraw's excellent Drunkard's Walk dungeon generator.
 * http://mpatraw.github.io/libdrunkard/
 *
 * @see squidpony.squidgrid.mapping.SerpentMapGenerator a normal use for
 *      MixedGenerator that makes winding dungeons
 * @see squidpony.squidgrid.mapping.SerpentDeepMapGenerator uses MixedGenerator
 *      as it makes a multi-level dungeon Created by Tommy Ettinger on
 *      10/22/2015.
 */
public class MixedGenerator implements IDungeonGenerator {
    public enum CarverType {
	CAVE, BOX, ROUND, BOX_WALLED, ROUND_WALLED
    }

    /**
     * Constant for environment tiles that are not near a cave, room, or corridor.
     * Value is 0.
     */
    public static final int UNTOUCHED = 0;
    /**
     * Constant for environment tiles that are floors for a room. Value is 1.
     */
    public static final int ROOM_FLOOR = 1;
    /**
     * Constant for environment tiles that are walls near a room. Value is 2.
     */
    public static final int ROOM_WALL = 2;
    /**
     * Constant for environment tiles that are floors for a cave. Value is 3.
     */
    public static final int CAVE_FLOOR = 3;
    /**
     * Constant for environment tiles that are walls near a cave. Value is 4.
     */
    public static final int CAVE_WALL = 4;
    /**
     * Constant for environment tiles that are floors for a corridor. Value is 5.
     */
    public static final int CORRIDOR_FLOOR = 5;
    /**
     * Constant for environment tiles that are walls near a corridor. Value is 6.
     */
    public static final int CORRIDOR_WALL = 6;
    protected EnumMap<CarverType, Integer> carvers;
    protected int width, height;
    protected float roomWidth, roomHeight;
    public RNG rng;
    protected char[][] dungeon;
    protected boolean generated = false;
    protected int[][] environment;
    protected boolean[][] marked, walled, fixedRooms;
    protected IntVLA points;
    protected int totalPoints;

    /**
     * Mainly for internal use; this can be used with
     * {@link #MixedGenerator(int, int, RNG)} to get its room positions. This was
     * the default for generating a List of Coord if no other collection of Coord
     * was supplied to the constructor, but it has been swapped out for
     * {@link #cleanPoints(int, int, RNG)}, which produces a cleaner layout of rooms
     * with less overlap. If you want the exact old behavior while only supplying a
     * width and height as ints as well as an RNG, construct a MixedGenerator with
     * {@code new MixedGenerator(width, height, rng, basicPoints(width, height, rng))}.
     * <br>
     * <a href=
     * "https://gist.githubusercontent.com/tommyettinger/be0ed51858cb492bc7e8cda43a04def1/raw/dae9d8e4f45dd3a3577bdd5f58b419ea5f9ed570/PoissonDungeon.txt">Preview
     * map.</a>
     *
     * @param width  dungeon width in cells
     * @param height dungeon height in cells
     * @param rng    rng to use
     * @return evenly spaced Coord points in a list made by PoissonDisk, trimmed
     *         down so they aren't all used
     * @see PoissonDisk used to make the list
     */
    public static List<Coord> basicPoints(final int width, final int height, final RNG rng) {
	return PoissonDisk.sampleRectangle(Coord.get(2, 2), Coord.get(width - 3, height - 3),
		8.5f * (width + height) / 120f, width, height, 35, rng);
    }

    /**
     * Mainly for internal use; this is used by
     * {@link #MixedGenerator(int, int, RNG)} to get its room positions. It produces
     * a cleaner layout of rooms that should have less overlap between rooms and
     * corridors; a good approach is to favor {@link #putWalledBoxRoomCarvers(int)}
     * and {@link #putWalledRoundRoomCarvers(int)} more than you might otherwise
     * consider in place of caves, since caves may be larger than you would expect
     * here. The exact technique used here is to get points from a Halton-like
     * sequence, formed using {@link VanDerCorputQRNG} to get a van der Corput
     * sequence, for the x axis and a scrambled van der Corput sequence for the y
     * axis. MixedGenerator will connect these points in pairs. The current method
     * is much better at avoiding "clumps" of closely-positioned rooms in the center
     * of the map. <br>
     * <a href=
     * "https://gist.githubusercontent.com/tommyettinger/2745e6fc16fc2acebe2fc959fb4e4c2e/raw/77e1f4dbc844d8892c1a686754535c02cadaa270/TenDungeons.txt">Preview
     * maps, with and without box drawing characters.</a>
     *
     * @param width  dungeon width in cells
     * @param height dungeon height in cells
     * @param rng    rng to use
     * @return erratically-positioned but generally separated Coord points to pass
     *         to a MixedGenerator constructor
     * @see VanDerCorputQRNG used to get separated positions
     */
    public static List<Coord> cleanPoints(int width, int height, final RNG rng) {
	width -= 2;
	height -= 2;
	final float mx = rng.nextFloat() * 0.2f, my = rng.nextFloat() * 0.2f;
	int index = 9 + rng.next(4);
	final int sz = width * height / 157;
	// System.out.println("mx: " + mx + ", my: " + my + ", index: " + index);
	final List<Coord> list = new ArrayList<>(sz);
	list.add(Coord.get((int) ((VanDerCorputQRNG.determine2(index) + mx) % 1.0 * width + 1),
		(int) ((VanDerCorputQRNG.determine2_scrambled(index - 1) + my) % 1.0 * height + 1)));
	for (int i = 0; i < sz; i++) {
	    list.add(Coord.get((int) ((VanDerCorputQRNG.determine2(++index) + mx) % 1.0 * width + 1),
		    (int) ((VanDerCorputQRNG.determine2_scrambled(index - 1) + my) % 1.0 * height + 1)));
	}
	return list;
    }

    /**
     * This prepares a map generator that will generate a map with the given width
     * and height, using the given RNG. This version of the constructor uses Poisson
     * Disk sampling to generate the points it will draw caves and corridors
     * between, ensuring a minimum distance between points, but it does not ensure
     * that paths between points will avoid overlapping with rooms or other paths.
     * You call the different carver-adding methods to affect what the dungeon will
     * look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(),
     * defaulting to only caves if none are called. You call generate() after adding
     * carvers, which returns a char[][] for a map.
     *
     * @param width  the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng    an RNG object to use for random choices; this make a lot of
     *               random choices.
     * @see PoissonDisk used to ensure spacing for the points.
     */
    public MixedGenerator(final int width, final int height, final RNG rng) {
	this(width, height, rng, MixedGenerator.cleanPoints(width, height, rng));
    }

    /**
     * This prepares a map generator that will generate a map with the given width
     * and height, using the given RNG. This version of the constructor uses a List
     * of Coord points from some other source to determine the path to add rooms or
     * caves to and then connect. You call the different carver-adding methods to
     * affect what the dungeon will look like, putCaveCarvers(),
     * putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only caves if
     * none are called. You call generate() after adding carvers, which returns a
     * char[][] for a map.
     *
     * @param width    the width of the final map in cells
     * @param height   the height of the final map in cells
     * @param rng      an RNG object to use for random choices; this make a lot of
     *                 random choices.
     * @param sequence a List of Coord to connect in order; index 0 is the start,
     *                 index size() - 1 is the end.
     * @see SerpentMapGenerator a class that uses this technique
     */
    public MixedGenerator(final int width, final int height, final RNG rng, final List<Coord> sequence) {
	this.width = width;
	this.height = height;
	this.roomWidth = width / 64.0f;
	this.roomHeight = height / 64.0f;
	if (width <= 2 || height <= 2) {
	    throw new IllegalStateException("width and height must be greater than 2");
	}
	this.rng = rng;
	this.dungeon = new char[width][height];
	this.environment = new int[width][height];
	this.marked = new boolean[width][height];
	this.walled = new boolean[width][height];
	this.fixedRooms = new boolean[width][height];
	Arrays.fill(this.dungeon[0], '#');
	Arrays.fill(this.environment[0], MixedGenerator.UNTOUCHED);
	for (int i = 1; i < width; i++) {
	    System.arraycopy(this.dungeon[0], 0, this.dungeon[i], 0, height);
	    System.arraycopy(this.environment[0], 0, this.environment[i], 0, height);
	}
	this.totalPoints = sequence.size() - 1;
	this.points = new IntVLA(this.totalPoints);
	for (int i = 0; i < this.totalPoints; i++) {
	    final Coord c1 = sequence.get(i), c2 = sequence.get(i + 1);
	    this.points.add((c1.x & 0xff) << 24 | (c1.y & 0xff) << 16 | (c2.x & 0xff) << 8 | c2.y & 0xff);
	}
	this.carvers = new EnumMap<>(CarverType.class);
    }

    /**
     * This prepares a map generator that will generate a map with the given width
     * and height, using the given RNG. This version of the constructor uses a Map
     * with Coord keys and Coord array values to determine a branching path for the
     * dungeon to take; each key will connect once to each of the Coords in its
     * value, and you usually don't want to connect in both directions. You call the
     * different carver-adding methods to affect what the dungeon will look like,
     * putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting
     * to only caves if none are called. You call generate() after adding carvers,
     * which returns a char[][] for a map.
     *
     * @param width       the width of the final map in cells
     * @param height      the height of the final map in cells
     * @param rng         an RNG object to use for random choices; this make a lot
     *                    of random choices.
     * @param connections a Map of Coord keys to arrays of Coord to connect to next;
     *                    shouldn't connect both ways
     * @see SerpentMapGenerator a class that uses this technique
     */
    public MixedGenerator(final int width, final int height, final RNG rng, final Map<Coord, List<Coord>> connections) {
	this(width, height, rng, connections, 0.8f);
    }

    /**
     * This prepares a map generator that will generate a map with the given width
     * and height, using the given RNG. This version of the constructor uses a Map
     * with Coord keys and Coord array values to determine a branching path for the
     * dungeon to take; each key will connect once to each of the Coords in its
     * value, and you usually don't want to connect in both directions. You call the
     * different carver-adding methods to affect what the dungeon will look like,
     * putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting
     * to only caves if none are called. You call generate() after adding carvers,
     * which returns a char[][] for a map.
     *
     * @param width              the width of the final map in cells
     * @param height             the height of the final map in cells
     * @param rng                an RNG object to use for random choices; this make
     *                           a lot of random choices.
     * @param connections        a Map of Coord keys to arrays of Coord to connect
     *                           to next; shouldn't connect both ways
     * @param roomSizeMultiplier a float multiplier that will be applied to each
     *                           room's width and height
     * @see SerpentMapGenerator a class that uses this technique
     */
    public MixedGenerator(final int width, final int height, final RNG rng, final Map<Coord, List<Coord>> connections,
	    final float roomSizeMultiplier) {
	this.width = width;
	this.height = height;
	this.roomWidth = width / 64.0f * roomSizeMultiplier;
	this.roomHeight = height / 64.0f * roomSizeMultiplier;
	if (width <= 2 || height <= 2) {
	    throw new IllegalStateException("width and height must be greater than 2");
	}
	this.rng = rng;
	this.dungeon = new char[width][height];
	this.environment = new int[width][height];
	this.marked = new boolean[width][height];
	this.walled = new boolean[width][height];
	this.fixedRooms = new boolean[width][height];
	Arrays.fill(this.dungeon[0], '#');
	Arrays.fill(this.environment[0], MixedGenerator.UNTOUCHED);
	for (int i = 1; i < width; i++) {
	    System.arraycopy(this.dungeon[0], 0, this.dungeon[i], 0, height);
	    System.arraycopy(this.environment[0], 0, this.environment[i], 0, height);
	}
	this.totalPoints = 0;
	for (final List<Coord> vals : connections.values()) {
	    this.totalPoints += vals.size();
	}
	this.points = new IntVLA(this.totalPoints);
	for (final Map.Entry<Coord, List<Coord>> kv : connections.entrySet()) {
	    final Coord c1 = kv.getKey();
	    for (final Coord c2 : kv.getValue()) {
		this.points.add((c1.x & 0xff) << 24 | (c1.y & 0xff) << 16 | (c2.x & 0xff) << 8 | c2.y & 0xff);
	    }
	}
	this.carvers = new EnumMap<>(CarverType.class);
    }

    /**
     * This prepares a map generator that will generate a map with the given width
     * and height, using the given RNG. This version of the constructor uses an
     * {@link squidpony.squidgrid.mapping.locks.IRoomLayout} to set up rooms, almost
     * always produced by
     * {@link squidpony.squidgrid.mapping.locks.generators.LayoutGenerator}. This
     * method does alter the individual Room objects inside layout, making the
     * center of each room match where that center is placed in the dungeon this
     * generates. You call the different carver-adding methods to affect what the
     * dungeon will look like, i.e. {@link #putCaveCarvers(int)},
     * {@link #putBoxRoomCarvers(int)} , {@link #putRoundRoomCarvers(int)},
     * {@link #putWalledBoxRoomCarvers(int)}, and
     * {@link #putWalledRoundRoomCarvers(int)}, defaulting to only caves if none are
     * called (using rooms is recommended for this constructor). You call generate()
     * after adding carvers, which returns a char[][] for a map and sets the
     * environment to be fetched with {@link #getEnvironment()}, which is usually
     * needed for {@link SectionDungeonGenerator} to correctly place doors and
     * various other features.
     *
     * @param width              the width of the final map in cells
     * @param height             the height of the final map in cells
     * @param rng                an RNG object to use for random choices; this make
     *                           a lot of random choices.
     * @param layout             an IRoomLayout that will almost always be produced
     *                           by LayoutGenerator; the rooms will be altered
     * @param roomSizeMultiplier a float multiplier that will be applied to each
     *                           room's width and height
     * @see SerpentMapGenerator a class that uses this technique
     */
    public MixedGenerator(final int width, final int height, final RNG rng, final IRoomLayout layout,
	    final float roomSizeMultiplier) {
	this.width = width;
	this.height = height;
	final Rect2I bounds = layout.getExtentBounds();
	final int offX = bounds.getBottomLeft().x, offY = bounds.getBottomLeft().y;
	final float rw = width / (bounds.width + 1f), rh = height / (bounds.height + 1f);
	this.roomWidth = roomSizeMultiplier * rw * 0.125f;
	this.roomHeight = roomSizeMultiplier * rh * 0.125f;
	if (width <= 2 || height <= 2) {
	    throw new IllegalStateException("width and height must be greater than 2");
	}
	this.rng = rng;
	this.dungeon = new char[width][height];
	this.environment = new int[width][height];
	this.marked = new boolean[width][height];
	this.walled = new boolean[width][height];
	this.fixedRooms = new boolean[width][height];
	ArrayTools.fill(this.dungeon, '#');
	ArrayTools.fill(this.environment, MixedGenerator.UNTOUCHED);
	this.totalPoints = layout.roomCount();
	this.points = new IntVLA(this.totalPoints);
	Coord c2;
	final Set<Room> rooms = layout.getRooms(), removing = new HashSet<>(rooms);
	Room t;
	for (final Room room : rooms) {
	    final Coord c1 = room.getCenter();
	    if (!bounds.contains(c1)) {
		removing.remove(room);
	    } else {
		room.setCenter(Coord.get((int) ((c1.x - offX + 0.75f) * rw) & 0xff,
			(int) ((c1.y - offY + 0.75f) * rh) & 0xff));
	    }
	}
	for (final Room room : rooms) {
	    final Coord c1 = room.getCenter();
	    for (final Edge e : room.getEdges()) {
		if (removing.contains(t = layout.get(e.getTargetRoomId()))) {
		    c2 = t.getCenter();
		    this.points.add(c1.x << 24 | c1.y << 16 | c2.x << 8 | c2.y);
		}
	    }
	    removing.remove(room);
	}
	this.totalPoints = this.points.size;
	this.carvers = new EnumMap<>(CarverType.class);
    }

    /**
     * Changes the number of "carvers" that will create caves from one room to the
     * next. If count is 0 or less, no caves will be made. If count is at least 1,
     * caves are possible, and higher numbers relative to the other carvers make
     * caves more likely. Carvers are shuffled when used, then repeat if exhausted
     * during generation. Since typically about 30-40 rooms are carved, large totals
     * for carver count aren't really needed; aiming for a total of 10 between the
     * count of putCaveCarvers(), putBoxRoomCarvers(), putRoundRoomCarvers(),
     * putWalledBoxRoomCarvers(), and putWalledRoundRoomCarvers() is reasonable.
     *
     * @param count the number of carvers making caves between rooms; only matters
     *              in relation to other carvers
     */
    public void putCaveCarvers(final int count) {
	this.carvers.put(CarverType.CAVE, count);
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
     * between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and
     * putWalledRoundRoomCarvers() is reasonable.
     *
     * @param count the number of carvers making box-shaped rooms and corridors
     *              between them; only matters in relation to other carvers
     */
    public void putBoxRoomCarvers(final int count) {
	this.carvers.put(CarverType.BOX, count);
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
     * total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and
     * putWalledRoundRoomCarvers() is reasonable.
     *
     * @param count the number of carvers making circular rooms and corridors
     *              between them; only matters in relation to other carvers
     */
    public void putRoundRoomCarvers(final int count) {
	this.carvers.put(CarverType.ROUND, count);
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from
     * one room to the next, create rooms with a random size in a box shape at the
     * start and end, and a small room at the corner if there is one, enforcing the
     * presence of walls around the rooms even if another room is already there or
     * would be placed there. Corridors can always pass through enforced walls, but
     * caves will open at most one cell in the wall. If count is 0 or less, no
     * box-shaped rooms will be made. If count is at least 1, box-shaped rooms are
     * possible, and higher numbers relative to the other carvers make box-shaped
     * rooms more likely. Carvers are shuffled when used, then repeat if exhausted
     * during generation. Since typically about 30-40 rooms are carved, large totals
     * for carver count aren't really needed; aiming for a total of 10 between the
     * count of putCaveCarvers(), putBoxRoomCarvers(), putRoundRoomCarvers(),
     * putWalledBoxRoomCarvers(), and putWalledRoundRoomCarvers() is reasonable.
     *
     * @param count the number of carvers making box-shaped rooms and corridors
     *              between them; only matters in relation to other carvers
     */
    public void putWalledBoxRoomCarvers(final int count) {
	this.carvers.put(CarverType.BOX_WALLED, count);
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from
     * one room to the next, create rooms with a random size in a circle shape at
     * the start and end, and a small circular room at the corner if there is one,
     * enforcing the presence of walls around the rooms even if another room is
     * already there or would be placed there. Corridors can always pass through
     * enforced walls, but caves will open at most one cell in the wall. If count is
     * 0 or less, no circular rooms will be made. If count is at least 1, circular
     * rooms are possible, and higher numbers relative to the other carvers make
     * circular rooms more likely. Carvers are shuffled when used, then repeat if
     * exhausted during generation. Since typically about 30-40 rooms are carved,
     * large totals for carver count aren't really needed; aiming for a total of 10
     * between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and
     * putWalledRoundRoomCarvers() is reasonable.
     *
     * @param count the number of carvers making circular rooms and corridors
     *              between them; only matters in relation to other carvers
     */
    public void putWalledRoundRoomCarvers(final int count) {
	this.carvers.put(CarverType.ROUND_WALLED, count);
    }

    /**
     * Uses the added carvers (or just makes caves if none were added) to carve from
     * point to point in sequence, if it was provided by the constructor, or
     * evenly-spaced randomized points if it was not. This will never carve out
     * cells on the very edge of the map. Uses the numbers of the various kinds of
     * carver that were added relative to each other to determine how frequently to
     * use a given carver type.
     *
     * @return a char[][] where '#' is a wall and '.' is a floor or corridor; x
     *         first y second
     */
    @Override
    public char[][] generate() {
	final CarverType[] carvings = this.carvers.keySet().toArray(new CarverType[this.carvers.size()]);
	final int[] carvingsCounters = new int[carvings.length];
	int totalLength = 0;
	for (int i = 0; i < carvings.length; i++) {
	    carvingsCounters[i] = this.carvers.get(carvings[i]);
	    totalLength += carvingsCounters[i];
	}
	CarverType[] allCarvings = new CarverType[totalLength];
	for (int i = 0, c = 0; i < carvings.length; i++) {
	    for (int j = 0; j < carvingsCounters[i]; j++) {
		allCarvings[c++] = carvings[i];
	    }
	}
	if (allCarvings.length == 0) {
	    allCarvings = new CarverType[] { CarverType.CAVE };
	    totalLength = 1;
	} else {
	    allCarvings = this.rng.shuffle(allCarvings, new CarverType[allCarvings.length]);
	}
	for (int p = 0, c = 0; p < this.totalPoints; p++, c = (c + 1) % totalLength) {
	    final int pair = this.points.get(p);
	    Coord start = Coord.get(pair >>> 24 & 0xff, pair >>> 16 & 0xff);
	    final Coord end = Coord.get(pair >>> 8 & 0xff, pair & 0xff);
	    final CarverType ct = allCarvings[c];
	    Direction dir;
	    switch (ct) {
	    case CAVE:
		this.markPiercing(end);
		this.markEnvironmentCave(end.x, end.y);
		this.store();
		double weight = 0.75;
		do {
		    final Coord cent = this.markPlusCave(start);
		    if (cent != null) {
			this.markPiercingCave(cent);
			this.markPiercingCave(cent.translate(1, 0));
			this.markPiercingCave(cent.translate(-1, 0));
			this.markPiercingCave(cent.translate(0, 1));
			this.markPiercingCave(cent.translate(0, -1));
			weight = 0.95;
		    }
		    dir = this.stepWobbly(start, end, weight);
		    start = start.translate(dir);
		} while (dir != Direction.NONE);
		break;
	    case BOX:
		this.markRectangle(end, this.rng.between(1, 5), this.rng.between(1, 5));
		this.markRectangle(start, this.rng.between(1, 4), this.rng.between(1, 4));
		this.store();
		dir = Direction.getDirection(end.x - start.x, end.y - start.y);
		if (dir.isDiagonal()) {
		    dir = this.rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
			    : Direction.getCardinalDirection(0, -dir.deltaY);
		}
		while (start.x != end.x && start.y != end.y) {
		    this.markPiercing(start);
		    this.markEnvironmentCorridor(start.x, start.y);
		    start = start.translate(dir);
		}
		this.markRectangle(start, 1, 1);
		dir = Direction.getCardinalDirection(end.x - start.x, -(end.y - start.y));
		while (!(start.x == end.x && start.y == end.y)) {
		    this.markPiercing(start);
		    this.markEnvironmentCorridor(start.x, start.y);
		    start = start.translate(dir);
		}
		break;
	    case BOX_WALLED:
		this.markRectangleWalled(end, this.rng.between(1, 5), this.rng.between(1, 5));
		this.markRectangleWalled(start, this.rng.between(1, 4), this.rng.between(1, 4));
		this.store();
		dir = Direction.getDirection(end.x - start.x, end.y - start.y);
		if (dir.isDiagonal()) {
		    dir = this.rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
			    : Direction.getCardinalDirection(0, -dir.deltaY);
		}
		while (start.x != end.x && start.y != end.y) {
		    this.markPiercing(start);
		    this.markEnvironmentCorridor(start.x, start.y);
		    start = start.translate(dir);
		}
		this.markRectangleWalled(start, 1, 1);
		dir = Direction.getCardinalDirection(end.x - start.x, -(end.y - start.y));
		while (!(start.x == end.x && start.y == end.y)) {
		    this.markPiercing(start);
		    this.markEnvironmentCorridor(start.x, start.y);
		    start = start.translate(dir);
		}
		break;
	    case ROUND:
		this.markCircle(end, this.rng.between(2, 6));
		this.markCircle(start, this.rng.between(2, 6));
		this.store();
		dir = Direction.getDirection(end.x - start.x, end.y - start.y);
		if (dir.isDiagonal()) {
		    dir = this.rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
			    : Direction.getCardinalDirection(0, -dir.deltaY);
		}
		while (start.x != end.x && start.y != end.y) {
		    this.markPiercing(start);
		    this.markEnvironmentCorridor(start.x, start.y);
		    start = start.translate(dir);
		}
		this.markCircle(start, 2);
		dir = Direction.getCardinalDirection(end.x - start.x, -(end.y - start.y));
		while (!(start.x == end.x && start.y == end.y)) {
		    this.markPiercing(start);
		    this.markEnvironmentCorridor(start.x, start.y);
		    start = start.translate(dir);
		}
		break;
	    case ROUND_WALLED:
		this.markCircleWalled(end, this.rng.between(2, 6));
		this.markCircleWalled(start, this.rng.between(2, 6));
		this.store();
		dir = Direction.getDirection(end.x - start.x, end.y - start.y);
		if (dir.isDiagonal()) {
		    dir = this.rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
			    : Direction.getCardinalDirection(0, -dir.deltaY);
		}
		while (start.x != end.x && start.y != end.y) {
		    this.markPiercing(start);
		    this.markEnvironmentCorridor(start.x, start.y);
		    start = start.translate(dir);
		}
		this.markCircleWalled(start, 2);
		dir = Direction.getCardinalDirection(end.x - start.x, -(end.y - start.y));
		while (!(start.x == end.x && start.y == end.y)) {
		    this.markPiercing(start);
		    this.markEnvironmentCorridor(start.x, start.y);
		    start = start.translate(dir);
		}
		break;
	    }
	    this.store();
	}
	for (int x = 0; x < this.width; x++) {
	    for (int y = 0; y < this.height; y++) {
		if (this.fixedRooms[x][y]) {
		    this.markPiercingRoom(x, y);
		}
	    }
	}
	this.store();
	this.markEnvironmentWalls();
	this.generated = true;
	return this.dungeon;
    }

    @Override
    public char[][] getDungeon() {
	return this.dungeon;
    }

    public int[][] getEnvironment() {
	return this.environment;
    }

    public boolean hasGenerated() {
	return this.generated;
    }

    public boolean[][] getFixedRooms() {
	return this.fixedRooms;
    }

    public void setFixedRooms(final boolean[][] fixedRooms) {
	this.fixedRooms = fixedRooms;
    }

    /**
     * Internal use. Takes cells that have been previously marked and permanently
     * stores them as floors in the dungeon.
     */
    protected void store() {
	for (int i = 0; i < this.width; i++) {
	    for (int j = 0; j < this.height; j++) {
		if (this.marked[i][j]) {
		    this.dungeon[i][j] = '.';
		    this.marked[i][j] = false;
		}
	    }
	}
    }

    /**
     * Internal use. Finds all floor cells by environment and marks untouched
     * adjacent (8-way) cells as walls, using the appropriate type for the nearby
     * floor.
     */
    protected void markEnvironmentWalls() {
	for (int i = 0; i < this.width; i++) {
	    for (int j = 0; j < this.height; j++) {
		if (this.environment[i][j] == MixedGenerator.UNTOUCHED) {
		    boolean allWalls = true;
		    // lowest precedence, also checks for any floors
		    for (int x = Math.max(0, i - 1); x <= Math.min(this.width - 1, i + 1); x++) {
			for (int y = Math.max(0, j - 1); y <= Math.min(this.height - 1, j + 1); y++) {
			    if (this.environment[x][y] == MixedGenerator.CORRIDOR_FLOOR) {
				this.markEnvironment(i, j, MixedGenerator.CORRIDOR_WALL);
			    }
			    if (this.dungeon[x][y] == '.') {
				allWalls = false;
			    }
			}
		    }
		    // if there are no floors we don't need to check twice again.
		    if (allWalls) {
			continue;
		    }
		    // more precedence
		    for (int x = Math.max(0, i - 1); x <= Math.min(this.width - 1, i + 1); x++) {
			for (int y = Math.max(0, j - 1); y <= Math.min(this.height - 1, j + 1); y++) {
			    if (this.environment[x][y] == MixedGenerator.CAVE_FLOOR) {
				this.markEnvironment(i, j, MixedGenerator.CAVE_WALL);
			    }
			}
		    }
		    // highest precedence
		    for (int x = Math.max(0, i - 1); x <= Math.min(this.width - 1, i + 1); x++) {
			for (int y = Math.max(0, j - 1); y <= Math.min(this.height - 1, j + 1); y++) {
			    if (this.environment[x][y] == MixedGenerator.ROOM_FLOOR) {
				this.markEnvironment(i, j, MixedGenerator.ROOM_WALL);
			    }
			}
		    }
		}
	    }
	}
    }

    /**
     * Internal use. Marks a point to be made into floor.
     *
     * @param x x position to mark
     * @param y y position to mark
     * @return false if everything is normal, true if and only if this failed to
     *         mark because the position is walled
     */
    protected boolean mark(final int x, final int y) {
	if (x > 0 && x < this.width - 1 && y > 0 && y < this.height - 1 && !this.walled[x][y]) {
	    this.marked[x][y] = true;
	    return false;
	} else {
	    return x > 0 && x < this.width - 1 && y > 0 && y < this.height - 1 && this.walled[x][y];
	}
    }

    /**
     * Internal use. Marks a point to be made into floor.
     *
     * @param x x position to mark
     * @param y y position to mark
     */
    protected void markPiercing(final int x, final int y) {
	if (x > 0 && x < this.width - 1 && y > 0 && y < this.height - 1) {
	    this.marked[x][y] = true;
	}
    }

    /**
     * Internal use. Marks a point's environment type as the appropriate kind of
     * environment.
     *
     * @param x    x position to mark
     * @param y    y position to mark
     * @param kind an int that should be one of the constants in MixedGenerator for
     *             environment types.
     */
    protected void markEnvironment(final int x, final int y, final int kind) {
	this.environment[x][y] = kind;
    }

    /**
     * Internal use. Marks a point's environment type as a corridor floor.
     *
     * @param x x position to mark
     * @param y y position to mark
     */
    protected void markEnvironmentCorridor(final int x, final int y) {
	if (x > 0 && x < this.width - 1 && y > 0 && y < this.height - 1
		&& this.environment[x][y] != MixedGenerator.ROOM_FLOOR
		&& this.environment[x][y] != MixedGenerator.CAVE_FLOOR) {
	    this.markEnvironment(x, y, MixedGenerator.CORRIDOR_FLOOR);
	}
    }

    /**
     * Internal use. Marks a point's environment type as a room floor.
     *
     * @param x x position to mark
     * @param y y position to mark
     */
    protected void markEnvironmentRoom(final int x, final int y) {
	if (x > 0 && x < this.width - 1 && y > 0 && y < this.height - 1) {
	    this.markEnvironment(x, y, MixedGenerator.ROOM_FLOOR);
	}
    }

    /**
     * Internal use. Marks a point's environment type as a cave floor.
     *
     * @param x x position to mark
     * @param y y position to mark
     */
    protected void markEnvironmentCave(final int x, final int y) {
	if (x > 0 && x < this.width - 1 && y > 0 && y < this.height - 1
		&& this.environment[x][y] != MixedGenerator.ROOM_FLOOR) {
	    this.markEnvironment(x, y, MixedGenerator.CAVE_FLOOR);
	}
    }

    /**
     * Internal use. Marks a point to be made into floor.
     *
     * @param x x position to mark
     * @param y y position to mark
     */
    protected void wallOff(final int x, final int y) {
	if (x > 0 && x < this.width - 1 && y > 0 && y < this.height - 1) {
	    this.walled[x][y] = true;
	}
    }

    /**
     * Internal use. Marks a point to be made into floor.
     *
     * @param pos position to mark
     * @return false if everything is normal, true if and only if this failed to
     *         mark because the position is walled
     */
    protected boolean mark(final Coord pos) {
	return this.mark(pos.x, pos.y);
    }

    /**
     * Internal use. Marks a point to be made into floor, piercing walls.
     *
     * @param pos position to mark
     */
    protected void markPiercing(final Coord pos) {
	this.markPiercing(pos.x, pos.y);
    }

    /**
     * Internal use. Marks a point to be made into floor, piercing walls, and also
     * marks the point as a cave floor.
     *
     * @param pos position to mark
     */
    protected void markPiercingCave(final Coord pos) {
	this.markPiercing(pos.x, pos.y);
	this.markEnvironmentCave(pos.x, pos.y);
    }

    /**
     * Internal use. Marks a point to be made into floor, piercing walls, and also
     * marks the point as a room floor.
     *
     * @param x x coordinate of position to mark
     * @param y y coordinate of position to mark
     */
    protected void markPiercingRoom(final int x, final int y) {
	this.markPiercing(x, y);
	this.markEnvironmentCave(x, y);
    }

    /**
     * Internal use. Marks a point and the four cells orthogonally adjacent to it,
     * and also marks any cells that weren't blocked as cave floors.
     *
     * @param pos center position to mark
     * @return null if the center of the plus shape wasn't blocked by wall,
     *         otherwise the Coord of the center
     */
    private Coord markPlusCave(final Coord pos) {
	Coord block = null;
	if (this.mark(pos.x, pos.y)) {
	    block = pos;
	} else {
	    this.markEnvironmentCave(pos.x, pos.y);
	}
	if (!this.mark(pos.x + 1, pos.y)) {
	    this.markEnvironmentCave(pos.x + 1, pos.y);
	}
	if (!this.mark(pos.x - 1, pos.y)) {
	    this.markEnvironmentCave(pos.x - 1, pos.y);
	}
	if (!this.mark(pos.x, pos.y + 1)) {
	    this.markEnvironmentCave(pos.x, pos.y + 1);
	}
	if (!this.mark(pos.x, pos.y - 1)) {
	    this.markEnvironmentCave(pos.x, pos.y - 1);
	}
	return block;
    }

    /**
     * Internal use. Marks a rectangle of points centered on pos, extending
     * halfWidth in both x directions and halfHeight in both vertical directions.
     * Marks all cells in the rectangle as room floors.
     *
     * @param pos        center position to mark
     * @param halfWidth  the distance from the center to extend horizontally
     * @param halfHeight the distance from the center to extend vertically
     * @return null if no points in the rectangle were blocked by walls, otherwise a
     *         Coord blocked by a wall
     */
    private Coord markRectangle(final Coord pos, int halfWidth, int halfHeight) {
	halfWidth = Math.max(1, Math.round(halfWidth * this.roomWidth));
	halfHeight = Math.max(1, Math.round(halfHeight * this.roomHeight));
	Coord block = null;
	for (int i = pos.x - halfWidth; i <= pos.x + halfWidth; i++) {
	    for (int j = pos.y - halfHeight; j <= pos.y + halfHeight; j++) {
		if (this.mark(i, j)) {
		    block = Coord.get(i, j);
		} else {
		    this.markEnvironmentRoom(i, j);
		}
	    }
	}
	return block;
    }

    /**
     * Internal use. Marks a rectangle of points centered on pos, extending
     * halfWidth in both x directions and halfHeight in both vertical directions.
     * Also considers the area just beyond each wall, but not corners, to be a
     * blocking wall that can only be passed by corridors and small cave openings.
     * Marks all cells in the rectangle as room floors.
     *
     * @param pos        center position to mark
     * @param halfWidth  the distance from the center to extend horizontally
     * @param halfHeight the distance from the center to extend vertically
     * @return null if no points in the rectangle were blocked by walls, otherwise a
     *         Coord blocked by a wall
     */
    private Coord markRectangleWalled(final Coord pos, int halfWidth, int halfHeight) {
	halfWidth = Math.max(1, Math.round(halfWidth * this.roomWidth));
	halfHeight = Math.max(1, Math.round(halfHeight * this.roomHeight));
	Coord block = null;
	for (int i = pos.x - halfWidth; i <= pos.x + halfWidth; i++) {
	    for (int j = pos.y - halfHeight; j <= pos.y + halfHeight; j++) {
		if (this.mark(i, j)) {
		    block = Coord.get(i, j);
		} else {
		    this.markEnvironmentRoom(i, j);
		}
	    }
	}
	for (int i = Math.max(0, pos.x - halfWidth - 1); i <= Math.min(this.width - 1, pos.x + halfWidth + 1); i++) {
	    for (int j = Math.max(0, pos.y - halfHeight - 1); j <= Math.min(this.height - 1,
		    pos.y + halfHeight + 1); j++) {
		this.wallOff(i, j);
	    }
	}
	return block;
    }

    /**
     * Internal use. Marks a circle of points centered on pos, extending out to
     * radius in Euclidean measurement. Marks all cells in the circle as room
     * floors.
     *
     * @param pos    center position to mark
     * @param radius radius to extend in all directions from center
     * @return null if no points in the circle were blocked by walls, otherwise a
     *         Coord blocked by a wall
     */
    private Coord markCircle(final Coord pos, int radius) {
	Coord block = null;
	int high;
	radius = Math.max(1, Math.round(radius * Math.min(this.roomWidth, this.roomHeight)));
	for (int dx = -radius; dx <= radius; ++dx) {
	    high = (int) Math.floor(Math.sqrt(radius * radius - dx * dx));
	    for (int dy = -high; dy <= high; ++dy) {
		if (this.mark(pos.x + dx, pos.y + dy)) {
		    block = pos.translate(dx, dy);
		} else {
		    this.markEnvironmentRoom(pos.x + dx, pos.y + dy);
		}
	    }
	}
	return block;
    }

    /**
     * Internal use. Marks a circle of points centered on pos, extending out to
     * radius in Euclidean measurement. Also considers the area just beyond each
     * wall, but not corners, to be a blocking wall that can only be passed by
     * corridors and small cave openings. Marks all cells in the circle as room
     * floors.
     *
     * @param pos    center position to mark
     * @param radius radius to extend in all directions from center
     * @return null if no points in the circle were blocked by walls, otherwise a
     *         Coord blocked by a wall
     */
    private Coord markCircleWalled(final Coord pos, int radius) {
	Coord block = null;
	int high;
	radius = Math.max(1, Math.round(radius * Math.min(this.roomWidth, this.roomHeight)));
	for (int dx = -radius; dx <= radius; ++dx) {
	    high = (int) Math.floor(Math.sqrt(radius * radius - dx * dx));
	    for (int dy = -high; dy <= high; ++dy) {
		if (this.mark(pos.x + dx, pos.y + dy)) {
		    block = pos.translate(dx, dy);
		} else {
		    this.markEnvironmentRoom(pos.x + dx, pos.y + dy);
		}
	    }
	}
	for (int dx = -radius; dx <= radius; ++dx) {
	    high = (int) Math.floor(Math.sqrt(radius * radius - dx * dx));
	    final int dx2 = Math.max(1, Math.min(pos.x + dx, this.width - 2));
	    for (int dy = -high; dy <= high; ++dy) {
		final int dy2 = Math.max(1, Math.min(pos.y + dy, this.height - 2));
		this.wallOff(dx2, dy2 - 1);
		this.wallOff(dx2 + 1, dy2 - 1);
		this.wallOff(dx2 - 1, dy2 - 1);
		this.wallOff(dx2, dy2);
		this.wallOff(dx2 + 1, dy2);
		this.wallOff(dx2 - 1, dy2);
		this.wallOff(dx2, dy2 + 1);
		this.wallOff(dx2 + 1, dy2 + 1);
		this.wallOff(dx2 - 1, dy2 + 1);
	    }
	}
	return block;
    }

    /**
     * Internal use. Drunkard's walk algorithm, single step. Based on Michael
     * Patraw's C code, used for cave carving. http://mpatraw.github.io/libdrunkard/
     *
     * @param current the current point
     * @param target  the point to wobble towards
     * @param weight  between 0.5 and 1.0, usually. 0.6 makes very random caves, 0.9
     *                is almost a straight line.
     * @return a Direction, either UP, DOWN, LEFT, or RIGHT if we should move, or
     *         NONE if we have reached our target
     */
    private Direction stepWobbly(final Coord current, final Coord target, final double weight) {
	int dx = target.x - current.x;
	int dy = target.y - current.y;
	if (dx > 1) {
	    dx = 1;
	}
	if (dx < -1) {
	    dx = -1;
	}
	if (dy > 1) {
	    dy = 1;
	}
	if (dy < -1) {
	    dy = -1;
	}
	double r = this.rng.nextDouble();
	Direction dir;
	if (dx == 0 && dy == 0) {
	    return Direction.NONE;
	} else if (dx == 0 || dy == 0) {
	    int dx2 = dx == 0 ? dx : dy, dy2 = dx == 0 ? dy : dx;
	    if (r >= weight * 0.5) {
		r -= weight * 0.5;
		if (r < weight * (1.0 / 6) + (1 - weight) * (1.0 / 3)) {
		    dx2 = -1;
		    dy2 = 0;
		} else if (r < weight * (2.0 / 6) + (1 - weight) * (2.0 / 3)) {
		    dx2 = 1;
		    dy2 = 0;
		} else {
		    dx2 = 0;
		    dy2 *= -1;
		}
	    }
	    dir = Direction.getCardinalDirection(dx2, -dy2);
	} else {
	    if (r < weight * 0.5) {
		dy = 0;
	    } else if (r < weight) {
		dx = 0;
	    } else if (r < weight + (1 - weight) * 0.5) {
		dx *= -1;
		dy = 0;
	    } else {
		dx = 0;
		dy *= -1;
	    }
	    dir = Direction.getCardinalDirection(dx, -dy);
	}
	if (current.x + dir.deltaX <= 0 || current.x + dir.deltaX >= this.width - 1) {
	    if (current.y < target.y) {
		dir = Direction.DOWN;
	    } else if (current.y > target.y) {
		dir = Direction.UP;
	    }
	} else if (current.y + dir.deltaY <= 0 || current.y + dir.deltaY >= this.height - 1) {
	    if (current.x < target.x) {
		dir = Direction.RIGHT;
	    } else if (current.x > target.x) {
		dir = Direction.LEFT;
	    }
	}
	return dir;
    }
}
