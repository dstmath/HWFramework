package org.bouncycastle.crypto.engines;

import java.math.BigInteger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.util.Arrays;

class RSACoreEngine {
    private boolean forEncryption;
    private RSAKeyParameters key;

    RSACoreEngine() {
    }

    public BigInteger convertInput(byte[] bArr, int i, int i2) {
        if (i2 > getInputBlockSize() + 1) {
            throw new DataLengthException("input too large for RSA cipher.");
        } else if (i2 != getInputBlockSize() + 1 || this.forEncryption) {
            if (!(i == 0 && i2 == bArr.length)) {
                byte[] bArr2 = new byte[i2];
                System.arraycopy(bArr, i, bArr2, 0, i2);
                bArr = bArr2;
            }
            BigInteger bigInteger = new BigInteger(1, bArr);
            if (bigInteger.compareTo(this.key.getModulus()) < 0) {
                return bigInteger;
            }
            throw new DataLengthException("input too large for RSA cipher.");
        } else {
            throw new DataLengthException("input too large for RSA cipher.");
        }
    }

    public byte[] convertOutput(BigInteger bigInteger) {
        byte[] bArr;
        byte[] byteArray = bigInteger.toByteArray();
        if (!this.forEncryption) {
            if (byteArray[0] == 0) {
                bArr = new byte[(byteArray.length - 1)];
                System.arraycopy(byteArray, 1, bArr, 0, bArr.length);
            } else {
                bArr = new byte[byteArray.length];
                System.arraycopy(byteArray, 0, bArr, 0, bArr.length);
            }
            Arrays.fill(byteArray, (byte) 0);
            return bArr;
        } else if (byteArray[0] == 0 && byteArray.length > getOutputBlockSize()) {
            byte[] bArr2 = new byte[(byteArray.length - 1)];
            System.arraycopy(byteArray, 1, bArr2, 0, bArr2.length);
            return bArr2;
        } else if (byteArray.length >= getOutputBlockSize()) {
            return byteArray;
        } else {
            byte[] bArr3 = new byte[getOutputBlockSize()];
            System.arraycopy(byteArray, 0, bArr3, bArr3.length - byteArray.length, byteArray.length);
            return bArr3;
        }
    }

    public int getInputBlockSize() {
        int bitLength = (this.key.getModulus().bitLength() + 7) / 8;
        return this.forEncryption ? bitLength - 1 : bitLength;
    }

    public int getOutputBlockSize() {
        int bitLength = (this.key.getModulus().bitLength() + 7) / 8;
        return this.forEncryption ? bitLength : bitLength - 1;
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        if (cipherParameters instanceof ParametersWithRandom) {
            cipherParameters = ((ParametersWithRandom) cipherParameters).getParameters();
        }
        this.key = (RSAKeyParameters) cipherParameters;
        this.forEncryption = z;
    }

    public BigInteger processBlock(BigInteger bigInteger) {
        RSAKeyParameters rSAKeyParameters = this.key;
        if (!(rSAKeyParameters instanceof RSAPrivateCrtKeyParameters)) {
            return bigInteger.modPow(rSAKeyParameters.getExponent(), this.key.getModulus());
        }
        RSAPrivateCrtKeyParameters rSAPrivateCrtKeyParameters = (RSAPrivateCrtKeyParameters) rSAKeyParameters;
        BigInteger p = rSAPrivateCrtKeyParameters.getP();
        BigInteger q = rSAPrivateCrtKeyParameters.getQ();
        BigInteger dp = rSAPrivateCrtKeyParameters.getDP();
        BigInteger dq = rSAPrivateCrtKeyParameters.getDQ();
        BigInteger qInv = rSAPrivateCrtKeyParameters.getQInv();
        BigInteger modPow = bigInteger.remainder(p).modPow(dp, p);
        BigInteger modPow2 = bigInteger.remainder(q).modPow(dq, q);
        return modPow.subtract(modPow2).multiply(qInv).mod(p).multiply(q).add(modPow2);
    }
}
