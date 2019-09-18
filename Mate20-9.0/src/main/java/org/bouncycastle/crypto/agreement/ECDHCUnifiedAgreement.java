package org.bouncycastle.crypto.agreement;

import java.math.BigInteger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.ECDHUPrivateParameters;
import org.bouncycastle.crypto.params.ECDHUPublicParameters;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;

public class ECDHCUnifiedAgreement {
    private ECDHUPrivateParameters privParams;

    public byte[] calculateAgreement(CipherParameters cipherParameters) {
        ECDHUPublicParameters eCDHUPublicParameters = (ECDHUPublicParameters) cipherParameters;
        ECDHCBasicAgreement eCDHCBasicAgreement = new ECDHCBasicAgreement();
        ECDHCBasicAgreement eCDHCBasicAgreement2 = new ECDHCBasicAgreement();
        eCDHCBasicAgreement.init(this.privParams.getStaticPrivateKey());
        BigInteger calculateAgreement = eCDHCBasicAgreement.calculateAgreement(eCDHUPublicParameters.getStaticPublicKey());
        eCDHCBasicAgreement2.init(this.privParams.getEphemeralPrivateKey());
        return Arrays.concatenate(BigIntegers.asUnsignedByteArray(getFieldSize(), eCDHCBasicAgreement2.calculateAgreement(eCDHUPublicParameters.getEphemeralPublicKey())), BigIntegers.asUnsignedByteArray(getFieldSize(), calculateAgreement));
    }

    public int getFieldSize() {
        return (this.privParams.getStaticPrivateKey().getParameters().getCurve().getFieldSize() + 7) / 8;
    }

    public void init(CipherParameters cipherParameters) {
        this.privParams = (ECDHUPrivateParameters) cipherParameters;
    }
}
