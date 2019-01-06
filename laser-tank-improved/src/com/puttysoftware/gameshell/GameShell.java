package com.puttysoftware.gameshell;

import javax.swing.JFrame;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.errors.ErrorLogger;

public abstract class GameShell {
    // Fields
    private final String gameName;
    private final ErrorLogger logger;
    private final ExceptionMessageConfiguration errorConfig;
    private final ExceptionMessageConfiguration warningConfig;
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

    public final JFrame getMasterFrame() {
	return this.theFrame;
    }

    public abstract MenuManagerShell getMenus();
}
