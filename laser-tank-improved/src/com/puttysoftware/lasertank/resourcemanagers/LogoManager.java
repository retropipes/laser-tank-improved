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
import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.strings.global.UntranslatedString;
import com.puttysoftware.lasertank.utilities.InvalidArenaException;

public class LogoManager {
    private static final String DEFAULT_LOAD_PATH = "/assets/locale/";
    private static Class<?> LOAD_CLASS = LogoManager.class;
    private static Font LOGO_DRAW_FONT = null;
    private static final String LOGO_DRAW_FONT_FALLBACK = "Times-BOLD-24";
    private static final int LOGO_FALLBACK_DRAW_HORZ = 196;
    private static final int LOGO_FALLBACK_DRAW_HORZ_MAX = 16;
    private static final int LOGO_FALLBACK_DRAW_HORZ_PCO = 8;
    private static final int LOGO_FALLBACK_DRAW_VERT = 152;
    private static final int LOGO_DRAW_HORZ = 156;
    private static final int LOGO_DRAW_HORZ_MAX = 2;
    private static final int LOGO_DRAW_HORZ_PCO = 2;
    private static final int LOGO_DRAW_VERT = 146;
    private static BufferedImageIcon openingCache, controlCache;

    public static BufferedImageIcon getOpening() {
	if (LogoManager.openingCache == null) {
	    LogoManager.openingCache = LogoManager.getLogo("opening.png", true);
	}
	return LogoManager.openingCache;
    }

    public static BufferedImageIcon getControl() {
	if (LogoManager.controlCache == null) {
	    LogoManager.controlCache = LogoManager.getLogo("control.png", false);
	}
	return LogoManager.controlCache;
    }

    public static void activeLanguageChanged() {
	// Invalidate caches
	LogoManager.openingCache = null;
	LogoManager.controlCache = null;
    }

    private static BufferedImageIcon getLogo(final String name, final boolean drawing) {
	try {
	    final URL url = LogoManager.LOAD_CLASS
		    .getResource(LogoManager.DEFAULT_LOAD_PATH + StringLoader.getLanguageName() + name);
	    final BufferedImage image = ImageIO.read(url);
	    final Graphics2D g2 = image.createGraphics();
	    g2.setColor(Color.yellow);
	    final String logoVer = Application.getLogoVersionString();
	    if (drawing) {
		if (LogoManager.LOGO_DRAW_FONT == null) {
		    try (InputStream is = LogoManager.class
			    .getResourceAsStream(GlobalLoader.loadUntranslated(UntranslatedString.FONT_PATH)
				    + GlobalLoader.loadUntranslated(UntranslatedString.FONT_FILENAME))) {
			final Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
			LogoManager.LOGO_DRAW_FONT = baseFont.deriveFont((float) 24);
			g2.setFont(LogoManager.LOGO_DRAW_FONT);
			g2.drawString(logoVer, LogoManager.LOGO_DRAW_HORZ
				+ (LogoManager.LOGO_DRAW_HORZ_MAX - logoVer.length()) * LogoManager.LOGO_DRAW_HORZ_PCO,
				LogoManager.LOGO_DRAW_VERT);
		    } catch (final Exception ex) {
			LogoManager.LOGO_DRAW_FONT = Font.decode(LogoManager.LOGO_DRAW_FONT_FALLBACK);
			g2.setFont(LogoManager.LOGO_DRAW_FONT);
			g2.drawString(logoVer,
				LogoManager.LOGO_FALLBACK_DRAW_HORZ
					+ (LogoManager.LOGO_FALLBACK_DRAW_HORZ_MAX - logoVer.length())
						* LogoManager.LOGO_FALLBACK_DRAW_HORZ_PCO,
				LogoManager.LOGO_FALLBACK_DRAW_VERT);
		    }
		}
	    }
	    return new BufferedImageIcon(image);
	} catch (final IOException ioe) {
	    throw new InvalidArenaException(ioe);
	}
    }

    private LogoManager() {
	// Do nothing
    }
}
