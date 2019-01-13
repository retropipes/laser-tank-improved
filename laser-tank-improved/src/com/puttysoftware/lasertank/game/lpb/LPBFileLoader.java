/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.game.lpb;

import java.io.FileInputStream;
import java.io.IOException;

import com.puttysoftware.lasertank.utilities.InvalidArenaException;

class LPBFileLoader {
    // Fields
    private static byte[] lname;
    private static byte[] author;
    private static byte[] lnum;
    private static byte[] rawsize;
    private static int size;
    private static byte[] data;
    private static final int LNAME_SIZE = 31;
    private static final int AUTHOR_SIZE = 31;
    private static final int LNUM_SIZE = 2;
    private static final int RAWSIZE_SIZE = 2;

    static byte[] getData() {
	return LPBFileLoader.data;
    }

    static boolean loadLPB(final FileInputStream file) {
	try {
	    LPBFileLoader.lname = new byte[LPBFileLoader.LNAME_SIZE];
	    LPBFileLoader.author = new byte[LPBFileLoader.AUTHOR_SIZE];
	    LPBFileLoader.lnum = new byte[LPBFileLoader.LNUM_SIZE];
	    LPBFileLoader.rawsize = new byte[LPBFileLoader.RAWSIZE_SIZE];
	    int bytesread = file.read(LPBFileLoader.lname, 0, LPBFileLoader.LNAME_SIZE);
	    if (bytesread != LPBFileLoader.LNAME_SIZE) {
		return false;
	    }
	    bytesread = file.read(LPBFileLoader.author, 0, LPBFileLoader.AUTHOR_SIZE);
	    if (bytesread != LPBFileLoader.AUTHOR_SIZE) {
		return false;
	    }
	    bytesread = file.read(LPBFileLoader.lnum, 0, LPBFileLoader.LNUM_SIZE);
	    if (bytesread != LPBFileLoader.LNUM_SIZE) {
		return false;
	    }
	    bytesread = file.read(LPBFileLoader.rawsize, 0, LPBFileLoader.RAWSIZE_SIZE);
	    if (bytesread != LPBFileLoader.RAWSIZE_SIZE) {
		return false;
	    }
	    LPBFileLoader.size = LPBFileLoader.toInt(LPBFileLoader.rawsize);
	    LPBFileLoader.data = new byte[LPBFileLoader.size];
	    bytesread = file.read(LPBFileLoader.data, 0, LPBFileLoader.size);
	    if (bytesread != LPBFileLoader.size) {
		return false;
	    }
	    return true;
	} catch (final IOException ioe) {
	    throw new InvalidArenaException(ioe);
	}
    }

    private static int toInt(final byte[] d) {
	if (d == null || d.length != 2) {
	    return 0x0;
	}
	return 0xff & d[0] | (0xff & d[1]) << 8;
    }

    private LPBFileLoader() {
	// Do nothing
    }
}