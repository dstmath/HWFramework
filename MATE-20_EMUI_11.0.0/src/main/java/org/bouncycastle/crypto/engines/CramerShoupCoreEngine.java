package org.bouncycastle.crypto.engines;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.params.CramerShoupKeyParameters;
import org.bouncycastle.crypto.params.CramerShoupPrivateKeyParameters;
import org.bouncycastle.crypto.params.CramerShoupPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.Strings;

public class CramerShoupCoreEngine {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private boolean forEncryption;
    private CramerShoupKeyParameters key;
    private byte[] label = null;
    private SecureRandom random;

    public static class CramerShoupCiphertextException extends Exception {
        private static final long serialVersionUID = -6360977166495345076L;

        public CramerShoupCiphertextException(String str) {
            super(str);
        }
    }

    private BigInteger generateRandomElement(BigInteger bigInteger, SecureRandom secureRandom) {
        BigInteger bigInteger2 = ONE;
        return BigIntegers.createRandomInRange(bigInteger2, bigInteger.subtract(bigInteger2), secureRandom);
    }

    private boolean isValidMessage(BigInteger bigInteger, BigInteger bigInteger2) {
        return bigInteger.compareTo(bigInteger2) < 0;
    }

    public BigInteger convertInput(byte[] bArr, int i, int i2) {
        if (i2 > getInputBlockSize() + 1) {
            throw new DataLengthException("input too large for Cramer Shoup cipher.");
        } else if (i2 != getInputBlockSize() + 1 || !this.forEncryption) {
            if (!(i == 0 && i2 == bArr.length)) {
                byte[] bArr2 = new byte[i2];
                System.arraycopy(bArr, i, bArr2, 0, i2);
                bArr = bArr2;
            }
            BigInteger bigInteger = new BigInteger(1, bArr);
            if (bigInteger.compareTo(this.key.getParameters().getP()) < 0) {
                return bigInteger;
            }
            throw new DataLengthException("input too large for Cramer Shoup cipher.");
        } else {
            throw new DataLengthException("input too large for Cramer Shoup cipher.");
        }
    }

    public byte[] convertOutput(BigInteger bigInteger) {
        byte[] byteArray = bigInteger.toByteArray();
        if (!this.forEncryption) {
            if (byteArray[0] == 0 && byteArray.length > getOutputBlockSize()) {
                byte[] bArr = new byte[(byteArray.length - 1)];
                System.arraycopy(byteArray, 1, bArr, 0, bArr.length);
                return bArr;
            } else if (byteArray.length < getOutputBlockSize()) {
                byte[] bArr2 = new byte[getOutputBlockSize()];
                System.arraycopy(byteArray, 0, bArr2, bArr2.length - byteArray.length, byteArray.length);
                return bArr2;
            }
        } else if (byteArray[0] == 0) {
            byte[] bArr3 = new byte[(byteArray.length - 1)];
            System.arraycopy(byteArray, 1, bArr3, 0, bArr3.length);
            return bArr3;
        }
        return byteArray;
    }

    public BigInteger decryptBlock(CramerShoupCiphertext cramerShoupCiphertext) throws CramerShoupCiphertextException {
        if (this.key.isPrivate() && !this.forEncryption) {
            CramerShoupKeyParameters cramerShoupKeyParameters = this.key;
            if (cramerShoupKeyParameters instanceof CramerShoupPrivateKeyParameters) {
                CramerShoupPrivateKeyParameters cramerShoupPrivateKeyParameters = (CramerShoupPrivateKeyParameters) cramerShoupKeyParameters;
                BigInteger p = cramerShoupPrivateKeyParameters.getParameters().getP();
                Digest h = cramerShoupPrivateKeyParameters.getParameters().getH();
                byte[] byteArray = cramerShoupCiphertext.getU1().toByteArray();
                h.update(byteArray, 0, byteArray.length);
                byte[] byteArray2 = cramerShoupCiphertext.getU2().toByteArray();
                h.update(byteArray2, 0, byteArray2.length);
                byte[] byteArray3 = cramerShoupCiphertext.getE().toByteArray();
                h.update(byteArray3, 0, byteArray3.length);
                byte[] bArr = this.label;
                if (bArr != null) {
                    h.update(bArr, 0, bArr.length);
                }
                byte[] bArr2 = new byte[h.getDigestSize()];
                h.doFinal(bArr2, 0);
                BigInteger bigInteger = new BigInteger(1, bArr2);
                if (cramerShoupCiphertext.v.equals(cramerShoupCiphertext.u1.modPow(cramerShoupPrivateKeyParameters.getX1().add(cramerShoupPrivateKeyParameters.getY1().multiply(bigInteger)), p).multiply(cramerShoupCiphertext.u2.modPow(cramerShoupPrivateKeyParameters.getX2().add(cramerShoupPrivateKeyParameters.getY2().multiply(bigInteger)), p)).mod(p))) {
                    return cramerShoupCiphertext.e.multiply(cramerShoupCiphertext.u1.modPow(cramerShoupPrivateKeyParameters.getZ(), p).modInverse(p)).mod(p);
                }
                throw new CramerShoupCiphertextException("Sorry, that ciphertext is not correct");
            }
        }
        return null;
    }

