package squidpony.squidgrid.zone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import squidpony.squidgrid.zone.Zone.Skeleton;
import squidpony.squidmath.Coord;

/**
 * A zone defined by a {@link List}.
 *
 * @author smelC
 */
public class ListZone extends Skeleton {
    protected final List<Coord> coords;
    private static final long serialVersionUID = 1166468942544595692L;

    public ListZone(final List<Coord> coords) {
	this.coords = coords;
    }

    public ListZone(final Coord[] coords) {
	this.coords = new ArrayList<>(coords.length);
	Collections.addAll(this.coords, coords);
    }

    public ListZone(final Collection<Coord> coordCollection) {
	this.coords = new ArrayList<>(coordCollection);
    }

    @Override
    public boolean isEmpty() {
	return this.coords.isEmpty();
    }

    @Override
    public int size() {
	return this.coords.size();
    }

    @Override
    public boolean contains(final Coord c) {
	return this.coords.contains(c);
    }

    @Override
    public boolean contains(final int x, final int y) {
	return this.coords.contains(Coord.get(x, y));
    }

    @Override
    public List<Coord> getAll() {
	return this.coords;
    }

    /**
     * @return The list that backs up {@code this}. Use at your own risks.
     */
    public List<Coord> getState() {
	return this.coords;
    }

    @Override
    public String toString() {
	return this.coords.toString();
    }
}