package squidpony.squidgrid.mapping;

import java.io.Serializable;
import java.util.ArrayList;

import squidpony.ArrayTools;
import squidpony.GwtCompatibility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;

/**
 * A subsection of a (typically modern-day or sci-fi) area map that can be
 * placed by ModularMapGenerator. Created by Tommy Ettinger on 4/4/2016.
 */
public class MapModule implements Comparable<MapModule>, Serializable {
    private static final long serialVersionUID = -1273406898212937188L;
    /**
     * The contents of this section of map.
     */
    public char[][] map;
    /**
     * The room/cave/corridor/wall status for each cell of this section of map.
     */
    public int[][] environment;
    /**
     * Stores Coords just outside the contents of the MapModule, where doors are
     * allowed to connect into this. Uses Coord positions that are relative to this
     * MapModule's map field, not whatever this is being placed into.
     */
    public Coord[] validDoors;
    /**
     * The minimum point on the bounding rectangle of the room, including walls.
     */
    public Coord min;
    /**
     * The maximum point on the bounding rectangle of the room, including walls.
     */
    public Coord max;
    public ArrayList<Coord> leftDoors, rightDoors, topDoors, bottomDoors;
    public int category;
    private static final char[] validPacking = new char[] { '.', ',', '"', '^', '<', '>' },
	    doors = new char[] { '+', '/' };

    public MapModule() {
	this(CoordPacker.unpackChar(CoordPacker.rectangle(1, 1, 6, 6), 8, 8, '.', '#'));
    }

    /**
     * Constructs a MapModule given only a 2D char array as the contents of this
     * section of map. The actual MapModule will use doors in the 2D char array as
     * '+' or '/' if present. Otherwise, the valid locations for doors will be any
     * outer wall adjacent to a floor ('.'), shallow water (','), grass ('"'), trap
     * ('^'), or staircase (less than or greater than signs). The max and min Coords
     * of the bounding rectangle, including one layer of outer walls, will also be
     * calculated. The map you pass to this does need to have outer walls present in
     * it already.
     *
     * @param map the 2D char array that contains the contents of this section of
     *            map
     */
    public MapModule(final char[][] map) {
	if (map == null || map.length <= 0) {
	    throw new UnsupportedOperationException("Given map cannot be empty in MapModule");
	}
	this.map = ArrayTools.copy(map);
	this.environment = ArrayTools.fill(MixedGenerator.ROOM_FLOOR, this.map.length, this.map[0].length);
	for (int x = 0; x < map.length; x++) {
	    for (int y = 0; y < map[0].length; y++) {
		if (this.map[x][y] == '#') {
		    this.environment[x][y] = MixedGenerator.ROOM_WALL;
		}
	    }
	}
	final short[] pk = CoordPacker.fringe(CoordPacker.pack(this.map, MapModule.validPacking), 1, this.map.length,
		this.map[0].length, false, true);
	final Coord[] tmp = CoordPacker.bounds(pk);
	this.min = tmp[0];
	this.max = tmp[1];
	this.category = MapModule.categorize(Math.max(this.max.x, this.max.y));
	final short[] drs = CoordPacker.pack(this.map, MapModule.doors);
	if (drs.length >= 2) {
	    this.validDoors = CoordPacker.allPacked(drs);
	} else {
	    this.validDoors = CoordPacker.fractionPacked(pk, 5);// CoordPacker.allPacked(pk);
	    // for(Coord dr : validDoors)
	    // this.map[dr.x][dr.y] = '+';
	}
	this.initSides();
    }

    /**
     * Constructs a MapModule given only a short array of packed data (as produced
     * by CoordPacker and consumed or produced by several other classes) that when
     * unpacked will yield the contents of this section of map. The actual MapModule
     * will use a slightly larger 2D array than the given width and height to ensure
     * walls can be drawn around the floors, and the valid locations for doors will
     * be any outer wall adjacent to an "on" coordinate in packed. The max and min
     * Coords of the bounding rectangle, including one layer of outer walls, will
     * also be calculated. Notably, the packed data you pass to this does not need
     * to have a gap between floors and the edge of the map to make walls.
     *
     * @param packed the short array, as packed data from CoordPacker, that contains
     *               the contents of this section of map
     */
    public MapModule(final short[] packed, final int width, final int height) {
	this(CoordPacker.unpackChar(packed, width, height, '.', '#'));
    }

