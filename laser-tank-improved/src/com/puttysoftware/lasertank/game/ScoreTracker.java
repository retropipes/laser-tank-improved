/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.game;

import java.io.File;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;
import com.puttysoftware.lasertank.utilities.Extension;
import com.puttysoftware.scoring.SavedScoreManager;
import com.puttysoftware.scoring.ScoreManager;

class ScoreTracker {
    private static final String MAC_PREFIX = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_DIRECTORY_UNIX_HOME);
    private static final String WIN_PREFIX = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_DIRECTORY_WINDOWS_APPDATA);
    private static final String UNIX_PREFIX = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_DIRECTORY_UNIX_HOME);
    private static final String MAC_DIR = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_DIRECTORY_SCORES_MAC);
    private static final String WIN_DIR = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_DIRECTORY_SCORES_WINDOWS);
    private static final String UNIX_DIR = StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
	    StringConstants.NOTL_STRING_DIRECTORY_SCORES_UNIX);

    private static String getScoreDirectory() {
	final String osName = System.getProperty(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_OS_NAME));
	if (osName.indexOf(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_MAC_OS_X)) != -1) {
	    // Mac OS X
	    return ScoreTracker.MAC_DIR;
	} else if (osName.indexOf(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_WINDOWS)) != -1) {
	    // Windows
	    return ScoreTracker.WIN_DIR;
	} else {
	    // Other - assume UNIX-like
	    return ScoreTracker.UNIX_DIR;
	}
    }

    private static String getScoreDirPrefix() {
	final String osName = System.getProperty(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_OS_NAME));
	if (osName.indexOf(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_MAC_OS_X)) != -1) {
	    // Mac OS X
	    return System.getenv(ScoreTracker.MAC_PREFIX);
	} else if (osName.indexOf(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		StringConstants.NOTL_STRING_WINDOWS)) != -1) {
	    // Windows
	    return System.getenv(ScoreTracker.WIN_PREFIX);
	} else {
	    // Other - assume UNIX-like
	    return System.getenv(ScoreTracker.UNIX_PREFIX);
	}
    }

    private static File getScoresFile(final String filename) {
	final StringBuilder b = new StringBuilder();
	b.append(ScoreTracker.getScoreDirPrefix());
	b.append(ScoreTracker.getScoreDirectory());
	b.append(filename);
	b.append(StringConstants.COMMON_STRING_UNDERSCORE);
	b.append(LaserTank.getApplication().getArenaManager().getArena().getActiveLevelNumber() + 1);
	b.append(Extension.getScoresExtensionWithPeriod());
	return new File(b.toString());
    }

    // Fields
    private SavedScoreManager ssMgr;
    private long moves;
    private long shots;
    private long others;
    private boolean trackScores;

    // Constructors
    public ScoreTracker() {
	this.moves = 0L;
	this.shots = 0L;
	this.others = 0L;
	this.ssMgr = null;
	this.trackScores = true;
    }

    // Methods
    boolean checkScore() {
	if (this.trackScores) {
	    return this.ssMgr.checkScore(new long[] { this.moves, this.shots, this.others });
	} else {
	    return false;
	}
    }

    void commitScore() {
	if (this.trackScores) {
	    final boolean result = this.ssMgr.addScore(new long[] { this.moves, this.shots, this.others });
	    if (result) {
		this.ssMgr.viewTable();
	    }
	}
    }

    void decrementMoves() {
	this.moves--;
    }

    void decrementOthers() {
	this.others--;
    }

    void decrementShots() {
	this.shots--;
    }

    long getMoves() {
	return this.moves;
    }

    long getOthers() {
	return this.others;
    }

    long getShots() {
	return this.shots;
    }

    void incrementMoves() {
	this.moves++;
    }

    void incrementOthers() {
	this.others++;
    }

    void incrementShots() {
	this.shots++;
    }

    void resetScore() {
	this.moves = 0L;
	this.shots = 0L;
	this.others = 0L;
    }

    void resetScore(final String filename) {
	this.setScoreFile(filename);
	this.moves = 0L;
	this.shots = 0L;
	this.others = 0L;
    }

    void setMoves(final long m) {
	this.moves = m;
    }

    void setOthers(final long o) {
	this.others = o;
    }

    void setScoreFile(final String filename) {
	this.trackScores = true;
	// Check filename argument
	if (filename != null) {
	    if (filename.isEmpty()) {
		this.trackScores = false;
	    }
	} else {
	    this.trackScores = false;
	}
	if (this.trackScores) {
	    // Make sure the needed directories exist first
	    final File sf = ScoreTracker.getScoresFile(filename);
	    final File parent = new File(sf.getParent());
	    if (!parent.exists()) {
		final boolean success = parent.mkdirs();
		if (!success) {
		    this.trackScores = false;
		}
	    }
	    if (this.trackScores) {
		final String scoresFile = sf.getAbsolutePath();
		this.ssMgr = new SavedScoreManager(3, 10, ScoreManager.SORT_ORDER_DESCENDING, 10000L,
			StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
				StringConstants.NOTL_STRING_PROGRAM_NAME)
				+ StringConstants.COMMON_STRING_SPACE
				+ StringLoader.loadString(StringConstants.GAME_STRINGS_FILE,
					StringConstants.GAME_STRING_SCORES),
			new String[] {
				StringLoader.loadString(StringConstants.GAME_STRINGS_FILE,
					StringConstants.GAME_STRING_SCORE_MOVES),
				StringLoader.loadString(StringConstants.GAME_STRINGS_FILE,
					StringConstants.GAME_STRING_SCORE_SHOTS),
				StringLoader.loadString(StringConstants.GAME_STRINGS_FILE,
					StringConstants.GAME_STRING_SCORE_OTHERS) },
			scoresFile);
	    }
	}
    }

    void setShots(final long s) {
	this.shots = s;
    }

    void showScoreTable() {
	if (this.trackScores) {
	    this.ssMgr.viewTable();
	} else {
	    CommonDialogs.showDialog(StringLoader.loadString(StringConstants.GAME_STRINGS_FILE,
		    StringConstants.GAME_STRING_SCORES_UNAVAILABLE));
	}
    }
}
