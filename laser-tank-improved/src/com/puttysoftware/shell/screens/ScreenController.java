package com.puttysoftware.shell.screens;

import java.awt.event.WindowListener;
import java.lang.ref.WeakReference;

public abstract class ScreenController implements WindowListener {
    // Fields
    private final ScreenModel model;
    private final ScreenView view;
    private boolean viewReady;

    // Constructors
    protected ScreenController(final ScreenModel theModel, final ScreenView theView) {
	super();
	this.model = theModel;
	this.view = theView;
	this.viewReady = false;
    }

    protected final void hideScreen() {
	if (!this.viewReady) {
	    this.view.setUpView(this.model, new WeakReference<>(this));
	    this.viewReady = true;
	}
	this.view.hideScreen();
    }

    // Methods
    public final void showScreen() {
	if (!this.viewReady) {
	    this.view.setUpView(this.model, new WeakReference<>(this));
	    this.viewReady = true;
	}
	this.view.showScreen();
    }
}
