package java.security;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DigestOutputStream extends FilterOutputStream {
    protected MessageDigest digest;
    private boolean on;

    public DigestOutputStream(OutputStream stream, MessageDigest digest) {
        super(stream);
        this.on = true;
        setMessageDigest(digest);
    }

    public MessageDigest getMessageDigest() {
        return this.digest;
    }

    public void setMessageDigest(MessageDigest digest) {
        this.digest = digest;
    }

    public void write(int b) throws IOException {
        if (this.on) {
            this.digest.update((byte) b);
        }
        this.out.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (this.on) {
            this.digest.update(b, off, len);
        }
        this.out.write(b, off, len);
    }

    public void on(boolean on) {
        this.on = on;
    }

    public String toString() {
        return "[Digest Output Stream] " + this.digest.toString();
    }
}
