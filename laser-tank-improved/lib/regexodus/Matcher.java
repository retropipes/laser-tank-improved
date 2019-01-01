/**
 * Copyright (c) 2001, Sergey A. Samokhodkin
 * All rights reserved.
 * <br>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <br>
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * - Redistributions in binary form
 * must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of jregex nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific prior
 * written permission.
 * <br>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @version 1.2_01
 */
package regexodus;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

import regexodus.ds.IntBitSet;

/**
 * Matcher is an automaton that actually performs matching. It provides the
 * following methods:
 * <ul>
 * <li>searching for a matching sub-strings : matcher.find() or
 * matcher.findAll();</li>
 * <li>testing whether a text matches a whole pattern : matcher.matches();</li>
 * <li>testing whether the text matches the beginning of a pattern :
 * matcher.matchesPrefix();</li>
 * <li>searching with custom options : matcher.find(int options)</li>
 * </ul>
 * <br>
 * <b>Obtaining results</b> <br>
 * After the search succeeded, i.e. if one of above methods returned
 * <code>true</code> one may obtain an information on the match:
 * <ul>
 * <li>may check whether some group is captured : matcher.isCaptured(int);</li>
 * <li>may obtain start and end positions of the match and its length :
 * matcher.start(int),matcher.end(int),matcher.length(int);</li>
 * <li>may obtain match contents as String : matcher.group(int).</li>
 * </ul>
 * <br>
 * The same way can be obtained the match prefix and suffix information. The
 * appropriate methods are grouped in MatchResult interface, which the Matcher
 * class implements. <br>
 * You typically obtain a Matcher through a Pattern instance's matcher() method.
 * See the Pattern documentation for the normal ways to create a Pattern; if you
 * are already familiar with java.util.regex.Pattern, constructing a regexodus
 * Pattern should be no different. <br>
 * Matcher (and Pattern) objects are not thread-safe, so only one thread may use
 * a matcher instance at a time.
 */
public class Matcher implements MatchResult, Serializable {
    private static final long serialVersionUID = -3628346657932720807L;
    /* Matching options */
    /**
     * The same effect as "^" without REFlags.MULTILINE.
     *
     * @see Matcher#find(int)
     */
    public static final int ANCHOR_START = 1;
    /**
     * The same effect as "\\G".
     *
     * @see Matcher#find(int)
     */
    public static final int ANCHOR_LASTMATCH = 2;
    /**
     * The same effect as "$" without REFlags.MULTILINE.
     *
     * @see Matcher#find(int)
     */
    public static final int ANCHOR_END = 4;
    /**
     * Experimental option; if a text ends up before the end of a pattern,report a
     * match.
     *
     * @see Matcher#find(int)
     */
    public static final int ACCEPT_INCOMPLETE = 8;
    // see search(ANCHOR_START|...)
    private static Term startAnchor = new Term(Term.START);
    // see search(ANCHOR_LASTMATCH|...)
    private static Term lastMatchAnchor = new Term(Term.LAST_MATCH_END);
    private Pattern re;
    private int[] counters;
    private MemReg[] memregs;
    private LAEntry[] lookaheads;
    private int counterCount;
    private int memregCount;
    private int lookaheadCount;
    private char[] data;
    private int offset, end, wOffset, wEnd;
    private boolean shared;
    private SearchEntry top; // stack entry
    private SearchEntry first; // object pool entry
    private SearchEntry defaultEntry; // called when moving the window
    private boolean called;
    private int minQueueLength;
    private CharSequence cache;
    // cache may be longer than the actual data
    // and contrariwise; so cacheOffset may have both signs.
    // cacheOffset is actually -(data offset).
    private int cacheOffset, cacheLength;
    private MemReg prefixBounds, suffixBounds, targetBounds;

    public Matcher copy() {
	final Matcher m = new Matcher(this.re, this.cache);
	m.wEnd = this.wEnd;
	m.wOffset = this.wOffset;
	m.called = this.called;
	m.offset = this.offset;
	m.end = this.end;
	return m;
    }

    public Matcher(final Pattern regex) {
	this.setPattern(regex);
    }

    public Matcher(final Pattern regex, final CharSequence target) {
	this.setPattern(regex);
	this.setTarget(target);
    }

    /**
     * Sets the regex Pattern this tries to match. Won't do anything until the
     * target is set as well.
     *
     * @param regex the Pattern this should match
     */
    public void setPattern(final Pattern regex) {
	this.re = regex;
	int memregCount, counterCount, lookaheadCount;
	if ((memregCount = regex.memregs) > 0) {
	    final MemReg[] memregs = new MemReg[memregCount];
	    for (int i = 0; i < memregCount; i++) {
		memregs[i] = new MemReg(-1); // unlikely to SearchEntry, in this case we know memreg indices by
					     // definition
	    }
	    this.memregs = memregs;
	}
	if ((counterCount = regex.counters) > 0) {
	    this.counters = new int[counterCount];
	}
	if ((lookaheadCount = regex.lookaheads) > 0) {
	    final LAEntry[] lookaheads = new LAEntry[lookaheadCount];
	    for (int i = 0; i < lookaheadCount; i++) {
		lookaheads[i] = new LAEntry();
	    }
	    this.lookaheads = lookaheads;
	}
	this.memregCount = memregCount;
	this.counterCount = counterCount;
	this.lookaheadCount = lookaheadCount;
	this.first = new SearchEntry();
	this.defaultEntry = new SearchEntry();
	this.minQueueLength = regex.stringRepr.length() / 2; // just evaluation!!!
    }

    /**
     * This method allows to efficiently pass data between matchers. Note that a
     * matcher may pass data to itself:
     *
     * <pre>
     * Matcher m = new Pattern("\\w+").matcher(myString);
     * if (m.find())
     *     m.setTarget(m, m.SUFFIX); // forget all that is not a suffix
     * </pre>
     *
     * Resets current search position to zero.
     *
     * @param m       - a matcher that is a source of data
     * @param groupId - which group to take data from
     * @see Matcher#setTarget(java.lang.CharSequence)
     * @see Matcher#setTarget(java.lang.CharSequence, int, int)
     * @see Matcher#setTarget(char[], int, int)
     * @see Matcher#setTarget(java.io.Reader, int)
     */
    public final void setTarget(final Matcher m, final int groupId) {
	final MemReg mr = m.bounds(groupId);
	if (mr == null) {
	    throw new IllegalArgumentException("group #" + groupId + " is not assigned");
	}
	this.data = m.data;
	this.offset = mr.in;
	this.end = mr.out;
	this.cache = m.cache;
	this.cacheLength = m.cacheLength;
	this.cacheOffset = m.cacheOffset;
	if (m != this) {
	    this.shared = true;
	    m.shared = true;
	}
	this.init();
    }

    /**
     * Supplies a text to search in/match with. Resets current search position to
     * zero.
     *
     * @param text - a data
     * @see Matcher#setTarget(regexodus.Matcher, int)
     * @see Matcher#setTarget(java.lang.CharSequence, int, int)
     * @see Matcher#setTarget(char[], int, int)
     * @see Matcher#setTarget(java.io.Reader, int)
     */
    public void setTarget(final CharSequence text) {
	this.setTarget(text, 0, text.length());
    }

    /**
     * Supplies a text to search in/match with, as a part of String. Resets current
     * search position to zero.
     *
     * @param text  - a data source
     * @param start - where the target starts
     * @param len   - how long is the target
     * @see Matcher#setTarget(regexodus.Matcher, int)
     * @see Matcher#setTarget(java.lang.CharSequence)
     * @see Matcher#setTarget(char[], int, int)
     * @see Matcher#setTarget(java.io.Reader, int)
     */
    public void setTarget(final CharSequence text, final int start, final int len) {
	char[] mychars = this.data;
	if (mychars == null || this.shared || mychars.length < len) {
	    this.data = mychars = new char[(int) (1.7f * len)];
	    this.shared = false;
	}
	for (int i = start, p = 0; i < len; i++, p++) {
	    mychars[p] = text.charAt(i);
	}
	// text.getChars(start, len, mychars, 0); //(srcBegin,srcEnd,dst[],dstBegin)
	this.offset = 0;
	this.end = len;
	this.cache = text;
	this.cacheOffset = -start;
	this.cacheLength = text.length();
	this.init();
    }

    /**
     * Supplies a text to search in/match with, as a part of char array. Resets
     * current search position to zero.
     *
     * @param text  - a data source
     * @param start - where the target starts
     * @param len   - how long is the target
     * @see Matcher#setTarget(regexodus.Matcher, int)
     * @see Matcher#setTarget(java.lang.CharSequence)
     * @see Matcher#setTarget(java.lang.CharSequence, int, int)
     * @see Matcher#setTarget(java.io.Reader, int)
     */
    public void setTarget(final char[] text, final int start, final int len) {
	this.setTarget(text, start, len, true);
    }

    /**
     * To be used with much care. Supplies a text to search in/match with, as a part
     * of a char array, as above, but also allows to permit to use the array as
     * internal buffer for subsequent inputs. That is, if we call it with
     * <code>shared=false</code>:
     *
     * <pre>
     *   myMatcher.setTarget(myCharArray,x,y,<b>false</b>); //we declare that array contents is NEITHER shared NOR will be used later, so may modifications on it are permitted
     * </pre>
     *
     * then we should expect the array contents to be changed on subsequent
     * setTarget(..) operations. Such method may yield some increase in perfomance
     * in the case of multiple setTarget() calls. Resets current search position to
     * zero.
     *
     * @param text   - a data source
     * @param start  - where the target starts
     * @param len    - how long is the target
     * @param shared - if <code>true</code>: data are shared or used later,
     *               <b>don't</b> modify it; if <code>false</code>: possible
     *               modifications of the text on subsequent
     *               <code>setTarget()</code> calls are perceived and allowed.
     * @see Matcher#setTarget(regexodus.Matcher, int)
     * @see Matcher#setTarget(java.lang.CharSequence)
     * @see Matcher#setTarget(java.lang.CharSequence, int, int)
     * @see Matcher#setTarget(char[], int, int)
     * @see Matcher#setTarget(java.io.Reader, int)
     */
    public void setTarget(final char[] text, final int start, final int len, final boolean shared) {
	this.cache = null;
	this.data = text;
	this.offset = start;
	this.end = start + len;
	this.shared = shared;
	this.init();
    }

