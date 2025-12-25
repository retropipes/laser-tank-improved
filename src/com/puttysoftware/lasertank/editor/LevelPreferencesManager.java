/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.strings.DialogString;
import com.puttysoftware.lasertank.strings.EditorString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.utilities.DifficultyConstants;

class LevelPreferencesManager {
    private class EventHandler implements ActionListener, WindowListener {
	public EventHandler() {
	    // Do nothing
	}

	// Handle buttons
	@Override
	public void actionPerformed(final ActionEvent e) {
	    try {
		final LevelPreferencesManager lpm = LevelPreferencesManager.this;
		final String cmd = e.getActionCommand();
		if (cmd.equals(StringLoader.loadDialog(DialogString.OK_BUTTON))) {
		    lpm.setPrefs();
		    lpm.hidePrefs();
		} else if (cmd.equals(StringLoader.loadDialog(DialogString.CANCEL_BUTTON))) {
		    lpm.hidePrefs();
		}
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}

	@Override
	public void windowActivated(final WindowEvent e) {
	    // Do nothing
	}

	@Override
	public void windowClosed(final WindowEvent e) {
	    // Do nothing
	}

	@Override
	public void windowClosing(final WindowEvent e) {
	    final LevelPreferencesManager pm = LevelPreferencesManager.this;
	    pm.hidePrefs();
	}

	@Override
	public void windowDeactivated(final WindowEvent e) {
	    // Do nothing
	}

	@Override
	public void windowDeiconified(final WindowEvent e) {
	    // Do nothing
	}

	@Override
	public void windowIconified(final WindowEvent e) {
	    // Do nothing
	}

	// handle window
	@Override
	public void windowOpened(final WindowEvent e) {
	    // Do nothing
	}
    }

    // Fields
    private JFrame prefFrame;
    private JCheckBox horizontalWrap;
    private JCheckBox verticalWrap;
    private JCheckBox thirdWrap;
    private JTextField name;
    private JTextField author;
    private JTextArea hint;
    private JComboBox<String> difficulty;
    private JCheckBox moveShoot;

    // Constructors
    public LevelPreferencesManager() {
	this.setUpGUI();
    }

    void hidePrefs() {
	this.prefFrame.setVisible(false);
	LaserTank.getApplication().getEditor().enableOutput();
	LaserTank.getApplication().getArenaManager().setDirty(true);
	LaserTank.getApplication().getEditor().redrawEditor();
    }

    private void loadPrefs() {
	final AbstractArena m = LaserTank.getApplication().getArenaManager().getArena();
	this.horizontalWrap.setSelected(m.isHorizontalWraparoundEnabled());
	this.verticalWrap.setSelected(m.isVerticalWraparoundEnabled());
	this.thirdWrap.setSelected(m.isThirdDimensionWraparoundEnabled());
	this.name.setText(m.getName());
	this.author.setText(m.getAuthor());
	this.hint.setText(m.getHint());
	this.difficulty.setSelectedIndex(m.getDifficulty() - 1);
	this.moveShoot.setSelected(m.isMoveShootAllowedThisLevel());
    }

    void setPrefs() {
	final AbstractArena m = LaserTank.getApplication().getArenaManager().getArena();
	if (this.horizontalWrap.isSelected()) {
	    m.enableHorizontalWraparound();
	} else {
	    m.disableHorizontalWraparound();
	}
	if (this.verticalWrap.isSelected()) {
	    m.enableVerticalWraparound();
	} else {
	    m.disableVerticalWraparound();
	}
	if (this.thirdWrap.isSelected()) {
	    m.enableThirdDimensionWraparound();
	} else {
	    m.disableThirdDimensionWraparound();
	}
	m.setName(this.name.getText());
	m.setAuthor(this.author.getText());
	m.setHint(this.hint.getText());
	m.setDifficulty(this.difficulty.getSelectedIndex() + 1);
	m.setMoveShootAllowedThisLevel(this.moveShoot.isSelected());
    }

    private void setUpGUI() {
	Container mainPrefPane, contentPane, buttonPane;
	JButton prefsOK, prefsCancel;
	final EventHandler handler = new EventHandler();
	this.prefFrame = new JFrame(StringLoader.loadEditor(EditorString.LEVEL_PREFERENCES));
	mainPrefPane = new Container();
	contentPane = new Container();
	buttonPane = new Container();
	prefsOK = new JButton(StringLoader.loadDialog(DialogString.OK_BUTTON));
	prefsOK.setDefaultCapable(true);
	this.prefFrame.getRootPane().setDefaultButton(prefsOK);
	prefsCancel = new JButton(StringLoader.loadDialog(DialogString.CANCEL_BUTTON));
	prefsCancel.setDefaultCapable(false);
	this.horizontalWrap = new JCheckBox(StringLoader.loadEditor(EditorString.ENABLE_HORIZONTAL_WRAP_AROUND), false);
	this.verticalWrap = new JCheckBox(StringLoader.loadEditor(EditorString.ENABLE_VERTICAL_WRAP_AROUND), false);
	this.thirdWrap = new JCheckBox(StringLoader.loadEditor(EditorString.ENABLE_THIRD_DIMENSION_WRAP_AROUND), false);
	this.name = new JTextField();
	this.author = new JTextField();
	this.hint = new JTextArea(8, 32);
	this.difficulty = new JComboBox<>(DifficultyConstants.getDifficultyNames());
	this.moveShoot = new JCheckBox(StringLoader.loadEditor(EditorString.ENABLE_MOVE_SHOOT), true);
	this.prefFrame.setContentPane(mainPrefPane);
	this.prefFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	this.prefFrame.addWindowListener(handler);
	mainPrefPane.setLayout(new BorderLayout());
	this.prefFrame.setResizable(false);
	contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
	contentPane.add(this.horizontalWrap);
	contentPane.add(this.verticalWrap);
	contentPane.add(this.thirdWrap);
	contentPane.add(new JLabel(StringLoader.loadEditor(EditorString.LEVEL_NAME)));
	contentPane.add(this.name);
	contentPane.add(new JLabel(StringLoader.loadEditor(EditorString.LEVEL_AUTHOR)));
	contentPane.add(this.author);
	contentPane.add(new JLabel(StringLoader.loadEditor(EditorString.LEVEL_HINT)));
	contentPane.add(this.hint);
	contentPane.add(new JLabel(StringLoader.loadEditor(EditorString.LEVEL_DIFFICULTY)));
	contentPane.add(this.difficulty);
	contentPane.add(this.moveShoot);
	buttonPane.setLayout(new FlowLayout());
	buttonPane.add(prefsOK);
	buttonPane.add(prefsCancel);
	mainPrefPane.add(contentPane, BorderLayout.CENTER);
	mainPrefPane.add(buttonPane, BorderLayout.SOUTH);
	prefsOK.addActionListener(handler);
	prefsCancel.addActionListener(handler);
	this.prefFrame.pack();
    }

    // Methods
    void showPrefs() {
	this.loadPrefs();
	LaserTank.getApplication().getEditor().disableOutput();
	this.prefFrame.setVisible(true);
    }
}
