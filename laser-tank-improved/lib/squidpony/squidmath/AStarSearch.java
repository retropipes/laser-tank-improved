package squidpony.squidmath;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

import squidpony.ArrayTools;
import squidpony.squidgrid.Direction;

/**
 * Performs A* search.
 *
 * A* is a best-first search algorithm for pathfinding. It uses a heuristic
 * value to reduce the total search space. If the heuristic is too large then
 * the optimal path is not guaranteed to be returned. <br>
 * This implementation outperforms DijkstraMap (it can be about 8x faster) on
 * relatively short paths (8 or less cells), but when an especially long path is
 * requested (20 or more cells), this can be slower by a significant degree (it
 * can be 10x slower for paths of 40-50 cells). This replaces an earlier version
 * of AStarSearch that was almost always slower than DijkstraMap and had serious
 * issues. One issue was recursion-related, and previously led to very long
 * paths taking 200x as much time as DijkstraMap instead of this version's
 * comparably modest 10x slowdown on the same paths. That recursion-related
 * issue could have caused StackOverflowExceptions to be thrown when finding
 * very long paths, though that may not have occurred "in the wild." The only
 * caveat to the optimizations here is that an AStarSearch object has slightly
 * more state that it stores now, though it also reuses more state and produces
 * less garbage data to collect. <br>
 * If you want pathfinding over an arbitrary graph or need really fast searches,
 * you may want to use gdx-ai's pathfinding code in its
 * {@code com.badlogic.gdx.ai.pfa} package. Their {@code IndexedAStarPathFinder}
 * class outperforms all of the pathfinders in squidlib in all cases tested,
 * though it has less features than DijkstraMap. You would need a dependency on
 * gdx-ai and libGDX, which the squidlib-util module does not have, but if you
 * use the squidlib display module, then you already depend on libGDX.
 *
 * @see squidpony.squidai.DijkstraMap a sometimes-faster pathfinding algorithm
 *      that can pathfind to multiple goals
 * @see squidpony.squidai.CustomDijkstraMap an alternative to DijkstraMap;
 *      faster and supports complex adjacency rules
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger - optimized code
 */
public class AStarSearch implements Serializable {
    private static final long serialVersionUID = 1248976538417655312L;

    /**
     * The type of heuristic to use.
     */
    public enum SearchType {
	/**
	 * The distance it takes when only the four primary directions can be moved in.
	 */
	MANHATTAN,
	/**
	 * The distance it takes when diagonal movement costs the same as cardinal
	 * movement.
	 */
	CHEBYSHEV,
	/**
	 * The distance it takes as the crow flies.
	 */
	EUCLIDEAN,
	/**
	 * Full space search. Least efficient but guaranteed to return a path if one
	 * exists. See also DijkstraMap class.
	 */
	DIJKSTRA
    }

    protected final double[][] map;
    protected final OrderedSet<Coord> open = new OrderedSet<>();
    protected final int width, height;
    protected byte[][] parent;
    protected double[][] gCache;
    protected transient Coord start, target;
    protected final SearchType type;
    protected Direction[] dirs;
    private int dirCount;
    private transient Direction inner = Direction.DOWN;
    private final boolean[][] finished;

    protected AStarSearch() {
	this.width = 0;
	this.height = 0;
	this.type = SearchType.MANHATTAN;
	this.map = new double[this.width][this.height];
	this.parent = new byte[this.width][this.height];
	this.gCache = new double[this.width][this.height];
	this.finished = new boolean[this.width][this.height];
	this.dirs = Direction.CARDINALS;
	this.dirCount = 4;
    }

    /**
     * Builds a pathing object to run searches on.
     *
     * Values in the map are treated as positive values (and 0) being legal weights,
     * with higher values being harder to pass through. Any negative value is
     * treated as being an impassible space.
     *
     * If the type is Manhattan, only the cardinal directions will be used. All
     * other search types will return results based on intercardinal and cardinal
     * pathing.
     *
     * @param map  the search map. It is not modified by this class, hence you can
     *             share this map among multiple instances.
     * @param type the manner of search
     */
    public AStarSearch(final double[][] map, final SearchType type) {
	if (map == null) {
	    throw new NullPointerException("map should not be null when building an AStarSearch");
	}
	this.map = map;
	this.width = map.length;
	this.height = this.width == 0 ? 0 : map[0].length;
	this.parent = new byte[this.width][this.height];
	this.gCache = new double[this.width][this.height];
	this.finished = new boolean[this.width][this.height];
	this.type = type == null ? SearchType.DIJKSTRA : type;
	switch (type) {
	case MANHATTAN:
	    this.dirs = Direction.CARDINALS;
	    this.dirCount = 4;
	    break;
	case CHEBYSHEV:
	case EUCLIDEAN:
	case DIJKSTRA:
	default:
	    this.dirs = Direction.OUTWARDS;
	    this.dirCount = 8;
	    break;
	}
    }

