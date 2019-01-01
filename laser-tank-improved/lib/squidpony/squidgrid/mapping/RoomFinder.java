package squidpony.squidgrid.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import squidpony.ArrayTools;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

//import static squidpony.squidmath.CoordPacker.*;
/**
 * A small class that can analyze a dungeon or other map and identify areas as
 * being "room" or "corridor" based on how thick the walkable areas are
 * (corridors are at most 2 cells wide at their widest, rooms are anything
 * else). Most methods of this class return 2D char arrays or Lists thereof,
 * with the subset of the map that is in a specific region kept the same, but
 * everything else replaced with '#'. Created by Tommy Ettinger on 2/3/2016.
 *
 * @see RectangleRoomFinder A simpler but faster alternative
 */
public class RoomFinder {
    /**
     * A copy of the dungeon map, however it was passed to the constructor.
     */
    public char[][] map,
	    /**
	     * A simplified version of the dungeon map, using '#' for walls and '.' for
	     * floors.
	     */
	    basic;
    public int[][] environment;
    /**
     * Not likely to be used directly, but there may be things you can do with these
     * that are cumbersome using only RoomFinder's simpler API.
     */
    public OrderedMap<GreasedRegion, List<GreasedRegion>> rooms,
	    /**
	     * Not likely to be used directly, but there may be things you can do with these
	     * that are cumbersome using only RoomFinder's simpler API.
	     */
	    corridors,
	    /**
	     * Not likely to be used directly, but there may be things you can do with these
	     * that are cumbersome using only RoomFinder's simpler API. Won't be assigned a
	     * value if this class is constructed with a 2D char array; it needs the two-arg
	     * constructor using the environment produced by a MixedGenerator,
	     * SerpentMapGenerator, or similar.
	     */
	    caves;
    public GreasedRegion allRooms, allCaves, allCorridors, allFloors;
    /**
     * When a RoomFinder is constructed, it stores all points of rooms that are
     * adjacent to another region here.
     */
    public Coord[] connections,
	    /**
	     * Potential doorways, where a room is adjacent to a corridor.
	     */
	    doorways,
	    /**
	     * Cave mouths, where a cave is adjacent to another type of terrain.
	     */
	    mouths;
    public int width, height;

    /**
     * Constructs a RoomFinder given a dungeon map, and finds rooms, corridors, and
     * their connections on the map. Does not find caves; if a collection of caves
     * is requested from this, it will be non-null but empty.
     *
     * @param dungeon a 2D char array that uses '#', box drawing characters, or ' '
     *                for walls.
     */
    public RoomFinder(final char[][] dungeon) {
	if (dungeon.length <= 0) {
	    return;
	}
	this.width = dungeon.length;
	this.height = dungeon[0].length;
	this.map = new char[this.width][this.height];
	this.environment = new int[this.width][this.height];
	for (int i = 0; i < this.width; i++) {
	    System.arraycopy(dungeon[i], 0, this.map[i], 0, this.height);
	}
	this.rooms = new OrderedMap<>(32);
	this.corridors = new OrderedMap<>(32);
	this.caves = new OrderedMap<>(8);
	this.basic = DungeonUtility.simplifyDungeon(this.map);
	this.allFloors = new GreasedRegion(this.basic, '.');
	this.allRooms = this.allFloors.copy().retract8way().flood(this.allFloors, 2);
	this.allCorridors = this.allFloors.copy().andNot(this.allRooms);
	this.environment = this.allCorridors.writeInts(
		this.allRooms.writeInts(this.environment, MixedGenerator.ROOM_FLOOR), MixedGenerator.CORRIDOR_FLOOR);
	this.allCaves = new GreasedRegion(this.width, this.height);
	final GreasedRegion d = this.allCorridors.copy().fringe().and(this.allRooms);
	this.connections = this.doorways = d.asCoords();
	this.mouths = new Coord[0];
	final List<GreasedRegion> rs = this.allRooms.split(), cs = this.allCorridors.split();
	for (final GreasedRegion sep : cs) {
	    final GreasedRegion someDoors = sep.copy().fringe().and(this.allRooms);
	    final Coord[] doors = someDoors.asCoords();
	    final List<GreasedRegion> near = new ArrayList<>(4);
	    for (final Coord door : doors) {
		near.addAll(GreasedRegion.whichContain(door.x, door.y, rs));
	    }
	    this.corridors.put(sep, near);
	}
	for (final GreasedRegion sep : rs) {
	    final GreasedRegion aroundDoors = sep.copy().fringe().and(this.allCorridors);
	    final Coord[] doors = aroundDoors.asCoords();
	    final List<GreasedRegion> near = new ArrayList<>(10);
	    for (final Coord door : doors) {
		near.addAll(GreasedRegion.whichContain(door.x, door.y, cs));
	    }
	    this.rooms.put(sep, near);
	}
    }

