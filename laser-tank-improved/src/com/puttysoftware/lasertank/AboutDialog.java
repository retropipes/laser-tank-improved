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

import com.puttysoftware.lasertank.strings.CommonString;
import com.puttysoftware.lasertank.strings.DialogString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.strings.global.UntranslatedString;

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
		if (cmd.equals(StringLoader.loadDialog(DialogString.OK_BUTTON))) {
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
	this.aboutFrame = new JFrame(
		StringLoader.loadDialog(DialogString.ABOUT) + StringLoader.loadCommon(CommonString.SPACE)
			+ GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME));
	aboutPane = new Container();
	textPane = new Container();
	buttonPane = new Container();
	logoPane = new Container();
	aboutOK = new JButton(StringLoader.loadDialog(DialogString.OK_BUTTON));
	aboutOK.setDefaultCapable(true);
	this.aboutFrame.getRootPane().setDefaultButton(aboutOK);
	this.aboutFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	aboutPane.setLayout(new BorderLayout());
	logoPane.setLayout(new FlowLayout());
	textPane.setLayout(new GridLayout(4, 1));
	textPane.add(new JLabel(GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME)
		+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadDialog(DialogString.VERSION)
		+ StringLoader.loadCommon(CommonString.SPACE) + ver));
	textPane.add(new JLabel(StringLoader.loadDialog(DialogString.AUTHOR)
		+ GlobalLoader.loadUntranslated(UntranslatedString.AUTHOR_NAME)));
	textPane.add(new JLabel(StringLoader.loadDialog(DialogString.WEB_SITE)
		+ GlobalLoader.loadUntranslated(UntranslatedString.GAME_WEB_URL)));
	textPane.add(new JLabel(StringLoader.loadDialog(DialogString.BUG_REPORTS)
		+ GlobalLoader.loadUntranslated(UntranslatedString.GAME_EMAIL)));
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