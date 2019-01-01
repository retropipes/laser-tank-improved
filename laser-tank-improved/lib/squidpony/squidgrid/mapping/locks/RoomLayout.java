package squidpony.squidgrid.mapping.locks;

import java.util.SortedSet;

import squidpony.squidgrid.mapping.locks.util.Rect2I;
import squidpony.squidmath.Coord;
import squidpony.squidmath.K2;

/**
 * @see IRoomLayout
 */
public class RoomLayout implements IRoomLayout {
    protected int itemCount;
    protected K2<Integer, Room> rooms;
    protected Rect2I bounds;

    public RoomLayout() {
	this.rooms = new K2<>();
	this.bounds = Rect2I.fromExtremes(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Override
    public Rect2I getExtentBounds() {
	return this.bounds;
    }

    @Override
    public SortedSet<Room> getRooms() {
	return this.rooms.getSetB();
    }

    @Override
    public int roomCount() {
	return this.rooms.size();
    }

    @Override
    public Room get(final int id) {
	return this.rooms.getBFromA(id);
    }

    @Override
    public void add(final Room room) {
	this.rooms.put(room.id, room);
	final Coord xy = room.center;
	if (xy.x < this.bounds.left()) {
	    this.bounds = Rect2I.fromExtremes(xy.x, this.bounds.top(), this.bounds.right(), this.bounds.bottom());
	}
	if (xy.x >= this.bounds.right()) {
	    this.bounds = Rect2I.fromExtremes(this.bounds.left(), this.bounds.top(), xy.x + 1, this.bounds.bottom());
	}
	if (xy.y < this.bounds.top()) {
	    this.bounds = Rect2I.fromExtremes(this.bounds.left(), xy.y, this.bounds.right(), this.bounds.bottom());
	}
	if (xy.y >= this.bounds.bottom()) {
	    this.bounds = Rect2I.fromExtremes(this.bounds.left(), this.bounds.top(), this.bounds.right(), xy.y + 1);
	}
    }

    @Override
    public void linkOneWay(final Room room1, final Room room2) {
	this.linkOneWay(room1, room2, Symbol.NOTHING);
    }

    @Override
    public void link(final Room room1, final Room room2) {
	this.link(room1, room2, Symbol.NOTHING);
    }

    @Override
    public void linkOneWay(final Room room1, final Room room2, final int cond) {
	assert this.rooms.containsB(room1) && this.rooms.containsB(room2);
	room1.setEdge(room2.id, cond);
    }

    @Override
    public void link(final Room room1, final Room room2, final int cond) {
	this.linkOneWay(room1, room2, cond);
	this.linkOneWay(room2, room1, cond);
    }

    @Override
    public boolean roomsAreLinked(final Room room1, final Room room2) {
	return room1.getEdge(room2.id) != null || room2.getEdge(room1.id) != null;
    }

    @Override
    public Room findStart() {
	for (final Room room : this.getRooms()) {
	    if (room.isStart()) {
		return room;
	    }
	}
	return null;
    }

    @Override
    public Room findBoss() {
	for (final Room room : this.getRooms()) {
	    if (room.isBoss()) {
		return room;
	    }
	}
	return null;
    }

    @Override
    public Room findGoal() {
	for (final Room room : this.getRooms()) {
	    if (room.isGoal()) {
		return room;
	    }
	}
	return null;
    }

    @Override
    public Room findSwitch() {
	for (final Room room : this.getRooms()) {
	    if (room.isSwitch()) {
		return room;
	    }
	}
	return null;
    }
}
