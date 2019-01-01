/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.utilities;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.puttysoftware.lasertank.LaserTank;
import com.puttysoftware.lasertank.improved.fileio.XMLFileReader;
import com.puttysoftware.lasertank.improved.images.BufferedImageIcon;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.objects.Acid;
import com.puttysoftware.ltremix.arena.objects.AntiBelt;
import com.puttysoftware.ltremix.arena.objects.AntiTank;
import com.puttysoftware.ltremix.arena.objects.AntiTankMover;
import com.puttysoftware.ltremix.arena.objects.AnyMover;
import com.puttysoftware.ltremix.arena.objects.Ball;
import com.puttysoftware.ltremix.arena.objects.Barrel;
import com.puttysoftware.ltremix.arena.objects.BlueDoor;
import com.puttysoftware.ltremix.arena.objects.BlueKey;
import com.puttysoftware.ltremix.arena.objects.Box;
import com.puttysoftware.ltremix.arena.objects.BoxMover;
import com.puttysoftware.ltremix.arena.objects.Bricks;
import com.puttysoftware.ltremix.arena.objects.Bridge;
import com.puttysoftware.ltremix.arena.objects.Cloak;
import com.puttysoftware.ltremix.arena.objects.Cracked;
import com.puttysoftware.ltremix.arena.objects.Crumbling;
import com.puttysoftware.ltremix.arena.objects.CrystalBlock;
import com.puttysoftware.ltremix.arena.objects.Damaged;
import com.puttysoftware.ltremix.arena.objects.Darkness;
import com.puttysoftware.ltremix.arena.objects.DeadAntiTank;
import com.puttysoftware.ltremix.arena.objects.DeepWater;
import com.puttysoftware.ltremix.arena.objects.DeeperWater;
import com.puttysoftware.ltremix.arena.objects.DeepestWater;
import com.puttysoftware.ltremix.arena.objects.Empty;
import com.puttysoftware.ltremix.arena.objects.ExplodingBarrel;
import com.puttysoftware.ltremix.arena.objects.FireAllButton;
import com.puttysoftware.ltremix.arena.objects.FireAllButtonDoor;
import com.puttysoftware.ltremix.arena.objects.FirePressureButton;
import com.puttysoftware.ltremix.arena.objects.FirePressureButtonDoor;
import com.puttysoftware.ltremix.arena.objects.FireTriggerButton;
import com.puttysoftware.ltremix.arena.objects.FireTriggerButtonDoor;
import com.puttysoftware.ltremix.arena.objects.Flag;
import com.puttysoftware.ltremix.arena.objects.FreezeSpell;
import com.puttysoftware.ltremix.arena.objects.FrostField;
import com.puttysoftware.ltremix.arena.objects.GreenDoor;
import com.puttysoftware.ltremix.arena.objects.GreenKey;
import com.puttysoftware.ltremix.arena.objects.Ground;
import com.puttysoftware.ltremix.arena.objects.HotBox;
import com.puttysoftware.ltremix.arena.objects.HotCrystalBlock;
import com.puttysoftware.ltremix.arena.objects.HotWall;
import com.puttysoftware.ltremix.arena.objects.Ice;
import com.puttysoftware.ltremix.arena.objects.IceAllButton;
import com.puttysoftware.ltremix.arena.objects.IceAllButtonDoor;
import com.puttysoftware.ltremix.arena.objects.IceBridge;
import com.puttysoftware.ltremix.arena.objects.IcePressureButton;
import com.puttysoftware.ltremix.arena.objects.IcePressureButtonDoor;
import com.puttysoftware.ltremix.arena.objects.IceTriggerButton;
import com.puttysoftware.ltremix.arena.objects.IceTriggerButtonDoor;
import com.puttysoftware.ltremix.arena.objects.IcyBox;
import com.puttysoftware.ltremix.arena.objects.IcyCrystalBlock;
import com.puttysoftware.ltremix.arena.objects.IcyWall;
import com.puttysoftware.ltremix.arena.objects.JumpBox;
import com.puttysoftware.ltremix.arena.objects.KillSpell;
import com.puttysoftware.ltremix.arena.objects.Lava;
import com.puttysoftware.ltremix.arena.objects.MagneticAllButton;
import com.puttysoftware.ltremix.arena.objects.MagneticAllButtonDoor;
import com.puttysoftware.ltremix.arena.objects.MagneticBox;
import com.puttysoftware.ltremix.arena.objects.MagneticMirror;
import com.puttysoftware.ltremix.arena.objects.MagneticPressureButton;
import com.puttysoftware.ltremix.arena.objects.MagneticPressureButtonDoor;
import com.puttysoftware.ltremix.arena.objects.MagneticTriggerButton;
import com.puttysoftware.ltremix.arena.objects.MagneticTriggerButtonDoor;
import com.puttysoftware.ltremix.arena.objects.MagneticWall;
import com.puttysoftware.ltremix.arena.objects.MetallicAllButton;
import com.puttysoftware.ltremix.arena.objects.MetallicAllButtonDoor;
import com.puttysoftware.ltremix.arena.objects.MetallicBox;
import com.puttysoftware.ltremix.arena.objects.MetallicBricks;
import com.puttysoftware.ltremix.arena.objects.MetallicMirror;
import com.puttysoftware.ltremix.arena.objects.MetallicPressureButton;
import com.puttysoftware.ltremix.arena.objects.MetallicPressureButtonDoor;
import com.puttysoftware.ltremix.arena.objects.MetallicRotaryMirror;
import com.puttysoftware.ltremix.arena.objects.MetallicTriggerButton;
import com.puttysoftware.ltremix.arena.objects.MetallicTriggerButtonDoor;
import com.puttysoftware.ltremix.arena.objects.Mirror;
import com.puttysoftware.ltremix.arena.objects.MirrorCrystalBlock;
import com.puttysoftware.ltremix.arena.objects.MirrorMover;
import com.puttysoftware.ltremix.arena.objects.PlasticAllButton;
import com.puttysoftware.ltremix.arena.objects.PlasticAllButtonDoor;
import com.puttysoftware.ltremix.arena.objects.PlasticBox;
import com.puttysoftware.ltremix.arena.objects.PlasticPressureButton;
import com.puttysoftware.ltremix.arena.objects.PlasticPressureButtonDoor;
import com.puttysoftware.ltremix.arena.objects.PlasticTriggerButton;
import com.puttysoftware.ltremix.arena.objects.PlasticTriggerButtonDoor;
import com.puttysoftware.ltremix.arena.objects.PowerBolt;
import com.puttysoftware.ltremix.arena.objects.RedDoor;
import com.puttysoftware.ltremix.arena.objects.RedKey;
import com.puttysoftware.ltremix.arena.objects.RemoteController;
import com.puttysoftware.ltremix.arena.objects.ReverseJumpBox;
import com.puttysoftware.ltremix.arena.objects.RollingBarrelHorizontal;
import com.puttysoftware.ltremix.arena.objects.RollingBarrelVertical;
import com.puttysoftware.ltremix.arena.objects.RotaryMirror;
import com.puttysoftware.ltremix.arena.objects.StairsDown;
import com.puttysoftware.ltremix.arena.objects.StairsUp;
import com.puttysoftware.ltremix.arena.objects.StickyBox;
import com.puttysoftware.ltremix.arena.objects.StoneAllButton;
import com.puttysoftware.ltremix.arena.objects.StoneAllButtonDoor;
import com.puttysoftware.ltremix.arena.objects.StonePressureButton;
import com.puttysoftware.ltremix.arena.objects.StonePressureButtonDoor;
import com.puttysoftware.ltremix.arena.objects.StoneTriggerButton;
import com.puttysoftware.ltremix.arena.objects.StoneTriggerButtonDoor;
import com.puttysoftware.ltremix.arena.objects.StrongAcid;
import com.puttysoftware.ltremix.arena.objects.StrongerAcid;
import com.puttysoftware.ltremix.arena.objects.StrongestAcid;
import com.puttysoftware.ltremix.arena.objects.Tank;
import com.puttysoftware.ltremix.arena.objects.TankMover;
import com.puttysoftware.ltremix.arena.objects.TenBlueLasers;
import com.puttysoftware.ltremix.arena.objects.TenBombs;
import com.puttysoftware.ltremix.arena.objects.TenBoosts;
import com.puttysoftware.ltremix.arena.objects.TenDisruptors;
import com.puttysoftware.ltremix.arena.objects.TenHeatBombs;
import com.puttysoftware.ltremix.arena.objects.TenIceBombs;
import com.puttysoftware.ltremix.arena.objects.TenMagnets;
import com.puttysoftware.ltremix.arena.objects.TenMissiles;
import com.puttysoftware.ltremix.arena.objects.TenStunners;
import com.puttysoftware.ltremix.arena.objects.ThinIce;
import com.puttysoftware.ltremix.arena.objects.TimeWarper;
import com.puttysoftware.ltremix.arena.objects.ToughBricks;
import com.puttysoftware.ltremix.arena.objects.Tunnel;
import com.puttysoftware.ltremix.arena.objects.UniversalAllButton;
import com.puttysoftware.ltremix.arena.objects.UniversalAllButtonDoor;
import com.puttysoftware.ltremix.arena.objects.UniversalPressureButton;
import com.puttysoftware.ltremix.arena.objects.UniversalPressureButtonDoor;
import com.puttysoftware.ltremix.arena.objects.UniversalTriggerButton;
import com.puttysoftware.ltremix.arena.objects.UniversalTriggerButtonDoor;
import com.puttysoftware.ltremix.arena.objects.Wall;
import com.puttysoftware.ltremix.arena.objects.Water;
import com.puttysoftware.ltremix.arena.objects.Weakened;
import com.puttysoftware.ltremix.arena.objects.WoodenAllButton;
import com.puttysoftware.ltremix.arena.objects.WoodenAllButtonDoor;
import com.puttysoftware.ltremix.arena.objects.WoodenBox;
import com.puttysoftware.ltremix.arena.objects.WoodenPressureButton;
import com.puttysoftware.ltremix.arena.objects.WoodenPressureButtonDoor;
import com.puttysoftware.ltremix.arena.objects.WoodenTriggerButton;
import com.puttysoftware.ltremix.arena.objects.WoodenTriggerButtonDoor;
import com.puttysoftware.ltremix.arena.objects.WoodenWall;
import com.puttysoftware.ltremix.resourcemanagers.ImageManager;
import com.puttysoftware.ltremix.stringmanagers.StringConstants;

