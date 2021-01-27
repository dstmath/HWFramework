package org.bouncycastle.cert;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.TBSCertList;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.asn1.x509.V2TBSCertListGenerator;
import org.bouncycastle.operator.ContentSigner;

public class X509v2CRLBuilder {
    private ExtensionsGenerator extGenerator;
    private V2TBSCertListGenerator tbsGen;

    public X509v2CRLBuilder(X500Name x500Name, Date date) {
        this.tbsGen = new V2TBSCertListGenerator();
        this.extGenerator = new ExtensionsGenerator();
        this.tbsGen.setIssuer(x500Name);
        this.tbsGen.setThisUpdate(new Time(date));
    }

    public X509v2CRLBuilder(X500Name x500Name, Date date, Locale locale) {
        this.tbsGen = new V2TBSCertListGenerator();
        this.extGenerator = new ExtensionsGenerator();
        this.tbsGen.setIssuer(x500Name);
        this.tbsGen.setThisUpdate(new Time(date, locale));
    }

    public X509v2CRLBuilder(X500Name x500Name, Time time) {
        this.tbsGen = new V2TBSCertListGenerator();
        this.extGenerator = new ExtensionsGenerator();
        this.tbsGen.setIssuer(x500Name);
        this.tbsGen.setThisUpdate(time);
    }

    public X509v2CRLBuilder(X509CRLHolder x509CRLHolder) {
        this.tbsGen = new V2TBSCertListGenerator();
        this.tbsGen.setIssuer(x509CRLHolder.getIssuer());
        this.tbsGen.setThisUpdate(new Time(x509CRLHolder.getThisUpdate()));
        Date nextUpdate = x509CRLHolder.getNextUpdate();
        if (nextUpdate != null) {
            this.tbsGen.setNextUpdate(new Time(nextUpdate));
        }
        addCRL(x509CRLHolder);
        this.extGenerator = new ExtensionsGenerator();
        Extensions extensions = x509CRLHolder.getExtensions();
        if (extensions != null) {
            Enumeration oids = extensions.oids();
            while (oids.hasMoreElements()) {
                this.extGenerator.addExtension(extensions.getExtension((ASN1ObjectIdentifier) oids.nextElement()));
            }
        }
    }

    private Extension doGetExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return this.extGenerator.generate().getExtension(aSN1ObjectIdentifier);
    }

    public X509v2CRLBuilder addCRL(X509CRLHolder x509CRLHolder) {
        TBSCertList tBSCertList = x509CRLHolder.toASN1Structure().getTBSCertList();
        if (tBSCertList != null) {
            Enumeration revokedCertificateEnumeration = tBSCertList.getRevokedCertificateEnumeration();
            while (revokedCertificateEnumeration.hasMoreElements()) {
                this.tbsGen.addCRLEntry(ASN1Sequence.getInstance(((ASN1Encodable) revokedCertificateEnumeration.nextElement()).toASN1Primitive()));
            }
        }
        return this;
    }

    public X509v2CRLBuilder addCRLEntry(BigInteger bigInteger, Date date, int i) {
        this.tbsGen.addCRLEntry(new ASN1Integer(bigInteger), new Time(date), i);
        return this;
    }

    public X509v2CRLBuilder addCRLEntry(BigInteger bigInteger, Date date, int i, Date date2) {
        this.tbsGen.addCRLEntry(new ASN1Integer(bigInteger), new Time(date), i, new ASN1GeneralizedTime(date2));
        return this;
    }

    public X509v2CRLBuilder addCRLEntry(BigInteger bigInteger, Date date, Extensions extensions) {
        this.tbsGen.addCRLEntry(new ASN1Integer(bigInteger), new Time(date), extensions);
        return this;
    }

    public X509v2CRLBuilder addExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier, boolean z, ASN1Encodable aSN1Encodable) throws CertIOException {
        CertUtils.addExtension(this.extGenerator, aSN1ObjectIdentifier, z, aSN1Encodable);
        return this;
    }

    public X509v2CRLBuilder addExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier, boolean z, byte[] bArr) throws CertIOException {
        this.extGenerator.addExtension(aSN1ObjectIdentifier, z, bArr);
        return this;
    }

    public X509v2CRLBuilder addExtension(Extension extension) throws CertIOException {
        this.extGenerator.addExtension(extension);
        return this;
    }

    public X509CRLHolder build(ContentSigner contentSigner) {
        this.tbsGen.setSignature(contentSigner.getAlgorithmIdentifier());
        if (!this.extGenerator.isEmpty()) {
            this.tbsGen.setExtensions(this.extGenerator.generate());
        }
        return CertUtils.generateFullCRL(contentSigner, this.tbsGen.generateTBSCertList());
    }

    public Extension getExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return doGetExtension(aSN1ObjectIdentifier);
    }

    public boolean hasExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return doGetExtension(aSN1ObjectIdentifier) != null;
    }

    public X509v2CRLBuilder removeExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.extGenerator = CertUtils.doRemoveExtension(this.extGenerator, aSN1ObjectIdentifier);
        return this;
    }

    public X509v2CRLBuilder replaceExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier, boolean z, ASN1Encodable aSN1Encodable) throws CertIOException {
        try {
            this.extGenerator = CertUtils.doReplaceExtension(this.extGenerator, new Extension(aSN1ObjectIdentifier, z, aSN1Encodable.toASN1Primitive().getEncoded(ASN1Encoding.DER)));
            return this;
        } catch (IOException e) {
            throw new CertIOException("cannot encode extension: " + e.getMessage(), e);
        }
    }

    public X509v2CRLBuilder replaceExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier, boolean z, byte[] bArr) throws CertIOException {
        this.extGenerator = CertUtils.doReplaceExtension(this.extGenerator, new Extension(aSN1ObjectIdentifier, z, bArr));
        return this;
    }

    public X509v2CRLBuilder replaceExtension(Extension extension) throws CertIOException {
        this.extGenerator = CertUtils.doReplaceExtension(this.extGenerator, extension);
        return this;
    }

    public X509v2CRLBuilder setNextUpdate(Date date) {
        return setNextUpdate(new Time(date));
    }

    public X509v2CRLBuilder setNextUpdate(Date date, Locale locale) {
        return setNextUpdate(new Time(date, locale));
    }

    public X509v2CRLBuilder setNextUpdate(Time time) {
        this.tbsGen.setNextUpdate(time);
        return this;
    }
}
