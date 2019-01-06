package com.puttysoftware.gameshell.loaders;

import com.puttysoftware.images.BufferedImageIcon;

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