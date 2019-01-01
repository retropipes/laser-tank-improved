package squidpony;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import squidpony.panel.IColoredString;

/**
 * An helper class for code that deals with lists of {@link IColoredString}s. It
 * does nothing smart, its only purpose is to save you some typing for frequent
 * calls. It is particularly useful when feeding large pieces of text to classes
 * like TextPanel in the display module.
 *
 * @author smelC
 */
public class ColoredStringList<T> extends ArrayList<IColoredString<T>> {
    private static final long serialVersionUID = -5111205714079762803L;

    public ColoredStringList() {
	super();
    }

    public ColoredStringList(final int expectedSize) {
	super(expectedSize);
    }

    /**
     * @return A fresh empty instance.
     */
    public static <T> ColoredStringList<T> create() {
	return new ColoredStringList<>();
    }

    /**
     * @param expectedSize
     * @return A fresh empty instance.
     */
    public static <T> ColoredStringList<T> create(final int expectedSize) {
	return new ColoredStringList<>(expectedSize);
    }

    /**
     * Appends {@code text} to {@code this}, without specifying its color.
     *
     * @param text the text to append
     */
    public void addText(final String text) {
	this.addColoredText(text, null);
    }

    /**
     * Appends {@code text} to {@code this}.
     *
     * @param text the text to append
     */
    public void addText(final IColoredString<T> text) {
	final int sz = this.size();
	if (sz == 0) {
	    this.add(text);
	} else {
	    this.get(sz - 1).append(text);
	}
    }

    /**
     * Appends colored text to {@code this}.
     *
     * @param text the text to append
     */
    public void addColoredText(final String text, final T c) {
	if (this.isEmpty()) {
	    this.addColoredTextOnNewLine(text, c);
	} else {
	    final IColoredString<T> last = this.get(this.size() - 1);
	    last.append(text, c);
	}
    }

    /**
     * Appends text to {@code this}, on a new line; without specifying its color.
     *
     * @param text the text to append
     */
    public void addTextOnNewLine(final String text) {
	this.addColoredTextOnNewLine(text, null);
    }

    public void addTextOnNewLine(final IColoredString<T> text) {
	this.add(text);
    }

    /**
     * Appends colored text to {@code this}.
     *
     * @param text the text to append
     */
    public void addColoredTextOnNewLine(final String text, /* @Nullable */ final T color) {
	this.add(IColoredString.Impl.<T>create(text, color));
    }

    /**
     * Adds {@code texts} to {@code this}, starting a new line for the first one.
     *
     * @param texts the Collection of objects extending IColoredString to append
     */
    public void addOnNewLine(final Collection<? extends IColoredString<T>> texts) {
	final Iterator<? extends IColoredString<T>> it = texts.iterator();
	boolean first = true;
	while (it.hasNext()) {
	    if (first) {
		this.addTextOnNewLine(it.next());
		first = false;
	    } else {
		this.addText(it.next());
	    }
	}
    }

    /**
     * Contrary to {@link Collection#addAll(Collection)}, this method appends text
     * to the current text, without inserting new lines.
     *
     * @param texts the Collection of objects extending IColoredString to append
     */
    public void addAllText(final Collection<? extends IColoredString<T>> texts) {
	for (final IColoredString<T> text : texts) {
	    this.addText(text);
	}
    }

    /**
     * Jumps a line.
     */
    public void addEmptyLine() {
	this.addTextOnNewLine("");
	this.addTextOnNewLine("");
    }

    /**
     * Changes a color in members of {@code this}.
     *
     * @param old The color to replace. Can be {@code null}.
     */
    public void replaceColor(final T old, final T new_) {
	final int sz = this.size();
	for (int i = 0; i < sz; i++) {
	    this.get(i).replaceColor(old, new_);
	}
    }
}
