/*  LaserTank: An Arena-Solving Game
 Copyright (C) 2008-2013 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.lasertank.utilities;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.puttysoftware.fileio.XMLFileReader;
import com.puttysoftware.images.BufferedImageIcon;
import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.lasertank.arena.objects.AntiBelt;
import com.puttysoftware.lasertank.arena.objects.AntiTank;
import com.puttysoftware.lasertank.arena.objects.AntiTankMover;
import com.puttysoftware.lasertank.arena.objects.Ball;
import com.puttysoftware.lasertank.arena.objects.Barrel;
import com.puttysoftware.lasertank.arena.objects.BlueDoor;
import com.puttysoftware.lasertank.arena.objects.BlueKey;
import com.puttysoftware.lasertank.arena.objects.Box;
import com.puttysoftware.lasertank.arena.objects.BoxMover;
import com.puttysoftware.lasertank.arena.objects.Bricks;
import com.puttysoftware.lasertank.arena.objects.Bridge;
import com.puttysoftware.lasertank.arena.objects.Cloak;
import com.puttysoftware.lasertank.arena.objects.Cracked;
import com.puttysoftware.lasertank.arena.objects.Crumbling;
import com.puttysoftware.lasertank.arena.objects.CrystalBlock;
import com.puttysoftware.lasertank.arena.objects.Damaged;
import com.puttysoftware.lasertank.arena.objects.Darkness;
import com.puttysoftware.lasertank.arena.objects.DeadAntiTank;
import com.puttysoftware.lasertank.arena.objects.DeepWater;
import com.puttysoftware.lasertank.arena.objects.DeeperWater;
import com.puttysoftware.lasertank.arena.objects.DeepestWater;
import com.puttysoftware.lasertank.arena.objects.Empty;
import com.puttysoftware.lasertank.arena.objects.ExplodingBarrel;
import com.puttysoftware.lasertank.arena.objects.FireAllButton;
import com.puttysoftware.lasertank.arena.objects.FireAllButtonDoor;
import com.puttysoftware.lasertank.arena.objects.FirePressureButton;
import com.puttysoftware.lasertank.arena.objects.FirePressureButtonDoor;
import com.puttysoftware.lasertank.arena.objects.FireTriggerButton;
import com.puttysoftware.lasertank.arena.objects.FireTriggerButtonDoor;
import com.puttysoftware.lasertank.arena.objects.Flag;
import com.puttysoftware.lasertank.arena.objects.FreezeSpell;
import com.puttysoftware.lasertank.arena.objects.FrostField;
import com.puttysoftware.lasertank.arena.objects.GreenDoor;
import com.puttysoftware.lasertank.arena.objects.GreenKey;
import com.puttysoftware.lasertank.arena.objects.Ground;
import com.puttysoftware.lasertank.arena.objects.HotBox;
import com.puttysoftware.lasertank.arena.objects.HotCrystalBlock;
import com.puttysoftware.lasertank.arena.objects.HotWall;
import com.puttysoftware.lasertank.arena.objects.Ice;
import com.puttysoftware.lasertank.arena.objects.IceAllButton;
import com.puttysoftware.lasertank.arena.objects.IceAllButtonDoor;
import com.puttysoftware.lasertank.arena.objects.IceBridge;
import com.puttysoftware.lasertank.arena.objects.IcePressureButton;
import com.puttysoftware.lasertank.arena.objects.IcePressureButtonDoor;
import com.puttysoftware.lasertank.arena.objects.IceTriggerButton;
import com.puttysoftware.lasertank.arena.objects.IceTriggerButtonDoor;
import com.puttysoftware.lasertank.arena.objects.IcyBox;
import com.puttysoftware.lasertank.arena.objects.IcyCrystalBlock;
import com.puttysoftware.lasertank.arena.objects.IcyWall;
import com.puttysoftware.lasertank.arena.objects.JumpBox;
import com.puttysoftware.lasertank.arena.objects.KillSpell;
import com.puttysoftware.lasertank.arena.objects.Lava;
import com.puttysoftware.lasertank.arena.objects.MagneticAllButton;
import com.puttysoftware.lasertank.arena.objects.MagneticAllButtonDoor;
import com.puttysoftware.lasertank.arena.objects.MagneticBox;
import com.puttysoftware.lasertank.arena.objects.MagneticMirror;
import com.puttysoftware.lasertank.arena.objects.MagneticPressureButton;
import com.puttysoftware.lasertank.arena.objects.MagneticPressureButtonDoor;
import com.puttysoftware.lasertank.arena.objects.MagneticTriggerButton;
import com.puttysoftware.lasertank.arena.objects.MagneticTriggerButtonDoor;
import com.puttysoftware.lasertank.arena.objects.MagneticWall;
import com.puttysoftware.lasertank.arena.objects.MetallicAllButton;
import com.puttysoftware.lasertank.arena.objects.MetallicAllButtonDoor;
import com.puttysoftware.lasertank.arena.objects.MetallicBox;
import com.puttysoftware.lasertank.arena.objects.MetallicBricks;
import com.puttysoftware.lasertank.arena.objects.MetallicMirror;
import com.puttysoftware.lasertank.arena.objects.MetallicPressureButton;
import com.puttysoftware.lasertank.arena.objects.MetallicPressureButtonDoor;
import com.puttysoftware.lasertank.arena.objects.MetallicRotaryMirror;
import com.puttysoftware.lasertank.arena.objects.MetallicTriggerButton;
import com.puttysoftware.lasertank.arena.objects.MetallicTriggerButtonDoor;
import com.puttysoftware.lasertank.arena.objects.Mirror;
import com.puttysoftware.lasertank.arena.objects.MirrorCrystalBlock;
import com.puttysoftware.lasertank.arena.objects.MirrorMover;
import com.puttysoftware.lasertank.arena.objects.PlasticAllButton;
import com.puttysoftware.lasertank.arena.objects.PlasticAllButtonDoor;
import com.puttysoftware.lasertank.arena.objects.PlasticBox;
import com.puttysoftware.lasertank.arena.objects.PlasticPressureButton;
import com.puttysoftware.lasertank.arena.objects.PlasticPressureButtonDoor;
import com.puttysoftware.lasertank.arena.objects.PlasticTriggerButton;
import com.puttysoftware.lasertank.arena.objects.PlasticTriggerButtonDoor;
import com.puttysoftware.lasertank.arena.objects.PowerBolt;
import com.puttysoftware.lasertank.arena.objects.RedDoor;
import com.puttysoftware.lasertank.arena.objects.RedKey;
import com.puttysoftware.lasertank.arena.objects.ReverseJumpBox;
import com.puttysoftware.lasertank.arena.objects.RollingBarrelHorizontal;
import com.puttysoftware.lasertank.arena.objects.RollingBarrelVertical;
import com.puttysoftware.lasertank.arena.objects.RotaryMirror;
import com.puttysoftware.lasertank.arena.objects.StairsDown;
import com.puttysoftware.lasertank.arena.objects.StairsUp;
import com.puttysoftware.lasertank.arena.objects.StoneAllButton;
import com.puttysoftware.lasertank.arena.objects.StoneAllButtonDoor;
import com.puttysoftware.lasertank.arena.objects.StonePressureButton;
import com.puttysoftware.lasertank.arena.objects.StonePressureButtonDoor;
import com.puttysoftware.lasertank.arena.objects.StoneTriggerButton;
import com.puttysoftware.lasertank.arena.objects.StoneTriggerButtonDoor;
import com.puttysoftware.lasertank.arena.objects.Tank;
import com.puttysoftware.lasertank.arena.objects.TankMover;
import com.puttysoftware.lasertank.arena.objects.TenBlueLasers;
import com.puttysoftware.lasertank.arena.objects.TenBombs;
import com.puttysoftware.lasertank.arena.objects.TenBoosts;
import com.puttysoftware.lasertank.arena.objects.TenDisruptors;
import com.puttysoftware.lasertank.arena.objects.TenHeatBombs;
import com.puttysoftware.lasertank.arena.objects.TenIceBombs;
import com.puttysoftware.lasertank.arena.objects.TenMagnets;
import com.puttysoftware.lasertank.arena.objects.TenMissiles;
import com.puttysoftware.lasertank.arena.objects.TenStunners;
import com.puttysoftware.lasertank.arena.objects.ThinIce;
import com.puttysoftware.lasertank.arena.objects.Tunnel;
import com.puttysoftware.lasertank.arena.objects.UniversalAllButton;
import com.puttysoftware.lasertank.arena.objects.UniversalAllButtonDoor;
import com.puttysoftware.lasertank.arena.objects.UniversalPressureButton;
import com.puttysoftware.lasertank.arena.objects.UniversalPressureButtonDoor;
import com.puttysoftware.lasertank.arena.objects.UniversalTriggerButton;
import com.puttysoftware.lasertank.arena.objects.UniversalTriggerButtonDoor;
import com.puttysoftware.lasertank.arena.objects.UpperGroundEmpty;
import com.puttysoftware.lasertank.arena.objects.UpperObjectsEmpty;
import com.puttysoftware.lasertank.arena.objects.Wall;
import com.puttysoftware.lasertank.arena.objects.Water;
import com.puttysoftware.lasertank.arena.objects.Weakened;
import com.puttysoftware.lasertank.arena.objects.WoodenAllButton;
import com.puttysoftware.lasertank.arena.objects.WoodenAllButtonDoor;
import com.puttysoftware.lasertank.arena.objects.WoodenBox;
import com.puttysoftware.lasertank.arena.objects.WoodenPressureButton;
import com.puttysoftware.lasertank.arena.objects.WoodenPressureButtonDoor;
import com.puttysoftware.lasertank.arena.objects.WoodenTriggerButton;
import com.puttysoftware.lasertank.arena.objects.WoodenTriggerButtonDoor;
import com.puttysoftware.lasertank.arena.objects.WoodenWall;
import com.puttysoftware.lasertank.resourcemanagers.ImageManager;
import com.puttysoftware.lasertank.strings.CommonString;
import com.puttysoftware.lasertank.strings.StringLoader;

public class ArenaObjectList {
    // Fields
    private final AbstractArenaObject[] allObjects = { new UpperGroundEmpty(), new Empty(), new UpperObjectsEmpty(),
	    new Ground(), new TankMover(), new Ice(), new Water(), new ThinIce(), new Bridge(), new Tank(1),
	    new Tank(2), new Tank(3), new Tank(4), new Tank(5), new Tank(6), new Tank(7), new Tank(8), new Tank(9),
	    new Flag(), new Wall(), new AntiTank(), new DeadAntiTank(), new CrystalBlock(), new Bricks(), new Tunnel(),
	    new Mirror(), new RotaryMirror(), new Box(), new AntiTankMover(), new TenMissiles(), new MagneticBox(),
	    new MagneticMirror(), new MirrorCrystalBlock(), new TenStunners(), new TenBoosts(), new TenMagnets(),
	    new MagneticWall(), new FrostField(), new StairsDown(), new StairsUp(), new TenBlueLasers(), new IcyBox(),
	    new BlueDoor(), new BlueKey(), new GreenDoor(), new GreenKey(), new RedDoor(), new RedKey(), new Barrel(),
	    new ExplodingBarrel(), new Ball(), new TenDisruptors(), new TenBombs(), new TenHeatBombs(),
	    new TenIceBombs(), new WoodenWall(), new IcyWall(), new HotWall(), new Lava(), new HotBox(),
	    new MetallicBricks(), new MetallicMirror(), new MetallicRotaryMirror(), new DeepWater(), new DeeperWater(),
	    new DeepestWater(), new WoodenBox(), new IceBridge(), new PlasticBox(), new MetallicBox(),
	    new FireAllButton(), new FireAllButtonDoor(), new FirePressureButton(), new FirePressureButtonDoor(),
	    new FireTriggerButton(), new FireTriggerButtonDoor(), new IceAllButton(), new IceAllButtonDoor(),
	    new IcePressureButton(), new IcePressureButtonDoor(), new IceTriggerButton(), new IceTriggerButtonDoor(),
	    new MagneticAllButton(), new MagneticAllButtonDoor(), new MagneticPressureButton(),
	    new MagneticPressureButtonDoor(), new MagneticTriggerButton(), new MagneticTriggerButtonDoor(),
	    new MetallicAllButton(), new MetallicAllButtonDoor(), new MetallicPressureButton(),
	    new MetallicPressureButtonDoor(), new MetallicTriggerButton(), new MetallicTriggerButtonDoor(),
	    new PlasticAllButton(), new PlasticAllButtonDoor(), new PlasticPressureButton(),
	    new PlasticPressureButtonDoor(), new PlasticTriggerButton(), new PlasticTriggerButtonDoor(),
	    new StoneAllButton(), new StoneAllButtonDoor(), new StonePressureButton(), new StonePressureButtonDoor(),
	    new StoneTriggerButton(), new StoneTriggerButtonDoor(), new UniversalAllButton(),
	    new UniversalAllButtonDoor(), new UniversalPressureButton(), new UniversalPressureButtonDoor(),
	    new UniversalTriggerButton(), new UniversalTriggerButtonDoor(), new WoodenAllButton(),
	    new WoodenAllButtonDoor(), new WoodenPressureButton(), new WoodenPressureButtonDoor(),
	    new WoodenTriggerButton(), new WoodenTriggerButtonDoor(), new BoxMover(), new JumpBox(),
	    new ReverseJumpBox(), new MirrorMover(), new HotCrystalBlock(), new IcyCrystalBlock(), new Cracked(),
	    new Crumbling(), new Damaged(), new Weakened(), new Cloak(), new Darkness(), new PowerBolt(),
	    new RollingBarrelHorizontal(), new RollingBarrelVertical(), new FreezeSpell(), new KillSpell(),
	    new AntiBelt() };

    public void enableAllObjects() {
	for (final AbstractArenaObject allObject : this.allObjects) {
	    allObject.setEnabled(true);
	}
    }

    public BufferedImageIcon[] getAllEditorAppearances() {
	final BufferedImageIcon[] allEditorAppearances = new BufferedImageIcon[this.allObjects.length];
	for (int x = 0; x < allEditorAppearances.length; x++) {
	    allEditorAppearances[x] = ImageManager.getImage(this.allObjects[x], false);
	}
	return allEditorAppearances;
    }

    public BufferedImageIcon[] getAllEditorAppearancesOnLayer(final int layer, final boolean useDisable) {
	if (useDisable) {
	    final BufferedImageIcon[] allEditorAppearancesOnLayer = new BufferedImageIcon[this.allObjects.length];
	    for (int x = 0; x < this.allObjects.length; x++) {
		if (this.allObjects[x].getLayer() == layer) {
		    this.allObjects[x].setEnabled(true);
		} else {
		    this.allObjects[x].setEnabled(false);
		}
		allEditorAppearancesOnLayer[x] = ImageManager.getImage(this.allObjects[x], false);
	    }
	    return allEditorAppearancesOnLayer;
	} else {
	    final BufferedImageIcon[] tempAllEditorAppearancesOnLayer = new BufferedImageIcon[this.allObjects.length];
	    int objectCount = 0;
	    for (int x = 0; x < this.allObjects.length; x++) {
		if (this.allObjects[x].getLayer() == layer) {
		    tempAllEditorAppearancesOnLayer[x] = ImageManager.getImage(this.allObjects[x], false);
		}
	    }
	    for (final BufferedImageIcon element : tempAllEditorAppearancesOnLayer) {
		if (element != null) {
		    objectCount++;
		}
	    }
	    final BufferedImageIcon[] allEditorAppearancesOnLayer = new BufferedImageIcon[objectCount];
	    objectCount = 0;
	    for (final BufferedImageIcon element : tempAllEditorAppearancesOnLayer) {
		if (element != null) {
		    allEditorAppearancesOnLayer[objectCount] = element;
		    objectCount++;
		}
	    }
	    return allEditorAppearancesOnLayer;
	}
    }

    public AbstractArenaObject[] getAllObjectsOnLayer(final int layer, final boolean useDisable) {
	if (useDisable) {
	    for (final AbstractArenaObject allObject : this.allObjects) {
		if (allObject.getLayer() == layer) {
		    allObject.setEnabled(true);
		} else {
		    allObject.setEnabled(false);
		}
	    }
	    return this.allObjects;
	} else {
	    final AbstractArenaObject[] tempAllObjectsOnLayer = new AbstractArenaObject[this.allObjects.length];
	    int objectCount = 0;
	    for (int x = 0; x < this.allObjects.length; x++) {
		if (this.allObjects[x].getLayer() == layer) {
		    tempAllObjectsOnLayer[x] = this.allObjects[x];
		}
	    }
	    for (final AbstractArenaObject element : tempAllObjectsOnLayer) {
		if (element != null) {
		    objectCount++;
		}
	    }
	    final AbstractArenaObject[] allObjectsOnLayer = new AbstractArenaObject[objectCount];
	    objectCount = 0;
	    for (final AbstractArenaObject element : tempAllObjectsOnLayer) {
		if (element != null) {
		    allObjectsOnLayer[objectCount] = element;
		    objectCount++;
		}
	    }
	    return allObjectsOnLayer;
	}
    }

    public boolean[] getObjectEnabledStatuses(final int layer) {
	final boolean[] allObjectEnabledStatuses = new boolean[this.allObjects.length];
	for (int x = 0; x < this.allObjects.length; x++) {
	    if (this.allObjects[x].getLayer() == layer) {
		allObjectEnabledStatuses[x] = true;
	    } else {
		allObjectEnabledStatuses[x] = false;
	    }
	}
	return allObjectEnabledStatuses;
    }

    public AbstractArenaObject readArenaObjectG2(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	AbstractArenaObject o = null;
	String UID = StringLoader.loadCommon(CommonString.SPACE);
	if (FormatConstants.isFormatVersionValidGeneration1(formatVersion)
		|| FormatConstants.isFormatVersionValidGeneration2(formatVersion)) {
	    UID = reader.readString();
	} else {
	    return null;
	}
	for (final AbstractArenaObject allObject : this.allObjects) {
	    try {
		final AbstractArenaObject instance = allObject.getClass().getConstructor().newInstance();
		if (FormatConstants.isFormatVersionValidGeneration1(formatVersion)
			|| FormatConstants.isFormatVersionValidGeneration2(formatVersion)) {
		    o = instance.readArenaObjectG2(reader, UID, formatVersion);
		} else {
		    return null;
		}
		if (o != null) {
		    return o;
		}
	    } catch (final InstantiationException ex) {
		LaserTank.logError(ex);
	    } catch (final IllegalAccessException ex) {
		LaserTank.logError(ex);
	    } catch (final IllegalArgumentException e) {
		LaserTank.logError(e);
	    } catch (final InvocationTargetException e) {
		LaserTank.logError(e);
	    } catch (final NoSuchMethodException e) {
		LaserTank.logError(e);
	    } catch (final SecurityException e) {
		LaserTank.logError(e);
	    }
	}
	return null;
    }

    public AbstractArenaObject readArenaObjectG3(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	AbstractArenaObject o = null;
	String UID = StringLoader.loadCommon(CommonString.SPACE);
	if (FormatConstants.isFormatVersionValidGeneration3(formatVersion)) {
	    UID = reader.readString();
	} else {
	    return null;
	}
	for (final AbstractArenaObject allObject : this.allObjects) {
	    try {
		final AbstractArenaObject instance = allObject.getClass().getConstructor().newInstance();
		if (FormatConstants.isFormatVersionValidGeneration3(formatVersion)) {
		    o = instance.readArenaObjectG3(reader, UID, formatVersion);
		} else {
		    return null;
		}
		if (o != null) {
		    return o;
		}
	    } catch (final InstantiationException ex) {
		LaserTank.logError(ex);
	    } catch (final IllegalAccessException ex) {
		LaserTank.logError(ex);
	    } catch (final IllegalArgumentException e) {
		LaserTank.logError(e);
	    } catch (final InvocationTargetException e) {
		LaserTank.logError(e);
	    } catch (final NoSuchMethodException e) {
		LaserTank.logError(e);
	    } catch (final SecurityException e) {
		LaserTank.logError(e);
	    }
	}
	return null;
    }

    public AbstractArenaObject readArenaObjectG4(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	AbstractArenaObject o = null;
	String UID = StringLoader.loadCommon(CommonString.SPACE);
	if (FormatConstants.isFormatVersionValidGeneration4(formatVersion)) {
	    UID = reader.readString();
	} else {
	    return null;
	}
	for (final AbstractArenaObject allObject : this.allObjects) {
	    try {
		final AbstractArenaObject instance = allObject.getClass().getConstructor().newInstance();
		if (FormatConstants.isFormatVersionValidGeneration4(formatVersion)) {
		    o = instance.readArenaObjectG4(reader, UID, formatVersion);
		} else {
		    return null;
		}
		if (o != null) {
		    return o;
		}
	    } catch (final InstantiationException ex) {
		LaserTank.logError(ex);
	    } catch (final IllegalAccessException ex) {
		LaserTank.logError(ex);
	    } catch (final IllegalArgumentException e) {
		LaserTank.logError(e);
	    } catch (final InvocationTargetException e) {
		LaserTank.logError(e);
	    } catch (final NoSuchMethodException e) {
		LaserTank.logError(e);
	    } catch (final SecurityException e) {
		LaserTank.logError(e);
	    }
	}
	return null;
    }

    public AbstractArenaObject readArenaObjectG5(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	AbstractArenaObject o = null;
	String UID = StringLoader.loadCommon(CommonString.SPACE);
	if (FormatConstants.isFormatVersionValidGeneration5(formatVersion)) {
	    UID = reader.readString();
	} else {
	    return null;
	}
	for (final AbstractArenaObject allObject : this.allObjects) {
	    try {
		final AbstractArenaObject instance = allObject.getClass().getConstructor().newInstance();
		if (FormatConstants.isFormatVersionValidGeneration5(formatVersion)) {
		    o = instance.readArenaObjectG5(reader, UID, formatVersion);
		} else {
		    return null;
		}
		if (o != null) {
		    return o;
		}
	    } catch (final InstantiationException ex) {
		LaserTank.logError(ex);
	    } catch (final IllegalAccessException ex) {
		LaserTank.logError(ex);
	    } catch (final IllegalArgumentException e) {
		LaserTank.logError(e);
	    } catch (final InvocationTargetException e) {
		LaserTank.logError(e);
	    } catch (final NoSuchMethodException e) {
		LaserTank.logError(e);
	    } catch (final SecurityException e) {
		LaserTank.logError(e);
	    }
	}
	return null;
    }

    public AbstractArenaObject readArenaObjectG6(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	AbstractArenaObject o = null;
	String UID = StringLoader.loadCommon(CommonString.SPACE);
	if (FormatConstants.isFormatVersionValidGeneration6(formatVersion)) {
	    UID = reader.readString();
	} else {
	    return null;
	}
	for (final AbstractArenaObject allObject : this.allObjects) {
	    try {
		final AbstractArenaObject instance = allObject.getClass().getConstructor().newInstance();
		if (FormatConstants.isFormatVersionValidGeneration6(formatVersion)) {
		    o = instance.readArenaObjectG6(reader, UID, formatVersion);
		} else {
		    return null;
		}
		if (o != null) {
		    return o;
		}
	    } catch (final InstantiationException ex) {
		LaserTank.logError(ex);
	    } catch (final IllegalAccessException ex) {
		LaserTank.logError(ex);
	    } catch (final IllegalArgumentException e) {
		LaserTank.logError(e);
	    } catch (final InvocationTargetException e) {
		LaserTank.logError(e);
	    } catch (final NoSuchMethodException e) {
		LaserTank.logError(e);
	    } catch (final SecurityException e) {
		LaserTank.logError(e);
	    }
	}
	return null;
    }
}
