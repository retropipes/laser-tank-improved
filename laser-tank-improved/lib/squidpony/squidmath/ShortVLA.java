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

/**
 * A resizable, ordered or unordered short variable-length array. Avoids the
 * boxing that occurs with {@code ArrayList<Short>}. If unordered, this class
 * avoids a memory copy when removing elements (the last element is moved to the
 * removed element's position). Used internally by CoordPacker, and unlikely to
 * be used outside of it. <br>
 * Was called IntArray in libGDX; to avoid confusion with the fixed-length
 * primitive array type, VLA (variable-length array) was chosen as a different
 * name. Also uses short instead of int, of course. Copied from LibGDX by Tommy
 * Ettinger on 10/1/2015.
 *
 * @author Nathan Sweet
 */
public class ShortVLA implements Serializable {
    private static final long serialVersionUID = -2948161891082748626L;
    public short[] items;
    public int size;
    public boolean ordered;

    /** Creates an ordered array with a capacity of 16. */
    public ShortVLA() {
	this(true, 16);
    }

    /** Creates an ordered array with the specified capacity. */
    public ShortVLA(final int capacity) {
	this(true, capacity);
    }

    /**
     * @param ordered  If false, methods that remove elements may change the order
     *                 of other elements in the array, which avoids a memory copy.
     * @param capacity Any elements added beyond this will cause the backing array
     *                 to be grown.
     */
    public ShortVLA(final boolean ordered, final int capacity) {
	this.ordered = ordered;
	this.items = new short[capacity];
    }

    /**
     * Creates a new array containing the elements in the specific array. The new
     * array will be ordered if the specific array is ordered. The capacity is set
     * to the number of elements, so any subsequent elements added will cause the
     * backing array to be grown.
     */
    public ShortVLA(final ShortVLA array) {
	this.ordered = array.ordered;
	this.size = array.size;
	this.items = new short[this.size];
	System.arraycopy(array.items, 0, this.items, 0, this.size);
    }

    /**
     * Creates a new ordered array containing the elements in the specified array.
     * The capacity is set to the number of elements, so any subsequent elements
     * added will cause the backing array to be grown.
     */
    public ShortVLA(final short[] array) {
	this(true, array, 0, array.length);
    }

    /**
     * Creates a new ordered array containing the elements in the specified array,
     * converted to short. The capacity is set to the number of elements, so any
     * subsequent elements added will cause the backing array to be grown.
     */
    public ShortVLA(final int[] array) {
	this(true, array.length);
	for (int i = 0; i < array.length; i++) {
	    this.items[this.size + i] = (short) array[i];
	}
	this.size += array.length;
    }

    /**
     * Creates a new array containing the elements in the specified array. The
     * capacity is set to the number of elements, so any subsequent elements added
     * will cause the backing array to be grown.
     *
     * @param ordered If false, methods that remove elements may change the order of
     *                other elements in the array, which avoids a memory copy.
     */
    public ShortVLA(final boolean ordered, final short[] array, final int startIndex, final int count) {
	this(ordered, count);
	this.size = count;
	System.arraycopy(array, startIndex, this.items, 0, count);
    }

    public void add(final short value) {
	short[] items = this.items;
	if (this.size == items.length) {
	    items = this.resize(Math.max(8, (int) (this.size * 1.75f)));
	}
	items[this.size++] = value;
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
	short[] items = this.items;
	final int sizeNeeded = this.size + length;
	if (sizeNeeded > items.length) {
	    items = this.resize(Math.max(8, (int) (sizeNeeded * 1.75f)));
	}
	System.arraycopy(array, offset, items, this.size, length);
	this.size += length;
    }

    public void addAll(final int[] array) {
	short[] items = this.items;
	final int sizeNeeded = this.size + array.length;
	if (sizeNeeded > items.length) {
	    items = this.resize(Math.max(8, (int) (sizeNeeded * 1.75f)));
	}
	for (int i = 0; i < array.length; i++) {
	    items[this.size + i] = (short) array[i];
	}
	this.size += array.length;
    }

    public void addRange(final int start, final int end) {
	short[] items = this.items;
	final int sizeNeeded = this.size + end - start;
	if (sizeNeeded > items.length) {
	    items = this.resize(Math.max(8, (int) (sizeNeeded * 1.75f)));
	}
	for (int r = start, i = this.size; r < end; r++, i++) {
	    items[i] = (short) r;
	}
	this.size += end - start;
    }

    public void addFractionRange(final int start, final int end, final int fraction) {
	short[] items = this.items;
	final int sizeNeeded = this.size + (end - start) / fraction + 2;
	if (sizeNeeded > items.length) {
	    items = this.resize(Math.max(8, (int) (sizeNeeded * 1.75f)));
	}
	for (int r = start, i = this.size; r < end; r = fraction * (r / fraction + 1), i++, this.size++) {
	    items[i] = (short) r;
	}
    }

    public short get(final int index) {
	if (index >= this.size) {
	    throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
	}
	return this.items[index];
    }

    public void set(final int index, final short value) {
	if (index >= this.size) {
	    throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
	}
	this.items[index] = value;
    }

    /**
     * Adds value to the item in the ShortVLA at index. Calling it "add" would
     * overlap with the collection method.
     *
     * @param index
     * @param value
     */
    public void incr(final int index, final short value) {
	if (index >= this.size) {
	    throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
	}
	this.items[index] += value;
    }

