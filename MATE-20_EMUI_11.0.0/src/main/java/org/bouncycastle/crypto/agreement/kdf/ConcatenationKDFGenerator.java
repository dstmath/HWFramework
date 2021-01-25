package org.bouncycastle.crypto.agreement.kdf;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.DerivationFunction;
import org.bouncycastle.crypto.DerivationParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.KDFParameters;

public class ConcatenationKDFGenerator implements DerivationFunction {
    private Digest digest;
    private int hLen;
    private byte[] otherInfo;
    private byte[] shared;

    public ConcatenationKDFGenerator(Digest digest2) {
        this.digest = digest2;
        this.hLen = digest2.getDigestSize();
    }

    private void ItoOSP(int i, byte[] bArr) {
        bArr[0] = (byte) (i >>> 24);
        bArr[1] = (byte) (i >>> 16);
        bArr[2] = (byte) (i >>> 8);
        bArr[3] = (byte) (i >>> 0);
    }

    @Override // org.bouncycastle.crypto.DerivationFunction
    public int generateBytes(byte[] bArr, int i, int i2) throws DataLengthException, IllegalArgumentException {
        int i3;
        int i4;
        if (bArr.length - i2 >= i) {
            byte[] bArr2 = new byte[this.hLen];
            byte[] bArr3 = new byte[4];
            this.digest.reset();
            int i5 = 1;
            if (i2 > this.hLen) {
                i3 = 0;
                while (true) {
                    ItoOSP(i5, bArr3);
                    this.digest.update(bArr3, 0, bArr3.length);
                    Digest digest2 = this.digest;
                    byte[] bArr4 = this.shared;
                    digest2.update(bArr4, 0, bArr4.length);
                    Digest digest3 = this.digest;
                    byte[] bArr5 = this.otherInfo;
                    digest3.update(bArr5, 0, bArr5.length);
                    this.digest.doFinal(bArr2, 0);
                    System.arraycopy(bArr2, 0, bArr, i + i3, this.hLen);
                    int i6 = this.hLen;
                    i3 += i6;
                    i4 = i5 + 1;
                    if (i5 >= i2 / i6) {
                        break;
                    }
                    i5 = i4;
                }
                i5 = i4;
            } else {
                i3 = 0;
            }
            if (i3 < i2) {
                ItoOSP(i5, bArr3);
                this.digest.update(bArr3, 0, bArr3.length);
                Digest digest4 = this.digest;
                byte[] bArr6 = this.shared;
                digest4.update(bArr6, 0, bArr6.length);
                Digest digest5 = this.digest;
                byte[] bArr7 = this.otherInfo;
                digest5.update(bArr7, 0, bArr7.length);
                this.digest.doFinal(bArr2, 0);
                System.arraycopy(bArr2, 0, bArr, i + i3, i2 - i3);
            }
            return i2;
        }
        throw new OutputLengthException("output buffer too small");
    }

    public Digest getDigest() {
        return this.digest;
    }

    @Override // org.bouncycastle.crypto.DerivationFunction
    public void init(DerivationParameters derivationParameters) {
        if (derivationParameters instanceof KDFParameters) {
            KDFParameters kDFParameters = (KDFParameters) derivationParameters;
            this.shared = kDFParameters.getSharedSecret();
            this.otherInfo = kDFParameters.getIV();
            return;
        }
        throw new IllegalArgumentException("KDF parameters required for generator");
    }
}
