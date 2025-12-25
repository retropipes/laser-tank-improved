/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena;

import java.io.IOException;

import com.puttysoftware.fileio.XMLFileReader;
import com.puttysoftware.fileio.XMLFileWriter;

public interface AbstractSuffixIO {
    void readSuffix(XMLFileReader reader, int formatVersion) throws IOException;

    void writeSuffix(XMLFileWriter writer) throws IOException;
}
