package squidpony.squidgrid;

import java.io.Serializable;

import squidpony.squidai.DijkstraMap.Measurement;
import squidpony.squidmath.Coord;
import squidpony.squidmath.IntDoubleOrderedMap;
import squidpony.squidmath.IntVLA;

/**
 * Some classes need detailed information about what cells are considered
 * adjacent to other cells, and may need to construct a customized mapping of
 * cells to their neighbors. Implementations of this abstract class provide
 * information about all sorts of things, including the distance metric (from
 * DijkstraMap), but also the maximum number of states that can be moved to in
 * one step (including rotations at the same point in space, in some cases), and
 * whether the type of map uses a "two-step" rule that needs two sequential
 * moves in the same direction to be viable and unobstructed to allow movement
 * (which is important in thin-wall maps). <br>
 * When CustomDijkstraMap and similar classes need to store more information
 * about a point than just its (x,y) position, they also use implementations of
 * this class to cram more information in a single int. This abstract class
 * provides methods to obtain four different numbers from a single int, though
 * not all implementations may provide all four as viable options. It also
 * provides a utility to get a Coord from an int. X and Y are exactly what they
 * always mean in 2D Coords, R is typically used for rotation, and N is
 * typically used for anything else when it is present. The convention is to use
 * N for the Z-axis when elevation/depth should be tracked, or for any more
 * specialized extensions to the information carried at a point. The composite()
 * method produces a compressed int from X, Y, R, and N values, and the
 * validate() method allows code to quickly check if an int is valid data this
 * class can use. Other information is tracked by fields, such as height, width,
 * rotations, and depths, where the maximum number of possible states is given
 * by height * width * rotations * depths, and the minimum for any of these int
 * fields is 1. <br>
 * Lastly, the neighborMaps() method produces very important information about
 * what neighbors each cell has, and by modifying the returned int[][], you can
 * produce "portal" effects, wraparound, and other useful concepts. The value it
 * returns consists of an array (with length == maxAdjacent) of arrays (each
 * with the same size, length == width * height * rotations * depth). The values
 * in the inner arrays can be any int between 0 and (width * height * rotations
 * * depth), which refers to the index in any of the inner arrays of a
 * neighboring cell, or can be -1 if there is no neighbor possible here
 * (typically at edges or corners of the map, some of the neighbors are not
 * valid and so use -1). In normal usage, a for loop is used from 0 to
 * maxAdjacent, and in each iteration the same index is looked up (the current
 * cell, encoded as by composite() or obtained as an already-composited neighbor
 * earlier), and this normally gets a different neighbor every time. In methods
 * that do a full-map search or act in a way that can possibly loop back over an
 * existing cell in the presence of wrapping (toroidal or "modulus" maps) or
 * portals, you may want to consider tracking a count of how many cells have
 * been processed and terminate any processing of further cells if the count
 * significantly exceeds the number of cells on the map (terminating when 4
 * times the cell count is reached may be the most extreme case for
 * very-portal-heavy maps). Created by Tommy Ettinger on 8/12/2016.
 */
