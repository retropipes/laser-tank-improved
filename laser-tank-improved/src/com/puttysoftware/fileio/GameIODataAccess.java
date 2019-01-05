package com.puttysoftware.fileio;

import java.io.IOException;
import java.io.RandomAccessFile;

public class GameIODataAccess implements AutoCloseable {
    // Fields
    private final RandomAccessFile raf;

    // Constructors
    public GameIODataAccess(final String filename) throws IOException {
	this.raf = new RandomAccessFile(filename, "rwd");
    }

    // Methods
    @Override
    public void close() throws IOException {
	this.raf.close();
    }

    public boolean readBoolean() throws IOException {
	return this.raf.readBoolean();
    }

    public byte readByte() throws IOException {
	return this.raf.readByte();
    }

    public double readDouble() throws IOException {
	return this.raf.readDouble();
    }

    public int readInt() throws IOException {
	return this.raf.readInt();
    }

    public long readLong() throws IOException {
	return this.raf.readLong();
    }

    public String readString() throws IOException {
	return this.raf.readUTF();
    }

    public void writeBoolean(final boolean value) throws IOException {
	this.raf.writeBoolean(value);
    }

    public void writeByte(final byte value) throws IOException {
	this.raf.writeByte(value);
    }

    public void writeDouble(final double value) throws IOException {
	this.raf.writeDouble(value);
    }

    public void writeInt(final int value) throws IOException {
	this.raf.writeInt(value);
    }

    public void writeLong(final long value) throws IOException {
	this.raf.writeLong(value);
    }

    public void writeString(final String value) throws IOException {
	this.raf.writeUTF(value);
    }
}
