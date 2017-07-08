package sun.security.ssl;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* compiled from: HandshakeHash */
final class CloneableDigest extends MessageDigest implements Cloneable {
    private final MessageDigest[] digests;

    private CloneableDigest(MessageDigest digest, int n, String algorithm) throws NoSuchAlgorithmException {
        super(algorithm);
        this.digests = new MessageDigest[n];
        this.digests[0] = digest;
        for (int i = 1; i < n; i++) {
            this.digests[i] = JsseJce.getMessageDigest(algorithm);
        }
    }

    static MessageDigest getDigest(String algorithm, int n) throws NoSuchAlgorithmException {
        MessageDigest digest = JsseJce.getMessageDigest(algorithm);
        try {
            digest.clone();
            return digest;
        } catch (CloneNotSupportedException e) {
            return new CloneableDigest(digest, n, algorithm);
        }
    }

    private void checkState() {
    }

    protected int engineGetDigestLength() {
        checkState();
        return this.digests[0].getDigestLength();
    }

    protected void engineUpdate(byte b) {
        checkState();
        int i = 0;
        while (i < this.digests.length && this.digests[i] != null) {
            this.digests[i].update(b);
            i++;
        }
    }

    protected void engineUpdate(byte[] b, int offset, int len) {
        checkState();
        int i = 0;
        while (i < this.digests.length && this.digests[i] != null) {
            this.digests[i].update(b, offset, len);
            i++;
        }
    }

    protected byte[] engineDigest() {
        checkState();
        byte[] digest = this.digests[0].digest();
        digestReset();
        return digest;
    }

    protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
        checkState();
        int n = this.digests[0].digest(buf, offset, len);
        digestReset();
        return n;
    }

    private void digestReset() {
        int i = 1;
        while (i < this.digests.length && this.digests[i] != null) {
            this.digests[i].reset();
            i++;
        }
    }

    protected void engineReset() {
        checkState();
        int i = 0;
        while (i < this.digests.length && this.digests[i] != null) {
            this.digests[i].reset();
            i++;
        }
    }

    public Object clone() {
        checkState();
        for (int i = this.digests.length - 1; i >= 0; i--) {
            if (this.digests[i] != null) {
                MessageDigest digest = this.digests[i];
                this.digests[i] = null;
                return digest;
            }
        }
        throw new InternalError();
    }
}
