package squidpony.squidgrid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.RNG;

/**
 * A data structure that seems to be re-implemented often for games, this
 * associates Coord positions and generic I identities with generic E elements.
 * You can get an element from a SpatialMap with either an identity or a
 * position, change the position of an element without changing its value or
 * identity, modify an element given its identity and a new value, and perform
 * analogues to most of the features of the Map interface, though this does not
 * implement Map because it essentially has two key types and one value type.
 * You can also iterate through the values in insertion order, where insertion
 * order should be stable even when elements are moved or modified (the relevant
 * key is the identity, which is never changed in this class). Uses two
 * OrderedMap fields internally. Created by Tommy Ettinger on 1/2/2016.
 */
public class SpatialMap<I, E> implements Iterable<E> {
    public static class SpatialTriple<I, E> {
	public Coord position;
	public I id;
	public E element;

	public SpatialTriple() {
	    this.position = Coord.get(0, 0);
	    this.id = null;
	    this.element = null;
	}

	public SpatialTriple(final Coord position, final I id, final E element) {
	    this.position = position;
	    this.id = id;
	    this.element = element;
	}

	@Override
	public boolean equals(final Object o) {
	    if (this == o) {
		return true;
	    }
	    if (o == null || this.getClass() != o.getClass()) {
		return false;
	    }
	    final SpatialTriple<?, ?> that = (SpatialTriple<?, ?>) o;
	    if (this.position != null ? !this.position.equals(that.position) : that.position != null) {
		return false;
	    }
	    if (this.id != null ? !this.id.equals(that.id) : that.id != null) {
		return false;
	    }
	    return this.element != null ? this.element.equals(that.element) : that.element == null;
	}

	@Override
	public int hashCode() {
	    int result = this.position != null ? this.position.hashCode() : 0;
	    result = 31 * result + (this.id != null ? this.id.hashCode() : 0);
	    result = 31 * result + (this.element != null ? this.element.hashCode() : 0);
	    return result;
	}
    }

    protected OrderedMap<I, SpatialTriple<I, E>> itemMapping;
    protected OrderedMap<Coord, SpatialTriple<I, E>> positionMapping;

    /**
     * Constructs a SpatialMap with capacity 32.
     */
    public SpatialMap() {
	this.itemMapping = new OrderedMap<>(32);
	this.positionMapping = new OrderedMap<>(32);
    }

    /**
     * Constructs a SpatialMap with the given capacity
     *
     * @param capacity the capacity for each of the internal OrderedMaps
     */
    public SpatialMap(final int capacity) {
	this.itemMapping = new OrderedMap<>(capacity);
	this.positionMapping = new OrderedMap<>(capacity);
    }

    /**
     * Constructs a SpatialMap given arrays of Coord, identity, and element; all 3
     * arrays should have the same length, since this will use only up to the
     * minimum length of these arrays for how many it adds. Each unique id will be
     * added with the corresponding element at the corresponding Coord position if
     * that position is not already filled.
     *
     * @param coords   a starting array of Coord positions; indices here correspond
     *                 to the other parameters
     * @param ids      a starting array of identities; indices here correspond to
     *                 the other parameters
     * @param elements a starting array of elements; indices here correspond to the
     *                 other parameters
     */
    public SpatialMap(final Coord[] coords, final I[] ids, final E[] elements) {
	this.itemMapping = new OrderedMap<>(Math.min(coords.length, Math.min(ids.length, elements.length)));
	this.positionMapping = new OrderedMap<>(Math.min(coords.length, Math.min(ids.length, elements.length)));
	for (int i = 0; i < coords.length && i < ids.length && i < elements.length; i++) {
	    this.add(coords[i], ids[i], elements[i]);
	}
    }

