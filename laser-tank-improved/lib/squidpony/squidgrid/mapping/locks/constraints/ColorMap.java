package squidpony.squidgrid.mapping.locks.constraints;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.mapping.locks.util.GenerationFailureException;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

public class ColorMap {
    protected int xsum, ysum, xmin, xmax, ymin, ymax;
    protected OrderedMap<Coord, Integer> map;

    public ColorMap() {
	this.map = new OrderedMap<>();
	this.ymin = this.xmin = Integer.MAX_VALUE;
	this.ymax = this.xmax = Integer.MIN_VALUE;
    }

    public void set(final int x, final int y, final int color) {
	final Coord xy = Coord.get(x, y);
	if (this.map.get(xy) == null) {
	    this.xsum += x;
	    this.ysum += y;
	}
	this.map.put(xy, color);
	if (x < this.xmin) {
	    this.xmin = x;
	}
	if (x > this.xmax) {
	    this.xmax = x;
	}
	if (y < this.ymin) {
	    this.ymin = y;
	}
	if (y > this.ymax) {
	    this.ymax = y;
	}
    }

    public Integer get(final int x, final int y) {
	return this.map.get(Coord.get(x, y));
    }

    public Coord getCenter() {
	return Coord.get(this.xsum / this.map.size(), this.ysum / this.map.size());
    }

    public int getWidth() {
	return this.xmax - this.xmin + 1;
    }

    public int getHeight() {
	return this.ymax - this.ymin + 1;
    }

    public int getLeft() {
	return this.xmin;
    }

    public int getTop() {
	return this.ymin;
    }

    public int getRight() {
	return this.xmax;
    }

    public int getBottom() {
	return this.ymax;
    }

    protected boolean isConnected() {
	if (this.map.size() == 0) {
	    return false;
	}
	// Do a breadth first search starting at the top left to check if
	// every position is reachable.
	final OrderedSet<Coord> world = this.map.keysAsOrderedSet(), queue = new OrderedSet<>();
	queue.add(world.removeFirst());
	while (!queue.isEmpty()) {
	    final Coord pos = queue.removeFirst();
	    for (final Direction d : Direction.CARDINALS) {
		final Coord neighbor = pos.translate(d);
		if (world.contains(neighbor)) {
		    world.remove(neighbor);
		    queue.add(neighbor);
		}
	    }
	}
	return world.size() == 0;
    }

    public void checkConnected() {
	if (!this.isConnected()) {
	    // Parts of the map are unreachable!
	    throw new GenerationFailureException("ColorMap is not fully connected");
	}
    }
}
