package org.bouncycastle.crypto.tls;

public interface TlsSRPIdentityManager {
    TlsSRPLoginParameters getLoginParameters(byte[] bArr);
}
