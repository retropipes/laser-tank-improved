package com.puttysoftware.lasertank.datatypes;

import com.puttysoftware.lasertank.dataload.DataLoader;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.DirectionResolver;

public class LevelObjectData {
    public static final int WEIGHT_LIGHT = 1;
    public static final int WEIGHT_MODERATE = 2;
    public static final int WEIGHT_HEAVY = 3;

    // Private constructor
    private LevelObjectData() {
	// Do nothing
    }

    public static boolean hitReflectiveSide(final Direction dir) {
	Direction trigger1, trigger2;
	trigger1 = DirectionResolver.previous(dir);
	trigger2 = DirectionResolver.next(dir);
	return dir == trigger1 || dir == trigger2;
    }

    public static boolean isSolid(final int objID) {
	return DataLoader.loadSolid(objID);
    }

    public static boolean isAttributeSolid(final int attrID) {
	return DataLoader.loadAttributeSolid(attrID);
    }

    public static int[] getValidDirections(final int objID) {
	return DataLoader.loadDirection(objID);
    }

    public static boolean isAnimated(final int objID) {
	int finalFrame = DataLoader.loadFrame(objID);
	if (finalFrame > 1) {
	    return true;
	}
	return false;
    }

    public static int getFirstFrameNumber(final int objID) {
	int finalFrame = DataLoader.loadFrame(objID);
	if (finalFrame > 1) {
	    return 1;
	}
	return 0;
    }

    public static int getLastFrameNumber(final int objID) {
	return DataLoader.loadFrame(objID);
    }

    public static boolean hasFriction(final int objID) {
	return DataLoader.loadFriction(objID);
    }

    public static int[] getValidIndexes(final int objID) {
	return DataLoader.loadIndex(objID);
    }

    public static boolean canMove(final int objID) {
	return DataLoader.loadMovable(objID);
    }

    public static boolean canShoot(final int objID) {
	return DataLoader.loadShoot(objID);
    }

    public static int getWeight(final int objID) {
	return DataLoader.loadWeight(objID);
    }
}
