package org.bouncycastle.jcajce.provider.asymmetric.x509;

import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.util.Date;
import java.util.Enumeration;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.jcajce.provider.asymmetric.util.PKCS12BagAttributeCarrierImpl;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;

class X509CertificateObject extends X509CertificateImpl implements PKCS12BagAttributeCarrier {
    private PKCS12BagAttributeCarrier attrCarrier = new PKCS12BagAttributeCarrierImpl();
    private final Object cacheLock = new Object();
    private volatile int hashValue;
    private volatile boolean hashValueSet;
    private X509CertificateInternal internalCertificateValue;
    private X500Principal issuerValue;
    private PublicKey publicKeyValue;
    private X500Principal subjectValue;
    private long[] validityValues;

    X509CertificateObject(JcaJceHelper jcaJceHelper, Certificate certificate) throws CertificateParsingException {
        super(jcaJceHelper, certificate, createBasicConstraints(certificate), createKeyUsage(certificate), createSigAlgName(certificate), createSigAlgParams(certificate));
    }

    private static BasicConstraints createBasicConstraints(Certificate certificate) throws CertificateParsingException {
        try {
            byte[] extensionOctets = getExtensionOctets(certificate, "2.5.29.19");
            if (extensionOctets == null) {
                return null;
            }
            return BasicConstraints.getInstance(ASN1Primitive.fromByteArray(extensionOctets));
        } catch (Exception e) {
            throw new CertificateParsingException("cannot construct BasicConstraints: " + e);
        }
    }

    private static boolean[] createKeyUsage(Certificate certificate) throws CertificateParsingException {
        try {
            byte[] extensionOctets = getExtensionOctets(certificate, "2.5.29.15");
            if (extensionOctets == null) {
                return null;
            }
            DERBitString instance = DERBitString.getInstance(ASN1Primitive.fromByteArray(extensionOctets));
            byte[] bytes = instance.getBytes();
            int length = (bytes.length * 8) - instance.getPadBits();
            int i = 9;
            if (length >= 9) {
                i = length;
            }
            boolean[] zArr = new boolean[i];
            for (int i2 = 0; i2 != length; i2++) {
                zArr[i2] = (bytes[i2 / 8] & (128 >>> (i2 % 8))) != 0;
            }
            return zArr;
        } catch (Exception e) {
            throw new CertificateParsingException("cannot construct KeyUsage: " + e);
        }
    }

    private static String createSigAlgName(Certificate certificate) throws CertificateParsingException {
        try {
            return X509SignatureUtil.getSignatureName(certificate.getSignatureAlgorithm());
        } catch (Exception e) {
            throw new CertificateParsingException("cannot construct SigAlgName: " + e);
        }
    }

    private static byte[] createSigAlgParams(Certificate certificate) throws CertificateParsingException {
        try {
            ASN1Encodable parameters = certificate.getSignatureAlgorithm().getParameters();
            if (parameters == null) {
                return null;
            }
            return parameters.toASN1Primitive().getEncoded(ASN1Encoding.DER);
        } catch (Exception e) {
            throw new CertificateParsingException("cannot construct SigAlgParams: " + e);
        }
    }

    private X509CertificateInternal getInternalCertificate() {
        byte[] bArr;
        X509CertificateInternal x509CertificateInternal;
        synchronized (this.cacheLock) {
            if (this.internalCertificateValue != null) {
                return this.internalCertificateValue;
            }
        }
        try {
            bArr = getEncoded();
        } catch (CertificateEncodingException e) {
            bArr = null;
        }
        X509CertificateInternal x509CertificateInternal2 = new X509CertificateInternal(this.bcHelper, this.c, this.basicConstraints, this.keyUsage, this.sigAlgName, this.sigAlgParams, bArr);
        synchronized (this.cacheLock) {
            if (this.internalCertificateValue == null) {
                this.internalCertificateValue = x509CertificateInternal2;
            }
            x509CertificateInternal = this.internalCertificateValue;
        }
        return x509CertificateInternal;
    }

