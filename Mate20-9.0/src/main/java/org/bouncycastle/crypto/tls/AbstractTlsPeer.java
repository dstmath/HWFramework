package org.bouncycastle.crypto.tls;

import java.io.IOException;

public abstract class AbstractTlsPeer implements TlsPeer {
    public void notifyAlertRaised(short s, short s2, String str, Throwable th) {
    }

    public void notifyAlertReceived(short s, short s2) {
    }

    public void notifyHandshakeComplete() throws IOException {
    }

    public void notifySecureRenegotiation(boolean z) throws IOException {
        if (!z) {
            throw new TlsFatalAlert(40);
        }
    }

    public boolean shouldUseGMTUnixTime() {
        return false;
    }
}
