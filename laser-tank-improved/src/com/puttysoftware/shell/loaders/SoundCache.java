package com.puttysoftware.shell.loaders;

import java.util.ArrayList;

import com.puttysoftware.sound.SoundFactory;

final class SoundCache {
    private static final int INITIAL_SIZE = 10;
    // Fields
    private final ArrayList<SoundCacheEntry> cache;

    // Constructor
    SoundCache() {
	this.cache = new ArrayList<>(SoundCache.INITIAL_SIZE);
    }

    void addToCache(final String name, final SoundFactory sound) {
	this.cache.add(new SoundCacheEntry(sound, name));
    }

    // Methods
    SoundFactory getCachedSound(final String name) {
	for (final SoundCacheEntry mce : this.cache) {
	    if (mce.getName().equals(name)) {
		return mce.getSound();
	    }
	}
	return null;
    }

    boolean isInCache(final String name) {
	for (final SoundCacheEntry mce : this.cache) {
	    if (mce.getName().equals(name)) {
		return true;
	    }
	}
	return false;
    }
}