package org.bouncycastle.crypto.agreement;

import java.math.BigInteger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.StagedAgreement;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECPoint;

public class ECDHCStagedAgreement implements StagedAgreement {
    ECPrivateKeyParameters key;

    private ECPoint calculateNextPoint(ECPublicKeyParameters eCPublicKeyParameters) {
        ECDomainParameters parameters = this.key.getParameters();
        if (parameters.equals(eCPublicKeyParameters.getParameters())) {
            BigInteger mod = parameters.getH().multiply(this.key.getD()).mod(parameters.getN());
            ECPoint cleanPoint = ECAlgorithms.cleanPoint(parameters.getCurve(), eCPublicKeyParameters.getQ());
            if (!cleanPoint.isInfinity()) {
                ECPoint normalize = cleanPoint.multiply(mod).normalize();
                if (!normalize.isInfinity()) {
                    return normalize;
                }
                throw new IllegalStateException("Infinity is not a valid agreement value for ECDHC");
            }
            throw new IllegalStateException("Infinity is not a valid public key for ECDHC");
        }
        throw new IllegalStateException("ECDHC public key has wrong domain parameters");
    }

    @Override // org.bouncycastle.crypto.BasicAgreement
    public BigInteger calculateAgreement(CipherParameters cipherParameters) {
        return calculateNextPoint((ECPublicKeyParameters) cipherParameters).getAffineXCoord().toBigInteger();
    }

    @Override // org.bouncycastle.crypto.StagedAgreement
    public AsymmetricKeyParameter calculateStage(CipherParameters cipherParameters) {
        return new ECPublicKeyParameters(calculateNextPoint((ECPublicKeyParameters) cipherParameters), this.key.getParameters());
    }

    @Override // org.bouncycastle.crypto.BasicAgreement
    public int getFieldSize() {
        return (this.key.getParameters().getCurve().getFieldSize() + 7) / 8;
    }

    @Override // org.bouncycastle.crypto.BasicAgreement
    public void init(CipherParameters cipherParameters) {
        this.key = (ECPrivateKeyParameters) cipherParameters;
    }
}
