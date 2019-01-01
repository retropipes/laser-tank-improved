package squidpony.squidmath;

import java.util.Arrays;

/**
 * This is a port of the public domain Isaac (cryptographic) random number
 * generator to Java, by Bob Jenkins. It is a RandomnessSource here, so it
 * should generally be used to make an RNG, which has more features. Isaac32RNG
 * is slower than the non-cryptographic RNGs in SquidLib, but much faster than
 * cryptographic RNGs that need SecureRandom, and it's compatible with GWT and
 * Android to boot! Isaac32RNG should perform better when you specifically need
 * a large amount of int values to be set using {@link #fillBlock(int[])}. If
 * you don't need that, then {@link IsaacRNG} will have better properties.
 * Created by Tommy Ettinger on 8/1/2016.
 */
public class Isaac32RNG implements RandomnessSource {
    private int count; /* count through the results in results[] */
    private final int results[]; /* the results given to the user */
    private final int mem[]; /* the internal state */
    private int a; /* accumulator */
    private int b; /* the last result */
    private int c; /* counter, guarantees cycle is at least 2^^40 */

    /* no seed, equivalent to randinit(ctx,FALSE) in C */
    public Isaac32RNG() {
	this.mem = new int[256];
	this.results = new int[256];
	this.init(false);
    }

    /* equivalent to randinit(ctx, TRUE) after putting seed in randctx in C */
    public Isaac32RNG(final int seed[]) {
	this.mem = new int[256];
	this.results = new int[256];
	if (seed == null) {
	    this.init(false);
	} else {
	    System.arraycopy(seed, 0, this.results, 0, Math.min(256, seed.length));
	    this.init(true);
	}
    }

    /**
     * Constructs an IsaacRNG with its state filled by the value of seed, run
     * through the LightRNG algorithm.
     *
     * @param seed any long; will have equal influence on all bits of state
     */
    public Isaac32RNG(long seed) {
	this.mem = new int[256];
	this.results = new int[256];
	long z;
	for (int i = 0; i < 256; i++) {
	    z = seed += 0x9E3779B97F4A7C15L;
	    z = (z ^ z >>> 30) * 0xBF58476D1CE4E5B9L;
	    z = (z ^ z >>> 27) * 0x94D049BB133111EBL;
	    this.results[i++] = (int) ((z ^ z >>> 31) & 0xffffffffL);
	    this.results[i] = (int) ((z ^ z >>> 31) >>> 32);
	}
	this.init(true);
    }

    /**
     * Constructs an IsaacRNG with its state filled by repeated hashing of seed.
     *
     * @param seed a String that should be exceptionally long to get the best
     *             results.
     */
    public Isaac32RNG(final String seed) {
	this.mem = new int[256];
	this.results = new int[256];
	if (seed == null) {
	    this.init(false);
	} else {
	    final char[] chars = seed.toCharArray();
	    final int slen = chars.length;
	    int i = 0;
	    for (; i < 256 && i < slen; i++) {
		this.results[i] = CrossHash.Wisp.hash(chars, i, slen);
	    }
	    for (; i < 256; i++) {
		this.results[i] = CrossHash.Wisp.hash(this.results);
	    }
	    this.init(true);
	}
    }

    /**
     * Generates 256 results to be used by later calls to next() or nextLong(). This
     * is a fast (not small) implementation.
     */
    public final void regen() {
	int i, j, x, y;
	this.b += ++this.c;
	for (i = 0, j = 128; i < 128;) {
	    x = this.mem[i];
	    this.a = (this.a ^ this.a << 13) + this.mem[j++];
	    this.mem[i] = y = this.mem[x >> 2 & 255] + this.a + this.b;
	    this.results[i++] = this.b = this.mem[y >> 10 & 255] + x;
	    x = this.mem[i];
	    this.a = (this.a ^ this.a >>> 6) + this.mem[j++];
	    this.mem[i] = y = this.mem[x >> 2 & 255] + this.a + this.b;
	    this.results[i++] = this.b = this.mem[y >> 10 & 255] + x;
	    x = this.mem[i];
	    this.a = (this.a ^ this.a << 2) + this.mem[j++];
	    this.mem[i] = y = this.mem[x >> 2 & 255] + this.a + this.b;
	    this.results[i++] = this.b = this.mem[y >> 10 & 255] + x;
	    x = this.mem[i];
	    this.a = (this.a ^ this.a >>> 16) + this.mem[j++];
	    this.mem[i] = y = this.mem[x >> 2 & 255] + this.a + this.b;
	    this.results[i++] = this.b = this.mem[y >> 10 & 255] + x;
	}
	for (j = 0; j < 128;) {
	    x = this.mem[i];
	    this.a = (this.a ^ this.a << 13) + this.mem[j++];
	    this.mem[i] = y = this.mem[x >> 2 & 255] + this.a + this.b;
	    this.results[i++] = this.b = this.mem[y >> 10 & 255] + x;
	    x = this.mem[i];
	    this.a = (this.a ^ this.a >>> 6) + this.mem[j++];
	    this.mem[i] = y = this.mem[x >> 2 & 255] + this.a + this.b;
	    this.results[i++] = this.b = this.mem[y >> 10 & 255] + x;
	    x = this.mem[i];
	    this.a = (this.a ^ this.a << 2) + this.mem[j++];
	    this.mem[i] = y = this.mem[x >> 2 & 255] + this.a + this.b;
	    this.results[i++] = this.b = this.mem[y >> 10 & 255] + x;
	    x = this.mem[i];
	    this.a = (this.a ^ this.a >>> 16) + this.mem[j++];
	    this.mem[i] = y = this.mem[x >> 2 & 255] + this.a + this.b;
	    this.results[i++] = this.b = this.mem[y >> 10 & 255] + x;
	}
    }

