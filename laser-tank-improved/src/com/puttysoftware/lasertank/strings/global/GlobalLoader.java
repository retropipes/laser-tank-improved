package com.puttysoftware.lasertank.strings.global;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.utilities.Extension;

public class GlobalLoader {
    private static final String LOAD_PATH = "/assets/locale/";
    private static Class<?> LOAD_CLASS = GlobalLoader.class;
    private static ArrayList<Properties> CACHE;

    public static void initialize() {
	final int files = GlobalFileNames.getFileCount();
	GlobalLoader.CACHE = new ArrayList<>(files);
	for (int f = 0; f < files; f++) {
	    GlobalLoader.CACHE.add(new Properties());
	}
	GlobalLoader.cacheFile(GlobalFile.LANGUAGES);
	GlobalLoader.cacheFile(GlobalFile.NOT_TRANSLATED);
	GlobalLoader.cacheFile(GlobalFile.IMAGES);
	GlobalLoader.cacheFile(GlobalFile.FRAMES);
	GlobalLoader.cacheFile(GlobalFile.DIRECTIONS);
	GlobalLoader.cacheFile(GlobalFile.INDEXES);
    }

    private static void cacheFile(final GlobalFile file) {
	final int fileID = file.ordinal();
	final String filename = GlobalFileNames.getFileName(file);
	try (final InputStream is = GlobalLoader.LOAD_CLASS
		.getResourceAsStream(GlobalLoader.LOAD_PATH + filename + Extension.getStringsExtensionWithPeriod())) {
	    GlobalLoader.CACHE.get(fileID).load(is);
	} catch (final IOException ioe) {
	    CommonDialogs.showErrorDialog("Something has gone horribly wrong trying to cache global string data!",
		    "FATAL ERROR");
	    LaserTank.logErrorDirectly(ioe);
	}
    }

    private static Properties getFromCache(final GlobalFile file) {
	final int fileID = file.ordinal();
	return GlobalLoader.CACHE.get(fileID);
    }

    public static String loadLanguage(final int strID) {
	return GlobalLoader.getFromCache(GlobalFile.LANGUAGES).getProperty(Integer.toString(strID));
    }

    public static String loadUntranslated(final UntranslatedString str) {
	return GlobalLoader.getFromCache(GlobalFile.NOT_TRANSLATED).getProperty(Integer.toString(str.ordinal()));
    }

    public static String loadImage(final int imageID) {
	return GlobalLoader.getFromCache(GlobalFile.IMAGES).getProperty(Integer.toString(imageID));
    }

    public static String loadFrame(final int frameID) {
	return GlobalLoader.getFromCache(GlobalFile.FRAMES).getProperty(Integer.toString(frameID));
    }

    public static String loadDirection(final int dir) {
	return GlobalLoader.getFromCache(GlobalFile.DIRECTIONS).getProperty(Integer.toString(dir));
    }

    public static String loadIndex(final int index) {
	return GlobalLoader.getFromCache(GlobalFile.LANGUAGES).getProperty(Integer.toString(index));
    }
}
