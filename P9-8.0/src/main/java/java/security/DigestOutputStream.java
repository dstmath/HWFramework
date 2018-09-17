package java.security;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DigestOutputStream extends FilterOutputStream {
    protected MessageDigest digest;
    private boolean on = true;

    public DigestOutputStream(OutputStream stream, MessageDigest digest) {
        super(stream);
        setMessageDigest(digest);
    }

    public MessageDigest getMessageDigest() {
        return this.digest;
    }

    public void setMessageDigest(MessageDigest digest) {
        this.digest = digest;
    }

    public void write(int b) throws IOException {
        this.out.write(b);
        if (this.on) {
            this.digest.update((byte) b);
        }
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (b == null || off + len > b.length) {
            throw new IllegalArgumentException("wrong parameters for write");
        } else if (off < 0 || len < 0) {
            throw new IndexOutOfBoundsException("wrong index for write");
        } else {
            this.out.write(b, off, len);
            if (this.on) {
                this.digest.update(b, off, len);
            }
        }
    }

    public void on(boolean on) {
        this.on = on;
    }

    public String toString() {
        return "[Digest Output Stream] " + this.digest.toString();
    }
}
