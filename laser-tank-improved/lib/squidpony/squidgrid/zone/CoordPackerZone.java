package squidpony.squidgrid.zone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import squidpony.squidgrid.zone.Zone.Skeleton;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.CrossHash;

/**
 * A zone constructed by {@link CoordPacker}.
 *
 * @author smelC
 */
public class CoordPackerZone extends Skeleton implements Collection<Coord>, ImmutableZone {
    protected final short[] shorts;
    protected transient List<Coord> unpacked;
    private static final long serialVersionUID = -3718415979846804238L;

    public CoordPackerZone(final short[] shorts) {
	this.shorts = shorts;
    }

    @Override
    public boolean isEmpty() {
	return CoordPacker.isEmpty(this.shorts);
    }

    /**
     * Returns <tt>true</tt> if this collection contains the specified element. More
     * formally, returns <tt>true</tt> if and only if this collection contains at
     * least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this collection is to be tested
     * @return <tt>true</tt> if this collection contains the specified element
     * @throws ClassCastException   if the type of the specified element is
     *                              incompatible with this collection
     *                              (<a href="#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              collection does not permit null elements
     *                              (<a href="#optional-restrictions">optional</a>)
     */
    @Override
    public boolean contains(final Object o) {
	return o instanceof Coord && CoordPacker.queryPacked(this.shorts, ((Coord) o).x, ((Coord) o).y);
    }

    /**
     * Returns an array containing all of the elements in this collection. If this
     * collection makes any guarantees as to what order its elements are returned by
     * its iterator, this method must return the elements in the same order.
     * <p>
     * <p>
     * The returned array will be "safe" in that no references to it are maintained
     * by this collection. (In other words, this method must allocate a new array
     * even if this collection is backed by an array). The caller is thus free to
     * modify the returned array.
     * <p>
     * <p>
     * This method acts as bridge between array-based and collection-based APIs.
     *
     * @return an array containing all of the elements in this collection
     */
    @Override
    public Object[] toArray() {
	return CoordPacker.allPacked(this.shorts);
    }

    /**
     * Returns an array containing all of the elements in this collection; the
     * runtime type of the returned array is that of the specified array. If the
     * collection fits in the specified array, it is returned therein. Otherwise, a
     * new array is allocated with the runtime type of the specified array and the
     * size of this collection.
     * <p>
     * <p>
     * If this collection fits in the specified array with room to spare (i.e., the
     * array has more elements than this collection), the element in the array
     * immediately following the end of the collection is set to <tt>null</tt>.
     * (This is useful in determining the length of this collection <i>only</i> if
     * the caller knows that this collection does not contain any <tt>null</tt>
     * elements.)
     * <p>
     * <p>
     * If this collection makes any guarantees as to what order its elements are
     * returned by its iterator, this method must return the elements in the same
     * order.
     * <p>
     * <p>
     * Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs. Further, this method allows precise
     * control over the runtime type of the output array, and may, under certain
     * circumstances, be used to save allocation costs.
     * <p>
     * <p>
     * Suppose <tt>x</tt> is a collection known to contain only strings. The
     * following code can be used to dump the collection into a newly allocated
     * array of <tt>String</tt>:
     * <p>
     *
     * <pre>
     * String[] y = x.toArray(new String[0]);
     * </pre>
     * <p>
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     *
     * @param a the array into which the elements of this collection are to be
     *          stored, if it is big enough; otherwise, a new array of the same
     *          runtime type is allocated for this purpose.
     * @return an array containing all of the elements in this collection
     * @throws ArrayStoreException  if the runtime type of the specified array is
     *                              not a supertype of the runtime type of every
     *                              element in this collection
     * @throws NullPointerException if the specified array is null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
	if (a == null) {
	    throw new NullPointerException("Array passed to CoordPackerZone.toArray() must not be null");
	}
	final int size = a.length, ssize = CoordPacker.count(this.shorts);
	if (ssize == size) {
	    return (T[]) CoordPacker.allPacked(this.shorts);
	}
	a = Arrays.copyOf(a, ssize);
	for (int i = 0; i < ssize; i++) {
	    a[i] = (T) CoordPacker.nth(this.shorts, i);
	}
	return a;
    }

    /**
     * Does nothing (this Zone is immutable).
     */
    @Override
    public boolean add(final Coord coord) {
	return false;
    }

