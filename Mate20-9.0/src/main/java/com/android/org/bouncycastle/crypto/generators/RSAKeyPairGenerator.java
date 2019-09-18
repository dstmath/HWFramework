package com.android.org.bouncycastle.crypto.generators;

import com.android.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import com.android.org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import com.android.org.bouncycastle.crypto.KeyGenerationParameters;
import com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import com.android.org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import com.android.org.bouncycastle.crypto.params.RSAKeyParameters;
import com.android.org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import com.android.org.bouncycastle.math.Primes;
import com.android.org.bouncycastle.math.ec.WNafUtil;
import java.math.BigInteger;

public class RSAKeyPairGenerator implements AsymmetricCipherKeyPairGenerator {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private RSAKeyGenerationParameters param;

    public void init(KeyGenerationParameters param2) {
        this.param = (RSAKeyGenerationParameters) param2;
    }

    public AsymmetricCipherKeyPair generateKeyPair() {
        BigInteger q;
        boolean done;
        BigInteger n;
        boolean done2;
        BigInteger q2;
        BigInteger q3;
        RSAKeyPairGenerator rSAKeyPairGenerator = this;
        AsymmetricCipherKeyPair result = null;
        boolean done3 = false;
        int strength = rSAKeyPairGenerator.param.getStrength();
        int pbitlength = (strength + 1) / 2;
        int qbitlength = strength - pbitlength;
        int mindiffbits = (strength / 2) - 100;
        if (mindiffbits < strength / 3) {
            mindiffbits = strength / 3;
        }
        int minWeight = strength >> 2;
        BigInteger dLowerBound = BigInteger.valueOf(2).pow(strength / 2);
        BigInteger squaredBound = ONE.shiftLeft(strength - 1);
        BigInteger minDiff = ONE.shiftLeft(mindiffbits);
        while (!done3) {
            BigInteger e = rSAKeyPairGenerator.param.getPublicExponent();
            BigInteger p = rSAKeyPairGenerator.chooseRandomPrime(pbitlength, e, squaredBound);
            while (true) {
                q = rSAKeyPairGenerator.chooseRandomPrime(qbitlength, e, squaredBound);
                BigInteger diff = q.subtract(p).abs();
                if (diff.bitLength() < mindiffbits || diff.compareTo(minDiff) <= 0) {
                    done = done3;
                    strength = strength;
                    pbitlength = pbitlength;
                    qbitlength = qbitlength;
                    rSAKeyPairGenerator = this;
                } else {
                    n = p.multiply(q);
                    done2 = done3;
                    if (n.bitLength() == strength) {
                        if (WNafUtil.getNafWeight(n) >= minWeight) {
                            break;
                        }
                        p = rSAKeyPairGenerator.chooseRandomPrime(pbitlength, e, squaredBound);
                    } else {
                        p = p.max(q);
                    }
                    done = done2;
                }
            }
            if (p.compareTo(q) < 0) {
                q2 = p;
                q3 = q;
            } else {
                q3 = p;
                q2 = q;
            }
            BigInteger pSub1 = q3.subtract(ONE);
            BigInteger qSub1 = q2.subtract(ONE);
            BigInteger gcd = pSub1.gcd(qSub1);
            int strength2 = strength;
            BigInteger lcm = pSub1.divide(gcd).multiply(qSub1);
            BigInteger bigInteger = gcd;
            BigInteger gcd2 = e.modInverse(lcm);
            if (gcd2.compareTo(dLowerBound) <= 0) {
                done3 = done2;
                strength = strength2;
            } else {
                BigInteger dP = gcd2.remainder(pSub1);
                BigInteger dQ = gcd2.remainder(qSub1);
                BigInteger qInv = q2.modInverse(q3);
                BigInteger bigInteger2 = lcm;
                int pbitlength2 = pbitlength;
                RSAKeyParameters rSAKeyParameters = new RSAKeyParameters(false, n, e);
                BigInteger bigInteger3 = qSub1;
                BigInteger bigInteger4 = pSub1;
                BigInteger bigInteger5 = n;
                RSAPrivateCrtKeyParameters rSAPrivateCrtKeyParameters = new RSAPrivateCrtKeyParameters(n, e, gcd2, q3, q2, dP, dQ, qInv);
                result = new AsymmetricCipherKeyPair((AsymmetricKeyParameter) rSAKeyParameters, (AsymmetricKeyParameter) rSAPrivateCrtKeyParameters);
                done3 = true;
                strength = strength2;
                pbitlength = pbitlength2;
                qbitlength = qbitlength;
            }
            rSAKeyPairGenerator = this;
        }
        int i = strength;
        int i2 = pbitlength;
        int i3 = qbitlength;
        return result;
    }

    /* access modifiers changed from: protected */
    public BigInteger chooseRandomPrime(int bitlength, BigInteger e, BigInteger sqrdBound) {
        for (int i = 0; i != 5 * bitlength; i++) {
            BigInteger p = new BigInteger(bitlength, 1, this.param.getRandom());
            if (!p.mod(e).equals(ONE) && p.multiply(p).compareTo(sqrdBound) >= 0 && isProbablePrime(p) && e.gcd(p.subtract(ONE)).equals(ONE)) {
                return p;
            }
        }
        throw new IllegalStateException("unable to generate prime number for RSA key");
    }

    /* access modifiers changed from: protected */
    public boolean isProbablePrime(BigInteger x) {
        return !Primes.hasAnySmallFactors(x) && Primes.isMRProbablePrime(x, this.param.getRandom(), getNumberOfIterations(x.bitLength(), this.param.getCertainty()));
    }

    private static int getNumberOfIterations(int bits, int certainty) {
        int i = 4;
        if (bits >= 1536) {
            if (certainty <= 100) {
                i = 3;
            } else if (certainty > 128) {
                i = 4 + (((certainty - 128) + 1) / 2);
            }
            return i;
        }
        int i2 = 5;
        if (bits >= 1024) {
            if (certainty > 100) {
                if (certainty <= 112) {
                    i = 5;
                } else {
                    i = (((certainty - 112) + 1) / 2) + 5;
                }
            }
            return i;
        } else if (bits >= 512) {
            if (certainty > 80) {
                if (certainty <= 100) {
                    i2 = 7;
                } else {
                    i2 = 7 + (((certainty - 100) + 1) / 2);
                }
            }
            return i2;
        } else {
            int i3 = 40;
            if (certainty > 80) {
                i3 = 40 + (((certainty - 80) + 1) / 2);
            }
            return i3;
        }
    }
}