    /**
     * Supplies a text to search in/match with through a stream. Resets current
     * search position to zero.
     *
     * @param in  - a data stream;
     * @param len - how much characters should be read; if len is -1, read the
     *            entire stream.
     * @see Matcher#setTarget(regexodus.Matcher, int)
     * @see Matcher#setTarget(java.lang.CharSequence)
     * @see Matcher#setTarget(java.lang.CharSequence, int, int)
     * @see Matcher#setTarget(char[], int, int)
     */
    @GwtIncompatible
    public void setTarget(final Reader in, int len) throws IOException {
	if (len < 0) {
	    this.setAll(in);
	    return;
	}
	char[] mychars = this.data;
	boolean shared = this.shared;
	if (mychars == null || shared || mychars.length < len) {
	    mychars = new char[len];
	    shared = false;
	}
	int count = 0;
	int c;
	while ((c = in.read(mychars, count, len)) >= 0) {
	    len -= c;
	    count += c;
	    if (len == 0) {
		break;
	    }
	}
	this.setTarget(mychars, 0, count, shared);
    }

    @GwtIncompatible
    public void setAll(final Reader in) throws IOException {
	char[] mychars = this.data;
	int free;
	if (mychars == null || this.shared) {
	    mychars = new char[free = 1024];
	} else {
	    free = mychars.length;
	}
	int count = 0;
	int c;
	while ((c = in.read(mychars, count, free)) >= 0) {
	    free -= c;
	    count += c;
	    if (free == 0) {
		final int newsize = count * 3;
		final char[] newchars = new char[newsize];
		System.arraycopy(mychars, 0, newchars, 0, count);
		mychars = newchars;
		free = newsize - count;
	    }
	}
	this.setTarget(mychars, 0, count, false);
    }

    public String getString(final int start, final int end) {
	/*
	 * if(end < 0) { return "<<<Incomplete Match>>> " + cache; }
	 */
	if (this.cache != null) {
	    final int co = this.cacheOffset;
	    return this.cache.toString().substring(start - co, end - co);
	}
	CharSequence src;
	final int tOffset = this.offset, tLen = this.end - tOffset;
	final char[] data = this.data;
	if (end - start >= tLen / 3) {
	    // it makes sense to make a cache
	    this.cache = new String(data);
	    src = new String(data, tOffset, tLen);
	    this.cacheOffset = tOffset;
	    this.cacheLength = tLen;
	    return src.toString(); // .toString().substring(start - tOffset, end - tOffset);
	}
	return new String(data, start, end - start);
    }

    /* Matching */
    /**
     * Tells whether the entire target matches the beginning of the pattern. The
     * whole pattern is also regarded as its beginning.<br>
     * This feature allows to find a mismatch by examining only a beginning part of
     * the target (as if the beginning of the target doesn't match the beginning of
     * the pattern, then the entire target also couldn't match).<br>
     * For example the following assertions yield <code>true</code>:
     *
     * <pre>
     * Pattern p = new Pattern("abcd");
     * p.matcher("").matchesPrefix();
     * p.matcher("a").matchesPrefix();
     * p.matcher("ab").matchesPrefix();
     * p.matcher("abc").matchesPrefix();
     * p.matcher("abcd").matchesPrefix();
     * </pre>
     *
     * and the following yield <code>false</code>:
     *
     * <pre>
     * p.matcher("b").isPrefix();
     * p.matcher("abcdef").isPrefix();
     * p.matcher("x").isPrefix();
     * </pre>
     *
     * @return true if the entire target matches the beginning of the pattern
     */
    public final boolean matchesPrefix() {
	this.setPosition(0);
	return this.search(Matcher.ANCHOR_START | Matcher.ACCEPT_INCOMPLETE | Matcher.ANCHOR_END);
    }

    /**
     * Just an old name for isPrefix().<br>
     * Retained for backwards compatibility.
     *
     * @deprecated Replaced by isPrefix()
     */
    public final boolean isStart() {
	return this.matchesPrefix();
    }

    /**
     * Tells whether a current target matches the whole pattern. For example the
     * following yields the <code>true</code>:
     *
     * <pre>
     * Pattern p = new Pattern("\\w+");
     * p.matcher("a").matches();
     * p.matcher("ab").matches();
     * p.matcher("abc").matches();
     * </pre>
     *
     * and the following yields the <code>false</code>:
     *
     * <pre>
     * p.matcher("abc def").matches();
     * p.matcher("bcd ").matches();
     * p.matcher(" bcd").matches();
     * p.matcher("#xyz#").matches();
     * </pre>
     *
     * @return whether a current target matches the whole pattern.
     */
    public final boolean matches() {
	if (this.called) {
	    this.setPosition(0);
	}
	return this.search(Matcher.ANCHOR_START | Matcher.ANCHOR_END);
    }

    /**
     * Just a combination of setTarget(String) and matches().
     *
     * @param s the target string;
     * @return whether the specified string matches the whole pattern.
     */
    public final boolean matches(final String s) {
	this.setTarget(s);
	return this.search(Matcher.ANCHOR_START | Matcher.ANCHOR_END);
    }

    /**
     * Allows to set a position the subsequent find()/find(int) will start from.
     *
     * @param pos the position to start from;
     * @see Matcher#find()
     * @see Matcher#find(int)
     */
    public void setPosition(final int pos) {
	this.wOffset = this.offset + pos;
	this.wEnd = -1;
	this.called = false;
	this.flush();
    }

    /**
     * Searches through a target for a matching substring, starting from just after
     * the end of last match. If there wasn't any search performed, starts from
     * zero.
     *
     * @return <code>true</code> if a match found.
     */
    public final boolean find() {
	if (this.called) {
	    this.skip();
	}
	return this.search(0);
    }

    /**
     * Searches through a target for a matching substring, starting from just after
     * the end of last match. If there wasn't any search performed, starts from
     * zero.
     *
     * @param anchors a zero or a combination(bitwise OR) of
     *                ANCHOR_START,ANCHOR_END,ANCHOR_LASTMATCH,ACCEPT_INCOMPLETE
     * @return <code>true</code> if a match found.
     */
    public boolean find(final int anchors) {
	if (this.called) {
	    this.skip();
	}
	return this.search(anchors);
    }

    /**
     * The same as findAll(int), but with default behaviour;
     */
    public MatchIterator findAll() {
	return this.findAll(0);
    }

    /**
     * Returns an iterator over the matches found by subsequently calling
     * find(options), the search starts from the zero position.
     */
    public MatchIterator findAll(final int options) {
	// setPosition(0);
	return new MatchIterator() {
	    private boolean checked = false;
	    private boolean hasMore = false;

	    @Override
	    public boolean hasNext() {
		if (!this.checked) {
		    this.check();
		}
		return this.hasMore;
	    }

	    @Override
	    public MatchResult next() {
		if (!this.checked) {
		    this.check();
		}
		if (!this.hasMore) {
		    throw new NoSuchElementException();
		}
		this.checked = false;
		return Matcher.this;
	    }

	    @Override
	    public void remove() {
		throw new UnsupportedOperationException("remove() not supported on MatchIterator");
	    }

	    private void check() {
		this.hasMore = Matcher.this.find(options);
		this.checked = true;
	    }

	    @Override
	    public int count() {
		if (!this.checked) {
		    this.check();
		}
		if (!this.hasMore) {
		    return 0;
		}
		int c = 1;
		while (Matcher.this.find(options)) {
		    c++;
		}
		this.checked = false;
		return c;
	    }

	    @Override
	    public ArrayList<String> asList() {
		if (!this.checked) {
		    this.check();
		}
		final ArrayList<String> found = new ArrayList<>(16);
		if (!this.hasMore) {
		    return found;
		}
		found.add(Matcher.this.group());
		while (Matcher.this.find(options)) {
		    found.add(Matcher.this.group());
		}
		this.checked = false;
		return found;
	    }
	};
    }

    public ArrayList<String> foundStrings() {
	return this.findAll().asList();
    }

    /**
     * Continues to search from where the last search left off. The same as
     * proceed(0).
     *
     * @see Matcher#proceed(int)
     */
    public final boolean proceed() {
	return this.proceed(0);
    }

    /**
     * Continues to search from where the last search left off using specified
     * options:
     *
     * <pre>
     * Matcher m = new Pattern("\\w+").matcher("abc");
     * while (m.proceed(0)) {
     *     System.out.println(m.group(0));
     * }
     * </pre>
     *
     * Output:
     *
     * <pre>
     * abc
     * ab
     * a
     * bc
     * b
     * c
     * </pre>
     *
     * For example, let's find all odd numbers occurring in a text:
     *
     * <pre>
     *    Matcher m=new Pattern("\\d+").matcher("123");
     *    while(m.proceed(0)){
     *       String match=m.group(0);
     *       if(isOdd(Integer.parseInt(match))) System.out.println(match);
     *    }
     *
     *    static boolean isOdd(int i){
     *       return (i&amp;1)&gt;0;
     *    }
     * </pre>
     *
     * This outputs:
     *
     * <pre>
     * 123
     * 1
     * 23
     * 3
     * </pre>
     *
     * Note that using <code>find()</code> method we would find '123' only.
     *
     * @param options search options, some of
     *                ANCHOR_START|ANCHOR_END|ANCHOR_LASTMATCH|ACCEPT_INCOMPLETE;
     *                zero value(default) stands for usual search for substring.
     */
    public boolean proceed(final int options) {
	if (this.called) {
	    if (this.top == null) {
		this.wOffset++;
	    }
	}
	return this.search(0);
    }

    /**
     * Sets the current search position just after the end of last match.
     */
    public void skip() {
	final int we = this.wEnd;
	if (this.wOffset == we) { // requires special handling
	    // if no variants at 'wOutside',advance pointer and clear
	    if (this.top == null) {
		this.wOffset++;
		this.flush();
	    }
	    // otherwise, if there exist a variant,
	    // don't clear(), i.e. allow it to match
	    return;
	} else {
	    if (we < 0) {
		this.wOffset = 0;
	    } else {
		this.wOffset = we;
	    }
	}
	// rflush(); //rflush() works faster on simple regexes (with a small
	// group/branch number)
	this.flush();
    }

    private void init() {
	// wOffset=-1;
	this.wOffset = this.offset;
	this.wEnd = -1;
	this.called = false;
	this.flush();
    }

    /**
     * Resets the internal state.
     */
    public void flush() {
	this.top = null;
	this.defaultEntry.reset(0);
	this.first.reset(this.minQueueLength);
	for (int i = this.memregs.length - 1; i > 0; i--) {
	    final MemReg mr = this.memregs[i];
	    mr.in = mr.out = -1;
	}
	/*
	 * for (int i = memregs.length - 1; i > 0; i--) { MemReg mr = memregs[i]; mr.in
	 * = mr.out = -1; }
	 */
	this.called = false;
    }

    /**
     */
    @Override
    public String toString() {
	return this.toString_d();
	// return getString(wOffset, wEnd);
    }

    @Override
    public Pattern pattern() {
	return this.re;
    }

    @Override
    public String target() {
	return this.getString(this.offset, this.end);
    }

    /**
     */
    @Override
    public char[] targetChars() {
	this.shared = true;
	return this.data;
    }

    /**
     */
    @Override
    public int targetStart() {
	return this.offset;
    }

    /**
     */
    @Override
    public int targetEnd() {
	return this.end;
    }

