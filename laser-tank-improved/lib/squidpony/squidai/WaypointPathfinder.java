package squidpony.squidai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.PoissonDisk;
import squidpony.squidmath.RNG;
import squidpony.squidmath.StatefulRNG;

/**
 * Pathfind to known connections between rooms or other "chokepoints" without
 * needing full-map Dijkstra scans. Pre-calculates a path either from or to any
 * given chokepoint to each other chokepoint. Created by Tommy Ettinger on
 * 10/25/2015.
 */
public class WaypointPathfinder {
    private final int width;
    private final int height;
    private DijkstraMap dm;
    private final int[][] expansionMap;
    public RNG rng;
    private final OrderedMap<Coord, OrderedMap<Coord, Edge>> waypoints;

    /**
     * Calculates and stores the doors and doors-like connections ("chokepoints") on
     * the given map as waypoints. Will use the given Radius enum to determine how
     * to handle DijkstraMap measurement in future pathfinding. Uses rng for all
     * random choices, or a new unseeded RNG if the parameter is null.
     *
     * @param map         a char[][] that stores a "complete" dungeon map, with any
     *                    chars as features that pathfinding needs.
     * @param measurement a Radius that should correspond to how you want path
     *                    distance calculated.
     * @param rng         an RNG object or null (which will make this use a new
     *                    RNG); will be used for all random choices
     */
    public WaypointPathfinder(final char[][] map, final Radius measurement, final RNG rng) {
	if (rng == null) {
	    this.rng = new StatefulRNG();
	} else {
	    this.rng = rng;
	}
	this.width = map.length;
	this.height = map[0].length;
	final char[][] simplified = DungeonUtility.simplifyDungeon(map);
	final ArrayList<Coord> centers = PoissonDisk.sampleMap(simplified, Math.min(this.width, this.height) * 0.4f,
		this.rng, '#');
	final int centerCount = centers.size();
	this.expansionMap = new int[this.width][this.height];
	this.waypoints = new OrderedMap<>(64);
	this.dm = new DijkstraMap(simplified, DijkstraMap.Measurement.MANHATTAN);
	for (final Coord center : centers) {
	    this.dm.clearGoals();
	    this.dm.resetMap();
	    this.dm.setGoal(center);
	    this.dm.scan(null);
	    double current;
	    for (int i = 0; i < this.width; i++) {
		for (int j = 0; j < this.height; j++) {
		    current = this.dm.gradientMap[i][j];
		    if (current >= DijkstraMap.FLOOR) {
			continue;
		    }
		    if (center.x == i && center.y == j) {
			this.expansionMap[i][j]++;
		    }
		    for (final Direction dir : Direction.CARDINALS) {
			if (this.dm.gradientMap[i + dir.deltaX][j + dir.deltaY] == current + 1
				|| this.dm.gradientMap[i + dir.deltaX][j + dir.deltaY] == current - 1) {
			    this.expansionMap[i][j]++;
			}
		    }
		}
	    }
	}
	for (int i = 0; i < this.width; i++) {
	    for (int j = 0; j < this.height; j++) {
		this.expansionMap[i][j] /= centerCount;
	    }
	}
	final OrderedSet<Coord> chokes = new OrderedSet<>(128);
	for (int i = 0; i < this.width; i++) {
	    ELEMENT_WISE: for (int j = 0; j < this.height; j++) {
		if (this.expansionMap[i][j] <= 0) {
		    continue;
		}
		final int current = this.expansionMap[i][j];
		boolean good = false;
		for (final Direction dir : Direction.CARDINALS) {
		    if (chokes.contains(Coord.get(i + dir.deltaX, j + dir.deltaY))) {
			continue ELEMENT_WISE;
		    }
		    if (this.expansionMap[i + dir.deltaX][j + dir.deltaY] > 0
			    && this.expansionMap[i + dir.deltaX][j + dir.deltaY] > current + 1
			    || this.expansionMap[i + dir.deltaX][j + dir.deltaY] > current
				    && this.expansionMap[i][j] <= 2) {
			if (this.expansionMap[i - dir.deltaX][j - dir.deltaY] > 0
				&& this.expansionMap[i - dir.deltaX][j - dir.deltaY] >= current) {
			    good = true;
			}
		    }
		}
		if (good) {
		    final Coord chk = Coord.get(i, j);
		    chokes.add(chk);
		    this.waypoints.put(chk, new OrderedMap<Coord, Edge>());
		}
	    }
	}
	/*
	 * for (int y = 0; y < height; y++) { for (int x = 0; x < width; x++) {
	 * if(expansionMap[x][y] <= 0) System.out.print('#'); else
	 * System.out.print((char)(expansionMap[x][y] + 64)); } System.out.println(); }
	 *
	 * for (int y = 0; y < height; y++) { for (int x = 0; x < width; x++) {
	 * if(expansionMap[x][y] <= 0) System.out.print('#'); else
	 * if(chokes.contains(Coord.get(x, y))) System.out.print('@'); else
	 * if(centers.contains(Coord.get(x, y))) System.out.print('*'); else
	 * System.out.print('.'); } System.out.println(); }
	 */
	this.dm = new DijkstraMap(map, DijkstraMap.findMeasurement(measurement));
	for (final Map.Entry<Coord, OrderedMap<Coord, Edge>> n : this.waypoints.entrySet()) {
	    chokes.remove(n.getKey());
	    if (chokes.isEmpty()) {
		break;
	    }
	    this.dm.clearGoals();
	    this.dm.resetMap();
	    this.dm.setGoal(n.getKey());
	    this.dm.scan(null);
	    for (final Coord c : chokes) {
		n.getValue().put(c,
			new Edge(n.getKey(), c, this.dm.findPathPreScanned(c), this.dm.gradientMap[c.x][c.y]));
	    }
	}
    }

