package org.bouncycastle.jce.provider;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.DerivationFunction;
import org.bouncycastle.crypto.DerivationParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.KDFParameters;

public class BrokenKDF2BytesGenerator implements DerivationFunction {
    private Digest digest;
    private byte[] iv;
    private byte[] shared;

    public BrokenKDF2BytesGenerator(Digest digest2) {
        this.digest = digest2;
    }

    public int generateBytes(byte[] bArr, int i, int i2) throws DataLengthException, IllegalArgumentException {
        if (bArr.length - i2 >= i) {
            long j = ((long) i2) * 8;
            if (j <= ((long) this.digest.getDigestSize()) * 8 * 2147483648L) {
                int digestSize = (int) (j / ((long) this.digest.getDigestSize()));
                byte[] bArr2 = new byte[this.digest.getDigestSize()];
                for (int i3 = 1; i3 <= digestSize; i3++) {
                    this.digest.update(this.shared, 0, this.shared.length);
                    this.digest.update((byte) (i3 & 255));
                    this.digest.update((byte) ((i3 >> 8) & 255));
                    this.digest.update((byte) ((i3 >> 16) & 255));
                    this.digest.update((byte) ((i3 >> 24) & 255));
                    this.digest.update(this.iv, 0, this.iv.length);
                    this.digest.doFinal(bArr2, 0);
                    int i4 = i2 - i;
                    if (i4 > bArr2.length) {
                        System.arraycopy(bArr2, 0, bArr, i, bArr2.length);
                        i += bArr2.length;
                    } else {
                        System.arraycopy(bArr2, 0, bArr, i, i4);
                    }
                }
                this.digest.reset();
                return i2;
            }
            throw new IllegalArgumentException("Output length too large");
        }
        throw new OutputLengthException("output buffer too small");
    }

    public Digest getDigest() {
        return this.digest;
    }

    public void init(DerivationParameters derivationParameters) {
        if (derivationParameters instanceof KDFParameters) {
            KDFParameters kDFParameters = (KDFParameters) derivationParameters;
            this.shared = kDFParameters.getSharedSecret();
            this.iv = kDFParameters.getIV();
            return;
        }
        throw new IllegalArgumentException("KDF parameters required for generator");
    }
}
