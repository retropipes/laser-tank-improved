package squidpony.squidmath;

import java.io.Serializable;

/**
 * Coord using double values for x and y instead of int. Not pooled. Created by
 * Tommy Ettinger on 8/12/2015.
 */
public class CoordDouble implements Serializable {
    private static final long serialVersionUID = 300L;
    public double x;
    public double y;

    public CoordDouble() {
	this(0, 0);
    }

    public CoordDouble(final double x, final double y) {
	this.x = x;
	this.y = y;
    }

    public CoordDouble(final CoordDouble other) {
	this.x = other.x;
	this.y = other.y;
    }

    public CoordDouble(final Coord other) {
	this.x = other.x;
	this.y = other.y;
    }

    public static CoordDouble get(final double x, final double y) {
	return new CoordDouble(x, y);
    }

    public CoordDouble getLocation() {
	return new CoordDouble(this.x, this.y);
    }

    public void translate(final double x, final double y) {
	this.x += x;
	this.y += y;
    }

    public void setLocation(final double x, final double y) {
	this.x = x;
	this.y = y;
    }

    public void setLocation(final CoordDouble co) {
	this.x = co.x;
	this.y = co.y;
    }

    public void move(final int x, final int y) {
	this.x = x;
	this.y = y;
    }

    public double distance(final double x2, final double y2) {
	return Math.sqrt((x2 - this.x) * (x2 - this.x) + (y2 - this.y) * (y2 - this.y));
    }

    public double distance(final CoordDouble co) {
	return Math.sqrt((co.x - this.x) * (co.x - this.x) + (co.y - this.y) * (co.y - this.y));
    }

    public double distanceSq(final double x2, final double y2) {
	return (x2 - this.x) * (x2 - this.x) + (y2 - this.y) * (y2 - this.y);
    }

    public double distanceSq(final CoordDouble co) {
	return (co.x - this.x) * (co.x - this.x) + (co.y - this.y) * (co.y - this.y);
    }

    public double getX() {
	return this.x;
    }

    public void setX(final int x) {
	this.x = x;
    }

    public double getY() {
	return this.y;
    }

    public void setY(final int y) {
	this.y = y;
    }

    @Override
    public String toString() {
	return "Coord (x " + this.x + ", y " + this.y + ")";
    }

    @Override
    /*
     * smelC: This is Eclipse-generated code. The previous version was
     * Gwt-incompatible (because of Double.doubleToRawLongBits).
     */
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	long temp;
	temp = NumberTools.doubleToLongBits(this.x);
	result = prime * result + (int) (temp ^ temp >>> 32);
	temp = NumberTools.doubleToLongBits(this.y);
	result = prime * result + (int) (temp ^ temp >>> 32);
	return result;
    }

    @Override
    public boolean equals(final Object o) {
	if (o instanceof CoordDouble) {
	    final CoordDouble other = (CoordDouble) o;
	    return this.x == other.x && this.y == other.y;
	} else {
	    return false;
	}
    }
}
