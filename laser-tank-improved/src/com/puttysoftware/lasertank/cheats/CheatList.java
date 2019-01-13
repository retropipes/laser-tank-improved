package com.puttysoftware.lasertank.cheats;

import java.util.ArrayList;
import java.util.Iterator;

public class CheatList extends ArrayList<Cheat> {
    private static final long serialVersionUID = 2216922516012569257L;

    public int indexOf(final String code) {
	if (code == null) {
	    return -1;
	}
	Iterator<Cheat> it = this.iterator();
	int counter = -1;
	while (it.hasNext()) {
	    Cheat c = it.next();
	    counter += 1;
	    if (c.getCode().equals(code)) {
		return counter;
	    }
	}
	return -1;
    }
}
