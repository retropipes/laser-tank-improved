package squidpony.squidgrid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import squidpony.annotation.GwtIncompatible;
import squidpony.squidmath.Bresenham;
import squidpony.squidmath.Coord;
import squidpony.squidmath.DDALine;
import squidpony.squidmath.Elias;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.OrthoLine;

/**
 * Line of Sight (LOS) algorithms find if there is or is not a path between two
 * given points. <br>
 * The line found between two points will end at either the target, the
 * obstruction closest to the start, or the edge of the map. <br>
 * For normal line of sight usage, you should prefer Bresenham lines, and these
 * are the default (they can also be specified by passing {@link #BRESENHAM} to
 * the constructor). For more specialized usage, there are other kinds of LOS in
 * this class, like lines that make no diagonal moves between cells (using
 * {@link #ORTHO}, or lines that check a wide path (but these use different
 * methods, like {@link #thickReachable(Radius)}). <br>
 * Performance-wise, all of these methods are rather fast and about the same
 * speed. {@link #RAY} is a tiny fraction faster than {@link #BRESENHAM} but
 * produces rather low-quality lines in comparison. Calculating the visibility
 * of 40,000 lines in a 102x102 dungeon takes within 3% of 950ms (on an Intel
 * i7-4700MQ laptop processor) for every one of BRESENHAM, DDA, ORTHO, and RAY,
 * even with ORTHO finding a different kind of line by design.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger Added DDA, ORTHO, and the thick lines; some cleanup
 * @author smelC optimized several methods
 */
public class LOS {
    // constants to indicate desired type of solving algorithm to use
    /**
     * A Bresenham-based line-of-sight algorithm.
     */
    public static final int BRESENHAM = 1;
    /**
     * Uses Wu's Algorithm as modified by Elias to draw the line. Does not end at an
     * obstruction but rather returns one of the possible attempted paths in full.
     *
     * <p>
     * Be aware, it is GWT-incompatible.
     * </p>
     */
    public static final int ELIAS = 2;
    /**
     * Uses a series of rays internal to the start and end point to determine
     * visibility. Appearance is extremely close to DDA, which is also probably a
     * faster algorithm, so BRESENHAM (which can look a little better) and DDA are
     * recommended instead of RAY.
     */
    public static final int RAY = 3;
    /**
     * Draws a line using only North/South/East/West movement.
     */
    public static final int ORTHO = 4;
    /**
     * Optimized algorithm for Bresenham-like lines. There are slight differences in
     * many parts of the lines this draws when compared to Bresenham lines, but it
     * may also perform significantly better, and may also be useful as a building
     * block for more complex LOS. Virtually identical in results to RAY, and just a
     * hair slower, but better-tested and more predictable.
     */
    public static final int DDA = 5;
    /**
     * Draws a line as if with a thick brush, going from a point between a corner of
     * the starting cell and the center of the starting cell to the corresponding
     * corner of the target cell, and considers the target visible if any portion of
     * the thick stroke reached it. Will result in 1-width lines for
     * exactly-orthogonal or exactly-diagonal lines and some parts of other lines,
     * but usually is 2 cells wide.
     */
    public static final int THICK = 6;
    private LinkedList<Coord> lastPath = new LinkedList<>();
    private final int type;
    private double[][] resistanceMap;
    private int startx, starty, targetx, targety;
    private Elias elias = null;

    /**
     * Gets the radius strategy this uses.
     *
     * @return the current Radius enum used to measure distance; starts at CIRCLE if
     *         not specified
     */
    public Radius getRadiusStrategy() {
	return this.radiusStrategy;
    }

    /**
     * Set the radius strategy to the given Radius; the default is CIRCLE if this is
     * not called.
     *
     * @param radiusStrategy a Radius enum to determine how distances are measured
     */
    public void setRadiusStrategy(final Radius radiusStrategy) {
	this.radiusStrategy = radiusStrategy;
    }

    private Radius radiusStrategy = Radius.CIRCLE;

    /**
     * Constructs an LOS that will draw Bresenham lines and measure distances using
     * the CIRCLE radius strategy.
     */
    public LOS() {
	this(LOS.BRESENHAM);
    }

    /**
     * Constructs an LOS with the given type number, which must equal a static field
     * in this class such as BRESENHAM.
     *
     * @param type an int that must correspond to the value of a static field in
     *             this class (such as BRESENHAM)
     */
    public LOS(final int type) {
	this.type = type;
	if (type == LOS.ELIAS) {
	    this.elias = new Elias();
	}
    }

