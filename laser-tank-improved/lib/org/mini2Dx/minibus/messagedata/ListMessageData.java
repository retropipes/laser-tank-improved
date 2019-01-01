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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.mini2Dx.minibus.MessageData;
import org.mini2Dx.minibus.pool.MessageDataPool;
import org.mini2Dx.minibus.pool.OptionallyPooledMessageData;
import org.mini2Dx.minibus.pool.PooledMessageData;

/**
 * A {@link MessageData} instance that also implements the {@link List} message,
 * backed by a {@link List} instance
 */
public class ListMessageData<T> extends OptionallyPooledMessageData implements List<T> {
    private final List<T> list;

    /**
     * Constructs a non-pooled {@link ListMessageData} instance backed by an
     * {@link ArrayList}
     */
    public ListMessageData() {
	this(new ArrayList<T>(1));
    }

    /**
     * Constructs a non-pooled {@link ListMessageData} instance
     *
     * @param list The backing {@link List} instance
     */
    public ListMessageData(final List<T> list) {
	super();
	this.list = list;
    }

    /**
     * Constructs a pooled {@link ListMessageData} instance backed by an
     * {@link ArrayList}
     *
     * @param pool The {@link MessageDataPool} managing this instance
     */
    public ListMessageData(final MessageDataPool<PooledMessageData> pool) {
	super(pool);
	this.list = new ArrayList<>(1);
    }

    @Override
    public int size() {
	return this.list.size();
    }

    @Override
    public boolean isEmpty() {
	return this.list.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
	return this.list.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
	return this.list.iterator();
    }

    @Override
    public Object[] toArray() {
	return this.list.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
	return this.list.toArray(a);
    }

    @Override
    public boolean add(final T e) {
	return this.list.add(e);
    }

    @Override
    public boolean remove(final Object o) {
	return this.list.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
	return this.list.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends T> c) {
	return this.list.addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends T> c) {
	return this.list.addAll(index, c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
	return this.list.removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
	return this.list.retainAll(c);
    }

    @Override
    public void clear() {
	this.list.clear();
    }

    @Override
    public T get(final int index) {
	return this.list.get(index);
    }

    @Override
    public T set(final int index, final T element) {
	return this.list.set(index, element);
    }

    @Override
    public void add(final int index, final T element) {
	this.list.add(index, element);
    }

    @Override
    public T remove(final int index) {
	return this.list.remove(index);
    }

    @Override
    public int indexOf(final Object o) {
	return this.list.indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
	return this.list.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
	return this.list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(final int index) {
	return this.list.listIterator(index);
    }

    @Override
    public List<T> subList(final int fromIndex, final int toIndex) {
	return this.list.subList(fromIndex, toIndex);
    }
}
