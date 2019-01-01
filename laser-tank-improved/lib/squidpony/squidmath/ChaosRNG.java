package squidpony.squidmath;

import java.security.SecureRandom;

import squidpony.annotation.GwtIncompatible;

/**
 * An RNG that cannot be seeded and should be fairly hard to predict what it
 * will return next. Useful for competitions where a seeded RNG is used for
 * dungeon generation and enemy placement but an unpredictable RNG is needed for
 * combat, so players can't abuse the RNG to make improbable events guaranteed
 * or unfavorable outcomes impossible. The performance of this as a
 * RandomnessSource is also fairly good, taking approximately 1.5x to 1.7x as
 * long as LightRNG to produce random 64-bit data, and of course it is far
 * faster than java.util.Random (which is 10x slower than this). In the secure
 * random numbers category, where this isn't quite as secure as most, ChaosRNG
 * is about 80x faster than SecureRandom once SecureRandom warms up, which takes
 * about 10 minutes of continuous number generation. Before that, ChaosRNG is
 * about 110x faster than SecureRandom for 64-bit data. <br>
 * This is intended to be used as a RandomnessSource for an RNG, and does not
 * have any methods other than those needed for that interface, with one
 * exception -- the randomize() method, which can be used to completely change
 * all 1024 bits of state using cryptographic random numbers. If you create a
 * ChaosRNG and keep it around for later, then you can pass it to the RNG
 * constructor and later call randomize() on the ChaosRNG if you suspect it may
 * be becoming predictable. The period on this RNG is (2 to the 1024) - 1, so
 * predicting it may be essentially impossible unless the user can poke around
 * in the application, use reflection, etc. Created by Tommy Ettinger on
 * 3/17/2016.
 */
@GwtIncompatible
public class ChaosRNG implements RandomnessSource {
    private transient long[] state = new long[16];
    private transient int choice;
    private transient SecureRandom sec;
    private static final long serialVersionUID = -254415589291474491L;

    /**
     * Builds a ChaosRNG with a cryptographically-random seed. Future random
     * generation uses less secure methods but should still make it extremely
     * difficult to "divine" the future RNG results.
     */
    public ChaosRNG() {
	this.sec = new SecureRandom();
	final byte[] bytes = new byte[128];
	this.sec.nextBytes(bytes);
	for (int i = this.sec.nextInt() & 127, c = 0; c < 128; c++, i = i + 1 & 127) {
	    this.state[i & 15] |= bytes[c] << (i >> 4 << 3);
	}
	this.choice = this.sec.nextInt(16);
    }

    @Override
    public int next(final int bits) {
	return (int) (this.nextLong() & (1L << bits) - 1);
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a
     * 64-bit signed integer.
     *
     * @return any long, all 64 bits are random
     */
    @Override
    public long nextLong() {
	final long s0 = this.state[this.choice];
	long s1 = this.state[this.choice = this.choice + 1 & 15];
	s1 ^= s1 << 31; // a
	this.state[this.choice] = s1 ^ s0 ^ s1 >>> 11 ^ s0 >>> 30; // b,c
	return this.state[this.choice] * 1181783497276652981L;
    }

    /**
     * Produces another ChaosRNG with no relation to this one; this breaks the
     * normal rules that RandomnessSource.copy abides by because this class should
     * never have its generated number sequence be predictable.
     *
     * @return a new, unrelated ChaosRNG as a RandomnessSource
     */
    @Override
    public RandomnessSource copy() {
	return new ChaosRNG();
    }

    /**
     * Changes the internal state to a new, fully-random version that should have no
     * relation to the previous state. May be somewhat slow; you shouldn't need to
     * call this often.
     */
    public void randomize() {
	final byte[] bytes = this.sec.generateSeed(128);
	for (int i = this.sec.nextInt() & 127, c = 0; c < 128; c++, i = i + 1 & 127) {
	    this.state[i & 15] |= bytes[c] << (i >> 4 << 3);
	}
	this.choice = this.sec.nextInt(16);
    }

    @Override
    public String toString() {
	return "ChaosRNG with hidden state (id is " + System.identityHashCode(this) + ')';
    }
}
