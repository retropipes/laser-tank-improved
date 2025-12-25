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
    public byte[] readBytes(final int len) throws IOException {
	final byte[] buf = new byte[len];
	this.raf.read(buf);
	return buf;
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

    @Override
    public int readUnsignedByte() throws IOException {
	return this.raf.readUnsignedByte();
    }

    @Override
    public int readUnsignedShortByteArrayAsInt() throws IOException {
	final byte[] buf = new byte[Short.BYTES];
	this.raf.read(buf);
	return GameIOUtilities.unsignedShortByteArrayToInt(buf);
    }

    @Override
    public String readWindowsString(final byte[] buf) throws IOException {
	this.raf.read(buf);
	return GameIOUtilities.decodeWindowsStringData(buf);
    }

    @Override
    public boolean atEOF() throws IOException {
	return this.raf.getFilePointer() == this.raf.length();
    }
}