    public CramerShoupCiphertext encryptBlock(BigInteger bigInteger) {
        if (this.key.isPrivate() || !this.forEncryption) {
            return null;
        }
        CramerShoupKeyParameters cramerShoupKeyParameters = this.key;
        if (!(cramerShoupKeyParameters instanceof CramerShoupPublicKeyParameters)) {
            return null;
        }
        CramerShoupPublicKeyParameters cramerShoupPublicKeyParameters = (CramerShoupPublicKeyParameters) cramerShoupKeyParameters;
        BigInteger p = cramerShoupPublicKeyParameters.getParameters().getP();
        BigInteger g1 = cramerShoupPublicKeyParameters.getParameters().getG1();
        BigInteger g2 = cramerShoupPublicKeyParameters.getParameters().getG2();
        BigInteger h = cramerShoupPublicKeyParameters.getH();
        if (!isValidMessage(bigInteger, p)) {
            return null;
        }
        BigInteger generateRandomElement = generateRandomElement(p, this.random);
        BigInteger modPow = g1.modPow(generateRandomElement, p);
        BigInteger modPow2 = g2.modPow(generateRandomElement, p);
        BigInteger mod = h.modPow(generateRandomElement, p).multiply(bigInteger).mod(p);
        Digest h2 = cramerShoupPublicKeyParameters.getParameters().getH();
        byte[] byteArray = modPow.toByteArray();
        h2.update(byteArray, 0, byteArray.length);
        byte[] byteArray2 = modPow2.toByteArray();
        h2.update(byteArray2, 0, byteArray2.length);
        byte[] byteArray3 = mod.toByteArray();
        h2.update(byteArray3, 0, byteArray3.length);
        byte[] bArr = this.label;
        if (bArr != null) {
            h2.update(bArr, 0, bArr.length);
        }
        byte[] bArr2 = new byte[h2.getDigestSize()];
        h2.doFinal(bArr2, 0);
        return new CramerShoupCiphertext(modPow, modPow2, mod, cramerShoupPublicKeyParameters.getC().modPow(generateRandomElement, p).multiply(cramerShoupPublicKeyParameters.getD().modPow(generateRandomElement.multiply(new BigInteger(1, bArr2)), p)).mod(p));
    }

    public int getInputBlockSize() {
        int bitLength = (this.key.getParameters().getP().bitLength() + 7) / 8;
        return this.forEncryption ? bitLength - 1 : bitLength;
    }

    public int getOutputBlockSize() {
        int bitLength = (this.key.getParameters().getP().bitLength() + 7) / 8;
        return this.forEncryption ? bitLength : bitLength - 1;
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        SecureRandom secureRandom;
        if (cipherParameters instanceof ParametersWithRandom) {
            ParametersWithRandom parametersWithRandom = (ParametersWithRandom) cipherParameters;
            this.key = (CramerShoupKeyParameters) parametersWithRandom.getParameters();
            secureRandom = parametersWithRandom.getRandom();
        } else {
            this.key = (CramerShoupKeyParameters) cipherParameters;
            secureRandom = null;
        }
        this.random = initSecureRandom(z, secureRandom);
        this.forEncryption = z;
    }

    public void init(boolean z, CipherParameters cipherParameters, String str) {
        init(z, cipherParameters);
        this.label = Strings.toUTF8ByteArray(str);
    }

    /* access modifiers changed from: protected */
    public SecureRandom initSecureRandom(boolean z, SecureRandom secureRandom) {
        if (!z) {
            return null;
        }
        return secureRandom != null ? secureRandom : CryptoServicesRegistrar.getSecureRandom();
    }
}
