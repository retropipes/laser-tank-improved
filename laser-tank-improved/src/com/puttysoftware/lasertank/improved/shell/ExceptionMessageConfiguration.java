package com.puttysoftware.lasertank.improved.shell;

public final class ExceptionMessageConfiguration {
    // Fields
    private final String message;
    private final String title;
    private final boolean dialogDisplayed;

    // Constructor
    public ExceptionMessageConfiguration(final String excMessage, final String excTitle, final boolean useDialog) {
	this.message = excMessage;
	this.title = excTitle;
	this.dialogDisplayed = useDialog;
    }

    public String getMessage() {
	return this.message;
    }

    public String getTitle() {
	return this.title;
    }

    public boolean isDialogDisplayed() {
	return this.dialogDisplayed;
    }
}