    /**
     * Constructs a SpatialMap given collections of Coord, identity, and element;
     * all 3 collections should have the same length, since this will use only up to
     * the minimum length of these collections for how many it adds. Each unique id
     * will be added with the corresponding element at the corresponding Coord
     * position if that position is not already filled.
     *
     * @param coords   a starting collection of Coord positions; indices here
     *                 correspond to the other parameters
     * @param ids      a starting collection of identities; indices here correspond
     *                 to the other parameters
     * @param elements a starting collection of elements; indices here correspond to
     *                 the other parameters
     */
    public SpatialMap(final Collection<Coord> coords, final Collection<I> ids, final Collection<E> elements) {
	this.itemMapping = new OrderedMap<>(Math.min(coords.size(), Math.min(ids.size(), elements.size())));
	this.positionMapping = new OrderedMap<>(Math.min(coords.size(), Math.min(ids.size(), elements.size())));
	if (this.itemMapping.size() <= 0) {
	    return;
	}
	final Iterator<Coord> cs = coords.iterator();
	final Iterator<I> is = ids.iterator();
	final Iterator<E> es = elements.iterator();
	Coord c = cs.next();
	I i = is.next();
	E e = es.next();
	for (; cs.hasNext() && is.hasNext() && es.hasNext(); c = cs.next(), i = is.next(), e = es.next()) {
	    this.add(c, i, e);
	}
    }

    /**
     * Adds a new element with the given identity and Coord position. If the
     * position is already occupied by an element in this data structure, does
     * nothing. If the identity is already used, this also does nothing. If the
     * identity and position are both unused, this adds element to the data
     * structure. <br>
     * You should strongly avoid calling remove() and add() to change an element;
     * prefer modify() and move().
     *
     * @param coord   the Coord position to place the element at; should be empty
     * @param id      the identity to associate the element with; should be unused
     * @param element the element to add
     */
    public void add(final Coord coord, final I id, final E element) {
	if (this.itemMapping.containsKey(id)) {
	    return;
	}
	if (!this.positionMapping.containsKey(coord)) {
	    final SpatialTriple<I, E> triple = new SpatialTriple<>(coord, id, element);
	    this.itemMapping.put(id, triple);
	    this.positionMapping.put(coord, triple);
	}
    }

    /**
     * Inserts a new element with the given identity and Coord position, potentially
     * overwriting an existing element. <br>
     * If you want to alter an existing element, use modify() or move().
     *
     * @param coord   the Coord position to place the element at; should be empty
     * @param id      the identity to associate the element with; should be unused
     * @param element the element to add
     */
    public void put(final Coord coord, final I id, final E element) {
	final SpatialTriple<I, E> triple = new SpatialTriple<>(coord, id, element);
	this.itemMapping.remove(id);
	this.positionMapping.remove(coord);
	this.itemMapping.put(id, triple);
	this.positionMapping.put(coord, triple);
    }

    /**
     * Inserts a SpatialTriple into this SpatialMap without changing it, potentially
     * overwriting an existing element. SpatialTriple objects can be obtained by the
     * triples() or tripleIterator() methods, and can also be constructed on their
     * own. <br>
     * If you want to alter an existing element, use modify() or move().
     *
     * @param triple a SpatialTriple (an inner class of SpatialMap) with the same
     *               type parameters as this class
     */
    public void put(final SpatialTriple<I, E> triple) {
	this.itemMapping.remove(triple.id);
	this.positionMapping.remove(triple.position);
	this.itemMapping.put(triple.id, triple);
	this.positionMapping.put(triple.position, triple);
    }

    /**
     * Changes the element's value associated with id. The key id should exist
     * before calling this; if there is no matching id, this returns null.
     *
     * @param id       the identity of the element to modify
     * @param newValue the element value to replace the previous element with.
     * @return the previous element value associated with id
     */
    public E modify(final I id, final E newValue) {
	final SpatialTriple<I, E> gotten = this.itemMapping.get(id);
	if (gotten != null) {
	    final E previous = gotten.element;
	    gotten.element = newValue;
	    return previous;
	}
	return null;
    }

