/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 See AUTHORS file
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.mini2Dx.minibus.messagedata;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mini2Dx.minibus.MessageData;
import org.mini2Dx.minibus.pool.MessageDataPool;
import org.mini2Dx.minibus.pool.OptionallyPooledMessageData;
import org.mini2Dx.minibus.pool.PooledMessageData;

/**
 * A {@link MessageData} instance that also implements the {@link Set}
 * interface, backed by a {@link Set} instance
 */
public class SetMessageData<T> extends OptionallyPooledMessageData implements Set<T> {
    private final Set<T> set;

    /**
     * Constructs a non-pooled {@link SetMessageData} instance backed by a
     * {@link HashSet}
     */
    public SetMessageData() {
	this(new HashSet<T>());
    }

    /**
     * Constructs a non-pooled {@link SetMessageData} instance
     *
     * @param set The backing {@link Set} instance
     */
    public SetMessageData(final Set<T> set) {
	super();
	this.set = set;
    }

    /**
     * Constructs a pooled {@link SetMessageData} instance
     *
     * @param pool The {@link MessageDataPool} managing this instance
     */
    public SetMessageData(final MessageDataPool<PooledMessageData> pool) {
	super(pool);
	this.set = new HashSet<>();
    }

    @Override
    public int size() {
	return this.set.size();
    }

    @Override
    public boolean isEmpty() {
	return this.set.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
	return this.set.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
	return this.set.iterator();
    }

    @Override
    public Object[] toArray() {
	return this.set.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
	return this.set.toArray(a);
    }

    @Override
    public boolean add(final T e) {
	return this.set.add(e);
    }

    @Override
    public boolean remove(final Object o) {
	return this.set.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
	return this.set.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends T> c) {
	return this.set.addAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
	return this.set.retainAll(c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
	return this.set.removeAll(c);
    }

    @Override
    public void clear() {
	this.set.clear();
    }
}
