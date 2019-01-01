package squidpony.squidgrid.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.Thesaurus;
import squidpony.annotation.Beta;
import squidpony.squidgrid.MultiSpill;
import squidpony.squidgrid.Spill;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.RNG;
import squidpony.squidmath.StatefulRNG;

/**
 * When you have a world map as produced by {@link WorldMapGenerator} or some
 * other source, you may want to fill it with claims by various
 * nations/factions, possibly procedural or possibly hand-made. This can assign
 * contiguous areas of land to various factions, while ignoring some amount of
 * "wild" land if desired, and keeping oceans unclaimed. The factions can be
 * given procedural names in an atlas that is linked to the chars used by the
 * world map. Uses {@link MultiSpill} internally to produce the semi-random
 * nation shapes. Stores an {@link #atlas} that can be used to figure out what a
 * char in a produced 2D char array means for its claiming nation, a
 * {@link #briefAtlas} that will have short, identifiable parts of generated
 * names corresponding to the same chars as in atlas, and an OrderedMap of
 * {@link #spokenLanguages} that contains any randomly generated languages
 * produced for nations. <a href=
 * "https://gist.github.com/tommyettinger/4a16a09bebed8e2fe8473c8ea444a2dd">Example
 * output of a related class</a>.
 */
@Beta
public class PoliticalMapper {
    public int width;
    public int height;
    public StatefulRNG rng;
    public String name;
    public char[][] politicalMap;
    public static final char[] letters = ArrayTools.letterSpan(256);
    /**
     * Maps chars, as found in the returned array from generate(), to Strings that
     * store the full name of nations.
     */
    public final OrderedMap<Character, String> atlas = new OrderedMap<>(32);
    /**
     * Maps chars, as found in the returned array from generate(), to Strings that
     * store the short name of nations.
     */
    public final OrderedMap<Character, String> briefAtlas = new OrderedMap<>(32);
    /**
     * Maps chars, as found in the returned array from generate(), to Strings that
     * store the languages spoken in those nations, which could be user-assigned,
     * unassigned, or randomly-generated.
     */
    public final OrderedMap<Character, List<FakeLanguageGen>> spokenLanguages = new OrderedMap<>(32);

    public PoliticalMapper() {
	this.name = "Permadeath Planet";
	this.rng = new StatefulRNG(CrossHash.Wisp.hash64(this.name));
    }

    /**
     * Constructs a SpillWorldMap using the given world name, and uses the world
     * name as the basis for all future random generation in this object.
     *
     * @param worldName a String name for the world that will be used as a seed for
     *                  all random generation here
     */
    public PoliticalMapper(final String worldName) {
	this.name = worldName;
	this.rng = new StatefulRNG(CrossHash.Wisp.hash64(this.name));
    }

    /**
     * Constructs a SpillWorldMap using the given world name, and uses the world
     * name as the basis for all future random generation in this object.
     *
     * @param random an RNG to generate the name for the world in a random language,
     *               which will also serve as a seed
     */
    public PoliticalMapper(final RNG random) {
	this(FakeLanguageGen.SIMPLISH.word(random, true));
    }

    /**
     * Produces a political map for the land stored in the given WorldMapGenerator,
     * with the given number of factions trying to take land in the world
     * (essentially, nations). The output is a 2D char array where each letter char
     * is tied to a different faction, while '~' is always water, and '%' is always
     * wilderness or unclaimed land. The amount of unclaimed land is determined by
     * the controlledFraction parameter, which will be clamped between 0.0 and 1.0,
     * with higher numbers resulting in more land owned by factions and lower
     * numbers meaning more wilderness. This version generates an atlas with the
     * procedural names of all the factions and a mapping to the chars used in the
     * output; the atlas will be in the {@link #atlas} member of this object. For
     * every Character key in atlas, there will be a String value in atlas that is
     * the name of the nation, and for the same key in {@link #spokenLanguages},
     * there will be a non-empty List of {@link FakeLanguageGen} languages (usually
     * one, sometimes two) that should match any names generated for the nation.
     * Ocean and Wilderness get the default FakeLanguageGen instances "ELF" and
     * "DEMONIC", in case you need languages for those areas for some reason.
     *
     * @param wmg                a WorldMapGenerator that has produced a map; this
     *                           gets the land parts of the map to assign claims to,
     *                           including rivers and lakes as part of nations but
     *                           not oceans
     * @param factionCount       the number of factions to have claiming land,
     *                           cannot be negative or more than 255
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more
     *                           land has a letter, lower has more '%'
     * @return a 2D char array where letters represent the claiming faction, '~' is
     *         water, and '%' is unclaimed
     */
    public char[][] generate(final WorldMapGenerator wmg, final int factionCount, final double controlledFraction) {
	return this.generate(new GreasedRegion(wmg.heightCodeData, 4, 999), factionCount, controlledFraction);
    }

