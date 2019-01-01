package squidpony.squidmath;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import squidpony.annotation.GwtIncompatible;

/**
 * RandomnessSource using Mersenne Twister algorithm (not recommended). <br>
 * Uses the Mersenne Twister algorithm to provide results with a longer period.
 * Mersenne Twister has known statistical vulnerabilities, however, and this
 * implementation is incredibly slow, which is why it is deprecated. You should
 * use {@link LongPeriodRNG} for most of the cases that MersenneTwister would be
 * good at in theory, or {@link IsaacRNG} for cases that need an extremely large
 * period and cryptographic-like properties. <br>
 *
 * @author Daniel Dyer (Java Port)
 * @author Makoto Matsumoto and Takuji Nishimura (original C version)
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Lewis Potter
 * @deprecated
 */
@GwtIncompatible /* Because of SecureRandom */
@Deprecated /*
	     * This code is really, really slow due to threading behavior, and should be
	     * avoided.
	     */
public class MersenneTwister implements RandomnessSource {
    // The actual seed size isn't that important, but it should be a multiple of 4.
    private static final int SEED_SIZE_BYTES = 16;
    // Magic numbers from original C version.
    private static final int N = 624;
    private static final int M = 397;
    private static final int[] MAG01 = { 0, 0x9908b0df };
    private static final int UPPER_MASK = 0x80000000;
    private static final int LOWER_MASK = 0x7fffffff;
    private static final int BOOTSTRAP_SEED = 19650218;
    private static final int BOOTSTRAP_FACTOR = 1812433253;
    private static final int SEED_FACTOR1 = 1664525;
    private static final int SEED_FACTOR2 = 1566083941;
    private static final int GENERATE_MASK1 = 0x9d2c5680;
    private static final int GENERATE_MASK2 = 0xefc60000;
    private final byte[] seed;
    // Lock to prevent concurrent modification of the RNG's internal state.
    private final ReentrantLock lock = new ReentrantLock();
    private final int[] mt = new int[MersenneTwister.N]; // State vector.
    private int mtIndex = 0; // Index into state vector.
    private static final int BITWISE_BYTE_TO_INT = 0x000000FF;
    private static final long serialVersionUID = 217351968847857679L;

    /**
     * Creates a new RNG and seeds it using the default seeding strategy.
     */
    public MersenneTwister() {
	this(new SecureRandom().generateSeed(MersenneTwister.SEED_SIZE_BYTES));
    }

    /**
     * Creates an RNG and seeds it with the specified seed data.
     *
     * @param seed The seed data used to initialize the RNG.
     */
    public MersenneTwister(final byte[] seed) {
	if (seed == null || seed.length != MersenneTwister.SEED_SIZE_BYTES) {
	    throw new IllegalArgumentException("Mersenne Twister RNG requires a 128-bit (16-byte) seed.");
	}
	this.seed = Arrays.copyOf(seed, seed.length);
	final int[] seedInts = MersenneTwister.convertBytesToInts(this.seed);
	// This section is translated from the init_genrand code in the C version.
	this.mt[0] = MersenneTwister.BOOTSTRAP_SEED;
	for (this.mtIndex = 1; this.mtIndex < MersenneTwister.N; this.mtIndex++) {
	    this.mt[this.mtIndex] = MersenneTwister.BOOTSTRAP_FACTOR
		    * (this.mt[this.mtIndex - 1] ^ this.mt[this.mtIndex - 1] >>> 30) + this.mtIndex;
	}
	// This section is translated from the init_by_array code in the C version.
	int i = 1;
	int j = 0;
	for (int k = Math.max(MersenneTwister.N, seedInts.length); k > 0; k--) {
	    this.mt[i] = (this.mt[i] ^ (this.mt[i - 1] ^ this.mt[i - 1] >>> 30) * MersenneTwister.SEED_FACTOR1)
		    + seedInts[j] + j;
	    i++;
	    j++;
	    if (i >= MersenneTwister.N) {
		this.mt[0] = this.mt[MersenneTwister.N - 1];
		i = 1;
	    }
	    if (j >= seedInts.length) {
		j = 0;
	    }
	}
	for (int k = MersenneTwister.N - 1; k > 0; k--) {
	    this.mt[i] = (this.mt[i] ^ (this.mt[i - 1] ^ this.mt[i - 1] >>> 30) * MersenneTwister.SEED_FACTOR2) - i;
	    i++;
	    if (i >= MersenneTwister.N) {
		this.mt[0] = this.mt[MersenneTwister.N - 1];
		i = 1;
	    }
	}
	this.mt[0] = MersenneTwister.UPPER_MASK; // Most significant bit is 1 - guarantees non-zero initial array.
    }

