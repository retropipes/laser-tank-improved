package com.puttysoftware.gameshell.loaders;

import com.puttysoftware.sound.SoundFactory;

final class SoundCacheEntry {
    // Fields
    private final SoundFactory sound;
    private final String name;

    // Constructor
    SoundCacheEntry(final SoundFactory newSound, final String newName) {
	this.sound = newSound;
	this.name = newName;
    }

    // Methods
    SoundFactory getSound() {
	return this.sound;
    }

    String getName() {
	return this.name;
    }
}