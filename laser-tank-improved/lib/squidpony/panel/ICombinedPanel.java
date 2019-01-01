package squidpony.panel;

import squidpony.IColorCenter;
import squidpony.annotation.Beta;

/**
 * The combination of two panels, one to color the background, the other to
 * write characters on the foreground.
 *
 * <p>
 * <ul>
 * <li>There is a very generic implementation in this file: {@link Impl} that
 * you should use if you're combining generic things.</li>
 * <li>There is a libgdx-{@code Group} based implementation that offers more
 * features and that you should likely use if you're a new user (in
 * squidlib-gdx).</li>
 * </ul>
 *
 * @author smelC
 *
 * @param <T> The type of colors.
 */
@Beta
public interface ICombinedPanel<T> {
    /**
     * Puts the character {@code c} at {@code (x, y)}.
     *
     * @param x
     * @param y
     * @param c
     */
    void putFG(int x, int y, char c);

    /**
     * Puts the character {@code c} at {@code (x, y)} with some {@code color}.
     *
     * @param x
     * @param y
     * @param c
     * @param color
     */
    void putFG(int x, int y, char c, T color);

    /**
     * Puts the given string horizontally with the first character at the given
     * offset.
     *
     * Does not word wrap. Characters that are not renderable (due to being at
     * negative offsets or offsets greater than the grid size) will not be shown but
     * will not cause any malfunctions.
     *
     * @param x      the x coordinate of the first character
     * @param y      the y coordinate of the first character
     * @param string the characters to be displayed
     * @param color  the color to draw the characters
     */
    void putFG(int x, int y, String string, T color);

    /**
     * Puts the given string horizontally with the first character at the given
     * offset.
     *
     * Does not word wrap. Characters that are not renderable (due to being at
     * negative offsets or offsets greater than the grid size) will not be shown but
     * will not cause any malfunctions.
     *
     * @param x  the x coordinate of the first character
     * @param y  the y coordinate of the first character
     * @param cs the text to be displayed, with its color.
     */
    void putFG(int x, int y, IColoredString<T> cs);

    /**
     * Puts the color {@code c} at {@code (x, y)}.
     *
     * @param x
     * @param y
     * @param color
     */
    void putBG(int x, int y, T color);

    /**
     * Puts {@code c} at (x, y), using {@code fgc} for {@code c} and {@code bgc} for
     * the background.
     */
    void put(int x, int y, char c, T bgc, T fgc);

    /**
     * Put {@code cs} at (x,y) using {@code bgc} for the background.
     */
    void put(int x, int y, T bgc, IColoredString<T> cs);

    /**
     * Put {@code cs} at (x,y) using {@code bgc} for the background and {@code fgc}
     * for the foreground.
     */
    void put(int x, int y, String s, T bgc, T fgc);

    /**
     * @param what  What to fill
     * @param color The color to put within this panel.
     */
    void fill(What what, T color);

    /**
     * @return Returns true if there are animations running when this method is
     *         called.
     */
    boolean hasActiveAnimations();

    /**
     * Changes the underlying {@link IColorCenter}.
     *
     * @param icc
     */
    void setColorCenter(IColorCenter<T> icc);

    /**
     * What to fill
     *
     * @author smelC
     */
    enum What {
	BG, FG, BG_AND_FG;
	/**
	 * @return {@code true} if {@code this} contains the background.
	 */
	public boolean hasBG() {
	    switch (this) {
	    case BG:
	    case BG_AND_FG:
		return true;
	    case FG:
		return false;
	    }
	    throw new IllegalStateException("Unmatched value: " + this);
	}

	/**
	 * @return {@code true} if {@code this} contains the foreground.
	 */
	public boolean hasFG() {
	    switch (this) {
	    case FG:
	    case BG_AND_FG:
		return true;
	    case BG:
		return false;
	    }
	    throw new IllegalStateException("Unmatched value: " + this);
	}
    }

