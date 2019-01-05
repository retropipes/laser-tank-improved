package com.puttysoftware.fileio;

import java.io.IOException;

public final class GameIOException extends IOException {
    private static final long serialVersionUID = -1106710258661927534L;

    public GameIOException() {
	super();
    }

    public GameIOException(final String msg) {
	super(msg);
    }
}