    /**
     */
    public int dataStart() {
	return 0;
    }

    /**
     */
    public int dataEnd() {
	return this.data.length;
    }

    @Override
    public char charAt(final int i) {
	final int in = this.wOffset;
	final int out = this.wEnd;
	if (in < 0 || out < in) {
	    throw new IllegalStateException("unassigned");
	}
	return this.data[in + i];
    }

    @Override
    public char charAt(final int i, final int groupId) {
	final MemReg mr = this.bounds(groupId);
	if (mr == null) {
	    throw new IllegalStateException("group #" + groupId + " is not assigned");
	}
	final int in = mr.in;
	if (i < 0 || i > mr.out - in) {
	    throw new StringIndexOutOfBoundsException("" + i);
	}
	return this.data[in + i];
    }

    @Override
    public final int length() {
	return this.wEnd - this.wOffset;
    }

    /**
     * Returns the start index of the match.
     *
     * @return The index of the first character matched
     */
    @Override
    public final int start() {
	return this.wOffset - this.offset;
    }

    /**
     * Returns the offset after the last character matched.
     *
     * @return The offset after the last character matched
     */
    @Override
    public final int end() {
	return this.wEnd - this.offset;
    }

    /**
     */
    @Override
    public String prefix() {
	return this.getString(this.offset, this.wOffset);
    }

    /**
     */
    @Override
    public String suffix() {
	return this.getString(this.wEnd, this.end);
    }

    /**
     * Returns the number of capturing groups in this match result's pattern.
     *
     * <p>
     * Group zero denotes the entire pattern by convention. It is not included in
     * this count.
     *
     * <p>
     * Any non-negative integer smaller than or equal to the value returned by this
     * method is guaranteed to be a valid group index for this matcher.
     * </p>
     *
     * @return The number of capturing groups in this matcher's pattern
     */
    @Override
    public int groupCount() {
	return this.memregs.length - 1;
    }

    /**
     * Returns the input subsequence captured by the given group during the previous
     * match operation.
     *
     * <p>
     * For a matcher <i>m</i>, input sequence <i>s</i>, and group index <i>g</i>,
     * the expressions <i>m.</i><tt>group(</tt><i>g</i><tt>)</tt> and
     * <i>s.</i><tt>substring(</tt><i>m.</i><tt>start(</tt><i>g</i><tt>),</tt>&nbsp;<i>m.</i><tt>end(</tt><i>g</i><tt>))</tt>
     * are equivalent.
     * </p>
     *
     * <p>
     * <a href="Pattern.html#cg">Capturing groups</a> are indexed from left to
     * right, starting at one. Group zero denotes the entire pattern, so the
     * expression <tt>m.group(0)</tt> is equivalent to <tt>m.group()</tt>.
     * </p>
     *
     * <p>
     * If the match was successful but the group specified failed to match any part
     * of the input sequence, then <tt>null</tt> is returned. Note that some groups,
     * for example <tt>(a*)</tt>, match the empty string. This method will return
     * the empty string when such a group successfully matches the empty string in
     * the input.
     * </p>
     *
     * @param group The index of a capturing group in this matcher's pattern
     *
     * @return The (possibly empty) subsequence captured by the group during the
     *         previous match, or <tt>""</tt> if the group failed to match part of
     *         the input
     */
    @Override
    public String group(final int group) {
	final MemReg mr = this.bounds(group);
	if (mr == null) {
	    return null;
	}
	return this.getString(mr.in, mr.out);
    }

    /**
     * Returns the input subsequence matched by the previous match.
     *
     * <p>
     * For a matcher <i>m</i> with input sequence <i>s</i>, the expressions
     * <i>m.</i><tt>group()</tt> and
     * <i>s.</i><tt>substring(</tt><i>m.</i><tt>start(),</tt>&nbsp;<i>m.</i><tt>end())</tt>
     * are equivalent.
     * </p>
     *
     * <p>
     * Note that some patterns, for example <tt>a*</tt>, match the empty string.
     * This method will return the empty string when the pattern successfully
     * matches the empty string in the input.
     * </p>
     *
     * @return The (possibly empty) subsequence matched by the previous match, in
     *         string form
     */
    @Override
    public String group() {
	return this.group(0);
    }

    /**
     * Returns the input subsequence captured by the given named group during the
     * previous match operation. <br>
     * Like {@link #group(int) group} but for named groups instead of numbered.
     *
     * @param name The name of a capturing group in this matcher's pattern
     *
     * @return The (possibly empty) subsequence captured by the group during the
     *         previous match, or <tt>null</tt> if the group failed to match part of
     *         the input
     */
    @Override
    public String group(final String name) {
	final Integer id = this.re.groupId(name);
	if (id == null) {
	    throw new IllegalArgumentException("<" + name + "> isn't defined");
	}
	return this.group(id);
    }

    @Override
    public boolean getGroup(final int group, final TextBuffer tb) {
	return this.getGroup(group, tb, 0);
    }

    @Override
    public boolean getGroup(final int group, final TextBuffer tb, final int modes) {
	final MemReg mr = this.bounds(group);
	if (mr == null) {
	    return false;
	}
	final int in = mr.in;
	if (modes == 0) {
	    tb.append(this.data, in, mr.out - in);
	} else {
	    final char[] working = new char[mr.out - in];
	    char t;
	    if ((modes & PerlSubstitution.MODE_REVERSE) > 0) {
		for (int i = working.length - 1, j = in; i >= 0; i--, j++) {
		    t = this.data[j];
		    if ((modes & PerlSubstitution.MODE_INSENSITIVE) > 0) {
			t = Category.caseFold(t);
		    }
		    if ((modes & PerlSubstitution.MODE_BRACKET) > 0) {
			t = Category.matchBracket(t);
		    }
		    working[i] = t;
		}
	    } else {
		for (int i = 0, j = in; i < working.length; i++, j++) {
		    t = this.data[j];
		    if ((modes & PerlSubstitution.MODE_INSENSITIVE) > 0) {
			t = Category.caseFold(t);
		    }
		    if ((modes & PerlSubstitution.MODE_BRACKET) > 0) {
			t = Category.matchBracket(t);
		    }
		    working[i] = t;
		}
	    }
	    tb.append(working, 0, working.length);
	}
	return true;
    }

    @Override
    public boolean getGroup(final String name, final TextBuffer tb) {
	return this.getGroup(name, tb, 0);
    }

    @Override
    public boolean getGroup(final String name, final TextBuffer tb, final int modes) {
	final Integer id = this.re.groupId(name);
	if (id == null) {
	    throw new IllegalArgumentException("unknown group: \"" + name + "\"");
	}
	return this.getGroup(id, tb);
    }

    @Override
    public boolean getGroup(final int group, final StringBuilder sb) {
	return this.getGroup(group, sb, 0);
    }

    @Override
    public boolean getGroup(final int group, final StringBuilder sb, final int modes) {
	final MemReg mr = this.bounds(group);
	if (mr == null) {
	    return false;
	}
	final int in = mr.in;
	if (modes == 0) {
	    sb.append(this.data, in, mr.out - in);
	} else {
	    final char[] working = new char[mr.out - in];
	    char t;
	    if ((modes & PerlSubstitution.MODE_REVERSE) > 0) {
		for (int i = working.length - 1, j = in; i >= 0; i--, j++) {
		    t = this.data[j];
		    if ((modes & PerlSubstitution.MODE_INSENSITIVE) > 0) {
			t = Category.caseFold(t);
		    }
		    if ((modes & PerlSubstitution.MODE_BRACKET) > 0) {
			t = Category.matchBracket(t);
		    }
		    working[i] = t;
		}
	    } else {
		for (int i = 0, j = in; i < working.length; i++, j++) {
		    t = this.data[j];
		    if ((modes & PerlSubstitution.MODE_INSENSITIVE) > 0) {
			t = Category.caseFold(t);
		    }
		    if ((modes & PerlSubstitution.MODE_BRACKET) > 0) {
			t = Category.matchBracket(t);
		    }
		    working[i] = t;
		}
	    }
	    sb.append(working);
	}
	return true;
    }

    @Override
    public boolean getGroup(final String name, final StringBuilder sb) {
	return this.getGroup(name, sb, 0);
    }

    @Override
    public boolean getGroup(final String name, final StringBuilder sb, final int modes) {
	final Integer id = this.re.groupId(name);
	if (id == null) {
	    throw new IllegalArgumentException("unknown group: \"" + name + "\"");
	}
	return this.getGroup(id, sb);
    }

    /**
     */
    public String[] groups() {
	final MemReg[] memregs = this.memregs;
	final String[] groups = new String[memregs.length];
	int in, out;
	MemReg mr;
	for (int i = 0; i < memregs.length; i++) {
	    mr = memregs[i];
	    out = mr.out;
	    if ((in = mr.in) < 0 || mr.out < in) {
		continue;
	    }
	    groups[i] = this.getString(in, out);
	}
	return groups;
    }

    /**
     */
    public ArrayList<String> groupv() {
	final MemReg[] memregs = this.memregs;
	final ArrayList<String> v = new ArrayList<>();
	MemReg mr;
	for (int i = 0; i < memregs.length; i++) {
	    mr = this.bounds(i);
	    if (mr == null) {
		v.add("empty");
		continue;
	    }
	    final String s = this.getString(mr.in, mr.out);
	    v.add(s);
	}
	return v;
    }

    private MemReg bounds(final int id) {
	MemReg mr;
	if (id >= this.memregs.length) {
	    return null;
	}
	if (id >= 0) {
	    mr = this.memregs[id];
	} else {
	    switch (id) {
	    case PREFIX:
		mr = this.prefixBounds;
		if (mr == null) {
		    this.prefixBounds = mr = new MemReg(MatchResult.PREFIX);
		}
		mr.in = this.offset;
		mr.out = this.wOffset;
		break;
	    case SUFFIX:
		mr = this.suffixBounds;
		if (mr == null) {
		    this.suffixBounds = mr = new MemReg(MatchResult.SUFFIX);
		}
		mr.in = this.wEnd;
		mr.out = this.end;
		break;
	    case TARGET:
		mr = this.targetBounds;
		if (mr == null) {
		    this.targetBounds = mr = new MemReg(MatchResult.TARGET);
		}
		mr.in = this.offset;
		mr.out = this.end;
		break;
	    default:
		throw new IllegalArgumentException("illegal group id: " + id
			+ "; must either nonnegative int, or MatchResult.PREFIX, or MatchResult.SUFFIX");
	    }
	}
	int in;
	if ((in = mr.in) < 0 || mr.out < in) {
	    return null;
	}
	return mr;
    }

    /**
     */
    @Override
    public final boolean isCaptured() {
	return this.wOffset >= 0 && this.wEnd >= this.wOffset;
    }

    /**
     */
    @Override
    public final boolean isCaptured(final int id) {
	return this.bounds(id) != null;
    }

