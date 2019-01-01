package squidpony.squidgrid.zone;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;

/**
 * Abstraction over a list of {@link Coord}. This allows to use the short arrays
 * coming from {@link squidpony.squidmath.CoordPacker}, which are compressed for
 * better memory usage, regular {@link List lists of Coord}, which are often the
 * simplest option, or {@link squidpony.squidmath.GreasedRegion GreasedRegions},
 * which are "greasy" in the fatty-food sense (they are heavier objects, and are
 * uncompressed) but also "greased" like greased lightning (they are very fast
 * at spatial transformations on their region).
 * <p>
 * Zones are {@link Serializable}, but serialization doesn't change the internal
 * representation (some would want to pack {@link ListZone} into
 * {@link CoordPackerZone}s when serializing). I find that overzealous for a
 * simple interface. If you want your zones to be be packed when serialized,
 * create {@link CoordPackerZone} yourself. In squidlib-extra, GreasedRegions
 * are given slightly special treatment during that JSON-like serialization so
 * they avoid repeating certain information, but they are still going to be
 * larger than compressed short arrays from CoordPacker.
 * </p>
 * <p>
 * While CoordPacker produces short arrays that can be wrapped in
 * CoordPackerZone objects, and a List of Coord can be similarly wrapped in a
 * ListZone object, GreasedRegion extends {@link Zone.Skeleton} and so
 * implements Zone itself. Unlike CoordPackerZone, which is immutable in
 * practice (changing the short array reference is impossible and changing the
 * elements rarely works as planned), GreasedRegion is mutable for performance
 * reasons, and may need copies to be created if you want to keep around older
 * GreasedRegions.
 * </p>
 *
 * <p>
 * The correct method to implement a {@link Zone} efficiently is to first try
 * implementing the interface directly, looking at each method and thinking
 * whether you can do something smart for it. Once you've inspected all methods,
 * then extend {@link Zone.Skeleton} (instead of Object in the first place) so
 * that it'll fill for you the methods for which you cannot provide a smart
 * implementation.
 * </p>
 *
 * @author smelC
 * @see squidpony.squidmath.CoordPacker
 * @see squidpony.squidmath.GreasedRegion
 */
public interface Zone extends Serializable, Iterable<Coord> {
    /**
     * @return Whether this zone is empty.
     */
    boolean isEmpty();

    /**
     * @return The number of cells that this zone contains (the size
     *         {@link #getAll()}).
     */
    int size();

    /**
     * @param x
     * @param y
     * @return Whether this zone contains the coordinate (x,y).
     */
    boolean contains(int x, int y);

    /**
     * @param c
     * @return Whether this zone contains {@code c}.
     */
    boolean contains(Coord c);

    /**
     * @param other
     * @return true if all cells of {@code other} are in {@code this}.
     */
    boolean contains(Zone other);

    /**
     * @param other
     * @return true if {@code this} and {@code other} have a common cell.
     */
    boolean intersectsWith(Zone other);

    /**
     * @return The approximate center of this zone, or null if this zone is empty.
     */
    /* @Nullable */ Coord getCenter();

    /**
     * @return The distance between the leftmost cell and the rightmost cell, or
     *         anything negative if {@code this} zone is empty; may be 0 if all
     *         cells are in one vertical line.
     */
    int getWidth();

    /**
     * @return The distance between the topmost cell and the lowest cell, or
     *         anything negative if {@code this} zone is empty; may be 0 if all
     *         cells are in one horizontal line.
     */
    int getHeight();

    /**
     * @return The approximation of the zone's diagonal, using {@link #getWidth()}
     *         and {@link #getHeight()}.
     */
    double getDiagonal();

    /**
     * @param smallestOrBiggest if true, finds the smallest x-coordinate value; if
     *                          false, finds the biggest.
     * @return The x-coordinate of the Coord within {@code this} that has the
     *         smallest (or biggest) x-coordinate. Or -1 if the zone is empty.
     */
    int x(boolean smallestOrBiggest);

