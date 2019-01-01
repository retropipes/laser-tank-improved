package squidpony.squidgrid.mapping;

import squidpony.annotation.Beta;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.IntVLA;
import squidpony.squidmath.Noise;
import squidpony.squidmath.Noise.Noise3D;
import squidpony.squidmath.Noise.Noise4D;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.RNG;
import squidpony.squidmath.SeededNoise;
import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.VanDerCorputQRNG;

/**
 * Can be used to generate world maps with a wide variety of data, starting with
 * height, temperature and moisture. From there, you can determine biome
 * information in as much detail as your game needs, with a default
 * implementation available that assigns a single biome to each cell based on
 * heat/moisture. The maps this produces are valid for spherical or toroidal
 * world projections, and will wrap from edge to opposite edge seamlessly thanks
 * to a technique from the Accidental Noise Library (
 * https://www.gamedev.net/blog/33/entry-2138456-seamless-noise/ ) that involves
 * getting a 2D slice of 4D Simplex noise. Because of how Simplex noise works,
 * this also allows extremely high zoom levels as long as certain parameters are
 * within reason. You can access the height map with the {@link #heightData}
 * field, the heat map with the {@link #heatData} field, the moisture map with
 * the {@link #moistureData} field, and a special map that stores ints
 * representing the codes for various ranges of elevation (0 to 8 inclusive,
 * with 0 the deepest ocean and 8 the highest mountains) with
 * {@link #heightCodeData}. The last map should be noted as being the simplest
 * way to find what is land and what is water; any height code 4 or greater is
 * land, and any height code 3 or less is water. This can produce rivers, and
 * keeps that information in a GreasedRegion (alongside a GreasedRegion
 * containing lake positions) instead of in the other map data. This class does
 * not use Coord at all, but if you want maps with width and/or height greater
 * than 256, and you want to use the river or lake data as a Collection of
 * Coord, then you should call {@link Coord#expandPoolTo(int, int)} with your
 * width and height so the Coords remain safely pooled. If you're fine with
 * keeping rivers and lakes as GreasedRegions and not requesting Coord values
 * from them, then you don't need to do anything with Coord. Certain parts of
 * this class are not necessary to generate, just in case you want river-less
 * maps or something similar; setting {@link #generateRivers} to false will
 * disable river generation (it defaults to true). <br>
 * The main trade-off this makes to obtain better quality is reduced speed;
 * generating a 512x512 map on a circa-2016 laptop (i7-6700HQ processor at 2.6
 * GHz) takes about 1 second (about 1.15 seconds for an un-zoomed map, 0.95 or
 * so seconds to increase zoom at double resolution). If you don't need a
 * 512x512 map, this takes commensurately less time to generate less grid cells,
 * with 64x64 maps generating faster than they can be accurately seen on the
 * same hardware. River positions are produced using a different method, and do
 * not involve the Simplex noise parts other than using the height map to
 * determine flow. Zooming with rivers is tricky, and generally requires
 * starting from the outermost zoom level and progressively enlarging and adding
 * detail to all rivers as zoom increases on specified points.
 */
@Beta
public abstract class WorldMapGenerator {
    public final int width, height;
    public long seed, cachedState;
    public StatefulRNG rng;
    public boolean generateRivers = true;
    public final double[][] heightData, heatData, moistureData;
    public final GreasedRegion landData, riverData, lakeData, partialRiverData, partialLakeData;
    protected transient GreasedRegion workingData;
    public final int[][] heightCodeData;
    public double waterModifier = -1.0, coolingModifier = 1.0, minHeight = Double.POSITIVE_INFINITY,
	    maxHeight = Double.NEGATIVE_INFINITY, minHeightActual = Double.POSITIVE_INFINITY,
	    maxHeightActual = Double.NEGATIVE_INFINITY, minHeat = Double.POSITIVE_INFINITY,
	    maxHeat = Double.NEGATIVE_INFINITY, minWet = Double.POSITIVE_INFINITY, maxWet = Double.NEGATIVE_INFINITY;
    public int zoom = 0;
    protected IntVLA startCacheX = new IntVLA(8), startCacheY = new IntVLA(8);
    public static final double deepWaterLower = -1.0, deepWaterUpper = -0.7, // 0
	    mediumWaterLower = -0.7, mediumWaterUpper = -0.3, // 1
	    shallowWaterLower = -0.3, shallowWaterUpper = -0.1, // 2
	    coastalWaterLower = -0.1, coastalWaterUpper = 0.1, // 3
	    sandLower = 0.1, sandUpper = 0.18, // 4
	    grassLower = 0.18, grassUpper = 0.35, // 5
	    forestLower = 0.35, forestUpper = 0.6, // 6
	    rockLower = 0.6, rockUpper = 0.8, // 7
	    snowLower = 0.8, snowUpper = 1.0; // 8
    public static final double[] lowers = { WorldMapGenerator.deepWaterLower, WorldMapGenerator.mediumWaterLower,
	    WorldMapGenerator.shallowWaterLower, WorldMapGenerator.coastalWaterLower, WorldMapGenerator.sandLower,
	    WorldMapGenerator.grassLower, WorldMapGenerator.forestLower, WorldMapGenerator.rockLower,
	    WorldMapGenerator.snowLower };

    /**
     * Constructs a WorldMapGenerator (this class is abstract, so you should
     * typically call this from a subclass or as part of an anonymous class that
     * implements {@link #regenerate(int, int, int, int, double, double, long)}).
     * Always makes a 256x256 map. If you were using
     * {@link WorldMapGenerator#WorldMapGenerator(long, int, int)}, then this would
     * be the same as passing the parameters {@code 0x1337BABE1337D00DL, 256, 256}.
     */
    protected WorldMapGenerator() {
	this(0x1337BABE1337D00DL, 256, 256);
    }

    /**
     * Constructs a WorldMapGenerator (this class is abstract, so you should
     * typically call this from a subclass or as part of an anonymous class that
     * implements {@link #regenerate(int, int, int, int, double, double, long)}).
     * Takes only the width/height of the map. The initial seed is set to the same
     * large long every time, and it's likely that you would set the seed when you
     * call {@link #generate(long)}. The width and height of the map cannot be
     * changed after the fact, but you can zoom in.
     *
     * @param mapWidth  the width of the map(s) to generate; cannot be changed later
     * @param mapHeight the height of the map(s) to generate; cannot be changed
     *                  later
     */
    protected WorldMapGenerator(final int mapWidth, final int mapHeight) {
	this(0x1337BABE1337D00DL, mapWidth, mapHeight);
    }

    /**
     * Constructs a WorldMapGenerator (this class is abstract, so you should
     * typically call this from a subclass or as part of an anonymous class that
     * implements {@link #regenerate(int, int, int, int, double, double, long)}).
     * Takes an initial seed and the width/height of the map. The
     * {@code initialSeed} parameter may or may not be used, since you can specify
     * the seed to use when you call {@link #generate(long)}. The width and height
     * of the map cannot be changed after the fact, but you can zoom in.
     *
     * @param initialSeed the seed for the StatefulRNG this uses; this may also be
     *                    set per-call to generate
     * @param mapWidth    the width of the map(s) to generate; cannot be changed
     *                    later
     * @param mapHeight   the height of the map(s) to generate; cannot be changed
     *                    later
     */
    protected WorldMapGenerator(final long initialSeed, final int mapWidth, final int mapHeight) {
	this.width = mapWidth;
	this.height = mapHeight;
	this.seed = initialSeed;
	this.cachedState = ~initialSeed;
	this.rng = new StatefulRNG(initialSeed);
	this.heightData = new double[this.width][this.height];
	this.heatData = new double[this.width][this.height];
	this.moistureData = new double[this.width][this.height];
	this.landData = new GreasedRegion(this.width, this.height);
	this.riverData = new GreasedRegion(this.width, this.height);
	this.lakeData = new GreasedRegion(this.width, this.height);
	this.partialRiverData = new GreasedRegion(this.width, this.height);
	this.partialLakeData = new GreasedRegion(this.width, this.height);
	this.workingData = new GreasedRegion(this.width, this.height);
	this.heightCodeData = new int[this.width][this.height];
    }

    /**
     * Generates a world using a random RNG state and all parameters randomized. The
     * worlds this produces will always have width and height as specified in the
     * constructor (default 256x256). You can call {@link #zoomIn(int, int, int)} to
     * double the resolution and center on the specified area, but the width and
     * height of the 2D arrays this changed, such as {@link #heightData} and
     * {@link #moistureData} will be the same.
     */
    public void generate() {
	this.generate(this.rng.nextLong());
    }

    /**
     * Generates a world using the specified RNG state as a long. Other parameters
     * will be randomized, using the same RNG state to start with. The worlds this
     * produces will always have width and height as specified in the constructor
     * (default 256x256). You can call {@link #zoomIn(int, int, int)} to double the
     * resolution and center on the specified area, but the width and height of the
     * 2D arrays this changed, such as {@link #heightData} and {@link #moistureData}
     * will be the same.
     *
     * @param state the state to give this generator's RNG; if the same as the last
     *              call, this will reuse data
     */
    public void generate(final long state) {
	this.generate(-1.0, -1.0, state);
    }

    /**
     * Generates a world using the specified RNG state as a long, with specific
     * water and cooling modifiers that affect the land-water ratio and the average
     * temperature, respectively. The worlds this produces will always have width
     * and height as specified in the constructor (default 256x256). You can call
     * {@link #zoomIn(int, int, int)} to double the resolution and center on the
     * specified area, but the width and height of the 2D arrays this changed, such
     * as {@link #heightData} and {@link #moistureData} will be the same.
     *
     * @param waterMod should be between 0.85 and 1.2; a random value will be used
     *                 if this is negative
     * @param coolMod  should be between 0.85 and 1.4; a random value will be used
     *                 if this is negative
     * @param state    the state to give this generator's RNG; if the same as the
     *                 last call, this will reuse data
     */
    public void generate(final double waterMod, final double coolMod, final long state) {
	if (this.cachedState != state || waterMod != this.waterModifier || coolMod != this.coolingModifier) {
	    this.seed = state;
	    this.zoom = 0;
	    this.startCacheX.clear();
	    this.startCacheY.clear();
	    this.startCacheX.add(0);
	    this.startCacheY.add(0);
	}
	this.regenerate(this.startCacheX.peek(), this.startCacheY.peek(), this.width >> this.zoom,
		this.height >> this.zoom, waterMod, coolMod, state);
    }

    /**
     * Halves the resolution of the map and doubles the area it covers; the 2D
     * arrays this uses keep their sizes. This version of zoomOut always zooms out
     * from the center of the currently used area. <br>
     * Only has an effect if you have previously zoomed in using
     * {@link #zoomIn(int, int, int)} or its overload.
     */
    public void zoomOut() {
	this.zoomOut(1, this.width >> 1, this.height >> 1);
    }

