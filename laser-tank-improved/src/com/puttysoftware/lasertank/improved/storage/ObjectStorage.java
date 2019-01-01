/*  LLDS: Arbitrary dimension arrays for Java programs
Licensed under Apache 2.0. See the LICENSE file for details.

All support is handled via the GitHub repository: https://github.com/wrldwzrd89/lib-java-low-level-data-storage
 */
package com.puttysoftware.lasertank.improved.storage;

import java.util.Arrays;

public class ObjectStorage {
    // Fields
    private final Object[] storage;
    private final int[] storageShape;
    private final int[] products;

    // Constructor
    public ObjectStorage(final int... shape) {
	this.storageShape = shape;
	this.products = new int[this.storageShape.length];
	int product = 1;
	for (int x = 0; x < this.storageShape.length; x++) {
	    this.products[x] = product;
	    product *= this.storageShape[x];
	}
	this.storage = new Object[product];
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

    public int[] getShape() {
	return this.storageShape;
    }

    protected Object getRawCell(final int rawLoc) {
	return this.storage[rawLoc];
    }

    protected void setRawCell(final Object cobj, final int rawLoc) {
	this.storage[rawLoc] = cobj;
    }

    protected int getRawLength() {
	return this.storage.length;
    }

    public Object getCell(final int... loc) {
	final int aloc = this.ravelLocation(loc);
	return this.storage[aloc];
    }

    public void setCell(final Object obj, final int... loc) {
	final int aloc = this.ravelLocation(loc);
	this.storage[aloc] = obj;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	final int result = 1;
	return prime * result + Arrays.hashCode(this.storage);
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof ObjectStorage)) {
	    return false;
	}
	final ObjectStorage other = (ObjectStorage) obj;
	if (!Arrays.equals(this.storage, other.storage)) {
	    return false;
	}
	return true;
    }
}