public abstract class Adjacency implements Serializable {
    private static final long serialVersionUID = 0L;
    /**
     * The array of all possible directions this allows, regardless of cost.
     */
    public Direction[] directions;
    /**
     * The maximum number of states that can be considered adjacent; when rotations
     * are present and have a cost this is almost always 3 (move forward, turn left,
     * turn right), and in most other cases this is 4 (when using Manhattan
     * distance) or 8 (for other distance metrics).
     */
    public int maxAdjacent;
    /**
     * Only needed for thin-wall maps; this requires two steps in the same direction
     * to both be valid moves for that direction to be considered, and always moves
     * the pathfinder two steps, typically to cells with even numbers for both x and
     * y (where odd-number-position cells are used for edges or corners between
     * cells, and can still be obstacles or possible to pass through, but not stay
     * on).
     */
    public boolean twoStepRule;
    /**
     * If you want obstacles present in orthogonal cells to prevent pathfinding
     * along the diagonal between them, this can be used to make single-cell
     * diagonal walls non-viable to move through, or even to prevent diagonal
     * movement if any one obstacle is orthogonally adjacent to both the start and
     * target cell of a diagonal move. <br>
     * If this is 0, as a special case no orthogonal obstacles will block diagonal
     * moves. <br>
     * If this is 1, having one orthogonal obstacle adjacent to both the current
     * cell and the cell the pathfinder is trying to diagonally enter will block
     * diagonal moves. This generally blocks movement around corners, the "hard
     * corner" rule used in some games. <br>
     * If this is 2, having two orthogonal obstacles adjacent to both the current
     * cell and the cell the pathfinder is trying to diagonally enter will block
     * diagonal moves. As an example, if there is a wall to the north and a wall to
     * the east, then the pathfinder won't be able to move northeast even if there
     * is a floor there. <br>
     * A similar effect can be achieved with a little more control by using thin
     * walls, where the presence of a "thin corner" can block diagonal movement
     * through that corner, or the absence of a blocking wall in a corner space
     * allows movement through it.
     */
    public int blockingRule;
    /**
     * This affects how distance is measured on diagonal directions vs. orthogonal
     * directions. MANHATTAN should form a diamond shape on a featureless map, while
     * CHEBYSHEV and EUCLIDEAN will form a square. EUCLIDEAN does not affect the
     * length of paths, though it will change the DijkstraMap's gradientMap to have
     * many non-integer values, and that in turn will make paths this finds much
     * more realistic and smooth (favoring orthogonal directions unless a diagonal
     * one is a better option).
     */
    public Measurement measurement;
    /**
     * Can be changed if the map changes; you should get the neighbors from
     * neighborMaps() again after changing this.
     */
    public int width,
	    /**
	     * Can be changed if the map changes; you should get the neighbors from
	     * neighborMaps() again after changing this.
	     */
	    height,
	    /**
	     * Can be changed if the map changes; you should get the neighbors from
	     * neighborMaps() again after changing this.
	     */
	    rotations,
	    /**
	     * Can be changed if the map changes; you should get the neighbors from
	     * neighborMaps() again after changing this.
	     */
	    depths;
    protected boolean standardCost = true;

    public boolean hasStandardCost() {
	return this.standardCost;
    }

    /**
     * Used in place of a double[][] of costs in CustomDijkstraMap; allows you to
     * set the costs to enter tiles (via {@link #addCostRule(char, double)} or
     * {@link #addCostRule(char, double, boolean)} if the map has rotations). A cost
     * of 1.0 is normal for most implementations; higher costs make a movement
     * harder to perform and take more time if the game uses that mechanic, while
     * lower costs (which should always be greater than 0.0) make a move easier to
     * perform. Most games can do perfectly well with just 1.0 and 2.0, if they use
     * this at all, plus possibly a very high value for impossible moves (say,
     * 9999.0 for something like a submarine trying to enter suburbia). <br>
     * You should not alter costRules in most cases except through the Adjacency's
     * addCostRule method; most Adjacency implementations will set a flag if any
     * cost is set through addCostRule that is different from the default, and this
     * flag determines early-stop behavior in pathfinding (it can be checked with
     * {@link #hasStandardCost()}, but cannot be set directly). <br>
     * Adjacency implementations are expected to set a reasonable default value for
     * when missing keys are queried, using
     * {@link IntDoubleOrderedMap#defaultReturnValue(double)}; there may be a reason
     * for user code to call this as well.
     */
    public IntDoubleOrderedMap costRules = new IntDoubleOrderedMap(32);

    public abstract int extractX(int data);

    public abstract int extractY(int data);

    public abstract int extractR(int data);

    public abstract int extractN(int data);

    /**
     * Encodes up to four components used by this Adjacency, putting them into one
     * int. Returns -1 if the encoded position is out of bounds or otherwise
     * invalid, otherwise any int is possible. You can get the individual values
     * with {@link #extractX(int)}, {@link #extractY(int)}, {@link #extractR(int)},
     * and {@link #extractN(int)}, though not all implementations use R and N.
     *
     * @param x the x component to encode
     * @param y the y component to encode
     * @param r the rotation component to encode; not all implementations use
     *          rotation and the max value varies
     * @param n the bonus component to encode; this can be used for height or other
     *          extra data in some implementations
     * @return the encoded position as an int; -1 if invalid, non-negative for valid
     *         positions
     */
    public abstract int composite(int x, int y, int r, int n);

    public abstract boolean validate(int data);