    /**
     * Initializes this IsaacRNG; typically used from the constructor but can be
     * called externally.
     *
     * @param flag if true, use data from seed; if false, initializes this to
     *             unseeded random state
     */
    public final void init(final boolean flag) {
	int i;
	int a, b, c, d, e, f, g, h;
	a = b = c = d = e = f = g = h = 0x9e3779b9; /* the golden ratio */
	for (i = 0; i < 4; ++i) {
	    a ^= b << 11;
	    d += a;
	    b += c;
	    b ^= c >>> 2;
	    e += b;
	    c += d;
	    c ^= d << 8;
	    f += c;
	    d += e;
	    d ^= e >>> 16;
	    g += d;
	    e += f;
	    e ^= f << 10;
	    h += e;
	    f += g;
	    f ^= g >>> 4;
	    a += f;
	    g += h;
	    g ^= h << 8;
	    b += g;
	    h += a;
	    h ^= a >>> 9;
	    c += h;
	    a += b;
	}
	for (i = 0; i < 256; i += 8) { /* fill in mem[] with messy stuff */
	    if (flag) {
		a += this.results[i];
		b += this.results[i + 1];
		c += this.results[i + 2];
		d += this.results[i + 3];
		e += this.results[i + 4];
		f += this.results[i + 5];
		g += this.results[i + 6];
		h += this.results[i + 7];
	    }
	    a ^= b << 11;
	    d += a;
	    b += c;
	    b ^= c >>> 2;
	    e += b;
	    c += d;
	    c ^= d << 8;
	    f += c;
	    d += e;
	    d ^= e >>> 16;
	    g += d;
	    e += f;
	    e ^= f << 10;
	    h += e;
	    f += g;
	    f ^= g >>> 4;
	    a += f;
	    g += h;
	    g ^= h << 8;
	    b += g;
	    h += a;
	    h ^= a >>> 9;
	    c += h;
	    a += b;
	    this.mem[i] = a;
	    this.mem[i + 1] = b;
	    this.mem[i + 2] = c;
	    this.mem[i + 3] = d;
	    this.mem[i + 4] = e;
	    this.mem[i + 5] = f;
	    this.mem[i + 6] = g;
	    this.mem[i + 7] = h;
	}
	if (flag) { /* second pass makes all of seed affect all of mem */
	    for (i = 0; i < 256; i += 8) {
		a += this.mem[i];
		b += this.mem[i + 1];
		c += this.mem[i + 2];
		d += this.mem[i + 3];
		e += this.mem[i + 4];
		f += this.mem[i + 5];
		g += this.mem[i + 6];
		h += this.mem[i + 7];
		a ^= b << 11;
		d += a;
		b += c;
		b ^= c >>> 2;
		e += b;
		c += d;
		c ^= d << 8;
		f += c;
		d += e;
		d ^= e >>> 16;
		g += d;
		e += f;
		e ^= f << 10;
		h += e;
		f += g;
		f ^= g >>> 4;
		a += f;
		g += h;
		g ^= h << 8;
		b += g;
		h += a;
		h ^= a >>> 9;
		c += h;
		a += b;
		this.mem[i] = a;
		this.mem[i + 1] = b;
		this.mem[i + 2] = c;
		this.mem[i + 3] = d;
		this.mem[i + 4] = e;
		this.mem[i + 5] = f;
		this.mem[i + 6] = g;
		this.mem[i + 7] = h;
	    }
	}
	this.regen();
	this.count = 256;
    }

