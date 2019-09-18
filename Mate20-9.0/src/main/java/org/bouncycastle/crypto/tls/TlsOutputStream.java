package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.OutputStream;

class TlsOutputStream extends OutputStream {
    private byte[] buf = new byte[1];
    private TlsProtocol handler;

    TlsOutputStream(TlsProtocol tlsProtocol) {
        this.handler = tlsProtocol;
    }

    public void close() throws IOException {
        this.handler.close();
    }

    public void flush() throws IOException {
        this.handler.flush();
    }

    public void write(int i) throws IOException {
        this.buf[0] = (byte) i;
        write(this.buf, 0, 1);
    }

    public void write(byte[] bArr, int i, int i2) throws IOException {
        this.handler.writeData(bArr, i, i2);
    }
}
