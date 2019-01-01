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
import java.util.Arrays;

import squidpony.StringKit;
import squidpony.annotation.GwtIncompatible;

/**
 * A resizable, ordered or unordered variable-length int array. Avoids boxing
 * that occurs with ArrayList of Integer. If unordered, this class avoids a
 * memory copy when removing elements (the last element is moved to the removed
 * element's position). <br>
 * Was called IntArray in libGDX; to avoid confusion with the fixed-length
 * primitive array type, VLA (variable-length array) was chosen as a different
 * name. Copied from LibGDX by Tommy Ettinger on 10/1/2015.
 *
 * @author Nathan Sweet
 */
public class IntVLA implements Serializable, Cloneable {
    private static final long serialVersionUID = -2948161891082748626L;
    public int[] items;
    public int size;

    /** Creates an ordered array with a capacity of 16. */
    public IntVLA() {
	this(16);
    }

    /** Creates an ordered array with the specified capacity. */
    public IntVLA(final int capacity) {
	this.items = new int[capacity];
    }

    /**
     * Creates a new array containing the elements in the specific array. The new
     * array will be ordered if the specific array is ordered. The capacity is set
     * to the number of elements, so any subsequent elements added will cause the
     * backing array to be grown.
     */
    public IntVLA(final IntVLA array) {
	this.size = array.size;
	this.items = new int[this.size];
	System.arraycopy(array.items, 0, this.items, 0, this.size);
    }

    /**
     * Creates a new ordered array containing the elements in the specified array.
     * The capacity is set to the number of elements, so any subsequent elements
     * added will cause the backing array to be grown.
     */
    public IntVLA(final int[] array) {
	this(array, 0, array.length);
    }

    /**
     * Creates a new array containing the elements in the specified array. The
     * capacity is set to the number of elements, so any subsequent elements added
     * will cause the backing array to be grown.
     *
     * @param array      the int array to copy from
     * @param startIndex the first index in array to copy from
     * @param count      the number of ints to copy from array into this IntVLA
     */
    public IntVLA(final int[] array, final int startIndex, final int count) {
	this(count);
	this.size = count;
	System.arraycopy(array, startIndex, this.items, 0, count);
    }

    public void add(final int value) {
	int[] items = this.items;
	if (this.size == items.length) {
	    items = this.resize(Math.max(8, (int) (this.size * 1.75f)));
	}
	items[this.size++] = value;
    }

    public void addAll(final IntVLA array) {
	this.addAll(array, 0, array.size);
    }

