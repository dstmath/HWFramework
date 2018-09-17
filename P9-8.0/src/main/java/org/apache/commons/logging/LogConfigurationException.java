package org.apache.commons.logging;

@Deprecated
public class LogConfigurationException extends RuntimeException {
    protected Throwable cause;

    public LogConfigurationException() {
        this.cause = null;
    }

    public LogConfigurationException(String message) {
        super(message);
        this.cause = null;
    }

    public LogConfigurationException(Throwable cause) {
        String str = null;
        if (cause != null) {
            str = cause.toString();
        }
        this(str, cause);
    }

    public LogConfigurationException(String message, Throwable cause) {
        super(message + " (Caused by " + cause + ")");
        this.cause = null;
        this.cause = cause;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
