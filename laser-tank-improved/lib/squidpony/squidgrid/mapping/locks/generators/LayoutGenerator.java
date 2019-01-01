package squidpony.squidgrid.mapping.locks.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.mapping.locks.Condition;
import squidpony.squidgrid.mapping.locks.Condition.SwitchState;
import squidpony.squidgrid.mapping.locks.Edge;
import squidpony.squidgrid.mapping.locks.IRoomLayout;
import squidpony.squidgrid.mapping.locks.Room;
import squidpony.squidgrid.mapping.locks.RoomLayout;
import squidpony.squidgrid.mapping.locks.Symbol;
import squidpony.squidgrid.mapping.locks.constraints.ILayoutConstraints;
import squidpony.squidgrid.mapping.locks.util.GenerationFailureException;
import squidpony.squidmath.Coord;
import squidpony.squidmath.IntVLA;
import squidpony.squidmath.RNG;

/**
 * The default and reference implementation of an {@link ILayoutGenerator}.
 */
public class LayoutGenerator implements ILayoutGenerator {
    public static final int MAX_RETRIES = 20;
    protected RNG random;
    protected RoomLayout dungeon;
    protected ILayoutConstraints constraints;
    protected boolean bossRoomLocked, generateGoal;

    /**
     * Creates a LayoutGenerator with a given random seed and places specific
     * constraints on {@link IRoomLayout}s it generates.
     *
     * @param rng         the random number generator to use
     * @param constraints the constraints to place on generation
     * @see ILayoutConstraints
     */
    public LayoutGenerator(final RNG rng, final ILayoutConstraints constraints) {
	this.random = rng;
	assert constraints != null;
	this.constraints = constraints;
	this.bossRoomLocked = this.generateGoal = true;
    }

    /**
     * Randomly chooses a {@link Room} within the given collection that has at least
     * one adjacent empty space.
     *
     * @param roomCollection the collection of rooms to choose from
     * @return the room that was chosen, or null if there are no rooms with adjacent
     *         empty spaces
     */
    protected Room chooseRoomWithFreeEdge(final Collection<Room> roomCollection, final int keyLevel) {
	final List<Room> rooms = new ArrayList<>(roomCollection);
	this.random.shuffle(rooms);
	Room room;
	IntVLA near;
	for (int i = 0; i < rooms.size(); ++i) {
	    room = rooms.get(i);
	    near = this.constraints.getAdjacentRooms(room.id, keyLevel);
	    for (int j = 0; j < near.size; j++) {
		if (this.dungeon.get(near.get(j)) == null) {
		    return room;
		}
	    }
	}
	return null;
    }

    /**
     * Randomly chooses a {@link Direction} in which the given {@link Room} has an
     * adjacent empty space.
     *
     * @param room the room
     * @return the Direction of the empty space chosen adjacent to the Room or null
     *         if there are no adjacent empty spaces
     */
    protected int chooseFreeEdge(final Room room, final int keyLevel) {
	final IntVLA neighbors = new IntVLA(this.constraints.getAdjacentRooms(room.id, keyLevel));
	neighbors.shuffle(this.random);
	while (neighbors.size > 0) {
	    final int choice = neighbors.getRandomElement(this.random);
	    if (this.dungeon.get(choice) == null) {
		return choice;
	    }
	    neighbors.removeValue(choice);
	}
	assert false;
	throw new GenerationFailureException("Internal error: Room doesn't have a free edge");
    }

    /**
     * Maps 'keyLevel' to the set of rooms within that keyLevel.
     * <p>
     * A 'keyLevel' is the count of the number of unique keys are needed for all the
     * locks we've placed. For example, all the rooms in keyLevel 0 are accessible
     * without collecting any keys, while to get to rooms in keyLevel 3, the player
     * must have collected at least 3 keys.
     */
    protected class KeyLevelRoomMapping {
	protected List<List<Room>> map = new ArrayList<>(LayoutGenerator.this.constraints.getMaxKeys());

	List<Room> getRooms(final int keyLevel) {
	    while (keyLevel >= this.map.size()) {
		this.map.add(null);
	    }
	    if (this.map.get(keyLevel) == null) {
		this.map.set(keyLevel, new ArrayList<Room>());
	    }
	    return this.map.get(keyLevel);
	}

	void addRoom(final int keyLevel, final Room room) {
	    this.getRooms(keyLevel).add(room);
	}

