package com.puttysoftware.lasertank;

class EventThreadUEH implements Thread.UncaughtExceptionHandler {
    EventThreadUEH() {
	super();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
	LaserTank.logNonFatalError(e);
    }
}
