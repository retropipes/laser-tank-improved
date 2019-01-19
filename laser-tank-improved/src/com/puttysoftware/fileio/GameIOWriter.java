package com.puttysoftware.fileio;

import java.io.IOException;

public abstract class GameIOWriter implements AutoCloseable {
    // Constructors
    protected GameIOWriter() throws IOException {
	super();
    }

    // Methods
    @Override
    public abstract void close() throws IOException;

    public abstract void writeBoolean(final boolean value) throws IOException;

    public abstract void writeByte(final byte value) throws IOException;

    public abstract void writeDouble(final double value) throws IOException;

    public abstract void writeInt(final int value) throws IOException;

    public abstract void writeLong(final long value) throws IOException;

    public abstract void writeString(final String value) throws IOException;

    public abstract void writeUnsignedByte(final int value) throws IOException;
}
