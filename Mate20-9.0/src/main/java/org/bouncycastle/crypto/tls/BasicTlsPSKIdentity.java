package org.bouncycastle.crypto.tls;

import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

public class BasicTlsPSKIdentity implements TlsPSKIdentity {
    protected byte[] identity;
    protected byte[] psk;

    public BasicTlsPSKIdentity(String str, byte[] bArr) {
        this.identity = Strings.toUTF8ByteArray(str);
        this.psk = Arrays.clone(bArr);
    }

    public BasicTlsPSKIdentity(byte[] bArr, byte[] bArr2) {
        this.identity = Arrays.clone(bArr);
        this.psk = Arrays.clone(bArr2);
    }

    public byte[] getPSK() {
        return this.psk;
    }

    public byte[] getPSKIdentity() {
        return this.identity;
    }

    public void notifyIdentityHint(byte[] bArr) {
    }

    public void skipIdentityHint() {
    }
}