    @Override // org.bouncycastle.jcajce.provider.asymmetric.x509.X509CertificateImpl, java.security.cert.X509Certificate
    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
        long time = date.getTime();
        long[] validityValues2 = getValidityValues();
        if (time > validityValues2[1]) {
            throw new CertificateExpiredException("certificate expired on " + this.c.getEndDate().getTime());
        } else if (time < validityValues2[0]) {
            throw new CertificateNotYetValidException("certificate not valid till " + this.c.getStartDate().getTime());
        }
    }

    @Override // java.security.cert.Certificate, java.lang.Object
    public boolean equals(Object obj) {
        DERBitString signature;
        if (obj == this) {
            return true;
        }
        if (obj instanceof X509CertificateObject) {
            X509CertificateObject x509CertificateObject = (X509CertificateObject) obj;
            if (!this.hashValueSet || !x509CertificateObject.hashValueSet) {
                if ((this.internalCertificateValue == null || x509CertificateObject.internalCertificateValue == null) && (signature = this.c.getSignature()) != null && !signature.equals((ASN1Primitive) x509CertificateObject.c.getSignature())) {
                    return false;
                }
            } else if (this.hashValue != x509CertificateObject.hashValue) {
                return false;
            }
        }
        return getInternalCertificate().equals(obj);
    }

    @Override // org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier
    public ASN1Encodable getBagAttribute(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return this.attrCarrier.getBagAttribute(aSN1ObjectIdentifier);
    }

    @Override // org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier
    public Enumeration getBagAttributeKeys() {
        return this.attrCarrier.getBagAttributeKeys();
    }

    @Override // org.bouncycastle.jcajce.provider.asymmetric.x509.X509CertificateImpl, java.security.cert.X509Certificate
    public X500Principal getIssuerX500Principal() {
        X500Principal x500Principal;
        synchronized (this.cacheLock) {
            if (this.issuerValue != null) {
                return this.issuerValue;
            }
        }
        X500Principal issuerX500Principal = super.getIssuerX500Principal();
        synchronized (this.cacheLock) {
            if (this.issuerValue == null) {
                this.issuerValue = issuerX500Principal;
            }
            x500Principal = this.issuerValue;
        }
        return x500Principal;
    }

    @Override // org.bouncycastle.jcajce.provider.asymmetric.x509.X509CertificateImpl, java.security.cert.Certificate
    public PublicKey getPublicKey() {
        PublicKey publicKey;
        synchronized (this.cacheLock) {
            if (this.publicKeyValue != null) {
                return this.publicKeyValue;
            }
        }
        PublicKey publicKey2 = super.getPublicKey();
        if (publicKey2 == null) {
            return null;
        }
        synchronized (this.cacheLock) {
            if (this.publicKeyValue == null) {
                this.publicKeyValue = publicKey2;
            }
            publicKey = this.publicKeyValue;
        }
        return publicKey;
    }

    @Override // org.bouncycastle.jcajce.provider.asymmetric.x509.X509CertificateImpl, java.security.cert.X509Certificate
    public X500Principal getSubjectX500Principal() {
        X500Principal x500Principal;
        synchronized (this.cacheLock) {
            if (this.subjectValue != null) {
                return this.subjectValue;
            }
        }
        X500Principal subjectX500Principal = super.getSubjectX500Principal();
        synchronized (this.cacheLock) {
            if (this.subjectValue == null) {
                this.subjectValue = subjectX500Principal;
            }
            x500Principal = this.subjectValue;
        }
        return x500Principal;
    }

    public long[] getValidityValues() {
        long[] jArr;
        synchronized (this.cacheLock) {
            if (this.validityValues != null) {
                return this.validityValues;
            }
        }
        long[] jArr2 = {super.getNotBefore().getTime(), super.getNotAfter().getTime()};
        synchronized (this.cacheLock) {
            if (this.validityValues == null) {
                this.validityValues = jArr2;
            }
            jArr = this.validityValues;
        }
        return jArr;
    }

    @Override // java.security.cert.Certificate, java.lang.Object
    public int hashCode() {
        if (!this.hashValueSet) {
            this.hashValue = getInternalCertificate().hashCode();
            this.hashValueSet = true;
        }
        return this.hashValue;
    }

    public int originalHashCode() {
        try {
            byte[] encoded = getInternalCertificate().getEncoded();
            int i = 0;
            for (int i2 = 1; i2 < encoded.length; i2++) {
                i += encoded[i2] * i2;
            }
            return i;
        } catch (CertificateEncodingException e) {
            return 0;
        }
    }

    @Override // org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier
    public void setBagAttribute(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Encodable aSN1Encodable) {
        this.attrCarrier.setBagAttribute(aSN1ObjectIdentifier, aSN1Encodable);
    }
}
