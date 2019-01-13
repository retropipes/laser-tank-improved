package com.puttysoftware.lasertank.stringmanagers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.fileio.ResourceStreamReader;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.prefs.PreferencesManager;
import com.puttysoftware.lasertank.resourcemanagers.ImageManager;
import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.DifficultyConstants;

public class StringLoader {
    private static final String LOAD_PATH = "/assets/locale/";
    private static Class<?> LOAD_CLASS = StringLoader.class;
    private static ArrayList<Properties> STRING_CACHE;
    private static ArrayList<Properties> LANGUAGE_STRING_CACHE;
    private static ArrayList<String> LOCALIZED_LANGUAGES;
    private static int LANGUAGE_ID = 0;
    private static String LANGUAGE_NAME = null;

    public static void activeLanguageChanged(final int newLanguageID) {
	StringLoader.STRING_CACHE = null;
	StringLoader.LOCALIZED_LANGUAGES = null;
	StringLoader.LANGUAGE_ID = newLanguageID;
	StringLoader.LANGUAGE_NAME = StringLoader.loadLanguageString(StringConstants.STRINGS_FILE_LANGUAGE,
		StringLoader.LANGUAGE_ID) + "/";
	DifficultyConstants.reloadDifficultyNames();
	ArenaConstants.activeLanguageChanged();
	LaserTank.getApplication().activeLanguageChanged();
	PreferencesManager.activeLanguageChanged();
	ImageManager.activeLanguageChanged();
    }

    public static String getLanguageName() {
	return StringLoader.LANGUAGE_NAME;
    }

    private static void cacheLanguageStringFile(final int fileID) {
	final String filename = StringConstants.LANGUAGE_STRINGS_FILES[fileID];
	try (final InputStream is = StringLoader.LOAD_CLASS
		.getResourceAsStream(StringLoader.LOAD_PATH + filename + StringConstants.STRINGS_EXTENSION)) {
	    if (StringLoader.LANGUAGE_STRING_CACHE.size() <= fileID
		    || StringLoader.LANGUAGE_STRING_CACHE.get(fileID) == null) {
		// Entry for string file doesn't exist, so create it
		for (int x = 0; x <= fileID; x++) {
		    if (StringLoader.LANGUAGE_STRING_CACHE.size() <= x
			    || StringLoader.LANGUAGE_STRING_CACHE.get(x) == null) {
			StringLoader.LANGUAGE_STRING_CACHE.add(new Properties());
		    }
		}
	    }
	    StringLoader.LANGUAGE_STRING_CACHE.get(fileID).load(is);
	} catch (final IOException ioe) {
	    CommonDialogs.showErrorDialog("Something has gone horribly wrong trying to load the language string data!",
		    "FATAL ERROR");
	    LaserTank.logErrorDirectly(ioe);
	}
    }

    private static void cacheStringFile(final int fileID) {
	final String filename = StringConstants.STRINGS_FILES[fileID];
	try (final InputStream is = StringLoader.LOAD_CLASS.getResourceAsStream(
		StringLoader.LOAD_PATH + StringLoader.LANGUAGE_NAME + filename + StringConstants.STRINGS_EXTENSION)) {
	    if (StringLoader.STRING_CACHE.size() <= fileID || StringLoader.STRING_CACHE.get(fileID) == null) {
		// Entry for string file doesn't exist, so create it
		for (int x = 0; x <= fileID; x++) {
		    if (StringLoader.STRING_CACHE.size() <= x || StringLoader.STRING_CACHE.get(x) == null) {
			StringLoader.STRING_CACHE.add(new Properties());
		    }
		}
	    }
	    StringLoader.STRING_CACHE.get(fileID).load(is);
	} catch (final IOException ioe) {
	    CommonDialogs.showErrorDialog("Something has gone horribly wrong trying to load the string data!",
		    "FATAL ERROR");
	    LaserTank.logErrorDirectly(ioe);
	}
    }

    private static String loadLanguageString(final int fileID, final int strID) {
	if (StringLoader.LANGUAGE_STRING_CACHE == null) {
	    // Create string cache
	    StringLoader.LANGUAGE_STRING_CACHE = new ArrayList<>();
	}
	if (StringLoader.LANGUAGE_STRING_CACHE.size() <= fileID
		|| StringLoader.LANGUAGE_STRING_CACHE.get(fileID) == null) {
	    // Cache string file
	    StringLoader.cacheLanguageStringFile(fileID);
	}
	String value = StringLoader.LANGUAGE_STRING_CACHE.get(fileID).getProperty(Integer.toString(strID));
	if (value == null) {
	    // Cache string file
	    StringLoader.cacheLanguageStringFile(fileID);
	    // Get correct value
	    value = StringLoader.LANGUAGE_STRING_CACHE.get(fileID).getProperty(Integer.toString(strID));
	}
	return value;
    }

    public static String[] loadLocalizedLanguagesList() {
	if (StringLoader.LOCALIZED_LANGUAGES == null) {
	    StringLoader.LOCALIZED_LANGUAGES = new ArrayList<>();
	    final String filename = StringConstants.LOCALIZED_LANGUAGE_FILE_NAME;
	    try (final InputStream is = StringLoader.LOAD_CLASS
		    .getResourceAsStream(StringLoader.LOAD_PATH + StringLoader.LANGUAGE_NAME + filename);
		    final ResourceStreamReader rsr = new ResourceStreamReader(is, "UTF-8")) {
		String line = StringConstants.COMMON_STRING_EMPTY;
		while (line != null) {
		    // Read line
		    line = rsr.readString();
		    if (line != null) {
			// Parse line
			StringLoader.LOCALIZED_LANGUAGES.add(line);
		    }
		}
	    } catch (final IOException ioe) {
		CommonDialogs.showErrorDialog("Something has gone horribly wrong trying to load the language data!",
			"FATAL ERROR");
		LaserTank.logErrorDirectly(ioe);
	    }
	}
	return StringLoader.LOCALIZED_LANGUAGES.toArray(new String[StringLoader.LOCALIZED_LANGUAGES.size()]);
    }

    public static String loadString(final int fileID, final int strID) {
	if (fileID < 0) {
	    return StringLoader.loadLanguageString(-StringConstants.STRINGS_FILE_GLOBAL, strID);
	} else {
	    if (StringLoader.STRING_CACHE == null) {
		// Create string cache
		StringLoader.STRING_CACHE = new ArrayList<>();
	    }
	    if (StringLoader.STRING_CACHE.size() <= fileID || StringLoader.STRING_CACHE.get(fileID) == null) {
		// Cache string file
		StringLoader.cacheStringFile(fileID);
	    }
	    String value = StringLoader.STRING_CACHE.get(fileID).getProperty(Integer.toString(strID));
	    if (value == null) {
		// Cache string file
		StringLoader.cacheStringFile(fileID);
		// Get correct value
		value = StringLoader.STRING_CACHE.get(fileID).getProperty(Integer.toString(strID));
	    }
	    return value;
	}
    }

    public static void setDefaultLanguage() {
	StringLoader.LOCALIZED_LANGUAGES = null;
	StringLoader.LANGUAGE_ID = 0;
	StringLoader.LANGUAGE_NAME = StringLoader.loadLanguageString(StringConstants.STRINGS_FILE_LANGUAGE,
		StringLoader.LANGUAGE_ID) + "/";
	// Initialize Image String Cache
	StringLoader.STRING_CACHE = new ArrayList<>();
	StringLoader.cacheStringFile(StringConstants.STRINGS_FILE_OBJECT);
	StringLoader.cacheStringFile(StringConstants.STRINGS_FILE_GENERIC);
    }
}
