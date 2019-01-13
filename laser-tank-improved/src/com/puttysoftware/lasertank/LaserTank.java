/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank;

import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;

import javax.swing.SwingUtilities;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.errors.ErrorLogger;
import com.puttysoftware.integration.NativeIntegration;
import com.puttysoftware.lasertank.prefs.PreferencesManager;
import com.puttysoftware.lasertank.strings.ErrorString;
import com.puttysoftware.lasertank.strings.StringLoader;

public class LaserTank {
    // Constants
    private static Application application;
    private static String PROGRAM_NAME = "LaserTank";
    private static String ERROR_MESSAGE = null;
    private static String ERROR_TITLE = null;
    private static String NONFATAL_MESSAGE = null;
    private static String NONFATAL_TITLE = null;
    private static ErrorLogger errorLogger;

    // Methods
    public static Application getApplication() {
	return LaserTank.application;
    }

    public static void logError(final Throwable t) {
	CommonDialogs.showErrorDialog(LaserTank.ERROR_MESSAGE, LaserTank.ERROR_TITLE);
	LaserTank.errorLogger.logError(t);
    }

    public static void logErrorDirectly(final Throwable t) {
	LaserTank.errorLogger.logError(t);
    }

    public static void logNonFatalError(final Throwable t) {
	LaserTank.errorLogger.logNonFatalError(t);
	CommonDialogs.showTitledDialog(LaserTank.NONFATAL_MESSAGE, LaserTank.NONFATAL_TITLE);
    }

    public static void logNonFatalErrorDirectly(final Throwable t) {
	LaserTank.errorLogger.logNonFatalError(t);
    }

    private static void initStrings() {
	StringLoader.setDefaultLanguage();
	LaserTank.ERROR_TITLE = StringLoader.loadError(ErrorString.ERROR_TITLE);
	LaserTank.ERROR_MESSAGE = StringLoader.loadError(ErrorString.ERROR_MESSAGE);
	LaserTank.NONFATAL_TITLE = StringLoader.loadError(ErrorString.NONFATAL_TITLE);
	LaserTank.NONFATAL_MESSAGE = StringLoader.loadError(ErrorString.NONFATAL_MESSAGE);
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
		LaserTank.logErrorDirectly(re);
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
	    // Handle Event Thread errors
	    EventThreadUEH etueh = new EventThreadUEH();
	    EventThreadUEHInstaller etuehi = new EventThreadUEHInstaller(etueh);
	    SwingUtilities.invokeAndWait(etuehi);
	    // Display GUI
	    LaserTank.application.bootGUI();
	} catch (final Throwable t) {
	    LaserTank.logError(t);
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
