package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.DERTaggedObject;
import com.android.org.bouncycastle.math.ec.ECFieldElement.F2m;

public class Holder extends ASN1Object {
    public static final int V1_CERTIFICATE_HOLDER = 0;
    public static final int V2_CERTIFICATE_HOLDER = 1;
    IssuerSerial baseCertificateID;
    GeneralNames entityName;
    ObjectDigestInfo objectDigestInfo;
    private int version;

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

    private Holder(ASN1TaggedObject tagObj) {
        this.version = V2_CERTIFICATE_HOLDER;
        switch (tagObj.getTagNo()) {
            case V1_CERTIFICATE_HOLDER /*0*/:
                this.baseCertificateID = IssuerSerial.getInstance(tagObj, true);
                break;
            case V2_CERTIFICATE_HOLDER /*1*/:
                this.entityName = GeneralNames.getInstance(tagObj, true);
                break;
            default:
                throw new IllegalArgumentException("unknown tag in Holder");
        }
        this.version = V1_CERTIFICATE_HOLDER;
    }

    private Holder(ASN1Sequence seq) {
        this.version = V2_CERTIFICATE_HOLDER;
        if (seq.size() > 3) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }
        for (int i = V1_CERTIFICATE_HOLDER; i != seq.size(); i += V2_CERTIFICATE_HOLDER) {
            ASN1TaggedObject tObj = ASN1TaggedObject.getInstance(seq.getObjectAt(i));
            switch (tObj.getTagNo()) {
                case V1_CERTIFICATE_HOLDER /*0*/:
                    this.baseCertificateID = IssuerSerial.getInstance(tObj, false);
                    break;
                case V2_CERTIFICATE_HOLDER /*1*/:
                    this.entityName = GeneralNames.getInstance(tObj, false);
                    break;
                case F2m.TPB /*2*/:
                    this.objectDigestInfo = ObjectDigestInfo.getInstance(tObj, false);
                    break;
                default:
                    throw new IllegalArgumentException("unknown tag in Holder");
            }
        }
        this.version = V2_CERTIFICATE_HOLDER;
    }

    public Holder(IssuerSerial baseCertificateID) {
        this(baseCertificateID, (int) V2_CERTIFICATE_HOLDER);
    }

    public Holder(IssuerSerial baseCertificateID, int version) {
        this.version = V2_CERTIFICATE_HOLDER;
        this.baseCertificateID = baseCertificateID;
        this.version = version;
    }

    public int getVersion() {
        return this.version;
    }

    public Holder(GeneralNames entityName) {
        this(entityName, (int) V2_CERTIFICATE_HOLDER);
    }

    public Holder(GeneralNames entityName, int version) {
        this.version = V2_CERTIFICATE_HOLDER;
        this.entityName = entityName;
        this.version = version;
    }

    public Holder(ObjectDigestInfo objectDigestInfo) {
        this.version = V2_CERTIFICATE_HOLDER;
        this.objectDigestInfo = objectDigestInfo;
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

    public ASN1Primitive toASN1Primitive() {
        if (this.version == V2_CERTIFICATE_HOLDER) {
            ASN1EncodableVector v = new ASN1EncodableVector();
            if (this.baseCertificateID != null) {
                v.add(new DERTaggedObject(false, V1_CERTIFICATE_HOLDER, this.baseCertificateID));
            }
            if (this.entityName != null) {
                v.add(new DERTaggedObject(false, V2_CERTIFICATE_HOLDER, this.entityName));
            }
            if (this.objectDigestInfo != null) {
                v.add(new DERTaggedObject(false, 2, this.objectDigestInfo));
            }
            return new DERSequence(v);
        } else if (this.entityName != null) {
            return new DERTaggedObject(true, V2_CERTIFICATE_HOLDER, this.entityName);
        } else {
            return new DERTaggedObject(true, V1_CERTIFICATE_HOLDER, this.baseCertificateID);
        }
    }
}
