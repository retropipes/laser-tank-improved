/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.puttysoftware.lasertank.arena.AbstractArena;

public class RCLGenerator {
    public static Container generateRowColumnLabels() {
	final Container outerOutputPane = new Container();
	outerOutputPane.setLayout(new BorderLayout());
	final Container rowsPane = new Container();
	rowsPane.setLayout(new BoxLayout(rowsPane, BoxLayout.Y_AXIS));
	// Generate row labels
	rowsPane.add(Box.createVerticalGlue());
	for (int r = 1; r <= AbstractArena.getMinRows(); r++) {
	    final JLabel j = new JLabel(Integer.toString(r));
	    j.setLabelFor(null);
	    j.setHorizontalAlignment(SwingConstants.RIGHT);
	    j.setVerticalAlignment(SwingConstants.CENTER);
	    rowsPane.add(j);
	    if (r < AbstractArena.getMinRows()) {
		rowsPane.add(Box.createVerticalGlue());
	    }
	}
	final Container columnsPane = new Container();
	columnsPane.setLayout(new BoxLayout(columnsPane, BoxLayout.X_AXIS));
	// Generate column labels
	columnsPane.add(Box.createHorizontalGlue());
	for (int c = 1; c <= AbstractArena.getMinColumns(); c++) {
	    final JLabel j = new JLabel(Character.toString((char) (c + 64)));
	    j.setLabelFor(null);
	    j.setHorizontalAlignment(SwingConstants.CENTER);
	    j.setVerticalAlignment(SwingConstants.BOTTOM);
	    columnsPane.add(j);
	    if (c < AbstractArena.getMinColumns()) {
		columnsPane.add(Box.createHorizontalGlue());
	    }
	}
	outerOutputPane.add(rowsPane, BorderLayout.WEST);
	outerOutputPane.add(columnsPane, BorderLayout.NORTH);
	return outerOutputPane;
    }

    // Constructor
    private RCLGenerator() {
	// Do nothing
    }
}
