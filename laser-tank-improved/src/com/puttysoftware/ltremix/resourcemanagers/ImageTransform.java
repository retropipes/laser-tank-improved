package com.puttysoftware.ltremix.resourcemanagers;

class ImageTransform {
    // Fields
    private final double red, green, blue, alpha;

    public ImageTransform(final double newRed, final double newGreen, final double newBlue, final double newAlpha) {
	super();
	this.red = newRed;
	this.green = newGreen;
	this.blue = newBlue;
	this.alpha = newAlpha;
    }

    public double getRed() {
	return this.red;
    }

    public double getGreen() {
	return this.green;
    }

    public double getBlue() {
	return this.blue;
    }

    public double getAlpha() {
	return this.alpha;
    }
}
