package org.bouncycastle.asn1.pkcs;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Name;

public class CertificationRequestInfo extends ASN1Object {
    ASN1Set attributes;
    X500Name subject;
    SubjectPublicKeyInfo subjectPKInfo;
    ASN1Integer version;

    public CertificationRequestInfo(ASN1Sequence aSN1Sequence) {
        this.version = new ASN1Integer(0);
        this.attributes = null;
        this.version = (ASN1Integer) aSN1Sequence.getObjectAt(0);
        this.subject = X500Name.getInstance(aSN1Sequence.getObjectAt(1));
        this.subjectPKInfo = SubjectPublicKeyInfo.getInstance(aSN1Sequence.getObjectAt(2));
        if (aSN1Sequence.size() > 3) {
            this.attributes = ASN1Set.getInstance((ASN1TaggedObject) aSN1Sequence.getObjectAt(3), false);
        }
        validateAttributes(this.attributes);
        if (this.subject == null || this.version == null || this.subjectPKInfo == null) {
            throw new IllegalArgumentException("Not all mandatory fields set in CertificationRequestInfo generator.");
        }
    }

    public CertificationRequestInfo(X500Name x500Name, SubjectPublicKeyInfo subjectPublicKeyInfo, ASN1Set aSN1Set) {
        this.version = new ASN1Integer(0);
        this.attributes = null;
        if (x500Name == null || subjectPublicKeyInfo == null) {
            throw new IllegalArgumentException("Not all mandatory fields set in CertificationRequestInfo generator.");
        }
        validateAttributes(aSN1Set);
        this.subject = x500Name;
        this.subjectPKInfo = subjectPublicKeyInfo;
        this.attributes = aSN1Set;
    }

    public CertificationRequestInfo(X509Name x509Name, SubjectPublicKeyInfo subjectPublicKeyInfo, ASN1Set aSN1Set) {
        this(X500Name.getInstance(x509Name.toASN1Primitive()), subjectPublicKeyInfo, aSN1Set);
    }

    public static CertificationRequestInfo getInstance(Object obj) {
        if (obj instanceof CertificationRequestInfo) {
            return (CertificationRequestInfo) obj;
        }
        if (obj != null) {
            return new CertificationRequestInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    private static void validateAttributes(ASN1Set aSN1Set) {
        if (aSN1Set != null) {
            Enumeration objects = aSN1Set.getObjects();
            while (objects.hasMoreElements()) {
                Attribute instance = Attribute.getInstance(objects.nextElement());
                if (instance.getAttrType().equals(PKCSObjectIdentifiers.pkcs_9_at_challengePassword) && instance.getAttrValues().size() != 1) {
                    throw new IllegalArgumentException("challengePassword attribute must have one value");
                }
            }
        }
    }

    public ASN1Set getAttributes() {
        return this.attributes;
    }

    public X500Name getSubject() {
        return this.subject;
    }

    public SubjectPublicKeyInfo getSubjectPublicKeyInfo() {
        return this.subjectPKInfo;
    }

    public ASN1Integer getVersion() {
        return this.version;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.version);
        aSN1EncodableVector.add(this.subject);
        aSN1EncodableVector.add(this.subjectPKInfo);
        if (this.attributes != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, this.attributes));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