    /**
     * Calculates and stores the doors and doors-like connections ("chokepoints") on
     * the given map as waypoints. Will use the given Radius enum to determine how
     * to handle DijkstraMap measurement in future pathfinding. Uses rng for all
     * random choices, or a new unseeded RNG if the parameter is null.
     *
     * @param map            a char[][] that stores a "complete" dungeon map, with
     *                       any chars as features that pathfinding needs.
     * @param measurement    a Radius that should correspond to how you want path
     *                       distance calculated.
     * @param rng            an RNG object or null (which will make this use a new
     *                       RNG); will be used for all random choices
     * @param thickCorridors true if most chokepoints on the map are 2 cells wide
     *                       instead of 1
     */
    public WaypointPathfinder(final char[][] map, final Radius measurement, final RNG rng,
	    final boolean thickCorridors) {
	if (rng == null) {
	    this.rng = new StatefulRNG();
	} else {
	    this.rng = rng;
	}
	this.width = map.length;
	this.height = map[0].length;
	final char[][] simplified = DungeonUtility.simplifyDungeon(map);
	this.expansionMap = new int[this.width][this.height];
	this.waypoints = new OrderedMap<>(64);
	final OrderedSet<Coord> chokes = new OrderedSet<>(128);
	if (thickCorridors) {
	    final short[] floors = CoordPacker.pack(simplified, '.'),
		    rooms = CoordPacker.flood(floors, CoordPacker.retract(floors, 1, 60, 60, true), 2, false),
		    corridors = CoordPacker.differencePacked(floors, rooms),
		    doors = CoordPacker.intersectPacked(rooms, CoordPacker.fringe(corridors, 1, 60, 60, false));
	    final Coord[] apart = CoordPacker.apartPacked(doors, 1);
	    Collections.addAll(chokes, apart);
	    for (final Coord element : apart) {
		this.waypoints.put(element, new OrderedMap<Coord, Edge>());
	    }
	} else {
	    final ArrayList<Coord> centers = PoissonDisk.sampleMap(simplified, Math.min(this.width, this.height) * 0.4f,
		    this.rng, '#');
	    final int centerCount = centers.size();
	    this.dm = new DijkstraMap(simplified, DijkstraMap.Measurement.MANHATTAN);
	    for (final Coord center : centers) {
		this.dm.clearGoals();
		this.dm.resetMap();
		this.dm.setGoal(center);
		this.dm.scan(null);
		double current;
		for (int i = 0; i < this.width; i++) {
		    for (int j = 0; j < this.height; j++) {
			current = this.dm.gradientMap[i][j];
			if (current >= DijkstraMap.FLOOR) {
			    continue;
			}
			if (center.x == i && center.y == j) {
			    this.expansionMap[i][j]++;
			}
			for (final Direction dir : Direction.CARDINALS) {
			    if (this.dm.gradientMap[i + dir.deltaX][j + dir.deltaY] == current + 1
				    || this.dm.gradientMap[i + dir.deltaX][j + dir.deltaY] == current - 1) {
				this.expansionMap[i][j]++;
			    }
			}
		    }
		}
	    }
	    for (int i = 0; i < this.width; i++) {
		for (int j = 0; j < this.height; j++) {
		    this.expansionMap[i][j] /= centerCount;
		}
	    }
	    for (int i = 0; i < this.width; i++) {
		ELEMENT_WISE: for (int j = 0; j < this.height; j++) {
		    if (this.expansionMap[i][j] <= 0) {
			continue;
		    }
		    final int current = this.expansionMap[i][j];
		    boolean good = false;
		    for (final Direction dir : Direction.CARDINALS) {
			if (chokes.contains(Coord.get(i + dir.deltaX, j + dir.deltaY))) {
			    continue ELEMENT_WISE;
			}
			if (this.expansionMap[i + dir.deltaX][j + dir.deltaY] > 0
				&& this.expansionMap[i + dir.deltaX][j + dir.deltaY] > current + 1
				|| this.expansionMap[i + dir.deltaX][j + dir.deltaY] > current
					&& this.expansionMap[i][j] <= 2) {
			    if (this.expansionMap[i - dir.deltaX][j - dir.deltaY] > 0
				    && this.expansionMap[i - dir.deltaX][j - dir.deltaY] >= current) {
				good = true;
			    }
			}
		    }
		    if (good) {
			final Coord chk = Coord.get(i, j);
			chokes.add(chk);
			this.waypoints.put(chk, new OrderedMap<Coord, Edge>());
		    }
		}
	    }
	}
	this.dm = new DijkstraMap(map, DijkstraMap.findMeasurement(measurement));
	for (final Map.Entry<Coord, OrderedMap<Coord, Edge>> n : this.waypoints.entrySet()) {
	    chokes.remove(n.getKey());
	    if (chokes.isEmpty()) {
		break;
	    }
	    this.dm.clearGoals();
	    this.dm.resetMap();
	    this.dm.setGoal(n.getKey());
	    this.dm.scan(null);
	    for (final Coord c : chokes) {
		n.getValue().put(c,
			new Edge(n.getKey(), c, this.dm.findPathPreScanned(c), this.dm.gradientMap[c.x][c.y]));
	    }
	}
    }

