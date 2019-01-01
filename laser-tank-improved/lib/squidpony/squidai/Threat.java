package squidpony.squidai;

import squidpony.squidgrid.Radius;
import squidpony.squidmath.Coord;

/**
 * A small class to store the area that a creature is perceived by other
 * creatures to threaten. Created by Tommy Ettinger on 11/8/2015.
 */
public class Threat {
    public Coord position;
    public Reach reach;

    public Threat(final Coord position, final int maxThreatDistance) {
	this.position = position;
	this.reach = new Reach(maxThreatDistance);
    }

    public Threat(final Coord position, final int minThreatDistance, final int maxThreatDistance) {
	this.position = position;
	this.reach = new Reach(minThreatDistance, maxThreatDistance);
    }

    public Threat(final Coord position, final int minThreatDistance, final int maxThreatDistance,
	    final Radius measurement) {
	this.position = position;
	this.reach = new Reach(minThreatDistance, maxThreatDistance, measurement);
    }

    public Threat(final Coord position, final int minThreatDistance, final int maxThreatDistance,
	    final Radius measurement, final AimLimit limits) {
	this.position = position;
	this.reach = new Reach(minThreatDistance, maxThreatDistance, measurement, limits);
    }
}
