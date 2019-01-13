/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.arena.v4;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.puttysoftware.lasertank.arena.AbstractArena;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.current.CurrentArenaData;
import com.puttysoftware.lasertank.arena.objects.AntiTank;
import com.puttysoftware.lasertank.arena.objects.Box;
import com.puttysoftware.lasertank.arena.objects.Bricks;
import com.puttysoftware.lasertank.arena.objects.CrystalBlock;
import com.puttysoftware.lasertank.arena.objects.Empty;
import com.puttysoftware.lasertank.arena.objects.Flag;
import com.puttysoftware.lasertank.arena.objects.Ground;
import com.puttysoftware.lasertank.arena.objects.Ice;
import com.puttysoftware.lasertank.arena.objects.Mirror;
import com.puttysoftware.lasertank.arena.objects.RotaryMirror;
import com.puttysoftware.lasertank.arena.objects.Tank;
import com.puttysoftware.lasertank.arena.objects.TankMover;
import com.puttysoftware.lasertank.arena.objects.ThinIce;
import com.puttysoftware.lasertank.arena.objects.Tunnel;
import com.puttysoftware.lasertank.arena.objects.Wall;
import com.puttysoftware.lasertank.arena.objects.Water;
import com.puttysoftware.lasertank.strings.global.GlobalLoader;
import com.puttysoftware.lasertank.strings.global.UntranslatedString;
import com.puttysoftware.lasertank.utilities.ColorConstants;
import com.puttysoftware.lasertank.utilities.Direction;
import com.puttysoftware.lasertank.utilities.InvalidArenaException;

class V4FileLevel {
    // Fields
    private static byte[] objects;
    private static byte[] name;
    private static byte[] hint;
    private static byte[] author;
    private static byte[] difficulty;
    private static final int OBJECTS_SIZE = 256;
    private static final int NAME_SIZE = 31;
    private static final int HINT_SIZE = 256;
    private static final int AUTHOR_SIZE = 31;
    private static final int DIFFICULTY_SIZE = 2;

