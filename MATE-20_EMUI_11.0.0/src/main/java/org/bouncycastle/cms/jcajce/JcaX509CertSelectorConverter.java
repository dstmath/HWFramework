package org.bouncycastle.cms.jcajce;

import java.security.cert.X509CertSelector;
import org.bouncycastle.cms.KeyTransRecipientId;
import org.bouncycastle.cms.SignerId;

public class JcaX509CertSelectorConverter extends org.bouncycastle.cert.selector.jcajce.JcaX509CertSelectorConverter {
    public X509CertSelector getCertSelector(KeyTransRecipientId keyTransRecipientId) {
        return doConversion(keyTransRecipientId.getIssuer(), keyTransRecipientId.getSerialNumber(), keyTransRecipientId.getSubjectKeyIdentifier());
    }

    public X509CertSelector getCertSelector(SignerId signerId) {
        return doConversion(signerId.getIssuer(), signerId.getSerialNumber(), signerId.getSubjectKeyIdentifier());
    }
}