    /**
     */
    @Override
    public final boolean isCaptured(final String groupName) {
	final Integer id = this.re.groupId(groupName);
	if (id == null) {
	    throw new IllegalArgumentException("unknown group: \"" + groupName + "\"");
	}
	return this.isCaptured(id);
    }

    /**
     */
    @Override
    public final int length(final int id) {
	final MemReg mr = this.bounds(id);
	if (mr != null) {
	    return mr.out - mr.in;
	}
	return 0;
    }

    /**
     * Returns the start index of the subsequence captured by the given group during
     * this match. <br>
     * Capturing groups are indexed from left to right, starting at one. Group zero
     * denotes the entire pattern, so the expression <i>m.</i><tt>start(0)</tt> is
     * equivalent to <i>m.</i><tt>start()</tt>.
     *
     * @param id The index of a capturing group in this matcher's pattern
     * @return The index of the first character captured by the group, or
     *         <tt>-1</tt> if the match was successful but the group itself did not
     *         match anything
     */
    @Override
    public final int start(final int id) {
	final MemReg b = this.bounds(id);
	if (b == null) {
	    return -1;
	}
	return b.in - this.offset;
    }

    /**
     * Returns the start index of the subsequence captured by the given
     * named-capturing group during the previous match operation.
     *
     * @param name The name of a named capturing group in this matcher's pattern
     * @return The index of the first character captured by the group, or
     *         <tt>-1</tt> if the match was successful but the group itself did not
     *         match anything
     */
    @Override
    public int start(final String name) {
	final Integer id = this.re.groupId(name);
	if (id == null) {
	    throw new IllegalArgumentException("<" + name + "> isn't defined");
	}
	return this.start(id);
    }

    /**
     * Returns the offset after the last character of the subsequence captured by
     * the given named-capturing group during the previous match operation.
     *
     * @param name The name of a named capturing group in this matcher's pattern
     * @return The offset after the last character captured by the group, or
     *         <tt>-1</tt> if the match was successful but the group itself did not
     *         match anything
     */
    @Override
    public int end(final String name) {
	final Integer id = this.re.groupId(name);
	if (id == null) {
	    throw new IllegalArgumentException("<" + name + "> isn't defined");
	}
	return this.end(id);
    }

    /**
     * Returns the offset after the last character of the subsequence captured by
     * the given group during this match. <br>
     * Capturing groups are indexed from left to right, starting at one. Group zero
     * denotes the entire pattern, so the expression <i>m.</i><tt>end(0)</tt> is
     * equivalent to <i>m.</i><tt>end()</tt>.
     *
     * @param id The index of a capturing group in this matcher's pattern
     *
     * @return The offset after the last character captured by the group, or
     *         <tt>-1</tt> if the match was successful but the group itself did not
     *         match anything
     */
    @Override
    public final int end(final int id) {
	final MemReg b = this.bounds(id);
	if (b == null) {
	    return -1;
	}
	return b.out - this.offset;
    }

