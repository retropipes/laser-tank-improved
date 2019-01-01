package com.puttysoftware.lasertank;

import javax.swing.JMenu;

public interface MenuSection {
    public void enableModeCommands();

    public void disableModeCommands();

    public void setInitialState();

    public JMenu createCommandsMenu();

    public void attachAccelerators(final Accelerators accel);

    public void enableLoadedCommands();

    public void disableLoadedCommands();

    public void enableDirtyCommands();

    public void disableDirtyCommands();
}
