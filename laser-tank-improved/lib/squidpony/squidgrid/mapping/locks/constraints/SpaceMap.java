package squidpony.squidgrid.mapping.locks.constraints;

import java.util.ArrayList;

import squidpony.squidgrid.mapping.locks.Room;
import squidpony.squidgrid.mapping.locks.generators.ILayoutGenerator;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;

/**
 * Controls which spaces are valid for an {@link ILayoutGenerator} to create
 * {@link Room}s in.
 * <p>
 * Essentially just a Set<{@link Coord}> with some convenience methods.
 *
 * @see Coord
 * @see SpaceConstraints
 */
public class SpaceMap {
    protected OrderedSet<Coord> spaces = new OrderedSet<>();

    public int numberSpaces() {
	return this.spaces.size();
    }

    public boolean get(final Coord c) {
	return this.spaces.contains(c);
    }

    public void set(final Coord c, final boolean val) {
	if (val) {
	    this.spaces.add(c);
	} else {
	    this.spaces.remove(c);
	}
    }

    private Coord getFirst() {
	return this.spaces.first();
    }

    public ArrayList<Coord> getBottomSpaces() {
	final ArrayList<Coord> bottomRow = new ArrayList<>();
	bottomRow.add(this.getFirst());
	int bottomY = this.getFirst().y;
	for (final Coord space : this.spaces) {
	    if (space.y > bottomY) {
		bottomY = space.y;
		bottomRow.clear();
		bottomRow.add(space);
	    } else if (space.y == bottomY) {
		bottomRow.add(space);
	    }
	}
	return bottomRow;
    }
}