    /**
     * Halves the resolution of the map and doubles the area it covers repeatedly,
     * halving {@code zoomAmount} times; the 2D arrays this uses keep their sizes.
     * This version of zoomOut allows you to specify where the zoom should be
     * centered, using the current coordinates (if the map size is 256x256, then
     * coordinates should be between 0 and 255, and will refer to the currently used
     * area and not necessarily the full world size). <br>
     * Only has an effect if you have previously zoomed in using
     * {@link #zoomIn(int, int, int)} or its overload.
     *
     * @param zoomCenterX the center X position to zoom out from; if too close to an
     *                    edge, this will stop moving before it would extend past an
     *                    edge
     * @param zoomCenterY the center Y position to zoom out from; if too close to an
     *                    edge, this will stop moving before it would extend past an
     *                    edge
     */
    public void zoomOut(final int zoomAmount, final int zoomCenterX, final int zoomCenterY) {
	if (zoomAmount == 0) {
	    return;
	}
	if (zoomAmount < 0) {
	    this.zoomIn(-zoomAmount, zoomCenterX, zoomCenterY);
	    return;
	}
	if (this.zoom > 0) {
	    if (this.seed != this.cachedState) {
		this.generate(this.rng.nextLong());
	    }
	    this.zoom -= zoomAmount;
	    this.startCacheX.pop();
	    this.startCacheY.pop();
	    this.startCacheX.add(Math.min(Math
		    .max(this.startCacheX.pop() + (zoomCenterX >> this.zoom + 1) - (this.width >> this.zoom + 2), 0),
		    this.width - (this.width >> this.zoom)));
	    this.startCacheY.add(Math.min(Math
		    .max(this.startCacheY.pop() + (zoomCenterY >> this.zoom + 1) - (this.height >> this.zoom + 2), 0),
		    this.height - (this.height >> this.zoom)));
	    this.regenerate(this.startCacheX.peek(), this.startCacheY.peek(), this.width >> this.zoom,
		    this.height >> this.zoom, this.waterModifier, this.coolingModifier, this.cachedState);
	    this.rng.setState(this.cachedState);
	}
    }

    /**
     * Doubles the resolution of the map and halves the area it covers; the 2D
     * arrays this uses keep their sizes. This version of zoomIn always zooms in to
     * the center of the currently used area. <br>
     * Although there is no technical restriction on maximum zoom, zooming in more
     * than 5 times (64x scale or greater) will make the map appear somewhat less
     * realistic due to rounded shapes appearing more bubble-like and less like a
     * normal landscape.
     */
    public void zoomIn() {
	this.zoomIn(1, this.width >> 1, this.height >> 1);
    }

    /**
     * Doubles the resolution of the map and halves the area it covers repeatedly,
     * doubling {@code zoomAmount} times; the 2D arrays this uses keep their sizes.
     * This version of zoomIn allows you to specify where the zoom should be
     * centered, using the current coordinates (if the map size is 256x256, then
     * coordinates should be between 0 and 255, and will refer to the currently used
     * area and not necessarily the full world size). <br>
     * Although there is no technical restriction on maximum zoom, zooming in more
     * than 5 times (64x scale or greater) will make the map appear somewhat less
     * realistic due to rounded shapes appearing more bubble-like and less like a
     * normal landscape.
     *
     * @param zoomCenterX the center X position to zoom in to; if too close to an
     *                    edge, this will stop moving before it would extend past an
     *                    edge
     * @param zoomCenterY the center Y position to zoom in to; if too close to an
     *                    edge, this will stop moving before it would extend past an
     *                    edge
     */
    public void zoomIn(final int zoomAmount, final int zoomCenterX, final int zoomCenterY) {
	if (zoomAmount == 0) {
	    return;
	}
	if (zoomAmount < 0) {
	    this.zoomOut(-zoomAmount, zoomCenterX, zoomCenterY);
	    return;
	}
	if (this.seed != this.cachedState) {
	    this.generate(this.rng.nextLong());
	}
	this.zoom += zoomAmount;
	if (this.startCacheX.isEmpty()) {
	    this.startCacheX.add(0);
	    this.startCacheY.add(0);
	} else {
	    this.startCacheX.add(Math.min(Math
		    .max(this.startCacheX.peek() + (zoomCenterX >> this.zoom - 1) - (this.width >> this.zoom + 1), 0),
		    this.width - (this.width >> this.zoom)));
	    this.startCacheY.add(Math.min(Math
		    .max(this.startCacheY.peek() + (zoomCenterY >> this.zoom - 1) - (this.height >> this.zoom + 1), 0),
		    this.height - (this.height >> this.zoom)));
	}
	this.regenerate(this.startCacheX.peek(), this.startCacheY.peek(), this.width >> this.zoom,
		this.height >> this.zoom, this.waterModifier, this.coolingModifier, this.cachedState);
	this.rng.setState(this.cachedState);
    }

    protected abstract void regenerate(int startX, int startY, int usedWidth, int usedHeight, double waterMod,
	    double coolMod, long state);

    public int codeHeight(final double high) {
	if (high < WorldMapGenerator.deepWaterUpper) {
	    return 0;
	}
	if (high < WorldMapGenerator.mediumWaterUpper) {
	    return 1;
	}
	if (high < WorldMapGenerator.shallowWaterUpper) {
	    return 2;
	}
	if (high < WorldMapGenerator.coastalWaterUpper) {
	    return 3;
	}
	if (high < WorldMapGenerator.sandUpper) {
	    return 4;
	}
	if (high < WorldMapGenerator.grassUpper) {
	    return 5;
	}
	if (high < WorldMapGenerator.forestUpper) {
	    return 6;
	}
	if (high < WorldMapGenerator.rockUpper) {
	    return 7;
	}
	return 8;
    }

    protected final int decodeX(final int coded) {
	return coded % this.width;
    }

    protected final int decodeY(final int coded) {
	return coded / this.width;
    }

    protected int wrapX(final int x) {
	return (x + this.width) % this.width;
    }

    protected int wrapY(final int y) {
	return (y + this.height) % this.height;
    }

    private static final Direction[] reuse = new Direction[6];

    private void appendDirToShuffle(final RNG rng) {
	rng.randomPortion(Direction.CARDINALS, WorldMapGenerator.reuse);
	WorldMapGenerator.reuse[rng.next(2)] = Direction.DIAGONALS[rng.next(2)];
	WorldMapGenerator.reuse[4] = Direction.DIAGONALS[rng.next(2)];
	WorldMapGenerator.reuse[5] = Direction.OUTWARDS[rng.next(3)];
    }

