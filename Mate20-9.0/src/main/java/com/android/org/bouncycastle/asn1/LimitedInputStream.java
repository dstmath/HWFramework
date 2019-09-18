package com.android.org.bouncycastle.asn1;

import java.io.InputStream;

abstract class LimitedInputStream extends InputStream {
    protected final InputStream _in;
    private int _limit;

    LimitedInputStream(InputStream in, int limit) {
        this._in = in;
        this._limit = limit;
    }

    /* access modifiers changed from: package-private */
    public int getRemaining() {
        return this._limit;
    }

    /* access modifiers changed from: protected */
    public void setParentEofDetect(boolean on) {
        if (this._in instanceof IndefiniteLengthInputStream) {
            ((IndefiniteLengthInputStream) this._in).setEofOn00(on);
        }
    }
}