    /**
     * Produces a political map for the land stored in the "on" cells of the given
     * GreasedRegion, with the given number of factions trying to take land in the
     * world (essentially, nations). The output is a 2D char array where each letter
     * char is tied to a different faction, while '~' is always water, and '%' is
     * always wilderness or unclaimed land. The amount of unclaimed land is
     * determined by the controlledFraction parameter, which will be clamped between
     * 0.0 and 1.0, with higher numbers resulting in more land owned by factions and
     * lower numbers meaning more wilderness. This version generates an atlas with
     * the procedural names of all the factions and a mapping to the chars used in
     * the output; the atlas will be in the {@link #atlas} member of this object.
     * For every Character key in atlas, there will be a String value in atlas that
     * is the name of the nation, and for the same key in {@link #spokenLanguages},
     * there will be a non-empty List of {@link FakeLanguageGen} languages (usually
     * one, sometimes two) that should match any names generated for the nation.
     * Ocean and Wilderness get the default FakeLanguageGen instances "ELF" and
     * "DEMONIC", in case you need languages for those areas for some reason.
     *
     * @param land               a GreasedRegion that stores "on" cells for land and
     *                           "off" cells for anything un-claimable, like ocean
     * @param factionCount       the number of factions to have claiming land,
     *                           cannot be negative or more than 255
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more
     *                           land has a letter, lower has more '%'
     * @return a 2D char array where letters represent the claiming faction, '~' is
     *         water, and '%' is unclaimed
     */
    public char[][] generate(final GreasedRegion land, int factionCount, final double controlledFraction) {
	factionCount &= 255;
	this.width = land.width;
	this.height = land.height;
	final MultiSpill spreader = new MultiSpill(new short[this.width][this.height], Spill.Measurement.MANHATTAN,
		this.rng);
	Coord.expandPoolTo(this.width, this.height);
	final GreasedRegion map = land.copy();
	// Coord[] centers = map.randomSeparated(0.1, rng, factionCount);
	final int controlled = (int) (map.size() * Math.max(0.0, Math.min(1.0, controlledFraction)));
	map.randomScatter(this.rng, (this.width + this.height) / 25, factionCount);
	spreader.initialize(land.toChars());
	final OrderedMap<Coord, Double> entries = new OrderedMap<>();
	entries.put(Coord.get(-1, -1), 0.0);
	for (int i = 0; i < factionCount; i++) {
	    entries.put(map.nth(i), this.rng.between(0.5, 1.0));
	}
	spreader.start(entries, controlled, null);
	final short[][] sm = spreader.spillMap;
	this.politicalMap = new char[this.width][this.height];
	for (int x = 0; x < this.width; x++) {
	    for (int y = 0; y < this.height; y++) {
		this.politicalMap[x][y] = sm[x][y] == -1 ? '~'
			: sm[x][y] == 0 ? '%' : PoliticalMapper.letters[sm[x][y] - 1 & 255];
	    }
	}
	this.atlas.clear();
	this.briefAtlas.clear();
	this.spokenLanguages.clear();
	this.atlas.put('~', "Ocean");
	this.briefAtlas.put('~', "Ocean");
	this.spokenLanguages.put('~', Collections.singletonList(FakeLanguageGen.ELF));
	this.atlas.put('%', "Wilderness");
	this.briefAtlas.put('%', "Wilderness");
	this.spokenLanguages.put('%', Collections.singletonList(FakeLanguageGen.DEMONIC));
	if (factionCount > 0) {
	    final Thesaurus th = new Thesaurus(this.rng.nextLong());
	    th.addKnownCategories();
	    for (int i = 0; i < factionCount && i < 256; i++) {
		this.atlas.put(PoliticalMapper.letters[i], th.makeNationName());
		this.briefAtlas.put(PoliticalMapper.letters[i], th.latestGenerated);
		if (th.randomLanguages == null || th.randomLanguages.isEmpty()) {
		    this.spokenLanguages.put(PoliticalMapper.letters[i],
			    Collections.singletonList(FakeLanguageGen.randomLanguage(this.rng)));
		} else {
		    this.spokenLanguages.put(PoliticalMapper.letters[i], new ArrayList<>(th.randomLanguages));
		}
	    }
	}
	return this.politicalMap;
    }

