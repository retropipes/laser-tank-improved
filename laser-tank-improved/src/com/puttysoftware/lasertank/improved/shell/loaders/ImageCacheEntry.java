package com.puttysoftware.lasertank.improved.shell.loaders;

import com.puttysoftware.lasertank.improved.images.BufferedImageIcon;

final class ImageCacheEntry {
    // Fields
    private final BufferedImageIcon image;
    private final String name;

    // Constructor
    ImageCacheEntry(final BufferedImageIcon newImage, final String newName) {
	this.image = newImage;
	this.name = newName;
    }

    // Methods
    BufferedImageIcon getImage() {
	return this.image;
    }

    String getName() {
	return this.name;
    }
}