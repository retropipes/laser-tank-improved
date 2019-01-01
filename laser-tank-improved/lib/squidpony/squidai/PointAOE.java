package squidpony.squidai;

import java.util.ArrayList;
import java.util.Collection;

import squidpony.annotation.GwtIncompatible;
import squidpony.squidgrid.FOVCache;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

/**
 * An AOE type that has a center Coord only and only affects that single Coord.
 * Useful if you need an AOE implementation for something that does not actually
 * affect an area. This will produce doubles for its findArea() method which are
 * equal to 1.0.
 *
 * This class doesn't use any other SquidLib class to create its area of effect.
 * Created by Tommy Ettinger on 7/13/2015.
 */
public class PointAOE implements AOE {
    private Coord center, origin = null;
    private int mapWidth, mapHeight;
    private final Reach reach = new Reach(1, 1, Radius.SQUARE, null);

    public PointAOE(final Coord center) {
	this.center = center;
    }

    public PointAOE(final Coord center, final int minRange, final int maxRange) {
	this.center = center;
	this.reach.minDistance = minRange;
	this.reach.maxDistance = maxRange;
    }

    public Coord getCenter() {
	return this.center;
    }

    public void setCenter(final Coord center) {
	if (center.isWithin(this.mapWidth, this.mapHeight) && AreaUtils.verifyReach(this.reach, this.origin, center)) {
	    this.center = center;
	}
    }

    @Override
    public void shift(final Coord aim) {
	this.setCenter(aim);
    }

    @Override
    public boolean mayContainTarget(final Collection<Coord> targets) {
	for (final Coord p : targets) {
	    if (this.center.x == p.x && this.center.y == p.y) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public OrderedMap<Coord, ArrayList<Coord>> idealLocations(final Collection<Coord> targets,
	    final Collection<Coord> requiredExclusions) {
	if (targets == null) {
	    return new OrderedMap<>();
	}
	final int totalTargets = targets.size();
	final OrderedMap<Coord, ArrayList<Coord>> bestPoints = new OrderedMap<>(totalTargets);
	if (totalTargets == 0) {
	    return bestPoints;
	}
	double dist = 0.0;
	for (final Coord p : targets) {
	    if (AreaUtils.verifyReach(this.reach, this.origin, p)) {
		dist = this.reach.metric.radius(this.origin.x, this.origin.y, p.x, p.y);
		if (dist <= this.reach.maxDistance && dist >= this.reach.minDistance) {
		    final ArrayList<Coord> ap = new ArrayList<>();
		    ap.add(p);
		    bestPoints.put(p, ap);
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
	final int totalTargets = priorityTargets.size() + lesserTargets.size();
	final OrderedMap<Coord, ArrayList<Coord>> bestPoints = new OrderedMap<>(totalTargets * 4);
	if (totalTargets == 0) {
	    return bestPoints;
	}
	double dist = 0.0;
	for (final Coord p : priorityTargets) {
	    if (AreaUtils.verifyReach(this.reach, this.origin, p)) {
		dist = this.reach.metric.radius(this.origin.x, this.origin.y, p.x, p.y);
		if (dist <= this.reach.maxDistance && dist >= this.reach.minDistance) {
		    final ArrayList<Coord> ap = new ArrayList<>();
		    ap.add(p);
		    ap.add(p);
		    ap.add(p);
		    ap.add(p);
		    bestPoints.put(p, ap);
		}
	    }
	}
	if (bestPoints.isEmpty()) {
	    for (final Coord p : lesserTargets) {
		if (AreaUtils.verifyReach(this.reach, this.origin, p)) {
		    dist = this.reach.metric.radius(this.origin.x, this.origin.y, p.x, p.y);
		    if (dist <= this.reach.maxDistance && dist >= this.reach.minDistance) {
			final ArrayList<Coord> ap = new ArrayList<>();
			ap.add(p);
			bestPoints.put(p, ap);
		    }
		}
	    }
	}
	return bestPoints;
    }

    /*
     * @Override public ArrayList<ArrayList<Coord>> idealLocations(Set<Coord>
     * targets, Set<Coord> requiredExclusions) { int totalTargets = targets.size() +
     * 1; int maxEffect = (int)radiusType.volume2D(radius);
     * ArrayList<ArrayList<Coord>> locs = new
     * ArrayList<ArrayList<Coord>>(totalTargets);
     *
     * for(int i = 0; i < totalTargets; i++) { locs.add(new
     * ArrayList<Coord>(maxEffect)); } if(totalTargets == 1) return locs;
     *
     * int ctr = 0; if(radius < 1) { locs.get(totalTargets - 2).addAll(targets);
     * return locs; }
     *
     * boolean[][] tested = new boolean[dungeon.length][dungeon[0].length]; for (int
     * x = 1; x < dungeon.length - 1; x += radius) { BY_POINT: for (int y = 1; y <
     * dungeon[x].length - 1; y += radius) { for(Coord ex : requiredExclusions) {
     * if(radiusType.radius(x, y, ex.x, ex.y) <= radius) continue BY_POINT; } ctr =
     * 0; for(Coord tgt : targets) { if(radiusType.radius(x, y, tgt.x, tgt.y) <=
     * radius) ctr++; } if(ctr > 0) locs.get(totalTargets - ctr).add(Coord.get(x,
     * y)); } } Coord it; for(int t = 0; t < totalTargets - 1; t++) {
     * if(locs.get(t).size() > 0) { int numPoints = locs.get(t).size(); for (int i =
     * 0; i < numPoints; i++) { it = locs.get(t).get(i); for (int x = Math.max(1,
     * it.x - radius / 2); x < it.x + (radius + 1) / 2 && x < dungeon.length - 1;
     * x++) { BY_POINT: for (int y = Math.max(1, it.y - radius / 2); y <= it.y +
     * (radius - 1) / 2 && y < dungeon[0].length - 1; y++) { if(tested[x][y])
     * continue; tested[x][y] = true;
     *
     * for(Coord ex : requiredExclusions) { if(radiusType.radius(x, y, ex.x, ex.y)
     * <= radius) continue BY_POINT; }
     *
     * ctr = 0; for(Coord tgt : targets) { if(radiusType.radius(x, y, tgt.x, tgt.y)
     * <= radius) ctr++; } if(ctr > 0) locs.get(totalTargets - ctr).add(Coord.get(x,
     * y)); } } } } } return locs; }
     */
    @Override
    public void setMap(final char[][] map) {
	if (map != null && map.length > 0) {
	    this.mapWidth = map.length;
	    this.mapHeight = map[0].length;
	}
    }

    @Override
    public OrderedMap<Coord, Double> findArea() {
	final OrderedMap<Coord, Double> ret = new OrderedMap<>(1);
	ret.put(Coord.get(this.center.x, this.center.y), 1.0);
	return ret;
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
