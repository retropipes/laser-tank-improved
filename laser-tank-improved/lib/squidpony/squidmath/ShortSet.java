/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package squidpony.squidmath;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * An unordered set that uses short keys. This implementation uses cuckoo
 * hashing using 3 hashes, random walking, and a small stash for problematic
 * keys. No allocation is done except when growing the table size. Used
 * internally by CoordPacker, and unlikely to be used outside of it. <br>
 * This set performs very fast contains and remove (typically O(1), worst case
 * O(log(n))). Add may be a bit slower, depending on hash collisions. Load
 * factors greater than 0.91 greatly increase the chances the set will have to
 * rehash to the next higher POT size.
 *
 * @author Nathan Sweet Ported from libGDX by Tommy Ettinger on 10/19/2015.
 */
public class ShortSet implements Serializable {
    private static final long serialVersionUID = -4390851800502156007L;
    private static final int PRIME2 = 0xb4b82e39;
    private static final int PRIME3 = 0xced1c241;
    private static final short EMPTY = 0;
    public int size;
    short[] keyTable;
    int capacity, stashSize;
    boolean hasZeroValue;
    private final float loadFactor;
    private int hashShift, threshold;
    private int stashCapacity;
    private int pushIterations;
    private int mask;
    private static LightRNG rng;
    private ShortSetIterator iterator1, iterator2;

    /**
     * Creates a new sets with an initial capacity of 32 and a load factor of 0.8.
     * This set will hold 25 items before growing the backing table.
     */
    public ShortSet() {
	this(32, 0.8f);
    }

    /**
     * Creates a new set with a load factor of 0.8. This set will hold
     * initialCapacity * 0.8 items before growing the backing table.
     */
    public ShortSet(final int initialCapacity) {
	this(initialCapacity, 0.8f);
    }

    /**
     * Creates a new set with the specified initial capacity and load factor. This
     * set will hold initialCapacity * loadFactor items before growing the backing
     * table.
     */
    public ShortSet(final int initialCapacity, final float loadFactor) {
	if (initialCapacity < 0) {
	    throw new IllegalArgumentException("initialCapacity must be >= 0: " + initialCapacity);
	}
	if (initialCapacity > 1 << 30) {
	    throw new IllegalArgumentException("initialCapacity is too large: " + initialCapacity);
	}
	this.capacity = ShortSet.nextPowerOfTwo(initialCapacity);
	ShortSet.rng = new LightRNG();
	if (loadFactor <= 0) {
	    throw new IllegalArgumentException("loadFactor must be > 0: " + loadFactor);
	}
	this.loadFactor = loadFactor;
	this.threshold = (int) (this.capacity * loadFactor);
	this.mask = this.capacity - 1;
	this.hashShift = 31 - Integer.numberOfTrailingZeros(this.capacity);
	this.stashCapacity = Math.max(3, (int) Math.ceil(Math.log(this.capacity)) * 2);
	this.pushIterations = Math.max(Math.min(this.capacity, 8), (int) Math.sqrt(this.capacity) / 8);
	this.keyTable = new short[this.capacity + this.stashCapacity];
    }

    /** Creates a new map identical to the specified map. */
    public ShortSet(final ShortSet map) {
	this(map.capacity, map.loadFactor);
	this.stashSize = map.stashSize;
	System.arraycopy(map.keyTable, 0, this.keyTable, 0, map.keyTable.length);
	this.size = map.size;
	this.hasZeroValue = map.hasZeroValue;
    }

