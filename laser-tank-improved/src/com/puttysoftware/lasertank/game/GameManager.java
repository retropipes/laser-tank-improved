/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.game;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import com.puttysoftware.dialogs.CommonDialogs;
import com.puttysoftware.fileio.XMLFileReader;
import com.puttysoftware.fileio.XMLFileWriter;
import com.puttysoftware.lasertank.Application;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.MenuManager;
import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.arena.ArenaManager;
import com.puttysoftware.lasertank.arena.HistoryStatus;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractCharacter;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.lasertank.arena.objects.AntiTankDisguise;
import com.puttysoftware.lasertank.arena.objects.Empty;
import com.puttysoftware.lasertank.arena.objects.PowerfulTank;
import com.puttysoftware.lasertank.arena.objects.Tank;
import com.puttysoftware.lasertank.cheats.CheatManager;
import com.puttysoftware.lasertank.prefs.PreferencesManager;
import com.puttysoftware.lasertank.resourcemanagers.ImageManager;
import com.puttysoftware.lasertank.resourcemanagers.SoundConstants;
import com.puttysoftware.lasertank.resourcemanagers.SoundManager;
import com.puttysoftware.lasertank.strings.CommonString;
import com.puttysoftware.lasertank.strings.DialogString;
import com.puttysoftware.lasertank.strings.ErrorString;
import com.puttysoftware.lasertank.strings.GameString;
import com.puttysoftware.lasertank.strings.MenuString;
import com.puttysoftware.lasertank.strings.StringLoader;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.strings.global.UntranslatedString;
import com.puttysoftware.lasertank.utilities.ActionConstants;
import com.puttysoftware.lasertank.utilities.AlreadyDeadException;
import com.puttysoftware.lasertank.utilities.ArenaConstants;
import com.puttysoftware.lasertank.utilities.CustomDialogs;
import com.puttysoftware.lasertank.utilities.DifficultyConstants;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.DirectionResolver;
import com.puttysoftware.lasertank.utilities.DrawGrid;
import com.puttysoftware.lasertank.utilities.Extension;
import com.puttysoftware.lasertank.utilities.InvalidArenaException;
import com.puttysoftware.lasertank.utilities.LaserTypeConstants;
import com.puttysoftware.lasertank.utilities.RCLGenerator;
import com.puttysoftware.lasertank.utilities.RangeTypeConstants;
import com.puttysoftware.lasertank.utilities.TankInventory;
import com.puttysoftware.lasertank.utilities.TypeConstants;

public class GameManager {
    private class DifficultyEventHandler implements ActionListener, WindowListener {
	public DifficultyEventHandler() {
	    // Do nothing
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
	    final String cmd = e.getActionCommand();
	    final GameManager gm = GameManager.this;
	    if (cmd.equals(StringLoader.loadDialog(DialogString.OK_BUTTON))) {
		gm.okButtonClicked();
	    } else {
		gm.cancelButtonClicked();
	    }
	}

	@Override
	public void windowActivated(final WindowEvent e) {
	    // Ignore
	}

	@Override
	public void windowClosed(final WindowEvent e) {
	    // Ignore
	}

	@Override
	public void windowClosing(final WindowEvent e) {
	    GameManager.this.cancelButtonClicked();
	}

	@Override
	public void windowDeactivated(final WindowEvent e) {
	    // Ignore
	}

	@Override
	public void windowDeiconified(final WindowEvent e) {
	    // Ignore
	}

	@Override
	public void windowIconified(final WindowEvent e) {
	    // Ignore
	}

	@Override
	public void windowOpened(final WindowEvent e) {
	    // Ignore
	}
    }

    private class EventHandler implements KeyListener, WindowListener, MouseListener {
	public EventHandler() {
	    // Do nothing
	}

