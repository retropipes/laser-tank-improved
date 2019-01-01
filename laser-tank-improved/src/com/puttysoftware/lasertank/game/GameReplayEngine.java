/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.game;

import java.io.IOException;

import com.puttysoftware.lasertank.improved.fileio.XMLFileReader;
import com.puttysoftware.lasertank.improved.fileio.XMLFileWriter;

class GameReplayEngine {
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

    int getX() {
	return this.destX;
    }

    int getY() {
	return this.destY;
    }

    static GameReplayEngine readReplay(final XMLFileReader reader) throws IOException {
	final GameReplayEngine gre = new GameReplayEngine();
	gre.redoHistory = LinkStack.read(reader);
	return gre;
    }

    void writeReplay(final XMLFileWriter writer) throws IOException {
	this.undoHistory.write(writer);
    }

    // Inner classes
    private static class Link {
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

	public static Link read(final XMLFileReader reader) throws IOException {
	    final boolean l = reader.readBoolean();
	    final int x = reader.readInt();
	    final int y = reader.readInt();
	    final boolean hasNextLink = reader.readBoolean();
	    final Link link = new Link(l, x, y);
	    link.hasNext = hasNextLink;
	    return link;
	}

	public void write(final XMLFileWriter writer) throws IOException {
	    writer.writeBoolean(this.laser);
	    writer.writeInt(this.coordX);
	    writer.writeInt(this.coordY);
	    writer.writeBoolean(this.next != null);
	}
    }

    private static class LinkList {
	// Fields
	private Link first;

	LinkList() {
	    this.first = null;
	}

	public boolean isEmpty() {
	    return this.first == null;
	}

	public void insertFirst(final boolean l, final int x, final int y) {
	    final Link newLink = new Link(l, x, y);
	    newLink.next = this.first;
	    this.first = newLink;
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

	private void insertNext(final Link currLink, final Link newLink) {
	    if (currLink == null) {
		this.first = newLink;
	    } else {
		currLink.next = newLink;
	    }
	}

	public Link deleteFirst() {
	    final Link temp = this.first;
	    this.first = this.first.next;
	    return temp;
	}

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
	// Fields
	private LinkList theList;

	LinkStack() {
	    this.theList = new LinkList();
	}

	public void push(final boolean l, final int x, final int y) {
	    this.theList.insertFirst(l, x, y);
	}

	public Link pop() {
	    return this.theList.deleteFirst();
	}

	public boolean isEmpty() {
	    return this.theList.isEmpty();
	}

	public static LinkStack read(final XMLFileReader reader) throws IOException {
	    final LinkStack ls = new LinkStack();
	    ls.theList = LinkList.read(reader);
	    return ls;
	}

	public void write(final XMLFileWriter writer) throws IOException {
	    this.theList.write(writer);
	}
    }
}
