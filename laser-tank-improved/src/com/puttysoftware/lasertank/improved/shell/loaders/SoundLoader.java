package com.puttysoftware.lasertank.improved.shell.loaders;

import java.net.URL;

import com.puttysoftware.lasertank.improved.sound.SoundFactory;

public final class SoundLoader {
    public static int getGraphicSize() {
	return 64;
    }

    // Fields
    private final String loadPath;
    private final Class<?> loadBase;
    private final SoundCache soundCache;

    // Constructors
    public SoundLoader(final String path, final Class<?> base) {
	this.loadPath = path;
	this.loadBase = base;
	this.soundCache = new SoundCache();
    }

    public SoundFactory getSound(final String name) {
	// Try and get it from the cache
	final SoundFactory cachedSound = this.soundCache.getCachedSound(name);
	if (cachedSound != null) {
	    // Cache hit
	    return cachedSound;
	} else {
	    // Cache miss
	    final URL url = this.loadBase.getResource(this.loadPath + name);
	    return SoundFactory.loadResource(url);
	}
    }
}
