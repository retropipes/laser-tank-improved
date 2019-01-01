package squidpony.squidgrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import squidpony.squidmath.Coord;
import squidpony.squidmath.Coord3D;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.RNG;

/**
 * Basic radius strategy implementations likely to be used for roguelikes.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public enum Radius {
    /**
     * In an unobstructed area the FOV would be a square.
     *
     * This is the shape that would represent movement radius in an 8-way movement
     * scheme with no additional cost for diagonal movement.
     */
    SQUARE,
    /**
     * In an unobstructed area the FOV would be a diamond.
     *
     * This is the shape that would represent movement radius in a 4-way movement
     * scheme.
     */
    DIAMOND,
    /**
     * In an unobstructed area the FOV would be a circle.
     *
     * This is the shape that would represent movement radius in an 8-way movement
     * scheme with all movement cost the same based on distance from the source
     */
    CIRCLE,
    /**
     * In an unobstructed area the FOV would be a cube.
     *
     * This is the shape that would represent movement radius in an 8-way movement
     * scheme with no additional cost for diagonal movement.
     */
    CUBE,
    /**
     * In an unobstructed area the FOV would be a octahedron.
     *
     * This is the shape that would represent movement radius in a 4-way movement
     * scheme.
     */
    OCTAHEDRON,
    /**
     * In an unobstructed area the FOV would be a sphere.
     *
     * This is the shape that would represent movement radius in an 8-way movement
     * scheme with all movement cost the same based on distance from the source
     */
    SPHERE;
    private static final double PI2 = Math.PI * 2;

    public double radius(final int startx, final int starty, final int startz, final int endx, final int endy,
	    final int endz) {
	return this.radius((double) startx, (double) starty, (double) startz, (double) endx, (double) endy,
		(double) endz);
    }

    public double radius(final double startx, final double starty, final double startz, final double endx,
	    final double endy, final double endz) {
	final double dx = Math.abs(startx - endx);
	final double dy = Math.abs(starty - endy);
	final double dz = Math.abs(startz - endz);
	return this.radius(dx, dy, dz);
    }

    public double radius(final int dx, final int dy, final int dz) {
	return this.radius((float) dx, (float) dy, (float) dz);
    }

    public double radius(double dx, double dy, double dz) {
	dx = Math.abs(dx);
	dy = Math.abs(dy);
	dz = Math.abs(dz);
	double radius = 0;
	switch (this) {
	case SQUARE:
	case CUBE:
	    radius = Math.max(dx, Math.max(dy, dz));// radius is longest axial distance
	    break;
	case DIAMOND:
	case OCTAHEDRON:
	    radius = dx + dy + dz;// radius is the manhattan distance
	    break;
	case CIRCLE:
	case SPHERE:
	    radius = Math.sqrt(dx * dx + dy * dy + dz * dz);// standard spherical radius
	}
	return radius;
    }

    public double radius(final int startx, final int starty, final int endx, final int endy) {
	return this.radius((double) startx, (double) starty, (double) endx, (double) endy);
    }

    public double radius(final Coord start, final Coord end) {
	return this.radius((double) start.x, (double) start.y, (double) end.x, (double) end.y);
    }

    public double radius(final Coord end) {
	return this.radius(0.0, 0.0, end.x, end.y);
    }

    public double radius(final double startx, final double starty, final double endx, final double endy) {
	final double dx = startx - endx;
	final double dy = starty - endy;
	return this.radius(dx, dy);
    }

    public double radius(final int dx, final int dy) {
	return this.radius((double) dx, (double) dy);
    }

    public double radius(final double dx, final double dy) {
	return this.radius(dx, dy, 0);
    }

    public Coord onUnitShape(final double distance, final RNG rng) {
	int x = 0, y = 0;
	switch (this) {
	case SQUARE:
	case CUBE:
	    x = rng.between((int) -distance, (int) distance + 1);
	    y = rng.between((int) -distance, (int) distance + 1);
	    break;
	case DIAMOND:
	case OCTAHEDRON:
	    x = rng.between((int) -distance, (int) distance + 1);
	    y = rng.between((int) -distance, (int) distance + 1);
	    if (this.radius(x, y) > distance) {
		if (x > 0) {
		    if (y > 0) {
			x = (int) (distance - x);
			y = (int) (distance - y);
		    } else {
			x = (int) (distance - x);
			y = (int) (-distance - y);
		    }
		} else {
		    if (y > 0) {
			x = (int) (-distance - x);
			y = (int) (distance - y);
		    } else {
			x = (int) (-distance - x);
			y = (int) (-distance - y);
		    }
		}
	    }
	    break;
	case CIRCLE:
	case SPHERE:
	    final double radius = distance * Math.sqrt(rng.between(0.0, 1.0));
	    final double theta = rng.between(0, Radius.PI2);
	    x = (int) Math.round(Math.cos(theta) * radius);
	    y = (int) Math.round(Math.sin(theta) * radius);
	}
	return Coord.get(x, y);
    }

    public Coord3D onUnitShape3D(final double distance, final RNG rng) {
	int x = 0, y = 0, z = 0;
	switch (this) {
	case SQUARE:
	case DIAMOND:
	case CIRCLE:
	    final Coord p = this.onUnitShape(distance, rng);
	    return new Coord3D(p.x, p.y, 0);// 2D strategies
	case CUBE:
	    x = rng.between((int) -distance, (int) distance + 1);
	    y = rng.between((int) -distance, (int) distance + 1);
	    z = rng.between((int) -distance, (int) distance + 1);
	    break;
	case OCTAHEDRON:
	case SPHERE:
	    do {
		x = rng.between((int) -distance, (int) distance + 1);
		y = rng.between((int) -distance, (int) distance + 1);
		z = rng.between((int) -distance, (int) distance + 1);
	    } while (this.radius(x, y, z) > distance);
	}
	return new Coord3D(x, y, z);
    }

    public double volume2D(final double radiusLength) {
	switch (this) {
	case SQUARE:
	case CUBE:
	    return (radiusLength * 2 + 1) * (radiusLength * 2 + 1);
	case DIAMOND:
	case OCTAHEDRON:
	    return radiusLength * (radiusLength + 1) * 2 + 1;
	default:
	    return Math.PI * radiusLength * radiusLength + 1;
	}
    }

    public double volume3D(final double radiusLength) {
	switch (this) {
	case SQUARE:
	case CUBE:
	    return (radiusLength * 2 + 1) * (radiusLength * 2 + 1) * (radiusLength * 2 + 1);
	case DIAMOND:
	case OCTAHEDRON:
	    double total = radiusLength * (radiusLength + 1) * 2 + 1;
	    for (double i = radiusLength - 1; i >= 0; i--) {
		total += (i * (i + 1) * 2 + 1) * 2;
	    }
	    return total;
	default:
	    return Math.PI * radiusLength * radiusLength * radiusLength * 4.0 / 3.0 + 1;
	}
    }

    private int clamp(final int n, final int min, final int max) {
	return Math.min(Math.max(min, n), max - 1);
    }

    public OrderedSet<Coord> perimeter(final Coord center, final int radiusLength, final boolean surpassEdges,
	    final int width, final int height) {
	final OrderedSet<Coord> rim = new OrderedSet<>(4 * radiusLength);
	if (!surpassEdges && (center.x < 0 || center.x >= width || center.y < 0 || center.y > height)) {
	    return rim;
	}
	if (radiusLength < 1) {
	    rim.add(center);
	    return rim;
	}
	switch (this) {
	case SQUARE:
	case CUBE: {
	    for (int i = center.x - radiusLength; i <= center.x + radiusLength; i++) {
		int x = i;
		if (!surpassEdges) {
		    x = this.clamp(i, 0, width);
		}
		rim.add(Coord.get(x, this.clamp(center.y - radiusLength, 0, height)));
		rim.add(Coord.get(x, this.clamp(center.y + radiusLength, 0, height)));
	    }
	    for (int j = center.y - radiusLength; j <= center.y + radiusLength; j++) {
		int y = j;
		if (!surpassEdges) {
		    y = this.clamp(j, 0, height);
		}
		rim.add(Coord.get(this.clamp(center.x - radiusLength, 0, height), y));
		rim.add(Coord.get(this.clamp(center.x + radiusLength, 0, height), y));
	    }
	}
	    break;
	case DIAMOND:
	case OCTAHEDRON: {
	    int xUp = center.x + radiusLength, xDown = center.x - radiusLength, yUp = center.y + radiusLength,
		    yDown = center.y - radiusLength;
	    if (!surpassEdges) {
		xDown = this.clamp(xDown, 0, width);
		xUp = this.clamp(xUp, 0, width);
		yDown = this.clamp(yDown, 0, height);
		yUp = this.clamp(yUp, 0, height);
	    }
	    rim.add(Coord.get(xDown, center.y));
	    rim.add(Coord.get(xUp, center.y));
	    rim.add(Coord.get(center.x, yDown));
	    rim.add(Coord.get(center.x, yUp));
	    for (int i = xDown + 1, c = 1; i < center.x; i++, c++) {
		int x = i;
		if (!surpassEdges) {
		    x = this.clamp(i, 0, width);
		}
		rim.add(Coord.get(x, this.clamp(center.y - c, 0, height)));
		rim.add(Coord.get(x, this.clamp(center.y + c, 0, height)));
	    }
	    for (int i = center.x + 1, c = 1; i < center.x + radiusLength; i++, c++) {
		int x = i;
		if (!surpassEdges) {
		    x = this.clamp(i, 0, width);
		}
		rim.add(Coord.get(x, this.clamp(center.y + radiusLength - c, 0, height)));
		rim.add(Coord.get(x, this.clamp(center.y - radiusLength + c, 0, height)));
	    }
	}
	    break;
	default: {
	    double theta;
	    int x, y, denom = 1;
	    boolean anySuccesses;
	    while (denom <= 256) {
		anySuccesses = false;
		for (int i = 1; i <= denom; i += 2) {
		    theta = i * (Radius.PI2 / denom);
		    x = (int) (Math.cos(theta) * (radiusLength + 0.25)) + center.x;
		    y = (int) (Math.sin(theta) * (radiusLength + 0.25)) + center.y;
		    if (!surpassEdges) {
			x = this.clamp(x, 0, width);
			y = this.clamp(y, 0, height);
		    }
		    final Coord p = Coord.get(x, y);
		    final boolean test = !rim.contains(p);
		    rim.add(p);
		    anySuccesses = test || anySuccesses;
		}
		if (!anySuccesses) {
		    break;
		}
		denom *= 2;
	    }
	}
	}
	return rim;
    }

    public Coord extend(final Coord center, final Coord middle, final int radiusLength, final boolean surpassEdges,
	    final int width, final int height) {
	if (!surpassEdges && (center.x < 0 || center.x >= width || center.y < 0 || center.y > height || middle.x < 0
		|| middle.x >= width || middle.y < 0 || middle.y > height)) {
	    return Coord.get(0, 0);
	}
	if (radiusLength < 1) {
	    return center;
	}
	final double theta = Math.atan2(middle.y - center.y, middle.x - center.x), cosTheta = Math.cos(theta),
		sinTheta = Math.sin(theta);
	Coord end = Coord.get(middle.x, middle.y);
	switch (this) {
	case SQUARE:
	case CUBE:
	case DIAMOND:
	case OCTAHEDRON: {
	    int rad2 = 0;
	    if (surpassEdges) {
		while (this.radius(center.x, center.y, end.x, end.y) < radiusLength) {
		    rad2++;
		    end = Coord.get((int) Math.round(cosTheta * rad2) + center.x,
			    (int) Math.round(sinTheta * rad2) + center.y);
		}
	    } else {
		while (this.radius(center.x, center.y, end.x, end.y) < radiusLength) {
		    rad2++;
		    end = Coord.get(this.clamp((int) Math.round(cosTheta * rad2) + center.x, 0, width),
			    this.clamp((int) Math.round(sinTheta * rad2) + center.y, 0, height));
		    if (end.x == 0 || end.x == width - 1 || end.y == 0 || end.y == height - 1) {
			return end;
		    }
		}
	    }
	    return end;
	}
	default: {
	    end = Coord.get(this.clamp((int) Math.round(cosTheta * radiusLength) + center.x, 0, width),
		    this.clamp((int) Math.round(sinTheta * radiusLength) + center.y, 0, height));
	    if (!surpassEdges) {
		long edgeLength = 0;
//                    if (end.x == 0 || end.x == width - 1 || end.y == 0 || end.y == height - 1)
		if (end.x < 0) {
		    // wow, we lucked out here. the only situation where cos(angle) is 0 is if the
		    // angle aims
		    // straight up or down, and then x cannot be < 0 or >= width.
		    edgeLength = Math.round((0 - center.x) / cosTheta);
		    end = end.setY(this.clamp((int) Math.round(sinTheta * edgeLength) + center.y, 0, height));
		} else if (end.x >= width) {
		    // wow, we lucked out here. the only situation where cos(angle) is 0 is if the
		    // angle aims
		    // straight up or down, and then x cannot be < 0 or >= width.
		    edgeLength = Math.round((width - 1 - center.x) / cosTheta);
		    end = end.setY(this.clamp((int) Math.round(sinTheta * edgeLength) + center.y, 0, height));
		}
		if (end.y < 0) {
		    // wow, we lucked out here. the only situation where sin(angle) is 0 is if the
		    // angle aims
		    // straight left or right, and then y cannot be < 0 or >= height.
		    edgeLength = Math.round((0 - center.y) / sinTheta);
		    end = end.setX(this.clamp((int) Math.round(cosTheta * edgeLength) + center.x, 0, width));
		} else if (end.y >= height) {
		    // wow, we lucked out here. the only situation where sin(angle) is 0 is if the
		    // angle aims
		    // straight left or right, and then y cannot be < 0 or >= height.
		    edgeLength = Math.round((height - 1 - center.y) / sinTheta);
		    end = end.setX(this.clamp((int) Math.round(cosTheta * edgeLength) + center.x, 0, width));
		}
	    }
	    return end;
	}
	}
    }

    /**
     * Compares two Radius enums as if they are both in a 2D plane; that is,
     * Radius.SPHERE is treated as equal to Radius.CIRCLE, Radius.CUBE is equal to
     * Radius.SQUARE, and Radius.OCTAHEDRON is equal to Radius.DIAMOND.
     *
     * @param other the Radius to compare this to
     * @return true if the 2D versions of both Radius enums are the same shape.
     */
    public boolean equals2D(final Radius other) {
	switch (this) {
	case CIRCLE:
	case SPHERE:
	    return other == CIRCLE || other == SPHERE;
	case SQUARE:
	case CUBE:
	    return other == SQUARE || other == CUBE;
	default:
	    return other == DIAMOND || other == OCTAHEDRON;
	}
    }

    public boolean inRange(final int startx, final int starty, final int endx, final int endy, final int minRange,
	    final int maxRange) {
	final double dist = this.radius(startx, starty, endx, endy);
	return dist >= minRange - 0.001 && dist <= maxRange + 0.001;
    }

    public int roughDistance(final int xPos, final int yPos) {
	final int x = Math.abs(xPos), y = Math.abs(yPos);
	switch (this) {
	case CIRCLE:
	case SPHERE: {
	    if (x == y) {
		return 3 * x;
	    } else if (x < y) {
		return 3 * x + 2 * (y - x);
	    } else {
		return 3 * y + 2 * (x - y);
	    }
	}
	case DIAMOND:
	case OCTAHEDRON:
	    return 2 * (x + y);
	default:
	    return 2 * Math.max(x, y);
	}
    }

    public List<Coord> pointsInside(final int centerX, final int centerY, final int radiusLength,
	    final boolean surpassEdges, final int width, final int height) {
	return this.pointsInside(centerX, centerY, radiusLength, surpassEdges, width, height, null);
    }

    public List<Coord> pointsInside(final Coord center, final int radiusLength, final boolean surpassEdges,
	    final int width, final int height) {
	if (center == null) {
	    return null;
	}
	return this.pointsInside(center.x, center.y, radiusLength, surpassEdges, width, height, null);
    }

    public List<Coord> pointsInside(final int centerX, final int centerY, final int radiusLength,
	    final boolean surpassEdges, final int width, final int height, final List<Coord> buf) {
	final List<Coord> contents = buf == null ? new ArrayList<>((int) Math.ceil(this.volume2D(radiusLength))) : buf;
	if (!surpassEdges && (centerX < 0 || centerX >= width || centerY < 0 || centerY >= height)) {
	    return contents;
	}
	if (radiusLength < 1) {
	    contents.add(Coord.get(centerX, centerY));
	    return contents;
	}
	switch (this) {
	case SQUARE:
	case CUBE: {
	    for (int i = centerX - radiusLength; i <= centerX + radiusLength; i++) {
		for (int j = centerY - radiusLength; j <= centerY + radiusLength; j++) {
		    if (!surpassEdges && (i < 0 || j < 0 || i >= width || j >= height)) {
			continue;
		    }
		    contents.add(Coord.get(i, j));
		}
	    }
	}
	    break;
	case DIAMOND:
	case OCTAHEDRON: {
	    for (int i = centerX - radiusLength; i <= centerX + radiusLength; i++) {
		for (int j = centerY - radiusLength; j <= centerY + radiusLength; j++) {
		    if (Math.abs(centerX - i) + Math.abs(centerY - j) > radiusLength
			    || !surpassEdges && (i < 0 || j < 0 || i >= width || j >= height)) {
			continue;
		    }
		    contents.add(Coord.get(i, j));
		}
	    }
	}
	    break;
	default: {
	    float high, changedX;
	    int rndX, rndY;
	    for (int dx = -radiusLength; dx <= radiusLength; ++dx) {
		changedX = dx - 0.25f * Math.signum(dx);
		rndX = Math.round(changedX);
		high = (float) Math.sqrt(radiusLength * radiusLength - changedX * changedX);
		if (surpassEdges || !(centerX + rndX < 0 || centerX + rndX >= width)) {
		    contents.add(Coord.get(centerX + rndX, centerY));
		}
		for (float dy = high; dy >= 0.75f; --dy) {
		    rndY = Math.round(dy - 0.25f);
		    if (surpassEdges || !(centerX + rndX < 0 || centerY + rndY < 0 || centerX + rndX >= width
			    || centerY + rndY >= height)) {
			contents.add(Coord.get(centerX + rndX, centerY + rndY));
		    }
		    if (surpassEdges || !(centerX + rndX < 0 || centerY - rndY < 0 || centerX + rndX >= width
			    || centerY - rndY >= height)) {
			contents.add(Coord.get(centerX + rndX, centerY - rndY));
		    }
		}
	    }
	}
	}
	return contents;
    }

    /**
     * Gets a List of all Coord points within {@code radiusLength} of {@code center}
     * using Chebyshev measurement (making a square). Appends Coords to {@code buf}
     * if it is non-null, and returns either buf or a freshly-allocated List of
     * Coord. If {@code surpassEdges} is false, which is the normal usage, this will
     * not produce Coords with x or y less than 0 or greater than {@code width} or
     * {@code height}; if surpassEdges is true, then it can produce any Coords in
     * the actual radius.
     *
     * @param centerX      the center Coord x
     * @param centerY      the center Coord x
     * @param radiusLength the inclusive distance from (centerX,centerY) for Coords
     *                     to use in the List
     * @param surpassEdges usually should be false; if true, can produce Coords with
     *                     negative x/y or past width/height
     * @param width        the width of the area this can place Coords (exclusive,
     *                     not relative to center, usually map width)
     * @param height       the height of the area this can place Coords (exclusive,
     *                     not relative to center, usually map height)
     * @return a new List containing the points within radiusLength of the center
     *
     */
    public static List<Coord> inSquare(final int centerX, final int centerY, final int radiusLength,
	    final boolean surpassEdges, final int width, final int height) {
	return SQUARE.pointsInside(centerX, centerY, radiusLength, surpassEdges, width, height, null);
    }

    /**
     * Gets a List of all Coord points within {@code radiusLength} of {@code center}
     * using Manhattan measurement (making a diamond). Appends Coords to {@code buf}
     * if it is non-null, and returns either buf or a freshly-allocated List of
     * Coord. If {@code surpassEdges} is false, which is the normal usage, this will
     * not produce Coords with x or y less than 0 or greater than {@code width} or
     * {@code height}; if surpassEdges is true, then it can produce any Coords in
     * the actual radius.
     *
     * @param centerX      the center Coord x
     * @param centerY      the center Coord x
     * @param radiusLength the inclusive distance from (centerX,centerY) for Coords
     *                     to use in the List
     * @param surpassEdges usually should be false; if true, can produce Coords with
     *                     negative x/y or past width/height
     * @param width        the width of the area this can place Coords (exclusive,
     *                     not relative to center, usually map width)
     * @param height       the height of the area this can place Coords (exclusive,
     *                     not relative to center, usually map height)
     * @return a new List containing the points within radiusLength of the center
     */
    public static List<Coord> inDiamond(final int centerX, final int centerY, final int radiusLength,
	    final boolean surpassEdges, final int width, final int height) {
	return DIAMOND.pointsInside(centerX, centerY, radiusLength, surpassEdges, width, height, null);
    }

    /**
     * Gets a List of all Coord points within {@code radiusLength} of {@code center}
     * using Euclidean measurement (making a circle). Appends Coords to {@code buf}
     * if it is non-null, and returns either buf or a freshly-allocated List of
     * Coord. If {@code surpassEdges} is false, which is the normal usage, this will
     * not produce Coords with x or y less than 0 or greater than {@code width} or
     * {@code height}; if surpassEdges is true, then it can produce any Coords in
     * the actual radius.
     *
     * @param centerX      the center Coord x
     * @param centerY      the center Coord x
     * @param radiusLength the inclusive distance from (centerX,centerY) for Coords
     *                     to use in the List
     * @param surpassEdges usually should be false; if true, can produce Coords with
     *                     negative x/y or past width/height
     * @param width        the width of the area this can place Coords (exclusive,
     *                     not relative to center, usually map width)
     * @param height       the height of the area this can place Coords (exclusive,
     *                     not relative to center, usually map height)
     * @return a new List containing the points within radiusLength of the center
     */
    public static List<Coord> inCircle(final int centerX, final int centerY, final int radiusLength,
	    final boolean surpassEdges, final int width, final int height) {
	return CIRCLE.pointsInside(centerX, centerY, radiusLength, surpassEdges, width, height, null);
    }

    /**
     * Gets a List of all Coord points within {@code radiusLength} of {@code center}
     * using Chebyshev measurement (making a square). Appends Coords to {@code buf}
     * if it is non-null, and returns either buf or a freshly-allocated List of
     * Coord. If {@code surpassEdges} is false, which is the normal usage, this will
     * not produce Coords with x or y less than 0 or greater than {@code width} or
     * {@code height}; if surpassEdges is true, then it can produce any Coords in
     * the actual radius.
     *
     * @param centerX      the center Coord x
     * @param centerY      the center Coord x
     * @param radiusLength the inclusive distance from (centerX,centerY) for Coords
     *                     to use in the List
     * @param surpassEdges usually should be false; if true, can produce Coords with
     *                     negative x/y or past width/height
     * @param width        the width of the area this can place Coords (exclusive,
     *                     not relative to center, usually map width)
     * @param height       the height of the area this can place Coords (exclusive,
     *                     not relative to center, usually map height)
     * @param buf          the List of Coord to append points to; may be null to
     *                     create a new List
     * @return buf, after appending Coords to it, or a new List if buf was null
     */
    public static List<Coord> inSquare(final int centerX, final int centerY, final int radiusLength,
	    final boolean surpassEdges, final int width, final int height, final List<Coord> buf) {
	return SQUARE.pointsInside(centerX, centerY, radiusLength, surpassEdges, width, height, buf);
    }

    /**
     * Gets a List of all Coord points within {@code radiusLength} of {@code center}
     * using Manhattan measurement (making a diamond). Appends Coords to {@code buf}
     * if it is non-null, and returns either buf or a freshly-allocated List of
     * Coord. If {@code surpassEdges} is false, which is the normal usage, this will
     * not produce Coords with x or y less than 0 or greater than {@code width} or
     * {@code height}; if surpassEdges is true, then it can produce any Coords in
     * the actual radius.
     *
     * @param centerX      the center Coord x
     * @param centerY      the center Coord x
     * @param radiusLength the inclusive distance from (centerX,centerY) for Coords
     *                     to use in the List
     * @param surpassEdges usually should be false; if true, can produce Coords with
     *                     negative x/y or past width/height
     * @param width        the width of the area this can place Coords (exclusive,
     *                     not relative to center, usually map width)
     * @param height       the height of the area this can place Coords (exclusive,
     *                     not relative to center, usually map height)
     * @param buf          the List of Coord to append points to; may be null to
     *                     create a new List
     * @return buf, after appending Coords to it, or a new List if buf was null
     */
    public static List<Coord> inDiamond(final int centerX, final int centerY, final int radiusLength,
	    final boolean surpassEdges, final int width, final int height, final List<Coord> buf) {
	return DIAMOND.pointsInside(centerX, centerY, radiusLength, surpassEdges, width, height, buf);
    }

    /**
     * Gets a List of all Coord points within {@code radiusLength} of {@code center}
     * using Euclidean measurement (making a circle). Appends Coords to {@code buf}
     * if it is non-null, and returns either buf or a freshly-allocated List of
     * Coord. If {@code surpassEdges} is false, which is the normal usage, this will
     * not produce Coords with x or y less than 0 or greater than {@code width} or
     * {@code height}; if surpassEdges is true, then it can produce any Coords in
     * the actual radius.
     *
     * @param centerX      the center Coord x
     * @param centerY      the center Coord x
     * @param radiusLength the inclusive distance from (centerX,centerY) for Coords
     *                     to use in the List
     * @param surpassEdges usually should be false; if true, can produce Coords with
     *                     negative x/y or past width/height
     * @param width        the width of the area this can place Coords (exclusive,
     *                     not relative to center, usually map width)
     * @param height       the height of the area this can place Coords (exclusive,
     *                     not relative to center, usually map height)
     * @param buf          the List of Coord to append points to; may be null to
     *                     create a new List
     * @return buf, after appending Coords to it, or a new List if buf was null
     */
    public static List<Coord> inCircle(final int centerX, final int centerY, final int radiusLength,
	    final boolean surpassEdges, final int width, final int height, final List<Coord> buf) {
	return CIRCLE.pointsInside(centerX, centerY, radiusLength, surpassEdges, width, height, buf);
    }

    /**
     * Given an Iterable of Coord (such as a List or Set), a distance to expand
     * outward by (using this Radius), and the bounding height and width of the map,
     * gets a "thickened" group of Coord as a Set where each Coord in points has
     * been expanded out by an amount no greater than distance. As an example, you
     * could call this on a line generated by Bresenham, OrthoLine, or an LOS
     * object's getLastPath() method, and expand the line into a thick "brush
     * stroke" where this Radius affects the shape of the ends. This will never
     * produce a Coord with negative x or y, a Coord with x greater than or equal to
     * width, or a Coord with y greater than or equal to height.
     *
     * @param distance the distance, as measured by this Radius, to expand each
     *                 Coord on points up to
     * @param width    the bounding width of the map (exclusive)
     * @param height   the bounding height of the map (exclusive)
     * @param points   an Iterable (such as a List or Set) of Coord that this will
     *                 make a "thickened" version of
     * @return a Set of Coord that covers a wider area than what points covers; each
     *         Coord will be unique (it's a Set)
     */
    public Set<Coord> expand(final int distance, final int width, final int height, final Iterable<Coord> points) {
	final List<Coord> around = this.pointsInside(Coord.get(distance, distance), distance, false, width, height);
	final OrderedSet<Coord> expanded = new OrderedSet<>(around.size() * 16);
	int tx, ty;
	for (final Coord pt : points) {
	    for (final Coord ar : around) {
		tx = pt.x + ar.x - distance;
		ty = pt.y + ar.y - distance;
		if (tx >= 0 && tx < width && ty >= 0 && ty < height) {
		    expanded.add(Coord.get(tx, ty));
		}
	    }
	}
	return expanded;
    }
}
