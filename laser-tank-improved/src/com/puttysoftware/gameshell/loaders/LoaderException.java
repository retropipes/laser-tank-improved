package com.puttysoftware.gameshell.loaders;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class LoaderException extends RuntimeException {
    public LoaderException() {
	super();
    }

    public LoaderException(String message) {
	super(message);
    }

    public LoaderException(Throwable cause) {
	super(cause);
    }

    public LoaderException(String message, Throwable cause) {
	super(message, cause);
    }

    public LoaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
	super(message, cause, enableSuppression, writableStackTrace);
    }

    @SuppressWarnings("static-method")
    private void writeObject(ObjectOutputStream out) throws IOException {
	throw new NotSerializableException();
    }

    @SuppressWarnings("static-method")
    private void readObject(ObjectInputStream in) throws IOException {
	throw new NotSerializableException();
    }
}
