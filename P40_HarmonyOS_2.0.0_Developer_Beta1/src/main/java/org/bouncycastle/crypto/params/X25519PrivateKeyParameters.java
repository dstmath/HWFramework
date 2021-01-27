package org.bouncycastle.crypto.params;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import org.bouncycastle.math.ec.rfc7748.X25519;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public final class X25519PrivateKeyParameters extends AsymmetricKeyParameter {
    public static final int KEY_SIZE = 32;
    public static final int SECRET_SIZE = 32;
    private final byte[] data;

    public X25519PrivateKeyParameters(InputStream inputStream) throws IOException {
        super(true);
        this.data = new byte[32];
        if (32 != Streams.readFully(inputStream, this.data)) {
            throw new EOFException("EOF encountered in middle of X25519 private key");
        }
    }

    public X25519PrivateKeyParameters(SecureRandom secureRandom) {
        super(true);
        this.data = new byte[32];
        X25519.generatePrivateKey(secureRandom, this.data);
    }

    public X25519PrivateKeyParameters(byte[] bArr, int i) {
        super(true);
        this.data = new byte[32];
        System.arraycopy(bArr, i, this.data, 0, 32);
    }

    public void encode(byte[] bArr, int i) {
        System.arraycopy(this.data, 0, bArr, i, 32);
    }

    public X25519PublicKeyParameters generatePublicKey() {
        byte[] bArr = new byte[32];
        X25519.generatePublicKey(this.data, 0, bArr, 0);
        return new X25519PublicKeyParameters(bArr, 0);
    }

    public void generateSecret(X25519PublicKeyParameters x25519PublicKeyParameters, byte[] bArr, int i) {
        byte[] bArr2 = new byte[32];
        x25519PublicKeyParameters.encode(bArr2, 0);
        if (!X25519.calculateAgreement(this.data, 0, bArr2, 0, bArr, i)) {
            throw new IllegalStateException("X25519 agreement failed");
        }
    }

    public byte[] getEncoded() {
        return Arrays.clone(this.data);
    }
}
