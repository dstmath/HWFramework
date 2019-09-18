package org.bouncycastle.crypto.tls;

import java.math.BigInteger;
import org.bouncycastle.crypto.BasicAgreement;
import org.bouncycastle.crypto.agreement.DHBasicAgreement;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.util.BigIntegers;

public class DefaultTlsAgreementCredentials extends AbstractTlsAgreementCredentials {
    protected BasicAgreement basicAgreement;
    protected Certificate certificate;
    protected AsymmetricKeyParameter privateKey;
    protected boolean truncateAgreement;

    public DefaultTlsAgreementCredentials(Certificate certificate2, AsymmetricKeyParameter asymmetricKeyParameter) {
        boolean z;
        if (certificate2 == null) {
            throw new IllegalArgumentException("'certificate' cannot be null");
        } else if (certificate2.isEmpty()) {
            throw new IllegalArgumentException("'certificate' cannot be empty");
        } else if (asymmetricKeyParameter == null) {
            throw new IllegalArgumentException("'privateKey' cannot be null");
        } else if (asymmetricKeyParameter.isPrivate()) {
            if (asymmetricKeyParameter instanceof DHPrivateKeyParameters) {
                this.basicAgreement = new DHBasicAgreement();
                z = true;
            } else if (asymmetricKeyParameter instanceof ECPrivateKeyParameters) {
                this.basicAgreement = new ECDHBasicAgreement();
                z = false;
            } else {
                throw new IllegalArgumentException("'privateKey' type not supported: " + asymmetricKeyParameter.getClass().getName());
            }
            this.truncateAgreement = z;
            this.certificate = certificate2;
            this.privateKey = asymmetricKeyParameter;
        } else {
            throw new IllegalArgumentException("'privateKey' must be private");
        }
    }

    public byte[] generateAgreement(AsymmetricKeyParameter asymmetricKeyParameter) {
        this.basicAgreement.init(this.privateKey);
        BigInteger calculateAgreement = this.basicAgreement.calculateAgreement(asymmetricKeyParameter);
        return this.truncateAgreement ? BigIntegers.asUnsignedByteArray(calculateAgreement) : BigIntegers.asUnsignedByteArray(this.basicAgreement.getFieldSize(), calculateAgreement);
    }

    public Certificate getCertificate() {
        return this.certificate;
    }
}
