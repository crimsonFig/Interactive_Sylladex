package app.util;

import java.io.PrintStream;

/**
 * Thrown to indicate that a runtime exception occurred in a Modus method that prevented a normal exit. Integrity of
 * the Modus object may be undetermined when this needs to be thrown.
 */
public class ModusRuntimeException extends RuntimeException {
    /**
     * Constructs a new runtime exception with {@code null} as its detail message.  The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause}.
     */
    public ModusRuntimeException() {
    }

    /**
     * Constructs a new modus runtime exception with the specified detail message.
     *
     * @param message
     *         the detail message.
     */
    public ModusRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs a new modus runtime exception with the specified detail message and cause.
     *
     * @param message
     *         the detail message.
     * @param cause
     *         the thrown cause.
     */
    public ModusRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new modus runtime exception with the specified cause and a detail message of <tt>(cause==null ? null :
     * cause.toString())</tt> (which typically contains the class and detail message of
     * <tt>cause</tt>).  This constructor is useful for runtime exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param cause
     *         the thrown cause.
     */
    public ModusRuntimeException(Throwable cause) {
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
