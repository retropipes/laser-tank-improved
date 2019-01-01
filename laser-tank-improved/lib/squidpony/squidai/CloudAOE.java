package squidpony.squidai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import squidpony.annotation.GwtIncompatible;
import squidpony.squidgrid.FOVCache;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.Spill;
import squidpony.squidmath.Coord;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.RNG;

/**
 * An AOE type that has a center and a volume, and will randomly expand in all
 * directions until it reaches volume or cannot expand further. Specify the
 * RadiusType as Radius.DIAMOND for Manhattan distance (and the best results),
 * RADIUS.SQUARE for Chebyshev, or RADIUS.CIRCLE for Euclidean. You can specify
 * a seed for the RNG and a fresh RNG will be used for all random expansion; the
 * RNG will reset to the specified seed after each generation so the same
 * CloudAOE can be used in different places by just changing the center. You can
 * cause the CloudAOE to not reset after generating each time by using
 * setExpanding(true) and cause it to reset after the next generation by setting
 * it back to the default of false. If expanding is true, then multiple calls to
 * findArea with the same center and larger volumes will produce more solid
 * clumps of affected area with fewer gaps, and can be spaced out over multiple
 * calls.
 *
 * This will produce doubles for its findArea() method which are equal to 1.0.
 *
 * This class uses squidpony.squidgrid.Spill to create its area of effect.
 * Created by Tommy Ettinger on 7/13/2015.
 */
public class CloudAOE implements AOE {
    private final Spill spill;
    private Coord center, origin = null;
    private int volume;
    private final long seed;
    private boolean expanding;
    private Radius rt;
    private Reach reach = new Reach(1, 1, Radius.SQUARE, null);
    private char[][] dungeon;

    public CloudAOE(final Coord center, final int volume, final Radius radiusType) {
	final LightRNG l = new LightRNG();
	this.seed = l.getState();
	this.spill = new Spill(new RNG(l));
	this.center = center;
	this.volume = volume;
	this.expanding = false;
	this.rt = radiusType;
	switch (radiusType) {
	case SPHERE:
	case CIRCLE:
	    this.spill.measurement = Spill.Measurement.EUCLIDEAN;
	    break;
	case CUBE:
	case SQUARE:
	    this.spill.measurement = Spill.Measurement.CHEBYSHEV;
	    break;
	default:
	    this.spill.measurement = Spill.Measurement.MANHATTAN;
	    break;
	}
    }

    public CloudAOE(final Coord center, final int volume, final Radius radiusType, final int minRange,
	    final int maxRange) {
	final LightRNG l = new LightRNG();
	this.seed = l.getState();
	this.spill = new Spill(new RNG(l));
	this.center = center;
	this.volume = volume;
	this.expanding = false;
	this.rt = radiusType;
	this.reach.minDistance = minRange;
	this.reach.maxDistance = maxRange;
	switch (radiusType) {
	case SPHERE:
	case CIRCLE:
	    this.spill.measurement = Spill.Measurement.EUCLIDEAN;
	    break;
	case CUBE:
	case SQUARE:
	    this.spill.measurement = Spill.Measurement.CHEBYSHEV;
	    break;
	default:
	    this.spill.measurement = Spill.Measurement.MANHATTAN;
	    break;
	}
    }

    public CloudAOE(final Coord center, final int volume, final Radius radiusType, final long rngSeed) {
	this.seed = rngSeed;
	this.spill = new Spill(new RNG(new LightRNG(rngSeed)));
	this.center = center;
	this.volume = volume;
	this.expanding = false;
	this.rt = radiusType;
	switch (radiusType) {
	case SPHERE:
	case CIRCLE:
	    this.spill.measurement = Spill.Measurement.EUCLIDEAN;
	    break;
	case CUBE:
	case SQUARE:
	    this.spill.measurement = Spill.Measurement.CHEBYSHEV;
	    break;
	default:
	    this.spill.measurement = Spill.Measurement.MANHATTAN;
	    break;
	}
    }

