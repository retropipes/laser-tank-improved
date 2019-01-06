package com.puttysoftware.gameshell.screens;

import java.lang.ref.WeakReference;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;

public abstract class ScreenView {
    // Fields
    protected final JFrame theFrame;

    // Constructors
    protected ScreenView() {
	super();
	this.theFrame = new JFrame();
    }

    // Methods
    final void showScreen() {
	this.theFrame.setVisible(true);
    }

    final void hideScreen() {
	this.theFrame.setVisible(false);
    }

    final void setUpView(final ScreenModel model, final WeakReference<ScreenController> controllerRef) {
	if (model.isCustomUI()) {
	    this.theFrame.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
	} else {
	    this.theFrame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
	    this.theFrame.setTitle(model.getTitle());
	    this.theFrame.setIconImage(model.getSystemIcon());
	}
	final JPanel thePanel = this.populateMainPanel(model);
	thePanel.setOpaque(true);
	this.theFrame.setContentPane(thePanel);
	if (!model.isCustomUI()) {
	    this.theFrame.addWindowListener(controllerRef.get());
	}
	this.theFrame.setResizable(false);
	this.theFrame.pack();
    }

    protected abstract JPanel populateMainPanel(final ScreenModel model);
}
