package org.bouncycastle.cert;

import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import org.bouncycastle.operator.ContentSigner;

public class X509v3CertificateBuilder {
    private ExtensionsGenerator extGenerator;
    private V3TBSCertificateGenerator tbsGen;

    public X509v3CertificateBuilder(X500Name x500Name, BigInteger bigInteger, Date date, Date date2, Locale locale, X500Name x500Name2, SubjectPublicKeyInfo subjectPublicKeyInfo) {
        this(x500Name, bigInteger, new Time(date, locale), new Time(date2, locale), x500Name2, subjectPublicKeyInfo);
    }

    public X509v3CertificateBuilder(X500Name x500Name, BigInteger bigInteger, Date date, Date date2, X500Name x500Name2, SubjectPublicKeyInfo subjectPublicKeyInfo) {
        this(x500Name, bigInteger, new Time(date), new Time(date2), x500Name2, subjectPublicKeyInfo);
    }

    public X509v3CertificateBuilder(X500Name x500Name, BigInteger bigInteger, Time time, Time time2, X500Name x500Name2, SubjectPublicKeyInfo subjectPublicKeyInfo) {
        this.tbsGen = new V3TBSCertificateGenerator();
        this.tbsGen.setSerialNumber(new ASN1Integer(bigInteger));
        this.tbsGen.setIssuer(x500Name);
        this.tbsGen.setStartDate(time);
        this.tbsGen.setEndDate(time2);
        this.tbsGen.setSubject(x500Name2);
        this.tbsGen.setSubjectPublicKeyInfo(subjectPublicKeyInfo);
        this.extGenerator = new ExtensionsGenerator();
    }

    public X509v3CertificateBuilder addExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier, boolean z, ASN1Encodable aSN1Encodable) throws CertIOException {
        CertUtils.addExtension(this.extGenerator, aSN1ObjectIdentifier, z, aSN1Encodable);
        return this;
    }

    public X509v3CertificateBuilder addExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier, boolean z, byte[] bArr) throws CertIOException {
        this.extGenerator.addExtension(aSN1ObjectIdentifier, z, bArr);
        return this;
    }

    public X509v3CertificateBuilder addExtension(Extension extension) throws CertIOException {
        this.extGenerator.addExtension(extension);
        return this;
    }

    public X509CertificateHolder build(ContentSigner contentSigner) {
        this.tbsGen.setSignature(contentSigner.getAlgorithmIdentifier());
        if (!this.extGenerator.isEmpty()) {
            this.tbsGen.setExtensions(this.extGenerator.generate());
        }
        return CertUtils.generateFullCert(contentSigner, this.tbsGen.generateTBSCertificate());
    }

    public X509v3CertificateBuilder copyAndAddExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier, boolean z, X509CertificateHolder x509CertificateHolder) {
        Extension extension = x509CertificateHolder.toASN1Structure().getTBSCertificate().getExtensions().getExtension(aSN1ObjectIdentifier);
        if (extension != null) {
            this.extGenerator.addExtension(aSN1ObjectIdentifier, z, extension.getExtnValue().getOctets());
            return this;
        }
        throw new NullPointerException("extension " + aSN1ObjectIdentifier + " not present");
    }

    public X509v3CertificateBuilder setIssuerUniqueID(boolean[] zArr) {
        this.tbsGen.setIssuerUniqueID(CertUtils.booleanToBitString(zArr));
        return this;
    }

    public X509v3CertificateBuilder setSubjectUniqueID(boolean[] zArr) {
        this.tbsGen.setSubjectUniqueID(CertUtils.booleanToBitString(zArr));
        return this;
    }
}