    public CloudAOE(final Coord center, final int volume, final Radius radiusType, final long rngSeed,
	    final int minRange, final int maxRange) {
	this.seed = rngSeed;
	this.spill = new Spill(new RNG(new LightRNG(rngSeed)));
	this.center = center;
	this.volume = volume;
	this.expanding = false;
	this.rt = radiusType;
	switch (radiusType) {
	case SPHERE:
	case CIRCLE:
	    this.spill.measurement = Spill.Measurement.EUCLIDEAN;
	    break;
	case CUBE:
	case SQUARE:
	    this.spill.measurement = Spill.Measurement.CHEBYSHEV;
	    break;
	default:
	    this.spill.measurement = Spill.Measurement.MANHATTAN;
	    break;
	}
	this.reach.minDistance = minRange;
	this.reach.maxDistance = maxRange;
    }

    public Coord getCenter() {
	return this.center;
    }

    public void setCenter(final Coord center) {
	if (this.dungeon != null && center.isWithin(this.dungeon.length, this.dungeon[0].length)
		&& AreaUtils.verifyReach(this.reach, this.origin, center)) {
	    this.center = center;
	}
    }

    public int getVolume() {
	return this.volume;
    }

    public void setVolume(final int volume) {
	this.volume = volume;
    }

    public Radius getRadiusType() {
	return this.rt;
    }

    public void setRadiusType(final Radius radiusType) {
	this.rt = radiusType;
	switch (radiusType) {
	case SPHERE:
	case CIRCLE:
	    break;
	case CUBE:
	case SQUARE:
	    break;
	default:
	    break;
	}
    }

    @Override
    public void shift(final Coord aim) {
	this.setCenter(aim);
    }

