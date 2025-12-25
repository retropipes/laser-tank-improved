package com.puttysoftware.lasertank.dataload;

class DataFileNames {
    // Static fields
    private static String[] LIST = new String[] { "rulesmap_solid", "solid_attribute", "directions", "frames",
	    "friction", "indexes", "movable", "shoots", "weight", "transform_fire", "transform_ice", "transform_stone",
	    "material", "height", "layer", "lethal", "reflect" };

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
