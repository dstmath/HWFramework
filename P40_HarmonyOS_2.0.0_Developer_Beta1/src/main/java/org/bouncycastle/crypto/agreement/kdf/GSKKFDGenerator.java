package org.bouncycastle.crypto.agreement.kdf;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.DerivationParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.DigestDerivationFunction;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class GSKKFDGenerator implements DigestDerivationFunction {
    private byte[] buf;
    private int counter;
    private final Digest digest;
    private byte[] r;
    private byte[] z;

    public GSKKFDGenerator(Digest digest2) {
        this.digest = digest2;
        this.buf = new byte[digest2.getDigestSize()];
    }

    @Override // org.bouncycastle.crypto.DerivationFunction
    public int generateBytes(byte[] bArr, int i, int i2) throws DataLengthException, IllegalArgumentException {
        if (i + i2 <= bArr.length) {
            Digest digest2 = this.digest;
            byte[] bArr2 = this.z;
            digest2.update(bArr2, 0, bArr2.length);
            int i3 = this.counter;
            this.counter = i3 + 1;
            byte[] intToBigEndian = Pack.intToBigEndian(i3);
            this.digest.update(intToBigEndian, 0, intToBigEndian.length);
            byte[] bArr3 = this.r;
            if (bArr3 != null) {
                this.digest.update(bArr3, 0, bArr3.length);
            }
            this.digest.doFinal(this.buf, 0);
            System.arraycopy(this.buf, 0, bArr, i, i2);
            Arrays.clear(this.buf);
            return i2;
        }
        throw new DataLengthException("output buffer too small");
    }

    @Override // org.bouncycastle.crypto.DigestDerivationFunction
    public Digest getDigest() {
        return this.digest;
    }

    @Override // org.bouncycastle.crypto.DerivationFunction
    public void init(DerivationParameters derivationParameters) {
        if (derivationParameters instanceof GSKKDFParameters) {
            GSKKDFParameters gSKKDFParameters = (GSKKDFParameters) derivationParameters;
            this.z = gSKKDFParameters.getZ();
            this.counter = gSKKDFParameters.getStartCounter();
            this.r = gSKKDFParameters.getNonce();
            return;
        }
        throw new IllegalArgumentException("unkown parameters type");
    }
}
