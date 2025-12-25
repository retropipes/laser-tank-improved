/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.game;

import java.io.IOException;

import com.puttysoftware.fileio.XMLFileReader;
import com.puttysoftware.fileio.XMLFileWriter;

class GameReplayEngine {
    // Inner classes
    private static class Link {
	public static Link read(final XMLFileReader reader) throws IOException {
	    final boolean l = reader.readBoolean();
	    final int x = reader.readInt();
	    final int y = reader.readInt();
	    final boolean hasNextLink = reader.readBoolean();
	    final Link link = new Link(l, x, y);
	    link.hasNext = hasNextLink;
	    return link;
	}

	// Fields
	public boolean laser;
	public int coordX, coordY;
	public Link next;
	public boolean hasNext;

	Link(final boolean l, final int x, final int y) {
	    this.laser = l;
	    this.coordX = x;
	    this.coordY = y;
	    this.next = null;
	}

	public void write(final XMLFileWriter writer) throws IOException {
	    writer.writeBoolean(this.laser);
	    writer.writeInt(this.coordX);
	    writer.writeInt(this.coordY);
	    writer.writeBoolean(this.next != null);
	}
    }

    private static class LinkList {
	public static LinkList read(final XMLFileReader reader) throws IOException {
	    final boolean hasData = reader.readBoolean();
	    final LinkList ll = new LinkList();
	    if (hasData) {
		Link curr = Link.read(reader);
		Link prev;
		ll.insertNext(null, curr);
		while (curr.hasNext) {
		    prev = curr;
		    curr = Link.read(reader);
		    ll.insertNext(prev, curr);
		}
	    }
	    return ll;
	}

	// Fields
	private Link first;

	LinkList() {
	    this.first = null;
	}

	public Link deleteFirst() {
	    final Link temp = this.first;
	    this.first = this.first.next;
	    return temp;
	}

	public void insertFirst(final boolean l, final int x, final int y) {
	    final Link newLink = new Link(l, x, y);
	    newLink.next = this.first;
	    this.first = newLink;
	}

	private void insertNext(final Link currLink, final Link newLink) {
	    if (currLink == null) {
		this.first = newLink;
	    } else {
		currLink.next = newLink;
	    }
	}

	public boolean isEmpty() {
	    return this.first == null;
	}

	private void reverse() {
	    Link current = this.first;
	    this.first = null;
	    while (current != null) {
		final Link save = current;
		current = current.next;
		save.next = this.first;
		this.first = save;
	    }
	}

	public void write(final XMLFileWriter writer) throws IOException {
	    this.reverse();
	    if (this.isEmpty()) {
		writer.writeBoolean(false);
	    } else {
		writer.writeBoolean(true);
		Link node = this.first;
		while (node != null) {
		    node.write(writer);
		    node = node.next;
		}
	    }
	}
    }

    private static class LinkStack {
	public static LinkStack read(final XMLFileReader reader) throws IOException {
	    final LinkStack ls = new LinkStack();
	    ls.theList = LinkList.read(reader);
	    return ls;
	}

	// Fields
	private LinkList theList;

	LinkStack() {
	    this.theList = new LinkList();
	}

	public boolean isEmpty() {
	    return this.theList.isEmpty();
	}

	public Link pop() {
	    return this.theList.deleteFirst();
	}

	public void push(final boolean l, final int x, final int y) {
	    this.theList.insertFirst(l, x, y);
	}

	public void write(final XMLFileWriter writer) throws IOException {
	    this.theList.write(writer);
	}
    }

    static GameReplayEngine readReplay(final XMLFileReader reader) throws IOException {
	final GameReplayEngine gre = new GameReplayEngine();
	gre.redoHistory = LinkStack.read(reader);
	return gre;
    }

    // Fields
    private final LinkStack undoHistory;
    private LinkStack redoHistory;
    private boolean isLaser;
    private int destX, destY;

    // Constructors
    public GameReplayEngine() {
	this.undoHistory = new LinkStack();
	this.redoHistory = new LinkStack();
	this.isLaser = false;
	this.destX = -1;
	this.destY = -1;
    }

    int getX() {
	return this.destX;
    }

    int getY() {
	return this.destY;
    }

    // Public methods
    void redo() {
	if (!this.redoHistory.isEmpty()) {
	    final Link entry = this.redoHistory.pop();
	    this.isLaser = entry.laser;
	    this.destX = entry.coordX;
	    this.destY = entry.coordY;
	} else {
	    this.isLaser = false;
	    this.destX = -1;
	    this.destY = -1;
	}
    }

    boolean tryRedo() {
	return !this.redoHistory.isEmpty();
    }

    void updateRedoHistory(final boolean laser, final int x, final int y) {
	this.redoHistory.push(laser, x, y);
    }

    void updateUndoHistory(final boolean laser, final int x, final int y) {
	this.undoHistory.push(laser, x, y);
    }

    boolean wasLaser() {
	return this.isLaser;
    }

    void writeReplay(final XMLFileWriter writer) throws IOException {
	this.undoHistory.write(writer);
    }
}