    /** Returns true if the key was not already in the set. */
    public boolean add(final short key) {
	if (key == 0) {
	    if (this.hasZeroValue) {
		return false;
	    }
	    this.hasZeroValue = true;
	    this.size++;
	    return true;
	}
	final short[] keyTable = this.keyTable;
	// Check for existing keys.
	final int index1 = key & this.mask;
	final short key1 = keyTable[index1];
	if (key1 == key) {
	    return false;
	}
	final int index2 = this.hash2(key);
	final short key2 = keyTable[index2];
	if (key2 == key) {
	    return false;
	}
	final int index3 = this.hash3(key);
	final short key3 = keyTable[index3];
	if (key3 == key) {
	    return false;
	}
	// Find key in the stash.
	for (int i = this.capacity, n = i + this.stashSize; i < n; i++) {
	    if (keyTable[i] == key) {
		return false;
	    }
	}
	// Check for empty buckets.
	if (key1 == ShortSet.EMPTY) {
	    keyTable[index1] = key;
	    if (this.size++ >= this.threshold) {
		this.resize(this.capacity << 1);
	    }
	    return true;
	}
	if (key2 == ShortSet.EMPTY) {
	    keyTable[index2] = key;
	    if (this.size++ >= this.threshold) {
		this.resize(this.capacity << 1);
	    }
	    return true;
	}
	if (key3 == ShortSet.EMPTY) {
	    keyTable[index3] = key;
	    if (this.size++ >= this.threshold) {
		this.resize(this.capacity << 1);
	    }
	    return true;
	}
	this.push(key, index1, key1, index2, key2, index3, key3);
	return true;
    }

    public void addAll(final ShortVLA array) {
	this.addAll(array, 0, array.size);
    }

