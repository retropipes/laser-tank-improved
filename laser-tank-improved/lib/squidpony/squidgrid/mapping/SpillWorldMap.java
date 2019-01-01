package squidpony.squidgrid.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import squidpony.ArrayTools;
import squidpony.Thesaurus;
import squidpony.annotation.Beta;
import squidpony.squidgrid.MultiSpill;
import squidpony.squidgrid.Spill;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.StatefulRNG;

/**
 * Generates a procedural world map and fills it with the requested number of
 * factions, keeping some land unclaimed. The factions are given procedural
 * names in an atlas that is linked to the chars used by the world map. Uses
 * MultiSpill internally to produce the semi-random land and water shapes, hence
 * the name. <a href=
 * "https://gist.github.com/tommyettinger/4a16a09bebed8e2fe8473c8ea444a2dd">Example
 * output</a>. Created by Tommy Ettinger on 9/12/2016.
 */
@Beta
public class SpillWorldMap {
    public int width;
    public int height;
    public StatefulRNG rng;
    public String name;
    public char[][] politicalMap;
    public int[][] heightMap;
    public Coord[] mountains;
    public static final char[] letters = ArrayTools.letterSpan(256);
    public final OrderedMap<Character, String> atlas = new OrderedMap<>(16);

    public SpillWorldMap() {
	this.width = 20;
	this.height = 20;
	this.name = "Permadeath Island";
	this.rng = new StatefulRNG(CrossHash.Lightning.hash64(this.name));
    }

    /**
     * Constructs a SpillWorldMap using the given width, height, and world name, and
     * uses the world name as the basis for all future random generation in this
     * object.
     *
     * @param width     the width of the map in cells
     * @param height    the height of the map in cells
     * @param worldName a String name for the world that will be used as a seed for
     *                  all random generation here
     */
    public SpillWorldMap(final int width, final int height, final String worldName) {
	this.width = Math.max(width, 20);
	this.height = Math.max(height, 20);
	this.name = worldName;
	this.rng = new StatefulRNG(CrossHash.Lightning.hash64(this.name));
    }

    /**
     * Generates a basic physical map for this world, then overlays a more involved
     * political map with the given number of factions trying to take land in the
     * world (essentially, nations). The output is a 2D char array where each letter
     * char is tied to a different faction, while '~' is always water, and '%' is
     * always wilderness or unclaimed land. A random amount, up to 10% of all land,
     * will be unclaimed wilderness with this method; for more precise control, use
     * the overload that takes a controlledFraction parameter and give it a value
     * between 0.0 and 1.0, inclusive. If makeAtlas is true, this also generates an
     * atlas with the procedural names of all the factions and a mapping to the
     * chars used in the output; the atlas will be in the {@link #atlas} member of
     * this object but will be empty if makeAtlas has never been true in a call to
     * this. <br>
     * If width or height is larger than 256, consider enlarging the Coord pool
     * before calling this with {@code Coord.expandPoolTo(width, height);}. This
     * will have no effect if width and height are both less than or equal to 256,
     * but if you expect to be using maps that are especially large (which makes
     * sense for world maps), expanding the pool will use more memory initially and
     * then (possibly) much less over time, easing pressure on the garbage collector
     * as well, as re-allocations of large Coords that would otherwise be un-cached
     * are avoided.
     *
     * @param factionCount the number of factions to have claiming land, cannot be
     *                     negative or more than 255
     * @param makeAtlas    if true, this will assign random names to factions,
     *                     accessible via {@link #atlas}
     * @return a 2D char array where letters represent the claiming faction, '~' is
     *         water, and '%' is unclaimed
     */
    public char[][] generate(final int factionCount, final boolean makeAtlas) {
	return this.generate(factionCount, makeAtlas, false, this.rng.between(0.9, 1.0), 1.0);
    }

