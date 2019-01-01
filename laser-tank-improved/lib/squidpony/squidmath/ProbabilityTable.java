package squidpony.squidmath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.SortedSet;

import squidpony.annotation.Beta;

/**
 * A generic method of holding a probability table to determine weighted random
 * outcomes.
 *
 * The weights do not need to add up to any particular value, they will be
 * normalized when choosing a random entry.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 *
 * @param <T> The type of object to be held in the table
 */
@Beta
public class ProbabilityTable<T> implements Serializable {
    private static final long serialVersionUID = -1307656083434154736L;
    /**
     * The set of items that can be produced directly from {@link #random()}
     * (without additional lookups).
     */
    public final Arrangement<T> table;
    /**
     * The list of items that can be produced indirectly from {@link #random()}
     * (looking up values from inside the nested tables).
     */
    public final ArrayList<ProbabilityTable<T>> extraTable;
    public final IntVLA weights;
    public RNG rng;
    protected int total, normalTotal;

    /**
     * Creates a new probability table with a random seed.
     */
    public ProbabilityTable() {
	this(new StatefulRNG());
    }

    /**
     * Creates a new probability table with the provided source of randomness used.
     * Gets one random long from rng to use as an internal identifier.
     *
     * @param rng the source of randomness
     */
    public ProbabilityTable(final RNG rng) {
	this.rng = rng;
	this.table = new Arrangement<>(64, 0.75f);
	this.extraTable = new ArrayList<>(16);
	this.weights = new IntVLA(64);
	this.total = 0;
	this.normalTotal = 0;
    }

    /**
     * Creates a new probability table with the provided long seed used.
     *
     * @param seed the RNG seed as a long
     */
    public ProbabilityTable(final long seed) {
	this.rng = new StatefulRNG(seed);
	this.table = new Arrangement<>(64, 0.75f);
	this.extraTable = new ArrayList<>(16);
	this.weights = new IntVLA(64);
	this.total = 0;
	this.normalTotal = 0;
    }

    /**
     * Creates a new probability table with the provided String seed used.
     *
     * @param seed the RNG seed as a String
     */
    public ProbabilityTable(final String seed) {
	this(CrossHash.Wisp.hash64(seed));
    }

    /**
     * Returns an object randomly based on assigned weights.
     *
     * Returns null if no elements have been put in the table.
     *
     * @return the chosen object or null
     */
    public T random() {
	if (this.table.isEmpty()) {
	    return null;
	}
	int index = this.rng.nextInt(this.total);
	final int sz = this.table.size();
	for (int i = 0; i < sz; i++) {
	    index -= this.weights.get(i);
	    if (index < 0) {
		return this.table.keyAt(i);
	    }
	}
	for (int i = 0; i < this.extraTable.size(); i++) {
	    index -= this.weights.get(sz + i);
	    if (index < 0) {
		return this.extraTable.get(i).random();
	    }
	}
	return null;// something went wrong, shouldn't have been able to get all the way through
		    // without finding an item
    }

    /**
     * Adds the given item to the table.
     *
     * Weight must be greater than 0.
     *
     * @param item   the object to be added
     * @param weight the weight to be given to the added object
     * @return this for chaining
     */
    public ProbabilityTable<T> add(final T item, final int weight) {
	if (weight <= 0) {
	    return this;
	}
	final int i = this.table.getInt(item);
	if (i < 0) {
	    this.weights.insert(this.table.size, Math.max(0, weight));
	    this.table.add(item);
	    final int w = Math.max(0, weight);
	    this.total += w;
	    this.normalTotal += w;
	} else {
	    final int i2 = this.weights.get(i);
	    final int w = Math.max(0, i2 + weight);
	    this.weights.set(i, w);
	    this.total += w - i2;
	    this.normalTotal += w - i2;
	}
	return this;
    }