    /**
     * Does nothing (this Zone is immutable).
     */
    @Override
    public boolean remove(final Object o) {
	return false;
    }

    /**
     * Returns <tt>true</tt> if this collection contains all of the elements in the
     * specified collection.
     *
     * @param c collection to be checked for containment in this collection
     * @return <tt>true</tt> if this collection contains all of the elements in the
     *         specified collection
     * @throws ClassCastException if the types of one or more elements in the
     *                            specified collection are not Coord
     * @see #contains(Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean containsAll(final Collection<?> c) {
	return CoordPacker.count(this.shorts) == CoordPacker
		.count(CoordPacker.insertSeveralPacked(this.shorts, (Collection) c));
    }

    /**
     * Does nothing (this Zone is immutable).
     */
    @Override
    public boolean addAll(final Collection<? extends Coord> c) {
	return false;
    }

    /**
     * Does nothing (this Zone is immutable).
     */
    @Override
    public boolean removeAll(final Collection<?> c) {
	return false;
    }

    /**
     * Does nothing (this Zone is immutable).
     */
    @Override
    public boolean retainAll(final Collection<?> c) {
	return false;
    }

    /**
     * Does nothing (this Zone is immutable).
     */
    @Override
    public void clear() {
    }

    @Override
    public int size() {
	return CoordPacker.count(this.shorts);
    }

    @Override
    public boolean contains(final int x, final int y) {
	return CoordPacker.regionsContain(this.shorts, CoordPacker.packOne(x, y));
    }

    @Override
    public boolean contains(final Coord c) {
	return CoordPacker.regionsContain(this.shorts, CoordPacker.packOne(c));
    }

    @Override
    public List<Coord> getAll() {
	if (this.unpacked == null) {
	    final Coord[] allPacked = CoordPacker.allPacked(this.shorts);
	    this.unpacked = new ArrayList<>(allPacked.length);
	    Collections.addAll(this.unpacked, allPacked);
	}
	return this.unpacked;
    }

    @Override
    public CoordPackerZone expand(final int distance) {
	return new CoordPackerZone(CoordPacker.expand(this.shorts, distance, 256, 256));
    }

    @Override
    public CoordPackerZone expand8way(final int distance) {
	return new CoordPackerZone(CoordPacker.expand(this.shorts, distance, 256, 256, true));
    }

    @Override
    public boolean contains(final Zone other) {
	return CoordPacker.count(this.shorts) == CoordPacker
		.count(CoordPacker.insertSeveralPacked(this.shorts, other.getAll()));
    }

    @Override
    public boolean intersectsWith(final Zone other) {
	if (other instanceof CoordPackerZone) {
	    return CoordPacker.intersects(this.shorts, ((CoordPackerZone) other).shorts);
	}
	for (final Coord c : other) {
	    if (CoordPacker.queryPacked(this.shorts, c.x, c.y)) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public Zone extend() {
	return new CoordPackerZone(CoordPacker.expand(this.shorts, 1, 256, 256, true));
    }

    @Override
    public Collection<Coord> getInternalBorder() {
	return new CoordPackerZone(CoordPacker.surface(this.shorts, 1, 256, 256, true));
    }

    @Override
    public Collection<Coord> getExternalBorder() {
	return new CoordPackerZone(CoordPacker.fringe(this.shorts, 1, 256, 256, true));
    }

    @Override
    public Zone translate(final int x, final int y) {
	return new CoordPackerZone(CoordPacker.translate(this.shorts, x, y, 256, 256));
    }

    @Override
    public String toString() {
	return (this.unpacked == null ? this.shorts : this.unpacked).toString();
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final CoordPackerZone that = (CoordPackerZone) o;
	return Arrays.equals(this.shorts, that.shorts);
    }

    @Override
    public int hashCode() {
	return CrossHash.Falcon.hash(this.shorts);
    }
}