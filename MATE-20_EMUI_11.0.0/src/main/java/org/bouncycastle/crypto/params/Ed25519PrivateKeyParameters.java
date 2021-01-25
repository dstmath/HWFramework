package org.bouncycastle.crypto.params;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public final class Ed25519PrivateKeyParameters extends AsymmetricKeyParameter {
    public static final int KEY_SIZE = 32;
    public static final int SIGNATURE_SIZE = 64;
    private Ed25519PublicKeyParameters cachedPublicKey;
    private final byte[] data;

    public Ed25519PrivateKeyParameters(InputStream inputStream) throws IOException {
        super(true);
        this.data = new byte[32];
        if (32 != Streams.readFully(inputStream, this.data)) {
            throw new EOFException("EOF encountered in middle of Ed25519 private key");
        }
    }

    public Ed25519PrivateKeyParameters(SecureRandom secureRandom) {
        super(true);
        this.data = new byte[32];
        Ed25519.generatePrivateKey(secureRandom, this.data);
    }

    public Ed25519PrivateKeyParameters(byte[] bArr, int i) {
        super(true);
        this.data = new byte[32];
        System.arraycopy(bArr, i, this.data, 0, 32);
    }

    public void encode(byte[] bArr, int i) {
        System.arraycopy(this.data, 0, bArr, i, 32);
    }

    public Ed25519PublicKeyParameters generatePublicKey() {
        Ed25519PublicKeyParameters ed25519PublicKeyParameters;
        synchronized (this.data) {
            if (this.cachedPublicKey == null) {
                byte[] bArr = new byte[32];
                Ed25519.generatePublicKey(this.data, 0, bArr, 0);
                this.cachedPublicKey = new Ed25519PublicKeyParameters(bArr, 0);
            }
            ed25519PublicKeyParameters = this.cachedPublicKey;
        }
        return ed25519PublicKeyParameters;
    }

    public byte[] getEncoded() {
        return Arrays.clone(this.data);
    }

    public void sign(int i, Ed25519PublicKeyParameters ed25519PublicKeyParameters, byte[] bArr, byte[] bArr2, int i2, int i3, byte[] bArr3, int i4) {
        sign(i, bArr, bArr2, i2, i3, bArr3, i4);
    }

    public void sign(int i, byte[] bArr, byte[] bArr2, int i2, int i3, byte[] bArr3, int i4) {
        byte[] bArr4 = new byte[32];
        generatePublicKey().encode(bArr4, 0);
        if (i != 0) {
            if (i == 1) {
                Ed25519.sign(this.data, 0, bArr4, 0, bArr, bArr2, i2, i3, bArr3, i4);
            } else if (i != 2) {
                throw new IllegalArgumentException("algorithm");
            } else if (64 == i3) {
                Ed25519.signPrehash(this.data, 0, bArr4, 0, bArr, bArr2, i2, bArr3, i4);
            } else {
                throw new IllegalArgumentException("msgLen");
            }
        } else if (bArr == null) {
            Ed25519.sign(this.data, 0, bArr4, 0, bArr2, i2, i3, bArr3, i4);
        } else {
            throw new IllegalArgumentException("ctx");
        }
    }
}
