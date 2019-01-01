package com.puttysoftware.ltremix.utilities;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

public class ProgressTracker {
    // Fields
    private final JFrame progressFrame;
    private final JProgressBar progressBar;
    private static final int FRAME_WIDTH = 250;
    private static final int BAR_WIDTH = 200;

    // Constructor
    public ProgressTracker(final String task) {
	this.progressFrame = new JFrame(task);
	this.progressFrame.setAlwaysOnTop(true);
	this.progressFrame.setResizable(false);
	this.progressFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	this.progressBar = new JProgressBar();
	this.progressBar.setValue(0);
	this.progressFrame.getContentPane().setLayout(new FlowLayout());
	this.progressFrame.getContentPane().add(this.progressBar);
	this.progressFrame.pack();
	final int currBarHeight = this.progressBar.getHeight();
	this.progressBar.setPreferredSize(new Dimension(ProgressTracker.BAR_WIDTH, currBarHeight));
	final int currFrameHeight = this.progressFrame.getHeight();
	this.progressFrame.setPreferredSize(new Dimension(ProgressTracker.FRAME_WIDTH, currFrameHeight));
	this.progressFrame.pack();
    }

    // Methods
    public void show() {
	this.progressFrame.setVisible(true);
    }

    public void hide() {
	this.progressFrame.setVisible(false);
    }

    public void setMaximum(final int max) {
	this.progressBar.setMaximum(max);
    }

    public void setMaximumDynamic(final int max) {
	this.progressBar.setMaximum(this.progressBar.getMaximum() + max);
    }

    public void resetProgress() {
	this.progressBar.setValue(0);
    }

    public void updateProgress() {
	this.progressBar.setValue(this.progressBar.getValue() + 1);
    }
}
