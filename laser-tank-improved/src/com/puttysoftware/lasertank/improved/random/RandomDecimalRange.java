package com.puttysoftware.lasertank.improved.random;

public class RandomDecimalRange {
    // Fields
    private static double minimum;
    private static double maximum;

    // Constructor
    private RandomDecimalRange() {
	// Do nothing
    }

    // Methods
    public static void setMinimum(final double newMin) {
	RandomDecimalRange.minimum = newMin;
    }

    public static void setMaximum(final double newMax) {
	RandomDecimalRange.maximum = newMax;
    }

    public static float generateFloat() {
	return (float) RandomDecimalRange.generateDouble();
    }

    public static double generateDouble() {
	return Math.abs(RandomnessSource.nextDouble() % (RandomDecimalRange.maximum - RandomDecimalRange.minimum + 1))
		+ RandomDecimalRange.minimum;
    }

    public static double generateRawDouble() {
	return RandomnessSource.nextDouble();
    }
}
