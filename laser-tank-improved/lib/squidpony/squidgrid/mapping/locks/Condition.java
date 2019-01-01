package squidpony.squidgrid.mapping.locks;

/**
 * Used to represent {@link Room}s' preconditions.
 * <p>
 * A Room's precondition can be considered the set of Symbols from the other
 * Rooms that the player must have collected to be able to reach this room. For
 * instance, if the Room is behind a locked door, the precondition for the Room
 * includes the key for that lock.
 * <p>
 * In practice, since there is always a time ordering on the collection of keys,
 * this can be implemented as a count of the number of keys the player must have
 * (the 'keyLevel').
 * <p>
 * The state of the {@link RoomLayout}'s switch is also recorded in the
 * Condition. A Room behind a link that requires the switch to be flipped into a
 * particular state will have a precondition that includes the switch's state.
 * <p>
 * A Condition is 'satisfied' when the player has all the keys it requires and
 * when the dungeon's switch is in the state that it requires.
 * <p>
 * A Condition x implies a Condition y if and only if y is satisfied whenever x
 * is.
 */
public class Condition {
    /**
     * A type to represent the required state of a switch for the Condition to be
     * satisfied.
     */
    public enum SwitchState {
	/**
	 * The switch may be in any state.
	 */
	EITHER,
	/**
	 * The switch must be off.
	 */
	OFF,
	/**
	 * The switch must be on.
	 */
	ON;
	/**
	 * Convert this SwitchState to a {@link Symbol}.
	 *
	 * @return a symbol representing the required state of the switch or null if the
	 *         switch may be in any state
	 */
	public int toSymbol() {
	    switch (this) {
	    case OFF:
		return Symbol.SWITCH_OFF;
	    case ON:
		return Symbol.SWITCH_ON;
	    default:
		return Symbol.NOTHING;
	    }
	}

	/**
	 * Invert the required state of the switch.
	 *
	 * @return a SwitchState with the opposite required switch state or this
	 *         SwitchState if no particular state is required
	 */
	public SwitchState invert() {
	    switch (this) {
	    case OFF:
		return ON;
	    case ON:
		return OFF;
	    default:
		return this;
	    }
	}
    }

    protected int keyLevel;
    protected SwitchState switchState;

    /**
     * Create a Condition that is always satisfied.
     */
    public Condition() {
	this.keyLevel = 0;
	this.switchState = SwitchState.EITHER;
    }

    /**
     * Creates a Condition that requires the player to have a particular
     * {@link Symbol}.
     *
     * @param e the symbol that the player must have for the Condition to be
     *          satisfied
     */
    public Condition(final int e) {
	if (e == Symbol.SWITCH_OFF) {
	    this.keyLevel = 0;
	    this.switchState = SwitchState.OFF;
	} else if (e == Symbol.SWITCH_ON) {
	    this.keyLevel = 0;
	    this.switchState = SwitchState.ON;
	} else {
	    this.keyLevel = e + 1;
	    this.switchState = SwitchState.EITHER;
	}
    }

    /**
     * Creates a Condition from another Condition (copy it).
     *
     * @param other the other Condition
     */
    public Condition(final Condition other) {
	this.keyLevel = other.keyLevel;
	this.switchState = other.switchState;
    }

    /**
     * Creates a Condition that requires the switch to be in a particular state.
     *
     * @param switchState the required state for the switch to be in
     */
    public Condition(final SwitchState switchState) {
	this.keyLevel = 0;
	this.switchState = switchState;
    }

    @Override
    public boolean equals(final Object other) {
	if (other instanceof Condition) {
	    final Condition o = (Condition) other;
	    return this.keyLevel == o.keyLevel && this.switchState == o.switchState;
	} else {
	    return super.equals(other);
	}
    }