	int keyCount() {
	    return this.map.size();
	}
    }

    /**
     * Thrown by several ILayoutGenerator methods that can fail. Should be caught
     * and handled in {@link #generate}.
     */
    protected static class RetryException extends Exception {
	private static final long serialVersionUID = 1L;
    }

    protected static class OutOfRoomsException extends Exception {
	private static final long serialVersionUID = 1L;
    }

    /**
     * Comparator objects for sorting {@link Room}s in a couple of different ways.
     * These are used to determine in which rooms of a given keyLevel it is best to
     * place the next key.
     *
     * @see #placeKeys
     */
    protected static final Comparator<Room> EDGE_COUNT_COMPARATOR = new Comparator<>() {
	@Override
	public int compare(final Room arg0, final Room arg1) {
	    return arg0.linkCount() - arg1.linkCount();
	}
    }, INTENSITY_COMPARATOR = new Comparator<>() {
	@Override
	public int compare(final Room arg0, final Room arg1) {
	    return arg0.getIntensity() > arg1.getIntensity() ? -1 : arg0.getIntensity() < arg1.getIntensity() ? 1 : 0;
	}
    };

    /**
     * Sets up the dungeon's entrance room.
     *
     * @param levels the keyLevel -> room-set mapping to update
     * @see KeyLevelRoomMapping
     */
    protected void initEntranceRoom(final KeyLevelRoomMapping levels) throws RetryException {
	int id;
	final IntVLA possibleEntries = this.constraints.initialRooms();
	assert possibleEntries.size > 0;
	id = possibleEntries.getRandomElement(this.random);
	final Room entry = new Room(id, this.constraints.getCoords(id), null, Symbol.START, new Condition());
	this.dungeon.add(entry);
	levels.addRoom(0, entry);
    }

    /**
     * Decides whether to add a new lock (and keyLevel) at this point.
     *
     * @param keyLevel           the number of distinct locks that have been placed
     *                           into the map so far
     * @param numRooms           the number of rooms at the current keyLevel
     * @param targetRoomsPerLock the number of rooms the generator has chosen as the
     *                           target number of rooms to place at each keyLevel
     *                           (which subclasses can ignore, if desired).
     */
    protected boolean shouldAddNewLock(final int keyLevel, final int numRooms, final int targetRoomsPerLock) {
	int usableKeys = this.constraints.getMaxKeys();
	if (this.isBossRoomLocked()) {
	    usableKeys -= 1;
	}
	return numRooms >= targetRoomsPerLock && keyLevel < usableKeys;
    }

    /**
     * Fill the dungeon's space with rooms and doors (some locked). Keys are not
     * inserted at this point.
     *
     * @param levels the keyLevel -> room-set mapping to update
     * @throws RetryException if it fails
     * @see KeyLevelRoomMapping
     */
    protected void placeRooms(final KeyLevelRoomMapping levels, final int roomsPerLock)
	    throws RetryException, OutOfRoomsException {
	// keyLevel: the number of keys required to get to the new room
	int keyLevel = 0;
	int latestKey = Symbol.NOTHING;
	// condition that must hold true for the player to reach the new room
	// (the set of keys they must have).
	Condition cond = new Condition();
	// Loop to place rooms and link them
	while (this.dungeon.roomCount() < this.constraints.getMaxRooms()) {
	    boolean doLock = false;
	    // Decide whether we need to place a new lock
	    // (Don't place the last lock, since that's reserved for the boss)
	    if (this.shouldAddNewLock(keyLevel, levels.getRooms(keyLevel).size(), roomsPerLock)) {
		latestKey = keyLevel++;
		cond = cond.and(latestKey);
		doLock = true;
	    }
	    // Find an existing room with a free edge:
	    Room parentRoom = null;
	    if (!doLock && this.random.nextIntHasty(10) > 0) {
		parentRoom = this.chooseRoomWithFreeEdge(levels.getRooms(keyLevel), keyLevel);
	    }
	    if (parentRoom == null) {
		parentRoom = this.chooseRoomWithFreeEdge(this.dungeon.getRooms(), keyLevel);
		doLock = true;
	    }
	    if (parentRoom == null) {
		throw new OutOfRoomsException();
	    }
	    // Decide which direction to put the new room in relative to the
	    // parent
	    final int nextId = this.chooseFreeEdge(parentRoom, keyLevel);
	    final Set<Coord> coords = this.constraints.getCoords(nextId);
	    final Room room = new Room(nextId, coords, parentRoom, Symbol.NOTHING, cond);
	    // Add the room to the dungeon
	    assert this.dungeon.get(room.id) == null;
	    // synchronized(dungeon) {
	    this.dungeon.add(room);
	    parentRoom.addChild(room);
	    this.dungeon.link(parentRoom, room, doLock ? latestKey : Symbol.NOTHING);
	    // }
	    levels.addRoom(keyLevel, room);
	}
    }

