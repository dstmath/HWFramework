package org.bouncycastle.crypto.tls;

public interface TlsPSKIdentity {
    byte[] getPSK();

    byte[] getPSKIdentity();

    void notifyIdentityHint(byte[] bArr);

    void skipIdentityHint();
}
