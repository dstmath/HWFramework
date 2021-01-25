package org.bouncycastle.crypto.generators;

import java.math.BigInteger;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.math.Primes;
import org.bouncycastle.math.ec.WNafUtil;
import org.bouncycastle.util.BigIntegers;

public class RSAKeyPairGenerator implements AsymmetricCipherKeyPairGenerator {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private RSAKeyGenerationParameters param;

    private static int getNumberOfIterations(int i, int i2) {
        if (i >= 1536) {
            if (i2 <= 100) {
                return 3;
            }
            if (i2 <= 128) {
                return 4;
            }
            return 4 + (((i2 - 128) + 1) / 2);
        } else if (i >= 1024) {
            if (i2 <= 100) {
                return 4;
            }
            if (i2 <= 112) {
                return 5;
            }
            return (((i2 - 112) + 1) / 2) + 5;
        } else if (i >= 512) {
            if (i2 <= 80) {
                return 5;
            }
            if (i2 <= 100) {
                return 7;
            }
            return (((i2 - 100) + 1) / 2) + 7;
        } else if (i2 <= 80) {
            return 40;
        } else {
            return 40 + (((i2 - 80) + 1) / 2);
        }
    }

    /* access modifiers changed from: protected */
    public BigInteger chooseRandomPrime(int i, BigInteger bigInteger, BigInteger bigInteger2) {
        for (int i2 = 0; i2 != i * 5; i2++) {
            BigInteger createRandomPrime = BigIntegers.createRandomPrime(i, 1, this.param.getRandom());
            if (!createRandomPrime.mod(bigInteger).equals(ONE) && createRandomPrime.multiply(createRandomPrime).compareTo(bigInteger2) >= 0 && isProbablePrime(createRandomPrime) && bigInteger.gcd(createRandomPrime.subtract(ONE)).equals(ONE)) {
                return createRandomPrime;
            }
        }
        throw new IllegalStateException("unable to generate prime number for RSA key");
    }

    @Override // org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
    public AsymmetricCipherKeyPair generateKeyPair() {
        BigInteger chooseRandomPrime;
        BigInteger chooseRandomPrime2;
        BigInteger multiply;
        BigInteger bigInteger;
        RSAKeyPairGenerator rSAKeyPairGenerator = this;
        int strength = rSAKeyPairGenerator.param.getStrength();
        int i = (strength + 1) / 2;
        int i2 = strength - i;
        int i3 = strength / 2;
        int i4 = i3 - 100;
        int i5 = strength / 3;
        if (i4 < i5) {
            i4 = i5;
        }
        int i6 = strength >> 2;
        BigInteger pow = BigInteger.valueOf(2).pow(i3);
        BigInteger shiftLeft = ONE.shiftLeft(strength - 1);
        BigInteger shiftLeft2 = ONE.shiftLeft(i4);
        AsymmetricCipherKeyPair asymmetricCipherKeyPair = null;
        boolean z = false;
        while (!z) {
            BigInteger publicExponent = rSAKeyPairGenerator.param.getPublicExponent();
            do {
                chooseRandomPrime = rSAKeyPairGenerator.chooseRandomPrime(i, publicExponent, shiftLeft);
                while (true) {
                    chooseRandomPrime2 = rSAKeyPairGenerator.chooseRandomPrime(i2, publicExponent, shiftLeft);
                    BigInteger abs = chooseRandomPrime2.subtract(chooseRandomPrime).abs();
                    if (abs.bitLength() >= i4 && abs.compareTo(shiftLeft2) > 0) {
                        multiply = chooseRandomPrime.multiply(chooseRandomPrime2);
                        if (multiply.bitLength() == strength) {
                            break;
                        }
                        chooseRandomPrime = chooseRandomPrime.max(chooseRandomPrime2);
                    } else {
                        rSAKeyPairGenerator = this;
                        strength = strength;
                    }
                }
            } while (WNafUtil.getNafWeight(multiply) < i6);
            if (chooseRandomPrime.compareTo(chooseRandomPrime2) < 0) {
                bigInteger = chooseRandomPrime;
                chooseRandomPrime = chooseRandomPrime2;
            } else {
                bigInteger = chooseRandomPrime2;
            }
            BigInteger subtract = chooseRandomPrime.subtract(ONE);
            BigInteger subtract2 = bigInteger.subtract(ONE);
            BigInteger modInverse = publicExponent.modInverse(subtract.divide(subtract.gcd(subtract2)).multiply(subtract2));
            if (modInverse.compareTo(pow) <= 0) {
                rSAKeyPairGenerator = this;
                strength = strength;
            } else {
                asymmetricCipherKeyPair = new AsymmetricCipherKeyPair((AsymmetricKeyParameter) new RSAKeyParameters(false, multiply, publicExponent), (AsymmetricKeyParameter) new RSAPrivateCrtKeyParameters(multiply, publicExponent, modInverse, chooseRandomPrime, bigInteger, modInverse.remainder(subtract), modInverse.remainder(subtract2), bigInteger.modInverse(chooseRandomPrime)));
                z = true;
                strength = strength;
                rSAKeyPairGenerator = this;
            }
        }
        return asymmetricCipherKeyPair;
    }

    @Override // org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
    public void init(KeyGenerationParameters keyGenerationParameters) {
        this.param = (RSAKeyGenerationParameters) keyGenerationParameters;
    }

    /* access modifiers changed from: protected */
    public boolean isProbablePrime(BigInteger bigInteger) {
        return !Primes.hasAnySmallFactors(bigInteger) && Primes.isMRProbablePrime(bigInteger, this.param.getRandom(), getNumberOfIterations(bigInteger.bitLength(), this.param.getCertainty()));
    }
}
