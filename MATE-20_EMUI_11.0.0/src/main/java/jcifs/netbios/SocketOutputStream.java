package jcifs.netbios;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class SocketOutputStream extends FilterOutputStream {
    SocketOutputStream(OutputStream out) {
        super(out);
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (len > 65535) {
            throw new IOException("write too large: " + len);
        } else if (off < 4) {
            throw new IOException("NetBIOS socket output buffer requires 4 bytes available before off");
        } else {
            int off2 = off - 4;
            b[off2 + 0] = 0;
            b[off2 + 1] = 0;
            b[off2 + 2] = (byte) ((len >> 8) & 255);
            b[off2 + 3] = (byte) (len & 255);
            this.out.write(b, off2, len + 4);
        }
    }
}
