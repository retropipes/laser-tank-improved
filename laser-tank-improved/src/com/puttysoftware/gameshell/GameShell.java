package com.puttysoftware.gameshell;

import java.util.HashMap;

import javax.swing.JFrame;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.errors.ErrorLogger;
import com.puttysoftware.gameshell.screens.ScreenController;

public abstract class GameShell {
    // Fields
    private final String gameName;
    private final ErrorLogger logger;
    private final ExceptionMessageConfiguration errorConfig;
    private final ExceptionMessageConfiguration warningConfig;
    private final HashMap<Integer, ScreenController> screens;
    protected final JFrame theFrame;

    // Constructor
    public GameShell(final String name, final ExceptionMessageConfiguration errorSettings,
	    final ExceptionMessageConfiguration warningSettings) {
	super();
	this.theFrame = new JFrame();
	this.gameName = name;
	this.errorConfig = errorSettings;
	this.warningConfig = warningSettings;
	this.logger = new ErrorLogger(this.gameName);
	this.screens = new HashMap<>();
    }

    // Methods
    public final void handleError(final Throwable problem) {
	if (this.errorConfig != null && this.errorConfig.isDialogDisplayed()) {
	    // Display error message
	    CommonDialogs.showErrorDialog(this.errorConfig.getMessage(), this.errorConfig.getTitle());
	}
	// Record it with the logger
	this.logger.logError(problem);
    }

    public final void handleWarning(final Throwable problem) {
	if (this.warningConfig != null && this.warningConfig.isDialogDisplayed()) {
	    // Display warning message
	    CommonDialogs.showTitledDialog(this.warningConfig.getMessage(), this.warningConfig.getTitle());
	}
	// Record it with the logger
	this.logger.logNonFatalError(problem);
    }

    public final void registerScreen(final int key, final ScreenController value) {
	this.screens.put(key, value);
    }

    public final JFrame getMasterFrame() {
	return this.theFrame;
    }

    public final ScreenController getScreen(final int key) {
	return this.screens.get(key);
    }

    public abstract MenuManagerShell getMenus();
}
