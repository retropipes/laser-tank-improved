package squidpony.squidai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import squidpony.annotation.GwtIncompatible;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.FOVCache;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

/**
 * An AOE type that has an origin, a radius, an angle, and a span; it will blast
 * from the origin to a length equal to radius along the angle (in degrees),
 * moving somewhat around corners/obstacles, and also spread a total of span
 * degrees around the angle (a span of 90 will affect a full quadrant, centered
 * on angle). You can specify the RadiusType to Radius.DIAMOND for Manhattan
 * distance, RADIUS.SQUARE for Chebyshev, or RADIUS.CIRCLE for Euclidean.
 *
 * RADIUS.CIRCLE (Euclidean measurement) will produce the most real-looking
 * cones. This will produce doubles for its findArea() method which are greater
 * than 0.0 and less than or equal to 1.0.
 *
 * This class uses squidpony.squidgrid.FOV to create its area of effect. Created
 * by Tommy Ettinger on 7/13/2015.
 */
public class ConeAOE implements AOE {
    private FOV fov;
    private Coord origin;
    private double radius, angle, span;
    private double[][] map;
    private char[][] dungeon;
    private Radius radiusType;
    private Reach reach = new Reach(1, 1, Radius.SQUARE, null);

    public ConeAOE(final Coord origin, final Coord endCenter, final double span, final Radius radiusType) {
	this.fov = new FOV(FOV.RIPPLE_LOOSE);
	this.origin = origin;
	this.radius = radiusType.radius(origin.x, origin.y, endCenter.x, endCenter.y);
	this.angle = (Math.toDegrees(Math.atan2(endCenter.y - origin.y, endCenter.x - origin.x)) % 360.0 + 360.0)
		% 360.0;
//        this.startAngle = Math.abs((angle - span / 2.0) % 360.0);
//        this.endAngle = Math.abs((angle + span / 2.0) % 360.0);
	this.span = span;
	this.radiusType = radiusType;
    }

    public ConeAOE(final Coord origin, final int radius, final double angle, final double span,
	    final Radius radiusType) {
	this.fov = new FOV(FOV.RIPPLE_LOOSE);
	this.origin = origin;
	this.radius = radius;
//        this.startAngle = Math.abs((angle - span / 2.0) % 360.0);
//        this.endAngle = Math.abs((angle + span / 2.0) % 360.0);
	this.angle = angle;
	this.span = span;
	this.radiusType = radiusType;
    }

    @Override
    public Coord getOrigin() {
	return this.origin;
    }

