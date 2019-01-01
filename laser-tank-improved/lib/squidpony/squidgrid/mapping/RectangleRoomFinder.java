package squidpony.squidgrid.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.iterator.SquidIterators;
import squidpony.squidmath.Coord;

/**
 * An algorithm to find rectangle areas in dungeons. It is a simpler and faster
 * alternative to {@link RoomFinder}. You can execute
 * {@code RectangleRoomsFinderTest} to see how it performs.
 *
 * @author smelC
 *
 * @see RoomFinder A fancier room finder
 */
public class RectangleRoomFinder {
    protected final char[][] dungeon;
    protected final int dungeonWidth;
    protected final int dungeonHeight;
    protected final Set<Character> floors;
    /**
     * The minimum number of cells that the diagonal of a room must have. Having 3
     * here means that, by default, only rooms at most 3x3 are considered.
     */
    public int minimumDiagonal = 3;
    /** {@code true} to restrict {@code this} to find square rooms */
    public boolean onlySquareRooms = false;

    public RectangleRoomFinder(final char[][] dungeon) {
	this.dungeon = dungeon;
	this.dungeonWidth = dungeon.length;
	this.dungeonHeight = this.dungeonWidth == 0 ? 0 : dungeon[0].length;
	this.floors = new HashSet<>();
	this.floors.add('.');
    }

    /**
     * Adds a character considered as a floor.
     *
     * @param c
     * @return {@code true} if {@code c} wasn't a floor character.
     */
    public boolean addFloorCharacter(final char c) {
	return this.floors.add(c);
    }

    /**
     * Removes a character from being considered as a floor.
     *
     * @param c
     * @return {@code true} if {@code c} was a floor character.
     */
    public boolean removeFloorCharacter(final char c) {
	return this.floors.remove(c);
    }

    /**
     * @return The rectangles of the dungeon given at creation time.
     */
    public List<Rectangle> findRectangles() {
	final List<Rectangle> result = new ArrayList<>();
	/*
	 * Every cell containing true indicates that this cell is included in an
	 * already-found room.
	 */
	final boolean[][] assigneds = new boolean[this.dungeonWidth][this.dungeonHeight];
	final Iterator<Coord> it = new SquidIterators.BottomLeftToTopRight(this.dungeonWidth, this.dungeonHeight);
	nextBottomLeft: while (it.hasNext()) {
	    final Coord c = it.next();
	    /*
	     * Try to find the room's diagonal, from its bottom left to its top right
	     */
	    Coord current = c;
	    int steps = 0;
	    while (!assigneds[c.x][c.y] && this.isFloor(this.dungeon[current.x][current.y])) {
		current = current.translate(Direction.UP_RIGHT);
		steps++;
	    }
	    if (steps < this.minimumDiagonal) {
		continue;
	    }
	    /*
	     * We have the diagonal. Let's check that this tentative room only contains
	     * (room-unassigned) floors.
	     */
	    Rectangle r = new Rectangle.Impl(c, steps, steps);
	    Iterator<Coord> cells = Rectangle.Utils.cells(r);
	    while (cells.hasNext()) {
		final Coord inr = cells.next();
		assert this.isInDungeon(inr);
		if (!this.isFloor(this.dungeon[inr.x][inr.y]) || assigneds[inr.x][inr.y]) {
		    continue nextBottomLeft;
		}
	    }
	    if (!this.onlySquareRooms) {
		/* Try to extend it */
		r = this.extendRoom(assigneds, r, Direction.LEFT);
		r = this.extendRoom(assigneds, r, Direction.RIGHT);
		r = this.extendRoom(assigneds, r, Direction.UP);
		r = this.extendRoom(assigneds, r, Direction.DOWN);
	    }
	    /* Found a room! Let's record the cells. */
	    result.add(r);
	    cells = Rectangle.Utils.cells(r);
	    while (cells.hasNext()) {
		final Coord inr = cells.next();
		assigneds[inr.x][inr.y] = true;
	    }
	}
	return result;
    }

    /**
     * @param assigneds Cells already in a room.
     * @param d         A cardinal direction.
     * @return A variant of {@code r} extended to the direction {@code d}, if
     *         possible. {@code r} itself if unaffected.
     */
    protected Rectangle extendRoom(final boolean[][] assigneds, final Rectangle r, final Direction d) {
	assert !d.isDiagonal();
	Rectangle result = r;
	while (true) {
	    final Rectangle next = this.extendRoomOnce(assigneds, result, d);
	    if (next == result) {
		/* No change */
		break;
	    } else {
		result = next;
	    }
	}
	return result;
    }

    /**
     * @param assigneds Cells already in a room. This array is muted by this call.
     */
    protected Rectangle extendRoomOnce(final boolean[][] assigneds, final Rectangle r, final Direction d) {
	final Coord bl = r.getBottomLeft();
	Coord first = null;
	Direction way = null;
	int steps = -1;
	switch (d) {
	case DOWN_LEFT:
	case DOWN_RIGHT:
	case NONE:
	case UP_LEFT:
	case UP_RIGHT:
	    throw new IllegalStateException(
		    "Unexpected direction in " + this.getClass().getSimpleName() + "::extendRoomOnce: " + d);
	case DOWN:
	    first = bl.translate(Direction.DOWN);
	    way = Direction.RIGHT;
	    steps = r.getWidth();
	    break;
	case LEFT:
	    first = bl.translate(Direction.LEFT);
	    way = Direction.UP;
	    steps = r.getHeight();
	    break;
	case RIGHT:
	    first = bl.translate(r.getWidth() - 1, 0).translate(Direction.RIGHT);
	    way = Direction.UP;
	    steps = r.getHeight();
	    break;
	case UP:
	    first = bl.translate(0, -r.getHeight() + 1).translate(Direction.UP);
	    way = Direction.RIGHT;
	    steps = r.getWidth();
	    break;
	}
	assert first != null;
	Coord current = first;
	assert 0 <= steps;
	assert way != null;
	while (0 < steps) {
	    if (!this.isInDungeon(current) || !this.isFloor(this.dungeon[current.x][current.y])
		    || assigneds[current.x][current.y]) {
		/* Cannot extend */
		return r;
	    }
	    current = current.translate(way);
	    steps--;
	}
	final Rectangle result = Rectangle.Utils.extend(r, d);
	assert this.validRoomCells(Rectangle.Utils.cells(result));
	return result;
    }

    protected boolean isFloor(final char c) {
	return this.floors.contains(c);
    }

    protected boolean isInDungeon(final Coord c) {
	return 0 <= c.x && c.x < this.dungeonWidth && 0 <= c.y && c.y < this.dungeonHeight;
    }

    private boolean validRoomCells(final Iterator<? extends Coord> cs) {
	while (cs.hasNext()) {
	    final Coord c = cs.next();
	    if (!this.isInDungeon(c) || !this.isFloor(this.dungeon[c.x][c.y])) {
		return false;
	    }
	}
	return true;
    }
}