    public void mul(final int index, final short value) {
	if (index >= this.size) {
	    throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
	}
	this.items[index] *= value;
    }

    public void insert(final int index, final short value) {
	if (index > this.size) {
	    throw new IndexOutOfBoundsException("index can't be > size: " + index + " > " + this.size);
	}
	short[] items = this.items;
	if (this.size == items.length) {
	    items = this.resize(Math.max(8, (int) (this.size * 1.75f)));
	}
	if (this.ordered) {
	    System.arraycopy(items, index, items, index + 1, this.size - index);
	} else {
	    items[this.size] = items[index];
	}
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
	final short[] items = this.items;
	final short firstValue = items[first];
	items[first] = items[second];
	items[second] = firstValue;
    }

    public boolean contains(final short value) {
	int i = this.size - 1;
	final short[] items = this.items;
	while (i >= 0) {
	    if (items[i--] == value) {
		return true;
	    }
	}
	return false;
    }

    public int indexOf(final short value) {
	final short[] items = this.items;
	for (int i = 0, n = this.size; i < n; i++) {
	    if (items[i] == value) {
		return i;
	    }
	}
	return -1;
    }

    public int lastIndexOf(final short value) {
	final short[] items = this.items;
	for (int i = this.size - 1; i >= 0; i--) {
	    if (items[i] == value) {
		return i;
	    }
	}
	return -1;
    }

    public boolean removeValue(final short value) {
	final short[] items = this.items;
	for (int i = 0, n = this.size; i < n; i++) {
	    if (items[i] == value) {
		this.removeIndex(i);
		return true;
	    }
	}
	return false;
    }

    /** Removes and returns the item at the specified index. */
    public short removeIndex(final int index) {
	if (index >= this.size) {
	    throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
	}
	final short[] items = this.items;
	final short value = items[index];
	this.size--;
	if (this.ordered) {
	    System.arraycopy(items, index + 1, items, index, this.size - index);
	} else {
	    items[index] = items[this.size];
	}
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
	final short[] items = this.items;
	final int count = end - start + 1;
	if (this.ordered) {
	    System.arraycopy(items, start + count, items, start, this.size - (start + count));
	} else {
	    final int lastIndex = this.size - 1;
	    for (int i = 0; i < count; i++) {
		items[start + i] = items[lastIndex - i];
	    }
	}
	this.size -= count;
    }

    /**
     * Removes from this array all of elements contained in the specified array.
     *
     * @return true if this array was modified.
     */
    public boolean removeAll(final ShortVLA array) {
	int size = this.size;
	final int startSize = size;
	final short[] items = this.items;
	for (int i = 0, n = array.size; i < n; i++) {
	    final short item = array.get(i);
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

    /** Removes and returns the last item. */
    public short pop() {
	return this.items[--this.size];
    }

    /** Returns the last item. */
    public short peek() {
	return this.items[this.size - 1];
    }

    /** Returns the first item. */
    public short first() {
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
    public short[] shrink() {
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
    public short[] ensureCapacity(final int additionalCapacity) {
	final int sizeNeeded = this.size + additionalCapacity;
	if (sizeNeeded > this.items.length) {
	    this.resize(Math.max(8, sizeNeeded));
	}
	return this.items;
    }

    protected short[] resize(final int newSize) {
	final short[] newItems = new short[newSize];
	final short[] items = this.items;
	System.arraycopy(items, 0, newItems, 0, Math.min(this.size, newItems.length));
	this.items = newItems;
	return newItems;
    }

    public int[] asInts() {
	final int[] newItems = new int[this.size];
	final short[] items = this.items;
	for (int i = 0; i < this.size; i++) {
	    newItems[i] = items[i] & 0xffff;
	}
	return newItems;
    }

    public void sort() {
	Arrays.sort(this.items, 0, this.size);
    }

    public void reverse() {
	final short[] items = this.items;
	for (int i = 0, lastIndex = this.size - 1, n = this.size / 2; i < n; i++) {
	    final int ii = lastIndex - i;
	    final short temp = items[i];
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

    public short[] toArray() {
	final short[] array = new short[this.size];
	System.arraycopy(this.items, 0, array, 0, this.size);
	return array;
    }

    @Override
    public int hashCode() {
	if (!this.ordered) {
	    return super.hashCode();
	}
	final short[] items = this.items;
	int h = 1;
	for (int i = 0, n = this.size; i < n; i++) {
	    h = h * 31 + items[i];
	}
	return h;
    }

    @Override
    public boolean equals(final Object object) {
	if (object == this) {
	    return true;
	}
	if (!this.ordered) {
	    return false;
	}
	if (!(object instanceof ShortVLA)) {
	    return false;
	}
	final ShortVLA array = (ShortVLA) object;
	if (!array.ordered) {
	    return false;
	}
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
	final short[] items = this.items;
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
	final short[] items = this.items;
	final StringBuilder buffer = new StringBuilder(32);
	buffer.append(items[0]);
	for (int i = 1; i < this.size; i++) {
	    buffer.append(separator);
	    buffer.append(items[i]);
	}
	return buffer.toString();
    }

    /** @see #ShortVLA(short[]) */
    public static ShortVLA with(final short... array) {
	return new ShortVLA(array);
    }
}