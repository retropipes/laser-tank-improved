package squidpony.squidai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;

import squidpony.annotation.GwtIncompatible;
import squidpony.squidgrid.FOVCache;
import squidpony.squidgrid.LOS;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

/**
 * Beam Area of Effect that affects an slightly expanded (Elias) line from a
 * given origin Coord out to a given length, plus an optional radius of cells
 * around the path of the line, while respecting obstacles in its path and
 * possibly stopping if obstructed. There are several ways to specify the
 * BeamAOE's direction and length, including specifying an endpoint or
 * specifying an angle in degrees and a distance to the end of the line (without
 * the radius included). You can specify the RadiusType to Radius.DIAMOND for
 * Manhattan distance, RADIUS.SQUARE for Chebyshev, or RADIUS.CIRCLE for
 * Euclidean.
 *
 * You may want the LineAOE class instead of this. LineAOE travels
 * point-to-point and does not restrict length, while BeamAOE travels a specific
 * length (and may have a radius, like LineAOE) but then stops only after the
 * travel down the length and radius has reached its end. This difference is
 * relevant if a game has effects that have a definite area measured in a
 * rectangle or elongated pillbox shape, such as a "20-foot-wide bolt of
 * lightning, 100 feet long." BeamAOE is more suitable for that effect, while
 * LineAOE may be more suitable for things like focused lasers that pass through
 * small (likely fleshy) obstacles but stop after hitting the aimed-at target.
 *
 * BeamAOE will strike a small area behind the user and in the opposite
 * direction of the target if the radius is greater than 0. This behavior may be
 * altered in a future version.
 *
 * This will produce doubles for its findArea() method which are equal to 1.0.
 *
 * This class uses squidpony.squidmath.Elias and squidpony.squidai.DijkstraMap
 * to create its area of effect. Created by Tommy Ettinger on 7/14/2015.
 */
public class BeamAOE implements AOE {
    private Coord origin, end;
    private int radius;
    private int length;
    private char[][] dungeon;
    private final DijkstraMap dijkstra;
    private Radius rt;
    private final LOS los;
    private Reach reach = new Reach(1, 1, Radius.SQUARE, null);

    public BeamAOE(final Coord origin, final Coord end) {
	this.dijkstra = new DijkstraMap();
	this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
	this.rt = Radius.SQUARE;
	this.origin = origin;
	this.end = end;
	this.length = (int) Math.round(this.rt.radius(origin.x, origin.y, end.x, end.y));
	this.reach.maxDistance = this.length;
	this.radius = 0;
	this.los = new LOS(LOS.THICK);
    }

    public BeamAOE(final Coord origin, final Coord end, final int radius) {
	this.dijkstra = new DijkstraMap();
	this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
	this.rt = Radius.SQUARE;
	this.origin = origin;
	this.end = end;
	this.radius = radius;
	this.length = (int) Math.round(this.rt.radius(origin.x, origin.y, end.x, end.y));
	this.reach.maxDistance = this.length;
	this.los = new LOS(LOS.THICK);
    }

    public BeamAOE(final Coord origin, final Coord end, final int radius, final Radius radiusType) {
	this.dijkstra = new DijkstraMap();
	this.rt = radiusType;
	switch (radiusType) {
	case OCTAHEDRON:
	case DIAMOND:
	    this.dijkstra.measurement = DijkstraMap.Measurement.MANHATTAN;
	    break;
	case CUBE:
	case SQUARE:
	    this.dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
	    break;
	default:
	    this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
	    break;
	}
	this.origin = origin;
	this.end = end;
	this.radius = radius;
	this.length = (int) Math.round(this.rt.radius(origin.x, origin.y, end.x, end.y));
	this.reach.maxDistance = this.length;
	this.los = new LOS(LOS.THICK);
    }

    public BeamAOE(final Coord origin, final double angle, final int length) {
	this.dijkstra = new DijkstraMap();
	this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
	this.rt = Radius.SQUARE;
	this.origin = origin;
	final double theta = Math.toRadians(angle);
	this.end = Coord.get((int) Math.round(Math.cos(theta) * length) + origin.x,
		(int) Math.round(Math.sin(theta) * length) + origin.y);
	this.length = length;
	this.reach.maxDistance = this.length;
	this.radius = 0;
	this.los = new LOS(LOS.THICK);
    }