    /**
     * Finds an A* path to the target from the start. If no path is possible,
     * returns null.
     *
     * @param startx  the x coordinate of the start location
     * @param starty  the y coordinate of the start location
     * @param targetx the x coordinate of the target location
     * @param targety the y coordinate of the target location
     * @return the shortest path, or null
     */
    public Queue<Coord> path(final int startx, final int starty, final int targetx, final int targety) {
	return this.path(Coord.get(startx, starty), Coord.get(targetx, targety));
    }

    /**
     * Finds an A* path to the target from the start. If no path is possible,
     * returns null.
     *
     * @param start  the start location
     * @param target the target location
     * @return the shortest path, or null
     */
    public Queue<Coord> path(final Coord start, final Coord target) {
	this.start = start;
	this.target = target;
	this.open.clear();
	ArrayTools.fill(this.finished, false);
	ArrayTools.fill(this.parent, (byte) -1);
	ArrayTools.fill(this.gCache, -1.0);
	this.gCache[start.x][start.y] = 0;
	/* Not using Deque nor ArrayDeque, they aren't Gwt compatible */
	final LinkedList<Coord> deq = new LinkedList<>();
	Coord p = start;
	this.open.add(p);
	Direction dir;
	byte turn;
	while (!p.equals(target)) {
	    this.finished[p.x][p.y] = true;
	    this.open.remove(p);
	    for (byte d = 0; d < this.dirCount; d++) {
		dir = this.dirs[d];
		final int x = p.x + dir.deltaX;
		if (x < 0 || x >= this.width) {
		    continue;// out of bounds so skip ahead
		}
		final int y = p.y + dir.deltaY;
		if (y < 0 || y >= this.height) {
		    continue;// out of bounds so skip ahead
		}
		if (!this.finished[x][y]) {
		    final Coord test = Coord.get(x, y);
		    if (this.open.contains(test)) {
			turn = this.parent[x][y];
			if (turn < 0) {
			    continue;
			}
			// look back and find what we had in gCache
			this.inner = this.dirs[turn];
			final double parentG = this.g(x - this.inner.deltaX, y - this.inner.deltaY);
			// double parentG = g(parent[x][y].x, parent[x][y].y);
			if (parentG < 0) {
			    continue;// not a valid point so skip ahead
			}
			final double g = this.g(p.x, p.y);
			if (g < 0) {
			    continue;// not a valid point so skip ahead
			}
			if (parentG > g) {
			    this.parent[x][y] = d;
			}
		    } else {
			this.open.add(test);
			this.parent[x][y] = d;
		    }
		}
	    }
	    p = this.smallestF();
	    if (p == null) {
		return deq;// no path possible
	    }
	}
	while (!p.equals(start)) {
	    deq.addFirst(p);
	    this.inner = this.dirs[this.parent[p.x][p.y]];
	    p = p.translate(-this.inner.deltaX, -this.inner.deltaY);
	}
	return deq;
    }

    public void changeCellWeight(final int x, final int y, final double d) {
	this.map[x][y] = d;
    }

    public int getWidth() {
	return this.width;
    }

    public int getHeight() {
	return this.height;
    }

    /**
     * Finds the g value (start to current) for the given location.
     *
     * If the given location is not valid or not attached to the pathfinding then -1
     * is returned.
     *
     * @param x coordinate
     * @param y coordinate
     */
    protected double g(final int x, final int y) {
	if (x == this.start.x && y == this.start.y) {
	    this.gCache[x][y] = 0;
	    return 0;
	}
	if (x < 0 || y < 0 || x >= this.width || y >= this.height || this.map[x][y] < 0 || this.parent[x][y] < 0) {
	    this.gCache[x][y] = -1;
	    return -1;// not a valid location
	}
	this.inner = this.dirs[this.parent[x][y]];
	final double parentG = this.gCache[x - this.inner.deltaX][y - this.inner.deltaY];
	// double parentG = g(parent[x][y].x, parent[x][y].y);
	if (parentG < 0) {
	    this.gCache[x][y] = -1;
	    return -1;// if any part of the path is not valid, this part is not valid
	}
	return this.gCache[x][y] = this.map[x][y] + parentG + 1;// follow path back to start
    }

