package com.puttysoftware.fileio;

import java.io.IOException;

public abstract class GameIOReader implements AutoCloseable {
    // Constructors
    protected GameIOReader() throws IOException {
	super();
    }

    // Methods
    @Override
    public abstract void close() throws IOException;

    public abstract boolean readBoolean() throws IOException;

    public abstract byte readByte() throws IOException;
    
    public abstract byte[] readBytes(int len) throws IOException;

    public abstract double readDouble() throws IOException;

    public abstract int readInt() throws IOException;

    public abstract long readLong() throws IOException;

    public abstract String readString() throws IOException;

    public abstract int readUnsignedByte() throws IOException;
    
    public abstract int readUnsignedShortByteArrayAsInt() throws IOException;
    
    public abstract String readWindowsString(byte[] buf) throws IOException;
    
    public abstract boolean atEOF() throws IOException;
}
