package com.puttysoftware.lasertank.datatypes;

import java.io.IOException;

import org.retropipes.diane.storage.NumberStorage;

import com.puttysoftware.fileio.GameIOReader;
import com.puttysoftware.fileio.GameIOWriter;

class LaserTankLevelStorage extends NumberStorage {
    public LaserTankLevelStorage(final int... shape) {
	super(shape);
    }

    public LaserTankLevelStorage(final LaserTankLevelStorage source) {
	super(source);
    }

    public void save(final GameIOWriter gio) throws IOException {
	final int[] shape = this.getShape();
	final int shapeLen = shape.length;
	gio.writeInt(shapeLen);
	for (int s = 0; s < shapeLen; s++) {
	    gio.writeInt(shape[s]);
	}
	final int rawLength = this.getRawLength();
	gio.writeInt(rawLength);
	for (int d = 0; d < rawLength; d++) {
	    gio.writeInt(this.getRawCell(d));
	}
    }

    public static LaserTankLevelStorage load(final GameIOReader gio) throws IOException {
	final int shapeLen = gio.readInt();
	final int[] shape = new int[shapeLen];
	for (int s = 0; s < shapeLen; s++) {
	    shape[s] = gio.readInt();
	}
	final LaserTankLevelStorage obj = new LaserTankLevelStorage(shape);
	final int rawLength = gio.readInt();
	for (int d = 0; d < rawLength; d++) {
	    obj.setRawCell(gio.readInt(), d);
	}
	return obj;
    }
}
