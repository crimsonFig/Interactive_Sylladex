package app.util;

/**
 * Thrown to indicate that a command's expected arguments did not match with what was given.
 * Preferably, the catch should include a correct syntax example of the given command.
 */
public class IllegalSyntaxException extends CommandRuntimeException {
    /**
     * Constructs a new exception with {@code null} as its detail message.  The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause}.
     */
    public IllegalSyntaxException() {
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     *
     * @param message
     *         the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */
    public IllegalSyntaxException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.  <p>Note that the detail message associated
     * with {@code cause} is <i>not</i> automatically incorporated in this runtime exception's detail message.
     *
     * @param message
     *         the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause
     *         the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or unknown.)
     * @since 1.8
     */
    public IllegalSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message of <tt>(cause==null ? null :
     * cause.toString())</tt> (which typically contains the class and detail message of
     * <tt>cause</tt>).  This constructor is useful for runtime exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param cause
     *         the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or unknown.)
     * @since 1.8
     */
    public IllegalSyntaxException(Throwable cause) {
        super(cause);
    }

    public static IllegalSyntaxException ofArgLength(int l) {
        return new IllegalSyntaxException(l + " is an invalid number of arguments");
    }
}
