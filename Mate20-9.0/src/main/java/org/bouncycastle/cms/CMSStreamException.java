package org.bouncycastle.cms;

import java.io.IOException;

public class CMSStreamException extends IOException {
    private final Throwable underlying;

    CMSStreamException(String str) {
        super(str);
        this.underlying = null;
    }

    CMSStreamException(String str, Throwable th) {
        super(str);
        this.underlying = th;
    }

    public Throwable getCause() {
        return this.underlying;
    }
}