    /**
     * Places the BOSS and GOAL rooms within the dungeon, in existing rooms. These
     * rooms are moved into the next keyLevel.
     *
     * @param levels the keyLevel -> room-set mapping to update
     * @throws RetryException if it fails
     * @see KeyLevelRoomMapping
     */
    protected void placeBossGoalRooms(final KeyLevelRoomMapping levels) throws RetryException {
	final List<Room> possibleGoalRooms = new ArrayList<>(this.dungeon.roomCount());
	final int goalSym = Symbol.GOAL, bossSym = Symbol.BOSS;
	for (final Room room : this.dungeon.getRooms()) {
	    if (room.getChildren().size() > 0 || room.getItem() != Symbol.NOTHING) {
		continue;
	    }
	    final Room parent = room.getParent();
	    if (parent == null || parent.getChildren().size() != 1 || room.getItem() != Symbol.NOTHING
		    || !parent.getPrecond().implies(room.getPrecond())) {
		continue;
	    }
	    if (this.isGenerateGoal()) {
		if (!this.constraints.roomCanFitItem(room.id, goalSym)
			|| !this.constraints.roomCanFitItem(parent.id, bossSym)) {
		    continue;
		}
	    } else {
		if (!this.constraints.roomCanFitItem(room.id, bossSym)) {
		    continue;
		}
	    }
	    possibleGoalRooms.add(room);
	}
	if (possibleGoalRooms.size() == 0) {
	    throw new RetryException();
	}
	Room goalRoom = this.random.getRandomElement(possibleGoalRooms), bossRoom = goalRoom.getParent();
	if (!this.isGenerateGoal()) {
	    bossRoom = goalRoom;
	    goalRoom = null;
	}
	if (goalRoom != null) {
	    goalRoom.setItem(goalSym);
	}
	bossRoom.setItem(bossSym);
	if (this.isBossRoomLocked()) {
	    final int oldKeyLevel = bossRoom.getPrecond().getKeyLevel(),
		    newKeyLevel = Math.min(levels.keyCount(), this.constraints.getMaxKeys());
	    final List<Room> oklRooms = levels.getRooms(oldKeyLevel);
	    if (goalRoom != null) {
		oklRooms.remove(goalRoom);
	    }
	    oklRooms.remove(bossRoom);
	    if (goalRoom != null) {
		levels.addRoom(newKeyLevel, goalRoom);
	    }
	    levels.addRoom(newKeyLevel, bossRoom);
	    final int bossKey = newKeyLevel - 1;
	    final Condition precond = bossRoom.getPrecond().and(bossKey);
	    bossRoom.setPrecond(precond);
	    if (goalRoom != null) {
		goalRoom.setPrecond(precond);
	    }
	    if (newKeyLevel == 0) {
		this.dungeon.link(bossRoom.getParent(), bossRoom);
	    } else {
		this.dungeon.link(bossRoom.getParent(), bossRoom, bossKey);
	    }
	    if (goalRoom != null) {
		this.dungeon.link(bossRoom, goalRoom);
	    }
	}
    }

    /**
     * Removes the given {@link Room} and all its descendants from the given list.
     *
     * @param rooms the list of Rooms to remove nodes from
     * @param room  the Room whose descendants to remove from the list
     */
    protected void removeDescendantsFromList(final List<Room> rooms, final Room room) {
	rooms.remove(room);
	for (final Room child : room.getChildren()) {
	    this.removeDescendantsFromList(rooms, child);
	}
    }

    /**
     * Adds extra conditions to the given {@link Room}'s preconditions and all of
     * its descendants.
     *
     * @param room the Room to add extra preconditions to
     * @param cond the extra preconditions to add
     */
    protected void addPrecond(final Room room, final Condition cond) {
	room.setPrecond(room.getPrecond().and(cond));
	for (final Room child : room.getChildren()) {
	    this.addPrecond(child, cond);
	}
    }

