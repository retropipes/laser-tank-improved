package squidpony.squidgrid.mapping;

import java.util.LinkedList;
import java.util.List;

import squidpony.annotation.Beta;
import squidpony.squidmath.Coord;
import squidpony.squidmath.PerlinNoise;
import squidpony.squidmath.StatefulRNG;

/**
 * A map generation factory using Perlin noise to make island chain style maps.
 *
 * Based largely on work done by Metsa from #rgrd
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class MetsaMapFactory {
    // HEIGHT LIMITS
    public static final double SEA_LEVEL = 0, BEACH_LEVEL = 0.15, PLAINS_LEVEL = 0.5, MOUNTAIN_LEVEL = 0.73,
	    SNOW_LEVEL = 0.95, DEEP_SEA_LEVEL = -0.1;
    // BIOMESTUFF
    private final double POLAR_LIMIT = 0.65, DESERT_LIMIT = 0.15;
    // SHADOW
    private final double SHADOW_LIMIT = 0.01;
//COLORORDER
    /*
     * 0 = deepsea 1 = beach 2 = low 3 = high 4 = mountain 5 = snowcap 6 = lowsea
     */
//            new SColor[]{SColor.DARK_SLATE_GRAY, SColor.SCHOOL_BUS_YELLOW, SColor.YELLOW_GREEN,
//        SColor.GREEN_BAMBOO, SColorFactory.lighter(SColor.LIGHT_BLUE_SILK), SColor.ALICE_BLUE, SColor.AZUL};
//            new SColor[]{SColor.DARK_SLATE_GRAY, SColor.SCHOOL_BUS_YELLOW, SColor.YELLOW_GREEN,
//        SColor.GREEN_BAMBOO, SColorFactory.lighter(SColor.LIGHT_BLUE_SILK), SColor.ALICE_BLUE, SColor.AZUL};
    private int width;
    private int height;
    private final int CITYAMOUNT = 14;
    private final List<Coord> cities = new LinkedList<>();
    private final StatefulRNG rng;
    private double maxPeak = 0;
    private double[][] map;

    public MetsaMapFactory() {
	this(240, 120, new StatefulRNG());
    }

    public MetsaMapFactory(final int width, final int height) {
	this(width, height, new StatefulRNG());
    }

    public MetsaMapFactory(final int width, final int height, final long rngSeed) {
	this(width, height, new StatefulRNG(rngSeed));
    }

    public MetsaMapFactory(final int width, final int height, final StatefulRNG rng) {
	this.rng = rng;
	this.width = width;
	this.height = height;
	this.map = this.makeHeightMap();
    }

    public int getShadow(final int x, final int y, final double[][] map) {
	if (x >= this.width - 1 || y <= 0) {
	    return 0;
	}
	final double upRight = map[x + 1][y - 1];
	final double right = map[x + 1][y];
	final double up = map[x][y - 1];
	final double cur = map[x][y];
	if (cur <= 0) {
	    return 0;
	}
	final double slope = cur - (upRight + up + right) / 3;
	if (slope < this.SHADOW_LIMIT && slope > -this.SHADOW_LIMIT) {
	    return 0;
	}
	if (slope >= this.SHADOW_LIMIT) {
	    return -1; // "alpha"
	}
	if (slope <= -this.SHADOW_LIMIT) {
	    return 1;
	} else {
	    return 0;
	}
    }

    /**
     * Finds and returns the closest point containing a city to the given point.
     * Does not include provided point as a possible city location.
     *
     * If there are no cities, null is returned.
     *
     * @param point
     * @return
     */
    public Coord closestCity(final Coord point) {
	double dist = 999999999, newdist;
	Coord closest = null;
	for (final Coord c : this.cities) {
	    if (c.equals(point)) {
		continue;// skip the one being tested for
	    }
	    newdist = Math.pow(point.x - c.x, 2) + Math.pow(point.y - c.y, 2);
	    if (newdist < dist) {
		dist = newdist;
		closest = c;
	    }
	}
	return closest;
    }

    public double[][] makeHeightMap() {
	final double[][] map = HeightMapFactory.heightMap(this.width, this.height, this.rng.nextInt());
	for (int x = 0; x < this.width / 8; x++) {
	    for (int y = 0; y < this.height; y++) {
		map[x][y] = map[x][y] - 1.0 + x / ((this.width - 1) * 0.125);
		if (map[x][y] > this.maxPeak) {
		    this.maxPeak = map[x][y];
		}
	    }
	}
	for (int x = this.width / 8; x < 7 * this.width / 8; x++) {
	    for (int y = 0; y < this.height; y++) {
		map[x][y] = map[x][y];
		if (map[x][y] > this.maxPeak) {
		    this.maxPeak = map[x][y];
		}
	    }
	}
	for (int x = 7 * this.width / 8; x < this.width; x++) {
	    for (int y = 0; y < this.height; y++) {
		map[x][y] = map[x][y] - 1.0 + (this.width - 1 - x) / ((this.width - 1) * 0.125);
		if (map[x][y] > this.maxPeak) {
		    this.maxPeak = map[x][y];
		}
	    }
	}
	return map;
    }

    public void regenerateHeightMap() {
	this.map = this.makeHeightMap();
    }

    public void regenerateHeightMap(final int width, final int height) {
	this.width = width;
	this.height = height;
	this.map = this.makeHeightMap();
	this.cities.clear();
    }

    public int[][] makeBiomeMap() {
	// biomes 0 normal 1 snow
	final int biomeMap[][] = new int[this.width][this.height];
	for (int x = 0; x < this.width; x++) {
	    for (int y = 0; y < this.height; y++) {
		biomeMap[x][y] = 0;
		double distanceFromEquator = Math.abs(y - this.height * 0.5) / (this.height * 0.5);
		distanceFromEquator += PerlinNoise.noise(x * 0.0625, y * 0.0625) / 8 + this.map[x][y] / 32;
		if (distanceFromEquator > this.POLAR_LIMIT) {
		    biomeMap[x][y] = 1;
		}
		if (distanceFromEquator < this.DESERT_LIMIT) {
		    biomeMap[x][y] = 2;
		}
		if (distanceFromEquator > this.POLAR_LIMIT + 0.25) {
		    biomeMap[x][y] = 3;
		}
	    }
	}
	return biomeMap;
    }

    public int[][] makeNationMap() {
	// nationmap, 4 times less accurate map used for nations -1 no nation
	final int nationMap[][] = new int[this.width][this.height];
	for (int i = 0; i < this.width / 4; i++) {
	    for (int j = 0; j < this.height / 4; j++) {
		if (this.map[i * 4][j * 4] < 0) {
		    nationMap[i][j] = -1;
		} else {
		    nationMap[i][j] = 0;
		}
	    }
	}
	return nationMap;
    }

    public double[][] makeWeightedMap() {
	// Weighted map for road
	final double weightedMap[][] = new double[this.width][this.height];
	final double SEALEVEL = 0;
	final double BEACHLEVEL = 0.05;
	final double PLAINSLEVEL = 0.3;
	for (int i = 0; i < this.width / 4; i++) {
	    for (int j = 0; j < this.height / 4; j++) {
		weightedMap[i][j] = 0;
		if (this.map[i * 4][j * 4] > BEACHLEVEL) {
		    weightedMap[i][j] = 2 + (this.map[i * 4][j * 4] - PLAINSLEVEL) * 8;
		}
		if (this.map[i][j] <= BEACHLEVEL && this.map[i * 4][j * 4] >= SEALEVEL) {
		    weightedMap[i][j] = 2 - this.map[i * 4][j * 4] * 2;
		}
	    }
	}
	CITIES: for (int i = 0; i < this.CITYAMOUNT; i++) {
	    int px = this.rng.between(0, this.width), py = this.rng.between(0, this.height), frustration = 0;
	    while (this.map[px][py] < SEALEVEL || this.map[px][py] > BEACHLEVEL) {
		px = this.rng.between(0, this.width);
		py = this.rng.between(0, this.height);
		if (frustration++ > 20) {
		    continue CITIES;
		}
	    }
	    this.cities.add(Coord.get(4 * (px >> 2), 4 * (py >> 2)));
	}
	return weightedMap;
    }

    public List<Coord> getCities() {
	return this.cities;
    }

    public double getMaxPeak() {
	return this.maxPeak;
    }

    public double[][] getHeightMap() {
	return this.map;
    }

    public int getHeight() {
	return this.height;
    }

    public int getWidth() {
	return this.width;
    }
}
