/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena;

import java.io.IOException;

import com.puttysoftware.fileio.XMLFileReader;
import com.puttysoftware.fileio.XMLFileWriter;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.utilities.FormatConstants;

public class SuffixHandler implements AbstractSuffixIO {
    @Override
    public void readSuffix(final XMLFileReader reader, final int formatVersion) throws IOException {
	if (FormatConstants.isFormatVersionValidGeneration1(formatVersion)) {
	    LaserTank.getApplication().getGameManager().loadGameHookG1(reader);
	} else if (FormatConstants.isFormatVersionValidGeneration2(formatVersion)) {
	    LaserTank.getApplication().getGameManager().loadGameHookG2(reader);
	} else if (FormatConstants.isFormatVersionValidGeneration3(formatVersion)) {
	    LaserTank.getApplication().getGameManager().loadGameHookG3(reader);
	} else if (FormatConstants.isFormatVersionValidGeneration4(formatVersion)) {
	    LaserTank.getApplication().getGameManager().loadGameHookG4(reader);
	} else if (FormatConstants.isFormatVersionValidGeneration5(formatVersion)) {
	    LaserTank.getApplication().getGameManager().loadGameHookG5(reader);
	} else if (FormatConstants.isFormatVersionValidGeneration6(formatVersion)) {
	    LaserTank.getApplication().getGameManager().loadGameHookG6(reader);
	}
    }

    @Override
    public void writeSuffix(final XMLFileWriter writer) throws IOException {
	LaserTank.getApplication().getGameManager().saveGameHook(writer);
    }
}