	public void handleBlueLasers() {
	    try {
		final GameManager gm = GameManager.this;
		gm.setLaserType(LaserTypeConstants.LASER_TYPE_BLUE);
		final int px = gm.getPlayerManager().getPlayerLocationX();
		final int py = gm.getPlayerManager().getPlayerLocationY();
		GameManager.this.fireLaser(px, py, gm.tank);
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}

	public void handleBombs() {
	    try {
		final GameManager gm = GameManager.this;
		if (!gm.getCheatStatus(GameManager.CHEAT_BOMBS) && TankInventory.getBombsLeft() > 0
			|| gm.getCheatStatus(GameManager.CHEAT_BOMBS)) {
		    TankInventory.fireBomb();
		    gm.fireRange();
		} else {
		    CommonDialogs.showDialog(StringLoader.loadGame(GameString.OUT_OF_BOMBS));
		}
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}

	public void handleBoosts(final KeyEvent e) {
	    try {
		final GameManager gm = GameManager.this;
		if (!gm.getCheatStatus(GameManager.CHEAT_BOOSTS) && TankInventory.getBoostsLeft() > 0
			|| gm.getCheatStatus(GameManager.CHEAT_BOOSTS)) {
		    TankInventory.fireBoost();
		    final int keyCode = e.getKeyCode();
		    switch (keyCode) {
		    case KeyEvent.VK_LEFT:
			gm.updatePositionRelative(-2, 0);
			break;
		    case KeyEvent.VK_DOWN:
			gm.updatePositionRelative(0, 2);
			break;
		    case KeyEvent.VK_RIGHT:
			gm.updatePositionRelative(2, 0);
			break;
		    case KeyEvent.VK_UP:
			gm.updatePositionRelative(0, -2);
			break;
		    default:
			break;
		    }
		} else {
		    CommonDialogs.showDialog(StringLoader.loadGame(GameString.OUT_OF_BOOSTS));
		}
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}

	public void handleDisruptors() {
	    try {
		final GameManager gm = GameManager.this;
		gm.setLaserType(LaserTypeConstants.LASER_TYPE_DISRUPTOR);
		final int px = gm.getPlayerManager().getPlayerLocationX();
		final int py = gm.getPlayerManager().getPlayerLocationY();
		GameManager.this.fireLaser(px, py, gm.tank);
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}

	public void handleHeatBombs() {
	    try {
		final GameManager gm = GameManager.this;
		if (!gm.getCheatStatus(GameManager.CHEAT_HEAT_BOMBS) && TankInventory.getHeatBombsLeft() > 0
			|| gm.getCheatStatus(GameManager.CHEAT_HEAT_BOMBS)) {
		    TankInventory.fireHeatBomb();
		    gm.fireRange();
		} else {
		    CommonDialogs.showDialog(StringLoader.loadGame(GameString.OUT_OF_HEAT_BOMBS));
		}
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}

	public void handleIceBombs() {
	    try {
		final GameManager gm = GameManager.this;
		if (!gm.getCheatStatus(GameManager.CHEAT_ICE_BOMBS) && TankInventory.getIceBombsLeft() > 0
			|| gm.getCheatStatus(GameManager.CHEAT_ICE_BOMBS)) {
		    TankInventory.fireIceBomb();
		    gm.fireRange();
		} else {
		    CommonDialogs.showDialog(StringLoader.loadGame(GameString.OUT_OF_ICE_BOMBS));
		}
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}

	private void handleKeystrokes(final KeyEvent e) {
	    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
		if (e.isAltDown() || e.isAltGraphDown() || e.isControlDown()) {
		    if (GameManager.this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_MISSILES) {
			this.handleMissiles();
		    } else if (GameManager.this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_STUNNERS) {
			this.handleStunners();
		    } else if (GameManager.this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_BLUE_LASERS) {
			this.handleBlueLasers();
		    } else if (GameManager.this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_DISRUPTORS) {
			this.handleDisruptors();
		    }
		} else {
		    this.handleLasers();
		}
	    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
		if (e.isAltDown() || e.isAltGraphDown() || e.isControlDown()) {
		    if (GameManager.this.otherRangeMode == GameManager.OTHER_RANGE_MODE_BOMBS) {
			this.handleBombs();
		    } else if (GameManager.this.otherRangeMode == GameManager.OTHER_RANGE_MODE_HEAT_BOMBS) {
			this.handleHeatBombs();
		    } else if (GameManager.this.otherRangeMode == GameManager.OTHER_RANGE_MODE_ICE_BOMBS) {
			this.handleIceBombs();
		    }
		}
	    } else {
		final Direction currDir = GameManager.this.tank.getDirection();
		final Direction newDir = this.mapKeyToDirection(e);
		if (currDir != newDir) {
		    this.handleTurns(newDir);
		} else {
		    if (e.isAltDown() || e.isAltGraphDown() || e.isControlDown()) {
			if (GameManager.this.otherToolMode == GameManager.OTHER_TOOL_MODE_BOOSTS) {
			    this.handleBoosts(e);
			} else if (GameManager.this.otherToolMode == GameManager.OTHER_TOOL_MODE_MAGNETS) {
			    this.handleMagnets(e);
			}
		    } else {
			this.handleMovement(e);
		    }
		}
	    }
	}

	public void handleLasers() {
	    try {
		final GameManager gm = GameManager.this;
		gm.setLaserType(LaserTypeConstants.LASER_TYPE_GREEN);
		final int px = gm.getPlayerManager().getPlayerLocationX();
		final int py = gm.getPlayerManager().getPlayerLocationY();
		GameManager.this.fireLaser(px, py, gm.tank);
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}

	public void handleMagnets(final KeyEvent e) {
	    try {
		final GameManager gm = GameManager.this;
		if (!gm.getCheatStatus(GameManager.CHEAT_MAGNETS) && TankInventory.getMagnetsLeft() > 0
			|| gm.getCheatStatus(GameManager.CHEAT_MAGNETS)) {
		    TankInventory.fireMagnet();
		    final int keyCode = e.getKeyCode();
		    switch (keyCode) {
		    case KeyEvent.VK_LEFT:
			gm.updatePositionRelative(-3, 0);
			break;
		    case KeyEvent.VK_DOWN:
			gm.updatePositionRelative(0, 3);
			break;
		    case KeyEvent.VK_RIGHT:
			gm.updatePositionRelative(3, 0);
			break;
		    case KeyEvent.VK_UP:
			gm.updatePositionRelative(0, -3);
			break;
		    default:
			break;
		    }
		} else {
		    CommonDialogs.showDialog(StringLoader.loadGame(GameString.OUT_OF_MAGNETS));
		}
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}

	public void handleMissiles() {
	    try {
		final GameManager gm = GameManager.this;
		gm.setLaserType(LaserTypeConstants.LASER_TYPE_MISSILE);
		final int px = gm.getPlayerManager().getPlayerLocationX();
		final int py = gm.getPlayerManager().getPlayerLocationY();
		GameManager.this.fireLaser(px, py, gm.tank);
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}

	public void handleMovement(final KeyEvent e) {
	    try {
		final GameManager gm = GameManager.this;
		final int keyCode = e.getKeyCode();
		switch (keyCode) {
		case KeyEvent.VK_LEFT:
		    gm.updatePositionRelative(-1, 0);
		    break;
		case KeyEvent.VK_DOWN:
		    gm.updatePositionRelative(0, 1);
		    break;
		case KeyEvent.VK_RIGHT:
		    gm.updatePositionRelative(1, 0);
		    break;
		case KeyEvent.VK_UP:
		    gm.updatePositionRelative(0, -1);
		    break;
		default:
		    break;
		}
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}

	public void handleStunners() {
	    try {
		final GameManager gm = GameManager.this;
		gm.setLaserType(LaserTypeConstants.LASER_TYPE_STUNNER);
		final int px = gm.getPlayerManager().getPlayerLocationX();
		final int py = gm.getPlayerManager().getPlayerLocationY();
		GameManager.this.fireLaser(px, py, gm.tank);
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}

	public void handleTurns(final Direction dir) {
	    try {
		final GameManager gm = GameManager.this;
		boolean fired = false;
		switch (dir) {
		case WEST:
		    gm.tank.setDirection(Direction.WEST);
		    if (!gm.isReplaying()) {
			gm.updateReplay(false, -1, 0);
		    }
		    fired = true;
		    break;
		case SOUTH:
		    gm.tank.setDirection(Direction.SOUTH);
		    if (!gm.isReplaying()) {
			gm.updateReplay(false, 0, 1);
		    }
		    fired = true;
		    break;
		case EAST:
		    gm.tank.setDirection(Direction.EAST);
		    if (!gm.isReplaying()) {
			gm.updateReplay(false, 1, 0);
		    }
		    fired = true;
		    break;
		case NORTH:
		    gm.tank.setDirection(Direction.NORTH);
		    if (!gm.isReplaying()) {
			gm.updateReplay(false, 0, -1);
		    }
		    fired = true;
		    break;
		default:
		    break;
		}
		if (fired) {
		    SoundManager.playSound(SoundConstants.SOUND_TURN);
		    gm.markTankAsDirty();
		    gm.redrawArena();
		}
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}

	@Override
	public void keyPressed(final KeyEvent e) {
	    final AbstractArena a = LaserTank.getApplication().getArenaManager().getArena();
	    if ((!a.isMoveShootAllowed() && !GameManager.this.laserActive || a.isMoveShootAllowed())
		    && !GameManager.this.moving) {
		if (!PreferencesManager.oneMove()) {
		    this.handleKeystrokes(e);
		}
	    }
	}

	@Override
	public void keyReleased(final KeyEvent e) {
	    final AbstractArena a = LaserTank.getApplication().getArenaManager().getArena();
	    if ((!a.isMoveShootAllowed() && !GameManager.this.laserActive || a.isMoveShootAllowed())
		    && !GameManager.this.moving) {
		if (PreferencesManager.oneMove()) {
		    this.handleKeystrokes(e);
		}
	    }
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	    // Do nothing
	}

	public Direction mapKeyToDirection(final KeyEvent e) {
	    final int keyCode = e.getKeyCode();
	    switch (keyCode) {
	    case KeyEvent.VK_LEFT:
		return Direction.WEST;
	    case KeyEvent.VK_DOWN:
		return Direction.SOUTH;
	    case KeyEvent.VK_RIGHT:
		return Direction.EAST;
	    case KeyEvent.VK_UP:
		return Direction.NORTH;
	    default:
		return Direction.INVALID;
	    }
	}

	public Direction mapMouseToDirection(final MouseEvent me) {
	    final GameManager gm = GameManager.this;
	    final int x = me.getX();
	    final int y = me.getY();
	    final int px = gm.getPlayerManager().getPlayerLocationX();
	    final int py = gm.getPlayerManager().getPlayerLocationY();
	    final int destX = (int) Math.signum(x / ImageManager.getGraphicSize() - px);
	    final int destY = (int) Math.signum(y / ImageManager.getGraphicSize() - py);
	    return DirectionResolver.resolveRelative(destX, destY);
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	    try {
		final GameManager gm = GameManager.this;
		if (e.getButton() == MouseEvent.BUTTON1) {
		    // Move
		    final Direction dir = this.mapMouseToDirection(e);
		    final Direction tankDir = gm.tank.getDirection();
		    if (tankDir != dir) {
			this.handleTurns(dir);
		    } else {
			final int x = e.getX();
			final int y = e.getY();
			final int px = gm.getPlayerManager().getPlayerLocationX();
			final int py = gm.getPlayerManager().getPlayerLocationY();
			final int destX = (int) Math.signum(x / ImageManager.getGraphicSize() - px);
			final int destY = (int) Math.signum(y / ImageManager.getGraphicSize() - py);
			gm.updatePositionRelative(destX, destY);
		    }
		} else if (e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3) {
		    // Fire Laser
		    gm.setLaserType(LaserTypeConstants.LASER_TYPE_GREEN);
		    final int px = gm.getPlayerManager().getPlayerLocationX();
		    final int py = gm.getPlayerManager().getPlayerLocationY();
		    gm.fireLaser(px, py, gm.tank);
		}
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	    // Do nothing
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	    // Do nothing
	}

	// handle mouse
	@Override
	public void mousePressed(final MouseEvent e) {
	    // Do nothing
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	    // Do nothing
	}

	// Handle windows
	@Override
	public void windowActivated(final WindowEvent we) {
	    // Do nothing
	}

	@Override
	public void windowClosed(final WindowEvent we) {
	    // Do nothing
	}

	@Override
	public void windowClosing(final WindowEvent we) {
	    try {
		final Application app = LaserTank.getApplication();
		boolean success = false;
		int status = 0;
		if (app.getArenaManager().getDirty()) {
		    status = ArenaManager.showSaveDialog();
		    if (status == JOptionPane.YES_OPTION) {
			success = app.getArenaManager().saveArena(app.getArenaManager().isArenaProtected());
			if (success) {
			    app.getGameManager().exitGame();
			    app.getGUIManager().showGUI();
			}
		    } else if (status == JOptionPane.NO_OPTION) {
			app.getGameManager().exitGame();
			app.getGUIManager().showGUI();
		    } else {
			// Don't stop controls from working
			final GameManager gm = GameManager.this;
			gm.moving = false;
			gm.laserActive = false;
		    }
		} else {
		    app.getGameManager().exitGame();
		    app.getGUIManager().showGUI();
		}
	    } catch (final Exception ex) {
		LaserTank.logError(ex);
	    }
	}

	@Override
	public void windowDeactivated(final WindowEvent we) {
	    // Do nothing
	}

	@Override
	public void windowDeiconified(final WindowEvent we) {
	    // Do nothing
	}

	@Override
	public void windowIconified(final WindowEvent we) {
	    // Do nothing
	}

	@Override
	public void windowOpened(final WindowEvent we) {
	    // Do nothing
	}
    }

    private class FocusHandler implements WindowFocusListener {
	public FocusHandler() {
	    // Do nothing
	}

	@Override
	public void windowGainedFocus(final WindowEvent e) {
	    LaserTank.getApplication().getMenuManager().updateMenuItemState();
	}

	@Override
	public void windowLostFocus(final WindowEvent e) {
	    // Do nothing
	}
    }

    private static final int OTHER_AMMO_MODE_MISSILES = 0;
    private static final int OTHER_AMMO_MODE_STUNNERS = 1;
    private static final int OTHER_AMMO_MODE_BLUE_LASERS = 2;
    private static final int OTHER_AMMO_MODE_DISRUPTORS = 3;
    private static final int OTHER_TOOL_MODE_BOOSTS = 0;
    private static final int OTHER_TOOL_MODE_MAGNETS = 1;
    private static final int OTHER_RANGE_MODE_BOMBS = 0;
    private static final int OTHER_RANGE_MODE_HEAT_BOMBS = 1;
    private static final int OTHER_RANGE_MODE_ICE_BOMBS = 2;
    static final int CHEAT_SWIMMING = 0;
    static final int CHEAT_GHOSTLY = 1;
    static final int CHEAT_INVINCIBLE = 2;
    private static final int CHEAT_MISSILES = 3;
    private static final int CHEAT_STUNNERS = 4;
    private static final int CHEAT_BOOSTS = 5;
    private static final int CHEAT_MAGNETS = 6;
    private static final int CHEAT_BLUE_LASERS = 7;
    private static final int CHEAT_DISRUPTORS = 8;
    private static final int CHEAT_BOMBS = 9;
    private static final int CHEAT_HEAT_BOMBS = 10;
    private static final int CHEAT_ICE_BOMBS = 11;
    private static String[] OTHER_AMMO_CHOICES = new String[] { StringLoader.loadGame(GameString.MISSILES),
	    StringLoader.loadGame(GameString.STUNNERS), StringLoader.loadGame(GameString.BLUE_LASERS),
	    StringLoader.loadGame(GameString.DISRUPTORS) };
    private static String[] OTHER_TOOL_CHOICES = new String[] { StringLoader.loadGame(GameString.BOOSTS),
	    StringLoader.loadGame(GameString.MAGNETS) };
    private static String[] OTHER_RANGE_CHOICES = new String[] { StringLoader.loadGame(GameString.BOMBS),
	    StringLoader.loadGame(GameString.HEAT_BOMBS), StringLoader.loadGame(GameString.ICE_BOMBS) };

    public static boolean canObjectMove(final int locX, final int locY, final int dirX, final int dirY) {
	return MLOTask.checkSolid(locX + dirX, locY + dirY);
    }

    private static int[] getEnabledDifficulties() {
	final ArrayList<Integer> temp = new ArrayList<>();
	if (PreferencesManager.isKidsDifficultyEnabled()) {
	    temp.add(Integer.valueOf(DifficultyConstants.DIFFICULTY_KIDS - 1));
	}
	if (PreferencesManager.isEasyDifficultyEnabled()) {
	    temp.add(Integer.valueOf(DifficultyConstants.DIFFICULTY_EASY - 1));
	}
	if (PreferencesManager.isMediumDifficultyEnabled()) {
	    temp.add(Integer.valueOf(DifficultyConstants.DIFFICULTY_MEDIUM - 1));
	}
	if (PreferencesManager.isHardDifficultyEnabled()) {
	    temp.add(Integer.valueOf(DifficultyConstants.DIFFICULTY_HARD - 1));
	}
	if (PreferencesManager.isDeadlyDifficultyEnabled()) {
	    temp.add(Integer.valueOf(DifficultyConstants.DIFFICULTY_DEADLY - 1));
	}
	final Integer[] temp2 = temp.toArray(new Integer[temp.size()]);
	final int[] retVal = new int[temp2.length];
	for (int x = 0; x < temp2.length; x++) {
	    retVal[x] = temp2[x].intValue();
	}
	return retVal;
    }

    private static void updateRedo(final boolean las, final boolean mis, final boolean stu, final boolean boo,
	    final boolean mag, final boolean blu, final boolean dis, final boolean bom, final boolean hbm,
	    final boolean ibm) {
	final Application app = LaserTank.getApplication();
	final AbstractArena a = app.getArenaManager().getArena();
	a.updateRedoHistory(new HistoryStatus(las, mis, stu, boo, mag, blu, dis, bom, hbm, ibm));
	app.getMenuManager().updateMenuItemState();
    }

    static void updateUndo(final boolean las, final boolean mis, final boolean stu, final boolean boo,
	    final boolean mag, final boolean blu, final boolean dis, final boolean bom, final boolean hbm,
	    final boolean ibm) {
	final Application app = LaserTank.getApplication();
	final AbstractArena a = app.getArenaManager().getArena();
	a.updateUndoHistory(new HistoryStatus(las, mis, stu, boo, mag, blu, dis, bom, hbm, ibm));
	app.getMenuManager().updateMenuItemState();
    }

    // Fields
    private Container borderPane, scorePane, infoPane, outerOutputPane;
    private GameDraw outputPane;
    AbstractCharacter tank;
    private boolean savedGameFlag;
    private int activeLaserType;
    final PlayerLocationManager plMgr;
    private final CheatManager cMgr;
    private final ScoreTracker st;
    private JLabel scoreMoves;
    private JLabel scoreShots;
    private JLabel scoreOthers;
    private JLabel otherAmmoLeft;
    private JLabel otherToolsLeft;
    private JLabel otherRangesLeft;
    private JLabel levelInfo;
    private boolean delayedDecayActive;
    private AbstractArenaObject delayedDecayObject;
    boolean laserActive;
    boolean moving;
    private boolean remoteDecay;
    private AnimationTask animator;
    private GameReplayEngine gre;
    private boolean recording;
    private boolean replaying;
    private boolean newGameResult;
    private MLOTask mlot;
    private JDialog difficultyFrame;
    private JList<String> difficultyList;
    private boolean lpbLoaded;
    private final boolean[] cheatStatus;
    private boolean autoMove;
    private boolean dead;
    int otherAmmoMode;
    int otherToolMode;
    int otherRangeMode;
    private final EventHandler handler = new EventHandler();
    private final FocusHandler fHandler = new FocusHandler();

    // Constructors
    public GameManager() {
	this.plMgr = new PlayerLocationManager();
	this.cMgr = new CheatManager();
	this.st = new ScoreTracker();
	this.setUpGUI();
	this.savedGameFlag = false;
	this.delayedDecayActive = false;
	this.delayedDecayObject = null;
	this.laserActive = false;
	this.activeLaserType = LaserTypeConstants.LASER_TYPE_GREEN;
	this.remoteDecay = false;
	this.moving = false;
	this.gre = new GameReplayEngine();
	this.recording = false;
	this.replaying = false;
	this.newGameResult = false;
	this.lpbLoaded = false;
	this.cheatStatus = new boolean[this.cMgr.getCheatCount()];
	this.autoMove = false;
	this.dead = false;
	this.otherAmmoMode = GameManager.OTHER_AMMO_MODE_MISSILES;
	this.otherToolMode = GameManager.OTHER_TOOL_MODE_BOOSTS;
	this.otherRangeMode = GameManager.OTHER_RANGE_MODE_BOMBS;
    }

    public void abortAndWaitForMLOLoop() {
	if (this.mlot != null && this.mlot.isAlive()) {
	    this.mlot.abortLoop();
	    boolean waiting = true;
	    while (waiting) {
		try {
		    this.mlot.join();
		    waiting = false;
		} catch (final InterruptedException ie) {
		    // Ignore
		}
	    }
	}
	this.moveLoopDone();
	this.laserDone();
    }

    private void abortMovementLaserObjectLoop() {
	this.mlot.abortLoop();
	this.moveLoopDone();
	this.laserDone();
    }

    // Methods
    public void activeLanguageChanged() {
	this.setUpDifficultyDialog();
	GameManager.OTHER_AMMO_CHOICES = new String[] { StringLoader.loadGame(GameString.MISSILES),
		StringLoader.loadGame(GameString.STUNNERS), StringLoader.loadGame(GameString.BLUE_LASERS),
		StringLoader.loadGame(GameString.DISRUPTORS) };
	GameManager.OTHER_TOOL_CHOICES = new String[] { StringLoader.loadGame(GameString.BOOSTS),
		StringLoader.loadGame(GameString.MAGNETS) };
	GameManager.OTHER_RANGE_CHOICES = new String[] { StringLoader.loadGame(GameString.BOMBS),
		StringLoader.loadGame(GameString.HEAT_BOMBS), StringLoader.loadGame(GameString.ICE_BOMBS) };
    }

    void cancelButtonClicked() {
	this.difficultyFrame.setVisible(false);
	this.newGameResult = false;
    }

    public void changeOtherAmmoMode() {
	final String choice = CommonDialogs.showInputDialog(StringLoader.loadGame(GameString.WHICH_AMMO),
		StringLoader.loadGame(GameString.CHANGE_AMMO), GameManager.OTHER_AMMO_CHOICES,
		GameManager.OTHER_AMMO_CHOICES[this.otherAmmoMode]);
	if (choice != null) {
	    for (int z = 0; z < GameManager.OTHER_AMMO_CHOICES.length; z++) {
		if (choice.equals(GameManager.OTHER_AMMO_CHOICES[z])) {
		    this.otherAmmoMode = z;
		    break;
		}
	    }
	    this.updateScoreText();
	    CommonDialogs.showDialog(StringLoader.loadGame(GameString.AMMO_CHANGED)
		    + StringLoader.loadCommon(CommonString.SPACE) + GameManager.OTHER_AMMO_CHOICES[this.otherAmmoMode]
		    + StringLoader.loadCommon(CommonString.NOTL_PERIOD));
	}
    }

    public void changeOtherRangeMode() {
	final String choice = CommonDialogs.showInputDialog(StringLoader.loadGame(GameString.WHICH_RANGE),
		StringLoader.loadGame(GameString.CHANGE_RANGE), GameManager.OTHER_RANGE_CHOICES,
		GameManager.OTHER_RANGE_CHOICES[this.otherRangeMode]);
	if (choice != null) {
	    for (int z = 0; z < GameManager.OTHER_RANGE_CHOICES.length; z++) {
		if (choice.equals(GameManager.OTHER_RANGE_CHOICES[z])) {
		    this.otherRangeMode = z;
		    break;
		}
	    }
	    this.updateScoreText();
	    CommonDialogs.showDialog(StringLoader.loadGame(GameString.RANGE_CHANGED)
		    + StringLoader.loadCommon(CommonString.SPACE) + GameManager.OTHER_RANGE_CHOICES[this.otherRangeMode]
		    + StringLoader.loadCommon(CommonString.NOTL_PERIOD));
	}
    }

    public void changeOtherToolMode() {
	final String choice = CommonDialogs.showInputDialog(StringLoader.loadGame(GameString.WHICH_TOOL),
		StringLoader.loadGame(GameString.CHANGE_TOOL), GameManager.OTHER_TOOL_CHOICES,
		GameManager.OTHER_TOOL_CHOICES[this.otherToolMode]);
	if (choice != null) {
	    for (int z = 0; z < GameManager.OTHER_TOOL_CHOICES.length; z++) {
		if (choice.equals(GameManager.OTHER_TOOL_CHOICES[z])) {
		    this.otherToolMode = z;
		    break;
		}
	    }
	    this.updateScoreText();
	    CommonDialogs.showDialog(StringLoader.loadGame(GameString.TOOL_CHANGED)
		    + StringLoader.loadCommon(CommonString.SPACE) + GameManager.OTHER_TOOL_CHOICES[this.otherToolMode]
		    + StringLoader.loadCommon(CommonString.NOTL_PERIOD));
	}
    }

    void clearDead() {
	this.dead = false;
    }

    public void clearReplay() {
	this.gre = new GameReplayEngine();
	this.lpbLoaded = true;
    }

    public void decay() {
	if (this.tank != null) {
	    this.tank.setSavedObject(new Empty());
	}
    }

    void doDelayedDecay() {
	this.tank.setSavedObject(this.delayedDecayObject);
	this.delayedDecayActive = false;
    }

    void doRemoteDelayedDecay(final AbstractMovableObject o) {
	o.setSavedObject(this.delayedDecayObject);
	this.remoteDecay = false;
	this.delayedDecayActive = false;
    }

    public void enterCheatCode() {
	final String rawCheat = this.cMgr.enterCheat();
	if (rawCheat != null) {
	    if (rawCheat.contains(StringLoader.loadGame(GameString.ENABLE_CHEAT))) {
		// Enable cheat
		final String cheat = rawCheat.substring(7);
		for (int x = 0; x < this.cMgr.getCheatCount(); x++) {
		    if (this.cMgr.queryCheatCache(cheat) == x) {
			this.cheatStatus[x] = true;
			break;
		    }
		}
	    } else {
		// Disable cheat
		final String cheat = rawCheat.substring(8);
		for (int x = 0; x < this.cMgr.getCheatCount(); x++) {
		    if (this.cMgr.queryCheatCache(cheat) == x) {
			this.cheatStatus[x] = false;
			break;
		    }
		}
	    }
	}
    }

    public void exitGame() {
	// Halt the animator
	if (this.animator != null) {
	    this.animator.stopAnimator();
	    this.animator = null;
	}
	// Halt the movement/laser processor
	if (this.mlot != null) {
	    this.abortMovementLaserObjectLoop();
	}
	this.mlot = null;
	final Application app = LaserTank.getApplication();
	final AbstractArena m = app.getArenaManager().getArena();
	// Restore the arena
	m.restore();
	final boolean playerExists = m.doesPlayerExist(this.plMgr.getActivePlayerNumber());
	if (playerExists) {
	    try {
		this.resetPlayerLocation();
	    } catch (final InvalidArenaException iae) {
		// Ignore
	    }
	} else {
	    app.getArenaManager().setLoaded(false);
	}
	// Reset saved game flag
	this.savedGameFlag = false;
	app.getArenaManager().setDirty(false);
	// Exit game
	app.getGUIManager().showGUI();
    }

    public boolean fireLaser(final int ox, final int oy, final AbstractArenaObject shooter) {
	if (this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_MISSILES
		&& this.activeLaserType == LaserTypeConstants.LASER_TYPE_MISSILE && TankInventory.getMissilesLeft() == 0
		&& !this.getCheatStatus(GameManager.CHEAT_MISSILES)) {
	    CommonDialogs.showDialog(StringLoader.loadGame(GameString.OUT_OF_MISSILES));
	} else if (this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_STUNNERS
		&& this.activeLaserType == LaserTypeConstants.LASER_TYPE_STUNNER && TankInventory.getStunnersLeft() == 0
		&& !this.getCheatStatus(GameManager.CHEAT_STUNNERS)) {
	    CommonDialogs.showDialog(StringLoader.loadGame(GameString.OUT_OF_STUNNERS));
	} else if (this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_BLUE_LASERS
		&& this.activeLaserType == LaserTypeConstants.LASER_TYPE_BLUE && TankInventory.getBlueLasersLeft() == 0
		&& !this.getCheatStatus(GameManager.CHEAT_BLUE_LASERS)) {
	    CommonDialogs.showDialog(StringLoader.loadGame(GameString.OUT_OF_BLUE_LASERS));
	} else if (this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_DISRUPTORS
		&& this.activeLaserType == LaserTypeConstants.LASER_TYPE_DISRUPTOR
		&& TankInventory.getDisruptorsLeft() == 0 && !this.getCheatStatus(GameManager.CHEAT_DISRUPTORS)) {
	    CommonDialogs.showDialog(StringLoader.loadGame(GameString.OUT_OF_DISRUPTORS));
	} else {
	    final AbstractArena a = LaserTank.getApplication().getArenaManager().getArena();
	    if (!a.isMoveShootAllowed() && !this.laserActive || a.isMoveShootAllowed()) {
		this.laserActive = true;
		final int[] currDirection = DirectionResolver.unresolveRelative(shooter.getDirection());
		final int x = currDirection[0];
		final int y = currDirection[1];
		if (this.mlot == null) {
		    this.mlot = new MLOTask();
		} else {
		    if (!this.mlot.isAlive()) {
			this.mlot = new MLOTask();
		    }
		}
		this.mlot.activateLasers(x, y, ox, oy, this.activeLaserType, shooter);
		if (!this.mlot.isAlive()) {
		    this.mlot.start();
		}
		if (this.replaying) {
		    // Wait
		    while (this.laserActive) {
			try {
			    Thread.sleep(100);
			} catch (final InterruptedException ie) {
			    // Ignore
			}
		    }
		}
		return true;
	    }
	}
	return false;
    }

    void fireRange() {
	// Boom!
	SoundManager.playSound(SoundConstants.SOUND_BOOM);
	this.updateScore(0, 0, 1);
	if (this.otherRangeMode == GameManager.OTHER_RANGE_MODE_BOMBS) {
	    GameManager.updateUndo(false, false, false, false, false, false, false, true, false, false);
	} else if (this.otherRangeMode == GameManager.OTHER_RANGE_MODE_HEAT_BOMBS) {
	    GameManager.updateUndo(false, false, false, false, false, false, false, false, true, false);
	} else if (this.otherRangeMode == GameManager.OTHER_RANGE_MODE_ICE_BOMBS) {
	    GameManager.updateUndo(false, false, false, false, false, false, false, false, false, true);
	}
	final AbstractArena a = LaserTank.getApplication().getArenaManager().getArena();
	final int px = this.plMgr.getPlayerLocationX();
	final int py = this.plMgr.getPlayerLocationY();
	final int pz = this.plMgr.getPlayerLocationZ();
	a.circularScanRange(px, py, pz, 1, this.otherRangeMode, AbstractArenaObject
		.getImbuedRangeForce(RangeTypeConstants.getMaterialForRangeType(this.otherRangeMode)));
	LaserTank.getApplication().getArenaManager().getArena().tickTimers(pz, ActionConstants.ACTION_NON_MOVE);
	this.updateScoreText();
    }

    public void gameOver() {
	// Check cheats
	if (this.getCheatStatus(GameManager.CHEAT_INVINCIBLE)) {
	    return;
	}
	// Check dead
	if (this.dead) {
	    // Already dead
	    throw new AlreadyDeadException();
	}
	// We are dead
	this.dead = true;
	// Stop the movement/laser/object loop
	if (this.mlot != null) {
	    if (this.mlot.isAlive()) {
		this.abortMovementLaserObjectLoop();
	    }
	}
	this.mlot = null;
	SoundManager.playSound(SoundConstants.SOUND_DEAD);
	final int choice = CustomDialogs.showDeadDialog();
	if (choice == JOptionPane.CANCEL_OPTION) {
	    // End
	    this.exitGame();
	} else if (choice == JOptionPane.YES_OPTION) {
	    // Undo
	    this.undoLastMove();
	} else if (choice == JOptionPane.NO_OPTION) {
	    // Restart
	    try {
		this.resetCurrentLevel();
	    } catch (final InvalidArenaException iae) {
		CommonDialogs.showErrorDialog(StringLoader.loadError(ErrorString.TANK_LOCATION),
			GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME));
		this.exitGame();
		return;
	    }
	} else {
	    // Closed Dialog
	    this.exitGame();
	}
    }

    boolean getCheatStatus(final int cheatID) {
	return this.cheatStatus[cheatID];
    }

    public PlayerLocationManager getPlayerManager() {
	return this.plMgr;
    }

    public AbstractCharacter getTank() {
	return this.tank;
    }

    public int[] getTankLocation() {
	return new int[] { this.plMgr.getPlayerLocationX(), this.plMgr.getPlayerLocationY(),
		this.plMgr.getPlayerLocationZ() };
    }

    public void haltMovingObjects() {
	if (this.mlot != null && this.mlot.isAlive()) {
	    this.mlot.haltMovingObjects();
	}
    }

    boolean isAutoMoveScheduled() {
	return this.autoMove;
    }

    boolean isDelayedDecayActive() {
	return this.delayedDecayActive;
    }

    boolean isRemoteDecayActive() {
	return this.remoteDecay;
    }

    boolean isReplaying() {
	return this.replaying;
    }

    void laserDone() {
	final Application app = LaserTank.getApplication();
	this.laserActive = false;
	app.getMenuManager().updateMenuItemState();
    }

    public void loadGameHookG1(final XMLFileReader arenaFile) throws IOException {
	final Application app = LaserTank.getApplication();
	app.getArenaManager().setScoresFileName(arenaFile.readString());
	this.st.setMoves(arenaFile.readLong());
	this.st.setShots(arenaFile.readLong());
	this.st.setOthers(arenaFile.readLong());
    }

    public void loadGameHookG2(final XMLFileReader arenaFile) throws IOException {
	final Application app = LaserTank.getApplication();
	app.getArenaManager().setScoresFileName(arenaFile.readString());
	this.st.setMoves(arenaFile.readLong());
	this.st.setShots(arenaFile.readLong());
	this.st.setOthers(arenaFile.readLong());
	TankInventory.setRedKeysLeft(arenaFile.readInt());
	TankInventory.setGreenKeysLeft(arenaFile.readInt());
	TankInventory.setBlueKeysLeft(arenaFile.readInt());
    }

    public void loadGameHookG3(final XMLFileReader arenaFile) throws IOException {
	final Application app = LaserTank.getApplication();
	app.getArenaManager().setScoresFileName(arenaFile.readString());
	this.st.setMoves(arenaFile.readLong());
	this.st.setShots(arenaFile.readLong());
	this.st.setOthers(arenaFile.readLong());
	TankInventory.readInventory(arenaFile);
    }

    public void loadGameHookG4(final XMLFileReader arenaFile) throws IOException {
	final Application app = LaserTank.getApplication();
	app.getArenaManager().setScoresFileName(arenaFile.readString());
	this.st.setMoves(arenaFile.readLong());
	this.st.setShots(arenaFile.readLong());
	this.st.setOthers(arenaFile.readLong());
	TankInventory.readInventory(arenaFile);
    }

    public void loadGameHookG5(final XMLFileReader arenaFile) throws IOException {
	final Application app = LaserTank.getApplication();
	app.getArenaManager().setScoresFileName(arenaFile.readString());
	this.st.setMoves(arenaFile.readLong());
	this.st.setShots(arenaFile.readLong());
	this.st.setOthers(arenaFile.readLong());
	TankInventory.readInventory(arenaFile);
    }

    public void loadGameHookG6(final XMLFileReader arenaFile) throws IOException {
	final Application app = LaserTank.getApplication();
	app.getArenaManager().setScoresFileName(arenaFile.readString());
	this.st.setMoves(arenaFile.readLong());
	this.st.setShots(arenaFile.readLong());
	this.st.setOthers(arenaFile.readLong());
	TankInventory.readInventory(arenaFile);
    }

    public void loadLevel() {
	final Application app = LaserTank.getApplication();
	final AbstractArena m = app.getArenaManager().getArena();
	final String[] choices = app.getLevelInfoList();
	final String res = CommonDialogs.showInputDialog(StringLoader.loadGame(GameString.LOAD_LEVEL_PROMPT),
		StringLoader.loadGame(GameString.LOAD_LEVEL), choices, choices[m.getActiveLevelNumber()]);
	int number = -1;
	for (number = 0; number < m.getLevels(); number++) {
	    if (choices[number].equals(res)) {
		break;
	    }
	}
	if (m.doesLevelExist(number)) {
	    this.suspendAnimator();
	    m.restore();
	    m.switchLevel(number);
	    app.getArenaManager().getArena().setDirtyFlags(this.plMgr.getPlayerLocationZ());
	    m.resetHistoryEngine();
	    this.gre = new GameReplayEngine();
	    app.getMenuManager().updateMenuItemState();
	    this.processLevelExists();
	}
    }

    public void loadReplay(final boolean laser, final int x, final int y) {
	this.gre.updateRedoHistory(laser, x, y);
    }

    void markTankAsDirty() {
	LaserTank.getApplication().getArenaManager().getArena().markAsDirty(this.plMgr.getPlayerLocationX(),
		this.plMgr.getPlayerLocationY(), this.plMgr.getPlayerLocationZ());
    }

    public void morph(final AbstractArenaObject morphInto, final int x, final int y, final int z, final int w) {
	final Application app = LaserTank.getApplication();
	final AbstractArena m = app.getArenaManager().getArena();
	try {
	    m.setCell(morphInto, x, y, z, w);
	    app.getArenaManager().setDirty(true);
	} catch (final ArrayIndexOutOfBoundsException ae) {
	    // Do nothing
	} catch (final NullPointerException np) {
	    // Do nothing
	}
    }

    void moveLoopDone() {
	final Application app = LaserTank.getApplication();
	this.moving = false;
	app.getMenuManager().updateMenuItemState();
    }

    public boolean newGame() {
	LaserTank.getApplication().getObjects().enableAllObjects();
	this.difficultyList.clearSelection();
	final int[] retVal = GameManager.getEnabledDifficulties();
	this.difficultyList.setSelectedIndices(retVal);
	this.difficultyFrame.setVisible(true);
	return this.newGameResult;
    }

    void okButtonClicked() {
	this.difficultyFrame.setVisible(false);
	if (this.difficultyList.isSelectedIndex(DifficultyConstants.DIFFICULTY_KIDS - 1)) {
	    PreferencesManager.setKidsDifficultyEnabled(true);
	} else {
	    PreferencesManager.setKidsDifficultyEnabled(false);
	}
	if (this.difficultyList.isSelectedIndex(DifficultyConstants.DIFFICULTY_EASY - 1)) {
	    PreferencesManager.setEasyDifficultyEnabled(true);
	} else {
	    PreferencesManager.setEasyDifficultyEnabled(false);
	}
	if (this.difficultyList.isSelectedIndex(DifficultyConstants.DIFFICULTY_MEDIUM - 1)) {
	    PreferencesManager.setMediumDifficultyEnabled(true);
	} else {
	    PreferencesManager.setMediumDifficultyEnabled(false);
	}
	if (this.difficultyList.isSelectedIndex(DifficultyConstants.DIFFICULTY_HARD - 1)) {
	    PreferencesManager.setHardDifficultyEnabled(true);
	} else {
	    PreferencesManager.setHardDifficultyEnabled(false);
	}
	if (this.difficultyList.isSelectedIndex(DifficultyConstants.DIFFICULTY_DEADLY - 1)) {
	    PreferencesManager.setDeadlyDifficultyEnabled(true);
	} else {
	    PreferencesManager.setDeadlyDifficultyEnabled(false);
	}
	this.newGameResult = true;
    }

    public void playArena() {
	final Application app = LaserTank.getApplication();
	if (app.getArenaManager().getLoaded()) {
	    app.setInGame(this.borderPane);
	    app.getArenaManager().getArena().switchLevel(0);
	    final boolean res = app.getArenaManager().getArena()
		    .switchToNextLevelWithDifficulty(GameManager.getEnabledDifficulties());
	    if (res) {
		try {
		    this.resetPlayerLocation();
		} catch (final InvalidArenaException iae) {
		    CommonDialogs.showErrorDialog(StringLoader.loadError(ErrorString.TANK_LOCATION),
			    GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME));
		    this.exitGame();
		    return;
		}
		this.updateTank();
		this.tank.setSavedObject(new Empty());
		this.st.setScoreFile(app.getArenaManager().getScoresFileName());
		if (!this.savedGameFlag) {
		    this.st.resetScore(app.getArenaManager().getScoresFileName());
		}
		this.updateInfo();
		this.borderPane.removeAll();
		this.borderPane.add(this.outerOutputPane, BorderLayout.CENTER);
		this.borderPane.add(this.scorePane, BorderLayout.NORTH);
		this.borderPane.add(this.infoPane, BorderLayout.SOUTH);
		app.getMenuManager().updateMenuItemState();
		app.getArenaManager().getArena().setDirtyFlags(this.plMgr.getPlayerLocationZ());
		this.redrawArena();
		this.updateScoreText();
		app.pack();
		this.replaying = false;
		// Start animator, if enabled
		if (PreferencesManager.enableAnimation()) {
		    this.animator = new AnimationTask();
		    this.animator.start();
		}
	    } else {
		CommonDialogs.showDialog(StringLoader.loadGame(GameString.NO_LEVEL_WITH_DIFFICULTY));
		LaserTank.getApplication().getGUIManager().showGUI();
	    }
	} else {
	    CommonDialogs.showDialog(StringLoader.loadMenu(MenuString.ERROR_NO_ARENA_OPENED));
	}
    }

    public void previousLevel() {
	final Application app = LaserTank.getApplication();
	final AbstractArena m = app.getArenaManager().getArena();
	m.resetHistoryEngine();
	this.gre = new GameReplayEngine();
	app.getMenuManager().updateMenuItemState();
	this.suspendAnimator();
	m.restore();
	if (m.doesLevelExistOffset(-1)) {
	    m.switchLevelOffset(-1);
	    final boolean levelExists = m.switchToPreviousLevelWithDifficulty(GameManager.getEnabledDifficulties());
	    if (levelExists) {
		m.setDirtyFlags(this.plMgr.getPlayerLocationZ());
		this.processLevelExists();
	    } else {
		CommonDialogs.showErrorDialog(StringLoader.loadError(ErrorString.NO_PREVIOUS_LEVEL),
			GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME));
	    }
	} else {
	    CommonDialogs.showErrorDialog(StringLoader.loadError(ErrorString.NO_PREVIOUS_LEVEL),
		    GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME));
	}
    }

    private void processLevelExists() {
	final Application app = LaserTank.getApplication();
	try {
	    this.resetPlayerLocation();
	} catch (final InvalidArenaException iae) {
	    CommonDialogs.showErrorDialog(StringLoader.loadError(ErrorString.TANK_LOCATION),
		    GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME));
	    this.exitGame();
	    return;
	}
	this.updateTank();
	this.st.resetScore(app.getArenaManager().getScoresFileName());
	TankInventory.resetInventory();
	this.scoreMoves.setText(StringLoader.loadGame(GameString.MOVES) + StringLoader.loadCommon(CommonString.COLON)
		+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadCommon(CommonString.ZERO));
	this.scoreShots.setText(StringLoader.loadGame(GameString.SHOTS) + StringLoader.loadCommon(CommonString.COLON)
		+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadCommon(CommonString.ZERO));
	this.scoreOthers.setText(StringLoader.loadGame(GameString.OTHERS) + StringLoader.loadCommon(CommonString.COLON)
		+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadCommon(CommonString.ZERO));
	if (this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_MISSILES) {
	    if (this.getCheatStatus(GameManager.CHEAT_MISSILES)) {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.MISSILES) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadGame(GameString.INFINITE)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    } else {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.MISSILES) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadCommon(CommonString.ZERO)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    }
	} else if (this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_STUNNERS) {
	    if (this.getCheatStatus(GameManager.CHEAT_STUNNERS)) {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.STUNNERS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadGame(GameString.INFINITE)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    } else {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.STUNNERS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadCommon(CommonString.ZERO)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    }
	} else if (this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_BLUE_LASERS) {
	    if (this.getCheatStatus(GameManager.CHEAT_BLUE_LASERS)) {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.BLUE_LASERS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadGame(GameString.INFINITE)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    } else {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.BLUE_LASERS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadCommon(CommonString.ZERO)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    }
	} else if (this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_DISRUPTORS) {
	    if (this.getCheatStatus(GameManager.CHEAT_DISRUPTORS)) {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.DISRUPTORS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadGame(GameString.INFINITE)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    } else {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.DISRUPTORS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadCommon(CommonString.ZERO)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    }
	}
	this.updateInfo();
	this.redrawArena();
	this.resumeAnimator();
    }

    private boolean readSolution() {
	try {
	    final int activeLevel = LaserTank.getApplication().getArenaManager().getArena().getActiveLevelNumber();
	    final String levelFile = LaserTank.getApplication().getArenaManager().getLastUsedArena();
	    final String filename = levelFile + StringLoader.loadCommon(CommonString.UNDERSCORE) + activeLevel
		    + Extension.getSolutionExtensionWithPeriod();
	    try (XMLFileReader file = new XMLFileReader(filename,
		    GlobalLoader.loadUntranslated(UntranslatedString.SOLUTION))) {
		this.gre = GameReplayEngine.readReplay(file);
	    }
	    return true;
	} catch (final IOException ioe) {
	    throw new InvalidArenaException(ioe);
	}
    }

    public void redoLastMove() {
	final Application app = LaserTank.getApplication();
	final AbstractArena a = app.getArenaManager().getArena();
	if (a.tryRedo()) {
	    this.moving = false;
	    this.laserActive = false;
	    a.redo();
	    final boolean laser = a.getWhatWas().wasSomething(HistoryStatus.WAS_LASER);
	    final boolean missile = a.getWhatWas().wasSomething(HistoryStatus.WAS_MISSILE);
	    final boolean stunner = a.getWhatWas().wasSomething(HistoryStatus.WAS_STUNNER);
	    final boolean boost = a.getWhatWas().wasSomething(HistoryStatus.WAS_BOOST);
	    final boolean magnet = a.getWhatWas().wasSomething(HistoryStatus.WAS_MAGNET);
	    final boolean blue = a.getWhatWas().wasSomething(HistoryStatus.WAS_BLUE_LASER);
	    final boolean disrupt = a.getWhatWas().wasSomething(HistoryStatus.WAS_DISRUPTOR);
	    final boolean bomb = a.getWhatWas().wasSomething(HistoryStatus.WAS_BOMB);
	    final boolean heatBomb = a.getWhatWas().wasSomething(HistoryStatus.WAS_HEAT_BOMB);
	    final boolean iceBomb = a.getWhatWas().wasSomething(HistoryStatus.WAS_ICE_BOMB);
	    final boolean other = missile || stunner || boost || magnet || blue || disrupt || bomb || heatBomb
		    || iceBomb;
	    if (other) {
		this.updateScore(0, 0, -1);
		if (boost) {
		    TankInventory.fireBoost();
		} else if (magnet) {
		    TankInventory.fireMagnet();
		} else if (missile) {
		    TankInventory.fireMissile();
		} else if (stunner) {
		    TankInventory.fireStunner();
		} else if (blue) {
		    TankInventory.fireBlueLaser();
		} else if (disrupt) {
		    TankInventory.fireDisruptor();
		} else if (bomb) {
		    TankInventory.fireBomb();
		} else if (heatBomb) {
		    TankInventory.fireHeatBomb();
		} else if (iceBomb) {
		    TankInventory.fireIceBomb();
		}
	    } else if (laser && !other) {
		this.updateScore(0, 1, 0);
	    } else {
		this.updateScore(1, 0, 0);
	    }
	    try {
		this.resetPlayerLocation();
	    } catch (final InvalidArenaException iae) {
		CommonDialogs.showErrorDialog(StringLoader.loadError(ErrorString.TANK_LOCATION),
			GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME));
		this.exitGame();
		return;
	    }
	    this.updateTank();
	    GameManager.updateUndo(laser, missile, stunner, boost, magnet, blue, disrupt, bomb, heatBomb, iceBomb);
	}
	app.getMenuManager().updateMenuItemState();
	this.updateScoreText();
	a.setDirtyFlags(this.plMgr.getPlayerLocationZ());
	this.redrawArena();
    }

    public synchronized void redrawArena() {
	// Draw the arena
	final Application app = LaserTank.getApplication();
	final AbstractArena a = app.getArenaManager().getArena();
	final DrawGrid drawGrid = this.outputPane.getGrid();
	int x, y;
	final int pz = this.plMgr.getPlayerLocationZ();
	for (x = GameViewingWindowManager.getViewingWindowLocationX(); x <= GameViewingWindowManager
		.getLowerRightViewingWindowLocationX(); x++) {
	    for (y = GameViewingWindowManager.getViewingWindowLocationY(); y <= GameViewingWindowManager
		    .getLowerRightViewingWindowLocationY(); y++) {
		if (a.isCellDirty(y, x, pz)) {
		    final AbstractArenaObject gbobj = a.getCell(y, x, pz, ArenaConstants.LAYER_LOWER_GROUND);
		    final AbstractArenaObject gtobj = a.getCell(y, x, pz, ArenaConstants.LAYER_UPPER_GROUND);
		    AbstractArenaObject obobj = a.getCell(y, x, pz, ArenaConstants.LAYER_LOWER_OBJECTS);
		    AbstractArenaObject otobj = a.getCell(y, x, pz, ArenaConstants.LAYER_UPPER_OBJECTS);
		    final AbstractArenaObject vbobj = a.getVirtualCell(y, x, pz, ArenaConstants.LAYER_VIRTUAL);
		    final AbstractArenaObject otrep = otobj.attributeGameRenderHook();
		    if (otrep != null) {
			if (otobj.isOfType(TypeConstants.TYPE_CLOAK)) {
			    obobj = otrep;
			}
			otobj = otrep;
		    }
		    drawGrid.setImageCell(ImageManager.getVirtualCompositeImage(gbobj, gtobj, obobj, otobj, vbobj), x,
			    y);
		}
	    }
	}
	a.clearDirtyFlags(this.plMgr.getPlayerLocationZ());
	this.outputPane.repaint();
    }

    public void remoteDelayedDecayTo(final AbstractArenaObject obj) {
	this.delayedDecayActive = true;
	this.delayedDecayObject = obj;
	this.remoteDecay = true;
    }

    void replayDone() {
	this.replaying = false;
    }

    boolean replayLastMove() {
	if (this.gre.tryRedo()) {
	    this.gre.redo();
	    final boolean laser = this.gre.wasLaser();
	    final int x = this.gre.getX();
	    final int y = this.gre.getY();
	    final int px = this.plMgr.getPlayerLocationX();
	    final int py = this.plMgr.getPlayerLocationY();
	    if (laser) {
		this.fireLaser(px, py, this.tank);
	    } else {
		final Direction currDir = this.tank.getDirection();
		final Direction newDir = DirectionResolver.resolveRelative(x, y);
		if (currDir != newDir) {
		    this.tank.setDirection(newDir);
		    SoundManager.playSound(SoundConstants.SOUND_TURN);
		    this.redrawArena();
		} else {
		    this.updatePositionRelative(x, y);
		}
	    }
	    return true;
	}
	return false;
    }

    public void replaySolution() {
	MenuManager menu = LaserTank.getApplication().getMenuManager();
	if (this.lpbLoaded) {
	    this.replaying = true;
	    // Turn recording off
	    this.recording = false;
	    menu.disableRecording();
	    try {
		this.resetCurrentLevel(false);
	    } catch (final InvalidArenaException iae) {
		CommonDialogs.showErrorDialog(StringLoader.loadError(ErrorString.TANK_LOCATION),
			GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME));
		this.exitGame();
		return;
	    }
	    final ReplayTask rt = new ReplayTask();
	    rt.start();
	} else {
	    final boolean success = this.readSolution();
	    if (!success) {
		CommonDialogs.showErrorDialog(StringLoader.loadError(ErrorString.NO_SOLUTION_FILE),
			GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME));
	    } else {
		this.replaying = true;
		// Turn recording off
		this.recording = false;
		menu.disableRecording();
		try {
		    this.resetCurrentLevel(false);
		} catch (final InvalidArenaException iae) {
		    CommonDialogs.showErrorDialog(StringLoader.loadError(ErrorString.TANK_LOCATION),
			    GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME));
		    this.exitGame();
		    return;
		}
		final ReplayTask rt = new ReplayTask();
		rt.start();
	    }
	}
    }