    /**
     * Randomly locks descendant rooms of the given {@link Room} with {@link Edge}s
     * that require the switch to be in the given state.
     * <p>
     * If the given state is EITHER, the required states will be random.
     *
     * @param room       the room whose child to lock
     * @param givenState the state to require the switch to be in for the child
     *                   rooms to be accessible
     * @return true if any locks were added, false if none were added (which can
     *         happen due to the way the random decisions are made)
     * @see SwitchState
     */
    protected boolean switchLockChildRooms(final Room room, final SwitchState givenState) {
	boolean anyLocks = false;
	SwitchState state = givenState != SwitchState.EITHER ? givenState
		: this.random.nextBoolean() ? SwitchState.ON : SwitchState.OFF;
	for (final Edge edge : room.getEdges()) {
	    final int neighborId = edge.getTargetRoomId();
	    final Room nextRoom = this.dungeon.get(neighborId);
	    if (room.getChildren().contains(nextRoom)) {
		if (room.getEdge(neighborId).getSymbol() == Symbol.NOTHING && this.random.nextIntHasty(4) != 0) {
		    this.dungeon.link(room, nextRoom, state.toSymbol());
		    this.addPrecond(nextRoom, new Condition(state.toSymbol()));
		    anyLocks = true;
		} else {
		    anyLocks |= this.switchLockChildRooms(nextRoom, state);
		}
		if (givenState == SwitchState.EITHER) {
		    state = state.invert();
		}
	    }
	}
	return anyLocks;
    }

    /**
     * Returns a path from the goal to the dungeon entrance, along the 'parent'
     * relations.
     *
     * @return a list of linked {@link Room}s starting with the goal room and ending
     *         with the start room.
     */
    protected List<Room> getSolutionPath() {
	final List<Room> solution = new ArrayList<>();
	Room room = this.dungeon.findGoal();
	while (room != null) {
	    solution.add(room);
	    room = room.getParent();
	}
	return solution;
    }

    /**
     * Makes some {@link Edge}s within the dungeon require the dungeon's switch to
     * be in a particular state, and places the switch in a room in the dungeon.
     *
     * @throws RetryException if it fails
     */
    protected void placeSwitches() throws RetryException {
	// Possible TODO: have multiple switches on separate circuits
	// At the moment, we only have one switch per dungeon.
	if (this.constraints.getMaxSwitches() <= 0) {
	    return;
	}
	final List<Room> solution = this.getSolutionPath();
	for (int attempt = 0; attempt < 10; ++attempt) {
	    final List<Room> rooms = new ArrayList<>(this.dungeon.getRooms());
	    this.random.shuffle(rooms);
	    this.random.shuffle(solution);
	    // Pick a base room from the solution path so that the player
	    // will have to encounter a switch-lock to solve the dungeon.
	    Room baseRoom = null;
	    for (final Room room : solution) {
		if (room.getChildren().size() > 1 && room.getParent() != null) {
		    baseRoom = room;
		    break;
		}
	    }
	    if (baseRoom == null) {
		throw new RetryException();
	    }
	    final Condition baseRoomCond = baseRoom.getPrecond();
	    this.removeDescendantsFromList(rooms, baseRoom);
	    final int switchSym = Symbol.SWITCH;
	    Room switchRoom = null;
	    for (final Room room : rooms) {
		if (room.getItem() == Symbol.NOTHING && baseRoomCond.implies(room.getPrecond())
			&& this.constraints.roomCanFitItem(room.id, switchSym)) {
		    switchRoom = room;
		    break;
		}
	    }
	    if (switchRoom == null) {
		continue;
	    }
	    if (this.switchLockChildRooms(baseRoom, SwitchState.EITHER)) {
		switchRoom.setItem(switchSym);
		return;
	    }
	}
	throw new RetryException();
    }