    /**
     * Constructs a MapModule from the given arguments without modifying them,
     * copying map without changing its size, copying validDoors, and using the same
     * min and max (which are immutable, so they can be reused).
     *
     * @param map        the 2D char array that contains the contents of this
     *                   section of map; will be copied exactly
     * @param validDoors a Coord array that stores viable locations to place doors
     *                   in map; will be cloned
     * @param min        the minimum Coord of this MapModule's bounding rectangle
     * @param max        the maximum Coord of this MapModule's bounding rectangle
     */
    public MapModule(final char[][] map, final Coord[] validDoors, final Coord min, final Coord max) {
	this.map = ArrayTools.copy(map);
	this.environment = ArrayTools.fill(MixedGenerator.ROOM_FLOOR, this.map.length, this.map[0].length);
	for (int x = 0; x < map.length; x++) {
	    for (int y = 0; y < map[0].length; y++) {
		if (this.map[x][y] == '#') {
		    this.environment[x][y] = MixedGenerator.ROOM_WALL;
		}
	    }
	}
	this.validDoors = GwtCompatibility.cloneCoords(validDoors);
	this.min = min;
	this.max = max;
	this.category = MapModule.categorize(Math.max(max.x, max.y));
	final ArrayList<Coord> doors2 = new ArrayList<>(16);
	for (int x = 0; x < map.length; x++) {
	    for (int y = 0; y < map[x].length; y++) {
		if (map[x][y] == '+' || map[x][y] == '/') {
		    doors2.add(Coord.get(x, y));
		}
	    }
	}
	if (!doors2.isEmpty()) {
	    this.validDoors = doors2.toArray(new Coord[doors2.size()]);
	}
	this.initSides();
    }

    /**
     * Copies another MapModule and uses it to construct a new one.
     *
     * @param other an already-constructed MapModule that this will copy
     */
    public MapModule(final MapModule other) {
	this(other.map, other.validDoors, other.min, other.max);
    }

    /**
     * Rotates a copy of this MapModule by the given number of 90-degree turns.
     * Describing the turns as clockwise or counter-clockwise depends on whether the
     * y-axis "points up" or "points down." If higher values for y are toward the
     * bottom of the screen (the default for when 2D arrays are printed), a turn of
     * 1 is clockwise 90 degrees, but if the opposite is true and higher y is toward
     * the top, then a turn of 1 is counter-clockwise 90 degrees.
     *
     * @param turns the number of 90 degree turns to adjust this by
     * @return a new MapModule (copied from this one) that has been rotated by the
     *         given amount
     */
    public MapModule rotate(int turns) {
	turns %= 4;
	char[][] map2;
	Coord[] doors2;
	Coord min2, max2;
	final int xSize = this.map.length - 1, ySize = this.map[0].length - 1;
	switch (turns) {
	case 1:
	    map2 = new char[this.map[0].length][this.map.length];
	    for (int i = 0; i < this.map.length; i++) {
		for (int j = 0; j < this.map[0].length; j++) {
		    map2[ySize - j][i] = this.map[i][j];
		}
	    }
	    doors2 = new Coord[this.validDoors.length];
	    for (int i = 0; i < this.validDoors.length; i++) {
		doors2[i] = Coord.get(ySize - this.validDoors[i].y, this.validDoors[i].x);
	    }
	    min2 = Coord.get(ySize - this.max.y, this.min.x);
	    max2 = Coord.get(ySize - this.min.y, this.max.x);
	    return new MapModule(map2, doors2, min2, max2);
	case 2:
	    map2 = new char[this.map.length][this.map[0].length];
	    for (int i = 0; i < this.map.length; i++) {
		for (int j = 0; j < this.map[0].length; j++) {
		    map2[xSize - i][ySize - j] = this.map[i][j];
		}
	    }
	    doors2 = new Coord[this.validDoors.length];
	    for (int i = 0; i < this.validDoors.length; i++) {
		doors2[i] = Coord.get(xSize - this.validDoors[i].x, ySize - this.validDoors[i].y);
	    }
	    min2 = Coord.get(xSize - this.max.x, ySize - this.max.y);
	    max2 = Coord.get(xSize - this.min.x, ySize - this.min.y);
	    return new MapModule(map2, doors2, min2, max2);
	case 3:
	    map2 = new char[this.map[0].length][this.map.length];
	    for (int i = 0; i < this.map.length; i++) {
		for (int j = 0; j < this.map[0].length; j++) {
		    map2[j][xSize - i] = this.map[i][j];
		}
	    }
	    doors2 = new Coord[this.validDoors.length];
	    for (int i = 0; i < this.validDoors.length; i++) {
		doors2[i] = Coord.get(this.validDoors[i].y, xSize - this.validDoors[i].x);
	    }
	    min2 = Coord.get(this.min.y, xSize - this.max.x);
	    max2 = Coord.get(this.max.y, xSize - this.min.x);
	    return new MapModule(map2, doors2, min2, max2);
	default:
	    return new MapModule(this.map, this.validDoors, this.min, this.max);
	}
    }

