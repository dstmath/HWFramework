package org.bouncycastle.crypto.signers;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.DSA;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECMultiplier;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.util.Arrays;

public class DSTU4145Signer implements DSA {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private ECKeyParameters key;
    private SecureRandom random;

    private static BigInteger fieldElement2Integer(BigInteger bigInteger, ECFieldElement eCFieldElement) {
        return truncate(eCFieldElement.toBigInteger(), bigInteger.bitLength() - 1);
    }

    private static BigInteger generateRandomInteger(BigInteger bigInteger, SecureRandom secureRandom) {
        return new BigInteger(bigInteger.bitLength() - 1, secureRandom);
    }

    private static ECFieldElement hash2FieldElement(ECCurve eCCurve, byte[] bArr) {
        return eCCurve.fromBigInteger(truncate(new BigInteger(1, Arrays.reverse(bArr)), eCCurve.getFieldSize()));
    }

    private static BigInteger truncate(BigInteger bigInteger, int i) {
        return bigInteger.bitLength() > i ? bigInteger.mod(ONE.shiftLeft(i)) : bigInteger;
    }

    /* access modifiers changed from: protected */
    public ECMultiplier createBasePointMultiplier() {
        return new FixedPointCombMultiplier();
    }

    public BigInteger[] generateSignature(byte[] bArr) {
        ECDomainParameters parameters = this.key.getParameters();
        ECCurve curve = parameters.getCurve();
        ECFieldElement hash2FieldElement = hash2FieldElement(curve, bArr);
        if (hash2FieldElement.isZero()) {
            hash2FieldElement = curve.fromBigInteger(ONE);
        }
        BigInteger n = parameters.getN();
        BigInteger d = ((ECPrivateKeyParameters) this.key).getD();
        ECMultiplier createBasePointMultiplier = createBasePointMultiplier();
        while (true) {
            BigInteger generateRandomInteger = generateRandomInteger(n, this.random);
            ECFieldElement affineXCoord = createBasePointMultiplier.multiply(parameters.getG(), generateRandomInteger).normalize().getAffineXCoord();
            if (!affineXCoord.isZero()) {
                BigInteger fieldElement2Integer = fieldElement2Integer(n, hash2FieldElement.multiply(affineXCoord));
                if (fieldElement2Integer.signum() != 0) {
                    BigInteger mod = fieldElement2Integer.multiply(d).add(generateRandomInteger).mod(n);
                    if (mod.signum() != 0) {
                        return new BigInteger[]{fieldElement2Integer, mod};
                    }
                } else {
                    continue;
                }
            }
        }
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        ECKeyParameters eCKeyParameters;
        if (z) {
            if (cipherParameters instanceof ParametersWithRandom) {
                ParametersWithRandom parametersWithRandom = (ParametersWithRandom) cipherParameters;
                this.random = parametersWithRandom.getRandom();
                cipherParameters = parametersWithRandom.getParameters();
            } else {
                this.random = CryptoServicesRegistrar.getSecureRandom();
            }
            eCKeyParameters = (ECPrivateKeyParameters) cipherParameters;
        } else {
            eCKeyParameters = (ECPublicKeyParameters) cipherParameters;
        }
        this.key = eCKeyParameters;
    }

    public boolean verifySignature(byte[] bArr, BigInteger bigInteger, BigInteger bigInteger2) {
        boolean z = false;
        if (bigInteger.signum() > 0) {
            if (bigInteger2.signum() <= 0) {
                return false;
            }
            ECDomainParameters parameters = this.key.getParameters();
            BigInteger n = parameters.getN();
            if (bigInteger.compareTo(n) < 0) {
                if (bigInteger2.compareTo(n) >= 0) {
                    return false;
                }
                ECCurve curve = parameters.getCurve();
                ECFieldElement hash2FieldElement = hash2FieldElement(curve, bArr);
                if (hash2FieldElement.isZero()) {
                    hash2FieldElement = curve.fromBigInteger(ONE);
                }
                ECPoint normalize = ECAlgorithms.sumOfTwoMultiplies(parameters.getG(), bigInteger2, ((ECPublicKeyParameters) this.key).getQ(), bigInteger).normalize();
                if (normalize.isInfinity()) {
                    return false;
                }
                if (fieldElement2Integer(n, hash2FieldElement.multiply(normalize.getAffineXCoord())).compareTo(bigInteger) == 0) {
                    z = true;
                }
            }
        }
        return z;
    }
}
