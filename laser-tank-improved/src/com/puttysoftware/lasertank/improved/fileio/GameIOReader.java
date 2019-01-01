package com.puttysoftware.lasertank.improved.fileio;

import java.io.IOException;

public abstract class GameIOReader implements AutoCloseable {
    // Constructors
    protected GameIOReader() throws IOException {
	super();
    }

    public abstract boolean readBoolean() throws IOException;

    public abstract byte readByte() throws IOException;

    // Methods
    public abstract double readDouble() throws IOException;

    public abstract int readInt() throws IOException;

    public abstract long readLong() throws IOException;

    public abstract String readString() throws IOException;
}
