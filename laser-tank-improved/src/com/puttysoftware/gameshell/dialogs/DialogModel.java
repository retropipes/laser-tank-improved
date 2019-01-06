package com.puttysoftware.gameshell.dialogs;

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

    // Methods
    public final String getTitle() {
	return this.title;
    }

    public final String getActionButtonText() {
	return this.actionButtonText;
    }

    public final Image getSystemIcon() {
	return this.systemIcon;
    }

    public final Icon getMainImage() {
	return this.mainImage;
    }

    public final void addMessage(final String newMessage) {
	this.messages.add(newMessage);
    }

    public final int getMessageCount() {
	return this.messages.size();
    }

    public final Iterable<String> getMessages() {
	return this.messages;
    }
}
