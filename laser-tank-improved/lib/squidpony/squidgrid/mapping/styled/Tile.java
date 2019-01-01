package squidpony.squidgrid.mapping.styled;

/**
 * Part of the JSON that defines a tileset. Created by Tommy Ettinger on
 * 3/10/2015.
 */
public class Tile {
    public int a_constraint, b_constraint, c_constraint, d_constraint, e_constraint, f_constraint, width, height;
    public long[] data;

    /**
     * Probably not something you will construct manually. See DungeonBoneGen .
     */
    public Tile() {
	this.a_constraint = 0;
	this.b_constraint = 0;
	this.c_constraint = 0;
	this.d_constraint = 0;
	this.e_constraint = 0;
	this.f_constraint = 0;
	this.width = 0;
	this.height = 0;
	this.data = new long[0];
    }

    /**
     * Constructor used internally.
     *
     * @param a_constraint
     * @param b_constraint
     * @param c_constraint
     * @param d_constraint
     * @param e_constraint
     * @param f_constraint
     * @param data
     */
    public Tile(final int a_constraint, final int b_constraint, final int c_constraint, final int d_constraint,
	    final int e_constraint, final int f_constraint, final int width, final int height, final long... data) {
	this.a_constraint = a_constraint;
	this.b_constraint = b_constraint;
	this.c_constraint = c_constraint;
	this.d_constraint = d_constraint;
	this.e_constraint = e_constraint;
	this.f_constraint = f_constraint;
	this.width = width;
	this.height = height;
	this.data = data;
    }
}