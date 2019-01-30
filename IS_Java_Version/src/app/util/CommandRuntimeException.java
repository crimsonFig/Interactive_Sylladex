package app.util;

import java.io.PrintStream;

/**
 * Thrown to indicate that an executing command has failed and could not complete in the desired manner.
 * Modus integrity may be undetermined when this exception is thrown.
 */
public class CommandRuntimeException extends RuntimeException {
    /**
     * Constructs a new exception with {@code null} as its detail message.  The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause}.
     */
    public CommandRuntimeException() {
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message
     *         the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */
    public CommandRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.  <p>Note that the detail message
     * associated with {@code cause} is <i>not</i> automatically incorporated in this runtime exception's detail message.
     *
     * @param message
     *         the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause
     *         the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or unknown.)
     * @since 1.8
     */
    public CommandRuntimeException(String message, Throwable cause) {
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
    public CommandRuntimeException(Throwable cause) {
        super(cause);
    }

    public void printStackTraceLess(Integer depth) {
        if (depth == null || depth < 1) throw new IllegalArgumentException("Depth cannot be less than 1");

        PrintStream s = System.err;
        synchronized (System.err) {
            // Print our stack trace
            s.println(this);
            StackTraceElement[] trace = this.getStackTrace();
            for (int i = 0; i < depth && i < trace.length; i++)
                s.println("\tat " + trace[i]);

            // Print cause, if any
            Throwable ourCause = getCause();
            if (ourCause != null) {
                s.println("Caused by: "+ourCause);
                StackTraceElement[] ourCauseTrace = ourCause.getStackTrace();
                for (int i = 0; i < depth && i < ourCauseTrace.length; i++)
                    s.println("\tat " + ourCauseTrace[i]);
            }
        }
    }
}