    /**
     * @param smallestOrBiggest if true, finds the smallest y-coordinate value; if
     *                          false, finds the biggest.
     * @return The y-coordinate of the Coord within {@code this} that has the
     *         smallest (or biggest) y-coordinate. Or -1 if the zone is empty.
     */
    int y(boolean smallestOrBiggest);

    /**
     * @return All cells in this zone.
     */
    List<Coord> getAll();

    /** @return {@code this} shifted by {@code (c.x,c.y)} */
    Zone translate(Coord c);

    /** @return {@code this} shifted by {@code (x,y)} */
    Zone translate(int x, int y);

    /**
     * @return Cells in {@code this} that are adjacent to a cell not in {@code this}
     */
    Collection<Coord> getInternalBorder();

    /**
     * Gets a Collection of Coord values that are not in this Zone, but are adjacent
     * to it, either orthogonally or diagonally. Related to the fringe() methods in
     * CoordPacker and GreasedRegion, but guaranteed to use 8-way adjacency and to
     * return a new Collection of Coord.
     *
     * @return Cells adjacent to {@code this} (orthogonally or diagonally) that
     *         aren't in {@code this}
     */
    Collection<Coord> getExternalBorder();

    /**
     * Gets a new Zone that contains all the Coords in {@code this} plus all
     * neighboring Coords, which can be orthogonally or diagonally adjacent to any
     * Coord this has in it. Related to the expand() methods in CoordPacker and
     * GreasedRegion, but guaranteed to use 8-way adjacency and to return a new
     * Zone.
     *
     * @return A variant of {@code this} where cells adjacent to {@code this}
     *         (orthogonally or diagonally) have been added (i.e. it's {@code this}
     *         plus {@link #getExternalBorder()}).
     */
    Zone extend();

    /**
     * A convenience partial implementation. Please try for all new implementations
     * of {@link Zone} to be subtypes of this class. It usually prove handy at some
     * point to have a common superclass.
     *
     * @author smelC
     */
    abstract class Skeleton implements Zone {
	private transient Coord center = null;
	protected transient int width = -2;
	protected transient int height = -2;
	private static final long serialVersionUID = 4436698111716212256L;

	@Override
	/* Convenience implementation, feel free to override */
	public int size() {
	    return this.getAll().size();
	}

	@Override
	/* Convenience implementation, feel free to override */
	public boolean contains(final int x, final int y) {
	    for (final Coord in : this) {
		if (in.x == x && in.y == y) {
		    return true;
		}
	    }
	    return false;
	}

	@Override
	/* Convenience implementation, feel free to override */
	public boolean contains(final Coord c) {
	    return this.contains(c.x, c.y);
	}

	@Override
	/* Convenience implementation, feel free to override */
	public boolean contains(final Zone other) {
	    for (final Coord c : other) {
		if (!this.contains(c)) {
		    return false;
		}
	    }
	    return true;
	}

	@Override
	public boolean intersectsWith(final Zone other) {
	    final int tsz = this.size();
	    final int osz = other.size();
	    final Iterable<Coord> iteratedOver = tsz < osz ? this : other;
	    final Zone other_ = tsz < osz ? other : this;
	    for (final Coord c : iteratedOver) {
		if (other_.contains(c)) {
		    return true;
		}
	    }
	    return false;
	}

	@Override
	/*
	 * Convenience implementation, feel free to override, in particular if you can
	 * avoid allocating the list usually allocated by getAll().
	 */
	public Iterator<Coord> iterator() {
	    return this.getAll().iterator();
	}

	@Override
	/* Convenience implementation, feel free to override. */
	public int getWidth() {
	    if (this.width == -2) {
		this.width = this.isEmpty() ? -1 : this.x(false) - this.x(true);
	    }
	    return this.width;
	}

