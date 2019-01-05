/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.resourcemanagers;

import com.puttysoftware.images.BufferedImageIcon;

class LogoCacheEntry {
    // Fields
    private BufferedImageIcon entry;
    private String nameEntry;

    // Constructor
    LogoCacheEntry() {
	// Do nothing
    }

    // Methods
    BufferedImageIcon getEntry() {
	return this.entry;
    }

    String getNameEntry() {
	return this.nameEntry;
    }

    void setEntry(final BufferedImageIcon entry1) {
	this.entry = entry1;
    }

    void setNameEntry(final String nameEntry1) {
	this.nameEntry = nameEntry1;
    }
}