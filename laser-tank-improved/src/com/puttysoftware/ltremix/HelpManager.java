/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.puttysoftware.lasertank.improved.help.GraphicalHelpViewer;
import com.puttysoftware.lasertank.improved.images.BufferedImageIcon;
import com.puttysoftware.ltremix.resourcemanagers.ImageManager;
import com.puttysoftware.ltremix.resourcemanagers.LogoManager;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;
import com.puttysoftware.ltremix.stringmanagers.StringLoader;
import com.puttysoftware.ltremix.utilities.ArenaObjectList;

class HelpManager {
    // Fields
    private JFrame helpFrame;
    GraphicalHelpViewer hv;
    private boolean inited = false;

    // Constructors
    public HelpManager() {
	// Do nothing
    }

    // Methods
    void showHelp() {
	this.initHelp();
	this.helpFrame.setVisible(true);
    }

    void activeLanguageChanged() {
	this.inited = false;
    }

    private void initHelp() {
	if (!this.inited) {
	    final ButtonHandler buttonHandler = new ButtonHandler();
	    final ArenaObjectList objectList = new ArenaObjectList();
	    final String[] objectNames = objectList.getAllDescriptions();
	    final BufferedImageIcon[] objectAppearances = objectList.getAllEditorAppearances();
	    this.hv = new GraphicalHelpViewer(objectAppearances, objectNames, new Color(223, 223, 223));
	    final JButton export = new JButton(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
		    StringConstants.DIALOG_STRING_EXPORT_BUTTON));
	    export.addActionListener(buttonHandler);
	    this.helpFrame = new JFrame(StringLoader.loadString(StringConstants.NOTL_STRINGS_FILE,
		    StringConstants.NOTL_STRING_PROGRAM_NAME) + StringConstants.COMMON_STRING_SPACE
		    + StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE, StringConstants.DIALOG_STRING_HELP));
	    final Image iconlogo = LogoManager.getIconLogo();
	    this.helpFrame.setIconImage(iconlogo);
	    this.helpFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	    this.helpFrame.setLayout(new BorderLayout());
	    this.helpFrame.add(this.hv.getHelp(), BorderLayout.CENTER);
	    this.helpFrame.add(export, BorderLayout.SOUTH);
	    this.hv.setHelpSize(ImageManager.MAX_WINDOW_SIZE, ImageManager.MAX_WINDOW_SIZE);
	    this.helpFrame.pack();
	    this.helpFrame.setResizable(false);
	    this.inited = true;
	}
    }

    private class ButtonHandler implements ActionListener {
	public ButtonHandler() {
	    // Do nothing
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
	    HelpManager.this.hv.exportHelp();
	}
    }
}