    public Coord extractCoord(final int data) {
	return Coord.get(this.extractX(data), this.extractY(data));
    }

    public int move(final int start, final int x, final int y, final int r, final int n) {
	return this.composite(this.extractX(start) + x, this.extractY(start) + y, this.extractR(start) + r,
		this.extractN(start) + n);
    }

    public int move(final int start, final int x, final int y) {
	return this.move(start, x, y, 0, 0);
    }

    public abstract int[][][] neighborMaps();

    public abstract void portal(int[][][] neighbors, int inputPortal, int outputPortal, boolean twoWay);

    public abstract boolean isBlocked(int start, int direction, int[][][] neighbors, double[] map, double wall);

    public IntDoubleOrderedMap addCostRule(final char tile, final double cost) {
	return this.addCostRule(tile, cost, false);
    }

    public abstract IntDoubleOrderedMap addCostRule(char tile, double cost, boolean isRotation);

    public IntDoubleOrderedMap putAllVariants(final IntDoubleOrderedMap map, final int key, final double value) {
	return this.putAllVariants(map, key, value, 1);
    }

    public abstract IntDoubleOrderedMap putAllVariants(IntDoubleOrderedMap map, int key, double value, int size);

    public void putAllVariants(final IntVLA list, final double[] map, final int key, final double value) {
	this.putAllVariants(list, map, key, value, 1);
    }

    public abstract void putAllVariants(IntVLA list, double[] map, int key, double value, int size);

    public void resetAllVariants(final double[] map, final int[] keys, final double[] values) {
	this.resetAllVariants(map, keys, keys.length, values, 1);
    }

    public void resetAllVariants(final double[] map, final int[] keys, final double[] values, final int size) {
	this.resetAllVariants(map, keys, keys.length, values, 1);
    }

    public abstract void resetAllVariants(double[] map, int[] keys, int usable, double[] values, int size);

    public int[] invertAdjacent;

    public String show(final int data) {
	if (data < 0) {
	    return "(-)";
	}
	if (this.rotations <= 1) {
	    if (this.depths <= 1) {
		return "(" + this.extractX(data) + ',' + this.extractY(data) + ')';
	    }
	    return "(" + this.extractX(data) + ',' + this.extractY(data) + ',' + this.extractN(data) + ')';
	}
	if (this.depths <= 1) {
	    return "(" + this.extractX(data) + ',' + this.extractY(data) + ',' + this.extractR(data) + ')';
	}
	return "(" + this.extractX(data) + ',' + this.extractY(data) + ',' + this.extractR(data) + ','
		+ this.extractN(data) + ')';
    }

    public String showMap(final int[] map, int r) {
	r %= this.rotations;
	final StringBuilder sb = new StringBuilder(this.width * this.height * 8);
	for (int y = 0; y < this.height; y++) {
	    for (int x = 0; x < this.width; x++) {
		sb.append(this.show(map[(y * this.width + x) * this.rotations + r])).append(' ');
	    }
	    sb.append('\n');
	}
	return sb.toString();
    }

    public static class BasicAdjacency extends Adjacency implements Serializable {
	private static final long serialVersionUID = 0L;

	public BasicAdjacency(final int width, final int height, final Measurement metric) {
	    this.width = width;
	    this.height = height;
	    this.rotations = 1;
	    this.depths = 1;
	    this.measurement = metric;
	    if (metric == Measurement.MANHATTAN) {
		this.directions = Direction.CARDINALS;
		this.maxAdjacent = 4;
		this.invertAdjacent = new int[] { 1, 0, 3, 2 };
	    } else {
		this.directions = Direction.OUTWARDS;
		this.maxAdjacent = 8;
		this.invertAdjacent = new int[] { 1, 0, 3, 2, 7, 6, 5, 4 };
	    }
	    this.twoStepRule = false;
	    this.blockingRule = 2;
	    this.costRules.defaultReturnValue(1.0);
	}

	@Override
	public int extractX(final int data) {
	    return data % this.width;
	}

	@Override
	public int extractY(final int data) {
	    return data / this.width;
	}

	@Override
	public int extractR(final int data) {
	    return 0;
	}

	@Override
	public int extractN(final int data) {
	    return 0;
	}

	@Override
	public int composite(final int x, final int y, final int r, final int n) {
	    if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
		return -1;
	    }
	    return y * this.width + x;
	}

