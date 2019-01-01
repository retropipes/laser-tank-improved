package squidpony.squidgrid.mapping.locks.util;

import java.util.List;

import squidpony.squidgrid.mapping.Rectangle;
import squidpony.squidmath.Coord;

/**
 * Created by Tommy Ettinger on 1/4/2017.
 */
public class Rect2I extends Rectangle.Impl {
    public Coord topLeft;
    public int width;
    public int height;

    public Rect2I(final Coord min, final int w, final int h) {
	super(min, w, h);
	this.topLeft = min;
	this.width = w;
	this.height = h;
    }

    public static Rect2I fromExtremes(final int minX, final int minY, final int maxX, final int maxY) {
	return new Rect2I(Coord.get(minX, minY), maxX - minX, maxY - minY);
    }

    @Override
    public Coord getBottomLeft() {
	return this.topLeft;
    }

    @Override
    public int getWidth() {
	return this.width;
    }

    @Override
    public int getHeight() {
	return this.height;
    }

    public int left() {
	return this.topLeft.x;
    }

    public int top() {
	return this.topLeft.y;
    }

    public int right() {
	return this.topLeft.x + this.width;
    }

    public int bottom() {
	return this.topLeft.y + this.height;
    }

    @Override
    public boolean isEmpty() {
	return this.width > 0 && this.height > 0;
    }

    @Override
    public int size() {
	return this.width * this.height;
    }

    @Override
    public boolean contains(final int x, final int y) {
	return x >= this.topLeft.x && x < this.topLeft.x + this.width && y >= this.topLeft.y
		&& y < this.topLeft.y + this.height;
    }

    @Override
    public boolean contains(final Coord coord) {
	return coord.x >= this.topLeft.x && coord.x < this.topLeft.x + this.width && coord.y >= this.topLeft.y
		&& coord.y < this.topLeft.y + this.height;
    }

    @Override
    public List<Coord> getAll() {
	return Rectangle.Utils.cellsList(this);
    }
}
