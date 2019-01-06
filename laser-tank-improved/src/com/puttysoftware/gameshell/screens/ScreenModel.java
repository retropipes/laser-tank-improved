package com.puttysoftware.gameshell.screens;

import java.awt.Image;

public final class ScreenModel {
    // Fields
    private final String title;
    private final Image systemIcon;
    private final boolean customUI;

    // Constructors
    public ScreenModel() {
	super();
	this.title = null;
	this.systemIcon = null;
	this.customUI = true;
    }

    public ScreenModel(final String theTitle, final Image theSystemIcon) {
	super();
	this.title = theTitle;
	this.systemIcon = theSystemIcon;
	this.customUI = false;
    }

    // Methods
    public final String getTitle() {
	return this.title;
    }

    public final Image getSystemIcon() {
	return this.systemIcon;
    }

    public final boolean isCustomUI() {
	return this.customUI;
    }
}
