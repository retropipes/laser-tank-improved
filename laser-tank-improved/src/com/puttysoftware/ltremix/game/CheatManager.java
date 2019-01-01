/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.game;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.puttysoftware.lasertank.improved.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.improved.fileio.ResourceStreamReader;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;

final class CheatManager {
    // Fields
    private final ArrayList<String> cheatCache;
    private int cheatCount;

    // Constructor
    public CheatManager() {
	this.cheatCount = 0;
	this.cheatCache = new ArrayList<>();
	this.loadCheatCache();
    }

    // Methods
    private void loadCheatCache() {
	try (InputStream is = CheatManager.class.getResourceAsStream(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_CHEATS_PATH));
		ResourceStreamReader rsr = new ResourceStreamReader(is)) {
	    String line = StringConstants.COMMON_STRING_EMPTY;
	    while (line != null) {
		line = rsr.readString();
		if (line != null) {
		    this.cheatCache.add(line);
		    this.cheatCount++;
		}
	    }
	    rsr.close();
	    is.close();
	} catch (final IOException e) {
	    // Ignore
	} catch (final NullPointerException e) {
	    // Ignore
	}
    }

    String enterCheat() {
	final String userInput = CommonDialogs.showTextInputDialog(
		StringLoader.loadString(StringConstants.GAME_STRINGS_FILE, StringConstants.GAME_STRING_CHEAT_PROMPT),
		StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE, StringConstants.DIALOG_STRING_CHEATS));
	if (userInput != null) {
	    final int index = this.cheatCache.indexOf(userInput.toLowerCase());
	    if (index != -1) {
		final int value = CommonDialogs.showConfirmDialog(
			StringLoader.loadString(StringConstants.GAME_STRINGS_FILE,
				StringConstants.GAME_STRING_CHEAT_ACTION),
			StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
				StringConstants.DIALOG_STRING_CHEATS));
		if (value == JOptionPane.YES_OPTION) {
		    return StringLoader.loadString(StringConstants.GAME_STRINGS_FILE,
			    StringConstants.GAME_STRING_ENABLE_CHEAT) + StringConstants.COMMON_STRING_SPACE
			    + userInput.toLowerCase();
		} else {
		    return StringLoader.loadString(StringConstants.GAME_STRINGS_FILE,
			    StringConstants.GAME_STRING_DISABLE_CHEAT) + StringConstants.COMMON_STRING_SPACE
			    + userInput.toLowerCase();
		}
	    } else {
		CommonDialogs.showErrorDialog(
			StringLoader.loadString(StringConstants.ERROR_STRINGS_FILE,
				StringConstants.ERROR_STRING_INVALID_CHEAT),
			StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
				StringConstants.DIALOG_STRING_CHEATS));
		return null;
	    }
	} else {
	    return null;
	}
    }

    int getCheatCount() {
	return this.cheatCount;
    }

    int queryCheatCache(final String query) {
	return this.cheatCache.indexOf(query);
    }
}