    public BeamAOE(final Coord origin, final double angle, final int length, final int radius) {
	this.dijkstra = new DijkstraMap();
	this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
	this.rt = Radius.SQUARE;
	this.origin = origin;
	final double theta = Math.toRadians(angle);
	this.end = Coord.get((int) Math.round(Math.cos(theta) * length) + origin.x,
		(int) Math.round(Math.sin(theta) * length) + origin.y);
	this.radius = radius;
	this.length = length;
	this.reach.maxDistance = this.length;
	this.los = new LOS(LOS.THICK);
    }

    public BeamAOE(final Coord origin, final double angle, final int length, final int radius,
	    final Radius radiusType) {
	this.dijkstra = new DijkstraMap();
	this.rt = radiusType;
	switch (radiusType) {
	case OCTAHEDRON:
	case DIAMOND:
	    this.dijkstra.measurement = DijkstraMap.Measurement.MANHATTAN;
	    break;
	case CUBE:
	case SQUARE:
	    this.dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
	    break;
	default:
	    this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
	    break;
	}
	this.origin = origin;
	final double theta = Math.toRadians(angle);
	this.end = Coord.get((int) Math.round(Math.cos(theta) * length) + origin.x,
		(int) Math.round(Math.sin(theta) * length) + origin.y);
	this.radius = radius;
	this.length = length;
	this.reach.maxDistance = this.length;
	this.los = new LOS(LOS.THICK);
    }

    private double[][] initDijkstra() {
	this.los.isReachable(this.dungeon, this.origin.x, this.origin.y, this.end.x, this.end.y, this.rt);
	final Queue<Coord> lit = this.los.getLastPath();
	this.dijkstra.initialize(this.dungeon);
	for (final Coord p : lit) {
	    this.dijkstra.setGoal(p);
	}
	if (this.radius == 0) {
	    return this.dijkstra.gradientMap;
	}
	return this.dijkstra.partialScan(this.radius, null);
    }

    @Override
    public Coord getOrigin() {
	return this.origin;
    }

    @Override
    public void setOrigin(final Coord origin) {
	this.origin = origin;
	this.dijkstra.resetMap();
	this.dijkstra.clearGoals();
    }

    @Override
    public AimLimit getLimitType() {
	return this.reach.limit;
    }

    @Override
    public int getMinRange() {
	return this.reach.minDistance;
    }

    @Override
    public int getMaxRange() {
	return this.reach.maxDistance;
    }

    @Override
    public Radius getMetric() {
	return this.reach.metric;
    }

    /**
     * Gets the same values returned by getLimitType(), getMinRange(),
     * getMaxRange(), and getMetric() bundled into one Reach object.
     *
     * @return a non-null Reach object.
     */
    @Override
    public Reach getReach() {
	return this.reach;
    }

    @Override
    public void setLimitType(final AimLimit limitType) {
	this.reach.limit = limitType;
    }

    @Override
    public void setMinRange(final int minRange) {
	this.reach.minDistance = minRange;
    }

    @Override
    public void setMaxRange(final int maxRange) {
	this.reach.maxDistance = maxRange;
	this.length = maxRange;
    }

    @Override
    public void setMetric(final Radius metric) {
	this.reach.metric = metric;
    }

    /**
     * Sets the same values as setLimitType(), setMinRange(), setMaxRange(), and
     * setMetric() using one Reach object.
     *
     * @param reach a non-null Reach object.
     */
    @Override
    public void setReach(final Reach reach) {
	if (reach != null) {
	    this.reach = reach;
	}
    }

    public Coord getEnd() {
	return this.end;
    }

    public void setEnd(final Coord end) {
	if (AreaUtils.verifyReach(this.reach, this.origin, end)) {
	    this.end = this.rt.extend(this.origin, end, this.length, false, this.dungeon.length,
		    this.dungeon[0].length);
	}
    }

