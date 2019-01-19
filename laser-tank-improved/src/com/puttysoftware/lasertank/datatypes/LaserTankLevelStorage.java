package com.puttysoftware.lasertank.datatypes;

import java.io.IOException;

import com.puttysoftware.fileio.GameIOReader;
import com.puttysoftware.fileio.GameIOWriter;
import com.puttysoftware.storage.NumberStorage;

class LaserTankLevelStorage extends NumberStorage {
    public LaserTankLevelStorage(int... shape) {
	super(shape);
    }

    public LaserTankLevelStorage(LaserTankLevelStorage source) {
	super(source);
    }

    public void save(final GameIOWriter gio) throws IOException {
	int[] shape = this.getShape();
	int shapeLen = shape.length;
	gio.writeInt(shapeLen);
	for (int s = 0; s < shapeLen; s++) {
	    gio.writeInt(shape[s]);
	}
	int rawLength = this.getRawLength();
	gio.writeInt(rawLength);
	for (int d = 0; d < rawLength; d++) {
	    gio.writeInt(this.getRawCell(d));
	}
    }

    public static LaserTankLevelStorage load(final GameIOReader gio) throws IOException {
	int shapeLen = gio.readInt();
	int[] shape = new int[shapeLen];
	for (int s = 0; s < shapeLen; s++) {
	    shape[s] = gio.readInt();
	}
	LaserTankLevelStorage obj = new LaserTankLevelStorage(shape);
	int rawLength = gio.readInt();
	for (int d = 0; d < rawLength; d++) {
	    obj.setRawCell(gio.readInt(), d);
	}
	return obj;
    }
}
