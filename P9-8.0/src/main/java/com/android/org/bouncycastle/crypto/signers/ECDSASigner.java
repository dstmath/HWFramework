package com.android.org.bouncycastle.crypto.signers;

import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DSA;
import com.android.org.bouncycastle.crypto.params.ECDomainParameters;
import com.android.org.bouncycastle.crypto.params.ECKeyParameters;
import com.android.org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import com.android.org.bouncycastle.crypto.params.ECPublicKeyParameters;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import com.android.org.bouncycastle.math.ec.ECAlgorithms;
import com.android.org.bouncycastle.math.ec.ECConstants;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECFieldElement;
import com.android.org.bouncycastle.math.ec.ECMultiplier;
import com.android.org.bouncycastle.math.ec.ECPoint;
import com.android.org.bouncycastle.math.ec.FixedPointCombMultiplier;
import java.math.BigInteger;
import java.security.SecureRandom;

public class ECDSASigner implements ECConstants, DSA {
    private final DSAKCalculator kCalculator;
    private ECKeyParameters key;
    private SecureRandom random;

    public ECDSASigner() {
        this.kCalculator = new RandomDSAKCalculator();
    }

    public ECDSASigner(DSAKCalculator kCalculator) {
        this.kCalculator = kCalculator;
    }

    public void init(boolean forSigning, CipherParameters param) {
        SecureRandom providedRandom = null;
        if (!forSigning) {
            this.key = (ECPublicKeyParameters) param;
        } else if (param instanceof ParametersWithRandom) {
            ParametersWithRandom rParam = (ParametersWithRandom) param;
            this.key = (ECPrivateKeyParameters) rParam.getParameters();
            providedRandom = rParam.getRandom();
        } else {
            this.key = (ECPrivateKeyParameters) param;
        }
        this.random = initSecureRandom(forSigning ? this.kCalculator.isDeterministic() ^ 1 : false, providedRandom);
    }

    public BigInteger[] generateSignature(byte[] message) {
        ECDomainParameters ec = this.key.getParameters();
        BigInteger n = ec.getN();
        BigInteger e = calculateE(n, message);
        BigInteger d = ((ECPrivateKeyParameters) this.key).getD();
        if (this.kCalculator.isDeterministic()) {
            this.kCalculator.init(n, d, message);
        } else {
            this.kCalculator.init(n, this.random);
        }
        ECMultiplier basePointMultiplier = createBasePointMultiplier();
        while (true) {
            BigInteger k = this.kCalculator.nextK();
            BigInteger r = basePointMultiplier.multiply(ec.getG(), k).normalize().getAffineXCoord().toBigInteger().mod(n);
            if (!r.equals(ZERO)) {
                if (!k.modInverse(n).multiply(e.add(d.multiply(r))).mod(n).equals(ZERO)) {
                    return new BigInteger[]{r, k.modInverse(n).multiply(e.add(d.multiply(r))).mod(n)};
                }
            }
        }
    }

    public boolean verifySignature(byte[] message, BigInteger r, BigInteger s) {
        ECDomainParameters ec = this.key.getParameters();
        BigInteger n = ec.getN();
        BigInteger e = calculateE(n, message);
        if (r.compareTo(ONE) < 0 || r.compareTo(n) >= 0) {
            return false;
        }
        if (s.compareTo(ONE) < 0 || s.compareTo(n) >= 0) {
            return false;
        }
        BigInteger c = s.modInverse(n);
        ECPoint point = ECAlgorithms.sumOfTwoMultiplies(ec.getG(), e.multiply(c).mod(n), ((ECPublicKeyParameters) this.key).getQ(), r.multiply(c).mod(n));
        if (point.isInfinity()) {
            return false;
        }
        ECCurve curve = point.getCurve();
        if (curve != null) {
            BigInteger cofactor = curve.getCofactor();
            if (cofactor != null && cofactor.compareTo(EIGHT) <= 0) {
                ECFieldElement D = getDenominator(curve.getCoordinateSystem(), point);
                if (!(D == null || (D.isZero() ^ 1) == 0)) {
                    ECFieldElement X = point.getXCoord();
                    while (curve.isValidFieldElement(r)) {
                        if (curve.fromBigInteger(r).multiply(D).equals(X)) {
                            return true;
                        }
                        r = r.add(n);
                    }
                    return false;
                }
            }
        }
        return point.normalize().getAffineXCoord().toBigInteger().mod(n).equals(r);
    }

    protected BigInteger calculateE(BigInteger n, byte[] message) {
        int log2n = n.bitLength();
        int messageBitLength = message.length * 8;
        BigInteger e = new BigInteger(1, message);
        if (log2n < messageBitLength) {
            return e.shiftRight(messageBitLength - log2n);
        }
        return e;
    }

    protected ECMultiplier createBasePointMultiplier() {
        return new FixedPointCombMultiplier();
    }

    protected ECFieldElement getDenominator(int coordinateSystem, ECPoint p) {
        switch (coordinateSystem) {
            case 1:
            case 6:
            case 7:
                return p.getZCoord(0);
            case 2:
            case 3:
            case 4:
                return p.getZCoord(0).square();
            default:
                return null;
        }
    }

    protected SecureRandom initSecureRandom(boolean needed, SecureRandom provided) {
        if (needed) {
            return provided == null ? new SecureRandom() : provided;
        } else {
            return null;
        }
    }
}
