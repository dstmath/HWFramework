package org.bouncycastle.crypto.generators;

import java.math.BigInteger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.DerivationParameters;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.MacDerivationFunction;
import org.bouncycastle.crypto.params.KDFFeedbackParameters;
import org.bouncycastle.crypto.params.KeyParameter;

public class KDFFeedbackBytesGenerator implements MacDerivationFunction {
    private static final BigInteger INTEGER_MAX = BigInteger.valueOf(2147483647L);
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private byte[] fixedInputData;
    private int generatedBytes;
    private final int h;
    private byte[] ios;
    private byte[] iv;
    private byte[] k = new byte[this.h];
    private int maxSizeExcl;
    private final Mac prf;
    private boolean useCounter;

    public KDFFeedbackBytesGenerator(Mac mac) {
        this.prf = mac;
        this.h = mac.getMacSize();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0038, code lost:
        r5.ios[r5.ios.length - 3] = (byte) (r0 >>> 16);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0044, code lost:
        r5.ios[r5.ios.length - 2] = (byte) (r0 >>> 8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0050, code lost:
        r5.ios[r5.ios.length - 1] = (byte) r0;
        r5.prf.update(r5.ios, 0, r5.ios.length);
     */
    private void generateNext() {
        Mac mac;
        byte[] bArr;
        int length;
        if (this.generatedBytes == 0) {
            mac = this.prf;
            bArr = this.iv;
            length = this.iv.length;
        } else {
            mac = this.prf;
            bArr = this.k;
            length = this.k.length;
        }
        mac.update(bArr, 0, length);
        if (this.useCounter) {
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
        this.prf.update(this.fixedInputData, 0, this.fixedInputData.length);
        this.prf.doFinal(this.k, 0);
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
        if (derivationParameters instanceof KDFFeedbackParameters) {
            KDFFeedbackParameters kDFFeedbackParameters = (KDFFeedbackParameters) derivationParameters;
            this.prf.init(new KeyParameter(kDFFeedbackParameters.getKI()));
            this.fixedInputData = kDFFeedbackParameters.getFixedInputData();
            int r = kDFFeedbackParameters.getR();
            this.ios = new byte[(r / 8)];
            int i = Integer.MAX_VALUE;
            if (kDFFeedbackParameters.useCounter()) {
                BigInteger multiply = TWO.pow(r).multiply(BigInteger.valueOf((long) this.h));
                if (multiply.compareTo(INTEGER_MAX) != 1) {
                    i = multiply.intValue();
                }
            }
            this.maxSizeExcl = i;
            this.iv = kDFFeedbackParameters.getIV();
            this.useCounter = kDFFeedbackParameters.useCounter();
            this.generatedBytes = 0;
            return;
        }
        throw new IllegalArgumentException("Wrong type of arguments given");
    }
}
