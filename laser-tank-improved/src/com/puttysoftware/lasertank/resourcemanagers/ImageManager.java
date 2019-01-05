/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.resourcemanagers;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import com.puttysoftware.images.BufferedImageIcon;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractTunnel;
import com.puttysoftware.lasertank.arena.objects.Tunnel;
import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;
import com.puttysoftware.lasertank.utilities.ColorConstants;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public class ImageManager {
    public static final int MAX_WINDOW_SIZE = 700;
    private static final Color TRANSPARENT = new Color(200, 100, 100);
    private static final String DEFAULT_LOAD_PATH = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_GRAPHICS_PATH);
    private static String LOAD_PATH = ImageManager.DEFAULT_LOAD_PATH;
    private static Class<?> LOAD_CLASS = ImageManager.class;
    private static Font DRAW_FONT = null;
    private static final String DRAW_FONT_FALLBACK = "Times-BOLD-14";
    private static final int DRAW_HORZ = 10;
    private static final int DRAW_VERT = 22;
    private static final float DRAW_SIZE = 14;

    public static void activeLanguageChanged() {
	ImageCache.flushCache();
    }

    public static BufferedImageIcon getCompositeImage(final AbstractArenaObject obj1, final AbstractArenaObject obj2,
	    final boolean useText) {
	final BufferedImageIcon icon1 = ImageManager.getImage(obj1, useText);
	final BufferedImageIcon icon2 = ImageManager.getImage(obj2, useText);
	return ImageManager.getCompositeImageDirectly(icon1, icon2);
    }

    private static BufferedImageIcon getCompositeImageDirectly(final BufferedImageIcon icon1,
	    final BufferedImageIcon icon2) {
	try {
	    final BufferedImageIcon result = new BufferedImageIcon(icon1);
	    if (icon1 != null && icon2 != null) {
		final Graphics2D g2 = result.createGraphics();
		g2.drawImage(icon2, 0, 0, null);
		return result;
	    } else {
		return null;
	    }
	} catch (final NullPointerException np) {
	    return null;
	} catch (final IllegalArgumentException ia) {
	    return null;
	}
    }

    public static int getGraphicSize() {
	return 32;
    }

    public static BufferedImageIcon getImage(final AbstractArenaObject obj, final boolean useText) {
	if (obj instanceof AbstractTunnel) {
	    return ImageManager.getTransformedTunnel(obj.getColor(), useText);
	} else {
	    return ImageCache.getCachedImage(obj, useText);
	}
    }

    private static BufferedImageIcon getTransformedTunnel(final int cc, final boolean useText) {
	try {
	    final BufferedImageIcon icon = ImageCache.getCachedImage(new Tunnel(), useText);
	    Color color;
	    if (cc == ColorConstants.COLOR_BLUE) {
		color = Color.blue;
	    } else if (cc == ColorConstants.COLOR_CYAN) {
		color = Color.cyan;
	    } else if (cc == ColorConstants.COLOR_GREEN) {
		color = Color.green;
	    } else if (cc == ColorConstants.COLOR_MAGENTA) {
		color = Color.magenta;
	    } else if (cc == ColorConstants.COLOR_RED) {
		color = Color.red;
	    } else if (cc == ColorConstants.COLOR_WHITE) {
		color = Color.white;
	    } else if (cc == ColorConstants.COLOR_YELLOW) {
		color = Color.yellow;
	    } else {
		color = Color.gray;
	    }
	    if (icon != null) {
		final BufferedImageIcon result = new BufferedImageIcon(icon);
		for (int x = 0; x < ImageManager.getGraphicSize(); x++) {
		    for (int y = 0; y < ImageManager.getGraphicSize(); y++) {
			final int pixel = icon.getRGB(x, y);
			final Color c = new Color(pixel);
			if (c.equals(ImageManager.TRANSPARENT)) {
			    result.setRGB(x, y, color.getRGB());
			}
		    }
		}
		return result;
	    } else {
		return null;
	    }
	} catch (final NullPointerException np) {
	    return null;
	} catch (final IllegalArgumentException ia) {
	    return null;
	}
    }

    static BufferedImageIcon getUncachedImage(final AbstractArenaObject obj, final boolean useText) {
	try {
	    String name, extraPath;
	    if (obj.isOfType(TypeConstants.TYPE_TUNNEL)) {
		name = obj.getBaseImageName();
	    } else {
		name = obj.getImageName();
	    }
	    if (obj.isEnabled()) {
		extraPath = "enabled/";
	    } else {
		extraPath = "disabled/";
	    }
	    final String normalName = ImageManager.normalizeName(name);
	    final URL url = ImageManager.LOAD_CLASS.getResource(ImageManager.LOAD_PATH
		    + StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			    StringConstants.NOTL_STRING_OBJECTS_SUBPATH)
		    + extraPath + normalName + StringConstants.COMMON_STRING_NOTL_IMAGE_EXTENSION_PNG);
	    final BufferedImage image = ImageIO.read(url);
	    final String customText = obj.getCustomText();
	    if (useText && customText != null) {
		if (ImageManager.DRAW_FONT == null) {
		    try (InputStream is = ImageManager.class.getResourceAsStream(StringLoader
			    .loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_FONT_PATH)
			    + StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
				    StringConstants.NOTL_STRING_FONT_FILENAME))) {
			final Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
			ImageManager.DRAW_FONT = baseFont.deriveFont(ImageManager.DRAW_SIZE);
		    } catch (final Exception ex) {
			ImageManager.DRAW_FONT = Font.decode(ImageManager.DRAW_FONT_FALLBACK);
		    }
		}
		final Graphics2D g2 = image.createGraphics();
		g2.setFont(ImageManager.DRAW_FONT);
		g2.setColor(obj.getCustomTextColor());
		g2.drawString(customText, ImageManager.DRAW_HORZ, ImageManager.DRAW_VERT);
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

    public static BufferedImageIcon getVirtualCompositeImage(final AbstractArenaObject obj1,
	    final AbstractArenaObject obj2, final AbstractArenaObject... otherObjs) {
	BufferedImageIcon result = ImageManager.getCompositeImage(obj1, obj2, true);
	for (final AbstractArenaObject otherObj : otherObjs) {
	    final BufferedImageIcon img = ImageManager.getImage(otherObj, true);
	    result = ImageManager.getCompositeImageDirectly(result, img);
	}
	return result;
    }

    private static String normalizeName(final String name) {
	final StringBuilder sb = new StringBuilder(name);
	for (int x = 0; x < sb.length(); x++) {
	    if (!Character.isLetter(sb.charAt(x)) && !Character.isDigit(sb.charAt(x))) {
		sb.setCharAt(x, '_');
	    }
	}
	return sb.toString().toLowerCase();
    }

    private ImageManager() {
	// Do nothing
    }
}
