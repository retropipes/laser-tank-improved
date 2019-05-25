package com.puttysoftware.fileio;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.puttysoftware.images.BufferedImageIcon;

public class GameIOUtilities {
    public static class ByteOrder {
	public static void reverse(final byte[] bytesToConvert) {
	    final int numberOfBytes = bytesToConvert.length;
	    final int numberOfBytesHalf = numberOfBytes / 2;
	    for (int b = 0; b < numberOfBytesHalf; b++) {
		final byte byteFromStart = bytesToConvert[b];
		bytesToConvert[b] = bytesToConvert[numberOfBytes - 1 - b];
		bytesToConvert[numberOfBytes - 1 - b] = byteFromStart;
	    }
	}

	public static int reverse(final int intToReverse) {
	    final byte[] intAsBytes = new byte[] { (byte) (intToReverse & 0xFF), (byte) (intToReverse >> 8 & 0xFF),
		    (byte) (intToReverse >> 16 & 0xFF), (byte) (intToReverse >> 24 & 0xFF), };
	    return (intAsBytes[3] & 0xFF) + ((intAsBytes[2] & 0xFF) << 8) + ((intAsBytes[1] & 0xFF) << 16)
		    + ((intAsBytes[0] & 0xFF) << 24);
	}

	public static long reverse(final long valueToReverse) {
	    final byte[] valueAsBytes = new byte[] { (byte) (valueToReverse & 0xFF),
		    (byte) (valueToReverse >> 8 & 0xFF), (byte) (valueToReverse >> 16 & 0xFF),
		    (byte) (valueToReverse >> 24 & 0xFF), (byte) (valueToReverse >> 32 & 0xFF),
		    (byte) (valueToReverse >> 40 & 0xFF), (byte) (valueToReverse >> 48 & 0xFF),
		    (byte) (valueToReverse >> 56 & 0xFF), };
	    long returnValue = valueAsBytes[7] & 0xFF;
	    returnValue += (valueAsBytes[6] & 0xFF) << 8;
	    returnValue += (valueAsBytes[5] & 0xFF) << 16;
	    returnValue += (valueAsBytes[4] & 0xFF) << 24;
	    returnValue += (valueAsBytes[3] & 0xFF) << 32;
	    returnValue += (valueAsBytes[2] & 0xFF) << 40;
	    returnValue += (valueAsBytes[1] & 0xFF) << 48;
	    returnValue += (valueAsBytes[0] & 0xFF) << 56;
	    return returnValue;
	}

	public static short reverse(final short valueToReverse) {
	    final byte[] valueAsBytes = new byte[] { (byte) (valueToReverse & 0xFF),
		    (byte) (valueToReverse >> 8 & 0xFF), };
	    return (short) ((valueAsBytes[1] & 0xFF) + ((valueAsBytes[0] & 0xFF) << 8));
	}
    }

    public static class Coords {
	public int x;
	public int y;

	public Coords(final int ix, final int iy) {
	    this.x = ix;
	    this.y = iy;
	}
    }

    public static class DataInputStreamLittleEndian implements AutoCloseable {
	private final DataInputStream systemStream;

	public DataInputStreamLittleEndian(final DataInputStream newSystemStream) {
	    this.systemStream = newSystemStream;
	}

	@Override
	public void close() throws IOException {
	    this.systemStream.close();
	}

	public void read(final byte[] bytesToReadInto) throws IOException {
	    this.systemStream.read(bytesToReadInto);
	}

	public int readInt() throws IOException {
	    return ByteOrder.reverse(this.systemStream.readInt());
	}

	public long readLong() throws IOException {
	    return ByteOrder.reverse(this.systemStream.readLong());
	}

	public short readShort() throws IOException {
	    return ByteOrder.reverse(this.systemStream.readShort());
	}

	public String readString(final int numberOfCharacters) throws IOException {
	    final byte[] bytesRead = new byte[numberOfCharacters];
	    this.systemStream.read(bytesRead);
	    return new String(bytesRead);
	}
    }

    public static class ImageBMP {
	public static abstract class DIBHeader {
	    public static class Instances {
		public static DIBHeader BitmapInfo = new DIBHeaderBitmapInfo();
		// public static DIBHeader BitmapV5 = new DIBHeaderV5();
	    }

	    public static DIBHeader buildFromStream(final DataInputStreamLittleEndian reader) {
		DIBHeader returnValue = null;
		try {
		    final int dibHeaderSizeInBytes = reader.readInt();
		    // hack
		    if (dibHeaderSizeInBytes == 40) {
			returnValue = new DIBHeaderBitmapInfo().readFromStream(reader);
		    }
		} catch (final IOException ex) {
		    ex.printStackTrace();
		}
		return returnValue;
	    }

