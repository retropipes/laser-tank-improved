/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.game;

import com.puttysoftware.storage.NumberStorage;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.AbstractArena;
import com.puttysoftware.ltremix.arena.current.CurrentArenaData;

public final class PlayerLocationManager {
    // Fields
    private int playerInstance;
    private int saveX, saveY, saveZ;
    private NumberStorage playerData;
    private NumberStorage savedPlayerData;
    private static final int PLAYER_DIMS = 3;
    private static final int NUM_PLAYERS = 9;
    private static final int NUM_ERAS = 3;

    // Constructors
    public PlayerLocationManager() {
	this.playerInstance = 0;
	this.saveX = 0;
	this.saveY = 0;
	this.saveZ = 0;
	this.playerData = new NumberStorage(PlayerLocationManager.PLAYER_DIMS, PlayerLocationManager.NUM_PLAYERS,
		PlayerLocationManager.NUM_ERAS);
	this.playerData.fill(-1);
	this.savedPlayerData = new NumberStorage(PlayerLocationManager.PLAYER_DIMS, PlayerLocationManager.NUM_PLAYERS,
		PlayerLocationManager.NUM_ERAS);
	this.savedPlayerData.fill(-1);
    }

    // Methods
    public int getPlayerLocationX() {
	final AbstractArena a = LTRemix.getApplication().getArenaManager().getArena();
	return this.playerData.getCell(1, this.playerInstance, a.getEra());
    }

    public int getPlayerLocationY() {
	final AbstractArena a = LTRemix.getApplication().getArenaManager().getArena();
	return this.playerData.getCell(0, this.playerInstance, a.getEra());
    }

    public int getPlayerLocationZ() {
	final AbstractArena a = LTRemix.getApplication().getArenaManager().getArena();
	return this.playerData.getCell(2, this.playerInstance, a.getEra());
    }

    private void setPlayerLocationX(final int val) {
	final AbstractArena a = LTRemix.getApplication().getArenaManager().getArena();
	this.playerData.setCell(val, 1, this.playerInstance, a.getEra());
    }

    private void setPlayerLocationY(final int val) {
	final AbstractArena a = LTRemix.getApplication().getArenaManager().getArena();
	this.playerData.setCell(val, 0, this.playerInstance, a.getEra());
    }

    private void setPlayerLocationZ(final int val) {
	final AbstractArena a = LTRemix.getApplication().getArenaManager().getArena();
	this.playerData.setCell(val, 2, this.playerInstance, a.getEra());
    }

    void initPlayerLocations() {
	final AbstractArena a = LTRemix.getApplication().getArenaManager().getArena();
	for (int pi = 0; pi < PlayerLocationManager.NUM_PLAYERS; pi++) {
	    for (int e = 0; e < PlayerLocationManager.NUM_ERAS; e++) {
		final int valX = a.getPlayerColumn(pi, e);
		final int valY = a.getPlayerRow(pi, e);
		final int valZ = a.getPlayerFloor(pi, e);
		this.initPlayerLocation(valX, valY, valZ, pi, e);
	    }
	}
    }

    private void initPlayerLocation(final int valX, final int valY, final int valZ, final int pi, final int e) {
	this.playerData.setCell(valX, 1, pi, e);
	this.playerData.setCell(valY, 0, pi, e);
	this.playerData.setCell(valZ, 2, pi, e);
    }

    public void setPlayerLocation(final int valX, final int valY, final int valZ) {
	this.setPlayerLocationX(valX);
	this.setPlayerLocationY(valY);
	this.setPlayerLocationZ(valZ);
    }

    void offsetPlayerLocationX(final int val) {
	final AbstractArena a = LTRemix.getApplication().getArenaManager().getArena();
	this.playerData.setCell(this.getPlayerLocationX() + val, 1, this.playerInstance, a.getEra());
    }

    void offsetPlayerLocationY(final int val) {
	final AbstractArena a = LTRemix.getApplication().getArenaManager().getArena();
	this.playerData.setCell(this.getPlayerLocationY() + val, 0, this.playerInstance, a.getEra());
    }

    public void togglePlayerInstance() {
	boolean doesNotExist = true;
	while (doesNotExist) {
	    this.playerInstance++;
	    if (this.playerInstance >= CurrentArenaData.getMaxPlayers()) {
		this.playerInstance = 0;
	    }
	    final int px = this.getPlayerLocationX();
	    final int py = this.getPlayerLocationY();
	    final int pz = this.getPlayerLocationZ();
	    if (px != -1 && py != -1 && pz != -1) {
		doesNotExist = false;
	    }
	}
    }

    void savePlayerLocation() {
	this.savedPlayerData = new NumberStorage(this.playerData);
    }

    void restorePlayerLocation() {
	this.playerData = new NumberStorage(this.savedPlayerData);
    }

    void saveRemoteLocation() {
	this.saveX = this.getPlayerLocationX();
	this.saveY = this.getPlayerLocationY();
	this.saveZ = this.getPlayerLocationZ();
    }

    void restoreRemoteLocation() {
	this.setPlayerLocationX(this.saveX);
	this.setPlayerLocationY(this.saveY);
	this.setPlayerLocationZ(this.saveZ);
    }
}
