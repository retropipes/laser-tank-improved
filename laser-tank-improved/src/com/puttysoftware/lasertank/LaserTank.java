/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank;

import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.errors.ErrorLogger;
import com.puttysoftware.integration.NativeIntegration;
import com.puttysoftware.lasertank.prefs.PreferencesManager;
import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;

public class LaserTank {
    // Constants
    private static Application application;
    private static String PROGRAM_NAME = "LaserTank";
    private static String ERROR_MESSAGE = null;
    private static String ERROR_TITLE = null;
    private static ErrorLogger errorLogger;

    // Methods
    public static Application getApplication() {
	return LaserTank.application;
    }

    public static ErrorLogger getErrorLogger() {
	CommonDialogs.showErrorDialog(LaserTank.ERROR_MESSAGE, LaserTank.ERROR_TITLE);
	return LaserTank.errorLogger;
    }

    public static ErrorLogger getErrorLoggerDirectly() {
	return LaserTank.errorLogger;
    }

    private static void initStrings() {
	StringLoader.setDefaultLanguage();
	LaserTank.ERROR_TITLE = StringLoader.loadString(StringConstants.ERROR_STRINGS_FILE,
		StringConstants.ERROR_STRING_ERROR_TITLE);
	LaserTank.ERROR_MESSAGE = StringLoader.loadString(StringConstants.ERROR_STRINGS_FILE,
		StringConstants.ERROR_STRING_ERROR_MESSAGE);
    }

    public static void main(final String[] args) {
	try {
	    // Integrate with host platform
	    NativeIntegration ni = new NativeIntegration();
	    ni.configureLookAndFeel();
	    try {
		// Initialize strings
		LaserTank.initStrings();
		// Initialize error logger
		LaserTank.errorLogger = new ErrorLogger(LaserTank.PROGRAM_NAME);
	    } catch (final RuntimeException re) {
		// Something has gone horribly wrong
		CommonDialogs.showErrorDialog("Something has gone horribly wrong trying to load the string data!",
			"FATAL ERROR");
		LaserTank.getErrorLoggerDirectly().logError(re);
	    }
	    // Create and initialize application
	    LaserTank.application = new Application(ni);
	    LaserTank.application.init();
	    // Set Up Common Dialogs
	    CommonDialogs.setDefaultTitle(LaserTank.PROGRAM_NAME);
	    // Initialize preferences
	    PreferencesManager.readPrefs();
	    StringLoader.activeLanguageChanged(PreferencesManager.getLanguageID());
	    // Register platform hooks
	    ni.setAboutHandler(LaserTank.application.getAboutDialog());
	    ni.setPreferencesHandler(new PreferencesInvoker());
	    ni.setQuitHandler(LaserTank.application.getGUIManager());
	    // Display GUI
	    LaserTank.application.getGUIManager().showGUI();
	} catch (final Throwable t) {
	    LaserTank.getErrorLogger().logError(t);
	}
    }

    private LaserTank() {
	// Do nothing
    }

    private static class PreferencesInvoker implements PreferencesHandler {
	public PreferencesInvoker() {
	    super();
	}

	@Override
	public void handlePreferences(PreferencesEvent e) {
	    PreferencesManager.showPrefs();
	}
    }
}
