package com.puttysoftware.lasertank.improved.datatypes;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.puttysoftware.lasertank.improved.fileio.GameIOUtilities;
import com.puttysoftware.lasertank.improved.images.BufferedImageIcon;

public class LaserTankGraphics {
    // Constants
    private static final String FILE_ID = "LTG1";
    private static final int FILE_ID_LEN = 5;
    private static final int NAME_LEN = 40;
    private static final int AUTHOR_LEN = 30;
    private static final int INFO_LEN = 245;
    // Fields
    private final String name;
    private final String author;
    private final String info;
    private final BufferedImageIcon graphics;

    // Constructor - used only internally
    private LaserTankGraphics(final String loadName, final String loadAuthor, final String loadInfo,
	    final BufferedImageIcon mergeGraphics) {
	this.name = loadName;
	this.info = loadInfo;
	this.author = loadAuthor;
	this.graphics = mergeGraphics;
    }

    // Methods
    public final String getName() {
	return this.name;
    }

    public final String getAuthor() {
	return this.author;
    }

    public final String getInfo() {
	return this.info;
    }

    public final BufferedImageIcon getGraphics() {
	return this.graphics;
    }

    public static LaserTankGraphics loadFromLTGFile(final File file) throws IOException {
	try (FileInputStream fs = new FileInputStream(file)) {
	    return LaserTankGraphics.loadFromLTG(fs);
	}
    }

    public static LaserTankGraphics loadFromLTGResource(final String resource) throws IOException {
	try (InputStream fs = LaserTankGraphics.class.getResourceAsStream(resource)) {
	    return LaserTankGraphics.loadFromLTG(fs);
	}
    }

    // Internal stuff
    private static LaserTankGraphics loadFromLTG(final InputStream fs) throws IOException {
	// Load file ID
	byte[] fileIdData = new byte[LaserTankGraphics.FILE_ID_LEN];
	fs.read(fileIdData);
	String loadFileId = GameIOUtilities.decodeWindowsStringData(fileIdData);
	// Check for a valid ID
	if (!LaserTankGraphics.FILE_ID.equals(loadFileId)) {
	    throw new LTGLoadException();
	}
	// Load name
	byte[] nameData = new byte[LaserTankGraphics.NAME_LEN];
	fs.read(nameData);
	String loadName = GameIOUtilities.decodeWindowsStringData(nameData);
	// Load author
	byte[] authorData = new byte[LaserTankGraphics.AUTHOR_LEN];
	fs.read(authorData);
	String loadAuthor = GameIOUtilities.decodeWindowsStringData(authorData);
	// Load info
	byte[] infoData = new byte[LaserTankGraphics.INFO_LEN];
	fs.read(infoData);
	String loadInfo = GameIOUtilities.decodeWindowsStringData(infoData);
	// Load game image
	BufferedImageIcon loadGame = ImageBMP.readFromStream(fs).convertToGameImage();
	// Load mask image
	BufferedImageIcon loadMask = ImageBMP.readFromStream(fs).convertToGameImage();
	// Merge game and mask images
	BufferedImageIcon mergeGraphics = LaserTankGraphics.mergeGameAndMask(loadGame, loadMask);
	// Return final result
	return new LaserTankGraphics(loadName, loadAuthor, loadInfo, mergeGraphics);
    }

    private static BufferedImageIcon mergeGameAndMask(final BufferedImageIcon game, final BufferedImageIcon mask) {
	final Color MASK_TRANSPARENT = Color.black;
	final int GAME_TRANSPARENT = new Color(200, 100, 100, 0).getRGB();
	if (game != null && mask != null) {
	    final int gWidth = game.getWidth();
	    final int mWidth = mask.getWidth();
	    final int gHeight = game.getHeight();
	    final int mHeight = mask.getHeight();
	    if (gWidth == mWidth && gHeight == mHeight) {
		final int width = gWidth;
		final int height = gHeight;
		final BufferedImageIcon result = new BufferedImageIcon(game);
		for (int x = 0; x < width; x++) {
		    for (int y = 0; y < height; y++) {
			final int pixel = mask.getRGB(x, y);
			final Color c = new Color(pixel);
			if (c.equals(MASK_TRANSPARENT)) {
			    result.setRGB(x, y, GAME_TRANSPARENT);
			}
		    }
		}
		return result;
	    }
	}
	return null;
    }

    public static class LTGLoadException extends IOException {
	private static final long serialVersionUID = 2667335570076496956L;

	public LTGLoadException() {
	    super();
	}
    }

    static class ByteOrder {
	public static void reverse(byte[] bytesToConvert) {
	    int numberOfBytes = bytesToConvert.length;
	    int numberOfBytesHalf = numberOfBytes / 2;
	    for (int b = 0; b < numberOfBytesHalf; b++) {
		byte byteFromStart = bytesToConvert[b];
		bytesToConvert[b] = bytesToConvert[numberOfBytes - 1 - b];
		bytesToConvert[numberOfBytes - 1 - b] = byteFromStart;
	    }
	}

