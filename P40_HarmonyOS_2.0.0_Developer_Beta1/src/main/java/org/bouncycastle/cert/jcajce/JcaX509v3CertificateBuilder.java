package org.bouncycastle.cert.jcajce;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.cert.X509v3CertificateBuilder;

public class JcaX509v3CertificateBuilder extends X509v3CertificateBuilder {
    public JcaX509v3CertificateBuilder(X509Certificate x509Certificate) throws CertificateEncodingException {
        super(new JcaX509CertificateHolder(x509Certificate));
    }

    public JcaX509v3CertificateBuilder(X509Certificate x509Certificate, BigInteger bigInteger, Date date, Date date2, X500Principal x500Principal, PublicKey publicKey) {
        this(x509Certificate.getSubjectX500Principal(), bigInteger, date, date2, x500Principal, publicKey);
    }

    public JcaX509v3CertificateBuilder(X509Certificate x509Certificate, BigInteger bigInteger, Date date, Date date2, X500Name x500Name, PublicKey publicKey) {
        this(X500Name.getInstance(x509Certificate.getSubjectX500Principal().getEncoded()), bigInteger, date, date2, x500Name, publicKey);
    }

    public JcaX509v3CertificateBuilder(X500Principal x500Principal, BigInteger bigInteger, Date date, Date date2, X500Principal x500Principal2, PublicKey publicKey) {
        super(X500Name.getInstance(x500Principal.getEncoded()), bigInteger, date, date2, X500Name.getInstance(x500Principal2.getEncoded()), SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    }

    public JcaX509v3CertificateBuilder(X500Name x500Name, BigInteger bigInteger, Date date, Date date2, X500Name x500Name2, PublicKey publicKey) {
        super(x500Name, bigInteger, date, date2, x500Name2, SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    }

    public JcaX509v3CertificateBuilder(X500Name x500Name, BigInteger bigInteger, Time time, Time time2, X500Name x500Name2, PublicKey publicKey) {
        super(x500Name, bigInteger, time, time2, x500Name2, SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    }

    public JcaX509v3CertificateBuilder copyAndAddExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier, boolean z, X509Certificate x509Certificate) throws CertificateEncodingException {
        copyAndAddExtension(aSN1ObjectIdentifier, z, new JcaX509CertificateHolder(x509Certificate));
        return this;
    }
}
