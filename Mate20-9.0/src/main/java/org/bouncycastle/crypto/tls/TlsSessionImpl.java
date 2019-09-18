package org.bouncycastle.crypto.tls;

import org.bouncycastle.util.Arrays;

class TlsSessionImpl implements TlsSession {
    final byte[] sessionID;
    SessionParameters sessionParameters;

    TlsSessionImpl(byte[] bArr, SessionParameters sessionParameters2) {
        if (bArr == null) {
            throw new IllegalArgumentException("'sessionID' cannot be null");
        } else if (bArr.length < 1 || bArr.length > 32) {
            throw new IllegalArgumentException("'sessionID' must have length between 1 and 32 bytes, inclusive");
        } else {
            this.sessionID = Arrays.clone(bArr);
            this.sessionParameters = sessionParameters2;
        }
    }

    public synchronized SessionParameters exportSessionParameters() {
        return this.sessionParameters == null ? null : this.sessionParameters.copy();
    }

    public synchronized byte[] getSessionID() {
        return this.sessionID;
    }

    public synchronized void invalidate() {
        if (this.sessionParameters != null) {
            this.sessionParameters.clear();
            this.sessionParameters = null;
        }
    }

    public synchronized boolean isResumable() {
        return this.sessionParameters != null;
    }
}
