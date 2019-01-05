package com.puttysoftware.lasertank.improved;

import java.util.Random;

public class IDGenerator {
    // Field
    private static Random rng;

    // Constructor
    private IDGenerator() {
	// Do nothing
    }

    // Method
    public static String getRandomIDString(final int radix) {
	if (IDGenerator.rng == null) {
	    IDGenerator.rng = new Random();
	}
	return Long.toString(IDGenerator.rng.nextLong(), radix);
    }
}
