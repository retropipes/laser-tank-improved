package com.puttysoftware.lasertank.improved.shell.screens;

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

    public Image getSystemIcon() {
	return this.systemIcon;
    }

    // Methods
    public String getTitle() {
	return this.title;
    }

    public boolean isCustomUI() {
	return this.customUI;
    }
}