    // Methods
    static CurrentArenaData loadAndConvert(final FileInputStream file, final AbstractArena a) {
	try {
	    V4FileLevel.objects = new byte[V4FileLevel.OBJECTS_SIZE];
	    V4FileLevel.name = new byte[V4FileLevel.NAME_SIZE];
	    V4FileLevel.hint = new byte[V4FileLevel.HINT_SIZE];
	    V4FileLevel.author = new byte[V4FileLevel.AUTHOR_SIZE];
	    V4FileLevel.difficulty = new byte[V4FileLevel.DIFFICULTY_SIZE];
	    final CurrentArenaData t = new CurrentArenaData();
	    // Convert object byte map
	    int bytesRead = file.read(V4FileLevel.objects, 0, V4FileLevel.OBJECTS_SIZE);
	    if (bytesRead != V4FileLevel.OBJECTS_SIZE) {
		return null;
	    }
	    for (int x = 0; x < 16; x++) {
		for (int y = 0; y < 16; y++) {
		    final int z = x * 16 + y;
		    AbstractArenaObject ao = null;
		    final byte b = V4FileLevel.objects[z];
		    switch (b) {
		    case 0:
			ao = new Ground();
			break;
		    case 1:
			ao = new Tank(1);
			break;
		    case 2:
			ao = new Flag();
			break;
		    case 3:
			ao = new Water();
			break;
		    case 4:
			ao = new Wall();
			break;
		    case 5:
			ao = new Box();
			break;
		    case 6:
			ao = new Bricks();
			break;
		    case 7:
			ao = new AntiTank();
			ao.setDirection(Direction.NORTH);
			break;
		    case 8:
			ao = new AntiTank();
			ao.setDirection(Direction.EAST);
			break;
		    case 9:
			ao = new AntiTank();
			ao.setDirection(Direction.SOUTH);
			break;
		    case 10:
			ao = new AntiTank();
			ao.setDirection(Direction.WEST);
			break;
		    case 11:
			ao = new Mirror();
			ao.setDirection(Direction.NORTHWEST);
			break;
		    case 12:
			ao = new Mirror();
			ao.setDirection(Direction.NORTHEAST);
			break;
		    case 13:
			ao = new Mirror();
			ao.setDirection(Direction.SOUTHEAST);
			break;
		    case 14:
			ao = new Mirror();
			ao.setDirection(Direction.SOUTHWEST);
			break;
		    case 15:
			ao = new TankMover();
			ao.setDirection(Direction.NORTH);
			break;
		    case 16:
			ao = new TankMover();
			ao.setDirection(Direction.EAST);
			break;
		    case 17:
			ao = new TankMover();
			ao.setDirection(Direction.SOUTH);
			break;
		    case 18:
			ao = new TankMover();
			ao.setDirection(Direction.WEST);
			break;
		    case 19:
			ao = new CrystalBlock();
			break;
		    case 20:
			ao = new RotaryMirror();
			ao.setDirection(Direction.NORTHWEST);
			break;
		    case 21:
			ao = new RotaryMirror();
			ao.setDirection(Direction.NORTHEAST);
			break;
		    case 22:
			ao = new RotaryMirror();
			ao.setDirection(Direction.SOUTHEAST);
			break;
		    case 23:
			ao = new RotaryMirror();
			ao.setDirection(Direction.SOUTHWEST);
			break;
		    case 24:
			ao = new Ice();
			break;
		    case 25:
			ao = new ThinIce();
			break;
		    case 64:
		    case 65:
			ao = new Tunnel();
			ao.setColor(ColorConstants.COLOR_RED);
			break;
		    case 66:
		    case 67:
			ao = new Tunnel();
			ao.setColor(ColorConstants.COLOR_GREEN);
			break;
		    case 68:
		    case 69:
			ao = new Tunnel();
			ao.setColor(ColorConstants.COLOR_BLUE);
			break;
		    case 70:
		    case 71:
			ao = new Tunnel();
			ao.setColor(ColorConstants.COLOR_CYAN);
			break;
		    case 72:
		    case 73:
			ao = new Tunnel();
			ao.setColor(ColorConstants.COLOR_YELLOW);
			break;
		    case 74:
		    case 75:
			ao = new Tunnel();
			ao.setColor(ColorConstants.COLOR_MAGENTA);
			break;
		    case 76:
		    case 77:
			ao = new Tunnel();
			ao.setColor(ColorConstants.COLOR_WHITE);
			break;
		    case 78:
		    case 79:
			ao = new Tunnel();
			ao.setColor(ColorConstants.COLOR_GRAY);
			break;
		    default:
			ao = new Empty();
		    }
		    t.setCell(a, ao, x, y, 0, ao.getLayer());
		}
	    }
	    // Convert level name
	    bytesRead = file.read(V4FileLevel.name, 0, V4FileLevel.NAME_SIZE);
	    if (bytesRead != V4FileLevel.NAME_SIZE) {
		return null;
	    }
	    final String levelName = Charset.forName(GlobalLoader.loadUntranslated(UntranslatedString.DEFAULT_CHARSET))
		    .decode(ByteBuffer.wrap(V4FileLevel.name)).toString();
	    a.setName(levelName);
	    // Convert level hint
	    bytesRead = file.read(V4FileLevel.hint, 0, V4FileLevel.HINT_SIZE);
	    if (bytesRead != V4FileLevel.HINT_SIZE) {
		return null;
	    }
	    final String levelHint = Charset.forName(GlobalLoader.loadUntranslated(UntranslatedString.DEFAULT_CHARSET))
		    .decode(ByteBuffer.wrap(V4FileLevel.hint)).toString();
	    a.setHint(levelHint);
	    // Convert level author
	    bytesRead = file.read(V4FileLevel.author, 0, V4FileLevel.AUTHOR_SIZE);
	    if (bytesRead != V4FileLevel.AUTHOR_SIZE) {
		return null;
	    }
	    final String levelAuthor = Charset
		    .forName(GlobalLoader.loadUntranslated(UntranslatedString.DEFAULT_CHARSET))
		    .decode(ByteBuffer.wrap(V4FileLevel.author)).toString();
	    a.setAuthor(levelAuthor);
	    // Convert level difficulty
	    bytesRead = file.read(V4FileLevel.difficulty, 0, V4FileLevel.DIFFICULTY_SIZE);
	    if (bytesRead != V4FileLevel.DIFFICULTY_SIZE) {
		return null;
	    }
	    final int tempDiff = V4FileLevel.toInt(V4FileLevel.difficulty);
	    switch (tempDiff) {
	    case 1:
		a.setDifficulty(1);
		break;
	    case 2:
		a.setDifficulty(2);
		break;
	    case 4:
		a.setDifficulty(3);
		break;
	    case 8:
		a.setDifficulty(4);
		break;
	    case 16:
		a.setDifficulty(5);
		break;
	    default:
		a.setDifficulty(3);
		break;
	    }
	    t.fillNulls(a, new Ground(), new Wall(), true);
	    t.resize(a, AbstractArena.getMinFloors(), new Empty());
	    t.fillVirtual();
	    return t;
	} catch (final IOException ioe) {
	    throw new InvalidArenaException(ioe);
	}
    }

    private static int toInt(final byte[] data) {
	if (data == null || data.length != 2) {
	    return 0x0;
	}
	return (0xff & data[0]) << 0 | (0xff & data[1]) << 8;
    }

    // Constructors
    private V4FileLevel() {
	// Do nothing
    }
}
