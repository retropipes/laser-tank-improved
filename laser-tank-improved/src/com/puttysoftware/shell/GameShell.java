package com.puttysoftware.shell;

import java.util.HashMap;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.errors.ErrorLogger;
import com.puttysoftware.shell.dialogs.DialogController;
import com.puttysoftware.shell.screens.ScreenController;

public abstract class GameShell {
    // Fields
    private final String gameName;
    private final ErrorLogger logger;
    private final ExceptionMessageConfiguration errorConfig;
    private final ExceptionMessageConfiguration warningConfig;
    private final HashMap<Integer, DialogController> dialogs;
    private final HashMap<Integer, ScreenController> screens;

    // Constructor
    public GameShell(final String name, final ExceptionMessageConfiguration errorSettings,
	    final ExceptionMessageConfiguration warningSettings) {
	super();
	this.gameName = name;
	this.errorConfig = errorSettings;
	this.warningConfig = warningSettings;
	this.logger = new ErrorLogger(this.gameName);
	this.dialogs = new HashMap<>();
	this.screens = new HashMap<>();
    }

    public final DialogController getDialog(final int key) {
	return this.dialogs.get(key);
    }

    public abstract MenuManagerShell getMenus();

    public final ScreenController getScreen(final int key) {
	return this.screens.get(key);
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

    public final void registerDialog(final int key, final DialogController value) {
	this.dialogs.put(key, value);
    }

    public final void registerScreen(final int key, final ScreenController value) {
	this.screens.put(key, value);
    }
}
