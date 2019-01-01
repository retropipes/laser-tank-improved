package com.puttysoftware.lasertank.improved.shell.dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.lang.ref.WeakReference;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

final class DialogView {
    // Fields
    private final JFrame theFrame;

    // Constructors
    DialogView() {
	super();
	this.theFrame = new JFrame();
    }

    void hideDialog() {
	this.theFrame.setVisible(false);
    }

    void setUpGUI(final DialogModel model, final WeakReference<DialogController> controllerRef) {
	this.theFrame.setTitle(model.getTitle());
	this.theFrame.setIconImage(model.getSystemIcon());
	final Container thePane = new Container();
	final Container textPane = new Container();
	final Container buttonPane = new Container();
	final Container logoPane = new Container();
	final JButton theOK = new JButton(model.getActionButtonText());
	final JLabel miniLabel = new JLabel("", model.getMainImage(), SwingConstants.LEFT);
	miniLabel.setLabelFor(null);
	theOK.setDefaultCapable(true);
	this.theFrame.getRootPane().setDefaultButton(theOK);
	this.theFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	thePane.setLayout(new BorderLayout());
	logoPane.setLayout(new FlowLayout());
	logoPane.add(miniLabel);
	textPane.setLayout(new GridLayout(model.getMessageCount(), 1));
	final Iterable<String> messages = model.getMessages();
	for (final String message : messages) {
	    textPane.add(new JLabel(message));
	}
	buttonPane.setLayout(new FlowLayout());
	buttonPane.add(theOK);
	thePane.add(logoPane, BorderLayout.WEST);
	thePane.add(textPane, BorderLayout.CENTER);
	thePane.add(buttonPane, BorderLayout.SOUTH);
	this.theFrame.setResizable(false);
	theOK.addActionListener(controllerRef.get());
	this.theFrame.setContentPane(thePane);
	this.theFrame.pack();
    }

    // Methods
    void showDialog() {
	this.theFrame.setVisible(true);
    }
}
