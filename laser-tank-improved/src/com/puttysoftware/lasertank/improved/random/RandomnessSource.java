package com.puttysoftware.lasertank.improved.random;

import java.util.Random;

class RandomnessSource {
    // Fields
    private static Random SOURCE = null;

    // Constructor
    private RandomnessSource() {
	// Do nothing
    }

    // Methods
    private static Random getSource() {
	if (RandomnessSource.SOURCE == null) {
	    RandomnessSource.SOURCE = new Random();
	}
	return RandomnessSource.SOURCE;
    }

    static int nextInt() {
	return RandomnessSource.getSource().nextInt();
    }

    static long nextLong() {
	return RandomnessSource.getSource().nextLong();
    }

    static double nextDouble() {
	return RandomnessSource.getSource().nextDouble();
    }
}