    protected void addRivers() {
	this.landData.refill(this.heightCodeData, 4, 999);
	final long rebuildState = this.rng.nextLong();
	// workingData.allOn();
	// .empty().insertRectangle(8, 8, width - 16, height - 16);
	this.riverData.empty().refill(this.heightCodeData, 6, 100);
	this.riverData.quasiRandomRegion(0.0036);
	final int[] starts = this.riverData.asTightEncoded();
	final int len = starts.length;
	int currentPos, choice, adjX, adjY, currX, currY, tcx, tcy, stx, sty, sbx, sby;
	this.riverData.clear();
	this.lakeData.clear();
	PER_RIVER: for (int i = 0; i < len; i++) {
	    this.workingData.clear();
	    currentPos = starts[i];
	    stx = tcx = currX = this.decodeX(currentPos);
	    sty = tcy = currY = this.decodeY(currentPos);
	    while (true) {
		double best = 999999;
		choice = -1;
		this.appendDirToShuffle(this.rng);
		for (int d = 0; d < 5; d++) {
		    adjX = this.wrapX(currX + WorldMapGenerator.reuse[d].deltaX);
		    /*
		     * if (adjX < 0 || adjX >= width) { if(rng.next(4) == 0)
		     * riverData.or(workingData); continue PER_RIVER; }
		     */
		    adjY = this.wrapY(currY + WorldMapGenerator.reuse[d].deltaY);
		    if (this.heightData[adjX][adjY] < best && !this.workingData.contains(adjX, adjY)) {
			best = this.heightData[adjX][adjY];
			choice = d;
			tcx = adjX;
			tcy = adjY;
		    }
		}
		currX = tcx;
		currY = tcy;
		if (best >= this.heightData[stx][sty]) {
		    tcx = this.rng.next(2);
		    adjX = this.wrapX(currX + ((tcx & 1) << 1) - 1);
		    adjY = this.wrapY(currY + (tcx & 2) - 1);
		    this.lakeData.insert(currX, currY);
		    this.lakeData.insert(this.wrapX(currX + 1), currY);
		    this.lakeData.insert(this.wrapX(currX - 1), currY);
		    this.lakeData.insert(currX, this.wrapY(currY + 1));
		    this.lakeData.insert(currX, this.wrapY(currY - 1));
		    if (this.heightCodeData[adjX][adjY] <= 3) {
			this.riverData.or(this.workingData);
			continue PER_RIVER;
		    } else if ((this.heightData[adjX][adjY] -= 0.0002) < 0.0) {
			if (this.rng.next(3) == 0) {
			    this.riverData.or(this.workingData);
			}
			continue PER_RIVER;
		    }
		    tcx = this.rng.next(2);
		    adjX = this.wrapX(currX + ((tcx & 1) << 1) - 1);
		    adjY = this.wrapY(currY + (tcx & 2) - 1);
		    if (this.heightCodeData[adjX][adjY] <= 3) {
			this.riverData.or(this.workingData);
			continue PER_RIVER;
		    } else if ((this.heightData[adjX][adjY] -= 0.0002) < 0.0) {
			if (this.rng.next(3) == 0) {
			    this.riverData.or(this.workingData);
			}
			continue PER_RIVER;
		    }
		}
		if (choice != -1 && WorldMapGenerator.reuse[choice].isDiagonal()) {
		    tcx = this.wrapX(currX - WorldMapGenerator.reuse[choice].deltaX);
		    tcy = this.wrapY(currY - WorldMapGenerator.reuse[choice].deltaY);
		    if (this.heightData[tcx][currY] <= this.heightData[currX][tcy]
			    && !this.workingData.contains(tcx, currY)) {
			if (this.heightCodeData[tcx][currY] < 3 || this.riverData.contains(tcx, currY)) {
			    this.riverData.or(this.workingData);
			    continue PER_RIVER;
			}
			this.workingData.insert(tcx, currY);
		    } else if (!this.workingData.contains(currX, tcy)) {
			if (this.heightCodeData[currX][tcy] < 3 || this.riverData.contains(currX, tcy)) {
			    this.riverData.or(this.workingData);
			    continue PER_RIVER;
			}
			this.workingData.insert(currX, tcy);
		    }
		}
		if (this.heightCodeData[currX][currY] < 3 || this.riverData.contains(currX, currY)) {
		    this.riverData.or(this.workingData);
		    continue PER_RIVER;
		}
		this.workingData.insert(currX, currY);
	    }
	}
	final GreasedRegion tempData = new GreasedRegion(this.width, this.height);
	final int riverCount = this.riverData.size() >> 4, currentMax = riverCount >> 3;
	int idx = 0, prevChoice;
	for (int h = 5; h < 9; h++) { // , currentMax += riverCount / 18
	    this.workingData.empty().refill(this.heightCodeData, h).and(this.riverData);
	    RIVER: for (int j = 0; j < currentMax && idx < riverCount; j++) {
		final double vdc = VanDerCorputQRNG.weakDetermine(idx++);
		double best = -999999;
		currentPos = this.workingData.atFractionTight(vdc);
		if (currentPos < 0) {
		    break;
		}
		stx = sbx = tcx = currX = this.decodeX(currentPos);
		sty = sby = tcy = currY = this.decodeY(currentPos);
		this.appendDirToShuffle(this.rng);
		choice = -1;
		prevChoice = -1;
		for (int d = 0; d < 5; d++) {
		    adjX = this.wrapX(currX + WorldMapGenerator.reuse[d].deltaX);
		    adjY = this.wrapY(currY + WorldMapGenerator.reuse[d].deltaY);
		    if (this.heightData[adjX][adjY] > best) {
			best = this.heightData[adjX][adjY];
			prevChoice = choice;
			choice = d;
			sbx = tcx;
			sby = tcy;
			tcx = adjX;
			tcy = adjY;
		    }
		}
		currX = sbx;
		currY = sby;
		if (prevChoice != -1 && this.heightCodeData[currX][currY] >= 4) {
		    if (WorldMapGenerator.reuse[prevChoice].isDiagonal()) {
			tcx = this.wrapX(currX - WorldMapGenerator.reuse[prevChoice].deltaX);
			tcy = this.wrapY(currY - WorldMapGenerator.reuse[prevChoice].deltaY);
			if (this.heightData[tcx][currY] <= this.heightData[currX][tcy]) {
			    if (this.heightCodeData[tcx][currY] < 3) {
				this.riverData.or(tempData);
				continue;
			    }
			    tempData.insert(tcx, currY);
			} else {
			    if (this.heightCodeData[currX][tcy] < 3) {
				this.riverData.or(tempData);
				continue;
			    }
			    tempData.insert(currX, tcy);
			}
		    }
		    if (this.heightCodeData[currX][currY] < 3) {
			this.riverData.or(tempData);
			continue;
		    }
		    tempData.insert(currX, currY);
		}
		while (true) {
		    best = -999999;
		    this.appendDirToShuffle(this.rng);
		    choice = -1;
		    for (int d = 0; d < 6; d++) {
			adjX = this.wrapX(currX + WorldMapGenerator.reuse[d].deltaX);
			adjY = this.wrapY(currY + WorldMapGenerator.reuse[d].deltaY);
			if (this.heightData[adjX][adjY] > best && !this.riverData.contains(adjX, adjY)) {
			    best = this.heightData[adjX][adjY];
			    choice = d;
			    sbx = adjX;
			    sby = adjY;
			}
		    }
		    currX = sbx;
		    currY = sby;
		    if (choice != -1) {
			if (WorldMapGenerator.reuse[choice].isDiagonal()) {
			    tcx = this.wrapX(currX - WorldMapGenerator.reuse[choice].deltaX);
			    tcy = this.wrapY(currY - WorldMapGenerator.reuse[choice].deltaY);
			    if (this.heightData[tcx][currY] <= this.heightData[currX][tcy]) {
				if (this.heightCodeData[tcx][currY] < 3) {
				    this.riverData.or(tempData);
				    continue RIVER;
				}
				tempData.insert(tcx, currY);
			    } else {
				if (this.heightCodeData[currX][tcy] < 3) {
				    this.riverData.or(tempData);
				    continue RIVER;
				}
				tempData.insert(currX, tcy);
			    }
			}
			if (this.heightCodeData[currX][currY] < 3) {
			    this.riverData.or(tempData);
			    continue RIVER;
			}
			tempData.insert(currX, currY);
		    } else {
			this.riverData.or(tempData);
			tempData.clear();
			continue RIVER;
		    }
		    if (best <= this.heightData[stx][sty]
			    || this.heightData[currX][currY] > this.rng.nextDouble(280.0)) {
			this.riverData.or(tempData);
			tempData.clear();
			if (this.heightCodeData[currX][currY] < 3) {
			    continue RIVER;
			}
			this.lakeData.insert(currX, currY);
			sbx = this.rng.next(8);
			sbx &= sbx >>> 4;
			if ((sbx & 1) == 0) {
			    this.lakeData.insert(this.wrapX(currX + 1), currY);
			}
			if ((sbx & 2) == 0) {
			    this.lakeData.insert(this.wrapX(currX - 1), currY);
			}
			if ((sbx & 4) == 0) {
			    this.lakeData.insert(currX, this.wrapY(currY + 1));
			}
			if ((sbx & 8) == 0) {
			    this.lakeData.insert(currX, this.wrapY(currY - 1));
			}
			sbx = this.rng.next(2);
			this.lakeData.insert(this.wrapX(currX + (-(sbx & 1) | 1)), this.wrapY(currY + (sbx & 2) - 1)); // random
			// diagonal
			this.lakeData.insert(currX, this.wrapY(currY + (sbx & 2) - 1)); // ortho next to random diagonal
			this.lakeData.insert(this.wrapX(currX + (-(sbx & 1) | 1)), currY); // ortho next to random
											   // diagonal
			continue RIVER;
		    }
		}
	    }
	}
	this.rng.setState(rebuildState);
    }

    /**
     * A way to get biome information for the cells on a map when you only need a
     * single value to describe a biome, such as "Grassland" or
     * "TropicalRainforest". <br>
     * To use: 1, Construct a SimpleBiomeMapper (constructor takes no arguments). 2,
     * call {@link #makeBiomes(WorldMapGenerator)} with a WorldMapGenerator that has
     * already produced at least one world map. 3, get biome codes from the
     * {@link #biomeCodeData} field, where a code is an int that can be used as an
     * index into the {@link #biomeTable} static field to get a String name for a
     * biome type, or used with an alternate biome table of your design. Biome
     * tables in this case are 54-element arrays organized into groups of 6
     * elements. Each group goes from the coldest temperature first to the warmest
     * temperature last in the group. The first group of 6 contains the dryest
     * biomes, the next 6 are medium-dry, the next are slightly-dry, the next
     * slightly-wet, then medium-wet, then wettest. After this first block of
     * dry-to-wet groups, there is a group of 6 for coastlines, a group of 6 for
     * rivers, and lastly a group for lakes. This also assigns moisture codes and
     * heat codes from 0 to 5 for each cell, which may be useful to simplify logic
     * that deals with those factors.
     */
    public static class SimpleBiomeMapper {
	/**
	 * The heat codes for the analyzed map, from 0 to 5 inclusive, with 0 coldest
	 * and 5 hottest.
	 */
	public int[][] heatCodeData,
		/**
		 * The moisture codes for the analyzed map, from 0 to 5 inclusive, with 0 driest
		 * and 5 wettest.
		 */
		moistureCodeData,
		/**
		 * The biome codes for the analyzed map, from 0 to 53 inclusive. You can use
		 * {@link #biomeTable} to look up String names for biomes, or construct your own
		 * table as you see fit (see docs in {@link SimpleBiomeMapper}).
		 */
		biomeCodeData;
	public static final double coldestValueLower = 0.0, coldestValueUpper = 0.15, // 0
		colderValueLower = 0.15, colderValueUpper = 0.31, // 1
		coldValueLower = 0.31, coldValueUpper = 0.5, // 2
		warmValueLower = 0.5, warmValueUpper = 0.69, // 3
		warmerValueLower = 0.69, warmerValueUpper = 0.85, // 4
		warmestValueLower = 0.85, warmestValueUpper = 1.0, // 5
		driestValueLower = 0.0, driestValueUpper = 0.27, // 0
		drierValueLower = 0.27, drierValueUpper = 0.4, // 1
		dryValueLower = 0.4, dryValueUpper = 0.6, // 2
		wetValueLower = 0.6, wetValueUpper = 0.8, // 3
		wetterValueLower = 0.8, wetterValueUpper = 0.9, // 4
		wettestValueLower = 0.9, wettestValueUpper = 1.0; // 5
	/**
	 * The default biome table to use with biome codes from {@link #biomeCodeData}.
	 * Biomes are assigned based on heat and moisture for the first 36 of 54
	 * elements (coldest to warmest for each group of 6, with the first group as the
	 * dryest and the last group the wettest), then the next 6 are for coastlines
	 * (coldest to warmest), then rivers (coldest to warmest), then lakes (coldest
	 * to warmest).
	 */
	public static final String[] biomeTable = {
		// COLDEST //COLDER //COLD //HOT //HOTTER //HOTTEST
		"Ice", "Ice", "Grassland", "Desert", "Desert", "Desert", // DRYEST
		"Ice", "Tundra", "Grassland", "Grassland", "Desert", "Desert", // DRYER
		"Ice", "Tundra", "Woodland", "Woodland", "Savanna", "Desert", // DRY
		"Ice", "Tundra", "SeasonalForest", "SeasonalForest", "Savanna", "Savanna", // WET
		"Ice", "Tundra", "BorealForest", "TemperateRainforest", "TropicalRainforest", "Savanna", // WETTER
		"Ice", "BorealForest", "BorealForest", "TemperateRainforest", "TropicalRainforest",
		"TropicalRainforest", // WETTEST
		"Rocky", "Rocky", "Beach", "Beach", "Beach", "Beach", // COASTS
		"Ice", "River", "River", "River", "River", "River", // RIVERS
		"Ice", "River", "River", "River", "River", "River", // LAKES
	};

	/**
	 * Simple constructor; pretty much does nothing. Make sure to call
	 * {@link #makeBiomes(WorldMapGenerator)} before using fields like
	 * {@link #biomeCodeData}.
	 */
	public SimpleBiomeMapper() {
	    this.heatCodeData = null;
	    this.moistureCodeData = null;
	    this.biomeCodeData = null;
	}

