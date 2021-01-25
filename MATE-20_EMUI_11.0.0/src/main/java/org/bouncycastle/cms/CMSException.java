package org.bouncycastle.cms;

public class CMSException extends Exception {
    Exception e;

    public CMSException(String str) {
        super(str);
    }

    public CMSException(String str, Exception exc) {
        super(str);
        this.e = exc;
    }

    @Override // java.lang.Throwable
    public Throwable getCause() {
        return this.e;
    }

    public Exception getUnderlyingException() {
        return this.e;
    }
}