public class ArenaObjectList {
    // Fields
    private final AbstractArenaObject[] allObjects = { new Empty(), new Ground(), new TankMover(), new Ice(),
	    new Water(), new ThinIce(), new Bridge(), new Tank(), new Flag(), new Wall(), new AntiTank(),
	    new DeadAntiTank(), new CrystalBlock(), new Bricks(), new Tunnel(), new Mirror(), new RotaryMirror(),
	    new Box(), new AntiTankMover(), new TenMissiles(), new MagneticBox(), new MagneticMirror(),
	    new MirrorCrystalBlock(), new TenStunners(), new TenBoosts(), new TenMagnets(), new MagneticWall(),
	    new FrostField(), new StairsDown(), new StairsUp(), new TenBlueLasers(), new IcyBox(), new BlueDoor(),
	    new BlueKey(), new GreenDoor(), new GreenKey(), new RedDoor(), new RedKey(), new Barrel(),
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
	    new AntiBelt(), new StickyBox(), new Acid(), new StrongAcid(), new StrongerAcid(), new StrongestAcid(),
	    new RemoteController(), new AnyMover(), new ToughBricks(), new TimeWarper() };

    public String[] getAllDescriptions() {
	final String[] allDescriptions = new String[this.allObjects.length];
	for (int x = 0; x < this.allObjects.length; x++) {
	    allDescriptions[x] = this.allObjects[x].getDescription();
	}
	return allDescriptions;
    }

