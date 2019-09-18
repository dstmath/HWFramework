package org.bouncycastle.cert.jcajce;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v1CertificateBuilder;

public class JcaX509v1CertificateBuilder extends X509v1CertificateBuilder {
    public JcaX509v1CertificateBuilder(X500Principal x500Principal, BigInteger bigInteger, Date date, Date date2, X500Principal x500Principal2, PublicKey publicKey) {
        super(X500Name.getInstance(x500Principal.getEncoded()), bigInteger, date, date2, X500Name.getInstance(x500Principal2.getEncoded()), SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    }

    public JcaX509v1CertificateBuilder(X500Name x500Name, BigInteger bigInteger, Date date, Date date2, X500Name x500Name2, PublicKey publicKey) {
        super(x500Name, bigInteger, date, date2, x500Name2, SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    }
}