    private void add(final int sym) {
	if (sym == Symbol.SWITCH_OFF) {
	    assert this.switchState == null;
	    this.switchState = SwitchState.OFF;
	} else if (sym == Symbol.SWITCH_ON) {
	    assert this.switchState == null;
	    this.switchState = SwitchState.ON;
	} else {
	    this.keyLevel = Math.max(this.keyLevel, sym + 1);
	}
    }

    private void add(final Condition cond) {
	if (this.switchState == SwitchState.EITHER) {
	    this.switchState = cond.switchState;
	} else {
	    assert this.switchState == cond.switchState;
	}
	this.keyLevel = Math.max(this.keyLevel, cond.keyLevel);
    }

    /**
     * Creates a new Condition that requires this Condition to be satisfied and
     * requires another {@link Symbol} to be obtained as well.
     *
     * @param sym the added symbol the player must have for the new Condition to be
     *            satisfied
     * @return the new Condition
     */
    public Condition and(final int sym) {
	final Condition result = new Condition(this);
	result.add(sym);
	return result;
    }

    /**
     * Creates a new Condition that requires this Condition and another Condition to
     * both be satisfied.
     *
     * @param other the other Condition that must be satisfied.
     * @return the new Condition
     */
    public Condition and(final Condition other) {
	if (other == null) {
	    return this;
	}
	final Condition result = new Condition(this);
	result.add(other);
	return result;
    }

    /**
     * Determines whether another Condition is necessarily true if this one is.
     *
     * @param other the other Condition
     * @return whether the other Condition is implied by this one
     */
    public boolean implies(final Condition other) {
	return this.keyLevel >= other.keyLevel
		&& (this.switchState == other.switchState || other.switchState == SwitchState.EITHER);
    }

    /**
     * Determines whether this Condition implies that a particular {@link Symbol}
     * has been obtained.
     *
     * @param s the Symbol
     * @return whether the Symbol is implied by this Condition
     */
    public boolean implies(final int s) {
	return this.implies(new Condition(s));
    }

    /**
     * Gets the single {@link Symbol} needed to make this Condition and another
     * Condition identical.
     * <p>
     * If {@link #and}ed to both Conditions, the Conditions would then imply each
     * other.
     *
     * @param other the other Condition
     * @return the Symbol needed to make the Conditions identical, or null if there
     *         is no single Symbol that would make them identical or if they are
     *         already identical.
     */
    public int singleSymbolDifference(final Condition other) {
	// If the difference between this and other can be made up by obtaining
	// a single new symbol, this returns the symbol. If multiple or no
	// symbols are required, returns Symbol.NOTHING.
	if (this.equals(other)) {
	    return Symbol.NOTHING;
	}
	if (this.switchState == other.switchState) {
	    return Math.max(this.keyLevel, other.keyLevel) - 1;
	} else {
	    if (this.keyLevel != other.keyLevel) {
		return Symbol.NOTHING;
	    }
	    // Multiple symbols needed ^^^
	    if (this.switchState != SwitchState.EITHER && other.switchState != SwitchState.EITHER) {
		return Symbol.NOTHING;
	    }
	    final SwitchState nonEither = this.switchState != SwitchState.EITHER ? this.switchState : other.switchState;
	    return nonEither == SwitchState.ON ? Symbol.SWITCH_ON : Symbol.SWITCH_OFF;
	}
    }

    @Override
    public String toString() {
	String result = "";
	if (this.keyLevel != 0) {
	    result += Symbol.toString(this.keyLevel - 1);
	}
	if (this.switchState != SwitchState.EITHER) {
	    if (!result.equals("")) {
		result += ",";
	    }
	    result += Symbol.toString(this.switchState.toSymbol());
	}
	return result;
    }

    /**
     * Get the number of keys that need to have been obtained for this Condition to
     * be satisfied.
     *
     * @return the number of keys
     */
    public int getKeyLevel() {
	return this.keyLevel;
    }

    /**
     * Get the state the switch is required to be in for this Condition to be
     * satisfied.
     */
    public SwitchState getSwitchState() {
	return this.switchState;
    }
}
