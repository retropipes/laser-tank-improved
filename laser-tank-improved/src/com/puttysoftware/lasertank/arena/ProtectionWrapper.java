package com.puttysoftware.lasertank.arena;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.strings.DialogString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.utilities.InvalidArenaException;

public class ProtectionWrapper {
    // Constants
    private static final int BLOCK_MULTIPLIER = 16;

    private static char[] getTransform() {
	return CommonDialogs.showPasswordInputDialog(StringLoader.loadDialog(DialogString.PROTECTION_PROMPT),
		StringLoader.loadDialog(DialogString.PROTECTION_TITLE), 15);
    }

    public static void protect(final File src, final File dst) throws IOException {
	try (FileInputStream in = new FileInputStream(src); FileOutputStream out = new FileOutputStream(dst)) {
	    final char[] transform = ProtectionWrapper.getTransform();
	    if (transform == null) {
		throw new ProtectionCancelException();
	    }
	    final byte[] buf = new byte[transform.length * ProtectionWrapper.BLOCK_MULTIPLIER];
	    int len;
	    while ((len = in.read(buf)) > 0) {
		for (int x = 0; x < buf.length; x++) {
		    buf[x] += transform[x % transform.length];
		}
		out.write(buf, 0, len);
	    }
	} catch (final IOException ioe) {
	    throw new InvalidArenaException(ioe);
	}
    }

    public static void unprotect(final File src, final File dst) throws IOException {
	try (FileInputStream in = new FileInputStream(src); FileOutputStream out = new FileOutputStream(dst)) {
	    final char[] transform = ProtectionWrapper.getTransform();
	    if (transform == null) {
		throw new ProtectionCancelException();
	    }
	    final byte[] buf = new byte[transform.length * ProtectionWrapper.BLOCK_MULTIPLIER];
	    int len;
	    while ((len = in.read(buf)) > 0) {
		for (int x = 0; x < buf.length; x++) {
		    buf[x] -= transform[x % transform.length];
		}
		out.write(buf, 0, len);
	    }
	} catch (final IOException ioe) {
	    throw new InvalidArenaException(ioe);
	}
    }

    private ProtectionWrapper() {
	// Do nothing
    }
}
