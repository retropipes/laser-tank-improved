package squidpony.squidmath;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import squidpony.annotation.Beta;

/**
 * Creates a field of particles that tend to form a neuron image type
 * distribution. The distribution tends to reach towards the largest area of
 * empty space, but features many nice branches and curls as well.
 *
 * If no points are added before the populate method is run, the center of the
 * area is chosen as the single pre-populated point.
 *
 * Based on work by Nolithius
 *
 * http://www.nolithius.com/game-development/neural-particle-deposition
 *
 * Source code is available on GitHub:
 * https://github.com/Nolithius/neural-particle as well as Google Code (now
 * archived): http://code.google.com/p/neural-particle/
 *
 * @author @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class NeuralParticle implements Serializable {
    private static final long serialVersionUID = -3742942580678517149L;
    private final RNG rng;
    private final int maxDistance, minDistance, width, height;
    private final LinkedList<Coord> distribution = new LinkedList<>();

    public NeuralParticle(final int width, final int height, final int maxDistance, final RNG rng) {
	this.rng = rng;
	this.maxDistance = maxDistance;
	this.width = width;
	this.height = height;
	this.minDistance = 1;
    }

    /**
     * Populates the field with given number of points.
     *
     * @param quantity the number of points to insert
     */
    public void populate(final int quantity) {
	for (int i = 0; i < quantity; i++) {
	    this.add(this.createPoint());
	}
    }

    /**
     * Returns a list of the current distribution.
     *
     * @return the distribution as a List of Coord
     */
    public List<Coord> asList() {
	return new LinkedList<>(this.distribution);
    }

    /**
     * Returns an integer mapping of the current distribution.
     *
     * @param scale the value that active points will hold
     * @return a 2D int array, with all elements equal to either 0 or scale
     */
    public int[][] asIntMap(final int scale) {
	final int ret[][] = new int[this.width][this.height];
	for (final Coord p : this.distribution) {
	    ret[p.x][p.y] = scale;
	}
	return ret;
    }

    /**
     * Adds a single specific point to the distribution.
     *
     * @param point the Coord, also called a pip here, to insert
     */
    public void add(final Coord point) {
	this.distribution.add(point);
    }

    /**
     * Creates a pip that falls within the required distance from the current
     * distribution. Does not add the pip to the distribution.
     *
     * @return the created pip
     */
    public Coord createPoint() {
	Coord randomPoint = this.randomPoint();
	Coord nearestPoint = this.nearestPoint(randomPoint);
	double pointDistance = randomPoint.distance(nearestPoint);
	// Too close, toss
	while (pointDistance < this.minDistance) {
	    randomPoint = this.randomPoint();
	    nearestPoint = this.nearestPoint(randomPoint);
	    pointDistance = randomPoint.distance(nearestPoint);
	}
	// Adjust if we're too far
	if (pointDistance > this.maxDistance) {
	    // Calculate unit vector
	    final double unitX = (randomPoint.x - nearestPoint.x) / pointDistance;
	    final double unitY = (randomPoint.y - nearestPoint.y) / pointDistance;
	    randomPoint = Coord.get(
		    (int) (this.rng.between(this.minDistance, this.maxDistance + 1) * unitX + nearestPoint.x),
		    (int) (this.rng.between(this.minDistance, this.maxDistance + 1) * unitY + nearestPoint.y));
	}
	return randomPoint;
    }

    private Coord nearestPoint(final Coord point) {
	if (this.distribution.isEmpty()) {
	    final Coord center = Coord.get(this.width / 2, this.height / 2);
	    this.distribution.add(center);
	    return center;
	}
	Coord nearestPoint = this.distribution.getFirst();
	double nearestDistance = point.distance(nearestPoint);
	for (final Coord candidatePoint : this.distribution) {
	    final double candidateDistance = point.distance(candidatePoint);
	    if (candidateDistance > 0 && candidateDistance <= this.maxDistance) {
		return candidatePoint;
	    }
	    if (candidateDistance < nearestDistance) {
		nearestPoint = candidatePoint;
		nearestDistance = candidateDistance;
	    }
	}
	return nearestPoint;
    }

    private Coord randomPoint() {
	return Coord.get(this.rng.nextInt(this.width), this.rng.nextInt(this.height));
    }
}
