/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.puttysoftware.lasertank.improved.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.resourcemanagers.ExternalMusicImporter;
import com.puttysoftware.lasertank.stringmanagers.StringConstants;
import com.puttysoftware.lasertank.stringmanagers.StringLoader;

public class Importer {
    // Fields
    static JFrame guiFrame;
    static JFrame sourceFrame;
    static JMenuBar sourceMenus;
    private static Container guiPane;
    private static JLabel logoLabel;
    private static boolean inited = false;
    private static TransferHandler handler = new TransferHandler() {
	private static final long serialVersionUID = 233255543L;

	@Override
	public boolean canImport(final TransferHandler.TransferSupport support) {
	    if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
		return false;
	    }
	    final boolean copySupported = (TransferHandler.COPY
		    & support.getSourceDropActions()) == TransferHandler.COPY;
	    if (!copySupported) {
		return false;
	    }
	    support.setDropAction(TransferHandler.COPY);
	    return true;
	}

	@Override
	public boolean importData(final TransferHandler.TransferSupport support) {
	    if (!this.canImport(support)) {
		return false;
	    }
	    final Transferable t = support.getTransferable();
	    try {
		final Object o = t.getTransferData(DataFlavor.javaFileListFlavor);
		if (o instanceof List<?>) {
		    final List<?> l = (List<?>) o;
		    for (final Object o2 : l) {
			if (o2 instanceof File) {
			    final File f = (File) o2;
			    final String ext = this.getExtension(f);
			    if (ext.equalsIgnoreCase("mod") || ext.equalsIgnoreCase("s3m")
				    || ext.equalsIgnoreCase("xm")) {
				// Import External Music
				ExternalMusicImporter.importMusic(f);
				Importer.guiFrame.setVisible(false);
				Importer.sourceFrame.setVisible(true);
				Importer.sourceFrame.setJMenuBar(Importer.sourceMenus);
			    } else {
				// Unknown file type
				CommonDialogs.showDialog(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
					StringConstants.DIALOG_STRING_IMPORT_FAILED_FILE_TYPE));
			    }
			} else {
			    // Not a file
			    CommonDialogs.showDialog(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
				    StringConstants.DIALOG_STRING_IMPORT_FAILED_NON_FILE));
			}
		    }
		}
		return true;
	    } catch (final UnsupportedFlavorException e) {
		return false;
	    } catch (final IOException e) {
		return false;
	    }
	}

	private String getExtension(final File f) {
	    String ext = null;
	    final String s = f.getName();
	    final int i = s.lastIndexOf('.');
	    if (i > 0 && i < s.length() - 1) {
		ext = s.substring(i + 1).toLowerCase();
	    }
	    return ext;
	}
    };

    private static class CloseHandler extends WindowAdapter {
	public CloseHandler() {
	    // Do nothing
	}

	@Override
	public void windowClosing(final WindowEvent we) {
	    Importer.guiFrame.setVisible(false);
	    Importer.sourceFrame.setVisible(true);
	    Importer.sourceFrame.setJMenuBar(Importer.sourceMenus);
	}
    }

    private static void init() {
	Importer.guiFrame = new JFrame(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
		StringConstants.DIALOG_STRING_IMPORT_TITLE));
	Importer.guiPane = Importer.guiFrame.getContentPane();
	Importer.guiFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	Importer.guiFrame.addWindowListener(new CloseHandler());
	Importer.guiFrame.setLayout(new GridLayout(1, 1));
	Importer.logoLabel = new JLabel(StringLoader.loadString(StringConstants.DIALOG_STRINGS_FILE,
		StringConstants.DIALOG_STRING_IMPORT_INSTRUCTIONS), null, SwingConstants.CENTER);
	Importer.logoLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
	Importer.logoLabel.setPreferredSize(new Dimension(500, 500));
	Importer.guiPane.add(Importer.logoLabel);
	Importer.guiFrame.setResizable(false);
	Importer.guiFrame.setTransferHandler(Importer.handler);
	Importer.guiFrame.pack();
	Importer.inited = true;
    }

    // Methods
    public static void showImporter(final JFrame source, final JMenuBar menus) {
	if (!Importer.inited) {
	    Importer.init();
	}
	Importer.sourceFrame = source;
	Importer.sourceMenus = menus;
	Importer.sourceFrame.setVisible(false);
	Importer.guiFrame.setJMenuBar(menus);
	Importer.guiFrame.setVisible(true);
    }

    public static boolean isImporterVisible() {
	if (!Importer.inited) {
	    Importer.init();
	}
	return Importer.guiFrame.isVisible();
    }
}
