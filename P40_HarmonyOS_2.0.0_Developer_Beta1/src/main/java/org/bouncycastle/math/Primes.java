package org.bouncycastle.math;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.asn1.cmc.BodyPartID;
import org.bouncycastle.asn1.eac.CertificateBody;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;

public abstract class Primes {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    public static final int SMALL_FACTOR_LIMIT = 211;
    private static final BigInteger THREE = BigInteger.valueOf(3);
    private static final BigInteger TWO = BigInteger.valueOf(2);

    public static class MROutput {
        private BigInteger factor;
        private boolean provablyComposite;

        private MROutput(boolean z, BigInteger bigInteger) {
            this.provablyComposite = z;
            this.factor = bigInteger;
        }

        /* access modifiers changed from: private */
        public static MROutput probablyPrime() {
            return new MROutput(false, null);
        }

        /* access modifiers changed from: private */
        public static MROutput provablyCompositeNotPrimePower() {
            return new MROutput(true, null);
        }

        /* access modifiers changed from: private */
        public static MROutput provablyCompositeWithFactor(BigInteger bigInteger) {
            return new MROutput(true, bigInteger);
        }

        public BigInteger getFactor() {
            return this.factor;
        }

        public boolean isNotPrimePower() {
            return this.provablyComposite && this.factor == null;
        }

        public boolean isProvablyComposite() {
            return this.provablyComposite;
        }
    }

    public static class STOutput {
        private BigInteger prime;
        private int primeGenCounter;
        private byte[] primeSeed;

        private STOutput(BigInteger bigInteger, byte[] bArr, int i) {
            this.prime = bigInteger;
            this.primeSeed = bArr;
            this.primeGenCounter = i;
        }

        public BigInteger getPrime() {
            return this.prime;
        }

        public int getPrimeGenCounter() {
            return this.primeGenCounter;
        }

        public byte[] getPrimeSeed() {
            return this.primeSeed;
        }
    }

    private static void checkCandidate(BigInteger bigInteger, String str) {
        if (bigInteger == null || bigInteger.signum() < 1 || bigInteger.bitLength() < 2) {
            throw new IllegalArgumentException("'" + str + "' must be non-null and >= 2");
        }
    }

    public static MROutput enhancedMRProbablePrimeTest(BigInteger bigInteger, SecureRandom secureRandom, int i) {
        boolean z;
        BigInteger bigInteger2;
        checkCandidate(bigInteger, "candidate");
        if (secureRandom == null) {
            throw new IllegalArgumentException("'random' cannot be null");
        } else if (i < 1) {
            throw new IllegalArgumentException("'iterations' must be > 0");
        } else if (bigInteger.bitLength() == 2) {
            return MROutput.probablyPrime();
        } else {
            if (!bigInteger.testBit(0)) {
                return MROutput.provablyCompositeWithFactor(TWO);
            }
            BigInteger subtract = bigInteger.subtract(ONE);
            BigInteger subtract2 = bigInteger.subtract(TWO);
            int lowestSetBit = subtract.getLowestSetBit();
            BigInteger shiftRight = subtract.shiftRight(lowestSetBit);
            for (int i2 = 0; i2 < i; i2++) {
                BigInteger createRandomInRange = BigIntegers.createRandomInRange(TWO, subtract2, secureRandom);
                BigInteger gcd = createRandomInRange.gcd(bigInteger);
                if (gcd.compareTo(ONE) > 0) {
                    return MROutput.provablyCompositeWithFactor(gcd);
                }
                BigInteger modPow = createRandomInRange.modPow(shiftRight, bigInteger);
                if (!modPow.equals(ONE) && !modPow.equals(subtract)) {
                    BigInteger bigInteger3 = modPow;
                    int i3 = 1;
                    while (true) {
                        if (i3 >= lowestSetBit) {
                            z = false;
                            bigInteger2 = bigInteger3;
                            break;
                        }
                        bigInteger2 = bigInteger3.modPow(TWO, bigInteger);
                        if (bigInteger2.equals(subtract)) {
                            z = true;
                            break;
                        } else if (bigInteger2.equals(ONE)) {
                            z = false;
                            break;
                        } else {
                            i3++;
                            bigInteger3 = bigInteger2;
                        }
                    }
                    if (!z) {
                        if (!bigInteger2.equals(ONE)) {
                            bigInteger3 = bigInteger2.modPow(TWO, bigInteger);
                            if (bigInteger3.equals(ONE)) {
                                bigInteger3 = bigInteger2;
                            }
                        }
                        BigInteger gcd2 = bigInteger3.subtract(ONE).gcd(bigInteger);
                        return gcd2.compareTo(ONE) > 0 ? MROutput.provablyCompositeWithFactor(gcd2) : MROutput.provablyCompositeNotPrimePower();
                    }
                }
            }
            return MROutput.probablyPrime();
        }
    }