    /**
     * Returns true if a line can be drawn from the start point to the target point
     * without intervening obstructions.
     *
     * Uses RadiusStrategy.CIRCLE, or whatever RadiusStrategy was set with
     * setRadiusStrategy .
     *
     * @param walls   '#' is fully opaque, anything else is fully transparent, as
     *                always this uses x,y indexing.
     * @param startx  starting x position on the grid
     * @param starty  starting y position on the grid
     * @param targetx ending x position on the grid
     * @param targety ending y position on the grid
     * @return true if a line can be drawn without being obstructed, false otherwise
     */
    public boolean isReachable(final char[][] walls, final int startx, final int starty, final int targetx,
	    final int targety) {
	if (walls.length < 1) {
	    return false;
	}
	final double[][] resMap = new double[walls.length][walls[0].length];
	for (int x = 0; x < walls.length; x++) {
	    for (int y = 0; y < walls[0].length; y++) {
		resMap[x][y] = walls[x][y] == '#' ? 1.0 : 0.0;
	    }
	}
	return this.isReachable(resMap, startx, starty, targetx, targety, this.radiusStrategy);
    }

    /**
     * Returns true if a line can be drawn from the start point to the target point
     * without intervening obstructions.
     *
     * Does not take into account resistance less than opaque or distance cost.
     *
     * Uses RadiusStrategy.CIRCLE, or whatever RadiusStrategy was set with
     * setRadiusStrategy .
     *
     * @param resistanceMap 0.0 is fully transparent, 1.0 is fully opaque, as always
     *                      this uses x,y indexing.
     * @param startx        starting x position on the grid
     * @param starty        starting y position on the grid
     * @param targetx       ending x position on the grid
     * @param targety       ending y position on the grid
     * @return true if a line can be drawn without being obstructed, false otherwise
     */
    public boolean isReachable(final double[][] resistanceMap, final int startx, final int starty, final int targetx,
	    final int targety) {
	return this.isReachable(resistanceMap, startx, starty, targetx, targety, this.radiusStrategy);
    }

    /**
     * Returns true if a line can be drawn from the start point to the target point
     * without intervening obstructions.
     *
     * @param resistanceMap  0.0 is fully transparent, 1.0 is fully opaque, as
     *                       always this uses x,y indexing.
     * @param startx         starting x position on the grid
     * @param starty         starting y position on the grid
     * @param targetx        ending x position on the grid
     * @param targety        ending y position on the grid
     * @param radiusStrategy the strategy to use in computing unit distance
     * @return true if a line can be drawn without being obstructed, false otherwise
     */
    public boolean isReachable(final double[][] resistanceMap, final int startx, final int starty, final int targetx,
	    final int targety, final Radius radiusStrategy) {
	if (resistanceMap.length < 1) {
	    return false;
	}
	this.resistanceMap = resistanceMap;
	this.startx = startx;
	this.starty = starty;
	this.targetx = targetx;
	this.targety = targety;
	switch (this.type) {
	case BRESENHAM:
	    return this.bresenhamReachable(radiusStrategy);
	case ELIAS:
	    throw new IllegalStateException("Elias LOS is Gwt Incompatible");
	    // Comment required to compile with GWT:
	    // return eliasReachable(radiusStrategy);
	case RAY:
	    return this.rayReachable(radiusStrategy);
	case ORTHO:
	    return this.orthoReachable(radiusStrategy);
	case DDA:
	    return this.ddaReachable(radiusStrategy);
	case THICK:
	    return this.thickReachable(radiusStrategy);
	}
	return false;
    }

    /**
     * Returns true if a line can be drawn from the start point to the target point
     * without intervening obstructions.
     *
     * @param walls          '#' is fully opaque, anything else is fully
     *                       transparent, as always this uses x,y indexing.
     * @param startx         starting x position on the grid
     * @param starty         starting y position on the grid
     * @param targetx        ending x position on the grid
     * @param targety        ending y position on the grid
     * @param radiusStrategy the strategy to use in computing unit distance
     * @return true if a line can be drawn without being obstructed, false otherwise
     */
    public boolean isReachable(final char[][] walls, final int startx, final int starty, final int targetx,
	    final int targety, final Radius radiusStrategy) {
	if (walls.length < 1) {
	    return false;
	}
	final double[][] resMap = new double[walls.length][walls[0].length];
	for (int x = 0; x < walls.length; x++) {
	    for (int y = 0; y < walls[0].length; y++) {
		resMap[x][y] = walls[x][y] == '#' ? 1.0 : 0.0;
	    }
	}
	return this.isReachable(resMap, startx, starty, targetx, targety, radiusStrategy);
    }