    /**
     * Adds the given probability table as a possible set of results for this table.
     * The table parameter should not be the same object as this ProbabilityTable,
     * nor should it contain cycles that could reference this object from inside the
     * values of table. This could cause serious issues that would eventually
     * terminate in a StackOverflowError if the cycles randomly repeated for too
     * long. Only the first case is checked for (if the contents of this and table
     * are equivalent, it returns without doing anything; this also happens if table
     * is empty or null).
     *
     * Weight must be greater than 0.
     *
     * @param table  the ProbabilityTable to be added; should not be the same as
     *               this object (avoid cycles)
     * @param weight the weight to be given to the added table
     * @return this for chaining
     */
    public ProbabilityTable<T> add(final ProbabilityTable<T> table, final int weight) {
	if (weight <= 0 || table == null || this.contentEquals(table) || table.total <= 0) {
	    return this;
	}
	this.weights.add(Math.max(0, weight));
	this.extraTable.add(table);
	this.total += Math.max(0, weight);
	return this;
    }

    /**
     * Returns the weight of the item if the item is in the table. Returns zero if
     * the item is not in the table.
     *
     * @param item the item searched for
     * @return the weight of the item, or zero
     */
    public int weight(final T item) {
	final int i = this.table.getInt(item);
	return i < 0 ? 0 : this.weights.get(i);
    }

    /**
     * Returns the weight of the extra table if present. Returns zero if the extra
     * table is not present.
     *
     * @param item the extra ProbabilityTable to search for
     * @return the weight of the ProbabilityTable, or zero
     */
    public int weight(final ProbabilityTable<T> item) {
	final int i = this.extraTable.indexOf(item);
	return i < 0 ? 0 : this.weights.get(i + this.table.size());
    }

    /**
     * Provides a set of the items in this table, without reference to their weight.
     * Includes nested ProbabilityTable values, but as is the case throughout this
     * class, cyclical references to ProbabilityTable values that reference this
     * table will result in significant issues (such as a {@link StackOverflowError}
     * crashing your program).
     *
     * @return an OrderedSet of all items stored; iteration order should be
     *         predictable
     */
    public OrderedSet<T> items() {
	final OrderedSet<T> os = this.table.keysAsOrderedSet();
	for (int i = 0; i < this.extraTable.size(); i++) {
	    os.addAll(this.extraTable.get(i).items());
	}
	return os;
    }

    /**
     * Provides a set of the items in this table that are not in nested tables,
     * without reference to their weight. These are the items that are simple to
     * access, hence the name. If you want the items that are in both the top-level
     * and nested tables, you can use {@link #items()}.
     *
     * @return a predictably-ordered set of the items in the top-level table
     */
    public SortedSet<T> simpleItems() {
	return this.table.keySet();
    }

    /**
     * Provides a set of the nested ProbabilityTable values in this table, without
     * reference to their weight. Does not include normal values (non-table); for
     * that, use items().
     *
     * @return a "sorted" set of all nested tables stored, really sorted in
     *         insertion order
     */
    public ArrayList<ProbabilityTable<T>> tables() {
	return this.extraTable;
    }

    /**
     * Sets the current RNG to the given RNG. You may prefer using a StatefulRNG
     * (typically passing one in the constructor, but you can pass one here too) and
     * setting its state in other code, which does not require calling this method
     * again when the StatefulRNG has its state set.
     *
     * @param random an RNG, typically with a seed you want control over; may be a
     *               StatefulRNG or some other subclass
     */
    public void setRandom(final RNG random) {
	if (random != null) {
	    this.rng = random;
	}
    }

    /**
     * Gets the RNG this uses.
     *
     * @return the RNG used by this class, which is often (but not always) a
     *         StatefulRNG
     */
    public RNG getRandom() {
	return this.rng;
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final ProbabilityTable<?> that = (ProbabilityTable<?>) o;
	if (!this.table.equals(that.table)) {
	    return false;
	}
	if (!this.extraTable.equals(that.extraTable)) {
	    return false;
	}
	if (!this.weights.equals(that.weights)) {
	    return false;
	}
	return this.rng != null ? this.rng.equals(that.rng) : that.rng == null;
    }

    public boolean contentEquals(final ProbabilityTable<T> o) {
	if (this == o) {
	    return true;
	}
	if (o == null) {
	    return false;
	}
	if (!this.table.equals(o.table)) {
	    return false;
	}
	if (!this.extraTable.equals(o.extraTable)) {
	    return false;
	}
	return this.weights.equals(o.weights);
    }

    @Override
    public int hashCode() {
	int result = this.table.hashCode();
	result = 31 * result + this.extraTable.hashCode();
	result = 31 * result + this.weights.hashWisp();
	result = 31 * result + (this.rng != null ? this.rng.hashCode() : 0);
	return result;
    }
}
