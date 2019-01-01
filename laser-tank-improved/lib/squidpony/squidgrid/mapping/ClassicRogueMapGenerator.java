package squidpony.squidgrid.mapping;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;
import squidpony.squidmath.RNG;

/**
 * Creates a dungeon in the style of the original Rogue game. It will always
 * make a grid style of rooms where there are a certain number horizontally and
 * vertically and it will link them only next to each other.
 *
 * This dungeon generator is based on a port of the rot.js version.
 *
 * @author hyakugei
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class ClassicRogueMapGenerator implements IDungeonGenerator {
    /**
     * Holds the information needed to track rooms in the classic rogue generation
     * algorithm.
     */
    private class ClassicRogueRoom {
	private int x, y, width, height;
	private final int cellx;
	private final int celly;
	private final List<ClassicRogueRoom> connections = new LinkedList<>();

	ClassicRogueRoom(final int x, final int y, final int width, final int height, final int cellx,
		final int celly) {
	    this.x = x;
	    this.y = y;
	    this.width = width;
	    this.height = height;
	    this.cellx = cellx;
	    this.celly = celly;
	}

	@Override
	public int hashCode() {
	    int hash = 5;
	    hash = 89 * hash + this.cellx;
	    hash = 89 * hash + this.celly;
	    return hash;
	}

	@Override
	public boolean equals(final Object obj) {
	    if (obj == null) {
		return false;
	    }
	    if (this.getClass() != obj.getClass()) {
		return false;
	    }
	    final ClassicRogueRoom other = (ClassicRogueRoom) obj;
	    if (this.cellx != other.cellx) {
		return false;
	    }
	    return this.celly == other.celly;
	}
    }

    private final RNG rng;
    private final int horizontalRooms, verticalRooms, dungeonWidth, dungeonHeight;
    private int minRoomWidth;
    private int maxRoomWidth;
    private int minRoomHeight;
    private int maxRoomHeight;
    private ClassicRogueRoom[][] rooms;
    private Terrain[][] map;
    private char[][] dungeon;

    /**
     * Initializes the generator to turn out random dungeons within the specific
     * parameters.
     *
     * Will size down the room width and height parameters if needed to ensure the
     * desired number of rooms will fit both horizontally and vertically.
     *
     * @param horizontalRooms How many rooms will be made horizontally
     * @param verticalRooms   How many rooms will be made vertically
     * @param dungeonWidth    How wide the total dungeon will be
     * @param dungeonHeight   How high the total dungeon will be
     * @param minRoomWidth    The minimum width a room can be
     * @param maxRoomWidth    The maximum width a room can be
     * @param minRoomHeight   The minimum height a room can be
     * @param maxRoomHeight   The maximum height a room can be
     */
    public ClassicRogueMapGenerator(final int horizontalRooms, final int verticalRooms, final int dungeonWidth,
	    final int dungeonHeight, final int minRoomWidth, final int maxRoomWidth, final int minRoomHeight,
	    final int maxRoomHeight) {
	this(horizontalRooms, verticalRooms, dungeonWidth, dungeonHeight, minRoomWidth, maxRoomWidth, minRoomHeight,
		maxRoomHeight, new RNG());
    }

    /**
     * Initializes the generator to turn out random dungeons within the specific
     * parameters.
     *
     * Will size down the room width and height parameters if needed to ensure the
     * desired number of rooms will fit both horizontally and vertically.
     *
     * @param horizontalRooms How many rooms will be made horizontally
     * @param verticalRooms   How many rooms will be made vertically
     * @param dungeonWidth    How wide the total dungeon will be
     * @param dungeonHeight   How high the total dungeon will be
     * @param minRoomWidth    The minimum width a room can be
     * @param maxRoomWidth    The maximum width a room can be
     * @param minRoomHeight   The minimum height a room can be
     * @param maxRoomHeight   The maximum height a room can be
     */
    public ClassicRogueMapGenerator(final int horizontalRooms, final int verticalRooms, final int dungeonWidth,
	    final int dungeonHeight, final int minRoomWidth, final int maxRoomWidth, final int minRoomHeight,
	    final int maxRoomHeight, final RNG rng) {
	this.rng = rng;
	this.horizontalRooms = horizontalRooms;
	this.verticalRooms = verticalRooms;
	this.dungeonWidth = dungeonWidth;
	this.dungeonHeight = dungeonHeight;
	this.minRoomWidth = minRoomWidth;
	this.maxRoomWidth = maxRoomWidth;
	this.minRoomHeight = minRoomHeight;
	this.maxRoomHeight = maxRoomHeight;
	this.sanitizeRoomDimensions();
    }

    private void sanitizeRoomDimensions() {
	int test = (this.dungeonWidth - 3 * this.horizontalRooms) / this.horizontalRooms;// have to leave space for
											 // hallways
	this.maxRoomWidth = Math.min(test, this.maxRoomWidth);
	this.minRoomWidth = Math.max(this.minRoomWidth, 2);
	this.minRoomWidth = Math.min(this.minRoomWidth, this.maxRoomWidth);
	test = (this.dungeonHeight - 3 * this.verticalRooms) / this.verticalRooms;// have to leave space for hallways
	this.maxRoomHeight = Math.min(test, this.maxRoomHeight);
	this.minRoomHeight = Math.max(this.minRoomHeight, 2);
	this.minRoomHeight = Math.min(this.minRoomHeight, this.maxRoomHeight);
    }

    /**
     * Builds and returns a map in the Classic Rogue style. <br>
     * Only includes rooms, corridors and doors. <br>
     * There is also a generate method that produces a 2D char array, which may be
     * more suitable.
     *
     * @return a 2D array of Terrain objects
     */
    public Terrain[][] create() {
	this.initRooms();
	this.connectRooms();
	this.connectUnconnectedRooms();
	this.fullyConnect();
	this.createRooms();
	this.createCorridors();
	return this.map;
    }

    /**
     * Builds and returns a map in the Classic Rogue style, returned as a 2D char
     * array.
     *
     * Only includes rooms ('.' for floor and '#' for walls), corridors (using the
     * same chars as rooms) and doors ('+' for closed doors, does not generate open
     * doors). <br>
     * There is also a create method that produces a 2D array of Terrain objects,
     * which could (maybe) be what you want. More methods in SquidLib expect char 2D
     * arrays than Terrain anything, particularly in DungeonUtility.
     *
     * @return a 2D char array version of the map
     */
    @Override
    public char[][] generate() {
	this.create();
	if (this.map.length <= 0) {
	    return new char[0][0];
	}
	if (this.dungeon == null || this.dungeon.length != this.map.length
		|| this.dungeon[0].length != this.map[0].length) {
	    this.dungeon = new char[this.map.length][this.map[0].length];
	}
	for (int x = 0; x < this.map.length; x++) {
	    for (int y = 0; y < this.map[x].length; y++) {
		this.dungeon[x][y] = this.map[x][y].symbol();
	    }
	}
	return this.dungeon;
    }

    @Override
    public char[][] getDungeon() {
	return this.dungeon;
    }

    private void initRooms() {
	this.rooms = new ClassicRogueRoom[this.horizontalRooms][this.verticalRooms];
	this.map = new Terrain[this.dungeonWidth][this.dungeonHeight];
	for (int x = 0; x < this.horizontalRooms; x++) {
	    for (int y = 0; y < this.verticalRooms; y++) {
		this.rooms[x][y] = new ClassicRogueRoom(0, 0, 0, 0, x, y);
	    }
	}
	for (int x = 0; x < this.dungeonWidth; x++) {
	    for (int y = 0; y < this.dungeonHeight; y++) {
		this.map[x][y] = Terrain.WALL;
	    }
	}
    }

    private void connectRooms() {
	List<ClassicRogueRoom> unconnected = new LinkedList<>();
	for (int x = 0; x < this.horizontalRooms; x++) {
	    for (int y = 0; y < this.verticalRooms; y++) {
		unconnected.add(this.rooms[x][y]);
	    }
	}
	unconnected = this.rng.shuffle(unconnected);
	final Direction[] dirToCheck = new Direction[4];
	for (final ClassicRogueRoom room : unconnected) {
	    this.rng.shuffle(Direction.CARDINALS, dirToCheck);
	    for (final Direction dir : dirToCheck) {
		final int nextX = room.x + dir.deltaX;
		final int nextY = room.y + dir.deltaY;
		if (nextX < 0 || nextX >= this.horizontalRooms || nextY < 0 || nextY >= this.verticalRooms) {
		    continue;
		}
		final ClassicRogueRoom otherRoom = this.rooms[nextX][nextY];
		if (room.connections.contains(otherRoom)) {
		    break;// already connected to this room
		}
		if (!otherRoom.connections.isEmpty()) {
		    room.connections.add(otherRoom);
		    break;
		}
	    }
	}
    }

    private void connectUnconnectedRooms() {
	for (int x = 0; x < this.horizontalRooms; x++) {
	    for (int y = 0; y < this.verticalRooms; y++) {
		final ClassicRogueRoom room = this.rooms[x][y];
		if (room.connections.isEmpty()) {
		    List<Direction> dirToCheck = Arrays.asList(Direction.CARDINALS);
		    dirToCheck = this.rng.shuffle(dirToCheck);
		    boolean validRoom = false;
		    ClassicRogueRoom otherRoom = null;
		    do {
			final Direction dir = dirToCheck.remove(0);
			final int nextX = x + dir.deltaX;
			if (nextX < 0 || nextX >= this.horizontalRooms) {
			    continue;
			}
			final int nextY = y + dir.deltaY;
			if (nextY < 0 || nextY >= this.verticalRooms) {
			    continue;
			}
			otherRoom = this.rooms[nextX][nextY];
			validRoom = true;
			if (otherRoom.connections.contains(room)) {
			    validRoom = false;
			} else {
			    break;
			}
		    } while (!dirToCheck.isEmpty());
		    if (validRoom) {
			room.connections.add(otherRoom);
		    }
		}
	    }
	}
    }

    private void fullyConnect() {
	boolean allGood;
	do {
	    final LinkedList<ClassicRogueRoom> deq = new LinkedList<>();
	    for (int x = 0; x < this.horizontalRooms; x++) {
		for (int y = 0; y < this.verticalRooms; y++) {
		    deq.offer(this.rooms[x][y]);
		}
	    }
	    final LinkedList<ClassicRogueRoom> connected = new LinkedList<>();
	    connected.add(deq.removeFirst());
	    boolean changed = true;
	    testing: while (changed) {
		changed = false;
		for (final ClassicRogueRoom test : deq) {
		    for (final ClassicRogueRoom r : connected) {
			if (test.connections.contains(r) || r.connections.contains(test)) {
			    connected.offer(test);
			    deq.remove(test);
			    changed = true;
			    continue testing;
			}
		    }
		}
	    }
	    allGood = true;
	    if (!deq.isEmpty()) {
		testing: for (final ClassicRogueRoom room : deq) {
		    for (final Direction dir : Direction.CARDINALS) {
			final int x = room.cellx + dir.deltaX;
			final int y = room.celly + dir.deltaY;
			if (x >= 0 && y >= 0 && x < this.horizontalRooms && y < this.verticalRooms) {
			    final ClassicRogueRoom otherRoom = this.rooms[x][y];
			    if (connected.contains(otherRoom)) {
				room.connections.add(otherRoom);
				allGood = false;
				break testing;
			    }
			}
		    }
		}
	    }
	} while (!allGood);
    }

    private void createRooms() {
	final int cwp = this.dungeonWidth / this.horizontalRooms;
	final int chp = this.dungeonHeight / this.verticalRooms;
	ClassicRogueRoom otherRoom;
	for (int x = 0; x < this.horizontalRooms; x++) {
	    for (int y = 0; y < this.verticalRooms; y++) {
		int sx = cwp * x;
		int sy = chp * y;
		sx = Math.max(sx, 2);
		sy = Math.max(sy, 2);
		int roomw = this.rng.between(this.minRoomWidth, this.maxRoomWidth + 1);
		int roomh = this.rng.between(this.minRoomHeight, this.maxRoomHeight + 1);
		if (y > 0) {
		    otherRoom = this.rooms[x][y - 1];
		    while (sy - (otherRoom.y + otherRoom.height) < 3) {
			sy++;
		    }
		}
		if (x > 0) {
		    otherRoom = this.rooms[x - 1][y];
		    while (sx - (otherRoom.x + otherRoom.width) < 3) {
			sx++;
		    }
		}
		int sxOffset = Math.round(this.rng.nextInt(cwp - roomw) / 2);
		int syOffset = Math.round(this.rng.nextInt(chp - roomh) / 2);
		while (sx + sxOffset + roomw >= this.dungeonWidth) {
		    if (sxOffset > 0) {
			sxOffset--;
		    } else {
			roomw--;
		    }
		}
		while (sy + syOffset + roomh >= this.dungeonHeight) {
		    if (syOffset > 0) {
			syOffset--;
		    } else {
			roomh--;
		    }
		}
		sx += sxOffset;
		sy += syOffset;
		final ClassicRogueRoom r = this.rooms[x][y];
		r.x = sx;
		r.y = sy;
		r.width = roomw;
		r.height = roomh;
		for (int xx = sx; xx < sx + roomw; xx++) {
		    for (int yy = sy; yy < sy + roomh; yy++) {
			this.map[xx][yy] = Terrain.FLOOR;
		    }
		}
	    }
	}
    }

    private Coord randomWallPosition(final ClassicRogueRoom room, final Direction dir) {
	int x, y;
	Coord p = null;
	switch (dir) {
	case LEFT:
	    y = this.rng.between(room.y + 1, room.y + room.height);
	    x = room.x - 1;
	    this.map[x][y] = Terrain.CLOSED_DOOR;
	    p = Coord.get(x - 1, y);
	    break;
	case RIGHT:
	    y = this.rng.between(room.y + 1, room.y + room.height);
	    x = room.x + room.width;
	    this.map[x][y] = Terrain.CLOSED_DOOR;
	    p = Coord.get(x + 1, y);
	    break;
	case UP:
	    x = this.rng.between(room.x + 1, room.x + room.width);
	    y = room.y - 1;
	    this.map[x][y] = Terrain.CLOSED_DOOR;
	    p = Coord.get(x, y - 1);
	    break;
	case DOWN:
	    x = this.rng.between(room.x + 1, room.x + room.width);
	    y = room.y + room.height;
	    this.map[x][y] = Terrain.CLOSED_DOOR;
	    p = Coord.get(x, y + 1);
	    break;
	case NONE:
	    break;
	case DOWN_LEFT:
	case DOWN_RIGHT:
	case UP_LEFT:
	case UP_RIGHT:
	    throw new IllegalStateException("There should only be cardinal positions here");
	}
	return p;
    }

    /**
     * Draws a corridor between the two points with a zig-zag in between.
     *
     * @param start
     * @param end
     */
    private void digPath(final Coord start, final Coord end) {
	final int xOffset = end.x - start.x;
	final int yOffset = end.y - start.y;
	int xpos = start.x;
	int ypos = start.y;
	final List<Magnitude> moves = new LinkedList<>();
	final int xAbs = Math.abs(xOffset);
	final int yAbs = Math.abs(yOffset);
	final double firstHalf = this.rng.nextDouble();
	final double secondHalf = 1 - firstHalf;
	final Direction xDir = xOffset < 0 ? Direction.LEFT : Direction.RIGHT;
	final Direction yDir = yOffset > 0 ? Direction.DOWN : Direction.UP;
	if (xAbs < yAbs) {
	    int tempDist = (int) Math.ceil(yAbs * firstHalf);
	    moves.add(new Magnitude(yDir, tempDist));
	    moves.add(new Magnitude(xDir, xAbs));
	    tempDist = (int) Math.floor(yAbs * secondHalf);
	    moves.add(new Magnitude(yDir, tempDist));
	} else {
	    int tempDist = (int) Math.ceil(xAbs * firstHalf);
	    moves.add(new Magnitude(xDir, tempDist));
	    moves.add(new Magnitude(yDir, yAbs));
	    tempDist = (int) Math.floor(xAbs * secondHalf);
	    moves.add(new Magnitude(xDir, tempDist));
	}
	this.map[xpos][ypos] = Terrain.FLOOR;
	while (!moves.isEmpty()) {
	    final Magnitude move = moves.remove(0);
	    final Direction dir = move.dir;
	    int dist = move.distance;
	    while (dist > 0) {
		xpos += dir.deltaX;
		ypos += dir.deltaY;
		this.map[xpos][ypos] = Terrain.FLOOR;
		dist--;
	    }
	}
    }

    private void createCorridors() {
	for (int x = 0; x < this.horizontalRooms; x++) {
	    for (int y = 0; y < this.verticalRooms; y++) {
		final ClassicRogueRoom room = this.rooms[x][y];
		for (final ClassicRogueRoom otherRoom : room.connections) {
		    final Direction dir = Direction.getCardinalDirection(otherRoom.cellx - room.cellx,
			    otherRoom.celly - room.celly);
		    this.digPath(this.randomWallPosition(room, dir),
			    this.randomWallPosition(otherRoom, dir.opposite()));
		}
	    }
	}
    }

    private class Magnitude {
	public Direction dir;
	public int distance;

	public Magnitude(final Direction dir, final int distance) {
	    this.dir = dir;
	    this.distance = distance;
	}
    }
}