	    public String name;
	    public int sizeInBytes;

	    public DIBHeader(final String newName, final int newSizeInBytes) {
		this.name = newName;
		this.sizeInBytes = newSizeInBytes;
	    }

	    // abstract method headers
	    public abstract int bitsPerPixel();

	    public abstract int imageSizeInBytes();

	    public abstract Coords imageSizeInPixels();

	    public abstract DIBHeader readFromStream(DataInputStreamLittleEndian reader);
	}

	public static class DIBHeaderBitmapInfo extends DIBHeader {
	    public Coords imageSizeInPixels;
	    public short planes;
	    public short bitsPerPixel;
	    public int compression;
	    public int imageSizeInBytes;
	    public Coords pixelsPerMeter;
	    public int numberOfColorsInPalette;
	    public int numberOfColorsUsed;

	    public DIBHeaderBitmapInfo() {
		super("BitmapInfo", 40);
	    }

	    public DIBHeaderBitmapInfo(final int newSizeInBytes, final Coords newImageSizeInPixels,
		    final short newPlanes, final short newBitsPerPixel, final int newCompression,
		    final int newImageSizeInBytes, final Coords newPixelsPerMeter, final int newNumberOfColorsInPalette,
		    final int newNumberOfColorsUsed) {
		this();
		this.sizeInBytes = newSizeInBytes;
		this.imageSizeInPixels = newImageSizeInPixels;
		this.planes = newPlanes;
		this.bitsPerPixel = newBitsPerPixel;
		this.compression = newCompression;
		this.imageSizeInBytes = newImageSizeInBytes;
		this.pixelsPerMeter = newPixelsPerMeter;
		this.numberOfColorsInPalette = newNumberOfColorsInPalette;
		this.numberOfColorsUsed = newNumberOfColorsUsed;
		if (this.imageSizeInBytes == 0) {
		    this.imageSizeInBytes = this.imageSizeInPixels.x * this.imageSizeInPixels.y * this.bitsPerPixel / 8;
		}
	    }

	    // DIBHeader members
	    @Override
	    public int bitsPerPixel() {
		return this.bitsPerPixel;
	    }

	    @Override
	    public int imageSizeInBytes() {
		return this.imageSizeInBytes;
	    }

	    @Override
	    public Coords imageSizeInPixels() {
		return this.imageSizeInPixels;
	    }

	    @Override
	    public DIBHeader readFromStream(final DataInputStreamLittleEndian reader) {
		DIBHeader dibHeader = null;
		try {
		    dibHeader = new DIBHeaderBitmapInfo(this.sizeInBytes, // dibHeaderSize;
			    // imageSizeInPixels
			    new Coords(reader.readInt(), reader.readInt()), reader.readShort(), // planes;
			    reader.readShort(), // bitsPerPixel;
			    reader.readInt(), // compression;
			    reader.readInt(), // imageSizeInBytes;
			    // pixelsPerMeter
			    new Coords(reader.readInt(), reader.readInt()), reader.readInt(), // numberOfColorsInPalette
			    reader.readInt() // numberOfColorsUsed
		    );
		} catch (final Exception ex) {
		    ex.printStackTrace();
		}
		return dibHeader;
	    }

	    @Override
	    public String toString() {
		final String returnValue = "<DIBHeader " + "size='" + this.sizeInBytes + "' " + "imageSizeInPixels='"
			+ this.imageSizeInPixels.x + "," + this.imageSizeInPixels.y + "' " + "planes='" + this.planes
			+ "' " + "bitsPerPixel='" + this.bitsPerPixel + "' " + "compression='" + this.compression + "' "
			+ "imageSizeInBytes='" + this.imageSizeInBytes + "' " + "pixelsPerMeter='"
			+ this.pixelsPerMeter.x + "," + this.pixelsPerMeter.y + "' " + "numberOfColorsInPalette='"
			+ this.numberOfColorsInPalette + "' " + "numberOfColorsUsed='" + this.numberOfColorsUsed + "' "
			+ "/>";
		return returnValue;
	    }
	}