    public void addAll(final IntVLA array, final int offset, final int length) {
	if (offset + length > array.size) {
	    throw new IllegalArgumentException(
		    "offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
	}
	this.addAll(array.items, offset, length);
    }

    public void addAll(final int... array) {
	this.addAll(array, 0, array.length);
    }

    public void addAll(final int[] array, final int offset, final int length) {
	int[] items = this.items;
	final int sizeNeeded = this.size + length;
	if (sizeNeeded > items.length) {
	    items = this.resize(Math.max(8, (int) (sizeNeeded * 1.75f)));
	}
	System.arraycopy(array, offset, items, this.size, length);
	this.size += length;
    }

    public void addRange(final int start, final int end) {
	int[] items = this.items;
	final int sizeNeeded = this.size + end - start;
	if (sizeNeeded > items.length) {
	    items = this.resize(Math.max(8, (int) (sizeNeeded * 1.75f)));
	}
	for (int r = start, i = this.size; r < end; r++, i++) {
	    items[i] = r;
	}
	this.size += end - start;
    }

    public void addFractionRange(final int start, final int end, final int fraction) {
	int[] items = this.items;
	final int sizeNeeded = this.size + (end - start) / fraction + 2;
	if (sizeNeeded > items.length) {
	    items = this.resize(Math.max(8, (int) (sizeNeeded * 1.75f)));
	}
	for (int r = start, i = this.size; r < end; r = fraction * (r / fraction + 1), i++, this.size++) {
	    items[i] = r;
	}
    }

    public int get(final int index) {
	if (index >= this.size) {
	    throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
	}
	return this.items[index];
    }

    public void set(final int index, final int value) {
	if (index >= this.size) {
	    throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
	}
	this.items[index] = value;
    }

    /**
     * Adds value to the item in the IntVLA at index. Calling it "add" would overlap
     * with the collection method.
     *
     * @param index
     * @param value
     */
    public void incr(final int index, final int value) {
	if (index >= this.size) {
	    throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
	}
	this.items[index] += value;
    }

    public void mul(final int index, final int value) {
	if (index >= this.size) {
	    throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
	}
	this.items[index] *= value;
    }

    public void insert(final int index, final int value) {
	if (index > this.size) {
	    throw new IndexOutOfBoundsException("index can't be > size: " + index + " > " + this.size);
	}
	int[] items = this.items;
	if (this.size == items.length) {
	    items = this.resize(Math.max(8, (int) (this.size * 1.75f)));
	}
	System.arraycopy(items, index, items, index + 1, this.size - index);
	this.size++;
	items[index] = value;
    }

    public void swap(final int first, final int second) {
	if (first >= this.size) {
	    throw new IndexOutOfBoundsException("first can't be >= size: " + first + " >= " + this.size);
	}
	if (second >= this.size) {
	    throw new IndexOutOfBoundsException("second can't be >= size: " + second + " >= " + this.size);
	}
	final int[] items = this.items;
	final int firstValue = items[first];
	items[first] = items[second];
	items[second] = firstValue;
    }

    /**
     * Given an array or varargs of replacement indices for the values of this
     * IntVLA, reorders this so the first item in the returned version is the same
     * as {@code get(ordering[0])} (with some care taken for negative or too-large
     * indices), the second item in the returned version is the same as
     * {@code get(ordering[1])}, etc. <br>
     * Negative indices are considered reversed distances from the end of ordering,
     * so -1 refers to the same index as {@code ordering[ordering.length - 1]}. If
     * ordering is smaller than this IntVLA, only the indices up to the length of
     * ordering will be modified. If ordering is larger than this IntVLA, only as
     * many indices will be affected as this IntVLA's size, and reversed distances
     * are measured from the end of this IntVLA instead of the end of ordering.
     * Duplicate values in ordering will produce duplicate values in the returned
     * IntVLA. <br>
     * This method modifies this IntVLA in-place and also returns it for chaining.
     *
     * @param ordering an array or varargs of int indices, where the nth item in
     *                 ordering changes the nth item in this IntVLA to have the
     *                 value currently in this IntVLA at the index specified by the
     *                 value in ordering
     * @return this for chaining, after modifying it in-place
     */
    public IntVLA reorder(final int... ordering) {
	int ol;
	if (ordering == null || (ol = Math.min(this.size, ordering.length)) == 0) {
	    return this;
	}
	final int[] items = this.items, alt = new int[ol];
	for (int i = 0; i < ol; i++) {
	    alt[i] = items[(ordering[i] % ol + ol) % ol];
	}
	System.arraycopy(alt, 0, items, 0, ol);
	return this;
    }

    public boolean contains(final int value) {
	int i = this.size - 1;
	final int[] items = this.items;
	while (i >= 0) {
	    if (items[i--] == value) {
		return true;
	    }
	}
	return false;
    }

    public int indexOf(final int value) {
	final int[] items = this.items;
	for (int i = 0, n = this.size; i < n; i++) {
	    if (items[i] == value) {
		return i;
	    }
	}
	return -1;
    }

    public int lastIndexOf(final int value) {
	final int[] items = this.items;
	for (int i = this.size - 1; i >= 0; i--) {
	    if (items[i] == value) {
		return i;
	    }
	}
	return -1;
    }

    /**
     * Removes the first occurrence of the requested value, and returns the index it
     * was removed at (-1 if not found)
     *
     * @param value a value in this IntVLA to remove
     * @return the index the value was found and removed at, or -1 if it was not
     *         present
     */
    public int removeValue(final int value) {
	final int[] items = this.items;
	for (int i = 0, n = this.size; i < n; i++) {
	    if (items[i] == value) {
		this.removeIndex(i);
		return i;
	    }
	}
	return -1;
    }

    /** Removes and returns the item at the specified index. */
    public int removeIndex(final int index) {
	if (index >= this.size) {
	    throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
	}
	final int[] items = this.items;
	final int value = items[index];
	this.size--;
	System.arraycopy(items, index + 1, items, index, this.size - index);
	return value;
    }

    /** Removes the items between the specified indices, inclusive. */
    public void removeRange(final int start, final int end) {
	if (end >= this.size) {
	    throw new IndexOutOfBoundsException("end can't be >= size: " + end + " >= " + this.size);
	}
	if (start > end) {
	    throw new IndexOutOfBoundsException("start can't be > end: " + start + " > " + end);
	}
	final int[] items = this.items;
	final int count = end - start + 1;
	System.arraycopy(items, start + count, items, start, this.size - (start + count));
	this.size -= count;
    }

    /**
     * Removes from this array all of elements contained in the specified array.
     *
     * @return true if this array was modified.
     */
    public boolean removeAll(final IntVLA array) {
	int size = this.size;
	final int startSize = size;
	final int[] items = this.items;
	for (int i = 0, n = array.size; i < n; i++) {
	    final int item = array.get(i);
	    for (int ii = 0; ii < size; ii++) {
		if (item == items[ii]) {
		    this.removeIndex(ii);
		    size--;
		    break;
		}
	    }
	}
	return size != startSize;
    }

    /** Moves the item at the specified index to the first index and returns it. */
    public int moveToFirst(final int index) {
	if (index >= this.size) {
	    throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
	}
	final int[] items = this.items;
	final int value = items[index];
	if (index == 0) {
	    return value;
	}
	System.arraycopy(items, 0, items, 1, index);
	items[0] = value;
	return value;
    }

    /** Moves the item at the specified index to the last index and returns it. */
    public int moveToLast(final int index) {
	if (index >= this.size) {
	    throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
	}
	final int[] items = this.items;
	final int value = items[index];
	if (index == this.size - 1) {
	    return value;
	}
	System.arraycopy(items, index + 1, items, index, this.size - index - 1);
	items[this.size - 1] = value;
	return value;
    }

    /** Removes and returns the last item. */
    public int pop() {
	return this.items[--this.size];
    }

    /** Returns the last item. */
    public int peek() {
	return this.items[this.size - 1];
    }

    /** Returns the first item. */
    public int first() {
	if (this.size == 0) {
	    throw new IllegalStateException("IntVLA is empty.");
	}
	return this.items[0];
    }

    public void clear() {
	this.size = 0;
    }

    /**
     * Reduces the size of the backing array to the size of the actual items. This
     * is useful to release memory when many items have been removed, or if it is
     * known that more items will not be added.
     *
     * @return {@link #items}
     */
    public int[] shrink() {
	if (this.items.length != this.size) {
	    this.resize(this.size);
	}
	return this.items;
    }

    /**
     * Increases the size of the backing array to accommodate the specified number
     * of additional items. Useful before adding many items to avoid multiple
     * backing array resizes.
     *
     * @return {@link #items}
     */
    public int[] ensureCapacity(final int additionalCapacity) {
	final int sizeNeeded = this.size + additionalCapacity;
	if (sizeNeeded > this.items.length) {
	    this.resize(Math.max(8, sizeNeeded));
	}
	return this.items;
    }

    /**
     * Sets the array size, leaving any values beyond the current size undefined.
     *
     * @return {@link #items}
     */
    public int[] setSize(final int newSize) {
	if (newSize > this.items.length) {
	    this.resize(Math.max(8, newSize));
	}
	this.size = newSize;
	return this.items;
    }

    protected int[] resize(final int newSize) {
	final int[] newItems = new int[newSize];
	final int[] items = this.items;
	System.arraycopy(items, 0, newItems, 0, Math.min(this.size, newItems.length));
	this.items = newItems;
	return newItems;
    }

    public void sort() {
	Arrays.sort(this.items, 0, this.size);
    }

    public void reverse() {
	final int[] items = this.items;
	for (int i = 0, lastIndex = this.size - 1, n = this.size / 2; i < n; i++) {
	    final int ii = lastIndex - i;
	    final int temp = items[i];
	    items[i] = items[ii];
	    items[ii] = temp;
	}
    }

    /**
     * Reduces the size of the array to the specified size. If the array is already
     * smaller than the specified size, no action is taken.
     */
    public void truncate(final int newSize) {
	if (this.size > newSize) {
	    this.size = newSize;
	}
    }

    public int getRandomElement(final RNG random) {
	return this.items[random.nextInt(this.items.length)];
    }

    /**
     * Shuffles this IntVLA in place using the given RNG.
     *
     * @param random an RNG used to generate the shuffled order
     * @return this object, modified, after shuffling
     */
    public IntVLA shuffle(final RNG random) {
	final int n = this.size;
	for (int i = 0; i < n; i++) {
	    this.swap(i + random.nextInt(n - i), i);
	}
	return this;
    }

    public int[] toArray() {
	final int[] array = new int[this.size];
	System.arraycopy(this.items, 0, array, 0, this.size);
	return array;
    }

    public IntVLA copy() {
	return new IntVLA(this);
    }

    @GwtIncompatible
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
	try {
	    final IntVLA nx = (IntVLA) super.clone();
	    nx.items = new int[this.items.length];
	    System.arraycopy(this.items, 0, nx.items, 0, this.items.length);
	    return nx;
	} catch (final CloneNotSupportedException e) {
	    throw new InternalError(e + (e.getMessage() != null ? "; " + e.getMessage() : ""));
	}
    }

