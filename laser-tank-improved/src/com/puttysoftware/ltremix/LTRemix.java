/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix;

import com.puttysoftware.lasertank.improved.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.improved.errors.ErrorLogger;
//import com.puttysoftware.lasertank.improved.integration.NativeIntegration;
import com.puttysoftware.ltremix.prefs.PreferencesManager;
import com.puttysoftware.ltremix.resourcemanagers.ImageManager;
import com.puttysoftware.ltremix.resourcemanagers.LogoManager;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;

public class LTRemix {
    private LTRemix() {
	// Do nothing
    }

    // Constants
    private static Application application;
    private static String PROGRAM_NAME = "LTRemix";
    private static String ERROR_MESSAGE = null;
    private static String ERROR_TITLE = null;
    private static ErrorLogger errorLogger;

    // Methods
    public static Application getApplication() {
	return LTRemix.application;
    }

    public static ErrorLogger getErrorLogger() {
	CommonDialogs.showErrorDialog(LTRemix.ERROR_MESSAGE, LTRemix.ERROR_TITLE);
	return LTRemix.errorLogger;
    }

    public static ErrorLogger getErrorLoggerDirectly() {
	return LTRemix.errorLogger;
    }

    public static void mainDISABLED() {
	try {
	    try {
		// Initialize strings
		LTRemix.initStrings();
		// Initialize error logger
		LTRemix.errorLogger = new ErrorLogger(LTRemix.PROGRAM_NAME);
		// Auto-compute graphic size
		ImageManager.autoSetGraphicSize();
	    } catch (final RuntimeException re) {
		// Something has gone horribly wrong
		CommonDialogs.showErrorDialog("Something has gone horribly wrong trying to load the string data!",
			"FATAL ERROR");
		System.exit(1);
	    }
	    // Create and initialize application
	    LTRemix.application = new Application();
	    LTRemix.application.postConstruct();
	    // Set Up Common Dialogs
	    CommonDialogs.setDefaultTitle(LTRemix.PROGRAM_NAME);
	    CommonDialogs.setIcon(LogoManager.getMicroLogo());
	    // Initialize preferences
	    PreferencesManager.readPrefs();
	    StringLoader.activeLanguageChanged(PreferencesManager.getLanguageID());
	    // Register platform hooks
//            NativeIntegration.hookAbout(LTRemix.application.getAboutDialog(),
//                    LTRemix.application.getAboutDialog().getClass()
//                            .getDeclaredMethod(StringLoader.loadString(
//                                    StringConstants.NOTL_STRINGS_FILE,
//                                    StringConstants.NOTL_STRING_SHOW_ABOUT_DIALOG_METHOD)));
//            NativeIntegration.hookPreferences(PreferencesManager.class,
//                    PreferencesManager.class.getDeclaredMethod(StringLoader
//                            .loadString(StringConstants.NOTL_STRINGS_FILE,
//                                    StringConstants.NOTL_STRING_SHOW_PREFERENCES_METHOD)));
//            NativeIntegration.hookQuit(LTRemix.application.getGUIManager(),
//                    LTRemix.application.getGUIManager().getClass()
//                            .getDeclaredMethod(StringLoader.loadString(
//                                    StringConstants.NOTL_STRINGS_FILE,
//                                    StringConstants.NOTL_STRING_QUIT_HANDLER_METHOD)));
	    // Display GUI
	    LTRemix.application.getGUIManager().showGUI();
	} catch (final Throwable t) {
	    LTRemix.getErrorLogger().logError(t);
	}
    }

    private static void initStrings() {
	StringLoader.setDefaultLanguage();
	LTRemix.ERROR_TITLE = StringLoader.loadString(StringConstants.ERROR_STRINGS_FILE,
		StringConstants.ERROR_STRING_ERROR_TITLE);
	LTRemix.ERROR_MESSAGE = StringLoader.loadString(StringConstants.ERROR_STRINGS_FILE,
		StringConstants.ERROR_STRING_ERROR_MESSAGE);
    }
}
