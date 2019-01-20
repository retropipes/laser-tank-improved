package com.puttysoftware.lasertank.dataload;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.utilities.Extension;

public class DataLoader {
    private static final String LOAD_PATH = "/assets/data/";
    private static Class<?> LOAD_CLASS = DataLoader.class;
    private static ArrayList<Properties> CACHE;
    private static ArrayList<Properties> SOLID_CACHE;
    private static final String LIST_SPLIT = ",";

    public static void initialize() {
	final int files = DataFileNames.getFileCount();
	DataLoader.CACHE = new ArrayList<>(files);
	for (int f = 0; f < files; f++) {
	    DataLoader.CACHE.add(new Properties());
	}
	DataLoader.cacheFile(DataFile.SOLID);
	DataLoader.cacheFile(DataFile.SOLID_ATTRIBUTE);
	DataLoader.cacheFile(DataFile.DIRECTION);
	DataLoader.cacheFile(DataFile.FRAME);
	DataLoader.cacheFile(DataFile.FRICTION);
	DataLoader.cacheFile(DataFile.INDEX);
	DataLoader.cacheFile(DataFile.MOVABLE);
	DataLoader.cacheFile(DataFile.SHOOT);
	DataLoader.cacheFile(DataFile.WEIGHT);
	DataLoader.cacheFile(DataFile.TRANSFORM_FIRE);
	DataLoader.cacheFile(DataFile.TRANSFORM_ICE);
	DataLoader.cacheFile(DataFile.TRANSFORM_STONE);
	DataLoader.cacheFile(DataFile.MATERIAL);
	DataLoader.cacheFile(DataFile.HEIGHT);
	DataLoader.cacheFile(DataFile.LAYER);
	DataLoader.cacheFile(DataFile.LETHAL);
	DataLoader.cacheFile(DataFile.REFLECT_EAST);
	DataLoader.cacheFile(DataFile.REFLECT_NORTH);
	DataLoader.cacheFile(DataFile.REFLECT_SOUTH);
	DataLoader.cacheFile(DataFile.REFLECT_WEST);
	DataLoader.cacheSolidFile(SolidDataFile.SOLID_BLUE_LASER);
	DataLoader.cacheSolidFile(SolidDataFile.SOLID_GREEN_LASER);
	DataLoader.cacheSolidFile(SolidDataFile.SOLID_HEAT_LASER);
	DataLoader.cacheSolidFile(SolidDataFile.SOLID_OTHER);
	DataLoader.cacheSolidFile(SolidDataFile.SOLID_POWER_LASER);
	DataLoader.cacheSolidFile(SolidDataFile.SOLID_RED_LASER);
	DataLoader.cacheSolidFile(SolidDataFile.SOLID_SHADOW_LASER);
    }

    private static void cacheFile(final DataFile file) {
	final int fileID = file.ordinal();
	final String filename = DataFileNames.getFileName(file);
	try (final InputStream is = DataLoader.LOAD_CLASS
		.getResourceAsStream(DataLoader.LOAD_PATH + filename + Extension.getStringsExtensionWithPeriod())) {
	    DataLoader.CACHE.get(fileID).load(is);
	} catch (final IOException ioe) {
	    CommonDialogs.showErrorDialog("Something has gone horribly wrong trying to cache object data!",
		    "FATAL ERROR");
	    LaserTank.logErrorDirectly(ioe);
	}
    }

    private static Properties getFromCache(final DataFile file) {
	final int fileID = file.ordinal();
	return DataLoader.CACHE.get(fileID);
    }

    private static void cacheSolidFile(final SolidDataFile file) {
	final int fileID = file.ordinal();
	final String filename = SolidDataFileNames.getFileName(file);
	try (final InputStream is = DataLoader.LOAD_CLASS
		.getResourceAsStream(DataLoader.LOAD_PATH + filename + Extension.getStringsExtensionWithPeriod())) {
	    DataLoader.SOLID_CACHE.get(fileID).load(is);
	} catch (final IOException ioe) {
	    CommonDialogs.showErrorDialog("Something has gone horribly wrong trying to cache object solidity data!",
		    "FATAL ERROR");
	    LaserTank.logErrorDirectly(ioe);
	}
    }

    private static Properties getFromSolidCache(final SolidDataFile file) {
	final int fileID = file.ordinal();
	return DataLoader.SOLID_CACHE.get(fileID);
    }

