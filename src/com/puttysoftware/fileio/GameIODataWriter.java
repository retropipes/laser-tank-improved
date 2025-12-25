package com.puttysoftware.fileio;

import java.io.IOException;
import java.io.RandomAccessFile;

public class GameIODataWriter extends GameIOWriter {
    // Fields
    private final RandomAccessFile raf;

    // Constructors
    public GameIODataWriter(final String filename) throws IOException {
	this.raf = new RandomAccessFile(filename, "rwd");
    }

    // Methods
    @Override
    public void close() throws IOException {
	this.raf.close();
    }

    @Override
    public void writeBoolean(final boolean value) throws IOException {
	this.raf.writeBoolean(value);
    }

    @Override
    public void writeByte(final byte value) throws IOException {
	this.raf.writeByte(value);
    }

    @Override
    public void writeDouble(final double value) throws IOException {
	this.raf.writeDouble(value);
    }

    @Override
    public void writeInt(final int value) throws IOException {
	this.raf.writeInt(value);
    }

    @Override
    public void writeLong(final long value) throws IOException {
	this.raf.writeLong(value);
    }

    @Override
    public void writeString(final String value) throws IOException {
	this.raf.writeUTF(value);
    }

    @Override
    public void writeUnsignedByte(final int value) throws IOException {
	this.raf.writeByte(value);
    }
}