    /**
     * Take four bytes from the specified position in the specified block and
     * convert them into a 32-bit int, using the big-endian convention.
     *
     * @param bytes  The data to read from.
     * @param offset The position to start reading the 4-byte int from.
     * @return The 32-bit integer represented by the four bytes.
     */
    public static int convertBytesToInt(final byte[] bytes, final int offset) {
	return MersenneTwister.BITWISE_BYTE_TO_INT & bytes[offset + 3]
		| (MersenneTwister.BITWISE_BYTE_TO_INT & bytes[offset + 2]) << 8
		| (MersenneTwister.BITWISE_BYTE_TO_INT & bytes[offset + 1]) << 16
		| (MersenneTwister.BITWISE_BYTE_TO_INT & bytes[offset]) << 24;
    }

    /**
     * Convert an array of bytes into an array of ints. 4 bytes from the input data
     * map to a single int in the output data.
     *
     * @param bytes The data to read from.
     * @return An array of 32-bit integers constructed from the data.
     * @since 1.1
     */
    public static int[] convertBytesToInts(final byte[] bytes) {
	if (bytes.length % 4 != 0) {
	    throw new IllegalArgumentException("Number of input bytes must be a multiple of 4.");
	}
	final int[] ints = new int[bytes.length / 4];
	for (int i = 0; i < ints.length; i++) {
	    ints[i] = MersenneTwister.convertBytesToInt(bytes, i * 4);
	}
	return ints;
    }

    public byte[] getSeed() {
	return Arrays.copyOf(this.seed, this.seed.length);
    }

    @Override
    public final int next(final int bits) {
	int y;
	try {
	    this.lock.lock();
	    if (this.mtIndex >= MersenneTwister.N) // Generate N ints at a time.
	    {
		int kk;
		for (kk = 0; kk < MersenneTwister.N - MersenneTwister.M; kk++) {
		    y = this.mt[kk] & MersenneTwister.UPPER_MASK | this.mt[kk + 1] & MersenneTwister.LOWER_MASK;
		    this.mt[kk] = this.mt[kk + MersenneTwister.M] ^ y >>> 1 ^ MersenneTwister.MAG01[y & 0x1];
		}
		for (; kk < MersenneTwister.N - 1; kk++) {
		    y = this.mt[kk] & MersenneTwister.UPPER_MASK | this.mt[kk + 1] & MersenneTwister.LOWER_MASK;
		    this.mt[kk] = this.mt[kk + MersenneTwister.M - MersenneTwister.N] ^ y >>> 1
			    ^ MersenneTwister.MAG01[y & 0x1];
		}
		y = this.mt[MersenneTwister.N - 1] & MersenneTwister.UPPER_MASK
			| this.mt[0] & MersenneTwister.LOWER_MASK;
		this.mt[MersenneTwister.N - 1] = this.mt[MersenneTwister.M - 1] ^ y >>> 1
			^ MersenneTwister.MAG01[y & 0x1];
		this.mtIndex = 0;
	    }
	    y = this.mt[this.mtIndex++];
	} finally {
	    this.lock.unlock();
	}
	// Tempering
	y ^= y >>> 11;
	y ^= y << 7 & MersenneTwister.GENERATE_MASK1;
	y ^= y << 15 & MersenneTwister.GENERATE_MASK2;
	y ^= y >>> 18;
	return y >>> 32 - bits;
    }

    @Override
    public final long nextLong() {
	return (this.next(32) & 0xffffffffL) << 32 | this.next(32) & 0xffffffffL;
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong()
     * are called on this object and the copy, both will generate the same sequence
     * of random numbers from the point copy() was called. This just needs to copy
     * the state so it isn't shared, usually, and produce a new value with the same
     * exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public RandomnessSource copy() {
	final MersenneTwister next = new MersenneTwister(this.seed);
	System.arraycopy(this.mt, 0, next.mt, 0, this.mt.length);
	next.mtIndex = this.mtIndex;
	return next;
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final MersenneTwister that = (MersenneTwister) o;
	if (this.mtIndex != that.mtIndex) {
	    return false;
	}
	if (!Arrays.equals(this.seed, that.seed)) {
	    return false;
	}
	return Arrays.equals(this.mt, that.mt);
    }

    @Override
    public int hashCode() {
	int result = CrossHash.Lightning.hash(this.seed);
	result = 31 * result + CrossHash.Lightning.hash(this.mt);
	result = 31 * result + this.mtIndex;
	return result;
    }

    @Override
    public String toString() {
	return "MersenneTwister with hidden state (id is " + System.identityHashCode(this) + ')';
    }
}
