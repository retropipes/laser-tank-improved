/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.resourcemanagers;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import com.puttysoftware.images.BufferedImageIcon;
import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;

public class LogoManager {
    private static final String DEFAULT_LOAD_PATH = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_GRAPHICS_PATH);
    private static Class<?> LOAD_CLASS = LogoManager.class;
    private static Font LOGO_DRAW_FONT = null;
    private static final String LOGO_DRAW_FONT_FALLBACK = "Times-BOLD-12";
    private static final int LOGO_DRAW_HORZ = 98;
    private static final int LOGO_DRAW_HORZ_MAX = 8;
    private static final int LOGO_DRAW_HORZ_PCO = 4;
    private static final int LOGO_DRAW_VERT = 76;

    public static Image getIconLogo() {
	return LogoCache.getCachedLogo(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_ICONLOGO),
		false);
    }

    public static BufferedImageIcon getLogo() {
	return LogoCache.getCachedLogo(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_LOGO), true);
    }

    public static BufferedImageIcon getMicroLogo() {
	return LogoCache.getCachedLogo(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_MICROLOGO),
		false);
    }

    public static BufferedImageIcon getMiniatureLogo() {
	return LogoCache.getCachedLogo(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_MINILOGO),
		false);
    }

    static BufferedImageIcon getUncachedLogo(final String name, final boolean drawing) {
	try {
	    final URL url = LogoManager.LOAD_CLASS.getResource(LogoManager.DEFAULT_LOAD_PATH
		    + StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			    StringConstants.NOTL_STRING_LOGO_SUBPATH)
		    + name + StringConstants.COMMON_STRING_NOTL_IMAGE_EXTENSION_PNG);
	    final BufferedImage image = ImageIO.read(url);
	    if (drawing) {
		if (LogoManager.LOGO_DRAW_FONT == null) {
		    try (InputStream is = LogoManager.class.getResourceAsStream(StringLoader
			    .loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_FONT_PATH)
			    + StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
				    StringConstants.NOTL_STRING_FONT_FILENAME))) {
			final Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
			LogoManager.LOGO_DRAW_FONT = baseFont.deriveFont((float) 18);
		    } catch (final Exception ex) {
			LogoManager.LOGO_DRAW_FONT = Font.decode(LogoManager.LOGO_DRAW_FONT_FALLBACK);
		    }
		}
		final Graphics2D g2 = image.createGraphics();
		g2.setFont(LogoManager.LOGO_DRAW_FONT);
		g2.setColor(Color.yellow);
		final String logoVer = Application.getLogoVersionString();
		g2.drawString(logoVer,
			LogoManager.LOGO_DRAW_HORZ
				+ (LogoManager.LOGO_DRAW_HORZ_MAX - logoVer.length()) * LogoManager.LOGO_DRAW_HORZ_PCO,
			LogoManager.LOGO_DRAW_VERT);
	    }
	    return new BufferedImageIcon(image);
	} catch (final IOException ie) {
	    return null;
	} catch (final NullPointerException np) {
	    return null;
	} catch (final IllegalArgumentException ia) {
	    return null;
	}
    }

    private LogoManager() {
	// Do nothing
    }
}
