package squidpony;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import squidpony.squidmath.Arrangement;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.IntVLA;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

/**
 * Ways to produce concrete implementations of StringConvert for various data
 * structures. Keeping the StringConvert producers separate from the data
 * structures allows us to convert JDK types as well as to keep the parts that
 * need ObText, and thus RegExodus, separate from the more general-use data
 * structures. Created by Tommy Ettinger on 3/9/2017.
 */
@SuppressWarnings("unchecked")
public class Converters {
    public static void appendQuoted(final StringBuilder sb, final String text) {
	ObText.appendQuoted(sb, text);
    }

    public static ObText.ContentMatcher makeMatcher(final CharSequence text) {
	return ObText.makeMatcherNoComments(text);
    }

    public static <K> StringConvert<OrderedSet<K>> convertOrderedSet(final StringConvert<K> convert) {
	final CharSequence[] types = StringConvert.asArray("OrderedSet", convert.name);
	final StringConvert found = StringConvert.lookup(types);
	if (found != null) {
	    return found; // in this case we've already created a StringConvert for this type combination
	}
	return new StringConvert<>(types) {
	    @Override
	    public String stringify(final OrderedSet<K> item) {
		final StringBuilder sb = new StringBuilder(100);
		K k;
		for (int i = 0; i < item.size();) {
		    k = item.getAt(i);
		    if (item == k) {
			return "";
		    }
		    ObText.appendQuoted(sb, convert.stringify(k));
		    if (++i < item.size()) {
			sb.append(' ');
		    }
		}
		return sb.toString();
	    }

	    @Override
	    public OrderedSet<K> restore(final String text) {
		final ObText.ContentMatcher m = Converters.makeMatcher(text);
		OrderedSet<K> d;
		if (convert.isArray) {
		    d = new OrderedSet<>(CrossHash.generalHasher);
		} else {
		    d = new OrderedSet<>();
		}
		while (m.find()) {
		    if (m.hasMatch()) {
			d.add(convert.restore(m.getMatch()));
		    }
		}
		return d;
	    }
	};
    }

    public static <K> StringConvert<OrderedSet<K>> convertOrderedSet(final CharSequence type) {
	return Converters.convertOrderedSet((StringConvert<K>) StringConvert.get(type));
    }

    public static <K> StringConvert<OrderedSet<K>> convertOrderedSet(final Class<K> type) {
	return Converters.convertOrderedSet((StringConvert<K>) StringConvert.get(type.getSimpleName()));
    }

    public static <K, V> StringConvert<OrderedMap<K, V>> convertOrderedMap(final StringConvert<K> convertK,
	    final StringConvert<V> convertV) {
	final CharSequence[] types = StringConvert.asArray("OrderedMap", convertK.name, convertV.name);
	final StringConvert found = StringConvert.lookup(types);
	if (found != null) {
	    return found; // in this case we've already created a StringConvert for this type combination
	}
	return new StringConvert<>(types) {
	    @Override
	    public String stringify(final OrderedMap<K, V> item) {
		final StringBuilder sb = new StringBuilder(100);
		K k;
		V v;
		for (int i = 0; i < item.size();) {
		    k = item.keyAt(i);
		    if (k == item) {
			return "";
		    }
		    Converters.appendQuoted(sb, convertK.stringify(k));
		    sb.append(' ');
		    v = item.getAt(i);
		    if (v == item) {
			return "";
		    }
		    Converters.appendQuoted(sb, convertV.stringify(v));
		    if (++i < item.size()) {
			sb.append(' ');
		    }
		}
		return sb.toString();
	    }

	    @Override
	    public OrderedMap<K, V> restore(final String text) {
		final ObText.ContentMatcher m = Converters.makeMatcher(text);
		OrderedMap<K, V> d;
		if (convertK.isArray) {
		    d = new OrderedMap<>(CrossHash.generalHasher);
		} else {
		    d = new OrderedMap<>();
		}
		String t;
		while (m.find()) {
		    if (m.hasMatch()) {
			t = m.getMatch();
			if (m.find() && m.hasMatch()) {
			    d.put(convertK.restore(t), convertV.restore(m.getMatch()));
			}
		    }
		}
		return d;
	    }
	};
    }

    public static <K, V> StringConvert<OrderedMap<K, V>> convertOrderedMap(final CharSequence typeK,
	    final CharSequence typeV) {
	return Converters.convertOrderedMap((StringConvert<K>) StringConvert.get(typeK),
		(StringConvert<V>) StringConvert.get(typeV));
    }

    public static <K, V> StringConvert<OrderedMap<K, V>> convertOrderedMap(final Class<K> typeK, final Class<V> typeV) {
	return Converters.convertOrderedMap((StringConvert<K>) StringConvert.get(typeK.getSimpleName()),
		(StringConvert<V>) StringConvert.get(typeV.getSimpleName()));
    }

    public static <K> StringConvert<HashSet<K>> convertHashSet(final StringConvert<K> convert) {
	final CharSequence[] types = StringConvert.asArray("HashSet", convert.name);
	final StringConvert found = StringConvert.lookup(types);
	if (found != null) {
	    return found; // in this case we've already created a StringConvert for this type combination
	}
	return new StringConvert<>(types) {
	    @Override
	    public String stringify(final HashSet<K> item) {
		final StringBuilder sb = new StringBuilder(100);
		final Iterator<K> it = item.iterator();
		K k;
		while (it.hasNext()) {
		    k = it.next();
		    if (item == k) {
			return "";
		    }
		    ObText.appendQuoted(sb, convert.stringify(k));
		    if (it.hasNext()) {
			sb.append(' ');
		    }
		}
		return sb.toString();
	    }

	    @Override
	    public HashSet<K> restore(final String text) {
		final ObText.ContentMatcher m = Converters.makeMatcher(text);
		final HashSet<K> d = new HashSet<>();
		while (m.find()) {
		    if (m.hasMatch()) {
			d.add(convert.restore(m.getMatch()));
		    }
		}
		return d;
	    }
	};
    }