    public BufferedImageIcon[] getAllEditorAppearances() {
	final BufferedImageIcon[] allEditorAppearances = new BufferedImageIcon[this.allObjects.length];
	for (int x = 0; x < allEditorAppearances.length; x++) {
	    allEditorAppearances[x] = ImageManager.getImage(this.allObjects[x], false);
	}
	return allEditorAppearances;
    }

    public void enableAllObjects() {
	for (final AbstractArenaObject allObject : this.allObjects) {
	    allObject.setEnabled(true);
	}
    }

    public AbstractArenaObject[] getAllObjectsOnLayer(final int layer) {
	for (final AbstractArenaObject allObject : this.allObjects) {
	    if (allObject.isOnLayer(layer)) {
		allObject.setEnabled(true);
	    } else {
		allObject.setEnabled(false);
	    }
	}
	return this.allObjects;
    }

    public boolean[] getObjectEnabledStatuses(final int layer) {
	final boolean[] allObjectEnabledStatuses = new boolean[this.allObjects.length];
	for (int x = 0; x < this.allObjects.length; x++) {
	    if (this.allObjects[x].isOnLayer(layer)) {
		allObjectEnabledStatuses[x] = true;
	    } else {
		allObjectEnabledStatuses[x] = false;
	    }
	}
	return allObjectEnabledStatuses;
    }