    /**
     * Randomly links up some adjacent rooms to make the dungeon graph less of a
     * tree.
     *
     * @throws RetryException if it fails
     */
    protected void graphify() throws RetryException {
	IntVLA near;
	for (final Room room : this.dungeon.getRooms()) {
	    if (room.isGoal() || room.isBoss()) {
		continue;
	    }
	    near = this.constraints.getAdjacentRooms(room.id, Integer.MAX_VALUE);
	    for (int i = 0; i < near.size; i++) {
		// Doesn't matter what the keyLevel is; later checks about
		// preconds ensure linkage doesn't trivialize the puzzle.
		final int nextId = near.get(i);
		if (room.getEdge(nextId) != null) {
		    continue;
		}
		final Room nextRoom = this.dungeon.get(nextId);
		if (nextRoom == null || nextRoom.isGoal() || nextRoom.isBoss()) {
		    continue;
		}
		final boolean forwardImplies = room.getPrecond().implies(nextRoom.getPrecond()),
			backwardImplies = nextRoom.getPrecond().implies(room.getPrecond());
		if (forwardImplies && backwardImplies) {
		    // both rooms are at the same keyLevel.
		    if (this.random.nextDouble() >= this.constraints.edgeGraphifyProbability(room.id, nextRoom.id)) {
			continue;
		    }
		    this.dungeon.link(room, nextRoom);
		} else {
		    final int difference = room.getPrecond().singleSymbolDifference(nextRoom.getPrecond());
		    if (difference == Symbol.NOTHING || !Symbol.isSwitchState(difference) && this.random
			    .nextDouble() >= this.constraints.edgeGraphifyProbability(room.id, nextRoom.id)) {
			continue;
		    }
		    this.dungeon.link(room, nextRoom, difference);
		}
	    }
	}
    }

    /**
     * Places keys within the dungeon in such a way that the dungeon is guaranteed
     * to be solvable.
     *
     * @param levels the keyLevel -> room-set mapping to use
     * @throws RetryException if it fails
     * @see KeyLevelRoomMapping
     */
    protected void placeKeys(final KeyLevelRoomMapping levels) throws RetryException {
	// Now place the keys. For every key-level but the last one, place a
	// key for the next level in it, preferring rooms with fewest links
	// (dead end rooms).
	for (int key = 0; key < levels.keyCount() - 1; ++key) {
	    final List<Room> rooms = levels.getRooms(key);
	    this.random.shuffle(rooms);
	    // Collections.sort is stable: it doesn't reorder "equal" elements,
	    // which means the shuffling we just did is still useful.
	    Collections.sort(rooms, LayoutGenerator.INTENSITY_COMPARATOR);
	    // Alternatively, use the EDGE_COUNT_COMPARATOR to put keys at
	    // 'dead end' rooms.
	    boolean placedKey = false;
	    for (final Room room : rooms) {
		if (room.getItem() == Symbol.NOTHING && this.constraints.roomCanFitItem(room.id, key)) {
		    room.setItem(key);
		    placedKey = true;
		    break;
		}
	    }
	    if (!placedKey) {
		// there were no rooms into which the key would fit
		throw new RetryException();
	    }
	}
    }

    protected static final double INTENSITY_GROWTH_JITTER = 0.1, INTENSITY_EASE_OFF = 0.2;

    /**
     * Recursively applies the given intensity to the given {@link Room}, and higher
     * intensities to each of its descendants that are within the same keyLevel.
     * <p>
     * Intensities set by this method may (will) be outside of the normal range from
     * 0.0 to 1.0. See {@link #normalizeIntensity} to correct this.
     *
     * @param room      the room to set the intensity of
     * @param intensity the value to set intensity to (some randomn variance is
     *                  added)
     * @see Room
     */
    protected double applyIntensity(final Room room, double intensity) {
	intensity *= 1.0 - LayoutGenerator.INTENSITY_GROWTH_JITTER / 2.0
		+ LayoutGenerator.INTENSITY_GROWTH_JITTER * this.random.nextDouble();
	room.setIntensity(intensity);
	double maxIntensity = intensity;
	for (final Room child : room.getChildren()) {
	    if (room.getPrecond().implies(child.getPrecond())) {
		maxIntensity = Math.max(maxIntensity, this.applyIntensity(child, intensity + 1.0));
	    }
	}
	return maxIntensity;
    }

    /**
     * Scales intensities within the dungeon down so that they all fit within the
     * range 0 <= intensity < 1.0.
     *
     * @see Room
     */
    protected void normalizeIntensity() {
	double maxIntensity = 0.0;
	for (final Room room : this.dungeon.getRooms()) {
	    maxIntensity = Math.max(maxIntensity, room.getIntensity());
	}
	for (final Room room : this.dungeon.getRooms()) {
	    room.setIntensity(room.getIntensity() * 0.99 / maxIntensity);
	}
    }

