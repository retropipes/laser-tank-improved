package regexodus.ds;

import java.util.Arrays;

/**
 * An implementation of BitSet (that does not technically extend BitSet due to
 * BitSet not existing under GWT) using 32-bit sections instead of the normal
 * 64-bit (again, for GWT reasons; 64-bit integer math is slower on GWT).
 * Created by Tommy Ettinger on 3/30/2016.
 */
public class IntBitSet {
    private final int[] data;

    public IntBitSet() {
	this.data = new int[8];
    }

    /**
     * Constructs a CharBitSet that includes all bits between start and end,
     * inclusive.
     *
     * @param start inclusive
     * @param end   inclusive
     */
    public IntBitSet(final int start, final int end) {
	this.data = new int[8];
	this.set(start, end + 1);
    }

    public IntBitSet(final int[] ints) {
	this.data = new int[8];
	System.arraycopy(ints, 0, this.data, 0, Math.min(8, ints.length));
    }

    public void flip(final int bitIndex) {
	this.data[bitIndex >> 5] ^= 1 << (bitIndex & 31);
    }

    public void flip(final int fromIndex, final int toIndex) {
	for (int i = fromIndex; i <= toIndex; i++) {
	    this.data[i >> 5] ^= 1 << (i & 31);
	}
    }

    public void set(final int bitIndex) {
	this.data[bitIndex >> 5] |= 1 << (bitIndex & 31);
    }

    public void set(final int bitIndex, final boolean value) {
	this.data[bitIndex >> 5] ^= ((value ? -1 : 0) ^ this.data[bitIndex >> 5]) & 1 << (bitIndex & 31);
    }

    public void set(final int fromIndex, final int toIndex) {
	for (int i = fromIndex; i <= toIndex; i++) {
	    this.data[i >> 5] |= 1 << (i & 31);
	}
    }

    public void set(final int fromIndex, final int toIndex, final boolean value) {
	final int val = value ? -1 : 0;
	for (int bitIndex = fromIndex; bitIndex <= toIndex; bitIndex++) {
	    this.data[bitIndex >> 5] ^= (val ^ this.data[bitIndex >> 5]) & 1 << (bitIndex & 31);
	}
    }

    public void clear(final int bitIndex) {
	this.data[bitIndex >> 5] &= ~(1 << (bitIndex & 31));
    }

    public void clear(final int fromIndex, final int toIndex) {
	for (int bitIndex = fromIndex; bitIndex <= toIndex; bitIndex++) {
	    this.data[bitIndex >> 5] &= ~(1 << (bitIndex & 31));
	}
    }

    public void clear() {
	Arrays.fill(this.data, 0);
    }

    public boolean get(final int bitIndex) {
	return (this.data[bitIndex >> 5] >>> (bitIndex & 31) & 1) != 0;
    }

    public IntBitSet get(final int fromIndex, final int toIndex) {
	final IntBitSet ibs = new IntBitSet();
	for (int bitIndex = fromIndex; bitIndex <= toIndex; bitIndex++) {
	    ibs.set(bitIndex, this.get(bitIndex));
	}
	return ibs;
    }

    public int length() {
	return 32 * this.data.length;
    }

    public boolean isEmpty() {
	for (int i = 0; i < 8; i++) {
	    if (this.data[i] != 0) {
		return false;
	    }
	}
	return true;
    }

    public boolean intersects(final IntBitSet set) {
	for (int i = 0; i < 8; i++) {
	    if ((this.data[i] & set.data[i]) != 0) {
		return true;
	    }
	}
	return false;
    }

    public int cardinality() {
	int card = 0;
	for (int i = 0; i < 8; i++) {
	    card += Integer.bitCount(this.data[i]);
	}
	return card;
    }

    public IntBitSet and(final IntBitSet set) {
	for (int i = 0; i < 8; i++) {
	    this.data[i] &= set.data[i];
	}
	return this;
    }

    public IntBitSet or(final IntBitSet set) {
	for (int i = 0; i < 8; i++) {
	    this.data[i] |= set.data[i];
	}
	return this;
    }

    public IntBitSet xor(final IntBitSet set) {
	for (int i = 0; i < 8; i++) {
	    this.data[i] ^= set.data[i];
	}
	return this;
    }

    public IntBitSet andNot(final IntBitSet set) {
	for (int i = 0; i < 8; i++) {
	    this.data[i] &= ~set.data[i];
	}
	return this;
    }

    public IntBitSet negate() {
	for (int i = 0; i < 8; i++) {
	    this.data[i] = ~this.data[i];
	}
	return this;
    }

    public int nextSetBit(int current) {
	int low = 0;
	for (int i = current >>> 5; i < 8 && current < 256; i++) {
	    if (current % 32 != 31) {
		low = Integer.numberOfTrailingZeros(Integer.lowestOneBit(this.data[current >>> 5] >>> (current & 31)));
	    }
	    if (low % 32 != 0) {
		return current + low;
	    }
	    current = ((current >>> 5) + 1) * 32;
	}
	return -1;
    }

    public int nextClearBit(int current) {
	int low = 0;
	for (int i = current >>> 5; i < 8 && current < 256; i++) {
	    if (current % 32 != 31) {
		low = Integer
			.numberOfTrailingZeros(Integer.lowestOneBit(~(this.data[current >>> 5] >>> (current & 31))));
	    }
	    if (low % 32 != 0) {
		return current + low;
	    }
	    current = ((current >>> 5) + 1) * 32;
	}
	return -1;
    }

    public int size() {
	return 256;
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final IntBitSet intBitSet = (IntBitSet) o;
	return Arrays.equals(this.data, intBitSet.data);
    }

    @Override
    public int hashCode() {
	return Arrays.hashCode(this.data);
    }

    @Override
    public IntBitSet clone() {
	return new IntBitSet(this.data);
    }
}
