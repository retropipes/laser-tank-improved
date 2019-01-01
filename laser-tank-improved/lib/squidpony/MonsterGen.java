package squidpony;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import squidpony.squidmath.CrossHash;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.RNG;
import squidpony.squidmath.StatefulRNG;

/**
 * A class for generating random monster descriptions; can be subclassed to
 * generate stats for a specific game. Use the nested Chimera class for most of
 * the functionality here; MonsterGen is here so you can change the descriptors
 * that monsters can be given (they are in public array fields). You can call
 * randomizeAppearance or randomizePowers on a Chimera to draw from the list of
 * descriptors in MonsterGen, or fuse two Chimera objects with the mix method in
 * the Chimera class. Chimeras can be printed to a usable format with
 * presentVisible or present; the former does not print special powers and is
 * suitable for monsters being encountered, and the latter is more useful for
 * texts in the game world that describe some monster. Created by Tommy Ettinger
 * on 1/31/2016.
 */
public class MonsterGen {
    public static StatefulRNG srng = new StatefulRNG();
    public String[] components = new String[] { "head", "tail", "legs", "claws", "fangs", "eyes", "hooves", "beak",
	    "wings", "pseudopods", "snout", "carapace", "sting", "pincers", "fins", "shell" },
	    adjectives = new String[] { "hairy", "scaly", "feathered", "chitinous", "pulpy", "writhing", "horrid",
		    "fuzzy", "reptilian", "avian", "insectoid", "tentacled", "thorny", "angular", "curvaceous", "lean",
		    "metallic", "stony", "glassy", "gaunt", "obese", "ill-proportioned", "sickly", "asymmetrical",
		    "muscular" },
	    powerAdjectives = new String[] { "fire-breathing", "electrified", "frigid", "toxic", "noxious", "nimble",
		    "brutish", "bloodthirsty", "furious", "reflective", "regenerating", "earth-shaking", "thunderous",
		    "screeching", "all-seeing", "semi-corporeal", "vampiric", "skulking", "terrifying", "undead",
		    "mechanical", "angelic", "plant-like", "fungal", "contagious", "graceful", "malevolent", "gigantic",
		    "wailing" },
	    powerPhrases = new String[] { "can evoke foul magic", "can petrify with its gaze",
		    "hurts your eyes to look at", "can spit venom", "can cast arcane spells",
		    "can call on divine power", "embodies the wilderness", "hates all other species",
		    "constantly drools acid", "whispers maddening secrets in forgotten tongues",
		    "shudders between impossible dimensions", "withers any life around it", "revels in pain" };

    /**
     * A creature that can be mixed with other Chimeras or given additional
     * descriptors, then printed in a usable format for game text.
     */
    public static class Chimera {
	public OrderedMap<String, List<String>> parts;
	public OrderedSet<String> unsaidAdjectives, wholeAdjectives, powerAdjectives, powerPhrases;
	public String name, mainForm, unknown;

	/**
	 * Copies an existing Chimera other into a new Chimera with potentially a
	 * different name.
	 *
	 * @param name  the name to use for the Chimera this constructs
	 * @param other the existing Chimera to copy all fields but name from.
	 */
	public Chimera(final String name, final Chimera other) {
	    this.name = name;
	    this.unknown = other.unknown;
	    if (this.unknown != null) {
		this.mainForm = this.unknown;
	    } else {
		this.mainForm = other.name;
	    }
	    this.parts = new OrderedMap<>(other.parts);
	    final List<String> oldParts = new ArrayList<>(this.parts.remove(this.mainForm));
	    this.parts.put(name, oldParts);
	    this.unsaidAdjectives = new OrderedSet<>(other.unsaidAdjectives);
	    this.wholeAdjectives = new OrderedSet<>(other.wholeAdjectives);
	    this.powerAdjectives = new OrderedSet<>(other.powerAdjectives);
	    this.powerPhrases = new OrderedSet<>(other.powerPhrases);
	}

