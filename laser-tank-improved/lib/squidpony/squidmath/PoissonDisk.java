package squidpony.squidmath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import squidpony.squidgrid.Radius;

/**
 * This provides a Uniform Poisson Disk Sampling technique that can be used to
 * generate random points that have a uniform minimum distance between each
 * other. Due to Coord in SquidLib using ints and most Poisson Disk algorithms
 * using floating-point numbers, some imprecision is to be expected from
 * rounding to the nearest integers x and y.
 *
 * The algorithm is from the "Fast Poisson Disk Sampling in Arbitrary
 * Dimensions" paper by Robert Bridson
 * http://www.cs.ubc.ca/~rbridson/docs/bridson-siggraph07-poissondisk.pdf
 *
 * Adapted from C# by Renaud Bedard, which was adapted from Java source by
 * Herman Tulleken
 * http://theinstructionlimit.com/fast-uniform-poisson-disk-sampling-in-c
 * Created by Tommy Ettinger on 10/20/2015.
 */
public class PoissonDisk {
    private static final float rootTwo = (float) Math.sqrt(2), pi = (float) Math.PI, pi2 = PoissonDisk.pi * 2f;
    private static final int defaultPointsPlaced = 10;
    private static final Radius disk = Radius.CIRCLE;

    private PoissonDisk() {
    }

    /**
     * Get a list of Coords, each randomly positioned around the given center out to
     * the given radius (measured with Euclidean distance, so a true circle), but
     * with the given minimum distance from any other Coord in the list. The
     * parameters maxX and maxY should typically correspond to the width and height
     * of the map; no points will have positions with x equal to or greater than
     * maxX and the same for y and maxY; similarly, no points will have negative x
     * or y.
     *
     * @param center          the center of the circle to spray Coords into
     * @param radius          the radius of the circle to spray Coords into
     * @param minimumDistance the minimum distance between Coords, in Euclidean
     *                        distance as a float.
     * @param maxX            one more than the highest x that can be assigned;
     *                        typically an array length
     * @param maxY            one more than the highest y that can be assigned;
     *                        typically an array length
     * @return an ArrayList of Coord that satisfy the minimum distance; the length
     *         of the array can vary
     */
    public static ArrayList<Coord> sampleCircle(final Coord center, final float radius, final float minimumDistance,
	    final int maxX, final int maxY) {
	return PoissonDisk.sampleCircle(center, radius, minimumDistance, maxX, maxY, PoissonDisk.defaultPointsPlaced,
		new StatefulRNG());
    }

    /**
     * Get a list of Coords, each randomly positioned around the given center out to
     * the given radius (measured with Euclidean distance, so a true circle), but
     * with the given minimum distance from any other Coord in the list. The
     * parameters maxX and maxY should typically correspond to the width and height
     * of the map; no points will have positions with x equal to or greater than
     * maxX and the same for y and maxY; similarly, no points will have negative x
     * or y.
     *
     * @param center             the center of the circle to spray Coords into
     * @param radius             the radius of the circle to spray Coords into
     * @param minimumDistance    the minimum distance between Coords, in Euclidean
     *                           distance as a float.
     * @param maxX               one more than the highest x that can be assigned;
     *                           typically an array length
     * @param maxY               one more than the highest y that can be assigned;
     *                           typically an array length
     * @param pointsPerIteration with small radii, this can be around 5; with larger
     *                           ones, 30 is reasonable
     * @param rng                an RNG to use for all random sampling.
     * @return an ArrayList of Coord that satisfy the minimum distance; the length
     *         of the array can vary
     */
    public static ArrayList<Coord> sampleCircle(final Coord center, final float radius, final float minimumDistance,
	    final int maxX, final int maxY, final int pointsPerIteration, final RNG rng) {
	final int radius2 = Math.round(radius);
	return PoissonDisk.sample(center.translate(-radius2, -radius2), center.translate(radius2, radius2), radius,
		minimumDistance, maxX, maxY, pointsPerIteration, rng);
    }

