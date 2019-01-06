/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.resourcemanagers.ImageManager;

public abstract class GenericEditor {
    // Fields
    private final String source;
    private JFrame outputFrame;
    private Container borderPane;
    private Container outputPane;
    private JLabel messageLabel;
    private JScrollPane scrollPane;
    private boolean objectChanged;
    private boolean readOnly;

    protected GenericEditor(final String newSource) {
	this.source = newSource;
	this.objectChanged = true;
    }

    protected abstract void borderPaneHook();

    public final boolean create() {
	if (this.usesImporter()) {
	    this.newObjectOptions();
	    return true;
	} else {
	    boolean success = true;
	    if (this.newObjectOptions()) {
		success = this.newObjectCreate();
		if (success) {
		    this.saveObject();
		    this.objectChanged = true;
		}
		return success;
	    }
	    return false;
	}
    }

    public abstract JMenu createEditorCommandsMenu();

    public abstract void disableEditorCommands();

    protected abstract boolean doesObjectExist();

    // Methods
    public final void edit() {
	// Create the managers
	if (this.objectChanged) {
	    this.loadObject();
	    this.editObjectChanged();
	    this.objectChanged = false;
	}
	this.setUpGUI();
	// Make sure message area is attached to border pane
	this.borderPane.removeAll();
	this.borderPane.add(this.messageLabel, BorderLayout.NORTH);
	this.borderPane.add(this.outputPane, BorderLayout.CENTER);
	this.borderPaneHook();
	this.showOutput();
	this.redrawEditor();
    }

    protected abstract void editObjectChanged();

    public abstract void enableEditorCommands();

    public final void exitEditor() {
	// Save changes
	this.saveObject();
	// Hide the editor
	this.hideOutput();
    }

    // Methods from EditorProperties
    public final String getEditorSource() {
	return this.source;
    }

    public final JFrame getOutputFrame() {
	if (this.outputFrame != null && this.outputFrame.isVisible()) {
	    return this.outputFrame;
	} else {
	    return null;
	}
    }

    protected abstract WindowListener guiHookWindow();

    public abstract void handleCloseWindow();

    public final void hideOutput() {
	if (this.outputFrame != null) {
	    this.outputFrame.setVisible(false);
	}
    }

    public final boolean isReadOnly() {
	return this.readOnly;
    }

    protected abstract void loadObject();

    protected abstract boolean newObjectCreate();

    protected abstract boolean newObjectOptions();

    public abstract void redrawEditor();

    protected abstract void reSetUpGUIHook(Container output);

    protected abstract void saveObject();

    protected void setUpGUI() {
	this.messageLabel = new JLabel(" ");
	this.outputFrame = LaserTank.getApplication().getMasterFrame();
	this.outputPane = new Container();
	this.borderPane = new Container();
	this.borderPane.setLayout(new BorderLayout());
	this.outputFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	this.setUpGUIHook(this.outputPane);
	this.scrollPane = new JScrollPane(this.borderPane);
	this.borderPane.add(this.outputPane, BorderLayout.CENTER);
	this.borderPane.add(this.messageLabel, BorderLayout.NORTH);
	this.outputFrame.setResizable(false);
	final WindowListener wl = this.guiHookWindow();
	if (wl != null) {
	    this.outputFrame.addWindowListener(wl);
	}
	this.outputFrame.setContentPane(this.scrollPane);
	this.outputFrame.pack();
	if (this.outputFrame.getWidth() > ImageManager.MAX_WINDOW_SIZE
		|| this.outputFrame.getHeight() > ImageManager.MAX_WINDOW_SIZE) {
	    int pw, ph;
	    if (this.outputFrame.getWidth() > ImageManager.MAX_WINDOW_SIZE) {
		pw = ImageManager.MAX_WINDOW_SIZE;
	    } else {
		pw = this.scrollPane.getWidth();
	    }
	    if (this.outputFrame.getHeight() > ImageManager.MAX_WINDOW_SIZE) {
		ph = ImageManager.MAX_WINDOW_SIZE;
	    } else {
		ph = this.scrollPane.getHeight();
	    }
	    this.scrollPane.setPreferredSize(new Dimension(pw, ph));
	}
    }

    protected abstract void setUpGUIHook(Container output);

    public final void showOutput() {
	this.outputFrame.pack();
    }

    public abstract void switchFromSubEditor();

    public abstract void switchToSubEditor();

    public boolean usesImporter() {
	return false;
    }
}