	/**
	 * Constructs a Chimera given a name (typically all lower-case), null if the
	 * creature is familiar or a String if the creature's basic shape is likely to
	 * be unknown to players, and an array or vararg of String terms containing,
	 * usually, several groups of String elements separated by the literal string
	 * ";" . The first group in terms contains what body parts this creature has and
	 * could potentially grant to another creature if mixed; examples are "head",
	 * "legs", "claws", "wings", and "eyes". In the next group are the "unsaid"
	 * adjectives, which are not listed if unknown is false, but may be contributed
	 * to other creatures if mixed (mixing a horse with a snake may make the horse
	 * scaly, since "scaly" is an unsaid adjective for snakes). Next are adjectives
	 * that apply to the whole creature's appearance, which don't need to replicate
	 * the unsaid adjectives and are often added as a step to randomize a creature;
	 * this part is often empty and simply ends on the separator ";" . Next are the
	 * power adjectives, which are any special abilities a creature might have that
	 * aren't immediately visible, like "furious" or "toxic". Last are the power
	 * phrases, which follow a format like "can cast arcane spells", "embodies the
	 * wilderness", or "constantly drools acid"; it should be able to be put in a
	 * sentence after the word "that", like "a snake that can cast arcane spells".
	 * <br>
	 * The unknown argument determines if descriptions need to include basic
	 * properties like calling a Snake scaly (null in this case) or a Pestilence
	 * Fiend chitinous (no one knows what that creature is, so a String needs to be
	 * given so a player and player character that don't know its name can call it
	 * something, like "demon"). <br>
	 * An example is
	 * {@code Chimera SNAKE = new Chimera("snake", null, "head", "tail", "fangs", "eyes", ";",
	 * "reptilian", "scaly", "lean", "curvaceous", ";", ";", "toxic");}
	 *
	 * @param name    the name to refer to the creature by and its body parts by
	 *                when mixed
	 * @param unknown true if the creature's basic shape is unlikely to be known by
	 *                a player, false for animals and possibly common mythological
	 *                creatures like dragons
	 * @param terms   an array or vararg of String elements, separated by ";" , see
	 *                method documentation for details
	 */
	public Chimera(final String name, final String unknown, final String... terms) {
	    this.name = name;
	    this.unknown = unknown;
	    if (unknown != null) {
		this.mainForm = unknown;
	    } else {
		this.mainForm = name;
	    }
	    this.parts = new OrderedMap<>();
	    this.unsaidAdjectives = new OrderedSet<>();
	    this.wholeAdjectives = new OrderedSet<>();
	    this.powerAdjectives = new OrderedSet<>();
	    this.powerPhrases = new OrderedSet<>();
	    final ArrayList<String> selfParts = new ArrayList<>();
	    int t = 0;
	    for (; t < terms.length; t++) {
		if (terms[t].equals(";")) {
		    t++;
		    break;
		}
		selfParts.add(terms[t]);
	    }
	    this.parts.put(name, selfParts);
	    for (; t < terms.length; t++) {
		if (terms[t].equals(";")) {
		    t++;
		    break;
		}
		this.unsaidAdjectives.add(terms[t]);
	    }
	    for (; t < terms.length; t++) {
		if (terms[t].equals(";")) {
		    t++;
		    break;
		}
		this.wholeAdjectives.add(terms[t]);
	    }
	    this.wholeAdjectives.removeAll(this.unsaidAdjectives);
	    for (; t < terms.length; t++) {
		if (terms[t].equals(";")) {
		    t++;
		    break;
		}
		this.powerAdjectives.add(terms[t]);
	    }
	    for (; t < terms.length; t++) {
		if (terms[t].equals(";")) {
		    break;
		}
		this.powerPhrases.add(terms[t]);
	    }
	}

