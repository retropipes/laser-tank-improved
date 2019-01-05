package com.puttysoftware.shell.loaders;

import java.util.ArrayList;

import com.puttysoftware.images.BufferedImageIcon;

final class ImageCache {
    private static final int INITIAL_SIZE = 10;
    // Fields
    private final ArrayList<ImageCacheEntry> cache;

    // Constructor
    ImageCache() {
	this.cache = new ArrayList<>(ImageCache.INITIAL_SIZE);
    }

    void addToCache(final String name, final BufferedImageIcon image) {
	this.cache.add(new ImageCacheEntry(image, name));
    }

    // Methods
    BufferedImageIcon getCachedImage(final String name) {
	for (final ImageCacheEntry ice : this.cache) {
	    if (ice.getName().equals(name)) {
		return ice.getImage();
	    }
	}
	return null;
    }

    boolean isInCache(final String name) {
	for (final ImageCacheEntry ice : this.cache) {
	    if (ice.getName().equals(name)) {
		return true;
	    }
	}
	return false;
    }
}