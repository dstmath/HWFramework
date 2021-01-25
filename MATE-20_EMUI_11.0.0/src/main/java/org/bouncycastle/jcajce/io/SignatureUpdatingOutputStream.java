package org.bouncycastle.jcajce.io;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Signature;
import java.security.SignatureException;

/* access modifiers changed from: package-private */
public class SignatureUpdatingOutputStream extends OutputStream {
    private Signature sig;

    SignatureUpdatingOutputStream(Signature signature) {
        this.sig = signature;
    }

    @Override // java.io.OutputStream
    public void write(int i) throws IOException {
        try {
            this.sig.update((byte) i);
        } catch (SignatureException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override // java.io.OutputStream
    public void write(byte[] bArr) throws IOException {
        try {
            this.sig.update(bArr);
        } catch (SignatureException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override // java.io.OutputStream
    public void write(byte[] bArr, int i, int i2) throws IOException {
        try {
            this.sig.update(bArr, i, i2);
        } catch (SignatureException e) {
            throw new IOException(e.getMessage());
        }
    }
}
