package app.util;

public class FatalModusException extends ModusRuntimeException {
    /**
     * Constructs a new runtime exception with {@code null} as its detail message.  The cause is not initialized, and
     * may subsequently be initialized by a call to {@link #initCause}.
     */
    public FatalModusException() {
    }

    /**
     * Constructs a new modus runtime exception with the specified detail message.
     *
     * @param message
     *         the detail message.
     */
    public FatalModusException(String message) {
        super(message);
    }

    /**
     * Constructs a new modus runtime exception with the specified detail message and cause.
     *
     * @param message
     *         the detail message.
     * @param cause
     *         the thrown cause
     */
    public FatalModusException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new modus runtime exception with the specified cause and a detail message of <tt>(cause==null ? null
     * : cause.toString())</tt> (which typically contains the class and detail message of
     * <tt>cause</tt>).  This constructor is useful for runtime exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param cause
     *         the thrown cause.
     */
    public FatalModusException(Throwable cause) {
        super(cause);
    }
}