    /**
     * Calculates and stores the specified fraction of walkable points from map as
     * waypoints. Does not perform any analysis of chokepoints and acts as a more
     * brute-force solution when maps may be unpredictable. The lack of an analysis
     * step may mean this could have drastically less of a penalty to startup time
     * than the other constructors, and with the right fraction parameter (29 seems
     * ideal), may perform better as well. Will use the given Radius enum to
     * determine how to handle DijkstraMap measurement in future pathfinding. Uses
     * rng for all random choices, or a new unseeded RNG if the parameter is null.
     * <br>
     * Remember, a fraction value of 29 works well!
     *
     * @param map         a char[][] that stores a "complete" dungeon map, with any
     *                    chars as features that pathfinding needs.
     * @param measurement a Radius that should correspond to how you want path
     *                    distance calculated.
     * @param rng         an RNG object or null (which will make this use a new
     *                    RNG); will be used for all random choices
     * @param fraction    the fractional denominator of passable cells to assign as
     *                    waypoints; use 29 if you aren't sure
     */
    public WaypointPathfinder(final char[][] map, final Radius measurement, final RNG rng, final int fraction) {
	if (rng == null) {
	    this.rng = new StatefulRNG();
	} else {
	    this.rng = rng;
	}
	this.width = map.length;
	this.height = map[0].length;
	final char[][] simplified = DungeonUtility.simplifyDungeon(map);
	this.expansionMap = new int[this.width][this.height];
	this.waypoints = new OrderedMap<>(64);
	final OrderedSet<Coord> chokes = new OrderedSet<>(128);
	final short[] floors = CoordPacker.pack(simplified, '.');
	final Coord[] apart = CoordPacker.fractionPacked(floors, fraction);
	Collections.addAll(chokes, apart);
	for (final Coord element : apart) {
	    this.waypoints.put(element, new OrderedMap<Coord, Edge>());
	}
	this.dm = new DijkstraMap(map, DijkstraMap.findMeasurement(measurement));
	for (final Map.Entry<Coord, OrderedMap<Coord, Edge>> n : this.waypoints.entrySet()) {
	    chokes.remove(n.getKey());
	    if (chokes.isEmpty()) {
		break;
	    }
	    this.dm.clearGoals();
	    this.dm.resetMap();
	    this.dm.setGoal(n.getKey());
	    this.dm.scan(null);
	    for (final Coord c : chokes) {
		n.getValue().put(c,
			new Edge(n.getKey(), c, this.dm.findPathPreScanned(c), this.dm.gradientMap[c.x][c.y]));
	    }
	}
    }

