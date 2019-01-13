package com.puttysoftware.lasertank.cheats;

class ToggleCheat extends Cheat {
    // Fields
    private boolean state;

    // Constructor
    public ToggleCheat(final String activator, final Effect doesWhat) {
	super(activator, doesWhat);
	this.state = false;
    }

    @Override
    public boolean getState() {
	return this.state;
    }

    @Override
    public boolean hasState() {
	return true;
    }

    @Override
    public void toggleState() {
	this.state = !this.state;
    }
}
