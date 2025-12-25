/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.editor;

public class ExternalMusic {
    // Fields
    private String name;
    private String path;

    // Constructor
    public ExternalMusic() {
	this.name = "";
	this.path = "";
    }

    // Methods
    public String getName() {
	return this.name;
    }

    public String getPath() {
	return this.path;
    }

    public void setName(final String newName) {
	this.name = newName;
    }

    public void setPath(final String newPath) {
	this.path = newPath;
    }
}
