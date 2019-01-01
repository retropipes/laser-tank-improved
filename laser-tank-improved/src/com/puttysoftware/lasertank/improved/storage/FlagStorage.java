package com.puttysoftware.lasertank.improved.storage;

import java.util.Arrays;

public final class FlagStorage {
    // Fields
    private final boolean[] dataStore;
    private final int[] dataShape;
    private final int[] interProd;

    // Constructor
    public FlagStorage(final int... shape) {
	this.dataShape = shape;
	this.interProd = new int[this.dataShape.length];
	int product = 1;
	for (int x = 0; x < this.dataShape.length; x++) {
	    this.interProd[x] = product;
	    product *= this.dataShape[x];
	}
	this.dataStore = new boolean[product];
    }

    // Copy constructor
    public FlagStorage(final FlagStorage source) {
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
	if (!(obj instanceof FlagStorage)) {
	    return false;
	}
	final FlagStorage other = (FlagStorage) obj;
	if (!Arrays.equals(this.dataStore, other.dataStore)) {
	    return false;
	}
	return true;
    }

    public boolean getCell(final int... loc) {
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

    public void setCell(final boolean obj, final int... loc) {
	final int aloc = this.ravelLocation(loc);
	this.dataStore[aloc] = obj;
    }
}
