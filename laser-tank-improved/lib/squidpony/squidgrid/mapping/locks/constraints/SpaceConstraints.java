package squidpony.squidgrid.mapping.locks.constraints;

import squidpony.squidmath.Coord;
import squidpony.squidmath.IntVLA;

/**
 * Constrains the coordinates where Rooms may be placed to be only those within
 * the {@link SpaceMap}, as well as placing limitations on the number of keys
 * and switches.
 *
 * @see CountConstraints
 * @see SpaceMap
 */
public class SpaceConstraints extends CountConstraints {
    public static final int DEFAULT_MAX_KEYS = 4, DEFAULT_MAX_SWITCHES = 1;
    protected SpaceMap spaceMap;

    public SpaceConstraints(final SpaceMap spaceMap) {
	super(spaceMap.numberSpaces(), SpaceConstraints.DEFAULT_MAX_KEYS, SpaceConstraints.DEFAULT_MAX_SWITCHES);
	this.spaceMap = spaceMap;
    }

    @Override
    protected boolean validRoomCoords(final Coord c) {
	return this.spaceMap.get(c);
    }

    @Override
    public IntVLA initialRooms() {
	final IntVLA ids = new IntVLA();
	for (final Coord xy : this.spaceMap.getBottomSpaces()) {
	    ids.add(this.getRoomId(xy));
	}
	return ids;
    }
}
