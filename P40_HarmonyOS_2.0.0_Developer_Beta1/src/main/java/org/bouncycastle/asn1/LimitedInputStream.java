package org.bouncycastle.asn1;

import java.io.InputStream;

/* access modifiers changed from: package-private */
public abstract class LimitedInputStream extends InputStream {
    protected final InputStream _in;
    private int _limit;

    LimitedInputStream(InputStream inputStream, int i) {
        this._in = inputStream;
        this._limit = i;
    }

    /* access modifiers changed from: package-private */
    public int getLimit() {
        return this._limit;
    }

    /* access modifiers changed from: protected */
    public void setParentEofDetect(boolean z) {
        InputStream inputStream = this._in;
        if (inputStream instanceof IndefiniteLengthInputStream) {
            ((IndefiniteLengthInputStream) inputStream).setEofOn00(z);
        }
    }
}