    /**
     * Returns the heuristic distance from the current cell to the goal location\
     * using the current calculation type.
     *
     * @param x coordinate
     * @param y coordinate
     * @return distance
     */
    protected double h(final int x, final int y) {
	switch (this.type) {
	case MANHATTAN:
	    return Math.abs(x - this.target.x) + Math.abs(y - this.target.y);
	case CHEBYSHEV:
	    return Math.max(Math.abs(x - this.target.x), Math.abs(y - this.target.y));
	case EUCLIDEAN:
	    int xDist = Math.abs(x - this.target.x);
	    xDist *= xDist;
	    int yDist = Math.abs(y - this.target.y);
	    yDist *= yDist;
	    return Math.sqrt(xDist + yDist);
	case DIJKSTRA:
	default:
	    return 0;
	}
    }

    /**
     * Combines g and h to get the estimated distance from start to goal going on
     * the current route.
     *
     * @param x coordinate
     * @param y coordinate
     * @return The current known shortest distance to the start position from the
     *         given position. If the current position cannot reach the start
     *         position or is invalid, -1 is returned.
     */
    protected double f(final int x, final int y) {
	final double foundG = this.g(x, y);
	if (foundG < 0) {
	    return -1;
	}
	return this.h(x, y) + foundG;
    }

    /**
     * @return the current open point with the smallest F
     */
    protected Coord smallestF() {
	Coord smallest = null;
	double smallF = Double.POSITIVE_INFINITY;
	double f;
	final int sz = this.open.size();
	Coord p;
	for (int o = 0; o < sz; o++) {
	    p = this.open.getAt(o);
	    if (p == null) {
		continue;
	    }
	    f = this.f(p.x, p.y);
	    if (f < 0) {
		continue;// current tested point is not valid so skip it
	    }
	    if (smallest == null || f < smallF) {
		smallest = p;
		smallF = f;
	    }
	}
	return smallest;
    }

    @Override
    public String toString() {
	final StringBuilder result = new StringBuilder(this.width * this.height);
	int maxLen = 0;
	/*
	 * First we compute the longest (String-wise) entry, so that we can "indent"
	 * shorter cells, so that the output looks good (and is hereby readable).
	 */
	for (int y = 0; y < this.height; y++) {
	    for (int x = 0; x < this.width; x++) {
		final String output = String.valueOf(Math.round(this.map[x][y]));
		final int locLen = output.length();
		if (maxLen < locLen) {
		    maxLen = locLen;
		}
	    }
	}
	for (int y = 0; y < this.height; y++) {
	    for (int x = 0; x < this.width; x++) {
		final long v = Math.round(this.map[x][y]);
		final String s = String.valueOf(v);
		final int slen = s.length();
		assert slen <= maxLen;
		int diff = maxLen - slen;
		while (0 < diff) {
		    result.append(" ");
		    diff--;
		}
		result.append(s);
	    }
	    if (y < this.height - 1) {
		result.append('\n');
	    }
	}
	return result.toString();
    }
    /*
     * public static final int DIMENSION = 40, PATH_LENGTH = (DIMENSION - 2) *
     * (DIMENSION - 2); public static DungeonGenerator dungeonGen = new
     * DungeonGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
     * public static SerpentMapGenerator serpent = new
     * SerpentMapGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
     * public static char[][] mp; public static double[][] astarMap; public static
     * GreasedRegion floors; public static void main(String[] args) {
     * serpent.putWalledBoxRoomCarvers(1); mp =
     * dungeonGen.generate(serpent.generate()); floors = new GreasedRegion(mp, '.');
     * astarMap = DungeonUtility.generateAStarCostMap(mp, Collections.<Character,
     * Double>emptyMap(), 1); long time = System.currentTimeMillis(), len; len =
     * doPathAStar2(); System.out.println(System.currentTimeMillis() - time);
     * System.out.println(len); } public static long doPathAStar2() { AStarSearch
     * astar = new AStarSearch(astarMap, AStarSearch.SearchType.CHEBYSHEV); Coord r;
     * long scanned = 0; DungeonUtility utility = new DungeonUtility(new
     * StatefulRNG(new LightRNG(0x1337BEEFDEAL))); Queue<Coord> latestPath; for (int
     * x = 1; x < DIMENSION - 1; x++) { for (int y = 1; y < DIMENSION - 1; y++) { if
     * (mp[x][y] == '#') continue; // this should ensure no blatant correlation
     * between R and W utility.rng.setState((x << 22) | (y << 16) | (x * y)); r =
     * floors.singleRandom(utility.rng); latestPath = astar.path(r, Coord.get(x,
     * y)); scanned+= latestPath.size(); } } return scanned; }
     */
}