    /**
     * Generates a basic physical map for this world, then overlays a more involved
     * political map with the given number of factions trying to take land in the
     * world (essentially, nations). The output is a 2D char array where each letter
     * char is tied to a different faction, while '~' is always water, and '%' is
     * always wilderness or unclaimed land. The amount of unclaimed land is
     * determined by the controlledFraction parameter, which will be clamped between
     * 0.0 and 1.0, with higher numbers resulting in more land owned by factions and
     * lower numbers meaning more wilderness. If makeAtlas is true, it also
     * generates an atlas with the procedural names of all the factions and a
     * mapping to the chars used in the output; the atlas will be in the
     * {@link #atlas} member of this object but will be empty if makeAtlas has never
     * been true in a call to this. <br>
     * If width or height is larger than 256, consider enlarging the Coord pool
     * before calling this with {@code Coord.expandPoolTo(width, height);}. This
     * will have no effect if width and height are both less than or equal to 256,
     * but if you expect to be using maps that are especially large (which makes
     * sense for world maps), expanding the pool will use more memory initially and
     * then (possibly) much less over time, easing pressure on the garbage collector
     * as well, as re-allocations of large Coords that would otherwise be un-cached
     * are avoided.
     *
     * @param factionCount       the number of factions to have claiming land,
     *                           cannot be negative or more than 255
     * @param makeAtlas          if true, this will assign random names to factions,
     *                           accessible via {@link #atlas}
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more
     *                           land has a letter, lower has more '%'
     * @return a 2D char array where letters represent the claiming faction, '~' is
     *         water, and '%' is unclaimed
     */
    public char[][] generate(final int factionCount, final boolean makeAtlas, final double controlledFraction) {
	return this.generate(factionCount, makeAtlas, false, controlledFraction, 1.0);
    }

    /**
     * Generates a basic physical map for this world, then overlays a more involved
     * political map with the given number of factions trying to take land in the
     * world (essentially, nations). The output is a 2D char array where each letter
     * char is tied to a different faction, while '~' is always water, and '%' is
     * always wilderness or unclaimed land. The amount of unclaimed land is
     * determined by the controlledFraction parameter, which will be clamped between
     * 0.0 and 1.0, with higher numbers resulting in more land owned by factions and
     * lower numbers meaning more wilderness. If makeAtlas is true, it also
     * generates an atlas with the procedural names of all the factions and a
     * mapping to the chars used in the output; the atlas will be in the
     * {@link #atlas} member of this object but will be empty if makeAtlas has never
     * been true in a call to this. <br>
     * If width or height is larger than 256, consider enlarging the Coord pool
     * before calling this with {@code Coord.expandPoolTo(width, height);}. This
     * will have no effect if width and height are both less than or equal to 256,
     * but if you expect to be using maps that are especially large (which makes
     * sense for world maps), expanding the pool will use more memory initially and
     * then (possibly) much less over time, easing pressure on the garbage collector
     * as well, as re-allocations of large Coords that would otherwise be un-cached
     * are avoided.
     *
     * @param factionCount       the number of factions to have claiming land,
     *                           cannot be negative or more than 255
     * @param makeAtlas          if true, this will assign random names to factions,
     *                           accessible via {@link #atlas}
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more
     *                           land has a letter, lower has more '%'
     * @param waterStrength      a non-negative multiplier that affects ocean size;
     *                           1 is more land than water
     * @return a 2D char array where letters represent the claiming faction, '~' is
     *         water, and '%' is unclaimed
     */
    public char[][] generate(final int factionCount, final boolean makeAtlas, final double controlledFraction,
	    final double waterStrength) {
	return this.generate(factionCount, makeAtlas, false, controlledFraction, waterStrength);
    }