	/**
	 * Constructs a Chimera given a name (typically all lower-case), null if the
	 * creature is familiar or a String if the creature's basic shape is likely to
	 * be unknown to players, and several String Collection args for the different
	 * aspects of the Chimera. The first Collection contains what body parts this
	 * creature has and could potentially grant to another creature if mixed;
	 * examples are "head", "legs", "claws", "wings", and "eyes". The next
	 * Collection contains "unsaid" adjectives, which are not listed if unknown is
	 * false, but may be contributed to other creatures if mixed (mixing a horse
	 * with a snake may make the horse scaly, since "scaly" is an unsaid adjective
	 * for snakes). Next are adjectives that apply to the "whole" creature's
	 * appearance, which don't need to replicate the unsaid adjectives and are often
	 * added as a step to randomize a creature; this Collection is often empty. Next
	 * are the power adjectives, which are any special abilities a creature might
	 * have that aren't immediately visible, like "furious" or "toxic". Last are the
	 * power phrases, which follow a format like "can cast arcane spells", "embodies
	 * the wilderness", or "constantly drools acid"; it should be able to be put in
	 * a sentence after the word "that", like "a snake that can cast arcane spells".
	 * <br>
	 * The unknown argument determines if descriptions need to include basic
	 * properties like calling a Snake scaly (null in this case) or a Pestilence
	 * Fiend chitinous (no one knows what that creature is, so a String needs to be
	 * given so a player and player character that don't know its name can call it
	 * something, like "demon"). <br>
	 * An example is
	 * {@code Chimera SNAKE = new Chimera("snake", null, "head", "tail", "fangs", "eyes", ";",
	 * "reptilian", "scaly", "lean", "curvaceous", ";", ";", "toxic");}
	 *
	 * @param name     the name to refer to the creature by and its body parts by
	 *                 when mixed
	 * @param unknown  true if the creature's basic shape is unlikely to be known by
	 *                 a player, false for animals and possibly common mythological
	 *                 creatures like dragons
	 * @param parts    the different body part nouns this creature can contribute to
	 *                 a creature when mixed
	 * @param unsaid   appearance adjectives that don't need to be said if the
	 *                 creature is familiar
	 * @param whole    appearance adjectives that apply to the whole creature
	 * @param powerAdj power adjectives like "furious" or "fire-breathing"
	 * @param powerPhr power phrases like "can cast arcane spells"
	 */
	public Chimera(final String name, final String unknown, final Collection<String> parts,
		final Collection<String> unsaid, final Collection<String> whole, final Collection<String> powerAdj,
		final Collection<String> powerPhr) {
	    this.name = name;
	    this.unknown = unknown;
	    if (unknown != null) {
		this.mainForm = unknown;
	    } else {
		this.mainForm = name;
	    }
	    this.parts = new OrderedMap<>();
	    this.unsaidAdjectives = new OrderedSet<>(unsaid);
	    this.wholeAdjectives = new OrderedSet<>(whole);
	    this.powerAdjectives = new OrderedSet<>(powerAdj);
	    this.powerPhrases = new OrderedSet<>(powerPhr);
	    final ArrayList<String> selfParts = new ArrayList<>(parts);
	    this.parts.put(name, selfParts);
	}

	/**
	 * Get a string description of this monster's appearance and powers.
	 *
	 * @param capitalize true if the description should start with a capital letter.
	 * @return a String description including both appearance and powers
	 */
	public String present(final boolean capitalize) {
	    final StringBuilder sb = new StringBuilder(), tmp = new StringBuilder();
	    if (capitalize) {
		sb.append('A');
	    } else {
		sb.append('a');
	    }
	    int i = 0;
	    final OrderedSet<String> allAdjectives = new OrderedSet<>(this.wholeAdjectives);
	    if (this.unknown != null) {
		allAdjectives.addAll(this.unsaidAdjectives);
	    }
	    allAdjectives.addAll(this.powerAdjectives);
	    for (final String adj : allAdjectives) {
		tmp.append(adj);
		if (++i < allAdjectives.size()) {
		    tmp.append(',');
		}
		tmp.append(' ');
	    }
	    tmp.append(this.mainForm);
	    final String ts = tmp.toString();
	    if (ts.matches("^[aeiouAEIOU].*")) {
		sb.append('n');
	    }
	    sb.append(' ');
	    sb.append(ts);
	    if (!(this.powerPhrases.isEmpty() && this.parts.size() == 1)) {
		sb.append(' ');
	    }
	    if (this.parts.size() > 1) {
		sb.append("with the");
		i = 1;
		for (final Map.Entry<String, List<String>> ent : this.parts.entrySet()) {
		    if (ent.getKey().equals(this.name)) {
			continue;
		    }
		    if (ent.getValue().isEmpty()) {
			sb.append(" feel");
		    } else {
			int j = 1;
			for (final String p : ent.getValue()) {
			    sb.append(' ');
			    sb.append(p);
			    if (j++ < ent.getValue().size() && ent.getValue().size() > 2) {
				sb.append(',');
			    }
			    if (j == ent.getValue().size() && ent.getValue().size() >= 2) {
				sb.append(" and");
			    }
			}
		    }
		    sb.append(" of a ");
		    sb.append(ent.getKey());
		    if (i++ < this.parts.size() && this.parts.size() > 3) {
			sb.append(',');
		    }
		    if (i == this.parts.size() && this.parts.size() >= 3) {
			sb.append(" and");
		    }
		    sb.append(' ');
		}
	    }
	    if (!this.powerPhrases.isEmpty()) {
		sb.append("that");
	    }
	    i = 1;
	    for (final String phr : this.powerPhrases) {
		sb.append(' ');
		sb.append(phr);
		if (i++ < this.powerPhrases.size() && this.powerPhrases.size() > 2) {
		    sb.append(',');
		}
		if (i == this.powerPhrases.size() && this.powerPhrases.size() >= 2) {
		    sb.append(" and");
		}
	    }
	    return sb.toString();
	}

