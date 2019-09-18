package org.bouncycastle.asn1.dvcs;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.PolicyInformation;

public class DVCSCertInfo extends ASN1Object {
    private static final int DEFAULT_VERSION = 1;
    private static final int TAG_CERTS = 3;
    private static final int TAG_DV_STATUS = 0;
    private static final int TAG_POLICY = 1;
    private static final int TAG_REQ_SIGNATURE = 2;
    private ASN1Sequence certs;
    private DVCSRequestInformation dvReqInfo;
    private PKIStatusInfo dvStatus;
    private Extensions extensions;
    private DigestInfo messageImprint;
    private PolicyInformation policy;
    private ASN1Set reqSignature;
    private DVCSTime responseTime;
    private ASN1Integer serialNumber;
    private int version = 1;

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0054  */
    private DVCSCertInfo(ASN1Sequence aSN1Sequence) {
        int i;
        ASN1Encodable aSN1Encodable;
        int i2;
        ASN1Encodable objectAt = aSN1Sequence.getObjectAt(0);
        try {
            this.version = ASN1Integer.getInstance(objectAt).getValue().intValue();
            i = 2;
            try {
                aSN1Encodable = aSN1Sequence.getObjectAt(1);
            } catch (IllegalArgumentException e) {
                aSN1Encodable = objectAt;
                this.dvReqInfo = DVCSRequestInformation.getInstance(aSN1Encodable);
                int i3 = i + 1;
                this.messageImprint = DigestInfo.getInstance(aSN1Sequence.getObjectAt(i));
                int i4 = i3 + 1;
                this.serialNumber = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(i3));
                i2 = i4 + 1;
                this.responseTime = DVCSTime.getInstance(aSN1Sequence.getObjectAt(i4));
                while (i2 < aSN1Sequence.size()) {
                }
            }
        } catch (IllegalArgumentException e2) {
            i = 1;
            aSN1Encodable = objectAt;
            this.dvReqInfo = DVCSRequestInformation.getInstance(aSN1Encodable);
            int i32 = i + 1;
            this.messageImprint = DigestInfo.getInstance(aSN1Sequence.getObjectAt(i));
            int i42 = i32 + 1;
            this.serialNumber = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(i32));
            i2 = i42 + 1;
            this.responseTime = DVCSTime.getInstance(aSN1Sequence.getObjectAt(i42));
            while (i2 < aSN1Sequence.size()) {
            }
        }
        this.dvReqInfo = DVCSRequestInformation.getInstance(aSN1Encodable);
        int i322 = i + 1;
        this.messageImprint = DigestInfo.getInstance(aSN1Sequence.getObjectAt(i));
        int i422 = i322 + 1;
        this.serialNumber = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(i322));
        i2 = i422 + 1;
        this.responseTime = DVCSTime.getInstance(aSN1Sequence.getObjectAt(i422));
        while (i2 < aSN1Sequence.size()) {
            int i5 = i2 + 1;
            ASN1Encodable objectAt2 = aSN1Sequence.getObjectAt(i2);
            if (objectAt2 instanceof ASN1TaggedObject) {
                ASN1TaggedObject instance = ASN1TaggedObject.getInstance(objectAt2);
                switch (instance.getTagNo()) {
                    case 0:
                        this.dvStatus = PKIStatusInfo.getInstance(instance, false);
                        break;
                    case 1:
                        this.policy = PolicyInformation.getInstance(ASN1Sequence.getInstance(instance, false));
                        break;
                    case 2:
                        this.reqSignature = ASN1Set.getInstance(instance, false);
                        break;
                    case 3:
                        this.certs = ASN1Sequence.getInstance(instance, false);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown tag encountered: " + r3);
                }
            } else {
                try {
                    this.extensions = Extensions.getInstance(objectAt2);
                } catch (IllegalArgumentException e3) {
                }
            }
            i2 = i5;
        }
    }

    public DVCSCertInfo(DVCSRequestInformation dVCSRequestInformation, DigestInfo digestInfo, ASN1Integer aSN1Integer, DVCSTime dVCSTime) {
        this.dvReqInfo = dVCSRequestInformation;
        this.messageImprint = digestInfo;
        this.serialNumber = aSN1Integer;
        this.responseTime = dVCSTime;
    }

    public static DVCSCertInfo getInstance(Object obj) {
        if (obj instanceof DVCSCertInfo) {
            return (DVCSCertInfo) obj;
        }
        if (obj != null) {
            return new DVCSCertInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static DVCSCertInfo getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    private void setDvReqInfo(DVCSRequestInformation dVCSRequestInformation) {
        this.dvReqInfo = dVCSRequestInformation;
    }

    private void setMessageImprint(DigestInfo digestInfo) {
        this.messageImprint = digestInfo;
    }

    private void setVersion(int i) {
        this.version = i;
    }

    public TargetEtcChain[] getCerts() {
        if (this.certs != null) {
            return TargetEtcChain.arrayFromSequence(this.certs);
        }
        return null;
    }

    public DVCSRequestInformation getDvReqInfo() {
        return this.dvReqInfo;
    }

    public PKIStatusInfo getDvStatus() {
        return this.dvStatus;
    }

    public Extensions getExtensions() {
        return this.extensions;
    }

    public DigestInfo getMessageImprint() {
        return this.messageImprint;
    }

    public PolicyInformation getPolicy() {
        return this.policy;
    }

    public ASN1Set getReqSignature() {
        return this.reqSignature;
    }

    public DVCSTime getResponseTime() {
        return this.responseTime;
    }

    public ASN1Integer getSerialNumber() {
        return this.serialNumber;
    }

    public int getVersion() {
        return this.version;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        if (this.version != 1) {
            aSN1EncodableVector.add(new ASN1Integer((long) this.version));
        }
        aSN1EncodableVector.add(this.dvReqInfo);
        aSN1EncodableVector.add(this.messageImprint);
        aSN1EncodableVector.add(this.serialNumber);
        aSN1EncodableVector.add(this.responseTime);
        if (this.dvStatus != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, this.dvStatus));
        }
        if (this.policy != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, this.policy));
        }
        if (this.reqSignature != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 2, this.reqSignature));
        }
        if (this.certs != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 3, this.certs));
        }
        if (this.extensions != null) {
            aSN1EncodableVector.add(this.extensions);
        }
        return new DERSequence(aSN1EncodableVector);
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("DVCSCertInfo {\n");
        if (this.version != 1) {
            stringBuffer.append("version: " + this.version + "\n");
        }
        stringBuffer.append("dvReqInfo: " + this.dvReqInfo + "\n");
        stringBuffer.append("messageImprint: " + this.messageImprint + "\n");
        stringBuffer.append("serialNumber: " + this.serialNumber + "\n");
        stringBuffer.append("responseTime: " + this.responseTime + "\n");
        if (this.dvStatus != null) {
            stringBuffer.append("dvStatus: " + this.dvStatus + "\n");
        }
        if (this.policy != null) {
            stringBuffer.append("policy: " + this.policy + "\n");
        }
        if (this.reqSignature != null) {
            stringBuffer.append("reqSignature: " + this.reqSignature + "\n");
        }
        if (this.certs != null) {
            stringBuffer.append("certs: " + this.certs + "\n");
        }
        if (this.extensions != null) {
            stringBuffer.append("extensions: " + this.extensions + "\n");
        }
        stringBuffer.append("}\n");
        return stringBuffer.toString();
    }
}
