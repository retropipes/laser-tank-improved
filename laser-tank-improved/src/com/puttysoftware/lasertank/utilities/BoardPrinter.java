/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.lasertank.strings.ErrorString;
import com.puttysoftware.lasertank.strings.MenuString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.strings.global.UntranslatedString;

public class BoardPrinter {
    public static void printBoard(final Container c) {
	try {
	    final Dimension d = c.getPreferredSize();
	    final BufferedImage bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
	    c.paintComponents(bi.createGraphics());
	    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageIO.write(bi, GlobalLoader.loadUntranslated(UntranslatedString.IMAGE_FORMAT_PNG), baos);
	    final byte[] data = baos.toByteArray();
	    final ByteArrayInputStream bais = new ByteArrayInputStream(data);
	    final PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
	    final DocFlavor flavor = DocFlavor.INPUT_STREAM.PNG;
	    final PrinterJob pj = PrinterJob.getPrinterJob();
	    final boolean okay = pj.printDialog(pras);
	    if (okay) {
		final PrintService service = pj.getPrintService();
		final DocPrintJob job = service.createPrintJob();
		final DocAttributeSet das = new HashDocAttributeSet();
		final Doc doc = new SimpleDoc(bais, flavor, das);
		job.print(doc, pras);
	    }
	} catch (final IOException ioe) {
	    CommonDialogs.showErrorDialog(StringLoader.loadError(ErrorString.PRINTING_FAILURE),
		    StringLoader.loadMenu(MenuString.ITEM_PRINT_GAMEBOARD));
	} catch (final PrintException pe) {
	    CommonDialogs.showErrorDialog(StringLoader.loadError(ErrorString.PRINTING_FAILURE),
		    StringLoader.loadMenu(MenuString.ITEM_PRINT_GAMEBOARD));
	} catch (final NullPointerException npe) {
	    CommonDialogs.showErrorDialog(StringLoader.loadError(ErrorString.PRINTING_FAILURE),
		    StringLoader.loadMenu(MenuString.ITEM_PRINT_GAMEBOARD));
	}
    }

    private BoardPrinter() {
	// Do nothing
    }
}
