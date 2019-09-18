package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.InputStream;

class TlsInputStream extends InputStream {
    private byte[] buf = new byte[1];
    private TlsProtocol handler = null;

    TlsInputStream(TlsProtocol tlsProtocol) {
        this.handler = tlsProtocol;
    }

    public int available() throws IOException {
        return this.handler.applicationDataAvailable();
    }

    public void close() throws IOException {
        this.handler.close();
    }

    public int read() throws IOException {
        if (read(this.buf) < 0) {
            return -1;
        }
        return this.buf[0] & 255;
    }

    public int read(byte[] bArr, int i, int i2) throws IOException {
        return this.handler.readApplicationData(bArr, i, i2);
    }
}
