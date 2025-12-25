package com.puttysoftware.lasertank.arena;

public class HistoryStatus {
    private static final int MAX_WHAT = 10;
    public static final int WAS_LASER = 0;
    public static final int WAS_MISSILE = 1;
    public static final int WAS_STUNNER = 2;
    public static final int WAS_BOOST = 3;
    public static final int WAS_MAGNET = 4;
    public static final int WAS_BLUE_LASER = 5;
    public static final int WAS_DISRUPTOR = 6;
    public static final int WAS_BOMB = 7;
    public static final int WAS_HEAT_BOMB = 8;
    public static final int WAS_ICE_BOMB = 9;
    // Fields
    private boolean[] wasWhat;

    public HistoryStatus(final boolean... entries) {
	if (entries == null || entries.length == 0) {
	    this.wasWhat = new boolean[HistoryStatus.MAX_WHAT];
	} else {
	    this.wasWhat = entries;
	}
    }

    public void setWasSomething(final int index, final boolean value) {
	this.wasWhat[index] = value;
    }

    public boolean wasSomething(final int index) {
	return this.wasWhat[index];
    }
}
