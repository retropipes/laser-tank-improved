package com.puttysoftware.lasertank.datatypes;

import com.puttysoftware.storage.NumberStorage;

class LaserTankLevelStorage extends NumberStorage {
    public LaserTankLevelStorage(int... shape) {
	super(shape);
    }

    public LaserTankLevelStorage(LaserTankLevelStorage source) {
	super(source);
    }
}
