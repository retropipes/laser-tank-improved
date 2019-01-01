package squidpony.squidmath;

/**
 * Generic three dimensional coordinate class. Not cached in a pool because it
 * is rarely used internally.
 *
 * @author Lewis Potter
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Coord3D extends Coord {
    public int z;
    private static final long serialVersionUID = 1835370798982845336L;

    /**
     * Creates a three dimensional coordinate with the given location.
     *
     * @param x
     * @param y
     * @param z
     */
    public Coord3D(final int x, final int y, final int z) {
	super(x, y);
	this.z = z;
    }

    public static Coord3D get(final int x, final int y, final int z) {
	return new Coord3D(x, y, z);
    }

    /**
     * Returns the linear distance between this coordinate point and the provided
     * one.
     *
     * @param other
     * @return
     */
    public double distance(final Coord3D other) {
	return Math.sqrt(this.squareDistance(other));
    }

    /**
     * Returns the square of the linear distance between this coordinate point and
     * the provided one.
     *
     * @param other
     * @return
     */
    public double squareDistance(final Coord3D other) {
	final double dx = this.x - other.x;
	final double dy = this.y - other.y;
	final double dz = this.z - other.z;
	return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Returns the Manhattan distance between this point and the provided one. The
     * Manhattan distance is the distance between each point on each separate axis
     * all added together.
     *
     * @param other
     * @return
     */
    public int manhattanDistance(final Coord3D other) {
	int distance = Math.abs(this.x - other.x);
	distance += Math.abs(this.y - other.y);
	distance += Math.abs(this.z - other.z);
	return distance;
    }

    /**
     * Returns the largest difference between the two points along any one axis.
     *
     * @param other
     * @return
     */
    public int maxAxisDistance(final Coord3D other) {
	return Math.max(Math.max(Math.abs(this.x - other.x), Math.abs(this.y - other.y)), Math.abs(this.z - other.z));
    }

    @Override
    public int hashCode() {
	int hash = 5;
	hash = 73 * hash + this.x;
	hash = 73 * hash + this.y;
	hash = 73 * hash + this.z;
	return hash;
    }

    @Override
    public boolean equals(final Object o) {
	if (o instanceof Coord3D) {
	    final Coord3D other = (Coord3D) o;
	    return this.x == other.x && this.y == other.y && this.z == other.z;
	} else {
	    return false;
	}
    }

    @Override
    public String toString() {
	return "(" + this.x + "," + this.y + "," + this.z + ")";
    }
}
