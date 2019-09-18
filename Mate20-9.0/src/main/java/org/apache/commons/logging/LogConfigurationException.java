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

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public LogConfigurationException(Throwable cause2) {
        this(cause2 == null ? null : cause2.toString(), cause2);
    }

    public LogConfigurationException(String message, Throwable cause2) {
        super(message + " (Caused by " + cause2 + ")");
        this.cause = null;
        this.cause = cause2;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