    /**
     * Produces a political map for the land stored in the given WorldMapGenerator,
     * with the given number of factions trying to take land in the world
     * (essentially, nations). The output is a 2D char array where each letter char
     * is tied to a different faction, while '~' is always water, and '%' is always
     * wilderness or unclaimed land. The amount of unclaimed land is determined by
     * the controlledFraction parameter, which will be clamped between 0.0 and 1.0,
     * with higher numbers resulting in more land owned by factions and lower
     * numbers meaning more wilderness. This version uses an existing atlas and does
     * not assign to {@link #spokenLanguages}; it does not alter the existingAtlas
     * parameter but uses it to determine what should be in this class'
     * {@link #atlas} field. The atlas field will always contain '~' as the first
     * key in its ordering (with value "Ocean" if no value was already assigned in
     * existingAtlas to that key), and '%' as the second key (with value
     * "Wilderness" if not already assigned); later entries will be taken from
     * existingAtlas (not duplicating '~' or '%', but using the rest).
     *
     * @param wmg                a WorldMapGenerator that has produced a map; this
     *                           gets the land parts of the map to assign claims to,
     *                           including rivers and lakes as part of nations but
     *                           not oceans
     * @param existingAtlas      a Map (ideally an OrderedMap) of Character keys to
     *                           be used in the 2D array, to String values that are
     *                           the names of nations; should not have size greater
     *                           than 255
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more
     *                           land has a letter, lower has more '%'
     * @return a 2D char array where letters represent the claiming faction, '~' is
     *         water, and '%' is unclaimed
     */
    public char[][] generate(final WorldMapGenerator wmg, final Map<Character, String> existingAtlas,
	    final double controlledFraction) {
	return this.generate(new GreasedRegion(wmg.heightCodeData, 4, 999), existingAtlas, controlledFraction);
    }

    /**
     * Produces a political map for the land stored in the "on" cells of the given
     * GreasedRegion, with the given number of factions trying to take land in the
     * world (essentially, nations). The output is a 2D char array where each letter
     * char is tied to a different faction, while '~' is always water, and '%' is
     * always wilderness or unclaimed land. The amount of unclaimed land is
     * determined by the controlledFraction parameter, which will be clamped between
     * 0.0 and 1.0, with higher numbers resulting in more land owned by factions and
     * lower numbers meaning more wilderness. This version uses an existing atlas
     * and does not assign to {@link #spokenLanguages}; it does not alter the
     * existingAtlas parameter but uses it to determine what should be in this
     * class' {@link #atlas} field. The atlas field will always contain '~' as the
     * first key in its ordering (with value "Ocean" if no value was already
     * assigned in existingAtlas to that key), and '%' as the second key (with value
     * "Wilderness" if not already assigned); later entries will be taken from
     * existingAtlas (not duplicating '~' or '%', but using the rest).
     *
     * @param land               a GreasedRegion that stores "on" cells for land and
     *                           "off" cells for anything un-claimable, like ocean
     * @param existingAtlas      a Map (ideally an OrderedMap) of Character keys to
     *                           be used in the 2D array, to String values that are
     *                           the names of nations; should not have size greater
     *                           than 255
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more
     *                           land has a letter, lower has more '%'
     * @return a 2D char array where letters represent the claiming faction, '~' is
     *         water, and '%' is unclaimed
     */
    public char[][] generate(final GreasedRegion land, final Map<Character, String> existingAtlas,
	    final double controlledFraction) {
	this.atlas.clear();
	this.briefAtlas.clear();
	this.atlas.putAll(existingAtlas);
	if (this.atlas.getAndMoveToFirst('%') == null) {
	    this.atlas.putAndMoveToFirst('%', "Wilderness");
	}
	if (this.atlas.getAndMoveToFirst('~') == null) {
	    this.atlas.putAndMoveToFirst('~', "Ocean");
	}
	final int factionCount = this.atlas.size() - 2;
	this.briefAtlas.putAll(this.atlas);
	this.width = land.width;
	this.height = land.height;
	final MultiSpill spreader = new MultiSpill(new short[this.width][this.height], Spill.Measurement.MANHATTAN,
		this.rng);
	Coord.expandPoolTo(this.width, this.height);
	final GreasedRegion map = land.copy();
	// Coord[] centers = map.randomSeparated(0.1, rng, factionCount);
	final int controlled = (int) (map.size() * Math.max(0.0, Math.min(1.0, controlledFraction)));
	map.randomScatter(this.rng, (this.width + this.height) / 25, factionCount);
	spreader.initialize(land.toChars());
	final OrderedMap<Coord, Double> entries = new OrderedMap<>();
	entries.put(Coord.get(-1, -1), 0.0);
	for (int i = 0; i < factionCount; i++) {
	    entries.put(map.nth(i), this.rng.between(0.5, 1.0));
	}
	spreader.start(entries, controlled, null);
	final short[][] sm = spreader.spillMap;
	this.politicalMap = new char[this.width][this.height];
	for (int x = 0; x < this.width; x++) {
	    for (int y = 0; y < this.height; y++) {
		this.politicalMap[x][y] = sm[x][y] == -1 ? '~' : sm[x][y] == 0 ? '%' : this.atlas.keyAt(sm[x][y] + 1);
	    }
	}
	return this.politicalMap;
    }

