package com.puttysoftware.lasertank;

import java.util.Random;

public class IDGenerator {
    // Field
    private static Random rng;

    // Method
    public static String getRandomIDString(final int radix) {
	if (IDGenerator.rng == null) {
	    IDGenerator.rng = new Random();
	}
	return Long.toString(IDGenerator.rng.nextLong(), radix);
    }

    // Constructor
    private IDGenerator() {
	// Do nothing
    }
}
