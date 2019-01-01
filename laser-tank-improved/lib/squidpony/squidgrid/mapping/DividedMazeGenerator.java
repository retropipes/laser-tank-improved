package squidpony.squidgrid.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import squidpony.squidgrid.Direction;
import squidpony.squidmath.RNG;

/**
 * Recursively divided maze. Creates only walls and passages.
 *
 * This dungeon generator is based on a port of the rot.js version.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class DividedMazeGenerator {
    private class DividedMazeRoom {
	private final int left, top, right, bottom;

	public DividedMazeRoom(final int left, final int top, final int right, final int bottom) {
	    this.left = left;
	    this.top = top;
	    this.right = right;
	    this.bottom = bottom;
	}
    }

    private final int width, height;
    private boolean[][] map;
    private final RNG rng;

    /**
     * Sets up the generator to make mazes the given width and height. The mazes
     * have a solid wall border.
     *
     * @param width
     * @param height
     */
    public DividedMazeGenerator(final int width, final int height) {
	this.width = width;
	this.height = height;
	this.rng = new RNG();
    }

    /**
     * Sets up the generator to make mazes the given width and height. The mazes
     * have a solid wall border.
     *
     * @param width  in cells
     * @param height in cells
     * @param rng    the random number generator to use
     */
    public DividedMazeGenerator(final int width, final int height, final RNG rng) {
	this.width = width;
	this.height = height;
	this.rng = rng;
    }

    /**
     * Builds a maze. True values represent walls.
     *
     * @return
     */
    public boolean[][] create() {
	this.map = new boolean[this.width][this.height];
	for (int x = 0; x < this.width; x++) {
	    for (int y = 0; y < this.height; y++) {
		this.map[x][y] = x == 0 || y == 0 || x + 1 == this.width || y + 1 == this.height;
	    }
	}
	this.process();
	return this.map;
    }

    private void process() {
	final LinkedList<DividedMazeRoom> stack = new LinkedList<>();
	stack.offer(new DividedMazeRoom(1, 1, this.width - 2, this.height - 2));
	while (!stack.isEmpty()) {
	    final DividedMazeRoom room = stack.removeFirst();
	    final ArrayList<Integer> availX = new ArrayList<>(), availY = new ArrayList<>();
	    for (int x = room.left + 1; x < room.right; x++) {
		final boolean top = this.map[x][room.top - 1];
		final boolean bottom = this.map[x][room.bottom + 1];
		if (top && bottom && x % 2 == 0) {
		    availX.add(x);
		}
	    }
	    for (int y = room.top + 1; y < room.bottom; y++) {
		final boolean left = this.map[room.left - 1][y];
		final boolean right = this.map[room.right + 1][y];
		if (left && right && y % 2 == 0) {
		    availY.add(y);
		}
	    }
	    if (availX.isEmpty() || availY.isEmpty()) {
		continue;
	    }
	    final int x2 = this.rng.getRandomElement(availX);
	    final int y2 = this.rng.getRandomElement(availY);
	    this.map[x2][y2] = true;
	    for (final Direction dir : Direction.CARDINALS) {
		switch (dir) {
		case LEFT:
		    for (int x = room.left; x < x2; x++) {
			this.map[x][y2] = true;
		    }
		    break;
		case RIGHT:
		    for (int x = x2 + 1; x <= room.right; x++) {
			this.map[x][y2] = true;
		    }
		    break;
		case UP:
		    for (int y = room.top; y < y2; y++) {
			this.map[x2][y] = true;
		    }
		    break;
		case DOWN:
		    for (int y = y2 + 1; y <= room.bottom; y++) {
			this.map[x2][y] = true;
		    }
		    break;
		case NONE:
		    break;
		case DOWN_LEFT:
		case DOWN_RIGHT:
		case UP_LEFT:
		case UP_RIGHT:
		    throw new IllegalStateException("There should only be cardinal directions here");
		}
	    }
	    final List<Direction> dirs = Arrays.asList(Direction.CARDINALS);
	    dirs.remove(this.rng.getRandomElement(dirs));
	    for (final Direction dir : dirs) {
		switch (dir) {
		case LEFT:
		    this.map[this.rng.between(room.left, x2)][y2] = false;
		    break;
		case RIGHT:
		    this.map[this.rng.between(x2 + 1, room.right + 1)][y2] = false;
		    break;
		case UP:
		    this.map[x2][this.rng.between(room.top, y2)] = false;
		    break;
		case DOWN:
		    this.map[x2][this.rng.between(y2 + 1, room.bottom + 1)] = false;
		    break;
		case NONE:
		    break;
		case DOWN_LEFT:
		case DOWN_RIGHT:
		case UP_LEFT:
		case UP_RIGHT:
		    throw new IllegalStateException("There should only be cardinal directions here");
		}
	    }
	    stack.offer(new DividedMazeRoom(room.left, room.top, x2 - 1, y2 - 1));
	    stack.offer(new DividedMazeRoom(x2 + 1, room.top, room.right, y2 - 1));
	    stack.offer(new DividedMazeRoom(room.left, y2 + 1, x2 - 1, room.bottom));
	    stack.offer(new DividedMazeRoom(x2 + 1, y2 + 1, room.right, room.bottom));
	}
    }
}
