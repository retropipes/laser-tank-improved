package com.puttysoftware.shell.loaders;

public final class LoaderException extends RuntimeException {
    private static final long serialVersionUID = 3906665752322984153L;

    public LoaderException() {
	super();
    }

    public LoaderException(final String message) {
	super(message);
    }

    public LoaderException(final String message, final Throwable cause) {
	super(message, cause);
    }

    public LoaderException(final String message, final Throwable cause, final boolean enableSuppression,
	    final boolean writableStackTrace) {
	super(message, cause, enableSuppression, writableStackTrace);
    }

    public LoaderException(final Throwable cause) {
	super(cause);
    }
}
