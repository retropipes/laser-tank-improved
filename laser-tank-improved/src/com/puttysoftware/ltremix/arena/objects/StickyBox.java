/*  LTRemix: An Arena-Solving Game
 Copyright (C) 2013-2014 Eric Ahnell

 Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package com.puttysoftware.ltremix.arena.objects;

import com.puttysoftware.ltremix.Application;
import com.puttysoftware.ltremix.LTRemix;
import com.puttysoftware.ltremix.arena.AbstractArena;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractArenaObject;
import com.puttysoftware.ltremix.arena.abstractobjects.AbstractMovableObject;
import com.puttysoftware.ltremix.resourcemanagers.SoundConstants;
import com.puttysoftware.ltremix.resourcemanagers.SoundManager;
import com.puttysoftware.ltremix.utilities.MaterialConstants;
import com.puttysoftware.ltremix.utilities.TypeConstants;

public class StickyBox extends AbstractMovableObject {
    // Constructors
    public StickyBox() {
	super(true);
	this.type.set(TypeConstants.TYPE_BOX);
	this.setMaterial(MaterialConstants.MATERIAL_PLASTIC);
    }

    @Override
    public void playSoundHook() {
	SoundManager.playSound(SoundConstants.SOUND_PUSH_BOX);
    }

    @Override
    public final int getStringBaseID() {
	return 145;
    }

    @Override
    public AbstractArenaObject changesToOnExposure(final int materialID) {
	switch (materialID) {
	case MaterialConstants.MATERIAL_ICE:
	    final IcyBox ib = new IcyBox();
	    ib.setPreviousState(this);
	    return ib;
	case MaterialConstants.MATERIAL_FIRE:
	    return new HotBox();
	default:
	    return this;
	}
    }

    @Override
    protected boolean preMoveObject(final int locX, final int locY, final int locZ, final int dirX, final int dirY,
	    final int laserType, final int forceUnits) {
	this.didPreCheck = true;
	final Application app = LTRemix.getApplication();
	final AbstractArena a = app.getArenaManager().getArena();
	AbstractArenaObject obj2;
	try {
	    obj2 = a.getCell(locX, locY - 1, locZ, this.getPrimaryLayer());
	} catch (final ArrayIndexOutOfBoundsException aioobe) {
	    obj2 = new Wall();
	}
	AbstractArenaObject obj4;
	try {
	    obj4 = a.getCell(locX - 1, locY, locZ, this.getPrimaryLayer());
	} catch (final ArrayIndexOutOfBoundsException aioobe) {
	    obj4 = new Wall();
	}
	AbstractArenaObject obj6;
	try {
	    obj6 = a.getCell(locX + 1, locY, locZ, this.getPrimaryLayer());
	} catch (final ArrayIndexOutOfBoundsException aioobe) {
	    obj6 = new Wall();
	}
	AbstractArenaObject obj8;
	try {
	    obj8 = a.getCell(locX, locY + 1, locZ, this.getPrimaryLayer());
	} catch (final ArrayIndexOutOfBoundsException aioobe) {
	    obj8 = new Wall();
	}
	boolean proceed = true;
	if (dirX == 0 && dirY == -1) {
	    if (obj2 instanceof StickyBox && !((StickyBox) obj2).didPreCheck) {
		((StickyBox) obj2).didPreCheck = true;
		proceed = proceed && ((StickyBox) obj2).preMoveObject(locX, locY - 1, locZ, dirX, dirY, laserType,
			forceUnits - obj2.getMinimumReactionForce());
		if (proceed) {
		    app.getGameManager().updatePushedPosition(locX, locY - 1, locX + dirX, locY - 1 + dirY,
			    (StickyBox) obj2);
		    ((StickyBox) obj2).didMove = true;
		}
	    } else if (!(obj2 instanceof StickyBox)) {
		proceed = proceed && super.preMoveObject(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	    }
	    if (obj4 instanceof StickyBox && !((StickyBox) obj4).didPreCheck) {
		((StickyBox) obj4).didPreCheck = true;
		proceed = proceed && ((StickyBox) obj4).preMoveObject(locX - 1, locY, locZ, dirX, dirY, laserType,
			forceUnits - obj4.getMinimumReactionForce());
		if (proceed) {
		    app.getGameManager().updatePushedPosition(locX - 1, locY, locX - 1 + dirX, locY + dirY,
			    (StickyBox) obj4);
		    ((StickyBox) obj4).didMove = true;
		}
	    }
	    if (obj6 instanceof StickyBox && !((StickyBox) obj6).didPreCheck) {
		((StickyBox) obj6).didPreCheck = true;
		proceed = proceed && ((StickyBox) obj6).preMoveObject(locX + 1, locY, locZ, dirX, dirY, laserType,
			forceUnits - obj6.getMinimumReactionForce());
		if (proceed) {
		    app.getGameManager().updatePushedPosition(locX + 1, locY, locX + 1 + dirX, locY + dirY,
			    (StickyBox) obj6);
		    ((StickyBox) obj6).didMove = true;
		}
	    }
	} else if (dirX == -1 && dirY == 0) {
	    if (obj4 instanceof StickyBox && !((StickyBox) obj4).didPreCheck) {
		((StickyBox) obj4).didPreCheck = true;
		proceed = proceed && ((StickyBox) obj4).preMoveObject(locX - 1, locY, locZ, dirX, dirY, laserType,
			forceUnits - obj4.getMinimumReactionForce());
		if (proceed) {
		    app.getGameManager().updatePushedPosition(locX - 1, locY, locX - 1 + dirX, locY + dirY,
			    (StickyBox) obj4);
		    ((StickyBox) obj4).didMove = true;
		}
	    } else if (!(obj4 instanceof StickyBox)) {
		proceed = proceed && super.preMoveObject(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	    }
	    if (obj2 instanceof StickyBox && !((StickyBox) obj2).didPreCheck) {
		((StickyBox) obj2).didPreCheck = true;
		proceed = proceed && ((StickyBox) obj2).preMoveObject(locX, locY - 1, locZ, dirX, dirY, laserType,
			forceUnits - obj2.getMinimumReactionForce());
		if (proceed) {
		    app.getGameManager().updatePushedPosition(locX, locY - 1, locX + dirX, locY - 1 + dirY,
			    (StickyBox) obj2);
		    ((StickyBox) obj2).didMove = true;
		}
	    }
	    if (obj8 instanceof StickyBox && !((StickyBox) obj8).didPreCheck) {
		((StickyBox) obj8).didPreCheck = true;
		proceed = proceed && ((StickyBox) obj8).preMoveObject(locX, locY + 1, locZ, dirX, dirY, laserType,
			forceUnits - obj8.getMinimumReactionForce());
		if (proceed) {
		    app.getGameManager().updatePushedPosition(locX, locY + 1, locX + dirX, locY + 1 + dirY,
			    (StickyBox) obj8);
		    ((StickyBox) obj8).didMove = true;
		}
	    }
	} else if (dirX == 1 && dirY == 0) {
	    if (obj6 instanceof StickyBox && !((StickyBox) obj6).didPreCheck) {
		((StickyBox) obj6).didPreCheck = true;
		proceed = proceed && ((StickyBox) obj6).preMoveObject(locX + 1, locY, locZ, dirX, dirY, laserType,
			forceUnits - obj6.getMinimumReactionForce());
		if (proceed) {
		    app.getGameManager().updatePushedPosition(locX + 1, locY, locX + 1 + dirX, locY + dirY,
			    (StickyBox) obj6);
		    ((StickyBox) obj6).didMove = true;
		}
	    } else if (!(obj6 instanceof StickyBox)) {
		proceed = proceed && super.preMoveObject(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	    }
	    if (obj2 instanceof StickyBox && !((StickyBox) obj2).didPreCheck) {
		((StickyBox) obj2).didPreCheck = true;
		proceed = proceed && ((StickyBox) obj2).preMoveObject(locX, locY - 1, locZ, dirX, dirY, laserType,
			forceUnits - obj2.getMinimumReactionForce());
		if (proceed) {
		    app.getGameManager().updatePushedPosition(locX, locY - 1, locX + dirX, locY - 1 + dirY,
			    (StickyBox) obj2);
		    ((StickyBox) obj2).didMove = true;
		}
	    }
	    if (obj8 instanceof StickyBox && !((StickyBox) obj8).didPreCheck) {
		((StickyBox) obj8).didPreCheck = true;
		proceed = proceed && ((StickyBox) obj8).preMoveObject(locX, locY + 1, locZ, dirX, dirY, laserType,
			forceUnits - obj8.getMinimumReactionForce());
		if (proceed) {
		    app.getGameManager().updatePushedPosition(locX, locY + 1, locX + dirX, locY + 1 + dirY,
			    (StickyBox) obj8);
		    ((StickyBox) obj8).didMove = true;
		}
	    }
	} else if (dirX == 0 && dirY == 1) {
	    if (obj8 instanceof StickyBox && !((StickyBox) obj8).didPreCheck) {
		((StickyBox) obj8).didPreCheck = true;
		proceed = proceed && ((StickyBox) obj8).preMoveObject(locX, locY + 1, locZ, dirX, dirY, laserType,
			forceUnits - obj8.getMinimumReactionForce());
		if (proceed) {
		    app.getGameManager().updatePushedPosition(locX, locY + 1, locX + dirX, locY + 1 + dirY,
			    (StickyBox) obj8);
		    ((StickyBox) obj8).didMove = true;
		}
	    } else if (!(obj8 instanceof StickyBox)) {
		proceed = proceed && super.preMoveObject(locX, locY, locZ, dirX, dirY, laserType, forceUnits);
	    }
	    if (obj4 instanceof StickyBox && !((StickyBox) obj4).didPreCheck) {
		((StickyBox) obj4).didPreCheck = true;
		proceed = proceed && ((StickyBox) obj4).preMoveObject(locX - 1, locY, locZ, dirX, dirY, laserType,
			forceUnits - obj4.getMinimumReactionForce());
		if (proceed) {
		    app.getGameManager().updatePushedPosition(locX - 1, locY, locX - 1 + dirX, locY + dirY,
			    (StickyBox) obj4);
		    ((StickyBox) obj4).didMove = true;
		}
	    }
	    if (obj6 instanceof StickyBox && !((StickyBox) obj6).didPreCheck) {
		((StickyBox) obj6).didPreCheck = true;
		proceed = proceed && ((StickyBox) obj6).preMoveObject(locX + 1, locY, locZ, dirX, dirY, laserType,
			forceUnits - obj6.getMinimumReactionForce());
		if (proceed) {
		    app.getGameManager().updatePushedPosition(locX + 1, locY, locX + 1 + dirX, locY + dirY,
			    (StickyBox) obj6);
		    ((StickyBox) obj6).didMove = true;
		}
	    }
	}
	return proceed;
    }
}