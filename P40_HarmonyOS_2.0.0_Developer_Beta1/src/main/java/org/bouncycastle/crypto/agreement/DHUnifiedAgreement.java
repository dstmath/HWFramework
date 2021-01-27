package org.bouncycastle.crypto.agreement;

import java.math.BigInteger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.DHUPrivateParameters;
import org.bouncycastle.crypto.params.DHUPublicParameters;
import org.bouncycastle.util.BigIntegers;

public class DHUnifiedAgreement {
    private DHUPrivateParameters privParams;

    public byte[] calculateAgreement(CipherParameters cipherParameters) {
        DHUPublicParameters dHUPublicParameters = (DHUPublicParameters) cipherParameters;
        DHBasicAgreement dHBasicAgreement = new DHBasicAgreement();
        DHBasicAgreement dHBasicAgreement2 = new DHBasicAgreement();
        dHBasicAgreement.init(this.privParams.getStaticPrivateKey());
        BigInteger calculateAgreement = dHBasicAgreement.calculateAgreement(dHUPublicParameters.getStaticPublicKey());
        dHBasicAgreement2.init(this.privParams.getEphemeralPrivateKey());
        BigInteger calculateAgreement2 = dHBasicAgreement2.calculateAgreement(dHUPublicParameters.getEphemeralPublicKey());
        int fieldSize = getFieldSize();
        byte[] bArr = new byte[(fieldSize * 2)];
        BigIntegers.asUnsignedByteArray(calculateAgreement2, bArr, 0, fieldSize);
        BigIntegers.asUnsignedByteArray(calculateAgreement, bArr, fieldSize, fieldSize);
        return bArr;
    }

    public int getFieldSize() {
        return (this.privParams.getStaticPrivateKey().getParameters().getP().bitLength() + 7) / 8;
    }

    public void init(CipherParameters cipherParameters) {
        this.privParams = (DHUPrivateParameters) cipherParameters;
    }
}