    public int getRadius() {
	return this.radius;
    }

    public void setRadius(final int radius) {
	this.radius = radius;
    }

    public Radius getRadiusType() {
	return this.rt;
    }

    public void setRadiusType(final Radius radiusType) {
	this.rt = radiusType;
	switch (radiusType) {
	case OCTAHEDRON:
	case DIAMOND:
	    this.dijkstra.measurement = DijkstraMap.Measurement.MANHATTAN;
	    break;
	case CUBE:
	case SQUARE:
	    this.dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
	    break;
	default:
	    this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
	    break;
	}
    }

    @Override
    public void shift(final Coord aim) {
	this.setEnd(aim);
    }

    @Override
    public boolean mayContainTarget(final Collection<Coord> targets) {
	for (final Coord p : targets) {
	    if (this.rt.radius(this.origin.x, this.origin.y, p.x, p.y)
		    + this.rt.radius(this.end.x, this.end.y, p.x, p.y)
		    - this.rt.radius(this.origin.x, this.origin.y, this.end.x, this.end.y) <= 3.0 + this.radius) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public OrderedMap<Coord, ArrayList<Coord>> idealLocations(final Collection<Coord> targets,
	    Collection<Coord> requiredExclusions) {
	if (targets == null) {
	    return new OrderedMap<>();
	}
	if (requiredExclusions == null) {
	    requiredExclusions = new OrderedSet<>();
	}
	// requiredExclusions.remove(origin);
	final int totalTargets = targets.size();
	final OrderedMap<Coord, ArrayList<Coord>> bestPoints = new OrderedMap<>(totalTargets * 8);
	if (totalTargets == 0) {
	    return bestPoints;
	}
	final Coord[] ts = targets.toArray(new Coord[targets.size()]);
	final Coord[] exs = requiredExclusions.toArray(new Coord[requiredExclusions.size()]);
	Coord t;
	final double[][][] compositeMap = new double[ts.length][this.dungeon.length][this.dungeon[0].length];
	final char[][] dungeonCopy = new char[this.dungeon.length][this.dungeon[0].length];
	for (int i = 0; i < this.dungeon.length; i++) {
	    System.arraycopy(this.dungeon[i], 0, dungeonCopy[i], 0, this.dungeon[i].length);
	}
	final DijkstraMap dt = new DijkstraMap(this.dungeon, this.dijkstra.measurement);
	final double[][] resMap = DungeonUtility.generateResistances(this.dungeon);
	Coord tempPt = Coord.get(0, 0);
	for (final Coord ex : exs) {
	    t = this.rt.extend(this.origin, ex, this.length, false, this.dungeon.length, this.dungeon[0].length);
	    dt.resetMap();
	    dt.clearGoals();
	    this.los.isReachable(resMap, this.origin.x, this.origin.y, t.x, t.y, this.rt);
	    final Queue<Coord> lit = this.los.getLastPath();
	    for (final Coord p : lit) {
		dt.setGoal(p);
	    }
	    if (this.radius > 0) {
		dt.partialScan(this.radius, null);
	    }
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    tempPt = Coord.get(x, y);
		    dungeonCopy[x][y] = dt.gradientMap[x][y] < DijkstraMap.FLOOR
			    || !AreaUtils.verifyReach(this.reach, this.origin, tempPt) ? '!' : dungeonCopy[x][y];
		}
	    }
	}
	// t = rt.extend(origin, ts[0], length, false, dungeon.length,
	// dungeon[0].length);
	for (int i = 0; i < ts.length; ++i) {
	    final DijkstraMap dm = new DijkstraMap(this.dungeon, this.dijkstra.measurement);
	    t = this.rt.extend(this.origin, ts[i], this.length, false, this.dungeon.length, this.dungeon[0].length);
	    dt.resetMap();
	    dt.clearGoals();
	    this.los.isReachable(resMap, this.origin.x, this.origin.y, t.x, t.y, this.rt);
	    final Queue<Coord> lit = this.los.getLastPath();
	    for (final Coord p : lit) {
		dt.setGoal(p);
	    }
	    if (this.radius > 0) {
		dt.partialScan(this.radius, null);
	    }
	    double dist = 0.0;
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    if (dt.gradientMap[x][y] < DijkstraMap.FLOOR) {
			dist = this.reach.metric.radius(this.origin.x, this.origin.y, x, y);
			if (dist <= this.reach.maxDistance + this.radius
				&& dist >= this.reach.minDistance - this.radius) {
			    compositeMap[i][x][y] = dm.physicalMap[x][y];
			} else {
			    compositeMap[i][x][y] = DijkstraMap.WALL;
			}
		    } else {
			compositeMap[i][x][y] = DijkstraMap.WALL;
		    }
		}
	    }
	    if (compositeMap[i][ts[i].x][ts[i].y] > DijkstraMap.FLOOR) {
		for (int x = 0; x < this.dungeon.length; x++) {
		    Arrays.fill(compositeMap[i][x], 99999.0);
		}
		continue;
	    }
	    dm.initialize(compositeMap[i]);
	    dm.setGoal(ts[i]);
	    dm.scan(null);
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    compositeMap[i][x][y] = dm.gradientMap[x][y] < DijkstraMap.FLOOR && dungeonCopy[x][y] != '!'
			    ? dm.gradientMap[x][y]
			    : 99999.0;
		}
	    }
	}
	double bestQuality = 99999 * ts.length;
	final double[][] qualityMap = new double[this.dungeon.length][this.dungeon[0].length];
	for (int x = 0; x < qualityMap.length; x++) {
	    for (int y = 0; y < qualityMap[x].length; y++) {
		qualityMap[x][y] = 0.0;
		long bits = 0;
		for (int i = 0; i < ts.length; ++i) {
		    qualityMap[x][y] += compositeMap[i][x][y];
		    if (compositeMap[i][x][y] < 99999.0 && i < 63) {
			bits |= 1 << i;
		    }
		}
		if (qualityMap[x][y] < bestQuality) {
		    final ArrayList<Coord> ap = new ArrayList<>();
		    for (int i = 0; i < ts.length && i < 63; ++i) {
			if ((bits & 1 << i) != 0) {
			    ap.add(ts[i]);
			}
		    }
		    if (ap.size() > 0) {
			bestQuality = qualityMap[x][y];
			bestPoints.clear();
			bestPoints.put(Coord.get(x, y), ap);
		    }
		} else if (qualityMap[x][y] == bestQuality) {
		    final ArrayList<Coord> ap = new ArrayList<>();
		    for (int i = 0; i < ts.length && i < 63; ++i) {
			if ((bits & 1 << i) != 0) {
			    ap.add(ts[i]);
			}
		    }
		    if (ap.size() > 0) {
			bestPoints.put(Coord.get(x, y), ap);
		    }
		}
	    }
	}
	return bestPoints;
    }

    @Override
    public OrderedMap<Coord, ArrayList<Coord>> idealLocations(final Collection<Coord> priorityTargets,
	    final Collection<Coord> lesserTargets, Collection<Coord> requiredExclusions) {
	if (priorityTargets == null) {
	    return this.idealLocations(lesserTargets, requiredExclusions);
	}
	if (requiredExclusions == null) {
	    requiredExclusions = new OrderedSet<>();
	}
	// requiredExclusions.remove(origin);
	final int totalTargets = priorityTargets.size() + lesserTargets.size();
	final OrderedMap<Coord, ArrayList<Coord>> bestPoints = new OrderedMap<>(totalTargets * 8);
	if (totalTargets == 0) {
	    return bestPoints;
	}
	final Coord[] pts = priorityTargets.toArray(new Coord[priorityTargets.size()]);
	final Coord[] lts = lesserTargets.toArray(new Coord[lesserTargets.size()]);
	final Coord[] exs = requiredExclusions.toArray(new Coord[requiredExclusions.size()]);
	Coord t;// = rt.extend(origin, exs[0], length, false, dungeon.length,
		// dungeon[0].length);
	final double[][][] compositeMap = new double[totalTargets][this.dungeon.length][this.dungeon[0].length];
	final char[][] dungeonCopy = new char[this.dungeon.length][this.dungeon[0].length],
		dungeonPriorities = new char[this.dungeon.length][this.dungeon[0].length];
	for (int i = 0; i < this.dungeon.length; i++) {
	    System.arraycopy(this.dungeon[i], 0, dungeonCopy[i], 0, this.dungeon[i].length);
	    Arrays.fill(dungeonPriorities[i], '#');
	}
	final DijkstraMap dt = new DijkstraMap(this.dungeon, this.dijkstra.measurement);
	final double[][] resMap = DungeonUtility.generateResistances(this.dungeon);
	Coord tempPt = Coord.get(0, 0);
	for (final Coord ex : exs) {
	    t = this.rt.extend(this.origin, ex, this.length, false, this.dungeon.length, this.dungeon[0].length);
	    dt.resetMap();
	    dt.clearGoals();
	    this.los.isReachable(resMap, this.origin.x, this.origin.y, t.x, t.y, this.rt);
	    final Queue<Coord> lit = this.los.getLastPath();
	    for (final Coord p : lit) {
		dt.setGoal(p);
	    }
	    if (this.radius > 0) {
		dt.partialScan(this.radius, null);
	    }
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    tempPt = Coord.get(x, y);
		    dungeonCopy[x][y] = dt.gradientMap[x][y] < DijkstraMap.FLOOR
			    || !AreaUtils.verifyReach(this.reach, this.origin, tempPt) ? '!' : dungeonCopy[x][y];
		}
	    }
	}
	t = this.rt.extend(this.origin, pts[0], this.length, false, this.dungeon.length, this.dungeon[0].length);
	for (int i = 0; i < pts.length; ++i) {
	    final DijkstraMap dm = new DijkstraMap(this.dungeon, this.dijkstra.measurement);
	    t = this.rt.extend(this.origin, pts[i], this.length, false, this.dungeon.length, this.dungeon[0].length);
	    dt.resetMap();
	    dt.clearGoals();
	    this.los.isReachable(resMap, this.origin.x, this.origin.y, t.x, t.y, this.rt);
	    final Queue<Coord> lit = this.los.getLastPath();
	    for (final Coord p : lit) {
		dt.setGoal(p);
	    }
	    if (this.radius > 0) {
		dt.partialScan(this.radius, null);
	    }
	    double dist = 0.0;
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    if (dt.gradientMap[x][y] < DijkstraMap.FLOOR) {
			dist = this.reach.metric.radius(this.origin.x, this.origin.y, x, y);
			if (dist <= this.reach.maxDistance + this.radius
				&& dist >= this.reach.minDistance - this.radius) {
			    compositeMap[i][x][y] = dm.physicalMap[x][y];
			    dungeonPriorities[x][y] = this.dungeon[x][y];
			} else {
			    compositeMap[i][x][y] = DijkstraMap.WALL;
			}
		    } else {
			compositeMap[i][x][y] = DijkstraMap.WALL;
		    }
		}
	    }
	    if (compositeMap[i][pts[i].x][pts[i].y] > DijkstraMap.FLOOR) {
		for (int x = 0; x < this.dungeon.length; x++) {
		    Arrays.fill(compositeMap[i][x], 399999.0);
		}
		continue;
	    }
	    dm.initialize(compositeMap[i]);
	    dm.setGoal(pts[i]);
	    dm.scan(null);
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    compositeMap[i][x][y] = dm.gradientMap[x][y] < DijkstraMap.FLOOR && dungeonCopy[x][y] != '!'
			    ? dm.gradientMap[x][y]
			    : 399999.0;
		}
	    }
	    dm.resetMap();
	    dm.clearGoals();
	}
	t = this.rt.extend(this.origin, lts[0], this.length, false, this.dungeon.length, this.dungeon[0].length);
	for (int i = pts.length; i < totalTargets; ++i) {
	    final DijkstraMap dm = new DijkstraMap(this.dungeon, this.dijkstra.measurement);
	    t = this.rt.extend(this.origin, lts[i - pts.length], this.length, false, this.dungeon.length,
		    this.dungeon[0].length);
	    dt.resetMap();
	    dt.clearGoals();
	    this.los.isReachable(resMap, this.origin.x, this.origin.y, t.x, t.y, this.rt);
	    final Queue<Coord> lit = this.los.getLastPath();
	    for (final Coord p : lit) {
		dt.setGoal(p);
	    }
	    if (this.radius > 0) {
		dt.partialScan(this.radius, null);
	    }
	    double dist = 0.0;
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    if (dt.gradientMap[x][y] < DijkstraMap.FLOOR) {
			dist = this.reach.metric.radius(this.origin.x, this.origin.y, x, y);
			if (dist <= this.reach.maxDistance + this.radius
				&& dist >= this.reach.minDistance - this.radius) {
			    compositeMap[i][x][y] = dm.physicalMap[x][y];
			} else {
			    compositeMap[i][x][y] = DijkstraMap.WALL;
			}
		    } else {
			compositeMap[i][x][y] = DijkstraMap.WALL;
		    }
		}
	    }
	    if (compositeMap[i][lts[i - pts.length].x][lts[i - pts.length].y] > DijkstraMap.FLOOR) {
		for (int x = 0; x < this.dungeon.length; x++) {
		    Arrays.fill(compositeMap[i][x], 99999.0);
		}
		continue;
	    }
	    dm.initialize(compositeMap[i]);
	    dm.setGoal(lts[i - pts.length]);
	    dm.scan(null);
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    compositeMap[i][x][y] = dm.gradientMap[x][y] < DijkstraMap.FLOOR && dungeonCopy[x][y] != '!'
			    && dungeonPriorities[x][y] != '#' ? dm.gradientMap[x][y] : 99999.0;
		}
	    }
	    dm.resetMap();
	    dm.clearGoals();
	}
	double bestQuality = 99999 * lts.length + 399999 * pts.length;
	final double[][] qualityMap = new double[this.dungeon.length][this.dungeon[0].length];
	for (int x = 0; x < qualityMap.length; x++) {
	    for (int y = 0; y < qualityMap[x].length; y++) {
		qualityMap[x][y] = 0.0;
		long pbits = 0, lbits = 0;
		for (int i = 0; i < pts.length; ++i) {
		    qualityMap[x][y] += compositeMap[i][x][y];
		    if (compositeMap[i][x][y] < 399999.0 && i < 63) {
			pbits |= 1 << i;
		    }
		}
		for (int i = pts.length; i < totalTargets; ++i) {
		    qualityMap[x][y] += compositeMap[i][x][y];
		    if (compositeMap[i][x][y] < 99999.0 && i < 63) {
			lbits |= 1 << i;
		    }
		}
		if (qualityMap[x][y] < bestQuality) {
		    final ArrayList<Coord> ap = new ArrayList<>();
		    for (int i = 0; i < pts.length && i < 63; ++i) {
			if ((pbits & 1 << i) != 0) {
			    ap.add(pts[i]);
			}
		    }
		    for (int i = pts.length; i < totalTargets && i < 63; ++i) {
			if ((lbits & 1 << i) != 0) {
			    ap.add(lts[i - pts.length]);
			}
		    }
		    if (ap.size() > 0) {
			bestQuality = qualityMap[x][y];
			bestPoints.clear();
			bestPoints.put(Coord.get(x, y), ap);
		    }
		} else if (qualityMap[x][y] == bestQuality) {
		    final ArrayList<Coord> ap = new ArrayList<>();
		    for (int i = 0; i < pts.length && i < 63; ++i) {
			if ((pbits & 1 << i) != 0) {
			    ap.add(pts[i]);
			    ap.add(pts[i]);
			    ap.add(pts[i]);
			    ap.add(pts[i]);
			}
		    }
		    for (int i = pts.length; i < totalTargets && i < 63; ++i) {
			if ((lbits & 1 << i) != 0) {
			    ap.add(lts[i - pts.length]);
			}
		    }
		    if (ap.size() > 0) {
			bestPoints.put(Coord.get(x, y), ap);
		    }
		}
	    }
	}
	return bestPoints;
    }

    /*
     * @Override public ArrayList<ArrayList<Coord>> idealLocations(Set<Coord>
     * targets, Set<Coord> requiredExclusions) { int totalTargets = targets.size() +
     * 1; int volume = (int)(rt.radius(1, 1, dungeon.length - 2, dungeon[0].length -
     * 2) * radius * 2.1); ArrayList<ArrayList<Coord>> locs = new
     * ArrayList<ArrayList<Coord>>(totalTargets); for(int i = 0; i < totalTargets;
     * i++) { locs.add(new ArrayList<Coord>(volume)); } if(totalTargets == 1) return
     * locs;
     *
     * int ctr = 0;
     *
     * boolean[][] tested = new boolean[dungeon.length][dungeon[0].length]; for (int
     * x = 1; x < dungeon.length - 1; x += radius) { for (int y = 1; y <
     * dungeon[x].length - 1; y += radius) {
     *
     * if(mayContainTarget(requiredExclusions, x, y)) continue; ctr = 0; for(Coord
     * tgt : targets) { if(rt.radius(origin.x, origin.y, tgt.x, tgt.y) +
     * rt.radius(end.x, end.y, tgt.x, tgt.y) - rt.radius(origin.x, origin.y, end.x,
     * end.y) <= 3.0 + radius) ctr++; } if(ctr > 0) locs.get(totalTargets -
     * ctr).add(Coord.get(x, y)); } } Coord it; for(int t = 0; t < totalTargets - 1;
     * t++) { if(locs.get(t).size() > 0) { int numPoints = locs.get(t).size(); for
     * (int i = 0; i < numPoints; i++) { it = locs.get(t).get(i); for (int x =
     * Math.max(1, it.x - radius / 2); x < it.x + (radius + 1) / 2 && x <
     * dungeon.length - 1; x++) { for (int y = Math.max(1, it.y - radius / 2); y <=
     * it.y + (radius - 1) / 2 && y < dungeon[0].length - 1; y++) { if(tested[x][y])
     * continue; tested[x][y] = true;
     *
     * if(mayContainTarget(requiredExclusions, x, y)) continue;
     *
     * ctr = 0; for(Coord tgt : targets) { if(rt.radius(origin.x, origin.y, tgt.x,
     * tgt.y) + rt.radius(end.x, end.y, tgt.x, tgt.y) - rt.radius(origin.x,
     * origin.y, end.x, end.y) <= 3.0 + radius) ctr++; } if(ctr > 0)
     * locs.get(totalTargets - ctr).add(Coord.get(x, y)); } } } } } return locs; }
     */
    @Override
    public void setMap(final char[][] map) {
	this.dungeon = map;
	this.end = this.rt.extend(this.origin, this.end, this.length, false, map.length, map[0].length);
	this.dijkstra.resetMap();
	this.dijkstra.clearGoals();
    }

    @Override
    public OrderedMap<Coord, Double> findArea() {
	final double[][] dmap = this.initDijkstra();
	dmap[this.origin.x][this.origin.y] = DijkstraMap.DARK;
	this.dijkstra.resetMap();
	this.dijkstra.clearGoals();
	return AreaUtils.dijkstraToHashMap(dmap);
    }

    /**
     * If you use FOVCache to pre-compute FOV maps for a level, you can share the
     * speedup from using the cache with some AOE implementations that rely on FOV.
     * Not all implementations need to actually make use of the cache, but those
     * that use FOV for calculations should benefit. The cache parameter this
     * receives should have completed its calculations, which can be confirmed by
     * calling awaitCache(). Ideally, the FOVCache will have done its initial
     * calculations in another thread while the previous level or menu was being
     * displayed, and awaitCache() will only be a formality.
     *
     * @param cache The FOVCache for the current level; can be null to stop using
     *              the cache
     */
    @GwtIncompatible
    @Override
    public void setCache(final FOVCache cache) {
    }
}
