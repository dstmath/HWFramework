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

    private void generateNext() {
        int i;
        byte[] bArr;
        Mac mac;
        if (this.generatedBytes == 0) {
            mac = this.prf;
            bArr = this.iv;
            i = bArr.length;
        } else {
            mac = this.prf;
            bArr = this.k;
            i = bArr.length;
        }
        mac.update(bArr, 0, i);
        if (this.useCounter) {
            int i2 = (this.generatedBytes / this.h) + 1;
            byte[] bArr2 = this.ios;
            int length = bArr2.length;
            if (length != 1) {
                if (length != 2) {
                    if (length != 3) {
                        if (length == 4) {
                            bArr2[0] = (byte) (i2 >>> 24);
                        } else {
                            throw new IllegalStateException("Unsupported size of counter i");
                        }
                    }
                    byte[] bArr3 = this.ios;
                    bArr3[bArr3.length - 3] = (byte) (i2 >>> 16);
                }
                byte[] bArr4 = this.ios;
                bArr4[bArr4.length - 2] = (byte) (i2 >>> 8);
            }
            byte[] bArr5 = this.ios;
            bArr5[bArr5.length - 1] = (byte) i2;
            this.prf.update(bArr5, 0, bArr5.length);
        }
        Mac mac2 = this.prf;
        byte[] bArr6 = this.fixedInputData;
        mac2.update(bArr6, 0, bArr6.length);
        this.prf.doFinal(this.k, 0);
    }

    @Override // org.bouncycastle.crypto.DerivationFunction
    public int generateBytes(byte[] bArr, int i, int i2) throws DataLengthException, IllegalArgumentException {
        int i3 = this.generatedBytes;
        int i4 = i3 + i2;
        if (i4 < 0 || i4 >= this.maxSizeExcl) {
            throw new DataLengthException("Current KDFCTR may only be used for " + this.maxSizeExcl + " bytes");
        }
        if (i3 % this.h == 0) {
            generateNext();
        }
        int i5 = this.generatedBytes;
        int i6 = this.h;
        int i7 = i5 % i6;
        int min = Math.min(i6 - (i5 % i6), i2);
        System.arraycopy(this.k, i7, bArr, i, min);
        this.generatedBytes += min;
        int i8 = i2 - min;
        while (true) {
            i += min;
            if (i8 <= 0) {
                return i2;
            }
            generateNext();
            min = Math.min(this.h, i8);
            System.arraycopy(this.k, 0, bArr, i, min);
            this.generatedBytes += min;
            i8 -= min;
        }
    }

    @Override // org.bouncycastle.crypto.MacDerivationFunction
    public Mac getMac() {
        return this.prf;
    }

    @Override // org.bouncycastle.crypto.DerivationFunction
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
