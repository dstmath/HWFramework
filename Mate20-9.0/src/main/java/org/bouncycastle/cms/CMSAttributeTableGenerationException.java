package org.bouncycastle.cms;

public class CMSAttributeTableGenerationException extends CMSRuntimeException {
    Exception e;

    public CMSAttributeTableGenerationException(String str) {
        super(str);
    }

    public CMSAttributeTableGenerationException(String str, Exception exc) {
        super(str);
        this.e = exc;
    }

    public Throwable getCause() {
        return this.e;
    }

    public Exception getUnderlyingException() {
        return this.e;
    }
}