    public void addAll(final ShortVLA array, final int offset, final int length) {
	if (offset + length > array.size) {
	    throw new IllegalArgumentException(
		    "offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
	}
	this.addAll(array.items, offset, length);
    }

    public void addAll(final short... array) {
	this.addAll(array, 0, array.length);
    }

    public void addAll(final short[] array, final int offset, final int length) {
	this.ensureCapacity(length);
	for (int i = offset, n = i + length; i < n; i++) {
	    this.add(array[i]);
	}
    }

    public void addAll(final ShortSet set) {
	this.ensureCapacity(set.size);
	final ShortSetIterator iterator = set.iterator();
	while (iterator.hasNext) {
	    this.add(iterator.next());
	}
    }

    /** Skips checks for existing keys. */
    private void addResize(final short key) {
	if (key == 0) {
	    this.hasZeroValue = true;
	    return;
	}
	// Check for empty buckets.
	final int index1 = key & this.mask;
	final short key1 = this.keyTable[index1];
	if (key1 == ShortSet.EMPTY) {
	    this.keyTable[index1] = key;
	    if (this.size++ >= this.threshold) {
		this.resize(this.capacity << 1);
	    }
	    return;
	}
	final int index2 = this.hash2(key);
	final short key2 = this.keyTable[index2];
	if (key2 == ShortSet.EMPTY) {
	    this.keyTable[index2] = key;
	    if (this.size++ >= this.threshold) {
		this.resize(this.capacity << 1);
	    }
	    return;
	}
	final int index3 = this.hash3(key);
	final short key3 = this.keyTable[index3];
	if (key3 == ShortSet.EMPTY) {
	    this.keyTable[index3] = key;
	    if (this.size++ >= this.threshold) {
		this.resize(this.capacity << 1);
	    }
	    return;
	}
	this.push(key, index1, key1, index2, key2, index3, key3);
    }

    private void push(short insertKey, int index1, short key1, int index2, short key2, int index3, short key3) {
	final short[] keyTable = this.keyTable;
	final int mask = this.mask;
	// Push keys until an empty bucket is found.
	short evictedKey;
	int i = 0;
	final int pushIterations = this.pushIterations;
	do {
	    // Replace the key and value for one of the hashes.
	    switch (ShortSet.rng.nextInt(2)) {
	    case 0:
		evictedKey = key1;
		keyTable[index1] = insertKey;
		break;
	    case 1:
		evictedKey = key2;
		keyTable[index2] = insertKey;
		break;
	    default:
		evictedKey = key3;
		keyTable[index3] = insertKey;
		break;
	    }
	    // If the evicted key hashes to an empty bucket, put it there and stop.
	    index1 = evictedKey & mask;
	    key1 = keyTable[index1];
	    if (key1 == ShortSet.EMPTY) {
		keyTable[index1] = evictedKey;
		if (this.size++ >= this.threshold) {
		    this.resize(this.capacity << 1);
		}
		return;
	    }
	    index2 = this.hash2(evictedKey);
	    key2 = keyTable[index2];
	    if (key2 == ShortSet.EMPTY) {
		keyTable[index2] = evictedKey;
		if (this.size++ >= this.threshold) {
		    this.resize(this.capacity << 1);
		}
		return;
	    }
	    index3 = this.hash3(evictedKey);
	    key3 = keyTable[index3];
	    if (key3 == ShortSet.EMPTY) {
		keyTable[index3] = evictedKey;
		if (this.size++ >= this.threshold) {
		    this.resize(this.capacity << 1);
		}
		return;
	    }
	    if (++i == pushIterations) {
		break;
	    }
	    insertKey = evictedKey;
	} while (true);
	this.addStash(evictedKey);
    }

    private void addStash(final short key) {
	if (this.stashSize == this.stashCapacity) {
	    // Too many pushes occurred and the stash is full, increase the table size.
	    this.resize(this.capacity << 1);
	    this.add(key);
	    return;
	}
	// Store key in the stash.
	final int index = this.capacity + this.stashSize;
	this.keyTable[index] = key;
	this.stashSize++;
	this.size++;
    }

    /** Returns true if the key was removed. */
    public boolean remove(final short key) {
	if (key == 0) {
	    if (!this.hasZeroValue) {
		return false;
	    }
	    this.hasZeroValue = false;
	    this.size--;
	    return true;
	}
	int index = key & this.mask;
	if (this.keyTable[index] == key) {
	    this.keyTable[index] = ShortSet.EMPTY;
	    this.size--;
	    return true;
	}
	index = this.hash2(key);
	if (this.keyTable[index] == key) {
	    this.keyTable[index] = ShortSet.EMPTY;
	    this.size--;
	    return true;
	}
	index = this.hash3(key);
	if (this.keyTable[index] == key) {
	    this.keyTable[index] = ShortSet.EMPTY;
	    this.size--;
	    return true;
	}
	return this.removeStash(key);
    }

    boolean removeStash(final short key) {
	final short[] keyTable = this.keyTable;
	for (int i = this.capacity, n = i + this.stashSize; i < n; i++) {
	    if (keyTable[i] == key) {
		this.removeStashIndex(i);
		this.size--;
		return true;
	    }
	}
	return false;
    }

    void removeStashIndex(final int index) {
	// If the removed location was not last, move the last tuple to the removed
	// location.
	this.stashSize--;
	final int lastIndex = this.capacity + this.stashSize;
	if (index < lastIndex) {
	    this.keyTable[index] = this.keyTable[lastIndex];
	}
    }

    /**
     * Reduces the size of the backing arrays to be the specified capacity or less.
     * If the capacity is already less, nothing is done. If the set contains more
     * items than the specified capacity, the next highest power of two capacity is
     * used instead.
     */
    public void shrink(int maximumCapacity) {
	if (maximumCapacity < 0) {
	    throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity);
	}
	if (this.size > maximumCapacity) {
	    maximumCapacity = this.size;
	}
	if (this.capacity <= maximumCapacity) {
	    return;
	}
	maximumCapacity = ShortSet.nextPowerOfTwo(maximumCapacity);
	this.resize(maximumCapacity);
    }

    /**
     * Clears the map and reduces the size of the backing arrays to be the specified
     * capacity if they are larger.
     */
    public void clear(final int maximumCapacity) {
	if (this.capacity <= maximumCapacity) {
	    this.clear();
	    return;
	}
	this.hasZeroValue = false;
	this.size = 0;
	this.resize(maximumCapacity);
    }

    public void clear() {
	if (this.size == 0) {
	    return;
	}
	final short[] keyTable = this.keyTable;
	for (int i = this.capacity + this.stashSize; i-- > 0;) {
	    keyTable[i] = ShortSet.EMPTY;
	}
	this.size = 0;
	this.stashSize = 0;
	this.hasZeroValue = false;
    }

