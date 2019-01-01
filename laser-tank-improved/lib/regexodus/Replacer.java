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
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <b>The Replacer class</b> suggests some methods to replace occurrences of a
 * pattern either by a result of evaluation of a perl-like expression, or by a
 * plain string, or according to a custom substitution model, provided as a
 * Substitution interface implementation.<br>
 * A Replacer instance may be obtained either using Pattern.replacer(...)
 * method, or by constructor:<code>
 * Pattern p=new Pattern("\\w+");
 * Replacer perlExpressionReplacer=p.replacer("[$&amp;]");
 * //or another way to do the same
 * Substitution myOwnModel=new Substitution(){
 *    public void appendSubstitution(MatchResult match,TextBuffer tb){
 *       tb.append('[');
 *       match.getGroup(MatchResult.MATCH,tb);
 *       tb.append(']');
 *    }
 * }
 * Replacer myVeryOwnReplacer=new Replacer(p,myOwnModel);
 * </code> The second method is much more verbose, but gives more freedom. To
 * perform a replacement call replace(someInput):
 *
 * <pre>
 * System.out.print(perlExpressionReplacer.replace("All your base "));
 * System.out.println(myVeryOwnReplacer.replace("are belong to us"));
 * //result: "[All] [your] [base] [are] [belong] [to] [us]"
 * </pre>
 *
 * This code was mostly written in 2001, I hope the reference isn't too
 * outdated...
 *
 * @see Substitution
 * @see PerlSubstitution
 * @see Replacer#Replacer(regexodus.Pattern, regexodus.Substitution)
 */
