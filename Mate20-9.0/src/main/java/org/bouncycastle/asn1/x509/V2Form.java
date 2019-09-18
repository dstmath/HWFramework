package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

public class V2Form extends ASN1Object {
    IssuerSerial baseCertificateID;
    GeneralNames issuerName;
    ObjectDigestInfo objectDigestInfo;

    public V2Form(ASN1Sequence aSN1Sequence) {
        int i;
        if (aSN1Sequence.size() <= 3) {
            if (!(aSN1Sequence.getObjectAt(0) instanceof ASN1TaggedObject)) {
                this.issuerName = GeneralNames.getInstance(aSN1Sequence.getObjectAt(0));
                i = 1;
            } else {
                i = 0;
            }
            while (i != aSN1Sequence.size()) {
                ASN1TaggedObject instance = ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(i));
                if (instance.getTagNo() == 0) {
                    this.baseCertificateID = IssuerSerial.getInstance(instance, false);
                } else if (instance.getTagNo() == 1) {
                    this.objectDigestInfo = ObjectDigestInfo.getInstance(instance, false);
                } else {
                    throw new IllegalArgumentException("Bad tag number: " + instance.getTagNo());
                }
                i++;
            }
            return;
        }
        throw new IllegalArgumentException("Bad sequence size: " + aSN1Sequence.size());
    }

    public V2Form(GeneralNames generalNames) {
        this(generalNames, null, null);
    }

    public V2Form(GeneralNames generalNames, IssuerSerial issuerSerial) {
        this(generalNames, issuerSerial, null);
    }

    public V2Form(GeneralNames generalNames, IssuerSerial issuerSerial, ObjectDigestInfo objectDigestInfo2) {
        this.issuerName = generalNames;
        this.baseCertificateID = issuerSerial;
        this.objectDigestInfo = objectDigestInfo2;
    }

    public V2Form(GeneralNames generalNames, ObjectDigestInfo objectDigestInfo2) {
        this(generalNames, null, objectDigestInfo2);
    }

    public static V2Form getInstance(Object obj) {
        if (obj instanceof V2Form) {
            return (V2Form) obj;
        }
        if (obj != null) {
            return new V2Form(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static V2Form getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public IssuerSerial getBaseCertificateID() {
        return this.baseCertificateID;
    }

    public GeneralNames getIssuerName() {
        return this.issuerName;
    }

    public ObjectDigestInfo getObjectDigestInfo() {
        return this.objectDigestInfo;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        if (this.issuerName != null) {
            aSN1EncodableVector.add(this.issuerName);
        }
        if (this.baseCertificateID != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, this.baseCertificateID));
        }
        if (this.objectDigestInfo != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, this.objectDigestInfo));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
