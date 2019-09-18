package org.bouncycastle.crypto.tls;

import java.io.IOException;

public class AbstractTlsCipherFactory implements TlsCipherFactory {
    public TlsCipher createCipher(TlsContext tlsContext, int i, int i2) throws IOException {
        throw new TlsFatalAlert(80);
    }
}