	public static int reverse(int intToReverse) {
	    byte[] intAsBytes = new byte[] { (byte) (intToReverse & 0xFF), (byte) ((intToReverse >> 8) & 0xFF),
		    (byte) ((intToReverse >> 16) & 0xFF), (byte) ((intToReverse >> 24) & 0xFF), };
	    return ((intAsBytes[3] & 0xFF) + ((intAsBytes[2] & 0xFF) << 8) + ((intAsBytes[1] & 0xFF) << 16)
		    + ((intAsBytes[0] & 0xFF) << 24));
	}

	public static long reverse(long valueToReverse) {
	    byte[] valueAsBytes = new byte[] { (byte) (valueToReverse & 0xFF), (byte) ((valueToReverse >> 8) & 0xFF),
		    (byte) ((valueToReverse >> 16) & 0xFF), (byte) ((valueToReverse >> 24) & 0xFF),
		    (byte) ((valueToReverse >> 32) & 0xFF), (byte) ((valueToReverse >> 40) & 0xFF),
		    (byte) ((valueToReverse >> 48) & 0xFF), (byte) ((valueToReverse >> 56) & 0xFF), };
	    long returnValue = (valueAsBytes[7] & 0xFF);
	    returnValue += ((valueAsBytes[6] & 0xFF) << 8);
	    returnValue += ((valueAsBytes[5] & 0xFF) << 16);
	    returnValue += ((valueAsBytes[4] & 0xFF) << 24);
	    returnValue += ((valueAsBytes[3] & 0xFF) << 32);
	    returnValue += ((valueAsBytes[2] & 0xFF) << 40);
	    returnValue += ((valueAsBytes[1] & 0xFF) << 48);
	    returnValue += ((valueAsBytes[0] & 0xFF) << 56);
	    return returnValue;
	}

	public static short reverse(short valueToReverse) {
	    byte[] valueAsBytes = new byte[] { (byte) (valueToReverse & 0xFF), (byte) ((valueToReverse >> 8) & 0xFF), };
	    return (short) (((valueAsBytes[1] & 0xFF)) + ((valueAsBytes[0] & 0xFF) << 8));
	}
    }

    static class Coords {
	public int x;
	public int y;

	public Coords(int ix, int iy) {
	    this.x = ix;
	    this.y = iy;
	}
    }

    static class DataInputStreamLittleEndian implements AutoCloseable {
	private DataInputStream systemStream;

	public DataInputStreamLittleEndian(DataInputStream newSystemStream) {
	    this.systemStream = newSystemStream;
	}

	@Override
	public void close() throws IOException {
	    this.systemStream.close();
	}

	public void read(byte[] bytesToReadInto) throws IOException {
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

	public String readString(int numberOfCharacters) throws IOException {
	    byte[] bytesRead = new byte[numberOfCharacters];
	    this.systemStream.read(bytesRead);
	    return new String(bytesRead);
	}
    }

    static class ImageBMP {
	public FileHeader fileHeader;
	public DIBHeader dibHeader;
	public int[] colorTable;
	public byte[] pixelData;

	public ImageBMP(FileHeader newFileHeader, DIBHeader newDibHeader, int[] newColorTable, byte[] newPixelData) {
	    this.fileHeader = newFileHeader;
	    this.dibHeader = newDibHeader;
	    this.colorTable = newColorTable;
	    this.pixelData = newPixelData;
	}

	public BufferedImageIcon convertToGameImage() {
	    // hack
	    // We're assuming things about the color model in this method
	    // that may not necessarily be true in all .BMP files.
	    Coords imageSizeInPixels = this.dibHeader.imageSizeInPixels();
	    java.awt.image.BufferedImage returnValue;
	    returnValue = new java.awt.image.BufferedImage(imageSizeInPixels.x, imageSizeInPixels.y,
		    java.awt.image.BufferedImage.TYPE_INT_ARGB);
	    int bitsPerPixel = this.dibHeader.bitsPerPixel();
	    int bytesPerPixel = bitsPerPixel / 8;
	    int colorOpaqueBlackAsArgb = 0xFF << bytesPerPixel * 8;
	    for (int y = 0; y < imageSizeInPixels.y; y++) {
		for (int x = 0; x < imageSizeInPixels.x; x++) {
		    int bitOffsetForPixel = ((imageSizeInPixels.y - y - 1) // invert y
			    * imageSizeInPixels.x + x) * bitsPerPixel;
		    int byteOffsetForPixel = bitOffsetForPixel / 8;
		    int pixelColorArgb = colorOpaqueBlackAsArgb;
		    for (int b = 0; b < bytesPerPixel; b++) {
			pixelColorArgb += (this.pixelData[byteOffsetForPixel + b] & 0xFF) << (8 * b);
		    }
		    returnValue.setRGB(x, y, pixelColorArgb);
		}
	    }
	    return new BufferedImageIcon(returnValue);
	}