    /**
     * Returns true if a line can be drawn from the any of the points within spread
     * cells of the start point, to any of the corresponding points at the same
     * direction and distance from the target point, without intervening
     * obstructions. Primarily useful to paint a broad line that can be retrieved
     * with getLastPath.
     *
     * @param walls          '#' is fully opaque, anything else is fully
     *                       transparent, as always this uses x,y indexing.
     * @param startx         starting x position on the grid
     * @param starty         starting y position on the grid
     * @param targetx        ending x position on the grid
     * @param targety        ending y position on the grid
     * @param radiusStrategy the strategy to use in computing unit distance
     * @param spread         the number of cells outward, measured by
     *                       radiusStrategy, to place extra start and target points
     * @return true if a line can be drawn without being obstructed, false otherwise
     */
    public boolean spreadReachable(final char[][] walls, final int startx, final int starty, final int targetx,
	    final int targety, final Radius radiusStrategy, final int spread) {
	if (walls.length < 1) {
	    return false;
	}
	this.resistanceMap = new double[walls.length][walls[0].length];
	for (int x = 0; x < walls.length; x++) {
	    for (int y = 0; y < walls[0].length; y++) {
		this.resistanceMap[x][y] = walls[x][y] == '#' ? 1.0 : 0.0;
	    }
	}
	this.startx = startx;
	this.starty = starty;
	this.targetx = targetx;
	this.targety = targety;
	return this.brushReachable(radiusStrategy, spread);
    }

    /**
     * Returns true if a line can be drawn from the any of the points within spread
     * cells of the start point, to any of the corresponding points at the same
     * direction and distance from the target point, without intervening
     * obstructions. Primarily useful to paint a broad line that can be retrieved
     * with getLastPath.
     *
     * @param resistanceMap  0.0 is fully transparent, 1.0 is fully opaque, as
     *                       always this uses x,y indexing.
     * @param startx         starting x position on the grid
     * @param starty         starting y position on the grid
     * @param targetx        ending x position on the grid
     * @param targety        ending y position on the grid
     * @param radiusStrategy the strategy to use in computing unit distance
     * @param spread         the number of cells outward, measured by
     *                       radiusStrategy, to place extra start and target points
     * @return true if a line can be drawn without being obstructed, false otherwise
     */
    public boolean spreadReachable(final double[][] resistanceMap, final int startx, final int starty,
	    final int targetx, final int targety, final Radius radiusStrategy, final int spread) {
	if (resistanceMap.length < 1) {
	    return false;
	}
	this.resistanceMap = resistanceMap;
	this.startx = startx;
	this.starty = starty;
	this.targetx = targetx;
	this.targety = targety;
	return this.brushReachable(radiusStrategy, spread);
    }

    /**
     * Returns the path of the last LOS calculation, with the starting point as the
     * head of the queue.
     *
     * @return
     */
    public LinkedList<Coord> getLastPath() {
	return this.lastPath;
    }

    /*
     * private boolean bresenhamReachable(Radius radiusStrategy) { Queue<Coord> path
     * = Bresenham.line2D(startx, starty, targetx, targety); lastPath = new
     * LinkedList<>(); lastPath.add(Coord.get(startx, starty)); double decay = 1 /
     * radiusStrategy.radius(startx, starty, targetx, targety); double currentForce
     * = 1; for (Coord p : path) { lastPath.offer(p); if (p.x == targetx && p.y ==
     * targety) { return true;//reached the end } if (p.x != startx || p.y !=
     * starty) {//don't discount the start location even if on resistant cell
     * currentForce -= resistanceMap[p.x][p.y]; } double r =
     * radiusStrategy.radius(startx, starty, p.x, p.y); if (currentForce - (r *
     * decay) <= 0) { return false;//too much resistance } } return false;//never
     * got to the target point }
     */
    private boolean bresenhamReachable(final Radius radiusStrategy) {
	final Coord[] path = Bresenham.line2D_(this.startx, this.starty, this.targetx, this.targety);
	this.lastPath = new LinkedList<>();
	final double rad = radiusStrategy.radius(this.startx, this.starty, this.targetx, this.targety);
	if (rad == 0.0) {
	    this.lastPath.add(Coord.get(this.startx, this.starty));
	    return true; // already at the point; we can see our own feet just fine!
	}
	final double decay = 1 / rad;
	double currentForce = 1;
	Coord p;
	for (final Coord element : path) {
	    p = element;
	    this.lastPath.offer(p);
	    if (p.x == this.targetx && p.y == this.targety) {
		return true;// reached the end
	    }
	    if (p.x != this.startx || p.y != this.starty) {// don't discount the start location even if on resistant
							   // cell
		currentForce -= this.resistanceMap[p.x][p.y];
	    }
	    final double r = radiusStrategy.radius(this.startx, this.starty, p.x, p.y);
	    if (currentForce - r * decay <= 0) {
		return false;// too much resistance
	    }
	}
	return false;// never got to the target point
    }

