package com.android.org.bouncycastle.crypto.generators;

import com.android.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import com.android.org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import com.android.org.bouncycastle.crypto.KeyGenerationParameters;
import com.android.org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import com.android.org.bouncycastle.crypto.params.RSAKeyParameters;
import com.android.org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import com.android.org.bouncycastle.math.Primes;
import com.android.org.bouncycastle.math.ec.WNafUtil;
import java.math.BigInteger;

public class RSAKeyPairGenerator implements AsymmetricCipherKeyPairGenerator {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private RSAKeyGenerationParameters param;

    public void init(KeyGenerationParameters param) {
        this.param = (RSAKeyGenerationParameters) param;
    }

    public AsymmetricCipherKeyPair generateKeyPair() {
        AsymmetricCipherKeyPair result = null;
        boolean done = false;
        int strength = this.param.getStrength();
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
        while (!done) {
            BigInteger q;
            BigInteger n;
            BigInteger e = this.param.getPublicExponent();
            BigInteger p = chooseRandomPrime(pbitlength, e, squaredBound);
            while (true) {
                q = chooseRandomPrime(qbitlength, e, squaredBound);
                BigInteger diff = q.subtract(p).abs();
                if (diff.bitLength() >= mindiffbits && diff.compareTo(minDiff) > 0) {
                    n = p.multiply(q);
                    if (n.bitLength() == strength) {
                        if (WNafUtil.getNafWeight(n) >= minWeight) {
                            break;
                        }
                        p = chooseRandomPrime(pbitlength, e, squaredBound);
                    } else {
                        p = p.max(q);
                    }
                }
            }
            if (p.compareTo(q) < 0) {
                BigInteger gcd = p;
                p = q;
                q = gcd;
            }
            BigInteger pSub1 = p.subtract(ONE);
            BigInteger qSub1 = q.subtract(ONE);
            BigInteger d = e.modInverse(pSub1.divide(pSub1.gcd(qSub1)).multiply(qSub1));
            if (d.compareTo(dLowerBound) > 0) {
                done = true;
                BigInteger dP = d.remainder(pSub1);
                BigInteger dQ = d.remainder(qSub1);
                BigInteger qInv = q.modInverse(p);
                AsymmetricCipherKeyPair asymmetricCipherKeyPair = new AsymmetricCipherKeyPair(new RSAKeyParameters(false, n, e), new RSAPrivateCrtKeyParameters(n, e, d, p, q, dP, dQ, qInv));
            }
        }
        return result;
    }

    protected BigInteger chooseRandomPrime(int bitlength, BigInteger e, BigInteger sqrdBound) {
        for (int i = 0; i != bitlength * 5; i++) {
            BigInteger p = new BigInteger(bitlength, 1, this.param.getRandom());
            if (!p.mod(e).equals(ONE) && p.multiply(p).compareTo(sqrdBound) >= 0 && isProbablePrime(p) && e.gcd(p.subtract(ONE)).equals(ONE)) {
                return p;
            }
        }
        throw new IllegalStateException("unable to generate prime number for RSA key");
    }

    protected boolean isProbablePrime(BigInteger x) {
        return !Primes.hasAnySmallFactors(x) ? Primes.isMRProbablePrime(x, this.param.getRandom(), getNumberOfIterations(x.bitLength(), this.param.getCertainty())) : false;
    }

    private static int getNumberOfIterations(int bits, int certainty) {
        int i = 5;
        int i2 = 4;
        if (bits >= 1536) {
            if (certainty <= 100) {
                i2 = 3;
            } else if (certainty > 128) {
                i2 = (((certainty - 128) + 1) / 2) + 4;
            }
            return i2;
        } else if (bits >= 1024) {
            if (certainty > 100) {
                if (certainty <= 112) {
                    i2 = 5;
                } else {
                    i2 = (((certainty - 112) + 1) / 2) + 5;
                }
            }
            return i2;
        } else if (bits >= 512) {
            if (certainty > 80) {
                if (certainty <= 100) {
                    i = 7;
                } else {
                    i = (((certainty - 100) + 1) / 2) + 7;
                }
            }
            return i;
        } else {
            if (certainty <= 80) {
                i2 = 40;
            } else {
                i2 = (((certainty - 80) + 1) / 2) + 40;
            }
            return i2;
        }
    }
}
