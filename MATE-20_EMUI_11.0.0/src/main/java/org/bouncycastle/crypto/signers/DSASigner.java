package org.bouncycastle.crypto.signers;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.DSAExt;
import org.bouncycastle.crypto.params.DSAKeyParameters;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.util.BigIntegers;

public class DSASigner implements DSAExt {
    private final DSAKCalculator kCalculator;
    private DSAKeyParameters key;
    private SecureRandom random;

    public DSASigner() {
        this.kCalculator = new RandomDSAKCalculator();
    }

    public DSASigner(DSAKCalculator dSAKCalculator) {
        this.kCalculator = dSAKCalculator;
    }

    private BigInteger calculateE(BigInteger bigInteger, byte[] bArr) {
        if (bigInteger.bitLength() >= bArr.length * 8) {
            return new BigInteger(1, bArr);
        }
        byte[] bArr2 = new byte[(bigInteger.bitLength() / 8)];
        System.arraycopy(bArr, 0, bArr2, 0, bArr2.length);
        return new BigInteger(1, bArr2);
    }

    private BigInteger getRandomizer(BigInteger bigInteger, SecureRandom secureRandom) {
        if (secureRandom == null) {
            secureRandom = CryptoServicesRegistrar.getSecureRandom();
        }
        return BigIntegers.createRandomBigInteger(7, secureRandom).add(BigInteger.valueOf(128)).multiply(bigInteger);
    }

    @Override // org.bouncycastle.crypto.DSA
    public BigInteger[] generateSignature(byte[] bArr) {
        DSAParameters parameters = this.key.getParameters();
        BigInteger q = parameters.getQ();
        BigInteger calculateE = calculateE(q, bArr);
        BigInteger x = ((DSAPrivateKeyParameters) this.key).getX();
        if (this.kCalculator.isDeterministic()) {
            this.kCalculator.init(q, x, bArr);
        } else {
            this.kCalculator.init(q, this.random);
        }
        BigInteger nextK = this.kCalculator.nextK();
        BigInteger mod = parameters.getG().modPow(nextK.add(getRandomizer(q, this.random)), parameters.getP()).mod(q);
        return new BigInteger[]{mod, nextK.modInverse(q).multiply(calculateE.add(x.multiply(mod))).mod(q)};
    }

    @Override // org.bouncycastle.crypto.DSAExt
    public BigInteger getOrder() {
        return this.key.getParameters().getQ();
    }

    @Override // org.bouncycastle.crypto.DSA
    public void init(boolean z, CipherParameters cipherParameters) {
        SecureRandom secureRandom;
        DSAKeyParameters dSAKeyParameters;
        if (!z) {
            dSAKeyParameters = (DSAPublicKeyParameters) cipherParameters;
        } else if (cipherParameters instanceof ParametersWithRandom) {
            ParametersWithRandom parametersWithRandom = (ParametersWithRandom) cipherParameters;
            this.key = (DSAPrivateKeyParameters) parametersWithRandom.getParameters();
            secureRandom = parametersWithRandom.getRandom();
            this.random = initSecureRandom(!z && !this.kCalculator.isDeterministic(), secureRandom);
        } else {
            dSAKeyParameters = (DSAPrivateKeyParameters) cipherParameters;
        }
        this.key = dSAKeyParameters;
        secureRandom = null;
        this.random = initSecureRandom(!z && !this.kCalculator.isDeterministic(), secureRandom);
    }

    /* access modifiers changed from: protected */
    public SecureRandom initSecureRandom(boolean z, SecureRandom secureRandom) {
        if (!z) {
            return null;
        }
        return secureRandom != null ? secureRandom : CryptoServicesRegistrar.getSecureRandom();
    }

    @Override // org.bouncycastle.crypto.DSA
    public boolean verifySignature(byte[] bArr, BigInteger bigInteger, BigInteger bigInteger2) {
        DSAParameters parameters = this.key.getParameters();
        BigInteger q = parameters.getQ();
        BigInteger calculateE = calculateE(q, bArr);
        BigInteger valueOf = BigInteger.valueOf(0);
        if (valueOf.compareTo(bigInteger) >= 0 || q.compareTo(bigInteger) <= 0 || valueOf.compareTo(bigInteger2) >= 0 || q.compareTo(bigInteger2) <= 0) {
            return false;
        }
        BigInteger modInverse = bigInteger2.modInverse(q);
        BigInteger mod = calculateE.multiply(modInverse).mod(q);
        BigInteger mod2 = bigInteger.multiply(modInverse).mod(q);
        BigInteger p = parameters.getP();
        return parameters.getG().modPow(mod, p).multiply(((DSAPublicKeyParameters) this.key).getY().modPow(mod2, p)).mod(p).mod(q).equals(bigInteger);
    }
}