	/**
	 * Get a string description of this monster's appearance.
	 *
	 * @param capitalize true if the description should start with a capital letter.
	 * @return a String description including only the monster's appearance
	 */
	public String presentVisible(final boolean capitalize) {
	    final StringBuilder sb = new StringBuilder(), tmp = new StringBuilder();
	    if (capitalize) {
		sb.append('A');
	    } else {
		sb.append('a');
	    }
	    int i = 0;
	    final OrderedSet<String> allAdjectives = new OrderedSet<>(this.wholeAdjectives);
	    if (this.unknown != null) {
		allAdjectives.addAll(this.unsaidAdjectives);
	    }
	    for (final String adj : allAdjectives) {
		tmp.append(adj);
		if (++i < allAdjectives.size()) {
		    tmp.append(',');
		}
		tmp.append(' ');
	    }
	    tmp.append(this.mainForm);
	    final String ts = tmp.toString();
	    if (ts.matches("^[aeiouAEIOU].*")) {
		sb.append('n');
	    }
	    sb.append(' ');
	    sb.append(ts);
	    if (this.parts.size() > 1) {
		sb.append(" with the");
		i = 1;
		for (final Map.Entry<String, List<String>> ent : this.parts.entrySet()) {
		    if (ent.getKey().equals(this.name)) {
			continue;
		    }
		    if (ent.getValue().isEmpty()) {
			sb.append(" feel");
		    } else {
			int j = 1;
			for (final String p : ent.getValue()) {
			    sb.append(' ');
			    sb.append(p);
			    if (j++ < ent.getValue().size() && ent.getValue().size() > 2) {
				sb.append(',');
			    }
			    if (j == ent.getValue().size() && ent.getValue().size() >= 2) {
				sb.append(" and");
			    }
			}
		    }
		    sb.append(" of a ");
		    sb.append(ent.getKey());
		    if (i++ < this.parts.size() && this.parts.size() > 3) {
			sb.append(',');
		    }
		    if (i == this.parts.size() && this.parts.size() >= 3) {
			sb.append(" and");
		    }
		    sb.append(' ');
		}
	    }
	    return sb.toString();
	}

	@Override
	public String toString() {
	    return this.name;
	}

	/**
	 * Fuse two Chimera objects by some fraction of influence, using the given RNG
	 * and possibly renaming the creature. Does not modify the existing Chimera
	 * objects.
	 *
	 * @param rng            the RNG to determine random factors
	 * @param newName        the name to call the produced Chimera
	 * @param other          the Chimera to mix with this one
	 * @param otherInfluence the fraction between 0.0 and 1.0 of descriptors from
	 *                       other to use
	 * @return a new Chimera mixing features from both inputs
	 */
	public Chimera mix(final RNG rng, final String newName, final Chimera other, final double otherInfluence) {
	    final Chimera next = new Chimera(newName, this);
	    final List<String> otherParts = other.parts.get(other.name),
		    p2 = rng.randomPortion(otherParts, (int) Math.round(otherParts.size() * otherInfluence * 0.5));
	    next.parts.put(other.name, p2);
	    String[] unsaid = other.unsaidAdjectives.toArray(new String[other.unsaidAdjectives.size()]),
		    talentAdj = other.powerAdjectives.toArray(new String[other.powerAdjectives.size()]),
		    talentPhr = other.powerPhrases.toArray(new String[other.powerPhrases.size()]);
	    unsaid = MonsterGen.portion(rng, unsaid, (int) Math.round(unsaid.length * otherInfluence));
	    talentAdj = MonsterGen.portion(rng, talentAdj, (int) Math.round(talentAdj.length * otherInfluence));
	    talentPhr = MonsterGen.portion(rng, talentPhr, (int) Math.round(talentPhr.length * otherInfluence));
	    Collections.addAll(next.wholeAdjectives, unsaid);
	    Collections.addAll(next.powerAdjectives, talentAdj);
	    Collections.addAll(next.powerPhrases, talentPhr);
	    return next;
	}

