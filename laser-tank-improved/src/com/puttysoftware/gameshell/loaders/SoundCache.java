package com.puttysoftware.gameshell.loaders;

import java.util.ArrayList;

import com.puttysoftware.sound.SoundFactory;

final class SoundCache {
    // Fields
    private ArrayList<SoundCacheEntry> cache;
    private static final int INITIAL_SIZE = 10;

    // Constructor
    SoundCache() {
	this.cache = new ArrayList<>(SoundCache.INITIAL_SIZE);
    }

    // Methods
    SoundFactory getCachedSound(final String name) {
	for (SoundCacheEntry mce : this.cache) {
	    if (mce.getName().equals(name)) {
		return mce.getSound();
	    }
	}
	return null;
    }

    void addToCache(final String name, final SoundFactory sound) {
	this.cache.add(new SoundCacheEntry(sound, name));
    }

    boolean isInCache(final String name) {
	for (SoundCacheEntry mce : this.cache) {
	    if (mce.getName().equals(name)) {
		return true;
	    }
	}
	return false;
    }
}