    @Override
    public void setOrigin(final Coord origin) {
	this.origin = origin;
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

    public double getRadius() {
	return this.radius;
    }

    public void setRadius(final double radius) {
	this.radius = radius;
    }

    public double getAngle() {
	return this.angle;
    }

    public void setAngle(final double angle) {
	if (this.reach.limit == null || this.reach.limit == AimLimit.FREE
		|| this.reach.limit == AimLimit.EIGHT_WAY && (int) angle % 45 == 0
		|| this.reach.limit == AimLimit.DIAGONAL && (int) angle % 90 == 45
		|| this.reach.limit == AimLimit.ORTHOGONAL && (int) angle % 90 == 0) {
	    this.angle = angle;
//            this.startAngle = Math.abs((angle - span / 2.0) % 360.0);
//            this.endAngle = Math.abs((angle + span / 2.0) % 360.0);
	}
    }

    public void setEndCenter(final Coord endCenter) {
//        radius = radiusType.radius(origin.x, origin.y, endCenter.x, endCenter.y);
	if (AreaUtils.verifyLimit(this.reach.limit, this.origin, endCenter)) {
	    this.angle = (Math.toDegrees(Math.atan2(endCenter.y - this.origin.y, endCenter.x - this.origin.x)) % 360.0
		    + 360.0) % 360.0;
//            startAngle = Math.abs((angle - span / 2.0) % 360.0);
//            endAngle = Math.abs((angle + span / 2.0) % 360.0);
	}
    }

    public double getSpan() {
	return this.span;
    }

    public void setSpan(final double span) {
	this.span = span;
//        this.startAngle = Math.abs((angle - span / 2.0) % 360.0);
//        this.endAngle = Math.abs((angle + span / 2.0) % 360.0);
    }

    public Radius getRadiusType() {
	return this.radiusType;
    }

    public void setRadiusType(final Radius radiusType) {
	this.radiusType = radiusType;
    }

    @Override
    public void shift(final Coord aim) {
	this.setEndCenter(aim);
    }

    @Override
    public boolean mayContainTarget(final Collection<Coord> targets) {
	for (final Coord p : targets) {
	    if (this.radiusType.radius(this.origin.x, this.origin.y, p.x, p.y) <= this.radius) {
		double d = (this.angle - Math.toDegrees(Math.atan2(p.y - this.origin.y, p.x - this.origin.x)) % 360.0
			+ 360.0) % 360.0;
		if (d > 180) {
		    d = 360 - d;
		}
		if (d < this.span / 2.0) {
		    return true;
		}
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
	Coord t = exs[0];
	final double[][][] compositeMap = new double[ts.length][this.dungeon.length][this.dungeon[0].length];
	double tAngle; // , tStartAngle, tEndAngle;
	final char[][] dungeonCopy = new char[this.dungeon.length][this.dungeon[0].length];
	for (int i = 0; i < this.dungeon.length; i++) {
	    System.arraycopy(this.dungeon[i], 0, dungeonCopy[i], 0, this.dungeon[i].length);
	}
	double[][] tmpfov;
	Coord tempPt = Coord.get(0, 0);
	for (final Coord ex : exs) {
	    t = ex;
//            tRadius = radiusType.radius(origin.x, origin.y, t.x, t.y);
	    tAngle = (Math.toDegrees(Math.atan2(t.y - this.origin.y, t.x - this.origin.x)) % 360.0 + 360.0) % 360.0;
//            tStartAngle = Math.abs((tAngle - span / 2.0) % 360.0);
//            tEndAngle = Math.abs((tAngle + span / 2.0) % 360.0);
	    tmpfov = this.fov.calculateFOV(this.map, this.origin.x, this.origin.y, this.radius, this.radiusType, tAngle,
		    this.span);
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    tempPt = Coord.get(x, y);
		    dungeonCopy[x][y] = tmpfov[x][y] > 0.0
			    || !AreaUtils.verifyLimit(this.reach.limit, this.origin, tempPt) ? '!' : dungeonCopy[x][y];
		}
	    }
	}
	t = ts[0];
	DijkstraMap.Measurement dmm = DijkstraMap.Measurement.MANHATTAN;
	if (this.radiusType == Radius.SQUARE || this.radiusType == Radius.CUBE) {
	    dmm = DijkstraMap.Measurement.CHEBYSHEV;
	} else if (this.radiusType == Radius.CIRCLE || this.radiusType == Radius.SPHERE) {
	    dmm = DijkstraMap.Measurement.EUCLIDEAN;
	}
	for (int i = 0; i < ts.length; ++i) {
	    final DijkstraMap dm = new DijkstraMap(this.dungeon, dmm);
	    t = ts[i];
//            tRadius = radiusType.radius(origin.x, origin.y, t.x, t.y);
	    tAngle = (Math.toDegrees(Math.atan2(t.y - this.origin.y, t.x - this.origin.x)) % 360.0 + 360.0) % 360.0;
//            tStartAngle = Math.abs((tAngle - span / 2.0) % 360.0);
//            tEndAngle = Math.abs((tAngle + span / 2.0) % 360.0);
	    tmpfov = this.fov.calculateFOV(this.map, this.origin.x, this.origin.y, this.radius, this.radiusType, tAngle,
		    this.span);
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    if (tmpfov[x][y] > 0.0) {
			compositeMap[i][x][y] = dm.physicalMap[x][y];
		    } else {
			compositeMap[i][x][y] = DijkstraMap.WALL;
		    }
		}
	    }
	    if (compositeMap[i][t.x][t.y] > DijkstraMap.FLOOR) {
		for (int x = 0; x < this.dungeon.length; x++) {
		    Arrays.fill(compositeMap[i][x], 99999.0);
		}
		continue;
	    }
	    dm.initialize(compositeMap[i]);
	    dm.setGoal(t);
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
	Coord t = exs[0];
	final double[][][] compositeMap = new double[totalTargets][this.dungeon.length][this.dungeon[0].length];
	double tAngle; // , tStartAngle, tEndAngle;
	final char[][] dungeonCopy = new char[this.dungeon.length][this.dungeon[0].length],
		dungeonPriorities = new char[this.dungeon.length][this.dungeon[0].length];
	for (int i = 0; i < this.dungeon.length; i++) {
	    System.arraycopy(this.dungeon[i], 0, dungeonCopy[i], 0, this.dungeon[i].length);
	    Arrays.fill(dungeonPriorities[i], '#');
	}
	double[][] tmpfov;
	Coord tempPt = Coord.get(0, 0);
	for (final Coord ex : exs) {
	    t = ex;
	    tAngle = (Math.toDegrees(Math.atan2(t.y - this.origin.y, t.x - this.origin.x)) % 360.0 + 360.0) % 360.0;
//            tStartAngle = Math.abs((tAngle - span / 2.0) % 360.0);
//            tEndAngle = Math.abs((tAngle + span / 2.0) % 360.0);
	    tmpfov = this.fov.calculateFOV(this.map, this.origin.x, this.origin.y, this.radius, this.radiusType, tAngle,
		    this.span);
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    tempPt = Coord.get(x, y);
		    dungeonCopy[x][y] = tmpfov[x][y] > 0.0
			    || !AreaUtils.verifyLimit(this.reach.limit, this.origin, tempPt) ? '!' : dungeonCopy[x][y];
		}
	    }
	}
	t = pts[0];
	DijkstraMap.Measurement dmm = DijkstraMap.Measurement.MANHATTAN;
	if (this.radiusType == Radius.SQUARE || this.radiusType == Radius.CUBE) {
	    dmm = DijkstraMap.Measurement.CHEBYSHEV;
	} else if (this.radiusType == Radius.CIRCLE || this.radiusType == Radius.SPHERE) {
	    dmm = DijkstraMap.Measurement.EUCLIDEAN;
	}
	for (int i = 0; i < pts.length; ++i) {
	    final DijkstraMap dm = new DijkstraMap(this.dungeon, dmm);
	    t = pts[i];
	    tAngle = (Math.toDegrees(Math.atan2(t.y - this.origin.y, t.x - this.origin.x)) % 360.0 + 360.0) % 360.0;
//            tStartAngle = Math.abs((tAngle - span / 2.0) % 360.0);
//            tEndAngle = Math.abs((tAngle + span / 2.0) % 360.0);
	    tmpfov = this.fov.calculateFOV(this.map, this.origin.x, this.origin.y, this.radius, this.radiusType, tAngle,
		    this.span);
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    if (tmpfov[x][y] > 0.0) {
			compositeMap[i][x][y] = dm.physicalMap[x][y];
			dungeonPriorities[x][y] = this.dungeon[x][y];
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
	    dm.setGoal(t);
	    dm.scan(null);
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    compositeMap[i][x][y] = dm.gradientMap[x][y] < DijkstraMap.FLOOR && dungeonCopy[x][y] != '!'
			    ? dm.gradientMap[x][y]
			    : 399999.0;
		}
	    }
	}
	t = lts[0];
	for (int i = pts.length; i < totalTargets; ++i) {
	    final DijkstraMap dm = new DijkstraMap(this.dungeon, dmm);
	    t = lts[i - pts.length];
	    tAngle = (Math.toDegrees(Math.atan2(t.y - this.origin.y, t.x - this.origin.x)) % 360.0 + 360.0) % 360.0;
//            tStartAngle = Math.abs((tAngle - span / 2.0) % 360.0);
//            tEndAngle = Math.abs((tAngle + span / 2.0) % 360.0);
	    tmpfov = this.fov.calculateFOV(this.map, this.origin.x, this.origin.y, this.radius, this.radiusType, tAngle,
		    this.span);
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    if (tmpfov[x][y] > 0.0) {
			compositeMap[i][x][y] = dm.physicalMap[x][y];
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
	    dm.setGoal(t);
	    dm.scan(null);
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    compositeMap[i][x][y] = dm.gradientMap[x][y] < DijkstraMap.FLOOR && dungeonCopy[x][y] != '!'
			    && dungeonPriorities[x][y] != '#' ? dm.gradientMap[x][y] : 99999.0;
		}
	    }
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
     * 1; int maxEffect = (int)(radiusType.volume2D(radius) * Math.max(5, span) /
     * 360.0); double allowed = Math.toRadians(span / 2.0);
     * ArrayList<ArrayList<Coord>> locs = new
     * ArrayList<ArrayList<Coord>>(totalTargets);
     *
     * for(int i = 0; i < totalTargets; i++) { locs.add(new
     * ArrayList<Coord>(maxEffect)); } if(totalTargets == 1) return locs;
     *
     * int ctr = 0; if(radius < 1) { locs.get(totalTargets - 2).addAll(targets);
     * return locs; }
     *
     * double tmpAngle, ang; boolean[][] tested = new
     * boolean[dungeon.length][dungeon[0].length]; for (int x = 1; x <
     * dungeon.length - 1; x += radius) { BY_POINT: for (int y = 1; y <
     * dungeon[x].length - 1; y += radius) { ang = Math.atan2(y - origin.y, x -
     * origin.x); // between -pi and pi
     *
     * for(Coord ex : requiredExclusions) { if (radiusType.radius(x, y, ex.x, ex.y)
     * <= radius) { tmpAngle = Math.abs(ang - Math.atan2(ex.y - origin.y, ex.x -
     * origin.x)); if(tmpAngle > Math.PI) tmpAngle = PI2 - tmpAngle; if(tmpAngle <
     * allowed) continue BY_POINT; } } ctr = 0; for(Coord tgt : targets) { if
     * (radiusType.radius(x, y, tgt.x, tgt.y) <= radius) { tmpAngle = Math.abs(ang -
     * Math.atan2(tgt.y - origin.y, tgt.x - origin.x)); if(tmpAngle > Math.PI)
     * tmpAngle = PI2 - tmpAngle; if(tmpAngle < allowed) ctr++; } } if(ctr > 0)
     * locs.get(totalTargets - ctr).add(Coord.get(x, y)); } } Coord it; for(int t =
     * 0; t < totalTargets - 1; t++) { if(locs.get(t).size() > 0) { int numPoints =
     * locs.get(t).size(); for (int i = 0; i < numPoints; i++) { it =
     * locs.get(t).get(i); for (int x = Math.max(1, it.x - (int)(radius) / 2); x <
     * it.x + (radius + 1) / 2 && x < dungeon.length - 1; x++) { BY_POINT: for (int
     * y = Math.max(1, it.y - (int)(radius) / 2); y <= it.y + (radius - 1) / 2 && y
     * < dungeon[0].length - 1; y++) { if(tested[x][y]) continue; tested[x][y] =
     * true; ang = Math.atan2(y - origin.y, x - origin.x); // between -pi and pi
     * for(Coord ex : requiredExclusions) { if (radiusType.radius(x, y, ex.x, ex.y)
     * <= radius) { tmpAngle = Math.abs(ang - Math.atan2(ex.y - origin.y, ex.x -
     * origin.x)); if(tmpAngle > Math.PI) tmpAngle = PI2 - tmpAngle; if(tmpAngle <
     * allowed) continue BY_POINT; } }
     *
     * ctr = 0; for(Coord tgt : targets) { if (radiusType.radius(x, y, tgt.x, tgt.y)
     * <= radius) { tmpAngle = Math.abs(ang - Math.atan2(tgt.y - origin.y, tgt.x -
     * origin.x)); if(tmpAngle > Math.PI) tmpAngle = PI2 - tmpAngle; if(tmpAngle <
     * allowed) ctr++; } } if(ctr > 0) locs.get(totalTargets - ctr).add(Coord.get(x,
     * y)); } } } } } return locs; }
     */
    @Override
    public void setMap(final char[][] map) {
	this.map = DungeonUtility.generateResistances(map);
	this.dungeon = map;
    }

    @Override
    public OrderedMap<Coord, Double> findArea() {
	final OrderedMap<Coord, Double> r = AreaUtils.arrayToHashMap(this.fov.calculateFOV(this.map, this.origin.x,
		this.origin.y, this.radius, this.radiusType, this.angle, this.span));
	r.remove(this.origin);
	return r;
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
	this.fov = cache;
    }
}
