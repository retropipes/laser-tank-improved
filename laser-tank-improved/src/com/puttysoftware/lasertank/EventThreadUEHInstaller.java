package com.puttysoftware.lasertank;

class EventThreadUEHInstaller implements Runnable {
    private EventThreadUEH handler;

    EventThreadUEHInstaller(final EventThreadUEH etueh) {
	super();
	this.handler = etueh;
    }

    @Override
    public void run() {
	Thread.currentThread().setUncaughtExceptionHandler(this.handler);
    }
}
