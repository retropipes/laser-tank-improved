package com.puttysoftware.lasertank.dataload;

class DataFileNames {
    // Static fields
    private static String[] LIST = new String[] { "rulesmap_solid", "solid_attribute", "directions", "frames",
	    "friction", "indexes", "movable", "shoots", "weight" };

    // Private constructor
    private DataFileNames() {
	// Do nothing
    }

    // Static methods
    static int getFileCount() {
	return DataFileNames.LIST.length;
    }

    static String getFileName(final DataFile file) {
	return DataFileNames.LIST[file.ordinal()];
    }
}
