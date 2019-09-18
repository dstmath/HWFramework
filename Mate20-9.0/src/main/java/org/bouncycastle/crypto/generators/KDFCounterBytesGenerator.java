package org.bouncycastle.crypto.generators;

import java.math.BigInteger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.DerivationParameters;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.MacDerivationFunction;
import org.bouncycastle.crypto.params.KDFCounterParameters;
import org.bouncycastle.crypto.params.KeyParameter;

public class KDFCounterBytesGenerator implements MacDerivationFunction {
    private static final BigInteger INTEGER_MAX = BigInteger.valueOf(2147483647L);
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private byte[] fixedInputDataCtrPrefix;
    private byte[] fixedInputData_afterCtr;
    private int generatedBytes;
    private final int h;
    private byte[] ios;
    private byte[] k = new byte[this.h];
    private int maxSizeExcl;
    private final Mac prf;

    public KDFCounterBytesGenerator(Mac mac) {
        this.prf = mac;
        this.h = mac.getMacSize();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:5:0x001d, code lost:
        r5.ios[r5.ios.length - 3] = (byte) (r0 >>> 16);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0029, code lost:
        r5.ios[r5.ios.length - 2] = (byte) (r0 >>> 8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0035, code lost:
        r5.ios[r5.ios.length - 1] = (byte) r0;
        r5.prf.update(r5.fixedInputDataCtrPrefix, 0, r5.fixedInputDataCtrPrefix.length);
        r5.prf.update(r5.ios, 0, r5.ios.length);
        r5.prf.update(r5.fixedInputData_afterCtr, 0, r5.fixedInputData_afterCtr.length);
        r5.prf.doFinal(r5.k, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0064, code lost:
        return;
     */
    private void generateNext() {
        int i = (this.generatedBytes / this.h) + 1;
        switch (this.ios.length) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                this.ios[0] = (byte) (i >>> 24);
                break;
            default:
                throw new IllegalStateException("Unsupported size of counter i");
        }
    }

    public int generateBytes(byte[] bArr, int i, int i2) throws DataLengthException, IllegalArgumentException {
        int i3 = this.generatedBytes + i2;
        if (i3 < 0 || i3 >= this.maxSizeExcl) {
            throw new DataLengthException("Current KDFCTR may only be used for " + this.maxSizeExcl + " bytes");
        }
        if (this.generatedBytes % this.h == 0) {
            generateNext();
        }
        int i4 = this.generatedBytes % this.h;
        int min = Math.min(this.h - (this.generatedBytes % this.h), i2);
        System.arraycopy(this.k, i4, bArr, i, min);
        this.generatedBytes += min;
        int i5 = i2 - min;
        while (true) {
            i += min;
            if (i5 <= 0) {
                return i2;
            }
            generateNext();
            min = Math.min(this.h, i5);
            System.arraycopy(this.k, 0, bArr, i, min);
            this.generatedBytes += min;
            i5 -= min;
        }
    }

    public Mac getMac() {
        return this.prf;
    }

    public void init(DerivationParameters derivationParameters) {
        if (derivationParameters instanceof KDFCounterParameters) {
            KDFCounterParameters kDFCounterParameters = (KDFCounterParameters) derivationParameters;
            this.prf.init(new KeyParameter(kDFCounterParameters.getKI()));
            this.fixedInputDataCtrPrefix = kDFCounterParameters.getFixedInputDataCounterPrefix();
            this.fixedInputData_afterCtr = kDFCounterParameters.getFixedInputDataCounterSuffix();
            int r = kDFCounterParameters.getR();
            this.ios = new byte[(r / 8)];
            BigInteger multiply = TWO.pow(r).multiply(BigInteger.valueOf((long) this.h));
            this.maxSizeExcl = multiply.compareTo(INTEGER_MAX) == 1 ? Integer.MAX_VALUE : multiply.intValue();
            this.generatedBytes = 0;
            return;
        }
        throw new IllegalArgumentException("Wrong type of arguments given");
    }
}
