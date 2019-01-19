package com.puttysoftware.lasertank.datatypes;

import com.puttysoftware.lasertank.dataload.DataLoader;

public class LevelObjectData {
    // Private constructor
    private LevelObjectData() {
	// Do nothing
    }

    public static boolean isObjectSolid(final int objID) {
	return Boolean.parseBoolean(DataLoader.loadSolid(objID));
    }

    public static boolean isObjectAttributeSolid(final int attrID) {
	return Boolean.parseBoolean(DataLoader.loadAttributeSolid(attrID));
    }
}
