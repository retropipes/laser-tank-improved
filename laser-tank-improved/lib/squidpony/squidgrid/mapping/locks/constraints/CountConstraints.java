package squidpony.squidgrid.mapping.locks.constraints;

import java.util.Collections;
import java.util.Set;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.mapping.locks.IRoomLayout;
import squidpony.squidgrid.mapping.locks.generators.ILayoutGenerator;
import squidpony.squidmath.Arrangement;
import squidpony.squidmath.Coord;
import squidpony.squidmath.IntVLA;

/**
 * Limits the {@link ILayoutGenerator} in the <i>number</i> of keys, switches
 * and rooms it is allowed to place.
 *
 * Also restrict to a grid of 1x1 rooms.
 *
 * @see ILayoutConstraints
 */
public class CountConstraints implements ILayoutConstraints {
    protected int maxSpaces, maxKeys, maxSwitches;
    protected Arrangement<Coord> roomIds;
    protected int firstRoomId;

    public CountConstraints(final int maxSpaces, final int maxKeys, final int maxSwitches) {
	this.maxSpaces = maxSpaces;
	this.maxKeys = maxKeys;
	this.maxSwitches = maxSwitches;
	this.roomIds = new Arrangement<>();
	final Coord first = Coord.get(127, 127);
	this.firstRoomId = this.getRoomId(first);
    }

    public int getRoomId(final Coord xy) {
	if (this.roomIds.containsKey(xy)) {
	    return this.roomIds.get(xy);
	} else {
	    this.roomIds.add(xy);
	    return this.roomIds.size() - 1;
	}
    }

    public Coord getRoomCoords(final int id) {
	assert this.roomIds.containsValue(id);
	return this.roomIds.keyAt(id);
    }

    @Override
    public int getMaxRooms() {
	return this.maxSpaces;
    }

    public void setMaxSpaces(final int maxSpaces) {
	this.maxSpaces = maxSpaces;
    }

    @Override
    public IntVLA initialRooms() {
	return IntVLA.with(this.firstRoomId);
    }

    @Override
    public int getMaxKeys() {
	return this.maxKeys;
    }

    public void setMaxKeys(final int maxKeys) {
	this.maxKeys = maxKeys;
    }

    @Override
    public boolean isAcceptable(final IRoomLayout dungeon) {
	return true;
    }

    @Override
    public int getMaxSwitches() {
	return this.maxSwitches;
    }

    public void setMaxSwitches(final int maxSwitches) {
	this.maxSwitches = maxSwitches;
    }

    protected boolean validRoomCoords(final Coord c) {
	return c.y >= 0 && c.x >= 0 && c.x <= 255 && c.y <= 255;
    }

    @Override
    public IntVLA getAdjacentRooms(final int id, final int keyLevel) {
	final Coord xy = this.roomIds.keyAt(id);
	final IntVLA ids = new IntVLA();
	for (final Direction d : Direction.CARDINALS) {
	    final Coord neighbor = xy.translate(d);
	    if (this.validRoomCoords(neighbor)) {
		ids.add(this.getRoomId(neighbor));
	    }
	}
	return ids;
    }

    @Override
    public Set<Coord> getCoords(final int id) {
	return Collections.singleton(this.getRoomCoords(id));
    }

    @Override
    public double edgeGraphifyProbability(final int id, final int nextId) {
	return 0.2;
    }

    @Override
    public boolean roomCanFitItem(final int id, final int key) {
	return true;
    }
}
