package com.puttysoftware.lasertank;

class EventThreadUEH implements Thread.UncaughtExceptionHandler {
    EventThreadUEH() {
	super();
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
	LaserTank.logNonFatalError(e);
    }
}
