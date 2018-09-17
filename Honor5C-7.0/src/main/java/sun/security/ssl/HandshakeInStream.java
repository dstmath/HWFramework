package sun.security.ssl;

import java.io.IOException;
import java.io.InputStream;
import javax.net.ssl.SSLException;

public class HandshakeInStream extends InputStream {
    InputRecord r;

    HandshakeInStream(HandshakeHash handshakeHash) {
        this.r = new InputRecord();
        this.r.setHandshakeHash(handshakeHash);
    }

    public int available() {
        return this.r.available();
    }

    public int read() throws IOException {
        int n = this.r.read();
        if (n != -1) {
            return n;
        }
        throw new SSLException("Unexpected end of handshake data");
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int n = this.r.read(b, off, len);
        if (n == len) {
            return n;
        }
        throw new SSLException("Unexpected end of handshake data");
    }

    public long skip(long n) throws IOException {
        return this.r.skip(n);
    }

    public void mark(int readlimit) {
        this.r.mark(readlimit);
    }

    public void reset() {
        this.r.reset();
    }

    public boolean markSupported() {
        return true;
    }

    void incomingRecord(InputRecord in) throws IOException {
        this.r.queueHandshake(in);
    }

    void digestNow() {
        this.r.doHashes();
    }

    void ignore(int n) {
        this.r.ignore(n);
    }

    int getInt8() throws IOException {
        return read();
    }

    int getInt16() throws IOException {
        return (getInt8() << 8) | getInt8();
    }

    int getInt24() throws IOException {
        return ((getInt8() << 16) | (getInt8() << 8)) | getInt8();
    }

    int getInt32() throws IOException {
        return (((getInt8() << 24) | (getInt8() << 16)) | (getInt8() << 8)) | getInt8();
    }

    byte[] getBytes8() throws IOException {
        int len = getInt8();
        verifyLength(len);
        byte[] b = new byte[len];
        read(b, 0, len);
        return b;
    }

    public byte[] getBytes16() throws IOException {
        int len = getInt16();
        verifyLength(len);
        byte[] b = new byte[len];
        read(b, 0, len);
        return b;
    }

    byte[] getBytes24() throws IOException {
        int len = getInt24();
        verifyLength(len);
        byte[] b = new byte[len];
        read(b, 0, len);
        return b;
    }

    private void verifyLength(int len) throws SSLException {
        if (len > available()) {
            throw new SSLException("Not enough data to fill declared vector size");
        }
    }
}