    /**
     * Changes the element's value associated with pos. The key pos should exist
     * before calling this; if there is no matching position, this returns null.
     *
     * @param pos      the position of the element to modify
     * @param newValue the element value to replace the previous element with.
     * @return the previous element value associated with id
     */
    public E positionalModify(final Coord pos, final E newValue) {
	final SpatialTriple<I, E> gotten = this.positionMapping.get(pos);
	if (gotten != null) {
	    final E previous = gotten.element;
	    gotten.element = newValue;
	    return previous;
	}
	return null;
    }

    /**
     * Move an element from one position to another; moves whatever is at the Coord
     * position previous to the new Coord position target. The element will not be
     * present at its original position if target is unoccupied, but nothing will
     * change if target is occupied.
     *
     * @param previous the starting Coord position of an element to move
     * @param target   the Coord position to move the element to
     * @return the moved element if movement was successful or null otherwise
     */
    public E move(final Coord previous, final Coord target) {
	if (this.positionMapping.containsKey(previous) && !this.positionMapping.containsKey(target)) {
	    final SpatialTriple<I, E> gotten = this.positionMapping.remove(previous);
	    gotten.position = target;
	    this.positionMapping.put(target, gotten);
	    return gotten.element;
	}
	return null;
    }

    /**
     * Move an element, picked by its identity, to a new Coord position. Finds the
     * element using only the id, and does not need the previous position. The
     * target position must be empty for this to move successfully, and the id must
     * exist in this data structure for this to move anything.
     *
     * @param id     the identity of the element to move
     * @param target the Coord position to move the element to
     * @return the moved element if movement was successful or null otherwise
     */
    public E move(final I id, final Coord target) {
	if (this.itemMapping.containsKey(id) && !this.positionMapping.containsKey(target)) {
	    final SpatialTriple<I, E> gotten = this.itemMapping.get(id);
	    this.positionMapping.remove(gotten.position);
	    gotten.position = target;
	    this.positionMapping.put(target, gotten);
	    return gotten.element;
	}
	return null;
    }

    /**
     * Removes the element at the given position from all storage in this data
     * structure. <br>
     * You should strongly avoid calling remove() and add() to change an element;
     * prefer modify() and move().
     *
     * @param coord the position of the element to remove
     * @return the value of the element that was removed or null if nothing was
     *         present at the position
     */
    public E remove(final Coord coord) {
	final SpatialTriple<I, E> gotten = this.positionMapping.remove(coord);
	if (gotten != null) {
	    this.itemMapping.remove(gotten.id);
	    return gotten.element;
	}
	return null;
    }

    /**
     * Removes the element with the given identity from all storage in this data
     * structure. <br>
     * You should strongly avoid calling remove() and add() to change an element;
     * prefer modify() and move().
     *
     * @param id the identity of the element to remove
     * @return the value of the element that was removed or null if nothing was
     *         present at the position
     */
    public E remove(final I id) {
	final SpatialTriple<I, E> gotten = this.itemMapping.remove(id);
	if (gotten != null) {
	    this.positionMapping.remove(gotten.position);
	    return gotten.element;
	}
	return null;
    }