	/**
	 * Analyzes the last world produced by the given WorldMapGenerator and uses all
	 * of its generated information to assign biome codes for each cell (along with
	 * heat and moisture codes). After calling this, biome codes can be taken from
	 * {@link #biomeCodeData} and used as indices into {@link #biomeTable} or a
	 * custom biome table.
	 *
	 * @param world a WorldMapGenerator that should have generated at least one map;
	 *              it may be at any zoom
	 */
	public void makeBiomes(final WorldMapGenerator world) {
	    if (world == null || world.width <= 0 || world.height <= 0) {
		return;
	    }
	    if (this.heatCodeData == null || this.heatCodeData.length != world.width
		    || this.heatCodeData[0].length != world.height) {
		this.heatCodeData = new int[world.width][world.height];
	    }
	    if (this.moistureCodeData == null || this.moistureCodeData.length != world.width
		    || this.moistureCodeData[0].length != world.height) {
		this.moistureCodeData = new int[world.width][world.height];
	    }
	    if (this.biomeCodeData == null || this.biomeCodeData.length != world.width
		    || this.biomeCodeData[0].length != world.height) {
		this.biomeCodeData = new int[world.width][world.height];
	    }
	    final double i_hot = world.maxHeat == world.minHeat ? 1.0 : 1.0 / (world.maxHeat - world.minHeat);
	    for (int x = 0; x < world.width; x++) {
		for (int y = 0; y < world.height; y++) {
		    final double hot = (world.heatData[x][y] - world.minHeat) * i_hot, moist = world.moistureData[x][y];
		    final int heightCode = world.heightCodeData[x][y];
		    int hc, mc;
		    final boolean isLake = world.generateRivers && world.partialLakeData.contains(x, y)
			    && heightCode >= 4,
			    isRiver = world.generateRivers && world.partialRiverData.contains(x, y) && heightCode >= 4;
		    if (moist > SimpleBiomeMapper.wetterValueUpper) {
			mc = 5;
		    } else if (moist > SimpleBiomeMapper.wetValueUpper) {
			mc = 4;
		    } else if (moist > SimpleBiomeMapper.dryValueUpper) {
			mc = 3;
		    } else if (moist > SimpleBiomeMapper.drierValueUpper) {
			mc = 2;
		    } else if (moist > SimpleBiomeMapper.driestValueUpper) {
			mc = 1;
		    } else {
			mc = 0;
		    }
		    if (hot > SimpleBiomeMapper.warmerValueUpper) {
			hc = 5;
		    } else if (hot > SimpleBiomeMapper.warmValueUpper) {
			hc = 4;
		    } else if (hot > SimpleBiomeMapper.coldValueUpper) {
			hc = 3;
		    } else if (hot > SimpleBiomeMapper.colderValueUpper) {
			hc = 2;
		    } else if (hot > SimpleBiomeMapper.coldestValueUpper) {
			hc = 1;
		    } else {
			hc = 0;
		    }
		    this.heatCodeData[x][y] = hc;
		    this.moistureCodeData[x][y] = mc;
		    this.biomeCodeData[x][y] = isLake ? hc + 48
			    : isRiver ? hc + 42 : heightCode == 4 ? hc + 36 : hc + mc * 6;
		}
	    }
	}
    }

    /**
     * A way to get biome information for the cells on a map when you want an area's
     * biome to be a combination of two main biome types, such as "Grassland" or
     * "TropicalRainforest", with the biomes varying in weight between areas. <br>
     * To use: 1, Construct a DetailedBiomeMapper (constructor takes no arguments).
     * 2, call {@link #makeBiomes(WorldMapGenerator)} with a WorldMapGenerator that
     * has already produced at least one world map. 3, get biome codes from the
     * {@link #biomeCodeData} field, where a code is an int that can be used with
     * the extract methods in this class to get various information from it (these
     * are {@link #extractBiomeA(int)}, {@link #extractBiomeB(int)},
     * {@link #extractPartA(int)}, {@link #extractPartB(int)}, and
     * {@link #extractMixAmount(int)}). You can get predefined names for biomes
     * using the extractBiome methods (these names can be changed in
     * {@link #biomeTable}), or raw indices into some (usually 54-element)
     * collection or array with the extractPart methods. The extractMixAmount()
     * method gets a float that is the amount by which biome B affects biome A; if
     * this is higher than 0.5, then biome B is the "dominant" biome in the area.
     */
    public static class DetailedBiomeMapper {
	/**
	 * The heat codes for the analyzed map, from 0 to 5 inclusive, with 0 coldest
	 * and 5 hottest.
	 */
	public int[][] heatCodeData,
		/**
		 * The moisture codes for the analyzed map, from 0 to 5 inclusive, with 0 driest
		 * and 5 wettest.
		 */
		moistureCodeData,
		/**
		 * The biome codes for the analyzed map, using one int to store the codes for
		 * two biomes and the degree by which the second biome affects the first. These
		 * codes can be used with methods in this class like
		 * {@link #extractBiomeA(int)}, {@link #extractBiomeB(int)}, and
		 * {@link #extractMixAmount(int)} to find the two dominant biomes in an area,
		 * called biome A and biome B, and the mix amount, for finding how much biome B
		 * affects biome A.
		 */
		biomeCodeData;
	public static final double coldestValueLower = 0.0, coldestValueUpper = 0.15, // 0
		colderValueLower = 0.15, colderValueUpper = 0.31, // 1
		coldValueLower = 0.31, coldValueUpper = 0.5, // 2
		warmValueLower = 0.5, warmValueUpper = 0.69, // 3
		warmerValueLower = 0.69, warmerValueUpper = 0.85, // 4
		warmestValueLower = 0.85, warmestValueUpper = 1.0, // 5
		driestValueLower = 0.0, driestValueUpper = 0.27, // 0
		drierValueLower = 0.27, drierValueUpper = 0.4, // 1
		dryValueLower = 0.4, dryValueUpper = 0.6, // 2
		wetValueLower = 0.6, wetValueUpper = 0.8, // 3
		wetterValueLower = 0.8, wetterValueUpper = 0.9, // 4
		wettestValueLower = 0.9, wettestValueUpper = 1.0; // 5
	/**
	 * The default biome table to use with parts of biome codes from
	 * {@link #biomeCodeData}. Biomes are assigned by heat and moisture for the
	 * first 36 of 54 elements (coldest to warmest for each group of 6, with the
	 * first group as the dryest and the last group the wettest), then the next 6
	 * are for coastlines (coldest to warmest), then rivers (coldest to warmest),
	 * then lakes (coldest to warmest). Unlike with {@link SimpleBiomeMapper}, you
	 * cannot use a biome code directly from biomeCodeData as an index into this in
	 * almost any case; you should pass the biome code to one of the extract
	 * methods. {@link #extractBiomeA(int)} or {@link #extractBiomeB(int)} will work
	 * if you want a biome name, or {@link #extractPartA(int)} or
	 * {@link #extractPartB(int)} should be used if you want a non-coded int that
	 * represents one of the biomes' indices into something like this. You can also
	 * get the amount by which biome B is affecting biome A with
	 * {@link #extractMixAmount(int)}.
	 */
	public static final String[] biomeTable = {
		// COLDEST //COLDER //COLD //HOT //HOTTER //HOTTEST
		"Ice", "Ice", "Grassland", "Desert", "Desert", "Desert", // DRYEST
		"Ice", "Tundra", "Grassland", "Grassland", "Desert", "Desert", // DRYER
		"Ice", "Tundra", "Woodland", "Woodland", "Savanna", "Desert", // DRY
		"Ice", "Tundra", "SeasonalForest", "SeasonalForest", "Savanna", "Savanna", // WET
		"Ice", "Tundra", "BorealForest", "TemperateRainforest", "TropicalRainforest", "Savanna", // WETTER
		"Ice", "BorealForest", "BorealForest", "TemperateRainforest", "TropicalRainforest",
		"TropicalRainforest", // WETTEST
		"Rocky", "Rocky", "Beach", "Beach", "Beach", "Beach", // COASTS
		"Ice", "River", "River", "River", "River", "River", // RIVERS
		"Ice", "River", "River", "River", "River", "River", // LAKES
		"Ocean", "Ocean", "Ocean", "Ocean", "Ocean", "Ocean", // OCEAN
	};

	/**
	 * Gets the int stored in part A of the given biome code, which can be used as
	 * an index into other collections. This int should almost always range from 0
	 * to 53 (both inclusive), so collections this is used as an index for should
	 * have a length of at least 54.
	 *
	 * @param biomeCode a biome code that was probably received from
	 *                  {@link #biomeCodeData}
	 * @return an int stored in the biome code's part A; almost always between 0 and
	 *         53, inclusive.
	 */
	public int extractPartA(final int biomeCode) {
	    return biomeCode & 1023;
	}

	/**
	 * Gets a String from {@link #biomeTable} that names the appropriate biome in
	 * part A of the given biome code.
	 *
	 * @param biomeCode a biome code that was probably received from
	 *                  {@link #biomeCodeData}
	 * @return a String that names the biome in part A of biomeCode, or "Ocean" if
	 *         none can be found
	 */
	public String extractBiomeA(int biomeCode) {
	    biomeCode &= 1023;
	    if (biomeCode < 54) {
		return DetailedBiomeMapper.biomeTable[biomeCode];
	    }
	    return "Ocean";
	}

	/**
	 * Gets the int stored in part B of the given biome code, which can be used as
	 * an index into other collections. This int should almost always range from 0
	 * to 53 (both inclusive), so collections this is used as an index for should
	 * have a length of at least 54.
	 *
	 * @param biomeCode a biome code that was probably received from
	 *                  {@link #biomeCodeData}
	 * @return an int stored in the biome code's part B; almost always between 0 and
	 *         53, inclusive.
	 */
	public int extractPartB(final int biomeCode) {
	    return biomeCode >>> 10 & 1023;
	}

	/**
	 * Gets a String from {@link #biomeTable} that names the appropriate biome in
	 * part B of the given biome code.
	 *
	 * @param biomeCode a biome code that was probably received from
	 *                  {@link #biomeCodeData}
	 * @return a String that names the biome in part B of biomeCode, or "Ocean" if
	 *         none can be found
	 */
	public String extractBiomeB(int biomeCode) {
	    biomeCode = biomeCode >>> 10 & 1023;
	    if (biomeCode < 54) {
		return DetailedBiomeMapper.biomeTable[biomeCode];
	    }
	    return "Ocean";
	}

	/**
	 * This gets the portion of a biome code that represents the amount of mixing
	 * between two biomes. Biome codes are normally obtained from the
	 * {@link #biomeCodeData} field, and aren't very usable on their own without
	 * calling methods like this, {@link #extractBiomeA(int)}, and
	 * {@link #extractBiomeB(int)}. This returns a float between 0.0f (inclusive)
	 * and 1.0f (exclusive), with 0.0f meaning biome B has no effect on an area and
	 * biome A is the only one used, 0.5f meaning biome A and biome B have equal
	 * effect, and 0.75f meaning biome B has most of the effect, three-fourths of
	 * the area, and biome A has less, one-fourth of the area.
	 *
	 * @param biomeCode a biome code that was probably received from
	 *                  {@link #biomeCodeData}
	 * @return a float between 0.0f (inclusive) and 1.0f (exclusive) representing
	 *         mixing of biome B into biome A
	 */
	public float extractMixAmount(final int biomeCode) {
	    return (biomeCode >>> 20) * 0x1p-10f;
	}