    /**
     * Generates a basic physical map for this world, then overlays a more involved
     * political map with the given number of factions trying to take land in the
     * world (essentially, nations). The output is a 2D char array where each letter
     * char is tied to a different faction, while '~' is always water, and '%' is
     * always wilderness or unclaimed land. The amount of unclaimed land is
     * determined by the controlledFraction parameter, which will be clamped between
     * 0.0 and 1.0, with higher numbers resulting in more land owned by factions and
     * lower numbers meaning more wilderness. If makeAtlas is true, it also
     * generates an atlas with the procedural names of all the factions and a
     * mapping to the chars used in the output; the atlas will be in the
     * {@link #atlas} member of this object but will be empty if makeAtlas has never
     * been true in a call to this. If makeHeight is true, this will generate a
     * height map in an organic way (though it isn't especially fast on very large
     * maps), assigning the height map as an int[][] to {@link #heightMap} and the
     * potential mountains, hills, or peaks to the Coord[] {@link #mountains}. The
     * first Coord in mountains is usually the tallest point on the map, though two
     * or more small peaks that are very close to one another might fuse into one
     * larger mountain range, with higher int values than those for the first
     * mountain on its own. <br>
     * If width or height is larger than 256, consider enlarging the Coord pool
     * before calling this with {@code Coord.expandPoolTo(width, height);}. This
     * will have no effect if width and height are both less than or equal to 256,
     * but if you expect to be using maps that are especially large (which makes
     * sense for world maps), expanding the pool will use more memory initially and
     * then (possibly) much less over time, easing pressure on the garbage collector
     * as well, as re-allocations of large Coords that would otherwise be un-cached
     * are avoided.
     *
     * @param factionCount       the number of factions to have claiming land,
     *                           cannot be negative or more than 255
     * @param makeAtlas          if true, this will assign random names to factions,
     *                           accessible via {@link #atlas}
     * @param makeHeight         if true, this will generate a height map,
     *                           accessible via {@link #heightMap}, with -1 for
     *                           water
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more
     *                           land has a letter, lower has more '%'
     * @param waterStrength      a non-negative multiplier that affects ocean size;
     *                           1 is more land than water
     * @return a 2D char array where letters represent the claiming faction, '~' is
     *         water, and '%' is unclaimed
     */
    public char[][] generate(int factionCount, final boolean makeAtlas, final boolean makeHeight,
	    final double controlledFraction, double waterStrength) {
	factionCount &= 255;
	// , extra = 25 + (height * width >>> 4);
	final MultiSpill spreader = new MultiSpill(new short[this.width][this.height], Spill.Measurement.MANHATTAN,
		this.rng);
	final GreasedRegion bounds = new GreasedRegion(this.width, this.height).not().retract(5),
		smallerBounds = bounds.copy().retract(5), area = new GreasedRegion(this.width, this.height),
		tmpEdge = new GreasedRegion(this.width, this.height),
		tmpInner = new GreasedRegion(this.width, this.height),
		tmpOOB = new GreasedRegion(this.width, this.height);
	final Coord[] pts = smallerBounds.randomPortion(this.rng, this.rng.between(24, 48));
	waterStrength = Math.max(0.0, waterStrength);
	final int aLen = pts.length;
	short[][] sm = new short[this.width][this.height], tmpSM;
	Arrays.fill(sm[0], (short) -1);
	for (int i = 1; i < this.width; i++) {
	    System.arraycopy(sm[0], 0, sm[i], 0, this.height);
	}
	OrderedMap<Coord, Double> entries = new OrderedMap<>();
	for (int i = 0; i < aLen; i++) {
	    final int volume = 10 + this.height * this.width / (aLen * 2);
	    area.empty().insert(pts[i]).spill(bounds, volume, this.rng).expand(3);
	    tmpInner.remake(area).retract(4).expand8way().and(area);
	    tmpEdge.remake(area).surface8way();
	    final Coord[] edges = tmpEdge.separatedPortion(0.35);
	    int eLen = edges.length;
	    final Double[] powers = new Double[eLen];
	    Arrays.fill(powers, 0.1 * waterStrength);
	    entries = new OrderedMap<>(edges, powers);
	    eLen = entries.size();
	    for (int j = 0; j < 32; j++) {
		entries.put(tmpInner.singleRandom(this.rng), (this.rng.nextDouble() + 1.5) * 0.4);
	    }
	    tmpOOB.remake(area).not();
	    spreader.start(entries, -1, tmpOOB);
	    tmpSM = spreader.spillMap;
	    for (int x = 0; x < this.width; x++) {
		for (int y = 0; y < this.height; y++) {
		    if (tmpSM[x][y] >= eLen) {
			sm[x][y] = 0;
		    }
		}
	    }
	}
	/*
	 * // old version; produced a strange "swiss cheese" world map layout. SobolQRNG
	 * sobol = new SobolQRNG(2); sobol.skipTo(rng.between(1000, 6500));
	 * //sobol.fillVector(filler); //entries.put(Coord.get((int)(dim * filler[0]),
	 * (int)(dim * filler[1])), (filler[2] + 0.25) / 1.25); for (int j = 0; j < 8;
	 * j++) { entries.put(sobol.nextCoord(width - 16, height - 16).add(8), 0.85); }
	 * for (int j = 8; j < count; j++) { entries.put(sobol.nextCoord(width - 16,
	 * height - 16).add(8), (rng.nextDouble() + 0.25) * 0.3); } count =
	 * entries.size(); double edgePower = 0.95;// count * 0.8 / extra; for (int x =
	 * 1; x < width - 1; x += 4) { entries.put(Coord.get(x, 0), edgePower);
	 * entries.put(Coord.get(x, height - 1), edgePower); } for (int y = 1; y <
	 * height - 1; y += 4) { entries.put(Coord.get(0, y), edgePower);
	 * entries.put(Coord.get(width - 1, y), edgePower); } spreader.start(entries,
	 * -1, null); short[][] sm = spreader.spillMap; for (int x = 0; x < width; x++)
	 * { for (int y = 0; y < height; y++) { //blank[x][y] = (char) ('a' +
	 * Integer.bitCount(sm[x][y] + 7) % 26); if (sm[x][y] >= count || (sm[x][y] & 7)
	 * == 0) sm[x][y] = -1; else sm[x][y] = 0; } }
	 */
	final GreasedRegion map = new GreasedRegion(sm, 0, 0x7fff);
	final Coord[] centers = map.randomSeparated(0.1, this.rng, factionCount);
	final int controlled = (int) (map.size() * Math.max(0.0, Math.min(1.0, controlledFraction)));
	spreader.initialize(sm);
	entries.clear();
	entries.put(Coord.get(-1, -1), 0.0);
	for (int i = 0; i < factionCount; i++) {
	    entries.put(centers[i], this.rng.between(0.5, 1.0));
	}
	spreader.start(entries, controlled, null);
	sm = spreader.spillMap;
	this.politicalMap = new char[this.width][this.height];
	for (int x = 0; x < this.width; x++) {
	    for (int y = 0; y < this.height; y++) {
		this.politicalMap[x][y] = sm[x][y] == -1 ? '~'
			: sm[x][y] == 0 ? '%' : SpillWorldMap.letters[sm[x][y] - 1 & 255];
	    }
	}
	if (makeAtlas) {
	    this.atlas.clear();
	    this.atlas.put('~', "Water");
	    this.atlas.put('%', "Wilderness");
	    if (factionCount > 0) {
		final Thesaurus th = new Thesaurus(this.rng.nextLong());
		th.addKnownCategories();
		for (int i = 0; i < factionCount && i < 256; i++) {
		    this.atlas.put(SpillWorldMap.letters[i], th.makeNationName());
		}
	    }
	}
	if (makeHeight) {
	    final GreasedRegion m2 = new GreasedRegion(map);
	    GreasedRegion g2;
	    map.retract(1);
	    final OrderedSet<Coord> peaks = new OrderedSet<>(
		    this.mountains = map.quasiRandomSeparated(0.4, this.rng.between(100, 150)));
	    final int peakCount = peaks.size();
	    final ArrayList<GreasedRegion> groups = new ArrayList<>(peakCount * 3);
	    for (int i = 0; i < peakCount; i++) {
		groups.add(new GreasedRegion(this.width, this.height).insertSeveral(peaks).spill(map, peaks.size() * 17,
			this.rng));
		peaks.removeLast();
	    }
	    Collections.addAll(peaks, map.randomPortion(this.rng, peakCount));
	    for (int i = 0; i < peakCount; i++) {
		g2 = new GreasedRegion(this.width, this.height).insertSeveral(peaks).expand(4).deteriorate(this.rng, 2)
			.and(m2);
		groups.add(g2);
		if (this.rng.nextBoolean()) {
		    groups.add(g2);
		}
		peaks.clear();
		Collections.addAll(peaks, map.randomPortion(this.rng, peakCount));
	    }
	    this.heightMap = GreasedRegion.sum(groups);
	    m2.not().writeIntsInto(this.heightMap, -1);
	} else {
	    this.heightMap = new int[this.width][this.height];
	}
	return this.politicalMap;
    }

    /**
     * Generates a basic physical map for this world, then overlays a more involved
     * political map with the given number of factions trying to take land in the
     * world (essentially, nations). The output is a 2D char array where each letter
     * char is tied to a different faction, while '~' is always water, and '%' is
     * always wilderness or unclaimed land. Does not generate an atlas, so you
     * should come up with meanings for the letters yourself.
     *
     * @param factionCount the number of factions to have claiming land
     * @return a 2D char array where letters represent the claiming faction, '~' is
     *         water, and '%' is unclaimed
     */
    public char[][] generate(final int factionCount) {
	return this.generate(factionCount, false);
    }
}