    public boolean contains(final short key) {
	if (key == 0) {
	    return this.hasZeroValue;
	}
	int index = key & this.mask;
	if (this.keyTable[index] != key) {
	    index = this.hash2(key);
	    if (this.keyTable[index] != key) {
		index = this.hash3(key);
		if (this.keyTable[index] != key) {
		    return this.containsKeyStash(key);
		}
	    }
	}
	return true;
    }

    private boolean containsKeyStash(final short key) {
	final short[] keyTable = this.keyTable;
	for (int i = this.capacity, n = i + this.stashSize; i < n; i++) {
	    if (keyTable[i] == key) {
		return true;
	    }
	}
	return false;
    }

    public int first() {
	if (this.hasZeroValue) {
	    return 0;
	}
	final short[] keyTable = this.keyTable;
	for (int i = 0, n = this.capacity + this.stashSize; i < n; i++) {
	    if (keyTable[i] != ShortSet.EMPTY) {
		return keyTable[i];
	    }
	}
	throw new IllegalStateException("IntSet is empty.");
    }

    /**
     * Increases the size of the backing array to accommodate the specified number
     * of additional items. Useful before adding many items to avoid multiple
     * backing array resizes.
     */
    public void ensureCapacity(final int additionalCapacity) {
	final int sizeNeeded = this.size + additionalCapacity;
	if (sizeNeeded >= this.threshold) {
	    this.resize(ShortSet.nextPowerOfTwo((int) (sizeNeeded / this.loadFactor)));
	}
    }

    private void resize(final int newSize) {
	final int oldEndIndex = this.capacity + this.stashSize;
	this.capacity = newSize;
	this.threshold = (int) (newSize * this.loadFactor);
	this.mask = newSize - 1;
	this.hashShift = 31 - Integer.numberOfTrailingZeros(newSize);
	this.stashCapacity = Math.max(3, (int) Math.ceil(Math.log(newSize)) * 2);
	this.pushIterations = Math.max(Math.min(newSize, 8), (int) Math.sqrt(newSize) / 8);
	final short[] oldKeyTable = this.keyTable;
	this.keyTable = new short[newSize + this.stashCapacity];
	final int oldSize = this.size;
	this.size = this.hasZeroValue ? 1 : 0;
	this.stashSize = 0;
	if (oldSize > 0) {
	    for (int i = 0; i < oldEndIndex; i++) {
		final short key = oldKeyTable[i];
		if (key != ShortSet.EMPTY) {
		    this.addResize(key);
		}
	    }
	}
    }

    private int hash2(int h) {
	h *= ShortSet.PRIME2;
	return (h ^ h >>> this.hashShift) & this.mask;
    }

    private int hash3(int h) {
	h *= ShortSet.PRIME3;
	return (h ^ h >>> this.hashShift) & this.mask;
    }

    @Override
    public int hashCode() {
	int h = 0;
	for (int i = 0, n = this.capacity + this.stashSize; i < n; i++) {
	    if (this.keyTable[i] != ShortSet.EMPTY) {
		h += this.keyTable[i];
	    }
	}
	return h;
    }

    @Override
    public boolean equals(final Object obj) {
	if (!(obj instanceof ShortSet)) {
	    return false;
	}
	final ShortSet other = (ShortSet) obj;
	if (other.size != this.size) {
	    return false;
	}
	if (other.hasZeroValue != this.hasZeroValue) {
	    return false;
	}
	for (int i = 0, n = this.capacity + this.stashSize; i < n; i++) {
	    if (this.keyTable[i] != ShortSet.EMPTY && !other.contains(this.keyTable[i])) {
		return false;
	    }
	}
	return true;
    }