	/**
	 * Simple constructor; pretty much does nothing. Make sure to call
	 * {@link #makeBiomes(WorldMapGenerator)} before using fields like
	 * {@link #biomeCodeData}.
	 */
	public DetailedBiomeMapper() {
	    this.heatCodeData = null;
	    this.moistureCodeData = null;
	    this.biomeCodeData = null;
	}

	/**
	 * Analyzes the last world produced by the given WorldMapGenerator and uses all
	 * of its generated information to assign biome codes for each cell (along with
	 * heat and moisture codes). After calling this, biome codes can be taken from
	 * {@link #biomeCodeData} and used with methods in this class like
	 * {@link #extractBiomeA(int)}, {@link #extractBiomeB(int)}, and
	 * {@link #extractMixAmount(int)} to find the two dominant biomes in an area,
	 * called biome A and biome B, and the mix amount, for finding how much biome B
	 * affects biome A.
	 *
	 * @param world a WorldMapGenerator that should have generated at least one map;
	 *              it may be at any zoom
	 */
	public void makeBiomes(final WorldMapGenerator world) {
	    if (world == null || world.width <= 0 || world.height <= 0) {
		return;
	    }
	    if (this.heatCodeData == null || this.heatCodeData.length != world.width
		    || this.heatCodeData[0].length != world.height) {
		this.heatCodeData = new int[world.width][world.height];
	    }
	    if (this.moistureCodeData == null || this.moistureCodeData.length != world.width
		    || this.moistureCodeData[0].length != world.height) {
		this.moistureCodeData = new int[world.width][world.height];
	    }
	    if (this.biomeCodeData == null || this.biomeCodeData.length != world.width
		    || this.biomeCodeData[0].length != world.height) {
		this.biomeCodeData = new int[world.width][world.height];
	    }
	    final int[][] heightCodeData = world.heightCodeData;
	    final double[][] heatData = world.heatData, moistureData = world.moistureData,
		    heightData = world.heightData;
	    int hc, mc, heightCode, bc;
	    double hot, moist, high;
	    final double i_hot = 1.0 / world.maxHeat;
	    for (int x = 0; x < world.width; x++) {
		for (int y = 0; y < world.height; y++) {
		    heightCode = heightCodeData[x][y];
		    hot = heatData[x][y];
		    moist = moistureData[x][y];
		    high = heightData[x][y];
		    final boolean isLake = heightCode >= 4 && world.partialLakeData.contains(x, y),
			    isRiver = heightCode >= 4 && world.partialRiverData.contains(x, y);
		    if (moist >= DetailedBiomeMapper.wettestValueUpper
			    - (DetailedBiomeMapper.wetterValueUpper - DetailedBiomeMapper.wetterValueLower) * 0.2) {
			mc = 5;
		    } else if (moist >= DetailedBiomeMapper.wetterValueUpper
			    - (DetailedBiomeMapper.wetValueUpper - DetailedBiomeMapper.wetValueLower) * 0.2) {
			mc = 4;
		    } else if (moist >= DetailedBiomeMapper.wetValueUpper
			    - (DetailedBiomeMapper.dryValueUpper - DetailedBiomeMapper.dryValueLower) * 0.2) {
			mc = 3;
		    } else if (moist >= DetailedBiomeMapper.dryValueUpper
			    - (DetailedBiomeMapper.drierValueUpper - DetailedBiomeMapper.drierValueLower) * 0.2) {
			mc = 2;
		    } else if (moist >= DetailedBiomeMapper.drierValueUpper
			    - DetailedBiomeMapper.driestValueUpper * 0.2) {
			mc = 1;
		    } else {
			mc = 0;
		    }
		    if (hot >= (DetailedBiomeMapper.warmestValueUpper
			    - (DetailedBiomeMapper.warmerValueUpper - DetailedBiomeMapper.warmerValueLower) * 0.2)
			    * i_hot) {
			hc = 5;
		    } else if (hot >= (DetailedBiomeMapper.warmerValueUpper
			    - (DetailedBiomeMapper.warmValueUpper - DetailedBiomeMapper.warmValueLower) * 0.2)
			    * i_hot) {
			hc = 4;
		    } else if (hot >= (DetailedBiomeMapper.warmValueUpper
			    - (DetailedBiomeMapper.coldValueUpper - DetailedBiomeMapper.coldValueLower) * 0.2)
			    * i_hot) {
			hc = 3;
		    } else if (hot >= (DetailedBiomeMapper.coldValueUpper
			    - (DetailedBiomeMapper.colderValueUpper - DetailedBiomeMapper.colderValueLower) * 0.2)
			    * i_hot) {
			hc = 2;
		    } else if (hot >= (DetailedBiomeMapper.colderValueUpper
			    - DetailedBiomeMapper.coldestValueUpper * 0.2) * i_hot) {
			hc = 1;
		    } else {
			hc = 0;
		    }
		    this.heatCodeData[x][y] = hc;
		    this.moistureCodeData[x][y] = mc;
		    bc = heightCode < 4 ? hc + 54 // 54 == 9 * 6, 9 is used for Ocean groups
			    : isLake ? hc + 48 : isRiver ? hc + 42 : heightCode == 4 ? hc + 36 : hc + mc * 6;
		    if (heightCode < 4) {
			mc = 9;
		    } else if (moist >= DetailedBiomeMapper.wetterValueUpper
			    + (DetailedBiomeMapper.wettestValueUpper - DetailedBiomeMapper.wettestValueLower) * 0.2) {
			mc = 5;
		    } else if (moist >= DetailedBiomeMapper.wetValueUpper
			    + (DetailedBiomeMapper.wetterValueUpper - DetailedBiomeMapper.wetterValueLower) * 0.2) {
			mc = 4;
		    } else if (moist >= DetailedBiomeMapper.dryValueUpper
			    + (DetailedBiomeMapper.wetValueUpper - DetailedBiomeMapper.wetValueLower) * 0.2) {
			mc = 3;
		    } else if (moist >= DetailedBiomeMapper.drierValueUpper
			    + (DetailedBiomeMapper.dryValueUpper - DetailedBiomeMapper.dryValueLower) * 0.2) {
			mc = 2;
		    } else if (moist >= DetailedBiomeMapper.driestValueUpper
			    + (DetailedBiomeMapper.drierValueUpper - DetailedBiomeMapper.drierValueLower) * 0.2) {
			mc = 1;
		    } else {
			mc = 0;
		    }
		    if (hot >= (DetailedBiomeMapper.warmerValueUpper
			    + (DetailedBiomeMapper.warmestValueUpper - DetailedBiomeMapper.warmestValueLower) * 0.2)
			    * i_hot) {
			hc = 5;
		    } else if (hot >= (DetailedBiomeMapper.warmValueUpper
			    + (DetailedBiomeMapper.warmerValueUpper - DetailedBiomeMapper.warmerValueLower) * 0.2)
			    * i_hot) {
			hc = 4;
		    } else if (hot >= (DetailedBiomeMapper.coldValueUpper
			    + (DetailedBiomeMapper.warmValueUpper - DetailedBiomeMapper.warmValueLower) * 0.2)
			    * i_hot) {
			hc = 3;
		    } else if (hot >= (DetailedBiomeMapper.colderValueUpper
			    + (DetailedBiomeMapper.coldValueUpper - DetailedBiomeMapper.coldValueLower) * 0.2)
			    * i_hot) {
			hc = 2;
		    } else if (hot >= (DetailedBiomeMapper.coldestValueUpper
			    + (DetailedBiomeMapper.colderValueUpper - DetailedBiomeMapper.colderValueLower) * 0.2)
			    * i_hot) {
			hc = 1;
		    } else {
			hc = 0;
		    }
		    bc |= hc + mc * 6 << 10;
		    if (heightCode < 4) {
			this.biomeCodeData[x][y] = bc | (int) ((heightData[x][y] + 1.0) / 1.1) << 20;
		    } else if (isRiver || isLake) {
			this.biomeCodeData[x][y] = bc | (int) (moist * 358.4 + 665.0) << 20;
		    } else {
			this.biomeCodeData[x][y] = bc | (int) (heightCode == 4 ? (0.18 - high) * 12800.0
				: NumberTools.bounce((high + moist) * (4.1 + high - hot)) * 512 + 512) << 20;
		    }
		}
	    }
	}
    }

    /**
     * A concrete implementation of {@link WorldMapGenerator} that tiles both
     * east-to-west and north-to-south. It tends to not appear distorted like
     * {@link SphereMap} does in some areas, even though this is inaccurate for a
     * rectangular projection of a spherical world (that inaccuracy is likely what
     * players expect in a map, though).
     * <a href="http://squidpony.github.io/SquidLib/DetailedWorldMapRiverDemo.png"
     * >Example map</a>.
     */
    public static class TilingMap extends WorldMapGenerator {
	// protected static final double terrainFreq = 1.5, terrainRidgedFreq = 1.3,
	// heatFreq = 2.8, moistureFreq = 2.9, otherFreq = 4.5;
	protected static final double terrainFreq = 1.175, terrainRidgedFreq = 1.3, heatFreq = 2.8, moistureFreq = 2.9,
		otherFreq = 4.5;
	private double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
		minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
		minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;
	public final Noise4D terrain, terrainRidged, heat, moisture, otherRidged;

	/**
	 * Constructs a concrete WorldMapGenerator for a map that can be used as a
	 * tiling, wrapping east-to-west as well as north-to-south. Always makes a
	 * 256x256 map. Uses SeededNoise as its noise generator, with 1.0 as the octave
	 * multiplier affecting detail. If you were using
	 * {@link WorldMapGenerator.TilingMap#TilingMap(long, int, int, Noise.Noise4D, double)},
	 * then this would be the same as passing the parameters
	 * {@code 0x1337BABE1337D00DL, 256, 256, SeededNoise.instance, 1.0}.
	 */
	public TilingMap() {
	    this(0x1337BABE1337D00DL, 256, 256, SeededNoise.instance, 1.0);
	}