    /**
     * Get a list of Coords, each randomly positioned within the rectangle between
     * the given minPosition and maxPosition, but with the given minimum distance
     * from any other Coord in the list. The parameters maxX and maxY should
     * typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY;
     * similarly, no points will have negative x or y.
     *
     * @param minPosition     the Coord with the lowest x and lowest y to be used as
     *                        a corner for the bounding box
     * @param maxPosition     the Coord with the highest x and highest y to be used
     *                        as a corner for the bounding box
     * @param minimumDistance the minimum distance between Coords, in Euclidean
     *                        distance as a float.
     * @param maxX            one more than the highest x that can be assigned;
     *                        typically an array length
     * @param maxY            one more than the highest y that can be assigned;
     *                        typically an array length
     * @return an ArrayList of Coord that satisfy the minimum distance; the length
     *         of the array can vary
     */
    public static ArrayList<Coord> sampleRectangle(final Coord minPosition, final Coord maxPosition,
	    final float minimumDistance, final int maxX, final int maxY) {
	return PoissonDisk.sampleRectangle(minPosition, maxPosition, minimumDistance, maxX, maxY,
		PoissonDisk.defaultPointsPlaced, new StatefulRNG());
    }

    /**
     * Get a list of Coords, each randomly positioned within the rectangle between
     * the given minPosition and maxPosition, but with the given minimum distance
     * from any other Coord in the list. The parameters maxX and maxY should
     * typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY;
     * similarly, no points will have negative x or y.
     *
     * @param minPosition        the Coord with the lowest x and lowest y to be used
     *                           as a corner for the bounding box
     * @param maxPosition        the Coord with the highest x and highest y to be
     *                           used as a corner for the bounding box
     * @param minimumDistance    the minimum distance between Coords, in Euclidean
     *                           distance as a float.
     * @param maxX               one more than the highest x that can be assigned;
     *                           typically an array length
     * @param maxY               one more than the highest y that can be assigned;
     *                           typically an array length
     * @param pointsPerIteration with small areas, this can be around 5; with larger
     *                           ones, 30 is reasonable
     * @param rng                an RNG to use for all random sampling.
     * @return an ArrayList of Coord that satisfy the minimum distance; the length
     *         of the array can vary
     */
    public static ArrayList<Coord> sampleRectangle(final Coord minPosition, final Coord maxPosition,
	    final float minimumDistance, final int maxX, final int maxY, final int pointsPerIteration, final RNG rng) {
	return PoissonDisk.sample(minPosition, maxPosition, 0f, minimumDistance, maxX, maxY, pointsPerIteration, rng);
    }