    public BufferedImageIcon[] getAllEditorAppearancesOnLayer(final int layer) {
	final BufferedImageIcon[] allEditorAppearancesOnLayer = new BufferedImageIcon[this.allObjects.length];
	for (int x = 0; x < this.allObjects.length; x++) {
	    if (this.allObjects[x].isOnLayer(layer)) {
		this.allObjects[x].setEnabled(true);
	    } else {
		this.allObjects[x].setEnabled(false);
	    }
	    allEditorAppearancesOnLayer[x] = ImageManager.getImage(this.allObjects[x], false);
	}
	return allEditorAppearancesOnLayer;
    }

    public AbstractArenaObject readArenaObjectG2(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	AbstractArenaObject o = null;
	String UID = StringConstants.COMMON_STRING_SPACE;
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
		LTRemix.getErrorLogger().logError(ex);
	    } catch (final IllegalAccessException ex) {
		LTRemix.getErrorLogger().logError(ex);
	    } catch (IllegalArgumentException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (InvocationTargetException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (NoSuchMethodException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (SecurityException e) {
		LaserTank.getErrorLogger().logError(e);
	    }
	}
	return null;
    }

    public AbstractArenaObject readArenaObjectG3(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	AbstractArenaObject o = null;
	String UID = StringConstants.COMMON_STRING_SPACE;
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
		LTRemix.getErrorLogger().logError(ex);
	    } catch (final IllegalAccessException ex) {
		LTRemix.getErrorLogger().logError(ex);
	    } catch (IllegalArgumentException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (InvocationTargetException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (NoSuchMethodException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (SecurityException e) {
		LaserTank.getErrorLogger().logError(e);
	    }
	}
	return null;
    }

    public AbstractArenaObject readArenaObjectG4(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	AbstractArenaObject o = null;
	String UID = StringConstants.COMMON_STRING_SPACE;
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
		LTRemix.getErrorLogger().logError(ex);
	    } catch (final IllegalAccessException ex) {
		LTRemix.getErrorLogger().logError(ex);
	    } catch (IllegalArgumentException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (InvocationTargetException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (NoSuchMethodException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (SecurityException e) {
		LaserTank.getErrorLogger().logError(e);
	    }
	}
	return null;
    }

    public AbstractArenaObject readArenaObjectG5(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	AbstractArenaObject o = null;
	String UID = StringConstants.COMMON_STRING_SPACE;
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
		LTRemix.getErrorLogger().logError(ex);
	    } catch (final IllegalAccessException ex) {
		LTRemix.getErrorLogger().logError(ex);
	    } catch (IllegalArgumentException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (InvocationTargetException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (NoSuchMethodException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (SecurityException e) {
		LaserTank.getErrorLogger().logError(e);
	    }
	}
	return null;
    }

    public AbstractArenaObject readArenaObjectG6(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	AbstractArenaObject o = null;
	String UID = StringConstants.COMMON_STRING_SPACE;
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
		LTRemix.getErrorLogger().logError(ex);
	    } catch (final IllegalAccessException ex) {
		LTRemix.getErrorLogger().logError(ex);
	    } catch (IllegalArgumentException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (InvocationTargetException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (NoSuchMethodException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (SecurityException e) {
		LaserTank.getErrorLogger().logError(e);
	    }
	}
	return null;
    }

    public AbstractArenaObject readArenaObjectG7(final XMLFileReader reader, final int formatVersion)
	    throws IOException {
	AbstractArenaObject o = null;
	String UID = StringConstants.COMMON_STRING_SPACE;
	if (FormatConstants.isFormatVersionValidGeneration7(formatVersion)) {
	    UID = reader.readString();
	} else {
	    return null;
	}
	for (final AbstractArenaObject allObject : this.allObjects) {
	    try {
		final AbstractArenaObject instance = allObject.getClass().getConstructor().newInstance();
		if (FormatConstants.isFormatVersionValidGeneration7(formatVersion)) {
		    o = instance.readArenaObjectG7(reader, UID, formatVersion);
		} else {
		    return null;
		}
		if (o != null) {
		    return o;
		}
	    } catch (final InstantiationException ex) {
		LTRemix.getErrorLogger().logError(ex);
	    } catch (final IllegalAccessException ex) {
		LTRemix.getErrorLogger().logError(ex);
	    } catch (IllegalArgumentException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (InvocationTargetException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (NoSuchMethodException e) {
		LaserTank.getErrorLogger().logError(e);
	    } catch (SecurityException e) {
		LaserTank.getErrorLogger().logError(e);
	    }
	}
	return null;
    }
}