    /**
     * Produces a political map for the land stored in the given WorldMapGenerator,
     * with the given number of factions trying to take land in the world
     * (essentially, nations). The output is a 2D char array where each letter char
     * is tied to a different faction, while '~' is always water, and '%' is always
     * wilderness or unclaimed land. The amount of unclaimed land is determined by
     * the controlledFraction parameter, which will be clamped between 0.0 and 1.0,
     * with higher numbers resulting in more land owned by factions and lower
     * numbers meaning more wilderness. This version uses a "recipe for an atlas"
     * instead of a complete atlas; this is an OrderedMap of Character keys to
     * FakeLanguageGen values, where each key represents a faction and each value is
     * the language to use to generate names for that faction. This does assign to
     * {@link #spokenLanguages}, but it doesn't change the actual FakeLanguageGen
     * objects, since they are immutable. It may add some "factions" if not present
     * to represent oceans and unclaimed land. The atlas field will always contain
     * '~' as the first key in its ordering (with value "Ocean" if no value was
     * already assigned in existingAtlas to that key, or a random nation name in the
     * language that was mapped if there is one), and '%' as the second key (with
     * value "Wilderness" if not already assigned, or a similar random nation name
     * if there is one); later entries will be taken from existingAtlas (not
     * duplicating '~' or '%', but using the rest).
     *
     * @param wmg                a WorldMapGenerator that has produced a map; this
     *                           gets the land parts of the map to assign claims to,
     *                           including rivers and lakes as part of nations but
     *                           not oceans
     * @param atlasLanguages     an OrderedMap of Character keys to be used in the
     *                           2D array, to FakeLanguageGen objects that will be
     *                           used to generate names; should not have size
     *                           greater than 255
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more
     *                           land has a letter, lower has more '%'
     * @return a 2D char array where letters represent the claiming faction, '~' is
     *         water, and '%' is unclaimed
     */
    public char[][] generate(final WorldMapGenerator wmg, final OrderedMap<Character, FakeLanguageGen> atlasLanguages,
	    final double controlledFraction) {
	return this.generate(new GreasedRegion(wmg.heightCodeData, 4, 999), atlasLanguages, controlledFraction);
    }