    /**
     * Constructs a RoomFinder given a dungeon map and a general kind of environment
     * for the whole map, then finds rooms, corridors, and their connections on the
     * map. Defaults to treating all areas as cave unless {@code environmentKind} is
     * {@code MixedGenerator.ROOM_FLOOR} (or its equivalent, 1).
     *
     * @param dungeon         a 2D char array that uses '#', box drawing characters,
     *                        or ' ' for walls.
     * @param environmentKind if 1 ({@code MixedGenerator.ROOM_FLOOR}), this will
     *                        find rooms and corridors, else caves
     */
    public RoomFinder(final char[][] dungeon, final int environmentKind) {
	if (dungeon.length <= 0) {
	    return;
	}
	this.width = dungeon.length;
	this.height = dungeon[0].length;
	this.map = new char[this.width][this.height];
	this.environment = new int[this.width][this.height];
	for (int i = 0; i < this.width; i++) {
	    System.arraycopy(dungeon[i], 0, this.map[i], 0, this.height);
	}
	this.rooms = new OrderedMap<>(32);
	this.corridors = new OrderedMap<>(32);
	this.caves = new OrderedMap<>(8);
	this.basic = DungeonUtility.simplifyDungeon(this.map);
	if (environmentKind == MixedGenerator.ROOM_FLOOR) {
	    this.allFloors = new GreasedRegion(this.basic, '.');
	    this.allRooms = this.allFloors.copy().retract8way().flood(this.allFloors, 2);
	    this.allCorridors = this.allFloors.copy().andNot(this.allRooms);
	    this.allCaves = new GreasedRegion(this.width, this.height);
	    this.environment = this.allCorridors.writeInts(
		    this.allRooms.writeInts(this.environment, MixedGenerator.ROOM_FLOOR),
		    MixedGenerator.CORRIDOR_FLOOR);
	    final GreasedRegion d = this.allCorridors.copy().fringe().and(this.allRooms);
	    this.connections = this.doorways = d.asCoords();
	    this.mouths = new Coord[0];
	    final List<GreasedRegion> rs = this.allRooms.split(), cs = this.allCorridors.split();
	    for (final GreasedRegion sep : cs) {
		final GreasedRegion someDoors = sep.copy().fringe().and(this.allRooms);
		final Coord[] doors = someDoors.asCoords();
		final List<GreasedRegion> near = new ArrayList<>(4);
		for (final Coord door : doors) {
		    near.addAll(GreasedRegion.whichContain(door.x, door.y, rs));
		}
		this.corridors.put(sep, near);
	    }
	    for (final GreasedRegion sep : rs) {
		final GreasedRegion aroundDoors = sep.copy().fringe().and(this.allCorridors);
		final Coord[] doors = aroundDoors.asCoords();
		final List<GreasedRegion> near = new ArrayList<>(10);
		for (final Coord door : doors) {
		    near.addAll(GreasedRegion.whichContain(door.x, door.y, cs));
		}
		this.rooms.put(sep, near);
	    }
	} else {
	    this.allCaves = new GreasedRegion(this.basic, '.');
	    this.allFloors = new GreasedRegion(this.width, this.height);
	    this.allRooms = new GreasedRegion(this.width, this.height);
	    this.allCorridors = new GreasedRegion(this.width, this.height);
	    this.caves.put(this.allCaves, new ArrayList<GreasedRegion>());
	    this.connections = this.mouths = this.allCaves.copy().andNot(this.allCaves.copy().retract8way()).retract()
		    .asCoords();
	    this.doorways = new Coord[0];
	    this.environment = this.allCaves.writeInts(this.environment, MixedGenerator.CAVE_FLOOR);
	}
    }

