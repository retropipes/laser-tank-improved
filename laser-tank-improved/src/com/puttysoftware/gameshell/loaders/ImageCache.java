package com.puttysoftware.gameshell.loaders;

import java.util.ArrayList;

import com.puttysoftware.images.BufferedImageIcon;

final class ImageCache {
    // Fields
    private ArrayList<ImageCacheEntry> cache;
    private static final int INITIAL_SIZE = 10;

    // Constructor
    ImageCache() {
	this.cache = new ArrayList<>(ImageCache.INITIAL_SIZE);
    }

    // Methods
    BufferedImageIcon getCachedImage(final String name) {
	for (ImageCacheEntry ice : this.cache) {
	    if (ice.getName().equals(name)) {
		return ice.getImage();
	    }
	}
	return null;
    }

    void addToCache(final String name, final BufferedImageIcon image) {
	this.cache.add(new ImageCacheEntry(image, name));
    }

    boolean isInCache(final String name) {
	for (ImageCacheEntry ice : this.cache) {
	    if (ice.getName().equals(name)) {
		return true;
	    }
	}
	return false;
    }
}