    private boolean orthoReachable(final Radius radiusStrategy) {
	final Coord[] path = OrthoLine.line_(this.startx, this.starty, this.targetx, this.targety);
	this.lastPath = new LinkedList<>();
	final double rad = radiusStrategy.radius(this.startx, this.starty, this.targetx, this.targety);
	if (rad == 0.0) {
	    this.lastPath.add(Coord.get(this.startx, this.starty));
	    return true; // already at the point; we can see our own feet just fine!
	}
	final double decay = 1 / rad;
	double currentForce = 1;
	Coord p;
	for (final Coord element : path) {
	    p = element;
	    this.lastPath.offer(p);
	    if (p.x == this.targetx && p.y == this.targety) {
		return true;// reached the end
	    }
	    if (p.x != this.startx || p.y != this.starty) {// don't discount the start location even if on resistant
							   // cell
		currentForce -= this.resistanceMap[p.x][p.y];
	    }
	    final double r = radiusStrategy.radius(this.startx, this.starty, p.x, p.y);
	    if (currentForce - r * decay <= 0) {
		return false;// too much resistance
	    }
	}
	return false;// never got to the target point
    }

    private boolean ddaReachable(final Radius radiusStrategy) {
	final Coord[] path = DDALine.line_(this.startx, this.starty, this.targetx, this.targety);
	this.lastPath = new LinkedList<>();
	final double rad = radiusStrategy.radius(this.startx, this.starty, this.targetx, this.targety);
	if (rad == 0.0) {
	    this.lastPath.add(Coord.get(this.startx, this.starty));
	    return true; // already at the point; we can see our own feet just fine!
	}
	final double decay = 1 / rad;
	double currentForce = 1;
	Coord p;
	for (final Coord element : path) {
	    p = element;
	    if (p.x == this.targetx && p.y == this.targety) {
		this.lastPath.offer(p);
		return true;// reached the end
	    }
	    if (p.x != this.startx || p.y != this.starty) {// don't discount the start location even if on resistant
							   // cell
		currentForce -= this.resistanceMap[p.x][p.y];
	    }
	    final double r = radiusStrategy.radius(this.startx, this.starty, p.x, p.y);
	    if (currentForce - r * decay <= 0) {
		return false;// too much resistance
	    }
	    this.lastPath.offer(p);
	}
	return false;// never got to the target point
    }

