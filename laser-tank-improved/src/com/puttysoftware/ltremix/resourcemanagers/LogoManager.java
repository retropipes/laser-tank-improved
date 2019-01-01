/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.resourcemanagers;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.puttysoftware.lasertank.improved.images.BufferedImageIcon;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;

public class LogoManager {
    private static final String DEFAULT_LOAD_PATH = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_GRAPHICS_PATH);
    private static Class<?> LOAD_CLASS = LogoManager.class;

    private LogoManager() {
	// Do nothing
    }

    static BufferedImageIcon getUncachedLogo(final String name) {
	try {
	    final URL url = LogoManager.LOAD_CLASS.getResource(LogoManager.DEFAULT_LOAD_PATH
		    + StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			    StringConstants.NOTL_STRING_LOGO_SUBPATH)
		    + name + StringConstants.COMMON_STRING_NOTL_IMAGE_EXTENSION_PNG);
	    final BufferedImage image = ImageIO.read(url);
	    return new BufferedImageIcon(image);
	} catch (final IOException ie) {
	    return null;
	} catch (final NullPointerException np) {
	    return null;
	} catch (final IllegalArgumentException ia) {
	    return null;
	}
    }

    public static BufferedImageIcon getLogo() {
	return LogoCache.getCachedLogo(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_LOGO));
    }

    public static BufferedImageIcon getMiniatureLogo() {
	return LogoCache.getCachedLogo(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_MINILOGO));
    }

    public static BufferedImageIcon getMicroLogo() {
	return LogoCache.getCachedLogo(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_MICROLOGO));
    }

    public static Image getIconLogo() {
	return LogoCache.getCachedLogo(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_MINILOGO));
    }
}
