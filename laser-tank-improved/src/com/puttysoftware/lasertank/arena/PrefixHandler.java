/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena;

import java.io.IOException;

import com.puttysoftware.fileio.XMLFileReader;
import com.puttysoftware.fileio.XMLFileWriter;
import com.puttysoftware.lasertank.strings.ErrorString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.utilities.FormatConstants;

public class PrefixHandler implements AbstractPrefixIO {
    private static final byte FORMAT_VERSION = (byte) FormatConstants.ARENA_FORMAT_LATEST;

    private static boolean checkFormatVersion(final byte version) {
	if (version > PrefixHandler.FORMAT_VERSION) {
	    return false;
	} else {
	    return true;
	}
    }

    private static byte readFormatVersion(final XMLFileReader reader) throws IOException {
	return reader.readByte();
    }

    private static void writeFormatVersion(final XMLFileWriter writer) throws IOException {
	writer.writeByte(PrefixHandler.FORMAT_VERSION);
    }

    @Override
    public int readPrefix(final XMLFileReader reader) throws IOException {
	final byte formatVer = PrefixHandler.readFormatVersion(reader);
	final boolean res = PrefixHandler.checkFormatVersion(formatVer);
	if (!res) {
	    throw new IOException(StringLoader.loadError(ErrorString.UNKNOWN_ARENA_FORMAT));
	}
	return formatVer;
    }

    @Override
    public void writePrefix(final XMLFileWriter writer) throws IOException {
	PrefixHandler.writeFormatVersion(writer);
    }
}
