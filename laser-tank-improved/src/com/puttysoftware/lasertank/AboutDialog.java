/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;

public class AboutDialog implements AboutHandler {
    private class EventHandler implements ActionListener {
	public EventHandler() {
	    // Do nothing
	}

	// Handle buttons
	@Override
	public void actionPerformed(final ActionEvent e) {
	    try {
		final AboutDialog ad = AboutDialog.this;
		final String cmd = e.getActionCommand();
		if (cmd.equals(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
			StringConstants.DIALOG_STRING_OK_BUTTON))) {
		    ad.hideAboutDialog();
		}
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}
    }

    // Fields
    private JFrame aboutFrame;

    // Constructors
    AboutDialog(final String ver) {
	this.setUpGUI(ver);
    }

    void hideAboutDialog() {
	this.aboutFrame.setVisible(false);
    }

    private void setUpGUI(final String ver) {
	Container aboutPane, textPane, buttonPane, logoPane;
	JButton aboutOK;
	EventHandler handler;
	handler = new EventHandler();
	this.aboutFrame = new JFrame(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
		StringConstants.DIALOG_STRING_ABOUT) + StringConstants.COMMON_STRING_SPACE
		+ StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_PROGRAM_NAME));
	aboutPane = new Container();
	textPane = new Container();
	buttonPane = new Container();
	logoPane = new Container();
	aboutOK = new JButton(
		StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE, StringConstants.DIALOG_STRING_OK_BUTTON));
	aboutOK.setDefaultCapable(true);
	this.aboutFrame.getRootPane().setDefaultButton(aboutOK);
	this.aboutFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	aboutPane.setLayout(new BorderLayout());
	logoPane.setLayout(new FlowLayout());
	textPane.setLayout(new GridLayout(4, 1));
	textPane.add(new JLabel(
		StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_PROGRAM_NAME)
			+ StringConstants.COMMON_STRING_SPACE + StringLoader
				.loadString(StringConstants.DIALOG_STRINGS_FILE, StringConstants.DIALOG_STRING_VERSION)
			+ StringConstants.COMMON_STRING_SPACE + ver));
	textPane.add(new JLabel(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
		StringConstants.DIALOG_STRING_AUTHOR)
		+ StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_AUTHOR_NAME)));
	textPane.add(new JLabel(
		StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE, StringConstants.DIALOG_STRING_WEB_SITE)
			+ StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
				StringConstants.NOTL_STRING_GAME_WEB_URL)));
	textPane.add(new JLabel(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
		StringConstants.DIALOG_STRING_BUG_REPORTS)
		+ StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE, StringConstants.NOTL_STRING_GAME_EMAIL)));
	buttonPane.setLayout(new FlowLayout());
	buttonPane.add(aboutOK);
	aboutPane.add(logoPane, BorderLayout.WEST);
	aboutPane.add(textPane, BorderLayout.CENTER);
	aboutPane.add(buttonPane, BorderLayout.SOUTH);
	this.aboutFrame.setResizable(false);
	aboutOK.addActionListener(handler);
	this.aboutFrame.setContentPane(aboutPane);
	this.aboutFrame.pack();
    }

    // Methods
    public void showAboutDialog() {
	this.aboutFrame.setVisible(true);
    }

    @Override
    public void handleAbout(AboutEvent e) {
	this.aboutFrame.setVisible(true);
    }
}