    /**
     * Checks whether this contains the given element. Slower than containsKey and
     * containsPosition (linear time).
     *
     * @param o an Object that should be an element if you expect this to possibly
     *          return true
     * @return true if o is contained as an element in this data structure
     */
    public boolean containsValue(final Object o) {
	if (o == null) {
	    for (final SpatialTriple<I, E> v : this.itemMapping.values()) {
		if (v != null && v.element == null) {
		    return true;
		}
	    }
	} else {
	    for (final SpatialTriple<I, E> v : this.itemMapping.values()) {
		if (v != null && v.element != null && v.element.equals(o)) {
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     * Checks whether this contains the given identity key.
     *
     * @param o an Object that should be of the generic I type if you expect this to
     *          possibly return true
     * @return true if o is an identity key that can be used with this data
     *         structure
     */
    public boolean containsKey(final Object o) {
	return this.itemMapping.containsKey(o);
    }

    /**
     * Checks whether this contains anything at the given position.
     *
     * @param o an Object that should be a Coord if you expect this to possibly
     *          return true
     * @return true if o is a Coord that is associated with some element in this
     *         data structure
     */
    public boolean containsPosition(final Object o) {
	return this.positionMapping.containsKey(o);
    }

    /**
     * Gets the element at the given Coord position.
     *
     * @param c the position to get an element from
     * @return the element if it exists or null otherwise
     */
    public E get(final Coord c) {
	final SpatialTriple<I, E> gotten = this.positionMapping.get(c);
	if (gotten != null) {
	    return gotten.element;
	}
	return null;
    }

    /**
     * Gets the element with the given identity.
     *
     * @param i the identity of the element to get
     * @return the element if it exists or null otherwise
     */
    public E get(final I i) {
	final SpatialTriple<I, E> gotten = this.itemMapping.get(i);
	if (gotten != null) {
	    return gotten.element;
	}
	return null;
    }

    /**
     * Gets the position of the element with the given identity.
     *
     * @param i the identity of the element to get a position from
     * @return the position of the element if it exists or null otherwise
     */
    public Coord getPosition(final I i) {
	final SpatialTriple<I, E> gotten = this.itemMapping.get(i);
	if (gotten != null) {
	    return gotten.position;
	}
	return null;
    }

    /**
     * Gets the identity of the element at the given Coord position.
     *
     * @param c the position to get an identity from
     * @return the identity of the element if it exists at the given position or
     *         null otherwise
     */
    public I getIdentity(final Coord c) {
	final SpatialTriple<I, E> gotten = this.positionMapping.get(c);
	if (gotten != null) {
	    return gotten.id;
	}
	return null;
    }

    /**
     * Get a Set of all positions used for values in this data structure, returning
     * a OrderedSet (defensively copying the key set used internally) for its stable
     * iteration order.
     *
     * @return a OrderedSet of Coord corresponding to the positions present in this
     *         data structure.
     */
    public OrderedSet<Coord> positions() {
	return new OrderedSet<>(this.positionMapping.keySet());
    }

    /**
     * Get a Set of all identities used for values in this data structure, returning
     * a OrderedSet (defensively copying the key set used internally) for its stable
     * iteration order.
     *
     * @return a OrderedSet of I corresponding to the identities present in this
     *         data structure.
     */
    public OrderedSet<I> identities() {
	return new OrderedSet<>(this.itemMapping.keySet());
    }

    /**
     * Gets all data stored in this as a collection of values similar to Map.Entry,
     * but containing a Coord, I, and E value for each entry, in insertion order.
     * The type is SpatialTriple, defined in a nested class.
     *
     * @return a Collection of SpatialTriple of I, E
     */
    public Collection<SpatialTriple<I, E>> triples() {
	return this.itemMapping.values();
    }

    /**
     * Given an Iterable (such as a List, Set, or other Collection) of Coord, gets
     * all elements in this SpatialMap that share a position with one of the Coord
     * objects in positions and returns them as an ArrayList of elements.
     *
     * @param positions an Iterable (such as a List or Set) of Coord
     * @return an ArrayList, possibly empty, of elements that share a position with
     *         a Coord in positions
     */
    public ArrayList<E> getManyPositions(final Iterable<Coord> positions) {
	final ArrayList<E> gotten = new ArrayList<>();
	SpatialTriple<I, E> ie;
	for (final Coord p : positions) {
	    if ((ie = this.positionMapping.get(p)) != null) {
		gotten.add(ie.element);
	    }
	}
	return gotten;
    }

    /**
     * Given an Iterable (such as a List, Set, or other Collection) of I, gets all
     * elements in this SpatialMap that share an identity with one of the I objects
     * in identities and returns them as an ArrayList of elements.
     *
     * @param identities an Iterable (such as a List or Set) of I
     * @return an ArrayList, possibly empty, of elements that share an Identity with
     *         an I in identities
     */
    public ArrayList<E> getManyIdentities(final Iterable<I> identities) {
	final ArrayList<E> gotten = new ArrayList<>();
	SpatialTriple<I, E> ie;
	for (final I i : identities) {
	    if ((ie = this.itemMapping.get(i)) != null) {
		gotten.add(ie.element);
	    }
	}
	return gotten;
    }

    /**
     * Given an array of Coord, gets all elements in this SpatialMap that share a
     * position with one of the Coord objects in positions and returns them as an
     * ArrayList of elements.
     *
     * @param positions an array of Coord
     * @return an ArrayList, possibly empty, of elements that share a position with
     *         a Coord in positions
     */
    public ArrayList<E> getManyPositions(final Coord[] positions) {
	final ArrayList<E> gotten = new ArrayList<>(positions.length);
	SpatialTriple<I, E> ie;
	for (final Coord p : positions) {
	    if ((ie = this.positionMapping.get(p)) != null) {
		gotten.add(ie.element);
	    }
	}
	return gotten;
    }

    /**
     * Given an array of I, gets all elements in this SpatialMap that share an
     * identity with one of the I objects in identities and returns them as an
     * ArrayList of elements.
     *
     * @param identities an array of I
     * @return an ArrayList, possibly empty, of elements that share an Identity with
     *         an I in identities
     */
    public ArrayList<E> getManyIdentities(final I[] identities) {
	final ArrayList<E> gotten = new ArrayList<>(identities.length);
	SpatialTriple<I, E> ie;
	for (final I i : identities) {
	    if ((ie = this.itemMapping.get(i)) != null) {
		gotten.add(ie.element);
	    }
	}
	return gotten;
    }

    public E randomElement(final RNG rng) {
	if (this.itemMapping.isEmpty()) {
	    return null;
	}
	return this.itemMapping.randomValue(rng).element;
    }

    public Coord randomPosition(final RNG rng) {
	if (this.positionMapping.isEmpty()) {
	    return null;
	}
	return this.positionMapping.randomKey(rng);
    }

    public I randomIdentity(final RNG rng) {
	if (this.itemMapping.isEmpty()) {
	    return null;
	}
	return this.itemMapping.randomKey(rng);
    }

    public SpatialTriple<I, E> randomEntry(final RNG rng) {
	if (this.itemMapping.isEmpty()) {
	    return null;
	}
	return this.itemMapping.randomValue(rng);
    }

    /**
     * Given the size and position of a rectangular area, creates a new SpatialMap
     * from this one that refers only to the subsection of this SpatialMap shared
     * with the rectangular area. Will not include any elements from this SpatialMap
     * with positions beyond the bounds of the given rectangular area, and will
     * include all elements from this that are in the area.
     *
     * @param x      the minimum x-coordinate of the rectangular area
     * @param y      the minimum y-coordinate of the rectangular area
     * @param width  the total width of the rectangular area
     * @param height the total height of the rectangular area
     * @return a new SpatialMap that refers to a subsection of this one
     */
    public SpatialMap<I, E> rectangleSection(final int x, final int y, final int width, final int height) {
	final SpatialMap<I, E> next = new SpatialMap<>(this.positionMapping.size());
	Coord tmp;
	for (final SpatialTriple<I, E> ie : this.positionMapping.values()) {
	    tmp = ie.position;
	    if (tmp.x >= x && tmp.y >= y && tmp.x + width > x && tmp.y + height > y) {
		next.put(ie);
	    }
	}
	return next;
    }

    /**
     * Given the center position, Radius to determine measurement, and maximum
     * distance from the center, creates a new SpatialMap from this one that refers
     * only to the subsection of this SpatialMap shared with the area within the
     * given distance from the center as measured by measurement. Will not include
     * any elements from this SpatialMap with positions beyond the bounds of the
     * given area, and will include all elements from this that are in the area.
     *
     * @param x           the center x-coordinate of the area
     * @param y           the center y-coordinate of the area
     * @param measurement a Radius enum, such as Radius.CIRCLE or Radius.DIAMOND,
     *                    that calculates distance
     * @param distance    the maximum distance from the center to include in the
     *                    area
     * @return a new SpatialMap that refers to a subsection of this one
     */
    public SpatialMap<I, E> radiusSection(final int x, final int y, final Radius measurement, final int distance) {
	final SpatialMap<I, E> next = new SpatialMap<>(this.positionMapping.size());
	Coord tmp;
	for (final SpatialTriple<I, E> ie : this.positionMapping.values()) {
	    tmp = ie.position;
	    if (measurement.inRange(x, y, tmp.x, tmp.y, 0, distance)) {
		next.put(ie);
	    }
	}
	return next;
    }

    /**
     * Given the center position and maximum distance from the center, creates a new
     * SpatialMap from this one that refers only to the subsection of this
     * SpatialMap shared with the area within the given distance from the center,
     * measured with Euclidean distance to produce a circle shape. Will not include
     * any elements from this SpatialMap with positions beyond the bounds of the
     * given area, and will include all elements from this that are in the area.
     *
     * @param x      the center x-coordinate of the area
     * @param y      the center y-coordinate of the area
     * @param radius the maximum distance from the center to include in the area,
     *               using Euclidean distance
     * @return a new SpatialMap that refers to a subsection of this one
     */
    public SpatialMap<I, E> circleSection(final int x, final int y, final int radius) {
	return this.radiusSection(x, y, Radius.CIRCLE, radius);
    }

    public void clear() {
	this.itemMapping.clear();
	this.positionMapping.clear();
    }

    public boolean isEmpty() {
	return this.itemMapping.isEmpty();
    }

    public int size() {
	return this.itemMapping.size();
    }

    public Object[] toArray() {
	final Object[] contents = this.itemMapping.values().toArray();
	for (int i = 0; i < contents.length; i++) {
	    contents[i] = ((SpatialTriple<?, ?>) contents[i]).element;
	}
	return contents;
    }

    /**
     * Replaces the contents of the given array with the elements this holds, in
     * insertion order, until either this data structure or the array has been
     * exhausted.
     *
     * @param a the array to replace; should usually have the same length as this
     *          data structure's size.
     * @return an array of elements that should be the same as the changed array
     *         originally passed as a parameter.
     */
    public E[] toArray(final E[] a) {
	final Collection<SpatialTriple<I, E>> contents = this.itemMapping.values();
	int i = 0;
	for (final SpatialTriple<I, E> triple : contents) {
	    if (i < a.length) {
		a[i] = triple.element;
	    } else {
		break;
	    }
	    i++;
	}
	return a;
    }

    /**
     * Iterates through values in insertion order.
     *
     * @return an Iterator of generic type E
     */
    @Override
    public Iterator<E> iterator() {
	final Iterator<SpatialTriple<I, E>> it = this.itemMapping.values().iterator();
	return new Iterator<>() {
	    @Override
	    public boolean hasNext() {
		return it.hasNext();
	    }

	    @Override
	    public E next() {
		final SpatialTriple<I, E> triple = it.next();
		if (triple != null) {
		    return triple.element;
		}
		return null;
	    }

	    @Override
	    public void remove() {
		throw new UnsupportedOperationException();
	    }
	};
    }

    /**
     * Iterates through values similar to Map.Entry, but containing a Coord, I, and
     * E value for each entry, in insertion order. The type is SpatialTriple,
     * defined in a nested class.
     *
     * @return an Iterator of SpatialTriple of I, E
     */
    public Iterator<SpatialTriple<I, E>> tripleIterator() {
	return this.itemMapping.values().iterator();
    }

    /**
     * Iterates through positions in insertion order; has less predictable iteration
     * order than the other iterators.
     *
     * @return an Iterator of Coord
     */
    public Iterator<Coord> positionIterator() {
	return this.positionMapping.keySet().iterator();
    }

    /**
     * Iterates through identity keys in insertion order.
     *
     * @return an Iterator of generic type I
     */
    public Iterator<I> identityIterator() {
	return this.itemMapping.keySet().iterator();
    }

    /**
     * Iterates through positions in a rectangular region (starting at a minimum of
     * x, y and extending to the specified width and height) in left-to-right, then
     * top-to-bottom order (the same as reading a page of text). Any Coords this
     * returns should be viable arguments to get() if you want a corresponding
     * element.
     *
     * @return an Iterator of Coord
     */
    public Iterator<Coord> rectanglePositionIterator(final int x, final int y, final int width, final int height) {
	return new RectangularIterator(x, y, width, height);
    }

    /**
     * Iterates through positions in a region defined by a Radius (starting at a
     * minimum of x - distance, y - distance and extending to x + distance, y +
     * distance but skipping any positions where the Radius considers a position
     * further from x, y than distance) in left-to-right, then top-to-bottom order
     * (the same as reading a page of text). You can use Radius.SQUARE to make a
     * square region (which could also be made with rectanglePositionIterator()),
     * Radius.DIAMOND to make a, well, diamond-shaped region, or Radius.CIRCLE to
     * make a circle (which could also be made with circlePositionIterator). Any
     * Coords this returns should be viable arguments to get() if you want a
     * corresponding element.
     *
     * @return an Iterator of Coord
     */
    public Iterator<Coord> radiusPositionIterator(final int x, final int y, final Radius measurement,
	    final int distance) {
	return new RadiusIterator(x, y, measurement, distance);
    }

    /**
     * Iterates through positions in a circular region (starting at a minimum of x -
     * distance, y - distance and extending to x + distance, y + distance but
     * skipping any positions where the Euclidean distance from x,y to the position
     * is more than distance) in left-to-right, then top-to-bottom order (the same
     * as reading a page of text). Any Coords this returns should be viable
     * arguments to get() if you want a corresponding element.
     *
     * @return an Iterator of Coord
     */
    public Iterator<Coord> circlePositionIterator(final int x, final int y, final int distance) {
	return new RadiusIterator(x, y, Radius.CIRCLE, distance);
    }

    private class RectangularIterator implements Iterator<Coord> {
	int x, y, width, height, idx, poolWidth = Coord.getCacheWidth(), poolHeight = Coord.getCacheHeight();
	Set<Coord> positions;
	Coord temp;

	RectangularIterator(final int x, final int y, final int width, final int height) {
	    this.x = x;
	    this.y = y;
	    this.width = width;
	    this.height = height;
	    this.idx = -1;
	    this.positions = SpatialMap.this.positionMapping.keySet();
	}

	@Override
	public boolean hasNext() {
	    if (this.idx < this.width * this.height - 1) {
		Coord t2;
		int n = this.idx;
		do {
		    n = this.findNext(n);
		    if (this.idx < 0) {
			return n >= 0;
		    } else {
			if (this.x + n % this.width >= 0 && this.x + n % this.width < this.poolWidth
				&& this.y + n / this.width >= 0 && this.y + n / this.width < this.poolHeight) {
			    t2 = Coord.get(this.x + n % this.width, this.y + n / this.width);
			} else {
			    t2 = Coord.get(-1, -1);
			}
		    }
		} while (!this.positions.contains(t2));
		/* Not done && has next */
		return n >= 0;
	    }
	    return false;
	}

	@Override
	public Coord next() {
	    do {
		this.idx = this.findNext(this.idx);
		if (this.idx < 0) {
		    throw new NoSuchElementException();
		}
		if (this.x + this.idx % this.width >= 0 && this.x + this.idx % this.width < this.poolWidth
			&& this.y + this.idx / this.width >= 0 && this.y + this.idx / this.width < this.poolHeight) {
		    this.temp = Coord.get(this.x + this.idx % this.width, this.y + this.idx / this.width);
		} else {
		    this.temp = Coord.get(-1, -1);
		}
	    } while (!this.positions.contains(this.temp));
	    return this.temp;
	}

	@Override
	public void remove() {
	    throw new UnsupportedOperationException();
	}

	private int findNext(final int idx) {
	    if (idx < 0) {
		/* First iteration */
		return 0;
	    } else {
		if (idx >= this.width * this.height - 1) {
		    /* Done iterating */
		    return -1;
		} else {
		    return idx + 1;
		}
	    }
	}
    }

    private class RadiusIterator implements Iterator<Coord> {
	int x, y, width, height, distance, idx, poolWidth = Coord.getCacheWidth(), poolHeight = Coord.getCacheHeight();
	Set<Coord> positions;
	Coord temp;
	Radius measurement;

	RadiusIterator(final int x, final int y, final Radius measurement, final int distance) {
	    this.x = x;
	    this.y = y;
	    this.width = 1 + distance * 2;
	    this.height = 1 + distance * 2;
	    this.distance = distance;
	    this.measurement = measurement;
	    this.idx = -1;
	    this.positions = SpatialMap.this.positionMapping.keySet();
	}

	@Override
	public boolean hasNext() {
	    if (this.idx < this.width * this.height - 1) {
		Coord t2;
		int n = this.idx;
		do {
		    n = this.findNext(n);
		    if (this.idx < 0) {
			return n >= 0;
		    } else {
			if (this.x - this.distance + n % this.width >= 0
				&& this.x - this.distance + n % this.width < this.poolWidth
				&& this.y - this.distance + n / this.width >= 0
				&& this.y - this.distance + n / this.width < this.poolHeight
				&& this.measurement.radius(this.x, this.y, this.x - this.distance + n % this.width,
					this.y - this.distance + n / this.width) <= this.distance) {
			    t2 = Coord.get(this.x - this.distance + n % this.width,
				    this.y - this.distance + n / this.width);
			} else {
			    t2 = Coord.get(-1, -1);
			}
		    }
		} while (!this.positions.contains(t2));
		/* Not done && has next */
		return n >= 0;
	    }
	    return false;
	}

	@Override
	public Coord next() {
	    do {
		this.idx = this.findNext(this.idx);
		if (this.idx < 0) {
		    throw new NoSuchElementException();
		}
		if (this.x - this.distance + this.idx % this.width >= 0
			&& this.x - this.distance + this.idx % this.width < this.poolWidth
			&& this.y - this.distance + this.idx / this.width >= 0
			&& this.y - this.distance + this.idx / this.width < this.poolHeight
			&& this.measurement.radius(this.x, this.y, this.x - this.distance + this.idx % this.width,
				this.y - this.distance + this.idx / this.width) <= this.distance) {
		    this.temp = Coord.get(this.x - this.distance + this.idx % this.width,
			    this.y - this.distance + this.idx / this.width);
		} else {
		    this.temp = Coord.get(-1, -1);
		}
	    } while (!this.positions.contains(this.temp));
	    return this.temp;
	}

	@Override
	public void remove() {
	    throw new UnsupportedOperationException();
	}

	private int findNext(final int idx) {
	    if (idx < 0) {
		/* First iteration */
		return 0;
	    } else {
		if (idx >= this.width * this.height - 1) {
		    /* Done iterating */
		    return -1;
		} else {
		    return idx + 1;
		}
	    }
	}
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final SpatialMap<?, ?> that = (SpatialMap<?, ?>) o;
	if (this.itemMapping != null ? !this.itemMapping.equals(that.itemMapping) : that.itemMapping != null) {
	    return false;
	}
	return this.positionMapping != null ? this.positionMapping.equals(that.positionMapping)
		: that.positionMapping == null;
    }

    @Override
    public int hashCode() {
	int result = this.itemMapping != null ? this.itemMapping.hashCode() : 0;
	result = 31 * result + (this.positionMapping != null ? this.positionMapping.hashCode() : 0);
	return result;
    }

    @Override
    public String toString() {
	return "SpatialMap{" + "itemMapping=" + this.itemMapping + ", positionMapping=" + this.positionMapping + '}';
    }
}
