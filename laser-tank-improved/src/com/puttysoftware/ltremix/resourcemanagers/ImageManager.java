/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.resourcemanagers;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import com.puttysoftware.lasertank.improved.images.BufferedImageIcon;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.objects.Tunnel;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;
import com.puttysoftware.ltremix.utilities.ColorConstants;
import com.puttysoftware.ltremix.utilities.EraConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class ImageManager {
    public static final int MAX_WINDOW_SIZE = 700;
    private static final Color TRANSPARENT = new Color(200, 100, 100);
    private static final String DEFAULT_LOAD_PATH = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_GRAPHICS_PATH);
    private static String LOAD_PATH = ImageManager.DEFAULT_LOAD_PATH;
    private static Class<?> LOAD_CLASS = ImageManager.class;
    private static Font DRAW_FONT = null;
    private static String DRAW_FONT_FALLBACK_SMALL = "Times-BOLD-7";
    private static String DRAW_FONT_FALLBACK_MEDIUM = "Times-BOLD-10.5";
    private static String DRAW_FONT_FALLBACK_LARGE = "Times-BOLD-14";
    private static final int DRAW_HORZ = 10;
    private static final int DRAW_VERT = 22;
    private static final float DRAW_SIZE = 14;
    private static final float DRAW_MULT_SMALL = (float) 0.5;
    private static final float DRAW_MULT_MEDIUM = (float) 0.75;
    private static final float DRAW_MULT_LARGE = (float) 1.0;
    private static int GRAPHIC_SIZE = 32;
    private static final int TRIGGER_SMALL = 600;
    private static final int TRIGGER_MEDIUM = 1000;
    private static final ImageTransform PAST = new ImageTransform(0.6, 0.6, 0.6, 1);
    private static final ImageTransform PRESENT = new ImageTransform(1, 1, 1, 1);
    private static final ImageTransform FUTURE = new ImageTransform(1, 1, 1, 0.6);
    private static int ACTIVE_ERA = EraConstants.ERA_PRESENT;

    private ImageManager() {
	// Do nothing
    }

    public static void changeEra(final int newEra) {
	ImageManager.ACTIVE_ERA = newEra;
    }

    public static void activeLanguageChanged() {
	ImageCache.flushCache();
    }

    public static BufferedImageIcon getImage(final AbstractArenaObject obj, final boolean useText) {
	BufferedImageIcon source = null;
	if (obj.getBaseImageName().equals(StringLoader.loadImageString(StringConstants.OBJECT_STRINGS_FILE,
		new Tunnel().getStringBaseID() * 3 + 0))) {
	    source = ImageManager.getTransformedTunnel(obj.getColor(), useText);
	} else {
	    source = ImageCache.getCachedImage(obj, useText);
	}
	if (ImageManager.ACTIVE_ERA == EraConstants.ERA_PAST) {
	    return ImageManager.applyTransform(source, ImageManager.PAST);
	} else if (ImageManager.ACTIVE_ERA == EraConstants.ERA_FUTURE) {
	    return ImageManager.applyTransform(source, ImageManager.FUTURE);
	} else {
	    return ImageManager.applyTransform(source, ImageManager.PRESENT);
	}
    }

    private static BufferedImageIcon applyTransform(final BufferedImageIcon source, final ImageTransform xform) {
	if (source == null) {
	    return null;
	} else {
	    final BufferedImageIcon target = new BufferedImageIcon(source);
	    final int w = source.getWidth();
	    final int h = source.getHeight();
	    for (int x = 0; x < w; x++) {
		for (int y = 0; y < h; y++) {
		    final int rgba = source.getRGB(x, y);
		    final Color sRGBA = new Color(rgba, true);
		    final int sRed = sRGBA.getRed();
		    final int sGreen = sRGBA.getGreen();
		    final int sBlue = sRGBA.getBlue();
		    final int sAlpha = sRGBA.getAlpha();
		    final int tRed = (int) (sRed * xform.getRed());
		    final int tGreen = (int) (sGreen * xform.getGreen());
		    final int tBlue = (int) (sBlue * xform.getBlue());
		    final int tAlpha = (int) (sAlpha * xform.getAlpha());
		    final Color tRGBA = new Color(tRed, tGreen, tBlue, tAlpha);
		    target.setRGB(x, y, tRGBA.getRGB());
		}
	    }
	    return target;
	}
    }

    static BufferedImageIcon getUncachedImage(final AbstractArenaObject obj, final boolean useText) {
	try {
	    String name, extraPath, sizePath, fontFallback;
	    float fontMult;
	    if (obj.isOfType(TypeConstants.TYPE_TUNNEL)) {
		name = obj.getBaseImageName();
	    } else {
		name = obj.getImageName();
	    }
	    if (obj.isEnabled()) {
		extraPath = "";
	    } else {
		extraPath = "disabled_";
	    }
	    if (ImageManager.getGraphicSize() == 16) {
		sizePath = "small/";
		fontFallback = ImageManager.DRAW_FONT_FALLBACK_SMALL;
		fontMult = ImageManager.DRAW_MULT_SMALL;
	    } else if (ImageManager.getGraphicSize() == 24) {
		sizePath = "medium/";
		fontFallback = ImageManager.DRAW_FONT_FALLBACK_MEDIUM;
		fontMult = ImageManager.DRAW_MULT_MEDIUM;
	    } else if (ImageManager.getGraphicSize() == 32) {
		sizePath = "large/";
		fontFallback = ImageManager.DRAW_FONT_FALLBACK_LARGE;
		fontMult = ImageManager.DRAW_MULT_LARGE;
	    } else {
		sizePath = "";
		fontFallback = "";
		fontMult = 0;
	    }
	    final String normalName = ImageManager.normalizeName(name);
	    final URL url = ImageManager.LOAD_CLASS.getResource(ImageManager.LOAD_PATH
		    + StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
			    StringConstants.NOTL_STRING_OBJECTS_SUBPATH)
		    + sizePath + extraPath + normalName + StringConstants.COMMON_STRING_NOTL_IMAGE_EXTENSION_PNG);
	    final BufferedImage image = ImageIO.read(url);
	    final String customText = obj.getCustomText();
	    if (useText && customText != null) {
		if (ImageManager.DRAW_FONT == null) {
		    try (InputStream is = ImageManager.class.getResourceAsStream(StringLoader
			    .loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_FONT_PATH)
			    + StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
				    StringConstants.NOTL_STRING_FONT_FILENAME))) {
			final Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
			ImageManager.DRAW_FONT = baseFont.deriveFont(ImageManager.DRAW_SIZE * fontMult);
		    } catch (final Exception ex) {
			ImageManager.DRAW_FONT = Font.decode(fontFallback);
		    }
		}
		final Graphics2D g2 = image.createGraphics();
		g2.setFont(ImageManager.DRAW_FONT);
		g2.setColor(obj.getCustomTextColor());
		g2.drawString(customText, ImageManager.DRAW_HORZ * fontMult, ImageManager.DRAW_VERT * fontMult);
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

    public static int getGraphicSize() {
	return ImageManager.GRAPHIC_SIZE;
    }

    public static int getMinimumGraphicSize() {
	return 16;
    }

    public static void autoSetGraphicSize() {
	final Toolkit tk = Toolkit.getDefaultToolkit();
	final int horz = tk.getScreenSize().width;
	final int vert = tk.getScreenSize().height;
	final int smaller = Math.min(horz, vert);
	if (smaller <= ImageManager.TRIGGER_SMALL) {
	    ImageManager.GRAPHIC_SIZE = 16;
	} else if (smaller <= ImageManager.TRIGGER_MEDIUM) {
	    ImageManager.GRAPHIC_SIZE = 24;
	} else {
	    ImageManager.GRAPHIC_SIZE = 32;
	}
    }
}
