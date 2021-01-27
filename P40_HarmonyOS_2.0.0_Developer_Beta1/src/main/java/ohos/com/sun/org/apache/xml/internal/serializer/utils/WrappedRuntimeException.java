package ohos.com.sun.org.apache.xml.internal.serializer.utils;

public final class WrappedRuntimeException extends RuntimeException {
    static final long serialVersionUID = 7140414456714658073L;
    private Exception m_exception;

    public WrappedRuntimeException(Exception exc) {
        super(exc.getMessage());
        this.m_exception = exc;
    }

    public WrappedRuntimeException(String str, Exception exc) {
        super(str);
        this.m_exception = exc;
    }

    public Exception getException() {
        return this.m_exception;
    }
}
