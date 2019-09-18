package org.bouncycastle.crypto.generators;

import java.math.BigInteger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.DerivationParameters;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.MacDerivationFunction;
import org.bouncycastle.crypto.params.KDFDoublePipelineIterationParameters;
import org.bouncycastle.crypto.params.KeyParameter;

public class KDFDoublePipelineIterationBytesGenerator implements MacDerivationFunction {
    private static final BigInteger INTEGER_MAX = BigInteger.valueOf(2147483647L);
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private byte[] a = new byte[this.h];
    private byte[] fixedInputData;
    private int generatedBytes;
    private final int h;
    private byte[] ios;
    private byte[] k = new byte[this.h];
    private int maxSizeExcl;
    private final Mac prf;
    private boolean useCounter;

    public KDFDoublePipelineIterationBytesGenerator(Mac mac) {
        this.prf = mac;
        this.h = mac.getMacSize();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0049, code lost:
        r5.ios[r5.ios.length - 3] = (byte) (r0 >>> 16);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0055, code lost:
        r5.ios[r5.ios.length - 2] = (byte) (r0 >>> 8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0061, code lost:
        r5.ios[r5.ios.length - 1] = (byte) r0;
        r5.prf.update(r5.ios, 0, r5.ios.length);
     */
    private void generateNext() {
        Mac mac;
        byte[] bArr;
        int length;
        if (this.generatedBytes == 0) {
            mac = this.prf;
            bArr = this.fixedInputData;
            length = this.fixedInputData.length;
        } else {
            mac = this.prf;
            bArr = this.a;
            length = this.a.length;
        }
        mac.update(bArr, 0, length);
        this.prf.doFinal(this.a, 0);
        this.prf.update(this.a, 0, this.a.length);
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
        if (derivationParameters instanceof KDFDoublePipelineIterationParameters) {
            KDFDoublePipelineIterationParameters kDFDoublePipelineIterationParameters = (KDFDoublePipelineIterationParameters) derivationParameters;
            this.prf.init(new KeyParameter(kDFDoublePipelineIterationParameters.getKI()));
            this.fixedInputData = kDFDoublePipelineIterationParameters.getFixedInputData();
            int r = kDFDoublePipelineIterationParameters.getR();
            this.ios = new byte[(r / 8)];
            int i = Integer.MAX_VALUE;
            if (kDFDoublePipelineIterationParameters.useCounter()) {
                BigInteger multiply = TWO.pow(r).multiply(BigInteger.valueOf((long) this.h));
                if (multiply.compareTo(INTEGER_MAX) != 1) {
                    i = multiply.intValue();
                }
            }
            this.maxSizeExcl = i;
            this.useCounter = kDFDoublePipelineIterationParameters.useCounter();
            this.generatedBytes = 0;
            return;
        }
        throw new IllegalArgumentException("Wrong type of arguments given");
    }
}
