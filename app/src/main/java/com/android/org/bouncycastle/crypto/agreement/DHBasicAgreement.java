package com.android.org.bouncycastle.crypto.agreement;

import com.android.org.bouncycastle.crypto.BasicAgreement;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import com.android.org.bouncycastle.crypto.params.DHParameters;
import com.android.org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import com.android.org.bouncycastle.crypto.params.DHPublicKeyParameters;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import java.math.BigInteger;

public class DHBasicAgreement implements BasicAgreement {
    private DHParameters dhParams;
    private DHPrivateKeyParameters key;

    public void init(CipherParameters param) {
        AsymmetricKeyParameter kParam;
        if (param instanceof ParametersWithRandom) {
            kParam = (AsymmetricKeyParameter) ((ParametersWithRandom) param).getParameters();
        } else {
            kParam = (AsymmetricKeyParameter) param;
        }
        if (kParam instanceof DHPrivateKeyParameters) {
            this.key = (DHPrivateKeyParameters) kParam;
            this.dhParams = this.key.getParameters();
            return;
        }
        throw new IllegalArgumentException("DHEngine expects DHPrivateKeyParameters");
    }

    public int getFieldSize() {
        return (this.key.getParameters().getP().bitLength() + 7) / 8;
    }

    public BigInteger calculateAgreement(CipherParameters pubKey) {
        DHPublicKeyParameters pub = (DHPublicKeyParameters) pubKey;
        if (pub.getParameters().equals(this.dhParams)) {
            return pub.getY().modPow(this.key.getX(), this.dhParams.getP());
        }
        throw new IllegalArgumentException("Diffie-Hellman public key has wrong parameters.");
    }
}