    public boolean search(final int anchors) {
	this.called = true;
	final int end = this.end;
	final int offset = this.offset;
	final char[] data = this.data;
	int wOffset = this.wOffset;
	final int wEnd = this.wEnd;
	final MemReg[] memregs = this.memregs;
	final int[] counters = this.counters;
	final LAEntry[] lookaheads = this.lookaheads;
	// int memregCount=memregs.length;
	// int cntCount=counters.length;
	final SearchEntry defaultEntry = this.defaultEntry;
	final SearchEntry first = this.first;
	SearchEntry top = this.top;
	SearchEntry actual;
	int cnt, regLen;
	int i;
	final boolean matchEnd = (anchors & Matcher.ANCHOR_END) > 0;
	final boolean allowIncomplete = (anchors & Matcher.ACCEPT_INCOMPLETE) > 0;
	final Pattern re = this.re;
	Term root = re.root;
	Term term;
	if (top == null) {
	    if ((anchors & Matcher.ANCHOR_START) > 0) {
		term = re.root0; // raw root
		root = Matcher.startAnchor;
	    } else if ((anchors & Matcher.ANCHOR_LASTMATCH) > 0) {
		term = re.root0; // raw root
		root = Matcher.lastMatchAnchor;
	    } else {
		term = root; // optimized root
	    }
	    i = wOffset;
	    actual = first;
	    SearchEntry.popState(defaultEntry, memregs, counters);
	} else {
	    top = (actual = top).sub;
	    term = actual.term;
	    i = actual.index;
	    SearchEntry.popState(actual, memregs, counters);
	}
	cnt = actual.cnt;
	regLen = actual.regLen;
	main: while (wOffset <= end) {
	    matchHere: for (;;) {
		int memreg, cntreg;
		char c;
		if (term != null) {
		    switch (term.type) {
		    case Term.FIND: {
			final int jump = Matcher.find(data, i + term.distance, end, term.target); // don't eat the last
												  // match
			if (jump < 0) {
			    break main; // return false
			}
			i += jump;
			wOffset = i; // force window to move
			if (term.eat) {
			    if (i == end) {
				break;
			    }
			    i++;
			}
			term = term.next;
			continue matchHere;
		    }
		    case Term.FINDREG: {
			final MemReg mr = memregs[term.target.memreg];
			final int sampleOff = mr.in;
			final int sampleLen = mr.out - sampleOff;
			// if(sampleOff<0 || sampleLen<0) throw new Error("backreference used before
			// definition: \\"+term.memreg);
			/* @since 1.2 */
			if (sampleOff < 0 || sampleLen < 0) {
			    break;
			} else if (sampleLen == 0) {
			    term = term.next;
			    continue matchHere;
			}
			final int jump = Matcher.findReg(data, i + term.distance, sampleOff, sampleLen, term.target,
				end); // don't
			// eat the
			// last
			// match
			if (jump < 0) {
			    break main; // return false
			}
			i += jump;
			wOffset = i; // force window to move
			if (term.eat) {
			    i += sampleLen;
			    if (i > end) {
				break;
			    }
			}
			term = term.next;
			continue matchHere;
		    }
		    case Term.VOID:
			term = term.next;
			continue matchHere;
		    case Term.CHAR:
			// can only be 1-char-wide
			// \/
			if (i >= end || (re.caseless ? Category.caseFold(data[i]) : data[i]) != term.c) {
			    break;
			}
			i++;
			term = term.next;
			continue matchHere;
		    case Term.ANY_CHAR:
			// can only be 1-char-wide
			// \/
			if (i >= end) {
			    break;
			}
			i++;
			term = term.next;
			continue matchHere;
		    case Term.ANY_CHAR_NE:
			// can only be 1-char-wide
			// \/
			if (i >= end || (c = data[i]) == '\r' || c == '\n') {
			    break;
			}
			i++;
			term = term.next;
			continue matchHere;
		    case Term.END:
			if (i >= end) { // meets
			    term = term.next;
			    continue matchHere;
			}
			break;
		    case Term.END_EOL: // perl's $
			if (i >= end) { // meets
			    term = term.next;
			    continue matchHere;
			} else {
			    final boolean matches = i >= end | (i + 1 == end && data[i] == '\n')
				    | (i + 2 == end && data[i] == '\r' && data[i + 1] == '\n');
			    if (matches) {
				term = term.next;
				continue matchHere;
			    } else {
				break;
			    }
			}
		    case Term.LINE_END:
			if (i >= end) { // meets
			    term = term.next;
			    continue matchHere;
			} else {
			    /*
			     * if(((c=data[i])=='\r' || c=='\n') && (c=data[i-1])!='\r' && c!='\n'){
			     * term=term.next; continue matchHere; }
			     */
			    // 5 aug 2001
			    if ((c = data[i]) == '\n' || c == '\u0085' || c == '\u2028' || c == '\u2029'
				    || i < data.length - 1 && data[i + 1] == '\n' && c == '\r' || c == '\r') {
				term = term.next;
				continue matchHere;
			    }
			}
			break;
		    case Term.START: // Perl's "^"
			if (i == offset) { // meets
			    term = term.next;
			    continue matchHere;
			}
			// break;
			// changed on 27-04-2002
			// due to a side effect: if ALLOW_INCOMPLETE is enabled,
			// the anchorStart moves up to the end and succeeds
			// (see comments at the last lines of matchHere, ~line 1830)
			// Solution: if there are some entries on the stack ("^a|b$"),
			// try them; otherwise it's a final 'no'
			// if(top!=null) break;
			// else break main;
			// changed on 25-05-2002
			// rationale: if the term is startAnchor,
			// it's the root term by definition,
			// so if it doesn't match, the entire pattern
			// couldn't match too;
			// otherwise we could have the following problem:
			// "c|^a" against "abc" finds only "a"
			if (top != null) {
			    break;
			}
			if (term != Matcher.startAnchor) {
			    break;
			} else {
			    break main;
			}
		    case Term.LAST_MATCH_END:
			if (i == wEnd) { // meets
			    term = term.next;
			    continue matchHere;
			}
			break main; // return false
		    case Term.LINE_START:
			if (i == offset) { // meets
			    term = term.next;
			    continue matchHere;
			} else if (i < end) {
			    /*
			     * if(((c=data[i-1])=='\r' || c=='\n') && (c=data[i])!='\r' && c!='\n'){
			     * term=term.next; continue matchHere; }
			     */
			    // 5 aug 2001
			    // if((c=data[i-1])=='\r' || c=='\n'){ ??
			    if ((c = data[i - 1]) == '\n' || c == '\u0085' || c == '\u2028' || c == '\u2029'
				    || data[i] == '\n' && c == '\r' || c == '\r') {
				term = term.next;
				continue matchHere;
			    }
			}
			break;
		    case Term.BITSET: {
			// can only be 1-char-wide
			// \/
			if (i >= end) {
			    break;
			}
			c = re.caseless ? Category.caseFold(data[i]) : data[i];
			if (!(c <= 255 && term.bitset.get(c)) ^ term.inverse) {
			    break;
			}
			i++;
			term = term.next;
			continue matchHere;
		    }
		    case Term.BITSET2: {
			// can only be 1-char-wide
			// \/
			if (i >= end) {
			    break;
			}
			c = re.caseless ? Category.caseFold(data[i]) : data[i];
			final IntBitSet arr = term.bitset2[c >> 8];
			if (arr == null || !arr.get(c & 255) ^ term.inverse) {
			    break;
			}
			i++;
			term = term.next;
			continue matchHere;
		    }
		    case Term.BOUNDARY: {
			boolean ch1Meets = false, ch2Meets = false;
			final IntBitSet bitset = term.bitset;
			test1: {
			    final int j = i - 1;
			    // if(j<offset || j>=end) break test1;
			    if (j < offset) {
				break test1;
			    }
			    c = re.caseless ? Category.caseFold(data[j]) : data[j];
			    ch1Meets = c < 256 && bitset.get(c);
			}
			test2: {
			    // if(i<offset || i>=end) break test2;
			    if (i >= end) {
				break test2;
			    }
			    c = re.caseless ? Category.caseFold(data[i]) : data[i];
			    ch2Meets = c < 256 && bitset.get(c);
			}
			if (ch1Meets ^ ch2Meets ^ term.inverse) { // meets
			    term = term.next;
			    continue matchHere;
			} else {
			    break;
			}
		    }
		    case Term.UBOUNDARY: {
			boolean ch1Meets = false, ch2Meets = false;
			final IntBitSet[] bitset2 = term.bitset2;
			test1: {
			    final int j = i - 1;
			    // if(j<offset || j>=end) break test1;
			    if (j < offset) {
				break test1;
			    }
			    c = re.caseless ? Category.caseFold(data[j]) : data[j];
			    final IntBitSet bits = bitset2[c >> 8];
			    ch1Meets = bits != null && bits.get(c & 0xff);
			}
			test2: {
			    // if(i<offset || i>=end) break test2;
			    if (i >= end) {
				break test2;
			    }
			    c = re.caseless ? Category.caseFold(data[i]) : data[i];
			    final IntBitSet bits = bitset2[c >> 8];
			    ch2Meets = bits != null && bits.get(c & 0xff);
			}
			if (ch1Meets ^ ch2Meets ^ term.inverse) { // is boundary ^ inv
			    term = term.next;
			    continue matchHere;
			} else {
			    break;
			}
		    }
		    case Term.DIRECTION: {
			boolean ch1Meets = false, ch2Meets = false;
			final IntBitSet bitset = term.bitset;
			final boolean inv = term.inverse;
			final int j = i - 1;
			// if(j>=offset && j<end){
			if (j >= offset) {
			    c = re.caseless ? Category.caseFold(data[j]) : data[j];
			    ch1Meets = c < 256 && bitset.get(c);
			}
			if (ch1Meets ^ inv) {
			    break;
			}
			// if(i>=offset && i<end){
			if (i < end) {
			    c = re.caseless ? Category.caseFold(data[i]) : data[i];
			    ch2Meets = c < 256 && bitset.get(c);
			}
			if (!ch2Meets ^ inv) {
			    break;
			}
			term = term.next;
			continue matchHere;
		    }
		    case Term.UDIRECTION: {
			boolean ch1Meets = false, ch2Meets = false;
			final IntBitSet[] bitset2 = term.bitset2;
			final boolean inv = term.inverse;
			final int j = i - 1;
			// if(j>=offset && j<end){
			if (j >= offset) {
			    c = re.caseless ? Category.caseFold(data[j]) : data[j];
			    final IntBitSet bits = bitset2[c >> 8];
			    ch1Meets = bits != null && bits.get(c & 0xff);
			}
			if (ch1Meets ^ inv) {
			    break;
			}
			// if(i>=offset && i<end){
			if (i < end) {
			    c = re.caseless ? Category.caseFold(data[i]) : data[i];
			    final IntBitSet bits = bitset2[c >> 8];
			    ch2Meets = bits != null && bits.get(c & 0xff);
			}
			if (!ch2Meets ^ inv) {
			    break;
			}
			term = term.next;
			continue matchHere;
		    }
		    case Term.REG:
		    case Term.REG_I: {
			if (term.memreg >= memregs.length) {
			    break;
			}
			final MemReg mr = memregs[term.memreg];
			final int sampleOffset = mr.in;
			final int sampleOutside = mr.out;
			int rLen;
			if (sampleOffset < 0 || (rLen = sampleOutside - sampleOffset) < 0) {
			    break;
			} else if (rLen == 0) {
			    term = term.next;
			    continue matchHere;
			}
			// don't prevent us from reaching the 'end'
			if (i + rLen > end) {
			    break;
			}
			if (Matcher.compareRegions(data, sampleOffset, i, rLen, end, term)) {
			    i += rLen;
			    term = term.next;
			    continue matchHere;
			}
			break;
		    }
		    /*
		     * case Term.REG_I: { MemReg mr = memregs[term.memreg]; int sampleOffset =
		     * mr.in; int sampleOutside = mr.out; int rLen; if (sampleOffset < 0 || (rLen =
		     * sampleOutside - sampleOffset) < 0) { break; } else if (rLen == 0) { term =
		     * term.next; continue matchHere; }
		     *
		     * // don't prevent us from reaching the 'end' if ((i + rLen) > end) break;
		     *
		     * if (compareRegionsI(data, sampleOffset, i, rLen, end)) { i += rLen; term =
		     * term.next; continue matchHere; } break; }
		     */
		    case Term.REPEAT_0_INF: {
			// i+=(cnt=repeat(data,i,end,term.target));
			if ((cnt = Matcher.repeat(data, i, end, term.target)) <= 0) {
			    term = term.next;
			    continue;
			}
			i += cnt;
			// branch out the backtracker (that is term.failNext, see Term.make*())
			actual.cnt = cnt;
			actual.term = term.failNext;
			actual.index = i;
			actual = (top = actual).on;
			if (actual == null) {
			    actual = new SearchEntry();
			    top.on = actual;
			    actual.sub = top;
			}
			term = term.next;
			continue;
		    }
		    case Term.REPEAT_MIN_INF: {
			cnt = Matcher.repeat(data, i, end, term.target);
			if (cnt < term.minCount) {
			    break;
			}
			i += cnt;
			// branch out the backtracker (that is term.failNext, see Term.make*())
			actual.cnt = cnt;
			actual.term = term.failNext;
			actual.index = i;
			actual = (top = actual).on;
			if (actual == null) {
			    actual = new SearchEntry();
			    top.on = actual;
			    actual.sub = top;
			}
			term = term.next;
			continue;
		    }
		    case Term.REPEAT_MIN_MAX: {
			final int out2 = i + term.maxCount;
			cnt = Matcher.repeat(data, i, end < out2 ? end : out2, term.target);
			if (cnt < term.minCount) {
			    break;
			}
			i += cnt;
			// branch out the backtracker (that is term.failNext, see Term.make*())
			actual.cnt = cnt;
			actual.term = term.failNext;
			actual.index = i;
			actual = (top = actual).on;
			if (actual == null) {
			    actual = new SearchEntry();
			    top.on = actual;
			    actual.sub = top;
			}
			term = term.next;
			continue;
		    }
		    case Term.REPEAT_REG_MIN_INF: {
			final MemReg mr = memregs[term.memreg];
			final int sampleOffset = mr.in;
			final int sampleOutside = mr.out;
			/* @since 1.2 */
			int bitset;
			if (sampleOffset < 0 || (bitset = sampleOutside - sampleOffset) < 0) {
			    break;
			} else if (bitset == 0) {
			    term = term.next;
			    continue matchHere;
			}
			cnt = 0;
			while (Matcher.compareRegions(data, i, sampleOffset, bitset, end, term)) {
			    cnt++;
			    i += bitset;
			}
			if (cnt < term.minCount) {
			    break;
			}
			actual.cnt = cnt;
			actual.term = term.failNext;
			actual.index = i;
			actual.regLen = bitset;
			actual = (top = actual).on;
			if (actual == null) {
			    actual = new SearchEntry();
			    top.on = actual;
			    actual.sub = top;
			}
			term = term.next;
			continue;
		    }
		    case Term.REPEAT_REG_MIN_MAX: {
			final MemReg mr = memregs[term.memreg];
			final int sampleOffset = mr.in;
			final int sampleOutside = mr.out;
			/* @since 1.2 */
			int bitset;
			if (sampleOffset < 0 || (bitset = sampleOutside - sampleOffset) < 0) {
			    break;
			} else if (bitset == 0) {
			    term = term.next;
			    continue matchHere;
			}
			cnt = 0;
			int countBack = term.maxCount;
			while (countBack > 0 && Matcher.compareRegions(data, i, sampleOffset, bitset, end, term)) {
			    cnt++;
			    i += bitset;
			    countBack--;
			}
			if (cnt < term.minCount) {
			    break;
			}
			actual.cnt = cnt;
			actual.term = term.failNext;
			actual.index = i;
			actual.regLen = bitset;
			actual = (top = actual).on;
			if (actual == null) {
			    actual = new SearchEntry();
			    top.on = actual;
			    actual.sub = top;
			}
			term = term.next;
			continue;
		    }
		    case Term.BACKTRACK_0:
			cnt = actual.cnt;
			if (cnt > 0) {
			    cnt--;
			    i--;
			    actual.cnt = cnt;
			    actual.index = i;
			    actual.term = term;
			    actual = (top = actual).on;
			    if (actual == null) {
				actual = new SearchEntry();
				top.on = actual;
				actual.sub = top;
			    }
			    term = term.next;
			    continue;
			} else {
			    break;
			}
		    case Term.BACKTRACK_MIN:
			cnt = actual.cnt;
			if (cnt > term.minCount) {
			    cnt--;
			    i--;
			    actual.cnt = cnt;
			    actual.index = i;
			    actual.term = term;
			    actual = (top = actual).on;
			    if (actual == null) {
				actual = new SearchEntry();
				top.on = actual;
				actual.sub = top;
			    }
			    term = term.next;
			    continue;
			} else {
			    break;
			}
		    case Term.BACKTRACK_FIND_MIN: {
			cnt = actual.cnt;
			int minCnt;
			if (cnt > (minCnt = term.minCount)) {
			    final int start = i + term.distance;
			    if (start > end) {
				final int exceed = start - end;
				cnt -= exceed;
				if (cnt <= minCnt) {
				    break;
				}
				i -= exceed;
			    }
			    final int back = Matcher.findBack(data, i + term.distance, cnt - minCnt, term.target);
			    if (back < 0) {
				break;
			    }
			    // cnt-=back;
			    // i-=back;
			    if ((cnt -= back) <= minCnt) {
				i -= back;
				if (term.eat) {
				    i++;
				}
				term = term.next;
				continue;
			    }
			    i -= back;
			    actual.cnt = cnt;
			    actual.index = i;
			    if (term.eat) {
				i++;
			    }
			    actual.term = term;
			    actual = (top = actual).on;
			    if (actual == null) {
				actual = new SearchEntry();
				top.on = actual;
				actual.sub = top;
			    }
			    term = term.next;
			    continue;
			} else {
			    break;
			}
		    }
		    case Term.BACKTRACK_FINDREG_MIN: {
			cnt = actual.cnt;
			int minCnt;
			if (cnt > (minCnt = term.minCount)) {
			    final int start = i + term.distance;
			    if (start > end) {
				final int exceed = start - end;
				cnt -= exceed;
				if (cnt <= minCnt) {
				    break;
				}
				i -= exceed;
			    }
			    final MemReg mr = memregs[term.target.memreg];
			    final int sampleOff = mr.in;
			    final int sampleLen = mr.out - sampleOff;
			    /* @since 1.2 */
			    int back;
			    if (sampleOff < 0 || sampleLen < 0) {
				// the group is not def., as in the case of '(\w+)\1'
				// treat as usual BACKTRACK_MIN
				cnt--;
				i--;
				actual.cnt = cnt;
				actual.index = i;
				actual.term = term;
				actual = (top = actual).on;
				if (actual == null) {
				    actual = new SearchEntry();
				    top.on = actual;
				    actual.sub = top;
				}
				term = term.next;
				continue;
			    } else if (sampleLen == 0) {
				back = -1;
			    } else {
				back = Matcher.findBackReg(data, i + term.distance, sampleOff, sampleLen, cnt - minCnt,
					term.target, end);
				if (back < 0) {
				    break;
				}
			    }
			    cnt -= back;
			    i -= back;
			    actual.cnt = cnt;
			    actual.index = i;
			    if (term.eat) {
				i += sampleLen;
			    }
			    actual.term = term;
			    actual = (top = actual).on;
			    if (actual == null) {
				actual = new SearchEntry();
				top.on = actual;
				actual.sub = top;
			    }
			    term = term.next;
			    continue;
			} else {
			    break;
			}
		    }
		    case Term.BACKTRACK_REG_MIN:
			cnt = actual.cnt;
			if (cnt > term.minCount) {
			    regLen = actual.regLen;
			    cnt--;
			    i -= regLen;
			    actual.cnt = cnt;
			    actual.index = i;
			    actual.term = term;
			    // actual.regLen=regLen;
			    actual = (top = actual).on;
			    if (actual == null) {
				actual = new SearchEntry();
				top.on = actual;
				actual.sub = top;
			    }
			    term = term.next;
			    continue;
			} else {
			    break;
			}
		    case Term.GROUP_IN: {
			memreg = term.memreg;
			// memreg=0 is a regex itself; we don't need to handle it
			// because regex bounds already are in wOffset and wEnd
			if (memreg > 0) {
			    memregs[memreg].tmp = i; // assume
			}
			term = term.next;
			continue;
		    }
		    case Term.GROUP_OUT:
			memreg = term.memreg;
			// see above
			if (memreg > 0) {
			    final MemReg mr = memregs[memreg];
			    SearchEntry.saveMemregState(top != null ? top : defaultEntry, memreg, mr);
			    mr.in = mr.tmp; // commit
			    mr.out = i;
			}
			term = term.next;
			continue;
		    case Term.PLOOKBEHIND_IN: {
			final int tmp = i - term.distance;
			if (tmp < offset) {
			    break;
			}
			final LAEntry le = lookaheads[term.lookaheadId];
			le.index = i;
			i = tmp;
			le.actual = actual;
			le.top = top;
			term = term.next;
			continue;
		    }
		    case Term.INDEPENDENT_IN:
		    case Term.PLOOKAHEAD_IN: {
			final LAEntry le = lookaheads[term.lookaheadId];
			le.index = i;
			le.actual = actual;
			le.top = top;
			term = term.next;
			continue;
		    }
		    case Term.LOOKBEHIND_CONDITION_OUT:
		    case Term.LOOKAHEAD_CONDITION_OUT:
		    case Term.PLOOKAHEAD_OUT:
		    case Term.PLOOKBEHIND_OUT: {
			final LAEntry le = lookaheads[term.lookaheadId];
			i = le.index;
			actual = le.actual;
			top = le.top;
			term = term.next;
			continue;
		    }
		    case Term.INDEPENDENT_OUT: {
			final LAEntry le = lookaheads[term.lookaheadId];
			actual = le.actual;
			top = le.top;
			term = term.next;
			continue;
		    }
		    case Term.NLOOKBEHIND_IN: {
			final int tmp = i - term.distance;
			if (tmp < offset) {
			    term = term.failNext;
			    continue;
			}
			final LAEntry le = lookaheads[term.lookaheadId];
			le.actual = actual;
			le.top = top;
			actual.term = term.failNext;
			actual.index = i;
			i = tmp;
			actual = (top = actual).on;
			if (actual == null) {
			    actual = new SearchEntry();
			    top.on = actual;
			    actual.sub = top;
			}
			term = term.next;
			continue;
		    }
		    case Term.NLOOKAHEAD_IN: {
			final LAEntry le = lookaheads[term.lookaheadId];
			le.actual = actual;
			le.top = top;
			actual.term = term.failNext;
			actual.index = i;
			actual = (top = actual).on;
			if (actual == null) {
			    actual = new SearchEntry();
			    top.on = actual;
			    actual.sub = top;
			}
			term = term.next;
			continue;
		    }
		    case Term.NLOOKBEHIND_OUT:
		    case Term.NLOOKAHEAD_OUT: {
			final LAEntry le = lookaheads[term.lookaheadId];
			actual = le.actual;
			top = le.top;
			break;
		    }
		    case Term.LOOKBEHIND_CONDITION_IN: {
			final int tmp = i - term.distance;
			if (tmp < offset) {
			    term = term.failNext;
			    continue;
			}
			final LAEntry le = lookaheads[term.lookaheadId];
			le.index = i;
			le.actual = actual;
			le.top = top;
			actual.term = term.failNext;
			actual.index = i;
			actual = (top = actual).on;
			if (actual == null) {
			    actual = new SearchEntry();
			    top.on = actual;
			    actual.sub = top;
			}
			i = tmp;
			term = term.next;
			continue;
		    }
		    case Term.LOOKAHEAD_CONDITION_IN: {
			final LAEntry le = lookaheads[term.lookaheadId];
			le.index = i;
			le.actual = actual;
			le.top = top;
			actual.term = term.failNext;
			actual.index = i;
			actual = (top = actual).on;
			if (actual == null) {
			    actual = new SearchEntry();
			    top.on = actual;
			    actual.sub = top;
			}
			term = term.next;
			continue;
		    }
		    case Term.MEMREG_CONDITION: {
			final MemReg mr = memregs[term.memreg];
			final int sampleOffset = mr.in;
			final int sampleOutside = mr.out;
			if (sampleOffset >= 0 && sampleOutside >= 0 && sampleOutside >= sampleOffset) {
			    term = term.next;
			} else {
			    term = term.failNext;
			}
			continue;
		    }
		    case Term.BRANCH_STORE_CNT_AUX1:
			actual.regLen = regLen;
		    case Term.BRANCH_STORE_CNT:
			actual.cnt = cnt;
		    case Term.BRANCH:
			actual.term = term.failNext;
			actual.index = i;
			actual = (top = actual).on;
			if (actual == null) {
			    actual = new SearchEntry();
			    top.on = actual;
			    actual.sub = top;
			}
			term = term.next;
			continue;
		    case Term.SUCCESS:
			if (!matchEnd || i == end) {
			    this.wOffset = memregs[0].in = wOffset;
			    this.wEnd = memregs[0].out = i;
			    this.top = top;
			    return true;
			} else {
			    break;
			}
		    case Term.CNT_SET_0:
			cnt = 0;
			term = term.next;
			continue;
		    case Term.CNT_INC:
			cnt++;
			term = term.next;
			continue;
		    case Term.CNT_GT_EQ:
			if (cnt >= term.maxCount) {
			    term = term.next;
			    continue;
			} else {
			    break;
			}
		    case Term.READ_CNT_LT:
			cnt = actual.cnt;
			if (cnt < term.maxCount) {
			    term = term.next;
			    continue;
			} else {
			    break;
			}
		    case Term.CRSTORE_CRINC: {
			int cntvalue = counters[cntreg = term.cntreg];
			SearchEntry.saveCntState(top != null ? top : defaultEntry, cntreg, cntvalue);
			counters[cntreg] = ++cntvalue;
			term = term.next;
			continue;
		    }
		    case Term.CR_SET_0:
			counters[term.cntreg] = 0;
			term = term.next;
			continue;
		    case Term.CR_LT:
			if (counters[term.cntreg] < term.maxCount) {
			    term = term.next;
			    continue;
			} else {
			    break;
			}
		    case Term.CR_GT_EQ:
			if (counters[term.cntreg] >= term.maxCount) {
			    term = term.next;
			    continue;
			} else {
			    break;
			}
		    default:
			throw new Error("unknown term type: " + term.type);
		    }
		} else {
		    this.wOffset = memregs[0].in = wOffset;
		    this.wEnd = memregs[0].out = i;
		    this.top = top;
		    return true;
		}
		if (allowIncomplete && i == end) {
		    // an attempt to implement matchesPrefix()
		    // not sure it's a good way
		    // 27-04-2002: just as expected,
		    // the side effect was found (and POSSIBLY fixed);
		    // see the case Term.START
		    // newly added June-18-2016
		    this.wOffset = memregs[0].in = wOffset;
		    this.wEnd = memregs[0].out = i;
		    this.top = top;
		    return true;
		}
		if (top == null) {
		    break;
		}
		// pop the stack
		top = (actual = top).sub;
		term = actual.term;
		i = actual.index;
		if (actual.isState) {
		    SearchEntry.popState(actual, memregs, counters);
		}
	    }
	    if (defaultEntry.isState) {
		SearchEntry.popState(defaultEntry, memregs, counters);
	    }
	    term = root;
	    // wOffset++;
	    // i=wOffset;
	    i = ++wOffset;
	}
	this.wOffset = wOffset;
	this.top = top;
	return false;
    }

