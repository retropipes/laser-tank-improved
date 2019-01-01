package squidpony.squidmath;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A type of RNG that can generate values larger or smaller than the normal
 * maximum or minimum, based on a modifier. You should not use this as a
 * general-purpose substitute for {@link RNG}; it is meant for cases where there
 * is no hard maximum or minimum for a random value, so it is a poor fit for
 * getting random items from collections or shuffling. It also uses a curved
 * distribution (almost Gaussian, but slightly more shallow), which makes its
 * results to be most often near the center of the range they can fall into. The
 * {@link #luck} field affects the distribution simply, and should generally be
 * between -0.5f and 0.5f except in cases where a character or event routinely
 * defies all odds. There is no value for luck that will prevent this from
 * sometimes producing values outside the requested range, though at luck = 0 it
 * is somewhat rare for the bounds to be significantly exceeded. <br>
 * The name comes from "critical hit," the rare but potentially very significant
 * strike in many role-playing games. <br>
 * Created by Tommy Ettinger on 9/20/2017.
 */
public class CriticalRNG extends RNG {
    /**
     * Positive for higher results, negative for lower results; usually this is
     * small, between -0.5f and 0.5f .
     */
    public float luck = 0f;

    /**
     * Makes a CriticalRNG with a luck factor of 0 and a randomly-seeded ThrustRNG
     * for its RandomnessSource.
     */
    public CriticalRNG() {
	super(new ThrustRNG());
    }

    /**
     * Makes a CriticalRNG with a luck factor of 0 and a ThrustRNG with the given
     * seed for its RandomnessSource.
     *
     * @param seed any long
     */
    public CriticalRNG(final long seed) {
	super(new ThrustRNG(seed));
    }

    /**
     * Makes a CriticalRNG with a luck factor of 0 and a ThrustRNG with the given
     * seed for its RandomnessSource (this will hash seedString using
     * {@link CrossHash#hash64(CharSequence)} and use the result to seed the
     * ThrustRNG).
     *
     * @param seedString any String
     */
    public CriticalRNG(final CharSequence seedString) {
	super(new ThrustRNG(CrossHash.hash64(seedString)));
    }

    /**
     * Makes a CriticalRNG with a luck factor of 0 and the given RandomnessSource.
     *
     * @param random a RandomnessSource, such as a {@link LongPeriodRNG} or
     *               {@link LightRNG}
     */
    public CriticalRNG(final RandomnessSource random) {
	super(random);
    }

    /**
     * Makes a CriticalRNG with the given luck factor and a randomly-seeded
     * ThrustRNG for its RandomnessSource.
     *
     * @param luck typically a small float, often between -0.5f and 0.5f, that will
     *             affect the results this returns
     */
    public CriticalRNG(final float luck) {
	super(new ThrustRNG());
	this.luck = luck;
    }

    /**
     * Makes a CriticalRNG with the given luck factor and a ThrustRNG with the given
     * seed for its RandomnessSource.
     *
     * @param seed any long
     * @param luck typically a small float, often between -0.5f and 0.5f, that will
     *             affect the results this returns
     */
    public CriticalRNG(final long seed, final float luck) {
	super(new ThrustRNG(seed));
	this.luck = luck;
    }

    /**
     * Makes a CriticalRNG with a luck factor of 0 and a ThrustRNG with the given
     * seed for its RandomnessSource (this will hash seedString using
     * {@link CrossHash#hash64(CharSequence)} and use the result to seed the
     * ThrustRNG).
     *
     * @param seedString any String
     * @param luck       typically a small float, often between -0.5f and 0.5f, that
     *                   will affect the results this returns
     */
    public CriticalRNG(final CharSequence seedString, final float luck) {
	super(new ThrustRNG(CrossHash.hash64(seedString)));
	this.luck = luck;
    }

    /**
     * Makes a CriticalRNG with a luck factor of 0 and the given RandomnessSource.
     *
     * @param random a RandomnessSource, such as a {@link LongPeriodRNG} or
     *               {@link LightRNG}
     * @param luck   typically a small float, often between -0.5f and 0.5f, that
     *               will affect the results this returns
     */
    public CriticalRNG(final RandomnessSource random, final float luck) {
	super(random);
	this.luck = luck;
    }

    @Override
    public double nextDouble() {
	return NumberTools.formCurvedFloat(this.random.nextLong()) * 0.875 + 0.5 + this.luck;
    }

    @Override
    public double nextDouble(final double max) {
	return (NumberTools.formCurvedFloat(this.random.nextLong()) * 0.875 + 0.5 + this.luck) * max;
    }

    @Override
    public float nextFloat() {
	return NumberTools.formCurvedFloat(this.random.nextLong()) * 0.875f + 0.5f + this.luck;
    }

    @Override
    public boolean nextBoolean() {
	return NumberTools.formCurvedFloat(this.random.nextLong()) * 0.875f + 0.5f + this.luck >= 0f;
    }

    private static int intify(final double t) {
	return t >= 0.0 ? (int) (t + 0.5) : (int) (t - 0.5);
    }

    private static long longify(final double t) {
	return t >= 0.0 ? (long) (t + 0.5) : (long) (t - 0.5);
    }

    @Override
    public long nextLong() {
	return CriticalRNG.longify(
		(NumberTools.formCurvedFloat(this.random.nextLong()) + this.luck * -2.0) * 0x8000000000000000L);
    }

    @Override
    public long nextLong(final long bound) {
	return CriticalRNG
		.longify((NumberTools.formCurvedFloat(this.random.nextLong()) * 0.875 + 0.5 + this.luck) * bound);
    }

    @Override
    public int nextInt(final int bound) {
	return CriticalRNG
		.intify((NumberTools.formCurvedFloat(this.random.nextLong()) * 0.875 + 0.5 + this.luck) * bound);
    }

    @Override
    public int nextIntHasty(final int bound) {
	return CriticalRNG
		.intify((NumberTools.formCurvedFloat(this.random.nextLong()) * 0.875 + 0.5 + this.luck) * bound);
    }

    @Override
    public int nextInt() {
	return CriticalRNG
		.intify((NumberTools.formCurvedFloat(this.random.nextLong()) + this.luck * -2.0) * 0x80000000);
    }

    @Override
    public <T> T getRandomElement(final T[] array) {
	if (array.length < 1) {
	    return null;
	}
	return array[super.nextIntHasty(array.length)];
    }

    @Override
    public <T> T getRandomElement(final List<T> list) {
	if (list.isEmpty()) {
	    return null;
	}
	return list.get(super.nextIntHasty(list.size()));
    }

    @Override
    public short getRandomElement(final ShortSet set) {
	if (set.size <= 0) {
	    throw new UnsupportedOperationException("ShortSet cannot be empty when getting a random element");
	}
	int n = super.nextIntHasty(set.size);
	short s = 0;
	final ShortSet.ShortSetIterator ssi = set.iterator();
	while (n-- >= 0 && ssi.hasNext) {
	    s = ssi.next();
	}
	ssi.reset();
	return s;
    }

    @Override
    public <T> T getRandomElement(final Collection<T> coll) {
	int n;
	if ((n = coll.size()) <= 0) {
	    return null;
	}
	n = super.nextIntHasty(n);
	T t = null;
	final Iterator<T> it = coll.iterator();
	while (n-- >= 0 && it.hasNext()) {
	    t = it.next();
	}
	return t;
    }

    @Override
    public double nextGaussian() {
	return NumberTools.formCurvedFloat(this.random.nextLong()) * 1.75 + this.luck * 2;
    }
}
