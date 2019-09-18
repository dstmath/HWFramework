package org.bouncycastle.cert;

import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.asn1.x509.V1TBSCertificateGenerator;
import org.bouncycastle.operator.ContentSigner;

public class X509v1CertificateBuilder {
    private V1TBSCertificateGenerator tbsGen;

    public X509v1CertificateBuilder(X500Name x500Name, BigInteger bigInteger, Date date, Date date2, Locale locale, X500Name x500Name2, SubjectPublicKeyInfo subjectPublicKeyInfo) {
        this(x500Name, bigInteger, new Time(date, locale), new Time(date2, locale), x500Name2, subjectPublicKeyInfo);
    }

    public X509v1CertificateBuilder(X500Name x500Name, BigInteger bigInteger, Date date, Date date2, X500Name x500Name2, SubjectPublicKeyInfo subjectPublicKeyInfo) {
        this(x500Name, bigInteger, new Time(date), new Time(date2), x500Name2, subjectPublicKeyInfo);
    }

    public X509v1CertificateBuilder(X500Name x500Name, BigInteger bigInteger, Time time, Time time2, X500Name x500Name2, SubjectPublicKeyInfo subjectPublicKeyInfo) {
        if (x500Name == null) {
            throw new IllegalArgumentException("issuer must not be null");
        } else if (subjectPublicKeyInfo != null) {
            this.tbsGen = new V1TBSCertificateGenerator();
            this.tbsGen.setSerialNumber(new ASN1Integer(bigInteger));
            this.tbsGen.setIssuer(x500Name);
            this.tbsGen.setStartDate(time);
            this.tbsGen.setEndDate(time2);
            this.tbsGen.setSubject(x500Name2);
            this.tbsGen.setSubjectPublicKeyInfo(subjectPublicKeyInfo);
        } else {
            throw new IllegalArgumentException("publicKeyInfo must not be null");
        }
    }

    public X509CertificateHolder build(ContentSigner contentSigner) {
        this.tbsGen.setSignature(contentSigner.getAlgorithmIdentifier());
        return CertUtils.generateFullCert(contentSigner, this.tbsGen.generateTBSCertificate());
    }
}
