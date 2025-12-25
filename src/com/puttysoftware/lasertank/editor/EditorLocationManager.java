/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.editor;

import com.puttysoftware.lasertank.arena.AbstractArena;

public final class EditorLocationManager {
    // Fields
    private int locX, locY, locZ, locW;
    private int locU;
    private int minX, minY, minZ, minU;
    private int maxU;
    private int maxX, maxY, maxZ;

    // Constructors
    public EditorLocationManager() {
	this.resetEditorLocation();
    }

    private void checkLimits() {
	// Check for limits out of bounds
	if (this.locU < this.minU) {
	    this.locU = this.minU;
	}
	if (this.locU > this.maxU) {
	    this.locU = this.maxU;
	}
	if (this.locX < this.minX) {
	    this.locX = this.minX;
	}
	if (this.locX > this.maxX) {
	    this.locX = this.maxX;
	}
	if (this.locY < this.minY) {
	    this.locY = this.minY;
	}
	if (this.locY > this.maxY) {
	    this.locY = this.maxY;
	}
	if (this.locZ < this.minZ) {
	    this.locZ = this.minZ;
	}
	if (this.locZ > this.maxZ) {
	    this.locZ = this.maxZ;
	}
    }

    public int getEditorLocationU() {
	return this.locU;
    }

    int getEditorLocationW() {
	return this.locW;
    }

    // Methods
    int getEditorLocationX() {
	return this.locX;
    }

    int getEditorLocationY() {
	return this.locY;
    }

    public int getEditorLocationZ() {
	return this.locZ;
    }

    public int getMaxEditorLocationU() {
	return this.maxU;
    }

    public int getMaxEditorLocationZ() {
	return this.maxZ;
    }

    public int getMinEditorLocationU() {
	return this.minU;
    }

    public int getMinEditorLocationZ() {
	return this.minZ;
    }

    void offsetEditorLocationU(final int val) {
	this.locU += val;
	this.checkLimits();
    }

    void offsetEditorLocationZ(final int val) {
	this.locZ += val;
	this.checkLimits();
    }

    private void resetEditorLocation() {
	this.locX = 0;
	this.locY = 0;
	this.locZ = 0;
	this.locW = 0;
	this.locU = 0;
	this.maxX = 0;
	this.maxY = 0;
	this.maxZ = 0;
	this.maxU = 0;
	this.minX = 0;
	this.minY = 0;
	this.minZ = 0;
	this.minU = 0;
    }

    void setEditorLocationU(final int val) {
	this.locU = val;
	this.checkLimits();
    }

    void setEditorLocationW(final int val) {
	this.locW = val;
    }

    void setEditorLocationX(final int val) {
	this.locX = val;
	this.checkLimits();
    }

    void setEditorLocationY(final int val) {
	this.locY = val;
	this.checkLimits();
    }

    void setLimitsFromArena(final AbstractArena m) {
	this.minX = 0;
	this.minY = 0;
	this.minZ = 0;
	this.minU = 0;
	this.maxU = m.getLevels() - 1;
	this.maxX = m.getRows();
	this.maxY = m.getColumns();
	this.maxZ = m.getFloors() - 1;
    }
}