    public void resetCurrentLevel() throws InvalidArenaException {
	this.resetLevel(true);
    }

    private void resetCurrentLevel(final boolean flag) throws InvalidArenaException {
	this.resetLevel(flag);
    }

    public void resetGameState() {
	final Application app = LaserTank.getApplication();
	final AbstractArena m = app.getArenaManager().getArena();
	app.getArenaManager().setDirty(false);
	m.restore();
	this.setSavedGameFlag(false);
	this.st.resetScore();
	final boolean playerExists = m.doesPlayerExist(this.plMgr.getActivePlayerNumber());
	if (playerExists) {
	    this.plMgr.setPlayerLocation(m.getStartColumn(0), m.getStartRow(0), m.getStartFloor(0));
	}
    }

    private void resetLevel(final boolean flag) throws InvalidArenaException {
	final Application app = LaserTank.getApplication();
	final AbstractArena m = app.getArenaManager().getArena();
	if (flag) {
	    m.resetHistoryEngine();
	}
	app.getArenaManager().setDirty(true);
	if (this.mlot != null) {
	    if (this.mlot.isAlive()) {
		this.abortMovementLaserObjectLoop();
	    }
	}
	this.moving = false;
	this.laserActive = false;
	TankInventory.resetInventory();
	m.restore();
	m.setDirtyFlags(this.plMgr.getPlayerLocationZ());
	final boolean playerExists = m.doesPlayerExist(this.plMgr.getActivePlayerNumber());
	if (playerExists) {
	    this.st.resetScore(app.getArenaManager().getScoresFileName());
	    this.resetPlayerLocation();
	    this.updateTank();
	    m.clearVirtualGrid();
	    this.updateScore();
	    this.decay();
	    this.redrawArena();
	}
	app.getMenuManager().updateMenuItemState();
    }