    public final int nextInt() {
	if (0 == this.count--) {
	    this.regen();
	    this.count = 255;
	}
	return this.results[this.count];
    }

    /**
     * Generates and returns a block of 256 pseudo-random int values.
     *
     * @return a freshly-allocated array of 256 pseudo-random ints, with all bits
     *         possible
     */
    public final int[] nextBlock() {
	this.regen();
	final int[] block = new int[256];
	System.arraycopy(this.results, 0, block, 0, 256);
	this.count = 0;
	return block;
    }

    /**
     * Generates enough pseudo-random int values to fill {@code data} and assigns
     * them to it.
     */
    public final void fillBlock(final int[] data) {
	int len, i;
	if (data == null || (len = data.length) == 0) {
	    return;
	}
	for (i = 0; len > 256; i += 256, len -= 256) {
	    this.regen();
	    System.arraycopy(this.results, 0, data, i, 256);
	}
	this.regen();
	System.arraycopy(this.results, 0, data, i, len);
	this.count = len & 255;
    }

    /**
     * Generates enough pseudo-random float values between 0f and 1f to fill
     * {@code data} and assigns them to it. Inclusive on 0f, exclusive on 1f.
     * Intended for cases where you need some large source of randomness to be
     * checked later repeatedly, such as how permutation tables are used in Simplex
     * noise.
     */
    public final void fillFloats(final float[] data) {
	int len, n;
	if (data == null || (len = data.length) == 0) {
	    return;
	}
	for (int i = 0; i < len; i++) {
	    data[i] = NumberTools.intBitsToFloat((n = this.nextInt()) >>> 9 ^ n & 0x7fffff | 0x3f800000) - 1f;
	}
    }

    /**
     * Generates enough pseudo-random float values between -1f and 1f to fill
     * {@code data} and assigns them to it. Inclusive on -1f, exclusive on 1f.
     * Intended for cases where you need some large source of randomness to be
     * checked later repeatedly, such as how permutation tables are used in Simplex
     * noise.
     */
    public final void fillSignedFloats(final float[] data) {
	int len, n;
	if (data == null || (len = data.length) == 0) {
	    return;
	}
	for (int i = 0; i < len; i++) {
	    data[i] = NumberTools.intBitsToFloat((n = this.nextInt()) >>> 9 ^ n & 0x7fffff | 0x40000000) - 3f;
	}
    }

    @Override
    public final int next(final int bits) {
	return this.nextInt() >>> 32 - bits;
    }

    /**
     * Using this method, any algorithm that needs to efficiently generate more than
     * 32 bits of random data can interface with this randomness source.
     * <p>
     * Get a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive).
     *
     * @return a random long between Long.MIN_VALUE and Long.MAX_VALUE (both
     *         inclusive)
     */
    @Override
    public long nextLong() {
	return this.nextInt() & 0xffffffffL | (this.nextInt() & 0xffffffffL) << 32;
    }

    /**
     * Produces another RandomnessSource, but the new one will not produce the same
     * data as this one. This is meant to be a "more-secure" generator, so this
     * helps reduce the ability to guess future results from a given sequence of
     * output.
     *
     * @return another RandomnessSource with the same implementation but no
     *         guarantees as to generation
     */
    @Override
    public RandomnessSource copy() {
	return new Isaac32RNG(this.results);
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final Isaac32RNG isaac32RNG = (Isaac32RNG) o;
	if (this.count != isaac32RNG.count) {
	    return false;
	}
	if (this.a != isaac32RNG.a) {
	    return false;
	}
	if (this.b != isaac32RNG.b) {
	    return false;
	}
	if (this.c != isaac32RNG.c) {
	    return false;
	}
	if (!Arrays.equals(this.results, isaac32RNG.results)) {
	    return false;
	}
	return Arrays.equals(this.mem, isaac32RNG.mem);
    }

    @Override
    public int hashCode() {
	int result = this.count;
	result = 31 * result + CrossHash.Wisp.hash(this.results);
	result = 31 * result + CrossHash.Wisp.hash(this.mem);
	result = 31 * result + this.a;
	result = 31 * result + this.b;
	result = 31 * result + this.c;
	return result;
    }

    @Override
    public String toString() {
	return "Isaac32RNG with a hidden state (id is " + System.identityHashCode(this) + ')';
    }
}