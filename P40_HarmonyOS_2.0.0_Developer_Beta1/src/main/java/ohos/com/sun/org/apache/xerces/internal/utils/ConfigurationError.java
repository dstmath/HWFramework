package ohos.com.sun.org.apache.xerces.internal.utils;

public final class ConfigurationError extends Error {
    private Exception exception;

    ConfigurationError(String str, Exception exc) {
        super(str);
        this.exception = exc;
    }

    public Exception getException() {
        return this.exception;
    }
}
