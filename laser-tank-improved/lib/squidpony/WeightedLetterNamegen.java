package squidpony;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import regexodus.Category;
import squidpony.annotation.Beta;
import squidpony.squidmath.ProbabilityTable;
import squidpony.squidmath.RNG;

/**
 * Based on work by Nolithius available at the following two sites
 * https://github.com/Nolithius/weighted-letter-namegen
 * http://code.google.com/p/weighted-letter-namegen/
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class WeightedLetterNamegen {
//<editor-fold defaultstate="collapsed" desc="Viking Style static name list">
    public static final String[] VIKING_STYLE_NAMES = new String[] { "Andor", "Baatar", "Beowulf", "Drogo", "Freya",
	    "Grog", "Gruumsh", "Grunt", "Hodor", "Hrothgar", "Hrun", "Korg", "Lothar", "Odin", "Theodrin", "Thor",
	    "Yngvar", "Xandor" };
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Star Wars Style static name list">
    public static final String[] STAR_WARS_STYLE_NAMES = new String[] { "Lutoif Vap", "Nasoi Seert", "Jitpai", "Sose",
	    "Vainau", "Jairkau", "Tirka Kist", "Boush", "Wofe", "Voxin Voges", "Koux Boiti", "Loim", "Gaungu",
	    "Mut Tep", "Foimo Saispi", "Toneeg Vaiba", "Nix Nast", "Gup Dangisp", "Distark Toonausp", "Tex Brinki",
	    "Kat Tosha", "Tauna Foip", "Frip Cex", "Fexa Lun", "Tafa", "Zeesheerk", "Cremoim Kixoop", "Tago",
	    "Kesha Diplo" };
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="USA male names static name list">
    public static final String[] COMMON_USA_MALE_NAMES = new String[] { "James", "John", "Robert", "Michael", "William",
	    "David", "Richard", "Charles", "Joseph", "Tomas", "Christopher", "Daniel", "Paul", "Mark", "Donald",
	    "George", "Kenneth", "Steven", "Edward", "Brian", "Ronald", "Anthony", "Kevin", "Jason", "Matthew", "Gary",
	    "Timothy", "Jose", "Larry", "Jeffrey", "Frank", "Scott", "Eric", "Stephen", "Andrew", "Raymond", "Gregory",
	    "Joshua", "Jerry", "Dennis", "Walter", "Patrick", "Peter", "Harold", "Douglas", "Henry", "Carl", "Arthur",
	    "Ryan", "Roger" };
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="USA female names static name list">
    public static final String[] COMMON_USA_FEMALE_NAMES = new String[] { "Mary", "Patricia", "Linda", "Barbara",
	    "Elizabeth", "Jennifer", "Maria", "Susan", "Margaret", "Dorothy", "Lisa", "Nancy", "Karen", "Betty",
	    "Helen", "Sandra", "Donna", "Carol", "Ruth", "Sharon", "Michelle", "Laura", "Sarah", "Kimberly", "Deborah",
	    "Jessica", "Shirley", "Cynthia", "Angela", "Melissa", "Brenda", "Amy", "Anna", "Crystal", "Virginia",
	    "Kathleen", "Pamela", "Martha", "Becky", "Amanda", "Stephanie", "Carolyn", "Christine", "Marie", "Janet",
	    "Catherine", "Frances", "Ann", "Joyce", "Diane", "Jane", "Shauna", "Trisha", "Eileen", "Danielle",
	    "Jacquelyn", "Lynn", "Hannah", "Brittany" };
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="USA last names static name list">
    public static final String[] COMMON_USA_LAST_NAMES = new String[] { "Smith", "Johnson", "Williams", "Brown",
	    "Jones", "Miller", "Davis", "Wilson", "Anderson", "Taylor", "Thomas", "Moore", "Martin", "Jackson",
	    "Thompson", "White", "Clark", "Lewis", "Robinson", "Walker", "Willis", "Carter", "King", "Lee", "Grant",
	    "Howard", "Morris", "Bartlett", "Paine", "Wayne", "Lorraine" };
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Lovecraft Mythos style static name list">
    public static final String[] LOVECRAFT_MYTHOS_NAMES = new String[] { "Koth", "Ghlatelilt", "Siarlut", "Nyogongogg",
	    "Nyialan", "Nyithiark", "Lyun", "Kethoshigr", "Shobik", "Tekogr", "Hru-yn", "Lya-ehibos", "Hruna-oma-ult",
	    "Shabo'en", "Shrashangal", "Shukhaniark", "Thaghum", "Shrilang", "Lukhungu'ith", "Nyun", "Nyia-ongin",
	    "Shogia-usun", "Lyu-yl", "Liathiagragr", "Lyathagg", "Hri'osurkut", "Shothegh", "No-orleshigh",
	    "Zvriangekh", "Nyesashiv", "Lyarkio", "Le'akh", "Liashi-en", "Shurkano'um", "Hrakhanoth", "Ghlotsuban",
	    "Cthitughias", "Ftanugh" };
//</editor-fold>
    private static final char[] vowels = new char[] { 'a', 'e', 'i', 'o' };// not using y because it looks strange as a
									   // vowel in names
    private static final int LAST_LETTER_CANDIDATES_MAX = 52;
    private final RNG rng;
    private final String[] names;
    private final int consonantLimit;
    private ArrayList<Integer> sizes;
    private TreeMap<Character, HashMap<Character, ProbabilityTable<Character>>> letters;
    private ArrayList<Character> firstLetterSamples;
    private ArrayList<Character> lastLetterSamples;
    private final DamerauLevenshteinAlgorithm dla = new DamerauLevenshteinAlgorithm(1, 1, 1, 1);

    /**
     * Creates the generator by seeding the provided list of names.
     *
     * @param names an array of Strings that are typical names to be emulated
     */
    public WeightedLetterNamegen(final String[] names) {
	this(names, 2);
    }

    /**
     * Creates the generator by seeding the provided list of names.
     *
     * @param names          an array of Strings that are typical names to be
     *                       emulated
     * @param consonantLimit the maximum allowed consonants in a row
     */
    public WeightedLetterNamegen(final String[] names, final int consonantLimit) {
	this(names, consonantLimit, new RNG());
    }

    /**
     * Creates the generator by seeding the provided list of names.
     *
     * @param names          an array of Strings that are typical names to be
     *                       emulated
     * @param consonantLimit the maximum allowed consonants in a row
     * @param rng            the source of randomness to be used
     */
    public WeightedLetterNamegen(final String[] names, final int consonantLimit, final RNG rng) {
	this.names = names;
	this.consonantLimit = consonantLimit;
	this.rng = rng;
	this.init();
    }

    /**
     * Initialization, statistically measures letter likelihood.
     */
    private void init() {
	this.sizes = new ArrayList<>();
	this.letters = new TreeMap<>();
	this.firstLetterSamples = new ArrayList<>();
	this.lastLetterSamples = new ArrayList<>();
	for (int i = 0; i < this.names.length - 1; i++) {
	    final String name = this.names[i];
	    if (name == null || name.length() < 1) {
		continue;
	    }
	    // (1) Insert size
	    this.sizes.add(name.length());
	    // (2) Grab first letter
	    this.firstLetterSamples.add(name.charAt(0));
	    // (3) Grab last letter
	    this.lastLetterSamples.add(name.charAt(name.length() - 1));
	    // (4) Process all letters
	    for (int n = 0; n < name.length() - 1; n++) {
		char letter = name.charAt(n);
		final char nextLetter = name.charAt(n + 1);
		// Create letter if it doesn't exist
		HashMap<Character, ProbabilityTable<Character>> wl = this.letters.get(letter);
		if (wl == null) {
		    wl = new HashMap<>();
		    this.letters.put(letter, wl);
		}
		ProbabilityTable<Character> wlg = wl.get(letter);
		if (wlg == null) {
		    wlg = new ProbabilityTable<>();
		    wl.put(letter, wlg);
		}
		wlg.add(nextLetter, 1);
		// If letter was uppercase (beginning of name), also add a lowercase entry
		if (Category.Lu.contains(letter)) {
		    letter = Character.toLowerCase(letter);
		    wlg = wl.get(letter);
		    if (wlg == null) {
			wlg = new ProbabilityTable<>();
			wl.put(letter, wlg);
		    }
		    wlg.add(nextLetter, 1);
		}
	    }
	}
    }

    public String[] generate() {
	return this.generate(1);
    }

    public String[] generate(final int amountToGenerate) {
	final ArrayList<String> result = new ArrayList<>();
	int nameCount = 0;
	while (nameCount < amountToGenerate) {
	    String name = "";
	    // Pick size
	    final int size = this.rng.getRandomElement(this.sizes);
	    // Pick first letter
	    final char firstLetter = this.rng.getRandomElement(this.firstLetterSamples);
	    name += firstLetter;
	    for (int i = 1; i < size - 2; i++) {
		name += this.getRandomNextLetter(name.charAt(name.length() - 1));
	    }
	    // Attempt to find a last letter
	    for (int lastLetterFits = 0; lastLetterFits < WeightedLetterNamegen.LAST_LETTER_CANDIDATES_MAX; lastLetterFits++) {
		final char lastLetter = this.rng.getRandomElement(this.lastLetterSamples);
		final char intermediateLetterCandidate = this.getIntermediateLetter(name.charAt(name.length() - 1),
			lastLetter);
		// Only attach last letter if the candidate is valid (if no candidate, the
		// antepenultimate letter always occurs at the end)
		if (Category.L.contains(intermediateLetterCandidate)) {
		    name += intermediateLetterCandidate;
		    name += lastLetter;
		    break;
		}
	    }
	    final String nameString = name;
	    // Check that the word has no triple letter sequences, and that the Levenshtein
	    // distance is kosher
	    if (this.validateGrouping(name) && this.checkLevenshtein(nameString)) {
		result.add(nameString);
		// Only increase the counter if we've successfully added a name
		nameCount++;
	    }
	}
	return result.toArray(new String[0]);
    }

    /**
     * Searches for the best fit letter between the letter before and the letter
     * after (non-random). Used to determine penultimate letters in names.
     *
     * @param letterBefore The letter before the desired letter.
     * @param letterAfter  The letter after the desired letter.
     * @return The best fit letter between the provided letters.
     */
    private char getIntermediateLetter(final char letterBefore, final char letterAfter) {
	if (Category.L.contains(letterBefore) && Category.L.contains(letterAfter)) {
	    // First grab all letters that come after the 'letterBefore'
	    HashMap<Character, ProbabilityTable<Character>> wl = this.letters.get(letterBefore);
	    if (wl == null) {
		return this.getRandomNextLetter(letterBefore);
	    }
	    final Set<Character> letterCandidates = wl.get(letterBefore).items();
	    char bestFitLetter = '\'';
	    int bestFitScore = 0;
	    // Step through candidates, and return best scoring letter
	    for (final char letter : letterCandidates) {
		wl = this.letters.get(letter);
		if (wl == null) {
		    continue;
		}
		final ProbabilityTable<Character> weightedLetterGroup = wl.get(letterBefore);
		if (weightedLetterGroup != null) {
		    final int letterCounter = weightedLetterGroup.weight(letterAfter);
		    if (letterCounter > bestFitScore) {
			bestFitLetter = letter;
			bestFitScore = letterCounter;
		    }
		}
	    }
	    return bestFitLetter;
	} else {
	    return '-';
	}
    }

    /**
     * Checks that no three letters happen in succession.
     *
     * @param name The name array (easier to iterate)
     * @return True if no triple letter sequence is found.
     */
    private boolean validateGrouping(final String name) {
	for (int i = 2; i < name.length(); i++) {
	    if (name.charAt(i) == name.charAt(i - 1) && name.charAt(i) == name.charAt(i - 2)) {
		return false;
	    }
	}
	int consonants = 0;
	for (int i = 0; i < name.length(); i++) {
	    if (this.isVowel(name.charAt(i))) {
		consonants = 0;
	    } else {
		consonants++;
	    }
	    if (consonants > this.consonantLimit) {
		return false;
	    }
	}
	return true;
    }

    private boolean isVowel(final char c) {
	switch (c) {
	case 'a':
	case 'e':
	case 'i':
	case 'o':
	    return true;
	default:
	    return false;
	}
    }

    /**
     * Checks that the Damerau-Levenshtein distance of this name is within a given
     * bias from a name on the master list.
     *
     * @param name The name string.
     * @return True if a name is found that is within the bias.
     */
    private boolean checkLevenshtein(final String name) {
	final int levenshteinBias = name.length() / 2;
	for (final String name1 : this.names) {
	    final int levenshteinDistance = this.dla.execute(name, name1);
	    if (levenshteinDistance <= levenshteinBias) {
		return true;
	    }
	}
	return false;
    }

    private char getRandomNextLetter(final char letter) {
	if (this.letters.containsKey(letter)) {
	    return this.letters.get(letter).get(letter).random();
	} else {
	    return WeightedLetterNamegen.vowels[this.rng.nextIntHasty(4)];
	}
    }
}