    public void resetPlayerLocation() throws InvalidArenaException {
	final Application app = LaserTank.getApplication();
	final AbstractArena m = app.getArenaManager().getArena();
	final int[] found = m.findPlayer(1);
	if (found == null) {
	    throw new InvalidArenaException(StringLoader.loadError(ErrorString.TANK_LOCATION));
	}
	this.plMgr.setPlayerLocation(found[0], found[1], found[2]);
    }

    private void resetTank() {
	LaserTank.getApplication().getArenaManager().getArena().setCell(this.tank, this.plMgr.getPlayerLocationX(),
		this.plMgr.getPlayerLocationY(), this.plMgr.getPlayerLocationZ(), this.tank.getLayer());
	this.markTankAsDirty();
    }

    private void resumeAnimator() {
	if (this.animator == null) {
	    this.animator = new AnimationTask();
	    this.animator.start();
	}
    }

    public void saveGameHook(final XMLFileWriter arenaFile) throws IOException {
	final Application app = LaserTank.getApplication();
	arenaFile.writeString(app.getArenaManager().getScoresFileName());
	arenaFile.writeLong(this.st.getMoves());
	arenaFile.writeLong(this.st.getShots());
	arenaFile.writeLong(this.st.getOthers());
	TankInventory.writeInventory(arenaFile);
    }

