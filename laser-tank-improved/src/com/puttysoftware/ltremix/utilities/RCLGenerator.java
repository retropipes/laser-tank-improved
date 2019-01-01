/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.utilities;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.puttysoftware.ltremix.arena.AbstractArena;
import com.puttysoftware.ltremix.resourcemanagers.ImageManager;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;

public class RCLGenerator {
    // Constructor
    private RCLGenerator() {
	// Do nothing
    }

    public static Container generateRowColumnLabels() {
	final Container outerOutputPane = new Container();
	outerOutputPane.setLayout(new BorderLayout());
	final Container rowsPane = new Container();
	rowsPane.setLayout(new BoxLayout(rowsPane, BoxLayout.Y_AXIS));
	// Generate row labels
	rowsPane.add(Box.createVerticalGlue());
	for (int r = 1; r <= AbstractArena.getMinRows(); r++) {
	    final JLabel j = new JLabel(ArenaConstants.COORDS_LIST_Y[r - 1]);
	    j.setLabelFor(null);
	    j.setPreferredSize(new Dimension(ImageManager.getMinimumGraphicSize(), ImageManager.getGraphicSize()));
	    j.setHorizontalAlignment(SwingConstants.RIGHT);
	    j.setVerticalAlignment(SwingConstants.CENTER);
	    rowsPane.add(j);
	}
	final Container columnsPane = new Container();
	columnsPane.setLayout(new BoxLayout(columnsPane, BoxLayout.X_AXIS));
	// Generate column labels
	columnsPane.add(Box.createHorizontalGlue());
	final JLabel spacer = new JLabel(StringConstants.COMMON_STRING_SPACE);
	spacer.setLabelFor(null);
	spacer.setPreferredSize(
		new Dimension(ImageManager.getMinimumGraphicSize(), ImageManager.getMinimumGraphicSize()));
	columnsPane.add(spacer);
	for (int c = 1; c <= AbstractArena.getMinColumns(); c++) {
	    final JLabel j = new JLabel(ArenaConstants.COORDS_LIST_X[c - 1]);
	    j.setLabelFor(null);
	    j.setPreferredSize(new Dimension(ImageManager.getGraphicSize(), ImageManager.getMinimumGraphicSize()));
	    j.setHorizontalAlignment(SwingConstants.CENTER);
	    j.setVerticalAlignment(SwingConstants.BOTTOM);
	    columnsPane.add(j);
	}
	outerOutputPane.add(rowsPane, BorderLayout.WEST);
	outerOutputPane.add(columnsPane, BorderLayout.NORTH);
	return outerOutputPane;
    }
}