    /**
     * Constructs a RoomFinder given a dungeon map and an environment map (which
     * currently is only produced by MixedGenerator by the getEnvironment() method
     * after generate() is called, but other classes that use MixedGenerator may
     * also expose that environment, such as SerpentMapGenerator.getEnvironment()),
     * and finds rooms, corridors, caves, and their connections on the map.
     *
     * @param dungeon     a 2D char array that uses '#' for walls.
     * @param environment a 2D int array using constants from MixedGenerator;
     *                    typically produced by a call to getEnvironment() in
     *                    MixedGenerator or SerpentMapGenerator after dungeon
     *                    generation.
     */
    public RoomFinder(final char[][] dungeon, final int[][] environment) {
	if (dungeon.length <= 0) {
	    return;
	}
	this.width = dungeon.length;
	this.height = dungeon[0].length;
	this.map = new char[this.width][this.height];
	this.environment = ArrayTools.copy(environment);
	for (int i = 0; i < this.width; i++) {
	    System.arraycopy(dungeon[i], 0, this.map[i], 0, this.height);
	}
	this.rooms = new OrderedMap<>(32);
	this.corridors = new OrderedMap<>(32);
	this.caves = new OrderedMap<>(32);
	this.basic = DungeonUtility.simplifyDungeon(this.map);
	this.allFloors = new GreasedRegion(this.basic, '.');
	this.allRooms = new GreasedRegion(environment, MixedGenerator.ROOM_FLOOR);
	this.allCorridors = new GreasedRegion(environment, MixedGenerator.CORRIDOR_FLOOR);
	this.allCaves = new GreasedRegion(environment, MixedGenerator.CAVE_FLOOR);
	final GreasedRegion d = this.allCorridors.copy().fringe().and(this.allRooms),
		m = this.allCaves.copy().fringe().and(this.allRooms.copy().or(this.allCorridors));
	this.doorways = d.asCoords();
	this.mouths = m.asCoords();
	this.connections = new Coord[this.doorways.length + this.mouths.length];
	System.arraycopy(this.doorways, 0, this.connections, 0, this.doorways.length);
	System.arraycopy(this.mouths, 0, this.connections, this.doorways.length, this.mouths.length);
	final List<GreasedRegion> rs = this.allRooms.split(), cs = this.allCorridors.split(),
		vs = this.allCaves.split();
	for (final GreasedRegion sep : cs) {
	    final GreasedRegion someDoors = sep.copy().fringe().and(this.allRooms);
	    Coord[] doors = someDoors.asCoords();
	    final List<GreasedRegion> near = new ArrayList<>(16);
	    for (final Coord door : doors) {
		near.addAll(GreasedRegion.whichContain(door.x, door.y, rs));
	    }
	    someDoors.remake(sep).fringe().and(this.allCaves);
	    doors = someDoors.asCoords();
	    for (final Coord door : doors) {
		near.addAll(GreasedRegion.whichContain(door.x, door.y, vs));
	    }
	    this.corridors.put(sep, near);
	}
	for (final GreasedRegion sep : rs) {
	    final GreasedRegion aroundDoors = sep.copy().fringe().and(this.allCorridors);
	    Coord[] doors = aroundDoors.asCoords();
	    final List<GreasedRegion> near = new ArrayList<>(32);
	    for (final Coord door : doors) {
		near.addAll(GreasedRegion.whichContain(door.x, door.y, cs));
	    }
	    aroundDoors.remake(sep).fringe().and(this.allCaves);
	    doors = aroundDoors.asCoords();
	    for (final Coord door : doors) {
		near.addAll(GreasedRegion.whichContain(door.x, door.y, vs));
	    }
	    this.rooms.put(sep, near);
	}
	for (final GreasedRegion sep : vs) {
	    final GreasedRegion aroundMouths = sep.copy().fringe().and(this.allCorridors);
	    Coord[] maws = aroundMouths.asCoords();
	    final List<GreasedRegion> near = new ArrayList<>(48);
	    for (final Coord maw : maws) {
		near.addAll(GreasedRegion.whichContain(maw.x, maw.y, cs));
	    }
	    aroundMouths.remake(sep).fringe().and(this.allRooms);
	    maws = aroundMouths.asCoords();
	    for (final Coord maw : maws) {
		near.addAll(GreasedRegion.whichContain(maw.x, maw.y, rs));
	    }
	    this.caves.put(sep, near);
	}
    }