    private static boolean compareRegions(final char[] arr, final int off1, final int off2, final int len,
	    final int out, final Term opts) {
	if (opts.mode_reverse) {
	    return Matcher.compareRegionsReverse(arr, off1, off2, len, out, opts.mode_insensitive, opts.mode_bracket);
	} else {
	    return Matcher.compareRegionsForward(arr, off1, off2, len, out, opts.mode_insensitive, opts.mode_bracket);
	}
    }

    private static boolean compareRegionsForward(final char[] arr, final int off1, final int off2, final int len,
	    final int out, final boolean insensitive, final boolean bracket) {
	int p1 = off1 + len - 1;
	int p2 = off2 + len - 1;
	if (p1 >= out || p2 >= out) {
	    return false;
	}
	char a, b;
	for (int c = len; c > 0; c--, p1--, p2--) {
	    a = arr[p1];
	    b = arr[p2];
	    if (insensitive) {
		a = Category.caseFold(a);
		b = Category.caseFold(b);
	    }
	    if (bracket) {
		b = Category.matchBracket(b);
	    }
	    if (a != b) {
		return false;
	    }
	}
	return true;
    }

    private static boolean compareRegionsReverse(final char[] arr, final int off1, final int off2, final int len,
	    final int out, final boolean insensitive, final boolean bracket) {
	int p1 = off1 + len - 1;
	int p2 = off2;
	if (p1 >= out || p2 >= out) {
	    return false;
	}
	char a, b;
	for (int c = len; c > 0 && p2 < out; c--, p1--, p2++) {
	    a = arr[p1];
	    b = arr[p2];
	    if (insensitive) {
		a = Category.caseFold(a);
		b = Category.caseFold(b);
	    }
	    if (bracket) {
		b = Category.matchBracket(b);
	    }
	    if (a != b) {
		return false;
	    }
	}
	return true;
    }

