package org.bouncycastle.pqc.crypto.lms;

public class LMSException extends Exception {
    public LMSException() {
    }

    public LMSException(String str) {
        super(str);
    }

    public LMSException(String str, Throwable th) {
        super(str, th);
    }

    public LMSException(String str, Throwable th, boolean z, boolean z2) {
        super(str, th, z, z2);
    }

    public LMSException(Throwable th) {
        super(th);
    }
}