public class Replacer implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;
    private Pattern pattern;
    private Substitution substitution;

    /**
     * Constructs a Replacer from a Pattern and implementation of Substitution. Only
     * meant to be used if you have complex substitution behavior. An example of how
     * to make such an implementation that surrounds each match with an increasing
     * number of square brackets could be: <br>
     * <code>
     * Substitution mySub=new Substitution(){
     *    public int counter = 1;
     *    public void appendSubstitution(MatchResult match,TextBuffer tb){
     *       for(int i = 0; i &lt; counter; i++)
     *           tb.append('[');
     *       //appends the full match into tb; 0 can be used in place of MatchResult.MATCH
     *       match.getGroup(MatchResult.MATCH, tb);
     *       for(int i = 0; i &lt; counter; i++)
     *           tb.append(']');
     *       counter++;
     *    }
     * }
     * </code>
     *
     * @param pattern      a regexodus.Pattern that determines what should be
     *                     replaced
     * @param substitution an implementation of the Substitution interface, which
     *                     allows custom replacement behavior
     */
    public Replacer(final Pattern pattern, final Substitution substitution) {
	this.pattern = pattern;
	this.substitution = substitution;
    }

    /**
     * Constructs a Replacer from a Pattern and a String to replace occurrences of
     * the Pattern with.
     *
     * @param pattern      a regexodus.Pattern that determines what should be
     *                     replaced
     * @param substitution a String that will be used to replace occurrences of the
     *                     Pattern
     */
    public Replacer(final Pattern pattern, final String substitution) {
	this(pattern, substitution, true);
    }

    public Replacer(final Pattern pattern, final String substitution, final boolean isPerlExpr) {
	this.pattern = pattern;
	this.substitution = isPerlExpr ? new PerlSubstitution(substitution) : new DummySubstitution(substitution);
    }

    public void setSubstitution(final String s, final boolean isPerlExpr) {
	this.substitution = isPerlExpr ? new PerlSubstitution(s) : new DummySubstitution(s);
    }

    /**
     * Takes all instances in text of the Pattern this was constructed with, and
     * replaces them with substitution.
     *
     * @param text a String, StringBuilder, or other CharSequence that may contain
     *             the text to replace
     * @return the post-replacement text
     */
    public String replace(final CharSequence text) {
	final TextBuffer tb = Replacer.wrap(new StringBuilder(text.length()));
	Replacer.replace(this.pattern.matcher(text), this.substitution, tb);
	return tb.toString();
    }

    /**
     * Takes instances in text of the Pattern this was constructed with, up to count
     * times, and replaces them with substitution. If you want to change the
     * position in a Matcher so you start the next replacement at a later point in
     * text, you can use {@code replace(Matcher, Substitution, TextBuffer, int)},
     * which this uses internally. The difference is that internally, this uses a
     * temporary Matcher that doesn't store the change in position, and code that
     * should track replacement points should use a longer-lived Matcher.
     *
     * @param text  a String, StringBuilder, or other CharSequence that may contain
     *              the text to replace
     * @param count the maximum number of replacements to perform; will make no
     *              changes if less than 1
     * @return the post-replacement text
     */
    public String replace(final CharSequence text, final int count) {
	final TextBuffer tb = Replacer.wrap(new StringBuilder(text.length()));
	Replacer.replace(this.pattern.matcher(text), this.substitution, tb, count);
	return tb.toString();
    }

    public String replace(final char[] chars, final int off, final int len) {
	final TextBuffer tb = Replacer.wrap(new StringBuilder(len));
	Replacer.replace(this.pattern.matcher(chars, off, len), this.substitution, tb);
	return tb.toString();
    }

    public String replace(final MatchResult res, final int group) {
	final TextBuffer tb = Replacer.wrap(new StringBuilder());
	Replacer.replace(this.pattern.matcher(res, group), this.substitution, tb);
	return tb.toString();
    }

    @GwtIncompatible
    public String replace(final Reader text, final int length) throws IOException {
	final TextBuffer tb = Replacer.wrap(new StringBuilder(length >= 0 ? length : 0));
	Replacer.replace(this.pattern.matcher(text, length), this.substitution, tb);
	return tb.toString();
    }

    /**
     * Takes all occurrences of the pattern this was constructed with in text and
     * replaces them with the substitution. Appends the replaced text into sb.
     *
     * @param text a String, StringBuilder, or other CharSequence that may contain
     *             the text to replace
     * @param sb   the StringBuilder to append the result into
     * @return the number of individual replacements performed; the results are
     *         applied to sb
     */
    public int replace(final CharSequence text, final StringBuilder sb) {
	return Replacer.replace(this.pattern.matcher(text), this.substitution, Replacer.wrap(sb));
    }

    /**
     * Takes instances in text of the Pattern this was constructed with, up to count
     * times, and replaces them with the substitution. Appends the replaced text
     * into sb.
     *
     * @param text  a String, StringBuilder, or other CharSequence that may contain
     *              the text to replace
     * @param sb    the StringBuilder to append the result into
     * @param count the maximum number of replacements to perform; will make no
     *              changes if less than 1
     * @return the number of individual replacements performed; the results are
     *         applied to sb
     */
    public int replace(final CharSequence text, final StringBuilder sb, final int count) {
	return Replacer.replace(this.pattern.matcher(text), this.substitution, Replacer.wrap(sb), count);
    }

    /**
     */
    public int replace(final char[] chars, final int off, final int len, final StringBuilder sb) {
	return this.replace(chars, off, len, Replacer.wrap(sb));
    }

    /**
     */
    public int replace(final MatchResult res, final int group, final StringBuilder sb) {
	return this.replace(res, group, Replacer.wrap(sb));
    }

    /**
     */
    public int replace(final MatchResult res, final String groupName, final StringBuilder sb) {
	return this.replace(res, groupName, Replacer.wrap(sb));
    }

    @GwtIncompatible
    public int replace(final Reader text, final int length, final StringBuilder sb) throws IOException {
	return this.replace(text, length, Replacer.wrap(sb));
    }

    /**
     */
    public int replace(final CharSequence text, final TextBuffer dest) {
	return Replacer.replace(this.pattern.matcher(text), this.substitution, dest);
    }

    /**
     */
    private int replace(final char[] chars, final int off, final int len, final TextBuffer dest) {
	return Replacer.replace(this.pattern.matcher(chars, off, len), this.substitution, dest);
    }

    /**
     */
    private int replace(final MatchResult res, final int group, final TextBuffer dest) {
	return Replacer.replace(this.pattern.matcher(res, group), this.substitution, dest);
    }

    /**
     */
    private int replace(final MatchResult res, final String groupName, final TextBuffer dest) {
	return Replacer.replace(this.pattern.matcher(res, groupName), this.substitution, dest);
    }

    @GwtIncompatible
    private int replace(final Reader text, final int length, final TextBuffer dest) throws IOException {
	return Replacer.replace(this.pattern.matcher(text, length), this.substitution, dest);
    }

    /**
     * Replaces all occurrences of a matcher's pattern in a matcher's target by a
     * given substitution appending the result to a buffer.<br>
     * The substitution starts from current matcher's position, current match not
     * included.
     */
    public static int replace(final Matcher m, final Substitution substitution, final TextBuffer dest) {
	boolean firstPass = true;
	int c = 0;
	while (m.find()) {
	    if (m.end() == 0 && !firstPass) {
		continue; // allow to replace at "^"
	    }
	    if (m.start() > 0) {
		m.getGroup(MatchResult.PREFIX, dest);
	    }
	    substitution.appendSubstitution(m, dest);
	    c++;
	    m.setTarget(m, MatchResult.SUFFIX);
	    firstPass = false;
	}
	m.getGroup(MatchResult.TARGET, dest);
	return c;
    }

    /**
     * Replaces the first n occurrences of a matcher's pattern, where n is equal to
     * count, in a matcher's target by a given substitution, appending the result to
     * a buffer. <br>
     * The substitution starts from current matcher's position, current match not
     * included.
     *
     * @param m            a Matcher
     * @param substitution a Substitution, typically a PerlSubstitution
     * @param dest         the TextBuffer this will write to; see Replacer.wrap()
     * @param count        the number of replacements to attempt
     * @return the number of replacements performed
     */
    public static int replace(final Matcher m, final Substitution substitution, final TextBuffer dest,
	    final int count) {
	boolean firstPass = true;
	int c = 0;
	while (c < count && m.find()) {
	    if (m.end() == 0 && !firstPass) {
		continue; // allow to replace at "^"
	    }
	    if (m.start() > 0) {
		m.getGroup(MatchResult.PREFIX, dest);
	    }
	    substitution.appendSubstitution(m, dest);
	    c++;
	    m.setTarget(m, MatchResult.SUFFIX);
	    firstPass = false;
	}
	m.getGroup(MatchResult.TARGET, dest);
	return c;
    }

    /**
     * Replaces the next occurrence of a matcher's pattern in a matcher's target by
     * a given substitution, appending the result to a buffer but not writing the
     * remainder of m's match to the end of dest. <br>
     * The substitution starts from current matcher's position, current match not
     * included. <br>
     * You typically want to call {@code m.getGroup(MatchResult.TARGET, dest);}
     * after you have called replaceStep() until it returns false, which will fill
     * in the remainder of the matching text into dest.
     *
     * @param m            a Matcher
     * @param substitution a Substitution, typically a PerlSubstitution
     * @param dest         the TextBuffer this will write to; see Replacer.wrap()
     * @return the number of replacements performed
     */
    public static boolean replaceStep(final Matcher m, final Substitution substitution, final TextBuffer dest) {
	boolean firstPass = true;
	int c = 0;
	final int count = 1;
	while (c < count && m.find()) {
	    if (m.end() == 0 && !firstPass) {
		continue; // allow to replace at "^"
	    }
	    if (m.start() > 0) {
		m.getGroup(MatchResult.PREFIX, dest);
	    }
	    substitution.appendSubstitution(m, dest);
	    c++;
	    m.setTarget(m, MatchResult.SUFFIX);
	    firstPass = false;
	}
	return c > 0;
    }

    @GwtIncompatible
    private static int replace(final Matcher m, final Substitution substitution, final Writer out) throws IOException {
	try {
	    return Replacer.replace(m, substitution, Replacer.wrap(out));
	} catch (final WriteException e) {
	    throw e.reason;
	}
    }

    @GwtIncompatible
    public void replace(final CharSequence text, final Writer out) throws IOException {
	Replacer.replace(this.pattern.matcher(text), this.substitution, out);
    }

    @GwtIncompatible
    public void replace(final char[] chars, final int off, final int len, final Writer out) throws IOException {
	Replacer.replace(this.pattern.matcher(chars, off, len), this.substitution, out);
    }

    @GwtIncompatible
    public void replace(final MatchResult res, final int group, final Writer out) throws IOException {
	Replacer.replace(this.pattern.matcher(res, group), this.substitution, out);
    }

    @GwtIncompatible
    public void replace(final MatchResult res, final String groupName, final Writer out) throws IOException {
	Replacer.replace(this.pattern.matcher(res, groupName), this.substitution, out);
    }

    @GwtIncompatible
    public void replace(final Reader in, final int length, final Writer out) throws IOException {
	Replacer.replace(this.pattern.matcher(in, length), this.substitution, out);
    }

    private static class DummySubstitution implements Substitution {
	String str;

	DummySubstitution(final String s) {
	    this.str = s;
	}

	@Override
	public void appendSubstitution(final MatchResult match, final TextBuffer res) {
	    if (this.str != null) {
		res.append(this.str);
	    }
	}
    }

    private static class TableSubstitution implements Substitution {
	final LinkedHashMap<String, String> dictionary;

	TableSubstitution(final LinkedHashMap<String, String> dict) {
	    this.dictionary = dict;
	}

	TableSubstitution(final String... dict) {
	    this.dictionary = new LinkedHashMap<>(dict.length / 2);
	    for (int i = 0; i < dict.length - 1; i += 2) {
		this.dictionary.put(dict[i], dict[i + 1]);
	    }
	}

	@Override
	public void appendSubstitution(final MatchResult match, final TextBuffer dest) {
	    final String m = match.group(0);
	    if (m == null) {
		return;
	    }
	    for (final Map.Entry<String, String> kv : this.dictionary.entrySet()) {
		if (kv.getKey().equals(m)) {
		    dest.append(kv.getValue());
		    return;
		}
	    }
	    dest.append(m);
	}
    }

    /**
     * Makes a Replacer that replaces a literal String at index i in pairs with the
     * String at index i+1. Doesn't need escapes in the Strings it searches for (at
     * index 0, 2, 4, etc.), but cannot search for the exact two characters in
     * immediate succession, backslash then capital E, because it finds literal
     * Strings using {@code \\Q...\\E}. Uses only default modes (not
     * case-insensitive, and most other flags don't have any effect since this
     * doesn't care about "\\w" or other backslash-escaped special categories), but
     * you can get the Pattern from this afterwards and set its flags with its
     * setFlags() method. The Strings this replaces with are at index 1, 3, 5, etc.
     * and correspond to the search String immediately before it; they are also
     * literal.
     *
     * @param pairs alternating search String, then replacement String, then search,
     *              replacement, etc.
     * @return a Replacer that will act as a replacement table for the given Strings
     */
    public static Replacer makeTable(final String... pairs) {
	if (pairs == null || pairs.length < 2) {
	    return new Replacer(Pattern.compile("$"), new DummySubstitution(""));
	}
	final TableSubstitution tab = new TableSubstitution(pairs);
	final StringBuilder sb = new StringBuilder(128);
	sb.append("(?>");
	for (final String s : tab.dictionary.keySet()) {
	    sb.append("\\Q");
	    sb.append(s);
	    sb.append("\\E|");
	}
	if (sb.length() > 3) {
	    sb.setCharAt(sb.length() - 1, ')');
	} else {
	    sb.append(')');
	}
	return new Replacer(Pattern.compile(sb.toString()), tab);
    }

    /**
     * Makes a Replacer that replaces a literal String key in dict with the
     * corresponding String value in dict. Doesn't need escapes in the Strings it
     * searches for (at index 0, 2, 4, etc.), but cannot search for the exact two
     * characters in immediate succession, backslash then capital E, because it
     * finds literal Strings using {@code \\Q...\\E}. Uses only default modes (not
     * case-insensitive, and most other flags don't have any effect since this
     * doesn't care about "\\w" or other backslash-escaped special categories), but
     * you can get the Pattern from this afterwards and set its flags with its
     * setFlags() method. The Strings this replaces with are the values, and are
     * also literal. If the Map this is given is a sorted Map of some kind or a
     * (preferably) LinkedHashMap, then the order search strings will be tried will
     * be stable; the same is not necessarily true for HashMap.
     *
     * @param dict a Map (hopefully with stable order) with search String keys and
     *             replacement String values
     * @return a Replacer that will act as a replacement table for the given Strings
     */
    public static Replacer makeTable(final Map<String, String> dict) {
	if (dict == null || dict.isEmpty()) {
	    return new Replacer(Pattern.compile("$"), new DummySubstitution(""));
	}
	final TableSubstitution tab = new TableSubstitution(new LinkedHashMap<>(dict));
	final StringBuilder sb = new StringBuilder(128);
	sb.append("(?>");
	for (final String s : tab.dictionary.keySet()) {
	    sb.append("\\Q");
	    sb.append(s);
	    sb.append("\\E|");
	}
	if (sb.length() > 3) {
	    sb.setCharAt(sb.length() - 1, ')');
	} else {
	    sb.append(')');
	}
	return new Replacer(Pattern.compile(sb.toString()), tab);
    }

    public static StringBuilderBuffer wrap(final StringBuilder sb) {
	return new StringBuilderBuffer(sb);
    }

    public static class StringBuilderBuffer implements TextBuffer, Serializable {
	private static final long serialVersionUID = 2589054766833218313L;
	public StringBuilder sb;

	public StringBuilderBuffer() {
	    this.sb = new StringBuilder();
	}

	public StringBuilderBuffer(final StringBuilder builder) {
	    this.sb = builder;
	}

	@Override
	public void append(final char c) {
	    this.sb.append(c);
	}

	@Override
	public void append(final char[] chars, final int start, final int len) {
	    this.sb.append(chars, start, len);
	}

	@Override
	public void append(final String s) {
	    this.sb.append(s);
	}

	@Override
	public String toString() {
	    return this.sb.toString();
	}

	public StringBuilder toStringBuilder() {
	    return this.sb;
	}
    }

    public static StringBufferBuffer wrap(final StringBuffer sb) {
	return new StringBufferBuffer(sb);
    }

    public static class StringBufferBuffer implements TextBuffer, Serializable {
	private static final long serialVersionUID = 2589054766833218313L;
	public StringBuffer sb;

	public StringBufferBuffer() {
	    this.sb = new StringBuffer();
	}

	public StringBufferBuffer(final StringBuffer builder) {
	    this.sb = builder;
	}

	@Override
	public void append(final char c) {
	    this.sb.append(c);
	}

	@Override
	public void append(final char[] chars, final int start, final int len) {
	    this.sb.append(chars, start, len);
	}

	@Override
	public void append(final String s) {
	    this.sb.append(s);
	}

	@Override
	public String toString() {
	    return this.sb.toString();
	}

	public StringBuffer toStringBuffer() {
	    return this.sb;
	}
    }

    @GwtIncompatible
    private static TextBuffer wrap(final Writer writer) {
	return new TextBuffer() {
	    @Override
	    public void append(final char c) {
		try {
		    writer.write(c);
		} catch (final IOException e) {
		    throw new WriteException(e);
		}
	    }

	    @Override
	    public void append(final char[] chars, final int off, final int len) {
		try {
		    writer.write(chars, off, len);
		} catch (final IOException e) {
		    throw new WriteException(e);
		}
	    }

	    @Override
	    public void append(final String s) {
		try {
		    writer.write(s);
		} catch (final IOException e) {
		    throw new WriteException(e);
		}
	    }
	};
    }

    private static class WriteException extends RuntimeException {
	IOException reason;

	WriteException(final IOException io) {
	    this.reason = io;
	}
    }

    public Pattern getPattern() {
	return this.pattern;
    }

    public void setPattern(final Pattern pattern) {
	this.pattern = pattern;
    }

    public Substitution getSubstitution() {
	return this.substitution;
    }

    public void setSubstitution(final Substitution substitution) {
	this.substitution = substitution;
    }

    public void setSubstitution(final String substitution) {
	this.substitution = new PerlSubstitution(substitution);
    }

    @Override
    public boolean equals(final Object o) {
	if (this == o) {
	    return true;
	}
	if (o == null || this.getClass() != o.getClass()) {
	    return false;
	}
	final Replacer replacer = (Replacer) o;
	return this.pattern != null ? this.pattern.equals(replacer.pattern)
		: replacer.pattern == null
			&& (this.substitution != null ? this.substitution.equals(replacer.substitution)
				: replacer.substitution == null);
    }

    @Override
    public int hashCode() {
	int result = this.pattern != null ? this.pattern.hashCode() : 0;
	result = 31 * result + (this.substitution != null ? this.substitution.hashCode() : 0);
	return result;
    }

    @Override
    public String toString() {
	return "Replacer{" + "pattern=" + this.pattern + ", substitution=" + this.substitution + '}';
    }
}