    void scheduleAutoMove() {
	this.autoMove = true;
    }

    public void setDisguisedTank() {
	final AbstractCharacter saveTank = this.tank;
	this.tank = new AntiTankDisguise(saveTank.getDirection(), saveTank.getNumber());
	this.resetTank();
    }

    public void setLaserType(final int type) {
	this.activeLaserType = type;
    }

    public void setNormalTank() {
	final AbstractCharacter saveTank = this.tank;
	this.tank = new Tank(saveTank.getDirection(), saveTank.getNumber());
	this.resetTank();
    }

    public void setPowerfulTank() {
	final AbstractCharacter saveTank = this.tank;
	this.tank = new PowerfulTank(saveTank.getDirection(), saveTank.getNumber());
	this.resetTank();
    }

    public void setSavedGameFlag(final boolean value) {
	this.savedGameFlag = value;
    }

    private void setUpDifficultyDialog() {
	// Set up Difficulty Dialog
	final DifficultyEventHandler dhandler = new DifficultyEventHandler();
	this.difficultyFrame = new JDialog((JFrame) null, StringLoader.loadGame(GameString.SELECT_DIFFICULTY));
	final Container difficultyPane = new Container();
	final Container listPane = new Container();
	final Container buttonPane = new Container();
	this.difficultyList = new JList<>(DifficultyConstants.getDifficultyNames());
	final JButton okButton = new JButton(StringLoader.loadDialog(DialogString.OK_BUTTON));
	final JButton cancelButton = new JButton(StringLoader.loadDialog(DialogString.CANCEL_BUTTON));
	buttonPane.setLayout(new FlowLayout());
	buttonPane.add(okButton);
	buttonPane.add(cancelButton);
	listPane.setLayout(new FlowLayout());
	listPane.add(this.difficultyList);
	difficultyPane.setLayout(new BorderLayout());
	difficultyPane.add(listPane, BorderLayout.CENTER);
	difficultyPane.add(buttonPane, BorderLayout.SOUTH);
	this.difficultyFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	this.difficultyFrame.setModal(true);
	this.difficultyFrame.setResizable(false);
	okButton.setDefaultCapable(true);
	cancelButton.setDefaultCapable(false);
	this.difficultyFrame.getRootPane().setDefaultButton(okButton);
	this.difficultyFrame.addWindowListener(dhandler);
	okButton.addActionListener(dhandler);
	cancelButton.addActionListener(dhandler);
	this.difficultyFrame.setContentPane(difficultyPane);
	this.difficultyFrame.pack();
    }

