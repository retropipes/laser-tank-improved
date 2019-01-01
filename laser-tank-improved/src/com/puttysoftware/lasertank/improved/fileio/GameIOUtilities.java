package com.puttysoftware.lasertank.improved.fileio;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class GameIOUtilities {
    public static long readUnsignedInt(final DataInput data) throws IOException {
	long val = data.readInt();
	if (val < 0) {
	    val += 0X100000000L;
	}
	return val;
    }

    public static String decodeWindowsStringData(byte[] data) {
	return Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(data)).toString();
    }
}
