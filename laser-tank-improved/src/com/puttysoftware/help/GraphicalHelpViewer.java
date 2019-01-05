package com.puttysoftware.help;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.fileio.FilenameChecker;
import com.puttysoftware.images.BufferedImageIcon;

public final class GraphicalHelpViewer {
    private static String getExtension(final File f) {
	String ext = null;
	final String s = f.getName();
	final int i = s.lastIndexOf('.');
	if (i > 0 && i < s.length() - 1) {
	    ext = s.substring(i + 1).toLowerCase();
	}
	return ext;
    }

    private static String getNameWithoutExtension(final File f) {
	String ext = null;
	final String s = f.getAbsolutePath();
	final int i = s.lastIndexOf('.');
	if (i > 0 && i < s.length() - 1) {
	    ext = s.substring(0, i);
	} else {
	    ext = s;
	}
	return ext;
    }

    // Fields
    private final Container helpContainer;
    private final Container choiceContainer;
    private final JScrollPane scrollPane;
    private final Color fill;

    // Constructor
    public GraphicalHelpViewer(final BufferedImageIcon[] pictures, final String[] descriptions, final Color fillColor) {
	this.helpContainer = new Container();
	this.helpContainer.setLayout(new BorderLayout());
	this.choiceContainer = new Container();
	this.scrollPane = new JScrollPane(this.choiceContainer);
	this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	this.scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	this.helpContainer.add(this.scrollPane, BorderLayout.CENTER);
	this.fill = fillColor;
	this.helpContainer.setBackground(fillColor);
	this.choiceContainer.setBackground(fillColor);
	this.scrollPane.setBackground(fillColor);
	this.updateHelp(pictures, descriptions);
    }

    public void exportHelp() {
	String filename = "";
	String fileOnly = "\\";
	String extension;
	final FileDialog fc = new FileDialog((JFrame) null, "Export Help", FileDialog.SAVE);
	while (!FilenameChecker.isFilenameOK(fileOnly)) {
	    fc.setVisible(true);
	    if (fc.getFile() != null && fc.getDirectory() != null) {
		final File file = new File(fc.getDirectory() + fc.getFile());
		extension = GraphicalHelpViewer.getExtension(file);
		filename = file.getAbsolutePath();
		final String dirOnly = fc.getDirectory();
		fileOnly = filename.substring(dirOnly.length() + 1);
		if (!FilenameChecker.isFilenameOK(fileOnly)) {
		    CommonDialogs.showErrorDialog("The file name you entered contains illegal characters.\n"
			    + "These characters are not allowed: /?<>\\:|\"\n"
			    + "Files named con, nul, or prn are illegal, as are files\n"
			    + "named com1 through com9 and lpt1 through lpt9.", "Save");
		} else {
		    if (extension != null) {
			if (!extension.equals("png")) {
			    filename = GraphicalHelpViewer.getNameWithoutExtension(file) + ".png";
			}
		    } else {
			filename += ".png";
		    }
		    final Container c = this.choiceContainer;
		    final Dimension d = c.getPreferredSize();
		    final BufferedImage bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
		    for (int x = 0; x < d.width; x++) {
			for (int y = 0; y < d.height; y++) {
			    bi.setRGB(x, y, this.fill.getRGB());
			}
		    }
		    c.paintComponents(bi.createGraphics());
		    try {
			ImageIO.write(bi, "PNG", new File(filename));
			CommonDialogs.showDialog("Export Successful!");
		    } catch (final IOException io) {
			CommonDialogs.showDialog("Export Failed!");
		    }
		}
	    } else {
		break;
	    }
	}
    }

    // Methods
    public Container getHelp() {
	return this.helpContainer;
    }

    public void setHelpSize(final int horz, final int vert) {
	this.helpContainer.setPreferredSize(new Dimension(horz, vert));
	this.scrollPane.setPreferredSize(new Dimension(horz, vert));
    }

    public void updateHelp(final BufferedImageIcon[] newImages, final String[] newNames) {
	BufferedImageIcon[] choices;
	String[] choiceNames;
	JLabel[] choiceArray;
	choices = newImages;
	choiceNames = newNames;
	this.choiceContainer.removeAll();
	this.choiceContainer.setLayout(new GridLayout(choices.length, 1));
	choiceArray = new JLabel[choices.length];
	for (int x = 0; x < choices.length; x++) {
	    choiceArray[x] = new JLabel(choiceNames[x], choices[x], SwingConstants.LEFT);
	    choiceArray[x].setOpaque(true);
	    choiceArray[x].setBackground(this.fill);
	    this.choiceContainer.add(choiceArray[x]);
	}
    }
}
