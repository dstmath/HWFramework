package org.bouncycastle.asn1.pkcs;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class SignerInfo extends ASN1Object {
    private ASN1Set authenticatedAttributes;
    private AlgorithmIdentifier digAlgorithm;
    private AlgorithmIdentifier digEncryptionAlgorithm;
    private ASN1OctetString encryptedDigest;
    private IssuerAndSerialNumber issuerAndSerialNumber;
    private ASN1Set unauthenticatedAttributes;
    private ASN1Integer version;

    public SignerInfo(ASN1Integer aSN1Integer, IssuerAndSerialNumber issuerAndSerialNumber2, AlgorithmIdentifier algorithmIdentifier, ASN1Set aSN1Set, AlgorithmIdentifier algorithmIdentifier2, ASN1OctetString aSN1OctetString, ASN1Set aSN1Set2) {
        this.version = aSN1Integer;
        this.issuerAndSerialNumber = issuerAndSerialNumber2;
        this.digAlgorithm = algorithmIdentifier;
        this.authenticatedAttributes = aSN1Set;
        this.digEncryptionAlgorithm = algorithmIdentifier2;
        this.encryptedDigest = aSN1OctetString;
        this.unauthenticatedAttributes = aSN1Set2;
    }

    public SignerInfo(ASN1Sequence aSN1Sequence) {
        Enumeration objects = aSN1Sequence.getObjects();
        this.version = (ASN1Integer) objects.nextElement();
        this.issuerAndSerialNumber = IssuerAndSerialNumber.getInstance(objects.nextElement());
        this.digAlgorithm = AlgorithmIdentifier.getInstance(objects.nextElement());
        Object nextElement = objects.nextElement();
        if (nextElement instanceof ASN1TaggedObject) {
            this.authenticatedAttributes = ASN1Set.getInstance((ASN1TaggedObject) nextElement, false);
            nextElement = objects.nextElement();
        } else {
            this.authenticatedAttributes = null;
        }
        this.digEncryptionAlgorithm = AlgorithmIdentifier.getInstance(nextElement);
        this.encryptedDigest = DEROctetString.getInstance(objects.nextElement());
        if (objects.hasMoreElements()) {
            this.unauthenticatedAttributes = ASN1Set.getInstance((ASN1TaggedObject) objects.nextElement(), false);
        } else {
            this.unauthenticatedAttributes = null;
        }
    }

    public static SignerInfo getInstance(Object obj) {
        if (obj instanceof SignerInfo) {
            return (SignerInfo) obj;
        }
        if (obj instanceof ASN1Sequence) {
            return new SignerInfo((ASN1Sequence) obj);
        }
        throw new IllegalArgumentException("unknown object in factory: " + obj.getClass().getName());
    }

    public ASN1Set getAuthenticatedAttributes() {
        return this.authenticatedAttributes;
    }

    public AlgorithmIdentifier getDigestAlgorithm() {
        return this.digAlgorithm;
    }

    public AlgorithmIdentifier getDigestEncryptionAlgorithm() {
        return this.digEncryptionAlgorithm;
    }

    public ASN1OctetString getEncryptedDigest() {
        return this.encryptedDigest;
    }

    public IssuerAndSerialNumber getIssuerAndSerialNumber() {
        return this.issuerAndSerialNumber;
    }

    public ASN1Set getUnauthenticatedAttributes() {
        return this.unauthenticatedAttributes;
    }

    public ASN1Integer getVersion() {
        return this.version;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.version);
        aSN1EncodableVector.add(this.issuerAndSerialNumber);
        aSN1EncodableVector.add(this.digAlgorithm);
        if (this.authenticatedAttributes != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, this.authenticatedAttributes));
        }
        aSN1EncodableVector.add(this.digEncryptionAlgorithm);
        aSN1EncodableVector.add(this.encryptedDigest);
        if (this.unauthenticatedAttributes != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, this.unauthenticatedAttributes));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
