package com.puttysoftware.lasertank.cheats;

class InstantCheat extends Cheat {
    // Constructor
    public InstantCheat(final String activator, final Effect doesWhat) {
	super(activator, doesWhat);
    }

    @Override
    public boolean getState() {
	return false;
    }

    @Override
    public boolean hasState() {
	return false;
    }

    @Override
    public void toggleState() {
	// We do not have state, so there is nothing to do
    }
}
