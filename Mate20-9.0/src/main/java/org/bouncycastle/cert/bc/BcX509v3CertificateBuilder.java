package org.bouncycastle.cert.bc;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;

public class BcX509v3CertificateBuilder extends X509v3CertificateBuilder {
    public BcX509v3CertificateBuilder(X500Name x500Name, BigInteger bigInteger, Date date, Date date2, X500Name x500Name2, AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        super(x500Name, bigInteger, date, date2, x500Name2, SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(asymmetricKeyParameter));
    }

    public BcX509v3CertificateBuilder(X509CertificateHolder x509CertificateHolder, BigInteger bigInteger, Date date, Date date2, X500Name x500Name, AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        super(x509CertificateHolder.getSubject(), bigInteger, date, date2, x500Name, SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(asymmetricKeyParameter));
    }
}