	// inner classes
	public static class FileHeader {
	    public static FileHeader readFromStream(final DataInputStreamLittleEndian reader) {
		FileHeader returnValue = null;
		try {
		    returnValue = new FileHeader(reader.readString(2), // signature
			    reader.readInt(), // fileSize,
			    reader.readShort(), // reserved1
			    reader.readShort(), // reserved2
			    reader.readInt() // fileOffsetToPixelArray
		    );
		} catch (final IOException ex) {
		    ex.printStackTrace();
		}
		return returnValue;
	    }

	    // 14 bytes
	    public String signature;
	    public int fileSize;
	    public short reserved1;
	    public short reserved2;
	    public int fileOffsetToPixelArray;

	    public FileHeader(final String newSignature, final int newFileSize, final short newReserved1,
		    final short newReserved2, final int newFileOffsetToPixelArray) {
		this.signature = newSignature;
		this.fileSize = newFileSize;
		this.reserved1 = newReserved1;
		this.reserved2 = newReserved2;
		this.fileOffsetToPixelArray = newFileOffsetToPixelArray;
	    }

	    @Override
	    public String toString() {
		final String returnValue = "<FileHeader " + "signature='" + this.signature + "' " + "fileSize='"
			+ this.fileSize + "' " + "fileOffsetToPixelArray ='" + this.fileOffsetToPixelArray + "' "
			+ "/>";
		return returnValue;
	    }
	}

	public static ImageBMP readFromStream(final InputStream is) throws IOException {
	    ImageBMP returnValue = null;
	    try (DataInputStreamLittleEndian reader = new DataInputStreamLittleEndian(new DataInputStream(is))) {
		final FileHeader fileHeader = FileHeader.readFromStream(reader);
		final DIBHeader dibHeader = DIBHeader.buildFromStream(reader);
		final int[] colorTable = new int[] {};
		final int numberOfBytesInPixelData = dibHeader.imageSizeInBytes();
		final byte[] pixelData = new byte[numberOfBytesInPixelData];
		reader.read(pixelData);
		returnValue = new ImageBMP(fileHeader, dibHeader, colorTable, pixelData);
	    }
	    return returnValue;
	}

	public FileHeader fileHeader;
	public DIBHeader dibHeader;
	public int[] colorTable;
	public byte[] pixelData;

	public ImageBMP(final FileHeader newFileHeader, final DIBHeader newDibHeader, final int[] newColorTable,
		final byte[] newPixelData) {
	    this.fileHeader = newFileHeader;
	    this.dibHeader = newDibHeader;
	    this.colorTable = newColorTable;
	    this.pixelData = newPixelData;
	}

	public BufferedImageIcon convertToGameImage() {
	    // hack
	    // We're assuming things about the color model in this method
	    // that may not necessarily be true in all .BMP files.
	    final Coords imageSizeInPixels = this.dibHeader.imageSizeInPixels();
	    java.awt.image.BufferedImage returnValue;
	    returnValue = new java.awt.image.BufferedImage(imageSizeInPixels.x, imageSizeInPixels.y,
		    java.awt.image.BufferedImage.TYPE_INT_ARGB);
	    final int bitsPerPixel = this.dibHeader.bitsPerPixel();
	    final int bytesPerPixel = bitsPerPixel / 8;
	    final int colorOpaqueBlackAsArgb = 0xFF << bytesPerPixel * 8;
	    for (int y = 0; y < imageSizeInPixels.y; y++) {
		for (int x = 0; x < imageSizeInPixels.x; x++) {
		    final int bitOffsetForPixel = ((imageSizeInPixels.y - y - 1) // invert y
			    * imageSizeInPixels.x + x) * bitsPerPixel;
		    final int byteOffsetForPixel = bitOffsetForPixel / 8;
		    int pixelColorArgb = colorOpaqueBlackAsArgb;
		    for (int b = 0; b < bytesPerPixel; b++) {
			pixelColorArgb += (this.pixelData[byteOffsetForPixel + b] & 0xFF) << 8 * b;
		    }
		    returnValue.setRGB(x, y, pixelColorArgb);
		}
	    }
	    return new BufferedImageIcon(returnValue);
	}
    }

    public static String decodeWindowsStringData(final byte[] data) {
	return Charset.forName("ISO-8859-1").decode(ByteBuffer.wrap(data)).toString();
    }

    public static long readUnsignedInt(final DataInput data) throws IOException {
	long val = data.readInt();
	if (val < 0) {
	    val += 0X100000000L;
	}
	return val;
    }

    public static int unsignedShortByteArrayToInt(final byte[] d) {
	if (d == null || d.length != 2) {
	    return 0x0;
	}
	return 0xff & d[0] | (0xff & d[1]) << 8;
    }
}
