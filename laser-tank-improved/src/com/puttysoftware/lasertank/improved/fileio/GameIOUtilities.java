package com.puttysoftware.lasertank.improved.fileio;

import java.io.DataInput;
import java.io.IOException;

public class GameIOUtilities {
    public static long readUnsignedInt(final DataInput data) throws IOException {
	long val = data.readInt();
	if (val < 0) {
	    val += 0X100000000L;
	}
	return val;
    }
}