    /**
     * Calculates and stores the doors and doors-like connections ("chokepoints") on
     * the given map as waypoints. Will use the given DijkstraMap for pathfinding
     * after construction (and during some initial calculations). The dijkstra
     * parameter will be mutated by this class, so it should not be reused
     * elsewhere. Uses rng for all random choices, or a new unseeded RNG if the
     * parameter is null.
     *
     * @param map      a char[][] that stores a "complete" dungeon map, with any
     *                 chars as features that pathfinding needs
     * @param dijkstra a DijkstraMap that will be used to find paths; may have costs
     *                 but they will not be used
     * @param rng      an RNG object or null (which will make this use a new RNG);
     *                 will be used for all random choices
     */
    public WaypointPathfinder(final char[][] map, final DijkstraMap dijkstra, final RNG rng) {
	if (rng == null) {
	    this.rng = new StatefulRNG();
	} else {
	    this.rng = rng;
	}
	this.width = map.length;
	this.height = map[0].length;
	final char[][] simplified = DungeonUtility.simplifyDungeon(map);
	final ArrayList<Coord> centers = PoissonDisk.sampleMap(simplified, Math.min(this.width, this.height) * 0.4f,
		this.rng, '#');
	final int centerCount = centers.size();
	this.expansionMap = new int[this.width][this.height];
	this.waypoints = new OrderedMap<>(64);
	this.dm = new DijkstraMap(simplified, DijkstraMap.Measurement.MANHATTAN);
	for (final Coord center : centers) {
	    this.dm.clearGoals();
	    this.dm.resetMap();
	    this.dm.setGoal(center);
	    this.dm.scan(null);
	    double current;
	    for (int i = 0; i < this.width; i++) {
		for (int j = 0; j < this.height; j++) {
		    current = this.dm.gradientMap[i][j];
		    if (current >= DijkstraMap.FLOOR) {
			continue;
		    }
		    if (center.x == i && center.y == j) {
			this.expansionMap[i][j]++;
		    }
		    for (final Direction dir : Direction.CARDINALS) {
			if (this.dm.gradientMap[i + dir.deltaX][j + dir.deltaY] == current + 1
				|| this.dm.gradientMap[i + dir.deltaX][j + dir.deltaY] == current - 1) {
			    this.expansionMap[i][j]++;
			}
		    }
		}
	    }
	}
	for (int i = 0; i < this.width; i++) {
	    for (int j = 0; j < this.height; j++) {
		this.expansionMap[i][j] /= centerCount;
	    }
	}
	final OrderedSet<Coord> chokes = new OrderedSet<>(128);
	for (int i = 0; i < this.width; i++) {
	    ELEMENT_WISE: for (int j = 0; j < this.height; j++) {
		if (this.expansionMap[i][j] <= 0) {
		    continue;
		}
		final int current = this.expansionMap[i][j];
		boolean good = false;
		for (final Direction dir : Direction.CARDINALS) {
		    if (chokes.contains(Coord.get(i + dir.deltaX, j + dir.deltaY))) {
			continue ELEMENT_WISE;
		    }
		    if (this.expansionMap[i + dir.deltaX][j + dir.deltaY] > 0
			    && this.expansionMap[i + dir.deltaX][j + dir.deltaY] > current + 1
			    || this.expansionMap[i + dir.deltaX][j + dir.deltaY] > current
				    && this.expansionMap[i][j] <= 2) {
			if (this.expansionMap[i - dir.deltaX][j - dir.deltaY] > 0
				&& this.expansionMap[i - dir.deltaX][j - dir.deltaY] >= current) {
			    good = true;
			}
		    }
		}
		if (good) {
		    final Coord chk = Coord.get(i, j);
		    chokes.add(chk);
		    this.waypoints.put(chk, new OrderedMap<Coord, Edge>());
		}
	    }
	}
	this.dm = dijkstra;
	for (final Map.Entry<Coord, OrderedMap<Coord, Edge>> n : this.waypoints.entrySet()) {
	    chokes.remove(n.getKey());
	    if (chokes.isEmpty()) {
		break;
	    }
	    this.dm.clearGoals();
	    this.dm.resetMap();
	    this.dm.setGoal(n.getKey());
	    this.dm.scan(null);
	    for (final Coord c : chokes) {
		n.getValue().put(c,
			new Edge(n.getKey(), c, this.dm.findPathPreScanned(c), this.dm.gradientMap[c.x][c.y]));
	    }
	}
    }

