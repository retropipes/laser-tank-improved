package com.puttysoftware.lasertank.improved.shell.loaders;

import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.puttysoftware.lasertank.improved.images.BufferedImageIcon;

public final class ImageLoader {
    public static int getGraphicSize() {
	return 64;
    }

    // Fields
    private final String loadPath;
    private final Class<?> loadBase;
    private final ImageCache imageCache;

    // Constructors
    public ImageLoader(final String path, final Class<?> base) {
	this.loadPath = path;
	this.loadBase = base;
	this.imageCache = new ImageCache();
    }

    public BufferedImageIcon getImage(final String name) {
	// Try and get it from the cache
	final BufferedImageIcon cachedImage = this.imageCache.getCachedImage(name);
	if (cachedImage != null) {
	    // Cache hit
	    return cachedImage;
	} else {
	    // Cache miss
	    try {
		final URL url = this.loadBase.getResource(this.loadPath + name);
		return new BufferedImageIcon(ImageIO.read(url));
	    } catch (final IOException e) {
		throw new LoaderException(e);
	    }
	}
    }
}