    private static ArrayList<Coord> sample(final Coord minPosition, final Coord maxPosition,
	    final float rejectionDistance, final float minimumDistance, final int maxX, final int maxY,
	    final int pointsPerIteration, final RNG rng) {
	final Coord center = minPosition.average(maxPosition);
	final Coord dimensions = maxPosition.subtract(minPosition);
	final float cellSize = Math.max(minimumDistance / PoissonDisk.rootTwo, 0.25f);
	final int gridWidth = (int) (dimensions.x / cellSize) + 1;
	final int gridHeight = (int) (dimensions.y / cellSize) + 1;
	final Coord[][] grid = new Coord[gridWidth][gridHeight];
	ArrayList<Coord> activePoints = new ArrayList<>();
	final OrderedSet<Coord> points = new OrderedSet<>(128);
	// add first point
	boolean added = false;
	while (!added) {
	    float d = rng.nextFloat();
	    final int xr = Math.round(minPosition.x + dimensions.x * d);
	    d = rng.nextFloat();
	    final int yr = Math.round(minPosition.y + dimensions.y * d);
	    if (rejectionDistance > 0 && PoissonDisk.disk.radius(center.x, center.y, xr, yr) > rejectionDistance) {
		continue;
	    }
	    added = true;
	    final Coord p = Coord.get(Math.min(xr, maxX - 1), Math.min(yr, maxY - 1));
	    final Coord index = p.subtract(minPosition).divide(cellSize);
	    grid[index.x][index.y] = p;
	    activePoints.add(p);
	    points.add(p);
	}
	// end add first point
	while (activePoints.size() != 0) {
	    final int listIndex = rng.nextInt(activePoints.size());
	    final Coord point = activePoints.get(listIndex);
	    boolean found = false;
	    for (int k = 0; k < pointsPerIteration; k++) {
		// add next point
		// get random point around
		float d = rng.nextFloat();
		final float radius = minimumDistance + minimumDistance * d;
		d = rng.nextFloat();
		final float angle = PoissonDisk.pi2 * d;
		final float newX = radius * (float) Math.sin(angle);
		final float newY = radius * (float) Math.cos(angle);
		final Coord q = point.translateCapped(Math.round(newX), Math.round(newY), maxX, maxY);
		// end get random point around
		if (q.x >= minPosition.x && q.x <= maxPosition.x && q.y >= minPosition.y && q.y <= maxPosition.y
			&& (rejectionDistance <= 0
				|| PoissonDisk.disk.radius(center.x, center.y, q.x, q.y) <= rejectionDistance)) {
		    final Coord qIndex = q.subtract(minPosition).divide((int) Math.ceil(cellSize));
		    boolean tooClose = false;
		    for (int i = Math.max(0, qIndex.x - 2); i < Math.min(gridWidth, qIndex.x + 3) && !tooClose; i++) {
			for (int j = Math.max(0, qIndex.y - 2); j < Math.min(gridHeight, qIndex.y + 3); j++) {
			    if (grid[i][j] != null && PoissonDisk.disk.radius(grid[i][j], q) < minimumDistance) {
				tooClose = true;
				break;
			    }
			}
		    }
		    if (!tooClose) {
			found = true;
			activePoints.add(q);
			points.add(q);
			grid[qIndex.x][qIndex.y] = q;
		    }
		}
		// end add next point
	    }
	    if (!found) {
		activePoints.remove(listIndex);
	    }
	}
	activePoints = new ArrayList<>(points);
	return activePoints;
    }

    public static ArrayList<Coord> sampleMap(final char[][] map, final float minimumDistance, final RNG rng,
	    final Character... blocking) {
	return PoissonDisk.sampleMap(Coord.get(1, 1), Coord.get(map.length - 2, map[0].length - 2), map,
		minimumDistance, rng, blocking);
    }