    @Override
    public int hashCode() {
	final int[] items = this.items;
	int h = 1;
	for (int i = 0, n = this.size; i < n; i++) {
	    h = h * 31 + items[i];
	}
	return h;
    }

    public int hashWisp() {
	final int[] data = this.items;
	long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
	final int len = this.size;
	for (int i = 0; i < len; i++) {
	    result += a ^= 0x8329C6EB9E6AD3E3L * data[i];
	}
	return (int) (result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public long hash64() {
	final int[] data = this.items;
	long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
	final int len = this.size;
	for (int i = 0; i < len; i++) {
	    result += a ^= 0x8329C6EB9E6AD3E3L * data[i];
	}
	return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    @Override
    public boolean equals(final Object object) {
	if (object == this) {
	    return true;
	}
	if (!(object instanceof IntVLA)) {
	    return false;
	}
	final IntVLA array = (IntVLA) object;
	final int n = this.size;
	if (n != array.size) {
	    return false;
	}
	for (int i = 0; i < n; i++) {
	    if (this.items[i] != array.items[i]) {
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
	final int[] items = this.items;
	final StringBuilder buffer = new StringBuilder(32);
	buffer.append('[');
	buffer.append(items[0]);
	for (int i = 1; i < this.size; i++) {
	    buffer.append(", ");
	    buffer.append(items[i]);
	}
	buffer.append(']');
	return buffer.toString();
    }

    public String toString(final String separator) {
	if (this.size == 0) {
	    return "";
	}
	final int[] items = this.items;
	final StringBuilder buffer = new StringBuilder(32);
	buffer.append(items[0]);
	for (int i = 1; i < this.size; i++) {
	    buffer.append(separator);
	    buffer.append(items[i]);
	}
	return buffer.toString();
    }

    public static IntVLA deserializeFromString(final String data) {
	final int amount = StringKit.count(data, ",");
	if (amount <= 0) {
	    return new IntVLA();
	}
	final IntVLA iv = new IntVLA(amount + 1);
	final int dl = 1;
	int idx = -dl, idx2;
	for (int i = 0; i < amount; i++) {
	    iv.add(StringKit.intFromDec(data, idx + dl, idx = data.indexOf(",", idx + dl)));
	}
	if ((idx2 = data.indexOf(",", idx + dl)) < 0) {
	    iv.add(StringKit.intFromDec(data, idx + dl, data.length()));
	} else {
	    iv.add(StringKit.intFromDec(data, idx + dl, idx2));
	}
	return iv;
    }

    /** @see #IntVLA(int[]) */
    public static IntVLA with(final int... array) {
	return new IntVLA(array);
    }

    public boolean isEmpty() {
	return this.size == 0;
    }
}