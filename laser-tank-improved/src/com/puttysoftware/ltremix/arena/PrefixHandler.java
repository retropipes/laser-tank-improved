/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena;

import java.io.IOException;

import com.puttysoftware.lasertank.improved.fileio.XMLFileReader;
import com.puttysoftware.lasertank.improved.fileio.XMLFileWriter;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;
import com.puttysoftware.ltremix.utilities.FormatConstants;

public class PrefixHandler implements AbstractPrefixIO {
    private static final byte FORMAT_VERSION = (byte) FormatConstants.ARENA_FORMAT_LATEST;

    @Override
    public int readPrefix(final XMLFileReader reader) throws IOException {
	final byte formatVer = PrefixHandler.readFormatVersion(reader);
	final boolean res = PrefixHandler.checkFormatVersion(formatVer);
	if (!res) {
	    throw new IOException(StringLoader.loadString(StringConstants.ERROR_STRINGS_FILE,
		    StringConstants.ERROR_STRING_UNKNOWN_ARENA_FORMAT));
	}
	return formatVer;
    }

    @Override
    public void writePrefix(final XMLFileWriter writer) throws IOException {
	PrefixHandler.writeFormatVersion(writer);
    }

    private static byte readFormatVersion(final XMLFileReader reader) throws IOException {
	return reader.readByte();
    }

    private static boolean checkFormatVersion(final byte version) {
	if (version > PrefixHandler.FORMAT_VERSION) {
	    return false;
	} else {
	    return true;
	}
    }

    private static void writeFormatVersion(final XMLFileWriter writer) throws IOException {
	writer.writeByte(PrefixHandler.FORMAT_VERSION);
    }
}