    // repeat while matches
    private static int repeat(final char[] data, final int off, final int out, final Term term) {
	switch (term.type) {
	case Term.CHAR: {
	    final char c = term.c;
	    int i = off;
	    while (i < out) {
		if (data[i] != c) {
		    break;
		}
		i++;
	    }
	    return i - off;
	}
	case Term.ANY_CHAR: {
	    return out - off;
	}
	case Term.ANY_CHAR_NE: {
	    int i = off;
	    char c;
	    while (i < out) {
		if ((c = data[i]) == '\r' || c == '\n') {
		    break;
		}
		i++;
	    }
	    return i - off;
	}
	case Term.BITSET: {
	    final IntBitSet arr = term.bitset;
	    int i = off;
	    char c;
	    if (term.inverse) {
		while (i < out) {
		    if ((c = data[i]) <= 255 && arr.get(c)) {
			break;
		    } else {
			i++;
		    }
		}
	    } else {
		while (i < out) {
		    if ((c = data[i]) <= 255 && arr.get(c)) {
			i++;
		    } else {
			break;
		    }
		}
	    }
	    return i - off;
	}
	case Term.BITSET2: {
	    int i = off;
	    final IntBitSet[] bitset2 = term.bitset2;
	    char c;
	    if (term.inverse) {
		while (i < out) {
		    final IntBitSet arr = bitset2[(c = data[i]) >> 8];
		    if (arr != null && arr.get(c & 0xff)) {
			break;
		    } else {
			i++;
		    }
		}
	    } else {
		while (i < out) {
		    final IntBitSet arr = bitset2[(c = data[i]) >> 8];
		    if (arr != null && arr.get(c & 0xff)) {
			i++;
		    } else {
			break;
		    }
		}
	    }
	    return i - off;
	}
	}
	throw new Error("this kind of term can't be quantified:" + term.type);
    }

    // repeat while doesn't match
    private static int find(final char[] data, final int off, final int out, final Term term) {
	if (off >= out) {
	    return -1;
	}
	switch (term.type) {
	case Term.CHAR: {
	    final char c = term.c;
	    int i = off;
	    while (i < out) {
		if (data[i] == c) {
		    break;
		}
		i++;
	    }
	    return i - off;
	}
	case Term.BITSET: {
	    final IntBitSet arr = term.bitset;
	    int i = off;
	    char c;
	    if (!term.inverse) {
		while (i < out) {
		    if ((c = data[i]) <= 255 && arr.get(c)) {
			break;
		    } else {
			i++;
		    }
		}
	    } else {
		while (i < out) {
		    if ((c = data[i]) <= 255 && arr.get(c)) {
			i++;
		    } else {
			break;
		    }
		}
	    }
	    return i - off;
	}
	case Term.BITSET2: {
	    int i = off;
	    final IntBitSet[] bitset2 = term.bitset2;
	    char c;
	    if (!term.inverse) {
		while (i < out) {
		    final IntBitSet arr = bitset2[(c = data[i]) >> 8];
		    if (arr != null && arr.get(c & 0xff)) {
			break;
		    } else {
			i++;
		    }
		}
	    } else {
		while (i < out) {
		    final IntBitSet arr = bitset2[(c = data[i]) >> 8];
		    if (arr != null && arr.get(c & 0xff)) {
			i++;
		    } else {
			break;
		    }
		}
	    }
	    return i - off;
	}
	}
	throw new IllegalArgumentException("can't seek this kind of term:" + term.type);
    }

    private static int findReg(final char[] data, final int off, final int regOff, final int regLen, final Term term,
	    final int out) {
	if (off >= out) {
	    return -1;
	}
	int i = off;
	if (term.type == Term.REG || term.type == Term.REG_I) {
	    while (i < out) {
		if (Matcher.compareRegions(data, i, regOff, regLen, out, term)) {
		    break;
		}
		i++;
	    }
	} else {
	    throw new IllegalArgumentException("wrong findReg() target:" + term.type);
	}
	return off - i;
    }

    private static int findBack(final char[] data, final int off, final int maxCount, final Term term) {
	switch (term.type) {
	case Term.CHAR: {
	    final char c = term.c;
	    int i = off;
	    final int iMin = off - maxCount;
	    for (;;) {
		if (data[--i] == c) {
		    break;
		}
		if (i <= iMin) {
		    return -1;
		}
	    }
	    return off - i;
	}
	case Term.BITSET: {
	    final IntBitSet arr = term.bitset;
	    int i = off;
	    char c;
	    final int iMin = off - maxCount;
	    if (!term.inverse) {
		for (;;) {
		    if ((c = data[--i]) <= 255 && arr.get(c)) {
			break;
		    }
		    if (i <= iMin) {
			return -1;
		    }
		}
	    } else {
		for (;;) {
		    if ((c = data[--i]) > 255 || !arr.get(c)) {
			break;
		    }
		    if (i <= iMin) {
			return -1;
		    }
		}
	    }
	    return off - i;
	}
	case Term.BITSET2: {
	    final IntBitSet[] bitset2 = term.bitset2;
	    int i = off;
	    char c;
	    final int iMin = off - maxCount;
	    if (!term.inverse) {
		for (;;) {
		    final IntBitSet arr = bitset2[(c = data[--i]) >> 8];
		    if (arr != null && arr.get(c & 0xff)) {
			break;
		    }
		    if (i <= iMin) {
			return -1;
		    }
		}
	    } else {
		for (;;) {
		    final IntBitSet arr = bitset2[(c = data[--i]) >> 8];
		    if (arr == null || arr.get(c & 0xff)) {
			break;
		    }
		    if (i <= iMin) {
			return -1;
		    }
		}
	    }
	    return off - i;
	}
	}
	throw new IllegalArgumentException("can't find this kind of term:" + term.type);
    }

    private static int findBackReg(final char[] data, final int off, int regOff, int regLen, final int maxCount,
	    final Term term, final int out) {
	// assume that the cases when regLen==0 or maxCount==0 are handled by caller
	int i = off;
	final int iMin = off - maxCount;
	if (term.type == Term.REG || term.type == Term.REG_I) {
	    /* @since 1.2 */
	    final char first = data[regOff];
	    regOff++;
	    regLen--;
	    for (;;) {
		i--;
		if (data[i] == first && Matcher.compareRegions(data, i + 1, regOff, regLen, out, term)) {
		    break;
		}
		if (i <= iMin) {
		    return -1;
		}
	    }
	} /*
	   * else if (term.type == Term.REG_I) { char c, firstChar =
	   * Category.caseFold(data[regOff]); regOff++; regLen--; for (; ; ) { i--; if
	   * (((c = Category.caseFold(data[i])) == firstChar) && compareRegionsI(data, i +
	   * 1, regOff, regLen, out)) break; if (i <= iMin) return -1; } return off - i; }
	   */ else {
	    throw new IllegalArgumentException("wrong findBackReg() target type :" + term.type);
	}
	return off - i;
    }

    private String toString_d() {
	final StringBuilder s = new StringBuilder();
	s.append("counters: ");
	s.append(this.counters == null ? 0 : this.counters.length);
	s.append("\r\nmemregs: ");
	s.append(this.memregs.length);
	for (int i = 0; i < this.memregs.length; i++) {
	    if (this.memregs[i].in < 0 || this.memregs[i].out < 0) {
		s.append("\r\n #").append(i).append(": [INVALID]");
	    } else {
		s.append("\r\n #").append(i).append(": [").append(this.memregs[i].in).append(",")
			.append(this.memregs[i].out).append("](\"")
			.append(this.getString(this.memregs[i].in, this.memregs[i].out)).append("\")");
	    }
	}
	s.append("\r\ndata: ");
	if (this.data != null) {
	    s.append(this.data.length);
	} else {
	    s.append("[none]");
	}
	s.append("\r\noffset: ");
	s.append(this.offset);
	s.append("\r\nend: ");
	s.append(this.end);
	s.append("\r\nwOffset: ");
	s.append(this.wOffset);
	s.append("\r\nwEnd: ");
	s.append(this.wEnd);
	s.append("\r\nregex: ");
	s.append(this.re);
	return s.toString();
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final Matcher matcher = (Matcher) o;
	if (this.counterCount != matcher.counterCount) {
	    return false;
	}
	if (this.memregCount != matcher.memregCount) {
	    return false;
	}
	if (this.lookaheadCount != matcher.lookaheadCount) {
	    return false;
	}
	if (this.offset != matcher.offset) {
	    return false;
	}
	if (this.end != matcher.end) {
	    return false;
	}
	if (this.wOffset != matcher.wOffset) {
	    return false;
	}
	if (this.wEnd != matcher.wEnd) {
	    return false;
	}
	if (this.shared != matcher.shared) {
	    return false;
	}
	if (this.called != matcher.called) {
	    return false;
	}
	if (this.minQueueLength != matcher.minQueueLength) {
	    return false;
	}
	if (this.cacheOffset != matcher.cacheOffset) {
	    return false;
	}
	if (this.cacheLength != matcher.cacheLength) {
	    return false;
	}
	if (this.re != null ? !this.re.equals(matcher.re) : matcher.re != null) {
	    return false;
	}
	if (!Arrays.equals(this.counters, matcher.counters)) {
	    return false;
	}
	// Probably incorrect - comparing Object[] arrays with Arrays.equals
	if (!Arrays.equals(this.memregs, matcher.memregs)) {
	    return false;
	}
	// Probably incorrect - comparing Object[] arrays with Arrays.equals
	if (!Arrays.equals(this.lookaheads, matcher.lookaheads)) {
	    return false;
	}
	if (!Arrays.equals(this.data, matcher.data)) {
	    return false;
	}
	if (this.top != null ? !this.top.equals(matcher.top) : matcher.top != null) {
	    return false;
	}
	if (this.first != null ? !this.first.equals(matcher.first) : matcher.first != null) {
	    return false;
	}
	if (this.defaultEntry != null ? !this.defaultEntry.equals(matcher.defaultEntry)
		: matcher.defaultEntry != null) {
	    return false;
	}
	if (this.cache != null ? !this.cache.equals(matcher.cache) : matcher.cache != null) {
	    return false;
	}
	return this.prefixBounds != null ? this.prefixBounds.equals(matcher.prefixBounds)
		: matcher.prefixBounds == null
			&& (this.suffixBounds != null ? this.suffixBounds.equals(matcher.suffixBounds)
				: matcher.suffixBounds == null
					&& (this.targetBounds != null ? this.targetBounds.equals(matcher.targetBounds)
						: matcher.targetBounds == null));
    }