    /**
     * Gets all the rooms this found during construction, returning them as an
     * ArrayList of 2D char arrays, where an individual room is "masked" so only its
     * contents have normal map chars and the rest have only '#'.
     *
     * @return an ArrayList of 2D char arrays representing rooms.
     */
    public ArrayList<char[][]> findRooms() {
	final ArrayList<char[][]> rs = new ArrayList<>(this.rooms.size());
	for (final GreasedRegion r : this.rooms.keySet()) {
	    rs.add(r.mask(this.map, '#'));
	}
	return rs;
    }

    /**
     * Gets all the corridors this found during construction, returning them as an
     * ArrayList of 2D char arrays, where an individual corridor is "masked" so only
     * its contents have normal map chars and the rest have only '#'.
     *
     * @return an ArrayList of 2D char arrays representing corridors.
     */
    public ArrayList<char[][]> findCorridors() {
	final ArrayList<char[][]> cs = new ArrayList<>(this.corridors.size());
	for (final GreasedRegion c : this.corridors.keySet()) {
	    cs.add(c.mask(this.map, '#'));
	}
	return cs;
    }

    /**
     * Gets all the caves this found during construction, returning them as an
     * ArrayList of 2D char arrays, where an individual room is "masked" so only its
     * contents have normal map chars and the rest have only '#'. Will only return a
     * non-empty collection if the two-arg constructor was used and the environment
     * contains caves.
     *
     * @return an ArrayList of 2D char arrays representing caves.
     */
    public ArrayList<char[][]> findCaves() {
	final ArrayList<char[][]> vs = new ArrayList<>(this.caves.size());
	for (final GreasedRegion v : this.caves.keySet()) {
	    vs.add(v.mask(this.map, '#'));
	}
	return vs;
    }

    /**
     * Gets all the rooms, corridors, and caves this found during construction,
     * returning them as an ArrayList of 2D char arrays, where an individual room or
     * corridor is "masked" so only its contents have normal map chars and the rest
     * have only '#'.
     *
     * @return an ArrayList of 2D char arrays representing rooms, corridors, or
     *         caves.
     */
    public ArrayList<char[][]> findRegions() {
	final ArrayList<char[][]> rs = new ArrayList<>(this.rooms.size() + this.corridors.size() + this.caves.size());
	for (final GreasedRegion r : this.rooms.keySet()) {
	    rs.add(r.mask(this.map, '#'));
	}
	for (final GreasedRegion c : this.corridors.keySet()) {
	    rs.add(c.mask(this.map, '#'));
	}
	for (final GreasedRegion v : this.caves.keySet()) {
	    rs.add(v.mask(this.map, '#'));
	}
	return rs;
    }

    private static char[][] defaultFill(final int width, final int height) {
	final char[][] d = new char[width][height];
	for (int x = 0; x < width; x++) {
	    Arrays.fill(d[x], '#');
	}
	return d;
    }

    /**
     * Merges multiple 2D char arrays where the '#' character means "no value", and
     * combines them so all cells with value are on one map, with '#' filling any
     * other cells. If regions is empty, this uses width and height to construct a
     * blank map, all '#'. It will also use width and height for the size of the
     * returned 2D array.
     *
     * @param regions An ArrayList of 2D char array regions, where '#' is an empty
     *                value and all others will be merged
     * @param width   the width of any map this returns
     * @param height  the height of any map this returns
     * @return a 2D char array that merges all non-'#' areas in regions, and fills
     *         the rest with '#'
     */
    public static char[][] merge(final ArrayList<char[][]> regions, final int width, final int height) {
	if (regions == null || regions.isEmpty()) {
	    return RoomFinder.defaultFill(width, height);
	}
	final char[][] first = regions.get(0);
	final char[][] dungeon = new char[Math.min(width, first.length)][Math.min(height, first[0].length)];
	for (int x = 0; x < first.length; x++) {
	    Arrays.fill(dungeon[x], '#');
	}
	for (final char[][] region : regions) {
	    for (int x = 0; x < width; x++) {
		for (int y = 0; y < height; y++) {
		    if (region[x][y] != '#') {
			dungeon[x][y] = region[x][y];
		    }
		}
	    }
	}
	return dungeon;
    }

