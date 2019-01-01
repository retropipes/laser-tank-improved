/*  LLDS: Arbitrary dimension arrays for Java programs
Licensed under Apache 2.0. See the LICENSE file for details.

All support is handled via the GitHub repository: https://github.com/wrldwzrd89/lib-java-low-level-data-storage
 */
package com.puttysoftware.lasertank.improved.storage;

public final class DecimalStorage implements Cloneable {
    // Fields
    private final double[] storage;
    private final int[] storageShape;
    private final int[] products;

    // Constructor
    public DecimalStorage(final int... shape) {
	this.storageShape = shape;
	this.products = new int[this.storageShape.length];
	int product = 1;
	for (int x = 0; x < this.storageShape.length; x++) {
	    this.products[x] = product;
	    product *= this.storageShape[x];
	}
	this.storage = new double[product];
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
	final DecimalStorage copy = new DecimalStorage(this.storageShape);
	System.arraycopy(this.storage, 0, copy.storage, 0, this.storage.length);
	return copy;
    }

    public int[] getShape() {
	return this.storageShape;
    }

    public double getCell(final int... loc) {
	final int aloc = this.ravelLocation(loc);
	return this.storage[aloc];
    }

    public void setCell(final double obj, final int... loc) {
	final int aloc = this.ravelLocation(loc);
	this.storage[aloc] = obj;
    }
}
