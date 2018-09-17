package com.android.org.bouncycastle.crypto.generators;

import com.android.org.bouncycastle.asn1.x509.ReasonFlags;
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
    private static final BigInteger ONE = null;
    private int iterations;
    private RSAKeyGenerationParameters param;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.generators.RSAKeyPairGenerator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.crypto.generators.RSAKeyPairGenerator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.generators.RSAKeyPairGenerator.<clinit>():void");
    }

    public void init(KeyGenerationParameters param) {
        this.param = (RSAKeyGenerationParameters) param;
        this.iterations = getNumberOfIterations(this.param.getStrength(), this.param.getCertainty());
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
            BigInteger e = this.param.getPublicExponent();
            BigInteger p = chooseRandomPrime(pbitlength, e, squaredBound);
            while (true) {
                BigInteger n;
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
                AsymmetricKeyParameter rSAKeyParameters = new RSAKeyParameters(false, n, e);
                AsymmetricCipherKeyPair asymmetricCipherKeyPair = new AsymmetricCipherKeyPair(r0, new RSAPrivateCrtKeyParameters(n, e, d, p, q, dP, dQ, qInv));
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
        return !Primes.hasAnySmallFactors(x) ? Primes.isMRProbablePrime(x, this.param.getRandom(), this.iterations) : false;
    }

    private static int getNumberOfIterations(int bits, int certainty) {
        int i = 5;
        int i2 = 4;
        if (bits >= 1536) {
            if (certainty <= 100) {
                i2 = 3;
            } else if (certainty > ReasonFlags.unused) {
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
