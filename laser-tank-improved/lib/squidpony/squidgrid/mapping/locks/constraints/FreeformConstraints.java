package squidpony.squidgrid.mapping.locks.constraints;

import java.util.Collections;
import java.util.Set;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.mapping.locks.IRoomLayout;
import squidpony.squidgrid.mapping.locks.util.GenerationFailureException;
import squidpony.squidmath.Coord;
import squidpony.squidmath.IntVLA;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

public class FreeformConstraints implements ILayoutConstraints {
    public static final int DEFAULT_MAX_KEYS = 8;

    protected static class Group {
	public int id;
	public OrderedSet<Coord> coords;
	public OrderedSet<Integer> adjacentGroups;

	public Group(final int id) {
	    this.id = id;
	    this.coords = new OrderedSet<>();
	    this.adjacentGroups = new OrderedSet<>();
	}
    }

    protected ColorMap colorMap;
    protected OrderedMap<Integer, Group> groups;
    protected int maxKeys;

    public FreeformConstraints(final ColorMap colorMap) {
	this.colorMap = colorMap;
	this.groups = new OrderedMap<>();
	this.maxKeys = FreeformConstraints.DEFAULT_MAX_KEYS;
	this.analyzeMap();
    }

    protected void analyzeMap() {
	this.colorMap.checkConnected();
	for (int x = this.colorMap.getLeft(); x <= this.colorMap.getRight(); ++x) {
	    for (int y = this.colorMap.getTop(); y <= this.colorMap.getBottom(); ++y) {
		final Integer val = this.colorMap.get(x, y);
		if (val == null) {
		    continue;
		}
		Group group = this.groups.get(val);
		if (group == null) {
		    group = new Group(val);
		    this.groups.put(val, group);
		}
		group.coords.add(Coord.get(x + 127, y + 127));
	    }
	}
	System.out.println(this.groups.size() + " groups");
	for (final Group group : this.groups.values()) {
	    for (final Coord xy : group.coords) {
		for (final Direction d : Direction.CARDINALS) {
		    final Coord neighbor = xy.translate(d);
		    if (group.coords.contains(neighbor)) {
			continue;
		    }
		    final Integer val = this.colorMap.get(neighbor.x, neighbor.y);
		    if (val != null && this.allowRoomsToBeAdjacent(group.id, val)) {
			group.adjacentGroups.add(val);
		    }
		}
	    }
	}
	this.checkConnected();
    }

    protected boolean isConnected() {
	// This is different from ColorMap.checkConnected because it also checks
	// what the client says for allowRoomsToBeAdjacent allows the map to be
	// full connected.
	// Do a breadth first search starting at the top left to check if
	// every position is reachable.
	final OrderedSet<Integer> world = this.groups.keysAsOrderedSet(), queue = new OrderedSet<>();
	final Integer first = world.first();
	world.remove(first);
	queue.add(first);
	while (!queue.isEmpty()) {
	    final Integer pos = queue.removeFirst();
	    final IntVLA rooms = this.getAdjacentRooms(pos, this.getMaxKeys() + 1);
	    for (int i = 0; i < rooms.size; i++) {
		final Integer adjId = rooms.get(i);
		if (world.contains(adjId)) {
		    world.remove(adjId);
		    queue.add(adjId);
		}
	    }
	}
	return world.size() == 0;
    }

    protected void checkConnected() {
	if (!this.isConnected()) {
	    // Parts of the map are unreachable!
	    throw new GenerationFailureException("ColorMap is not fully connected");
	}
    }

    @Override
    public int getMaxRooms() {
	return this.groups.size();
    }

    @Override
    public int getMaxKeys() {
	return this.maxKeys;
    }

    public void setMaxKeys(final int maxKeys) {
	this.maxKeys = maxKeys;
    }

    @Override
    public int getMaxSwitches() {
	return 0;
    }

    @Override
    public IntVLA initialRooms() {
	return IntVLA.with(this.groups.getAt(0).id);
    }

    @Override
    public IntVLA getAdjacentRooms(final int id, final int keyLevel) {
	final IntVLA options = new IntVLA();
	for (final int i : this.groups.get(id).adjacentGroups) {
	    options.add(i);
	}
	return options;
    }

    /*
     * The reason for this being separate from getAdjacentRooms is that this method
     * is called at most once for each pair of rooms during analyzeMap, while
     * getAdjacentRooms is called many times during generation under the assumption
     * that it's simply a cheap "getter". Subclasses may override this method to
     * perform more expensive checks than with getAdjacentRooms.
     */
    protected boolean allowRoomsToBeAdjacent(final int id0, final int id1) {
	return true;
    }

    @Override
    public Set<Coord> getCoords(final int id) {
	return Collections.unmodifiableSet(this.groups.get(id).coords);
    }

    @Override
    public boolean isAcceptable(final IRoomLayout dungeon) {
	return true;
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