    private boolean thickReachable(final Radius radiusStrategy) {
	this.lastPath = new LinkedList<>();
	final double dist = radiusStrategy.radius(this.startx, this.starty, this.targetx, this.targety);
	final double decay = 1.0 / dist; // note: decay can be positive infinity if dist is 0; this is actually OK
	final OrderedSet<Coord> visited = new OrderedSet<>((int) dist + 3);
	final List<List<Coord>> paths = new ArrayList<>(4);
	/*
	 * // actual corners paths.add(DDALine.line(startx, starty, targetx, targety, 0,
	 * 0)); paths.add(DDALine.line(startx, starty, targetx, targety, 0, 0xffff));
	 * paths.add(DDALine.line(startx, starty, targetx, targety, 0xffff, 0));
	 * paths.add(DDALine.line(startx, starty, targetx, targety, 0xffff, 0xffff));
	 */
	// halfway between the center and a corner
	paths.add(DDALine.line(this.startx, this.starty, this.targetx, this.targety, 0x3fff, 0x3fff));
	paths.add(DDALine.line(this.startx, this.starty, this.targetx, this.targety, 0x3fff, 0xbfff));
	paths.add(DDALine.line(this.startx, this.starty, this.targetx, this.targety, 0xbfff, 0x3fff));
	paths.add(DDALine.line(this.startx, this.starty, this.targetx, this.targety, 0xbfff, 0xbfff));
	final int length = Math.max(paths.get(0).size(),
		Math.max(paths.get(1).size(), Math.max(paths.get(2).size(), paths.get(3).size())));
	final double[] forces = new double[] { 1, 1, 1, 1 };
	final boolean[] go = new boolean[] { true, true, true, true };
	Coord p;
	for (int d = 0; d < length; d++) {
	    for (int pc = 0; pc < 4; pc++) {
		final List<Coord> path = paths.get(pc);
		if (d < path.size() && go[pc]) {
		    p = path.get(d);
		} else {
		    continue;
		}
		if (p.x == this.targetx && p.y == this.targety) {
		    visited.add(p);
		    this.lastPath.addAll(visited);
		    return true;// reached the end
		}
		if (p.x != this.startx || p.y != this.starty) {// don't discount the start location even if on resistant
							       // cell
		    forces[pc] -= this.resistanceMap[p.x][p.y];
		}
		final double r = radiusStrategy.radius(this.startx, this.starty, p.x, p.y);
		if (forces[pc] - r * decay <= 0) {
		    go[pc] = false;
		    continue;// too much resistance
		}
		visited.add(p);
	    }
	}
	this.lastPath.addAll(visited);
	return false;// never got to the target point
    }

    private boolean brushReachable(final Radius radiusStrategy, final int spread) {
	this.lastPath = new LinkedList<>();
	final double dist = radiusStrategy.radius(this.startx, this.starty, this.targetx, this.targety) + spread * 2,
		decay = 1 / dist;
	final OrderedSet<Coord> visited = new OrderedSet<>((int) (dist + 3) * spread);
	final List<List<Coord>> paths = new ArrayList<>((int) (radiusStrategy.volume2D(spread) * 1.25));
	int length = 0;
	List<Coord> currentPath;
	for (int i = -spread; i <= spread; i++) {
	    for (int j = -spread; j <= spread; j++) {
		if (radiusStrategy.inRange(this.startx, this.starty, this.startx + i, this.starty + j, 0, spread)
			&& this.startx + i >= 0 && this.starty + j >= 0 && this.startx + i < this.resistanceMap.length
			&& this.starty + j < this.resistanceMap[0].length && this.targetx + i >= 0
			&& this.targety + j >= 0 && this.targetx + i < this.resistanceMap.length
			&& this.targety + j < this.resistanceMap[0].length) {
		    for (int q = 0x3fff; q < 0xffff; q += 0x8000) {
			for (int r = 0x3fff; r < 0xffff; r += 0x8000) {
			    currentPath = DDALine.line(this.startx + i, this.starty + j, this.targetx + i,
				    this.targety + j, q, r);
			    paths.add(currentPath);
			    length = Math.max(length, currentPath.size());
			}
		    }
		}
	    }
	}
	final double[] forces = new double[paths.size()];
	Arrays.fill(forces, 1.0);
	final boolean[] go = new boolean[paths.size()];
	Arrays.fill(go, true);
	Coord p;
	boolean found = false;
	for (int d = 0; d < length; d++) {
	    for (int pc = 0; pc < paths.size(); pc++) {
		final List<Coord> path = paths.get(pc);
		if (d < path.size() && go[pc]) {
		    p = path.get(d);
		} else {
		    continue;
		}
		if (p.x == this.targetx && p.y == this.targety) {
		    found = true;
		}
		if (p.x != this.startx || p.y != this.starty) {// don't discount the start location even if on resistant
							       // cell
		    forces[pc] -= this.resistanceMap[p.x][p.y];
		}
		final double r = radiusStrategy.radius(this.startx, this.starty, p.x, p.y);
		if (forces[pc] - r * decay <= 0) {
		    go[pc] = false;
		    continue;// too much resistance
		}
		visited.add(p);
	    }
	}
	this.lastPath.addAll(visited);
	return found;// never got to the target point
    }