	/**
	 * Constructs a concrete WorldMapGenerator for a map that can be used as a
	 * tiling, wrapping east-to-west as well as north-to-south. Takes only the
	 * width/height of the map. The initial seed is set to the same large long every
	 * time, and it's likely that you would set the seed when you call
	 * {@link #generate(long)}. The width and height of the map cannot be changed
	 * after the fact, but you can zoom in. Uses SeededNoise as its noise generator,
	 * with 1.0 as the octave multiplier affecting detail.
	 *
	 * @param mapWidth  the width of the map(s) to generate; cannot be changed later
	 * @param mapHeight the height of the map(s) to generate; cannot be changed
	 *                  later
	 */
	public TilingMap(final int mapWidth, final int mapHeight) {
	    this(0x1337BABE1337D00DL, mapWidth, mapHeight, SeededNoise.instance, 1.0);
	}

	/**
	 * Constructs a concrete WorldMapGenerator for a map that can be used as a
	 * tiling, wrapping east-to-west as well as north-to-south. Takes an initial
	 * seed and the width/height of the map. The {@code initialSeed} parameter may
	 * or may not be used, since you can specify the seed to use when you call
	 * {@link #generate(long)}. The width and height of the map cannot be changed
	 * after the fact, but you can zoom in. Uses SeededNoise as its noise generator,
	 * with 1.0 as the octave multiplier affecting detail.
	 *
	 * @param initialSeed the seed for the StatefulRNG this uses; this may also be
	 *                    set per-call to generate
	 * @param mapWidth    the width of the map(s) to generate; cannot be changed
	 *                    later
	 * @param mapHeight   the height of the map(s) to generate; cannot be changed
	 *                    later
	 */
	public TilingMap(final long initialSeed, final int mapWidth, final int mapHeight) {
	    this(initialSeed, mapWidth, mapHeight, SeededNoise.instance, 1.0);
	}

	/**
	 * Constructs a concrete WorldMapGenerator for a map that can be used as a
	 * tiling, wrapping east-to-west as well as north-to-south. Takes an initial
	 * seed, the width/height of the map, and a noise generator (a {@link Noise4D}
	 * implementation, which is usually {@link SeededNoise#instance}. The
	 * {@code initialSeed} parameter may or may not be used, since you can specify
	 * the seed to use when you call {@link #generate(long)}. The width and height
	 * of the map cannot be changed after the fact, but you can zoom in. Currently
	 * only SeededNoise makes sense to use as the value for {@code noiseGenerator},
	 * and the seed it's constructed with doesn't matter because it will change the
	 * seed several times at different scales of noise (it's fine to use the static
	 * {@link SeededNoise#instance} because it has no changing state between runs of
	 * the program; it's effectively a constant). The detail level, which is the
	 * {@code octaveMultiplier} parameter that can be passed to another constructor,
	 * is always 1.0 with this constructor.
	 *
	 * @param initialSeed    the seed for the StatefulRNG this uses; this may also
	 *                       be set per-call to generate
	 * @param mapWidth       the width of the map(s) to generate; cannot be changed
	 *                       later
	 * @param mapHeight      the height of the map(s) to generate; cannot be changed
	 *                       later
	 * @param noiseGenerator an instance of a noise generator capable of 4D noise,
	 *                       almost always {@link SeededNoise}
	 */
	public TilingMap(final long initialSeed, final int mapWidth, final int mapHeight,
		final Noise4D noiseGenerator) {
	    this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
	}

	/**
	 * Constructs a concrete WorldMapGenerator for a map that can be used as a
	 * tiling, wrapping east-to-west as well as north-to-south. Takes an initial
	 * seed, the width/height of the map, and parameters for noise generation (a
	 * {@link Noise4D} implementation, which is usually
	 * {@link SeededNoise#instance}, and a multiplier on how many octaves of noise
	 * to use, with 1.0 being normal (high) detail and higher multipliers producing
	 * even more detailed noise when zoomed-in). The {@code initialSeed} parameter
	 * may or may not be used, since you can specify the seed to use when you call
	 * {@link #generate(long)}. The width and height of the map cannot be changed
	 * after the fact, but you can zoom in. Currently only SeededNoise makes sense
	 * to use as the value for {@code noiseGenerator}, and the seed it's constructed
	 * with doesn't matter because it will change the seed several times at
	 * different scales of noise (it's fine to use the static
	 * {@link SeededNoise#instance} because it has no changing state between runs of
	 * the program; it's effectively a constant). The {@code octaveMultiplier}
	 * parameter should probably be no lower than 0.5, but can be arbitrarily high
	 * if you're willing to spend much more time on generating detail only
	 * noticeable at very high zoom; normally 1.0 is fine and may even be too high
	 * for maps that don't require zooming.
	 *
	 * @param initialSeed      the seed for the StatefulRNG this uses; this may also
	 *                         be set per-call to generate
	 * @param mapWidth         the width of the map(s) to generate; cannot be
	 *                         changed later
	 * @param mapHeight        the height of the map(s) to generate; cannot be
	 *                         changed later
	 * @param noiseGenerator   an instance of a noise generator capable of 4D noise,
	 *                         almost always {@link SeededNoise}
	 * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the
	 *                         bare-minimum detail and 1.0 normal
	 */
	public TilingMap(final long initialSeed, final int mapWidth, final int mapHeight, final Noise4D noiseGenerator,
		final double octaveMultiplier) {
	    super(initialSeed, mapWidth, mapHeight);
	    this.terrain = new Noise.Layered4D(noiseGenerator, (int) (0.5 + octaveMultiplier * 8),
		    TilingMap.terrainFreq);
	    this.terrainRidged = new Noise.Ridged4D(noiseGenerator, (int) (0.5 + octaveMultiplier * 10),
		    TilingMap.terrainRidgedFreq);
	    this.heat = new Noise.Layered4D(noiseGenerator, (int) (0.5 + octaveMultiplier * 3), TilingMap.heatFreq);
	    this.moisture = new Noise.Layered4D(noiseGenerator, (int) (0.5 + octaveMultiplier * 4),
		    TilingMap.moistureFreq);
	    this.otherRidged = new Noise.Ridged4D(noiseGenerator, (int) (0.5 + octaveMultiplier * 6),
		    TilingMap.otherFreq);
	}

	@Override
	protected void regenerate(final int startX, final int startY, final int usedWidth, final int usedHeight,
		final double waterMod, final double coolMod, final long state) {
	    boolean fresh = false;
	    if (this.cachedState != state || waterMod != this.waterModifier || coolMod != this.coolingModifier) {
		this.minHeight = Double.POSITIVE_INFINITY;
		this.maxHeight = Double.NEGATIVE_INFINITY;
		this.minHeat0 = Double.POSITIVE_INFINITY;
		this.maxHeat0 = Double.NEGATIVE_INFINITY;
		this.minHeat1 = Double.POSITIVE_INFINITY;
		this.maxHeat1 = Double.NEGATIVE_INFINITY;
		this.minHeat = Double.POSITIVE_INFINITY;
		this.maxHeat = Double.NEGATIVE_INFINITY;
		this.minWet0 = Double.POSITIVE_INFINITY;
		this.maxWet0 = Double.NEGATIVE_INFINITY;
		this.minWet = Double.POSITIVE_INFINITY;
		this.maxWet = Double.NEGATIVE_INFINITY;
		this.cachedState = state;
		fresh = true;
	    }
	    this.rng.setState(state);
	    final int seedA = this.rng.nextInt(), seedB = this.rng.nextInt(), seedC = this.rng.nextInt();
	    int t;
	    this.waterModifier = waterMod <= 0 ? this.rng.nextDouble(0.29) + 0.91 : waterMod;
	    this.coolingModifier = coolMod <= 0 ? this.rng.nextDouble(0.45) * (this.rng.nextDouble() - 0.5) + 1.1
		    : coolMod;
	    double p, q, ps, pc, qs, qc, h, temp;
	    final double i_w = 6.283185307179586 / this.width, i_h = 6.283185307179586 / this.height;
	    double xPos = startX, yPos = startY;
	    final double i_uw = usedWidth / (double) this.width, i_uh = usedHeight / (double) this.height;
	    final double[] trigTable = new double[this.width << 1];
	    for (int x = 0; x < this.width; x++, xPos += i_uw) {
		p = xPos * i_w;
		trigTable[x << 1] = Math.sin(p);
		trigTable[x << 1 | 1] = Math.cos(p);
	    }
	    for (int y = 0; y < this.height; y++, yPos += i_uh) {
		q = yPos * i_h;
		qs = Math.sin(q);
		qc = Math.cos(q);
		for (int x = 0, xt = 0; x < this.width; x++) {
		    ps = trigTable[xt++];// Math.sin(p);
		    pc = trigTable[xt++];// Math.cos(p);
		    h = this.terrain.getNoiseWithSeed(
			    pc + this.terrainRidged.getNoiseWithSeed(pc, ps, qc, qs, seedA + seedB), ps, qc, qs, seedA);
		    h *= this.waterModifier;
		    this.heightData[x][y] = h;
		    this.heatData[x][y] = p = this.heat.getNoiseWithSeed(pc, ps,
			    qc + this.otherRidged.getNoiseWithSeed(pc, ps, qc, qs, seedB + seedC), qs, seedB);
		    this.moistureData[x][y] = temp = this.moisture.getNoiseWithSeed(pc, ps, qc,
			    qs + this.otherRidged.getNoiseWithSeed(pc, ps, qc, qs, seedC + seedA), seedC);
		    this.minHeightActual = Math.min(this.minHeightActual, h);
		    this.maxHeightActual = Math.max(this.maxHeightActual, h);
		    if (fresh) {
			this.minHeight = Math.min(this.minHeight, h);
			this.maxHeight = Math.max(this.maxHeight, h);
			this.minHeat0 = Math.min(this.minHeat0, p);
			this.maxHeat0 = Math.max(this.maxHeat0, p);
			this.minWet0 = Math.min(this.minWet0, temp);
			this.maxWet0 = Math.max(this.maxWet0, temp);
		    }
		}
		this.minHeightActual = Math.min(this.minHeightActual, this.minHeight);
		this.maxHeightActual = Math.max(this.maxHeightActual, this.maxHeight);
	    }
	    final double heightDiff = 2.0 / (this.maxHeightActual - this.minHeightActual);
	    double heatDiff = 0.8 / (this.maxHeat0 - this.minHeat0);
	    final double wetDiff = 1.0 / (this.maxWet0 - this.minWet0);
	    double hMod;
	    final double halfHeight = (this.height - 1) * 0.5, i_half = 1.0 / halfHeight;
	    double minHeightActual0 = this.minHeightActual;
	    double maxHeightActual0 = this.maxHeightActual;
	    yPos = startY;
	    ps = Double.POSITIVE_INFINITY;
	    pc = Double.NEGATIVE_INFINITY;
	    for (int y = 0; y < this.height; y++, yPos += i_uh) {
		temp = Math.abs(yPos - halfHeight) * i_half;
		temp *= 2.4 - temp;
		temp = 2.2 - temp;
		for (int x = 0; x < this.width; x++) {
		    this.heightData[x][y] = h = (this.heightData[x][y] - this.minHeightActual) * heightDiff - 1.0;
		    minHeightActual0 = Math.min(minHeightActual0, h);
		    maxHeightActual0 = Math.max(maxHeightActual0, h);
		    this.heightCodeData[x][y] = t = this.codeHeight(h);
		    hMod = 1.0;
		    switch (t) {
		    case 0:
		    case 1:
		    case 2:
		    case 3:
			h = 0.4;
			hMod = 0.2;
			break;
		    case 6:
			h = -0.1 * (h - WorldMapGenerator.forestLower - 0.08);
			break;
		    case 7:
			h *= -0.25;
			break;
		    case 8:
			h *= -0.4;
			break;
		    default:
			h *= 0.05;
		    }
		    this.heatData[x][y] = h = ((this.heatData[x][y] - this.minHeat0) * heatDiff * hMod + h + 0.6)
			    * temp;
		    if (fresh) {
			ps = Math.min(ps, h); // minHeat0
			pc = Math.max(pc, h); // maxHeat0
		    }
		}
	    }
	    if (fresh) {
		this.minHeat1 = ps;
		this.maxHeat1 = pc;
	    }
	    heatDiff = this.coolingModifier / (this.maxHeat1 - this.minHeat1);
	    qs = Double.POSITIVE_INFINITY;
	    qc = Double.NEGATIVE_INFINITY;
	    ps = Double.POSITIVE_INFINITY;
	    pc = Double.NEGATIVE_INFINITY;
	    for (int y = 0; y < this.height; y++) {
		for (int x = 0; x < this.width; x++) {
		    this.heatData[x][y] = h = (this.heatData[x][y] - this.minHeat1) * heatDiff;
		    this.moistureData[x][y] = temp = (this.moistureData[x][y] - this.minWet0) * wetDiff;
		    if (fresh) {
			qs = Math.min(qs, h);
			qc = Math.max(qc, h);
			ps = Math.min(ps, temp);
			pc = Math.max(pc, temp);
		    }
		}
	    }
	    if (fresh) {
		this.minHeat = qs;
		this.maxHeat = qc;
		this.minWet = ps;
		this.maxWet = pc;
	    }
	    this.landData.refill(this.heightCodeData, 4, 999);
	    if (this.generateRivers) {
		if (fresh) {
		    this.addRivers();
		    this.riverData.connect8way().thin().thin();
		    this.lakeData.connect8way().thin();
		    this.partialRiverData.remake(this.riverData);
		    this.partialLakeData.remake(this.lakeData);
		} else {
		    this.partialRiverData.remake(this.riverData);
		    this.partialLakeData.remake(this.lakeData);
		    for (int i = 1; i <= this.zoom; i++) {
			final int stx = this.startCacheX.get(i) - this.startCacheX.get(i - 1) << i - 1,
				sty = this.startCacheY.get(i) - this.startCacheY.get(i - 1) << i - 1;
			if ((i & 3) == 3) {
			    this.partialRiverData.zoom(stx, sty).connect8way();
			    this.partialRiverData
				    .or(this.workingData.remake(this.partialRiverData).fringe().quasiRandomRegion(0.4));
			    this.partialLakeData.zoom(stx, sty).connect8way();
			    this.partialLakeData
				    .or(this.workingData.remake(this.partialLakeData).fringe().quasiRandomRegion(0.55));
			} else {
			    this.partialRiverData.zoom(stx, sty).connect8way().thin();
			    this.partialRiverData
				    .or(this.workingData.remake(this.partialRiverData).fringe().quasiRandomRegion(0.5));
			    this.partialLakeData.zoom(stx, sty).connect8way().thin();
			    this.partialLakeData
				    .or(this.workingData.remake(this.partialLakeData).fringe().quasiRandomRegion(0.7));
			}
		    }
		}
	    }
	}
    }

