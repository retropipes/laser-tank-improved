package com.puttysoftware.lasertank;

import javax.swing.JMenu;

public interface MenuSection {
    void attachAccelerators(final Accelerators accel);

    JMenu createCommandsMenu();

    void disableDirtyCommands();

    void disableLoadedCommands();

    void disableModeCommands();

    void enableDirtyCommands();

    void enableLoadedCommands();

    void enableModeCommands();

    void setInitialState();
    
    void setUp(boolean force);
    
    void tearDown();
}