    private boolean rayReachable(final Radius radiusStrategy) {
	this.lastPath = new LinkedList<>();// save path for later retrieval
	if (this.startx == this.targetx && this.starty == this.targety) {// already there!
	    this.lastPath.add(Coord.get(this.startx, this.starty));
	    return true;
	}
	final int width = this.resistanceMap.length;
	final int height = this.resistanceMap[0].length;
	Coord end = Coord.get(this.targetx, this.targety);
	// find out which direction to step, on each axis
	final int stepX = this.targetx == this.startx ? 0 : this.targetx - this.startx >> 31 | 1, // signum with less
												  // converting to/from
												  // float
		stepY = this.targety == this.starty ? 0 : this.targety - this.starty >> 31 | 1;
	final int deltaY = Math.abs(this.targetx - this.startx), deltaX = Math.abs(this.targety - this.starty);
	int testX = this.startx, testY = this.starty;
	int maxX = deltaX, maxY = deltaY;
	while (testX >= 0 && testX < width && testY >= 0 && testY < height
		&& (testX != this.targetx || testY != this.targety)) {
	    this.lastPath.add(Coord.get(testX, testY));
	    if (maxY - maxX > deltaX) {
		maxX += deltaX;
		testX += stepX;
		if (this.resistanceMap[testX][testY] >= 1f) {
		    end = Coord.get(testX, testY);
		    break;
		}
	    } else if (maxX - maxY > deltaY) {
		maxY += deltaY;
		testY += stepY;
		if (this.resistanceMap[testX][testY] >= 1f) {
		    end = Coord.get(testX, testY);
		    break;
		}
	    } else {// directly on diagonal, move both full step
		maxY += deltaY;
		testY += stepY;
		maxX += deltaX;
		testX += stepX;
		if (this.resistanceMap[testX][testY] >= 1f) {
		    end = Coord.get(testX, testY);
		    break;
		}
	    }
	    if (radiusStrategy.radius(testX, testY, this.startx, this.starty) > radiusStrategy.radius(this.startx,
		    this.starty, end.x, end.y)) {// went too far
		break;
	    }
	}
	if (end.x >= 0 && end.x < width && end.y >= 0 && end.y < height) {
	    this.lastPath.add(Coord.get(end.x, end.y));
	}
	return end.x == this.targetx && end.y == this.targety;
    }

    @GwtIncompatible /* Because of Thread */
    private boolean eliasReachable(final Radius radiusStrategy) {
	if (this.elias == null) {
	    this.elias = new Elias();
	}
	final List<Coord> ePath = this.elias.line(this.startx, this.starty, this.targetx, this.targety);
	this.lastPath = new LinkedList<>(ePath);// save path for later retreival
	final HashMap<EliasWorker, Thread> pool = new HashMap<>();
	for (final Coord p : ePath) {
	    final EliasWorker worker = new EliasWorker(p.x, p.y, radiusStrategy);
	    final Thread thread = new Thread(worker);
	    thread.start();
	    pool.put(worker, thread);
	}
	for (final EliasWorker w : pool.keySet()) {
	    try {
		pool.get(w).join();
	    } catch (final InterruptedException ex) {
	    }
	    if (w.succeeded) {
		this.lastPath = w.path;
		return true;
	    }
	}
	return false;// never got to the target point
    }

    private class EliasWorker implements Runnable {
	private LinkedList<Coord> path;
	private boolean succeeded = false;
	private final int testx, testy;
	private final Radius eliasRadiusStrategy;

	EliasWorker(final int testx, final int testy, final Radius radiusStrategy) {
	    this.testx = testx;
	    this.testy = testy;
	    this.eliasRadiusStrategy = radiusStrategy;
	}

	@Override
	public void run() {
	    final LOS los1 = new LOS(LOS.BRESENHAM);
	    final LOS los2 = new LOS(LOS.BRESENHAM);
	    // if a non-solid midpoint on the path can see both the start and end, consider
	    // the two ends to be able to see each other
	    if (LOS.this.resistanceMap[this.testx][this.testy] < 1
		    && this.eliasRadiusStrategy.radius(LOS.this.startx, LOS.this.starty, this.testx,
			    this.testy) <= this.eliasRadiusStrategy.radius(LOS.this.startx, LOS.this.starty,
				    LOS.this.targetx, LOS.this.targety)
		    && los1.isReachable(LOS.this.resistanceMap, this.testx, this.testy, LOS.this.targetx,
			    LOS.this.targety, this.eliasRadiusStrategy)
		    && los2.isReachable(LOS.this.resistanceMap, LOS.this.startx, LOS.this.starty, this.testx,
			    this.testy, this.eliasRadiusStrategy)) {
		// record actual sight path used
		this.path = new LinkedList<>(los2.lastPath);
		this.path.addAll(los1.lastPath);
		this.succeeded = true;
	    }
	}
    }
}