    private static int extract32(byte[] bArr) {
        int min = Math.min(4, bArr.length);
        int i = 0;
        int i2 = 0;
        while (i < min) {
            int i3 = i + 1;
            i2 |= (bArr[bArr.length - i3] & 255) << (i * 8);
            i = i3;
        }
        return i2;
    }

    public static STOutput generateSTRandomPrime(Digest digest, int i, byte[] bArr) {
        if (digest == null) {
            throw new IllegalArgumentException("'hash' cannot be null");
        } else if (i < 2) {
            throw new IllegalArgumentException("'length' must be >= 2");
        } else if (bArr != null && bArr.length != 0) {
            return implSTRandomPrime(digest, i, Arrays.clone(bArr));
        } else {
            throw new IllegalArgumentException("'inputSeed' cannot be null or empty");
        }
    }

    public static boolean hasAnySmallFactors(BigInteger bigInteger) {
        checkCandidate(bigInteger, "candidate");
        return implHasAnySmallFactors(bigInteger);
    }

    private static void hash(Digest digest, byte[] bArr, byte[] bArr2, int i) {
        digest.update(bArr, 0, bArr.length);
        digest.doFinal(bArr2, i);
    }

    private static BigInteger hashGen(Digest digest, byte[] bArr, int i) {
        int digestSize = digest.getDigestSize();
        int i2 = i * digestSize;
        byte[] bArr2 = new byte[i2];
        for (int i3 = 0; i3 < i; i3++) {
            i2 -= digestSize;
            hash(digest, bArr, bArr2, i2);
            inc(bArr, 1);
        }
        return new BigInteger(1, bArr2);
    }

