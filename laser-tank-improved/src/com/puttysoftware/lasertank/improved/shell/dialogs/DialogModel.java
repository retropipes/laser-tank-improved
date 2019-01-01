package com.puttysoftware.lasertank.improved.shell.dialogs;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

public final class DialogModel {
    // Fields
    private final String title;
    private final String actionButtonText;
    private final Image systemIcon;
    private final Icon mainImage;
    private final List<String> messages;

    // Constructors
    public DialogModel(final String theTitle, final String theActionButtonText, final Image theSystemIcon,
	    final Icon theMainImage) {
	super();
	this.title = theTitle;
	this.actionButtonText = theActionButtonText;
	this.systemIcon = theSystemIcon;
	this.mainImage = theMainImage;
	this.messages = new ArrayList<>();
    }

    public void addMessage(final String newMessage) {
	this.messages.add(newMessage);
    }

    public String getActionButtonText() {
	return this.actionButtonText;
    }

    public Icon getMainImage() {
	return this.mainImage;
    }

    public int getMessageCount() {
	return this.messages.size();
    }

    public Iterable<String> getMessages() {
	return this.messages;
    }

    public Image getSystemIcon() {
	return this.systemIcon;
    }

    // Methods
    public String getTitle() {
	return this.title;
    }
}