	public static ImageBMP readFromStream(InputStream is) throws IOException {
	    ImageBMP returnValue = null;
	    try (DataInputStreamLittleEndian reader = new DataInputStreamLittleEndian(new DataInputStream(is))) {
		FileHeader fileHeader = FileHeader.readFromStream(reader);
		DIBHeader dibHeader = DIBHeader.buildFromStream(reader);
		int[] colorTable = dibHeader.readColorTable(reader);
		int numberOfBytesInPixelData = dibHeader.imageSizeInBytes();
		byte[] pixelData = new byte[numberOfBytesInPixelData];
		reader.read(pixelData);
		returnValue = new ImageBMP(fileHeader, dibHeader, colorTable, pixelData);
	    }
	    return returnValue;
	}

	// inner classes
	public static class FileHeader {
	    // 14 bytes
	    public String signature;
	    public int fileSize;
	    public short reserved1;
	    public short reserved2;
	    public int fileOffsetToPixelArray;

	    public FileHeader(String newSignature, int newFileSize, short newReserved1, short newReserved2,
		    int newFileOffsetToPixelArray) {
		this.signature = newSignature;
		this.fileSize = newFileSize;
		this.reserved1 = newReserved1;
		this.reserved2 = newReserved2;
		this.fileOffsetToPixelArray = newFileOffsetToPixelArray;
	    }

	    public static FileHeader readFromStream(DataInputStreamLittleEndian reader) {
		FileHeader returnValue = null;
		try {
		    returnValue = new FileHeader(reader.readString(2), // signature
			    reader.readInt(), // fileSize,
			    reader.readShort(), // reserved1
			    reader.readShort(), // reserved2
			    reader.readInt() // fileOffsetToPixelArray
		    );
		} catch (IOException ex) {
		    ex.printStackTrace();
		}
		return returnValue;
	    }

	    @Override
	    public String toString() {
		String returnValue = "<FileHeader " + "signature='" + this.signature + "' " + "fileSize='"
			+ this.fileSize + "' " + "fileOffsetToPixelArray ='" + this.fileOffsetToPixelArray + "' "
			+ "/>";
		return returnValue;
	    }
	}

	public static abstract class DIBHeader {
	    public String name;
	    public int sizeInBytes;

	    public DIBHeader(String newName, int newSizeInBytes) {
		this.name = newName;
		this.sizeInBytes = newSizeInBytes;
	    }

	    public static class Instances {
		public static DIBHeader BitmapInfo = new DIBHeaderBitmapInfo();
		// public static DIBHeader BitmapV5 = new DIBHeaderV5();
	    }

	    public static DIBHeader buildFromStream(DataInputStreamLittleEndian reader) {
		DIBHeader returnValue = null;
		try {
		    int dibHeaderSizeInBytes = reader.readInt();
		    // hack
		    if (dibHeaderSizeInBytes == 40) {
			returnValue = new DIBHeaderBitmapInfo().readFromStream(reader);
		    }
		} catch (IOException ex) {
		    ex.printStackTrace();
		}
		return returnValue;
	    }

	    public int[] readColorTable(DataInputStreamLittleEndian reader) {
		// todo
		return new int[] {};
	    }

	    // abstract method headers
	    public abstract int bitsPerPixel();

	    public abstract DIBHeader readFromStream(DataInputStreamLittleEndian reader);

	    public abstract int imageSizeInBytes();

	    public abstract Coords imageSizeInPixels();
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

	    public DIBHeaderBitmapInfo(int newSizeInBytes, Coords newImageSizeInPixels, short newPlanes,
		    short newBitsPerPixel, int newCompression, int newImageSizeInBytes, Coords newPixelsPerMeter,
		    int newNumberOfColorsInPalette, int newNumberOfColorsUsed) {
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

	    @Override
	    public String toString() {
		String returnValue = "<DIBHeader " + "size='" + this.sizeInBytes + "' " + "imageSizeInPixels='"
			+ this.imageSizeInPixels.x + "," + this.imageSizeInPixels.y + "' " + "planes='" + this.planes
			+ "' " + "bitsPerPixel='" + this.bitsPerPixel + "' " + "compression='" + this.compression + "' "
			+ "imageSizeInBytes='" + this.imageSizeInBytes + "' " + "pixelsPerMeter='"
			+ this.pixelsPerMeter.x + "," + this.pixelsPerMeter.y + "' " + "numberOfColorsInPalette='"
			+ this.numberOfColorsInPalette + "' " + "numberOfColorsUsed='" + this.numberOfColorsUsed + "' "
			+ "/>";
		return returnValue;
	    }

	    // DIBHeader members
	    @Override
	    public int bitsPerPixel() {
		return this.bitsPerPixel;
	    }

	    @Override
	    public DIBHeader readFromStream(DataInputStreamLittleEndian reader) {
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
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
		return dibHeader;
	    }

	    @Override
	    public int imageSizeInBytes() {
		return this.imageSizeInBytes;
	    }

	    @Override
	    public Coords imageSizeInPixels() {
		return this.imageSizeInPixels;
	    }
	}
    }
}
