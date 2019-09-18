package org.bouncycastle.crypto.tls;

import java.io.IOException;

public interface TlsCipherFactory {
    TlsCipher createCipher(TlsContext tlsContext, int i, int i2) throws IOException;
}
