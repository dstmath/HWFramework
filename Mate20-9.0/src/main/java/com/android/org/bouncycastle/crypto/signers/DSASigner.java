package com.android.org.bouncycastle.crypto.signers;

import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DSA;
import com.android.org.bouncycastle.crypto.params.DSAKeyParameters;
import com.android.org.bouncycastle.crypto.params.DSAParameters;
import com.android.org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import com.android.org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import java.math.BigInteger;
import java.security.SecureRandom;

public class DSASigner implements DSA {
    private final DSAKCalculator kCalculator;
    private DSAKeyParameters key;
    private SecureRandom random;

    public DSASigner() {
        this.kCalculator = new RandomDSAKCalculator();
    }

    public DSASigner(DSAKCalculator kCalculator2) {
        this.kCalculator = kCalculator2;
    }

    public void init(boolean forSigning, CipherParameters param) {
        SecureRandom providedRandom = null;
        if (!forSigning) {
            this.key = (DSAPublicKeyParameters) param;
        } else if (param instanceof ParametersWithRandom) {
            ParametersWithRandom rParam = (ParametersWithRandom) param;
            this.key = (DSAPrivateKeyParameters) rParam.getParameters();
            providedRandom = rParam.getRandom();
        } else {
            this.key = (DSAPrivateKeyParameters) param;
        }
        this.random = initSecureRandom(forSigning && !this.kCalculator.isDeterministic(), providedRandom);
    }

    public BigInteger[] generateSignature(byte[] message) {
        DSAParameters params = this.key.getParameters();
        BigInteger q = params.getQ();
        BigInteger m = calculateE(q, message);
        BigInteger x = ((DSAPrivateKeyParameters) this.key).getX();
        if (this.kCalculator.isDeterministic()) {
            this.kCalculator.init(q, x, message);
        } else {
            this.kCalculator.init(q, this.random);
        }
        BigInteger k = this.kCalculator.nextK();
        BigInteger r = params.getG().modPow(k.add(getRandomizer(q, this.random)), params.getP()).mod(q);
        return new BigInteger[]{r, k.modInverse(q).multiply(m.add(x.multiply(r))).mod(q)};
    }

    public boolean verifySignature(byte[] message, BigInteger r, BigInteger s) {
        DSAParameters params = this.key.getParameters();
        BigInteger q = params.getQ();
        BigInteger m = calculateE(q, message);
        BigInteger zero = BigInteger.valueOf(0);
        if (zero.compareTo(r) >= 0 || q.compareTo(r) <= 0 || zero.compareTo(s) >= 0 || q.compareTo(s) <= 0) {
            return false;
        }
        BigInteger w = s.modInverse(q);
        BigInteger u1 = m.multiply(w).mod(q);
        BigInteger u2 = r.multiply(w).mod(q);
        BigInteger p = params.getP();
        return params.getG().modPow(u1, p).multiply(((DSAPublicKeyParameters) this.key).getY().modPow(u2, p)).mod(p).mod(q).equals(r);
    }

    private BigInteger calculateE(BigInteger n, byte[] message) {
        if (n.bitLength() >= message.length * 8) {
            return new BigInteger(1, message);
        }
        byte[] trunc = new byte[(n.bitLength() / 8)];
        System.arraycopy(message, 0, trunc, 0, trunc.length);
        return new BigInteger(1, trunc);
    }

    /* access modifiers changed from: protected */
    public SecureRandom initSecureRandom(boolean needed, SecureRandom provided) {
        if (!needed) {
            return null;
        }
        return provided != null ? provided : new SecureRandom();
    }

    private BigInteger getRandomizer(BigInteger q, SecureRandom provided) {
        return new BigInteger(7, provided != null ? provided : new SecureRandom()).add(BigInteger.valueOf(128)).multiply(q);
    }
}
