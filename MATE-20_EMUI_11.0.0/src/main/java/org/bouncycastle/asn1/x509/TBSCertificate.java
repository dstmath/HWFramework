package org.bouncycastle.asn1.x509;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.Properties;

public class TBSCertificate extends ASN1Object {
    Time endDate;
    Extensions extensions;
    X500Name issuer;
    DERBitString issuerUniqueId;
    ASN1Sequence seq;
    ASN1Integer serialNumber;
    AlgorithmIdentifier signature;
    Time startDate;
    X500Name subject;
    SubjectPublicKeyInfo subjectPublicKeyInfo;
    DERBitString subjectUniqueId;
    ASN1Integer version;

    private TBSCertificate(ASN1Sequence aSN1Sequence) {
        int i;
        boolean z;
        boolean z2;
        this.seq = aSN1Sequence;
        if (aSN1Sequence.getObjectAt(0) instanceof ASN1TaggedObject) {
            this.version = ASN1Integer.getInstance((ASN1TaggedObject) aSN1Sequence.getObjectAt(0), true);
            i = 0;
        } else {
            this.version = new ASN1Integer(0);
            i = -1;
        }
        if (this.version.hasValue(BigInteger.valueOf(0))) {
            z = false;
            z2 = true;
        } else if (this.version.hasValue(BigInteger.valueOf(1))) {
            z2 = false;
            z = true;
        } else if (this.version.hasValue(BigInteger.valueOf(2))) {
            z2 = false;
            z = false;
        } else {
            throw new IllegalArgumentException("version number not recognised");
        }
        this.serialNumber = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(i + 1));
        this.signature = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(i + 2));
        this.issuer = X500Name.getInstance(aSN1Sequence.getObjectAt(i + 3));
        ASN1Sequence aSN1Sequence2 = (ASN1Sequence) aSN1Sequence.getObjectAt(i + 4);
        this.startDate = Time.getInstance(aSN1Sequence2.getObjectAt(0));
        this.endDate = Time.getInstance(aSN1Sequence2.getObjectAt(1));
        this.subject = X500Name.getInstance(aSN1Sequence.getObjectAt(i + 5));
        int i2 = i + 6;
        this.subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(aSN1Sequence.getObjectAt(i2));
        int size = (aSN1Sequence.size() - i2) - 1;
        if (size == 0 || !z2) {
            while (size > 0) {
                ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) aSN1Sequence.getObjectAt(i2 + size);
                int tagNo = aSN1TaggedObject.getTagNo();
                if (tagNo == 1) {
                    this.issuerUniqueId = DERBitString.getInstance(aSN1TaggedObject, false);
                } else if (tagNo == 2) {
                    this.subjectUniqueId = DERBitString.getInstance(aSN1TaggedObject, false);
                } else if (tagNo != 3) {
                    throw new IllegalArgumentException("Unknown tag encountered in structure: " + aSN1TaggedObject.getTagNo());
                } else if (!z) {
                    this.extensions = Extensions.getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, true));
                } else {
                    throw new IllegalArgumentException("version 2 certificate cannot contain extensions");
                }
                size--;
            }
            return;
        }
        throw new IllegalArgumentException("version 1 certificate contains extra data");
    }

    public static TBSCertificate getInstance(Object obj) {
        if (obj instanceof TBSCertificate) {
            return (TBSCertificate) obj;
        }
        if (obj != null) {
            return new TBSCertificate(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static TBSCertificate getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public Time getEndDate() {
        return this.endDate;
    }

    public Extensions getExtensions() {
        return this.extensions;
    }

    public X500Name getIssuer() {
        return this.issuer;
    }

    public DERBitString getIssuerUniqueId() {
        return this.issuerUniqueId;
    }

    public ASN1Integer getSerialNumber() {
        return this.serialNumber;
    }

    public AlgorithmIdentifier getSignature() {
        return this.signature;
    }

    public Time getStartDate() {
        return this.startDate;
    }

    public X500Name getSubject() {
        return this.subject;
    }

    public SubjectPublicKeyInfo getSubjectPublicKeyInfo() {
        return this.subjectPublicKeyInfo;
    }

    public DERBitString getSubjectUniqueId() {
        return this.subjectUniqueId;
    }

    public ASN1Integer getVersion() {
        return this.version;
    }

    public int getVersionNumber() {
        return this.version.intValueExact() + 1;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        if (Properties.getPropertyValue("org.bouncycastle.x509.allow_non-der_tbscert") == null) {
            return this.seq;
        }
        if (Properties.isOverrideSet("org.bouncycastle.x509.allow_non-der_tbscert")) {
            return this.seq;
        }
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        if (!this.version.hasValue(BigIntegers.ZERO)) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 0, this.version));
        }
        aSN1EncodableVector.add(this.serialNumber);
        aSN1EncodableVector.add(this.signature);
        aSN1EncodableVector.add(this.issuer);
        ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector(2);
        aSN1EncodableVector2.add(this.startDate);
        aSN1EncodableVector2.add(this.endDate);
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
        ASN1Encodable aSN1Encodable = this.subject;
        if (aSN1Encodable == null) {
            aSN1Encodable = new DERSequence();
        }
        aSN1EncodableVector.add(aSN1Encodable);
        aSN1EncodableVector.add(this.subjectPublicKeyInfo);
        DERBitString dERBitString = this.issuerUniqueId;
        if (dERBitString != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, dERBitString));
        }
        DERBitString dERBitString2 = this.subjectUniqueId;
        if (dERBitString2 != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 2, dERBitString2));
        }
        Extensions extensions2 = this.extensions;
        if (extensions2 != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 3, extensions2));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
