package com.puttysoftware.images;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

public class BufferedImageIcon extends BufferedImage implements Icon {
    // Fields
    private static final int DEFAULT_TYPE = BufferedImage.TYPE_INT_ARGB;

    /**
     * Creates a BufferedImageIcon based on a BufferedImage object.
     *
     * @param bi
     */
    public BufferedImageIcon(final BufferedImage bi) {
	super(bi.getWidth(), bi.getHeight(), BufferedImageIcon.DEFAULT_TYPE);
	for (int x = 0; x < bi.getWidth(); x++) {
	    for (int y = 0; y < bi.getHeight(); y++) {
		this.setRGB(x, y, bi.getRGB(x, y));
	    }
	}
    }

    /**
     * Creates a square BufferedImageIcon of a given size and color.
     *
     * @param size
     * @param color
     */
    public BufferedImageIcon(final int size, final Color color) {
	super(size, size, BufferedImageIcon.DEFAULT_TYPE);
	for (int x = 0; x < size; x++) {
	    for (int y = 0; y < size; y++) {
		this.setRGB(x, y, color.getRGB());
	    }
	}
    }

    // Constructors
    /**
     * Creates a BufferedImageIcon of a given size.
     *
     * @param width
     * @param height
     */
    public BufferedImageIcon(final int width, final int height) {
	super(width, height, BufferedImageIcon.DEFAULT_TYPE);
    }

    /**
     * @return the height of this BufferedImageIcon, in pixels
     */
    @Override
    public int getIconHeight() {
	return this.getHeight();
    }

    /**
     * @return the width of this BufferedImageIcon, in pixels
     */
    @Override
    public int getIconWidth() {
	return this.getWidth();
    }

    /**
     * Paints the BufferedImageIcon, using the given Graphics, on the given
     * Component at the given x, y location.
     *
     * @param c
     * @param g
     * @param x
     * @param y
     */
    @Override
    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
	g.drawImage(this, x, y, c);
    }
}