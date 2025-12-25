package com.puttysoftware.lasertank.dataload;

class SolidDataFileNames {
    // Static fields
    private static String[] LIST = new String[] { "solid_blue_laser", "solid_green_laser", "solid_heat_laser",
	    "solid_other", "solid_power_laser", "solid_red_laser", "solid_shadow_laser" };

    // Private constructor
    private SolidDataFileNames() {
	// Do nothing
    }

    // Static methods
    static int getFileCount() {
	return SolidDataFileNames.LIST.length;
    }

    static String getFileName(final SolidDataFile file) {
	return SolidDataFileNames.LIST[file.ordinal()];
    }

    static SolidDataFile getFile(final String fileName) {
	return SolidDataFile.valueOf(fileName.toUpperCase());
    }
}
