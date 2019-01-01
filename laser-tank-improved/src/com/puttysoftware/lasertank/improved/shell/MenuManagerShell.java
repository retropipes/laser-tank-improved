package com.puttysoftware.lasertank.improved.shell;

import javax.swing.JMenuBar;

public abstract class MenuManagerShell {
    // Fields
    protected final JMenuBar menuBar;

    // Constructors
    public MenuManagerShell() {
	this.menuBar = new JMenuBar();
    }

    // Methods
    public final JMenuBar getMenuBar() {
	return this.menuBar;
    }

    public abstract void populateMenuBar();

    public abstract void updateMenuItemState(final boolean loaded, final boolean dirty);
}
