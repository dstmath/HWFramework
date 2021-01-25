package org.bouncycastle.cert.selector.jcajce;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.selector.X509CertificateHolderSelector;

public class JcaX509CertificateHolderSelector extends X509CertificateHolderSelector {
    public JcaX509CertificateHolderSelector(X509Certificate x509Certificate) {
        super(convertPrincipal(x509Certificate.getIssuerX500Principal()), x509Certificate.getSerialNumber(), getSubjectKeyId(x509Certificate));
    }

    public JcaX509CertificateHolderSelector(X500Principal x500Principal, BigInteger bigInteger) {
        super(convertPrincipal(x500Principal), bigInteger);
    }

    public JcaX509CertificateHolderSelector(X500Principal x500Principal, BigInteger bigInteger, byte[] bArr) {
        super(convertPrincipal(x500Principal), bigInteger, bArr);
    }

    private static X500Name convertPrincipal(X500Principal x500Principal) {
        if (x500Principal == null) {
            return null;
        }
        return X500Name.getInstance(x500Principal.getEncoded());
    }

    private static byte[] getSubjectKeyId(X509Certificate x509Certificate) {
        byte[] extensionValue = x509Certificate.getExtensionValue(Extension.subjectKeyIdentifier.getId());
        if (extensionValue != null) {
            return ASN1OctetString.getInstance(ASN1OctetString.getInstance(extensionValue).getOctets()).getOctets();
        }
        return null;
    }
}
