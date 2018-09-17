package com.android.org.bouncycastle.crypto.agreement;

import com.android.org.bouncycastle.crypto.BasicAgreement;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import com.android.org.bouncycastle.crypto.params.ECPublicKeyParameters;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECPoint;
import java.math.BigInteger;

public class ECDHBasicAgreement implements BasicAgreement {
    private ECPrivateKeyParameters key;

    public void init(CipherParameters key) {
        this.key = (ECPrivateKeyParameters) key;
    }

    public int getFieldSize() {
        return (this.key.getParameters().getCurve().getFieldSize() + 7) / 8;
    }

    public BigInteger calculateAgreement(CipherParameters pubKey) {
        ECPoint peerPoint = ((ECPublicKeyParameters) pubKey).getQ();
        ECCurve myCurve = this.key.getParameters().getCurve();
        if (peerPoint.isInfinity()) {
            throw new IllegalStateException("Infinity is not a valid public key for ECDH");
        }
        try {
            myCurve.validatePoint(peerPoint.getXCoord().toBigInteger(), peerPoint.getYCoord().toBigInteger());
            ECPoint pubPoint = myCurve.createPoint(peerPoint.getXCoord().toBigInteger(), peerPoint.getYCoord().toBigInteger());
            if (((ECPublicKeyParameters) pubKey).getParameters().equals(this.key.getParameters())) {
                ECPoint P = pubPoint.multiply(this.key.getD()).normalize();
                if (!P.isInfinity()) {
                    return P.getAffineXCoord().toBigInteger();
                }
                throw new IllegalStateException("Infinity is not a valid agreement value for ECDH");
            }
            throw new IllegalStateException("ECDH public key has wrong domain parameters");
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("The peer public key must be on the curve for ECDH");
        }
    }
}
