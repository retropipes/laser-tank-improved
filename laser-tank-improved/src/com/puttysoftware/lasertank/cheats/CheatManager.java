/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.cheats;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.cheats.Cheat.Effect;
import com.puttysoftware.lasertank.strings.CommonString;
import com.puttysoftware.lasertank.strings.DialogString;
import com.puttysoftware.lasertank.strings.ErrorString;
import com.puttysoftware.lasertank.strings.GameString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.strings.global.UntranslatedString;
import com.puttysoftware.lasertank.utilities.InvalidArenaException;

public final class CheatManager {
    // Fields
    private final CheatList cheatCache;
    private int cheatCount;

    // Constructor
    public CheatManager() {
	this.cheatCache = new CheatList();
	this.loadCheatCache();
    }

    public String enterCheat() {
	final String userInput = CommonDialogs.showTextInputDialog(StringLoader.loadGame(GameString.CHEAT_PROMPT),
		StringLoader.loadDialog(DialogString.CHEATS));
	if (userInput != null) {
	    final int index = this.cheatCache.indexOf(userInput.toLowerCase());
	    if (index != -1) {
		final int value = CommonDialogs.showConfirmDialog(StringLoader.loadGame(GameString.CHEAT_ACTION),
			StringLoader.loadDialog(DialogString.CHEATS));
		if (value == JOptionPane.YES_OPTION) {
		    return StringLoader.loadGame(GameString.ENABLE_CHEAT) + StringLoader.loadCommon(CommonString.SPACE)
			    + userInput.toLowerCase();
		} else {
		    return StringLoader.loadGame(GameString.DISABLE_CHEAT) + StringLoader.loadCommon(CommonString.SPACE)
			    + userInput.toLowerCase();
		}
	    } else {
		CommonDialogs.showErrorDialog(StringLoader.loadError(ErrorString.INVALID_CHEAT),
			StringLoader.loadDialog(DialogString.CHEATS));
		return null;
	    }
	} else {
	    return null;
	}
    }

    public int getCheatCount() {
	return this.cheatCount;
    }

    // Methods
    private void loadCheatCache() {
	Properties instant = new Properties();
	try (InputStream is = CheatManager.class
		.getResourceAsStream(GlobalLoader.loadUntranslated(UntranslatedString.INSTANT_CHEATS_PATH))) {
	    instant.load(is);
	} catch (final IOException ioe) {
	    throw new InvalidArenaException(ioe);
	}
	Properties toggle = new Properties();
	try (InputStream is = CheatManager.class
		.getResourceAsStream(GlobalLoader.loadUntranslated(UntranslatedString.TOGGLE_CHEATS_PATH))) {
	    toggle.load(is);
	} catch (final IOException ioe) {
	    throw new InvalidArenaException(ioe);
	}
	int iLimit = Cheat.instantCount();
	for (int i = 0; i < iLimit; i++) {
	    String code = instant.getProperty(Integer.toString(i));
	    this.cheatCache.add(new InstantCheat(code, Effect.values()[i]));
	}
	int tLimit = Cheat.count();
	for (int t = iLimit; t < tLimit; t++) {
	    String code = toggle.getProperty(Integer.toString(t));
	    this.cheatCache.add(new ToggleCheat(code, Effect.values()[t]));
	}
	this.cheatCount = tLimit;
    }

    public int queryCheatCache(final String query) {
	return this.cheatCache.indexOf(query);
    }
}