    public static <K> StringConvert<HashSet<K>> convertHashSet(final CharSequence type) {
	return Converters.convertHashSet((StringConvert<K>) StringConvert.get(type));
    }

    public static <K> StringConvert<HashSet<K>> convertHashSet(final Class<K> type) {
	return Converters.convertHashSet((StringConvert<K>) StringConvert.get(type.getSimpleName()));
    }

    public static <K, V> StringConvert<HashMap<K, V>> convertHashMap(final StringConvert<K> convertK,
	    final StringConvert<V> convertV) {
	final CharSequence[] types = StringConvert.asArray("HashMap", convertK.name, convertV.name);
	final StringConvert found = StringConvert.lookup(types);
	if (found != null) {
	    return found; // in this case we've already created a StringConvert for this type combination
	}
	return new StringConvert<>(types) {
	    @Override
	    public String stringify(final HashMap<K, V> item) {
		final StringBuilder sb = new StringBuilder(100);
		K k;
		V v;
		final Iterator<K> kit = item.keySet().iterator();
		final Iterator<V> vit = item.values().iterator();
		while (kit.hasNext()) {
		    k = kit.next();
		    if (k == item) {
			return "";
		    }
		    Converters.appendQuoted(sb, convertK.stringify(k));
		    sb.append(' ');
		    v = vit.next();
		    if (v == item) {
			return "";
		    }
		    Converters.appendQuoted(sb, convertV.stringify(v));
		    if (kit.hasNext()) {
			sb.append(' ');
		    }
		}
		return sb.toString();
	    }

	    @Override
	    public HashMap<K, V> restore(final String text) {
		final ObText.ContentMatcher m = Converters.makeMatcher(text);
		final HashMap<K, V> d = new HashMap<>();
		String t;
		while (m.find()) {
		    if (m.hasMatch()) {
			t = m.getMatch();
			if (m.find() && m.hasMatch()) {
			    d.put(convertK.restore(t), convertV.restore(m.getMatch()));
			}
		    }
		}
		return d;
	    }
	};
    }

    public static <K, V> StringConvert<HashMap<K, V>> convertHashMap(final CharSequence typeK,
	    final CharSequence typeV) {
	return Converters.convertHashMap((StringConvert<K>) StringConvert.get(typeK),
		(StringConvert<V>) StringConvert.get(typeV));
    }

    public static <K, V> StringConvert<HashMap<K, V>> convertHashMap(final Class<K> typeK, final Class<V> typeV) {
	return Converters.convertHashMap((StringConvert<K>) StringConvert.get(typeK.getSimpleName()),
		(StringConvert<V>) StringConvert.get(typeV.getSimpleName()));
    }

    public static <K> StringConvert<Arrangement<K>> convertArrangement(final StringConvert<K> convert) {
	final CharSequence[] types = StringConvert.asArray("Arrangement", convert.name);
	final StringConvert found = StringConvert.lookup(types);
	if (found != null) {
	    return found; // in this case we've already created a StringConvert for this type combination
	}
	return new StringConvert<>(types) {
	    @Override
	    public String stringify(final Arrangement<K> item) {
		final StringBuilder sb = new StringBuilder(100);
		K k;
		for (int i = 0; i < item.size();) {
		    k = item.keyAt(i);
		    if (item == k) {
			return "";
		    }
		    ObText.appendQuoted(sb, convert.stringify(k));
		    if (++i < item.size()) {
			sb.append(' ');
		    }
		}
		return sb.toString();
	    }

	    @Override
	    public Arrangement<K> restore(final String text) {
		final ObText.ContentMatcher m = Converters.makeMatcher(text);
		Arrangement<K> d;
		if (convert.isArray) {
		    d = new Arrangement<>(CrossHash.generalHasher);
		} else {
		    d = new Arrangement<>();
		}
		while (m.find()) {
		    if (m.hasMatch()) {
			d.add(convert.restore(m.getMatch()));
		    }
		}
		return d;
	    }
	};
    }

    public static <K> StringConvert<Arrangement<K>> convertArrangement(final CharSequence type) {
	return Converters.convertArrangement((StringConvert<K>) StringConvert.get(type));
    }

    public static <K> StringConvert<Arrangement<K>> convertArrangement(final Class<K> type) {
	return Converters.convertArrangement((StringConvert<K>) StringConvert.get(type.getSimpleName()));
    }

