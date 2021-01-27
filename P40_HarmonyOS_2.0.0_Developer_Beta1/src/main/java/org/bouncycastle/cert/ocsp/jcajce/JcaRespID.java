package org.bouncycastle.cert.ocsp.jcajce;

import java.security.PublicKey;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.RespID;
import org.bouncycastle.operator.DigestCalculator;

public class JcaRespID extends RespID {
    public JcaRespID(PublicKey publicKey, DigestCalculator digestCalculator) throws OCSPException {
        super(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()), digestCalculator);
    }

    public JcaRespID(X500Principal x500Principal) {
        super(X500Name.getInstance(x500Principal.getEncoded()));
    }
}