    /**
     * Produces a political map for the land stored in the "on" cells of the given
     * GreasedRegion, with the given number of factions trying to take land in the
     * world (essentially, nations). The output is a 2D char array where each letter
     * char is tied to a different faction, while '~' is always water, and '%' is
     * always wilderness or unclaimed land. The amount of unclaimed land is
     * determined by the controlledFraction parameter, which will be clamped between
     * 0.0 and 1.0, with higher numbers resulting in more land owned by factions and
     * lower numbers meaning more wilderness. This version uses a "recipe for an
     * atlas" instead of a complete atlas; this is an OrderedMap of Character keys
     * to FakeLanguageGen values, where each key represents a faction and each value
     * is the language to use to generate names for that faction. This does assign
     * to {@link #spokenLanguages}, but it doesn't change the actual FakeLanguageGen
     * objects, since they are immutable. It may add some "factions" if not present
     * to represent oceans and unclaimed land. The atlas field will always contain
     * '~' as the first key in its ordering (with value "Ocean" if no value was
     * already assigned in existingAtlas to that key, or a random nation name in the
     * language that was mapped if there is one), and '%' as the second key (with
     * value "Wilderness" if not already assigned, or a similar random nation name
     * if there is one); later entries will be taken from existingAtlas (not
     * duplicating '~' or '%', but using the rest).
     *
     * @param land               a GreasedRegion that stores "on" cells for land and
     *                           "off" cells for anything un-claimable, like ocean
     * @param atlasLanguages     an OrderedMap of Character keys to be used in the
     *                           2D array, to FakeLanguageGen objects that will be
     *                           used to generate names; should not have size
     *                           greater than 255
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more
     *                           land has a letter, lower has more '%'
     * @return a 2D char array where letters represent the claiming faction, '~' is
     *         water, and '%' is unclaimed
     */
    public char[][] generate(final GreasedRegion land, final OrderedMap<Character, FakeLanguageGen> atlasLanguages,
	    final double controlledFraction) {
	this.atlas.clear();
	this.briefAtlas.clear();
	this.spokenLanguages.clear();
	final Thesaurus th = new Thesaurus(this.rng.nextLong());
	th.addKnownCategories();
	FakeLanguageGen flg;
	if ((flg = atlasLanguages.get('~')) == null) {
	    this.atlas.put('~', "Ocean");
	    this.briefAtlas.put('~', "Ocean");
	    this.spokenLanguages.put('~', Collections.singletonList(FakeLanguageGen.ELF));
	} else {
	    this.atlas.put('~', th.makeNationName(flg));
	    this.briefAtlas.put('~', th.latestGenerated);
	    this.spokenLanguages.put('~', Collections.singletonList(flg));
	}
	if ((flg = atlasLanguages.get('%')) == null) {
	    this.atlas.put('%', "Wilderness");
	    this.briefAtlas.put('%', "Wilderness");
	    this.spokenLanguages.put('%', Collections.singletonList(FakeLanguageGen.DEMONIC));
	} else {
	    this.atlas.put('%', th.makeNationName(flg));
	    this.briefAtlas.put('%', th.latestGenerated);
	    this.spokenLanguages.put('%', Collections.singletonList(flg));
	}
	for (int i = 0; i < atlasLanguages.size() && i < 256; i++) {
	    final Character c = atlasLanguages.keyAt(i);
	    flg = atlasLanguages.getAt(i);
	    this.atlas.put(c, th.makeNationName(flg));
	    this.briefAtlas.put(c, th.latestGenerated);
	    this.spokenLanguages.put(c, Collections.singletonList(flg));
	}
	final int factionCount = this.atlas.size() - 2;
	this.width = land.width;
	this.height = land.height;
	final MultiSpill spreader = new MultiSpill(new short[this.width][this.height], Spill.Measurement.MANHATTAN,
		this.rng);
	Coord.expandPoolTo(this.width, this.height);
	final GreasedRegion map = land.copy();
	// Coord[] centers = map.randomSeparated(0.1, rng, factionCount);
	final int controlled = (int) (map.size() * Math.max(0.0, Math.min(1.0, controlledFraction)));
	map.randomScatter(this.rng, (this.width + this.height) / 25, factionCount);
	spreader.initialize(land.toChars());
	final OrderedMap<Coord, Double> entries = new OrderedMap<>();
	entries.put(Coord.get(-1, -1), 0.0);
	for (int i = 0; i < factionCount; i++) {
	    entries.put(map.nth(i), this.rng.between(0.5, 1.0));
	}
	spreader.start(entries, controlled, null);
	final short[][] sm = spreader.spillMap;
	this.politicalMap = new char[this.width][this.height];
	for (int x = 0; x < this.width; x++) {
	    for (int y = 0; y < this.height; y++) {
		this.politicalMap[x][y] = this.atlas.keyAt(sm[x][y] + 1);
	    }
	}
	return this.politicalMap;
    }
}