    public static <K> StringConvert<ArrayList<K>> convertArrayList(final StringConvert<K> convert) {
	final CharSequence[] types = StringConvert.asArray("ArrayList", convert.name);
	final StringConvert found = StringConvert.lookup(types);
	if (found != null) {
	    return found; // in this case we've already created a StringConvert for this type combination
	}
	return new StringConvert<>(types) {
	    @Override
	    public String stringify(final ArrayList<K> item) {
		final StringBuilder sb = new StringBuilder(100);
		K k;
		for (int i = 0; i < item.size();) {
		    k = item.get(i);
		    if (item == k) {
			return "";
		    }
		    Converters.appendQuoted(sb, convert.stringify(k));
		    if (++i < item.size()) {
			sb.append(' ');
		    }
		}
		return sb.toString();
	    }

	    @Override
	    public ArrayList<K> restore(final String text) {
		final ObText.ContentMatcher m = Converters.makeMatcher(text);
		final ArrayList<K> d = new ArrayList<>();
		while (m.find()) {
		    if (m.hasMatch()) {
			d.add(convert.restore(m.getMatch()));
		    }
		}
		return d;
	    }
	};
    }

    public static <K> StringConvert<ArrayList<K>> convertArrayList(final CharSequence type) {
	return Converters.convertArrayList((StringConvert<K>) StringConvert.get(type));
    }

    public static <K> StringConvert<ArrayList<K>> convertArrayList(final Class<K> type) {
	return Converters.convertArrayList((StringConvert<K>) StringConvert.get(type.getSimpleName()));
    }

    public static <K> StringConvert<List<K>> convertList(final StringConvert<K> convert) {
	final CharSequence[] types = StringConvert.asArray("List", convert.name);
	final StringConvert found = StringConvert.lookup(types);
	if (found != null) {
	    return found; // in this case we've already created a StringConvert for this type combination
	}
	return new StringConvert<>(types) {
	    @Override
	    public String stringify(final List<K> item) {
		final StringBuilder sb = new StringBuilder(100);
		K k;
		for (int i = 0; i < item.size();) {
		    k = item.get(i);
		    if (item == k) {
			return "";
		    }
		    Converters.appendQuoted(sb, convert.stringify(k));
		    if (++i < item.size()) {
			sb.append(' ');
		    }
		}
		return sb.toString();
	    }

	    @Override
	    public ArrayList<K> restore(final String text) {
		final ObText.ContentMatcher m = Converters.makeMatcher(text);
		final ArrayList<K> d = new ArrayList<>();
		while (m.find()) {
		    if (m.hasMatch()) {
			d.add(convert.restore(m.getMatch()));
		    }
		}
		return d;
	    }
	};
    }

    public static <K> StringConvert<List<K>> convertList(final CharSequence type) {
	return Converters.convertList((StringConvert<K>) StringConvert.get(type));
    }

    public static <K> StringConvert<List<K>> convertList(final Class<K> type) {
	return Converters.convertList((StringConvert<K>) StringConvert.get(type.getSimpleName()));
    }

