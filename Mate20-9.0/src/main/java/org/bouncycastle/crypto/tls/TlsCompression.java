package org.bouncycastle.crypto.tls;

import java.io.OutputStream;

public interface TlsCompression {
    OutputStream compress(OutputStream outputStream);

    OutputStream decompress(OutputStream outputStream);
}