    /**
     * A concrete implementation of {@link WorldMapGenerator} that distorts the map
     * as it nears the poles, expanding the smaller-diameter latitude lines in
     * extreme north and south regions so they take up the same space as the
     * equator. This is ideal for projecting onto a 3D sphere, which could squash
     * the poles to counteract the stretch this does. You might also want to produce
     * an oval map that more-accurately represents the changes in the diameter of a
     * latitude line on a spherical world; this could be done by using one of the
     * maps this class makes and removing a portion of each non-equator row,
     * arranging the removal so if the map is n units wide at the equator, the
     * height should be n divided by {@link Math#PI}, and progressively more cells
     * are removed from rows as you move away from the equator (down to empty space
     * or 1 cell left at the poles).
     * <a href="http://i.imgur.com/wth01QD.png" >Example map, showing distortion</a>
     */
    public static class SphereMap extends WorldMapGenerator {
	protected static final double terrainFreq = 1.5, terrainRidgedFreq = 1.8, heatFreq = 2.1, moistureFreq = 2.125,
		otherFreq = 3.375;
	private double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
		minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
		minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;
	public final Noise3D terrain, terrainRidged, heat, moisture, otherRidged;
	public final double[][] xPositions, yPositions, zPositions;

	/**
	 * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a
	 * sphere (as with a texture on a 3D model), with seamless east-west wrapping,
	 * no north-south wrapping, and distortion that causes the poles to have
	 * significantly-exaggerated-in-size features while the equator is not
	 * distorted. Always makes a 256x256 map. Uses SeededNoise as its noise
	 * generator, with 1.0 as the octave multiplier affecting detail. If you were
	 * using
	 * {@link WorldMapGenerator.SphereMap#SphereMap(long, int, int, Noise3D, double)},
	 * then this would be the same as passing the parameters
	 * {@code 0x1337BABE1337D00DL, 256, 256, SeededNoise.instance, 1.0}.
	 */
	public SphereMap() {
	    this(0x1337BABE1337D00DL, 256, 256, SeededNoise.instance, 1.0);
	}

	/**
	 * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a
	 * sphere (as with a texture on a 3D model), with seamless east-west wrapping,
	 * no north-south wrapping, and distortion that causes the poles to have
	 * significantly-exaggerated-in-size features while the equator is not
	 * distorted. Takes only the width/height of the map. The initial seed is set to
	 * the same large long every time, and it's likely that you would set the seed
	 * when you call {@link #generate(long)}. The width and height of the map cannot
	 * be changed after the fact, but you can zoom in. Uses SeededNoise as its noise
	 * generator, with 1.0 as the octave multiplier affecting detail.
	 *
	 * @param mapWidth  the width of the map(s) to generate; cannot be changed later
	 * @param mapHeight the height of the map(s) to generate; cannot be changed
	 *                  later
	 */
	public SphereMap(final int mapWidth, final int mapHeight) {
	    this(0x1337BABE1337D00DL, mapWidth, mapHeight, SeededNoise.instance, 1.0);
	}

	/**
	 * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a
	 * sphere (as with a texture on a 3D model), with seamless east-west wrapping,
	 * no north-south wrapping, and distortion that causes the poles to have
	 * significantly-exaggerated-in-size features while the equator is not
	 * distorted. Takes an initial seed and the width/height of the map. The
	 * {@code initialSeed} parameter may or may not be used, since you can specify
	 * the seed to use when you call {@link #generate(long)}. The width and height
	 * of the map cannot be changed after the fact, but you can zoom in. Uses
	 * SeededNoise as its noise generator, with 1.0 as the octave multiplier
	 * affecting detail.
	 *
	 * @param initialSeed the seed for the StatefulRNG this uses; this may also be
	 *                    set per-call to generate
	 * @param mapWidth    the width of the map(s) to generate; cannot be changed
	 *                    later
	 * @param mapHeight   the height of the map(s) to generate; cannot be changed
	 *                    later
	 */
	public SphereMap(final long initialSeed, final int mapWidth, final int mapHeight) {
	    this(initialSeed, mapWidth, mapHeight, SeededNoise.instance, 1.0);
	}

	/**
	 * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a
	 * sphere (as with a texture on a 3D model), with seamless east-west wrapping,
	 * no north-south wrapping, and distortion that causes the poles to have
	 * significantly-exaggerated-in-size features while the equator is not
	 * distorted. Takes an initial seed, the width/height of the map, and a noise
	 * generator (a {@link Noise3D} implementation, which is usually
	 * {@link SeededNoise#instance}. The {@code initialSeed} parameter may or may
	 * not be used, since you can specify the seed to use when you call
	 * {@link #generate(long)}. The width and height of the map cannot be changed
	 * after the fact, but you can zoom in. Currently only SeededNoise makes sense
	 * to use as the value for {@code noiseGenerator}, and the seed it's constructed
	 * with doesn't matter because it will change the seed several times at
	 * different scales of noise (it's fine to use the static
	 * {@link SeededNoise#instance} because it has no changing state between runs of
	 * the program; it's effectively a constant). The detail level, which is the
	 * {@code octaveMultiplier} parameter that can be passed to another constructor,
	 * is always 1.0 with this constructor.
	 *
	 * @param initialSeed    the seed for the StatefulRNG this uses; this may also
	 *                       be set per-call to generate
	 * @param mapWidth       the width of the map(s) to generate; cannot be changed
	 *                       later
	 * @param mapHeight      the height of the map(s) to generate; cannot be changed
	 *                       later
	 * @param noiseGenerator an instance of a noise generator capable of 3D noise,
	 *                       almost always {@link SeededNoise}
	 */
	public SphereMap(final long initialSeed, final int mapWidth, final int mapHeight,
		final Noise3D noiseGenerator) {
	    this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
	}