    public static final StringConvert<Coord> convertCoord = new StringConvert<>("Coord") {
	@Override
	public String stringify(final Coord item) {
	    if (item == null) {
		return "n";
	    }
	    return item.x + "," + item.y;
	}

	@Override
	public Coord restore(final String text) {
	    if (text == null || text.equals("n")) {
		return null;
	    }
	    return Coord.get(StringKit.intFromDec(text),
		    StringKit.intFromDec(text, text.indexOf(',') + 1, text.length()));
	}
    };
    public static final StringConvert<Coord[]> convertArrayCoord = new StringConvert<>(true, "Coord[]") {
	@Override
	public String stringify(final Coord[] item) {
	    if (item == null) {
		return "N";
	    }
	    final int len = item.length;
	    final StringBuilder sb = new StringBuilder(len * 5);
	    for (int i = 0; i < len;) {
		if (item[i] == null) {
		    sb.append('n');
		} else {
		    sb.append(item[i].x).append(',').append(item[i].y);
		}
		if (++i < len) {
		    sb.append(';');
		}
	    }
	    return sb.toString();
	}

	@Override
	public Coord[] restore(final String text) {
	    if (text == null || text.equals("N")) {
		return null;
	    }
	    final Coord[] coords = new Coord[StringKit.count(text, ';') + 1];
	    int start = -1, end = text.indexOf(',');
	    for (int i = 0; i < coords.length; i++) {
		if (text.charAt(start + 1) == 'n') {
		    coords[i] = null;
		    start = text.indexOf(';', end + 1);
		} else {
		    coords[i] = Coord.get(StringKit.intFromDec(text, start + 1, end),
			    StringKit.intFromDec(text, end + 1, start = text.indexOf(';', end + 1)));
		}
		end = text.indexOf(',', start + 1);
	    }
	    return coords;
	}
    };
    public static final StringConvert<GreasedRegion> convertGreasedRegion = new StringConvert<>("GreasedRegion") {
	@Override
	public String stringify(final GreasedRegion item) {
	    return item.serializeToString();
	}

	@Override
	public GreasedRegion restore(final String text) {
	    return GreasedRegion.deserializeFromString(text);
	}
    };
    public static final StringConvert<IntVLA> convertIntVLA = new StringConvert<>("IntVLA") {
	@Override
	public String stringify(final IntVLA item) {
	    return item.toString(",");
	}

	@Override
	public IntVLA restore(final String text) {
	    return IntVLA.deserializeFromString(text);
	}
    };
    public static final StringConvert<FakeLanguageGen> convertFakeLanguageGen = new StringConvert<>("FakeLanguageGen") {
	@Override
	public String stringify(final FakeLanguageGen item) {
	    return item.serializeToString();
	}

	@Override
	public FakeLanguageGen restore(final String text) {
	    return FakeLanguageGen.deserializeFromString(text);
	}
    };
    public static final StringConvert<FakeLanguageGen.SentenceForm> convertSentenceForm = new StringConvert<>(
	    "FakeLanguageGen$SentenceForm") {
	@Override
	public String stringify(final FakeLanguageGen.SentenceForm item) {
	    return item.serializeToString();
	}

	@Override
	public FakeLanguageGen.SentenceForm restore(final String text) {
	    return FakeLanguageGen.SentenceForm.deserializeFromString(text);
	}
    };
    public static final StringConvert<ObText> convertObText = new StringConvert<>("ObText") {
	@Override
	public String stringify(final ObText item) {
	    return item.serializeToString();
	}

	@Override
	public ObText restore(final String text) {
	    return ObText.deserializeFromString(text);
	}
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////// CORE JDK TYPES, PRIMITIVES, AND PRIMITIVE ARRAYS ARE THE ONLY TYPES
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////// AFTER
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////// THIS
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////// POINT
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Simple implementation to help when passing StringConverts around with data
     * that is already a String.
     */
    public static final StringConvert<String> convertString = new StringConvert<>("String") {
	@Override
	public String stringify(final String item) {
	    return item;
	}

	@Override
	public String restore(final String text) {
	    return text;
	}
    };
    public static final StringConvert<Boolean> convertBoolean = new StringConvert<>("Boolean") {
	@Override
	public String stringify(final Boolean item) {
	    return item == null ? "n" : item ? "1" : "0";
	}

	@Override
	public Boolean restore(final String text) {
	    char c;
	    return text == null || text.isEmpty() || (c = text.charAt(0)) == 'n' ? null : c == '1';
	}
    };
    public static final StringConvert<Byte> convertByte = new StringConvert<>("Byte") {
	@Override
	public String stringify(final Byte item) {
	    return item.toString();
	}

	@Override
	public Byte restore(final String text) {
	    return Byte.decode(text);
	}
    };
    public static final StringConvert<Short> convertShort = new StringConvert<>("Short") {
	@Override
	public String stringify(final Short item) {
	    return item.toString();
	}

	@Override
	public Short restore(final String text) {
	    return Short.decode(text);
	}
    };
    public static final StringConvert<Integer> convertInt = new StringConvert<>("Integer") {
	@Override
	public String stringify(final Integer item) {
	    return item.toString();
	}

	@Override
	public Integer restore(final String text) {
	    return Integer.decode(text);
	}
    };
    public static final StringConvert<Long> convertLong = new StringConvert<>("Long") {
	@Override
	public String stringify(final Long item) {
	    return item.toString();
	}

	@Override
	public Long restore(final String text) {
	    return Long.decode(text);
	}
    };
    public static final StringConvert<Float> convertFloat = new StringConvert<>("Float") {
	@Override
	public String stringify(final Float item) {
	    return item.toString();
	}

	@Override
	public Float restore(final String text) {
	    return Float.parseFloat(text);
	}
    };
    public static final StringConvert<Double> convertDouble = new StringConvert<>("Double") {
	@Override
	public String stringify(final Double item) {
	    return item.toString();
	}

	@Override
	public Double restore(final String text) {
	    return Double.parseDouble(text);
	}
    };
    public static final StringConvert<Character> convertChar = new StringConvert<>("Character") {
	@Override
	public String stringify(final Character item) {
	    return item.toString();
	}

	@Override
	public Character restore(final String text) {
	    return text.charAt(0);
	}
    };
    public static final StringConvert<boolean[]> convertArrayBoolean = new StringConvert<>(true, "boolean[]") {
	@Override
	public String stringify(final boolean[] item) {
	    return StringKit.joinAlt(item);
	}

	@Override
	public boolean[] restore(final String text) {
	    if (text == null || text.equals("N")) {
		return null;
	    }
	    final int amount = text.length();
	    if (amount <= 0) {
		return new boolean[0];
	    }
	    final boolean[] splat = new boolean[amount];
	    for (int i = 0; i < amount; i++) {
		splat[amount] = text.charAt(i) == '1';
	    }
	    return splat;
	}
    };
    public static final StringConvert<byte[]> convertArrayByte = new StringConvert<>(true, "byte[]") {
	@Override
	public String stringify(final byte[] item) {
	    if (item == null) {
		return "N";
	    }
	    return StringKit.join(",", item);
	}

	@Override
	public byte[] restore(final String text) {
	    if (text == null || text.equals("N")) {
		return null;
	    }
	    final int amount = StringKit.count(text, ",");
	    if (amount <= 0) {
		return new byte[] { Byte.decode(text) };
	    }
	    final byte[] splat = new byte[amount + 1];
	    final int dl = 1;
	    int idx = -dl, idx2;
	    for (int i = 0; i < amount; i++) {
		splat[i] = Byte.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
	    }
	    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
		splat[amount] = Byte.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
	    } else {
		splat[amount] = Byte.decode(StringKit.safeSubstring(text, idx + dl, idx2));
	    }
	    return splat;
	}
    };
    public static final StringConvert<short[]> convertArrayShort = new StringConvert<>(true, "short[]") {
	@Override
	public String stringify(final short[] item) {
	    if (item == null) {
		return "N";
	    }
	    return StringKit.join(",", item);
	}

	@Override
	public short[] restore(final String text) {
	    if (text == null || text.equals("N")) {
		return null;
	    }
	    final int amount = StringKit.count(text, ",");
	    if (amount <= 0) {
		return new short[] { Short.decode(text) };
	    }
	    final short[] splat = new short[amount + 1];
	    final int dl = 1;
	    int idx = -dl, idx2;
	    for (int i = 0; i < amount; i++) {
		splat[i] = Short.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
	    }
	    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
		splat[amount] = Short.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
	    } else {
		splat[amount] = Short.decode(StringKit.safeSubstring(text, idx + dl, idx2));
	    }
	    return splat;
	}
    };
    public static final StringConvert<int[]> convertArrayInt = new StringConvert<>(true, "int[]") {
	@Override
	public String stringify(final int[] item) {
	    if (item == null) {
		return "N";
	    }
	    return StringKit.join(",", item);
	}

	@Override
	public int[] restore(final String text) {
	    if (text == null || text.equals("N")) {
		return null;
	    }
	    final int amount = StringKit.count(text, ",");
	    if (amount <= 0) {
		return new int[] { Integer.decode(text) };
	    }
	    final int[] splat = new int[amount + 1];
	    final int dl = 1;
	    int idx = -dl, idx2;
	    for (int i = 0; i < amount; i++) {
		splat[i] = Integer.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
	    }
	    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
		splat[amount] = Integer.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
	    } else {
		splat[amount] = Integer.decode(StringKit.safeSubstring(text, idx + dl, idx2));
	    }
	    return splat;
	}
    };
    public static final StringConvert<long[]> convertArrayLong = new StringConvert<>(true, "long[]") {
	@Override
	public String stringify(final long[] item) {
	    if (item == null) {
		return "N";
	    }
	    return StringKit.join(",", item);
	}

	@Override
	public long[] restore(final String text) {
	    if (text == null || text.equals("N")) {
		return null;
	    }
	    final int amount = StringKit.count(text, ",");
	    if (amount <= 0) {
		return new long[] { Long.decode(text) };
	    }
	    final long[] splat = new long[amount + 1];
	    final int dl = 1;
	    int idx = -dl, idx2;
	    for (int i = 0; i < amount; i++) {
		splat[i] = Long.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
	    }
	    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
		splat[amount] = Long.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
	    } else {
		splat[amount] = Long.decode(StringKit.safeSubstring(text, idx + dl, idx2));
	    }
	    return splat;
	}
    };
    public static final StringConvert<float[]> convertArrayFloat = new StringConvert<>(true, "float[]") {
	@Override
	public String stringify(final float[] item) {
	    if (item == null) {
		return "N";
	    }
	    return StringKit.join(",", item);
	}

	@Override
	public float[] restore(final String text) {
	    if (text == null || text.equals("N")) {
		return null;
	    }
	    final int amount = StringKit.count(text, ",");
	    if (amount <= 0) {
		return new float[] { Float.parseFloat(text) };
	    }
	    final float[] splat = new float[amount + 1];
	    final int dl = 1;
	    int idx = -dl, idx2;
	    for (int i = 0; i < amount; i++) {
		splat[i] = Float.parseFloat(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
	    }
	    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
		splat[amount] = Float.parseFloat(StringKit.safeSubstring(text, idx + dl, text.length()));
	    } else {
		splat[amount] = Float.parseFloat(StringKit.safeSubstring(text, idx + dl, idx2));
	    }
	    return splat;
	}
    };
    public static final StringConvert<double[]> convertArrayDouble = new StringConvert<>(true, "double[]") {
	@Override
	public String stringify(final double[] item) {
	    if (item == null) {
		return "N";
	    }
	    return StringKit.join(",", item);
	}

	@Override
	public double[] restore(final String text) {
	    if (text == null || text.equals("N")) {
		return null;
	    }
	    final int amount = StringKit.count(text, ",");
	    if (amount <= 0) {
		return new double[] { Double.parseDouble(text) };
	    }
	    final double[] splat = new double[amount + 1];
	    final int dl = 1;
	    int idx = -dl, idx2;
	    for (int i = 0; i < amount; i++) {
		splat[i] = Double
			.parseDouble(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
	    }
	    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
		splat[amount] = Double.parseDouble(StringKit.safeSubstring(text, idx + dl, text.length()));
	    } else {
		splat[amount] = Double.parseDouble(StringKit.safeSubstring(text, idx + dl, idx2));
	    }
	    return splat;
	}
    };
    public static final StringConvert<char[]> convertArrayChar = new StringConvert<>(true, "char[]") {
	@Override
	public String stringify(final char[] item) {
	    if (item == null) {
		return "";
	    }
	    return String.valueOf(item);
	}

	@Override
	public char[] restore(final String text) {
	    return text.toCharArray();
	}
    };
    public static final StringConvert<boolean[][]> convertArrayBoolean2D = new StringConvert<>(true, "boolean[][]") {
	@Override
	public String stringify(final boolean[][] item) {
	    if (item == null) {
		return "N";
	    }
	    int len;
	    if ((len = item.length) <= 0) {
		return "";
	    }
	    final StringBuilder sb = new StringBuilder(len * 128);
	    if (item[0] == null) {
		sb.append('n');
	    } else {
		sb.append(StringKit.joinAlt(item[0]));
	    }
	    for (int i = 1; i < len; i++) {
		if (item[i] == null) {
		    sb.append(";n");
		} else {
		    sb.append(';').append(StringKit.joinAlt(item[i]));
		}
	    }
	    return sb.toString();
	}

	@Override
	public boolean[][] restore(final String text) {
	    if (text == null) {
		return null;
	    }
	    int len;
	    if ((len = text.length()) <= 0) {
		return new boolean[0][0];
	    }
	    if (text.charAt(0) == 'N') {
		return null;
	    }
	    final int width = StringKit.count(text, ';') + 1;
	    final boolean[][] val = new boolean[width][];
	    int start = 0, end = text.indexOf(';');
	    for (int i = 0; i < width; i++) {
		if (start == end || start >= len) {
		    val[i] = new boolean[0];
		} else if (text.charAt(start) == 'n') {
		    val[i] = null;
		} else {
		    final int amount = end - start;
		    val[i] = new boolean[amount];
		    for (int j = 0; j < amount; j++) {
			val[i][j] = text.charAt(start + j) == '1';
		    }
		}
		start = end + 1;
		end = text.indexOf(';', start);
	    }
	    return val;
	}
    };
    public static final StringConvert<byte[][]> convertArrayByte2D = new StringConvert<>(true, "byte[][]") {
	@Override
	public String stringify(final byte[][] item) {
	    if (item == null) {
		return "N";
	    }
	    int len;
	    if ((len = item.length) <= 0) {
		return "";
	    }
	    final StringBuilder sb = new StringBuilder(len * 128);
	    if (item[0] == null) {
		sb.append('n');
	    } else {
		sb.append(StringKit.join(",", item[0]));
	    }
	    for (int i = 1; i < len; i++) {
		if (item[i] == null) {
		    sb.append(";n");
		} else {
		    sb.append(';').append(StringKit.join(",", item[i]));
		}
	    }
	    return sb.toString();
	}

	@Override
	public byte[][] restore(final String text) {
	    if (text == null) {
		return null;
	    }
	    int len;
	    if ((len = text.length()) <= 0) {
		return new byte[0][0];
	    }
	    if (text.charAt(0) == 'N') {
		return null;
	    }
	    final int width = StringKit.count(text, ';') + 1;
	    final byte[][] val = new byte[width][];
	    int start = 0, end = text.indexOf(';');
	    for (int i = 0; i < width; i++) {
		if (start == end || start >= len) {
		    val[i] = new byte[0];
		} else if (text.charAt(start) == 'n') {
		    val[i] = null;
		} else {
		    final int amount = StringKit.count(text, ",", start, end);
		    if (amount <= 0) {
			val[i] = new byte[] { Byte.decode(text) };
			continue;
		    }
		    val[i] = new byte[amount + 1];
		    final int dl = 1;
		    int idx = start - dl, idx2;
		    for (int j = 0; j < amount; j++) {
			val[i][j] = Byte
				.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
		    }
		    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
			val[i][amount] = Byte.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
		    } else if (idx2 < end) {
			val[i][amount] = Byte.decode(StringKit.safeSubstring(text, idx + dl, idx2));
		    } else {
			val[i][amount] = Byte.decode(StringKit.safeSubstring(text, idx + dl, end));
		    }
		}
		start = end + 1;
		end = text.indexOf(';', start);
	    }
	    return val;
	}
    };
    public static final StringConvert<short[][]> convertArrayShort2D = new StringConvert<>(true, "short[][]") {
	@Override
	public String stringify(final short[][] item) {
	    if (item == null) {
		return "N";
	    }
	    int len;
	    if ((len = item.length) <= 0) {
		return "";
	    }
	    final StringBuilder sb = new StringBuilder(len * 128);
	    if (item[0] == null) {
		sb.append('n');
	    } else {
		sb.append(StringKit.join(",", item[0]));
	    }
	    for (int i = 1; i < len; i++) {
		if (item[i] == null) {
		    sb.append(";n");
		} else {
		    sb.append(';').append(StringKit.join(",", item[i]));
		}
	    }
	    return sb.toString();
	}

	@Override
	public short[][] restore(final String text) {
	    if (text == null) {
		return null;
	    }
	    int len;
	    if ((len = text.length()) <= 0) {
		return new short[0][0];
	    }
	    if (text.charAt(0) == 'N') {
		return null;
	    }
	    final int width = StringKit.count(text, ';') + 1;
	    final short[][] val = new short[width][];
	    int start = 0, end = text.indexOf(';');
	    for (int i = 0; i < width; i++) {
		if (start == end || start >= len) {
		    val[i] = new short[0];
		} else if (text.charAt(start) == 'n') {
		    val[i] = null;
		} else {
		    final int amount = StringKit.count(text, ",", start, end);
		    if (amount <= 0) {
			val[i] = new short[] { Short.decode(text) };
			continue;
		    }
		    val[i] = new short[amount + 1];
		    final int dl = 1;
		    int idx = start - dl, idx2;
		    for (int j = 0; j < amount; j++) {
			val[i][j] = Short
				.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
		    }
		    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
			val[i][amount] = Short.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
		    } else if (idx2 < end) {
			val[i][amount] = Short.decode(StringKit.safeSubstring(text, idx + dl, idx2));
		    } else {
			val[i][amount] = Short.decode(StringKit.safeSubstring(text, idx + dl, end));
		    }
		}
		start = end + 1;
		end = text.indexOf(';', start);
	    }
	    return val;
	}
    };
    public static final StringConvert<int[][]> convertArrayInt2D = new StringConvert<>(true, "int[][]") {
	@Override
	public String stringify(final int[][] item) {
	    if (item == null) {
		return "N";
	    }
	    int len;
	    if ((len = item.length) <= 0) {
		return "";
	    }
	    final StringBuilder sb = new StringBuilder(len * 128);
	    if (item[0] == null) {
		sb.append('n');
	    } else {
		sb.append(StringKit.join(",", item[0]));
	    }
	    for (int i = 1; i < len; i++) {
		if (item[i] == null) {
		    sb.append(";n");
		} else {
		    sb.append(';').append(StringKit.join(",", item[i]));
		}
	    }
	    return sb.toString();
	}

	@Override
	public int[][] restore(final String text) {
	    if (text == null) {
		return null;
	    }
	    int len;
	    if ((len = text.length()) <= 0) {
		return new int[0][0];
	    }
	    if (text.charAt(0) == 'N') {
		return null;
	    }
	    final int width = StringKit.count(text, ';') + 1;
	    final int[][] val = new int[width][];
	    int start = 0, end = text.indexOf(';');
	    for (int i = 0; i < width; i++) {
		if (start == end || start >= len) {
		    val[i] = new int[0];
		} else if (text.charAt(start) == 'n') {
		    val[i] = null;
		} else {
		    final int amount = StringKit.count(text, ",", start, end);
		    if (amount <= 0) {
			val[i] = new int[] { Integer.decode(text) };
			continue;
		    }
		    val[i] = new int[amount + 1];
		    final int dl = 1;
		    int idx = start - dl, idx2;
		    for (int j = 0; j < amount; j++) {
			val[i][j] = Integer
				.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
		    }
		    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
			val[i][amount] = Integer.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
		    } else if (idx2 < end) {
			val[i][amount] = Integer.decode(StringKit.safeSubstring(text, idx + dl, idx2));
		    } else {
			val[i][amount] = Integer.decode(StringKit.safeSubstring(text, idx + dl, end));
		    }
		}
		start = end + 1;
		end = text.indexOf(';', start);
	    }
	    return val;
	}
    };
    public static final StringConvert<long[][]> convertArrayLong2D = new StringConvert<>(true, "long[][]") {
	@Override
	public String stringify(final long[][] item) {
	    if (item == null) {
		return "N";
	    }
	    int len;
	    if ((len = item.length) <= 0) {
		return "";
	    }
	    final StringBuilder sb = new StringBuilder(len * 128);
	    if (item[0] == null) {
		sb.append('n');
	    } else {
		sb.append(StringKit.join(",", item[0]));
	    }
	    for (int i = 1; i < len; i++) {
		if (item[i] == null) {
		    sb.append(";n");
		} else {
		    sb.append(';').append(StringKit.join(",", item[i]));
		}
	    }
	    return sb.toString();
	}

	@Override
	public long[][] restore(final String text) {
	    if (text == null) {
		return null;
	    }
	    int len;
	    if ((len = text.length()) <= 0) {
		return new long[0][0];
	    }
	    if (text.charAt(0) == 'N') {
		return null;
	    }
	    final int width = StringKit.count(text, ';') + 1;
	    final long[][] val = new long[width][];
	    int start = 0, end = text.indexOf(';');
	    for (int i = 0; i < width; i++) {
		if (start == end || start >= len) {
		    val[i] = new long[0];
		} else if (text.charAt(start) == 'n') {
		    val[i] = null;
		} else {
		    final int amount = StringKit.count(text, ",", start, end);
		    if (amount <= 0) {
			val[i] = new long[] { Long.decode(text) };
			continue;
		    }
		    val[i] = new long[amount + 1];
		    final int dl = 1;
		    int idx = start - dl, idx2;
		    for (int j = 0; j < amount; j++) {
			val[i][j] = Long
				.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
		    }
		    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
			val[i][amount] = Long.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
		    } else if (idx2 < end) {
			val[i][amount] = Long.decode(StringKit.safeSubstring(text, idx + dl, idx2));
		    } else {
			val[i][amount] = Long.decode(StringKit.safeSubstring(text, idx + dl, end));
		    }
		}
		start = end + 1;
		end = text.indexOf(';', start);
	    }
	    return val;
	}
    };
    public static final StringConvert<float[][]> convertArrayFloat2D = new StringConvert<>(true, "float[][]") {
	@Override
	public String stringify(final float[][] item) {
	    if (item == null) {
		return "N";
	    }
	    int len;
	    if ((len = item.length) <= 0) {
		return "";
	    }
	    final StringBuilder sb = new StringBuilder(len * 128);
	    if (item[0] == null) {
		sb.append('n');
	    } else {
		sb.append(StringKit.join(",", item[0]));
	    }
	    for (int i = 1; i < len; i++) {
		if (item[i] == null) {
		    sb.append(";n");
		} else {
		    sb.append(';').append(StringKit.join(",", item[i]));
		}
	    }
	    return sb.toString();
	}

	@Override
	public float[][] restore(final String text) {
	    if (text == null) {
		return null;
	    }
	    int len;
	    if ((len = text.length()) <= 0) {
		return new float[0][0];
	    }
	    if (text.charAt(0) == 'N') {
		return null;
	    }
	    final int width = StringKit.count(text, ';') + 1;
	    final float[][] val = new float[width][];
	    int start = 0, end = text.indexOf(';');
	    for (int i = 0; i < width; i++) {
		if (start == end || start >= len) {
		    val[i] = new float[0];
		} else if (text.charAt(start) == 'n') {
		    val[i] = null;
		} else {
		    final int amount = StringKit.count(text, ",", start, end);
		    if (amount <= 0) {
			val[i] = new float[] { Float.parseFloat(text) };
			continue;
		    }
		    val[i] = new float[amount + 1];
		    final int dl = 1;
		    int idx = start - dl, idx2;
		    for (int j = 0; j < amount; j++) {
			val[i][j] = Float
				.parseFloat(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
		    }
		    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
			val[i][amount] = Float.parseFloat(StringKit.safeSubstring(text, idx + dl, text.length()));
		    } else if (idx2 < end) {
			val[i][amount] = Float.parseFloat(StringKit.safeSubstring(text, idx + dl, idx2));
		    } else {
			val[i][amount] = Float.parseFloat(StringKit.safeSubstring(text, idx + dl, end));
		    }
		}
		start = end + 1;
		end = text.indexOf(';', start);
	    }
	    return val;
	}
    };
    public static final StringConvert<double[][]> convertArrayDouble2D = new StringConvert<>(true, "double[][]") {
	@Override
	public String stringify(final double[][] item) {
	    if (item == null) {
		return "N";
	    }
	    int len;
	    if ((len = item.length) <= 0) {
		return "";
	    }
	    final StringBuilder sb = new StringBuilder(len * 128);
	    if (item[0] == null) {
		sb.append('n');
	    } else {
		sb.append(StringKit.join(",", item[0]));
	    }
	    for (int i = 1; i < len; i++) {
		if (item[i] == null) {
		    sb.append(";n");
		} else {
		    sb.append(';').append(StringKit.join(",", item[i]));
		}
	    }
	    return sb.toString();
	}

	@Override
	public double[][] restore(final String text) {
	    if (text == null) {
		return null;
	    }
	    int len;
	    if ((len = text.length()) <= 0) {
		return new double[0][0];
	    }
	    if (text.charAt(0) == 'N') {
		return null;
	    }
	    final int width = StringKit.count(text, ';') + 1;
	    final double[][] val = new double[width][];
	    int start = 0, end = text.indexOf(';');
	    for (int i = 0; i < width; i++) {
		if (start == end || start >= len) {
		    val[i] = new double[0];
		} else if (text.charAt(start) == 'n') {
		    val[i] = null;
		} else {
		    final int amount = StringKit.count(text, ",", start, end);
		    if (amount <= 0) {
			val[i] = new double[] { Double.parseDouble(text) };
			continue;
		    }
		    val[i] = new double[amount + 1];
		    final int dl = 1;
		    int idx = start - dl, idx2;
		    for (int j = 0; j < amount; j++) {
			val[i][j] = Double.parseDouble(
				StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
		    }
		    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
			val[i][amount] = Double.parseDouble(StringKit.safeSubstring(text, idx + dl, text.length()));
		    } else if (idx2 < end) {
			val[i][amount] = Double.parseDouble(StringKit.safeSubstring(text, idx + dl, idx2));
		    } else {
			val[i][amount] = Double.parseDouble(StringKit.safeSubstring(text, idx + dl, end));
		    }
		}
		start = end + 1;
		end = text.indexOf(';', start);
	    }
	    return val;
	}
    };
    public static final StringConvert<char[][]> convertArrayChar2D = new StringConvert<>(true, "char[][]") {
	@Override
	public String stringify(final char[][] item) {
	    int len, l2, sum;
	    if (item == null) {
		return "N"; // N for null
	    }
	    if ((len = item.length) <= 0) {
		return "R0|0|";
	    }
	    sum = l2 = item[0].length;
	    char regular = 'R'; // R for rectangular
	    for (int i = 1; i < len; i++) {
		if (item[i] == null) {
		    regular = 'J'; // J for jagged
		} else if (l2 != (l2 = item[i].length)) {
		    regular = 'J'; // J for jagged
		    sum += l2;
		}
	    }
	    StringBuilder sb;
	    if (regular == 'R') {
		sb = new StringBuilder(len * l2 + 15);
		sb.append('R').append(len).append('|').append(l2).append('|');
		for (int i = 0; i < len; i++) {
		    sb.append(item[i]);
		}
	    } else {
		sb = new StringBuilder(len * 7 + sum + 8);
		sb.append('J').append(len).append('|');
		for (int i = 0; i < len; i++) {
		    if (item[i] == null) {
			sb.append("-|");
		    } else {
			sb.append(item[i].length).append('|').append(item[i]);
		    }
		}
	    }
	    return sb.toString();
	}

	@Override
	public char[][] restore(final String text) {
	    if (text == null || text.length() <= 1) {
		return null;
	    }
	    if (text.charAt(0) == 'R') {
		int width, height, start = 1, end = text.indexOf('|');
		width = StringKit.intFromDec(text, start, end);
		start = end + 1;
		end = text.indexOf('|', start);
		height = StringKit.intFromDec(text, start, end);
		start = end + 1;
		final char[][] val = new char[width][height];
		for (int i = 0; i < width; i++) {
		    text.getChars(start, start += height, val[i], 0);
		}
		return val;
	    } else {
		int width, current, start = 1, end = text.indexOf('|');
		width = StringKit.intFromDec(text, start, end);
		start = end + 1;
		final char[][] val = new char[width][];
		for (int i = 0; i < width; i++) {
		    end = text.indexOf('|', start);
		    if (text.charAt(start) == '-') {
			val[i] = null;
			start = end + 1;
		    } else {
			current = StringKit.intFromDec(text, start, end);
			start = end + 1;
			val[i] = new char[current];
			text.getChars(start, start += current, val[i], 0);
		    }
		}
		return val;
	    }
	}
    };
}