	@Override
	public int move(final int start, final int x, final int y) {
	    final int xx = start % this.width + x, yy = start / this.width + y;
	    if (xx < 0 || yy < 0 || xx >= this.width || yy >= this.height) {
		return -1;
	    }
	    return yy * this.width + xx;
	}

	@Override
	public boolean validate(final int data) {
	    return data >= 0 && this.extractY(data) < this.height;
	}

	@Override
	public int[][][] neighborMaps() {
	    final int[][][] maps = new int[2][this.maxAdjacent][this.width * this.height * this.rotations
		    * this.depths];
	    for (int m = 0; m < this.maxAdjacent; m++) {
		final Direction dir = this.directions[m];
		for (int x = 0; x < this.width; x++) {
		    for (int y = 0; y < this.height; y++) {
			maps[0][m][y * this.width + x] = this.composite(x - dir.deltaX, y - dir.deltaY, 0, 0);
			maps[1][m][y * this.width + x] = this.composite(x + dir.deltaX, y + dir.deltaY, 0, 0);
		    }
		}
	    }
	    return maps;
	}

	@Override
	public boolean isBlocked(final int start, final int direction, final int[][][] neighbors, final double[] map,
		final double wall) {
	    if (direction < 4) {
		return !this.validate(start);
	    }
	    final int[][] near = neighbors[0];
	    switch (direction) {
	    case 4: // UP_LEFT
		return (near[0][start] < 0 || map[near[0][start]] >= wall)
			&& (near[2][start] < 0 || map[near[2][start]] >= wall);
	    case 5: // UP_RIGHT
		return (near[0][start] < 0 || map[near[0][start]] >= wall)
			&& (near[3][start] < 0 || map[near[3][start]] >= wall);
	    case 6: // DOWN_LEFT
		return (near[1][start] < 0 || map[near[1][start]] >= wall)
			&& (near[2][start] < 0 || map[near[2][start]] >= wall);
	    default: // DOWN_RIGHT
		return (near[1][start] < 0 || map[near[1][start]] >= wall)
			&& (near[3][start] < 0 || map[near[3][start]] >= wall);
	    }
	}

	@Override
	public void portal(final int[][][] neighbors, final int inputPortal, final int outputPortal,
		final boolean twoWay) {
	    if (neighbors == null || !this.validate(inputPortal) || !this.validate(outputPortal)
		    || neighbors.length != this.maxAdjacent) {
		return;
	    }
	    for (int d = 0; d < this.maxAdjacent; d++) {
		for (int i = 0; i < this.width * this.height; i++) {
		    if (neighbors[1][d][i] == inputPortal) {
			neighbors[1][d][i] = outputPortal;
		    } else if (twoWay && neighbors[1][d][i] == outputPortal) {
			neighbors[1][d][i] = inputPortal;
		    }
		    if (neighbors[0][d][i] == outputPortal) {
			neighbors[0][d][i] = inputPortal;
		    } else if (twoWay && neighbors[0][d][i] == inputPortal) {
			neighbors[0][d][i] = outputPortal;
		    }
		}
	    }
	}

	@Override
	public IntDoubleOrderedMap addCostRule(final char tile, final double cost, final boolean isRotation) {
	    this.costRules.put(tile, cost);
	    if (cost != this.costRules.defaultReturnValue()) {
		this.standardCost = false;
	    }
	    return this.costRules;
	}

