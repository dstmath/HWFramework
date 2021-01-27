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

    private void generateNext() {
        int i = (this.generatedBytes / this.h) + 1;
        byte[] bArr = this.ios;
        int length = bArr.length;
        if (length != 1) {
            if (length != 2) {
                if (length != 3) {
                    if (length == 4) {
                        bArr[0] = (byte) (i >>> 24);
                    } else {
                        throw new IllegalStateException("Unsupported size of counter i");
                    }
                }
                byte[] bArr2 = this.ios;
                bArr2[bArr2.length - 3] = (byte) (i >>> 16);
            }
            byte[] bArr3 = this.ios;
            bArr3[bArr3.length - 2] = (byte) (i >>> 8);
        }
        byte[] bArr4 = this.ios;
        bArr4[bArr4.length - 1] = (byte) i;
        Mac mac = this.prf;
        byte[] bArr5 = this.fixedInputDataCtrPrefix;
        mac.update(bArr5, 0, bArr5.length);
        Mac mac2 = this.prf;
        byte[] bArr6 = this.ios;
        mac2.update(bArr6, 0, bArr6.length);
        Mac mac3 = this.prf;
        byte[] bArr7 = this.fixedInputData_afterCtr;
        mac3.update(bArr7, 0, bArr7.length);
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
