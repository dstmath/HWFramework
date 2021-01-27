package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

public class Holder extends ASN1Object {
    public static final int V1_CERTIFICATE_HOLDER = 0;
    public static final int V2_CERTIFICATE_HOLDER = 1;
    IssuerSerial baseCertificateID;
    GeneralNames entityName;
    ObjectDigestInfo objectDigestInfo;
    private int version;

    private Holder(ASN1Sequence aSN1Sequence) {
        this.version = 1;
        if (aSN1Sequence.size() <= 3) {
            for (int i = 0; i != aSN1Sequence.size(); i++) {
                ASN1TaggedObject instance = ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(i));
                int tagNo = instance.getTagNo();
                if (tagNo == 0) {
                    this.baseCertificateID = IssuerSerial.getInstance(instance, false);
                } else if (tagNo == 1) {
                    this.entityName = GeneralNames.getInstance(instance, false);
                } else if (tagNo == 2) {
                    this.objectDigestInfo = ObjectDigestInfo.getInstance(instance, false);
                } else {
                    throw new IllegalArgumentException("unknown tag in Holder");
                }
            }
            this.version = 1;
            return;
        }
        throw new IllegalArgumentException("Bad sequence size: " + aSN1Sequence.size());
    }

    private Holder(ASN1TaggedObject aSN1TaggedObject) {
        this.version = 1;
        int tagNo = aSN1TaggedObject.getTagNo();
        if (tagNo == 0) {
            this.baseCertificateID = IssuerSerial.getInstance(aSN1TaggedObject, true);
        } else if (tagNo == 1) {
            this.entityName = GeneralNames.getInstance(aSN1TaggedObject, true);
        } else {
            throw new IllegalArgumentException("unknown tag in Holder");
        }
        this.version = 0;
    }

    public Holder(GeneralNames generalNames) {
        this(generalNames, 1);
    }

    public Holder(GeneralNames generalNames, int i) {
        this.version = 1;
        this.entityName = generalNames;
        this.version = i;
    }

    public Holder(IssuerSerial issuerSerial) {
        this(issuerSerial, 1);
    }

    public Holder(IssuerSerial issuerSerial, int i) {
        this.version = 1;
        this.baseCertificateID = issuerSerial;
        this.version = i;
    }

    public Holder(ObjectDigestInfo objectDigestInfo2) {
        this.version = 1;
        this.objectDigestInfo = objectDigestInfo2;
    }

    public static Holder getInstance(Object obj) {
        if (obj instanceof Holder) {
            return (Holder) obj;
        }
        if (obj instanceof ASN1TaggedObject) {
            return new Holder(ASN1TaggedObject.getInstance(obj));
        }
        if (obj != null) {
            return new Holder(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public IssuerSerial getBaseCertificateID() {
        return this.baseCertificateID;
    }

    public GeneralNames getEntityName() {
        return this.entityName;
    }

    public ObjectDigestInfo getObjectDigestInfo() {
        return this.objectDigestInfo;
    }

    public int getVersion() {
        return this.version;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        if (this.version == 1) {
            ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(3);
            IssuerSerial issuerSerial = this.baseCertificateID;
            if (issuerSerial != null) {
                aSN1EncodableVector.add(new DERTaggedObject(false, 0, issuerSerial));
            }
            GeneralNames generalNames = this.entityName;
            if (generalNames != null) {
                aSN1EncodableVector.add(new DERTaggedObject(false, 1, generalNames));
            }
            ObjectDigestInfo objectDigestInfo2 = this.objectDigestInfo;
            if (objectDigestInfo2 != null) {
                aSN1EncodableVector.add(new DERTaggedObject(false, 2, objectDigestInfo2));
            }
            return new DERSequence(aSN1EncodableVector);
        }
        GeneralNames generalNames2 = this.entityName;
        return generalNames2 != null ? new DERTaggedObject(true, 1, generalNames2) : new DERTaggedObject(true, 0, this.baseCertificateID);
    }
}