    private static boolean implHasAnySmallFactors(BigInteger bigInteger) {
        int intValue = bigInteger.mod(BigInteger.valueOf((long) 223092870)).intValue();
        if (!(intValue % 2 == 0 || intValue % 3 == 0 || intValue % 5 == 0 || intValue % 7 == 0 || intValue % 11 == 0 || intValue % 13 == 0 || intValue % 17 == 0 || intValue % 19 == 0 || intValue % 23 == 0)) {
            int intValue2 = bigInteger.mod(BigInteger.valueOf((long) 58642669)).intValue();
            if (!(intValue2 % 29 == 0 || intValue2 % 31 == 0 || intValue2 % 37 == 0 || intValue2 % 41 == 0 || intValue2 % 43 == 0)) {
                int intValue3 = bigInteger.mod(BigInteger.valueOf((long) 600662303)).intValue();
                if (!(intValue3 % 47 == 0 || intValue3 % 53 == 0 || intValue3 % 59 == 0 || intValue3 % 61 == 0 || intValue3 % 67 == 0)) {
                    int intValue4 = bigInteger.mod(BigInteger.valueOf((long) 33984931)).intValue();
                    if (!(intValue4 % 71 == 0 || intValue4 % 73 == 0 || intValue4 % 79 == 0 || intValue4 % 83 == 0)) {
                        int intValue5 = bigInteger.mod(BigInteger.valueOf((long) 89809099)).intValue();
                        if (!(intValue5 % 89 == 0 || intValue5 % 97 == 0 || intValue5 % 101 == 0 || intValue5 % 103 == 0)) {
                            int intValue6 = bigInteger.mod(BigInteger.valueOf((long) 167375713)).intValue();
                            if (!(intValue6 % 107 == 0 || intValue6 % 109 == 0 || intValue6 % 113 == 0 || intValue6 % CertificateBody.profileType == 0)) {
                                int intValue7 = bigInteger.mod(BigInteger.valueOf((long) 371700317)).intValue();
                                if (!(intValue7 % 131 == 0 || intValue7 % 137 == 0 || intValue7 % 139 == 0 || intValue7 % 149 == 0)) {
                                    int intValue8 = bigInteger.mod(BigInteger.valueOf((long) 645328247)).intValue();
                                    if (!(intValue8 % 151 == 0 || intValue8 % 157 == 0 || intValue8 % 163 == 0 || intValue8 % 167 == 0)) {
                                        int intValue9 = bigInteger.mod(BigInteger.valueOf((long) 1070560157)).intValue();
                                        if (!(intValue9 % 173 == 0 || intValue9 % 179 == 0 || intValue9 % 181 == 0 || intValue9 % 191 == 0)) {
                                            int intValue10 = bigInteger.mod(BigInteger.valueOf((long) 1596463769)).intValue();
                                            if (intValue10 % 193 != 0 && intValue10 % 197 != 0 && intValue10 % 199 != 0 && intValue10 % SMALL_FACTOR_LIMIT != 0) {
                                                return false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean implMRProbablePrimeToBase(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, int i, BigInteger bigInteger4) {
        BigInteger modPow = bigInteger4.modPow(bigInteger3, bigInteger);
        if (modPow.equals(ONE) || modPow.equals(bigInteger2)) {
            return true;
        }
        BigInteger bigInteger5 = modPow;
        for (int i2 = 1; i2 < i; i2++) {
            bigInteger5 = bigInteger5.modPow(TWO, bigInteger);
            if (bigInteger5.equals(bigInteger2)) {
                return true;
            }
            if (bigInteger5.equals(ONE)) {
                return false;
            }
        }
        return false;
    }

    private static STOutput implSTRandomPrime(Digest digest, int i, byte[] bArr) {
        Object obj;
        int digestSize = digest.getDigestSize();
        Object obj2 = null;
        int i2 = 1;
        if (i < 33) {
            byte[] bArr2 = new byte[digestSize];
            byte[] bArr3 = new byte[digestSize];
            int i3 = 0;
            do {
                hash(digest, bArr, bArr2, 0);
                inc(bArr, 1);
                hash(digest, bArr, bArr3, 0);
                inc(bArr, 1);
                i3++;
                long extract32 = ((long) (((extract32(bArr2) ^ extract32(bArr3)) & (-1 >>> (32 - i))) | (1 << (i - 1)) | 1)) & BodyPartID.bodyIdMax;
                if (isPrime32(extract32)) {
                    return new STOutput(BigInteger.valueOf(extract32), bArr, i3);
                }
            } while (i3 <= i * 4);
            throw new IllegalStateException("Too many iterations in Shawe-Taylor Random_Prime Routine");
        }
        STOutput implSTRandomPrime = implSTRandomPrime(digest, (i + 3) / 2, bArr);
        BigInteger prime = implSTRandomPrime.getPrime();
        byte[] primeSeed = implSTRandomPrime.getPrimeSeed();
        int primeGenCounter = implSTRandomPrime.getPrimeGenCounter();
        int i4 = i - 1;
        int i5 = (i4 / (digestSize * 8)) + 1;
        BigInteger bit = hashGen(digest, primeSeed, i5).mod(ONE.shiftLeft(i4)).setBit(i4);
        BigInteger shiftLeft = prime.shiftLeft(1);
        BigInteger shiftLeft2 = bit.subtract(ONE).divide(shiftLeft).add(ONE).shiftLeft(1);
        BigInteger add = shiftLeft2.multiply(prime).add(ONE);
        int i6 = 0;
        BigInteger bigInteger = shiftLeft2;
        int i7 = primeGenCounter;
        while (true) {
            if (add.bitLength() > i) {
                bigInteger = ONE.shiftLeft(i4).subtract(ONE).divide(shiftLeft).add(ONE).shiftLeft(i2);
                add = bigInteger.multiply(prime).add(ONE);
            }
            i7 += i2;
            if (!implHasAnySmallFactors(add)) {
                BigInteger add2 = hashGen(digest, primeSeed, i5).mod(add.subtract(THREE)).add(TWO);
                BigInteger add3 = bigInteger.add(BigInteger.valueOf((long) i6));
                BigInteger modPow = add2.modPow(add3, add);
                if (add.gcd(modPow.subtract(ONE)).equals(ONE) && modPow.modPow(prime, add).equals(ONE)) {
                    return new STOutput(add, primeSeed, i7);
                }
                obj = null;
                bigInteger = add3;
                i6 = 0;
            } else {
                obj = obj2;
                inc(primeSeed, i5);
            }
            if (i7 < (i * 4) + primeGenCounter) {
                i6 += 2;
                add = add.add(shiftLeft);
                obj2 = obj;
                i2 = 1;
            } else {
                throw new IllegalStateException("Too many iterations in Shawe-Taylor Random_Prime Routine");
            }
        }
    }

    private static void inc(byte[] bArr, int i) {
        int length = bArr.length;
        while (i > 0) {
            length--;
            if (length >= 0) {
                int i2 = i + (bArr[length] & 255);
                bArr[length] = (byte) i2;
                i = i2 >>> 8;
            } else {
                return;
            }
        }
    }

    public static boolean isMRProbablePrime(BigInteger bigInteger, SecureRandom secureRandom, int i) {
        checkCandidate(bigInteger, "candidate");
        if (secureRandom == null) {
            throw new IllegalArgumentException("'random' cannot be null");
        } else if (i < 1) {
            throw new IllegalArgumentException("'iterations' must be > 0");
        } else if (bigInteger.bitLength() == 2) {
            return true;
        } else {
            if (!bigInteger.testBit(0)) {
                return false;
            }
            BigInteger subtract = bigInteger.subtract(ONE);
            BigInteger subtract2 = bigInteger.subtract(TWO);
            int lowestSetBit = subtract.getLowestSetBit();
            BigInteger shiftRight = subtract.shiftRight(lowestSetBit);
            for (int i2 = 0; i2 < i; i2++) {
                if (!implMRProbablePrimeToBase(bigInteger, subtract, shiftRight, lowestSetBit, BigIntegers.createRandomInRange(TWO, subtract2, secureRandom))) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean isMRProbablePrimeToBase(BigInteger bigInteger, BigInteger bigInteger2) {
        checkCandidate(bigInteger, "candidate");
        checkCandidate(bigInteger2, "base");
        if (bigInteger2.compareTo(bigInteger.subtract(ONE)) >= 0) {
            throw new IllegalArgumentException("'base' must be < ('candidate' - 1)");
        } else if (bigInteger.bitLength() == 2) {
            return true;
        } else {
            BigInteger subtract = bigInteger.subtract(ONE);
            int lowestSetBit = subtract.getLowestSetBit();
            return implMRProbablePrimeToBase(bigInteger, subtract, subtract.shiftRight(lowestSetBit), lowestSetBit, bigInteger2);
        }
    }

    private static boolean isPrime32(long j) {
        if ((j >>> 32) == 0) {
            int i = (j > 5 ? 1 : (j == 5 ? 0 : -1));
            if (i <= 0) {
                return j == 2 || j == 3 || i == 0;
            }
            if ((1 & j) == 0 || j % 3 == 0 || j % 5 == 0) {
                return false;
            }
            long[] jArr = {1, 7, 11, 13, 17, 19, 23, 29};
            long j2 = 0;
            int i2 = 1;
            while (true) {
                if (i2 >= jArr.length) {
                    j2 += 30;
                    if (j2 * j2 >= j) {
                        return true;
                    }
                    i2 = 0;
                } else if (j % (jArr[i2] + j2) == 0) {
                    return j < 30;
                } else {
                    i2++;
                }
            }
        } else {
            throw new IllegalArgumentException("Size limit exceeded");
        }
    }
}
