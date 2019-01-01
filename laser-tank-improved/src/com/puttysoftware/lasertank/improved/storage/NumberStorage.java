package com.puttysoftware.lasertank.improved.storage;

import java.util.Arrays;

public final class NumberStorage {
    // Fields
    private final int[] dataStore;
    private final int[] dataShape;
    private final int[] interProd;

    // Constructor
    public NumberStorage(final int... shape) {
	this.dataShape = shape;
	this.interProd = new int[shape.length];
	int product = 1;
	for (int x = 0; x < shape.length; x++) {
	    this.interProd[x] = product;
	    product *= shape[x];
	}
	this.dataStore = new int[product];
    }

    // Copy constructor
    public NumberStorage(final NumberStorage source) {
	this.dataShape = source.dataShape;
	this.interProd = new int[this.dataShape.length];
	int product = 1;
	for (int x = 0; x < this.dataShape.length; x++) {
	    this.interProd[x] = product;
	    product *= this.dataShape[x];
	}
	this.dataStore = Arrays.copyOf(source.dataStore, product);
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof NumberStorage)) {
	    return false;
	}
	final NumberStorage other = (NumberStorage) obj;
	if (!Arrays.equals(this.dataStore, other.dataStore)) {
	    return false;
	}
	return true;
    }

    public void fill(final int obj) {
	for (int x = 0; x < this.dataStore.length; x++) {
	    this.dataStore[x] = obj;
	}
    }

    public int getCell(final int... loc) {
	final int aloc = this.ravelLocation(loc);
	return this.dataStore[aloc];
    }

    public int[] getShape() {
	return this.dataShape;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	final int result = 1;
	return prime * result + Arrays.hashCode(this.dataStore);
    }

    // Methods
    private int ravelLocation(final int... loc) {
	int res = 0;
	// Sanity check #1
	if (loc.length != this.interProd.length) {
	    throw new IllegalArgumentException(Integer.toString(loc.length));
	}
	for (int x = 0; x < this.interProd.length; x++) {
	    // Sanity check #2
	    if (loc[x] < 0 || loc[x] >= this.dataShape[x]) {
		throw new ArrayIndexOutOfBoundsException(loc[x]);
	    }
	    res += loc[x] * this.interProd[x];
	}
	return res;
    }

    public void setCell(final int obj, final int... loc) {
	final int aloc = this.ravelLocation(loc);
	this.dataStore[aloc] = obj;
    }
}