    /**
     * Takes an x, y position and finds the room, corridor, or cave at that
     * position, if there is one, returning the same 2D char array format as the
     * other methods.
     *
     * @param x the x coordinate of a position that should be in a room or corridor
     * @param y the y coordinate of a position that should be in a room or corridor
     * @return a masked 2D char array where anything not in the current region is
     *         '#'
     */
    public char[][] regionAt(final int x, final int y) {
	final OrderedSet<GreasedRegion> regions = GreasedRegion.whichContain(x, y, this.rooms.keySet());
	regions.addAll(GreasedRegion.whichContain(x, y, this.corridors.keySet()));
	regions.addAll(GreasedRegion.whichContain(x, y, this.caves.keySet()));
	GreasedRegion found;
	if (regions.isEmpty()) {
	    found = new GreasedRegion(this.width, this.height);
	} else {
	    found = regions.first();
	}
	return found.mask(this.map, '#');
    }

    /**
     * Takes an x, y position and finds the room or corridor at that position and
     * the rooms, corridors or caves that it directly connects to, and returns the
     * group as one merged 2D char array.
     *
     * @param x the x coordinate of a position that should be in a room or corridor
     * @param y the y coordinate of a position that should be in a room or corridor
     * @return a masked 2D char array where anything not in the current region or
     *         one nearby is '#'
     */
    public char[][] regionsNear(final int x, final int y) {
	final OrderedSet<GreasedRegion> atRooms = GreasedRegion.whichContain(x, y, this.rooms.keySet()),
		atCorridors = GreasedRegion.whichContain(x, y, this.corridors.keySet()),
		atCaves = GreasedRegion.whichContain(x, y, this.caves.keySet()), regions = new OrderedSet<>(64);
	regions.addAll(atRooms);
	regions.addAll(atCorridors);
	regions.addAll(atCaves);
	GreasedRegion found;
	if (regions.isEmpty()) {
	    found = new GreasedRegion(this.width, this.height);
	} else {
	    found = regions.first();
	    List<List<GreasedRegion>> near = this.rooms.getMany(atRooms);
	    for (final List<GreasedRegion> links : near) {
		for (final GreasedRegion n : links) {
		    found.or(n);
		}
	    }
	    near = this.corridors.getMany(atCorridors);
	    for (final List<GreasedRegion> links : near) {
		for (final GreasedRegion n : links) {
		    found.or(n);
		}
	    }
	    near = this.caves.getMany(atCaves);
	    for (final List<GreasedRegion> links : near) {
		for (final GreasedRegion n : links) {
		    found.or(n);
		}
	    }
	}
	return found.mask(this.map, '#');
    }

    /**
     * Takes an x, y position and finds the rooms or corridors that are directly
     * connected to the room, corridor or cave at that position, and returns the
     * group as an ArrayList of 2D char arrays, one per connecting region.
     *
     * @param x the x coordinate of a position that should be in a room or corridor
     * @param y the y coordinate of a position that should be in a room or corridor
     * @return an ArrayList of masked 2D char arrays where anything not in a
     *         connected region is '#'
     */
    public ArrayList<char[][]> regionsConnected(final int x, final int y) {
	final ArrayList<char[][]> regions = new ArrayList<>(10);
	List<List<GreasedRegion>> near = this.rooms.getMany(GreasedRegion.whichContain(x, y, this.rooms.keySet()));
	for (final List<GreasedRegion> links : near) {
	    for (final GreasedRegion n : links) {
		regions.add(n.mask(this.map, '#'));
	    }
	}
	near = this.corridors.getMany(GreasedRegion.whichContain(x, y, this.corridors.keySet()));
	for (final List<GreasedRegion> links : near) {
	    for (final GreasedRegion n : links) {
		regions.add(n.mask(this.map, '#'));
	    }
	}
	near = this.caves.getMany(GreasedRegion.whichContain(x, y, this.caves.keySet()));
	for (final List<GreasedRegion> links : near) {
	    for (final GreasedRegion n : links) {
		regions.add(n.mask(this.map, '#'));
	    }
	}
	return regions;
    }
}
