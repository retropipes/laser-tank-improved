package com.puttysoftware.fileio;

import java.io.IOException;
import java.io.RandomAccessFile;

public class GameIODataReader extends GameIOReader {
    // Fields
    private final RandomAccessFile raf;

    // Constructors
    public GameIODataReader(final String filename) throws IOException {
	this.raf = new RandomAccessFile(filename, "r");
    }

    // Methods
    @Override
    public void close() throws IOException {
	this.raf.close();
    }

    @Override
    public boolean readBoolean() throws IOException {
	return this.raf.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
	return this.raf.readByte();
    }

    @Override
    public double readDouble() throws IOException {
	return this.raf.readDouble();
    }

    @Override
    public int readInt() throws IOException {
	return this.raf.readInt();
    }

    @Override
    public long readLong() throws IOException {
	return this.raf.readLong();
    }

    @Override
    public String readString() throws IOException {
	return this.raf.readUTF();
    }
}
