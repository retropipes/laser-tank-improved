/*  LLDS: Arbitrary dimension arrays for Java programs
Licensed under Apache 2.0. See the LICENSE file for details.

All support is handled via the GitHub repository: https://github.com/wrldwzrd89/lib-java-low-level-data-storage
 */
package com.puttysoftware.lasertank.improved.storage;

public final class LongStorage implements Cloneable {
    // Fields
    private final long[] storage;
    private final int[] storageShape;
    private final int[] products;

    // Constructor
    public LongStorage(final int... shape) {
	this.storageShape = shape;
	this.products = new int[this.storageShape.length];
	int product = 1;
	for (int x = 0; x < this.storageShape.length; x++) {
	    this.products[x] = product;
	    product *= this.storageShape[x];
	}
	this.storage = new long[product];
    }

    // Methods
    private int ravelLocation(final int... loc) {
	int res = 0;
	// Sanity check #1
	if (loc.length != this.products.length) {
	    throw new IllegalArgumentException(Integer.toString(loc.length));
	}
	for (int x = 0; x < this.products.length; x++) {
	    // Sanity check #2
	    if (loc[x] < 0 || loc[x] >= this.storageShape[x]) {
		throw new ArrayIndexOutOfBoundsException(loc[x]);
	    }
	    res += loc[x] * this.products[x];
	}
	return res;
    }

    @Override
    public Object clone() {
	final LongStorage copy = new LongStorage(this.storageShape);
	System.arraycopy(this.storage, 0, copy.storage, 0, this.storage.length);
	return copy;
    }

    public int[] getShape() {
	return this.storageShape;
    }

    public long getCell(final int... loc) {
	final int aloc = this.ravelLocation(loc);
	return this.storage[aloc];
    }

    public void setCell(final long obj, final int... loc) {
	final int aloc = this.ravelLocation(loc);
	this.storage[aloc] = obj;
    }

    public void fill(final long obj) {
	for (int x = 0; x < this.storage.length; x++) {
	    this.storage[x] = obj;
	}
    }
}