    @Override
    public int hashCode() {
	int result = this.re != null ? this.re.hashCode() : 0;
	result = 31 * result + Arrays.hashCode(this.counters);
	result = 31 * result + Arrays.hashCode(this.memregs);
	result = 31 * result + Arrays.hashCode(this.lookaheads);
	result = 31 * result + this.counterCount;
	result = 31 * result + this.memregCount;
	result = 31 * result + this.lookaheadCount;
	result = 31 * result + Arrays.hashCode(this.data);
	result = 31 * result + this.offset;
	result = 31 * result + this.end;
	result = 31 * result + this.wOffset;
	result = 31 * result + this.wEnd;
	result = 31 * result + (this.shared ? 1 : 0);
	result = 31 * result + (this.top != null ? this.top.hashCode() : 0);
	result = 31 * result + (this.first != null ? this.first.hashCode() : 0);
	result = 31 * result + (this.defaultEntry != null ? this.defaultEntry.hashCode() : 0);
	result = 31 * result + (this.called ? 1 : 0);
	result = 31 * result + this.minQueueLength;
	result = 31 * result + (this.cache != null ? this.cache.hashCode() : 0);
	result = 31 * result + this.cacheOffset;
	result = 31 * result + this.cacheLength;
	result = 31 * result + (this.prefixBounds != null ? this.prefixBounds.hashCode() : 0);
	result = 31 * result + (this.suffixBounds != null ? this.suffixBounds.hashCode() : 0);
	result = 31 * result + (this.targetBounds != null ? this.targetBounds.hashCode() : 0);
	return result;
    }

    /**
     * Replaces the first match this Matcher can find with replacement, as
     * interpreted by PerlSubstitution (so $1 refers to the first group and so on).
     * Advances the search position for this Matcher, so it can also be used to
     * repeatedly replace the next match when called successively.
     *
     * @param replacement the String to replace the first match with
     * @return this Matcher's String it operated on, after a replacement
     */
    public String replaceFirst(final String replacement) {
	final TextBuffer tb = Replacer.wrap(new StringBuilder(this.data.length));
	Replacer.replace(this, new PerlSubstitution(replacement), tb, 1);
	return tb.toString();
    }

    /**
     * Replaces the first amount matches this Matcher can find with replacement, as
     * interpreted by PerlSubstitution (so $1 refers to the first group and so on).
     * Advances the search position for this Matcher, so it can also be used to
     * repeatedly replace the next amount matches when called successively.
     *
     * @param replacement the String to replace the first match with
     * @param amount      the number of replacements to perform
     * @return this Matcher's String it operated on, after replacements
     */
    public String replaceAmount(final String replacement, final int amount) {
	final TextBuffer tb = Replacer.wrap(new StringBuilder(this.data.length));
	Replacer.replace(this, new PerlSubstitution(replacement), tb, amount);
	return tb.toString();
    }

    /**
     * Replaces all matches this Matcher can find with replacement, as interpreted
     * by PerlSubstitution (so $1 refers to the first group and so on).
     *
     * @param replacement the String to replace the first match with
     * @return this Matcher's String it operated on, after replacements
     */
    public String replaceAll(final String replacement) {
	final TextBuffer tb = Replacer.wrap(new StringBuilder(this.data.length));
	Replacer.replace(this, new PerlSubstitution(replacement), tb);
	return tb.toString();
    }

    /**
     * Replaces the first match this Matcher can find with replacement, as
     * interpreted by PerlSubstitution (so $1 refers to the first group and so on).
     * Advances the search position for this Matcher, so it can also be used to
     * repeatedly replace the next match when called successively.
     *
     * @param replacement the String to replace the first match with
     * @return this Matcher's String it operated on, after a replacement
     */
    public String replaceFirst(final Substitution replacement) {
	final TextBuffer tb = Replacer.wrap(new StringBuilder(this.data.length));
	Replacer.replace(this, replacement, tb, 1);
	return tb.toString();
    }

    /**
     * Replaces the first amount matches this Matcher can find with replacement, as
     * interpreted by PerlSubstitution (so $1 refers to the first group and so on).
     * Advances the search position for this Matcher, so it can also be used to
     * repeatedly replace the next amount matches when called successively.
     *
     * @param replacement the String to replace the first match with
     * @param amount      the number of replacements to perform
     * @return this Matcher's String it operated on, after replacements
     */
    public String replaceAmount(final Substitution replacement, final int amount) {
	final TextBuffer tb = Replacer.wrap(new StringBuilder(this.data.length));
	Replacer.replace(this, replacement, tb, amount);
	return tb.toString();
    }

    /**
     * Replaces all matches this Matcher can find with replacement, as interpreted
     * by PerlSubstitution (so $1 refers to the first group and so on).
     *
     * @param replacement the String to replace the first match with
     * @return this Matcher's String it operated on, after replacements
     */
    public String replaceAll(final Substitution replacement) {
	final TextBuffer tb = Replacer.wrap(new StringBuilder(this.data.length));
	Replacer.replace(this, replacement, tb);
	return tb.toString();
    }
}

class SearchEntry implements Serializable {
    private static final long serialVersionUID = -3628346657932720807L;
    Term term;
    int index;
    int cnt;
    int regLen;
    boolean isState;
    SearchEntry sub, on;

    private static class MState {
	int index, in, out;
	MState next, prev;
    }

    private static class CState {
	int index, value;
	CState next, prev;
    }

    private MState mHead, mCurrent;
    private CState cHead, cCurrent;

    static void saveMemregState(final SearchEntry entry, final int memreg, final MemReg mr) {
	entry.isState = true;
	MState current = entry.mCurrent;
	if (current == null) {
	    final MState head = entry.mHead;
	    if (head == null) {
		entry.mHead = entry.mCurrent = current = new MState();
	    } else {
		current = head;
	    }
	} else {
	    MState next = current.next;
	    if (next == null) {
		current.next = next = new MState();
		next.prev = current;
	    }
	    current = next;
	}
	current.index = memreg;
	current.in = mr.in;
	current.out = mr.out;
	entry.mCurrent = current;
    }

    static void saveCntState(final SearchEntry entry, final int cntreg, final int value) {
	entry.isState = true;
	CState current = entry.cCurrent;
	if (current == null) {
	    final CState head = entry.cHead;
	    if (head == null) {
		entry.cHead = entry.cCurrent = current = new CState();
	    } else {
		current = head;
	    }
	} else {
	    CState next = current.next;
	    if (next == null) {
		current.next = next = new CState();
		next.prev = current;
	    }
	    current = next;
	}
	current.index = cntreg;
	current.value = value;
	entry.cCurrent = current;
    }

    static void popState(final SearchEntry entry, final MemReg[] memregs, final int[] counters) {
	MState ms = entry.mCurrent;
	while (ms != null) {
	    final MemReg mr = memregs[ms.index];
	    mr.in = ms.in;
	    mr.out = ms.out;
	    ms = ms.prev;
	}
	CState cs = entry.cCurrent;
	while (cs != null) {
	    counters[cs.index] = cs.value;
	    cs = cs.prev;
	}
	entry.mCurrent = null;
	entry.cCurrent = null;
	entry.isState = false;
    }

    final void reset(final int restQueue) {
	this.term = null;
	this.index = this.cnt = this.regLen = 0;
	this.mCurrent = null;
	this.cCurrent = null;
	this.isState = false;
	final SearchEntry on = this.on;
	if (on != null) {
	    if (restQueue > 0) {
		on.reset(restQueue - 1);
	    } else {
		this.on = null;
		on.sub = null;
	    }
	}
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final SearchEntry that = (SearchEntry) o;
	if (this.index != that.index) {
	    return false;
	}
	if (this.cnt != that.cnt) {
	    return false;
	}
	if (this.regLen != that.regLen) {
	    return false;
	}
	if (this.isState != that.isState) {
	    return false;
	}
	if (this.term != null ? !this.term.equals(that.term) : that.term != null) {
	    return false;
	}
	if (this.sub != null ? !this.sub.equals(that.sub) : that.sub != null) {
	    return false;
	}
	if (this.on != null ? !this.on.equals(that.on) : that.on != null) {
	    return false;
	}
	if (this.mHead != null ? !this.mHead.equals(that.mHead) : that.mHead != null) {
	    return false;
	}
	return this.mCurrent != null ? this.mCurrent.equals(that.mCurrent)
		: that.mCurrent == null && (this.cHead != null ? this.cHead.equals(that.cHead)
			: that.cHead == null && (this.cCurrent != null ? this.cCurrent.equals(that.cCurrent)
				: that.cCurrent == null));
    }

    @Override
    public int hashCode() {
	int result = this.term != null ? this.term.hashCode() : 0;
	result = 31 * result + this.index;
	result = 31 * result + this.cnt;
	result = 31 * result + this.regLen;
	result = 31 * result + (this.isState ? 1 : 0);
	result = 31 * result + (this.mHead != null ? this.mHead.hashCode() : 0);
	result = 31 * result + (this.mCurrent != null ? this.mCurrent.hashCode() : 0);
	result = 31 * result + (this.cHead != null ? this.cHead.hashCode() : 0);
	result = 31 * result + (this.cCurrent != null ? this.cCurrent.hashCode() : 0);
	return result;
    }

    @Override
    public String toString() {
	return "SearchEntry{???}";
    }
}

class MemReg implements Serializable {
    private static final long serialVersionUID = -3628346657932720807L;
    private final int index;
    int in = -1, out = -1;
    int tmp = -1; // for assuming at GROUP_IN

    MemReg(final int index) {
	this.index = index;
    }

    void reset() {
	this.in = this.out = -1;
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final MemReg memReg = (MemReg) o;
	if (this.index != memReg.index) {
	    return false;
	}
	return this.in == memReg.in && this.out == memReg.out && this.tmp == memReg.tmp;
    }

    @Override
    public int hashCode() {
	int result = this.index;
	result = 31 * result + this.in;
	result = 31 * result + this.out;
	result = 31 * result + this.tmp;
	return result;
    }

    @Override
    public String toString() {
	return "MemReg{" + "index=" + this.index + ", in=" + this.in + ", out=" + this.out + ", tmp=" + this.tmp + '}';
    }
}

class LAEntry implements Serializable {
    private static final long serialVersionUID = -3628346657932720807L;
    int index;
    SearchEntry top, actual;

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final LAEntry laEntry = (LAEntry) o;
	return this.index == laEntry.index && (this.top != null ? this.top.equals(laEntry.top)
		: laEntry.top == null
			&& (this.actual != null ? this.actual.equals(laEntry.actual) : laEntry.actual == null));
    }

    @Override
    public int hashCode() {
	int result = this.index;
	result = 31 * result + (this.top != null ? this.top.hashCode() : 0);
	result = 31 * result + (this.actual != null ? this.actual.hashCode() : 0);
	return result;
    }

    @Override
    public String toString() {
	return "LAEntry{" + "index=" + this.index + ", top=" + this.top + ", actual=" + this.actual + '}';
    }
}