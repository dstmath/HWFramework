package org.bouncycastle.crypto.agreement.srp;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.util.BigIntegers;

public class SRP6Util {
    private static BigInteger ONE = BigInteger.valueOf(1);
    private static BigInteger ZERO = BigInteger.valueOf(0);

    public static BigInteger calculateK(Digest digest, BigInteger bigInteger, BigInteger bigInteger2) {
        return hashPaddedPair(digest, bigInteger, bigInteger, bigInteger2);
    }

    public static BigInteger calculateKey(Digest digest, BigInteger bigInteger, BigInteger bigInteger2) {
        byte[] padded = getPadded(bigInteger2, (bigInteger.bitLength() + 7) / 8);
        digest.update(padded, 0, padded.length);
        byte[] bArr = new byte[digest.getDigestSize()];
        digest.doFinal(bArr, 0);
        return new BigInteger(1, bArr);
    }

    public static BigInteger calculateM1(Digest digest, BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4) {
        return hashPaddedTriplet(digest, bigInteger, bigInteger2, bigInteger3, bigInteger4);
    }

    public static BigInteger calculateM2(Digest digest, BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4) {
        return hashPaddedTriplet(digest, bigInteger, bigInteger2, bigInteger3, bigInteger4);
    }

    public static BigInteger calculateU(Digest digest, BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3) {
        return hashPaddedPair(digest, bigInteger, bigInteger2, bigInteger3);
    }

    public static BigInteger calculateX(Digest digest, BigInteger bigInteger, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        byte[] bArr4 = new byte[digest.getDigestSize()];
        digest.update(bArr2, 0, bArr2.length);
        digest.update((byte) 58);
        digest.update(bArr3, 0, bArr3.length);
        digest.doFinal(bArr4, 0);
        digest.update(bArr, 0, bArr.length);
        digest.update(bArr4, 0, bArr4.length);
        digest.doFinal(bArr4, 0);
        return new BigInteger(1, bArr4);
    }

    public static BigInteger generatePrivateValue(Digest digest, BigInteger bigInteger, BigInteger bigInteger2, SecureRandom secureRandom) {
        return BigIntegers.createRandomInRange(ONE.shiftLeft(Math.min(256, bigInteger.bitLength() / 2) - 1), bigInteger.subtract(ONE), secureRandom);
    }

    private static byte[] getPadded(BigInteger bigInteger, int i) {
        byte[] asUnsignedByteArray = BigIntegers.asUnsignedByteArray(bigInteger);
        if (asUnsignedByteArray.length >= i) {
            return asUnsignedByteArray;
        }
        byte[] bArr = new byte[i];
        System.arraycopy(asUnsignedByteArray, 0, bArr, i - asUnsignedByteArray.length, asUnsignedByteArray.length);
        return bArr;
    }

    private static BigInteger hashPaddedPair(Digest digest, BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3) {
        int bitLength = (bigInteger.bitLength() + 7) / 8;
        byte[] padded = getPadded(bigInteger2, bitLength);
        byte[] padded2 = getPadded(bigInteger3, bitLength);
        digest.update(padded, 0, padded.length);
        digest.update(padded2, 0, padded2.length);
        byte[] bArr = new byte[digest.getDigestSize()];
        digest.doFinal(bArr, 0);
        return new BigInteger(1, bArr);
    }

    private static BigInteger hashPaddedTriplet(Digest digest, BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4) {
        int bitLength = (bigInteger.bitLength() + 7) / 8;
        byte[] padded = getPadded(bigInteger2, bitLength);
        byte[] padded2 = getPadded(bigInteger3, bitLength);
        byte[] padded3 = getPadded(bigInteger4, bitLength);
        digest.update(padded, 0, padded.length);
        digest.update(padded2, 0, padded2.length);
        digest.update(padded3, 0, padded3.length);
        byte[] bArr = new byte[digest.getDigestSize()];
        digest.doFinal(bArr, 0);
        return new BigInteger(1, bArr);
    }

    public static BigInteger validatePublicValue(BigInteger bigInteger, BigInteger bigInteger2) throws CryptoException {
        BigInteger mod = bigInteger2.mod(bigInteger);
        if (!mod.equals(ZERO)) {
            return mod;
        }
        throw new CryptoException("Invalid public value: 0");
    }
}