    public static ArrayList<Coord> sampleMap(final Coord minPosition, final Coord maxPosition, final char[][] map,
	    final float minimumDistance, final RNG rng, final Character... blocking) {
	final int width = map.length;
	final int height = map[0].length;
	final HashSet<Character> blocked = new HashSet<>();
	Collections.addAll(blocked, blocking);
	if (blocked.size() > 0) {
	}
	final Coord dimensions = maxPosition.subtract(minPosition);
	final float cellSize = Math.max(minimumDistance / PoissonDisk.rootTwo, 1f);
	final int gridWidth = (int) (dimensions.x / cellSize) + 1;
	final int gridHeight = (int) (dimensions.y / cellSize) + 1;
	final Coord[][] grid = new Coord[gridWidth][gridHeight];
	ArrayList<Coord> activePoints = new ArrayList<>();
	final OrderedSet<Coord> points = new OrderedSet<>(128);
	// add first point
	final Coord p = PoissonDisk.randomUnblockedTile(minPosition, maxPosition, map, rng, blocked);
	if (p == null) {
	    return activePoints;
	}
	final Coord index = p.subtract(minPosition).divide(cellSize);
	grid[index.x][index.y] = p;
	activePoints.add(p);
	points.add(p);
	// end add first point
	while (activePoints.size() != 0) {
	    final int listIndex = rng.nextInt(activePoints.size());
	    final Coord point = activePoints.get(listIndex);
	    boolean found = false;
	    for (int k = 0; k < 20; k++) {
		// add next point
		// get random point around
		float d = rng.nextFloat();
		final float radius = minimumDistance + minimumDistance * d;
		d = rng.nextFloat();
		float angle = PoissonDisk.pi2 * d;
		float newX = radius * (float) Math.sin(angle);
		float newY = radius * (float) Math.cos(angle);
		Coord q = point.translateCapped(Math.round(newX), Math.round(newY), width, height);
		int frustration = 0;
		while (blocked.contains(map[q.x][q.y]) && frustration < 8) {
		    d = rng.nextFloat();
		    angle = PoissonDisk.pi2 * d;
		    newX = radius * (float) Math.sin(angle);
		    newY = radius * (float) Math.cos(angle);
		    q = point.translateCapped(Math.round(newX), Math.round(newY), width, height);
		    frustration++;
		}
		// end get random point around
		if (q.x >= minPosition.x && q.x <= maxPosition.x && q.y >= minPosition.y && q.y <= maxPosition.y) {
		    final Coord qIndex = q.subtract(minPosition).divide((int) Math.ceil(cellSize));
		    boolean tooClose = false;
		    for (int i = Math.max(0, qIndex.x - 2); i < Math.min(gridWidth, qIndex.x + 3) && !tooClose; i++) {
			for (int j = Math.max(0, qIndex.y - 2); j < Math.min(gridHeight, qIndex.y + 3); j++) {
			    if (grid[i][j] != null && PoissonDisk.disk.radius(grid[i][j], q) < minimumDistance) {
				tooClose = true;
				break;
			    }
			}
		    }
		    if (!tooClose) {
			found = true;
			activePoints.add(q);
			if (!blocked.contains(map[q.x][q.y])) {
			    points.add(q);
			}
			grid[qIndex.x][qIndex.y] = q;
		    }
		}
		// end add next point
	    }
	    if (!found) {
		activePoints.remove(listIndex);
	    }
	}
	activePoints = new ArrayList<>(points);
	return activePoints;
    }

    /**
     * Finds a random Coord where the x and y match up to a [x][y] location on map
     * that has any value not in blocking. Uses the given RNG for pseudo-random
     * number generation.
     *
     * @param minPosition the Coord with the lowest x and lowest y to be used as a
     *                    corner for the bounding box
     * @param maxPosition the Coord with the highest x and highest y to be used as a
     *                    corner for the bounding box
     * @param map         a dungeon map or something, x then y
     * @param rng         a RNG to generate random choices
     * @param blocked     a Set of Characters that block a tile from being chosen
     * @return a Coord that corresponds to a map element equal to tile, or null if
     *         tile cannot be found or if map is too small.
     */
    public static Coord randomUnblockedTile(final Coord minPosition, final Coord maxPosition, final char[][] map,
	    final RNG rng, final HashSet<Character> blocked) {
	final int width = map.length;
	final int height = map[0].length;
	if (width < 3 || height < 3) {
	    return null;
	}
	if (blocked.size() == 0) {
	    return Coord.get(rng.between(minPosition.x, maxPosition.x), rng.between(minPosition.y, maxPosition.y));
	}
	int x = rng.between(minPosition.x, maxPosition.x), y = rng.between(minPosition.y, maxPosition.y);
	for (int i = 0; i < (width + height) / 4; i++) {
	    if (!blocked.contains(map[x][y])) {
		return Coord.get(x, y);
	    } else {
		x = rng.between(minPosition.x, maxPosition.x);
		y = rng.between(minPosition.y, maxPosition.y);
	    }
	}
	x = 1;
	y = 1;
	if (!blocked.contains(map[x][y])) {
	    return Coord.get(x, y);
	}
	while (blocked.contains(map[x][y])) {
	    x += 1;
	    if (x >= width - 1) {
		x = 1;
		y += 1;
	    }
	    if (y >= height - 1) {
		return null;
	    }
	}
	return Coord.get(x, y);
    }
}