    @Override
    public String toString() {
	if (this.size == 0) {
	    return "[]";
	}
	final StringBuilder buffer = new StringBuilder(32);
	buffer.append('[');
	final short[] keyTable = this.keyTable;
	int i = keyTable.length;
	if (this.hasZeroValue) {
	    buffer.append("0");
	} else {
	    while (i-- > 0) {
		final int key = keyTable[i];
		if (key == ShortSet.EMPTY) {
		    continue;
		}
		buffer.append(key);
		break;
	    }
	}
	while (i-- > 0) {
	    final int key = keyTable[i];
	    if (key == ShortSet.EMPTY) {
		continue;
	    }
	    buffer.append(", ");
	    buffer.append(key);
	}
	buffer.append(']');
	return buffer.toString();
    }

    private static int nextPowerOfTwo(final int n) {
	final int highest = Integer.highestOneBit(n);
	return highest == Integer.lowestOneBit(n) ? highest : highest << 1;
    }

    /**
     * Returns an iterator for the keys in the set. Remove is supported. Note that
     * the same iterator instance is returned each time this method is called. Use
     * the {@link ShortSetIterator} constructor for nested or multithreaded
     * iteration.
     */
    public ShortSetIterator iterator() {
	if (this.iterator1 == null) {
	    this.iterator1 = new ShortSetIterator(this);
	    this.iterator2 = new ShortSetIterator(this);
	}
	if (!this.iterator1.valid) {
	    this.iterator1.reset();
	    this.iterator1.valid = true;
	    this.iterator2.valid = false;
	    return this.iterator1;
	}
	this.iterator2.reset();
	this.iterator2.valid = true;
	this.iterator1.valid = false;
	return this.iterator2;
    }

    public static ShortSet with(final short... array) {
	final ShortSet set = new ShortSet();
	set.addAll(array);
	return set;
    }

    public static class ShortSetIterator {
	static final int INDEX_ILLEGAL = -2;
	static final int INDEX_ZERO = -1;
	public boolean hasNext;
	final ShortSet set;
	int nextIndex, currentIndex;
	boolean valid = true;

	public ShortSetIterator(final ShortSet set) {
	    this.set = set;
	    this.reset();
	}

	public void reset() {
	    this.currentIndex = ShortSetIterator.INDEX_ILLEGAL;
	    this.nextIndex = ShortSetIterator.INDEX_ZERO;
	    if (this.set.hasZeroValue) {
		this.hasNext = true;
	    } else {
		this.findNextIndex();
	    }
	}

	void findNextIndex() {
	    this.hasNext = false;
	    final short[] keyTable = this.set.keyTable;
	    for (final int n = this.set.capacity + this.set.stashSize; ++this.nextIndex < n;) {
		if (keyTable[this.nextIndex] != ShortSet.EMPTY) {
		    this.hasNext = true;
		    break;
		}
	    }
	}

	public void remove() {
	    if (this.currentIndex == ShortSetIterator.INDEX_ZERO && this.set.hasZeroValue) {
		this.set.hasZeroValue = false;
	    } else if (this.currentIndex < 0) {
		throw new IllegalStateException("next must be called before remove.");
	    } else if (this.currentIndex >= this.set.capacity) {
		this.set.removeStashIndex(this.currentIndex);
		this.nextIndex = this.currentIndex - 1;
		this.findNextIndex();
	    } else {
		this.set.keyTable[this.currentIndex] = ShortSet.EMPTY;
	    }
	    this.currentIndex = ShortSetIterator.INDEX_ILLEGAL;
	    this.set.size--;
	}

	public short next() {
	    if (!this.hasNext) {
		throw new NoSuchElementException();
	    }
	    if (!this.valid) {
		throw new RuntimeException("ShortSetIterator cannot be used nested.");
	    }
	    final short key = this.nextIndex == ShortSetIterator.INDEX_ZERO ? 0 : this.set.keyTable[this.nextIndex];
	    this.currentIndex = this.nextIndex;
	    this.findNextIndex();
	    return key;
	}

	/** Returns a new array containing the remaining keys. */
	public ShortVLA toArray() {
	    final ShortVLA array = new ShortVLA(true, this.set.size);
	    while (this.hasNext) {
		array.add(this.next());
	    }
	    return array;
	}
    }
}