    @Override
    public boolean mayContainTarget(final Collection<Coord> targets) {
	for (final Coord p : targets) {
	    if (this.rt.radius(this.center.x, this.center.y, p.x, p.y) <= Math.sqrt(this.volume) * 0.75) {
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
	if (totalTargets == 0 || this.volume <= 0) {
	    return bestPoints;
	}
	if (this.volume == 1) {
	    for (final Coord p : targets) {
		final ArrayList<Coord> ap = new ArrayList<>();
		ap.add(p);
		bestPoints.put(p, ap);
	    }
	    return bestPoints;
	}
	final Coord[] ts = targets.toArray(new Coord[targets.size()]);
	final Coord[] exs = requiredExclusions.toArray(new Coord[requiredExclusions.size()]);
	Coord t = exs[0];
	final double[][][] compositeMap = new double[ts.length][this.dungeon.length][this.dungeon[0].length];
	Spill sp;
	final char[][] dungeonCopy = new char[this.dungeon.length][this.dungeon[0].length];
	for (int i = 0; i < this.dungeon.length; i++) {
	    System.arraycopy(this.dungeon[i], 0, dungeonCopy[i], 0, this.dungeon[i].length);
	}
	Coord tempPt;
	for (final Coord ex : exs) {
	    t = ex;
	    sp = new Spill(this.dungeon, this.spill.measurement);
	    sp.lrng.setState(this.seed);
	    sp.start(t, this.volume, null);
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    tempPt = Coord.get(x, y);
		    dungeonCopy[x][y] = sp.spillMap[x][y] || !AreaUtils.verifyReach(this.reach, this.origin, tempPt)
			    ? '!'
			    : dungeonCopy[x][y];
		}
	    }
	}
	DijkstraMap.Measurement dmm = DijkstraMap.Measurement.MANHATTAN;
	if (this.spill.measurement == Spill.Measurement.CHEBYSHEV) {
	    dmm = DijkstraMap.Measurement.CHEBYSHEV;
	} else if (this.spill.measurement == Spill.Measurement.EUCLIDEAN) {
	    dmm = DijkstraMap.Measurement.EUCLIDEAN;
	}
	final double radius = Math.sqrt(this.volume) * 0.75;
	for (int i = 0; i < ts.length; ++i) {
	    final DijkstraMap dm = new DijkstraMap(this.dungeon, dmm);
	    t = ts[i];
	    sp = new Spill(this.dungeon, this.spill.measurement);
	    sp.lrng.setState(this.seed);
	    sp.start(t, this.volume, null);
	    double dist = 0.0;
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    if (sp.spillMap[x][y]) {
			dist = this.reach.metric.radius(this.origin.x, this.origin.y, x, y);
			if (dist <= this.reach.maxDistance + radius && dist >= this.reach.minDistance - radius) {
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
	    dm.setGoal(t);
	    dm.scan(null);
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    compositeMap[i][x][y] = dm.gradientMap[x][y] < DijkstraMap.FLOOR && dungeonCopy[x][y] != '!'
			    ? dm.gradientMap[x][y]
			    : 99999.0;
		}
	    }
	    dm.resetMap();
	    dm.clearGoals();
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
	if (totalTargets == 0 || this.volume <= 0) {
	    return bestPoints;
	}
	if (this.volume == 1) {
	    for (final Coord p : priorityTargets) {
		final ArrayList<Coord> ap = new ArrayList<>();
		ap.add(p);
		bestPoints.put(p, ap);
	    }
	    return bestPoints;
	}
	final Coord[] pts = priorityTargets.toArray(new Coord[priorityTargets.size()]);
	final Coord[] lts = lesserTargets.toArray(new Coord[lesserTargets.size()]);
	final Coord[] exs = requiredExclusions.toArray(new Coord[requiredExclusions.size()]);
	Coord t = exs[0];
	final double[][][] compositeMap = new double[totalTargets][this.dungeon.length][this.dungeon[0].length];
	Spill sp;
	final char[][] dungeonCopy = new char[this.dungeon.length][this.dungeon[0].length],
		dungeonPriorities = new char[this.dungeon.length][this.dungeon[0].length];
	for (int i = 0; i < this.dungeon.length; i++) {
	    System.arraycopy(this.dungeon[i], 0, dungeonCopy[i], 0, this.dungeon[i].length);
	    Arrays.fill(dungeonPriorities[i], '#');
	}
	Coord tempPt = Coord.get(0, 0);
	for (final Coord ex : exs) {
	    t = ex;
	    sp = new Spill(this.dungeon, this.spill.measurement);
	    sp.lrng.setState(this.seed);
	    sp.start(t, this.volume, null);
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    tempPt = Coord.get(x, y);
		    dungeonCopy[x][y] = sp.spillMap[x][y] || !AreaUtils.verifyReach(this.reach, this.origin, tempPt)
			    ? '!'
			    : dungeonCopy[x][y];
		}
	    }
	}
	t = pts[0];
	DijkstraMap.Measurement dmm = DijkstraMap.Measurement.MANHATTAN;
	if (this.spill.measurement == Spill.Measurement.CHEBYSHEV) {
	    dmm = DijkstraMap.Measurement.CHEBYSHEV;
	} else if (this.spill.measurement == Spill.Measurement.EUCLIDEAN) {
	    dmm = DijkstraMap.Measurement.EUCLIDEAN;
	}
	final double radius = Math.sqrt(this.volume) * 0.75;
	for (int i = 0; i < pts.length; ++i) {
	    final DijkstraMap dm = new DijkstraMap(this.dungeon, dmm);
	    t = pts[i];
	    sp = new Spill(this.dungeon, this.spill.measurement);
	    sp.lrng.setState(this.seed);
	    sp.start(t, this.volume, null);
	    double dist = 0.0;
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    if (sp.spillMap[x][y]) {
			dist = this.reach.metric.radius(this.origin.x, this.origin.y, x, y);
			if (dist <= this.reach.maxDistance + radius && dist >= this.reach.minDistance - radius) {
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
	    dm.setGoal(t);
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
	t = lts[0];
	for (int i = pts.length; i < totalTargets; ++i) {
	    final DijkstraMap dm = new DijkstraMap(this.dungeon, dmm);
	    t = lts[i - pts.length];
	    sp = new Spill(this.dungeon, this.spill.measurement);
	    sp.lrng.setState(this.seed);
	    sp.start(t, this.volume, null);
	    double dist = 0.0;
	    for (int x = 0; x < this.dungeon.length; x++) {
		for (int y = 0; y < this.dungeon[x].length; y++) {
		    if (sp.spillMap[x][y]) {
			dist = this.reach.metric.radius(this.origin.x, this.origin.y, x, y);
			if (dist <= this.reach.maxDistance + radius && dist >= this.reach.minDistance - radius) {
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
	    dm.setGoal(t);
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
			}
		    }
		    for (int i = pts.length; i < totalTargets && i < 63; ++i) {
			if ((pbits & 1 << i) != 0) {
			    ap.add(pts[i]);
			    ap.add(pts[i]);
			    ap.add(pts[i]);
			    ap.add(pts[i]);
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
     * 1; int radius = Math.max(1, (int) (Math.sqrt(volume) * 1.5));
     * ArrayList<ArrayList<Coord>> locs = new
     * ArrayList<ArrayList<Coord>>(totalTargets);
     *
     * for(int i = 0; i < totalTargets; i++) { locs.add(new
     * ArrayList<Coord>(volume)); } if(totalTargets == 1) return locs; double ctr =
     * 0; if(radius < 1) { locs.get(totalTargets - 2).addAll(targets); return locs;
     * } double tempRad; boolean[][] tested = new
     * boolean[dungeon.length][dungeon[0].length]; for (int x = 1; x <
     * dungeon.length - 1; x += radius) { BY_POINT: for (int y = 1; y <
     * dungeon[x].length - 1; y += radius) { for(Coord ex : requiredExclusions) {
     * if(rt.radius(x, y, ex.x, ex.y) <= radius * 0.75) continue BY_POINT; } ctr =
     * 0; for(Coord tgt : targets) { tempRad = rt.radius(x, y, tgt.x, tgt.y);
     * if(tempRad < radius) ctr += 1.0 - (tempRad / radius) * 0.5; } if(ctr >= 1)
     * locs.get((int)(totalTargets - ctr)).add(Coord.get(x, y)); } } Coord it;
     * for(int t = 0; t < totalTargets - 1; t++) { if(locs.get(t).size() > 0) { int
     * numPoints = locs.get(t).size(); for (int i = 0; i < numPoints; i++) { it =
     * locs.get(t).get(i); for (int x = Math.max(1, it.x - radius / 2); x < it.x +
     * (radius + 1) / 2 && x < dungeon.length - 1; x++) { BY_POINT: for (int y =
     * Math.max(1, it.y - radius / 2); y <= it.y + (radius - 1) / 2 && y <
     * dungeon[0].length - 1; y++) { if(tested[x][y]) continue; tested[x][y] = true;
     *
     * for(Coord ex : requiredExclusions) { if(rt.radius(x, y, ex.x, ex.y) <= radius
     * * 0.75) continue BY_POINT; }
     *
     * ctr = 0; for(Coord tgt : targets) { tempRad = rt.radius(x, y, tgt.x, tgt.y);
     * if(tempRad < radius) ctr += 1.0 - (tempRad / radius) * 0.5; } if(ctr >= 1)
     * locs.get((int)(totalTargets - ctr)).add(Coord.get(x, y)); } } } } } return
     * locs; }
     */
    @Override
    public void setMap(final char[][] map) {
	this.spill.initialize(map);
	this.dungeon = map;
    }

    @Override
    public OrderedMap<Coord, Double> findArea() {
	this.spill.start(this.center, this.volume, null);
	final OrderedMap<Coord, Double> r = AreaUtils.arrayToHashMap(this.spill.spillMap);
	if (!this.expanding) {
	    this.spill.reset();
	    this.spill.lrng.setState(this.seed);
	}
	return r;
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

    public boolean isExpanding() {
	return this.expanding;
    }

    public void setExpanding(final boolean expanding) {
	this.expanding = expanding;
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
