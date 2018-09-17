package java.security;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DigestInputStream extends FilterInputStream {
    protected MessageDigest digest;
    private boolean on = true;

    public DigestInputStream(InputStream stream, MessageDigest digest) {
        super(stream);
        setMessageDigest(digest);
    }

    public MessageDigest getMessageDigest() {
        return this.digest;
    }

    public void setMessageDigest(MessageDigest digest) {
        this.digest = digest;
    }

    public int read() throws IOException {
        int ch = this.in.read();
        if (this.on && ch != -1) {
            this.digest.update((byte) ch);
        }
        return ch;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int result = this.in.read(b, off, len);
        if (this.on && result != -1) {
            this.digest.update(b, off, result);
        }
        return result;
    }

    public void on(boolean on) {
        this.on = on;
    }

    public String toString() {
        return "[Digest Input Stream] " + this.digest.toString();
    }
}
