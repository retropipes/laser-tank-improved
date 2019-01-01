package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.squidmath.RNG;

/**
 * Meant to produce the sort of narrow, looping, not-quite-maze-like passages
 * found in a certain famous early arcade game. Created by Tommy Ettinger on
 * 3/30/2016.
 */
public class PacMazeGenerator {
    public RNG rng;
    public int width, height;
    private boolean[][] map;
    private int[][] env;
    private char[][] maze;

    public PacMazeGenerator() {
	this(250, 250);
    }

    public PacMazeGenerator(final int width, final int height) {
	this.height = height;
	this.width = width;
	this.rng = new RNG();
    }

    public PacMazeGenerator(final int width, final int height, final RNG rng) {
	this.height = height;
	this.width = width;
	this.rng = rng;
    }

    private static final byte[] // unbiased_connections = new byte[]{3, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15},
    connections = new byte[] { 3, 5, 6, 9, 10, 12,/*
						   * 3, 5, 6, 9, 10, 12, 3, 5, 6, 9, 10, 12, 3, 5, 6, 9, 10, 12, 7, 11,
						   * 13, 14, 7, 11, 13, 14, 15
						   */
    };
    private static final int connections_length = PacMazeGenerator.connections.length;

    private boolean write(final boolean[][] m, final int x, final int y, final int xOffset, final int yOffset,
	    final boolean value) {
	final int nx = x * 3 + xOffset + 1, ny = y * 3 + yOffset + 1;
	if (nx >= 0 && nx < m.length && ny >= 0 && ny < m[nx].length) {
	    m[nx][ny] = value;
	    return true;
	}
	return false;
    }

    public boolean[][] create() {
	this.map = new boolean[this.width][this.height];
	final byte[][] conns = new byte[(this.width + 2) / 3][(this.height + 2) / 3];
	final int xOff = this.width % 3 == 1 ? -1 : 0, yOff = this.height % 3 == 1 ? -1 : 0;
	for (int x = 0; x < (this.width + 2) / 3; x++) {
	    for (int y = 0; y < (this.height + 2) / 3; y++) {
		conns[x][y] = PacMazeGenerator.connections[this.rng.nextInt(PacMazeGenerator.connections_length)];
	    }
	}
	for (int x = 0; x < (this.width + 2) / 3; x++) {
	    for (int y = 0; y < (this.height + 2) / 3; y++) {
		this.write(this.map, x, y, xOff, yOff, true);
		if (x > 0 && ((conns[x - 1][y] & 1) != 0 || (conns[x][y] & 2) != 0)) {
		    conns[x - 1][y] |= 1;
		    conns[x][y] |= 2;
		}
		if (x < conns.length - 1 && ((conns[x + 1][y] & 2) != 0 || (conns[x][y] & 1) != 0)) {
		    conns[x + 1][y] |= 2;
		    conns[x][y] |= 1;
		}
		if (y > 0 && ((conns[x][y - 1] & 4) != 0 || (conns[x][y] & 8) != 0)) {
		    conns[x][y - 1] |= 4;
		    conns[x][y] |= 8;
		}
		if (y < conns[0].length - 1 && ((conns[x][y + 1] & 8) != 0 || (conns[x][y] & 4) != 0)) {
		    conns[x][y + 1] |= 8;
		    conns[x][y] |= 4;
		}
	    }
	}
	for (int x = 1; x < (this.width - 1) / 3; x++) {
	    for (int y = 1; y < (this.height - 1) / 3; y++) {
		if (Integer.bitCount(conns[x][y]) >= 4) {
		    // byte temp = connections[rng.nextInt(connections_length)];
		    final int temp = 1 << this.rng.nextInt(4);
		    conns[x][y] ^= temp;
		    if ((temp & 2) != 0) {
			conns[x - 1][y] ^= 1;
		    } else if ((temp & 1) != 0) {
			conns[x + 1][y] ^= 2;
		    } else if ((temp & 8) != 0) {
			conns[x][y - 1] ^= 4;
		    } else if ((temp & 4) != 0) {
			conns[x][y + 1] ^= 8;
		    }
		}
	    }
	}
	for (int x = 0; x < (this.width + 2) / 3; x++) {
	    for (int y = 0; y < (this.height + 2) / 3; y++) {
		this.write(this.map, x, y, xOff, yOff, true);
		if (x > 0 && (conns[x][y] & 2) != 0) {
		    this.write(this.map, x, y, xOff - 1, yOff, true);
		}
		if (x < conns.length - 1 && (conns[x][y] & 1) != 0) {
		    this.write(this.map, x, y, xOff + 1, yOff, true);
		}
		if (y > 0 && (conns[x][y] & 8) != 0) {
		    this.write(this.map, x, y, xOff, yOff - 1, true);
		}
		if (y < conns[0].length - 1 && (conns[x][y] & 4) != 0) {
		    this.write(this.map, x, y, xOff, yOff + 1, true);
		}
	    }
	}
	final int upperY = this.height - 1;
	final int upperX = this.width - 1;
	for (int i = 0; i < this.width; i++) {
	    this.map[i][0] = false;
	    this.map[i][upperY] = false;
	}
	for (int i = 0; i < this.height; i++) {
	    this.map[0][i] = false;
	    this.map[upperX][i] = false;
	}
	return this.map;
    }

    public char[][] generate() {
	this.create();
	this.maze = new char[this.width][this.height];
	this.env = new int[this.width][this.height];
	for (int x = 0; x < this.width; x++) {
	    for (int y = 0; y < this.height; y++) {
		this.maze[x][y] = this.map[x][y] ? '.' : '#';
		this.env[x][y] = this.map[x][y] ? MixedGenerator.CORRIDOR_FLOOR : MixedGenerator.CORRIDOR_WALL;
	    }
	}
	return this.maze;
    }

    public int[][] getEnvironment() {
	if (this.env == null) {
	    return ArrayTools.fill(MixedGenerator.CORRIDOR_WALL, this.width, this.height);
	}
	return this.env;
    }

    /**
     * Gets the maze as a 2D array of true for passable or false for blocked.
     *
     * @return a 2D boolean array; true is passable and false is not.
     */
    public boolean[][] getMap() {
	if (this.map == null) {
	    return new boolean[this.width][this.height];
	}
	return this.map;
    }

    /**
     * Gets the maze as a 2D array of ',' for passable or '#' for blocked.
     *
     * @return a 2D char array; '.' is passable and '#' is not.
     */
    public char[][] getMaze() {
	if (this.maze == null) {
	    return ArrayTools.fill('#', this.width, this.height);
	}
	return this.maze;
    }

    /**
     * Gets the maze as a 2D array of ',' for passable or '#' for blocked.
     *
     * @return a 2D char array; '.' is passable and '#' is not.
     */
    public char[][] getDungeon() {
	return this.getMaze();
    }
}
