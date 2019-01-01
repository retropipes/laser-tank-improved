package squidpony.squidmath;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import squidpony.annotation.Beta;

/**
 * Mostly just a way to have an EnumMap-like class that can be serialized
 * reasonably. Should be usable for anything an EnumMap is usable for.
 * Serialization is possible with this, like EnumMap can be serialized in
 * squidlib-extra, but this still needs some hack-y techniques. Created by Tommy
 * Ettinger on 9/5/2017.
 */
@Beta
public class EnumMapPlus<K extends Enum<K>, V> implements Map<K, V> {
    public final K[] keys;
    public final Class<K> keyType;
    public final V[] vals;
    public int size;

    /**
     * Creates an empty enum map with the specified key type.
     *
     * @param keyType the class object of the key type for this enum map
     * @throws NullPointerException if <tt>keyType</tt> is null
     */
    @SuppressWarnings("unchecked")
    public EnumMapPlus(final Class<K> keyType) {
	this.keyType = keyType;
	this.keys = keyType.getEnumConstants();
	this.vals = (V[]) new Object[this.keys.length];
    }

    /**
     * Creates an enum map with the same key type as the specified enum map,
     * initially containing the same mappings (if any).
     *
     * @param m the enum map from which to initialize this enum map
     * @throws NullPointerException if <tt>m</tt> is null
     */
    public EnumMapPlus(final EnumMapPlus<K, ? extends V> m) {
	this(m.keyType);
    }

    @Override
    public int size() {
	return this.size;
    }

    @Override
    public boolean isEmpty() {
	return this.size <= 0;
    }

    public int validate(final Object key) {
	if (key != null && key instanceof Enum
		&& ((Enum) key).getDeclaringClass() == this.keyType.getDeclaringClass()) {
	    final int o = ((Enum) key).ordinal();
	    return o >= 0 && o < this.keys.length ? o : -1;
	}
	return -1;
    }

    @Override
    public boolean containsKey(final Object key) {
	final int v = this.validate(key);
	return v != -1 && this.vals[v] != null;
    }

    @Override
    public boolean containsValue(final Object value) {
	if (value == null) {
	    return false;
	}
	final int len = this.keys.length;
	for (int i = 0; i < len; i++) {
	    if (value.equals(this.vals[i])) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public V get(final Object key) {
	final int v = this.validate(key);
	return v != -1 ? this.vals[v] : null;
    }

    @Override
    public V put(final K key, final V value) {
	if (key == null || value == null) {
	    return null;
	}
	final int ord = key.ordinal();
	final V old = this.vals[ord];
	if (old == null) {
	    this.size++;
	}
	this.vals[ord] = value;
	return old;
    }

    public V putAt(final int place, final V value) {
	if (value == null || place < 0 || place >= this.keys.length) {
	    return null;
	}
	final V old = this.vals[place];
	if (old == null) {
	    this.size++;
	}
	this.vals[place] = value;
	return old;
    }

    @Override
    public V remove(final Object key) {
	final int v = this.validate(key);
	if (v != -1) {
	    this.vals[v] = null;
	    this.size--;
	}
	return null;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
	for (final Entry<? extends K, ? extends V> kv : m.entrySet()) {
	    this.put(kv.getKey(), kv.getValue());
	}
    }

    @Override
    public void clear() {
	Arrays.fill(this.vals, null);
	this.size = 0;
    }

    @Override
    public OrderedSet<K> keySet() {
	final OrderedSet<K> k = new OrderedSet<>(this.size);
	final int len = this.keys.length;
	for (int i = 0, s = 0; s < this.size && i < len; i++) {
	    if (this.vals[i] != null) {
		k.add(this.keys[i]);
		s++;
	    }
	}
	return k;
    }

    @Override
    public ArrayList<V> values() {
	final ArrayList<V> v = new ArrayList<>(this.size);
	final int len = this.keys.length;
	V vl;
	for (int i = 0, s = 0; s < this.size && i < len; i++) {
	    if ((vl = this.vals[i]) != null) {
		v.add(vl);
		s++;
	    }
	}
	return v;
    }

    @Override
    public OrderedSet<Entry<K, V>> entrySet() {
	final OrderedSet<Entry<K, V>> kv = new OrderedSet<>(this.size);
	final int len = this.keys.length;
	V v;
	for (int i = 0, s = 0; s < this.size && i < len; i++) {
	    if ((v = this.vals[i]) != null) {
		kv.add(new AbstractMap.SimpleEntry<>(this.keys[i], v));
		s++;
	    }
	}
	return kv;
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final EnumMapPlus<?, ?> that = (EnumMapPlus<?, ?>) o;
	if (this.size != that.size) {
	    return false;
	}
	if (!this.keyType.equals(that.keyType)) {
	    return false;
	}
	final int len = this.vals.length;
	V v;
	for (int i = 0; i < len; i++) {
	    if ((v = this.vals[i]) != null && !v.equals(that.vals[i])) {
		return false;
	    }
	}
	return true;
    }

    @Override
    public int hashCode() {
	int result = CrossHash.hash(this.keys);
	result = 31 * result + this.keyType.hashCode();
	result = 31 * result + CrossHash.hash(this.vals);
	result = 31 * result + this.size;
	return result;
    }

    @Override
    public String toString() {
	final StringBuilder sb = new StringBuilder(this.size * 32);
	final int len = this.keys.length;
	V v;
	sb.append('{');
	for (int i = 0, s = 0; s < this.size && i < len; i++) {
	    if ((v = this.vals[i]) != null) {
		sb.append(this.keys[i]).append("=>").append(v);
		if (++s < this.size) {
		    sb.append(", ");
		}
	    }
	}
	return sb.append('}').toString();
    }
}
