package org.bouncycastle.cert.ocsp.jcajce;

import java.security.PublicKey;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.ocsp.BasicOCSPRespBuilder;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.operator.DigestCalculator;

public class JcaBasicOCSPRespBuilder extends BasicOCSPRespBuilder {
    public JcaBasicOCSPRespBuilder(PublicKey publicKey, DigestCalculator digestCalculator) throws OCSPException {
        super(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()), digestCalculator);
    }

    public JcaBasicOCSPRespBuilder(X500Principal x500Principal) {
        super(new JcaRespID(x500Principal));
    }
}
