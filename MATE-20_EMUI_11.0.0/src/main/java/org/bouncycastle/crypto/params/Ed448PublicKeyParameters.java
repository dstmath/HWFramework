package org.bouncycastle.crypto.params;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public final class Ed448PublicKeyParameters extends AsymmetricKeyParameter {
    public static final int KEY_SIZE = 57;
    private final byte[] data;

    public Ed448PublicKeyParameters(InputStream inputStream) throws IOException {
        super(false);
        this.data = new byte[57];
        if (57 != Streams.readFully(inputStream, this.data)) {
            throw new EOFException("EOF encountered in middle of Ed448 public key");
        }
    }

    public Ed448PublicKeyParameters(byte[] bArr, int i) {
        super(false);
        this.data = new byte[57];
        System.arraycopy(bArr, i, this.data, 0, 57);
    }

    public void encode(byte[] bArr, int i) {
        System.arraycopy(this.data, 0, bArr, i, 57);
    }

    public byte[] getEncoded() {
        return Arrays.clone(this.data);
    }
}
