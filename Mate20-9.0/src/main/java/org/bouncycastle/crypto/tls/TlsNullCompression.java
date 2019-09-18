package org.bouncycastle.crypto.tls;

import java.io.OutputStream;

public class TlsNullCompression implements TlsCompression {
    public OutputStream compress(OutputStream outputStream) {
        return outputStream;
    }

    public OutputStream decompress(OutputStream outputStream) {
        return outputStream;
    }
}
