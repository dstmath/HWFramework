package org.bouncycastle.pqc.crypto.gmss.util;

import org.bouncycastle.crypto.Digest;

public class GMSSRandom {
    private Digest messDigestTree;

    public GMSSRandom(Digest digest) {
        this.messDigestTree = digest;
    }

    private void addByteArrays(byte[] bArr, byte[] bArr2) {
        byte b = 0;
        for (int i = 0; i < bArr.length; i++) {
            int i2 = (bArr[i] & 255) + (bArr2[i] & 255) + b;
            bArr[i] = (byte) i2;
            b = (byte) (i2 >> 8);
        }
    }

    private void addOne(byte[] bArr) {
        byte b = 1;
        for (int i = 0; i < bArr.length; i++) {
            int i2 = (bArr[i] & 255) + b;
            bArr[i] = (byte) i2;
            b = (byte) (i2 >> 8);
        }
    }

    public byte[] nextSeed(byte[] bArr) {
        byte[] bArr2 = new byte[bArr.length];
        this.messDigestTree.update(bArr, 0, bArr.length);
        byte[] bArr3 = new byte[this.messDigestTree.getDigestSize()];
        this.messDigestTree.doFinal(bArr3, 0);
        addByteArrays(bArr, bArr3);
        addOne(bArr);
        return bArr3;
    }
}