	/**
	 * Fuse two Chimera objects by some fraction of influence, using the default RNG
	 * and possibly renaming the creature. Does not modify the existing Chimera
	 * objects.
	 *
	 * @param newName        the name to call the produced Chimera
	 * @param other          the Chimera to mix with this one
	 * @param otherInfluence the fraction between 0.0 and 1.0 of descriptors from
	 *                       other to use
	 * @return a new Chimera mixing features from both inputs
	 */
	public Chimera mix(final String newName, final Chimera other, final double otherInfluence) {
	    return this.mix(MonsterGen.srng, newName, other, otherInfluence);
	}
    }

    public static final Chimera SNAKE = new Chimera("snake", null, "head", "tail", "fangs", "eyes", ";", "reptilian",
	    "scaly", "lean", "curvaceous", ";", ";", "toxic"),
	    LION = new Chimera("lion", null, "head", "tail", "legs", "claws", "fangs", "eyes", ";", "hairy", "muscular",
		    ";", ";", "furious"),
	    HORSE = new Chimera("horse", null, "head", "tail", "legs", "hooves", "eyes", ";", "fuzzy", "muscular",
		    "lean", ";", ";", "nimble"),
	    HAWK = new Chimera("hawk", null, "head", "tail", "legs", "claws", "beak", "eyes", "wings", ";", "feathered",
		    "avian", "lean", ";", ";", "screeching", "nimble"),
	    SHOGGOTH = new Chimera("shoggoth", "non-Euclidean ooze", "eyes", "fangs", "pseudopods", ";", "pulpy",
		    "horrid", "tentacled", ";", ";", "terrifying", "regenerating", "semi-corporeal", ";",
		    "shudders between impossible dimensions");

    /**
     * Constructs a MonsterGen with a random seed for the default RNG.
     */
    public MonsterGen() {
    }

    /**
     * Constructs a MonsterGen with the given seed for the default RNG.
     */
    public MonsterGen(final long seed) {
	MonsterGen.srng.setState(seed);
    }

    /**
     * Constructs a MonsterGen with the given seed (hashing seed with CrossHash) for
     * the default RNG.
     */
    public MonsterGen(final String seed) {
	MonsterGen.srng.setState(CrossHash.hash(seed));
    }

    /**
     * Randomly add appearance descriptors to a copy of the Chimera creature.
     * Produces a new Chimera, potentially with a different name, and adds the
     * specified count of adjectives (if any are added that the creature already
     * has, they are ignored, and this includes unsaid adjectives if the creature is
     * known).
     *
     * @param rng            the RNG to determine random factors
     * @param creature       the Chimera to add descriptors to
     * @param newName        the name to call the produced Chimera
     * @param adjectiveCount the number of adjectives to add; may add less if some
     *                       overlap
     * @return a new Chimera with additional appearance descriptors
     */
    public Chimera randomizeAppearance(final RNG rng, final Chimera creature, final String newName,
	    final int adjectiveCount) {
	final Chimera next = new Chimera(newName, creature);
	Collections.addAll(next.wholeAdjectives, MonsterGen.portion(rng, this.adjectives, adjectiveCount));
	next.wholeAdjectives.removeAll(next.unsaidAdjectives);
	return next;
    }

    /**
     * Randomly add appearance descriptors to a copy of the Chimera creature.
     * Produces a new Chimera, potentially with a different name, and adds the
     * specified count of adjectives (if any are added that the creature already
     * has, they are ignored, and this includes unsaid adjectives if the creature is
     * known).
     *
     * @param creature       the Chimera to add descriptors to
     * @param newName        the name to call the produced Chimera
     * @param adjectiveCount the number of adjectives to add; may add less if some
     *                       overlap
     * @return a new Chimera with additional appearance descriptors
     */
    public Chimera randomizeAppearance(final Chimera creature, final String newName, final int adjectiveCount) {
	return this.randomizeAppearance(MonsterGen.srng, creature, newName, adjectiveCount);
    }

