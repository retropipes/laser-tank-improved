package com.puttysoftware.lasertank.cheats;

import java.util.Objects;

abstract class Cheat {
    // Enumerations
    public static enum Effect {
	KILL_ANTI_TANKS, BREAK_BRICKS, LIGHT_MODE_TANK, SHADOW_MODE_TANK, FROZEN_MODE_TANK, MOLTEN_MODE_TANK,
	POWER_MODE_TANK, BRIDGES_TO_GROUND, GROUND_TO_BRIDGES, OPEN_DOORS, HEAT_ALL, MELT_ALL, HOVERING, COLLISIONS_OFF,
	INVINCIBLE, INF_MISSILES, INF_BLUE_LASERS, INF_BOOSTS, INF_MAGNETS, INF_DISRUPTORS, INF_BOMBS, INF_HEAT_BOMBS,
	INF_ICE_BOMBS, INF_CONTROLLERS, INACTIVE_MOVERS;
    }

    // Fields
    private static final int INSTANTS = 12;
    private final String code;
    private Effect effect;

    // Constructor
    public Cheat(final String activator, final Effect doesWhat) {
	this.code = activator;
	this.effect = doesWhat;
    }

    public abstract boolean getState();

    public abstract boolean hasState();

    public abstract void toggleState();

    public final String getCode() {
	return this.code;
    }

    public final Effect getEffect() {
	return this.effect;
    }

    public static int instantCount() {
	return Cheat.INSTANTS;
    }

    public static int count() {
	return Effect.values().length;
    }

    @Override
    public int hashCode() {
	return Objects.hash(this.code, this.effect);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof Cheat)) {
	    return false;
	}
	Cheat other = (Cheat) obj;
	return Objects.equals(this.code, other.code) && this.effect == other.effect;
    }
}
