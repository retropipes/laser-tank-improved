/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.resourcemanagers;

import com.puttysoftware.lasertank.improved.images.BufferedImageIcon;

class LogoCache {
    // Fields
    private static LogoCacheEntry[] cache;
    private final static int CACHE_INCREMENT = 20;
    private static int CACHE_SIZE = 0;

    // Constructor
    private LogoCache() {
	// Do nothing
    }

    // Methods
    static BufferedImageIcon getCachedLogo(final String name, final boolean drawing) {
	if (!LogoCache.isInCache(name)) {
	    final BufferedImageIcon bii = LogoManager.getUncachedLogo(name, drawing);
	    LogoCache.addToCache(name, bii);
	}
	for (final LogoCacheEntry element : LogoCache.cache) {
	    if (name.equals(element.getNameEntry())) {
		return element.getEntry();
	    }
	}
	return null;
    }

    private static void expandCache() {
	final LogoCacheEntry[] tempCache = new LogoCacheEntry[LogoCache.cache.length + LogoCache.CACHE_INCREMENT];
	for (int x = 0; x < LogoCache.CACHE_SIZE; x++) {
	    tempCache[x] = LogoCache.cache[x];
	}
	LogoCache.cache = tempCache;
    }

    private static void addToCache(final String name, final BufferedImageIcon bii) {
	if (LogoCache.cache == null) {
	    LogoCache.cache = new LogoCacheEntry[LogoCache.CACHE_INCREMENT];
	}
	if (LogoCache.CACHE_SIZE == LogoCache.cache.length) {
	    LogoCache.expandCache();
	}
	LogoCache.cache[LogoCache.CACHE_SIZE] = new LogoCacheEntry();
	LogoCache.cache[LogoCache.CACHE_SIZE].setEntry(bii);
	LogoCache.cache[LogoCache.CACHE_SIZE].setNameEntry(name);
	LogoCache.CACHE_SIZE++;
    }

    private static boolean isInCache(final String name) {
	if (LogoCache.cache == null) {
	    LogoCache.cache = new LogoCacheEntry[LogoCache.CACHE_INCREMENT];
	}
	for (int x = 0; x < LogoCache.CACHE_SIZE; x++) {
	    if (name.equals(LogoCache.cache[x].getNameEntry())) {
		return true;
	    }
	}
	return false;
    }
}