    /**
     * A generic implementation of {@link ICombinedPanel}. Useful to combine things.
     * If you're a new user, you likely would prefer the more specific
     * implementation using libGDX, GroupCombinedPanel, instead.
     *
     * @author smelC
     *
     * @param <T> The type of colors.
     */
    @Beta
    class Impl<T> implements ICombinedPanel<T> {
	protected final ISquidPanel<T> bg;
	protected final ISquidPanel<T> fg;
	protected final int width;
	protected final int height;

	/**
	 * @param bg     The backing background panel.
	 * @param fg     The backing foreground panel.
	 * @param width  The width of this panel, used for {@link #fillBG(Object)} (so
	 *               that it fills within {@code [0, width)}).
	 * @param height The height of this panel, used for {@link #fillBG(Object)} (so
	 *               that it fills within {@code [0, height)}).
	 * @throws IllegalStateException In various cases of errors regarding sizes of
	 *                               panels.
	 */
	public Impl(final ISquidPanel<T> bg, final ISquidPanel<T> fg, final int width, final int height) {
	    if (bg.gridWidth() != fg.gridWidth()) {
		throw new IllegalStateException("Cannot build a combined panel with backers of different widths");
	    }
	    if (bg.gridHeight() != fg.gridHeight()) {
		throw new IllegalStateException("Cannot build a combined panel with backers of different heights");
	    }
	    this.bg = bg;
	    this.fg = fg;
	    if (width < 0) {
		throw new IllegalStateException("Cannot create a panel with a negative width");
	    }
	    this.width = width;
	    if (height < 0) {
		throw new IllegalStateException("Cannot create a panel with a negative height");
	    }
	    this.height = height;
	}

	@Override
	public void putFG(final int x, final int y, final char c) {
	    this.fg.put(x, y, c);
	}

	@Override
	public void putFG(final int x, final int y, final char c, final T color) {
	    this.fg.put(x, y, c, color);
	}

	@Override
	public void putFG(final int x, final int y, final String string, final T foreground) {
	    this.fg.put(x, y, string, foreground);
	}

	@Override
	public void putFG(final int x, final int y, final IColoredString<T> cs) {
	    this.fg.put(x, y, cs);
	}

	@Override
	public void putBG(final int x, final int y, final T color) {
	    this.bg.put(x, y, color);
	}

	@Override
	public void put(final int x, final int y, final char c, final T bgc, final T fgc) {
	    this.bg.put(x, y, bgc);
	    this.fg.put(x, y, c, fgc);
	}

	@Override
	public void put(final int x, final int y, final T bgc, final IColoredString<T> cs) {
	    final int l = cs.length();
	    for (int i = x; i < l && i < this.width; i++) {
		this.bg.put(i, y, bgc);
	    }
	    this.fg.put(x, y, cs);
	}

	@Override
	public void put(final int x, final int y, final String s, final T bgc, final T fgc) {
	    final int l = s.length();
	    for (int i = x; i < l && i < this.width; i++) {
		this.bg.put(i, y, bgc);
	    }
	    this.fg.put(x, y, s, fgc);
	}

	@Override
	public void fill(final What what, final T color) {
	    /* Nope, not Doom's Big Fucking Gun */
	    final boolean bfg = what.hasFG();
	    final boolean bbg = what.hasBG();
	    for (int x = 0; x < this.width; x++) {
		for (int y = 0; y < this.height; y++) {
		    if (bfg) {
			this.putFG(x, y, ' ', color);
		    }
		    if (bbg) {
			this.putBG(x, y, color);
		    }
		}
	    }
	}

	/**
	 * Convenience method that fills the background with the given color. Equivalent
	 * to calling {@link #fill(What, Object)} with {@link What#BG} as the first
	 * parameter.
	 *
	 * @param color the color to fill the background with
	 */
	public void fillBG(final T color) {
	    this.fill(What.BG, color);
	}

	@Override
	public boolean hasActiveAnimations() {
	    return this.bg.hasActiveAnimations() || this.fg.hasActiveAnimations();
	}

	@Override
	public void setColorCenter(final IColorCenter<T> icc) {
	    this.bg.setColorCenter(icc);
	    this.fg.setColorCenter(icc);
	}
    }
}
