package org.bouncycastle.asn1.crmf;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;

public class CertTemplateBuilder {
    private Extensions extensions;
    private X500Name issuer;
    private DERBitString issuerUID;
    private SubjectPublicKeyInfo publicKey;
    private ASN1Integer serialNumber;
    private AlgorithmIdentifier signingAlg;
    private X500Name subject;
    private DERBitString subjectUID;
    private OptionalValidity validity;
    private ASN1Integer version;

    private void addOptional(ASN1EncodableVector aSN1EncodableVector, int i, boolean z, ASN1Encodable aSN1Encodable) {
        if (aSN1Encodable != null) {
            aSN1EncodableVector.add(new DERTaggedObject(z, i, aSN1Encodable));
        }
    }

    public CertTemplate build() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(10);
        addOptional(aSN1EncodableVector, 0, false, this.version);
        addOptional(aSN1EncodableVector, 1, false, this.serialNumber);
        addOptional(aSN1EncodableVector, 2, false, this.signingAlg);
        addOptional(aSN1EncodableVector, 3, true, this.issuer);
        addOptional(aSN1EncodableVector, 4, false, this.validity);
        addOptional(aSN1EncodableVector, 5, true, this.subject);
        addOptional(aSN1EncodableVector, 6, false, this.publicKey);
        addOptional(aSN1EncodableVector, 7, false, this.issuerUID);
        addOptional(aSN1EncodableVector, 8, false, this.subjectUID);
        addOptional(aSN1EncodableVector, 9, false, this.extensions);
        return CertTemplate.getInstance(new DERSequence(aSN1EncodableVector));
    }

    public CertTemplateBuilder setExtensions(Extensions extensions2) {
        this.extensions = extensions2;
        return this;
    }

    public CertTemplateBuilder setExtensions(X509Extensions x509Extensions) {
        return setExtensions(Extensions.getInstance(x509Extensions));
    }

    public CertTemplateBuilder setIssuer(X500Name x500Name) {
        this.issuer = x500Name;
        return this;
    }

    public CertTemplateBuilder setIssuerUID(DERBitString dERBitString) {
        this.issuerUID = dERBitString;
        return this;
    }

    public CertTemplateBuilder setPublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        this.publicKey = subjectPublicKeyInfo;
        return this;
    }

    public CertTemplateBuilder setSerialNumber(ASN1Integer aSN1Integer) {
        this.serialNumber = aSN1Integer;
        return this;
    }

    public CertTemplateBuilder setSigningAlg(AlgorithmIdentifier algorithmIdentifier) {
        this.signingAlg = algorithmIdentifier;
        return this;
    }

    public CertTemplateBuilder setSubject(X500Name x500Name) {
        this.subject = x500Name;
        return this;
    }

    public CertTemplateBuilder setSubjectUID(DERBitString dERBitString) {
        this.subjectUID = dERBitString;
        return this;
    }

    public CertTemplateBuilder setValidity(OptionalValidity optionalValidity) {
        this.validity = optionalValidity;
        return this;
    }

    public CertTemplateBuilder setVersion(int i) {
        this.version = new ASN1Integer((long) i);
        return this;
    }
}
