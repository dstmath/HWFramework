package org.bouncycastle.crypto.signers;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.DSAExt;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECKeyParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;

public class ECNRSigner implements DSAExt {
    private boolean forSigning;
    private ECKeyParameters key;
    private SecureRandom random;

    private BigInteger extractT(ECPublicKeyParameters eCPublicKeyParameters, BigInteger bigInteger, BigInteger bigInteger2) {
        BigInteger n = eCPublicKeyParameters.getParameters().getN();
        if (bigInteger.compareTo(ECConstants.ONE) < 0 || bigInteger.compareTo(n) >= 0 || bigInteger2.compareTo(ECConstants.ZERO) < 0 || bigInteger2.compareTo(n) >= 0) {
            return null;
        }
        ECPoint normalize = ECAlgorithms.sumOfTwoMultiplies(eCPublicKeyParameters.getParameters().getG(), bigInteger2, eCPublicKeyParameters.getQ(), bigInteger).normalize();
        if (normalize.isInfinity()) {
            return null;
        }
        return bigInteger.subtract(normalize.getAffineXCoord().toBigInteger()).mod(n);
    }

    @Override // org.bouncycastle.crypto.DSA
    public BigInteger[] generateSignature(byte[] bArr) {
        AsymmetricCipherKeyPair generateKeyPair;
        BigInteger mod;
        if (this.forSigning) {
            BigInteger order = getOrder();
            BigInteger bigInteger = new BigInteger(1, bArr);
            ECPrivateKeyParameters eCPrivateKeyParameters = (ECPrivateKeyParameters) this.key;
            if (bigInteger.compareTo(order) < 0) {
                do {
                    ECKeyPairGenerator eCKeyPairGenerator = new ECKeyPairGenerator();
                    eCKeyPairGenerator.init(new ECKeyGenerationParameters(eCPrivateKeyParameters.getParameters(), this.random));
                    generateKeyPair = eCKeyPairGenerator.generateKeyPair();
                    mod = ((ECPublicKeyParameters) generateKeyPair.getPublic()).getQ().getAffineXCoord().toBigInteger().add(bigInteger).mod(order);
                } while (mod.equals(ECConstants.ZERO));
                return new BigInteger[]{mod, ((ECPrivateKeyParameters) generateKeyPair.getPrivate()).getD().subtract(mod.multiply(eCPrivateKeyParameters.getD())).mod(order)};
            }
            throw new DataLengthException("input too large for ECNR key");
        }
        throw new IllegalStateException("not initialised for signing");
    }

    @Override // org.bouncycastle.crypto.DSAExt
    public BigInteger getOrder() {
        return this.key.getParameters().getN();
    }

    public byte[] getRecoveredMessage(BigInteger bigInteger, BigInteger bigInteger2) {
        if (!this.forSigning) {
            BigInteger extractT = extractT((ECPublicKeyParameters) this.key, bigInteger, bigInteger2);
            if (extractT != null) {
                return BigIntegers.asUnsignedByteArray(extractT);
            }
            return null;
        }
        throw new IllegalStateException("not initialised for verifying/recovery");
    }

    @Override // org.bouncycastle.crypto.DSA
    public void init(boolean z, CipherParameters cipherParameters) {
        ECKeyParameters eCKeyParameters;
        this.forSigning = z;
        if (!z) {
            eCKeyParameters = (ECPublicKeyParameters) cipherParameters;
        } else if (cipherParameters instanceof ParametersWithRandom) {
            ParametersWithRandom parametersWithRandom = (ParametersWithRandom) cipherParameters;
            this.random = parametersWithRandom.getRandom();
            this.key = (ECPrivateKeyParameters) parametersWithRandom.getParameters();
            return;
        } else {
            this.random = CryptoServicesRegistrar.getSecureRandom();
            eCKeyParameters = (ECPrivateKeyParameters) cipherParameters;
        }
        this.key = eCKeyParameters;
    }

    @Override // org.bouncycastle.crypto.DSA
    public boolean verifySignature(byte[] bArr, BigInteger bigInteger, BigInteger bigInteger2) {
        if (!this.forSigning) {
            ECPublicKeyParameters eCPublicKeyParameters = (ECPublicKeyParameters) this.key;
            BigInteger n = eCPublicKeyParameters.getParameters().getN();
            int bitLength = n.bitLength();
            BigInteger bigInteger3 = new BigInteger(1, bArr);
            if (bigInteger3.bitLength() <= bitLength) {
                BigInteger extractT = extractT(eCPublicKeyParameters, bigInteger, bigInteger2);
                return extractT != null && extractT.equals(bigInteger3.mod(n));
            }
            throw new DataLengthException("input too large for ECNR key.");
        }
        throw new IllegalStateException("not initialised for verifying");
    }
}
