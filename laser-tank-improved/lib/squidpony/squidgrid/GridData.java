package squidpony.squidgrid;

import java.util.ArrayList;

import squidpony.squidmath.Arrangement;
import squidpony.squidmath.CrossHash;

/**
 * Customized storage of multiple types of 2D data, accessible by String (or
 * CharSequence) keys. Meant for cases where SquidLib needs to produce a grid
 * with multiple types of data, such as what could be represented by a 2D array
 * of objects containing only char, int, double, and/or float data, but this is
 * much more memory-efficient than producing many objects. This can be seen as a
 * "struct-of-array" approach, to use C terminology, rather than an
 * "array-of-struct" approach. This also allows new 2D arrays to be added to an
 * existing GridData if needed.
 */
public class GridData {
    public Arrangement<CharSequence> names;
    public ArrayList<char[][]> charMaps;
    public ArrayList<int[][]> intMaps;
    public ArrayList<double[][]> doubleMaps;
    public ArrayList<float[][]> floatMaps;

    public GridData() {
	this(16);
    }

    public GridData(final int expectedSize) {
	this.names = new Arrangement<>(expectedSize, CrossHash.stringHasher);
	this.charMaps = new ArrayList<>(expectedSize);
	this.intMaps = new ArrayList<>(expectedSize);
	this.doubleMaps = new ArrayList<>(expectedSize);
	this.floatMaps = new ArrayList<>(expectedSize);
    }

    public boolean contains(final CharSequence item) {
	return this.names.containsKey(item);
    }

    public int indexOf(final CharSequence item) {
	return this.names.getInt(item);
    }

    public char[][] getChars(final CharSequence item) {
	final int i = this.names.getInt(item);
	char[][] d = null;
	if (i >= 0 && i < this.charMaps.size()) {
	    d = this.charMaps.get(i);
	}
	return d;
    }

    public char[][] getChars(final int index) {
	char[][] d = null;
	if (index >= 0 && index < this.charMaps.size()) {
	    d = this.charMaps.get(index);
	}
	return d;
    }

    public int[][] getInts(final CharSequence item) {
	final int i = this.names.getInt(item);
	int[][] d = null;
	if (i >= 0 && i < this.intMaps.size()) {
	    d = this.intMaps.get(i);
	}
	return d;
    }

    public int[][] getInts(final int index) {
	int[][] d = null;
	if (index >= 0 && index < this.intMaps.size()) {
	    d = this.intMaps.get(index);
	}
	return d;
    }

    public double[][] getDoubles(final CharSequence item) {
	final int i = this.names.getInt(item);
	double[][] d = null;
	if (i >= 0 && i < this.charMaps.size()) {
	    d = this.doubleMaps.get(i);
	}
	return d;
    }

    public double[][] getDoubles(final int index) {
	double[][] d = null;
	if (index >= 0 && index < this.doubleMaps.size()) {
	    d = this.doubleMaps.get(index);
	}
	return d;
    }

    public float[][] getFloats(final CharSequence item) {
	final int i = this.names.getInt(item);
	float[][] d = null;
	if (i >= 0 && i < this.floatMaps.size()) {
	    d = this.floatMaps.get(i);
	}
	return d;
    }

    public float[][] getFloats(final int index) {
	float[][] d = null;
	if (index >= 0 && index < this.floatMaps.size()) {
	    d = this.floatMaps.get(index);
	}
	return d;
    }

    public int putChars(final CharSequence name, final char[][] item) {
	int i = this.names.getInt(name);
	if (i < 0) {
	    i = this.names.size();
	    this.names.add(name);
	    this.charMaps.add(item);
	    this.intMaps.add(null);
	    this.doubleMaps.add(null);
	    this.floatMaps.add(null);
	} else {
	    this.charMaps.set(i, item);
	    this.intMaps.set(i, null);
	    this.doubleMaps.set(i, null);
	    this.floatMaps.set(i, null);
	}
	return i;
    }

    public int putInts(final CharSequence name, final int[][] item) {
	int i = this.names.getInt(name);
	if (i < 0) {
	    i = this.names.size();
	    this.names.add(name);
	    this.charMaps.add(null);
	    this.intMaps.add(item);
	    this.doubleMaps.add(null);
	    this.floatMaps.add(null);
	} else {
	    this.charMaps.set(i, null);
	    this.intMaps.set(i, item);
	    this.doubleMaps.set(i, null);
	    this.floatMaps.set(i, null);
	}
	return i;
    }

    public int putDoubles(final CharSequence name, final double[][] item) {
	int i = this.names.getInt(name);
	if (i < 0) {
	    i = this.names.size();
	    this.names.add(name);
	    this.charMaps.add(null);
	    this.intMaps.add(null);
	    this.doubleMaps.add(item);
	    this.floatMaps.add(null);
	} else {
	    this.charMaps.set(i, null);
	    this.intMaps.set(i, null);
	    this.doubleMaps.set(i, item);
	    this.floatMaps.set(i, null);
	}
	return i;
    }

    public int putFloats(final CharSequence name, final float[][] item) {
	int i = this.names.getInt(name);
	if (i < 0) {
	    i = this.names.size();
	    this.names.add(name);
	    this.charMaps.add(null);
	    this.intMaps.add(null);
	    this.doubleMaps.add(null);
	    this.floatMaps.add(item);
	} else {
	    this.charMaps.set(i, null);
	    this.intMaps.set(i, null);
	    this.doubleMaps.set(i, null);
	    this.floatMaps.set(i, item);
	}
	return i;
    }
}
