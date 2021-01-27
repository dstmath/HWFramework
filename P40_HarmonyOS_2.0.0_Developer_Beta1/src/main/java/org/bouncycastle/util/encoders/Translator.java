package org.bouncycastle.util.encoders;

public interface Translator {
    int decode(byte[] bArr, int i, int i2, byte[] bArr2, int i3);

    int encode(byte[] bArr, int i, int i2, byte[] bArr2, int i3);

    int getDecodedBlockSize();

    int getEncodedBlockSize();
}
