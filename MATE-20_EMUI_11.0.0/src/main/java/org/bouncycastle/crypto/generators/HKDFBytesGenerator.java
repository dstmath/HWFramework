package org.bouncycastle.crypto.generators;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.DerivationFunction;
import org.bouncycastle.crypto.DerivationParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;

public class HKDFBytesGenerator implements DerivationFunction {
    private byte[] currentT;
    private int generatedBytes;
    private HMac hMacHash;
    private int hashLen;
    private byte[] info;

    public HKDFBytesGenerator(Digest digest) {
        this.hMacHash = new HMac(digest);
        this.hashLen = digest.getDigestSize();
    }

    private void expandNext() throws DataLengthException {
        int i = this.generatedBytes;
        int i2 = this.hashLen;
        int i3 = (i / i2) + 1;
        if (i3 < 256) {
            if (i != 0) {
                this.hMacHash.update(this.currentT, 0, i2);
            }
            HMac hMac = this.hMacHash;
            byte[] bArr = this.info;
            hMac.update(bArr, 0, bArr.length);
            this.hMacHash.update((byte) i3);
            this.hMacHash.doFinal(this.currentT, 0);
            return;
        }
        throw new DataLengthException("HKDF cannot generate more than 255 blocks of HashLen size");
    }

    private KeyParameter extract(byte[] bArr, byte[] bArr2) {
        if (bArr == null) {
            this.hMacHash.init(new KeyParameter(new byte[this.hashLen]));
        } else {
            this.hMacHash.init(new KeyParameter(bArr));
        }
        this.hMacHash.update(bArr2, 0, bArr2.length);
        byte[] bArr3 = new byte[this.hashLen];
        this.hMacHash.doFinal(bArr3, 0);
        return new KeyParameter(bArr3);
    }

    @Override // org.bouncycastle.crypto.DerivationFunction
    public int generateBytes(byte[] bArr, int i, int i2) throws DataLengthException, IllegalArgumentException {
        int i3 = this.generatedBytes;
        int i4 = i3 + i2;
        int i5 = this.hashLen;
        if (i4 <= i5 * GF2Field.MASK) {
            if (i3 % i5 == 0) {
                expandNext();
            }
            int i6 = this.generatedBytes;
            int i7 = this.hashLen;
            int i8 = i6 % i7;
            int min = Math.min(i7 - (i6 % i7), i2);
            System.arraycopy(this.currentT, i8, bArr, i, min);
            this.generatedBytes += min;
            int i9 = i2 - min;
            while (true) {
                i += min;
                if (i9 <= 0) {
                    return i2;
                }
                expandNext();
                min = Math.min(this.hashLen, i9);
                System.arraycopy(this.currentT, 0, bArr, i, min);
                this.generatedBytes += min;
                i9 -= min;
            }
        } else {
            throw new DataLengthException("HKDF may only be used for 255 * HashLen bytes of output");
        }
    }

    public Digest getDigest() {
        return this.hMacHash.getUnderlyingDigest();
    }

    @Override // org.bouncycastle.crypto.DerivationFunction
    public void init(DerivationParameters derivationParameters) {
        KeyParameter keyParameter;
        HMac hMac;
        if (derivationParameters instanceof HKDFParameters) {
            HKDFParameters hKDFParameters = (HKDFParameters) derivationParameters;
            if (hKDFParameters.skipExtract()) {
                hMac = this.hMacHash;
                keyParameter = new KeyParameter(hKDFParameters.getIKM());
            } else {
                hMac = this.hMacHash;
                keyParameter = extract(hKDFParameters.getSalt(), hKDFParameters.getIKM());
            }
            hMac.init(keyParameter);
            this.info = hKDFParameters.getInfo();
            this.generatedBytes = 0;
            this.currentT = new byte[this.hashLen];
            return;
        }
        throw new IllegalArgumentException("HKDF parameters required for HKDFBytesGenerator");
    }
}
