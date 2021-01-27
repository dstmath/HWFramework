package org.bouncycastle.crypto.params;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public final class X25519PublicKeyParameters extends AsymmetricKeyParameter {
    public static final int KEY_SIZE = 32;
    private final byte[] data;

    public X25519PublicKeyParameters(InputStream inputStream) throws IOException {
        super(false);
        this.data = new byte[32];
        if (32 != Streams.readFully(inputStream, this.data)) {
            throw new EOFException("EOF encountered in middle of X25519 public key");
        }
    }

    public X25519PublicKeyParameters(byte[] bArr, int i) {
        super(false);
        this.data = new byte[32];
        System.arraycopy(bArr, i, this.data, 0, 32);
    }

    public void encode(byte[] bArr, int i) {
        System.arraycopy(this.data, 0, bArr, i, 32);
    }

    public byte[] getEncoded() {
        return Arrays.clone(this.data);
    }
}
