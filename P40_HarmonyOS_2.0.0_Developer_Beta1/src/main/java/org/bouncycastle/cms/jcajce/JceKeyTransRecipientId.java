package org.bouncycastle.cms.jcajce;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cms.KeyTransRecipientId;

public class JceKeyTransRecipientId extends KeyTransRecipientId {
    public JceKeyTransRecipientId(X509Certificate x509Certificate) {
        super(convertPrincipal(x509Certificate.getIssuerX500Principal()), x509Certificate.getSerialNumber(), CMSUtils.getSubjectKeyId(x509Certificate));
    }

    public JceKeyTransRecipientId(X500Principal x500Principal, BigInteger bigInteger) {
        super(convertPrincipal(x500Principal), bigInteger);
    }

    public JceKeyTransRecipientId(X500Principal x500Principal, BigInteger bigInteger, byte[] bArr) {
        super(convertPrincipal(x500Principal), bigInteger, bArr);
    }

    private static X500Name convertPrincipal(X500Principal x500Principal) {
        if (x500Principal == null) {
            return null;
        }
        return X500Name.getInstance(x500Principal.getEncoded());
    }
}
