/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import java.io.IOException;

import com.puttysoftware.fileio.XMLFileReader;
import com.puttysoftware.fileio.XMLFileWriter;

public class TankInventory {
    // Fields
    private static int missilesLeft = 0;
    private static int stunnersLeft = 0;
    private static int boostsLeft = 0;
    private static int magnetsLeft = 0;
    private static int blueLasersLeft = 0;
    private static int disruptorsLeft = 0;
    private static int redKeysLeft = 0;
    private static int greenKeysLeft = 0;
    private static int blueKeysLeft = 0;
    private static int bombsLeft = 0;
    private static int heatBombsLeft = 0;
    private static int iceBombsLeft = 0;

    public static void addOneBlueKey() {
	TankInventory.blueKeysLeft++;
    }

    public static void addOneBlueLaser() {
	TankInventory.blueLasersLeft++;
    }

    public static void addOneBomb() {
	TankInventory.bombsLeft++;
    }

    public static void addOneBoost() {
	TankInventory.boostsLeft++;
    }

    public static void addOneDisruptor() {
	TankInventory.disruptorsLeft++;
    }

    public static void addOneGreenKey() {
	TankInventory.greenKeysLeft++;
    }

    public static void addOneHeatBomb() {
	TankInventory.heatBombsLeft++;
    }

    public static void addOneIceBomb() {
	TankInventory.iceBombsLeft++;
    }

    public static void addOneMagnet() {
	TankInventory.magnetsLeft++;
    }

    public static void addOneMissile() {
	TankInventory.missilesLeft++;
    }

    public static void addOneRedKey() {
	TankInventory.redKeysLeft++;
    }

    public static void addOneStunner() {
	TankInventory.stunnersLeft++;
    }

    public static void addTenBlueLasers() {
	TankInventory.blueLasersLeft += 10;
    }

    public static void addTenBombs() {
	TankInventory.bombsLeft += 10;
    }

    public static void addTenBoosts() {
	TankInventory.boostsLeft += 10;
    }

    public static void addTenDisruptors() {
	TankInventory.disruptorsLeft += 10;
    }

    public static void addTenHeatBombs() {
	TankInventory.heatBombsLeft += 10;
    }

    public static void addTenIceBombs() {
	TankInventory.iceBombsLeft += 10;
    }

    public static void addTenMagnets() {
	TankInventory.magnetsLeft += 10;
    }

    public static void addTenMissiles() {
	TankInventory.missilesLeft += 10;
    }

    public static void addTenStunners() {
	TankInventory.stunnersLeft += 10;
    }

    public static void fireBlueLaser() {
	TankInventory.blueLasersLeft--;
    }

    public static void fireBomb() {
	TankInventory.bombsLeft--;
    }

    public static void fireBoost() {
	TankInventory.boostsLeft--;
    }

    public static void fireDisruptor() {
	TankInventory.disruptorsLeft--;
    }

    public static void fireHeatBomb() {
	TankInventory.heatBombsLeft--;
    }

    public static void fireIceBomb() {
	TankInventory.iceBombsLeft--;
    }

    public static void fireMagnet() {
	TankInventory.magnetsLeft--;
    }

    public static void fireMissile() {
	TankInventory.missilesLeft--;
    }

    public static void fireStunner() {
	TankInventory.stunnersLeft--;
    }

    public static int getBlueKeysLeft() {
	return TankInventory.blueKeysLeft;
    }

    public static int getBlueLasersLeft() {
	return TankInventory.blueLasersLeft;
    }

    public static int getBombsLeft() {
	return TankInventory.bombsLeft;
    }

    public static int getBoostsLeft() {
	return TankInventory.boostsLeft;
    }

    public static int getDisruptorsLeft() {
	return TankInventory.disruptorsLeft;
    }

    public static int getGreenKeysLeft() {
	return TankInventory.greenKeysLeft;
    }

    public static int getHeatBombsLeft() {
	return TankInventory.heatBombsLeft;
    }

    public static int getIceBombsLeft() {
	return TankInventory.iceBombsLeft;
    }

    public static int getMagnetsLeft() {
	return TankInventory.magnetsLeft;
    }

    public static int getMissilesLeft() {
	return TankInventory.missilesLeft;
    }

    public static int getRedKeysLeft() {
	return TankInventory.redKeysLeft;
    }

    public static int getStunnersLeft() {
	return TankInventory.stunnersLeft;
    }

    public static void readInventory(final XMLFileReader reader) throws IOException {
	TankInventory.missilesLeft = reader.readInt();
	TankInventory.stunnersLeft = reader.readInt();
	TankInventory.boostsLeft = reader.readInt();
	TankInventory.magnetsLeft = reader.readInt();
	TankInventory.blueLasersLeft = reader.readInt();
	TankInventory.disruptorsLeft = reader.readInt();
	TankInventory.redKeysLeft = reader.readInt();
	TankInventory.greenKeysLeft = reader.readInt();
	TankInventory.blueKeysLeft = reader.readInt();
	TankInventory.bombsLeft = reader.readInt();
	TankInventory.heatBombsLeft = reader.readInt();
	TankInventory.iceBombsLeft = reader.readInt();
    }

    public static void resetInventory() {
	TankInventory.missilesLeft = 0;
	TankInventory.stunnersLeft = 0;
	TankInventory.boostsLeft = 0;
	TankInventory.magnetsLeft = 0;
	TankInventory.blueLasersLeft = 0;
	TankInventory.disruptorsLeft = 0;
	TankInventory.redKeysLeft = 0;
	TankInventory.greenKeysLeft = 0;
	TankInventory.blueKeysLeft = 0;
	TankInventory.bombsLeft = 0;
	TankInventory.heatBombsLeft = 0;
	TankInventory.iceBombsLeft = 0;
    }

    public static void setBlueKeysLeft(final int newBlueKeys) {
	TankInventory.blueKeysLeft = newBlueKeys;
    }

    public static void setGreenKeysLeft(final int newGreenKeys) {
	TankInventory.greenKeysLeft = newGreenKeys;
    }

    public static void setRedKeysLeft(final int newRedKeys) {
	TankInventory.redKeysLeft = newRedKeys;
    }

    public static void useBlueKey() {
	TankInventory.blueKeysLeft--;
    }

    public static void useGreenKey() {
	TankInventory.greenKeysLeft--;
    }

    public static void useRedKey() {
	TankInventory.redKeysLeft--;
    }

    public static void writeInventory(final XMLFileWriter writer) throws IOException {
	writer.writeInt(TankInventory.missilesLeft);
	writer.writeInt(TankInventory.stunnersLeft);
	writer.writeInt(TankInventory.boostsLeft);
	writer.writeInt(TankInventory.magnetsLeft);
	writer.writeInt(TankInventory.blueLasersLeft);
	writer.writeInt(TankInventory.disruptorsLeft);
	writer.writeInt(TankInventory.redKeysLeft);
	writer.writeInt(TankInventory.greenKeysLeft);
	writer.writeInt(TankInventory.blueKeysLeft);
	writer.writeInt(TankInventory.bombsLeft);
	writer.writeInt(TankInventory.heatBombsLeft);
	writer.writeInt(TankInventory.iceBombsLeft);
    }

    // Constructor
    private TankInventory() {
	// Do nothing
    }
}