	@Override
	/* Convenience implementation, feel free to override. */
	public int getHeight() {
	    if (this.height == -2) {
		this.height = this.isEmpty() ? -1 : this.y(false) - this.y(true);
	    }
	    return this.height;
	}

	@Override
	public double getDiagonal() {
	    final int w = this.getWidth();
	    final int h = this.getHeight();
	    return Math.sqrt(w * w + h * h);
	}

	@Override
	/* Convenience implementation, feel free to override. */
	public int x(final boolean smallestOrBiggest) {
	    return smallestOrBiggest ? this.smallest(true) : this.biggest(true);
	}

	@Override
	/* Convenience implementation, feel free to override. */
	public int y(final boolean smallestOrBiggest) {
	    return smallestOrBiggest ? this.smallest(false) : this.biggest(false);
	}

	@Override
	/* Convenience implementation, feel free to override. */
	/*
	 * A possible enhancement would be to check that the center is within the zone,
	 * and if not to return the coord closest to the center, that is in the zone .
	 */
	public /* @Nullable */ Coord getCenter() {
	    if (this.center == null) {
		/* Need to compute it */
		if (this.isEmpty()) {
		    return null;
		}
		int x = 0, y = 0;
		float nb = 0;
		for (final Coord c : this) {
		    x += c.x;
		    y += c.y;
		    nb++;
		}
		/* Remember it */
		this.center = Coord.get(Math.round(x / nb), Math.round(y / nb));
	    }
	    return this.center;
	}

	@Override
	/* Convenience implementation, feel free to override. */
	public Zone translate(final Coord c) {
	    return this.translate(c.x, c.y);
	}

	@Override
	/* Convenience implementation, feel free to override. */
	public Zone translate(final int x, final int y) {
	    final List<Coord> initial = this.getAll();
	    final List<Coord> shifted = new ArrayList<>(initial);
	    final int sz = initial.size();
	    for (int i = 0; i < sz; i++) {
		final Coord c = initial.get(i);
		shifted.add(Coord.get(c.x + x, c.y + y));
	    }
	    return new ListZone(shifted);
	}

	@Override
	/* Convenience implementation, feel free to override. */
	public Collection<Coord> getInternalBorder() {
	    final int sz = this.size();
	    if (sz <= 1) {
		return this.getAll();
	    }
	    final List<Coord> result = new ArrayList<>(sz);
	    final List<Coord> all = this.getAll();
	    assert sz == all.size();
	    nextCell: for (int i = 0; i < sz; i++) {
		final Coord c = all.get(i);
		for (final Direction out : Direction.OUTWARDS) {
		    final Coord neighbor = c.translate(out);
		    if (!this.contains(neighbor)) {
			result.add(c);
			continue nextCell;
		    }
		}
	    }
	    return result;
	}

	@Override
	/* Convenience implementation, feel free to override. */
	public Collection<Coord> getExternalBorder() {
	    return DungeonUtility.border(this.getAll(), null);
	}

	@Override
	/* Convenience implementation, feel free to override. */
	public Zone extend() {
	    final List<Coord> list = new ArrayList<>(this.getAll());
	    list.addAll(this.getExternalBorder());
	    return new ListZone(list);
	}

	private int smallest(final boolean xOrY) {
	    if (this.isEmpty()) {
		return -1;
	    }
	    int min = Integer.MAX_VALUE;
	    if (xOrY) {
		for (final Coord c : this) {
		    if (c.x < min) {
			min = c.x;
		    }
		}
	    } else {
		for (final Coord c : this) {
		    if (c.y < min) {
			min = c.y;
		    }
		}
	    }
	    return min;
	}

	private int biggest(final boolean xOrY) {
	    int max = -1;
	    if (xOrY) {
		for (final Coord c : this) {
		    if (c.x > max) {
			max = c.x;
		    }
		}
	    } else {
		for (final Coord c : this) {
		    if (c.y > max) {
			max = c.y;
		    }
		}
	    }
	    return max;
	}
    }
}
