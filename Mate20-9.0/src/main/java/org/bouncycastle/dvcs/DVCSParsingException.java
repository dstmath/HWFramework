package org.bouncycastle.dvcs;

public class DVCSParsingException extends DVCSException {
    private static final long serialVersionUID = -7895880961377691266L;

    public DVCSParsingException(String str) {
        super(str);
    }

    public DVCSParsingException(String str, Throwable th) {
        super(str, th);
    }
}