	@Override
	public IntDoubleOrderedMap putAllVariants(final IntDoubleOrderedMap map, final int key, final double value,
		final int size) {
	    final int baseX = key % this.width, baseY = key / this.width;
	    int comp;
	    if (key >= 0 && baseY < this.height) {
		if (size < 0) {
		    for (int x = size + 1; x <= 0; x++) {
			for (int y = size + 1; y <= 0; y++) {
			    comp = this.composite(baseX + x, baseY + y, 0, 0);
			    if (comp >= 0) {
				map.put(comp, value);
			    }
			}
		    }
		} else {
		    for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
			    comp = this.composite(baseX + x, baseY + y, 0, 0);
			    if (comp >= 0) {
				map.put(comp, value);
			    }
			}
		    }
		}
	    }
	    return map;
	}

	@Override
	public void putAllVariants(final IntVLA list, final double[] map, final int key, final double value,
		final int size) {
	    final int baseX = key % this.width, baseY = key / this.width;
	    int comp;
	    if (key >= 0 && baseY < this.height) {
		if (size < 0) {
		    if (list == null) {
			for (int x = size + 1; x <= 0; x++) {
			    for (int y = size + 1; y <= 0; y++) {
				comp = this.composite(baseX + x, baseY + y, 0, 0);
				if (comp >= 0) {
				    map[comp] = value;
				}
			    }
			}
		    } else {
			for (int x = size + 1; x <= 0; x++) {
			    for (int y = size + 1; y <= 0; y++) {
				comp = this.composite(baseX + x, baseY + y, 0, 0);
				if (comp >= 0 && !list.contains(comp)) {
				    list.add(comp);
				    map[comp] = value;
				}
			    }
			}
		    }
		} else {
		    if (list == null) {
			for (int x = 0; x < size; x++) {
			    for (int y = 0; y < size; y++) {
				comp = this.composite(baseX + x, baseY + y, 0, 0);
				if (comp >= 0) {
				    map[comp] = value;
				}
			    }
			}
		    } else {
			for (int x = 0; x < size; x++) {
			    for (int y = 0; y < size; y++) {
				comp = this.composite(baseX + x, baseY + y, 0, 0);
				if (comp >= 0 && !list.contains(comp)) {
				    list.add(comp);
				    map[comp] = value;
				}
			    }
			}
		    }
		}
	    }
	}

	@Override
	public void resetAllVariants(final double[] map, final int[] keys, final int usable, final double[] values,
		final int size) {
	    int key;
	    for (int i = 0; i < usable && i < keys.length; i++) {
		key = keys[i];
		final int baseX = key % this.width, baseY = key / this.width;
		int comp;
		if (key >= 0 && baseY < this.height) {
		    if (size < 0) {
			for (int x = size + 1; x <= 0; x++) {
			    for (int y = size + 1; y <= 0; y++) {
				comp = this.composite(baseX + x, baseY + y, 0, 0);
				if (comp >= 0) {
				    map[comp] = values[comp];
				}
			    }
			}
		    } else {
			for (int x = 0; x < size; x++) {
			    for (int y = 0; y < size; y++) {
				comp = this.composite(baseX + x, baseY + y, 0, 0);
				if (comp >= 0) {
				    map[comp] = values[comp];
				}
			    }
			}
		    }
		}
	    }
	}
    }

    public static class ThinWallAdjacency extends BasicAdjacency implements Serializable {
	private static final long serialVersionUID = 0L;

	public ThinWallAdjacency(final int width, final int height, final Measurement metric) {
	    super(width, height, metric);
	    this.twoStepRule = true;
	    this.costRules.defaultReturnValue(0.5);
	}

	@Override
	public IntDoubleOrderedMap addCostRule(final char tile, final double cost, final boolean isRotation) {
	    this.costRules.put(tile, cost * 0.5);
	    if (cost * 0.5 != this.costRules.defaultReturnValue()) {
		this.standardCost = false;
	    }
	    return this.costRules;
	}
    }

    public static class RotationAdjacency extends Adjacency implements Serializable {
	private static final long serialVersionUID = 0L;
	private int shift;

	public RotationAdjacency(final int width, final int height, final Measurement metric) {
	    this.width = width;
	    this.height = height;
	    this.measurement = metric;
	    if (metric == Measurement.MANHATTAN) {
		this.rotations = 4;
		this.shift = 2;
		this.directions = Direction.CARDINALS_CLOCKWISE;
		this.invertAdjacent = new int[] { 2, 3, 0, 1 };
	    } else {
		this.rotations = 8;
		this.shift = 3;
		this.directions = Direction.CLOCKWISE;
		this.invertAdjacent = new int[] { 4, 5, 6, 7, 0, 1, 2, 3 };
	    }
	    this.depths = 1;
	    this.maxAdjacent = 3;
	    this.twoStepRule = false;
	    this.blockingRule = 2;
	    this.costRules.defaultReturnValue(1.0);
	    // invertAdjacent = new int[]{2, 1, 0};
	}

	@Override
	public int extractX(final int data) {
	    return (data >>> this.shift) % this.width;
	}

	@Override
	public int extractY(final int data) {
	    return (data >>> this.shift) / this.width;
	}

	@Override
	public int extractR(final int data) {
	    return data & this.rotations - 1;
	}

	@Override
	public int extractN(final int data) {
	    return 0;
	}

	@Override
	public int composite(final int x, final int y, final int r, final int n) {
	    if (x < 0 || y < 0 || x >= this.width || y >= this.height || r < 0 || r >= this.rotations) {
		return -1;
	    }
	    return y * this.width + x << this.shift | r;
	}

	@Override
	public boolean validate(final int data) {
	    return data >= 0 && this.extractY(data) < this.height;
	}

	@Override
	public int[][][] neighborMaps() {
	    final int[][][] maps = new int[2][this.maxAdjacent][this.width * this.height * this.rotations
		    * this.depths];
	    int current;
	    Direction dir;
	    for (int r = 0; r < this.rotations; r++) {
		dir = this.directions[r];
		for (int x = 0; x < this.width; x++) {
		    for (int y = 0; y < this.height; y++) {
			current = y * this.width + x << this.shift | r;
			maps[0][1][current] = this.composite(x - dir.deltaX, y - dir.deltaY, r, 0);
			maps[1][1][current] = this.composite(x + dir.deltaX, y + dir.deltaY, r, 0);
			maps[0][0][current] = maps[1][0][current] = this.composite(x, y, r - 1 & this.rotations - 1, 0);
			maps[0][2][current] = maps[1][2][current] = this.composite(x, y, r + 1 & this.rotations - 1, 0);
			// maps[0][composite(x, y, r - 1 & (rotations - 1), 0)] = current;
			// maps[2][composite(x, y, r + 1 & (rotations - 1), 0)] = current;
		    }
		}
	    }
	    return maps;
	}

	@Override
	public boolean isBlocked(final int start, final int direction, final int[][][] neighbors, final double[] map,
		final double wall) {
	    if (this.rotations <= 4 || (direction & 1) == 0) {
		return !this.validate(start);
	    }
	    return neighbors[0][0][start] < 0 || map[neighbors[0][0][start]] >= wall || neighbors[0][2][start] < 0
		    || map[neighbors[0][2][start]] >= wall;
	}

	@Override
	public void portal(final int[][][] neighbors, final int inputPortal, final int outputPortal,
		final boolean twoWay) {
	    if (neighbors == null || !this.validate(inputPortal) || !this.validate(outputPortal)
		    || neighbors.length != this.maxAdjacent) {
		return;
	    }
	    for (int i = 0; i < this.width * this.height * this.rotations; i++) {
		if (neighbors[0][1][i] == inputPortal) {
		    neighbors[0][1][i] = outputPortal;
		} else if (twoWay && neighbors[0][1][i] == outputPortal) {
		    neighbors[0][1][i] = inputPortal;
		}
		if (neighbors[1][1][i] == outputPortal) {
		    neighbors[1][1][i] = inputPortal;
		} else if (twoWay && neighbors[1][1][i] == inputPortal) {
		    neighbors[1][1][i] = outputPortal;
		}
	    }
	}

	@Override
	public IntDoubleOrderedMap addCostRule(final char tile, final double cost, final boolean isRotation) {
	    if (isRotation) {
		this.costRules.put(tile | 0x10000, Math.max(0.001, cost));
		if (Math.max(0.001, cost) != this.costRules.defaultReturnValue()) {
		    this.standardCost = false;
		}
	    } else {
		this.costRules.put(tile, cost);
		if (cost != this.costRules.defaultReturnValue()) {
		    this.standardCost = false;
		}
	    }
	    return this.costRules;
	}

	@Override
	public IntDoubleOrderedMap putAllVariants(final IntDoubleOrderedMap map, final int key, final double value,
		final int size) {
	    final int baseX = (key >>> this.shift) % this.width, baseY = (key >>> this.shift) / this.width;
	    int comp;
	    if (key >= 0 && baseY < this.height) {
		if (size == 1) {
		    for (int r = 0; r < this.rotations; r++) {
			comp = this.composite(baseX, baseY, r, 0);
			if (comp >= 0) {
			    map.put(comp, value);
			}
		    }
		} else if (size < 0) {
		    for (int x = size + 1; x <= 0; x++) {
			for (int y = size + 1; y <= 0; y++) {
			    for (int r = 0; r < this.rotations; r++) {
				comp = this.composite(baseX + x, baseY + y, r, 0);
				if (comp >= 0) {
				    map.put(comp, value);
				}
			    }
			}
		    }
		} else {
		    for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
			    for (int r = 0; r < this.rotations; r++) {
				comp = this.composite(baseX + x, baseY + y, r, 0);
				if (comp >= 0) {
				    map.put(comp, value);
				}
			    }
			}
		    }
		}
	    }
	    return map;
	}

	@Override
	public void putAllVariants(final IntVLA list, final double[] map, final int key, final double value,
		final int size) {
	    final int baseX = (key >>> this.shift) % this.width, baseY = (key >>> this.shift) / this.width;
	    int comp;
	    if (key >= 0 && baseY < this.height) {
		if (size == 1) {
		    if (list == null) {
			for (int r = 0; r < this.rotations; r++) {
			    comp = this.composite(baseX, baseY, r, 0);
			    if (comp >= 0) {
				map[comp] = value;
			    }
			}
		    } else {
			for (int r = 0; r < this.rotations; r++) {
			    comp = this.composite(baseX, baseY, r, 0);
			    if (comp >= 0 && !list.contains(comp)) {
				list.add(comp);
				map[comp] = value;
			    }
			}
		    }
		} else if (size < 0) {
		    if (list == null) {
			for (int x = size + 1; x <= 0; x++) {
			    for (int y = size + 1; y <= 0; y++) {
				for (int r = 0; r < this.rotations; r++) {
				    comp = this.composite(baseX + x, baseY + y, r, 0);
				    if (comp >= 0) {
					map[comp] = value;
				    }
				}
			    }
			}
		    } else {
			for (int x = size + 1; x <= 0; x++) {
			    for (int y = size + 1; y <= 0; y++) {
				for (int r = 0; r < this.rotations; r++) {
				    comp = this.composite(baseX + x, baseY + y, r, 0);
				    if (comp >= 0 && !list.contains(comp)) {
					list.add(comp);
					map[comp] = value;
				    }
				}
			    }
			}
		    }
		} else {
		    if (list == null) {
			for (int x = 0; x < size; x++) {
			    for (int y = 0; y < size; y++) {
				for (int r = 0; r < this.rotations; r++) {
				    comp = this.composite(baseX + x, baseY + y, r, 0);
				    if (comp >= 0) {
					map[comp] = value;
				    }
				}
			    }
			}
		    } else {
			for (int x = 0; x < size; x++) {
			    for (int y = 0; y < size; y++) {
				for (int r = 0; r < this.rotations; r++) {
				    comp = this.composite(baseX + x, baseY + y, r, 0);
				    if (comp >= 0 && !list.contains(comp)) {
					list.add(comp);
					map[comp] = value;
				    }
				}
			    }
			}
		    }
		}
	    }
	}

	@Override
	public void resetAllVariants(final double[] map, final int[] keys, final int usable, final double[] values,
		final int size) {
	    int key;
	    for (int i = 0; i < usable && i < keys.length; i++) {
		key = keys[i];
		final int baseX = (key >>> this.shift) % this.width, baseY = (key >>> this.shift) / this.width;
		int comp;
		if (key >= 0 && baseY < this.height) {
		    if (size == 1) {
			for (int r = 0; r < this.rotations; r++) {
			    comp = this.composite(baseX, baseY, r, 0);
			    if (comp >= 0) {
				map[comp] = values[comp];
			    }
			}
		    } else if (size < 0) {
			for (int x = size + 1; x <= 0; x++) {
			    for (int y = size + 1; y <= 0; y++) {
				for (int r = 0; r < this.rotations; r++) {
				    comp = this.composite(baseX + x, baseY + y, r, 0);
				    if (comp >= 0) {
					map[comp] = values[comp];
				    }
				}
			    }
			}
		    } else {
			for (int x = 0; x < size; x++) {
			    for (int y = 0; y < size; y++) {
				for (int r = 0; r < this.rotations; r++) {
				    comp = this.composite(baseX + x, baseY + y, r, 0);
				    if (comp >= 0) {
					map[comp] = values[comp];
				    }
				}
			    }
			}
		    }
		}
	    }
	}
    }
}