    /**
     * Finds the appropriate one of the already-calculated, possibly-long paths this
     * class stores to get from a waypoint to another waypoint, then quickly finds a
     * path to get on the long path, and returns the total path. This does not need
     * to perform any full-map scans with DijkstraMap.
     *
     * @param self              the pathfinder's position
     * @param approximateTarget the Coord that represents the approximate area to
     *                          pathfind to; will be randomized if it is not
     *                          walkable.
     * @return an ArrayList of Coord that will go from a cell adjacent to self to a
     *         waypoint near approximateTarget
     */
    public ArrayList<Coord> getKnownPath(final Coord self, final Coord approximateTarget) {
	final ArrayList<Coord> near = this.dm.findNearestMultiple(approximateTarget, 5, this.waypoints.keySet());
	final Coord me = this.dm.findNearest(self, this.waypoints.keySet());
	double bestCost = 999999.0;
	ArrayList<Coord> path = new ArrayList<>();
	/*
	 * if (waypoints.containsKey(me)) { Edge[] ed =
	 * waypoints.get(me).values().toArray(new Edge[waypoints.get(me).size()]);
	 * Arrays.sort(ed); path = ed[0].path;
	 */
	boolean reversed = false;
	for (final Coord test : near) {
	    if (this.waypoints.containsKey(test)) {
		Edge ed;
		if (this.waypoints.get(test).containsKey(me)) {
		    ed = this.waypoints.get(test).get(me);
		    reversed = true;
		} else if (this.waypoints.containsKey(me) && this.waypoints.get(me).containsKey(test)) {
		    ed = this.waypoints.get(me).get(test);
		} else {
		    continue;
		}
		if (ed.cost < bestCost) {
		    bestCost = ed.cost;
		    path = new ArrayList<>(ed.path);
		}
	    }
	}
	if (path.isEmpty()) {
	    return path;
	}
	if (reversed) {
	    Collections.reverse(path);
	}
	final ArrayList<Coord> getToPath = this.dm.findShortcutPath(self, path.toArray(new Coord[0]));
	if (getToPath.size() > 0) {
	    getToPath.remove(getToPath.size() - 1);
	    getToPath.addAll(path);
	    path = getToPath;
	}
	return path;
    }

    /**
     * If a creature is interrupted or obstructed on a "highway" path, it may need
     * to travel off the path to its goal. This method gets a straight-line path
     * back to the path to goal. It does not contain the "highway" path, only the
     * "on-ramp" to enter the ideal path.
     *
     * @param currentPosition the current position of the pathfinder, which is
     *                        probably not on the ideal path
     * @param path            the ideal path, probably returned by getKnownPath
     * @return an ArrayList of Coord that go from a cell adjacent to currentPosition
     *         to a Coord on or adjacent to path.
     */
    public ArrayList<Coord> goBackToPath(final Coord currentPosition, final ArrayList<Coord> path) {
	return this.dm.findShortcutPath(currentPosition, path.toArray(new Coord[0]));
    }

    public OrderedSet<Coord> getWaypoints() {
	return new OrderedSet<>(this.waypoints.keySet());
    }

    private static class Edge implements Comparable<Edge> {
	public Coord from;
	public Coord to;
	public ArrayList<Coord> path;
	public double cost;

	public Edge(final Coord from, final Coord to, final ArrayList<Coord> path, final double cost) {
	    this.from = from;
	    this.to = to;
	    this.path = path;
	    this.cost = cost;
	}

	@Override
	public boolean equals(final Object o) {
	    if (this == o) {
		return true;
	    }
	    if (o == null || this.getClass() != o.getClass()) {
		return false;
	    }
	    final Edge edge = (Edge) o;
	    if (Double.compare(edge.cost, this.cost) != 0) {
		return false;
	    }
	    if (!this.from.equals(edge.from)) {
		return false;
	    }
	    return this.to.equals(edge.to);
	}

	@Override
	public int hashCode() {
	    int result;
	    long temp;
	    result = this.from.hashCode();
	    result = 31 * result + this.to.hashCode();
	    temp = NumberTools.doubleToLongBits(this.cost);
	    result = 31 * result + (int) (temp ^ temp >>> 32);
	    return result;
	}

	/**
	 * Compares this object with the specified object for order. Returns a negative
	 * integer, zero, or a positive integer as this object is less than, equal to,
	 * or greater than the specified object.
	 *
	 * Note: this class has a natural ordering that is inconsistent with equals.
	 *
	 * @param o the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 * @throws NullPointerException if the specified object is null
	 * @throws ClassCastException   if the specified object's type prevents it from
	 *                              being compared to this object.
	 */
	@Override
	public int compareTo(final Edge o) {
	    return this.cost - o.cost > 0 ? 1 : this.cost - o.cost < 0 ? -1 : 0;
	}
    }
}
