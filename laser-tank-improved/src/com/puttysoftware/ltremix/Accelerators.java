/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix;

import javax.swing.KeyStroke;

public abstract class Accelerators {
    public KeyStroke fileNewAccel, fileOpenAccel, fileCloseAccel, fileSaveAccel, fileSaveAsAccel, filePreferencesAccel,
	    filePrintAccel, fileExitAccel;
    public KeyStroke playPlayArenaAccel, playEditArenaAccel;
    public KeyStroke gameResetAccel, gameShowTableAccel;
    public KeyStroke editorUndoAccel, editorRedoAccel, editorCutLevelAccel, editorCopyLevelAccel, editorPasteLevelAccel,
	    editorInsertLevelFromClipboardAccel, editorClearHistoryAccel, editorGoToLocationAccel,
	    editorUpOneLevelAccel, editorDownOneLevelAccel, editorUpOneFloorAccel, editorDownOneFloorAccel;

    Accelerators() {
	// Do nothing
    }
}
