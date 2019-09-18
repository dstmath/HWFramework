package org.bouncycastle.crypto.tls;

import java.io.IOException;

public interface TlsCipher {
    byte[] decodeCiphertext(long j, short s, byte[] bArr, int i, int i2) throws IOException;

    byte[] encodePlaintext(long j, short s, byte[] bArr, int i, int i2) throws IOException;

    int getPlaintextLimit(int i);
}
