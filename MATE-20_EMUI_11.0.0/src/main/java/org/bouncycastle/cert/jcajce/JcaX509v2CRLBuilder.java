package org.bouncycastle.cert.jcajce;

import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v2CRLBuilder;

public class JcaX509v2CRLBuilder extends X509v2CRLBuilder {
    public JcaX509v2CRLBuilder(X509CRL x509crl) throws CRLException {
        super(new JcaX509CRLHolder(x509crl));
    }

    public JcaX509v2CRLBuilder(X509Certificate x509Certificate, Date date) {
        this(x509Certificate.getSubjectX500Principal(), date);
    }

    public JcaX509v2CRLBuilder(X500Principal x500Principal, Date date) {
        super(X500Name.getInstance(x500Principal.getEncoded()), date);
    }
}
