package com.puttysoftware.lasertank.datatypes;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.puttysoftware.fileio.GameIOUtilities;
import com.puttysoftware.fileio.GameIOUtilities.ImageBMP;
import com.puttysoftware.images.BufferedImageIcon;

public class LaserTankGraphics {
    public static class LTGLoadException extends IOException {
	private static final long serialVersionUID = 2667335570076496956L;

	public LTGLoadException() {
	    super();
	}
    }

    // Constants
    private static final String FILE_ID = "LTG1";
    private static final int FILE_ID_LEN = 5;
    private static final int NAME_LEN = 40;
    private static final int AUTHOR_LEN = 30;
    private static final int INFO_LEN = 245;

    public static LaserTankGraphics loadFromFile(final File file) throws IOException {
	try (FileInputStream fs = new FileInputStream(file)) {
	    return LaserTankGraphics.loadFromStream(fs);
	}
    }

    public static LaserTankGraphics loadFromResource(final String resource) throws IOException {
	try (InputStream fs = LaserTankGraphics.class.getResourceAsStream(resource)) {
	    return LaserTankGraphics.loadFromStream(fs);
	}
    }

    // Internal stuff
    private static LaserTankGraphics loadFromStream(final InputStream fs) throws IOException {
	int bytesRead = 0;
	// Load file ID
	final byte[] fileIdData = new byte[LaserTankGraphics.FILE_ID_LEN];
	fs.read(fileIdData);
	final String loadFileId = GameIOUtilities.decodeWindowsStringData(fileIdData);
	// Check for a valid ID
	if (!LaserTankGraphics.FILE_ID.equals(loadFileId)) {
	    throw new LTGLoadException();
	}
	// Load name
	final byte[] nameData = new byte[LaserTankGraphics.NAME_LEN];
	bytesRead = fs.read(nameData);
	if (bytesRead < LaserTankGraphics.NAME_LEN) {
	    throw new LTGLoadException();
	}
	final String loadName = GameIOUtilities.decodeWindowsStringData(nameData);
	// Load author
	final byte[] authorData = new byte[LaserTankGraphics.AUTHOR_LEN];
	bytesRead = fs.read(authorData);
	if (bytesRead < LaserTankGraphics.AUTHOR_LEN) {
	    throw new LTGLoadException();
	}
	final String loadAuthor = GameIOUtilities.decodeWindowsStringData(authorData);
	// Load info
	final byte[] infoData = new byte[LaserTankGraphics.INFO_LEN];
	bytesRead = fs.read(infoData);
	if (bytesRead < LaserTankGraphics.INFO_LEN) {
	    throw new LTGLoadException();
	}
	final String loadInfo = GameIOUtilities.decodeWindowsStringData(infoData);
	// Load game image
	final BufferedImageIcon loadGame = ImageBMP.readFromStream(fs).convertToGameImage();
	// Load mask image
	final BufferedImageIcon loadMask = ImageBMP.readFromStream(fs).convertToGameImage();
	// Merge game and mask images
	final BufferedImageIcon mergeGraphics = LaserTankGraphics.mergeGameAndMask(loadGame, loadMask);
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

    public final String getAuthor() {
	return this.author;
    }

    public final BufferedImageIcon getGraphics() {
	return this.graphics;
    }

    public final String getInfo() {
	return this.info;
    }

    // Methods
    public final String getName() {
	return this.name;
    }
}
