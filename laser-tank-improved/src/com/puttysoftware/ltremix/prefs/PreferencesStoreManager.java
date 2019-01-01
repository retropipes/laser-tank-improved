/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.prefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

class PreferencesStoreManager {
    // Fields
    private final Properties store;

    // Constructors
    PreferencesStoreManager() {
	this.store = new Properties();
    }

    // Methods
    public String getString(final String key, final String defaultValue) {
	return this.store.getProperty(key, defaultValue);
    }

    public void setString(final String key, final String newValue) {
	this.store.setProperty(key, newValue);
    }

    public boolean getBoolean(final String key, final boolean defaultValue) {
	final String strVal = this.getString(key, Boolean.toString(defaultValue));
	return Boolean.parseBoolean(strVal);
    }

    public void setBoolean(final String key, final boolean newValue) {
	this.setString(key, Boolean.toString(newValue));
    }

    public int getInteger(final String key, final int defaultValue) {
	final String strVal = this.getString(key, Integer.toString(defaultValue));
	return Integer.parseInt(strVal);
    }

    public void setInteger(final String key, final int newValue) {
	this.setString(key, Integer.toString(newValue));
    }

    public void loadStore(final InputStream source) throws IOException {
	this.store.loadFromXML(source);
    }

    public void saveStore(final OutputStream dest) throws IOException {
	this.store.storeToXML(dest, null);
    }
}
