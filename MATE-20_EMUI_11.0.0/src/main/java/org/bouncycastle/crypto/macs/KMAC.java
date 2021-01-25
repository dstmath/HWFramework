package org.bouncycastle.crypto.macs;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.Xof;
import org.bouncycastle.crypto.digests.CSHAKEDigest;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

public class KMAC implements Mac, Xof {
    private static final byte[] padding = new byte[100];
    private final int bitLength;
    private final CSHAKEDigest cshake;
    private boolean firstOutput;
    private boolean initialised;
    private byte[] key;
    private final int outputLength;

    public KMAC(int i, byte[] bArr) {
        this.cshake = new CSHAKEDigest(i, Strings.toByteArray("KMAC"), bArr);
        this.bitLength = i;
        this.outputLength = (i * 2) / 8;
    }

    private void bytePad(byte[] bArr, int i) {
        byte[] leftEncode = leftEncode((long) i);
        update(leftEncode, 0, leftEncode.length);
        byte[] encode = encode(bArr);
        update(encode, 0, encode.length);
        int length = i - ((leftEncode.length + encode.length) % i);
        if (length > 0) {
            while (true) {
                byte[] bArr2 = padding;
                if (length > bArr2.length) {
                    update(bArr2, 0, bArr2.length);
                    length -= padding.length;
                } else {
                    update(bArr2, 0, length);
                    return;
                }
            }
        }
    }

    private static byte[] encode(byte[] bArr) {
        return Arrays.concatenate(leftEncode((long) (bArr.length * 8)), bArr);
    }

    private static byte[] leftEncode(long j) {
        long j2 = j;
        byte b = 1;
        while (true) {
            j2 >>= 8;
            if (j2 == 0) {
                break;
            }
            b = (byte) (b + 1);
        }
        byte[] bArr = new byte[(b + 1)];
        bArr[0] = b;
        for (int i = 1; i <= b; i++) {
            bArr[i] = (byte) ((int) (j >> ((b - i) * 8)));
        }
        return bArr;
    }

    private static byte[] rightEncode(long j) {
        byte b = 1;
        long j2 = j;
        while (true) {
            j2 >>= 8;
            if (j2 == 0) {
                break;
            }
            b = (byte) (b + 1);
        }
        byte[] bArr = new byte[(b + 1)];
        bArr[b] = b;
        for (int i = 0; i < b; i++) {
            int i2 = b - i;
            bArr[i2 - 1] = (byte) ((int) (j >> (i2 * 8)));
        }
        return bArr;
    }

    @Override // org.bouncycastle.crypto.Mac
    public int doFinal(byte[] bArr, int i) throws DataLengthException, IllegalStateException {
        if (this.firstOutput) {
            if (this.initialised) {
                byte[] rightEncode = rightEncode((long) (getMacSize() * 8));
                this.cshake.update(rightEncode, 0, rightEncode.length);
            } else {
                throw new IllegalStateException("KMAC not initialized");
            }
        }
        int doFinal = this.cshake.doFinal(bArr, i, getMacSize());
        reset();
        return doFinal;
    }

    @Override // org.bouncycastle.crypto.Xof
    public int doFinal(byte[] bArr, int i, int i2) {
        if (this.firstOutput) {
            if (this.initialised) {
                byte[] rightEncode = rightEncode((long) (i2 * 8));
                this.cshake.update(rightEncode, 0, rightEncode.length);
            } else {
                throw new IllegalStateException("KMAC not initialized");
            }
        }
        int doFinal = this.cshake.doFinal(bArr, i, i2);
        reset();
        return doFinal;
    }

    @Override // org.bouncycastle.crypto.Xof
    public int doOutput(byte[] bArr, int i, int i2) {
        if (this.firstOutput) {
            if (this.initialised) {
                byte[] rightEncode = rightEncode(0);
                this.cshake.update(rightEncode, 0, rightEncode.length);
                this.firstOutput = false;
            } else {
                throw new IllegalStateException("KMAC not initialized");
            }
        }
        return this.cshake.doOutput(bArr, i, i2);
    }

    @Override // org.bouncycastle.crypto.Mac
    public String getAlgorithmName() {
        return "KMACwith" + this.cshake.getAlgorithmName();
    }

    @Override // org.bouncycastle.crypto.ExtendedDigest
    public int getByteLength() {
        return this.cshake.getByteLength();
    }

    @Override // org.bouncycastle.crypto.Digest
    public int getDigestSize() {
        return this.outputLength;
    }

    @Override // org.bouncycastle.crypto.Mac
    public int getMacSize() {
        return this.outputLength;
    }

    @Override // org.bouncycastle.crypto.Mac
    public void init(CipherParameters cipherParameters) throws IllegalArgumentException {
        this.key = Arrays.clone(((KeyParameter) cipherParameters).getKey());
        this.initialised = true;
        reset();
    }

    @Override // org.bouncycastle.crypto.Mac
    public void reset() {
        this.cshake.reset();
        byte[] bArr = this.key;
        if (bArr != null) {
            bytePad(bArr, this.bitLength == 128 ? 168 : 136);
        }
        this.firstOutput = true;
    }

    @Override // org.bouncycastle.crypto.Mac
    public void update(byte b) throws IllegalStateException {
        if (this.initialised) {
            this.cshake.update(b);
            return;
        }
        throw new IllegalStateException("KMAC not initialized");
    }

    @Override // org.bouncycastle.crypto.Mac
    public void update(byte[] bArr, int i, int i2) throws DataLengthException, IllegalStateException {
        if (this.initialised) {
            this.cshake.update(bArr, i, i2);
            return;
        }
        throw new IllegalStateException("KMAC not initialized");
    }
}
