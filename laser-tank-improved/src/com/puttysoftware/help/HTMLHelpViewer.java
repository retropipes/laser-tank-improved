package com.puttysoftware.help;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

public final class HTMLHelpViewer {
    // Fields
    private JEditorPane helpContents;
    private final Container helpContainer;
    private final JScrollPane scrollPane;

    // Constructor
    public HTMLHelpViewer(final URL helpPage) {
	this.helpContainer = new Container();
	this.helpContainer.setLayout(new FlowLayout());
	try {
	    this.helpContents = new JEditorPane(helpPage);
	} catch (final Exception e) {
	    this.helpContents = new JEditorPane("text/plain", "An error occurred while fetching the help contents.");
	}
	this.helpContents.setEditable(false);
	this.scrollPane = new JScrollPane(this.helpContents);
	this.helpContainer.add(this.scrollPane);
    }

    // Methods
    public Container getHelp() {
	return this.helpContainer;
    }

    public void setHelpSize(final int horz, final int vert) {
	this.helpContents.setPreferredSize(new Dimension(horz, vert));
	this.scrollPane.setPreferredSize(new Dimension(horz, vert));
    }
}
