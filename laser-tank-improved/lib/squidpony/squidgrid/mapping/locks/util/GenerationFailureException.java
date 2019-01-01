package squidpony.squidgrid.mapping.locks.util;

public class GenerationFailureException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public GenerationFailureException(final String message) {
	super(message);
    }

    public GenerationFailureException(final String message, final Throwable cause) {
	super(message, cause);
    }
}