    public static boolean loadSolid(final int objID) {
	SolidDataFile sdf = SolidDataFileNames
		.getFile(DataLoader.getFromCache(DataFile.SOLID).getProperty(Integer.toString(objID)));
	return Boolean.parseBoolean(DataLoader.getFromSolidCache(sdf).getProperty(Integer.toString(objID)));
    }

    public static boolean loadAttributeSolid(final int attrID) {
	return Boolean
		.parseBoolean(DataLoader.getFromCache(DataFile.SOLID_ATTRIBUTE).getProperty(Integer.toString(attrID)));
    }

    public static int[] loadDirection(final int objID) {
	String rawList = DataLoader.getFromCache(DataFile.DIRECTION).getProperty(Integer.toString(objID));
	String[] rawSplit = rawList.split(DataLoader.LIST_SPLIT);
	int[] data = new int[rawSplit.length];
	for (int x = 0; x < data.length; x++) {
	    data[x] = Integer.parseInt(rawSplit[x]);
	}
	return data;
    }

    public static int loadFrame(final int objID) {
	return Integer.parseInt(DataLoader.getFromCache(DataFile.FRAME).getProperty(Integer.toString(objID)));
    }

    public static boolean loadFriction(final int objID) {
	return Boolean.parseBoolean(DataLoader.getFromCache(DataFile.FRICTION).getProperty(Integer.toString(objID)));
    }

    public static int[] loadIndex(final int objID) {
	String rawList = DataLoader.getFromCache(DataFile.INDEX).getProperty(Integer.toString(objID));
	String[] rawSplit = rawList.split(DataLoader.LIST_SPLIT);
	int[] data = new int[rawSplit.length];
	for (int x = 0; x < data.length; x++) {
	    data[x] = Integer.parseInt(rawSplit[x]);
	}
	return data;
    }

    public static boolean loadMovable(final int objID) {
	return Boolean.parseBoolean(DataLoader.getFromCache(DataFile.MOVABLE).getProperty(Integer.toString(objID)));
    }

    public static boolean loadShoot(final int objID) {
	return Boolean.parseBoolean(DataLoader.getFromCache(DataFile.SHOOT).getProperty(Integer.toString(objID)));
    }

    public static int loadWeight(final int objID) {
	return Integer.parseInt(DataLoader.getFromCache(DataFile.WEIGHT).getProperty(Integer.toString(objID)));
    }

    public static int loadTransformFire(final int objID) {
	return Integer.parseInt(DataLoader.getFromCache(DataFile.TRANSFORM_FIRE).getProperty(Integer.toString(objID)));
    }

    public static int loadTransformIce(final int objID) {
	return Integer.parseInt(DataLoader.getFromCache(DataFile.TRANSFORM_ICE).getProperty(Integer.toString(objID)));
    }

    public static int loadTransformStone(final int objID) {
	return Integer.parseInt(DataLoader.getFromCache(DataFile.TRANSFORM_STONE).getProperty(Integer.toString(objID)));
    }

    public static int loadMaterial(final int objID) {
	return Integer.parseInt(DataLoader.getFromCache(DataFile.MATERIAL).getProperty(Integer.toString(objID)));
    }

    public static int loadHeight(final int objID) {
	return Integer.parseInt(DataLoader.getFromCache(DataFile.HEIGHT).getProperty(Integer.toString(objID)));
    }

    public static int loadLayer(final int objID) {
	return Integer.parseInt(DataLoader.getFromCache(DataFile.LAYER).getProperty(Integer.toString(objID)));
    }

    public static boolean loadLethal(final int objID) {
	return Boolean.parseBoolean(DataLoader.getFromCache(DataFile.LETHAL).getProperty(Integer.toString(objID)));
    }

    public static boolean loadReflectEast(final int objID) {
	return Boolean
		.parseBoolean(DataLoader.getFromCache(DataFile.REFLECT_EAST).getProperty(Integer.toString(objID)));
    }

    public static boolean loadReflectNorth(final int objID) {
	return Boolean
		.parseBoolean(DataLoader.getFromCache(DataFile.REFLECT_NORTH).getProperty(Integer.toString(objID)));
    }

    public static boolean loadReflectSouth(final int objID) {
	return Boolean
		.parseBoolean(DataLoader.getFromCache(DataFile.REFLECT_SOUTH).getProperty(Integer.toString(objID)));
    }

    public static boolean loadReflectWest(final int objID) {
	return Boolean
		.parseBoolean(DataLoader.getFromCache(DataFile.REFLECT_WEST).getProperty(Integer.toString(objID)));
    }
}
