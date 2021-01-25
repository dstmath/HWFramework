package org.bouncycastle.crypto.signers;

import java.io.ByteArrayOutputStream;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.bouncycastle.util.Arrays;

public class Ed25519ctxSigner implements Signer {
    private final Buffer buffer = new Buffer();
    private final byte[] context;
    private boolean forSigning;
    private Ed25519PrivateKeyParameters privateKey;
    private Ed25519PublicKeyParameters publicKey;

    /* access modifiers changed from: private */
    public static class Buffer extends ByteArrayOutputStream {
        private Buffer() {
        }

        /* access modifiers changed from: package-private */
        public synchronized byte[] generateSignature(Ed25519PrivateKeyParameters ed25519PrivateKeyParameters, byte[] bArr) {
            byte[] bArr2;
            bArr2 = new byte[64];
            ed25519PrivateKeyParameters.sign(1, bArr, this.buf, 0, this.count, bArr2, 0);
            reset();
            return bArr2;
        }

        @Override // java.io.ByteArrayOutputStream
        public synchronized void reset() {
            Arrays.fill(this.buf, 0, this.count, (byte) 0);
            this.count = 0;
        }

        /* access modifiers changed from: package-private */
        public synchronized boolean verifySignature(Ed25519PublicKeyParameters ed25519PublicKeyParameters, byte[] bArr, byte[] bArr2) {
            if (64 != bArr2.length) {
                return false;
            }
            boolean verify = Ed25519.verify(bArr2, 0, ed25519PublicKeyParameters.getEncoded(), 0, bArr, this.buf, 0, this.count);
            reset();
            return verify;
        }
    }

    public Ed25519ctxSigner(byte[] bArr) {
        this.context = Arrays.clone(bArr);
    }

    @Override // org.bouncycastle.crypto.Signer
    public byte[] generateSignature() {
        Ed25519PrivateKeyParameters ed25519PrivateKeyParameters;
        if (this.forSigning && (ed25519PrivateKeyParameters = this.privateKey) != null) {
            return this.buffer.generateSignature(ed25519PrivateKeyParameters, this.context);
        }
        throw new IllegalStateException("Ed25519ctxSigner not initialised for signature generation.");
    }

    @Override // org.bouncycastle.crypto.Signer
    public void init(boolean z, CipherParameters cipherParameters) {
        this.forSigning = z;
        if (z) {
            this.privateKey = (Ed25519PrivateKeyParameters) cipherParameters;
            this.publicKey = null;
        } else {
            this.privateKey = null;
            this.publicKey = (Ed25519PublicKeyParameters) cipherParameters;
        }
        reset();
    }

    @Override // org.bouncycastle.crypto.Signer
    public void reset() {
        this.buffer.reset();
    }

    @Override // org.bouncycastle.crypto.Signer
    public void update(byte b) {
        this.buffer.write(b);
    }

    @Override // org.bouncycastle.crypto.Signer
    public void update(byte[] bArr, int i, int i2) {
        this.buffer.write(bArr, i, i2);
    }

    @Override // org.bouncycastle.crypto.Signer
    public boolean verifySignature(byte[] bArr) {
        Ed25519PublicKeyParameters ed25519PublicKeyParameters;
        if (!this.forSigning && (ed25519PublicKeyParameters = this.publicKey) != null) {
            return this.buffer.verifySignature(ed25519PublicKeyParameters, this.context, bArr);
        }
        throw new IllegalStateException("Ed25519ctxSigner not initialised for verification");
    }
}
