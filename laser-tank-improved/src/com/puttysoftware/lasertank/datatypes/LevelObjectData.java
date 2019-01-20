package com.puttysoftware.lasertank.datatypes;

import com.puttysoftware.lasertank.dataload.DataLoader;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.DirectionResolver;
import com.puttysoftware.lasertank.utilities.MaterialConstants;

public class LevelObjectData {
    public static final int WEIGHT_LIGHT = 1;
    public static final int WEIGHT_MODERATE = 2;
    public static final int WEIGHT_HEAVY = 3;

    // Private constructor
    private LevelObjectData() {
	// Do nothing
    }

    public static final int getWeight(final int material) {
	if (material == MaterialConstants.MATERIAL_PLASTIC) {
	    return LevelObjectData.WEIGHT_LIGHT;
	} else if (material == MaterialConstants.MATERIAL_METALLIC) {
	    return LevelObjectData.WEIGHT_HEAVY;
	} else {
	    return LevelObjectData.WEIGHT_MODERATE;
	}
    }

    public static boolean hitReflectiveSide(final Direction dir) {
	Direction trigger1, trigger2;
	trigger1 = DirectionResolver.previous(dir);
	trigger2 = DirectionResolver.next(dir);
	return dir == trigger1 || dir == trigger2;
    }

    public static boolean isObjectSolid(final int objID) {
	return Boolean.parseBoolean(DataLoader.loadSolid(objID));
    }

    public static boolean isObjectAttributeSolid(final int attrID) {
	return Boolean.parseBoolean(DataLoader.loadAttributeSolid(attrID));
    }
}
