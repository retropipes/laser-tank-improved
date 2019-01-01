package squidpony.squidgrid.mapping;

import java.util.ArrayList;

import squidpony.annotation.Beta;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;
import squidpony.squidmath.RNG;

/**
 *
 *
 * Based in part on code from
 * http://weblog.jamisbuck.org/2011/1/27/maze-generation-growing-tree-algorithm
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class GrowingTreeMazeGenerator {
    private final RNG rng;
    private final int width, height;

    public GrowingTreeMazeGenerator(final int width, final int height) {
	this.width = width;
	this.height = height;
	this.rng = new RNG();
    }

    public GrowingTreeMazeGenerator(final int width, final int height, final RNG rng) {
	this.width = width;
	this.height = height;
	this.rng = rng;
    }

    /**
     * Builds and returns a boolean mapping of a maze using the provided chooser
     * method object.
     *
     * @param choosing the callback object for making the split decision
     * @return
     */
    public boolean[][] create(final ChoosingMethod choosing) {
	final boolean[][] map = new boolean[this.width][this.height];
	final boolean[][] visited = new boolean[this.width][this.height];
	int x = this.rng.nextInt(this.width / 2);
	int y = this.rng.nextInt(this.height / 2);
	x *= 2;
	y *= 2;
	final ArrayList<Coord> deck = new ArrayList<>();
	deck.add(Coord.get(x, y));
	Direction[] dirs = Direction.CARDINALS;
	while (!deck.isEmpty()) {
	    final int i = choosing.chooseIndex(deck.size());
	    final Coord p = deck.get(i);
	    dirs = this.rng.shuffle(dirs, new Direction[dirs.length]);
	    boolean foundNeighbor = false;
	    for (final Direction dir : dirs) {
		x = p.x + dir.deltaX * 2;
		y = p.y + dir.deltaY * 2;
		if (x >= 0 && x < this.width && y >= 0 && y < this.height) {
		    if (!visited[x][y]) {
			foundNeighbor = true;
//                        if (rng.nextBoolean()) {
			visited[x][y] = true;
//                        }
			map[x][y] = true;
			map[p.x + dir.deltaX][p.y + dir.deltaY] = true;
			deck.add(Coord.get(x, y));
			break;
		    }
		}
	    }
	    if (!foundNeighbor) {
		deck.remove(p);
	    }
	}
	return map;
    }

    public interface ChoosingMethod {
	/**
	 * Given the size to choose from, will return a single value smaller than the
	 * passed in value and greater than or equal to 0. The value chosen is dependant
	 * on the individual implementation.
	 *
	 * @param size
	 * @return
	 */
	int chooseIndex(int size);
    }
}