    /**
     * Randomly add power descriptors to a copy of the Chimera creature. Produces a
     * new Chimera, potentially with a different name, and adds the specified total
     * count of power adjectives and phrases (if any are added that the creature
     * already has, they are ignored).
     *
     * @param rng        the RNG to determine random factors
     * @param creature   the Chimera to add descriptors to
     * @param newName    the name to call the produced Chimera
     * @param powerCount the number of adjectives to add; may add less if some
     *                   overlap
     * @return a new Chimera with additional power descriptors
     */
    public Chimera randomizePowers(final RNG rng, final Chimera creature, final String newName, final int powerCount) {
	final Chimera next = new Chimera(newName, creature);
	final int adjs = rng.nextInt(powerCount + 1), phrs = powerCount - adjs;
	Collections.addAll(next.powerAdjectives, MonsterGen.portion(rng, this.powerAdjectives, adjs));
	Collections.addAll(next.powerPhrases, MonsterGen.portion(rng, this.powerPhrases, phrs));
	return next;
    }

    /**
     * Randomly add power descriptors to a copy of the Chimera creature. Produces a
     * new Chimera, potentially with a different name, and adds the specified total
     * count of power adjectives and phrases (if any are added that the creature
     * already has, they are ignored).
     *
     * @param creature   the Chimera to add descriptors to
     * @param newName    the name to call the produced Chimera
     * @param powerCount the number of adjectives to add; may add less if some
     *                   overlap
     * @return a new Chimera with additional power descriptors
     */
    public Chimera randomizePowers(final Chimera creature, final String newName, final int powerCount) {
	return this.randomizePowers(MonsterGen.srng, creature, newName, powerCount);
    }

    /**
     * Randomly add appearance and power descriptors to a new Chimera creature with
     * random body part adjectives. Produces a new Chimera with the specified name,
     * and adds the specified total count (detail) of appearance adjectives, power
     * adjectives and phrases, and the same count (detail) of body parts.
     *
     * @param rng     the RNG to determine random factors
     * @param newName the name to call the produced Chimera
     * @param detail  the number of adjectives and phrases to add, also the number
     *                of body parts
     * @return a new Chimera with random traits
     */
    public Chimera randomize(final RNG rng, final String newName, final int detail) {
	final ArrayList<String> ps = new ArrayList<>();
	Collections.addAll(ps, MonsterGen.portion(rng, this.components, detail));
	final Chimera next = new Chimera(newName, "thing", ps, new ArrayList<String>(), new ArrayList<String>(),
		new ArrayList<String>(), new ArrayList<String>());
	if (detail > 0) {
	    final int powerCount = rng.nextInt(detail), bodyCount = detail - powerCount;
	    final int adjs = rng.nextInt(powerCount + 1), phrs = powerCount - adjs;
	    Collections.addAll(next.unsaidAdjectives, MonsterGen.portion(rng, this.adjectives, bodyCount));
	    Collections.addAll(next.powerAdjectives, MonsterGen.portion(rng, this.powerAdjectives, adjs));
	    Collections.addAll(next.powerPhrases, MonsterGen.portion(rng, this.powerPhrases, phrs));
	}
	return next;
    }

    /**
     * Randomly add appearance and power descriptors to a new Chimera creature with
     * random body part adjectives. Produces a new Chimera with the specified name,
     * and adds the specified total count (detail) of appearance adjectives, power
     * adjectives and phrases, and the same count (detail) of body parts.
     *
     * @param newName the name to call the produced Chimera
     * @param detail  the number of adjectives and phrases to add, also the number
     *                of body parts
     * @return a new Chimera with random traits
     */
    public Chimera randomize(final String newName, final int detail) {
	return this.randomize(MonsterGen.srng, newName, detail);
    }

    /**
     * Randomly add appearance and power descriptors to a new Chimera creature with
     * random body part adjectives. Produces a new Chimera with a random name using
     * FakeLanguageGen, and adds a total of 5 appearance adjectives, power
     * adjectives and phrases, and 5 body parts.
     *
     * @return a new Chimera with random traits
     */
    public Chimera randomize() {
	return this.randomize(MonsterGen.srng, this.randomName(MonsterGen.srng), 5);
    }

    /**
     * Gets a random name as a String using FakeLanguageGen.
     *
     * @param rng the RNG to use for random factors
     * @return a String meant to be used as a creature name
     */
    public String randomName(final RNG rng) {
	return FakeLanguageGen.FANTASY_NAME.word(rng, false, rng.between(2, 4));
    }

    /**
     * Gets a random name as a String using FakeLanguageGen.
     *
     * @return a String meant to be used as a creature name
     */
    public String randomName() {
	return this.randomName(MonsterGen.srng);
    }

    private static String[] portion(final RNG rng, final String[] source, final int amount) {
	return rng.randomPortion(source, new String[Math.min(source.length, amount)]);
    }
}