    public MapModule flip(final boolean flipLeftRight, final boolean flipUpDown) {
	if (!flipLeftRight && !flipUpDown) {
	    return new MapModule(this.map, this.validDoors, this.min, this.max);
	}
	final char[][] map2 = new char[this.map.length][this.map[0].length];
	final Coord[] doors2 = new Coord[this.validDoors.length];
	Coord min2, max2;
	final int xSize = this.map.length - 1, ySize = this.map[0].length - 1;
	if (flipLeftRight && flipUpDown) {
	    for (int i = 0; i < this.map.length; i++) {
		for (int j = 0; j < this.map[0].length; j++) {
		    map2[xSize - i][ySize - j] = this.map[i][j];
		}
	    }
	    for (int i = 0; i < this.validDoors.length; i++) {
		doors2[i] = Coord.get(xSize - this.validDoors[i].x, ySize - this.validDoors[i].y);
	    }
	    min2 = Coord.get(xSize - this.max.x, ySize - this.max.y);
	    max2 = Coord.get(xSize - this.min.x, xSize - this.min.y);
	} else if (flipLeftRight) {
	    for (int i = 0; i < this.map.length; i++) {
		System.arraycopy(this.map[i], 0, map2[xSize - i], 0, this.map[0].length);
	    }
	    for (int i = 0; i < this.validDoors.length; i++) {
		doors2[i] = Coord.get(xSize - this.validDoors[i].x, this.validDoors[i].y);
	    }
	    min2 = Coord.get(xSize - this.max.x, this.min.y);
	    max2 = Coord.get(xSize - this.min.x, this.max.y);
	} else {
	    for (int i = 0; i < this.map.length; i++) {
		for (int j = 0; j < this.map[0].length; j++) {
		    map2[i][ySize - j] = this.map[i][j];
		}
	    }
	    for (int i = 0; i < this.validDoors.length; i++) {
		doors2[i] = Coord.get(this.validDoors[i].x, ySize - this.validDoors[i].y);
	    }
	    min2 = Coord.get(this.min.x, ySize - this.max.y);
	    max2 = Coord.get(this.max.x, xSize - this.min.y);
	}
	return new MapModule(map2, doors2, min2, max2);
    }

    static int categorize(final int n) {
	final int highest = Integer.highestOneBit(n);
	return Math.max(4, highest == Integer.lowestOneBit(n) ? highest : highest << 1);
    }

    private void initSides() {
	this.leftDoors = new ArrayList<>(8);
	this.rightDoors = new ArrayList<>(8);
	this.topDoors = new ArrayList<>(8);
	this.bottomDoors = new ArrayList<>(8);
	for (final Coord dr : this.validDoors) {
	    if (dr.x * this.max.y < dr.y * this.max.x && dr.y * this.max.x < (this.max.x - dr.x) * this.max.y) {
		this.leftDoors.add(dr);
	    } else if (dr.x * this.max.y > dr.y * this.max.x && dr.y * this.max.x > (this.max.x - dr.x) * this.max.y) {
		this.rightDoors.add(dr);
	    } else if (dr.x * this.max.y > dr.y * this.max.x && dr.y * this.max.x < (this.max.x - dr.x) * this.max.y) {
		this.topDoors.add(dr);
	    } else if (dr.x * this.max.y < dr.y * this.max.x && dr.y * this.max.x > (this.max.x - dr.x) * this.max.y) {
		this.bottomDoors.add(dr);
	    }
	}
    }

    @Override
    public int compareTo(final MapModule o) {
	if (o == null) {
	    return 1;
	}
	return this.category - o.category;
    }
}