	/**
	 * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a
	 * sphere (as with a texture on a 3D model), with seamless east-west wrapping,
	 * no north-south wrapping, and distortion that causes the poles to have
	 * significantly-exaggerated-in-size features while the equator is not
	 * distorted. Takes an initial seed, the width/height of the map, and parameters
	 * for noise generation (a {@link Noise3D} implementation, which is usually
	 * {@link SeededNoise#instance}, and a multiplier on how many octaves of noise
	 * to use, with 1.0 being normal (high) detail and higher multipliers producing
	 * even more detailed noise when zoomed-in). The {@code initialSeed} parameter
	 * may or may not be used, since you can specify the seed to use when you call
	 * {@link #generate(long)}. The width and height of the map cannot be changed
	 * after the fact, but you can zoom in. Currently only SeededNoise makes sense
	 * to use as the value for {@code noiseGenerator}, and the seed it's constructed
	 * with doesn't matter because it will change the seed several times at
	 * different scales of noise (it's fine to use the static
	 * {@link SeededNoise#instance} because it has no changing state between runs of
	 * the program; it's effectively a constant). The {@code octaveMultiplier}
	 * parameter should probably be no lower than 0.5, but can be arbitrarily high
	 * if you're willing to spend much more time on generating detail only
	 * noticeable at very high zoom; normally 1.0 is fine and may even be too high
	 * for maps that don't require zooming.
	 *
	 * @param initialSeed      the seed for the StatefulRNG this uses; this may also
	 *                         be set per-call to generate
	 * @param mapWidth         the width of the map(s) to generate; cannot be
	 *                         changed later
	 * @param mapHeight        the height of the map(s) to generate; cannot be
	 *                         changed later
	 * @param noiseGenerator   an instance of a noise generator capable of 3D noise,
	 *                         almost always {@link SeededNoise}
	 * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the
	 *                         bare-minimum detail and 1.0 normal
	 */
	public SphereMap(final long initialSeed, final int mapWidth, final int mapHeight, final Noise3D noiseGenerator,
		final double octaveMultiplier) {
	    super(initialSeed, mapWidth, mapHeight);
	    this.xPositions = new double[this.width][this.height];
	    this.yPositions = new double[this.width][this.height];
	    this.zPositions = new double[this.width][this.height];
	    this.terrain = new Noise.Layered3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 8),
		    SphereMap.terrainFreq);
	    this.terrainRidged = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 10),
		    SphereMap.terrainRidgedFreq);
	    this.heat = new Noise.Layered3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 3), SphereMap.heatFreq);
	    this.moisture = new Noise.Layered3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 4),
		    SphereMap.moistureFreq);
	    this.otherRidged = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 6),
		    SphereMap.otherFreq);
	}

	@Override
	protected int wrapY(final int y) {
	    return Math.max(0, Math.min(y, this.height - 1));
	}

	@Override
	protected void regenerate(final int startX, final int startY, final int usedWidth, final int usedHeight,
		final double waterMod, final double coolMod, final long state) {
	    boolean fresh = false;
	    if (this.cachedState != state || waterMod != this.waterModifier || coolMod != this.coolingModifier) {
		this.minHeight = Double.POSITIVE_INFINITY;
		this.maxHeight = Double.NEGATIVE_INFINITY;
		this.minHeat0 = Double.POSITIVE_INFINITY;
		this.maxHeat0 = Double.NEGATIVE_INFINITY;
		this.minHeat1 = Double.POSITIVE_INFINITY;
		this.maxHeat1 = Double.NEGATIVE_INFINITY;
		this.minHeat = Double.POSITIVE_INFINITY;
		this.maxHeat = Double.NEGATIVE_INFINITY;
		this.minWet0 = Double.POSITIVE_INFINITY;
		this.maxWet0 = Double.NEGATIVE_INFINITY;
		this.minWet = Double.POSITIVE_INFINITY;
		this.maxWet = Double.NEGATIVE_INFINITY;
		this.cachedState = state;
		fresh = true;
	    }
	    this.rng.setState(state);
	    final int seedA = this.rng.nextInt(), seedB = this.rng.nextInt(), seedC = this.rng.nextInt();
	    int t;
	    this.waterModifier = waterMod <= 0 ? this.rng.nextDouble(0.29) + 0.91 : waterMod;
	    this.coolingModifier = coolMod <= 0 ? this.rng.nextDouble(0.45) * (this.rng.nextDouble() - 0.5) + 1.1
		    : coolMod;
	    double p, ps, pc, qs, qc, h, temp;
	    final double i_w = 6.283185307179586 / this.width, i_h = 3.141592653589793 / (this.height + 2.0);
	    double xPos = startX, yPos;
	    final double i_uw = usedWidth / (double) this.width, i_uh = usedHeight / (this.height + 2.0);
	    final double[] trigTable = new double[this.width << 1];
	    for (int x = 0; x < this.width; x++, xPos += i_uw) {
		p = xPos * i_w;
		trigTable[x << 1] = Math.sin(p);
		trigTable[x << 1 | 1] = Math.cos(p);
	    }
	    yPos = startY + i_uh;
	    for (int y = 0; y < this.height; y++, yPos += i_uh) {
		qs = -1.5707963267948966 + yPos * i_h;
		qc = Math.cos(qs);
		qs = Math.sin(qs);
		// qs = Math.sin(qs);
		for (int x = 0, xt = 0; x < this.width; x++) {
		    ps = trigTable[xt++] * qc;// Math.sin(p);
		    pc = trigTable[xt++] * qc;// Math.cos(p);
		    this.xPositions[x][y] = pc;
		    this.yPositions[x][y] = ps;
		    this.zPositions[x][y] = qs;
		    h = this.terrain.getNoiseWithSeed(
			    pc + this.terrainRidged.getNoiseWithSeed(pc, ps, qs, seedA + seedB), ps, qs, seedA);
		    h *= this.waterModifier;
		    this.heightData[x][y] = h;
		    this.heatData[x][y] = p = this.heat.getNoiseWithSeed(pc,
			    ps + this.otherRidged.getNoiseWithSeed(pc, ps, qs, seedB + seedC), qs, seedB);
		    this.moistureData[x][y] = temp = this.moisture.getNoiseWithSeed(pc, ps,
			    qs + this.otherRidged.getNoiseWithSeed(pc, ps, qs, seedC + seedA), seedC);
		    this.minHeightActual = Math.min(this.minHeightActual, h);
		    this.maxHeightActual = Math.max(this.maxHeightActual, h);
		    if (fresh) {
			this.minHeight = Math.min(this.minHeight, h);
			this.maxHeight = Math.max(this.maxHeight, h);
			this.minHeat0 = Math.min(this.minHeat0, p);
			this.maxHeat0 = Math.max(this.maxHeat0, p);
			this.minWet0 = Math.min(this.minWet0, temp);
			this.maxWet0 = Math.max(this.maxWet0, temp);
		    }
		}
		this.minHeightActual = Math.min(this.minHeightActual, this.minHeight);
		this.maxHeightActual = Math.max(this.maxHeightActual, this.maxHeight);
	    }
	    final double heightDiff = 2.0 / (this.maxHeightActual - this.minHeightActual);
	    double heatDiff = 0.8 / (this.maxHeat0 - this.minHeat0);
	    final double wetDiff = 1.0 / (this.maxWet0 - this.minWet0);
	    double hMod;
	    final double halfHeight = (this.height - 1) * 0.5, i_half = 1.0 / halfHeight;
	    double minHeightActual0 = this.minHeightActual;
	    double maxHeightActual0 = this.maxHeightActual;
	    yPos = startY + i_uh;
	    ps = Double.POSITIVE_INFINITY;
	    pc = Double.NEGATIVE_INFINITY;
	    for (int y = 0; y < this.height; y++, yPos += i_uh) {
		temp = Math.abs(yPos - halfHeight) * i_half;
		temp *= 2.4 - temp;
		temp = 2.2 - temp;
		for (int x = 0; x < this.width; x++) {
		    this.heightData[x][y] = h = (this.heightData[x][y] - this.minHeightActual) * heightDiff - 1.0;
		    minHeightActual0 = Math.min(minHeightActual0, h);
		    maxHeightActual0 = Math.max(maxHeightActual0, h);
		    this.heightCodeData[x][y] = t = this.codeHeight(h);
		    hMod = 1.0;
		    switch (t) {
		    case 0:
		    case 1:
		    case 2:
		    case 3:
			h = 0.4;
			hMod = 0.2;
			break;
		    case 6:
			h = -0.1 * (h - WorldMapGenerator.forestLower - 0.08);
			break;
		    case 7:
			h *= -0.25;
			break;
		    case 8:
			h *= -0.4;
			break;
		    default:
			h *= 0.05;
		    }
		    this.heatData[x][y] = h = ((this.heatData[x][y] - this.minHeat0) * heatDiff * hMod + h + 0.6)
			    * temp;
		    if (fresh) {
			ps = Math.min(ps, h); // minHeat0
			pc = Math.max(pc, h); // maxHeat0
		    }
		}
	    }
	    if (fresh) {
		this.minHeat1 = ps;
		this.maxHeat1 = pc;
	    }
	    heatDiff = this.coolingModifier / (this.maxHeat1 - this.minHeat1);
	    qs = Double.POSITIVE_INFINITY;
	    qc = Double.NEGATIVE_INFINITY;
	    ps = Double.POSITIVE_INFINITY;
	    pc = Double.NEGATIVE_INFINITY;
	    for (int y = 0; y < this.height; y++) {
		for (int x = 0; x < this.width; x++) {
		    this.heatData[x][y] = h = (this.heatData[x][y] - this.minHeat1) * heatDiff;
		    this.moistureData[x][y] = temp = (this.moistureData[x][y] - this.minWet0) * wetDiff;
		    if (fresh) {
			qs = Math.min(qs, h);
			qc = Math.max(qc, h);
			ps = Math.min(ps, temp);
			pc = Math.max(pc, temp);
		    }
		}
	    }
	    if (fresh) {
		this.minHeat = qs;
		this.maxHeat = qc;
		this.minWet = ps;
		this.maxWet = pc;
	    }
	    this.landData.refill(this.heightCodeData, 4, 999);
	    if (this.generateRivers) {
		if (fresh) {
		    this.addRivers();
		    this.riverData.connect8way().thin().thin();
		    this.lakeData.connect8way().thin();
		    this.partialRiverData.remake(this.riverData);
		    this.partialLakeData.remake(this.lakeData);
		} else {
		    this.partialRiverData.remake(this.riverData);
		    this.partialLakeData.remake(this.lakeData);
		    for (int i = 1; i <= this.zoom; i++) {
			final int stx = this.startCacheX.get(i) - this.startCacheX.get(i - 1) << i - 1,
				sty = this.startCacheY.get(i) - this.startCacheY.get(i - 1) << i - 1;
			if ((i & 3) == 3) {
			    this.partialRiverData.zoom(stx, sty).connect8way();
			    this.partialRiverData
				    .or(this.workingData.remake(this.partialRiverData).fringe().quasiRandomRegion(0.4));
			    this.partialLakeData.zoom(stx, sty).connect8way();
			    this.partialLakeData
				    .or(this.workingData.remake(this.partialLakeData).fringe().quasiRandomRegion(0.55));
			} else {
			    this.partialRiverData.zoom(stx, sty).connect8way().thin();
			    this.partialRiverData
				    .or(this.workingData.remake(this.partialRiverData).fringe().quasiRandomRegion(0.5));
			    this.partialLakeData.zoom(stx, sty).connect8way().thin();
			    this.partialLakeData
				    .or(this.workingData.remake(this.partialLakeData).fringe().quasiRandomRegion(0.7));
			}
		    }
		}
	    }
	}
    }
}
