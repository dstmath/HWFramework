package com.android.org.bouncycastle.crypto.io;

import com.android.org.bouncycastle.crypto.Digest;
import java.io.IOException;
import java.io.OutputStream;

public class DigestOutputStream extends OutputStream {
    protected Digest digest;

    public DigestOutputStream(Digest Digest) {
        this.digest = Digest;
    }

    public void write(int b) throws IOException {
        this.digest.update((byte) b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.digest.update(b, off, len);
    }

    public byte[] getDigest() {
        byte[] res = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(res, 0);
        return res;
    }
}