    private void setUpGUI() {
	this.borderPane = new Container();
	this.borderPane.setLayout(new BorderLayout());
	this.outerOutputPane = RCLGenerator.generateRowColumnLabels();
	this.outputPane = new GameDraw();
	this.outputPane.setLayout(new GridLayout(GameViewingWindowManager.getViewingWindowSizeX(),
		GameViewingWindowManager.getViewingWindowSizeY()));
	this.outputPane.addMouseListener(this.handler);
	this.scoreMoves = new JLabel(
		StringLoader.loadGame(GameString.MOVES) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadCommon(CommonString.ZERO));
	this.scoreShots = new JLabel(
		StringLoader.loadGame(GameString.SHOTS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadCommon(CommonString.ZERO));
	this.scoreOthers = new JLabel(
		StringLoader.loadGame(GameString.OTHERS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadCommon(CommonString.ZERO));
	this.otherAmmoLeft = new JLabel(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
		+ StringLoader.loadGame(GameString.MISSILES) + StringLoader.loadCommon(CommonString.COLON)
		+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadCommon(CommonString.ZERO)
		+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	this.otherToolsLeft = new JLabel(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
		+ StringLoader.loadGame(GameString.BOOSTS) + StringLoader.loadCommon(CommonString.COLON)
		+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadCommon(CommonString.ZERO)
		+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	this.otherRangesLeft = new JLabel(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
		+ StringLoader.loadGame(GameString.BOMBS) + StringLoader.loadCommon(CommonString.COLON)
		+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadCommon(CommonString.ZERO)
		+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	this.scorePane = new Container();
	this.scorePane.setLayout(new FlowLayout());
	this.scorePane.add(this.scoreMoves);
	this.scorePane.add(this.scoreShots);
	this.scorePane.add(this.scoreOthers);
	this.scorePane.add(this.otherAmmoLeft);
	this.scorePane.add(this.otherToolsLeft);
	this.scorePane.add(this.otherRangesLeft);
	this.levelInfo = new JLabel(StringLoader.loadCommon(CommonString.SPACE));
	this.infoPane = new Container();
	this.infoPane.setLayout(new FlowLayout());
	this.infoPane.add(this.levelInfo);
	this.scoreMoves.setLabelFor(this.outputPane);
	this.scoreShots.setLabelFor(this.outputPane);
	this.scoreOthers.setLabelFor(this.outputPane);
	this.otherAmmoLeft.setLabelFor(this.outputPane);
	this.otherToolsLeft.setLabelFor(this.outputPane);
	this.otherRangesLeft.setLabelFor(this.outputPane);
	this.levelInfo.setLabelFor(this.outputPane);
	this.outerOutputPane.add(this.outputPane, BorderLayout.CENTER);
	this.borderPane.add(this.outerOutputPane, BorderLayout.CENTER);
	this.borderPane.add(this.scorePane, BorderLayout.NORTH);
	this.borderPane.add(this.infoPane, BorderLayout.SOUTH);
	this.setUpDifficultyDialog();
    }

    public void showScoreTable() {
	this.st.showScoreTable();
    }

    private void solvedArena() {
	TankInventory.resetInventory();
	this.exitGame();
    }

    public void solvedLevel(final boolean playSound) {
	if (playSound) {
	    SoundManager.playSound(SoundConstants.SOUND_END_LEVEL);
	}
	final Application app = LaserTank.getApplication();
	final AbstractArena m = app.getArenaManager().getArena();
	if (playSound) {
	    if (this.recording) {
		this.writeSolution();
	    }
	    if (this.st.checkScore()) {
		this.st.commitScore();
	    }
	}
	m.resetHistoryEngine();
	this.gre = new GameReplayEngine();
	app.getMenuManager().updateMenuItemState();
	this.suspendAnimator();
	m.restore();
	if (m.doesLevelExistOffset(1)) {
	    m.switchLevelOffset(1);
	    final boolean levelExists = m.switchToNextLevelWithDifficulty(GameManager.getEnabledDifficulties());
	    if (levelExists) {
		m.setDirtyFlags(this.plMgr.getPlayerLocationZ());
		this.processLevelExists();
	    } else {
		this.solvedArena();
	    }
	} else {
	    this.solvedArena();
	}
    }

    private void suspendAnimator() {
	if (this.animator != null) {
	    this.animator.stopAnimator();
	    try {
		this.animator.join();
	    } catch (final InterruptedException ie) {
		// Ignore
	    }
	    this.animator = null;
	}
    }

    public void toggleRecording() {
	this.recording = !this.recording;
    }

    public void undoLastMove() {
	final Application app = LaserTank.getApplication();
	final AbstractArena a = app.getArenaManager().getArena();
	if (a.tryUndo()) {
	    this.moving = false;
	    this.laserActive = false;
	    a.undo();
	    final boolean laser = a.getWhatWas().wasSomething(HistoryStatus.WAS_LASER);
	    final boolean missile = a.getWhatWas().wasSomething(HistoryStatus.WAS_MISSILE);
	    final boolean stunner = a.getWhatWas().wasSomething(HistoryStatus.WAS_STUNNER);
	    final boolean boost = a.getWhatWas().wasSomething(HistoryStatus.WAS_BOOST);
	    final boolean magnet = a.getWhatWas().wasSomething(HistoryStatus.WAS_MAGNET);
	    final boolean blue = a.getWhatWas().wasSomething(HistoryStatus.WAS_BLUE_LASER);
	    final boolean disrupt = a.getWhatWas().wasSomething(HistoryStatus.WAS_DISRUPTOR);
	    final boolean bomb = a.getWhatWas().wasSomething(HistoryStatus.WAS_BOMB);
	    final boolean heatBomb = a.getWhatWas().wasSomething(HistoryStatus.WAS_HEAT_BOMB);
	    final boolean iceBomb = a.getWhatWas().wasSomething(HistoryStatus.WAS_ICE_BOMB);
	    final boolean other = missile || stunner || boost || magnet || blue || disrupt || bomb || heatBomb
		    || iceBomb;
	    if (other) {
		this.updateScore(0, 0, -1);
		if (boost) {
		    TankInventory.addOneBoost();
		} else if (magnet) {
		    TankInventory.addOneMagnet();
		} else if (missile) {
		    TankInventory.addOneMissile();
		} else if (stunner) {
		    TankInventory.addOneStunner();
		} else if (blue) {
		    TankInventory.addOneBlueLaser();
		} else if (disrupt) {
		    TankInventory.addOneDisruptor();
		} else if (bomb) {
		    TankInventory.addOneBomb();
		} else if (heatBomb) {
		    TankInventory.addOneHeatBomb();
		} else if (iceBomb) {
		    TankInventory.addOneIceBomb();
		}
	    } else if (laser) {
		this.updateScore(0, -1, 0);
	    } else {
		this.updateScore(-1, 0, 0);
	    }
	    try {
		this.resetPlayerLocation();
	    } catch (final InvalidArenaException iae) {
		CommonDialogs.showErrorDialog(StringLoader.loadError(ErrorString.TANK_LOCATION),
			GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME));
		this.exitGame();
		return;
	    }
	    this.updateTank();
	    GameManager.updateRedo(laser, missile, stunner, boost, magnet, blue, disrupt, bomb, heatBomb, iceBomb);
	}
	app.getMenuManager().updateMenuItemState();
	this.updateScoreText();
	a.setDirtyFlags(this.plMgr.getPlayerLocationZ());
	this.redrawArena();
    }

    void unscheduleAutoMove() {
	this.autoMove = false;
    }

    private void updateInfo() {
	final AbstractArena a = LaserTank.getApplication().getArenaManager().getArena();
	this.levelInfo.setText(StringLoader.loadGame(GameString.LEVEL) + StringLoader.loadCommon(CommonString.SPACE)
		+ (a.getActiveLevelNumber() + 1) + StringLoader.loadCommon(CommonString.COLON)
		+ StringLoader.loadCommon(CommonString.SPACE) + a.getName().trim()
		+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadDialog(DialogString.ARENA_LEVEL_BY)
		+ StringLoader.loadCommon(CommonString.SPACE) + a.getAuthor().trim());
    }

    public void updatePositionAbsoluteNoEvents(final int z) {
	final int x = this.plMgr.getPlayerLocationX();
	final int y = this.plMgr.getPlayerLocationY();
	this.updatePositionAbsoluteNoEvents(x, y, z);
    }

    public void updatePositionAbsoluteNoEvents(final int x, final int y, final int z) {
	final Tank template = new Tank(this.plMgr.getActivePlayerNumber() + 1);
	final Application app = LaserTank.getApplication();
	final AbstractArena m = app.getArenaManager().getArena();
	this.plMgr.savePlayerLocation();
	try {
	    if (!m.getCell(x, y, z, template.getLayer()).isConditionallySolid()) {
		if (z != 0) {
		    this.suspendAnimator();
		    m.setDirtyFlags(this.plMgr.getPlayerLocationZ());
		    m.setDirtyFlags(z);
		}
		m.setCell(this.tank.getSavedObject(), this.plMgr.getPlayerLocationX(), this.plMgr.getPlayerLocationY(),
			this.plMgr.getPlayerLocationZ(), template.getLayer());
		this.plMgr.setPlayerLocation(x, y, z);
		this.tank.setSavedObject(m.getCell(this.plMgr.getPlayerLocationX(), this.plMgr.getPlayerLocationY(),
			this.plMgr.getPlayerLocationZ(), template.getLayer()));
		m.setCell(this.tank, this.plMgr.getPlayerLocationX(), this.plMgr.getPlayerLocationY(),
			this.plMgr.getPlayerLocationZ(), template.getLayer());
		app.getArenaManager().setDirty(true);
		if (z != 0) {
		    this.resumeAnimator();
		}
	    }
	} catch (final ArrayIndexOutOfBoundsException ae) {
	    this.plMgr.restorePlayerLocation();
	    m.setCell(this.tank, this.plMgr.getPlayerLocationX(), this.plMgr.getPlayerLocationY(),
		    this.plMgr.getPlayerLocationZ(), template.getLayer());
	    LaserTank.getApplication().showMessage(StringLoader.loadGame(GameString.OUTSIDE_ARENA));
	} catch (final NullPointerException np) {
	    this.plMgr.restorePlayerLocation();
	    m.setCell(this.tank, this.plMgr.getPlayerLocationX(), this.plMgr.getPlayerLocationY(),
		    this.plMgr.getPlayerLocationZ(), template.getLayer());
	    LaserTank.getApplication().showMessage(StringLoader.loadGame(GameString.OUTSIDE_ARENA));
	}
    }

    void updatePositionRelative(final int x, final int y) {
	if (!this.moving) {
	    this.moving = true;
	    if (this.mlot == null) {
		this.mlot = new MLOTask();
	    } else {
		if (!this.mlot.isAlive()) {
		    this.mlot = new MLOTask();
		}
	    }
	    this.mlot.activateMovement(x, y);
	    if (!this.mlot.isAlive()) {
		this.mlot.start();
	    }
	    if (this.replaying) {
		// Wait
		while (this.moving) {
		    try {
			Thread.sleep(100);
		    } catch (final InterruptedException ie) {
			// Ignore
		    }
		}
	    }
	}
    }

    public void updatePositionRelativeFrozen() {
	if (this.mlot == null) {
	    this.mlot = new MLOTask();
	} else {
	    if (!this.mlot.isAlive()) {
		this.mlot = new MLOTask();
	    }
	}
	final Direction dir = this.getTank().getDirection();
	final int[] unres = DirectionResolver.unresolveRelative(dir);
	final int x = unres[0];
	final int y = unres[1];
	this.mlot.activateFrozenMovement(x, y);
	if (!this.mlot.isAlive()) {
	    this.mlot.start();
	}
	if (this.replaying) {
	    // Wait
	    while (this.moving) {
		try {
		    Thread.sleep(100);
		} catch (final InterruptedException ie) {
		    // Ignore
		}
	    }
	}
    }

    public void updatePushedIntoPositionAbsolute(final int x, final int y, final int z, final int x2, final int y2,
	    final int z2, final AbstractMovableObject pushedInto, final AbstractArenaObject source) {
	final Tank template = new Tank(this.plMgr.getActivePlayerNumber() + 1);
	final Application app = LaserTank.getApplication();
	final AbstractArena m = app.getArenaManager().getArena();
	boolean needsFixup1 = false;
	boolean needsFixup2 = false;
	try {
	    if (!m.getCell(x, y, z, pushedInto.getLayer()).isConditionallySolid()) {
		final AbstractArenaObject saved = m.getCell(x, y, z, pushedInto.getLayer());
		final AbstractArenaObject there = m.getCell(x2, y2, z2, pushedInto.getLayer());
		if (there.isOfType(TypeConstants.TYPE_CHARACTER)) {
		    needsFixup1 = true;
		}
		if (saved.isOfType(TypeConstants.TYPE_CHARACTER)) {
		    needsFixup2 = true;
		}
		if (needsFixup2) {
		    m.setCell(this.tank, x, y, z, template.getLayer());
		    pushedInto.setSavedObject(saved.getSavedObject());
		    this.tank.setSavedObject(pushedInto);
		} else {
		    m.setCell(pushedInto, x, y, z, pushedInto.getLayer());
		    pushedInto.setSavedObject(saved);
		}
		if (needsFixup1) {
		    m.setCell(this.tank, x2, y2, z2, template.getLayer());
		    this.tank.setSavedObject(source);
		} else {
		    m.setCell(source, x2, y2, z2, pushedInto.getLayer());
		}
		app.getArenaManager().setDirty(true);
	    }
	} catch (final ArrayIndexOutOfBoundsException ae) {
	    final Empty e = new Empty();
	    m.setCell(e, x2, y2, z2, pushedInto.getLayer());
	}
    }

    public synchronized void updatePushedPosition(final int x, final int y, final int pushX, final int pushY,
	    final AbstractMovableObject o) {
	if (this.mlot == null) {
	    this.mlot = new MLOTask();
	} else {
	    if (!this.mlot.isAlive()) {
		this.mlot = new MLOTask();
	    }
	}
	this.mlot.activateObjects(x, y, pushX, pushY, o);
	if (!this.mlot.isAlive()) {
	    this.mlot.start();
	}
    }

    public void updatePushedPositionLater(final int x, final int y, final int pushX, final int pushY,
	    final AbstractMovableObject o, final int x2, final int y2, final AbstractMovableObject other,
	    final int laserType, final int forceUnits) {
	new Thread() {
	    @Override
	    public void run() {
		try {
		    other.laserEnteredAction(x2, y2, GameManager.this.plMgr.getPlayerLocationZ(), pushX, pushY,
			    laserType, forceUnits);
		    GameManager.this.waitForMLOLoop();
		    GameManager.this.updatePushedPosition(x, y, x + pushX, y + pushY, o);
		    GameManager.this.waitForMLOLoop();
		} catch (final Throwable t) {
		    LaserTank.logError(t);
		}
	    }
	}.start();
    }

    void updateReplay(final boolean laser, final int x, final int y) {
	this.gre.updateUndoHistory(laser, x, y);
    }

    private void updateScore() {
	this.scoreMoves.setText(StringLoader.loadGame(GameString.MOVES) + StringLoader.loadCommon(CommonString.COLON)
		+ StringLoader.loadCommon(CommonString.SPACE) + this.st.getMoves());
	this.scoreShots.setText(StringLoader.loadGame(GameString.SHOTS) + StringLoader.loadCommon(CommonString.COLON)
		+ StringLoader.loadCommon(CommonString.SPACE) + this.st.getShots());
	this.scoreShots.setText(StringLoader.loadGame(GameString.OTHERS) + StringLoader.loadCommon(CommonString.COLON)
		+ StringLoader.loadCommon(CommonString.SPACE) + this.st.getOthers());
	this.updateScoreText();
    }

    void updateScore(final int moves, final int shots, final int others) {
	if (moves > 0) {
	    this.st.incrementMoves();
	} else if (moves < 0) {
	    this.st.decrementMoves();
	}
	if (shots > 0) {
	    this.st.incrementShots();
	} else if (shots < 0) {
	    this.st.decrementShots();
	}
	if (others > 0) {
	    this.st.incrementOthers();
	} else if (others < 0) {
	    this.st.decrementOthers();
	}
	this.scoreMoves.setText(StringLoader.loadGame(GameString.MOVES) + StringLoader.loadCommon(CommonString.COLON)
		+ StringLoader.loadCommon(CommonString.SPACE) + this.st.getMoves());
	this.scoreShots.setText(StringLoader.loadGame(GameString.SHOTS) + StringLoader.loadCommon(CommonString.COLON)
		+ StringLoader.loadCommon(CommonString.SPACE) + this.st.getShots());
	this.scoreOthers.setText(StringLoader.loadGame(GameString.OTHERS) + StringLoader.loadCommon(CommonString.COLON)
		+ StringLoader.loadCommon(CommonString.SPACE) + this.st.getOthers());
	this.updateScoreText();
    }

    private void updateScoreText() {
	// Ammo
	if (this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_MISSILES) {
	    if (this.getCheatStatus(GameManager.CHEAT_MISSILES)) {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.MISSILES) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadGame(GameString.INFINITE)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    } else {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.MISSILES) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + TankInventory.getMissilesLeft()
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    }
	} else if (this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_STUNNERS) {
	    if (this.getCheatStatus(GameManager.CHEAT_STUNNERS)) {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.STUNNERS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadGame(GameString.INFINITE)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    } else {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.STUNNERS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + TankInventory.getStunnersLeft()
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    }
	} else if (this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_BLUE_LASERS) {
	    if (this.getCheatStatus(GameManager.CHEAT_BLUE_LASERS)) {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.BLUE_LASERS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadGame(GameString.INFINITE)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    } else {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.BLUE_LASERS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + TankInventory.getBlueLasersLeft()
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    }
	} else if (this.otherAmmoMode == GameManager.OTHER_AMMO_MODE_DISRUPTORS) {
	    if (this.getCheatStatus(GameManager.CHEAT_DISRUPTORS)) {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.DISRUPTORS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadGame(GameString.INFINITE)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    } else {
		this.otherAmmoLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.DISRUPTORS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + TankInventory.getDisruptorsLeft()
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    }
	}
	// Tools
	if (this.otherToolMode == GameManager.OTHER_TOOL_MODE_BOOSTS) {
	    if (this.getCheatStatus(GameManager.CHEAT_BOOSTS)) {
		this.otherToolsLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.BOOSTS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadGame(GameString.INFINITE)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    } else {
		this.otherToolsLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.BOOSTS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + TankInventory.getBoostsLeft()
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    }
	} else if (this.otherToolMode == GameManager.OTHER_TOOL_MODE_MAGNETS) {
	    if (this.getCheatStatus(GameManager.CHEAT_MAGNETS)) {
		this.otherToolsLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.MAGNETS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadGame(GameString.INFINITE)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    } else {
		this.otherToolsLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.MAGNETS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + TankInventory.getMagnetsLeft()
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    }
	}
	// Ranges
	if (this.otherRangeMode == GameManager.OTHER_RANGE_MODE_BOMBS) {
	    if (this.getCheatStatus(GameManager.CHEAT_BOMBS)) {
		this.otherRangesLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.BOMBS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadGame(GameString.INFINITE)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    } else {
		this.otherRangesLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.BOMBS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + TankInventory.getBombsLeft()
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    }
	} else if (this.otherRangeMode == GameManager.OTHER_RANGE_MODE_HEAT_BOMBS) {
	    if (this.getCheatStatus(GameManager.CHEAT_HEAT_BOMBS)) {
		this.otherRangesLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.HEAT_BOMBS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadGame(GameString.INFINITE)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    } else {
		this.otherRangesLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.HEAT_BOMBS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + TankInventory.getHeatBombsLeft()
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    }
	} else if (this.otherRangeMode == GameManager.OTHER_RANGE_MODE_ICE_BOMBS) {
	    if (this.getCheatStatus(GameManager.CHEAT_ICE_BOMBS)) {
		this.otherRangesLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.ICE_BOMBS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + StringLoader.loadGame(GameString.INFINITE)
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    } else {
		this.otherRangesLeft.setText(StringLoader.loadCommon(CommonString.OPEN_PARENTHESES)
			+ StringLoader.loadGame(GameString.ICE_BOMBS) + StringLoader.loadCommon(CommonString.COLON)
			+ StringLoader.loadCommon(CommonString.SPACE) + TankInventory.getIceBombsLeft()
			+ StringLoader.loadCommon(CommonString.CLOSE_PARENTHESES));
	    }
	}
    }

    void updateTank() {
	final Tank template = new Tank(this.plMgr.getActivePlayerNumber() + 1);
	this.tank = (AbstractCharacter) LaserTank.getApplication().getArenaManager().getArena().getCell(
		this.plMgr.getPlayerLocationX(), this.plMgr.getPlayerLocationY(), this.plMgr.getPlayerLocationZ(),
		template.getLayer());
    }

    void waitForMLOLoop() {
	if (this.mlot != null && this.mlot.isAlive()) {
	    boolean waiting = true;
	    while (waiting) {
		try {
		    this.mlot.join();
		    waiting = false;
		} catch (final InterruptedException ie) {
		    // Ignore
		}
	    }
	}
    }

    private void writeSolution() {
	try {
	    final int activeLevel = LaserTank.getApplication().getArenaManager().getArena().getActiveLevelNumber();
	    final String levelFile = LaserTank.getApplication().getArenaManager().getLastUsedArena();
	    final String filename = levelFile + StringLoader.loadCommon(CommonString.UNDERSCORE) + activeLevel
		    + Extension.getSolutionExtensionWithPeriod();
	    try (XMLFileWriter file = new XMLFileWriter(filename,
		    GlobalLoader.loadUntranslated(UntranslatedString.SOLUTION))) {
		this.gre.writeReplay(file);
	    }
	} catch (final IOException ioe) {
	    throw new InvalidArenaException(ioe);
	}
    }

    public void setUp() {
	Application app = LaserTank.getApplication();
	app.setTitle(GlobalLoader.loadUntranslated(UntranslatedString.PROGRAM_NAME));
	app.addKeyListener(this.handler);
	app.addWindowListener(this.handler);
	app.addWindowFocusListener(this.fHandler);
    }

    public void tearDown() {
	Application app = LaserTank.getApplication();
	app.removeKeyListener(this.handler);
	app.removeWindowListener(this.handler);
	app.removeWindowFocusListener(this.fHandler);
    }
}