    /**
     * Computes the 'intensity' of each {@link Room}. Rooms generally get more
     * intense the deeper they are into the dungeon.
     *
     * @param levels the keyLevel -> room-set mapping to update
     * @throws RetryException if it fails
     * @see KeyLevelRoomMapping
     * @see Room
     */
    protected void computeIntensity(final KeyLevelRoomMapping levels) throws RetryException {
	double nextLevelBaseIntensity = 0.0;
	for (int level = 0; level < levels.keyCount(); ++level) {
	    final double intensity = nextLevelBaseIntensity * (1.0 - LayoutGenerator.INTENSITY_EASE_OFF);
	    for (final Room room : levels.getRooms(level)) {
		if (room.getParent() == null || !room.getParent().getPrecond().implies(room.getPrecond())) {
		    nextLevelBaseIntensity = Math.max(nextLevelBaseIntensity, this.applyIntensity(room, intensity));
		}
	    }
	}
	this.normalizeIntensity();
	this.dungeon.findBoss().setIntensity(1.0);
	final Room goalRoom = this.dungeon.findGoal();
	if (goalRoom != null) {
	    goalRoom.setIntensity(0.0);
	}
    }

    /**
     * Checks with the {@link ILayoutConstraints} that the dungeon is OK to use.
     *
     * @throws RetryException if the ILayoutConstraints decided generation must be
     *                        re-attempted
     * @see ILayoutConstraints
     */
    protected void checkAcceptable() throws RetryException {
	if (!this.constraints.isAcceptable(this.dungeon)) {
	    throw new RetryException();
	}
    }

    @Override
    public void generate() {
	int attempt = 0;
	while (true) {
	    try {
		KeyLevelRoomMapping levels;
		int roomsPerLock;
		if (this.constraints.getMaxKeys() > 0) {
		    roomsPerLock = this.constraints.getMaxRooms() / this.constraints.getMaxKeys();
		} else {
		    roomsPerLock = this.constraints.getMaxRooms();
		}
		while (true) {
		    this.dungeon = new RoomLayout();
		    // Maps keyLevel -> Rooms that were created when lockCount had that
		    // value
		    levels = new KeyLevelRoomMapping();
		    // Create the entrance to the dungeon:
		    this.initEntranceRoom(levels);
		    try {
			// Fill the dungeon with rooms:
			this.placeRooms(levels, roomsPerLock);
			break;
		    } catch (final OutOfRoomsException e) {
			// We can run out of rooms where certain links have
			// predetermined locks. Example: if a river bisects the
			// map, the keyLevel for rooms in the river > 0 because
			// crossing water requires a key. If there are not
			// enough rooms before the river to build up to the
			// key for the river, we've run out of rooms.
			// log("Ran out of rooms. roomsPerLock was "+roomsPerLock);
			roomsPerLock = roomsPerLock * this.constraints.getMaxKeys()
				/ (this.constraints.getMaxKeys() + 1);
			// log("roomsPerLock is now "+roomsPerLock);
			if (roomsPerLock == 0) {
			    throw new GenerationFailureException(
				    "Failed to place rooms. Have you forgotten to disable boss-locking?");
			    // If the boss room is locked, the final key is used
			    // only for the boss room. So if the final key is
			    // also used to cross the river, rooms cannot be
			    // placed.
			}
		    }
		}
		// Place the boss and goal rooms:
		this.placeBossGoalRooms(levels);
		// Place switches and the locks that require it:
		this.placeSwitches();
		// Make the dungeon less tree-like:
		this.graphify();
		this.computeIntensity(levels);
		// Place the keys within the dungeon:
		this.placeKeys(levels);
		if (levels.keyCount() - 1 != this.constraints.getMaxKeys()) {
		    throw new RetryException();
		}
		this.checkAcceptable();
		return;
	    } catch (final RetryException e) {
		if (++attempt > LayoutGenerator.MAX_RETRIES) {
		    throw new GenerationFailureException("Dungeon generator failed", e);
		}
		// log("Retrying dungeon generation...");
	    }
	}
    }

    @Override
    public IRoomLayout getRoomLayout() {
	return this.dungeon;
    }

    public boolean isBossRoomLocked() {
	return this.bossRoomLocked;
    }

    public void setBossRoomLocked(final boolean bossRoomLocked) {
	this.bossRoomLocked = bossRoomLocked;
    }

    public boolean isGenerateGoal() {
	return this.generateGoal;
    }

    public void setGenerateGoal(final boolean generateGoal) {
	this.generateGoal = generateGoal;
    }
}
