package org.bouncycastle.cert.bc;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;

public class BcX509v1CertificateBuilder extends X509v1CertificateBuilder {
    public BcX509v1CertificateBuilder(X500Name x500Name, BigInteger bigInteger, Date date, Date date2, X500Name x500Name2, AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        super(x500Name, bigInteger, date, date2, x500Name2, SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(asymmetricKeyParameter));
    }
}
