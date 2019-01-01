/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.resourcemanagers.ImageManager;
import com.puttysoftware.lasertank.resourcemanagers.LogoManager;

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

    // Methods from EditorProperties
    public final String getEditorSource() {
	return this.source;
    }

    // Methods
    public final void edit() {
	final Application app = LaserTank.getApplication();
	app.getGUIManager().hideGUI();
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

    public final void showOutput() {
	final Application app = LaserTank.getApplication();
	this.outputFrame.setJMenuBar(app.getMenuManager().getMainMenuBar());
	this.outputFrame.setVisible(true);
	this.outputFrame.pack();
    }

    public final void hideOutput() {
	if (this.outputFrame != null) {
	    this.outputFrame.setVisible(false);
	}
    }

    public final JFrame getOutputFrame() {
	if (this.outputFrame != null && this.outputFrame.isVisible()) {
	    return this.outputFrame;
	} else {
	    return null;
	}
    }

    public final boolean isReadOnly() {
	return this.readOnly;
    }

    public final void exitEditor() {
	// Save changes
	this.saveObject();
	// Hide the editor
	this.hideOutput();
    }

    public boolean usesImporter() {
	return false;
    }

    public abstract JMenu createEditorCommandsMenu();

    public abstract void enableEditorCommands();

    public abstract void disableEditorCommands();

    public abstract void handleCloseWindow();

    protected abstract boolean doesObjectExist();

    protected abstract boolean newObjectOptions();

    protected abstract boolean newObjectCreate();

    protected abstract void editObjectChanged();

    protected abstract void borderPaneHook();

    protected abstract void loadObject();

    protected abstract void saveObject();

    protected abstract void setUpGUIHook(Container output);

    protected abstract void reSetUpGUIHook(Container output);

    protected abstract WindowListener guiHookWindow();

    public abstract void switchToSubEditor();

    public abstract void switchFromSubEditor();

    public abstract void redrawEditor();

    protected void setUpGUI() {
	// Destroy the old GUI, if one exists
	if (this.outputFrame != null) {
	    this.outputFrame.dispose();
	}
	this.messageLabel = new JLabel(" ");
	this.outputFrame = new JFrame(this.getEditorSource());
	final Image iconlogo = LogoManager.getIconLogo();
	this.outputFrame.setIconImage(iconlogo);
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
}
